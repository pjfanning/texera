import { Dataset } from "../../../common/type/dataset";

export interface DashboardDataset {
  isOwner: boolean;
  ownerName: string | undefined;
  dataset: Dataset;
  accessLevel: string;
}
