/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated;


import edu.uci.ics.texera.web.model.jooq.generated.tables.File;
import edu.uci.ics.texera.web.model.jooq.generated.tables.KeywordDictionary;
import edu.uci.ics.texera.web.model.jooq.generated.tables.User;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserDictionary;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Workflow;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserAccess;


/**
 * Convenience access to all tables in texera_db
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>texera_db.file</code>.
     */
    public static final File FILE = File.FILE;

    /**
     * The table <code>texera_db.keyword_dictionary</code>.
     */
    public static final KeywordDictionary KEYWORD_DICTIONARY = KeywordDictionary.KEYWORD_DICTIONARY;

    /**
     * The table <code>texera_db.user</code>.
     */
    public static final User USER = User.USER;

    /**
     * The table <code>texera_db.user_dictionary</code>.
     */
    public static final UserDictionary USER_DICTIONARY = UserDictionary.USER_DICTIONARY;

    /**
     * The table <code>texera_db.workflow</code>.
     */
    public static final Workflow WORKFLOW = Workflow.WORKFLOW;

    /**
     * The table <code>texera_db.workflow_of_user</code>.
     */
    public static final WorkflowOfUser WORKFLOW_OF_USER = WorkflowOfUser.WORKFLOW_OF_USER;

    /**
     * The table <code>texera_db.workflow_user_access</code>.
     */
    public static final WorkflowUserAccess WORKFLOW_USER_ACCESS = WorkflowUserAccess.WORKFLOW_USER_ACCESS;
}
