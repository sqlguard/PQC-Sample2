/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
//for cachedRowSet
public class OracleMetaDataWrapper implements ResultSetMetaData {
    private ResultSetMetaData meta;
	OracleMetaDataWrapper(ResultSetMetaData meta){
		this.meta = meta;
	}
	public int getColumnCount() throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnCount();
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isAutoIncrement(column);
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isCaseSensitive(column);
	}

	public boolean isSearchable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isSearchable(column);
	}

	public boolean isCurrency(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isCurrency(column);
	}

	public int isNullable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isNullable(column);
	}

	public boolean isSigned(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isSigned(column);
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnDisplaySize(column);
	}

	public String getColumnLabel(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnLabel(column);
	}

	public String getColumnName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnName(column);
	}

	public String getSchemaName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getSchemaName(column);
	}

	public int getPrecision(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getPrecision(column);
	}

	public int getScale(int column) throws SQLException {
		// TODO Auto-generated method stub
		int result = meta.getScale(column);
		if (result<0)
			return 0;
		else
			return result; 
	}

	public String getTableName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getTableName(column);
	}

	public int getColumnType(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnType(column);

		
	}
	public String getCatalogName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getCatalogName(column);
	}

	public String getColumnTypeName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnTypeName(column);
	}

	public boolean isReadOnly(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isReadOnly(column);
	}

	public boolean isWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isWritable(column);
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.isDefinitelyWritable(column);
	}

	public String getColumnClassName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return meta.getColumnClassName(column);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {

		return meta.isWrapperFor(arg0);
	}
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		
		return meta.unwrap(arg0);
	}

}
