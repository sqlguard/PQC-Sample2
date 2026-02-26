/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

//import sun.applet.resources.MsgAppletViewer;

import com.guardium.data.DataSourceConnectException;
import com.guardium.utils.i18n.Say;

/**
 * Convenience class gets textual data about jdbc objects for logging, error messages and tracing.
 * @author dtoland on Jun 16, 2006 at 5:26:59 PM
 */
public class Informer {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(Informer.class);

	/**
	 * @param t The throwable
	 * @return A list of the causes of the throwable.
	 */
	public static String causality(Throwable t) {
		StringBuilder buf = new StringBuilder();

		Throwable cause = t;
		String msg = null;
		while (cause!=null) {
			if ( isSQLException(cause) ) {
				SQLException sqle = (SQLException) cause;
				msg = sqlException(sqle);
			} else {
				msg = thrownMessage(cause);
			}

			// don't repeat messages
			if ( !Check.isEmpty(msg) && !Check.contains(buf, msg) ) {
				Stringer.newLn(buf).append(msg);
			}
			cause = cause.getCause();
		}
		return buf.toString();
	}

	/**
	 * @param throwable
	 * @return Whether the Throwable is assignable to SQLException
	 */
	public static boolean isSQLException(Throwable throwable) {
		if (throwable!=null) {
			return SQLException.class.isAssignableFrom( throwable.getClass() );
		}
		return false;
	}

	/**
	 * @param throwable
	 * @return Whether the Throwable is assignable to SQLException
	 */
	public static boolean isDataSourceConnectException(Throwable throwable) {
		if (throwable!=null) {
			return DataSourceConnectException.class.isAssignableFrom( throwable.getClass() );
		}
		return false;
	}

	private static final String STACKTRACE = "STACKTRACE:";
	private static final String NESTED_EXCEPTION = "** BEGIN NESTED EXCEPTION";
	private static final String COM_HXTT_SQL = "com.hxtt.sql.";
	private static String truncateMessage(String msg) {
		int pos;
		String result = msg;

		if ( !Check.isEmpty(result) ) {
			
			// HXTT drivers put stack trace in the message
			if ( Check.contains(result, COM_HXTT_SQL) ) {
				pos = result.indexOf(Say.NL);
				result = result.substring(0, pos).trim();
	
			}

			// check for nexted exception keyword
			pos = result.indexOf(NESTED_EXCEPTION);
			if (pos >= 0) {
				result = result.substring(0, pos-1).trim();
			}
	
			// check for stacktrace keyword
			pos = result.indexOf(STACKTRACE);
			if (pos >= 0) {
				result = result.substring(0, pos-1).trim();
			}
			
			// see if it is just too long
			final int maxSize = 255;
			int size = result.length();
			if (size > maxSize) {
				int newline = result.indexOf(Say.NL);
				if (newline >= 0 && newline < maxSize) {
					result = result.substring(0, newline);
				} else {
					result = result.substring(0, maxSize) + Say.ELIPSIS;
				}
			}
		}
		return result;
	}
	public static String thrownMessage(Throwable throwable) {
		
		return thrownMessage(throwable, true);
	}

	/**
	 * Will cascade embedded SQLExceptions if the throwable, or throwable's cause is a SQLException.
	 * @param throwable The throwable
	 * @return The exception name and message.
	 */
	public static String thrownMessage(Throwable throwable, boolean includeDetails) {
		if (throwable == null) {
			return Say.EMPTY;
		}

		String exceptionName = Check.className(throwable) + Say.CLN;
		StringBuilder buf = new StringBuilder();

		// get the exception message
  	if ( includeDetails && !Check.isEmpty( throwable.getLocalizedMessage() ) ) {
  		buf.append( truncateMessage( throwable.getLocalizedMessage() ) );
  	}

		// cascade SQLExceptions or DataSourceConnectionExceptions
		if ( isSQLException(throwable) ) {
			SQLException sqle = (SQLException)throwable;

			// get the SQL State
			if ( !Check.isEmpty( sqle.getSQLState() ) ) {
				buf.append((includeDetails ? "  " : "") + "SqlState: " + sqle.getSQLState() );
			}

			// Get the error code
			if ( !Check.isEmpty( sqle.getErrorCode() ) ) {
				buf.append("  Error Code: " +  sqle.getErrorCode() );
			}

			// get the next exception if not self referential
			if (sqle.getNextException() != null && sqle.getNextException() != throwable) {
				sqle = sqle.getNextException();
				String msg = thrownMessage(sqle);
				if ( !Check.contains(buf, msg) ) {
 					Stringer.newLn(buf).append(msg);
				}
			}

		// add the cause if not self referential
		} else if ( throwable.getCause() != null && throwable.getCause() != throwable) {
			Throwable cause = throwable.getCause();
			String msg;
			// if the cause is a sql or connection exception, do it up full
			if ( isSQLException(cause) || isDataSourceConnectException(cause) ) {
				msg = thrownMessage( throwable.getCause() );
				if ( Check.contains(buf, msg) ) {
					msg = null;
				}
			
			// add the cause if it there is one - no cascading
			} else {
				msg = truncateMessage( cause.getLocalizedMessage() );
				if ( Check.contains(buf, msg) ) {
					msg = null;
				} else {
					msg = Check.className(cause) + Say.CLN + msg;
				}
			}
			
			if ( !Check.isEmpty(msg) ) {
				Stringer.newLn(buf).append(msg);
			}
		}

		// no valuable information.
	  if ( Check.isEmpty(buf) ) {
    	buf.append( Say.what(Say.INFO_LIT_NO_MSG ) );
	  }
	  
	  String message = new String();
	  if(includeDetails)
		  message += exceptionName;
	  message += buf.toString();
	  // return the exeption name and information
	  return  message;
	}

	public static String thrownLimitMsg(Throwable throwable) {
		return thrownLimitMsg(throwable,0);
	}

	public static String thrownLimitMsg(Throwable throwable, int level) {
		return thrownLimitMsg( throwable, level, false); 
	}
	/**
	 *for text jdbc driver, sometimes it throwns the entire stack trace as next exception msg,
	 *it's too ugly and maybe insecure to display all these. So I'll limit the msg. -- RUI*/
	public static String thrownLimitMsg(Throwable throwable, int level, boolean displayStackTrace) {
	  if(level++==5)//prevent loop
		  return null;

	  StringBuilder buf = new StringBuilder();

	  if (throwable!=null) {
	  	String msg = throwable.getLocalizedMessage();
	  	if ( !Check.isEmpty(msg) ) {
		  	buf.append( Check.className(throwable) + ": ");

	  	}

  		// cascade SQLExceptions or DataSourceConnectionExceptions
  		if ( isSQLException(throwable) ) {
  			SQLException sqle = (SQLException)throwable;
  			buf.append("  SqlState: " + sqle.getSQLState() + "  Error: " + + sqle.getErrorCode() );
  			if((!Check.isEmpty(msg))&&(msg.indexOf('\n')>0))
	    			buf.append(msg.substring(0,msg.indexOf('\n')));
    		else
	    			buf.append(msg);
  			if (sqle.getNextException() != null) {
  				sqle = sqle.getNextException();
  				msg = thrownLimitMsg(sqle, level);
  			}
  			if ( displayStackTrace){
  				msg = thrownStack(throwable, level);
  				buf.append(msg);
  			}
  		} else
	    {
			buf.append(msg);
			msg = thrownStack(throwable, level);
			buf.append(msg);
			msg = null;
			if (
				isSQLException( throwable.getCause() )
				|| isDataSourceConnectException( throwable )
				|| isDataSourceConnectException( throwable.getCause() )
			) {
  			  msg = thrownMessage( throwable.getCause() );
  		    }
	   }

	  	  if ( Check.isEmpty(buf) && Check.isEmpty(msg) ) {
	  	  	// no localized message
	  	  	buf.append( Check.className(throwable) + ": ");
	    		buf.append( Say.what(Say.INFO_LIT_NO_MSG ) );

	    		if ( throwable.getCause() != null ) {
	    			msg = thrownMessage( throwable.getCause() );
	    		}
	  	  }

	  	// try not to repeat messages
		if ( !Check.isEmpty(msg) ) {
			if ( Check.contains(msg, buf) ) {
				buf = new StringBuilder(msg);
			} else if ( !Check.contains(buf, msg) ) {
				Stringer.newLn(buf).append(msg);
			}
		}
  	}

	  return  buf.toString();
	}

	/**
	 * get n stack trace element
	 * @param throwable
	 * @param level
	 * @return
	 */
	public static String thrownStack(Throwable t, int level) {
		StringBuffer sb = new StringBuffer();
		StackTraceElement[] a = t.getStackTrace();
		int len = a.length;
		if (level<len)
			len = level;
		for(int i = 0; i<len; i++)
		{
			sb.append(a[i].toString()).append("\n");
		}
		if(sb.length()>0)
			sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	/**
	 * Looks down the cause chain of the throwable searching for any SQLExceptions.
	 * Iterates through all data of a SQLException for those nebulous, hard-to-figure exceptions.
	 * @param t The Throwable in question.
	 * @return a big string with all the data from the exception.  Empty if none
	 */
	public static String sqlException(Throwable t) {
		String result = "";
		if ( isSQLException(t) ) {
			SQLException sqle = (SQLException)t;
			result = sqlException(sqle);
		}
		return result;
	}

	/**
	 * Looks down the cause chain of the throwable searching for any SQLExceptions.
	 * Iterates through all data of a SQLException for those nebulous, hard-to-figure exceptions.
	 * @param sqle The Throwable in question.
	 * @return a big string with all the data from the exception.  Empty if none
	 */
	public static String sqlException(SQLException sqle) {
		StringBuilder buf = new StringBuilder();

		SQLException current = sqle;
  	while (current!=null) {
			Stringer.newLn(buf).append(
  				Say.what(
	  				Say.SQL_MSG_DETAIL,
	  				Say.SQL_SUB_ERRORCODE, String.valueOf( current.getErrorCode() ),
	  				Say.SQL_SUB_STATE, current.getSQLState(),
	  				Say.SQL_SUB_MSG, current.getLocalizedMessage()
	  			)
  		);
  		current = current.getNextException();
  	}
	 	return buf.toString();
	}

	/**
	 * Appends the warnings into a string and then clears them from the statement.
	 * @param warning
	 * @return The statements Sql warnings in a string suitable for logging.
	 */
	private static String sqlWarnings(SQLWarning warning) {
		if (warning==null) {
			return Say.what(Say.INFO_LIT_NO_MSG);
		}
		Check.freeMemory();

		StringBuffer buf = new StringBuffer();
		SQLWarning current = warning;
		while (current!=null) {
  		Stringer.newLn(buf).append(
  				Say.what(
	  				Say.SQL_MSG_DETAIL,
	  				Say.SQL_SUB_ERRORCODE, String.valueOf( current.getErrorCode() ),
	  				Say.SQL_SUB_STATE, current.getSQLState(),
	  				Say.SQL_SUB_MSG, current.getLocalizedMessage()
	  			)
  		);
  		current = current.getNextWarning();
		}

		if (buf.length()==0) {
			return Say.what(Say.INFO_LIT_NO_MSG);
		}

		return buf.toString();
	}

	/**
	 * Appends the warnings into a string and then clears them from the statement.
	 * @param rs
	 * @return The statements Sql warnings in a string suitable for logging.
	 */
	public static String sqlWarnings(ResultSet rs) {
		String warnings;
		try {
			if (rs!=null) {
				warnings = sqlWarnings( rs.getWarnings() );
				rs.clearWarnings();
			} else {
				warnings = Say.what(Say.INFO_LIT_NO_MSG);;
			}

		} catch (Throwable t) {
			warnings =
				Say.what(Say.SQL_MSG_WARN_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, Check.className(rs) )
				+ Say.NL + thrownMessage(t)
			;
		}

		return Say.what(Say.SQL_LIT_WARN_RS) + " " + warnings;
	}

	/**
	 * Appends the warnings into a string and then clears them from the statement.
	 * @param stmt
	 * @return The statements Sql warnings in a string suitable for logging.
	 */
	public static String sqlWarnings(Statement stmt) {
		String warnings;
		try {
			if (stmt!=null) {
				warnings = sqlWarnings( stmt.getWarnings() );
				stmt.clearWarnings();
			} else {
				warnings = Say.what(Say.INFO_LIT_NO_MSG);;
			}

		} catch (Throwable t) {
			warnings =
				Say.what(Say.SQL_MSG_WARN_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, Check.className(stmt) )
				+ Say.NL + thrownMessage(t)
			;
		}

		return Say.what(Say.SQL_LIT_WARN_STMT) + " " + warnings;
	}

	/**
	 * Appends the warnings into a string and then clears them from the statement.
	 * @param con
	 * @return The statements Sql warnings in a string suitable for logging.
	 */
	public static String sqlWarnings(Connection con) {
		String warnings;
		try {
			if (con!=null) {
				warnings = sqlWarnings( con.getWarnings() );
				con.clearWarnings();
			} else {
				warnings = Say.what(Say.INFO_LIT_NO_MSG);
			}

		} catch (Throwable t) {
			warnings =
				Say.what(Say.SQL_MSG_WARN_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, Check.className(con) )
				+ Say.NL + thrownMessage(t)
			;
		}

		return Say.what(Say.SQL_LIT_WARN_CON) + " " + warnings;
	}

	/**
	 * @param stmt A Statement.
	 * @return Connection information for a datasource.
	 */
	public static String driverInfo(Statement stmt) {
		try {
			return driverInfo( findConnection(stmt) );
		} catch (Throwable t) {
			return thrownMessage(t);
		}
	}
	/**
	 * @param rs A Result Set.
	 * @return Connection information for a datasource.
	 */
	public static String driverInfo(ResultSet rs) {
		try {
			return driverInfo( findConnection(rs) );
		} catch (Throwable t) {
			return thrownMessage(t);
		}
	}

	/**
	 * @param con A Connection.
	 * @return Connection information for a datasource.
	 */
	public static String driverInfo(Connection con) {
		String detail = null;

		if (con==null) {
			detail = Say.what(Say.INFO_LIT_NO_MSG);

		} else {
			try {
				DatabaseMetaData meta = con.getMetaData();
				detail = Say.what(
						Say.SQL_MSG_DRIVER_INFO_DETAIL,
						Say.SQL_SUB_PROD_NAME, meta.getDatabaseProductName(),
						Say.SQL_SUB_PROD_VER, meta.getDatabaseProductVersion()
				);


			} catch (SQLException e) {
				detail =
					Say.what(Say.SQL_MSG_DRIVER_INFO_FAIL, Say.SQL_SUB_WARN_OBJECT, String.valueOf(con) )
					+ causality(e)
				;

			} catch (Throwable t) {
				detail =
					Say.what(Say.SQL_MSG_DRIVER_INFO_FAIL, Say.SQL_SUB_WARN_OBJECT, String.valueOf(con) )
					+ causality(t)
				;
			}
		}

		return Say.what(Say.SQL_LIT_DRIVER_INFO) + " " + detail;
	}

	/**
	 * @return Information about the current state of memory available for the Java VM.
	 */
	public static String memoryInfo() {
		Runtime rt = Runtime.getRuntime();
		String detail = Say.what(
				Say.INFO_MSG_MEMORY,
				Say.INFO_SUB_DATETIME, new Timestamp( System.currentTimeMillis() ).toString(),
				Say.INFO_SUB_MEM_MAX, String.valueOf( rt.maxMemory()/1024 ),
				Say.INFO_SUB_MEM_TOTAL, String.valueOf( rt.totalMemory()/1024 ),
				Say.INFO_SUB_MEM_FREE, String.valueOf( rt.freeMemory()/1024)
		);
		return detail;
	}
	
	public static String usedMemoryInfo() {
		Runtime rt = Runtime.getRuntime();
		String detail = "Memory used = "+ String.valueOf( (rt.totalMemory()-rt.freeMemory())/1024)+"k";
		
		return detail;
	}

	/**
	 * @param stmt
	 * @return The connection for the statement.
	 * @throws SQLException
	 */
	public static Connection findConnection(Statement stmt) throws SQLException {
		try {
			return stmt.getConnection();
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_CON_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(stmt)
			);
			throw new SQLException(msg + thrownMessage(t) );
		}
	}

	/**
	 * @param rs
	 * @return The connection that the result set was drawn from
	 * @throws SQLException
	 */
	public static Connection findConnection(ResultSet rs) throws SQLException {
		try {
			return findConnection( findStatement(rs) );
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_CON_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(rs)
			);
			throw new SQLException(msg + thrownMessage(t) );
		}
	}

	/**
	 * @param rs
	 * @return The statement that created the result set.
	 * @throws SQLException
	 */
	public static Statement findStatement(ResultSet rs) throws SQLException {
		try {
			return rs.getStatement();
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_STMT_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(rs)
			);
			throw new SQLException(msg + thrownMessage(t) );
		}
	}

	/**
	 * @param con
	 * @return The url for this connection
	 */
	public static String findUrl(Connection con) {
		try {
			return con.getMetaData().getURL();
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_URL_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(con)
			);
			return msg + thrownMessage(t);
		}
	}

	/**
	 * @param stmt
	 * @return The url for this connection
	 */
	public static String findUrl(Statement stmt) {
		try {
			return findUrl( stmt.getConnection() );
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_URL_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(stmt)
			);
			return msg + thrownMessage(t);
		}
	}

	/**
	 * @param rs
	 * @return The url for this connection
	 */
	public static String findUrl(ResultSet rs) {
		try {
			return findUrl( rs.getStatement() );
		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_URL_NOT_FOUND, Say.SQL_SUB_WARN_OBJECT, String.valueOf(rs)
			);
			return msg + thrownMessage(t);
		}
	}

	/**
	 * @return The class, method and line for the entire stack.
	 */
	public static String locationInfo() {
		StringBuffer buf = new StringBuffer( Say.what( Say.INFO_LIT_LOCATION) );

		StackTraceElement[] stack = getStack();
		int len = stack.length;
		for (int i=1; i<len && i<9; i++) {

			// filter instances of the same method
			if (
					i+1<len
					&& stack[i].getClassName().equals( stack[i+1].getClassName() )
					&& stack[i].getMethodName().equals( stack[i+1].getMethodName() )
			) {
				continue;
			}

			Stringer.newLn(buf).append(
				Say.what(
						Say.INFO_MSG_LOCATION_LINE,
						Say.INFO_SUB_CLS_NAME, stack[i].getClassName(),
						Say.INFO_SUB_MTHD_NAME, stack[i].getMethodName(),
						Say.INFO_SUB_EXEC_LINE, String.valueOf( stack[i].getLineNumber() )
				)
			);
		}
		return buf.toString();
	}

	/**
	 * @param referenceClass The class that will be searched for in the execution stack.
	 * @return The class, method and line that was last executed by the class.
	 */
	public static String locationInfo(Class<?> referenceClass) {
		return locationInfo(referenceClass, 0);
	}

	/**
	 * @param offset The relative location on the stack to return.
	 *  Positive numbers return elements further down the stack,
	 *  negative numbers offset higher in the stack.
	 * @return The class, method and line that was last executed by the class.
	 */
	public static String locationInfo(int offset) {
		return locationInfo(null, offset);
	}

	/**
	 * Return a class before or after the known class in the stack.
	 * @param referenceClass The class that will be searched for in the execution stack.
	 * @param offset The relative location on the stack to return.
	 *  Positive numbers return elements further down the stack,
	 *  negative numbers offset higher in the stack.
	 * @return The class, method, and line number of the class offset from the argument class
	 */
	public static String locationInfo(Class<?> referenceClass, int offset) {
		StackTraceElement stack = relativeStack(referenceClass, offset);

		return Say.what(
				Say.INFO_MSG_LOCATION,
				Say.INFO_SUB_CLS_NAME, stack.getClassName(),
				Say.INFO_SUB_MTHD_NAME, stack.getMethodName(),
				Say.INFO_SUB_EXEC_LINE, String.valueOf( stack.getLineNumber() )
		);
	}

	/**
	 * @param referenceClass The class that will be searched for in the execution stack.
	 * @param offset The relative location on the stack to return.
	 *  Positive numbers return elements further down the stack,
	 *  negative numbers offset higher in the stack.
	 * @return The class name and method name relative to the caller.
	 */
	public static String relativeMethod(Class<?> referenceClass, int offset) {
		String methodName = null;
		return relativeMethod(referenceClass, methodName, offset);
	}

	/**
	 * @param referenceClass The class that will be searched for in the execution stack.
	 * @param methodName The method name that will be searched for in the referenceClass.
	 * @param offset The relative location on the stack to return.
	 *  Positive numbers return elements further down the stack,
	 *  negative numbers offset higher in the stack.
	 * @return The class name and method name relative to the caller.
	 */
	public static String relativeMethod(Class<?> referenceClass, String methodName, int offset) {
		StackTraceElement stack = relativeStack(referenceClass, methodName, offset);
		String result = Check.className( stack.getClassName() ) + Say.DOT + stack.getMethodName();
		return result;
	}

	/**
	 * Returns the calling object and method.
	 * @return The class name and method name relative to the caller.
	 */
	public static String relativeMethod() {
		return relativeMethod(1);
	}

	/**
	 * Returns the calling object and method.
	 * @param offset
	 * @return The classname and method name relative to the caller.
	 */
	public static String relativeMethod(int offset) {
		return relativeMethod(Informer.class, offset);
	}

	/**
	 * @param referenceClass
	 * @param offset
	 * @return The class relative to the caller.
	 */
	public static Class<?> relativeClass(Class<?> referenceClass, int offset) {
		String methodName = null;
		return relativeClass(referenceClass, methodName, offset);
	}

	/**
	 * @param referenceClass
	 * @param offset
	 * @param methodName The method name that will be searched for in the referenceClass.
	 * @return The class relative to the caller.
	 */
	public static Class<?> relativeClass(Class<?> referenceClass, String methodName, int offset) {
		Class<?> cls = null;
		StackTraceElement stack = relativeStack(referenceClass, methodName, offset);
		try {
			cls = Class.forName( stack.getClassName() );
		} catch (ClassNotFoundException e) {
			/*
			if ( LOG.isEnabledFor(Level.WARN) ) {
				LOG.warn("Could not find class: " + stack.getClassName(), e);
			}
			*/
			String msg = "exception is " + e.getMessage();
		}
		return cls;
	}

	/**
	 * @return The currently executing stack
	 */
	public static StackTraceElement[] getStack() {
		Thread thread = Thread.currentThread();
		return thread.getStackTrace();
	}

	/**
	 * @param referenceClass
	 * @return The currently executing stack
	 */
	public static StackTraceElement[] getStack(Class<?> referenceClass) {
		return getStack(referenceClass, 0);
	}

	/**
	 * @param referenceClass
	 * @param offset
	 * @return The currently executing stack
	 */
	public static StackTraceElement[] getStack(Class<?> referenceClass, int offset) {
		StackTraceElement[] stack = getStack();

		// find the reference class in the stack and spin until the next class
		boolean hit = false;
		int ndx = -1;
		for (int i=0; i<stack.length; i++) {
			String name = stack[i].getClassName();
			if ( name.equals( referenceClass.getName() ) ) {
				ndx = i+1;
				hit = true;

			} else if (hit) {
				// got through the reference class, get out
				break;

			} else if (
					name.equals( Informer.class.getName() )
					|| name.equals( Thread.class.getName() )
			) {
				// eliminate this class from the result
				ndx = i+1;
			}
		}

		// found the reference class now truncate the results
		if (ndx>=0) {
			int size = Math.max(1, stack.length -ndx -offset);
			StackTraceElement[] result = new StackTraceElement[size];
			for (int i=0; i +ndx +offset <stack.length; i++) {
				result[i] = stack[i +ndx +offset];
			}
			return result;
		}

		// couldn't find the reference return the whole thing.
		return stack;
	}

	/**
	 * @param referenceClass
	 * @param offset
	 * @return The stack trace element relative to the reference class.
	 */
	protected static StackTraceElement relativeStack(Class<?> referenceClass, int offset) {
		String methodName = null;
		return relativeStack(referenceClass, methodName, offset);
	}

	/**
	 * @param referenceClass
	 * @param methodName The method name that will be searched for in the referenceClass.
	 * @param offset
	 * @return The stack trace element relative to the reference class.
	 */
	protected static StackTraceElement relativeStack(Class<?> referenceClass, String methodName, int offset) {
		StackTraceElement[] stack = getStack();
		return relativeStack(referenceClass, methodName, offset, stack);
	}

	/**
	 * @param referenceClass
	 * @param methodName The method name that will be searched for in the referenceClass.
	 * @param offset
	 * @param stack
	 * @return The stack trace element relative to the reference class.
	 */
	protected static StackTraceElement relativeStack(
			Class<?> referenceClass, String methodName, int offset, StackTraceElement[] stack
	) {

		int ndx = 0;
		boolean classHit = false;
		boolean methodHit = false;
		if ( !Check.isEmpty(referenceClass) ) {
			String classname = referenceClass.getName();

			// spin through the contiguous matches until a fresh class is hit.
			for (StackTraceElement element : stack) {

				if ( classname.equals( element.getClassName() ) ) {
					classHit = true;

					if (methodName == null) {
						methodHit = true;
					} else if ( methodName.equals(element.getMethodName() ) ) {
						methodHit = true;
					} else if (methodHit) {
						break;
					}

				} else if (classHit && methodHit) {
					break;
				}
				ndx++;
			}
		}

		ndx = ndx + Math.abs(offset) - 1;
		if (ndx >= stack.length) {
			ndx = stack.length;
		}

		return stack[ndx];
	}

}
