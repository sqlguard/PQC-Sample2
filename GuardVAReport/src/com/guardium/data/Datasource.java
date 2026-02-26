/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

//import org.apache.torque.TorqueException;
//import org.apache.torque.util.Criteria;

import com.guardium.data.DataSourceConnectException;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceEnum;
import com.guardium.data.DatasourceHandler;
import com.guardium.data.DatasourceVersionHistory;
//import com.guardium.map.DatasourceVersionHistoryMap;
import com.guardium.data.JDBCDatasourceHandler;
//import com.guardium.map.DatasourceVersionHistoryPeer;
//import com.guardium.datamodel.dbSource.MongoDatasourceHandler;
//import com.guardium.dbSource.InvalidDBTypeException;

//import com.guardium.data.DatasourceEnum;
import com.guardium.map.CveReferenceMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.DatasourceVersionHistoryMap;
import com.guardium.map.DbDriverMap;
import com.guardium.runtest.CVETest;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.utils.Regexer;
//import com.guardium.utils.FileUtils;
import com.guardium.utils.Stringer;
import com.guardium.utils.i18n.Say;
//import com.guardium.utils.i18n.SayAppRes;
//import com.mongodb.MongoClient;

public class Datasource {

	//private static final transient Logger LOG = Logger.getLogger(Datasource.class);

	public static final String CSV_SEPERATOR = "_CSV_Separator";
	public static final String CSV_FILE_EXTENTION = "csvfileExtension";
	public static final String CSV_HEADER = "_CSV_Header";
	public static final String CSV_SEPERATOR_DEFAULT_VALUE = ",";
	public static final String CSV_FILE_EXTENTION_DEFAULT_VALUE = "CSV";
	public static final String CSV_HEADER_DEFAULT_VALUE = "false";
	public static final String PROP_DELIM = ";";
	// public static final String IMPORT_DIR = FileUtils.getDumpDir();

	public static final String PREFIX_HTTP = "http://";
	public static final String PREFIX_HTTPS = "https://";
	public static final String PREFIX_FTP = "ftp://";
	public static final String PREFIX_SMB = "smb://";

	/** constant pattern to check for content of just numbers and dots */
	protected static final Pattern PROD_VER_PATTERN = Regexer.compilePattern("\\d\\.\\d");

	public String versionType = "N/A";

	/** Constant controls how many minutes between detecting new versions of the database */
	public static final int VERSION_DETECTION_PERIOD = 1;
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
	
	DatasourceVersionHistoryMap DatasourceVersionHistoryPeer = DatasourceVersionHistoryMap.getDatasourceVersionHistoryMapObject();
	
	private static Locale guardLocale = getGuardLocale();
	private static ResourceBundle messages = !"ww".equalsIgnoreCase(guardLocale.getLanguage()) ? ResourceBundle.getBundle("com.guardium.data.DataSourceResources") : ResourceBundle.getBundle("com.guardium.data.DataSourceResources", guardLocale);
	
	private static Locale getGuardLocale() {
		
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
	
	public Datasource(int sid, int type_id, String dname, String des,
			String dhost, int dport, String sname, String uname, String psw,
			boolean password_stored_flag, String dbname, Date lconnect,
			Date ts, int aid, boolean shared_flag, String constr,
			String os_uname, String db_dir, String curl, int sev, int dbid,
			String cmode, boolean use_ssl_flag, boolean import_flag) {

		datasource_id = sid;
		datasource_type_id = type_id;
		name = dname;
		description = des;
		host = dhost;
		port = dport;
		service_name = sname;
		user_name = uname;
		password = psw;
		password_stored = password_stored_flag;
		db_name = dbname;
		last_connect = lconnect;
		timestamp = ts;
		application_id = aid;
		shared = shared_flag;
		con_property = constr;
		os_username = os_uname;
		db_home_dir = db_dir;
		url = curl;
		severity = sev;
		db_driver_id = dbid;
		compatibility_mode = cmode;
		use_ssl = use_ssl_flag;
		import_server_ssl_certificate = import_flag;
	}

	/**
	 * The value for the datasource_id field
	 */
	private int datasource_id;

	/**
	 * The value for the datasource_type_id field
	 */
	private int datasource_type_id;

	/**
	 * The value for the name field
	 */
	private String name;

	/**
	 * The value for the description field
	 */
	private String description;

	/**
	 * The value for the host field
	 */
	private String host;

	/**
	 * The value for the host ip field
	 */
	private String hostIp;

	/**
	 * The value for the port field
	 */
	private int port;

	/**
	 * The value for the default port field
	 */
	private int defaultPort;
	
	public void setDefaultPort(int default_port) {
		this.defaultPort = port;
	}

	/**
	 * The value for the service_name field
	 */
	private String service_name = "";

	/**
	 * The value for the user_name field
	 */
	private String user_name;

	/**
	 * The value for the password field
	 */
	// private byte[] password;

	private String password;
	/**
	 * The value for the password_stored field
	 */
	private boolean password_stored = false;

	/**
	 * The value for the db_name field
	 */
	private String db_name;

	/**
	 * The value for the last_connect field
	 */
	private Date last_connect;

	/**
	 * The value for the timestamp field
	 */
	private Date timestamp;

	/**
	 * The value for the application_id field
	 */
	private int application_id = 0;

	/**
	 * The value for the shared field
	 */
	private boolean shared = false;

	/**
	 * The value for the con_property field
	 */
	private String con_property;

	/**
	 * The value for the os_username field
	 */
	private String os_username;

	/**
	 * The value for the db_home_dir field
	 */
	private String db_home_dir;

	/**
	 * The value for the custom_url field
	 */
	private String url;

	/**
	 * The value for the severity field
	 */
	private int severity = 2;

	/**
	 * The value for the db_driver_id field
	 */
	private int db_driver_id;

	/**
	 * The value for the compatibility_mode field
	 */
	private String compatibility_mode = "";

	/**
	 * The value for the use_ssl field
	 */
	private boolean use_ssl = false;

	/**
	 * The value for the import_server_ssl_certificate field
	 */
	private boolean import_server_ssl_certificate = false;

	private Driver driver;

	public int connectTimeoutSeconds = 60;

	private DbDriver db_driver;

	private String locale;

	private String screenName = "";

	public int getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

	/**
	 * getter and setter
	 * 
	 */
	public int getDatasourceId() {
		return datasource_id;
	}

	public void setDatasourceId(int datasource_id) {
		this.datasource_id = datasource_id;
	}

	public int getDatasourceTypeId() {
		return datasource_type_id;
	}

	public void setDatasourceTypeId(int datasource_type_id) {
		this.datasource_type_id = datasource_type_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHostIp() {
		return Stringer.ip( this.getHost() );
	}


	public void setHostIp(String ip) {
		this.hostIp = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServiceName() {
		return service_name;
	}

	public void setServiceName(String service_name) {
		this.service_name = service_name;
	}

	public String getUserName() {
		return user_name;
	}

	public void setUserName(String user_name) {
		this.user_name = user_name;
	}

	public String getScreenUserName () {
		return user_name;
	}
	
	public String getScreenName () {
		return screenName;
	}
	
	public void setScreenName (String str) {
		screenName = str;
	}

	public String getScreenPassword() {
		return password;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDecryptedPassword() {
		// not encrypt
		return password;
	}
	
	public boolean isPasswordStored() {
		return password_stored;
	}

	public boolean getPasswordStored() {
		return password_stored;
	}
	
	public void setPasswordStored(boolean password_stored) {
		this.password_stored = password_stored;
	}

	public String getDbName() {
		return db_name;
	}

	public void setDbName(String db_name) {
		this.db_name = db_name;
	}

	public Date getLastConnect() {
		return last_connect;
	}

	public void setLastConnect(Date last_connect) {
		this.last_connect = last_connect;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getApplicationId() {
		return application_id;
	}

	public void setApplicationId(int application_id) {
		this.application_id = application_id;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public String getConProperty() {
		return con_property;
	}

	public void setConProperty(String con_property) {
		this.con_property = con_property;
	}

	public String getOsUsername() {
		return os_username;
	}

	public void setOsUsername(String os_username) {
		this.os_username = os_username;
	}

	public String getDbHomeDir() {
		return db_home_dir;
	}

	public void setDbHomeDir(String db_home_dir) {
		this.db_home_dir = db_home_dir;
	}

	public String getCustomUrl() {
		return url;
	}
	
	/**
	 * Just-in-time, checks whether a dbName exists and builds the appropriate
	 * URL.
	 * 
	 * @return A URL for the datasource.
	 * @author dtoland
	 */
	public String getUrl() {
		if (!Check.isEmpty(this.getCustomUrl())) {
			return this.getCustomUrl();
		}
		if (this.url == null || this.url.isEmpty()) {
			String catalog = this.getCatalog();
			// find the proper template
			// find the proper template
			// if database is not specified for sybase, mssql, or informix use
			// the baseUrlTemplate
			String template;
			DatasourceType type = null;

			type = getDatasourceType();

			if (!this.isDB2()
					&& !this.isOracle()
					&& (Check.isEmpty(catalog) || (this.isInformix() && !this.usingInformixDbName))) {
				template = getDbDriver().getBaseUrlTemplate();
			} else {
				template = getDbDriver().getUrlTemplate();
			}

			// resolve the url
			// replace symbols representing various datasource characteristics
			// to derive the JDBC URL
			this.url = this.buildUrl(template, type);
		}

		return this.url;
	}
	
	
	
	
	public void setUrl(String custom_url) {
		this.url = custom_url;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	// Driver driver = this.datasource.getDriver();

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver dv) {
		driver = dv;
	}

	/*
	 * public void removeDriver() { String typeName = this.getTypeName(); if (
	 * drivers.containsKey(typeName) ) { drivers.remove(typeName); // Need to
	 * remove the drivers when Future throws a timeout (in ConcurrentConnecter
	 * method Connect) See Bug 35134 } }
	 */

	public int getDbDriverId() {
		return db_driver_id;
	}

	public void setDbDriverId(int db_driver_id) {
		this.db_driver_id = db_driver_id;
	}

	public DbDriver getDbDriver() {
		return db_driver;
	}

	public void setDbDriver(DbDriver dd) {
		db_driver = dd;
		return;
	}

	public String getCompatibility_mode() {
		return compatibility_mode;
	}

	public void setCompatibility_mode(String compatibility_mode) {
		this.compatibility_mode = compatibility_mode;
	}

	public boolean isUse_ssl() {
		return use_ssl;
	}

	public void setUse_ssl(boolean use_ssl) {
		this.use_ssl = use_ssl;
	}

	public boolean isImport_server_ssl_certificate() {
		return import_server_ssl_certificate;
	}

	public void setImport_server_ssl_certificate(
			boolean import_server_ssl_certificate) {
		this.import_server_ssl_certificate = import_server_ssl_certificate;
	}

	public boolean isDB2() {
		return DatasourceEnum.DB2.equals(this);
	}

	public boolean isDB2_400() {
		return DatasourceEnum.DB2_400.equals(this);
	}

	public boolean isInformix() {
		return DatasourceEnum.INFORMIX.equals(this);
	}

	public boolean isPostgreSQL() {
		return DatasourceEnum.POSTGRESQL.equals(this);
	}

	public boolean isGreenplum() {
		return DatasourceEnum.GREENPLUM.equals(this);
	}

	public boolean isAster() {
		return DatasourceEnum.ASTER.equals(this);
	}

	public boolean isMsSqlServer() {
		return DatasourceEnum.MSSQL.equals(this);
	}

	public boolean isMySql() {
		return DatasourceEnum.MYSQL.equals(this);
	}

	public boolean isOracle() {
		return DatasourceEnum.ORACLE.equals(this);
	}

	public boolean isSybase() {
		return DatasourceEnum.SYBASE.equals(this);
	}

	public boolean isSybaseIq() {
		return DatasourceEnum.SYBASE_IQ.equals(this);
	}

	public boolean isTeradata() {
		return DatasourceEnum.TERADATA.equals(this);
	}

	public boolean isNetezza() {
		return DatasourceEnum.NETEZZA.equals(this);
	}

	public boolean isText() {
		return DatasourceEnum.TEXT.equals(this);
	}

	public boolean isTextFtp() {
		return DatasourceEnum.TEXT_FTP.equals(this);
	}

	public boolean isTextHttp() {
		return DatasourceEnum.TEXT_HTTP.equals(this);
	}

	public boolean isTextHttps() {
		return DatasourceEnum.TEXT_HTTPS.equals(this);
	}

	public boolean isTextSmb() {
		return DatasourceEnum.TEXT_SMB.equals(this);
	}

	public boolean isDB2_ZOS() {
		return DatasourceEnum.DB2_ZOS.equals(this);
	}

	public boolean isN_A() {
		return DatasourceEnum.N_A.equals(this);
	}

	private Object lastDatasourceVersionHistoryCriteria;

	/**
	 * @return Whether the Datasource Type is any of the Text Types.
	 */
	public boolean isCsv() {
		DatasourceEnum type = DatasourceEnum.get(this);
		boolean result = type != null && type.isText();

		return result;
	}

	private DatasourceType aDatasourceType;

	/**
	 * Declares an association between this object and a DatasourceType object
	 * 
	 * @param v
	 *            DatasourceType
	 * @throws TorqueException
	 */
	public void setDatasourceType(DatasourceType v) {
		/*
		if (v == null) {
			setDatasourceTypeId(0);
		} else {
			setDatasourceTypeId(v.getDatasourceTypeId());
		}
		*/
		aDatasourceType = v;
	}

	/**
	 * D Get the associated DatasourceType object
	 * 
	 * @return the associated DatasourceType object
	 * @throws TorqueException
	 */
	public DatasourceType getDatasourceType() {
		//if (aDatasourceType == null && (this.datasource_type_id > 0)) {

			//DatasourceTypeMap vmap = new DatasourceTypeMap();
			//aDatasourceType = vmap.getDatasourceType(this.datasource_type_id);

			/*
			 * The following can be used instead of the line above to guarantee
			 * the related object contains a reference to this object, but this
			 * level of coupling may be undesirable in many circumstances. As it
			 * can lead to a db query with many results that may never be used.
			 * DatasourceType obj =
			 * DatasourceTypePeer.retrieveByPK(this.datasource_type_id);
			 * obj.addDatasources(this);
			 */
		//}
		return aDatasourceType;
	}

	/**
	 * Provides convenient way to set a relationship based on a ObjectKey. e.g.
	 * <code>bar.setFooKey(foo.getPrimaryKey())</code>
	 * 
	 */
	/*
	 * public void setDatasourceTypeKey(int key) { setDatasourceTypeId((key); }
	 */

	public String getTypeName() {
		/*
		String name = null;

		DatasourceType type = this.getDatasourceType();
		name = type.getName();

		return name;
		*/
		
		DatasourceEnum type = DatasourceEnum.get(this);
		String name = type.getName();
		return name;
		
	}

 	private String hashKey = null;
 	
 	public String getHashKey() {
 		return hashKey;
 	}
 
 	public void setHashKey(String hashKey) {
 		this.hashKey = hashKey;
 	}
	
 	private int majorVersion = 0;

	private int minorVersion = 0;
	
	public int getMajorVersion() {
		if (majorVersion > 0) {
			return majorVersion;
		}
		else {
			int tmp = 0;
			if (!this.versionLevel.isEmpty()) {
				String [] strs = this.versionLevel.split("\\.");
				tmp = Integer.parseInt(strs[0]);
			}
			return tmp;
		}
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public int getMinorVersion() {
		if (minorVersion > 0) {
			return minorVersion;
		}
		else {
			int tmp = 0;
			if (!this.versionLevel.isEmpty()) {
				String [] strs = this.versionLevel.split("\\.");
				if (strs.length > 1)
					tmp = Integer.parseInt(strs[1]);
			}
			return tmp;
		}
		
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public String getInstanceName() {
		StringBuilder buf = new StringBuilder(this.getTypeName() + Say.SP);

		if (!Check.isEmpty(this.getHost())) {
			String ip;
			if (this.isCsv()) {
				ip = this.getHost();
			} else {
				ip = Stringer.ip(this.getHost());
			}
			buf.append(ip);

			if (!this.getHost().equals(ip)) {
				buf.append("/" + this.getHost());
			}
			buf.append(Say.CLN);
		}

		if (this.getPort() > 0) {
			buf.append(this.getPort() + Say.SP);
		}

		if (!Check.isEmpty(this.getServiceName())) {
			buf.append(this.getServiceName() + Say.SP);
		}

		if (!Check.isEmpty(this.getDbName())) {
			buf.append(this.getDbName() + Say.SP);
		}

		return buf.toString();
	}

	public boolean isPortRequired() {
		DatasourceEnum type = DatasourceEnum.get(this);
		boolean result = type != null && type.isPortRequired();

		return result;
	}

	/**
	 * @return The base url
	 * @throws TorqueException
	 */
	public String getBaseUrl() {
		// getBaseUrlTemplate(),
		return this.buildUrl(getDbDriver().getBaseUrlTemplate(), 
				this.getDatasourceType());
	}

	private String buildUrl(String urlTemplate, DatasourceType dataSrcType) {
		// make a copy of the template so that argument is not modified
		String url = urlTemplate;
		// pre no sql type - if(urlTemplate != null && dataSrcType != null) {
		if (!Check.isEmpty(urlTemplate) && dataSrcType != null) {

			// check for CSV host type prefixes
			String host = this.getHost();

			// remove prefixes, included in the template
			// HTTP
			if (this.isTextHttp() && !Check.isEmpty(host)
					&& host.toLowerCase().startsWith(PREFIX_HTTP)) {
				host = host.substring(PREFIX_HTTP.length());

				// HTTPS
			} else if (this.isTextHttps() && !Check.isEmpty(host)
					&& host.toLowerCase().startsWith(PREFIX_HTTPS)) {
				host = host.substring(PREFIX_HTTPS.length());

				// FTP
			} else if (this.isTextFtp() && !Check.isEmpty(host)
					&& host.toLowerCase().startsWith(PREFIX_FTP)) {
				host = host.substring(PREFIX_FTP.length());

				// SMB
			} else if (this.isTextSmb() && !Check.isEmpty(host)
					&& host.toLowerCase().startsWith(PREFIX_SMB)) {
				host = host.substring(PREFIX_SMB.length());
			}

			// replace host, if it exists
			if (!Check.isEmpty(host)) {
				url = urlTemplate.replaceAll(DatasourceType.HOST_NAME_SYMBOL,
						host);
			}

			// if there is no port in the saved datasource, use the default
			// port, except for CSV
			int port = this.getPort();
			// Bug 29464 if (port <= 0 && !this.isCsv() ) {
			if (port <= 0 && this.isPortRequired()) {
				port = dataSrcType.getDefaultPort();
			}

			// replace the port symbol
			if (this.isCsv()) {
				if (port > 0) {
					url = url
							.replaceAll(DatasourceType.PORT_SYMBOL, ":" + port);
				} else {
					url = url.replaceAll(DatasourceType.PORT_SYMBOL, Say.EMPTY);
				}

			} else {
				url = url.replaceAll(DatasourceType.PORT_SYMBOL,
						Integer.toString(port));
			}

			// replace dbName, if it exists
			String dbName = this.getDbName();
			if (!Check.isEmpty(dbName) || this.isDB2_400()) {
				url = url.replaceAll(DatasourceType.DB_NAME_SYMBOL, dbName);
				url = url
						.replaceAll(DatasourceType.SERVICE_NAME_SYMBOL, dbName);
			}

			// replace serviceName, if it exists
			String serviceName = this.getServiceName();
			if (!Check.isEmpty(serviceName)) {
				String dollar = "\\\\\\$";
				String toReplace = serviceName.replaceAll("[$]", dollar);
				// For Oracle
				//url = url.replaceAll(DatasourceType.SERVICE_NAME_SYMBOL,
				//		toReplace);
				
				url = url.replaceAll(DatasourceType.SERVICE_NAME_SYMBOL,
						serviceName);

				/*
				if (this.isOracle()) {
					url = url.replaceAll(";SID=",
							"/");
				}
				*/
				
				// For Informix
				url = url.replaceAll(DatasourceType.DBSERVER_NAME_SYMBOL,
						toReplace);

			} else {
				url = url.replaceAll(DatasourceType.SERVICE_NAME_SYMBOL,
						Say.EMPTY);
			}

			// replace locale, if it exists
			String locale = this.getLocale();
			if (!Check.isEmpty(locale)) {
				url = url.replaceAll(DatasourceType.LOCALE_SYMBOL, locale);

			} else {

				// if no locale is set if URL has DB_LOCALE in it , remove it
				int localeIndex = url.indexOf(";DB_LOCALE=");
				if (localeIndex > -1) {
					// remove DB_LOCALE from URL
					url = url.substring(0, localeIndex);
				}
			}

			/*
			 * if ( dataSrcType.isText() ) { url =
			 * url.replaceAll("<IMPORT_DIR>", IMPORT_DIR); }
			 */
		}

		return url;
	}

	public boolean testConnection() throws DataSourceConnectException {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		Datasource datasource = this;
		
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
			//LOG.warn(msg);
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

			//LOG.warn(msg);
			throw new DataSourceConnectException(msg, e);
			
		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
			con = Check.disposal(con);
		}
	}

	public Connection getConnection() throws DataSourceConnectException {
		String user = this.getUserName();
		String pw = this.getPassword();

		return getConnection(user, pw);

	}

	public Connection getConnection(String userName, String password)
			throws DataSourceConnectException {
		String user;
		String pw;

		Datasource dt = this;
		user = userName;
		pw = password;
		String url_template = "";
    	String base_url_template = ""; 
		
		
		DbDriver dd = getDbDriver();
		
        if (dd == null) {
        	int dsid = this.getDbDriverId();
        	dd = DbDriverPeer.getDbDriverById(dsid);
        }
        if (dd != null) {
        	url_template = dd.getUrlTemplate();
        	base_url_template = dd.getBaseUrlTemplate();
        }
        else {
        	//exception
        	DataSourceConnectException dsce = new DataSourceConnectException("Can not find Datasource driver");
        	throw dsce;
        }
        
        
        DatasourceType dtype = dt.getDatasourceType();
        String str = dt.getName() + "_" + dtype.getName() + "(Security Assessment)";
        dt.setScreenName(str);
		//System.out.println("url template is " + url_template);
		//System.out.println("Base url template is " + base_url_template);

		Properties props = dt.getOtherConnectionProperties();

		// register the jdbc driver, if necessary
		// this.registerDriver();
		/*
		 * if ( datasource.getPasswordStored() ) { user =
		 * datasource.getUserName(); pw = datasource.getDecryptedPassword(); }
		 * else { user = userName; pw = password; }
		 */

		try {
			// Properties props = datasource.getOtherConnectionProperties();

			String url = this.getUrl();		// this.getBaseUrl();
			//System.out.println("before connection url is " + url);

			/*
			 * if (url.isEmpty()) { url = url_template; }
			 * 
			 * String hostname = this.getHost(); int port = this.getPort();
			 * String serviceName = this.getServiceName(); String dbName =
			 * this.getDbName();
			 * 
			 * url = url.replaceAll("<HOST>",hostname); url =
			 * url.replaceAll("<PORT>",String.valueOf(port)); url =
			 * url.replaceAll("<SERVICE_NAME>",dbName);
			 * 
			 * System.out.println("2 url is " + url);
			 */

			DriverManager.setLoginTimeout(this.connectTimeoutSeconds);
			//System.out.println("got DriverManager ok");
			
			/*
			 * if ( LOG.isDebugEnabled() ) {
			 * LOG.debug("Attempting connection to: " + url); }
			 * 
			 * if ( datasource.isTextFtp() || datasource.isTextSmb() ) { url =
			 * url.replaceAll("<USER>",user);
			 * 
			 * // 2011-06-05 sbuschman 29198 try { pw = URLEncoder.encode(pw,
			 * "UTF-8"); } catch (UnsupportedEncodingException e)
			 * {e.printStackTrace(); }
			 * 
			 * url = url.replaceAll("<PASSWD>",pw); }
			 */

			// url = url.replaceAll("<PASSWD>",pw);

			// check if the server socket is accepting connections
			// if ( !this.isText() && !this.isTextHttps() &&
			// !this.usesSsl(props) ) {
			// DatasourceUtil.checkHostPortOpen(this);
			// }

			// Stopwatch watch = new Stopwatch();

			Connection con = ConcurrentConnecter.connect(url, user, pw,
					connectTimeoutSeconds * 1000, props, dt);

			//System.out.println("after get connection");
			
			// System.out.println("after get connection" +
			// String.valueOf(con.getNetworkTimeout()));

			/*
			 * if ( LOG.isDebugEnabled() ) { LOG.debug(
			 * watch.checkElapsed("Successfully connected to: " +
			 * datasource.getInstanceName() ) ); }
			 * 
			 * // check if the informix database needs a database name other
			 * than the default if ( datasource.isInformix() ) { if (
			 * needsInformixDbName() ) { con = connectUsingInformixDbName(con,
			 * user, pw); } }
			 */

			// update the last connection field.
			// setLastConnect( new Date() );

			/*
			 * // Update the last connection only if this is a real DS // If
			 * called from App User Translation the Datasource Object should not
			 * be saved // and does not have a name (temporary object created
			 * only to get the connection) if ( !Check.isEmpty(
			 * datasource.getName() ) ) { try { datasource.save(); } catch
			 * (Exception e) { // Shouldn't happen if turbine is functional
			 * AdHocLogger.logException(e);
			 * LOG.warn("Could not save last connect time. " +
			 * Informer.thrownMessage(e) ); } }
			 */

			return con;
		} catch (DataSourceConnectException e) {
			DataSourceConnectException dsce =
					//datasource.expandConnectionException(e);
					this.expandConnectionException(e);
			throw dsce;
			//System.out.println("DataSourceConnect exception is " + e.getMessage());
			//throw e;
		}
	}

	/**
	 * @return A Properties object containing any of the properties defined in
	 *         the ConProperty field.
	 * @throws DataSourceConnectException
	 */
	public Properties getOtherConnectionProperties()
			throws DataSourceConnectException {
		Properties props = new Properties();
		if (isOracle()) {
			props.put("oracle.jdbc.ReadTimeout", "60001");
			props.put("oracle.net.READ_TIMEOUT", "70000");
		} else if (isInformix()) {
			props.put("INFORMIXCONTIME", "60");
		} else if (isMySql()) {
			props.put("connectTimeout", "60000");
			props.put("socketTimeout", "60000");
		}
		String conProperties = this.getConProperty();
		if (Check.isEmpty(conProperties)) {
			return props;
		}

		StringTokenizer st = new StringTokenizer(conProperties, PROP_DELIM);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			String[] strs = token.split("=");
			if (strs == null || strs.length != 2) {
				throw new DataSourceConnectException(
						"Error parsing connection string");
			}
			props.put(strs[0].trim(), strs[1].trim());
		}

		return props;
	}

	/**
	 * @return The default port for this datasource
	 */
	public int getDefaultPort()
	{
		if( this.defaultPort <= 0)
		{
				DatasourceType dsType = this.getDatasourceType();
				if(dsType != null)
				{
					this.defaultPort = dsType.getDefaultPort();
				}
		}
		return this.defaultPort;
	}
	
	public boolean isSqltype() {
		return true;
	}
	
	private String versionLevel = "";
	public String getVersionLevel() {
		return versionLevel;
	}
	
	public void setVersionLevel(String t) {
		versionLevel = t;
	}
	
	private String patchLevel = "";
	
	public String getPatchLevel() {
		return patchLevel;
	}
	
	public void setPatchLevel(String t) {
		patchLevel = t;
	}
	
	public String getPatchLevel(Connection con) {
		// con ???
		return patchLevel;
	}	
	
	public String getFullVersionInfo () {
		String str = "";
		// ???
		return str;
	}
	
	/*
	public void addVersionHistory(DatasourceVersionHistory dw) {
		// new DatasourceVersionHistory
		//String newkey = "";
		// dw.setId = newkey;
		// DatasourceVersionHistoryPeer.add(newkey, dw);
		return;
	}
	*/
	
	private String dbType;
	
	public String getDbType()
	{
		if(this.dbType == null || this.dbType.length() == 0)
		{
			this.dbType = DatasourceEnum.getDatasourceTypeName(this);
		}
		return this.dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	//private String versionType = "";
	
	public void setVersionType (String vt) {
		versionType = vt;
	}
	
	public String getVersionType () {
		return versionType;
	}
	
	private boolean usingInformixDbName = false;
	
	public void setUsingInformixDbName (boolean fg) {
		usingInformixDbName = fg;
		return;
	}
	
	public boolean getUsingInformixDbName () {
		return usingInformixDbName;
	}
	
	public boolean isUsingInformixDbName () {
		return usingInformixDbName;
	}
	
	
	private String catalog;
	
	public String getCatalog() {
		return catalog;
	}
	
	public void setCatalog(String st) {
		catalog = st;
		return;
	}
	
 	private DatasourceHandler dbHandler = null;
 	
	public DatasourceHandler getHandler() {
		if(dbHandler == null)
		{
			if(isSqltype())
				//dbHandler = new JDBCDatasourceHandler(this, LOG);
				dbHandler = new JDBCDatasourceHandler(this);
			//else if (isMongodb())
			//	dbHandler = new MongoDatasourceHandler(this, LOG);
		}
/*abstract		
		if(dbHandler == null)
			dbHandler = DatasourceHandler.getInstance(this, LOG);
*/			
		return dbHandler;
	}	
	
	/**
	 * Attempts to piece together more information about the connection exception
	 * @param exception The Exception
	 * @return A Datasource Connection exception with some details about the failure.
	 */
	protected DataSourceConnectException expandConnectionException(Exception exception) {


		// general DataSourceConnectionException message
		StringBuilder buf = new StringBuilder();

		String error = exception.getLocalizedMessage();

		// informix specific details
		if ( this.isInformix() ) {
			if ( Check.contains(error, "Database not found") ) {
				Stringer.newLn(buf).append( messages.getString("database.error.Informix.databaseNotFound") );

			} else if ( Check.contains(error, "Encoding or code set not supported") ) {
				Stringer.newLn(buf).append( messages.getString("database.error.charactersetNotSupported") );

			} else if ( Check.contains(error, "Locale not supported") ) {
				Stringer.newLn(buf).append( messages.getString("database.error.localeNotSupported") );
			}
		}

		// general problems details
		if ( Check.contains(error, "attempt was made to access a database") ) {
			Stringer.newLn(buf).append( messages.getString("database.error.databaseName") );

		} else if (
				Check.contains(error, "invalid username/password")
				|| Check.contains(error, "password invalid")
				|| Check.contains(error, "Incorrect password or user")
				|| Check.contains(error, "password is not correct")
		) {
			Stringer.newLn(buf).append( messages.getString("database.error.invalidUserNamePassword") );

		} else if ( Check.contains(error, "login failed") ) {
			Stringer.newLn(buf).append( messages.getString("database.error.Loginfailed") );

		} else if ( Check.contains(error, "password length, 0, is not allowed") ) {
			Stringer.newLn(buf).append( messages.getString("database.error.passwordRequired") );

		} else if ( Check.contains(error, "password length, 0, is not allowed") ) {
			Stringer.newLn(buf).append( messages.getString("database.error.passwordRequired") );

		} else if (
				Check.contains(error, "UnknownHostException")
				|| Check.contains(error, "Unknown server")
		) {
			Stringer.newLn(buf).append( messages.getString("database.error.invalidHostName") );
			buf.append(": " + this.getHost() );

		} else if (!Check.isEmpty(error) && Check.isEmpty(buf) ) {
			Stringer.newLn(buf).append(error);
		}

		// eliminates double Datasource Connection Exceptions
		if ( 
				exception.getCause() != null 
				&& !Informer.isDataSourceConnectException( exception.getCause() ) 
		) {
			return new DataSourceConnectException( buf.toString(), exception.getCause() );
		}
		return new DataSourceConnectException( buf.toString(), exception);
	}
	
	
	
	/*
	private 
	public void setDatasourceVersionHistorys() {
		
	}
	*/
	
	/*
	public List<String> getCatalogs(Connection con)
	{ 
		return ((JDBCDatasourceHandler)getHandler()).getCatalogs(con);
	}
	*/
	//setDatasourceVersionHistorys
	
	
	private List <DatasourceVersionHistory> collDatasourceVersionHistorys;
	
	public List<DatasourceVersionHistory> getDatasourceVersionHistorys() {
	    if (this.collDatasourceVersionHistorys == null) {
	    	this.collDatasourceVersionHistorys = DatasourceVersionHistoryPeer.getListByDatasourceId(this.getDatasourceId());
	    	//return this.getDatasourceVersionHistorys( new Criteria(10) );
	    }
	    return this.collDatasourceVersionHistorys;
	
	}	
	
	/**
	 * @param list
	 *  
	 */
	public void setDatasourceVersionHistorys(List<DatasourceVersionHistory> list){
	    	this.collDatasourceVersionHistorys = list;
    }

	/**
	   * @param criteria
		 * @return The list of Version Histories with the most recent first.
	   * @throws TorqueException
    */

	/*
	public List<DatasourceVersionHistory> getDatasourceVersionHistorys(Criteria criteria)
			throws TorqueException {
	  	// list is not yet initialized
		if (this.collDatasourceVersionHistorys == null) {
	  		// this is not yet saved, not possible to retrieve the list, just init.
	  		if ( this.isNew() ) {
	  			this.collDatasourceVersionHistorys = new ArrayList<DatasourceVersionHistory>();

	  		} else {
	  			criteria.add(DatasourceVersionHistoryPeer.DATASOURCE_ID, this.getDatasourceId() );
	  			criteria.add(DatasourceVersionHistoryPeer.VERSION_TYPE,getVersionType());
	  			criteria.addDescendingOrderByColumn(DatasourceVersionHistoryPeer.DATASOURCE_VERSION_HISTORY_ID);
	  			this.collDatasourceVersionHistorys = DatasourceVersionHistoryPeer.doSelect(criteria);
	  		}

	  	} else {
	  		// if this has been saved, can retrieve the list
	  		if ( !this.isNew() ) {
	  			criteria.add(DatasourceVersionHistoryPeer.DATASOURCE_ID, this.getDatasourceId() );
	  			criteria.add(DatasourceVersionHistoryPeer.VERSION_TYPE,getVersionType());
	  			criteria.addDescendingOrderByColumn(DatasourceVersionHistoryPeer.DATASOURCE_VERSION_HISTORY_ID);

	  			// if the last criteria is the not same as this one, do a new query
	  			if ( !this.lastDatasourceVersionHistoryCriteria.equals(criteria) ) {
	    			this.collDatasourceVersionHistorys = DatasourceVersionHistoryPeer.doSelect(criteria);
	  			}
	  		}
	  	}
	  	this.lastDatasourceVersionHistoryCriteria = criteria;

	  	return this.collDatasourceVersionHistorys;
	}	
	*/
	
	/**
	 * Updates the datasource name, datasource type, and verified date of the most recent history
	 * if this history is not new.
	 * @param history
	 * @return Whether The history has new values, or is the same as the most recently recorded value.
	 */
	public boolean isNewVersionHistory(DatasourceVersionHistory history) {
		// null history can't be new
		if (history == null) {
			return false;
		}

		// get the most recent version history
		DatasourceVersionHistory priorHistory = null;
			List<DatasourceVersionHistory> list = this.getDatasourceVersionHistorys();
			// no prior history, this must be new
			if ( Check.isEmpty(list) ) {
				return true;
			}
			priorHistory = list.get(0);

		// no prior history, this must be new
		if (priorHistory == null) {
			return true;
		}

		// calculate whether this is a new version and patch level
		boolean newVersion = !history.getVersionLevel().equals( priorHistory.getVersionLevel() );
		boolean knownVersion = !DatasourceVersionHistoryPeer.isUnknown( history.getVersionLevel() );
		boolean newPatch = !history.getPatchLevel().equalsIgnoreCase( priorHistory.getPatchLevel() );
		boolean knownPatch = !DatasourceVersionHistoryPeer.isUnknown( history.getPatchLevel() );

		boolean newEntry = (knownVersion && newVersion) || (knownPatch && newPatch);

		// set the most recent version history to represent the current state
		// don't do the update if we got a new "Unknown" value in the version or the patch level
		if (!newEntry && !newVersion && !newPatch) {
			priorHistory.setDatasourceTypeDescription( this.getTypeName() );
			priorHistory.setDatasourceDescription( this.getName() );
			priorHistory.setVerified( new Date() );

			// save the prior history
			try {
				priorHistory.save();
				/*
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Updated Version History: " + priorHistory);
				}
				*/
				
			} catch (Exception e) {
				String msg = "Unable to update the Version History verified date for: " + priorHistory;
				//LOG.error(msg, e);
				AdHocLogger.logException(e);
			}
		}

		// whether the patch was new
		return newEntry;
	}

	
	/**
	 * Adds a version history to this datasource and saves it, if it is different than the most
	 * recent version history.
	 * @param versionHistory
	 */
	public void addVersionHistory(DatasourceVersionHistory versionHistory) {
		versionHistory.setDatasourceId( this.getDatasourceId() );
		if ( this.isNewVersionHistory(versionHistory) ) {
			try {
				versionHistory.save();

				// add the history to the top position so the next time we access it, we have the most recent
				this.getDatasourceVersionHistorys().add(0, versionHistory);
				//if ( LOG.isInfoEnabled() ) {
				//	LOG.info("Added new Version History: " + versionHistory);
				//}

			} catch (Exception e) {
				AdHocLogger.logException(e);
				//LOG.error("Could not save Version History: " + versionHistory, e);
			}
		}
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
	protected DatasourceVersionHistory findLatestVersionHistory() throws DataSourceConnectException {
		return getHandler().findLatestVersionHistory();
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
	protected DatasourceVersionHistory findLatestVersionHistory(Connection con) {
		return ((JDBCDatasourceHandler)getHandler()).findLatestVersionHistory(con);
	}

	/**
	 * @return A new version history
	 * @throws DataSourceConnectException
	 */
	public DatasourceVersionHistory findVersionHistory() throws DataSourceConnectException {
		return getHandler().findVersionHistory();
	}

	/**
	 * @param con
	 * @return A new version history
	 */
	public DatasourceVersionHistory findVersionHistory(Connection con) {

		return ((JDBCDatasourceHandler)getHandler()).findVersionHistory(con);
	}
	
	public void save() {
		DatasourcePeer.add(this);
		return;
	}
	
	public void dump() {
        System.out.println("datasouce id: " + this.getDatasourceId());
        System.out.println("datasouce name: " + this.getName());
        System.out.println("datasouce type id: " + this.getDatasourceTypeId());
        System.out.println("datasouce type name: " + this.getTypeName());
        System.out.println("datasouce application id: " + this.getApplicationId());
        System.out.println("datasouce host: " + this.getHost());
        System.out.println("datasouce port: " + this.getPort());
        System.out.println("datasouce user: " + this.getUserName());
        //System.out.println("datasouce password: " + this.getPassword());
        System.out.println("datasouce service : " + this.getServiceName());
        System.out.println("datasouce DB name : " + this.getDbName());
        System.out.println("datasouce Db Driver ID : " + this.getDbDriverId());
        System.out.println("datasouce Con property : " + this.getConProperty());
	}
	
	
}
