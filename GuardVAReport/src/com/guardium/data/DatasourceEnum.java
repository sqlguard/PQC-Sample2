/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.DbDriverMap;
import com.guardium.utils.Check;
import com.guardium.utils.i18n.SayAppRes;

public enum DatasourceEnum {
	ORACLE(     1, "oracle",    false, false, true,2,2), 
	DB2(        2, "db2",       false, false, true,0,0),
	SYBASE(     3, "sybase",    false,  true, true,0,0),
	MSSQL(      4, "mssql",     false,  true, true,1,1),
	INFORMIX(   5, "informix",  false,  true, true,0,0),
	MYSQL(      6, "mysql",     false,  true, true,0,0),
	TEXT(       7, "text",       true,  true, false,0,0),
	TEXT_HTTP(  8, "textHttp",   true,  true, true,0,0),
	TEXT_FTP(   9, "textFtp",    true,  true, true,0,0),
	TEXT_SMB(  10, "textSmb",    true,  true, true,0,0),
	TEXT_HTTPS(11, "textHttps",  true,  true, true,0,0),
	TERADATA(  12, "teradata",  false,  false, true,0,0),
	N_A(       13, null,        false,  true, false,0,0),
	DB2_400(   14, "db2_400",   false, false, true,0,0),
	POSTGRESQL(15, "postgres",  false,  true, true,0,0),
	NETEZZA(   16, "netezza",   false,  true, true,0,0),
	DB2_ZOS(   17, "DB2_z/OS",   false,  false, true,0,0),
	SYBASE_IQ(   18, "sybase_IQ",   false,  false, true,0,0),
	GREENPLUM(19, "greenplum",  false,  true, true,0,0), 
	ASTER(20, "aster",  false,  true, true,0,0),
	MONGODB(21, "mongo",  false,  true, true,0,0),
	SAPHANA(22,"sapHana",false,  true, true,0,0);


	static private final Map<Integer, DbDriver> allDriverMap       = new HashMap<Integer,DbDriver>(18);
	static private final List<DatasourceType>   allDatasourceTypes = new ArrayList<DatasourceType>(16);
	
	private final int datasourceTypeId;
	private final boolean isText;
	private final boolean isCatalog; 
	private final boolean isPortRequired;
	private final String uploadFileName;
	private final int NumOfOpensourceDrivers;
	private final int numOfDataDirectDrivers;
	
	private DatasourceType datasourceType;
	private List<DbDriver> drivers;
	private List<Integer> driverIds;
	
	private DatasourceEnum(int datasourceTypeId, String uploadFileName, boolean isText, boolean isCatalog, boolean isPortRequired, int NumOfOpensourceDrivers, int numOfDataDirectDrivers) {
		this.datasourceTypeId = datasourceTypeId;
		this.uploadFileName   = uploadFileName;
		this.isText           = isText;
		this.isCatalog        = isCatalog;
		this.isPortRequired = isPortRequired;
		this.NumOfOpensourceDrivers = NumOfOpensourceDrivers;
		this.numOfDataDirectDrivers = numOfDataDirectDrivers;
	}

	/**
	 * @return true=meta.getCatalogs(), false=meta.getSchemas()
	 */
	public boolean isCatalog() {
		return isCatalog;
	}
	
	public boolean isPortRequired() {
		return isPortRequired;
	}
	public int getDatasourceTypeId() {
		return datasourceTypeId;
	}

	public boolean isText() {
		return isText;
	}
	
	public String getUploadJarFileName() {
		return "upload_" + uploadFileName + ".jar";
	}

	/**
	 * This is imperfect because users still to restart in order for the jar files to work. But it's close.
	 */
	synchronized static private void reinit() {
		allDriverMap.clear();
		init();
	}
	
	synchronized static private void init() {
		// I only want to execute this once, so testing on allDriverMap is easy.
		if (allDriverMap.size() > 0) {
			return;
		}

		// These clears can't hurt - even though it seems to contradict the above if statement. If the if test changes, then in fact the clears are a good thing.
		allDriverMap.clear();
		allDatasourceTypes.clear();
		
		for (DatasourceEnum type : DatasourceEnum.values()) {
				type.initType();
		}
	}

	/**
	 * This is only called by init(), strictly for initialization
	 */
	@SuppressWarnings("unchecked")
	
	private void initType()  {
		
		// get datasourcetype list
		DatasourceTypeMap vtmap = new DatasourceTypeMap ();

		List <DatasourceType> mlist = new ArrayList<DatasourceType>();
		mlist = vtmap.getList();

		/*
		for (DatasourceType s : mlist) {
			allDatasourceTypes.add(s);
		}

		for (DatasourceType s : allDatasourceTypes) {
	        System.out.println("datasource type id: " + s.getDatasourceTypeId());
	        System.out.println("datasource type name: " + s.getName());
	        System.out.println("datasource default driver id: " + s.getDefaultDriverId());
	        System.out.println("datasource default port: " + s.getDefaultPort());
	    }
		*/
		
		datasourceType = vtmap.getDatasourceType(datasourceTypeId);
		allDatasourceTypes.add(datasourceType);
		
	
		// get datasource driver list
		DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
		
		// create DbDriver, put in DbDriver list
		
		List <DbDriver> mdlist = new ArrayList<DbDriver>();
		mdlist = DbDriverPeer.getList();		
		
		drivers = DbDriverPeer.getDbDriverByType(datasourceTypeId);

		driverIds = new ArrayList<Integer>(drivers.size());

		for (DbDriver rec : drivers) {
			int id = rec.getDbDriverId();
			allDriverMap.put(id, rec);
			driverIds.add(id);
		}
	}
	
	
	private void initType_2()  {

		// get datasourcetype list
		DatasourceTypeMap vtmap = new DatasourceTypeMap ();
		// create Datasource, put in Datasource list

		List <DatasourceType> mlist = new ArrayList<DatasourceType>();
		mlist = vtmap.getList();
		
		for (DatasourceType s : mlist) {
			allDatasourceTypes.add(s);
		}
		
		for (DatasourceType s : allDatasourceTypes) {
	        System.out.println("datasource type id: " + s.getDatasourceTypeId());
	        System.out.println("datasource type name: " + s.getName());
	        System.out.println("datasource default driver id: " + s.getDefaultDriverId());
	        System.out.println("datasource default port: " + s.getDefaultPort());
	    }
		
		// get datasource driver list
		//DbDriverMap vmap = new DbDriverMap ();
		DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
		
		// List <DbDriver> mdlist = new ArrayList<DbDriver>();
		drivers = DbDriverPeer.getList();		
		
		for (DbDriver rec : drivers) {
			int id = rec.getDbDriverId();
			allDriverMap.put(id, rec);
			driverIds.add(id);
		}
		
	}
	/*
	@SuppressWarnings("unchecked")
	public void addToDrivers() {
		init();
		int numDrivers = getNumOfOpensourceDrivers();
		if(numDrivers <1) 
			return;
		try {
			String prefix    = "dbDriver.";	
			for(int i= 1; i<numDrivers + 1 ; i++)
			{
				String driverClass = SayAppRes.sayException(prefix + "class." + i+ "." + datasourceTypeId); // Intentionally throw an exception if not found
				String driverId = SayAppRes.sayException(prefix + "dbDriverId." + i+ "." + datasourceTypeId);
				
				try {
//since we know the driver_id, it is better to do it using id.  This will be a problem if there are multiple with the same class name
					//Criteria       criteria = new Criteria().add(DbDriverPeer.DATASOURCE_TYPE_ID, datasourceTypeId).add(DbDriverPeer.DRIVER_CLASS, driverClass);
					Criteria       criteria = new Criteria().add(DbDriverPeer.DATASOURCE_TYPE_ID, datasourceTypeId).add(DbDriverPeer.DB_DRIVER_ID, driverId);
					List<DbDriver> list     = DbDriverPeer.doSelect(criteria);
					DbDriver       driver;
					
					if (list.size() > 0) {
						driver = list.get(0);
					}
					else {
						driver = new DbDriver();
						
						driver.setDatasourceTypeId(datasourceTypeId);
						driver.setDriverClass(driverClass);
						// 2010-06-28 sbuschman 21251 
//						driver.setDbDriverId(Integer.valueOf(SayAppRes.sayException(prefix + "dbDriverId." + i+ "." + datasourceTypeId)));
						driver.setDbDriverId(Integer.valueOf(driverId));
					}

					driver.setName(SayAppRes.sayException(prefix + "name." + i+ "." + datasourceTypeId));
					driver.setUrlTemplate(SayAppRes.sayException(prefix + "url." + i+ "." + datasourceTypeId));
					driver.setBaseUrlTemplate(SayAppRes.sayException(prefix + "baseUrl." + i+ "." + datasourceTypeId));
					driver.save();
				} catch (MissingResourceException e) { e.printStackTrace(); } // I do want to know if a property is missing
				  catch (TorqueException e)          { e.printStackTrace(); } 
				  catch (Exception e)                { e.printStackTrace(); }						
			}
			reinit();
		} catch (MissingResourceException e) {} // Ignore the exception
	}
	*/
	
	public DatasourceType getDatasourceType() {
		init();
		return datasourceType;
	}
	
	public List<DbDriver> getDrivers() {
		init();
		return drivers;
	}
	
	public List<Integer> getDriverIds() {
		init();
		return driverIds;
	}
	
	public String getName() {
		init();
		return datasourceType.getName();
	}

	public boolean equals(Datasource datasource) {
		init();
		return datasourceTypeId == datasource.getDatasourceTypeId();
	}

	public boolean equals(DatasourceType datasourceType) {
		init();
		return datasourceTypeId == datasourceType.getDatasourceTypeId();
	}

	public boolean equals(int driverId) {
		init();
		return driverIds.contains(driverId);
	}

	/**
	 * 2011-06-24 There are DataDirect drivers for Oracle and MSSQL  
	 */
	static public boolean isDataDirect(Datasource datasource) {
		init();
		
		int datasourceTypeId = datasource.getDatasourceTypeId();
		boolean result = 
			(datasourceTypeId == ORACLE.getDatasourceTypeId() || datasourceTypeId == MSSQL.datasourceTypeId) && 
		    datasource.getDbDriverId() == datasourceTypeId;
		
		return result;
	}
	
	public DbDriver getDefaultDriver() {
		init();
		
		for (DbDriver dbDriver : drivers) {
			if (datasourceType.getDefaultDriverId() == dbDriver.getDbDriverId()) {
				return dbDriver;
			}
		}
		
		// The above shouldn't fail, but what heck, make nice and don't go crazy and return null
		return drivers.get(0);
	}
	
	public static DatasourceEnum[] getValues() {
		init();
		return DatasourceEnum.values();
	}
	
	public static DatasourceEnum get(Datasource datasource) {
		for (DatasourceEnum type : getValues()) {
			if (type.equals(datasource)) {
				return type;					
			}
		}
		
		return null;
	}

	public static DatasourceEnum get(DatasourceType datasourceType) {
		for (DatasourceEnum type : getValues()) {
			if (type.equals(datasourceType)) {
				return type;
			}
		}
		
		return null;
	}

	public static DatasourceEnum getByDbDriverId(int dbDriverId) {
		for (DatasourceEnum type : getValues()) {
			if (type.equals(dbDriverId)) {
				return type;
			}
		}

		return null;
	}
	
	public static DatasourceEnum getByDatasourceTypeId(int datasourceTypeId) {
		for (DatasourceEnum type : getValues()) {
			if (type.datasourceTypeId == datasourceTypeId) {
				return type;
			}
		}

		return null;
	}
	
	public static Map<Integer, DbDriver> getAllDbDriverMap() {
		init();
		return allDriverMap;
	}

	public static List<DbDriver> getAllDrivers() {
		init();
		return new ArrayList<DbDriver>(allDriverMap.values());
	}

	public static List<DatasourceType> getAllDatasourceTypes() {
		init();
		return allDatasourceTypes;
	}

	public static String getDriverName(Datasource datasource) {
		String   result   = null;
		DbDriver dbDriver = getAllDbDriverMap().get(datasource.getDbDriverId());
		
		if (dbDriver != null) {
			result = dbDriver.getName();				
		}
		
		return result;
	}	

	/**
	 * @param datasource
	 * @return DATASOURCE_TYPE.NAME
	 */
	public static String getDatasourceTypeName(Datasource datasource) {
		String         result = null;
		DatasourceEnum type   = DatasourceEnum.getByDatasourceTypeId(datasource.getDatasourceTypeId());
		
		if (type != null) {
			result = type.getName();				
		}
		
		return result;
	}
	
	/**
	 * It's strongly encouraged that you do *not* use this method, rather use getDatasourceTypeName(datasource)
	 * 
	 * @param datasourceTypeId
	 * @return DATASOURCE_TYPE.NAME
	 */
	public static String getDatasourceTypeName(int datasourceTypeId) {
		String         result = null;
		DatasourceEnum type   = DatasourceEnum.getByDatasourceTypeId(datasourceTypeId);
		
		if (type != null) {
			result = type.getName();				
		}
		
		return result;
	}


	public static List<DatasourceType> getAllCsvDatasourceTypes() {
		List<DatasourceType> result = new ArrayList<DatasourceType>();

		for (DatasourceEnum type : getValues()) {
			if (type.isText) {
				result.add(type.datasourceType);
			}
		}

		return result;
	}

	public static void addAllCsvDatasourceTypeIds(List<Integer> types) {
		for (DatasourceEnum type : getValues()) {
			if (type.isText) {
				types.add(type.getDatasourceTypeId());
			}
		}
	}
	
	/**
	 * @return<pre>
	 * all the drivers ids in format: [DATASOURCE_TYPE_ID:DB_DRIVER_ID].
	 * [1:1][1:18][2:2][3:3][4:4][4:17][5:5][6:6][7:7][8:8][9:9][10:10][11:11][12:12][13:13][14:14][15:15][16:16]
	 * [1:1] = Oracle DataDirect, [1:18] = Uploaded Oracle
	 * [4:4] = MS DataDirect,     [4:17] = Uploaded MS
	 * </pre>
	 * Only used in datasourceEdit.jsp.
	 */
	public static String getAllDriverIdsJsp() {
		init();
		
		String result = "";
		
		for (DatasourceEnum type : getValues()) {
			for (int id : type.driverIds) {
				result += "[" + type.datasourceTypeId + ":" + id + "]";
			}
		}
		
		return "'" + result + "'";
	}
	public boolean isOpenSourceLoaded()
	{
		init();
		return (getNumOfOpensourceDrivers() > 0 &&
				!Check.isEmpty(getDriverIds()) && 
				getDriverIds().size() > numOfDataDirectDrivers &&
				getDriverIds().get(numOfDataDirectDrivers) > 0)? true : false;

	}
	public boolean isServiceOpenSourceLoaded()
	{
		init();
		return (getNumOfOpensourceDrivers() > 0 &&
				!Check.isEmpty(getDriverIds()) && 
				getDriverIds().size() > numOfDataDirectDrivers + 1 &&
				getDriverIds().get(numOfDataDirectDrivers + 1) > 0)? true : false;

	}
	public boolean isSidOpenSourceLoaded()
	{
		init();
		return (getNumOfOpensourceDrivers() > 0 &&
				!Check.isEmpty(getDriverIds()) && 
				getDriverIds().size() > numOfDataDirectDrivers &&
				getDriverIds().get(numOfDataDirectDrivers) > 0)? true : false;

		
	}
    /*
	@SuppressWarnings("unchecked")
	public void changeAllDatasourcesToOpensource() throws TorqueException, Exception
	{
		DatasourceType dsType = getDatasourceType();
		if(dsType == null)
		{
			throw new Exception(SayAppRes.say("message.upload.subscribed.groups.noDbType"));
			
		}
		List<Datasource> datasources = dsType.getDatasources();
		int openDriverId = getOpenSourceDriverId();
		if(openDriverId  < 1)
		{
			throw new Exception(SayAppRes.say("message.upload.subscribed.groups.noOpensourceDriverLoaded",dsType.getName()));
		}
		if(datasources == null)
			return;
		for(Datasource ds: datasources)
		{
			if(ds == null || ds.getDbDriverId() == openDriverId)
				continue;
			ds.setDbDriverId(openDriverId);
			ds.save();
		}
	}
	*/
	/*
	public void changeAllDatasourcesToOpensource(int openDriverId,String driverName) throws TorqueException, Exception
	{
		DatasourceType dsType = getDatasourceType();
		if(dsType == null)
		{
			throw new Exception(SayAppRes.say("message.upload.subscribed.groups.noDbType"));
			
		}
		List<Datasource> datasources = dsType.getDatasources();
		if(openDriverId  < 1)
		{
			throw new Exception(SayAppRes.say("message.upload.subscribed.groups.notSpecificOpensourceDriverLoaded", driverName,dsType.getName()));			
		}
		if(datasources == null)
			return;
		for(Datasource ds: datasources)
		{
			if(ds == null || ds.getDbDriverId() == openDriverId)
				continue;
			ds.setDbDriverId(openDriverId);
			ds.save();
		}
	}
	*/
	
	public int getOpenSourceDriverId() {
		return (NumOfOpensourceDrivers > 0 && !Check.isEmpty(driverIds) &&
				driverIds.size() > numOfDataDirectDrivers &&
				driverIds.get(numOfDataDirectDrivers)> 0) ? driverIds.get(numOfDataDirectDrivers) : 0;
		
	}
	
	public int getNumOfOpensourceDrivers() {
		return NumOfOpensourceDrivers;
	}
	
	public int getServiceOpenSourceDriverId() {
		return (NumOfOpensourceDrivers > 0 && !Check.isEmpty(driverIds) &&
				driverIds.size() > numOfDataDirectDrivers + 1 &&
				driverIds.get(numOfDataDirectDrivers + 1)> 0) ? driverIds.get(numOfDataDirectDrivers + 1) : 0;

	}
	
}