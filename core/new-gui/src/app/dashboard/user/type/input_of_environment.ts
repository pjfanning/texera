export interface InputOfEnvironment {
    eid: number,
    did: number,
    versionDescriptor: string | undefined
}

export interface DashboardEnvironmentInput {
    inputName: string,
    input: InputOfEnvironment,
}
