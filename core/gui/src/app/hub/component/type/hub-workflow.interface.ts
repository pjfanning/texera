import { WorkflowContent } from "../../../common/type/workflow";

export interface HubWorkflow {
  name: string;
  description: string | undefined;
  wid: number | undefined;
  content: string | WorkflowContent;
  creationTime: number | undefined;
  lastModifiedTime: number | undefined;
}
