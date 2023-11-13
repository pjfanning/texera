export interface DashboardEnvironment {
  environment: Environment,
  isOwner: string | undefined,
  inputs: string[] | undefined,
  outputs: string[] | undefined,
}

export interface Environment {
  eid: number | undefined,
  uid: number,
  name: string,
  description: string,
  creationTime: number | undefined,
}
