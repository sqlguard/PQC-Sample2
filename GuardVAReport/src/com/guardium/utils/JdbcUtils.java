/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//import org.apache.torque.Torque;
//import org.apache.torque.TorqueException;

import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceUtil;
import com.guardium.utils.i18n.SayAppRes;
import com.guardium.utils.GuardGeneralException;

public class JdbcUtils
{
	public static final String DEFAULT_DB_NAME = "guard_local";
	public static final String MYSQL_MYISAM = "ENGINE=MyISAM";
	public static final String MYSQL_INNODB = "ENGINE=InnoDB";
	public static final String DB_TURBINE = "TURBINE";
	public static final String DB_SANDBOX = "SANDBOX";
	public static final String DB_MODEL = "MODEL";
	 
	/*
	 static public Object executeSqlForSingleVal(String query, String conName) throws SQLException, Exception {
			Object value = null;
			Statement stmt = null;
			ResultSet rs = null;
			Connection con = null;
			try {
				con = Torque.getConnection(conName);
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);			

				rs = stmt.executeQuery(query);
				if ( rs.next() ) {
					value=rs.getObject(1);
				}

				return value;

			} finally {
				Check.disposal(rs);
				Check.disposal(stmt);
				Check.disposal(con);
				
			}
		}
	 */
	
	/*
	 static public int executeSqlForSingleInt(String query, String conName) throws SQLException, Exception{
			
		    int value=0;
			Statement stmt = null;
			ResultSet rs = null;
			Connection con = null;
			try {
				con = Torque.getConnection(conName);
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);			

				rs = stmt.executeQuery(query);
				if ( rs.next() ) {
					 value=rs.getInt(1);
				}

				return value;

			} finally {
				Check.disposal(rs);
				Check.disposal(stmt);
				Check.disposal(con);
				
			}
		}
	*/


	/*
	static public Timestamp executeSqlForTimestamp(String query, String conName) throws SQLException, Exception{
		Timestamp value = null;
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = Torque.getConnection(conName);
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);			

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				value=rs.getTimestamp(1);
			}

			return value;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
			Check.disposal(con);
			
		}
	}
	*/

	static public String executeSqlForSingleValAsString(String query, int colNum,Connection con) throws SQLException{
		Object value = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);			

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				value=rs.getObject(colNum);
			}

			return value==null?null:value.toString();

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	/*
	static public String executeSqlForSingleValAsString(String query, String conName) throws SQLException, Exception{
	    Connection con = null;

		try
		{
		   	con = Torque.getConnection(conName);
		   	return executeSqlForSingleValAsString(query,1,con);
		}
		finally
		{
			Check.disposal(con);
		}
	}
	*/
	/*
	static public String executeSqlForSingleValAsString(String query) throws SQLException, Exception{
		return executeSqlForSingleValAsString(query, DEFAULT_DB_NAME);
	}
	*/
	
	static public boolean existFromCount(String query,Connection con) throws DataSourceConnectException, SQLException{
		Long l = executeSqlForNumber( query, con);
		if(l>0)
			return true;
		else
			return false;
	}
	
	static public Long executeSqlForNumber(String query,Connection con) throws DataSourceConnectException, SQLException{
		String value = executeSqlForSingleValAsString(query,1,con);
		if(value!=null){
			return Long.parseLong(value.toString().trim());
		}
		return null;
	}
	
	/*
	static public Long executeSqlForNumber(String query, String conName) throws DataSourceConnectException, SQLException, Exception {
	    Connection con = null;

		try
		{
		   	con = Torque.getConnection(conName);
		   	return executeSqlForNumber(query,con);
		}
		finally
		{
			Check.disposal(con);
		}
	}
	*/
	/*
	static public Long executeSqlForNumber(String query) throws DataSourceConnectException, SQLException, Exception{
		return executeSqlForNumber(query, DEFAULT_DB_NAME);
	}
	*/
	
	/*
	static public ResultSet executeSqlForResultSet(String query, String conName) throws DataSourceConnectException, SQLException, Exception {
	    Connection con = null;

		try
		{
		   	con = Torque.getConnection(conName);
		   	return Utils.executeSqlForResultSet(query,con);
		}
		finally
		{
			Check.disposal(con);
		}
	}
	*/
	
	/*
	static public ResultSet executeSqlForResultSet(String query, Connection con) throws DataSourceConnectException, SQLException{
		return Utils.executeSqlForResultSet(query, con);
	}
	*/
	
	/*
	static public void executeSql(String sql) throws SQLException, DataSourceConnectException, Exception {
		Connection con = null;
		try
		{
		   	con = Torque.getConnection(DEFAULT_DB_NAME);
		   	executeSql(sql,con);
		}
		finally
		{
			Check.disposal(con);
		}
	}
	*/
	
	static public void executeSql(String sql, Connection con) throws SQLException {
	    Statement st = null;
	    
		try
		{
	        st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        st.execute(sql);
		}
		finally
		{
			Check.disposal(st);
		}
	}
	
	static public void executeSqls(List<String> sqls, Connection con) throws SQLException {
		for (int i = 0; i < sqls.size(); i++){
			JdbcUtils.executeSql(sqls.get(i),con);
		}
	}
	
	/*
	static public void executeSqls(List<String> sqls, String conName) throws Exception {
		Connection con = null;
		
		try {
			con = Torque.getConnection(conName);
			for (int i = 0; i < sqls.size(); i++){
				JdbcUtils.executeSql(sqls.get(i),con);
			}
		} finally {
			Check.disposal(con);
		}
		
	}
	*/
	/*
	public static List executeRemoteSqlForList(String selectSql) throws SQLException, Exception
	{
		return executeSqlForList(selectSql,"guard_remote");
	}

	public static List<String> executeSqlForList(String selectSql) throws  SQLException, Exception {
		return executeSqlForList(selectSql, DEFAULT_DB_NAME);
	}
	*/
	
	/*
	public static List<String> executeSqlForList(String selectSql, String conName) throws SQLException, Exception {
		
		Connection con = null;
		
		try {
			con = Torque.getConnection(conName);
			return executeSqlForList(selectSql,con);
		} finally {
			Check.disposal(con);
		}
 	}
 	
 	*/
	
	
	public static List<String> executeSqlForList(String selectSql, Connection con) throws  SQLException {
		List<String> l = new ArrayList<String>();
		
		Statement st = null;
		ResultSet rs = null;
		try {			
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {
				int colNum = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					l.add(rs.getString(1));
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
		}
		return l;
 	}
	
	/*
	public static List executeSqlForIds(String selectSql) throws  SQLException, Exception {
		
		return executeSqlForIds(selectSql, "guard_local");
 	}
	*/
	
	/*
	public static List executeSqlForIds(String selectSql, String conName) throws  SQLException, Exception {
		
		Connection con = null;
		
		try {
			con = Torque.getConnection(conName);
			
			return executeSqlForIds(selectSql,con);
		} finally {
			Check.disposal(con);
		}
 	}
 	*/
	
	public static List executeSqlForIds(String selectSql, Connection con) throws  SQLException {
		List l = new ArrayList<Long>();
		
		Statement st = null;
		ResultSet rs = null;
		try {			
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {

				rs.beforeFirst();
				int colNum = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					l.add(rs.getLong(1));
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
		}
		return l;
 	}
	
	/*
	public static List<List<Object>> executeSqlForListOfObjectList(String selectSql, String conName) throws  SQLException, Exception {
		
		Connection con = null;
		
		try {
			con = Torque.getConnection(conName);
			
			return executeSqlForListOfObjectList(selectSql, con);
		} finally {
			Check.disposal(con);
		}
 	}
	*/
	
	public static List<List<Object>> executeSqlForListOfObjectList(String selectSql, Connection con) throws  SQLException {
		List<List<Object>> l = new ArrayList<List<Object>>();
		
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {

				rs.beforeFirst();
				int colNum = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					List<Object> row = new ArrayList<Object>();					
					for(int i = 1; i<=colNum; i++)
						row.add(rs.getObject(i));
					l.add(row);
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
		}
		return l;
 	}
	
	/*
	static public void executeSingleDDLSql(String sql, String conName) throws  SQLException, Exception{
	    Connection con = null;
	    Statement st = null;
	    
		try
		{
		   	con = Torque.getConnection(conName);

	        st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

	        st.execute(sql);

		}
		finally
		{
			Check.disposal(st);
			Check.disposal(con);
		}
	}
	*/
	
	/*
    public static boolean isColInTable(String colName,String tbl,String conName) throws DataSourceConnectException,  SQLException
    {
    	String sql = "select limit 1 * from "+tbl;
    	
		ResultSet rs =	null;
		// 2013-07-02 Fixed by Rosa 35234 
		try {
			rs = JdbcUtils.executeSqlForResultSet(sql,conName);
			ResultSetMetaData md = rs.getMetaData();
			for (int i = 1; i <= md.getColumnCount(); i++)
			{
	            if(Check.same(md.getColumnName(i),colName))
	            {
	            	return true;
	            }
	        }
		return false;
		} finally {
			Check.disposal(rs);
		}
    }
    */
    public static boolean isColInTable(String colName, ResultSetMetaData md) throws DataSourceConnectException,  SQLException
    {

		for (int i = 1; i <= md.getColumnCount(); i++)
		{
            if(Check.same(md.getColumnName(i).toLowerCase(),colName.toLowerCase()))
            {
            	return true;
            }
        }
		return false;
    }
    /*
    public static ResultSetMetaData getTblMeta(String tbl, String conName) throws DataSourceConnectException,  SQLException, Exception{
    	//change from informix syntax to mysql syntax
    	String sql = "select * from "+tbl+" limit 1,2";
    	
		ResultSet rs =	null;
		// 2013-07-02 Fixed by Rosa 35234
		try {
			rs =	JdbcUtils.executeSqlForResultSet(sql,conName);
			ResultSetMetaData md = rs.getMetaData();
			rs.close();
			return md;
		} finally {
			Check.disposal(rs);
		}
    }
    */
    
    public static List<String> getListFromRowSet(ResultSet rs)
	throws Exception {

    	return getListFromRowSet(rs, 1);

	}
    
    public static List<String> getListFromRowSet(ResultSet rs, int firstN)
	throws Exception {

		List<String> result = new ArrayList<String>();
        int colNum = rs.getMetaData().getColumnCount();
		while ( rs.next() ) {
			if(colNum==1||firstN==1)
			result.add( rs.getString(1) );
			else{ 
				StringBuilder sb = new StringBuilder();
				for(int i=1;i<=colNum;i++)
				{
					String value = rs.getString(i);
					if(value!=null)
						value=value.trim();
					if(i!=1)
						sb.append("-");
				    sb.append(String.valueOf(value));				
				}
				result.add(sb.toString());
			}
		}

		return result;

	}

    /*
    public static Long getNumFieldVal(String table, String field) throws DataSourceConnectException, Exception
    {
    	String sql = "select "+field+" from "+table;
    	return executeSqlForNumber(sql, DEFAULT_DB_NAME);
    }
    */
    
    public static String sql(String sql)
    {
    	return sql;
    }
    
	/**
	 * @param con A Connection to use.
	 * @param query
	 * @return A list of databases for this datasource
	 * @throws SQLException
	 */
	public static List<String> getPostgreSqlDbList (Datasource ds) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		String tmpString= "";
		String query = "select datname as \"database_name\""
			+ " from pg_database"
			+ " where datistemplate = 'f'"               //-- we don't test template database.
			;
		List <String> dbList = new ArrayList<String>();
		Connection con = null;
		try {
			con = ds.getConnection();
		} catch (DataSourceConnectException e1) {
			//DebugUtils.getLogger(DebugUtils.COMP_CUST_TBL).error(e1);
		}
		// run query, get db list
		try {

			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);

			// check the result	
			while (rs.next()) {

				try {
					tmpString = rs.getString(1);
					tmpString = tmpString.trim();
					dbList.add(tmpString);			
				} catch (Exception e) {
					String err = "Invalid column index";
					if (e.getMessage().contains(err)) {
						;
					} else {
						AdHocLogger.logException(e);
					}
				}
			}
			
			return dbList;

		}
		catch (SQLException e) {
			//String msg =  " + this;
			//LOG.warn(msg + Say.NL + Informer.thrownMessage(e) );
			AdHocLogger.logException(e);
		}
		finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
			con = Check.disposal(con);
		}
		
		return dbList;

	}
	
	public static String getVarForSql(String val)
	{
		String retStr = val;
		retStr = retStr.replaceAll("\\\\", "\\\\\\\\");
        retStr = retStr.replaceAll("'", "''");
        return retStr;
	}
	
	/*
	public static <V> List<V> executeSqlForArrayList(String query, String conName) throws  SQLException, Exception {
		List<V> result = new ArrayList<V>();		
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;

		try {	
			con = Torque.getConnection(conName);
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(query);

			while (rs.next()) {
				result.add((V) rs.getObject(1));
			}
			
			return result;
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
			Check.disposal(con);
		}
 	}
	*/
	
	/*
	public static <V> List<V> executeSqlForArrayList(String query) throws  SQLException, Exception {
		return executeSqlForArrayList(query, DEFAULT_DB_NAME);
	}
	*/
	
	/**
	 * Recreates all tables of descDB database from srcDB database. It also populates all tables in tablesNeedingInit from srcDB database.
	 * @param srcDB - database from which the destDB tables are being recreated
	 * @param destDB - database whose tables are being recreated 
	 * @param tablesNeedingInit
	 * @throws Exception
	 */
	
	/*
	public static void reCreateAlltables(String srcDB, String destDB, List<String> tablesNeedingInit) throws Exception
	{
		if(Check.isEmpty(destDB))
			return; //or throw exception
		Connection con = null;
		String startingDB = DB_TURBINE;
		   try
		   {
		   		//Get a database connection
				con = Torque.getConnection(DEFAULT_DB_NAME);
				startingDB = con.getCatalog();
				
				//set the catalog to be descDB
				con.setCatalog(destDB);
				
				
				List<String> destDBTables =  DatasourceUtil.getTables(con, destDB, null, null);
				
				reCreatetables(con, srcDB, destDB, destDBTables, tablesNeedingInit);
		   }
		   catch (Exception e)
		   {
			   String err = Check.isEmpty(e.getMessage())? "" : e.getMessage() ;
			   String msg = SayAppRes.say("message.errorRecreateTablesInDB",destDB,err);
			   throw new Exception (msg,e);
		   }
		   finally
		   {
			  if (con != null)
			  {
				  con.setCatalog(startingDB);
				  Check.disposal(con);
			  }
		   }

	}
	
	*/
	
	/**
	 * Recreates all tables listed in destDBTables in descDB database from srcDB database. It also populates all tables in tablesNeedingInit from srcDB database.
	 * @param srcDB - database from which the destDB tables are being recreated
	 * @param destDB - database whose tables are being recreated 
	 * @param destDBTables
	 * @param tablesNeedingInit
	 * @throws Exception
	 */
	
	/*
	public static void reCreatetables(String srcDB, String destDB, List<String> destDBTables, List<String> tablesNeedingInit) throws Exception
	{
		if(Check.isEmpty(destDB) || Check.isEmpty(srcDB) || Check.isEmpty(destDBTables))
			return; //or throw exception
		Connection con = null;
		String startingDB = DB_TURBINE;
		   try
		   {
				con = Torque.getConnection(DEFAULT_DB_NAME);
				startingDB = con.getCatalog();
				
				//set the catalog to be destDB
				con.setCatalog(destDB);
								
				reCreatetables(con, srcDB, destDB, destDBTables, tablesNeedingInit);
		   }
		   catch (Exception e)
		   {
			   String err = Check.isEmpty(e.getMessage())? "" : e.getMessage() ;
			   String msg = SayAppRes.say("message.errorRecreateTablesInDB",destDB,err);
			   throw new Exception (msg,e);
		   }
		   finally
		   {
			  if (con != null)
			  {
				  con.setCatalog(startingDB);
				  Check.disposal(con);
			  }
		   }

	}
	*/
	
	private  static void reCreatetables(Connection con, String srcDB, String destDB, List<String> destDBTables, List<String> tablesNeedingInit) throws Exception
	{
		if(Check.isEmpty(con))
			return; //or throw exception?
		
		for(String tableName:destDBTables)
		{
			//get the create statement of the table from the TURBINE database
			String createTurbineTable = getCreateTableStatement(con, tableName,srcDB);
			if(Check.isEmpty(createTurbineTable))
				continue;
			//Drop the table from the destDB
			dropTable(con, tableName, destDB);
			
			//Change database creation string from INNODB to MYISAM
			createTurbineTable = createTurbineTable.replaceAll(MYSQL_INNODB, MYSQL_MYISAM);	
			
			//create the table in the dbName
			createTable(con, createTurbineTable);
			
			if(tablesNeedingInit.contains(tableName))
				populateTable(con, tableName, srcDB, destDB);					
		}
	}
	private static String getCreateTableStatement(Connection con, String tableName, String database) throws Exception
	{
		String createTableStmt = "";
		Statement stmt = null;
		ResultSet result = null;
		if(con == null || Check.isEmpty(tableName) || Check.isEmpty(database))
			return createTableStmt;
		
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = "show Create Table " + database + "." + tableName;
			result = stmt.executeQuery(query);
			if(result != null && result.next())
			{
				createTableStmt = result.getString(2);
			}
		}
	   finally
	   {
		  Check.disposal(result);
		  Check.disposal(stmt);
	   }
		return createTableStmt;
	}
	private static void dropTable(Connection con, String tableName, String database) throws Exception
	{
		Statement stmt = null;
		if(con == null || Check.isEmpty(tableName) || Check.isEmpty(database))
			return;
		
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = "Drop Table If Exists  " + database + "." + tableName;
			stmt.execute(query);
		}
		catch(Exception e)
		{
			String err = e.getMessage();
			AdHocLogger.logException(e);
		}
	   finally
	   {
		  Check.disposal(stmt);
	   }
	}
	private static boolean createTable(Connection con, String createTurbineTable) throws Exception
	{
		boolean succeeded = false;
		Statement stmt = null;
		if(con == null || Check.isEmpty(createTurbineTable))
			return succeeded;
		
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			stmt.execute(createTurbineTable);
			succeeded = true;
		}
	   finally
	   {
		  Check.disposal(stmt);
	   }
		return succeeded;
	}
	private static boolean populateTable(Connection con, String tableName, String srcDB, String destDB) throws Exception
	{
		boolean succeeded = false;
		Statement stmt = null;
		if(con == null || Check.isEmpty(tableName)|| Check.isEmpty(srcDB)|| Check.isEmpty(destDB))
			return succeeded;
		
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String sql = "insert into " + destDB + "." + tableName + " (select * from " + srcDB + "." + tableName + ")";
			stmt.execute(sql);
			succeeded = true;
		}
	   finally
	   {
		  Check.disposal(stmt);
	   }
		return succeeded;
	}	

}
