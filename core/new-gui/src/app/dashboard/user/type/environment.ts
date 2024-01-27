export interface DashboardEnvironment {
  environment: Environment,
  isEditable: boolean | undefined,
  datasets: string[] | undefined,
  outputs: string[] | undefined,
}

export interface Environment {
  eid: number | undefined,
  uid: number | undefined,
  name: string,
  description: string,
  creationTime: number | undefined,
}

export interface DatasetOfEnvironment {
  did: number;
  eid: number;
  dvid: number;
}
