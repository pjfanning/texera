/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated;


import edu.uci.ics.texera.web.model.jooq.generated.tables.Environment;
import edu.uci.ics.texera.web.model.jooq.generated.tables.File;
import edu.uci.ics.texera.web.model.jooq.generated.tables.FileOfProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.FileOfWorkflow;
import edu.uci.ics.texera.web.model.jooq.generated.tables.InputOfEnvironment;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Project;
import edu.uci.ics.texera.web.model.jooq.generated.tables.ProjectUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.PublicProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.User;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserConfig;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserFileAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Workflow;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowExecutions;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowRuntimeStatistics;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowVersion;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.EnvironmentRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.FileOfProjectRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.FileOfWorkflowRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.FileRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.InputOfEnvironmentRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.ProjectRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.ProjectUserAccessRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.PublicProjectRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserConfigRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserFileAccessRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowExecutionsRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowOfProjectRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowOfUserRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowRuntimeStatisticsRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowUserAccessRecord;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowVersionRecord;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>texera_db</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<EnvironmentRecord, UInteger> IDENTITY_ENVIRONMENT = Identities0.IDENTITY_ENVIRONMENT;
    public static final Identity<FileRecord, UInteger> IDENTITY_FILE = Identities0.IDENTITY_FILE;
    public static final Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Identities0.IDENTITY_PROJECT;
    public static final Identity<UserRecord, UInteger> IDENTITY_USER = Identities0.IDENTITY_USER;
    public static final Identity<WorkflowRecord, UInteger> IDENTITY_WORKFLOW = Identities0.IDENTITY_WORKFLOW;
    public static final Identity<WorkflowExecutionsRecord, UInteger> IDENTITY_WORKFLOW_EXECUTIONS = Identities0.IDENTITY_WORKFLOW_EXECUTIONS;
    public static final Identity<WorkflowVersionRecord, UInteger> IDENTITY_WORKFLOW_VERSION = Identities0.IDENTITY_WORKFLOW_VERSION;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<EnvironmentRecord> KEY_ENVIRONMENT_PRIMARY = UniqueKeys0.KEY_ENVIRONMENT_PRIMARY;
    public static final UniqueKey<FileRecord> KEY_FILE_OWNER_UID = UniqueKeys0.KEY_FILE_OWNER_UID;
    public static final UniqueKey<FileRecord> KEY_FILE_PRIMARY = UniqueKeys0.KEY_FILE_PRIMARY;
    public static final UniqueKey<FileOfProjectRecord> KEY_FILE_OF_PROJECT_PRIMARY = UniqueKeys0.KEY_FILE_OF_PROJECT_PRIMARY;
    public static final UniqueKey<FileOfWorkflowRecord> KEY_FILE_OF_WORKFLOW_PRIMARY = UniqueKeys0.KEY_FILE_OF_WORKFLOW_PRIMARY;
    public static final UniqueKey<InputOfEnvironmentRecord> KEY_INPUT_OF_ENVIRONMENT_PRIMARY = UniqueKeys0.KEY_INPUT_OF_ENVIRONMENT_PRIMARY;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = UniqueKeys0.KEY_PROJECT_PRIMARY;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_OWNER_ID = UniqueKeys0.KEY_PROJECT_OWNER_ID;
    public static final UniqueKey<ProjectUserAccessRecord> KEY_PROJECT_USER_ACCESS_PRIMARY = UniqueKeys0.KEY_PROJECT_USER_ACCESS_PRIMARY;
    public static final UniqueKey<PublicProjectRecord> KEY_PUBLIC_PROJECT_PRIMARY = UniqueKeys0.KEY_PUBLIC_PROJECT_PRIMARY;
    public static final UniqueKey<UserRecord> KEY_USER_PRIMARY = UniqueKeys0.KEY_USER_PRIMARY;
    public static final UniqueKey<UserRecord> KEY_USER_EMAIL = UniqueKeys0.KEY_USER_EMAIL;
    public static final UniqueKey<UserRecord> KEY_USER_GOOGLE_ID = UniqueKeys0.KEY_USER_GOOGLE_ID;
    public static final UniqueKey<UserConfigRecord> KEY_USER_CONFIG_PRIMARY = UniqueKeys0.KEY_USER_CONFIG_PRIMARY;
    public static final UniqueKey<UserFileAccessRecord> KEY_USER_FILE_ACCESS_PRIMARY = UniqueKeys0.KEY_USER_FILE_ACCESS_PRIMARY;
    public static final UniqueKey<WorkflowRecord> KEY_WORKFLOW_PRIMARY = UniqueKeys0.KEY_WORKFLOW_PRIMARY;
    public static final UniqueKey<WorkflowExecutionsRecord> KEY_WORKFLOW_EXECUTIONS_PRIMARY = UniqueKeys0.KEY_WORKFLOW_EXECUTIONS_PRIMARY;
    public static final UniqueKey<WorkflowOfProjectRecord> KEY_WORKFLOW_OF_PROJECT_PRIMARY = UniqueKeys0.KEY_WORKFLOW_OF_PROJECT_PRIMARY;
    public static final UniqueKey<WorkflowOfUserRecord> KEY_WORKFLOW_OF_USER_PRIMARY = UniqueKeys0.KEY_WORKFLOW_OF_USER_PRIMARY;
    public static final UniqueKey<WorkflowRuntimeStatisticsRecord> KEY_WORKFLOW_RUNTIME_STATISTICS_PRIMARY = UniqueKeys0.KEY_WORKFLOW_RUNTIME_STATISTICS_PRIMARY;
    public static final UniqueKey<WorkflowUserAccessRecord> KEY_WORKFLOW_USER_ACCESS_PRIMARY = UniqueKeys0.KEY_WORKFLOW_USER_ACCESS_PRIMARY;
    public static final UniqueKey<WorkflowVersionRecord> KEY_WORKFLOW_VERSION_PRIMARY = UniqueKeys0.KEY_WORKFLOW_VERSION_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<EnvironmentRecord, UserRecord> ENVIRONMENT_IBFK_1 = ForeignKeys0.ENVIRONMENT_IBFK_1;
    public static final ForeignKey<FileRecord, UserRecord> FILE_IBFK_1 = ForeignKeys0.FILE_IBFK_1;
    public static final ForeignKey<FileOfProjectRecord, FileRecord> FILE_OF_PROJECT_IBFK_1 = ForeignKeys0.FILE_OF_PROJECT_IBFK_1;
    public static final ForeignKey<FileOfProjectRecord, ProjectRecord> FILE_OF_PROJECT_IBFK_2 = ForeignKeys0.FILE_OF_PROJECT_IBFK_2;
    public static final ForeignKey<FileOfWorkflowRecord, FileRecord> FILE_OF_WORKFLOW_IBFK_1 = ForeignKeys0.FILE_OF_WORKFLOW_IBFK_1;
    public static final ForeignKey<FileOfWorkflowRecord, WorkflowRecord> FILE_OF_WORKFLOW_IBFK_2 = ForeignKeys0.FILE_OF_WORKFLOW_IBFK_2;
    public static final ForeignKey<InputOfEnvironmentRecord, EnvironmentRecord> INPUT_OF_ENVIRONMENT_IBFK_1 = ForeignKeys0.INPUT_OF_ENVIRONMENT_IBFK_1;
    public static final ForeignKey<ProjectRecord, UserRecord> PROJECT_IBFK_1 = ForeignKeys0.PROJECT_IBFK_1;
    public static final ForeignKey<ProjectUserAccessRecord, UserRecord> PROJECT_USER_ACCESS_IBFK_1 = ForeignKeys0.PROJECT_USER_ACCESS_IBFK_1;
    public static final ForeignKey<ProjectUserAccessRecord, ProjectRecord> PROJECT_USER_ACCESS_IBFK_2 = ForeignKeys0.PROJECT_USER_ACCESS_IBFK_2;
    public static final ForeignKey<PublicProjectRecord, ProjectRecord> PUBLIC_PROJECT_IBFK_1 = ForeignKeys0.PUBLIC_PROJECT_IBFK_1;
    public static final ForeignKey<UserConfigRecord, UserRecord> USER_CONFIG_IBFK_1 = ForeignKeys0.USER_CONFIG_IBFK_1;
    public static final ForeignKey<UserFileAccessRecord, UserRecord> USER_FILE_ACCESS_IBFK_1 = ForeignKeys0.USER_FILE_ACCESS_IBFK_1;
    public static final ForeignKey<UserFileAccessRecord, FileRecord> USER_FILE_ACCESS_IBFK_2 = ForeignKeys0.USER_FILE_ACCESS_IBFK_2;
    public static final ForeignKey<WorkflowRecord, EnvironmentRecord> FK_WORKFLOW_ENVIRONMENT = ForeignKeys0.FK_WORKFLOW_ENVIRONMENT;
    public static final ForeignKey<WorkflowExecutionsRecord, WorkflowVersionRecord> WORKFLOW_EXECUTIONS_IBFK_1 = ForeignKeys0.WORKFLOW_EXECUTIONS_IBFK_1;
    public static final ForeignKey<WorkflowExecutionsRecord, UserRecord> WORKFLOW_EXECUTIONS_IBFK_2 = ForeignKeys0.WORKFLOW_EXECUTIONS_IBFK_2;
    public static final ForeignKey<WorkflowOfProjectRecord, WorkflowRecord> WORKFLOW_OF_PROJECT_IBFK_1 = ForeignKeys0.WORKFLOW_OF_PROJECT_IBFK_1;
    public static final ForeignKey<WorkflowOfProjectRecord, ProjectRecord> WORKFLOW_OF_PROJECT_IBFK_2 = ForeignKeys0.WORKFLOW_OF_PROJECT_IBFK_2;
    public static final ForeignKey<WorkflowOfUserRecord, UserRecord> WORKFLOW_OF_USER_IBFK_1 = ForeignKeys0.WORKFLOW_OF_USER_IBFK_1;
    public static final ForeignKey<WorkflowOfUserRecord, WorkflowRecord> WORKFLOW_OF_USER_IBFK_2 = ForeignKeys0.WORKFLOW_OF_USER_IBFK_2;
    public static final ForeignKey<WorkflowRuntimeStatisticsRecord, WorkflowRecord> WORKFLOW_RUNTIME_STATISTICS_IBFK_1 = ForeignKeys0.WORKFLOW_RUNTIME_STATISTICS_IBFK_1;
    public static final ForeignKey<WorkflowRuntimeStatisticsRecord, WorkflowExecutionsRecord> WORKFLOW_RUNTIME_STATISTICS_IBFK_2 = ForeignKeys0.WORKFLOW_RUNTIME_STATISTICS_IBFK_2;
    public static final ForeignKey<WorkflowUserAccessRecord, UserRecord> WORKFLOW_USER_ACCESS_IBFK_1 = ForeignKeys0.WORKFLOW_USER_ACCESS_IBFK_1;
    public static final ForeignKey<WorkflowUserAccessRecord, WorkflowRecord> WORKFLOW_USER_ACCESS_IBFK_2 = ForeignKeys0.WORKFLOW_USER_ACCESS_IBFK_2;
    public static final ForeignKey<WorkflowVersionRecord, WorkflowRecord> WORKFLOW_VERSION_IBFK_1 = ForeignKeys0.WORKFLOW_VERSION_IBFK_1;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<EnvironmentRecord, UInteger> IDENTITY_ENVIRONMENT = Internal.createIdentity(Environment.ENVIRONMENT, Environment.ENVIRONMENT.EID);
        public static Identity<FileRecord, UInteger> IDENTITY_FILE = Internal.createIdentity(File.FILE, File.FILE.FID);
        public static Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Internal.createIdentity(Project.PROJECT, Project.PROJECT.PID);
        public static Identity<UserRecord, UInteger> IDENTITY_USER = Internal.createIdentity(User.USER, User.USER.UID);
        public static Identity<WorkflowRecord, UInteger> IDENTITY_WORKFLOW = Internal.createIdentity(Workflow.WORKFLOW, Workflow.WORKFLOW.WID);
        public static Identity<WorkflowExecutionsRecord, UInteger> IDENTITY_WORKFLOW_EXECUTIONS = Internal.createIdentity(WorkflowExecutions.WORKFLOW_EXECUTIONS, WorkflowExecutions.WORKFLOW_EXECUTIONS.EID);
        public static Identity<WorkflowVersionRecord, UInteger> IDENTITY_WORKFLOW_VERSION = Internal.createIdentity(WorkflowVersion.WORKFLOW_VERSION, WorkflowVersion.WORKFLOW_VERSION.VID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<EnvironmentRecord> KEY_ENVIRONMENT_PRIMARY = Internal.createUniqueKey(Environment.ENVIRONMENT, "KEY_environment_PRIMARY", Environment.ENVIRONMENT.EID);
        public static final UniqueKey<FileRecord> KEY_FILE_OWNER_UID = Internal.createUniqueKey(File.FILE, "KEY_file_owner_uid", File.FILE.OWNER_UID, File.FILE.NAME);
        public static final UniqueKey<FileRecord> KEY_FILE_PRIMARY = Internal.createUniqueKey(File.FILE, "KEY_file_PRIMARY", File.FILE.FID);
        public static final UniqueKey<FileOfProjectRecord> KEY_FILE_OF_PROJECT_PRIMARY = Internal.createUniqueKey(FileOfProject.FILE_OF_PROJECT, "KEY_file_of_project_PRIMARY", FileOfProject.FILE_OF_PROJECT.FID, FileOfProject.FILE_OF_PROJECT.PID);
        public static final UniqueKey<FileOfWorkflowRecord> KEY_FILE_OF_WORKFLOW_PRIMARY = Internal.createUniqueKey(FileOfWorkflow.FILE_OF_WORKFLOW, "KEY_file_of_workflow_PRIMARY", FileOfWorkflow.FILE_OF_WORKFLOW.FID, FileOfWorkflow.FILE_OF_WORKFLOW.WID);
        public static final UniqueKey<InputOfEnvironmentRecord> KEY_INPUT_OF_ENVIRONMENT_PRIMARY = Internal.createUniqueKey(InputOfEnvironment.INPUT_OF_ENVIRONMENT, "KEY_input_of_environment_PRIMARY", InputOfEnvironment.INPUT_OF_ENVIRONMENT.DID, InputOfEnvironment.INPUT_OF_ENVIRONMENT.EID);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = Internal.createUniqueKey(Project.PROJECT, "KEY_project_PRIMARY", Project.PROJECT.PID);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_OWNER_ID = Internal.createUniqueKey(Project.PROJECT, "KEY_project_owner_id", Project.PROJECT.OWNER_ID, Project.PROJECT.NAME);
        public static final UniqueKey<ProjectUserAccessRecord> KEY_PROJECT_USER_ACCESS_PRIMARY = Internal.createUniqueKey(ProjectUserAccess.PROJECT_USER_ACCESS, "KEY_project_user_access_PRIMARY", ProjectUserAccess.PROJECT_USER_ACCESS.UID, ProjectUserAccess.PROJECT_USER_ACCESS.PID);
        public static final UniqueKey<PublicProjectRecord> KEY_PUBLIC_PROJECT_PRIMARY = Internal.createUniqueKey(PublicProject.PUBLIC_PROJECT, "KEY_public_project_PRIMARY", PublicProject.PUBLIC_PROJECT.PID);
        public static final UniqueKey<UserRecord> KEY_USER_PRIMARY = Internal.createUniqueKey(User.USER, "KEY_user_PRIMARY", User.USER.UID);
        public static final UniqueKey<UserRecord> KEY_USER_EMAIL = Internal.createUniqueKey(User.USER, "KEY_user_email", User.USER.EMAIL);
        public static final UniqueKey<UserRecord> KEY_USER_GOOGLE_ID = Internal.createUniqueKey(User.USER, "KEY_user_google_id", User.USER.GOOGLE_ID);
        public static final UniqueKey<UserConfigRecord> KEY_USER_CONFIG_PRIMARY = Internal.createUniqueKey(UserConfig.USER_CONFIG, "KEY_user_config_PRIMARY", UserConfig.USER_CONFIG.UID, UserConfig.USER_CONFIG.KEY);
        public static final UniqueKey<UserFileAccessRecord> KEY_USER_FILE_ACCESS_PRIMARY = Internal.createUniqueKey(UserFileAccess.USER_FILE_ACCESS, "KEY_user_file_access_PRIMARY", UserFileAccess.USER_FILE_ACCESS.UID, UserFileAccess.USER_FILE_ACCESS.FID);
        public static final UniqueKey<WorkflowRecord> KEY_WORKFLOW_PRIMARY = Internal.createUniqueKey(Workflow.WORKFLOW, "KEY_workflow_PRIMARY", Workflow.WORKFLOW.WID);
        public static final UniqueKey<WorkflowExecutionsRecord> KEY_WORKFLOW_EXECUTIONS_PRIMARY = Internal.createUniqueKey(WorkflowExecutions.WORKFLOW_EXECUTIONS, "KEY_workflow_executions_PRIMARY", WorkflowExecutions.WORKFLOW_EXECUTIONS.EID);
        public static final UniqueKey<WorkflowOfProjectRecord> KEY_WORKFLOW_OF_PROJECT_PRIMARY = Internal.createUniqueKey(WorkflowOfProject.WORKFLOW_OF_PROJECT, "KEY_workflow_of_project_PRIMARY", WorkflowOfProject.WORKFLOW_OF_PROJECT.WID, WorkflowOfProject.WORKFLOW_OF_PROJECT.PID);
        public static final UniqueKey<WorkflowOfUserRecord> KEY_WORKFLOW_OF_USER_PRIMARY = Internal.createUniqueKey(WorkflowOfUser.WORKFLOW_OF_USER, "KEY_workflow_of_user_PRIMARY", WorkflowOfUser.WORKFLOW_OF_USER.UID, WorkflowOfUser.WORKFLOW_OF_USER.WID);
        public static final UniqueKey<WorkflowRuntimeStatisticsRecord> KEY_WORKFLOW_RUNTIME_STATISTICS_PRIMARY = Internal.createUniqueKey(WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS, "KEY_workflow_runtime_statistics_PRIMARY", WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.WORKFLOW_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.EXECUTION_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.OPERATOR_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.TIME);
        public static final UniqueKey<WorkflowUserAccessRecord> KEY_WORKFLOW_USER_ACCESS_PRIMARY = Internal.createUniqueKey(WorkflowUserAccess.WORKFLOW_USER_ACCESS, "KEY_workflow_user_access_PRIMARY", WorkflowUserAccess.WORKFLOW_USER_ACCESS.UID, WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID);
        public static final UniqueKey<WorkflowVersionRecord> KEY_WORKFLOW_VERSION_PRIMARY = Internal.createUniqueKey(WorkflowVersion.WORKFLOW_VERSION, "KEY_workflow_version_PRIMARY", WorkflowVersion.WORKFLOW_VERSION.VID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<EnvironmentRecord, UserRecord> ENVIRONMENT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, Environment.ENVIRONMENT, "environment_ibfk_1", Environment.ENVIRONMENT.UID);
        public static final ForeignKey<FileRecord, UserRecord> FILE_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, File.FILE, "file_ibfk_1", File.FILE.OWNER_UID);
        public static final ForeignKey<FileOfProjectRecord, FileRecord> FILE_OF_PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_FILE_PRIMARY, FileOfProject.FILE_OF_PROJECT, "file_of_project_ibfk_1", FileOfProject.FILE_OF_PROJECT.FID);
        public static final ForeignKey<FileOfProjectRecord, ProjectRecord> FILE_OF_PROJECT_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_PROJECT_PRIMARY, FileOfProject.FILE_OF_PROJECT, "file_of_project_ibfk_2", FileOfProject.FILE_OF_PROJECT.PID);
        public static final ForeignKey<FileOfWorkflowRecord, FileRecord> FILE_OF_WORKFLOW_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_FILE_PRIMARY, FileOfWorkflow.FILE_OF_WORKFLOW, "file_of_workflow_ibfk_1", FileOfWorkflow.FILE_OF_WORKFLOW.FID);
        public static final ForeignKey<FileOfWorkflowRecord, WorkflowRecord> FILE_OF_WORKFLOW_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, FileOfWorkflow.FILE_OF_WORKFLOW, "file_of_workflow_ibfk_2", FileOfWorkflow.FILE_OF_WORKFLOW.WID);
        public static final ForeignKey<InputOfEnvironmentRecord, EnvironmentRecord> INPUT_OF_ENVIRONMENT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_ENVIRONMENT_PRIMARY, InputOfEnvironment.INPUT_OF_ENVIRONMENT, "input_of_environment_ibfk_1", InputOfEnvironment.INPUT_OF_ENVIRONMENT.EID);
        public static final ForeignKey<ProjectRecord, UserRecord> PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, Project.PROJECT, "project_ibfk_1", Project.PROJECT.OWNER_ID);
        public static final ForeignKey<ProjectUserAccessRecord, UserRecord> PROJECT_USER_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, ProjectUserAccess.PROJECT_USER_ACCESS, "project_user_access_ibfk_1", ProjectUserAccess.PROJECT_USER_ACCESS.UID);
        public static final ForeignKey<ProjectUserAccessRecord, ProjectRecord> PROJECT_USER_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_PROJECT_PRIMARY, ProjectUserAccess.PROJECT_USER_ACCESS, "project_user_access_ibfk_2", ProjectUserAccess.PROJECT_USER_ACCESS.PID);
        public static final ForeignKey<PublicProjectRecord, ProjectRecord> PUBLIC_PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_PROJECT_PRIMARY, PublicProject.PUBLIC_PROJECT, "public_project_ibfk_1", PublicProject.PUBLIC_PROJECT.PID);
        public static final ForeignKey<UserConfigRecord, UserRecord> USER_CONFIG_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, UserConfig.USER_CONFIG, "user_config_ibfk_1", UserConfig.USER_CONFIG.UID);
        public static final ForeignKey<UserFileAccessRecord, UserRecord> USER_FILE_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, UserFileAccess.USER_FILE_ACCESS, "user_file_access_ibfk_1", UserFileAccess.USER_FILE_ACCESS.UID);
        public static final ForeignKey<UserFileAccessRecord, FileRecord> USER_FILE_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_FILE_PRIMARY, UserFileAccess.USER_FILE_ACCESS, "user_file_access_ibfk_2", UserFileAccess.USER_FILE_ACCESS.FID);
        public static final ForeignKey<WorkflowRecord, EnvironmentRecord> FK_WORKFLOW_ENVIRONMENT = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_ENVIRONMENT_PRIMARY, Workflow.WORKFLOW, "fk_workflow_environment", Workflow.WORKFLOW.EID);
        public static final ForeignKey<WorkflowExecutionsRecord, WorkflowVersionRecord> WORKFLOW_EXECUTIONS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_VERSION_PRIMARY, WorkflowExecutions.WORKFLOW_EXECUTIONS, "workflow_executions_ibfk_1", WorkflowExecutions.WORKFLOW_EXECUTIONS.VID);
        public static final ForeignKey<WorkflowExecutionsRecord, UserRecord> WORKFLOW_EXECUTIONS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowExecutions.WORKFLOW_EXECUTIONS, "workflow_executions_ibfk_2", WorkflowExecutions.WORKFLOW_EXECUTIONS.UID);
        public static final ForeignKey<WorkflowOfProjectRecord, WorkflowRecord> WORKFLOW_OF_PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowOfProject.WORKFLOW_OF_PROJECT, "workflow_of_project_ibfk_1", WorkflowOfProject.WORKFLOW_OF_PROJECT.WID);
        public static final ForeignKey<WorkflowOfProjectRecord, ProjectRecord> WORKFLOW_OF_PROJECT_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_PROJECT_PRIMARY, WorkflowOfProject.WORKFLOW_OF_PROJECT, "workflow_of_project_ibfk_2", WorkflowOfProject.WORKFLOW_OF_PROJECT.PID);
        public static final ForeignKey<WorkflowOfUserRecord, UserRecord> WORKFLOW_OF_USER_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowOfUser.WORKFLOW_OF_USER, "workflow_of_user_ibfk_1", WorkflowOfUser.WORKFLOW_OF_USER.UID);
        public static final ForeignKey<WorkflowOfUserRecord, WorkflowRecord> WORKFLOW_OF_USER_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowOfUser.WORKFLOW_OF_USER, "workflow_of_user_ibfk_2", WorkflowOfUser.WORKFLOW_OF_USER.WID);
        public static final ForeignKey<WorkflowRuntimeStatisticsRecord, WorkflowRecord> WORKFLOW_RUNTIME_STATISTICS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS, "workflow_runtime_statistics_ibfk_1", WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.WORKFLOW_ID);
        public static final ForeignKey<WorkflowRuntimeStatisticsRecord, WorkflowExecutionsRecord> WORKFLOW_RUNTIME_STATISTICS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_EXECUTIONS_PRIMARY, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS, "workflow_runtime_statistics_ibfk_2", WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.EXECUTION_ID);
        public static final ForeignKey<WorkflowUserAccessRecord, UserRecord> WORKFLOW_USER_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowUserAccess.WORKFLOW_USER_ACCESS, "workflow_user_access_ibfk_1", WorkflowUserAccess.WORKFLOW_USER_ACCESS.UID);
        public static final ForeignKey<WorkflowUserAccessRecord, WorkflowRecord> WORKFLOW_USER_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowUserAccess.WORKFLOW_USER_ACCESS, "workflow_user_access_ibfk_2", WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID);
        public static final ForeignKey<WorkflowVersionRecord, WorkflowRecord> WORKFLOW_VERSION_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.web.model.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowVersion.WORKFLOW_VERSION, "workflow_version_ibfk_1", WorkflowVersion.WORKFLOW_VERSION.WID);
    }
}
