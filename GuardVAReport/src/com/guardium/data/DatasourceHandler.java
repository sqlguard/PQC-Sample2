/*
 * 
 */
package com.guardium.data;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.guardium.data.DataSourceConnectException;

/**
 * @author msanayei
 *
 */
abstract class DatasourceHandler {
	abstract boolean testConnection() throws DataSourceConnectException;
	abstract String getFullVersionInfo() throws DataSourceConnectException;
/* using DBConnection class	
	abstract String getFullVersionInfo(DBConnection con);
	abstract String getVersionLevel(DBConnection con, String verType);
*/	
	abstract String getVersionLevel() throws DataSourceConnectException;
	abstract int getMinorVersion() throws DataSourceConnectException; //must write for mongo
	abstract int getMajorVersion() throws DataSourceConnectException;
	abstract String getPatchLevel() throws DataSourceConnectException;
	abstract DatasourceVersionHistory findVersionHistory() throws DataSourceConnectException;
	abstract DatasourceVersionHistory findLatestVersionHistory() throws DataSourceConnectException;
	
	abstract boolean isCatalogSupported() throws SQLException, DataSourceConnectException;
	static DatasourceHandler getInstance(Datasource datasource, Logger log){
		DatasourceHandler helper = null;
		if(datasource.isSqltype())
			helper = new JDBCDatasourceHandler(datasource, log);
		//else if (datasource.isMongodb())
		//	helper = new MongoDatasourceHandler(datasource, log);
		return helper;
	}
}
