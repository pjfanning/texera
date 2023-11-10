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


/**
 * Convenience access to all tables in texera_db
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>texera_db.environment</code>.
     */
    public static final Environment ENVIRONMENT = Environment.ENVIRONMENT;

    /**
     * The table <code>texera_db.file</code>.
     */
    public static final File FILE = File.FILE;

    /**
     * The table <code>texera_db.file_of_project</code>.
     */
    public static final FileOfProject FILE_OF_PROJECT = FileOfProject.FILE_OF_PROJECT;

    /**
     * The table <code>texera_db.file_of_workflow</code>.
     */
    public static final FileOfWorkflow FILE_OF_WORKFLOW = FileOfWorkflow.FILE_OF_WORKFLOW;

    /**
     * The table <code>texera_db.input_of_environment</code>.
     */
    public static final InputOfEnvironment INPUT_OF_ENVIRONMENT = InputOfEnvironment.INPUT_OF_ENVIRONMENT;

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
     * The table <code>texera_db.user_file_access</code>.
     */
    public static final UserFileAccess USER_FILE_ACCESS = UserFileAccess.USER_FILE_ACCESS;

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
     * The table <code>texera_db.workflow_version</code>.
     */
    public static final WorkflowVersion WORKFLOW_VERSION = WorkflowVersion.WORKFLOW_VERSION;
}
