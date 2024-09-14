import {PhysicalPlan} from "../../common/type/physical-plan";
import {OperatorInputSchema} from "../service/workflow-compilation/workflow-compiling.service";

export enum CompilationState {
  Uninitialized = "Uninitialized",
  Succeeded = "Succeeded",
  Failed = "Failed",
}

export type CompilationStateInfo = Readonly<
  | {
    // indicates the compilation is successful
    state: CompilationState.Succeeded
    // physicalPlan compiled from current logical plan
    physicalPlan: PhysicalPlan;
    // a map from opId to InputSchema, used for autocompletion of schema
    operatorInputSchemaMap: Readonly<Record<string, OperatorInputSchema>>;
    // a map from opId to static error description, used for static error display
    operatorStaticErrorMap: Readonly<Record<string, string>>;
   }
  | {
    state: CompilationState.Uninitialized
    }
  | {
  state: CompilationState.Failed
  operatorInputSchemaMap: Readonly<Record<string, OperatorInputSchema>>;
  operatorStaticErrorMap: Readonly<Record<string, string>>;
}
>
