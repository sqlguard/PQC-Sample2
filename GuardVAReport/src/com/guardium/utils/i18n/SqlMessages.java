/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils.i18n;

/**
 * @author dtoland on Aug 2, 2006 at 10:30:12 AM
 */
public interface SqlMessages {
	/** Constant for SQL Driver Info literal */
	public String SQL_LIT_DRIVER_INFO = "sql.driver.info";

	/** Constant for SQL Exception literal */
	public String SQL_LIT_EXCEPT = "sql.except";

	/** Constant for Connection Warnings literal */
	public String SQL_LIT_WARN_CON = "sql.warn.con";

	/** Constant for Result Set Warnings literal */
	public String SQL_LIT_WARN_RS = "sql.warn.rs";

	/** Constant for Statement Warnings literal */
	public String SQL_LIT_WARN_STMT = "sql.warn.stmt";

	/**
	 * Constant for grid column data read failure message.
	 * @see #SQL_SUB_COLUMN
	 * @see #SQL_SUB_MSG
	 */
	public String SQL_MSG_COL_DATA_FAIL = "sql.column.access.failure";

	/**
	 * Constant for the Connection info failure message
	 * @see #SQL_SUB_WARN_OBJECT
	 */
	public String SQL_MSG_CON_NOT_FOUND= "sql.con.not.found";

	/**
	 * Constant for the Connection Failure message
	 * @see #SQL_SUB_URL
	 * @see #SQL_SUB_USER
	 */
	public String SQL_MSG_CONNECT_FAILURE= "sql.connect.failure";

	/**
	 * Constant for the Connection Timeout message
	 * @see #SQL_SUB_URL
	 * @see #SQL_SUB_USER
	 * @see #SQL_SUB_TIME
	 */
	public String SQL_MSG_CONNECT_TIMEOUT= "sql.connect.timeout";

	/**
	 * Constant for the sql exception or warning detail line.
	 * @see #SQL_SUB_ERRORCODE
	 * @see #SQL_SUB_STATE
	 * @see #SQL_SUB_MSG
	 */
	public String SQL_MSG_DETAIL = "sql.detail";

	/**
	 * Constant for the Driver Info detail line
	 * @see #SQL_SUB_PROD_NAME
	 * @see #SQL_SUB_PROD_VER
	 */
	public String SQL_MSG_DRIVER_INFO_DETAIL = "sql.driver.info.detail";

	/**
	 * Constant for the driver info failure message
	 * @see #SQL_SUB_WARN_OBJECT
	 */
	public String SQL_MSG_DRIVER_INFO_FAIL= "sql.driver.info.fail";

	/**
	 * Constant for the Sql Execution Failure message
	 * @see #SQL_SUB_SQL
	 */
	public String SQL_MSG_EXEC_FAILURE= "sql.execute.failure";

	/**
	 * Constant for the Sql Execution Timeout message
	 * @see #SQL_SUB_SQL
	 * @see #SQL_SUB_TIME
	 */
	public String SQL_MSG_EXEC_TIMEOUT= "sql.execute.timeout";

	/**
	 * Constant for the Password required message
	 * @see #SQL_SUB_OBJ_DESC
	 * @see #SQL_SUB_USER
	 */
	public String SQL_MSG_PASSWORD_REQ= "sql.connect.password.required";

	/**
	 * Constant for the Statement info failure message
	 * @see #SQL_SUB_WARN_OBJECT
	 */
	public String SQL_MSG_STMT_NOT_FOUND= "sql.stmt.not.found";

	/**
	 * Constant for the URL info failure message
	 * @see #SQL_SUB_WARN_OBJECT
	 */
	public String SQL_MSG_URL_NOT_FOUND= "sql.url.not.found";

	/**
	 * Constant for the User ID required message
	 * @see #SQL_SUB_OBJ_DESC
	 */
	public String SQL_MSG_USER_REQ= "sql.connect.user.required";

	/**
	 * Constant for SQL Warnings could not be accessed for object.
	 * @see #SQL_SUB_WARN_OBJECT
	 */
	public String SQL_MSG_WARN_NOT_FOUND = "sql.warn.not.found";

	/** Constant for the user id symbolic */
	public String SQL_SUB_COLUMN = "column";

	/** Constant for the sql errorcode symbolic */
	public String SQL_SUB_ERRORCODE = "errorCode";

	/** Constant for the sql error message symbolic */
	public String SQL_SUB_MSG = "sqlMessage";

	/** Constant for the object description symbolic */
	public String SQL_SUB_OBJ_DESC= "objectDescription";

	/** Constant for the sql product name symbolic */
	public String SQL_SUB_PROD_NAME = "productName";

	/** Constant for the sql product version symbolic */
	public String SQL_SUB_PROD_VER = "productVersion";

	/** Constant for the SQL symbolic */
	public String SQL_SUB_SQL = "sql";

	/** Constant for the sql error state symbolic */
	public String SQL_SUB_STATE = "sqlState";

	/** Constant for the time amount symbolic */
	public String SQL_SUB_TIME = "time";

	/** Constant for the URL symbolic */
	public String SQL_SUB_URL = "url";

	/** Constant for the user id symbolic */
	public String SQL_SUB_USER = "user";

	/** Constant for the sql detail object symbolic */
	public String SQL_SUB_WARN_OBJECT = "warningObject";

}
