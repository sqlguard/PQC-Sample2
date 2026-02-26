/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
//import org.apache.torque.TorqueException;

import com.guardium.data.Datasource;
import com.guardium.data.ConcurrentConnecter;
import com.guardium.data.DataSourceConnectException;
import com.guardium.map.DatasourceVersionHistoryMap;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Dater;
import com.guardium.utils.Informer;
import com.guardium.utils.Regexer;
import com.guardium.utils.Utils;
import com.guardium.data.Stopwatch;
import com.guardium.data.DatasourceHandler;
//import com.guardium.utils.Utils;
import com.guardium.utils.i18n.Say;
import com.guardium.utils.i18n.SayAppRes;

 class JDBCDatasourceHandler extends DatasourceHandler {
	/** Constant Query for Informix major version */
	private static final String VER_IFX_MAJ = "SELECT UNIQUE dbinfo('version', 'major') FROM systables";
	/** Constant Query for Informix minor version */
	private static final String VER_IFX_MIN = "SELECT UNIQUE dbinfo('version', 'minor') FROM systables";
	/** Constant Query for Informix full version information */
	private static final String VER_IFX_FULL = "SELECT UNIQUE dbinfo('version', 'full') FROM systables";
	/** Constant Query for MSS full version information */
	private static final String VER_MSS_FULL = "SELECT @@version";
	/** Constant Query for MSS full version information */
	private static final String VER_ORA = "SELECT version FROM v$instance where upper(instance_name) = upper(?)";
	/** Constant Query For Teradata PDE Version information */
	private static final String VER_TRD_PDE = "select InfoData from DBC.DBCInfo where InfoKey = 'RELEASE'";
	/** Constant Query For Teradata TDBMS Version information */
	private static final String VER_TRD_TDBMS = "select InfoData from DBC.DBCInfo where InfoKey = 'VERSION'";
	/** Constant Query for Netezza full version information */
	private static final String VER_NTZA = "select version();";
	/** Constant Query for PostgreSql full version information */
	private static final String VER_POSTGRE = "select version();";
	/** Constant Query for DB2 full version information */
	private static final String VER_DB2 = "SELECT SERVICE_LEVEL FROM TABLE(SYSPROC.ENV_GET_INST_INFO()) AS INSTANCEINFO";

	DatasourceVersionHistoryMap DatasourceVersionHistoryPeer = DatasourceVersionHistoryMap.getDatasourceVersionHistoryMapObject();

	
	Datasource datasource = null;
	/** Local static logger for class */
	private static  transient Logger LOG;
	/** stores whether this datasource support catalogs (db_name) */
	private Boolean catalogSupported = null;
	
	JDBCDatasourceHandler(Datasource datasource)
	{
		this.datasource = datasource;
	}
	
	JDBCDatasourceHandler(Datasource datasource, Logger log)
	{
		this.datasource = datasource;
		this.LOG = log;
	}
	/**
	 * Connects using the screen name and password if present.
	 * Otherwise uses the stored versions.
	 * @return a Connection
	 * @throws DataSourceConnectException
	 */
	Connection getConnection() throws DataSourceConnectException {
		if(!datasource.isSqltype())
			throw new DataSourceConnectException(SayAppRes.say("datasource.error.noJdbcConnection"));
		String user;
		String pw;

		// see if the id and password were entered on the screen.
		if ( !datasource.getPasswordStored() ) {
			user = datasource.getScreenUserName();
			if ( Check.isEmpty(user) ) {
				user = datasource.getUserName();
			}

			// couldn't find a user
			if ( Check.isEmpty(user) ) {
				String msg = Say.what(
						Say.SQL_MSG_USER_REQ,
						Say.SQL_SUB_OBJ_DESC, datasource.getInstanceName()
				);
				throw new DataSourceConnectException(msg);
			}

			// get the password
			pw = datasource.getScreenPassword();
			if ( Check.isEmpty(pw) ) {
				String msg = Say.what(
						Say.SQL_MSG_PASSWORD_REQ,
						Say.SQL_SUB_OBJ_DESC, datasource.getInstanceName(),
						Say.SQL_SUB_USER, user
				);
				throw new DataSourceConnectException(msg);
			}

		} else {
			// use the stored versions
			user = datasource.getUserName();
			pw = datasource.getDecryptedPassword();
		}
		return getConnection(user, pw);
	}
	/**
	 * Gets a connection to the database.
	 * To Connect to informix, takes the following steps if a user enters a database name (catalog) :
	 * 	-Always connects to sysmaster database
	 *  -Gets list of all databases and their locals from "sysdbslocals".
	 *  -Since informix, interprets database name using the characterset of sysmaster, finds the characterset of database.
	 *    	To do this, if the database name is in different characterset, converts the database name using characterset of sysmaster.
	 *    	Since It doesn't know which character set a database has, converts the database name from each possible characterset to
	 *   	the sysmaster's characterset. Does this until it finds that database in the "sysdbslocals".
	 *  -Then, Finds its encoding to set DB_LOCALE for that database.
	 *  -Closes connection to sysmaster.
	 *  -Creates a new connection to the database(user entered) by setting DB_LOCALE in url to the locale that it was found in step 4.
	 * In Informix make connecttion to different databases with different locale possible
	 *
	 * To Connect to informix, take the following steps if a user enters a database name (catalog) :
	 *
	 * 1)always connect to sysmaster database.
	 * 2) get list of all databases and their locals from "sysdbslocals".
	 * 3) Since informix, interprets database name using the characterset of sysmaster, find the characterset of database.
	 * 	To do this, if the database name is in different characterset, convert the database name using characterset of sysmaster.
	 * 	Since we don't know which character set a database has, convert the database name from each possible characterset to
	 * 	the sysmaster's characterset. Do this until we find that database in the "sysdbslocals".
	 * 4) Then, Find its encoding to set DB_LOCALE for that database.
	 * 5) close connection to sysmaster.
	 * 6) create a new connection to the database(user entered) by setting DB_LOCALE in url to the locale that we found in step 4.
	 *
	 * Note: first time, usingInformixDbName is always false
	 *
	 * @param userName
	 * @param password
	 * @return A Connection to the datasource.
	 * @throws DataSourceConnectException
	 * @throws DataSourceConnectException
	 */
	Connection getConnection(String userName, String password)
	throws DataSourceConnectException {
		String user;
		String pw;

		// register the jdbc driver, if necessary
//		this.registerDriver();

		if ( datasource.getPasswordStored() ) {
			user = datasource.getUserName();
			pw = datasource.getDecryptedPassword();
		} else {
			user = userName;
			pw = password;
		}

		try {
			Properties props = datasource.getOtherConnectionProperties();

			String url = datasource.getUrl();
			DriverManager.setLoginTimeout(datasource.connectTimeoutSeconds);

			if ( LOG.isDebugEnabled() ) { LOG.debug("Attempting connection to: " + url); }

			if ( datasource.isTextFtp() || datasource.isTextSmb() ) {
				url = url.replaceAll("<USER>",user);
				
				// 2011-06-05 sbuschman 29198
				try {
					pw = URLEncoder.encode(pw, "UTF-8");
				} catch (UnsupportedEncodingException e) {e.printStackTrace(); }

				url = url.replaceAll("<PASSWD>",pw);
			}

			// check if the server socket is accepting connections
//			if ( !this.isText() && !this.isTextHttps() && !this.usesSsl(props) ) {
//				DatasourceUtil.checkHostPortOpen(this);
//			}
			if( datasource.isCsv())
			{
				if(!props.containsKey("maxScanRows"))
				{
					props.put("maxScanRows", "0");
				}
			}

			Stopwatch watch = new Stopwatch();
			Connection con = ConcurrentConnecter.connect(url, user, pw, datasource.connectTimeoutSeconds*1000, props,datasource);

			if ( LOG.isDebugEnabled() ) {
				LOG.debug( watch.checkElapsed("Successfully connected to: " + datasource.getInstanceName() ) );
			}

			// check if the informix database needs a database name other than the default
			if ( datasource.isInformix() ) {
				if ( needsInformixDbName() ) {
					con = connectUsingInformixDbName(con, user, pw);
				}
			}

			// update the last connection field.
			datasource.setLastConnect( new Date() );
			// Update the last connection only if this is a real DS
			// If called from App User Translation the Datasource Object should not be saved
			// and does not have a name (temporary object created only to get the connection)
			if ( !Check.isEmpty( datasource.getName() ) ) {
				try {
					datasource.save();
				} catch (Exception e) {
					// Shouldn't happen if turbine is functional
					AdHocLogger.logException(e);
					LOG.warn("Could not save last connect time. " + Informer.thrownMessage(e) );
				}
			}

			return con;

		} catch (DataSourceConnectException e) {
			DataSourceConnectException dsce = datasource.expandConnectionException(e);
			throw dsce;
		}
	}
	/**
	 *
	 * @param con
	 * @param userName
	 * @param password
	 * @return A connection
	 * @throws DataSourceConnectException
	 */
	private Connection connectUsingInformixDbName(Connection con, String userName, String password)
	throws DataSourceConnectException {

		datasource.setUsingInformixDbName(true);

		//getdbLocalMap and populate list of locals
		List<String> locales = new ArrayList<String>();
		HashMap<String,String> dbLocaleMap = getInformixDatabaseLocaleMap(con, locales);

		String database= datasource.getCatalog();
		try {
			database= this.getDbNameUsingSysmasterCharacterset( datasource.getCatalog(), con, dbLocaleMap, locales);
			if( database.length() >0) {

				//get locale of database
				String dbLocale = null;
				if(dbLocaleMap != null) {
					dbLocale = dbLocaleMap.get( database.toUpperCase() );
				}

				// empty locale should be null?
				if ( Check.isEmpty(dbLocale) ) {
					dbLocale = null;
				}

				//set locale
				datasource.setLocale(dbLocale);
				Check.disposal(con);

				//reset url
				datasource.setUrl(null);
				return  this.getConnection(userName, password);
			}

			return con;

		} finally {
			Check.disposal(locales);
			Check.disposal(dbLocaleMap);
		}
	}
	/**
	 * converts Database name to the characterset of sysmaster. This method is used only for informix.
	 * It is added to handle databases of different locale.
	 *
	 * @param catalog  Database name to be converted to the characterset of sysmaster
	 * @param con      Database connection
	 * @param dbLocaleMap
	 * @param locales
	 * @return  Database name converted to the characterset of sysmaster
	 * @throws DataSourceConnectException
	 */
	private String getDbNameUsingSysmasterCharacterset(String catalog,Connection con,HashMap dbLocaleMap, List locales)throws DataSourceConnectException
	{
		String dbName = catalog;
		//get characterset map
		HashMap charactersetMap = Utils.getCharactersetMap();

		if(dbLocaleMap != null )
		{
			//if sysmaster and catalog have the same local, ignore
			String sysmasterLocale = (String)dbLocaleMap.get("SYSMASTER");
			String dbLocale = (String)dbLocaleMap.get(catalog.toUpperCase());
			boolean found = false;

			if(sysmasterLocale != null)
			{
				String sysCharacterset = this.getCharactersetFromCollate(charactersetMap, sysmasterLocale);
				if(dbLocale != null && dbLocale.length()>0)
				{
// If dbLocale is found, we are done.  Because, the entire purpose of this method
// is to be able to get dbLocale of that database
//
					found = true;
				}//if(dbLocale != null && dbLocale.length()>0)
				if (!found)
				{
					//Query sysdbslocale to find locale of database entered by the user
					//Note: database name entered by the user, must first be converted to the characterset of the sysmaster,
					//This is done in order to find dbname in the "sysdbslocale" of sysmaster.
					if(locales != null )
					{

						for(int i=0; i<locales.size(); i++)
						{
							//loop through all possible charactersets in locals, to find the correct one for the database name.
							String locale = (String)locales.get(i);
							if(locale != null && locale.length()>0)
							{
								String characterset = this.getCharactersetFromCollate(charactersetMap,locale);
								String  convDbName = Utils.convertCharacterset(catalog,characterset,sysCharacterset);

								if(dbLocaleMap.containsKey(convDbName.trim().toUpperCase()))
								{
									dbName = convDbName.trim();
									found = true;
									break;
								}
							}
						}

					}//if(locals != null )

				}//if (!found)

				//if catalog is not found, then throw an exception to display an error message
				if(!found)
				{
					String errMsg = SayAppRes.say("database.error.connectToDb");
					String err= SayAppRes.say("database.error.Informix.databaseNotFound");
					errMsg += ": " + err;
					throw new DataSourceConnectException(err);
				}
			}//if(sysmasterLocale != null)
		}//if(dbLocalMap != null )
		//find encoding of catalog

		return dbName;
	}
	/**
	 * Finds locale from collate.  Collate looks like ja_JP.57351.
	 * @param charactersetMap
	 * @param sysmasterLocale
	 * @return
	 */
	private  String getCharactersetFromCollate(HashMap charactersetMap,String sysmasterLocale)
	{
		String characterset = new String();
		if(sysmasterLocale != null && sysmasterLocale.length()>0)
		{
			//search for "."
			//collate looks like ja_JP.57351
			int index = sysmasterLocale.indexOf('.');
			if(index > -1)
			{
				String charset = sysmasterLocale.substring(index+1, sysmasterLocale.length());
				if(charactersetMap != null && charactersetMap.containsKey(charset))
				{
					characterset = (String)charactersetMap.get(charset);
				}
				else
				{
					//may be we need to remove this
					characterset = charset;
				}
			}
		}
		return characterset;
	}


	private  HashMap<String,String> getInformixDatabaseLocaleMap(Connection con, List<String> locales)
	{
		HashMap<String,String> dbLocaleMap = new HashMap<String,String>();
		if(con != null && locales != null)
		{
			Statement stmt = null;
			try {
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String sql = "select dbs_dbsname, dbs_collate from sysdbslocale";
				ResultSet results = stmt.executeQuery(sql);
				if(results != null)
				{
					while(results.next())
					{
						String dbName = (results.getString(1) != null ? results.getString(1).trim().toUpperCase() : null) ;
						String locale = (results.getString(2) != null ? results.getString(2).trim() : ""); ;

						//populate dbLocaleMap with dbName and its locale
						if(dbName != null && !dbLocaleMap.containsKey(dbName))
						{
							dbLocaleMap.put(dbName,locale);
						}

						//populate locals with a list of unique locals
						if(locale.length()>0 && !locales.contains(locale))
						{
							locales.add(locale);
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}//if(con != null)
		return dbLocaleMap;
	}

	/**
	 * @return Whether the Informix Db name must be calculated.
	 */
	private boolean needsInformixDbName() {
		boolean needsIt =
			datasource.isInformix()
			&& !datasource.isUsingInformixDbName()
			&& !Check.isEmpty( datasource.getCatalog() )
			&& !"SYSMASTER".equals( datasource.getCatalog().toUpperCase() )
		;
		return needsIt;
	}

	/**
	 * Takes into account Text datasources that can't verify whether the connection is good
	 * until the file is read.
	 * @return Whether the connection was successful.
	 * @throws DataSourceConnectException If the connection was not successful.
	 */
	public boolean testConnection() throws DataSourceConnectException {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			
			if ( datasource.isCsv() ) {
				String sql = "select top 1 * from " + Say.QT2 + datasource.getDbName() + Say.QT2;
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(sql);
			}
			
			// didn't throw an exception, return true
			if (con!=null)
				return true;
			else 
				return false;
			
		} catch (SQLException e) {
			String msg = Say.what(
					Say.SQL_MSG_CONNECT_FAILURE,
					Say.SQL_SUB_URL, datasource.getUrl(),
					Say.SQL_SUB_USER, datasource.getScreenName()
			);
			boolean includeDetails = true;
			if ( datasource.isCsv() ) {
				includeDetails = false;
			}
			msg = msg + Say.NL + Informer.thrownMessage(e,includeDetails);
			LOG.warn(msg);
			throw new DataSourceConnectException(msg, e);
			
		} catch (Throwable e) {
			String msg = Say.what(
					Say.SQL_MSG_CONNECT_FAILURE,
					Say.SQL_SUB_URL, datasource.getUrl(),
					Say.SQL_SUB_USER, datasource.getScreenName()
			);
			boolean includeDetails = true;
			if ( datasource.isCsv() ) {
				includeDetails = false;
			}
			msg = msg + Say.NL + Informer.thrownMessage(e,includeDetails);

			LOG.warn(msg);
			throw new DataSourceConnectException(msg, e);
			
		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
			con = Check.disposal(con);
		}
	}
	
	@Override
	public String getFullVersionInfo() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return getFullVersionInfo(con);

		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * This may or maynot contain version and patch information depending on the Database Vendor.
	 * @param con A connectin to use.
	 * @return The JDBC Database Product Version string.
	 */
/*using DBConnection class	
	public  String getFullVersionInfo(DBConnection con){
		return getFullVersionInfo(((JDBCConnection)con).getCon());
	}
*/	
	/**
	 * This may or maynot contain version and patch information depending on the Database Vendor.
	 * @param con A connectin to use.
	 * @return The JDBC Database Product Version string.
	 */
	String getFullVersionInfo(Connection con) {
		DatasourceVersionHistory history = this.findLatestVersionHistory(con);
		return history.getFullVersionInfo();
	}
	/**
	 * Saves an entry to the list if the version or patch is new
	 * Updates the entry with the confirm date and datasource and type name change
	 * if the patch level can be found.
	 * Will only query the datasource for new information if there has not been an update in the
	 * past time period.
	 * @param con
	 * @return The most recent datasource version history information.
	 */
	DatasourceVersionHistory findLatestVersionHistory(Connection con) {
		// get the latest version history
		DatasourceVersionHistory history = null;
		//try {
			// get the list of version histories             getDatasourceVersionHistorys
			List<DatasourceVersionHistory> list = datasource.getDatasourceVersionHistorys();
			if ( Check.isEmpty(list) ) {
				history = this.findVersionHistory(con);
				datasource.addVersionHistory(history);

			} else {
				history = list.get(0);			

				// for performance and looping during multiple accesses
				// see if we have found the information recently and just return it if we have
				//double age = Dater.minutesAfter( history.getVerified() );
				double age = Dater.minutesAfter( history.getVerified() );
				if ( LOG.isDebugEnabled() ) {
					String msg =
						"Latest Version History age: "  + age
						+ " minutes. Detection period: " + Datasource.VERSION_DETECTION_PERIOD + ": " + history
					;
					LOG.debug(msg);
				}

				if (age > Datasource.VERSION_DETECTION_PERIOD) {
					history = this.findVersionHistory(con);
					datasource.addVersionHistory(history);
				}
			}

		/*
		} catch (Exception e) {
			// should not happen if torque has been functioning
			String msg = "Could not get Version History for: " + this;
			LOG.error(msg, e);
			AdHocLogger.logException(e);
		}
		*/
		return history;
	}
	/**
	 * @param con
	 * @return A new version history
	 */
	DatasourceVersionHistory findVersionHistory(Connection con) {
		String version = DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
		String patch = DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
		String fullInfo = null;

		if ( !Check.isEmpty(con) ) {
			version = this.findVersion(con);
			patch = this.findPatch(con);
			fullInfo = this.findFullInfo(con);
			// use fullInfo as version
			//version = fullInfo;
		}

		// return the history
		DatasourceVersionHistory history = new DatasourceVersionHistory(datasource, fullInfo, patch, version, datasource.getVersionType());
		return history;
	}
	/**
	 * @param con
	 * @return The datasouce version for adding to the version history.
	 */
	@SuppressWarnings("fallthrough")
	String findVersion(Connection con) {
		
		DatasourceEnum type = DatasourceEnum.getByDatasourceTypeId( datasource.getDatasourceTypeId() );
		switch (type) {							
			case MSSQL:
			case MYSQL:
			case POSTGRESQL:	
			case GREENPLUM:	
			case ASTER:
				try {
					DatabaseMetaData meta = con.getMetaData();
					int major = meta.getDatabaseMajorVersion();
					int minor = meta.getDatabaseMinorVersion();
					return major + Say.DOT + minor;

				} catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				break;
			case DB2:
			{
				String version = null;
				try {
					DatabaseMetaData meta = con.getMetaData();
					int major = meta.getDatabaseMajorVersion();
					int minor = meta.getDatabaseMinorVersion();
					version =  major + Say.DOT + minor;

				} catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				// jdbc returns 8.2 for 8.1 after patch 7 but only later versions support sql to retreive the version
				if(!"8.1".equals(version))
				{
					Statement stmt = null;
					ResultSet rs = null;
					String versql = VER_DB2;
					try {					
						stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						rs = stmt.executeQuery(versql);
						rs.next();
						version = rs.getString(1);
						/*
						 * result version string is in the format 
						 * DB2 v8.2.1.128   
						 * from this we want only the first two numerics 8.2 
						 */
						if ( !Check.isEmpty(version) ) {
							String[] elements = version.split(" ");
							if (elements.length > 0) 
							{
								String [] versionA = elements[1].split("\\.");
								version = versionA[0].replaceAll("\\D", "")+"."+versionA[1];
							}
						}
					}
					catch (SQLException e) 
					{
						if("8.2".equals(version))
						{
							/*
							 * If the sql fails then we set the version to 8.1  
							 */
							version="8.1";
							String msg = "sql version query failed - version assumed to be 8.1: " + this;
							LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
							AdHocLogger.logException(e);
						}
						else
						{
							 String msg = "Could not access meta data for: " + this;
							 LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
						}
					}
					finally {
						rs = Check.disposal(rs);
						stmt = Check.disposal(stmt);
					}
				}
				if(!Check.isEmpty(version))
						return version;
			}
				break;
			case DB2_ZOS:	
			{
				String version = null;
				try 
				{
					DatabaseMetaData meta = con.getMetaData();
					int major = meta.getDatabaseMajorVersion();
					int minor = meta.getDatabaseMinorVersion();
					version =  major + Say.DOT + minor;

				} 
				catch (SQLException e) 
				{
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
				}
				if(!Check.isEmpty(version))
					return version;
			}
				break;
				
	       case DB2_400:
	    	    try {
				String version = con.getMetaData().getDatabaseProductVersion();
				if ( !Check.isEmpty(version) ) {
					// 07.01.0000 V7R1m0
					String[] elements = version.split(" ");
					String temp = null;
					temp = elements[0];
					if ( !Check.isEmpty(temp) ) {
						version = temp;
					    StringTokenizer t = new StringTokenizer(version,Say.DOT);
					    String realVersion = null;
					    for ( int i=0 ; i < 2 & t.hasMoreElements() ; i++) {
					    	if (realVersion == null) {
					    		temp = t.nextToken();
					            // remove the first 0
					            if (temp.startsWith("0")) {
					            	temp = temp.substring(1);
					            } 
					            realVersion = temp;
					        }
					        else {
					            temp = t.nextToken();
					            // remove the first 0
					            if (temp.startsWith("0")) {
					            	temp = temp.substring(1);
					            }
					            realVersion = realVersion + Say.DOT + temp;
					        }
					    }
					
					    if (realVersion != null)
					    	return realVersion;
					    }
					}
					} catch (SQLException e) {
						String msg = "Could not access meta data for: " + this;
					    LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					    AdHocLogger.logException(e);
					}
					
					break;
			case INFORMIX:
				// Informix JDBC driver metadata mis-reports 11.0 as 10.1
				Statement ifxStmt = null;
				ResultSet ifxRs = null;
				try {
					ifxStmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ifxRs = ifxStmt.executeQuery(VER_IFX_MAJ);
					ifxRs.next();
					String major = ifxRs.getString(1);
					if ( !Check.isEmpty(major) ) { major = major.trim(); }
					ifxRs = Check.disposal(ifxRs);

					ifxRs = ifxStmt.executeQuery(VER_IFX_MIN);
					ifxRs.next();
					String minor = ifxRs.getString(1);
					if ( !Check.isEmpty(minor) ) { minor = minor.trim(); }
					return major + Say.DOT + minor;

				} catch (SQLException e) {
					String msg = "Could not access version data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);

				} finally {
					ifxRs = Check.disposal(ifxRs);
					ifxStmt = Check.disposal(ifxStmt);
				}
				break;
            // new code
            case ORACLE:
                try {
                        String version = con.getMetaData().getDatabaseProductVersion();      
                        //LOG.info("Oracle version 1 is " + version);
                        version = parseProductVersion(version, Say.SP);
                        //LOG.info("Oracle version 2 is " + version);

                        // try again to use sql command to get version
                        if ( Check.isEmpty(version) ) {
                                PreparedStatement oraStmt = null;
                                ResultSet oraRs = null;
                                try {
                                        oraStmt = con.prepareStatement(VER_ORA);
                                        String service = datasource.getServiceName();
                                        oraStmt.setString(1, service);
                                        oraRs = oraStmt.executeQuery();
                                        String version11 = null;
                                        if (oraRs.next()) {
                                                version11 = oraRs.getString(1);
                                                //LOG.info("Oracle version 3 is " + version11);
                                        }

                                        if ( !Check.isEmpty(version11) ) {
                                                version = version11.trim();
                                        } else {
                                                // if for some reason we got nothing
                                                // rather than return a potentially incorrect version just use the major and minor
                                                DatabaseMetaData meta = con.getMetaData();
                                                int major = meta.getDatabaseMajorVersion();
                                                int minor = meta.getDatabaseMinorVersion();
                                                version = major + Say.DOT + minor ;
                                                //LOG.info("Oracle version 4 is " + version);
                                        }

                                } catch (SQLException e) {
                                        // couldn't access the v$instance view.
                                        // rather than return a potentially incorrect version just use the major and minor
                                    String msg = "Could not access version data for: " + this;
                                    //LOG.error(msg + Say.NL + Informer.thrownMessage(e) );
                                    //AdHocLogger.logException(e);
                                    DatabaseMetaData meta = con.getMetaData();
                                    int major = meta.getDatabaseMajorVersion();
                                    int minor = meta.getDatabaseMinorVersion();
                                    version = major + Say.DOT + minor;
                                    //LOG.info("Oracle version 5 is " + version);

                            } finally {
                                    oraRs = Check.disposal(oraRs);
                                    oraStmt = Check.disposal(oraStmt);
                            }
                    }
                    return version;

            } catch (SQLException e) {
                    String msg = "Could not access meta data for: " + this;
                    //LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
                    //AdHocLogger.logException(e);
            }
            break;

            
			case SYBASE:	
				// need to parse other info out of the sybase version
				try {
					String version = con.getMetaData().getDatabaseProductVersion();
					version = parseProductVersion(version, Say.SLASH);
					return version;

				} catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				break;
				
			case SYBASE_IQ:
				try {
					String version = con.getMetaData().getDatabaseProductVersion();
					version = parseProductVersion(version, Say.SLASH);
					StringTokenizer t = new StringTokenizer(version,Say.DOT);
					String realVersion = null;
					for ( int i=0 ; i < 2 & t.hasMoreElements() ; i++) {
						if (realVersion == null)
							realVersion = t.nextToken();
						else 
							realVersion = realVersion + Say.DOT + t.nextToken();
					}    
					if (realVersion != null)
						return realVersion;

					return version;
				} catch (SQLException e) { 
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}    
				break;
				
			case TERADATA:
				Statement trdStmt = null;
				ResultSet trdRs = null;
				String verSQL = VER_TRD_PDE;
				try {
					if (datasource.getVersionType().toUpperCase().equals("TDBMS"))
						verSQL = VER_TRD_TDBMS; // PDE Used as default
						
						trdStmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						trdRs = trdStmt.executeQuery(verSQL);
						trdRs.next();
						String version = trdRs.getString(1);
						if ( !Check.isEmpty(version) ) {
							int pos = version.lastIndexOf(".");
							if (pos >= 0) {
								version = version.substring(0,pos).trim();
								return version;
							}
						}
				}
				catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					trdRs = Check.disposal(trdRs);
					trdStmt = Check.disposal(trdStmt);
				}
				break;
			case NETEZZA:
			{
				Statement stmt = null;
				ResultSet rs = null;
				String versql = VER_NTZA;
				try {					
					stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					rs = stmt.executeQuery(versql);
					rs.next();
					String version = rs.getString(1);
					//LOG.warn("datasource version is " + version);
					// Release 4.5 (P-3) [Build 7835]
					// Feb, 2014
					// Netezza has recently make change to their version and patching mechanism.
					// Old patch from the database server look like this:
                    //
                    // Release 4.6.8 (P-1)
                    // Release 6.0.6 
                    // 
                    // New method look like this.
                    // 
                    // Release 7.0.4.3-P1
                    // Release 7.0.4.3
                    // Release 7.0.4
                    //
                    // The first three element is the version.  
                    // The four element is the patch
                    // the fifth element is the sub-patch.
					// The code here needs to support old and new ways.
					//
					
					// old method
					// Release 4.6.8 (P-1)
					// Release 6.0.6 
					// Release 7.0, Dev 1 [Build 24913]
					// Release 6.0.6 [Build 22347]
					// Release 4.6.8 (P-1) [Build 13301]

					// New method look like this.

					// Release 7.0.4.3-P1
					// Release 7.0.4.3
					// Release 7.0.4
					
					// testing
					//version = "Release 4.6.8 (P-1)";
					//version = "Release 6.0.6";
					//version = "Release 7.0, Dev 1 [Build 24913]";
					//version = "Release 6.0.6 [Build 22347";
					//version = "Release 7.0.4.3-P1";
					//version = "7.0.4.3";
					//version = "7.0.4";
					
					String build_str = "[Build ";
					String release_str = "Release";
					if ( !Check.isEmpty(version) ) {
						int bidx = version.indexOf(build_str);
						if (bidx > 0) {
							// remove anything after [Build
							version = version.substring(0, bidx);
						}
						//LOG.warn("datasource version 2 is " + version);
						
						// remove starting word "Release"
						if (version.startsWith(release_str, 0)) {
							version = version.substring(release_str.length() + 1 );
							//LOG.warn("datasource remove Release version is " + version);
						}
						version.trim();
					}
					
					String dot = ".";
					if ( !Check.isEmpty(version) ) {
						String[] elements = version.split("\\s+");
						if (elements.length > 0) {
							// second word as version
							version = elements[0];
							//LOG.warn("datasource version is " + version);
							// check how many elements, split by "."
							// only need first three elements as version
							String[] delements = version.split("\\.");
							int len = delements.length;
							//LOG.warn("datasource delements length is " + len);
							if (delements.length >= 3) {
								version = delements[0] + dot + delements[1] + dot + delements[2];
							}
							
							// if ending with ",", remove it
							len = version.length();
							if (version.endsWith(",")) {
								version = version.substring(0, len-1);
								//LOG.warn("datasource remove \",\" version is " + version);
							}
							//LOG.warn("datasource final version is " + version);
							return version;
						}
					}
				}
				catch (SQLException e) {
					//String msg = "Could not access meta data for: " + this;
					//LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					rs = Check.disposal(rs);
					stmt = Check.disposal(stmt);
				}							
			}			
			break;
			default:
				break;
		}

		// could not find the version
		return DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
	}
	/**
	 * @param con
	 * @return The patch level for the database
	 */
	String findPatch(Connection con) {
		DatasourceEnum type = DatasourceEnum.getByDatasourceTypeId( datasource.getDatasourceTypeId() );
		switch (type) {
			case DB2:
			case DB2_ZOS:	
			case ORACLE:
				//TODO: DBT - need to call CAS when project dependencies are fixed.
				break;

			case INFORMIX:
				// Informix JDBC driver metadata mis-reports 11.0 as 10.1
				Statement stmt = null;
				ResultSet rs = null;
				try {
					stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					rs = stmt.executeQuery(VER_IFX_FULL);
					rs.next();
					String patch = rs.getString(1);
					if ( !Check.isEmpty(patch) ) {
						int pos = patch.lastIndexOf(".");
						if (pos >= 0) {
							patch = patch.substring(pos + 1).trim();
							return patch;
						}
					}

				} catch (SQLException e) {
					String msg = "Could not access patch data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);

				} finally {
					rs = Check.disposal(rs);
					stmt = Check.disposal(stmt);
				}
				break;

			case MSSQL:
				try {
					String patch = con.getMetaData().getDatabaseProductVersion();
					String ver = findVersion(con);
					String l[] = patch.substring(patch.indexOf(ver)).split("\\s+");
					patch = l[0];

					if ( !Check.isEmpty(patch) ) {
						patch = patch.substring(patch.indexOf(".")+1);
						patch = patch.substring(patch.indexOf(".")+1);
						int pos = patch.indexOf(".");
						if (pos >= 0) { 
							patch = patch.substring(0,pos );
						}    
						return patch;
					}    

				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;

			case MYSQL:
				try {
					String patch = con.getMetaData().getDatabaseProductVersion();
					if ( !Check.isEmpty(patch) ) {
						String[] elements = patch.split(Say.DASH);
						if (elements.length > 0) {
							patch = elements[0];
							int pos = patch.lastIndexOf(".");
							if (pos >= 0) {
								patch = patch.substring(pos + 1);
								return patch;
							}
						}
					}

				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;
			case ASTER:
				try {
					String patch = con.getMetaData().getDatabaseProductVersion();
					if ( !Check.isEmpty(patch) ) {
						String[] elements = patch.split("\\.");
						if (elements.length > 0) {
							patch = elements[elements.length -1];
							return patch;
						}
					}
				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;
			case POSTGRESQL:
			case GREENPLUM:
				try {
					String patch = con.getMetaData().getDatabaseProductVersion();
					if ( !Check.isEmpty(patch) ) {
							int pos = patch.lastIndexOf(".");
							if (pos >= 0) {
								patch = patch.substring(pos + 1);
								return patch;
							}
					}

				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;	

			case SYBASE:
				final String ebf = "EBF";
				try {
					String prod = con.getMetaData().getDatabaseProductVersion();
					for ( String element : prod.split(Say.SLASH) ) {
						if ( element.toUpperCase().startsWith(ebf) ) {
							return element;
						}
					}
				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;

			case SYBASE_IQ:
				try {
					String prod = con.getMetaData().getDatabaseProductVersion();
					String l[] = prod.split(Say.SLASH);
					if ( l.length >=5 )
						return l[4];
					//for ( String element : prod.split(Say.SLASH) ) {
					//	if ( element.toUpperCase().startsWith(esd) ) {
					//		return element;
					//	}
					//}
				} catch (SQLException e) {
					String msg = "Could not access Meta Data for: " + this;
					LOG.warn(msg, e);
					AdHocLogger.logException(e);
				}
				break;

			case TERADATA:
				Statement trdStmt = null;
				ResultSet trdRs = null;
				String verSQL = VER_TRD_PDE;
				try {
					if (datasource.getVersionType().toUpperCase().equals("TDBMS"))
						verSQL = VER_TRD_TDBMS; // PDE Used as default
						
						trdStmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						trdRs = trdStmt.executeQuery(verSQL);
						trdRs.next();
						String patch = trdRs.getString(1);
						if ( !Check.isEmpty(patch) ) {
							int pos = patch.lastIndexOf(".");
							if (pos >= 0) {
								patch = patch.substring(pos + 1).trim();
								return patch;
							}
						}
				}
				catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					trdRs = Check.disposal(trdRs);
					trdStmt = Check.disposal(trdStmt);
				}

			case NETEZZA:
				Statement netstmt = null;
				ResultSet netrs = null;
				String versql = VER_NTZA;
				try {					
					netstmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					netrs = netstmt.executeQuery(versql);
					netrs.next();
					String patch = netrs.getString(1);
					//LOG.warn("datasource patch is " + patch);
					// Release 4.5 (P-3) [Build 7835]
					
					// Feb, 2014
					// Netezza has recently make change to their version and patching mechanism.
					// Old patch from the database server look like this:
                    //
                    // Release 4.6.8 (P-1)
                    // Release 6.0.6 
                    // 
                    // New method look like this.
                    // 
                    // Release 7.0.4.3-P1
                    // Release 7.0.4.3
                    // Release 7.0.4
                    //
                    // The first three element is the version.  
                    // The four element is the patch
                    // the fifth element is the sub-patch.
					// The code here needs to support old and new ways.
					//
					// some system has version like following:
					// Release 7.0, Dev 1 [Build 24913] - patch "Dev 1"
					// Release 6.0.6 [Build 22347]  - patch = ""
					// Release 4.6.8 (P-1) [Build 13301] - patch "P-1" 
					// 
					
					// old method
					// Release 4.6.8 (P-1)
					// Release 6.0.6 
					// Release 7.0, Dev 1 [Build 24913]
					// Release 6.0.6 [Build 22347]
					// Release 4.6.8 (P-1) [Build 13301]

					// New method look like this.

					// Release 7.0.4.3-P1
					// Release 7.0.4.3
					// Release 7.0.4
					
					// testing
					//patch = "Release 4.6.8 (P-1)";
					//patch = "Release 6.0.6";
					//patch = "Release 7.0, Dev 1 [Build 24913]";
					//patch = "Release 6.0.6 [Build 22347";
					//patch = "Release 7.0.4.3-P1";
					//patch = "7.0.4.3";
					//patch = "7.0.4";
					
					String dot = ".";
					String build_str = "[Build ";
					String release_str = "Release";
					if ( !Check.isEmpty(patch) ) {
						int bidx = patch.indexOf(build_str);
						if (bidx > 0) {
							// remove anything after [Build
							patch = patch.substring(0, bidx);
						}
						//LOG.warn("datasource patch 2 is " + patch);
						
						// remove starting word "Release"
						if (patch.startsWith(release_str, 0)) {
							patch = patch.substring(release_str.length() + 1 );
							//LOG.warn("datasource remove Release patch is " + patch);
						}
						patch.trim();
					}
					
					int len = 0;
					if ( !Check.isEmpty(patch) ) {
						String[] elements = patch.split("\\s+");     // (" ");
						len = elements.length;
						//LOG.warn("datasource elements length is " + len);
						if ( len == 1) {
							// lenght = 1
							// check how many elements, split by "."
							// only need first three elements as version
							String[] delements = patch.split("\\.");
							
							int dlen = delements.length;
							//LOG.warn("datasource delements length is " + dlen);
							if (dlen > 3) {
								// fouth element is patch
								patch = delements[3];
								//LOG.warn("datasource patch 1 is " + patch);
							}
							else {
								// no fourth element, no patch
								patch = "";
								//LOG.warn("datasource patch 2 is empty " + patch);
							}
						}
						else {
							// more than 1
							if (len == 2) {
								// first one is version
								// second one is patch
							    patch = elements[1];
							    int idxbegin = patch.indexOf("(");
							    int idxend = patch.indexOf(")");
							    if ((idxbegin >=0) && (idxend >=0)) {
								    patch = patch.substring(idxbegin+1, idxend);
							    }
							    //LOG.warn("datasource patch 3 is " + patch);
							}
							else {
								patch = "";
								for (int i = 1; i< len; i++) {
									patch = patch + elements[i] + " ";
								}
								patch.trim();
								//LOG.warn("datasource patch 4 is " + patch);
							}
						}
					}
					else {
						// empty
						//LOG.warn("datasource patch 5 is empty");
						patch = "";
					}
					//LOG.warn("datasource final patch is " + patch);
					return patch;
				}
				catch (SQLException e) {
					//String msg = "Could not access meta data for: " + this;
					//LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					rs = Check.disposal(netrs);
					stmt = Check.disposal(netstmt);
				}				
							
			default:
				break;
		}

		// could not find the patch
		return DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
	}
	/**
	 * @param con
	 * @return The full version info for the database.
	 */
	String findFullInfo(Connection con) {
		String result = DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
		if ( datasource.isText() ) {
			return result;
		}

		// TODO dbt - pull full version info from CAS
		// in the meantime, just append some info found in metadata
		if ( datasource.isDB2() ) {
			try {
				DatabaseMetaData meta = con.getMetaData();
				result = meta.getDatabaseProductName() + Say.SP + meta.getDatabaseProductVersion();

			} catch (SQLException e) {
				result = Informer.thrownMessage(e);
				String msg = "Could not access Meta Data for: " + this;
				LOG.warn(msg + Say.NL + result);
				AdHocLogger.logException(e);
			}

		// MSS requires a query
		} else if ( datasource.isMsSqlServer() ) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(VER_MSS_FULL);
				if ( rs.next() ) {
					result = rs.getString(1);
				}

			} catch (SQLException e) {
				result = Informer.thrownMessage(e);
				String msg = "Could not access @@version for: " + this;
				LOG.warn(msg + Say.NL + result);
				AdHocLogger.logException(e);

			} finally {
				rs = Check.disposal(rs);
				stmt = Check.disposal(stmt);
			}

		} else if ( datasource.isInformix() ) {
			// Ifx 11 metadata mis-reports version as 10.01
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(VER_IFX_FULL);
				if ( rs.next() ) {
					result = rs.getString(1);
				}

			} catch (SQLException e) {
				String msg = "Could not access version information for: " + this;
				LOG.warn(msg, e);
				AdHocLogger.logException(e);

			} finally {
				rs = Check.disposal(rs);
				stmt = Check.disposal(stmt);
			}

		// all others have the info in the Database Product Version
		} else {
			try {
				DatabaseMetaData meta = con.getMetaData();
				result = meta.getDatabaseProductVersion();

			} catch (SQLException e) {
				result = Informer.thrownMessage(e);
				String msg = "Could not access Meta Data for: " + this;
				//LOG.warn(msg + Say.NL + result);
				AdHocLogger.logException(e);
			}
		}
		return result;
	}


	/**
	 * @return The version of this datasource
	 * @throws DataSourceConnectException
	 */
	String getVersionLevel() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return getVersionLevel(con);

		} finally {
			con = Check.disposal(con);
		}
	}
	
	/**
	 * @param con A connection to use.
	 * @return The version of this datasource
	 */	
	String getVersionLevel(Connection con) {
		return getVersionLevel(con,"N/A");
	}
	
	String getVersionLevel(Connection con, String verType) {
		datasource.setDatasourceVersionHistorys(null);//collDatasourceVersionHistorys = null;
		datasource.setVersionType(verType);
		DatasourceVersionHistory history = this.findLatestVersionHistory(con);
		return history.getVersionLevel();
	}
	/**
	 * @return The major version of this datasource
	 * @throws DataSourceConnectException
	 */
	int getMinorVersion() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return this.getMinorVersion(con);
		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * @param con A connection
	 * @return The major version of the connection
	 */
	int getMinorVersion(Connection con) {
		int minor = -1;
		String version = this.getVersionLevel(con);
		if ( !Check.isEmpty(version) && !DatasourceVersionHistoryPeer.isUnknown(version) ) {
			if ( DatasourceVersionHistoryPeer.isUnknown(version) ) {
				String msg = this + " version is: '" + version + "' returning minor version: " + minor + ".";
				LOG.info(msg);

			} else {
				String elements[] = version.split("\\.");
				if ( elements.length > 1 ) {
					try {
						minor = new Integer(elements[1]);
					} catch (NumberFormatException e) {
						String msg = "Could not interpret '" + elements[1] + "' from '" + version + "' as a number.";
						LOG.warn(msg, e);
					}
				}
			}
		}
		return minor;
	}
	/**
	 * @param con A connection
	 * @return The major version of the connection
	 */
	int getMajorVersion(Connection con) {
		int major = -1;
		String version = this.getVersionLevel(con);

		if ( !Check.isEmpty(version) ) {
			if ( DatasourceVersionHistoryPeer.isUnknown(version) ) {
				String msg = this + " version is: '" + version + "' returning major version: " + major + ".";
				LOG.info(msg);

			} else {
				String elements[] = version.split("\\.");
				if ( elements.length > 0 ) {
					try {
						major = new Integer(elements[0]);
					} catch (NumberFormatException e) {
						String msg = "Could not interpret '" + elements[0] + "' from '" + version + "' as a number.";
						LOG.warn(msg, e);
					}
				}
			}
		}
		return major;
	}

	/**
	 * @return The major version of this datasource
	 * @throws DataSourceConnectException
	 */
	int getMajorVersion() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return this.getMajorVersion(con);
		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * @return The patch level information for the database.
	 *  'Unknown' if the patch level is dependant on CAS or no connection could be established.
	 * @throws DataSourceConnectException
	 */
	String getPatchLevel() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return this.getPatchLevel(con);

		} finally {
			con = Check.disposal(con);
		}
	}

	/**
	 * @param con A connection to use.
	 * @return The patch level information for the database.
	 *  'Unknown' if the patch level is dependant on CAS or no connection could be established.
	 */
	String getPatchLevel(Connection con) {
		DatasourceVersionHistory history = this.findLatestVersionHistory(con);
		return history.getPatchLevel();
	}

	/**
	 * Saves an entry to the list if the version or patch is new
	 * Updates the entry with the confirm date and datasource and type name change
	 * if the patch level can be found.
	 * Will only query the datasource for new information if there has not been an update in the
	 * past time period.
	 * @return The most recent datasource version history information.
	 * @throws DataSourceConnectException
	 */
	DatasourceVersionHistory findLatestVersionHistory() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return this.findLatestVersionHistory(con);

		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * @return A new version history
	 * @throws DataSourceConnectException
	 */
	DatasourceVersionHistory findVersionHistory() throws DataSourceConnectException {
		Connection con = null;
		try {
			con = getConnection();
			return this.findVersionHistory(con);

		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * @return Whether this datasource supports catalogs - usually in the form of a database name;
	 * @throws SQLException
	 * @throws DataSourceConnectException 
	 */
	boolean isCatalogSupported() throws SQLException, DataSourceConnectException {
		if (this.catalogSupported != null)
			return this.catalogSupported ;
		Connection con = null;
		try {
			con = this.getConnection();
			return this.isCatalogSupported(con);
			
		} finally {
			con = Check.disposal(con);
		}
	}
	/**
	 * @param con A connection to use.
	 * @return Whether this datasource supports catalogs - usually in the form of a database name;
	 * @throws SQLException
	 */
	boolean isCatalogSupported(Connection con) throws SQLException {
		DatabaseMetaData meta = con.getMetaData();
		this.catalogSupported = meta.supportsCatalogsInTableDefinitions();
		return this.catalogSupported;
	}
	
	/* NOT UESED
	ResultSet getCachedResultSet(String getRadsColMappingSql) throws DataSourceConnectException, SQLException {
		Connection con = null;
		try{
			con = this.getConnection();
			return Utils.executeSqlForResultSet(getRadsColMappingSql,con);
		}finally{
			Check.disposal(con);
		}
	}
	*/
	
	List<String> getCatalogs(Connection con)
	{ 
		List<String> dbs = new ArrayList<String>();
		if(con == null)
			return dbs;
		ResultSet rs = null;
	
		try {
			DatabaseMetaData meta = con.getMetaData();
			if(DatasourceEnum.get(datasource ).isCatalog())
			{
				// get a list of the "databases"
				rs = meta.getCatalogs();
				while( rs.next() ) {
					dbs.add( rs.getString("TABLE_CAT") );
				}
				
			}else{

				rs = meta.getSchemas();
				while( rs.next() ) {
					dbs.add( rs.getString("TABLE_SCHEM") );
				}

			}
	
		} catch (Exception e) {
			String msg = "Could not get catalogs for '" + this + "'";
			LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
			
		} finally {
			rs = Check.disposal(rs);
		}
		return dbs;
	}
	int findDBMajorVersion() throws DataSourceConnectException {		
		Connection con = null;
		try {
			con = getConnection();
			return findDBMajorVersion(con);

		} finally {
			con = Check.disposal(con);
		}
	}
	int findDBMajorVersion(Connection con) {		
		int major = -1;
		if(con == null)
			return major;
		DatasourceEnum type = DatasourceEnum.getByDatasourceTypeId( datasource.getDatasourceTypeId() );
		switch (type) {							
			case MSSQL:
			case MYSQL:
			case POSTGRESQL:	
			case GREENPLUM:	
			case DB2:
			case DB2_ZOS:	
			case ORACLE:
			case SYBASE:	
			case SYBASE_IQ:
	      case DB2_400:
				try {
					DatabaseMetaData meta = con.getMetaData();
					major = meta.getDatabaseMajorVersion();

				} catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				break;
			case INFORMIX:
				// Informix JDBC driver metadata mis-reports 11.0 as 10.1
				Statement ifxStmt = null;
				ResultSet ifxRs = null;
				try {
					ifxStmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ifxRs = ifxStmt.executeQuery(VER_IFX_MAJ);
					ifxRs.next();
					String maj = ifxRs.getString(1);
					if ( !Check.isEmpty(maj) ) { maj = maj.trim(); }
					else maj = "0";
					ifxRs = Check.disposal(ifxRs);
					major = Integer.valueOf(maj);

				} catch (SQLException e) {
					String msg = "Could not access version data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);

				} finally {
					ifxRs = Check.disposal(ifxRs);
					ifxStmt = Check.disposal(ifxStmt);
				}
				break;

			case TERADATA:
				Statement trdStmt = null;
				ResultSet trdRs = null;
				String verSQL = VER_TRD_PDE;
				try {
					if (datasource.getVersionType().toUpperCase().equals("TDBMS"))
						verSQL = VER_TRD_TDBMS; // PDE Used as default
						
						trdStmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						trdRs = trdStmt.executeQuery(verSQL);
						trdRs.next();
						String version = trdRs.getString(1);
						if ( !Check.isEmpty(version) ) {
							int pos = version.indexOf(".");
							if (pos >= 0) {
								version = version.substring(0,pos).trim();
								major = Integer.valueOf(version);
							}
						}
				}
				catch (SQLException e) {
					String msg = "Could not access meta data for: " + this;
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					trdRs = Check.disposal(trdRs);
					trdStmt = Check.disposal(trdStmt);
				}
				break;
			case NETEZZA:
			{
				Statement stmt = null;
				ResultSet rs = null;
				String versql = VER_NTZA;
				try {					
					stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					rs = stmt.executeQuery(versql);
					rs.next();
					String version = rs.getString(1);
					// Release 4.5 (P-3) [Build 7835]
					if ( !Check.isEmpty(version) ) {
						String[] elements = version.split(" ");
						if (elements.length > 0) {
							version = elements[1];
							int idx = version.indexOf(".");
							if(idx > 0)
								major = Integer.valueOf(version.substring(0, idx));
						}
					}
				}
				catch (SQLException e) {
					String msg = "Could not access meta data for: " + datasource.getName();
					LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
					AdHocLogger.logException(e);
				}
				finally {
					rs = Check.disposal(rs);
					stmt = Check.disposal(stmt);
				}							
			}			
			break;
			default:
				break;
		}

		// could not find the version
		return major;
	}
	/**
	 * Extracts the version information out of a Product version string that has
	 * extraneous information in it.
	 * Scrolls through all the words looking for a a word with just numbers and dots,
	 * like: <code>8.1.7.0.0</code>
	 * <dl>
	 * <dt>Typical Oracle Output:</dt>
	 * <dd>Oracle8i Enterprise Edition Release 8.1.7.0.0 - Production JServer Release 8.1.7.0.0 - Production</dd>
	 * <dt>Typical Sybase Output:</dt>
	 * <dd>Adaptive Server Enterprise/12.5.2/EBF 11795/P/HP9000-879/HP-UX 11.0/ase1252/1831/64-bit/FBO/Fri Apr  9 12:36:59 2004</dd>
	 * <dt>Typical MySql output:</dt>
	 * <dd>5.1.19-beta-community-nt-debug</dd>
	 * </dl>
	 * @param version The product version string.
	 * @param delim
	 * @return Only version number portion of the string. If the pattern is not found, the input.
	 */
	String parseProductVersion(String version, String delim) {
		String word;
		StringTokenizer tok = new StringTokenizer(version, delim);
		while ( tok.hasMoreTokens() ) {
			word = tok.nextToken();
			boolean match = Regexer.matchRegex(word, datasource.PROD_VER_PATTERN);
			if (match) {
				return word;
			}
		}
		// no hits, return the incoming version
		return version;
	}

}
