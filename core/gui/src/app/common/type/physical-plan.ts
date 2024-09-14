import {ExecutionIdentity, PhysicalOpIdentity, WorkflowIdentity} from "./proto/edu/uci/ics/amber/engine/common/virtualidentity";
import {PartitionInfoUnion} from "./partition-info";
import {PhysicalLink} from "./proto/edu/uci/ics/amber/engine/common/workflow";

export interface PhysicalOp {
  id: PhysicalOpIdentity;
  workflowId: WorkflowIdentity;
  executionId: ExecutionIdentity;
  parallelizable: boolean;
  // partitionRequirement: Array<PartitionInfoUnion | null>;
  isOneToManyOp: boolean;
  suggestedWorkerNum: number | null;
}

export interface PhysicalPlan {
  operators: Set<PhysicalOp>;
  links: Set<PhysicalLink>;
}
