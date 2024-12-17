export interface WorkflowComputingUnit {
  cuid: number | undefined;
  uid: number;
  name: string;
  creationTime: number | undefined;
  terminateTime: number | undefined;
}
