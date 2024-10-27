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
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserActivity;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserClones;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserLikes;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowVersion;


/**
 * Convenience access to all tables in texera_db
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>texera_db.dataset</code>.
     */
    public static final Dataset DATASET = Dataset.DATASET;

    /**
     * The table <code>texera_db.dataset_user_access</code>.
     */
    public static final DatasetUserAccess DATASET_USER_ACCESS = DatasetUserAccess.DATASET_USER_ACCESS;

    /**
     * The table <code>texera_db.dataset_version</code>.
     */
    public static final DatasetVersion DATASET_VERSION = DatasetVersion.DATASET_VERSION;

    /**
     * The table <code>texera_db.project</code>.
     */
    public static final Project PROJECT = Project.PROJECT;

    /**
     * The table <code>texera_db.project_user_access</code>.
     */
    public static final ProjectUserAccess PROJECT_USER_ACCESS = ProjectUserAccess.PROJECT_USER_ACCESS;

    /**
     * The table <code>texera_db.public_project</code>.
     */
    public static final PublicProject PUBLIC_PROJECT = PublicProject.PUBLIC_PROJECT;

    /**
     * The table <code>texera_db.user</code>.
     */
    public static final User USER = User.USER;

    /**
     * The table <code>texera_db.user_config</code>.
     */
    public static final UserConfig USER_CONFIG = UserConfig.USER_CONFIG;

    /**
     * The table <code>texera_db.workflow</code>.
     */
    public static final Workflow WORKFLOW = Workflow.WORKFLOW;

    /**
     * The table <code>texera_db.workflow_executions</code>.
     */
    public static final WorkflowExecutions WORKFLOW_EXECUTIONS = WorkflowExecutions.WORKFLOW_EXECUTIONS;

    /**
     * The table <code>texera_db.workflow_of_project</code>.
     */
    public static final WorkflowOfProject WORKFLOW_OF_PROJECT = WorkflowOfProject.WORKFLOW_OF_PROJECT;

    /**
     * The table <code>texera_db.workflow_of_user</code>.
     */
    public static final WorkflowOfUser WORKFLOW_OF_USER = WorkflowOfUser.WORKFLOW_OF_USER;

    /**
     * The table <code>texera_db.workflow_runtime_statistics</code>.
     */
    public static final WorkflowRuntimeStatistics WORKFLOW_RUNTIME_STATISTICS = WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS;

    /**
     * The table <code>texera_db.workflow_user_access</code>.
     */
    public static final WorkflowUserAccess WORKFLOW_USER_ACCESS = WorkflowUserAccess.WORKFLOW_USER_ACCESS;

    /**
     * The table <code>texera_db.workflow_user_activity</code>.
     */
    public static final WorkflowUserActivity WORKFLOW_USER_ACTIVITY = WorkflowUserActivity.WORKFLOW_USER_ACTIVITY;

    /**
     * The table <code>texera_db.workflow_user_clones</code>.
     */
    public static final WorkflowUserClones WORKFLOW_USER_CLONES = WorkflowUserClones.WORKFLOW_USER_CLONES;

    /**
     * The table <code>texera_db.workflow_user_likes</code>.
     */
    public static final WorkflowUserLikes WORKFLOW_USER_LIKES = WorkflowUserLikes.WORKFLOW_USER_LIKES;

    /**
     * The table <code>texera_db.workflow_version</code>.
     */
    public static final WorkflowVersion WORKFLOW_VERSION = WorkflowVersion.WORKFLOW_VERSION;
}
