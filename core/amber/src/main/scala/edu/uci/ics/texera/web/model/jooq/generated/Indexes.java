/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated;


import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset;
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetVersion;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Project;
import edu.uci.ics.texera.web.model.jooq.generated.tables.ProjectUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.PublicProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.User;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserConfig;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Workflow;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowExecutions;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowRuntimeStatistics;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowVersion;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>texera_db</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index DATASET_IDX_DATASET_NAME_DESCRIPTION = Indexes0.DATASET_IDX_DATASET_NAME_DESCRIPTION;
    public static final Index DATASET_OWNER_UID = Indexes0.DATASET_OWNER_UID;
    public static final Index DATASET_PRIMARY = Indexes0.DATASET_PRIMARY;
    public static final Index DATASET_USER_ACCESS_PRIMARY = Indexes0.DATASET_USER_ACCESS_PRIMARY;
    public static final Index DATASET_USER_ACCESS_UID = Indexes0.DATASET_USER_ACCESS_UID;
    public static final Index DATASET_VERSION_DID = Indexes0.DATASET_VERSION_DID;
    public static final Index DATASET_VERSION_IDX_DATASET_VERSION_NAME = Indexes0.DATASET_VERSION_IDX_DATASET_VERSION_NAME;
    public static final Index DATASET_VERSION_PRIMARY = Indexes0.DATASET_VERSION_PRIMARY;
    public static final Index PROJECT_IDX_USER_PROJECT_NAME_DESCRIPTION = Indexes0.PROJECT_IDX_USER_PROJECT_NAME_DESCRIPTION;
    public static final Index PROJECT_OWNER_ID = Indexes0.PROJECT_OWNER_ID;
    public static final Index PROJECT_PRIMARY = Indexes0.PROJECT_PRIMARY;
    public static final Index PROJECT_USER_ACCESS_PID = Indexes0.PROJECT_USER_ACCESS_PID;
    public static final Index PROJECT_USER_ACCESS_PRIMARY = Indexes0.PROJECT_USER_ACCESS_PRIMARY;
    public static final Index PUBLIC_PROJECT_PRIMARY = Indexes0.PUBLIC_PROJECT_PRIMARY;
    public static final Index USER_EMAIL = Indexes0.USER_EMAIL;
    public static final Index USER_GOOGLE_ID = Indexes0.USER_GOOGLE_ID;
    public static final Index USER_IDX_USER_NAME = Indexes0.USER_IDX_USER_NAME;
    public static final Index USER_PRIMARY = Indexes0.USER_PRIMARY;
    public static final Index USER_CONFIG_PRIMARY = Indexes0.USER_CONFIG_PRIMARY;
    public static final Index WORKFLOW_IDX_WORKFLOW_NAME_DESCRIPTION_CONTENT = Indexes0.WORKFLOW_IDX_WORKFLOW_NAME_DESCRIPTION_CONTENT;
    public static final Index WORKFLOW_PRIMARY = Indexes0.WORKFLOW_PRIMARY;
    public static final Index WORKFLOW_EXECUTIONS_PRIMARY = Indexes0.WORKFLOW_EXECUTIONS_PRIMARY;
    public static final Index WORKFLOW_EXECUTIONS_UID = Indexes0.WORKFLOW_EXECUTIONS_UID;
    public static final Index WORKFLOW_EXECUTIONS_VID = Indexes0.WORKFLOW_EXECUTIONS_VID;
    public static final Index WORKFLOW_OF_PROJECT_PID = Indexes0.WORKFLOW_OF_PROJECT_PID;
    public static final Index WORKFLOW_OF_PROJECT_PRIMARY = Indexes0.WORKFLOW_OF_PROJECT_PRIMARY;
    public static final Index WORKFLOW_OF_USER_PRIMARY = Indexes0.WORKFLOW_OF_USER_PRIMARY;
    public static final Index WORKFLOW_OF_USER_WID = Indexes0.WORKFLOW_OF_USER_WID;
    public static final Index WORKFLOW_RUNTIME_STATISTICS_EXECUTION_ID = Indexes0.WORKFLOW_RUNTIME_STATISTICS_EXECUTION_ID;
    public static final Index WORKFLOW_RUNTIME_STATISTICS_PRIMARY = Indexes0.WORKFLOW_RUNTIME_STATISTICS_PRIMARY;
    public static final Index WORKFLOW_USER_ACCESS_PRIMARY = Indexes0.WORKFLOW_USER_ACCESS_PRIMARY;
    public static final Index WORKFLOW_USER_ACCESS_WID = Indexes0.WORKFLOW_USER_ACCESS_WID;
    public static final Index WORKFLOW_VERSION_PRIMARY = Indexes0.WORKFLOW_VERSION_PRIMARY;
    public static final Index WORKFLOW_VERSION_WID = Indexes0.WORKFLOW_VERSION_WID;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index DATASET_IDX_DATASET_NAME_DESCRIPTION = Internal.createIndex("idx_dataset_name_description", Dataset.DATASET, new OrderField[] { Dataset.DATASET.NAME, Dataset.DATASET.DESCRIPTION }, false);
        public static Index DATASET_OWNER_UID = Internal.createIndex("owner_uid", Dataset.DATASET, new OrderField[] { Dataset.DATASET.OWNER_UID }, false);
        public static Index DATASET_PRIMARY = Internal.createIndex("PRIMARY", Dataset.DATASET, new OrderField[] { Dataset.DATASET.DID }, true);
        public static Index DATASET_USER_ACCESS_PRIMARY = Internal.createIndex("PRIMARY", DatasetUserAccess.DATASET_USER_ACCESS, new OrderField[] { DatasetUserAccess.DATASET_USER_ACCESS.DID, DatasetUserAccess.DATASET_USER_ACCESS.UID }, true);
        public static Index DATASET_USER_ACCESS_UID = Internal.createIndex("uid", DatasetUserAccess.DATASET_USER_ACCESS, new OrderField[] { DatasetUserAccess.DATASET_USER_ACCESS.UID }, false);
        public static Index DATASET_VERSION_DID = Internal.createIndex("did", DatasetVersion.DATASET_VERSION, new OrderField[] { DatasetVersion.DATASET_VERSION.DID }, false);
        public static Index DATASET_VERSION_IDX_DATASET_VERSION_NAME = Internal.createIndex("idx_dataset_version_name", DatasetVersion.DATASET_VERSION, new OrderField[] { DatasetVersion.DATASET_VERSION.NAME }, false);
        public static Index DATASET_VERSION_PRIMARY = Internal.createIndex("PRIMARY", DatasetVersion.DATASET_VERSION, new OrderField[] { DatasetVersion.DATASET_VERSION.DVID }, true);
        public static Index PROJECT_IDX_USER_PROJECT_NAME_DESCRIPTION = Internal.createIndex("idx_user_project_name_description", Project.PROJECT, new OrderField[] { Project.PROJECT.NAME, Project.PROJECT.DESCRIPTION }, false);
        public static Index PROJECT_OWNER_ID = Internal.createIndex("owner_id", Project.PROJECT, new OrderField[] { Project.PROJECT.OWNER_ID, Project.PROJECT.NAME }, true);
        public static Index PROJECT_PRIMARY = Internal.createIndex("PRIMARY", Project.PROJECT, new OrderField[] { Project.PROJECT.PID }, true);
        public static Index PROJECT_USER_ACCESS_PID = Internal.createIndex("pid", ProjectUserAccess.PROJECT_USER_ACCESS, new OrderField[] { ProjectUserAccess.PROJECT_USER_ACCESS.PID }, false);
        public static Index PROJECT_USER_ACCESS_PRIMARY = Internal.createIndex("PRIMARY", ProjectUserAccess.PROJECT_USER_ACCESS, new OrderField[] { ProjectUserAccess.PROJECT_USER_ACCESS.UID, ProjectUserAccess.PROJECT_USER_ACCESS.PID }, true);
        public static Index PUBLIC_PROJECT_PRIMARY = Internal.createIndex("PRIMARY", PublicProject.PUBLIC_PROJECT, new OrderField[] { PublicProject.PUBLIC_PROJECT.PID }, true);
        public static Index USER_EMAIL = Internal.createIndex("email", User.USER, new OrderField[] { User.USER.EMAIL }, true);
        public static Index USER_GOOGLE_ID = Internal.createIndex("google_id", User.USER, new OrderField[] { User.USER.GOOGLE_ID }, true);
        public static Index USER_IDX_USER_NAME = Internal.createIndex("idx_user_name", User.USER, new OrderField[] { User.USER.NAME }, false);
        public static Index USER_PRIMARY = Internal.createIndex("PRIMARY", User.USER, new OrderField[] { User.USER.UID }, true);
        public static Index USER_CONFIG_PRIMARY = Internal.createIndex("PRIMARY", UserConfig.USER_CONFIG, new OrderField[] { UserConfig.USER_CONFIG.UID, UserConfig.USER_CONFIG.KEY }, true);
        public static Index WORKFLOW_IDX_WORKFLOW_NAME_DESCRIPTION_CONTENT = Internal.createIndex("idx_workflow_name_description_content", Workflow.WORKFLOW, new OrderField[] { Workflow.WORKFLOW.NAME, Workflow.WORKFLOW.DESCRIPTION, Workflow.WORKFLOW.CONTENT }, false);
        public static Index WORKFLOW_PRIMARY = Internal.createIndex("PRIMARY", Workflow.WORKFLOW, new OrderField[] { Workflow.WORKFLOW.WID }, true);
        public static Index WORKFLOW_EXECUTIONS_PRIMARY = Internal.createIndex("PRIMARY", WorkflowExecutions.WORKFLOW_EXECUTIONS, new OrderField[] { WorkflowExecutions.WORKFLOW_EXECUTIONS.EID }, true);
        public static Index WORKFLOW_EXECUTIONS_UID = Internal.createIndex("uid", WorkflowExecutions.WORKFLOW_EXECUTIONS, new OrderField[] { WorkflowExecutions.WORKFLOW_EXECUTIONS.UID }, false);
        public static Index WORKFLOW_EXECUTIONS_VID = Internal.createIndex("vid", WorkflowExecutions.WORKFLOW_EXECUTIONS, new OrderField[] { WorkflowExecutions.WORKFLOW_EXECUTIONS.VID }, false);
        public static Index WORKFLOW_OF_PROJECT_PID = Internal.createIndex("pid", WorkflowOfProject.WORKFLOW_OF_PROJECT, new OrderField[] { WorkflowOfProject.WORKFLOW_OF_PROJECT.PID }, false);
        public static Index WORKFLOW_OF_PROJECT_PRIMARY = Internal.createIndex("PRIMARY", WorkflowOfProject.WORKFLOW_OF_PROJECT, new OrderField[] { WorkflowOfProject.WORKFLOW_OF_PROJECT.WID, WorkflowOfProject.WORKFLOW_OF_PROJECT.PID }, true);
        public static Index WORKFLOW_OF_USER_PRIMARY = Internal.createIndex("PRIMARY", WorkflowOfUser.WORKFLOW_OF_USER, new OrderField[] { WorkflowOfUser.WORKFLOW_OF_USER.UID, WorkflowOfUser.WORKFLOW_OF_USER.WID }, true);
        public static Index WORKFLOW_OF_USER_WID = Internal.createIndex("wid", WorkflowOfUser.WORKFLOW_OF_USER, new OrderField[] { WorkflowOfUser.WORKFLOW_OF_USER.WID }, false);
        public static Index WORKFLOW_RUNTIME_STATISTICS_EXECUTION_ID = Internal.createIndex("execution_id", WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS, new OrderField[] { WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.EXECUTION_ID }, false);
        public static Index WORKFLOW_RUNTIME_STATISTICS_PRIMARY = Internal.createIndex("PRIMARY", WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS, new OrderField[] { WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.WORKFLOW_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.EXECUTION_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.OPERATOR_ID, WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.TIME }, true);
        public static Index WORKFLOW_USER_ACCESS_PRIMARY = Internal.createIndex("PRIMARY", WorkflowUserAccess.WORKFLOW_USER_ACCESS, new OrderField[] { WorkflowUserAccess.WORKFLOW_USER_ACCESS.UID, WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID }, true);
        public static Index WORKFLOW_USER_ACCESS_WID = Internal.createIndex("wid", WorkflowUserAccess.WORKFLOW_USER_ACCESS, new OrderField[] { WorkflowUserAccess.WORKFLOW_USER_ACCESS.WID }, false);
        public static Index WORKFLOW_VERSION_PRIMARY = Internal.createIndex("PRIMARY", WorkflowVersion.WORKFLOW_VERSION, new OrderField[] { WorkflowVersion.WORKFLOW_VERSION.VID }, true);
        public static Index WORKFLOW_VERSION_WID = Internal.createIndex("wid", WorkflowVersion.WORKFLOW_VERSION, new OrderField[] { WorkflowVersion.WORKFLOW_VERSION.WID }, false);
    }
}
