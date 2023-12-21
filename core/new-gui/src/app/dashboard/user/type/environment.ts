export interface DashboardEnvironment {
  environment: Environment,
  isOwner: boolean | undefined,
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
