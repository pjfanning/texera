export interface WorkflowRuntimeStatistics {
  [key: string]: any;
  operatorId: string;
  timestamp: number;
  inputTupleCount: number;
  outputTupleCount: number;
  totalDataProcessingTime: number;
  totalControlProcessingTime: number;
  totalIdleTime: number;
  numberOfWorkers: number;
  status: number;
}
