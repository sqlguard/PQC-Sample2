/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
/*
import org.apache.torque.TorqueException;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
*/

import com.guardium.data.Datasource;
import com.guardium.data.DatasourceEnum;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.data.Stopwatch;
//import com.guardium.utils.Utils;
import com.guardium.utils.i18n.Say;

public class DatasourceUtil {
	/** Local static logger for the class */
  private static final transient Logger LOG = Logger.getLogger(DatasourceUtil.class);

	public static final String MSSQL_DB_VERSION_2000 = "2000";
	public static final String MSSQL_DB_VERSION_2005 = "2005";
	public static final String MSSQL_DB_VERSION_2008 = "2008";
	public static final int MSSQL_DB_VERSION_NUM_2000 = 2000;
	public static final int MSSQL_DB_VERSION_NUM_2005 = 2005;
	public static final int MSSQL_DB_VERSION_NUM_2008 = 2008;
	public static final String PROCEDURE = "PROCEDURE";
	public static final String TRIGGER = "TRIGGER";

	public static final int USER = 0;
	public static final int SQL_ROLE = 1;
	public static final int APPLICATION_ROLE = 2;
	public static final String SYBASE_IQ = "Adaptive Server IQ";


	/** The amount of milliseconds to wait for a socket connection. */
	private static final transient int SOCKET_TIMEOUT_MILLIS = 12*1000;

	private static Locale locale = getLocale();
	private static ResourceBundle messages = !"ww".equalsIgnoreCase(locale.getLanguage()) ? ResourceBundle.getBundle("com.guardium.dbSource.DataSourceResources") : ResourceBundle.getBundle("com.guardium.dbSource.DataSourceResources", locale);
	
	private static Locale getLocale() {
		
		try {
			Locale.Builder wwLocaleBuilder = new Locale.Builder();
			wwLocaleBuilder.setLanguage("ww");
			wwLocaleBuilder.setRegion("CN");
			
			final String fname = "com.guardium.portal.admin.InstallationLanguage";
			ResourceBundle res = ResourceBundle.getBundle(fname, wwLocaleBuilder.build());
			
			if (res != null) {
				String country = res.getString("locale.country") ;
				String language = res.getString("locale.language") ;
				
				Locale.Builder builder = new Locale.Builder();
				if (!Check.isEmpty(language)) {
					language = language.trim();
					builder.setLanguage(language);
				}
				if (!Check.isEmpty(country)) {
					country = country.trim();
					builder.setRegion(country);
				}
				
				Locale aLocale = builder.build();
				return aLocale ;
			}
		} catch (Exception e) {
			//Do nothing
		}
		
		//Default to en-US
		return new Locale("en", "US");
	}

	/*
	public static void sortDataSourcesByName(List datasources)
	{
		Collections.sort(datasources, Datasource.nameComparator);
	}
	*/
	
	public static void printResultSet(ResultSet rs) throws SQLException
	{
	   ResultSetMetaData rsmd = rs.getMetaData();
	   int numCols = rsmd.getColumnCount();

	   // Display column titles
	   System.out.println("-----------------------------------------");
	   for(int i=1; i<=numCols; i++)
	   {
		  if(i > 1) System.out.print(",");
		  System.out.print(rsmd.getColumnLabel(i));
	   }
	   System.out.println("");
	   System.out.println("-----------------------------------------");

	   // Display data, fetching until end of the result set
	   // Calling next moves to first or next row and returns true if success
	   while(rs.next() )
	   {
		  // Each rs after next() contains next rows data
		  for(int i=1; i<=numCols; i++)
		  {
			 if(i > 1) System.out.print(",");
			 // Almost all SQL types can be cast to a string by JDBC
			 System.out.print(rs.getString(i));
		  }
		  System.out.println("");
	   }
	}

	/**
	 * checks if a name is a sql(database) role in sql server.
	 * @param con
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static boolean isRoleInMssql(Connection con, String name) throws SQLException
	{
		boolean isrole = false;
		if(con != null && !Check.isEmpty(name))
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String type = getMssqlDatabaseVersion(con);

			//this test is added to make sure that the database is a Sql Server not any other types
			if (type.equals(MSSQL_DB_VERSION_2000)|| type.equals(MSSQL_DB_VERSION_2005) || type.equals(MSSQL_DB_VERSION_2008))
			{
				String sql = "select * from ";
				if(type == MSSQL_DB_VERSION_2000)
					sql += "sysusers";
				else
					sql += "sys.sysusers";
				sql += " where issqlrole = 1 and name='" + name + "'";
				ResultSet result = stmt.executeQuery(sql);
				if(result != null && result.next())
				{
					isrole = true;
				}
			}
		}

		return isrole;
	}

	/**
	 * checks if a name is a user in sql server.
	 * @param con
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static boolean isUserInMssql(Connection con, String name) throws SQLException
	{
		boolean isUser = false;
		if(con != null && !Check.isEmpty(name))
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String type = getMssqlDatabaseVersion(con);

			//this test is added to make sure that the database is a Sql Server not any other types
			if (type.equals(MSSQL_DB_VERSION_2000)|| type.equals(MSSQL_DB_VERSION_2005) || type.equals(MSSQL_DB_VERSION_2008))
			{
				String sql = "select * from ";
				if(type == MSSQL_DB_VERSION_2000)
					sql += "sysusers";
				else
					sql += "sys.sysusers";
				sql += " where issqluser = 1 and name='" + name + "'";
				ResultSet result = stmt.executeQuery(sql);
				if(result != null && result.next())
				{
					isUser = true;
				}
			}
		}

		return isUser;
	}

	/**
	 * In a SQl Server database, it determines if a name is a user or a role.
	 * @param con
	 * @param name
	 * @return 0 if name is a user, 1 if it is a sql role, or 2 if it is an application role. Otherwise, it returns -1
	 * @throws SQLException
	 */
	public static int getRoleTypeInMssql(Connection con, String name) throws SQLException
	{
		int roleType = -1;
		if(con != null && !Check.isEmpty(name))
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String type = getMssqlDatabaseVersion(con);

			//this test is added to make sure that the database is a Sql Server not any other types
			if (type.equals(MSSQL_DB_VERSION_2000)|| type.equals(MSSQL_DB_VERSION_2005) || type.equals(MSSQL_DB_VERSION_2008))
			{
				String sql = "select issqluser,issqlrole,isapprole from ";
				if(type == MSSQL_DB_VERSION_2000)
					sql += "sysusers";
				else
					sql += "sys.sysusers";
				sql += " where name='" + name + "'";
				ResultSet result = stmt.executeQuery(sql);
				if(result != null && result.next())
				{
					if(result.getInt(1) == 1)
						roleType = USER;
					else if(result.getInt(2) == 1)
						roleType = SQL_ROLE;
					else if(result.getInt(3) == 1)
						roleType = APPLICATION_ROLE;
				}
			}
		}
		return roleType;
	}


	/**
	 * Gets members of the role specified by name.
	 * @param con
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static List getRoleMembersInMssql(Connection con, String name) throws SQLException
	{
		List members = new ArrayList();

		if(con != null && !Check.isEmpty(name))
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String sql = "sp_helprolemember '" + name + "'";
			ResultSet result = stmt.executeQuery(sql);
			if(result != null)
			{
				while(result.next())
				{
					String member = result.getString(2);
					if(isRoleInMssql(con,member))
					{
						List subMembers = getRoleMembersInMssql(con,member);
						members.addAll(subMembers);
					}
					else
						members.add(member);
				}
			}
		}

		return members;
	}

	/**
	 * checks existance of database.
	 * @param con
	 * @param database
	 * @return
	 * @throws SQLException
	 */
	public static boolean isDatabaseExists(Connection con, String database) throws SQLException
	{
		//catalog is the same as database
		boolean exists = false;
		if(con != null && database != null)
		{
			DatabaseMetaData metaData;
				metaData = con.getMetaData();
				ResultSet result = metaData.getCatalogs();
				result = metaData.getCatalogs();
				ResultSet result2 =metaData.getSchemas();
				DatasourceUtil.printResultSet(result2);
				if(result != null)
				{
					while(result.next())
					{
						String cat = result.getString(1);
						if(database.equalsIgnoreCase(cat))
						{
							exists = true;
							break;
						}
					}
				}
		}

		return exists;
	}
	/**
	 * Finds major version of database. If there is no connection, it returns 0
	 * @param con
	 * @param database
	 * @return
	 * @throws SQLException
	 */
	public static int getDatabaseVersion(Connection con) throws SQLException
	{
		//catalog is the same as database
		int dbVersion = 0;
		if(con != null )
		{
			DatabaseMetaData metaData;
			metaData = con.getMetaData();
			dbVersion = metaData.getDatabaseMajorVersion();
		}

		return dbVersion;
	}
	
	/**
	 * Finds minor version of database. If there is no connection, it returns 0
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static int getDatabaseMinorVersion(Connection con) throws SQLException
	{
		int minorVersion = 0;
		if(con!= null)
		{
			DatabaseMetaData metaData = con.getMetaData();
			minorVersion = metaData.getDatabaseMinorVersion();
		}
		return minorVersion;
	}

	/**
	 * checks whether a specific function is supported by the datasource or not. if not an exception is thrown.
	 *
	 * @param ds - datasource
	 * @param con -Connection to the server
	 * @param function Name of functionality. At this time is only PROCEDURE & TRIGGER
	 * @return
	 * @throws DataSourceException
	 */
	public static boolean checkSupported(Datasource ds, Connection con, String function) throws DataSourceException
	{
		boolean supported = true;
		if (ds != null )
		{
			supported = checkSupported(ds.getDatasourceTypeId(),con,function);
		}

		return 	supported;
	}

	/**
	 * checks whether a specific function is supported by dbType. if not an exception is thrown.
	 *
	 * @param datasourceTypeId - datasource type id
	 * @param con -Connection to the server
	 * @param function Name of functionality. At this time is only PROCEDURE & TRIGGER
	 * @return
	 * @throws DataSourceException
	 */
	public static boolean checkSupported(int datasourceTypeId, Connection con, String function) throws DataSourceException
	{
		boolean supported = true;
		if (!Check.isEmpty(function) && con != null)
		{
			int majorVersion = 0;
			int minorVersion = 0;
			//check if catalog exists
			try {
				DatabaseMetaData metaData;
				metaData = con.getMetaData();
				majorVersion = metaData.getDatabaseMajorVersion();
				minorVersion = metaData.getDatabaseMinorVersion();
//				version = getDatabaseVersion(con);
				if( DatasourceEnum.MYSQL.equals(datasourceTypeId) )
				{
					if(function.equalsIgnoreCase(PROCEDURE) ||
					   function.equalsIgnoreCase(TRIGGER))
					{
						if(majorVersion < 5 )
						{
							supported = false;
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				AdHocLogger.logException(e);
			}
			if(!supported)
			{
				Object[] arguments = {function,DatasourceEnum.getDatasourceTypeName(datasourceTypeId), majorVersion + "." + minorVersion};
				String msg =MessageFormat.format(messages.getString("database.error.functionNotSupported"),arguments);

			     throw new DataSourceException(msg);
			}
		}

		return 	supported;
	}

	/**
	 * checks existance of database. if catalog doesn't exists, throws DataSourceException
	 * @param con
	 * @param catalog
	 * @return
	 * @throws DataSourceException
	 */
	public static boolean checkCatalogExistence(String catalog, Connection con) throws DataSourceException
	{
		boolean exists = false;
		if (catalog != null && catalog.length() > 0 && con != null)
		{
			//check if catalog exists
			try {
				exists = isDatabaseExists(con,catalog);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				AdHocLogger.logException(e);
			}
			if(!exists)
			{
				//if catalog doesn't exists, display an error message
				String err = "'" + catalog + "' ";
				err += messages.getString("database.error.databaseNotFound");
			     throw new DataSourceException(err);
			}
		}

		return 	exists;
	}
	/**
	 * checks existance of database. if catalog doesn't exists, throws DataSourceException
	 * @param con
	 * @param catalog
	 * @return
	 * @throws DataSourceException
	 */
	public static boolean checkCatalogExistence(Datasource datasource,String catalog, Connection con) throws DataSourceException
	{
		boolean exists = false;
		if(datasource == null || Check.isEmpty(catalog) || con == null)
			return exists = false;
		
		//check if catalog exists
		try {
			if(datasource.isOracle() || datasource.isDB2()||datasource.isDB2_400()||datasource.isDB2_ZOS())
				exists = checkForchema(con, catalog);
			else
				exists = isDatabaseExists(con,catalog);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			AdHocLogger.logException(e);
		}
		if(!exists)
		{
			//if catalog doesn't exists, display an error message
			String err = "'" + catalog + "' ";
			err += messages.getString("database.error.databaseNotFound");
		     throw new DataSourceException(err);
		}
		return 	exists;
	}
	public static boolean checkForchema(Connection con,String schema)
	{
		//note: this method returns list of schemas which for oracle is the list of users

		boolean exists = false;
		if(con != null && schema != null)
		{
			try {
				DatabaseMetaData metaData  = con.getMetaData();
				ResultSet results = metaData.getSchemas();
				if(results != null)
				{
					while(results.next())
					{
						String sch = results.getString(1);
						if(sch != null)
						{
							if (sch.trim().equalsIgnoreCase(schema))
							{
								exists = true;
								break;
							}
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				AdHocLogger.logException(e);
			}


		}
		return exists;
	}
/**
 * 
 * For Oracle or DB2, if schemaPatten is empty, it will return value of catalog as the schema. 
 * Otherwise, it returns value of schemaPattern. 
 * @param datasource
 * @param catalog
 * @param schemaPattern
 * @return
 */
	public static String getAdjustedSchemaPattern(Datasource datasource, String catalog,String schemaPattern)
	{
		//Note: this method is added after allowing DB2/Oracle have schema(catalog) in UI
		String adjustedSchema = schemaPattern;
		if(!Check.isEmpty(schemaPattern) || 
				Check.isEmpty(catalog) ||
				datasource == null ||
				(!datasource.isOracle() && !datasource.isDB2() && ! datasource.isDB2_400() && !datasource.isDB2_ZOS()))
					return adjustedSchema;
		adjustedSchema = catalog;
		return adjustedSchema;
	}
	/**
	 * 
	 * For Oracle or DB2, set catalog to null. 
	 * Otherwise, it returns value of catalog.
	 * @param datasource
	 * @param catalog
	 * @return
	 */
		public static String getAdjustedCatalog(Datasource datasource, String catalog)
		{
			//Note: this method is added after allowing DB2/Oracle have schema(catalog) in UI
			if(datasource == null ||
				(!datasource.isOracle() && !datasource.isDB2() && ! datasource.isDB2_400() && !datasource.isDB2_ZOS()))
					return catalog;
			return null;
		}


	/**
	 *
	 * This method can be used to see if MS_SQL is 2000 or 2005.
	 * It returns 2000, 2005 or empty string if it does not apply
	 *
	 * @param con		Connection to database
	 * @return 	Version of SQL Server (2000 or 2005).
	 */

	public static String getMssqlDatabaseVersion(Connection con)
	{
		String dbVersion = new String();
		if(con != null)
		{
			Statement stmt = null;
			try {
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String sql = "select @@version";

				ResultSet result = stmt.executeQuery(sql);
				if(result != null && result.next())
				{
					String versionStr = result.getString(1);
					if (versionStr != null && versionStr.length()>0)
					{
						//check for sql server 2000
						if (versionStr.indexOf(MSSQL_DB_VERSION_2000) > -1)
						{
							//it is sql server 2000
							dbVersion = MSSQL_DB_VERSION_2000;
						}
						else if (versionStr.indexOf(MSSQL_DB_VERSION_2005) > -1)
						{
							//it is server 2005
							dbVersion = MSSQL_DB_VERSION_2005;
						}
						else if (versionStr.indexOf(MSSQL_DB_VERSION_2008) > -1)
						{
							//it is server 2008
							dbVersion = MSSQL_DB_VERSION_2008;
						}
					}
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				AdHocLogger.logException(e);
			}


		}//if(con != null)
		return dbVersion;
	}
	public static int getMssqlDatabaseVersionNumber(Connection con) throws Exception
	{
		int dbVersion = 0;
		int majorVer = getDatabaseVersion(con);
		switch (majorVer)
		{
			case (8):
				dbVersion = MSSQL_DB_VERSION_NUM_2000;
				break;
			case(9):
				dbVersion = MSSQL_DB_VERSION_NUM_2005;
				break;
			case(10):
				dbVersion = MSSQL_DB_VERSION_NUM_2008;
				break;
		}
		return dbVersion;
	}

	public static List getTables(Connection con,String catalog,String schemaPattern,String objectPattern)
	{
		List tableList = new ArrayList();
		if(con != null)
		{
			try
			{
				DatabaseMetaData metaData = con.getMetaData();
				String[] types = new String[]{"TABLE"};
				
				ResultSet results = metaData.getTables(catalog,schemaPattern,objectPattern,types);

				if(results != null)
				{
					while(results.next())
					{
						String tableName = (results.getString(3));
						if(tableName != null)
							tableList.add(tableName);

					}
				}

			}catch (Exception e)
			{
				AdHocLogger.logException(e);
			}

		}//if(con != null)
		return tableList;
	}
/**
 * Finds a list of columns of table specified by tableName
 * @param conAudit
 * @param database
 * @param schema
 * @param tableName
 * @return a column list of
 * @throws SQLException
 */
	public static List getColumns(Connection con,String database,String schema,String tableName) throws SQLException
	{
		List columnList = new ArrayList();
		if(con != null)
		{
			DatabaseMetaData metaData;
			metaData = con.getMetaData();
			ResultSet results = null;
			try
			{
/*		For testing
				results = metaData.getColumns(database,schema,tableName,null);
				if(results != null)
				{
					AdHocLogger.logDebug("Columns in all tables in all schema's using metaData.getColumns()",AdHocLogger.LOG_DEBUG);
					printResultSet(results);
				}
*/
				results = metaData.getColumns(database,schema,tableName,null);
				if(results != null)
				{

					while(results.next())
					{
						//I am reading all info about each column. I don't think I need all of them
						String column_name = results.getString(4);
						if(column_name != null && column_name.length()>0)
						{
							//add the column to the columnList

							columnList.add(column_name);
						}
					}
				}
			}
			finally {
				Check.disposal(results);
			}

		}//if(conAudit != null)
		return columnList;
	}

	/**
	 *
	 * Finds a list of comma separated jobs that are using objectName
	 * with value equal to id of this datasource.
	 *
	 * @param datasourceID		id of datasource
	 * @return 	a string representing comma separated list of jobNames that are
	 *  using objectName with value equal to id of this datasource
	 */
	/*
	public static String getAllScheduledJobsThatUseDatasource(int datasourceID)
	{
		String jobs = new String();
		if(datasourceID>0)
		{
			//check in Monitor
			try
			{
				//search in jobs for jobGroup monitorValuesTask
				List jobList = getJobsOfGroupThatUseDatasource(datasourceID,"MonitorValuesTask", "auditDatasourceId");
				jobs = Utils.convertStrListToCommaSeparatedStr(jobList);

			}catch (Exception e)
			{
				AdHocLogger.logException(e);
			}

		}
		return jobs;
	}
	*/
	
	/**
	 *
	 * Finds a list of scheduled jobs of groupName that are using objectName
	 * with value equal to id of this datasource.
	 *
	 * @param datasourceID		id of datasource
	 * @param groupName		Name of Group
	 * @param objectName	Name of object to search for in JobDataMap
	 * @return 	a list of jobNames that are using objectName with value equal to id of this datasourc
	 */
	/*
	public static List getJobsOfGroupThatUseDatasource(int datasourceID, String groupName, String objectName )
	{
		List jobList = new ArrayList();
		try {
	    	if(groupName != null && groupName.length()>0 &&
	        		objectName != null && objectName.length()>0 )
	        	{
	    			Integer dsId = new Integer(datasourceID);
					Scheduler sched = StdSchedulerFactory.getDefaultScheduler();
					String[] jobs = sched.getJobNames(groupName);
					if (jobs != null)
					{
						for (int i=0; i< jobs.length; i++)
						{
							String job = (String)jobs[i];
							if (job != null && job.length()>0)
							{
								JobDataMap jdm = sched.getJobDetail(job,groupName).getJobDataMap();
								if(jdm.containsKey(objectName))
								{
									Integer idInt = (Integer) jdm.get(objectName);
									if(idInt != null && idInt.equals(dsId))
									{
										//If there is a match, add it to the jobList
										//but first make sure there is a trigger set for it.
										Trigger[] triggers = sched.getTriggersOfJob(job,groupName);
										if(triggers != null && triggers.length>0)
											jobList.add(job);
									}

								}

							}
						}//for (int i=0; i< jobs.length; i++)
					}//if (jobs != null)
	        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AdHocLogger.logException(e);
		}


		return jobList;
	}
	*/
	
	/**
	 *
	 * Finds whether any of scheduled jobs of groupName is using objectName
	 * with value equal to id of this datasource.
	 *
	 * @param datasourceID		id of datasource
	 * @param groupName		Name of Group
	 * @param objectName	Name of object to search for in JobDataMap
	 * @return 	true if a match is found; Otherwise it returns false
	 */
	/*
	public boolean isDatsourceUsedInScheduledJobsOfGroup(int datasourceID, String groupName, String objectName )
	{
		boolean isUsed = false;
		try {
	    	if(groupName != null && groupName.length()>0 &&
	        		objectName != null && objectName.length()>0 )
	        	{
	    			Integer dsId = new Integer(datasourceID);
					Scheduler sched = StdSchedulerFactory.getDefaultScheduler();
					String[] jobs = sched.getJobNames(groupName);
					if (jobs != null)
					{
						for (int i=0; i< jobs.length; i++)
						{
							String job = (String)jobs[i];
							if (job != null && job.length()>0)
							{
								JobDataMap jdm = sched.getJobDetail(job,groupName).getJobDataMap();
								if(jdm.containsKey(objectName))
								{
									Integer idInt = (Integer) jdm.get(objectName);
									if(idInt != null && idInt.equals(dsId))
									{
										//there is a match
										isUsed = true;
										break;
									}

								}

							}
						}
					}//if (jobs != null)
	        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AdHocLogger.logException(e);
		}


		return isUsed;
	}
	*/
	
	/**
	 *
	 * Finds whether any of scheduled jobs is using objectName
	 * with value equal to id of this datasource.
	 *
	 * @param datasourceID		id of datasource
	 * @return 	true if a match is found; Otherwise it returns false
	 */
	/*
	public boolean isDatasourceUsedInAnyScheduledJobs(int datasourceID)
	{
		boolean isUsed = false;
		if(datasourceID>0)
		{
			//check in Monitor
			try
			{
				//search in jobs for jobGroup monitorValuesTask
				isUsed = isDatsourceUsedInScheduledJobsOfGroup(datasourceID, "MonitorValuesTask", "auditDatasourceId" );
			}catch (Exception e)
			{
				AdHocLogger.logException(e);
			}

		}
		return isUsed;
	}
	*/
	
	static public Long getNumber(Object value){
		if(value!=null){
			return Long.parseLong(value.toString().trim());
		}
		return null;
	}

	static public Object executeSqlForSingleVal(String query, int colNum,Connection con) throws SQLException{
		Object value = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				value = rs.getObject(colNum);
			}
			return value;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	/**
	 * Determines if a host exists and is listening to the port.
	 * @param host The desired host.
	 * @param port The desired port.
	 * @return Whether the host is alive and listening.
	 * @throws DataSourceConnectException
	 */
	public static boolean checkHostPortOpen(String host, int port)
	throws DataSourceConnectException {
		Stopwatch watch = new Stopwatch();

		Socket sock = null;
		InetSocketAddress addr = null;
		try {
			addr = new InetSocketAddress(host, port);
			sock = new Socket();
			sock.connect(addr, SOCKET_TIMEOUT_MILLIS);
			return true;

		} catch (IOException e) {
			String err= messages.getString("database.error.noSocketConnect") + addr;
			throw new DataSourceConnectException( watch.checkElapsed(err), e);

		// if the port number is invalid, such as -1
		} catch (IllegalArgumentException e) {
			String err= messages.getString("database.error.noSocketConnect") + addr;
			throw new DataSourceConnectException( watch.checkElapsed(err), e);

		} finally {
			sock = Check.disposal(sock);
		}
	}

	/**
	 * Determines if a host exists and is listening to the port.
	 * @param datasource
	 * @return Whether the host is alive and listening.
	 * @throws DataSourceConnectException
	 */
	public static boolean checkHostPortOpen(Datasource datasource)
	throws DataSourceConnectException {
		                                               
		int port = (datasource.getPort() == 0 ? datasource.getDefaultPort(): datasource.getPort());
		return checkHostPortOpen(datasource.getHost(), port );
	}

	/**
	 * @param datasource
	 * @return The number of tables in the datasource according to metadata.
	 * @throws DataSourceConnectException
	 */
	public static int countTables(Datasource datasource)
	throws DataSourceConnectException {
		Connection con = null;
		try {
	    con = datasource.getConnection();
	    return countTables(con);
    } finally {
	    con = Check.disposal(con);
    }
	}


	/**
	 * @param con
	 * @return The number of tables in the datasource according to metadata.
	 * @throws DataSourceConnectException
	 */
	public static int countTables(Connection con) throws DataSourceConnectException {
		int result = 0;
		ResultSet rs = null;
		try {
	    DatabaseMetaData meta = con.getMetaData();
	    rs = meta.getTables(null, null, null, null);
	    while ( rs.next() ) {
	    	result++;
	    }
	    return result;

		} catch (Throwable t) {
			String msg = "Could not get a list of tables for: '" + Informer.findUrl(con) + "'";
			throw new DataSourceConnectException(msg + Say.NL + Informer.causality(t), t);

    } finally {
    	rs = Check.disposal(rs);
    }
	}

	/**
	 * Test Case
	 * @param ds
	 * @return A list of table types.
	 */
	public static List<String> listTableTypes(Datasource ds) {
		List<String> result = new ArrayList<String>();

		Connection con = null;
		ResultSet rs = null;
		try {
      con = ds.getConnection();
      DatabaseMetaData meta = con.getMetaData();
      rs = meta.getTableTypes();
      while ( rs.next() ) {
      	String type = rs.getString(1);
      	result.add(type);
      }

    } catch (DataSourceConnectException e) {
      LOG.debug("Could not connect to: " + ds);

    } catch (SQLException e) {
      LOG.debug("Could not get metadata for: " + ds);

    } finally {
    	rs = Check.disposal(rs);
    	con = Check.disposal(con);
    }

    LOG.info(ds + Say.SP + result.toString() );
    return result;
	}

	public static boolean isOracleCon(Connection con) throws SQLException{
		if(con.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle"))
			return true;
		else
			return false;
	}
	public static boolean isSybaseIQ(Connection con)
	{
		boolean sybaseIQ = false;
		
		if (con == null)
			return  sybaseIQ;
		
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String sql = "select @@version";
			
			result = stmt.executeQuery(sql);
			if(result != null && result.next())
			{
				String versionStr = result.getString(1);
				if (versionStr != null && versionStr.length()>0)
				{
					//check for Sybase IQ
					if (versionStr.indexOf(SYBASE_IQ) > -1)
					{
						//it is Sybase IQ
						sybaseIQ = true;
					}
				}
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			AdHocLogger.logException(e);
		}
		finally
		{
			Check.disposal(result);
			Check.disposal(stmt);
		}
					
		return sybaseIQ;
	}

}
