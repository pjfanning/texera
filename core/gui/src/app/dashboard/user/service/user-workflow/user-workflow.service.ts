import { Injectable } from "@angular/core";
import { Input, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { NzModalService } from "ng-zorro-antd/modal";
import { firstValueFrom, observable, of } from "rxjs";
import {
  DEFAULT_WORKFLOW_NAME,
  WorkflowPersistService,
} from "../../../../common/service/workflow-persist/workflow-persist.service";
import { DashboardEntry } from "../../type/dashboard-entry";
import { UserService } from "../../../../common/service/user/user.service";
import { untilDestroyed } from "@ngneat/until-destroy";
import { NotificationService } from "../../../../common/service/notification/notification.service";
import { WorkflowContent } from "../../../../common/type/workflow";
import { FileSaverService } from "../../service/user-file/file-saver.service";
import { FiltersComponent } from "../../component/filters/filters.component";
import { SearchResultsComponent } from "../../component/search-results/search-results.component";
import { SearchService } from "../../service/search.service";
import { SortMethod } from "../../type/sort-method";
import { isDefined } from "../../../../common/util/predicate";
import { UserProjectService } from "../../service/user-project/user-project.service";
import { map, mergeMap, tap } from "rxjs/operators";
import { Observable } from "rxjs";
import { observe } from "@ngx-formly/core/lib/utils";

/**
 * UserWorkflowService facilitates creating a new workflow from the dataset dashboard
 */


// Define OperatorConfig type
interface OperatorConfig {
  operatorID: string;
  operatorType: string;
  operatorVersion: string;
  operatorProperties: any; 
  inputPorts: any[];
  outputPorts: any[];
  showAdvanced: boolean;
  isDisabled: boolean;
  customDisplayName: string;
  dynamicInputPorts: boolean;
  dynamicOutputPorts: boolean;
  position: { x: number; y: number };
}

@Injectable({
  providedIn: "root",
})

export class UserWorkflowService {
  public ROUTER_WORKFLOW_BASE_URL = "workflow";
  private _searchResultsComponent?: SearchResultsComponent;
  @ViewChild(SearchResultsComponent) get searchResultsComponent(): SearchResultsComponent {
    if (this._searchResultsComponent) {
      return this._searchResultsComponent;
    }
    throw new Error("Property cannot be accessed before it is initialized.");
  }
  set searchResultsComponent(value: SearchResultsComponent) {
    this._searchResultsComponent = value;
  }
  private _filters?: FiltersComponent;
  @ViewChild(FiltersComponent) get filters(): FiltersComponent {
    if (this._filters) {
      return this._filters;
    }
    throw new Error("Property cannot be accessed before it is initialized.");
  }
  set filters(value: FiltersComponent) {
    value.masterFilterListChange.pipe(untilDestroyed(this)).subscribe({ next: () => this.search() });
    this._filters = value;
  }
  private masterFilterList: ReadonlyArray<string> | null = null;

  // receive input from parent components (UserProjectSection), if any
  @Input() public pid?: number = undefined;
  @Input() public accessLevel?: string = undefined;
  public sortMethod = SortMethod.EditTimeDesc;
  lastSortMethod: SortMethod | null = null;

  constructor(
    private userService: UserService,
    private workflowPersistService: WorkflowPersistService,
    private userProjectService: UserProjectService,
    private notificationService: NotificationService,
    private modalService: NzModalService,
    private router: Router,
    private fileSaverService: FileSaverService,
    private searchService: SearchService
  ) {}

  // Define the operator configurations
  operatorConfigs: { [key: string]: OperatorConfig } = {
    'jsonl-file-scan': {
      operatorID: 'JSONLFileScan-operator-4c65a712-19f4-44b6-9165-aa34af72c176',
      operatorType: 'JSONLFileScan',
      operatorVersion: 'acc831f5798dbb8df9234be1863fc9a292bc766a',
      operatorProperties: { fileEncoding: 'UTF_8' },
      inputPorts: [],
      outputPorts: [{ portID: 'output-0', displayName: '', allowMultiInputs: false, isDynamicPort: false }],
      showAdvanced: false,
      isDisabled: false,
      customDisplayName: 'JSONL File Scan',
      dynamicInputPorts: false,
      dynamicOutputPorts: false,
      position: { x: 437, y: 193 }
    },
    'csv-file-scan': {
      operatorID: 'CSVFileScan-operator-5289e695-5bfb-4eeb-abad-b12d81e67fdc',
      operatorType: 'CSVFileScan',
      operatorVersion: 'acc831f5798dbb8df9234be1863fc9a292bc766a',
      operatorProperties: { fileEncoding: 'UTF_8', customDelimiter: ',', hasHeader: true },
      inputPorts: [],
      outputPorts: [{ portID: 'output-0', displayName: '', allowMultiInputs: false, isDynamicPort: false }],
      showAdvanced: false,
      isDisabled: false,
      customDisplayName: 'CSV File Scan',
      dynamicInputPorts: false,
      dynamicOutputPorts: false,
      position: { x: 463, y: 325 }
    },
    'text-input': {
      operatorID: 'TextInput-operator-90fb9125-c2b3-414c-8eb5-6e8db6abaf93',
      operatorType: 'TextInput',
      operatorVersion: '9cfcd7a35153ceaaa14bc24d814014b2ddcc3e51',
      operatorProperties: { attributeType: 'string', attributeName: 'line' },
      inputPorts: [],
      outputPorts: [{ portID: 'output-0', displayName: '', allowMultiInputs: false, isDynamicPort: false }],
      showAdvanced: false,
      isDisabled: false,
      customDisplayName: 'Text Input',
      dynamicInputPorts: false,
      dynamicOutputPorts: false,
      position: { x: 509, y: 503 }
    },
    'csv-old-file-scan': {
      operatorID: 'CSVOldFileScan-operator-f2c6e3d9-bd4b-4024-94af-87e0707e07dd',
      operatorType: 'CSVOldFileScan',
      operatorVersion: 'acc831f5798dbb8df9234be1863fc9a292bc766a',
      operatorProperties: { fileEncoding: 'UTF_8', customDelimiter: ',', hasHeader: true },
      inputPorts: [],
      outputPorts: [{ portID: 'output-0', displayName: '', allowMultiInputs: false, isDynamicPort: false }],
      showAdvanced: false,
      isDisabled: false,
      customDisplayName: 'CSVOld File Scan',
      dynamicInputPorts: false,
      dynamicOutputPorts: false,
      position: { x: 699, y: 312 }
    },
    'file-scan': {
      operatorID: 'FileScan-operator-22269944-b337-4a50-a659-0581edc7e258',
      operatorType: 'FileScan',
      operatorVersion: '4cf7d13b82b8941ffeec37e96ffc5ff6d6c79095',
      operatorProperties: { encoding: 'UTF_8', extract: false, outputFileName: false, attributeType: 'string', attributeName: 'line' },
      inputPorts: [],
      outputPorts: [{ portID: 'output-0', displayName: '', allowMultiInputs: false, isDynamicPort: false }],
      showAdvanced: false,
      isDisabled: false,
      customDisplayName: ' File Scan',
      dynamicInputPorts: false,
      dynamicOutputPorts: false,
      position: { x: 750, y: 444 }
    }
  };
  

  public onClickCreateNewWorkflowFromDatasetDashboard(datasetFile: string, scanOption: string): Observable<number | undefined> {
    const operatorConfig = this.operatorConfigs[scanOption]; 
    if (!operatorConfig) {
      throw new Error('Invalid scan option selected');
    }
  
    operatorConfig.operatorProperties.fileName = datasetFile;
  
    const emptyWorkflowContent: WorkflowContent = {
      operators: [operatorConfig],
      commentBoxes: [],
      groups: [],
      links: [],
      operatorPositions: {
        [operatorConfig.operatorID]: { x: 474, y: 235 }
      }
    };
    
    let localPid = this.pid;

    return this.workflowPersistService.createWorkflow(emptyWorkflowContent, DEFAULT_WORKFLOW_NAME).pipe(
      tap(createdWorkflow => {
        if (!createdWorkflow.workflow.wid) {
          throw new Error("Workflow creation failed.");
        }
      }),
      mergeMap(createdWorkflow => {
        // Check if localPid is defined; if so, add the workflow to the project
        if (localPid) {
          return this.userProjectService.addWorkflowToProject(localPid, createdWorkflow.workflow.wid!).pipe(
            // Regardless of the project addition outcome, pass the wid downstream
            map(() => createdWorkflow.workflow.wid)
          );
        } else {
          // If there's no localPid, skip adding to the project and directly pass the wid downstream
          return of(createdWorkflow.workflow.wid);
        }
      })
      //untilDestroyed(this)
    );
  }

  /**
   * Searches workflows with keywords and filters given in the masterFilterList.
   * @returns
   */
  async search(forced: Boolean = false): Promise<void> {
    const sameList =
      this.masterFilterList !== null &&
      this.filters.masterFilterList.length === this.masterFilterList.length &&
      this.filters.masterFilterList.every((v, i) => v === this.masterFilterList![i]);
    if (!forced && sameList && this.sortMethod === this.lastSortMethod) {
      // If the filter lists are the same, do no make the same request again.
      return;
    }
    this.lastSortMethod = this.sortMethod;
    this.masterFilterList = this.filters.masterFilterList;
    let filterParams = this.filters.getSearchFilterParameters();
    if (isDefined(this.pid)) {
      // force the project id in the search query to be the current pid.
      filterParams.projectIds = [this.pid];
    }
    this.searchResultsComponent.reset(async (start, count) => {
      const results = await firstValueFrom(
        this.searchService.search(
          this.filters.getSearchKeywords(),
          filterParams,
          start,
          count,
          "workflow",
          this.sortMethod
        )
      );
      return {
        entries: results.results.map(i => {
          if (i.workflow) {
            return new DashboardEntry(i.workflow);
          } else {
            throw new Error("Unexpected type in SearchResult.");
          }
        }),
        more: results.more,
      };
    });
    await this.searchResultsComponent.loadMore();
  }
}
