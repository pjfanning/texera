import {ExecutionIdentity, PhysicalOpIdentity, WorkflowIdentity} from "./proto/edu/uci/ics/amber/engine/common/virtualidentity";
import {PartitionInfoUnion} from "./partition-info";

export interface PhysicalOp {
  id: PhysicalOpIdentity;
  workflowId: WorkflowIdentity;
  executionId: ExecutionIdentity;
  parallelizable: boolean;
  partitionRequirement: Array<PartitionInfoUnion | null>;
  isOneToManyOp: boolean;
  suggestedWorkerNum: number | null;
}
