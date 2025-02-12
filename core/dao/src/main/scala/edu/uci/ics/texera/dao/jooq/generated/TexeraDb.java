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
import edu.uci.ics.texera.dao.jooq.generated.tables.UserActivity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TexeraDb extends SchemaImpl {

    private static final long serialVersionUID = -745081464;

    /**
     * The reference instance of <code>texera_db</code>
     */
    public static final TexeraDb TEXERA_DB = new TexeraDb();

    /**
     * The table <code>texera_db.dataset</code>.
     */
    public final Dataset DATASET = edu.uci.ics.texera.dao.jooq.generated.tables.Dataset.DATASET;

    /**
     * The table <code>texera_db.dataset_user_access</code>.
     */
    public final DatasetUserAccess DATASET_USER_ACCESS = edu.uci.ics.texera.dao.jooq.generated.tables.DatasetUserAccess.DATASET_USER_ACCESS;

    /**
     * The table <code>texera_db.dataset_version</code>.
     */
    public final DatasetVersion DATASET_VERSION = edu.uci.ics.texera.dao.jooq.generated.tables.DatasetVersion.DATASET_VERSION;

    /**
     * The table <code>texera_db.operator_executions</code>.
     */
    public final OperatorExecutions OPERATOR_EXECUTIONS = edu.uci.ics.texera.dao.jooq.generated.tables.OperatorExecutions.OPERATOR_EXECUTIONS;

    /**
     * The table <code>texera_db.operator_port_executions</code>.
     */
    public final OperatorPortExecutions OPERATOR_PORT_EXECUTIONS = edu.uci.ics.texera.dao.jooq.generated.tables.OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS;

    /**
     * The table <code>texera_db.project</code>.
     */
    public final Project PROJECT = edu.uci.ics.texera.dao.jooq.generated.tables.Project.PROJECT;

    /**
     * The table <code>texera_db.project_user_access</code>.
     */
    public final ProjectUserAccess PROJECT_USER_ACCESS = edu.uci.ics.texera.dao.jooq.generated.tables.ProjectUserAccess.PROJECT_USER_ACCESS;

    /**
     * The table <code>texera_db.public_project</code>.
     */
    public final PublicProject PUBLIC_PROJECT = edu.uci.ics.texera.dao.jooq.generated.tables.PublicProject.PUBLIC_PROJECT;

    /**
     * The table <code>texera_db.user</code>.
     */
    public final User USER = edu.uci.ics.texera.dao.jooq.generated.tables.User.USER;

    /**
     * The table <code>texera_db.user_activity</code>.
     */
    public final UserActivity USER_ACTIVITY = edu.uci.ics.texera.dao.jooq.generated.tables.UserActivity.USER_ACTIVITY;

    /**
     * The table <code>texera_db.user_config</code>.
     */
    public final UserConfig USER_CONFIG = edu.uci.ics.texera.dao.jooq.generated.tables.UserConfig.USER_CONFIG;

    /**
     * The table <code>texera_db.workflow</code>.
     */
    public final Workflow WORKFLOW = edu.uci.ics.texera.dao.jooq.generated.tables.Workflow.WORKFLOW;

    /**
     * The table <code>texera_db.workflow_executions</code>.
     */
    public final WorkflowExecutions WORKFLOW_EXECUTIONS = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowExecutions.WORKFLOW_EXECUTIONS;

    /**
     * The table <code>texera_db.workflow_of_project</code>.
     */
    public final WorkflowOfProject WORKFLOW_OF_PROJECT = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowOfProject.WORKFLOW_OF_PROJECT;

    /**
     * The table <code>texera_db.workflow_of_user</code>.
     */
    public final WorkflowOfUser WORKFLOW_OF_USER = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowOfUser.WORKFLOW_OF_USER;

    /**
     * The table <code>texera_db.workflow_user_access</code>.
     */
    public final WorkflowUserAccess WORKFLOW_USER_ACCESS = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserAccess.WORKFLOW_USER_ACCESS;

    /**
     * The table <code>texera_db.workflow_user_clones</code>.
     */
    public final WorkflowUserClones WORKFLOW_USER_CLONES = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserClones.WORKFLOW_USER_CLONES;

    /**
     * The table <code>texera_db.workflow_user_likes</code>.
     */
    public final WorkflowUserLikes WORKFLOW_USER_LIKES = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowUserLikes.WORKFLOW_USER_LIKES;

    /**
     * The table <code>texera_db.workflow_version</code>.
     */
    public final WorkflowVersion WORKFLOW_VERSION = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowVersion.WORKFLOW_VERSION;

    /**
     * The table <code>texera_db.workflow_view_count</code>.
     */
    public final WorkflowViewCount WORKFLOW_VIEW_COUNT = edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowViewCount.WORKFLOW_VIEW_COUNT;

    /**
     * No further instances allowed
     */
    private TexeraDb() {
        super("texera_db", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Dataset.DATASET,
            DatasetUserAccess.DATASET_USER_ACCESS,
            DatasetVersion.DATASET_VERSION,
            OperatorExecutions.OPERATOR_EXECUTIONS,
            OperatorPortExecutions.OPERATOR_PORT_EXECUTIONS,
            Project.PROJECT,
            ProjectUserAccess.PROJECT_USER_ACCESS,
            PublicProject.PUBLIC_PROJECT,
            User.USER,
            UserActivity.USER_ACTIVITY,
            UserConfig.USER_CONFIG,
            Workflow.WORKFLOW,
            WorkflowExecutions.WORKFLOW_EXECUTIONS,
            WorkflowOfProject.WORKFLOW_OF_PROJECT,
            WorkflowOfUser.WORKFLOW_OF_USER,
            WorkflowUserAccess.WORKFLOW_USER_ACCESS,
            WorkflowUserClones.WORKFLOW_USER_CLONES,
            WorkflowUserLikes.WORKFLOW_USER_LIKES,
            WorkflowVersion.WORKFLOW_VERSION,
            WorkflowViewCount.WORKFLOW_VIEW_COUNT);
    }
}
