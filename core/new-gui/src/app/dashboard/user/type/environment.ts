export interface DashboardEnvironment {
  environment: Environment,
  ownerName: string | undefined,
}

export interface Environment {
  eid: number,
  name: string,
  description: string,
  creationTime: number,
  inputs: string[],
  outputs: string[],
}
