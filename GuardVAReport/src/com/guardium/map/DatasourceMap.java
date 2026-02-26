/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

//import com.guardium.data.AvailableTest;
//import com.guardium.data.BaseDatasource;
import com.guardium.data.CveFix;
import com.guardium.data.Datasource;
import com.guardium.data.SecurityAssessment;
//import com.guardium.data.DbDriver;
import com.guardium.data.TestResult;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.utils.ReadDumpFile;

public class DatasourceMap {
 
	private static DatasourceMap DatasourceMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private DatasourceMap () {
		//initMap();
	}
	
	public static synchronized DatasourceMap getDatasourceMapObject() {
		if (DatasourceMapObject == null) {
			DatasourceMapObject = new DatasourceMap();
			initMap();
		}
		return DatasourceMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	// create a list
	private static List <Datasource> dtlist = new ArrayList<Datasource>();
	
	public List<Datasource> getList() {
		return dtlist;
	}

	public void setList(List<Datasource> tlist) {
		this.dtlist = tlist;
	}

	public static void add (Datasource t) {
		int idx = -1;
		Iterator itr = dtlist.iterator();
		while(itr.hasNext()) {
			Datasource ds = (Datasource) itr.next();
			if (t.getName().equals(ds.getName())) {
				idx = dtlist.indexOf(ds);
				break;
			}
		}
		if (idx < 0) {
			t.setDatasourceId(currentDatasourceId);
			dtlist.add(t);
			currentDatasourceId++;
		} else {
			dtlist.set(idx, t);
		}
		return;
	}
	
	
	
	// Create a hash map
	//private static ConcurrentHashMap hm = new ConcurrentHashMap();
	private static int currentDatasourceId = 20001;
	
	
	public static int getCurrentDatasourceId () {
		return currentDatasourceId;
	}
	


	private static int datasource_id = 0;
	
	public int getDatasource_id() {
		return datasource_id;
	}


	public void setDatasource_id(int datasource_id) {
		this.datasource_id = datasource_id;
	}
	
	/*
	public static void add_old(Datasource ds) {
		ds.setDatasourceId(currentDatasourceId);
		hm.put(currentDatasourceId, ds);
		currentDatasourceId++;
	}
	*/
	
	private void createDatasource () {
		//loop until end
	    Scanner in = new Scanner(System.in);
	    
	    while (true) {

	    System.out.println("Do you want to add datasource? (y/n):");
	    String ans = in.nextLine();

	    if (ans.equalsIgnoreCase("n"))
	    	break;
		
		// init datasource infor
		int sid = 0;
		int type_id = 0; 
		String dname = ""; 
		String des = "";
		String dhost = "";
		int dport = 0;
		String sname = "";
		String uname = "";
		String psw = "";
		boolean password_stored_flag = false;
		String dbname = ""; 
		Date lconnect = new Date();
		Date ts = new Date();
		int aid = 0;
		boolean shared_flag = false;
		String constr = "";
		String os_uname = "";
		String db_dir = "";
		String curl = ""; 
		int sev = 2;
		int dbid = 0;
		String cmode = "";
		boolean use_ssl_flag = false;
		boolean import_flag = false;
	
		datasource_id++;
		sid = datasource_id;
		
		// get all datasource info
		
		System.out.println("1, ORACLE, 1521");
		System.out.println("2, DB2, 50000");
		System.out.println("3, SYBASE, 4100");
		System.out.println("4, MS SQL SERVER, 1433");
		System.out.println("5, INFORMIX, 1526");
		System.out.println("6, MYSQL, 3306");
		System.out.println("7, TEXT, 0");
		System.out.println("8, TEXT:HTTP, 8000");
		System.out.println("9, TEXT:FTP, 21");
		System.out.println("10, TEXT:SAMBA, 445");
		System.out.println("11, TEXT:HTTPS, 8443");
		System.out.println("12, TERADATA, 1025");
		System.out.println("13, N_A, 0");
		System.out.println("14, IBM ISERIES, 446");
		System.out.println("15, POSTGRESQL, 5432");
		System.out.println("16, NETEZZA, 5480");
		System.out.println("17, DB2 z/OS, 446");
		System.out.println("18, SYBASE IQ, 2638");
		System.out.println("19, GREENPLUM, 5432");
		System.out.println("20, ASTER, 2406");
		System.out.println("21, MONGODB, 27017");
		System.out.println("22, SAP HANA, 30015");

		
	    System.out.println("Enter Datasource TYPE ID:");
	    type_id = in.nextInt();
	    
	    System.out.println("Enter Datasource name:");
	    dname = in.nextLine();

	    System.out.println("Enter Datasource description:");
	    des = in.nextLine();
	    
	    System.out.println("Enter Datasource host name or IP:");
	    dhost = in.nextLine();
	    System.out.println("Enter Datasource port:");
	    dport = in.nextInt();	    
	    System.out.println("Enter Datasource service name:");
	    sname = in.nextLine();
	    System.out.println("Enter Datasource user name:");
	    uname = in.nextLine();
	    System.out.println("Enter Datasource password:");
	    psw = in.nextLine();	    
	    
	    System.out.println("Enter password stored flag (0/1):");
	    int dtmp = in.nextInt();
	    if (dtmp == 0)
	    	password_stored_flag = false;
	    else if (dtmp == 1)
	    	password_stored_flag = true;
	    System.out.println("Enter Datasource database name:");
	    dbname = in.nextLine();
	    
	    //lconnect = now();
	    //ts = now();
	    aid = 8;
	    
	    System.out.println("Enter connection property:");
	    constr = in.nextLine();    	    
	    System.out.println("Enter OS user name:");
	    os_uname = in.nextLine();
	    System.out.println("Enter Datasource database directory:");
	    db_dir = in.nextLine();
	    System.out.println("Enter connection url:");
	    curl = in.nextLine();
	    System.out.println("Enter DB driver ID:");
	    dbid = in.nextInt();
	    
	    System.out.println("Enter compatibility mode:");
	    cmode = in.nextLine();	    
	    //boolean use_ssl_flag = false;
		//boolean import_flag = false;
	    
		Datasource ds = new Datasource (currentDatasourceId, type_id, dname, des, dhost, dport,
				sname, uname, psw, password_stored_flag, dbname, 
				lconnect, ts, aid, shared_flag, constr, os_uname,
				db_dir, curl, sev, dbid, cmode, use_ssl_flag, import_flag );
		this.add(ds);
	    }
	}
	
	public Datasource getDatasourceById (int id) {		
		return (Datasource)dtlist.get(id);
	}
	
	public Datasource getDatasourceByName (String name) {
		Datasource ds = null;
		List <Datasource> dlist = getList();
		
		for (Datasource dt: dlist) {
			if (dt.getName().equals(name)) {
				return dt;
			}
		}
		return ds;
	}
	
	public void deleteDatasourceByName (String name) {
		Datasource ds = null;
		List <Datasource> dlist = getList();
		
		for (Datasource dt: dlist) {
			if (dt.getName().equals(name)) {
				dtlist.remove(dt);
				return;
			}
		}
		return;
	}
	
	public void remove (int id) {
		dtlist.remove(id);
		return;
	}
	
	public static void initMap () {
		// Read data from the xml dump
		
		// datasource is read from user directory, 
		String fileName = TestUtils.getDirectoryPath() + System.getProperty("file.separator") + TestUtils.DS_OUTPUT_FILENAME;
		
		File f = new File(fileName);
		if(!f.exists()) {
			return;
		}
		
		ReadDumpFile rdf = new ReadDumpFile();
		boolean readok = rdf.readFile(fileName);
		if (!readok) {
			return;
		}
		
		List<List> tbList = rdf.getTableList();
		String tbName = rdf.getTableName();
			
		for (List rlist: tbList) {
			//System.out.println("rlist size " + rlist.size());	

			// init datasource infor
			int sid = 0;
			int type_id = 0; 
			String dname = ""; 
			String des = "";
			String dhost = "";
			int dport = 0;
			String sname = "";
			String uname = "";
			String psw = "";
			boolean password_stored_flag = false;
			String dbname = ""; 
			Date lconnect = new Date();
			Date ts = new Date();
			int aid = 0;
			boolean shared_flag = false;
			String constr = "";
			String os_uname = "";
			String db_dir = "";
			String curl = ""; 
			int sev = 2;
			int dbid = 0;
			String cmode = "";
			boolean use_ssl_flag = false;
			boolean import_flag = false;
		
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
				name = str[0];
	            value = str[1];
	            //System.out.println(name + "=" + value);
	                
	            /*
	<row>
        <field name="DATASOURCE_ID">20001</field>
        <field name="DATASOURCE_TYPE_ID">14</field>
        <field name="NAME">avc</field>
        <field name="DESCRIPTION">desc</field>
        <field name="HOST">1.2.3.4</field>
        <field name="PORT">5678</field>
        <field name="SERVICE_NAME">sname</field>
        <field name="USER_NAME">ddd</field>
        <field name="PASSWORD">eee</field>
        <field name="PASSWORD_STORED">1</field>
        <field name="DB_NAME">dname</field>
        <field name="LAST_CONNECT">Tue Jul 29 08:25:30 PDT 2014</field>
        <field name="TIMESTAMP">Tue Jul 29 08:25:30 PDT 2014</field>
        <field name="APPLICATION_ID">8</field>
        <field name="SHARED">1</field>
        <field name="CON_PROPERTY">x=y;a=b</field>
        <field name="OS_USERNAME"></field>
        <field name="DB_HOME_DIR"></field>
        <field name="CUSTOM_URL">abc@gmai.com</field>
        <field name="SEVERITY">2</field>
        <field name="DB_DRIVER_ID">0</field>
        <field name="COMPATIBILITY_MODE"></field>
	</row>
	             */
	                
	            if (name.equals("datasource_id")) {
	               	if (!value.isEmpty())
	               		sid = Integer.parseInt(value);
	            }
	            else if  (name.equals("datasource_type_id")) {
	               	if (!value.isEmpty())
	               		type_id = Integer.parseInt(value);
	            }              
	            else if  (name.equals("name")) {
	               	if (!value.isEmpty())
	               		dname = value;
	            }                
	            else if  (name.equals("description")) {
	               	if (!value.isEmpty())
	               		des = value;
	            }
	            else if  (name.equals("host")) {
	               	if (!value.isEmpty())
	               		dhost = value;
	            }    
	            else if  (name.equals("port")) {
	               	if (!value.isEmpty())
	               		dport = Integer.parseInt(value);
	            }
	            else if  (name.equals("service_name")) {
	               	if (!value.isEmpty())
	               		sname = value;
	            }
	            else if  (name.equals("user_name")) {
	               	if (!value.isEmpty())
	               		uname = value;
	            }
	            else if  (name.equals("password")) {
	               	if (!value.isEmpty())
	               		psw = value;
	            }
	            else if  (name.equals("password_stored")) {
	               	if (!value.isEmpty()) {
	               		int tmp = Integer.parseInt(value);
	               		password_stored_flag = false;
	               		if (tmp == 1)
	               			password_stored_flag = true;
	               	}
	            }
	            else if  (name.equals("db_name")) {
	               	if (!value.isEmpty())
	               		dbname = value;
	            }    
	            else if  (name.equals("last_connect")) {
	               	if (!value.isEmpty()) {
	               		String stmp = value;
	               		//lconnect = (Date) stmp;
	               		lconnect = new Date();
	               	}
	            }    
	            else if  (name.equals("timestamp")) {
	               	if (!value.isEmpty()) {
	               		String stmp = value;
	               		// ts = (Date) stmp;
	               		ts = new Date();
	               	}
	            }    
	            else if  (name.equals("application_id")) {
	               	if (!value.isEmpty())
	               		aid = Integer.parseInt(value);
	            }    
	            else if  (name.equals("shared")) {
	               	if (!value.isEmpty()) {
	               		int tmp = Integer.parseInt(value);
	               		shared_flag = false;
	               		if (tmp == 1)
	               			shared_flag = true;
	               	}
	            }    
	            else if  (name.equals("con_property")) {
	               	if (!value.isEmpty())
	               		constr = value;
	            }    
	            else if  (name.equals("os_username")) {
	               	if (!value.isEmpty())
	               		os_uname = value;
	            }    
	            else if  (name.equals("db_home_dir")) {
	               	if (!value.isEmpty())
	               		db_dir = value;
	            }    
	            else if  (name.equals("custom_url")) {
	               	if (!value.isEmpty())
	               		curl = value;
	            }  
	            else if  (name.equals("severity")) {
	               	if (!value.isEmpty())
	               		sev = Integer.parseInt(value);
	            }    
	            else if  (name.equals("db_driver_id")) {
	               	if (!value.isEmpty())
	               		dbid = Integer.parseInt(value);
	            }  
	            else if  (name.equals("compatibility_mode")) {
	               	if (!value.isEmpty())
	               		cmode = value;
	            }  
	        }
							
			// create datasource
			Datasource ds = new Datasource (currentDatasourceId, type_id, dname, des, dhost, dport,
					sname, uname, psw, password_stored_flag, dbname, 
					lconnect, ts, aid, shared_flag, constr, os_uname,
					db_dir, curl, sev, dbid, cmode, use_ssl_flag, import_flag );
			add(ds);
		

		}
		/*
		System.out.println("End of initMap");
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		//System.out.println("hm size is " + hm.size());
		 */
		return;
	}
	   
}
