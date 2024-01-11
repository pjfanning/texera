import { Dataset } from "../../../common/type/dataset";

export interface DashboardDataset {
  isOwner: boolean;
  ownerName: string;
  dataset: Dataset;
  accessPrivilege: "READ" | "WRITE" | "NONE";
}
