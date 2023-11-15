export interface Execution {
  workflowName: string;
  workflowId: number;
  userName: string;
  userId: number;
  executionId: number;
  executionEnvironmentName: string;
  executionEnvironmentId: number;
  executionStatus: string;
  executionTime: number;
  executionName: string;
  startTime: number;
  endTime: number;
  access: boolean;
}
