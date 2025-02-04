/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated;


import edu.uci.ics.texera.dao.jooq.generated.tables.Dataset;
import edu.uci.ics.texera.dao.jooq.generated.tables.DatasetUserAccess;
import edu.uci.ics.texera.dao.jooq.generated.tables.DatasetVersion;
import edu.uci.ics.texera.dao.jooq.generated.tables.OperatorExecutions;
import edu.uci.ics.texera.dao.jooq.generated.tables.OperatorPortExecutions;
import edu.uci.ics.texera.dao.jooq.generated.tables.Project;
import edu.uci.ics.texera.dao.jooq.generated.tables.ProjectUserAccess;
import edu.uci.ics.texera.dao.jooq.generated.tables.PublicProject;
import edu.uci.ics.texera.dao.jooq.generated.tables.User;
import edu.uci.ics.texera.dao.jooq.generated.tables.UserConfig;
import edu.uci.ics.texera.dao.jooq.generated.tables.Workflow;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowExecutions;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowOfProject;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserAccess;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserClones;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserLikes;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowVersion;
import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowViewCount;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.DatasetRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.DatasetUserAccessRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.DatasetVersionRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.OperatorExecutionsRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.OperatorPortExecutionsRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.ProjectRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.ProjectUserAccessRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.PublicProjectRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.UserConfigRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.UserRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowExecutionsRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowOfProjectRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowOfUserRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowUserAccessRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowUserClonesRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowUserLikesRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowVersionRecord;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowViewCountRecord;

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

    public static final Identity<DatasetRecord, UInteger> IDENTITY_DATASET = Identities0.IDENTITY_DATASET;
    public static final Identity<DatasetVersionRecord, UInteger> IDENTITY_DATASET_VERSION = Identities0.IDENTITY_DATASET_VERSION;
    public static final Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Identities0.IDENTITY_PROJECT;
    public static final Identity<UserRecord, UInteger> IDENTITY_USER = Identities0.IDENTITY_USER;
    public static final Identity<WorkflowRecord, UInteger> IDENTITY_WORKFLOW = Identities0.IDENTITY_WORKFLOW;
    public static final Identity<WorkflowExecutionsRecord, UInteger> IDENTITY_WORKFLOW_EXECUTIONS = Identities0.IDENTITY_WORKFLOW_EXECUTIONS;
    public static final Identity<WorkflowVersionRecord, UInteger> IDENTITY_WORKFLOW_VERSION = Identities0.IDENTITY_WORKFLOW_VERSION;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<DatasetRecord> KEY_DATASET_PRIMARY = UniqueKeys0.KEY_DATASET_PRIMARY;
    public static final UniqueKey<DatasetUserAccessRecord> KEY_DATASET_USER_ACCESS_PRIMARY = UniqueKeys0.KEY_DATASET_USER_ACCESS_PRIMARY;
    public static final UniqueKey<DatasetVersionRecord> KEY_DATASET_VERSION_PRIMARY = UniqueKeys0.KEY_DATASET_VERSION_PRIMARY;
    public static final UniqueKey<OperatorExecutionsRecord> KEY_OPERATOR_EXECUTIONS_WORKFLOW_EXECUTION_ID = UniqueKeys0.KEY_OPERATOR_EXECUTIONS_WORKFLOW_EXECUTION_ID;
    public static final UniqueKey<OperatorPortExecutionsRecord> KEY_OPERATOR_PORT_EXECUTIONS_WORKFLOW_EXECUTION_ID = UniqueKeys0.KEY_OPERATOR_PORT_EXECUTIONS_WORKFLOW_EXECUTION_ID;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = UniqueKeys0.KEY_PROJECT_PRIMARY;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_OWNER_ID = UniqueKeys0.KEY_PROJECT_OWNER_ID;
    public static final UniqueKey<ProjectUserAccessRecord> KEY_PROJECT_USER_ACCESS_PRIMARY = UniqueKeys0.KEY_PROJECT_USER_ACCESS_PRIMARY;
    public static final UniqueKey<PublicProjectRecord> KEY_PUBLIC_PROJECT_PRIMARY = UniqueKeys0.KEY_PUBLIC_PROJECT_PRIMARY;
    public static final UniqueKey<UserRecord> KEY_USER_PRIMARY = UniqueKeys0.KEY_USER_PRIMARY;
    public static final UniqueKey<UserRecord> KEY_USER_EMAIL = UniqueKeys0.KEY_USER_EMAIL;
    public static final UniqueKey<UserRecord> KEY_USER_GOOGLE_ID = UniqueKeys0.KEY_USER_GOOGLE_ID;
    public static final UniqueKey<UserConfigRecord> KEY_USER_CONFIG_PRIMARY = UniqueKeys0.KEY_USER_CONFIG_PRIMARY;
    public static final UniqueKey<WorkflowRecord> KEY_WORKFLOW_PRIMARY = UniqueKeys0.KEY_WORKFLOW_PRIMARY;
    public static final UniqueKey<WorkflowExecutionsRecord> KEY_WORKFLOW_EXECUTIONS_PRIMARY = UniqueKeys0.KEY_WORKFLOW_EXECUTIONS_PRIMARY;
    public static final UniqueKey<WorkflowOfProjectRecord> KEY_WORKFLOW_OF_PROJECT_PRIMARY = UniqueKeys0.KEY_WORKFLOW_OF_PROJECT_PRIMARY;
    public static final UniqueKey<WorkflowOfUserRecord> KEY_WORKFLOW_OF_USER_PRIMARY = UniqueKeys0.KEY_WORKFLOW_OF_USER_PRIMARY;
    public static final UniqueKey<WorkflowUserAccessRecord> KEY_WORKFLOW_USER_ACCESS_PRIMARY = UniqueKeys0.KEY_WORKFLOW_USER_ACCESS_PRIMARY;
    public static final UniqueKey<WorkflowUserClonesRecord> KEY_WORKFLOW_USER_CLONES_PRIMARY = UniqueKeys0.KEY_WORKFLOW_USER_CLONES_PRIMARY;
    public static final UniqueKey<WorkflowUserLikesRecord> KEY_WORKFLOW_USER_LIKES_PRIMARY = UniqueKeys0.KEY_WORKFLOW_USER_LIKES_PRIMARY;
    public static final UniqueKey<WorkflowVersionRecord> KEY_WORKFLOW_VERSION_PRIMARY = UniqueKeys0.KEY_WORKFLOW_VERSION_PRIMARY;
    public static final UniqueKey<WorkflowViewCountRecord> KEY_WORKFLOW_VIEW_COUNT_PRIMARY = UniqueKeys0.KEY_WORKFLOW_VIEW_COUNT_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<DatasetRecord, UserRecord> DATASET_IBFK_1 = ForeignKeys0.DATASET_IBFK_1;
    public static final ForeignKey<DatasetUserAccessRecord, DatasetRecord> DATASET_USER_ACCESS_IBFK_1 = ForeignKeys0.DATASET_USER_ACCESS_IBFK_1;
    public static final ForeignKey<DatasetUserAccessRecord, UserRecord> DATASET_USER_ACCESS_IBFK_2 = ForeignKeys0.DATASET_USER_ACCESS_IBFK_2;
    public static final ForeignKey<DatasetVersionRecord, DatasetRecord> DATASET_VERSION_IBFK_1 = ForeignKeys0.DATASET_VERSION_IBFK_1;
    public static final ForeignKey<OperatorExecutionsRecord, WorkflowExecutionsRecord> OPERATOR_EXECUTIONS_IBFK_1 = ForeignKeys0.OPERATOR_EXECUTIONS_IBFK_1;
    public static final ForeignKey<OperatorPortExecutionsRecord, WorkflowExecutionsRecord> OPERATOR_PORT_EXECUTIONS_IBFK_1 = ForeignKeys0.OPERATOR_PORT_EXECUTIONS_IBFK_1;
    public static final ForeignKey<ProjectRecord, UserRecord> PROJECT_IBFK_1 = ForeignKeys0.PROJECT_IBFK_1;
    public static final ForeignKey<ProjectUserAccessRecord, UserRecord> PROJECT_USER_ACCESS_IBFK_1 = ForeignKeys0.PROJECT_USER_ACCESS_IBFK_1;
    public static final ForeignKey<ProjectUserAccessRecord, ProjectRecord> PROJECT_USER_ACCESS_IBFK_2 = ForeignKeys0.PROJECT_USER_ACCESS_IBFK_2;
    public static final ForeignKey<PublicProjectRecord, ProjectRecord> PUBLIC_PROJECT_IBFK_1 = ForeignKeys0.PUBLIC_PROJECT_IBFK_1;
    public static final ForeignKey<UserConfigRecord, UserRecord> USER_CONFIG_IBFK_1 = ForeignKeys0.USER_CONFIG_IBFK_1;
    public static final ForeignKey<WorkflowExecutionsRecord, WorkflowVersionRecord> WORKFLOW_EXECUTIONS_IBFK_1 = ForeignKeys0.WORKFLOW_EXECUTIONS_IBFK_1;
    public static final ForeignKey<WorkflowExecutionsRecord, UserRecord> WORKFLOW_EXECUTIONS_IBFK_2 = ForeignKeys0.WORKFLOW_EXECUTIONS_IBFK_2;
    public static final ForeignKey<WorkflowOfProjectRecord, WorkflowRecord> WORKFLOW_OF_PROJECT_IBFK_1 = ForeignKeys0.WORKFLOW_OF_PROJECT_IBFK_1;
    public static final ForeignKey<WorkflowOfProjectRecord, ProjectRecord> WORKFLOW_OF_PROJECT_IBFK_2 = ForeignKeys0.WORKFLOW_OF_PROJECT_IBFK_2;
    public static final ForeignKey<WorkflowOfUserRecord, UserRecord> WORKFLOW_OF_USER_IBFK_1 = ForeignKeys0.WORKFLOW_OF_USER_IBFK_1;
    public static final ForeignKey<WorkflowOfUserRecord, WorkflowRecord> WORKFLOW_OF_USER_IBFK_2 = ForeignKeys0.WORKFLOW_OF_USER_IBFK_2;
    public static final ForeignKey<WorkflowUserAccessRecord, UserRecord> WORKFLOW_USER_ACCESS_IBFK_1 = ForeignKeys0.WORKFLOW_USER_ACCESS_IBFK_1;
    public static final ForeignKey<WorkflowUserAccessRecord, WorkflowRecord> WORKFLOW_USER_ACCESS_IBFK_2 = ForeignKeys0.WORKFLOW_USER_ACCESS_IBFK_2;
    public static final ForeignKey<WorkflowUserClonesRecord, UserRecord> WORKFLOW_USER_CLONES_IBFK_1 = ForeignKeys0.WORKFLOW_USER_CLONES_IBFK_1;
    public static final ForeignKey<WorkflowUserClonesRecord, WorkflowRecord> WORKFLOW_USER_CLONES_IBFK_2 = ForeignKeys0.WORKFLOW_USER_CLONES_IBFK_2;
    public static final ForeignKey<WorkflowUserLikesRecord, UserRecord> WORKFLOW_USER_LIKES_IBFK_1 = ForeignKeys0.WORKFLOW_USER_LIKES_IBFK_1;
    public static final ForeignKey<WorkflowUserLikesRecord, WorkflowRecord> WORKFLOW_USER_LIKES_IBFK_2 = ForeignKeys0.WORKFLOW_USER_LIKES_IBFK_2;
    public static final ForeignKey<WorkflowVersionRecord, WorkflowRecord> WORKFLOW_VERSION_IBFK_1 = ForeignKeys0.WORKFLOW_VERSION_IBFK_1;
    public static final ForeignKey<WorkflowViewCountRecord, WorkflowRecord> WORKFLOW_VIEW_COUNT_IBFK_1 = ForeignKeys0.WORKFLOW_VIEW_COUNT_IBFK_1;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<DatasetRecord, UInteger> IDENTITY_DATASET = Internal.createIdentity(Dataset.DATASET, Dataset.DATASET.DID);
        public static Identity<DatasetVersionRecord, UInteger> IDENTITY_DATASET_VERSION = Internal.createIdentity(DatasetVersion.DATASET_VERSION, DatasetVersion.DATASET_VERSION.DVID);
        public static Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Internal.createIdentity(Project.PROJECT, Project.PROJECT.PID);
        public static Identity<UserRecord, UInteger> IDENTITY_USER = Internal.createIdentity(User.USER, User.USER.UID);
        public static Identity<WorkflowRecord, UInteger> IDENTITY_WORKFLOW = Internal.createIdentity(Workflow.WORKFLOW, Workflow.WORKFLOW.WID);
        public static Identity<WorkflowExecutionsRecord, UInteger> IDENTITY_WORKFLOW_EXECUTIONS = Internal.createIdentity(WorkflowExecutions.WORKFLOW_EXECUTIONS, WorkflowExecutions.WORKFLOW_EXECUTIONS.EID);
        public static Identity<WorkflowVersionRecord, UInteger> IDENTITY_WORKFLOW_VERSION = Internal.createIdentity(WorkflowVersion.WORKFLOW_VERSION, WorkflowVersion.WORKFLOW_VERSION.VID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<DatasetRecord> KEY_DATASET_PRIMARY = Internal.createUniqueKey(Dataset.DATASET, "KEY_dataset_PRIMARY", Dataset.DATASET.DID);
        public static final UniqueKey<DatasetUserAccessRecord> KEY_DATASET_USER_ACCESS_PRIMARY = Internal.createUniqueKey(DatasetUserAccess.DATASET_USER_ACCESS, "KEY_dataset_user_access_PRIMARY", DatasetUserAccess.DATASET_USER_ACCESS.DID, DatasetUserAccess.DATASET_USER_ACCESS.UID);
        public static final UniqueKey<DatasetVersionRecord> KEY_DATASET_VERSION_PRIMARY = Internal.createUniqueKey(DatasetVersion.DATASET_VERSION, "KEY_dataset_version_PRIMARY", DatasetVersion.DATASET_VERSION.DVID);
        public static final UniqueKey<OperatorExecutionsRecord> KEY_OPERATOR_EXECUTIONS_WORKFLOW_EXECUTION_ID = Internal.createUniqueKey(OperatorExecutions.OPERATOR_EXECUTIONS, "KEY_operator_executions_workflow_execution_id", OperatorExecutions.OPERATOR_EXECUTIONS.WORKFLOW_EXECUTION_ID, OperatorExecutions.OPERATOR_EXECUTIONS.OPERATOR_ID);
        public static final UniqueKey<OperatorPortExecutionsRecord> KEY_OPERATOR_PORT_EXECUTIONS_WORKFLOW_EXECUTION_ID = Internal.createUniqueKey(OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS, "KEY_operator_port_executions_workflow_execution_id", OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS.WORKFLOW_EXECUTION_ID, OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS.OPERATOR_ID, OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS.PORT_ID);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = Internal.createUniqueKey(Project.PROJECT, "KEY_project_PRIMARY", Project.PROJECT.PID);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_OWNER_ID = Internal.createUniqueKey(Project.PROJECT, "KEY_project_owner_id", Project.PROJECT.OWNER_ID, Project.PROJECT.NAME);
        public static final UniqueKey<ProjectUserAccessRecord> KEY_PROJECT_USER_ACCESS_PRIMARY = Internal.createUniqueKey(ProjectUserAccess.PROJECT_USER_ACCESS, "KEY_project_user_access_PRIMARY", ProjectUserAccess.PROJECT_USER_ACCESS.UID, ProjectUserAccess.PROJECT_USER_ACCESS.PID);
        public static final UniqueKey<PublicProjectRecord> KEY_PUBLIC_PROJECT_PRIMARY = Internal.createUniqueKey(PublicProject.PUBLIC_PROJECT, "KEY_public_project_PRIMARY", PublicProject.PUBLIC_PROJECT.PID);
        public static final UniqueKey<UserRecord> KEY_USER_PRIMARY = Internal.createUniqueKey(User.USER, "KEY_user_PRIMARY", User.USER.UID);
        public static final UniqueKey<UserRecord> KEY_USER_EMAIL = Internal.createUniqueKey(User.USER, "KEY_user_email", User.USER.EMAIL);
        public static final UniqueKey<UserRecord> KEY_USER_GOOGLE_ID = Internal.createUniqueKey(User.USER, "KEY_user_google_id", User.USER.GOOGLE_ID);
        public static final UniqueKey<UserConfigRecord> KEY_USER_CONFIG_PRIMARY = Internal.createUniqueKey(UserConfig.USER_CONFIG, "KEY_user_config_PRIMARY", UserConfig.USER_CONFIG.UID, UserConfig.USER_CONFIG.KEY);
        public static final UniqueKey<WorkflowRecord> KEY_WORKFLOW_PRIMARY = Internal.createUniqueKey(Workflow.WORKFLOW, "KEY_workflow_PRIMARY", Workflow.WORKFLOW.WID);
        public static final UniqueKey<WorkflowExecutionsRecord> KEY_WORKFLOW_EXECUTIONS_PRIMARY = Internal.createUniqueKey(WorkflowExecutions.WORKFLOW_EXECUTIONS, "KEY_workflow_executions_PRIMARY", WorkflowExecutions.WORKFLOW_EXECUTIONS.EID);
        public static final UniqueKey<WorkflowOfProjectRecord> KEY_WORKFLOW_OF_PROJECT_PRIMARY = Internal.createUniqueKey(WorkflowOfProject.WORKFLOW_OF_PROJECT, "KEY_workflow_of_project_PRIMARY", WorkflowOfProject.WORKFLOW_OF_PROJECT.WID, WorkflowOfProject.WORKFLOW_OF_PROJECT.PID);
        public static final UniqueKey<WorkflowOfUserRecord> KEY_WORKFLOW_OF_USER_PRIMARY = Internal.createUniqueKey(WorkflowOfUser.WORKFLOW_OF_USER, "KEY_workflow_of_user_PRIMARY", WorkflowOfUser.WORKFLOW_OF_USER.UID, WorkflowOfUser.WORKFLOW_OF_USER.WID);
        public static final UniqueKey<WorkflowUserAccessRecord> KEY_WORKFLOW_USER_ACCESS_PRIMARY = Internal.createUniqueKey(WorkflowUserAccess.WORKFLOW_USER_ACCESS, "KEY_workflow_user_access_PRIMARY", WorkflowUserAccess.WORKFLOW_USER_ACCESS.UID, WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID);
        public static final UniqueKey<WorkflowUserClonesRecord> KEY_WORKFLOW_USER_CLONES_PRIMARY = Internal.createUniqueKey(WorkflowUserClones.WORKFLOW_USER_CLONES, "KEY_workflow_user_clones_PRIMARY", WorkflowUserClones.WORKFLOW_USER_CLONES.UID, WorkflowUserClones.WORKFLOW_USER_CLONES.WID);
        public static final UniqueKey<WorkflowUserLikesRecord> KEY_WORKFLOW_USER_LIKES_PRIMARY = Internal.createUniqueKey(WorkflowUserLikes.WORKFLOW_USER_LIKES, "KEY_workflow_user_likes_PRIMARY", WorkflowUserLikes.WORKFLOW_USER_LIKES.UID, WorkflowUserLikes.WORKFLOW_USER_LIKES.WID);
        public static final UniqueKey<WorkflowVersionRecord> KEY_WORKFLOW_VERSION_PRIMARY = Internal.createUniqueKey(WorkflowVersion.WORKFLOW_VERSION, "KEY_workflow_version_PRIMARY", WorkflowVersion.WORKFLOW_VERSION.VID);
        public static final UniqueKey<WorkflowViewCountRecord> KEY_WORKFLOW_VIEW_COUNT_PRIMARY = Internal.createUniqueKey(WorkflowViewCount.WORKFLOW_VIEW_COUNT, "KEY_workflow_view_count_PRIMARY", WorkflowViewCount.WORKFLOW_VIEW_COUNT.WID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<DatasetRecord, UserRecord> DATASET_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, Dataset.DATASET, "dataset_ibfk_1", Dataset.DATASET.OWNER_UID);
        public static final ForeignKey<DatasetUserAccessRecord, DatasetRecord> DATASET_USER_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_DATASET_PRIMARY, DatasetUserAccess.DATASET_USER_ACCESS, "dataset_user_access_ibfk_1", DatasetUserAccess.DATASET_USER_ACCESS.DID);
        public static final ForeignKey<DatasetUserAccessRecord, UserRecord> DATASET_USER_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, DatasetUserAccess.DATASET_USER_ACCESS, "dataset_user_access_ibfk_2", DatasetUserAccess.DATASET_USER_ACCESS.UID);
        public static final ForeignKey<DatasetVersionRecord, DatasetRecord> DATASET_VERSION_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_DATASET_PRIMARY, DatasetVersion.DATASET_VERSION, "dataset_version_ibfk_1", DatasetVersion.DATASET_VERSION.DID);
        public static final ForeignKey<OperatorExecutionsRecord, WorkflowExecutionsRecord> OPERATOR_EXECUTIONS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_EXECUTIONS_PRIMARY, OperatorExecutions.OPERATOR_EXECUTIONS, "operator_executions_ibfk_1", OperatorExecutions.OPERATOR_EXECUTIONS.WORKFLOW_EXECUTION_ID);
        public static final ForeignKey<OperatorPortExecutionsRecord, WorkflowExecutionsRecord> OPERATOR_PORT_EXECUTIONS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_EXECUTIONS_PRIMARY, OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS, "operator_port_executions_ibfk_1", OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS.WORKFLOW_EXECUTION_ID);
        public static final ForeignKey<ProjectRecord, UserRecord> PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, Project.PROJECT, "project_ibfk_1", Project.PROJECT.OWNER_ID);
        public static final ForeignKey<ProjectUserAccessRecord, UserRecord> PROJECT_USER_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, ProjectUserAccess.PROJECT_USER_ACCESS, "project_user_access_ibfk_1", ProjectUserAccess.PROJECT_USER_ACCESS.UID);
        public static final ForeignKey<ProjectUserAccessRecord, ProjectRecord> PROJECT_USER_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_PROJECT_PRIMARY, ProjectUserAccess.PROJECT_USER_ACCESS, "project_user_access_ibfk_2", ProjectUserAccess.PROJECT_USER_ACCESS.PID);
        public static final ForeignKey<PublicProjectRecord, ProjectRecord> PUBLIC_PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_PROJECT_PRIMARY, PublicProject.PUBLIC_PROJECT, "public_project_ibfk_1", PublicProject.PUBLIC_PROJECT.PID);
        public static final ForeignKey<UserConfigRecord, UserRecord> USER_CONFIG_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, UserConfig.USER_CONFIG, "user_config_ibfk_1", UserConfig.USER_CONFIG.UID);
        public static final ForeignKey<WorkflowExecutionsRecord, WorkflowVersionRecord> WORKFLOW_EXECUTIONS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_VERSION_PRIMARY, WorkflowExecutions.WORKFLOW_EXECUTIONS, "workflow_executions_ibfk_1", WorkflowExecutions.WORKFLOW_EXECUTIONS.VID);
        public static final ForeignKey<WorkflowExecutionsRecord, UserRecord> WORKFLOW_EXECUTIONS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowExecutions.WORKFLOW_EXECUTIONS, "workflow_executions_ibfk_2", WorkflowExecutions.WORKFLOW_EXECUTIONS.UID);
        public static final ForeignKey<WorkflowOfProjectRecord, WorkflowRecord> WORKFLOW_OF_PROJECT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowOfProject.WORKFLOW_OF_PROJECT, "workflow_of_project_ibfk_1", WorkflowOfProject.WORKFLOW_OF_PROJECT.WID);
        public static final ForeignKey<WorkflowOfProjectRecord, ProjectRecord> WORKFLOW_OF_PROJECT_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_PROJECT_PRIMARY, WorkflowOfProject.WORKFLOW_OF_PROJECT, "workflow_of_project_ibfk_2", WorkflowOfProject.WORKFLOW_OF_PROJECT.PID);
        public static final ForeignKey<WorkflowOfUserRecord, UserRecord> WORKFLOW_OF_USER_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowOfUser.WORKFLOW_OF_USER, "workflow_of_user_ibfk_1", WorkflowOfUser.WORKFLOW_OF_USER.UID);
        public static final ForeignKey<WorkflowOfUserRecord, WorkflowRecord> WORKFLOW_OF_USER_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowOfUser.WORKFLOW_OF_USER, "workflow_of_user_ibfk_2", WorkflowOfUser.WORKFLOW_OF_USER.WID);
        public static final ForeignKey<WorkflowUserAccessRecord, UserRecord> WORKFLOW_USER_ACCESS_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowUserAccess.WORKFLOW_USER_ACCESS, "workflow_user_access_ibfk_1", WorkflowUserAccess.WORKFLOW_USER_ACCESS.UID);
        public static final ForeignKey<WorkflowUserAccessRecord, WorkflowRecord> WORKFLOW_USER_ACCESS_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowUserAccess.WORKFLOW_USER_ACCESS, "workflow_user_access_ibfk_2", WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID);
        public static final ForeignKey<WorkflowUserClonesRecord, UserRecord> WORKFLOW_USER_CLONES_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowUserClones.WORKFLOW_USER_CLONES, "workflow_user_clones_ibfk_1", WorkflowUserClones.WORKFLOW_USER_CLONES.UID);
        public static final ForeignKey<WorkflowUserClonesRecord, WorkflowRecord> WORKFLOW_USER_CLONES_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowUserClones.WORKFLOW_USER_CLONES, "workflow_user_clones_ibfk_2", WorkflowUserClones.WORKFLOW_USER_CLONES.WID);
        public static final ForeignKey<WorkflowUserLikesRecord, UserRecord> WORKFLOW_USER_LIKES_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_USER_PRIMARY, WorkflowUserLikes.WORKFLOW_USER_LIKES, "workflow_user_likes_ibfk_1", WorkflowUserLikes.WORKFLOW_USER_LIKES.UID);
        public static final ForeignKey<WorkflowUserLikesRecord, WorkflowRecord> WORKFLOW_USER_LIKES_IBFK_2 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowUserLikes.WORKFLOW_USER_LIKES, "workflow_user_likes_ibfk_2", WorkflowUserLikes.WORKFLOW_USER_LIKES.WID);
        public static final ForeignKey<WorkflowVersionRecord, WorkflowRecord> WORKFLOW_VERSION_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowVersion.WORKFLOW_VERSION, "workflow_version_ibfk_1", WorkflowVersion.WORKFLOW_VERSION.WID);
        public static final ForeignKey<WorkflowViewCountRecord, WorkflowRecord> WORKFLOW_VIEW_COUNT_IBFK_1 = Internal.createForeignKey(edu.uci.ics.texera.dao.jooq.generated.Keys.KEY_WORKFLOW_PRIMARY, WorkflowViewCount.WORKFLOW_VIEW_COUNT, "workflow_view_count_ibfk_1", WorkflowViewCount.WORKFLOW_VIEW_COUNT.WID);
    }
}
