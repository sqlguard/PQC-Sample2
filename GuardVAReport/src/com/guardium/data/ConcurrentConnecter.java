/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;


import java.sql.Connection;
//import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.ddtek.jdbc.extensions.ExtEmbeddedConnection;
import com.guardium.data.Datasource;
import com.guardium.data.HangingThreadException;
import com.guardium.utils.i18n.Say;
//import com.guardium.utils.Check;
import com.guardium.utils.Informer;

/**
 *
 * @author dtoland on Jan 26, 2007 at 4:10:55 PM
 */
public class ConcurrentConnecter {
	/** Local static logger for class */
	//static final transient Logger LOG = Logger.getLogger(ConcurrentConnecter.class);

	private static ThreadPoolExecutor executorService = null;

	private static final AtomicLong CONNECT = new AtomicLong();
	private static final AtomicLong CANCEL = new AtomicLong();
	private static final AtomicLong EXCEPTION = new AtomicLong();
	private static final AtomicLong INTERUPT = new AtomicLong();
	private static final AtomicLong TIMEOUT = new AtomicLong();
	private static final AtomicLong UNKNOWN = new AtomicLong();

	private static Map<String,Date> outstandingConns = new Hashtable<String,Date>();
	/**
	 * Cache the executor service for future uses
	 * @return Executor Service
	 */
	protected static ThreadPoolExecutor getExecutorService() {
		if (executorService==null || executorService.isShutdown() || executorService.isTerminated() ) {
			executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		}
		return executorService;
	}

	/**
	 * Shuts down the service
	 */
	protected static void shutdown() {
		executorService.shutdownNow();
		executorService = null;
	}

	/**
	 * @param url URL to the database
	 * @param user The user id
	 * @param pw The password
	 * @param millis How long to wait before terminating the attempt to connect
	 * @param other Other connection properties.
	 * @param datasource The datasource that initiated the connection 
	 * @return A connection to the database;
	 * @throws DataSourceConnectException
	 */
	public static Connection connect(
			String url, String user, String pw, int millis, Properties other, Datasource datasource
	) throws DataSourceConnectException {

		
		//System.out.println("Do we got here 1 ???");
		
		Stopwatch watch = new Stopwatch();
		ThreadPoolExecutor exec = null;
		Future<Connection> future = null;
		Connection con = null;
		ConnectWorker worker = null;
		try {
			worker = new ConnectWorker(url, user, pw, other, datasource);
			exec = getExecutorService();
			
			//System.out.println("Do we got here 2 ???");
			
            /*
			// execute the work thread - return the result into future
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Connecting: " + user + "@" + describeInstance(datasource) );
			}
			*/
			watch.start();
			//System.out.println("Do we got here 3???");
			future = exec.submit(worker);
			//System.out.println("Do we got here 4 ???");
			
			con = future.get(millis, TimeUnit.MILLISECONDS);
			//System.out.println("Do we got here 5 ???");
			CONNECT.incrementAndGet();
			
			//System.out.println("Do we got here 6 ???");
            /*
			if ( LOG.isDebugEnabled() ) {
				LOG.debug( watch.checkElapsed("*** Connected: " + user + "@" + describeInstance(datasource) ) );
			}
			*/
			
			
			if (con instanceof ExtEmbeddedConnection) {
				ExtEmbeddedConnection embeddedCon = (ExtEmbeddedConnection)con;
				boolean unlocked = embeddedCon.unlock("4GZrDGNd");
				//System.out.println("Do we got here 7 ???");
				if (!unlocked) {
					throw new DataSourceConnectException("DataDirect unlock failed with: " + describeInstance(datasource));					
				}
			}
			
			return con;

		// the computation was canceled
		} catch (CancellationException e) {
			long count = CANCEL.incrementAndGet();

			/*
			if ( LOG.isInfoEnabled() ) {
				LOG.info(
						"* Cancelled Worker(" + count + "): " + user + "@" + describeInstance(datasource) 
						+ Say.NL + Informer.thrownMessage( e.getCause() )
				);
			}
			*/
			throw handleConnectException(e, Say.SQL_MSG_CONNECT_TIMEOUT, describeInstance(datasource) , user, millis);

	  // the computation threw an exception
		} catch (ExecutionException e) {
			EXCEPTION.incrementAndGet();
			/*
			if ( LOG.isInfoEnabled() ) {
				LOG.info(
						"* Exception: " + worker
						+ Say.NL + Informer.thrownMessage( e.getCause() )
				);
			}
			*/

			throw handleConnectException(e, Say.SQL_MSG_CONNECT_FAILURE, describeInstance(datasource) , user, millis);
		// the current thread was interrupted while waiting
		} catch (InterruptedException e) {
			INTERUPT.incrementAndGet();
			/*
			if ( LOG.isInfoEnabled() ) {
				LOG.info("* Interupted Worker: " + worker
						+ Say.NL + Informer.thrownMessage( e.getCause() )
				);
			}
			*/

			throw handleConnectException(e, Say.SQL_MSG_CONNECT_TIMEOUT, describeInstance(datasource) , user, millis);

		// the wait timed out
		} catch (TimeoutException e) {
			TIMEOUT.incrementAndGet();

			//System.out.println("Do we got here 11 ???");
			// Belt and braces, if the thread is still around it would throw a cancellation exceptions
			future.cancel(true);

			//datasource.removeDriver(); // Need to remove the drivers when Future throws a timeout (in ConcurrentConnecter method Connect) See Bug 35134

			// purge the thread pool
			int prePurge = exec.getPoolSize();
			exec.purge();
			/*
			if ( LOG.isInfoEnabled() ) {
				Throwable cause = e.getCause() != null ? e.getCause() : e;
				LOG.info(
						"*** TIMEOUT Worker: " + worker
						+ Say.NL + " Pre-Purge Pool: " + prePurge + "   Post-Purge Pool: " + exec.getPoolSize()
						+ Say.NL + " Cause: '"+ Informer.thrownMessage(cause) + "'"
				);
			}
			*/

			throw handleConnectException(e, Say.SQL_MSG_CONNECT_TIMEOUT, describeInstance(datasource) , user, millis);
		// other exceptions
		} catch (Throwable t) {
			UNKNOWN.incrementAndGet();

			String msg = Say.what(
					Say.SQL_MSG_CONNECT_FAILURE,
					Say.SQL_SUB_URL, describeInstance(datasource),
					Say.SQL_SUB_USER, user,
					Say.SQL_SUB_TIME, String.valueOf(millis)
			);
			//LOG.error(msg, t);
    
			//AdHocLogger.logException(t);
			throw new DataSourceConnectException(msg, t);
          
		} finally {
			/*
			if ( LOG.isInfoEnabled() ) {
				String msg =
						user + "@" + describeInstance(datasource) 
						+ Say.NL
						+ "  Connected: " + !Check.isEmpty(con)
						+ "  Done: " + future.isDone() + "  Cancelled: " + future.isCancelled()
						+ Say.NL
						+ "  Active Threads: " + exec.getActiveCount()
						+ "  Completed Tasks: " + exec.getCompletedTaskCount()
						+ "  Pool Size: " + exec.getPoolSize()
						+ "  Largest Pool: " + exec.getLargestPoolSize()
						+ Say.NL
						+ "  Connect: " + CONNECT.get()
						+ "  Cancel: " + CANCEL.get() + "  Exception: "+ EXCEPTION.get()
						+ "  Interupt: "+ INTERUPT.get() + "  Timeout: "+ TIMEOUT.get()
						+ "  Unknown: "+ UNKNOWN.get()
				;
				String mem = Informer.memoryInfo();
				LOG.info(msg + Say.NL + mem);
			}
			*/
		}
	}
	
	/**
	 * 
	 * @param datasource
	 * @return A description of the instance to be used in error messages
	 */
	private static String describeInstance(Datasource datasource) {
		String desc = 
			datasource.getName() + " " + datasource.getHostIp() + ":" + datasource.getPort()
		;
		return  desc;
	}

	public static void logOutstandingConns(){
		//if ( LOG.isDebugEnabled() ) {
			StringBuilder sb = new StringBuilder();
			sb.append("Outstanding Conns:\n");
			Set<String> keys = outstandingConns.keySet();
			for (String key:keys){
			    Date start = outstandingConns.get(key);
			    if(start!=null)
			    	sb.append(key).append(" ").append((System.currentTimeMillis()-start.getTime())/1000).append("\n");
			}
			//LOG.info(sb.toString());
		//}
	}

	public static void checkOutstandingConns() throws HangingThreadException{
		long maxSec = 0;
		long threshHoldMaxSec = 36*60*60;
		long threshHoldSec = 200;
		int num = 0;
		int threshCtr = 15;
		Set<String> keys = outstandingConns.keySet();
		for (String key:keys){
		    Date start = outstandingConns.get(key);
		    if(start!=null)
		    {
		    	long inverval = (System.currentTimeMillis()-start.getTime())/1000;
		    	if(inverval>maxSec)
		    		maxSec = inverval;
		    	if(inverval>threshHoldSec)
		    		num++;
		    }
		}
		if(num>threshCtr||maxSec>threshHoldMaxSec)
		{
			throw new HangingThreadException("num="+num+";"+"time="+maxSec);
		}
	}
	/**
	 * Formats the exception for throwing
	 * @param e
	 * @param msgKey
	 * @param url
	 * @param user
	 * @param millis
	 * @return A DatasourceConnectionException suitable for throwing.
	 */
	protected static DataSourceConnectException handleConnectException(
			Exception e, String msgKey, String url, String user, int millis
	) {
		Throwable cause = e;
		if ( e.getCause() != null) {
			cause = e.getCause();
		}

		String msg = Say.what(
				msgKey,
				Say.SQL_SUB_URL, url,
				Say.SQL_SUB_USER, user,
				Say.SQL_SUB_TIME, String.valueOf(millis/1000)
		);

		return new DataSourceConnectException(msg, cause);
	}

	/**
	 * @param stmt A statement for the the connection.
	 * @param sql The sql statement.
	 * @param seconds How long to wait before terminating the attempt to execute.
	 * @return The result set.
	 * @throws SqlTimeoutException
	 * @throws SQLException
	 */
	/*
	public static ResultSet executeSql(Statement stmt, String sql, int seconds)
			throws SqlTimeoutException, SQLException {

		Stopwatch watch = new Stopwatch();
		ExecutorService exec = null;
		Future<ResultSet> future = null;
		ResultSet rs = null;
		try {
			SqlWorker worker = new SqlWorker(stmt, sql);
			exec = getExecutorService();

			// execute the work thread - return the result into future
			future = exec.submit(worker);

			watch.start();
			rs = future.get(seconds, TimeUnit.SECONDS);
			return rs;

		// the computation was cancelled
		} catch (CancellationException e) {
			cancelStatement(stmt);
			throw handleSqlTimeoutException(e, Say.SQL_MSG_EXEC_TIMEOUT, stmt, sql, seconds);

	  // the computation threw an exception
		} catch (ExecutionException e) {
			throw handleSqlTimeoutException(e, Say.SQL_MSG_EXEC_FAILURE, stmt, sql, seconds);

		// the current thread was interrupted while waiting
		} catch (InterruptedException e) {
			cancelStatement(stmt);
			throw handleSqlTimeoutException(e, Say.SQL_MSG_EXEC_TIMEOUT, stmt, sql, seconds);

		// the wait timed out
		} catch (TimeoutException e) {
			cancelStatement(stmt);
			throw handleSqlTimeoutException(e, Say.SQL_MSG_EXEC_TIMEOUT, stmt, sql, seconds);

		} catch (Throwable t) {
			String msg = Say.what(
					Say.SQL_MSG_EXEC_TIMEOUT,
					Say.SQL_SUB_SQL, sql,
					Say.SQL_SUB_TIME, String.valueOf(seconds)
			);
			//LOG.error(msg, t);
			throw new SqlTimeoutException(msg, t);

		} finally {
			future.cancel(true);
		}
	}
    */
	
	/**
	 * Formats the exception for throwing
	 * @param e
	 * @param msgKey
	 * @param stmt
	 * @param sql
	 * @param seconds
	 * @return A DatasourceConnectionException suitable for throwing.
	 * @throws SQLException
	 */
	/*
	protected static SqlTimeoutException handleSqlTimeoutException(
			Exception e, String msgKey, Statement stmt, String sql, int seconds
	) throws SQLException {

		Throwable cause = e;
		if ( e.getCause()!=null) {
			cause = e.getCause();
			if ( Informer.isSQLException(cause) ) {
				SQLException sqle = (SQLException) cause;
				throw sqle;
			}
		}

		String msg = Say.what(
				msgKey,
				Say.SQL_SUB_SQL, sql,
				Say.SQL_SUB_TIME, String.valueOf(seconds)
		);
		//LOG.warn(msg + Say.NL + Informer.thrownMessage(cause), cause);

		return new SqlTimeoutException(msg, cause);
	}
    */
	
	/**
	 * @param stmt The statement to cancel.
	 */
	protected static void cancelStatement(Statement stmt) {
		try {
			//if  (LOG.isDebugEnabled() ) {
			//	LOG.debug("Cancelling Statement.");
			//}
			stmt.cancel();

		} catch (Throwable t) {
			/*
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Cancelled Statement", t);
			}
			*/
			
		}
	}

	/**
	 * Inner class implements the worker thread
	 * @author dtoland on Jan 26, 2007 at 4:30:28 PM
	 */
	protected static class ConnectWorker implements Callable<Connection> {
		private final String url;
		private final String user;
		private final String pw;
		private final Properties props;
		private static int testid =0;
		private int myId =0;
		private Datasource datasource = null;
		private synchronized int increaseId()
		{
			testid ++;
			return testid;
		}

		/**
		 * @param url
		 * @param user
		 * @param pw
		 * @param props
		 * @param datasource
		 */
		protected ConnectWorker(String url, String user, String pw, Properties props, Datasource datasource) {
			this.url = url;
			this.user = user;
			this.pw = pw;
			if (props != null) {
				this.props = props;
			} else {
				this.props = new Properties();
			}
			this.datasource = datasource;
			this.myId= increaseId();
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		@SuppressWarnings("synthetic-access")
		public Connection call() throws Exception {
			String id = "" + this.myId + "+" + this.datasource.getDatasourceId();
			
			//System.out.println("connection call id " + id);
			
			outstandingConns.put(id, new Date() );
			/*
			if ( LOG.isDebugEnabled() ) {
				LOG.debug( "Txy to connect "+id );
			}
			*/
			
			try{
				Connection con = null;
				Stopwatch watch = null;
				
				/*
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("About to connect: " + this + " Props: " + this.props);
					watch = new Stopwatch();
				}
				*/
				
				if (this.user != null) {
					this.props.put("user", this.user);
				}

				if (this.pw != null) {
					this.props.put("password", this.pw);
				}
				//Driver driver = this.datasource.getDriver();
				
				DbDriver dd = this.datasource.getDbDriver();
				String drivername = dd.getDriverClass();
				Class<Driver> driverClass = (Class<Driver>) Class.forName(drivername);
				Driver driver = driverClass.newInstance();
				DriverManager.registerDriver(driver);
				//con = DriverManager.getConnection(url, "system", "guardium");
				//System.out.println("Connected: " + dbms + " " + user + "@" + url);
				
				//System.out.println("connect driver is " + drivername);
				//System.out.println("connect driver major version is " + driver.getMajorVersion());
				//System.out.println("connect call url is " + this.url);
				//System.out.println("connect props is " + this.props);
				
				try
				{
				con = driver.connect(this.url, this.props);
				}catch (Throwable e)
				{
					//AdHocLogger.logException(e);
					throw new Exception(e);
				}
				/*
				if ( LOG.isDebugEnabled() && watch != null ) {
					LOG.debug( watch.checkElapsed("Connected!") );
				}
				*/
				return con;

			} finally {
				/*
				if ( LOG.isDebugEnabled() ) {
					LOG.debug( "Finixh connect "+id+":"+ (System.currentTimeMillis()-outstandingConns.get(""+this.myId+"+"+this.datasource.getDatasourceId()).getTime())/1000);
				}
				*/
				outstandingConns.remove(""+this.myId+"+"+this.datasource.getDatasourceId());
			}
		}
	}

	/**
	 * Inner class implements the worker thread
	 * @author dtoland on Jan 26, 2007 at 4:30:28 PM
	 */
	protected static class SqlWorker implements Callable<ResultSet> {
		private final Statement stmt;
		private final String sql;

		/**
		 * @param stmt
		 * @param sql
		 */
		protected SqlWorker(Statement stmt, String sql) {
			this.stmt = stmt;
			this.sql = sql;
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		public ResultSet call() throws Exception {
			return this.stmt.executeQuery(this.sql);
		}

		@Override
		public String toString() {
			return this.sql;
		}

	}
}
