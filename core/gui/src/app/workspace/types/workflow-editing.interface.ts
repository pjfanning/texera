import { WorkflowContent } from "../../common/type/workflow";
import { OperatorMetadata } from "./operator-schema.interface";

export abstract class WorkflowEdition {
  abstract getDescription(): string;
}
export interface LinkDescriptor {
  sourceOpId: string;
  sourcePortId: string;
  targetPortId: string;
}
export interface LinkDescriptor {
  sourceOpId: string;
  sourcePortId: string;
  targetPortId: string;
}

export class AddOpAndLinksEdition extends WorkflowEdition {
  workflowContent: WorkflowContent;
  operatorType: string;
  operatorProperties: Map<string, any>;
  links: LinkDescriptor[];
  description: string;

  constructor(
    workflowContent: WorkflowContent,
    operatorType: string,
    operatorProperties: Map<string, any>,
    links: LinkDescriptor[],
    description: string
  ) {
    super(); // Call the base class constructor
    this.workflowContent = workflowContent;
    this.operatorType = operatorType;
    this.operatorProperties = operatorProperties;
    this.links = links;
    this.description = description;
  }

  getDescription(): string {
    return this.description;
  }
}
