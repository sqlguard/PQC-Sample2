/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.guardium.data.AvailableTest;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;

public class DatasourceTypeMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DatasourceTypeMap vmap = new DatasourceTypeMap ();
		
		List <DatasourceType> mlist = new ArrayList<DatasourceType>();
		mlist = vmap.getList();
		
		for (DatasourceType s : mlist) {
			s.dump();
	    }
		
		
		int id = 2;
		System.out.println("\nGet data for Datasource type id " + id);
	    DatasourceType dt = vmap.getDatasourceType(id);
	    if (dt != null)
	    	dt.dump();
       
	    System.out.println("\nMap size " + vmap.getMapSize());
	    
	}

	// constructor
	public DatasourceTypeMap () {
		if (!initFlag) {
			initMap();
			initFlag = true;
		}
	}
	
	private static boolean initFlag = false;
	
	public int getMapSize() {
		return dtlist.size();
	}
	
	private static List <DatasourceType> dtlist = new ArrayList<DatasourceType>();
	
	public List<DatasourceType> getList() {
		return dtlist;
	}

	public void setList(List<DatasourceType> tlist) {
		this.dtlist = tlist;
	}

	public static void addList (DatasourceType t) {
		dtlist.add(t);
		return;
	}
	
	private static void initMap () {
		      
		// Put elements to the list
		Date date = new Date();

		DatasourceType t = new DatasourceType(1, "ORACLE", 1521, 1, date);
		addList(t);
		t = new DatasourceType(2, "DB2", 50000, 2, date);
		addList(t);
		t = new DatasourceType(3, "SYBASE", 4100, 3, date);
		addList(t);
		t = new DatasourceType(4, "MS SQL SERVER", 1433, 4, date);
		addList(t);
		t = new DatasourceType(5, "INFORMIX", 1526, 5, date);
		addList(t);
		t = new DatasourceType(6, "MYSQL", 3306, 6, date);
		addList(t);
		t = new DatasourceType(7, "TEXT", 0, 7, date);
		addList(t);
		t = new DatasourceType(8, "TEXT:HTTP", 8000, 8, date);
		addList(t);
		t = new DatasourceType(9, "TEXT:FTP", 21, 9, date);
		addList(t);
		t = new DatasourceType(10, "TEXT:SAMBA", 445, 10, date);
		addList(t);
		t = new DatasourceType(11, "TEXT:HTTPS", 8443, 11, date);
		addList(t);
		t = new DatasourceType(12, "TERADATA", 1025, 12, date);
		addList(t);	
		t = new DatasourceType(13, "N_A", 0, 13, date);
		addList(t);
		t = new DatasourceType(14, "IBM ISERIES", 446, 14, date);
		addList(t);
		t = new DatasourceType(15, "POSTGRESQL", 5432, 15, date);
		addList(t);
		t = new DatasourceType(16, "NETEZZA", 5480, 16, date);
		addList(t);
		t = new DatasourceType(17, "DB2 z/OS", 446, 17, date);
		addList(t);
		t = new DatasourceType(18, "SYBASE IQ", 2638, 18, date);
		addList(t);
		t = new DatasourceType(19, "GREENPLUM", 5432, 19, date);
		addList(t);
		t = new DatasourceType(20, "ASTER", 2406, 20, date);
		addList(t);
		t = new DatasourceType(21, "MONGODB", 27017, 21, date);
		addList(t);
		t = new DatasourceType(22, "SAP HANA", 30015, 22, date);
		addList(t);		      
	}
	
	public DatasourceType getDatasourceType (int id) {
		for (DatasourceType s : dtlist) {
			if (s.getDatasourceTypeId() == id) {
				return s;
			}
	    }
		return null;
	}
	/*
	 * mysql> select * from DATASOURCE_TYPE;
+--------------------+---------------+--------------+-------------------+---------------------+
| DATASOURCE_TYPE_ID | NAME          | DEFAULT_PORT | DEFAULT_DRIVER_ID | TIMESTAMP           |
+--------------------+---------------+--------------+-------------------+---------------------+
|                  1 | ORACLE        |         1521 |                 1 | 2014-05-31 03:08:23 |
|                  2 | DB2           |        50000 |                 2 | 2014-05-31 03:08:23 |
|                  3 | SYBASE        |         4100 |                 3 | 2014-05-31 03:08:23 |
|                  4 | MS SQL SERVER |         1433 |                 4 | 2014-05-31 03:08:23 |
|                  5 | INFORMIX      |         1526 |                 5 | 2014-05-31 03:08:23 |
|                  6 | MYSQL         |         3306 |                 6 | 2014-05-31 03:08:23 |
|                  7 | TEXT          |            0 |                 7 | 2014-05-31 03:08:23 |
|                  8 | TEXT:HTTP     |         8000 |                 8 | 2014-05-31 03:08:23 |
|                  9 | TEXT:FTP      |           21 |                 9 | 2014-05-31 03:08:23 |
|                 10 | TEXT:SAMBA    |          445 |                10 | 2014-05-31 03:08:23 |
|                 11 | TEXT:HTTPS    |         8443 |                11 | 2014-05-31 03:08:23 |
|                 12 | TERADATA      |         1025 |                12 | 2014-05-31 03:08:23 |
|                 13 | N_A           |            0 |                13 | 2014-05-31 03:08:23 |
|                 14 | IBM ISERIES   |          446 |                14 | 2014-05-31 03:08:23 |
|                 15 | POSTGRESQL    |         5432 |                15 | 2014-05-31 03:08:23 |
|                 16 | NETEZZA       |         5480 |                16 | 2014-05-31 03:08:23 |
|                 17 | DB2 z/OS      |          446 |                17 | 2014-05-31 03:08:23 |
|                 18 | SYBASE IQ     |         2638 |                18 | 2014-05-31 03:08:23 |
|                 19 | GREENPLUM     |         5432 |                19 | 2014-05-31 03:08:23 |
|                 20 | ASTER         |         2406 |                20 | 2014-05-31 03:08:23 |
|                 21 | MONGODB       |        27017 |                21 | 2014-05-31 03:08:23 |
|                 22 | SAP HANA      |        30015 |                22 | 2014-05-31 03:08:23 |
+--------------------+---------------+--------------+-------------------+---------------------+
22 rows in set (0.00 sec)


	 */
}
