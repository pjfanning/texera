import {
  ChangeDetectorRef,
  Component,
  ComponentFactoryResolver,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from "@angular/core";
import { ExecuteWorkflowService } from "../../../service/execute-workflow/execute-workflow.service";
import { Subject } from "rxjs";
import { Form, FormGroup } from "@angular/forms";
import { FormlyFieldConfig, FormlyFormOptions } from "@ngx-formly/core";
import Ajv from "ajv";
import { FormlyJsonschema } from "@ngx-formly/core/json-schema";
import { WorkflowActionService } from "../../../service/workflow-graph/model/workflow-action.service";
import { cloneDeep, isEqual } from "lodash-es";
import { CustomJSONSchema7, HideType, hideTypes } from "../../../types/custom-json-schema.interface";
import { isDefined } from "../../../../common/util/predicate";
import { ExecutionState } from "src/app/workspace/types/execute-workflow.interface";
import { DynamicSchemaService } from "../../../service/dynamic-schema/dynamic-schema.service";
import {
  OperatorInputSchema,
  SchemaAttribute,
  SchemaPropagationService,
} from "../../../service/dynamic-schema/schema-propagation/schema-propagation.service";
import {
  createOutputFormChangeEventStream,
  createShouldHideFieldFunc,
  setChildTypeDependency,
  setHideExpression,
} from "src/app/common/formly/formly-utils";
import {
  TYPE_CASTING_OPERATOR_TYPE,
  TypeCastingDisplayComponent,
} from "../typecasting-display/type-casting-display.component";
import { DynamicComponentConfig } from "../../../../common/type/dynamic-component-config";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { filter } from "rxjs/operators";
import { NotificationService } from "../../../../common/service/notification/notification.service";
import { PresetWrapperComponent } from "src/app/common/formly/preset-wrapper/preset-wrapper.component";
import { environment } from "src/environments/environment";
import { WorkflowCollabService } from "../../../service/workflow-collab/workflow-collab.service";
import { WorkflowVersionService } from "../../../../dashboard/service/workflow-version/workflow-version.service";
import { OperatorSchema } from "src/app/workspace/types/operator-schema.interface";
import { JSONSchema7Type } from "json-schema";
// @ts-ignore
import { levenshtein } from "edit-distance";
import { OperatorPredicate } from "src/app/workspace/types/workflow-common.interface";

export type PropertyDisplayComponent = TypeCastingDisplayComponent;

export type PropertyDisplayComponentConfig = DynamicComponentConfig<PropertyDisplayComponent>;

/**
 * Property Editor uses JSON Schema to automatically generate the form from the JSON Schema of an operator.
 * For example, the JSON Schema of Sentiment Analysis could be:
 *  'properties': {
 *    'attribute': { 'type': 'string' },
 *    'resultAttribute': { 'type': 'string' }
 *  }
 * The automatically generated form will show two input boxes, one titled 'attribute' and one titled 'resultAttribute'.
 * More examples of the operator JSON schema can be found in `mock-operator-metadata.data.ts`
 * More about JSON Schema: Understanding JSON Schema - https://spacetelescope.github.io/understanding-json-schema/
 *
 * OperatorMetadataService will fetch metadata about the operators, which includes the JSON Schema, from the backend.
 *
 * We use library `@ngx-formly` to generate form from json schema
 * https://github.com/ngx-formly/ngx-formly
 */
@UntilDestroy()
@Component({
  selector: "texera-formly-form-frame",
  templateUrl: "./operator-property-edit-frame.component.html",
  styleUrls: ["./operator-property-edit-frame.component.scss"],
})
export class OperatorPropertyEditFrameComponent implements OnInit, OnChanges, OnDestroy {
  @Input() currentOperatorId?: string;

  // re-declare enum for angular template to access it
  readonly ExecutionState = ExecutionState;

  // whether the editor can be edited
  interactive: boolean = this.evaluateInteractivity();

  // the source event stream of form change triggered by library at each user input
  sourceFormChangeEventStream = new Subject<Record<string, unknown>>();

  // the output form change event stream after debounce time and filtering out values
  operatorPropertyChangeStream = createOutputFormChangeEventStream(this.sourceFormChangeEventStream, data =>
    this.checkOperatorProperty(data)
  );

  // inputs and two-way bindings to formly component
  formlyFormGroup: FormGroup | undefined;
  formData: any;
  formlyOptions: FormlyFormOptions = {};
  formlyFields: FormlyFieldConfig[] | undefined;
  formTitle: string | undefined;

  // The field name and its css style to be overridden, e.g., for showing the diff between two workflows.
  // example: new Map([
  //     ["attribute", "outline: 3px solid green; transition: 0.3s ease-in-out outline;"],
  //     ["condition", "background: red; border-color: red;"],
  //   ]);
  fieldStyleOverride: Map<String, String> = new Map([]);

  editingTitle: boolean = false;

  // used to fill in default values in json schema to initialize new operator
  ajv = new Ajv({ useDefaults: true, strict: false });

  // for display component of some extra information
  extraDisplayComponentConfig?: PropertyDisplayComponentConfig;

  // used to tear down subscriptions that takeUntil(teardownObservable)
  private teardownObservable: Subject<void> = new Subject();
  public lockGranted: boolean = true;

  constructor(
    private formlyJsonschema: FormlyJsonschema,
    private workflowActionService: WorkflowActionService,
    public executeWorkflowService: ExecuteWorkflowService,
    private dynamicSchemaService: DynamicSchemaService,
    private schemaPropagationService: SchemaPropagationService,
    private notificationService: NotificationService,
    private workflowCollabService: WorkflowCollabService,
    private changeDetectorRef: ChangeDetectorRef,
    private workflowVersionService: WorkflowVersionService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.currentOperatorId = changes.currentOperatorId?.currentValue;
    if (!this.currentOperatorId) {
      return;
    }
    this.rerenderEditorForm();
  }

  switchDisplayComponent(targetConfig?: PropertyDisplayComponentConfig) {
    if (
      this.extraDisplayComponentConfig?.component === targetConfig?.component &&
      this.extraDisplayComponentConfig?.component === targetConfig?.componentInputs
    ) {
      return;
    }

    this.extraDisplayComponentConfig = targetConfig;
  }

  ngOnInit(): void {
    // listen to the autocomplete event, remove invalid properties, and update the schema displayed on the form
    this.registerOperatorSchemaChangeHandler();

    // when the operator's property is updated via program instead of user updating the json schema form,
    //  this observable will be responsible in handling these events.
    this.registerOperatorPropertyChangeHandler();

    // handle the form change event on the user interface to actually set the operator property
    this.registerOnFormChangeHandler();

    this.registerDisableEditorInteractivityHandler();

    this.registerOperatorDisplayNameChangeHandler();

    this.registerLockChangeHandler();
  }

  async ngOnDestroy() {
    // await this.checkAndSavePreset();
    this.teardownObservable.complete();
  }

  /**
   * Callback function provided to the Angular Json Schema Form library,
   *  whenever the form data is changed, this function is called.
   * It only serves as a bridge from a callback function to RxJS Observable
   * @param event
   */
  onFormChanges(event: Record<string, unknown>): void {
    this.sourceFormChangeEventStream.next(event);
  }

  /**
   * Changes the property editor to use the new operator data.
   * Sets all the data needed by the json schema form and displays the form.
   */
  rerenderEditorForm(): void {
    if (!this.currentOperatorId) {
      return;
    }
    const operator = this.workflowActionService.getTexeraGraph().getOperator(this.currentOperatorId);
    // set the operator data needed
    const currentOperatorSchema = this.dynamicSchemaService.getDynamicSchema(this.currentOperatorId);
    this.setFormlyFormBinding(currentOperatorSchema.jsonSchema);
    this.formTitle = operator.customDisplayName ?? currentOperatorSchema.additionalMetadata.userFriendlyName;

    /**
     * Important: make a deep copy of the initial property data object.
     * Prevent the form directly changes the value in the texera graph without going through workflow action service.
     */
    this.formData = cloneDeep(operator.operatorProperties);

    // use ajv to initialize the default value to data according to schema, see https://ajv.js.org/#assigning-defaults
    // WorkflowUtil service also makes sure that the default values are filled in when operator is added from the UI
    // However, we perform an addition check for the following reasons:
    // 1. the operator might be added not directly from the UI, which violates the precondition
    // 2. the schema might change, which specifies a new default value
    // 3. formly doesn't emit change event when it fills in default value, causing an inconsistency between component and service
    this.ajv.validate(currentOperatorSchema, this.formData);

    // manually trigger a form change event because default value might be filled in
    this.onFormChanges(this.formData);

    if (
      this.workflowActionService
        .getTexeraGraph()
        .getOperator(this.currentOperatorId)
        .operatorType.includes(TYPE_CASTING_OPERATOR_TYPE)
    ) {
      this.switchDisplayComponent({
        component: TypeCastingDisplayComponent,
        componentInputs: { currentOperatorId: this.currentOperatorId },
      });
    } else {
      this.switchDisplayComponent(undefined);
    }
    // execute set interactivity immediately in another task because of a formly bug
    // whenever the form model is changed, formly can only disable it after the UI is rendered
    setTimeout(() => {
      const interactive = this.evaluateInteractivity();
      this.setInteractivity(interactive);
      this.changeDetectorRef.detectChanges();
    }, 0);
  }

  evaluateInteractivity(): boolean {
    return this.workflowActionService.checkWorkflowModificationEnabled();
  }

  setInteractivity(interactive: boolean) {
    this.interactive = interactive;
    if (this.formlyFormGroup !== undefined) {
      if (this.interactive) {
        this.formlyFormGroup.enable();
      } else {
        this.formlyFormGroup.disable();
      }
    }
  }

  checkOperatorProperty(formData: object): boolean {
    // check if the component is displaying operator property
    if (this.currentOperatorId === undefined) {
      return false;
    }
    // check if the operator still exists, it might be deleted during debounce time
    const operator = this.workflowActionService.getTexeraGraph().getOperator(this.currentOperatorId);
    if (!operator) {
      return false;
    }
    // only emit change event if the form data actually changes
    return !isEqual(formData, operator.operatorProperties);
  }

  /**
   * This method handles the schema change event from autocomplete. It will get the new schema
   *  propagated from autocomplete and check if the operators' properties that users input
   *  previously are still valid. If invalid, it will remove these fields and triggered an event so
   *  that the user interface will be updated through registerOperatorPropertyChangeHandler() method.
   *
   * If the operator that experiences schema changed is the same as the operator that is currently
   *  displaying on the property panel, this handler will update the current operator schema
   *  to the new schema.
   */
  registerOperatorSchemaChangeHandler(): void {
    this.dynamicSchemaService
      .getOperatorDynamicSchemaChangedStream()
      .pipe(untilDestroyed(this))
      .subscribe(vals => {
        this.updateOperatorPropertiesOnSchemaChange(vals.operatorID, vals.oldSchema, vals.newSchema, vals.oldInputSchema, vals.newInputSchema);
        if (vals.operatorID === this.currentOperatorId)
          this.rerenderEditorForm();
      });

    // this.dynamicSchemaService
    //   .getOperatorDynamicSchemaChangedStream()
    //   .pipe(filter(({ operatorID }) => operatorID === this.currentOperatorId))
    //   .pipe(untilDestroyed(this))
    //   .subscribe(_ => this.rerenderEditorForm());
  }

  updateOperatorPropertiesOnSchemaChange(
    operatorID: string,
    oldSchema: OperatorSchema,
    newSchema: OperatorSchema,
    oldInputSchema: OperatorInputSchema,
    newInputSchema: OperatorInputSchema,
  ): void {
    const operator = this.workflowActionService.getTexeraGraph().getOperator(operatorID);
    console.log("updateOperatorPropertiesOnSchemaChange: op", operator, "oldSchema", oldSchema, "newSchema", newSchema);
    for (const property in oldSchema.jsonSchema.properties) {
      console.log(property)
      if (
        (oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).autofill === "attributeNameList" || 
        (oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).autofill === "attributeNameReorderList") {
        // TODO: Simplify the checking
        if (
          !(
            oldSchema.jsonSchema.properties &&
            newSchema.jsonSchema.properties &&
            ((oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).items as CustomJSONSchema7).enum &&
            ((newSchema.jsonSchema.properties[property] as CustomJSONSchema7).items as CustomJSONSchema7).enum &&
            oldSchema.jsonSchema.autofillAttributeOnPort &&
            newSchema.jsonSchema.autofillAttributeOnPort
          )
        ){
          // this.workflowActionService.setOperatorProperty(operatorID, { attributes: [] });
          return;
        }
        let newAttributes: string[] = [];

        let oldEnumList = {};
        if (!oldInputSchema[oldSchema.jsonSchema.autofillAttributeOnPort]) return;
        for (const attribute in (oldInputSchema[oldSchema.jsonSchema.autofillAttributeOnPort] as ReadonlyArray<SchemaAttribute>)) {
          oldEnumList = {
            ...oldEnumList,
            [attribute.attributeType]: null
          }
        }
        const newEnumList = ((newSchema.jsonSchema.properties[property] as CustomJSONSchema7).items as CustomJSONSchema7)
          .enum as string[];
        const attributeMapping = levenshtein(
          oldEnumList,
          newEnumList,
          (_: any) => 1,
          (_: any) => 1,
          (a: string, b: string) => (a !== b ? 1 : 0)
        );
        const oldAttributes = operator.operatorProperties[property];
        if (!oldAttributes) return;
        
        // console.log(attributes, attributeMapping.pairs());
        const pairs = attributeMapping.pairs();
        // pair = [old attribute name, new attribute name]
        for (var i = 0; i < pairs.length; ++i) {
          const pair = pairs[i];
          if (!pair[0] || (oldAttributes.includes(pair[0]) && pair[1])) {
            newAttributes.push(pair[1]);
          }
        }
        this.workflowActionService.setOperatorProperty(operatorID, { ...this.workflowActionService.getTexeraGraph().getOperator(operatorID).operatorProperties, [property]: newAttributes });
      } else if ((oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).autofill === "attributeName") {
        if (
          !(
            oldSchema.jsonSchema.properties &&
            newSchema.jsonSchema.properties &&
            (oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).enum &&
            (newSchema.jsonSchema.properties[property] as CustomJSONSchema7).enum
          )
        ){
          console.log("returned", (oldSchema.jsonSchema.properties[property] as CustomJSONSchema7).enum)
          return;
        }
        const oldEnumList = (oldSchema.jsonSchema.properties[property] as CustomJSONSchema7)
          .enum as string[];
        const newEnumList = (newSchema.jsonSchema.properties[property] as CustomJSONSchema7)
          .enum as string[];
        const attributeMapping = levenshtein(
          oldEnumList,
          newEnumList,
          (_: any) => 1,
          (_: any) => 1,
          (a: string, b: string) => (a !== b ? 1 : 0)
        );
        const oldAttribute: string = operator.operatorProperties[property];
        if (!oldAttribute) return;
        const pairs = attributeMapping.pairs();
        console.log("on prop:", property, "the pairing is ", pairs)
        let newAttribute = undefined;
        // pair = [old attribute name, new attribute name]
        for (var i = 0; i < pairs.length; ++i) {
          const pair = pairs[i];
          if (oldAttribute === pair[0] && pair[1]) {
            newAttribute = pair[1];
          }
        }
        this.workflowActionService.setOperatorProperty(operatorID, { ...this.workflowActionService.getTexeraGraph().getOperator(operatorID).operatorProperties, [property]: newAttribute });
        
      }
      console.log("after update", this.workflowActionService.getTexeraGraph().getOperator(operatorID));
    }
  }

  /**
   * This method captures the change in operator's property via program instead of user updating the
   *  json schema form in the user interface.
   *
   * For instance, when the input doesn't matching the new json schema and the UI needs to remove the
   *  invalid fields, this form will capture those events.
   */
  registerOperatorPropertyChangeHandler(): void {
    this.workflowActionService
      .getTexeraGraph()
      .getOperatorPropertyChangeStream()
      .pipe(
        filter(_ => this.currentOperatorId !== undefined),
        filter(operatorChanged => operatorChanged.operator.operatorID === this.currentOperatorId),
        filter(operatorChanged => !isEqual(this.formData, operatorChanged.operator.operatorProperties))
      )
      // .pipe(untilDestroyed(this))
      // Commented to prevent rollback of input data
      // TODO: check any potential effect of commenting this line
      .subscribe(operatorChanged => (this.formData = cloneDeep(operatorChanged.operator.operatorProperties)));
  }

  /**
   * This method handles the form change event and set the operator property
   *  in the texera graph.
   */
  registerOnFormChangeHandler(): void {
    this.operatorPropertyChangeStream.pipe(untilDestroyed(this)).subscribe(formData => {
      // set the operator property to be the new form data
      if (this.currentOperatorId) {
        this.workflowActionService.setOperatorProperty(this.currentOperatorId, cloneDeep(formData));
      }
    });
  }

  registerDisableEditorInteractivityHandler(): void {
    this.workflowActionService
      .getWorkflowModificationEnabledStream()
      .pipe(untilDestroyed(this))
      .subscribe(canModify => {
        if (this.currentOperatorId) {
          const interactive = this.evaluateInteractivity();
          this.setInteractivity(interactive);
          this.changeDetectorRef.detectChanges();
        }
      });
  }

  private registerOperatorDisplayNameChangeHandler(): void {
    this.workflowActionService
      .getTexeraGraph()
      .getOperatorDisplayNameChangedStream()
      .pipe(untilDestroyed(this))
      .subscribe(({ operatorID, newDisplayName }) => {
        if (operatorID === this.currentOperatorId) this.formTitle = newDisplayName;
      });
  }

  private registerLockChangeHandler(): void {
    this.workflowCollabService
      .getLockStatusStream()
      .pipe(untilDestroyed(this))
      .subscribe((lockGranted: boolean) => {
        this.lockGranted = lockGranted;
        this.changeDetectorRef.detectChanges();
      });
  }

  setFormlyFormBinding(schema: CustomJSONSchema7) {
    var operatorPropertyDiff = this.workflowVersionService.operatorPropertyDiff;
    if (this.currentOperatorId != undefined && operatorPropertyDiff[this.currentOperatorId] != undefined) {
      this.fieldStyleOverride = operatorPropertyDiff[this.currentOperatorId];
    }
    // intercept JsonSchema -> FormlySchema process, adding custom options
    // this requires a one-to-one mapping.
    // for relational custom options, have to do it after FormlySchema is generated.
    const jsonSchemaMapIntercept = (
      mappedField: FormlyFieldConfig,
      mapSource: CustomJSONSchema7
    ): FormlyFieldConfig => {
      // apply the overridden css style if applicable
      mappedField.expressionProperties = {
        "templateOptions.attributes": () => {
          if (
            isDefined(mappedField) &&
            typeof mappedField.key === "string" &&
            this.fieldStyleOverride.has(mappedField.key)
          ) {
            return { style: this.fieldStyleOverride.get(mappedField.key) };
          } else {
            return {};
          }
        },
      };

      // conditionally hide the field according to the schema
      if (
        isDefined(mapSource.hideExpectedValue) &&
        isDefined(mapSource.hideTarget) &&
        isDefined(mapSource.hideType) &&
        hideTypes.includes(mapSource.hideType)
      ) {
        mappedField.hideExpression = createShouldHideFieldFunc(
          mapSource.hideTarget,
          mapSource.hideType,
          mapSource.hideExpectedValue
        );
      }

      // if the title is python script (for Python UDF), then make this field a custom template 'codearea'
      if (mapSource?.description?.toLowerCase() === "input your code here") {
        if (mappedField.type) {
          mappedField.type = "codearea";
        }
      }
      // if presetService is ready and operator property allows presets, setup formly field to display presets
      if (
        environment.userSystemEnabled &&
        environment.userPresetEnabled &&
        mapSource["enable-presets"] !== undefined &&
        this.currentOperatorId !== undefined
      ) {
        PresetWrapperComponent.setupFieldConfig(
          mappedField,
          "operator",
          this.workflowActionService.getTexeraGraph().getOperator(this.currentOperatorId).operatorType,
          this.currentOperatorId
        );
      }
      // if we need to reorder the array, a custom template `dragablearray` is used
      if (mapSource?.autofill === "attributeNameReorderList") {
        if (mappedField.type) {
          mappedField.type = "draggablearray";
        }
        // sort according to the reordered list
        if (this.formData && this.formData.attributes && mappedField.templateOptions?.options) {
          const options = mappedField.templateOptions?.options as any[]; // safe to do so?
          for (var i = 0; i < this.formData.attributes.length; ++i) {
            for (var j = 0; j < options.length; ++j) {
              if (options[j].label === this.formData.attributes[i]) {
                const temp = options[j];
                options[j] = options[i];
                options[i] = temp;
              }
            }
          }
        }
      }
      return mappedField;
    };

    this.formlyFormGroup = new FormGroup({});
    this.formlyOptions = {};
    // convert the json schema to formly config, pass a copy because formly mutates the schema object
    const field = this.formlyJsonschema.toFieldConfig(cloneDeep(schema), {
      map: jsonSchemaMapIntercept,
    });
    field.hooks = {
      onInit: fieldConfig => {
        if (!this.interactive) {
          fieldConfig?.form?.disable();
        }
      },
    };

    const schemaProperties = schema.properties;
    const fields = field.fieldGroup;

    // adding custom options, relational N-to-M mapping.
    if (schemaProperties && fields) {
      Object.entries(schemaProperties).forEach(([propertyName, propertyValue]) => {
        if (typeof propertyValue === "boolean") {
          return;
        }
        if (propertyValue.toggleHidden) {
          setHideExpression(propertyValue.toggleHidden, fields, propertyName);
        }

        if (propertyValue.dependOn) {
          if (isDefined(this.currentOperatorId)) {
            const attributes: ReadonlyArray<ReadonlyArray<SchemaAttribute> | null> | undefined =
              this.schemaPropagationService.getOperatorInputSchema(this.currentOperatorId);
            setChildTypeDependency(attributes, propertyValue.dependOn, fields, propertyName);
          }
        }
      });
    }

    this.formlyFields = fields;
  }

  allowModifyOperatorLogic(): void {
    this.setInteractivity(true);
  }

  confirmModifyOperatorLogic(): void {
    if (this.currentOperatorId) {
      try {
        this.executeWorkflowService.modifyOperatorLogic(this.currentOperatorId);
        this.setInteractivity(false);
      } catch (e) {
        this.notificationService.error(e);
      }
    }
  }

  confirmChangeOperatorCustomName(customDisplayName: string) {
    if (this.currentOperatorId) {
      const currentOperatorSchema = this.dynamicSchemaService.getDynamicSchema(this.currentOperatorId);
      const userFriendlyName = currentOperatorSchema.additionalMetadata.userFriendlyName;
      // fall back to the original userFriendlyName if no valid name is provided
      const newDisplayName =
        customDisplayName === "" || customDisplayName === undefined
          ? currentOperatorSchema.additionalMetadata.userFriendlyName
          : customDisplayName;
      this.workflowActionService.setOperatorCustomName(this.currentOperatorId, newDisplayName, userFriendlyName);
      this.formTitle = newDisplayName;
    }

    this.editingTitle = false;
  }
}
