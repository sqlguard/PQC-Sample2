/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.assessment.i18n;

/**
 * @author dtoland on Apr 17, 2007 at 12:17:19 PM
 * @see <a href="file:///./AssessmentResources.properties">AssessmentResources.properties</a>
 */
public interface AssessmentMessages {

	public String ASMT_ACCESSRULESVIOLATIONS_FAILED = "asmt.accessrulesviolations.failed";

	/** Constants for Access Rules Violations Test 13 */
	public String ASMT_ACCESSRULESVIOLATIONS_PASS = "asmt.accessrulesviolations.pass";

	public String ASMT_ADMINCOMMANDS_FAILED = "asmt.admincommands.failed";

	/** Constants for Admin Commands Test 8 */
	public String ASMT_ADMINCOMMANDS_PASS = "asmt.admincommands.pass";

	public String ASMT_AFTERHOURLOGIN_FAILED = "asmt.afterHourlogin.failed";

	public String ASMT_AFTERHOURLOGIN_NORECSFOUND = "asmt.afterHourlogin.norecordsfound";

	/** Constants for after hours logins test 4 */
	public String ASMT_AFTERHOURLOGIN_PASS = "asmt.afterHourlogin.pass";

	public String ASMT_CALLSTOXPPROCS_FAILED = "asmt.callstoxpprocs.failed";

	/** Constants for Calls to XP procs test 3 */
	public String ASMT_CALLSTOXPPROCS_PASS = "asmt.callstoxpprocs.pass";

	public String ASMT_CAS_COMPARISON = "COMPARISON";

	public String ASMT_CAS_FILE_NAME = "FILE_NAME";

	public String ASMT_CAS_FOUND = "FOUND";

	public String ASMT_CAS_MS_SERVICE = "MS_SERVICE";

	public String ASMT_CAS_NODES = "NODES";

	public String ASMT_CAS_PROPERTY_NAME = "PROPERTY_NAME";

	public String ASMT_CAS_TEST_VALUE = "TEST_VALUE";

	public String ASMT_CAS_VERSION = "VERSION";

	public String ASMT_CLIENTSEXECADMIN_FAILED = "asmt.clientsexecadmin.failed";

	/** Constants For Clients Executing Admin Commands Test 9 */
	public String ASMT_CLIENTSEXECADMIN_PASS = "asmt.clientsexecadmin.pass";

	public String ASMT_CLIENTSEXECDDL_FAILED = "asmt.clientsexecddl.failed";

	/** Constants For Clients Executing DDL Commands Test 11 */
	public String ASMT_CLIENTSEXECDDL_PASS = "asmt.clientsexecddl.pass";

	public String ASMT_DBCCCOMMANDS_FAILED = "asmt.dbcccommands.failed";

	/** Constants for DBCC Commands Test 12 */
	public String ASMT_DBCCCOMMANDS_PASS = "asmt.dbcccommands.pass";

	public String ASMT_DDLCOMMANDS_FAILED = "asmt.ddlcommands.failed";

	/** Constants for DDL Commands Test 10 */
	public String ASMT_DDLCOMMANDS_PASS = "asmt.ddlcommands.pass";

	public String ASMT_EXCESIVEADMINLOGIN_FAILED = "asmt.excesiveadminlogin.failed";

	public String ASMT_EXCESIVEADMINLOGIN_NORECSFOUND = "asmt.excesiveadminlogin.norecordsfound";

	/** Constants for Excesive administrator logins test 2 */
	public String ASMT_EXCESIVEADMINLOGIN_PASS = "asmt.excesiveadminlogin.pass";

	/**
   * Constant For Generic Health Assessment Error
   */
  public String ASMT_GENERIC_ERROR="asmt.generic.error";
  
  public String ASMT_ERROR_RETRIEVING_OS="asmt.error.retrieving.os";
  public String ASMT_ERROR_RETRIEVING_PATCH="asmt.error.retrieving.patch";
  
	public static final String ASMT_RESULT_TESTDEPRECATED = "asmt.result.testdeprecated";
	public static final String ASMT_REC_TESTDEPRECATED = "asmt.recommend.testdeprecated";


	/** Constant for [All] literal */
	public String ASMT_LIT_ALL = "asmt.lit.all";

	/** Constant literal for "Datasources: " */
	public String ASMT_LIT_DATASOURCES = "asmt.lit.datasources";

	/** Constant for Grantable literal */
	public String ASMT_LIT_GRANT_OPT = "asmt.lit.grant.option";

	/** Constant for Not Found literal */
	public String ASMT_LIT_NOT_FOUND = "asmt.lit.not.found";

	/** Constant for Column literal */
	public String ASMT_LIT_OBJ_COLUMN = "asmt.lit.obj.column";

	/** Constant for Database literal */
	public String ASMT_LIT_OBJ_DATABASE = "asmt.lit.obj.database";

	/** Constant for Index literal */
	public String ASMT_LIT_OBJ_INDEX = "asmt.lit.obj.index";

	/** Constant for Index literal */
	public String ASMT_LIT_OBJ_LIBRARY = "asmt.lit.obj.library";

	/** Constant for Package literal */
	public String ASMT_LIT_OBJ_PACKAGE = "asmt.lit.obj.package";

	/** Constant for Index literal */
	public String ASMT_LIT_OBJ_ROUTINE = "asmt.lit.obj.routine";

	/** Constant for Schema literal */
	public String ASMT_LIT_OBJ_SCHEMA = "asmt.lit.obj.schema";

	/** Constant for Index literal */
	public String ASMT_LIT_OBJ_SEQUENCE = "asmt.lit.obj.sequence";

	/** Constant for Server literal */
	public String ASMT_LIT_OBJ_SERVER = "asmt.lit.obj.server";

	/** Constant for Table literal */
	public String ASMT_LIT_OBJ_TABLE = "asmt.lit.obj.table";

	/** Constant for Table literal */
	public String ASMT_LIT_OBJ_TABLESPACE = "asmt.lit.obj.tablespace";

	/** Constant for [Observed] literal */
	public String ASMT_LIT_OBSERVED = "asmt.lit.observed";

	/** Constant for Off literal */
	public String ASMT_LIT_OFF = "asmt.lit.off";

	/** Constant for On literal */
	public String ASMT_LIT_ON = "asmt.lit.on";

	/** Constant literal for system tempdb */
	public String ASMT_LIT_SYSTEMDB = "asmt.catalog.lit.systemdb";

	/** Constant literal for tempdb */
	public String ASMT_LIT_TEMPDB = "asmt.catalog.lit.tempdb";

	/** Constant literal for "Tests: " */
	public String ASMT_LIT_TESTS = "asmt.lit.tests";

	/** Constant literal for tempdb */
	public String ASMT_LIT_TOTAL = "asmt.lit.total";

	/** Constant for unknown literal */
	public String ASMT_LIT_UNKNOWN = "asmt.lit.unknown";

	/** Constants for Excesive login failures Tests 6 & 7 */
	public String ASMT_LOGINFAILURES_FAILED = "asmt.loginfailures.failed";

	/** Constants for Excesive login failures Tests 6 & 7 */
	public String ASMT_LOGINFAILURES_PASS = "asmt.loginfailures.pass";

	/**
	 * Constant for Admin Connection Caching Hijack message.
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_CACHE_CON = "asmt.catalog.cache.con";

	/**
	 * Constant for No Admin Connection Caching Hijack message.
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_CACHE_CON_NONE = "asmt.catalog.cache.con.none";

	/**
	 * Constant for Catalog Access Failure message.
	 * @see #ASMT_SUB_DS
	 */
	public String ASMT_MSG_CAT_ACC_FAIL = "asmt.catalog.access.failure";

	/**
	 * Constant for the Privilege Count message.
	 * @see #ASMT_SUB_COUNT
	 * @see #ASMT_SUB_DATABASE
	 */
	public String ASMT_MSG_CAT_DB_OBJ_COUNT = "asmt.catalog.database.object.count";

	/**
	 * Constant for the  dbo ownership message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_CAT_DBO_NOT_OWNS = "asmt.catalog.dbo.object.non.ownership";

	/**
	 * Constant for the  dbo ownership message.
	 */
	public String ASMT_MSG_CAT_DBO_OWNS = "asmt.catalog.dbo.object.ownership";

	/**
	 * Constant for the unique group id message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_CAT_GID_NOT_UNIQUE = "asmt.catalog.groupid.not.unique";

	/**
	 * Constant for the unique group id message.
	 */
	public String ASMT_MSG_CAT_GID_UNIQUE = "asmt.catalog.groupid.unique";

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_CAT_GUEST_ACCESS = "asmt.catalog.guest.access";

	/**
	 * Constant for the  guest access message.
	 */
	public String ASMT_MSG_CAT_GUEST_ACCESS_NONE = "asmt.catalog.guest.access.none";

	/**
	 * Constant for the System Table Privilege message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_CFG_PROD_USER_PROF = "asmt.cfg.product.user.profile";

	/**
	 * Constant for the System Table Privilege Not Found message.
	 */
	public String ASMT_MSG_CFG_PROD_USER_PROF_EMPTY = "asmt.cfg.product.user.profile.empty";

	/**
	 * Constant for the System Table Privilege Not Found message.
	 */
	public String ASMT_MSG_CFG_PROD_USER_PROF_UNDEFINED = "asmt.cfg.product.user.profile.undefined";

	/**
	 * Constant for DB Connection Failure message.
	 * @see #ASMT_SUB_DS
	 * @see #ASMT_SUB_USER
	 */
	public String ASMT_MSG_CONNECT_FAIL = "asmt.connection.failure";

	/**
	 * Constant for Full Correlation message.
	 * @see #ASMT_SUB_TYPE
	 */
	public String ASMT_MSG_CORRELATE_FULL = "asmt.correlation.full";

	/**
	 * Constant for Partial Correlation message.
	 * @see #ASMT_SUB_TYPE
	 */
	public String ASMT_MSG_CORRELATE_PART = "asmt.correlation.partial";

	/**
	 * Constant for Database Catalog Access Failure message.
	 * @see #ASMT_SUB_DS
	 * @see #ASMT_SUB_DATABASE
	 */
	public String ASMT_MSG_DB_CAT_ACC_FAIL = "asmt.database.catalog.access.failure";

	/**
	 * Constant for the  xp_cmdshell privileges message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_DB_NOT_TESTED = "asmt.catalog.database.not.tested";

	/**
	 * Constant for DB Patch Level message.
	 * @see #ASMT_SUB_TYPE
	 * @see #ASMT_SUB_VERSION
	 * @see #ASMT_SUB_PATCH
	 */
	public String ASMT_MSG_DB_PATCH = "asmt.database.version.patch";

	/**
	 * Constant for DB Patch Level message.
	 * @see #ASMT_SUB_TYPE
	 * @see #ASMT_SUB_VERSION
	 * @see #ASMT_SUB_PATCH
	 * @see #ASMT_SUB_VALUE
	 */
	public String ASMT_MSG_DB_PATCH_MATCH = "asmt.database.version.patch.match";

	/**
	 * Constant for DB Patch Level message.
	 * @see #ASMT_SUB_TYPE
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_DB_PATCH_NOT_DEFINED = "asmt.database.version.patch.not.defined";

	/**
	 * Constant for DB Patch Level message.
	 * @see #ASMT_SUB_DS
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_DB_PATCH_NOT_FOUND = "asmt.database.version.patch.not.found";

	/**
	 *	Constant for Database privilege message
	 */
	public String ASMT_MSG_DB_PRIVILEGE = "asmt.database.privilege";

	/**
	 * Constant for the  xp_cmdshell privileges message.
	 * @see #ASMT_SUB_DATABASE
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_DB_VALUES = "asmt.catalog.database.values";

	/**
	 * Constant for DB Version message.
	 * @see #ASMT_SUB_TYPE
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_DB_VERSION = "asmt.database.version";

	/**
	 * Constant for the pre-defined user default password message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_DFLT_PW = "asmt.predef.users.default.password";

	/**
	 * Constant for the pre-defined user default password message.
	 */
	public String ASMT_MSG_DFLT_PW_NONE = "asmt.predef.users.default.password.none";

	/**
	 * Constant for the Unsupported Datasource Type message.
	 * @see #ASMT_SUB_TEST
	 */
	public String ASMT_MSG_DS_NOT_ASSOC = "asmt.datasource.not.associated";

	/**
	 * Constant for the Datasource Not Associated message.
	 */
	public String ASMT_MSG_DS_SYBASEIQ_UNSUPPORTED = "asmt.datasource.sybaseiq.unsupported";

	/**
	 * Constant for the Unsupported Datasource Type message.
	 * @see #ASMT_SUB_TYPE
	 */
	public String ASMT_MSG_DS_TYPE_UNSUPPORTED = "asmt.datasource.type.unsupported";

	/**
	 * Constant for the Unsupported Datasource Type message.
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_DS_VERSION_UNSUPPORTED = "asmt.datasource.version.unsupported";

	/**
	 * Constant for the SqlGuard Group Not Found Error message.
	 * @see #ASMT_SUB_VALUE
	 */
	public String ASMT_MSG_ERROR_GROUP_NOT_FOUND = "asmt.error.group.not.found";

	/**
	 * Constant for the Not Connectable Error message.
	 */
	public String ASMT_MSG_ERROR_NO_CONNECT = "asmt.error.not.connectable";

	/**
	 * Constant for the Number Format Error message.
	 * @see #ASMT_SUB_VALUE
	 */
	public String ASMT_MSG_ERROR_NUMBER_FORMAT = "asmt.error.numberformatexception";

	/**
	 * Constant for the Date Format Error message.
	 * @see #ASMT_SUB_VALUE
	 */
	public String ASMT_MSG_ERROR_PARSE_DATE = "asmt.error.parse.date";

	/**
	 * Constant for the Type Comparison Error message.
	 * @see #ASMT_SUB_VALUE
	 * @see #ASMT_SUB_PARAMETER
	 */
	public String ASMT_MSG_ERROR_TYPE_COMPARISON = "asmt.error.comparison.type";

	/**
	 * Constant for the Fixed Server Role Found message.
	 * @see #ASMT_SUB_PRIVILEGE
	 */
	public String ASMT_MSG_FIX_SRV_ROLE_FOUND = "asmt.priv.fixSrvRole.found";

	/**
	 * Constant for the Fixed Server Role NOT Found message.
	 */
	public String ASMT_MSG_FIX_SRV_ROLE_NOT_FOUND = "asmt.priv.fixSrvRole.notFound";

	/**
	 * Constant for the GdmException create failure message.
	 */
	public String ASMT_MSG_GDM_EXCEPT_FAIL = "asmt.gdm.exception.failure";

	/**
	 * Constant for the Grant Option message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_GRANT_OPT = "asmt.catalog.grant.option";

	/**
	 * Constant for the Grant Option Not Found message.
	 */
	public String ASMT_MSG_GRANT_OPT_NONE = "asmt.catalog.grant.option.none";

	/**
	 * Constant for the Grant LOAD authorize message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_PRIV_GRANT_LOAD = "asmt.priv.grant.load";

	/**
	 * Constant for the Grant LOAD Not Found message.
	 */
	public String ASMT_PRIV_GRANT_LOAD_NONE = "asmt.priv.grant.load.none";
	
	/**
	 * Constant for the Guest Users message.
	 * @see #ASMT_SUB_DATABASE
	 */
	public String ASMT_MSG_GUEST_USERS = "asmt.catalog.guest.users";

	/**
	 * Constant for the Guest User Account message.
	 * @see #ASMT_SUB_DATABASE
	 * @see #ASMT_SUB_USER
	 */
	public String ASMT_MSG_GUEST_USERS_ACCT = "asmt.catalog.guest.user.account";

	/**
	 * Constant for the Guest Users Not Found message.
	 */
	public String ASMT_MSG_GUEST_USERS_NONE = "asmt.catalog.guest.users.none";

	/**
	 * Constant for Write to Assessment Log Cancellation message.
	 * @see #ASMT_SUB_ASMT
	 */
	public String ASMT_MSG_LOG_CANCEL = "asmt.log.cancelled";

	/**
	 * Constant for Write to Assessment Log Complete message.
	 * @see #ASMT_SUB_ASMT
	 */
	public String ASMT_MSG_LOG_COMPLETE = "asmt.log.complete";

	/**
	 * Constant for Write to Assessment Test Name message.
	 * @see #ASMT_SUB_DS
	 */
	public String ASMT_MSG_LOG_DATASOURCE_NAME = "asmt.log.datasource.name";

	/**
	 * Constant forAssessment Log Error message.
	 * @see #ASMT_SUB_TEST
	 * @see #ASMT_SUB_DS
	 */
	public String ASMT_MSG_LOG_ERROR_TEST = "asmt.log.error.test";

	/**
	 * Constant forAssessment Log Error message.
	 * @see #ASMT_SUB_TEST
	 */
	public String ASMT_MSG_LOG_ERROR_TEST_NO_DS = "asmt.log.error.test.no.datasource";

	/**
	 * Constant for Write to Assessment Log Failure message.
	 */
	public String ASMT_MSG_LOG_FAIL = "asmt.log.failure";

	/**
	 * Constant forAssessment Failure message.
	 * @see #ASMT_SUB_ASMT
	 */
	public String ASMT_MSG_LOG_FATAL = "asmt.log.fatal";

	/**
	 * Constant for Assessment Log Start message.
	 * @see #ASMT_SUB_ASMT
	 */
	public String ASMT_MSG_LOG_START = "asmt.log.start";

	/**
	 * Constant for Write to Assessment Test Name message.
	 * @see #ASMT_SUB_TEST
	 */
	public String ASMT_MSG_LOG_TEST_NAME = "asmt.log.test.name";

	/**
	 * Constant for DB Parameter message.
	 * @see #ASMT_SUB_PARAMETER
	 * @see #ASMT_SUB_VALUE
	 */
	public String ASMT_MSG_PARAMETER = "asmt.database.parameter";

	/**
	 * Constant for DB Parameter Deprecated message.
	 * @see #ASMT_SUB_PARAMETER
	 * @see #ASMT_SUB_VALUE
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_PARAMETER_DEPRECATED = "asmt.database.parameter.deprecated";

	/**
	 * Constant for DB Parameter Directory Function message.
	 * @see #ASMT_SUB_PARAMETER
	 * @see #ASMT_SUB_VALUE
	 * @see #ASMT_SUB_FUNCTION
	 */
	public String ASMT_MSG_PARAMETER_DIR = "asmt.database.parameter.directory.function";

	/**
	 * Constant for DB Parameter Aquisisiont Failure message.
	 * @see #ASMT_SUB_PARAMETER
	 */
	public String ASMT_MSG_PARAMETER_FAIL = "asmt.database.parameter.failure";

	/**
	 * Constant for DB Parameter Deprecated message.
	 * @see #ASMT_SUB_PARAMETER
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_PARAMETER_INVALID = "asmt.database.parameter.invalid";

	/**
	 * Constant for DB Parameter Not Set message.
	 * @see #ASMT_SUB_PARAMETER
	 */
	public String ASMT_MSG_PARAMETER_NOT_SET = "asmt.database.parameter.notset";

	/**
	 * Constant for DB Parameter Obsoleted message.
	 * @see #ASMT_SUB_PARAMETER
	 * @see #ASMT_SUB_VALUE
	 * @see #ASMT_SUB_VERSION
	 */
	public String ASMT_MSG_PARAMETER_OBSOLETED = "asmt.database.parameter.obsoleted";

	/**
	 * Constant for the Predefined DB Role Found Granted to public or guest message.
	 * @see #ASMT_SUB_DATABASE
	 */
	public String ASMT_MSG_PREDEF_DB_ROLE_FOUND = "asmt.testResultText.preDefDbRole.found";

	/**
	 * Constant for the Predefined DB Role NOT Found Granted to public or guest message.
	 */
	public String ASMT_MSG_PREDEF_DB_ROLE_NOT_FOUND = "asmt.testResultText.preDefDbRole.notFound";

	/**
	 * Constant for the become or alter user message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_BECOME_USER = "asmt.priv.become.user";

	/**
	 * Constant for the become or alter user message.
	 */
	public String ASMT_MSG_PRIV_BECOME_USER_NONE = "asmt.priv.become.user.none";

	/**
	 * Constant for the  builtin sysadmin message.
	 */
	public String ASMT_MSG_PRIV_BUILTIN_SYSADMIN = "asmt.priv.builtin.sysadmin";

	/**
	 * Constant for the  builtin sysadmin message.
	 */
	public String ASMT_MSG_PRIV_BUILTIN_SYSADMIN_NOT = "asmt.priv.builtin.sysadmin.not";

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_CAT_ROLE = "asmt.priv.cat.role.privs";

	/**
	 * Constant for the  guest access message.
	 */
	public String ASMT_MSG_PRIV_CAT_ROLE_NONE = "asmt.priv.cat.role.privs.none";

	/**
	 * Constant for the Object Count message.
	 * @see #ASMT_SUB_COUNT
	 * @see #ASMT_SUB_DB_OBJ
	 */
	public String ASMT_MSG_PRIV_COUNT = "asmt.priv.count";

	/**
	 * Constant for the become or create library message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_CREATE_LIB = "asmt.priv.create.library";

	/**
	 * Constant for the become or create library message.
	 */
	public String ASMT_MSG_PRIV_CREATE_LIB_NONE = "asmt.priv.create.library.none";

	/**
	 * Constant for the DBA_ROLE_PRIVS table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBA_ROLE_PRIVS = "asmt.priv.dba_role_privs";

	/**
	 * Constant for the DBA_ROLE_PRIVS table access message.
	 */
	public String ASMT_MSG_PRIV_DBA_ROLE_PRIVS_NONE = "asmt.priv.dba_role_privs.none";
	/**
	 * Constant for the  X$ table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBA_ROLES = "asmt.priv.dba_roles";

	/**
	 * Constant for the  X$ table access message.
	 */
	public String ASMT_MSG_PRIV_DBA_ROLES_NONE = "asmt.priv.dba_roles.none";

	/**
	 * Constant for the DBA_SYS_PRIVS table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBA_SYS_PRIVS = "asmt.priv.dba_sys_privs";

	/**
	 * Constant for the DBA_SYS_PRIVS table access message.
	 */
	public String ASMT_MSG_PRIV_DBA_SYS_PRIVS_NONE = "asmt.priv.dba_sys_privs.none";
	
	/**
	 * Constant for the DBA_TAB_PRIVS table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBA_TAB_PRIVS = "asmt.priv.dba_tab_privs";

	/**
	 * Constant for the DBA_TAB_PRIVS table access message.
	 */
	public String ASMT_MSG_PRIV_DBA_TAB_PRIVS_NONE = "asmt.priv.dba_tab_privs.none";

	/**
	 * Constant for the  X$ table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBA_VIEW = "asmt.priv.dba.views";

	/**
	 * Constant for the  X$ table access message.
	 */
	public String ASMT_MSG_PRIV_DBA_VIEW_NONE = "asmt.priv.dba.views.none";

	/**
	 * Constant for the dbo alias count message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_PRIV_DBO_ALIAS = "asmt.priv.dbo.alias";

	/**
	 * Constant for the  dbo alias count exceeded message.
	 * @see #ASMT_SUB_COUNT
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DBO_ALIAS_EXCEED = "asmt.priv.dbo.alias.exceeded";

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_DEFAULT_ROLE_GRANTS = "asmt.priv.default.role.grants";

	/**
	 * Constant for the grant option message.
	 */
	public String ASMT_MSG_PRIV_DEFAULT_ROLE_GRANTS_NONE = "asmt.priv.default.role.grants.none";

	/**
	 * Constant for the grant option message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_GRANT_OPTION = "asmt.priv.grant.option";

	/**
	 * Constant for the grant option message.
	 */
	public String ASMT_MSG_PRIV_GRANT_OPTION_NONE = "asmt.priv.grant.option.none";

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_OLE_PROC = "asmt.priv.ole.proc";

	/**
	 * Constant for the  guest access message.
	 */
	public String ASMT_MSG_PRIV_OLE_PROC_NONE = "asmt.priv.ole.proc.none";

	/**
	 * Constant for the SYS.AUD$ table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_SYS_AUD = "asmt.priv.sys.aud";

	/**
	 * Constant for the SYS.AUD$ table access message.
	 */
	public String ASMT_MSG_PRIV_SYS_AUD_NONE = "asmt.priv.sys.aud.none";

	/**
	 * Constant for the system table select message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_SYSTEM_TABLE_SELECT = "asmt.priv.system.table.select";

	/**
	 * Constant for the system table select message.
	 */
	public String ASMT_MSG_PRIV_SYSTEM_TABLE_SELECT_NONE = "asmt.priv.system.table.select.none";

	/**
	 * Constant for the User Privilege message.
	 * @see #ASMT_SUB_USER
	 * @see #ASMT_SUB_PRIVILEGE
	 * @see #ASMT_SUB_GRANT_OPT
	 */
	public String ASMT_MSG_PRIV_USER = "asmt.priv.user";

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_USER_ANY_ACCESS = "asmt.priv.user.any";

	/**
	 * Constant for the grant option message.
	 */
	public String ASMT_MSG_PRIV_USER_ANY_ACCESS_NONE = "asmt.priv.user.any.none";

	/**
	 * Constant for the User Privilege message.
	 * @see #ASMT_SUB_USER
	 */
	public String ASMT_MSG_PRIV_USER_CONNECT = "asmt.priv.user.connect";

	/**
	 * Constant for the User Privilege List message.
	 * @see #ASMT_SUB_PRIVILEGE
	 */
	public String ASMT_MSG_PRIV_USER_LIST = "asmt.priv.user.list";

	/**
	 * Constant for the User Privilege Not Found message.
	 */
	public String ASMT_MSG_PRIV_USER_NONE = "asmt.priv.user.none";

	/**
	 * Constant for the User Object Privilege message.
	 * @see #ASMT_SUB_USER
	 * @see #ASMT_SUB_DB_OBJ
	 */
	public String ASMT_MSG_PRIV_USER_OBJ = "asmt.priv.user.object";
	
	public String ASMT_MSG_PRIV_OBJ = "asmt.priv.object";
	

	/**
	 * Constant for the  guest access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_USER_USER_ACCESS = "asmt.priv.user.access.users";

	/**
	 * Constant for the grant option message.
	 */
	public String ASMT_MSG_PRIV_USER_USER_ACCESS_NONE = "asmt.priv.user.access.users.none";

	/**
	 * Constant for the V$ synonym access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_V$_SYNONYM = "asmt.priv.v$.synonym";

	/**
	 * Constant for the V$ synonym access message.
	 */
	public String ASMT_MSG_PRIV_V$_SYNONYM_NONE = "asmt.priv.v$.synonym.none";

	/**
	 * Constant for the V$ View access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_V$_VIEW = "asmt.priv.v$.view";

	/**
	 * Constant for the V$ View access message.
	 */
	public String ASMT_MSG_PRIV_V$_VIEW_NONE = "asmt.priv.v$.view.none";

	/**
	 * Constant for the  X$ table access message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_X$_TABLE = "asmt.priv.x$.table";

	/**
	 * Constant for the  X$ table access message.
	 */
	public String ASMT_MSG_PRIV_X$_TABLE_NONE = "asmt.priv.x$.table.none";

	/**
	 * Constant for the XP_CMDSHELL privileges message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_PRIV_XP_CMDSHELL = "asmt.priv.xp_cmdshell";

	/**
	 * Constant for the XP_CMDSHELL privileges message.
	 */
	public String ASMT_MSG_PRIV_XP_CMDSHELL_NONE = "asmt.priv.xp_cmdshell.none";

	/**
	 * Constant for the Public Privilege message.
	 * @see #ASMT_SUB_PRIVILEGE
	 */
	public String ASMT_MSG_PUB_PRIV = "asmt.priv.public";

	/**
	 * Constant for the Public Privilege List message.
	 * @see #ASMT_SUB_PRIVILEGE
	 */
	public String ASMT_MSG_PUB_PRIV_LIST = "asmt.priv.public.list";

	/**
	 * Constant for the Public Privilege Not Found message.
	 */
	public String ASMT_MSG_PUB_PRIV_NONE = "asmt.priv.public.list.none";

	/**
	 * Constant for the Error Score Recommendation.
	 */
	public String ASMT_MSG_REC_SCORE_ERROR = "asmt.recommend.score.error";

	/**
	 * Constant for the No CAS Score Recommendation.
	 */
	public String ASMT_MSG_REC_SCORE_NO_CAS = "asmt.recommend.score.no.cas";

	/**
	 * Constant for the No Data Found Score Recommendation.
	 */
	public String ASMT_MSG_REC_SCORE_NO_DATA_FOUND = "asmt.recommend.score.no.data.found";

	/**
	 * Constant for the No Recommendation text found for the score.
	 * @see #ASMT_SUB_SCORE
	 * @see #ASMT_SUB_TEST
	 */
	public String ASMT_MSG_REC_SCORE_NOT_FOUND = "asmt.recommend.score.not.found";

	/**
	 * Constant for the NoCAS Score Recommendation.
	 */
	public String ASMT_MSG_REC_SCORE_UNSUPPORTED_VERSION = "asmt.recommend.score.unsupported.version";

	/**
	 * Constant for the Sample Database message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_SAMPLE_DB = "asmt.catalog.sample.database";

  /**
	 * Constant for the Sample Database Not Found message.
	 */
	public String ASMT_MSG_SAMPLE_DB_NONE = "asmt.catalog.sample.database.none";

  /**
	 * Constant for the System Table Privilege message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_SYS_PRIV = "asmt.catalog.system.privilege";

  /**
	 * Constant for the System Table Privilege Not Found message.
	 */
	public String ASMT_MSG_SYS_PRIV_NONE = "asmt.catalog.system.privilege.none";

  /**
	 * Constant for the Torque Exception message.
	 */
	public String ASMT_MSG_TORQUE_EXCEPTION = "asmt.torque.failure";

  /**
	 * Constant for the truncate log on checkpoint message.
	 * @see #ASMT_SUB_OBJ_LIST
	 */
	public String ASMT_MSG_TRUNC_LOG_ON_CHKPT = "asmt.cfg.trunc.log.on.chkpt";

  /**
	 * Constant for the truncate log on checkpoint message.
	 */
	public String ASMT_MSG_TRUNC_LOG_ON_CHKPT_NONE = "asmt.cfg.trunc.log.on.chkpt.none";

  /**
	 * Constant for the Non-Unique Remote User message.
	 * @see #ASMT_SUB_COUNT
	 */
	public String ASMT_MSG_UNIQ_REMOTE = "asmt.catalog.unique.remote";

	/**
	 * Constant for the Non-Unique Remote User Not Found message.
	 */
	public String ASMT_MSG_UNIQ_REMOTE_NONE = "asmt.catalog.unique.remote.none";

	/**
	 * Constant for the Non-Unique Remote User message.
	 * @see #ASMT_SUB_REMOTE_USER
	 * @see #ASMT_SUB_DATABASE
	 * @see #ASMT_SUB_USER
	 */
	public String ASMT_MSG_UNIQ_REMOTE_USR = "asmt.catalog.unique.remote.user";

	/**
	 * Constant for the Unsupported Operator message.
	 * @see #ASMT_SUB_OPERATOR
	 */
	public String ASMT_MSG_UNSUPPORTED_OPERATOR = "asmt.error.unsupported.operator";

    /**
		 * Constant for the Individual User Privilege message.
		 * @see #ASMT_SUB_COUNT
		 */
		public String ASMT_MSG_USER_PRIV = "asmt.catalog.user.privilege";
    /**
		 * Constant for the Individual User Privilege Not Found message.
		 */
		public String ASMT_MSG_USER_PRIV_NONE = "asmt.catalog.user.privilege.none";
    /**
		 * Constant for the Mismatched User message.
		 * @see #ASMT_SUB_COUNT
		 */
		public String ASMT_MSG_USR_MISMATCH = "asmt.catalog.user.mismatch";

    /**
		 * Constant for the Mismatched User Not Found message.
		 */
		public String ASMT_MSG_USR_MISMATCH_NONE = "asmt.catalog.user.mismatch.none";
    /**
		 * Constant for the Orphaned User message.
		 * @see #ASMT_SUB_COUNT
		 */
		public String ASMT_MSG_USR_ORPHAN = "asmt.catalog.user.orphan";
    /**
		 * Constant for the System Orphaned User Not Found message.
		 */
		public String ASMT_MSG_USR_ORPHAN_NONE = "asmt.catalog.user.orphan.none";

    /**
		 * Constant for the  xp_cmdshell privileges message.
		 * @see #ASMT_SUB_COUNT
		 */
		public String ASMT_MSG_XPCMD_PRIV = "asmt.catalog.xpcmdshell.privilege";
    /**
		 * Constant for the xp_cmdshell privileges Not Found message.
		 */
		public String ASMT_MSG_XPCMD_PRIV_NONE = "asmt.catalog.xpcmdshell.privilege.none";

    public String ASMT_ONEUSERONEIP_FAILED = "asmt.oneUserOneIP.failed";

    public String ASMT_ONEUSERONEIP_NORECSFOUND = "asmt.oneUserOneIP.norecordsFound";

    /** Constants for One User One IP Test 1  */
    public String ASMT_ONEUSERONEIP_PASS = "asmt.oneUserOneIP.pass";

    public String ASMT_SQLERRORS_FAILED = "asmt.sqlerrors.failed";

    public String ASMT_SQLERRORS_NORECSFOUND = "asmt.sqlerrors.norecordsfound";

    /** Constants For SQL Errors Test 5 */
    public String ASMT_SQLERRORS_PASS = "asmt.sqlerrors.pass";

    /** Constant for the Assessment Symbol */
		public String ASMT_SUB_ASMT = "assessment";

    /** Constant for the Count Symbol */
		public String ASMT_SUB_COUNT = "count";

    /** Constant for the Databases Name Symbol */
		public String ASMT_SUB_DATABASE = "database";

    /** Constant for the Database Object Symbol */
		public String ASMT_SUB_DB_OBJ = "database-object";

    /** Constant for the Datasource Name Symbol */
		public String ASMT_SUB_DS = "datasource";

    public String ASMT_SUB_ERROR_DESC = "error_desc";

    /** Constant for the Function Symbol */
		public String ASMT_SUB_FUNCTION = "function";

    /** Constant for the Grant Option Symbol */
		public String ASMT_SUB_GRANT_OPT = "grant-option";

    /** Constant for the Database Object Symbol */
		public String ASMT_SUB_OBJ_LIST = "object-list";

    /** Constant for the Operator Object Symbol */
		public String ASMT_SUB_OPERATOR = "operator";

    /** Constant for the Datasource Parameter Symbol */
		public String ASMT_SUB_PARAMETER = "parameter";

    /** Constant for the Password Symbol */
		public String ASMT_SUB_PASSWD = "password";

    /** Constant for the Database Patch Level Symbol */
		public String ASMT_SUB_PATCH = "patch";

    /** Constant for the Privilege Symbol */
		public String ASMT_SUB_PRIVILEGE = "privileges";

    /** Constant for the User Name Symbol */
		public String ASMT_SUB_REMOTE_USER = "remote-user";

    /** Constant for the Score Symbol */
		public String ASMT_SUB_SCORE = "score";

    /** Constant for the Test Symbol */
		public String ASMT_SUB_TEST = "test";

    /** Constant for the Type Symbol */
		public String ASMT_SUB_TYPE = "type";

    /** Constant for the Database Url Symbol */
		public String ASMT_SUB_URL = "url";

    /** Constant for the User Name Symbol */
		public String ASMT_SUB_USER = "user";

    /** Constant for the Database Parameter Value Symbol */
		public String ASMT_SUB_VALUE = "value";

    /** Constant for the Database Version Symbol */
		public String ASMT_SUB_VERSION = "version";

		/** Constant for the Database role Symbol */
		public String ASMT_SUB_ROLE = "role";	
	
		/** Constant for mongodb role test  */ 	
		public static final String ASMT_ROLE_TEXT_PASS = "asmt.testResultText.mongodb.roles.pass";
		public static final String ASMT_ROLE_TEXT_FAILED = "asmt.testResultText.mongodb.roles.fail";
	
		/** Constant for Aster user with default password test  */ 	
		public static final String ASMT_DEFAULT_PASSWORD_PASS = "asmt.testResultText.Aster.DefaultPassword.pass";
		public static final String ASMT_DEFAULT_PASSWORD_FAILED = "asmt.testResultText.Aster.DefaultPassword.fail";		
	
	/** Constant for test not applicable to os */ 	
	public static final String ASMT_CVE_OS_NA = "asmt.cve.os.na";
	/** Constant for recommendation text for cve tests */
	public static final String ASMT_REC_CVE_PASSED = "asmt.rec.cve.passed";
	public static final String ASMT_REC_CVE_FAILED = "asmt.rec.cve.failed";
	public static final String ASMT_REC_CVE_ERROR = "asmt.rec.cve.error";
	public static final String ASMT_CVE_TEXT_PASS ="asmt.cve.text.pass"; 
	public static final String ASMT_CVE_TEXT_FAILED = "asmt.cve.text.failed";
	public static final String ASMT_CVE_NOT_REPORTED = "asmt.cve.text.not.reported";
	public static final String ASMT_CVE_CAS_REQUIRED= "asmt.cve.cas.required";
	public static final String ASMT_SQLBASED_DETAIL_ERROR= "asmt.sqlbased.details.error";
	public static final String ASMT_CVE_PATCH_NOT_DETECTED="asmt.cve.text.patch.not.detected";
	
	/** Constant for recommendation text for apar tests */
	public static final String ASMT_REC_APAR_PASSED = "asmt.rec.apar.passed";
	public static final String ASMT_REC_APAR_FAILED = "asmt.rec.apar.failed";
	public static final String ASMT_REC_APAR_ERROR = "asmt.rec.apar.error";
	public static final String ASMT_APAR_TEXT_PASS ="asmt.apar.text.pass"; 
	public static final String ASMT_APAR_TEXT_FAILED = "asmt.apar.text.failed";
	public static final String ASMT_APAR_NOT_REPORTED = "asmt.apar.text.not.reported";
	public static final String ASMT_APAR_CAS_REQUIRED= "asmt.apar.cas.required";
	public static final String ASMT_APAR_MODULES_NOT_PRESENT= "asmt.apar.text.modulesnotpresent";

	/** Constant for generic SQL Based tests Errors */
	public static final String ASMT_ERROR_SQL_NO_RECS = "asmt.error.sql.no.recs";
	public static final String ASMT_ERROR_PRE_TEST_ERROR = "asmt.error.pretest.error";
	
	/** Constant For No Databases To Loop */
	public static final String ASMT_ERROR_NO_DBS_TO_LOOP= "asmt.error.no.databases.to.loop";
	
	/** Constant for SQL Based Error Checking Version */
	public static final String ASMT_ERROR_CHECK_VERSION = "asmt.error.check.ver";
	
	/** Constant for Exception group Not Found */ 	
	public static final String ASMT_EXCEPTION_GROUP_NOT_FOUND = "asmt.exception.group.not.found";
	
	/** Constants for OLEDB Disallowed access test (205)*/
	public static final String ASMT_OLEDB_DISALLOWED_ACCESS_PASS = "asmt.cfg.oledb.disallowadhoc.access.pass";
	public static final String ASMT_OLEDB_DISALLOWED_ACCESS_FAIL = "asmt.cfg.oledb.disallowadhoc.access.fail";
	
	/** Constant for Exception Pass result */ 	
	public static final String ASMT_RESULT_PASS_EXCEPTION = "asmt.result.pass.exception";
	
	
	public static final String ASMT_NO_USER_DATA = "asmt.no.user.data";
	
	public static final String ASMT_RESULT_FAIL_ADDITIONAL_TEXT = "asmt.result.fail.additional_text";
	
	public static final String ASMT_ID_USED="asmt.result.id.is.used";
	public static final String ASMT_NOT_IN_USER_GROUP_TABLES="asmt.result.missing.user.or.group";
	public static final String ASMT_ROLE_NOT_FOUND = "asmt.result.role.not.defined";
	public static final String ASMT_VALUE_IS_DEFAULT= "asmt.result.id.is.default";
}