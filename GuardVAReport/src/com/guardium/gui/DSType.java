/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.guardium.data.Datasource;
import com.guardium.data.DbDriver;
import com.guardium.map.DbDriverMap;

public class DSType {

	public DSType (){
		initTypeList();
	}
	
	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DSType dd = new DSType();
		//dd.initTypeList();
		String st = DSTYPE_DEFAULT;
		
		int ret = dd.getDsTypeIndex(st);
		System.out.println("type index for " + st + " is " + ret);
		st = DSTYPE_MSSQL;
		ret = dd.getDsTypeIndex(st);
		System.out.println("type index for " + st + " is " + ret);
		
	}
	*/
	
	/*
	final static String DSTYPE_DEFAULT = "--------";
	final static String DSTYPE_DB2 = "DB2";
	final static String DSTYPE_DB2I = "DB2 for i";
	final static String DSTYPE_DB2ZOS = "DB2 z/OS";
	final static String DSTYPE_INFORMIX = "Informix";
	final static String DSTYPE_MSSQL = "MS SQL Server";
	final static String DSTYPE_MSSQLDD = "MS SQL Server (DataDirect)";
	final static String DSTYPE_NETEZZA = "Netezza";
	final static String DSTYPE_ORACLEDDSN = "Oracle (DataDirect - Service Name)";
	final static String DSTYPE_ORACLEDDSID = "Oracle (DataDirect - SID)";
	final static String DSTYPE_ORACLESN = "Oracle(Service Name)";
	final static String DSTYPE_ORACLESID = "Oracle(SID)";
	final static String DSTYPE_SYBASE = "Sybase";
	final static String DSTYPE_SYBASEIQ = "Sybase IQ";
	final static String DSTYPE_TERADATA = "Teradata";
    */
	
	DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
	// create a list
	private static List <String> dsTypeList = new ArrayList<String>();

	public List <String> getDSTypeList () {
		return dsTypeList;
	}
	
	public void addToTypeList (String t) {
		dsTypeList.add(t);
		return;
	}
	
	private void initTypeList () {
		// read list from map, if map has been updated, the list should be updated.
		
		List <DbDriver> drlist = DbDriverPeer.getList();
		for (DbDriver s : drlist) {
			// supported datasource type id
			// 1 - oracle
			// 2 - db2
			// 3 - sybase
			// 4 - ms sql server
			// 5  - informix
			// 12 - Teradata
			// 16 - Netezza
			// 18 - Sybase IQ
			
			int drid = s.getDatasourceTypeId();
			
	    	// check in the support ds type
	        if( Arrays.asList(1,2,3,4,5,12,16,18).contains(drid)) {
	        	addToTypeList(s.getName());
	        }
	    }
		
		//dsTypeList.
		Collections.sort(dsTypeList.subList(0, dsTypeList.size()));
	}
	
	public int getDsTypeIndex (String st) {
		return dsTypeList.indexOf(st);
	}
	
	public DbDriver getDbDriver (String str) {
		
		DbDriver dd = null;
		dd = DbDriverPeer.getDbDriverByName(str);		
		return dd;
	}
	
	public int[] getDsTypeId (String str) {


		     int [] retarr;
		     
		     retarr = new int[2];

		     int typeid = 0;
		     int driverid = 0;

			 DbDriver sdriver = DbDriverPeer.getDbDriverByName(str);
			 
			 if (sdriver != null) {
				 typeid = sdriver.getDatasourceTypeId();
				 driverid = sdriver.getDbDriverId();
			 }
			 			 
		     retarr[0] = typeid;
		     retarr[1] = driverid;
	    	 return retarr;
	   }
	   
	   public String getDsTypeString (int driverid) {
		 String str = "";
		 DbDriver sdriver = DbDriverPeer.getDbDriverById(driverid);
		 
		 
		 if (sdriver != null) {
			 str = sdriver.getName();
		 }
		 return str;
	 }
	  
}
