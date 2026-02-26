/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import javax.swing.JOptionPane;

//import com.guardium.data.Datasource;
//import com.guardium.data.DatasourceType;
import com.guardium.data.DbDriver;
import com.guardium.gui.TestUtils;
//import com.guardium.gui.VATest;
import com.guardium.utils.ReadDumpFile;

public class DbDriverMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DbDriverMap vmap = new DbDriverMap ();
		
		List <DbDriver> mlist = new ArrayList<DbDriver>();
		System.out.println("1 map size is " + vmap.getMapSize());
		mlist = vmap.getList();
		

		for (DbDriver s : mlist) {
			System.out.println("");
			s.dump();
	    }

		vmap.setDriverOracleStored(); 
		vmap.setDriverMsServerStored();
		
		System.out.println("2 map size is " + vmap.getMapSize());
		for (DbDriver s : mlist) {
			System.out.println("");
			s.dump();
	    }		
		
		vmap.savetofile ();
        /*		
		List <DbDriver> tlist = new ArrayList<DbDriver>();
		int type_id = 1;
		tlist = vmap.getDbDriverByType(type_id);
		System.out.println("\nList driver wiht type id - " + type_id);
		
		for (DbDriver s : tlist) {
			System.out.println("");
			s.dump();
	    }

		type_id = 2;
		tlist = vmap.getDbDriverByType(type_id);
		System.out.println("\nList driver wiht type id - " + type_id);
		
		for (DbDriver s : tlist) {
			System.out.println("");
			s.dump();
	    }
		
		type_id = 25;
		tlist = vmap.getDbDriverByType(type_id);
		System.out.println("\nList driver wiht type id - " + type_id);
		
		for (DbDriver s : tlist) {
			System.out.println("");
			s.dump();
	    }
		*/
		//DbDriverMap vmap2 = new DbDriverMap ();
		//System.out.println("2 map size is " + vmap2.getMapSize());
	}
	
	private static DbDriverMap DbDriverMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private DbDriverMap () {

	}
		
	public static synchronized DbDriverMap getDbDriverMapObject() {
		if (DbDriverMapObject == null) {
			DbDriverMapObject = new DbDriverMap();
			initMap();
		}
		return DbDriverMapObject;
	}
		
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
				
	private static boolean initFlag = false;
	// new driver id start at 100
	private static int newDriverID = 100;
	
	private static List <DbDriver> dtlist = new ArrayList<DbDriver>();
	
	public int getMapSize() {
		return dtlist.size();
	}
	public List<DbDriver> getList() {
		return dtlist;
	}

	public void setList(List<DbDriver> tlist) {
		this.dtlist = tlist;
	}

	public static void addDbDriver (int driver_id, int ds_type_id, String driver_name, String driver_class, String driver_url, String driver_base_url, boolean driver_stored) {

		DbDriver t = new DbDriver();
		

		// if driver_id not equal to 0, use the input one
		// if equal to 0, means it is a new driver, set up new driver id
		if (driver_id == 0) {
			newDriverID++;
			t.setDbDriverId(newDriverID);
		}
		else {
			t.setDbDriverId(driver_id);
		}

		
		//t.setDbDriverId(driver_id);
		t.setDatasourceTypeId(ds_type_id);
		t.setName(driver_name);
		t.setDriverClass(driver_class);
		t.setUrlTemplate(driver_url);
		t.setBaseUrlTemplate(driver_base_url);
		t.setDriverStored(driver_stored);
		Date date = new Date();
		t.setTimestamp(date);
	    dtlist.add(t);
	}
	/*
	private static void initMap_saved () {
		//System.out.println("do we got here..................");
		// Put elements to the list
		
		addDbDriver(1,1,"Oracle (DataDirect - SID)","com.ibm.guardium.jdbc.oracle.OracleDriver","jdbc:guardium:oracle://<HOST>:<PORT>;SID=<SERVICE_NAME>","jdbc:guardium:oracle://<HOST>:<PORT>");	
		addDbDriver(2,2,"DB2","com.ibm.db2.jcc.DB2Driver","jdbc:db2://<HOST>:<PORT>/<SERVICE_NAME>","jdbc:db2://<HOST>:<PORT>/<SERVICE_NAME>");	
		addDbDriver(3,3,"Sybase","com.sybase.jdbc3.jdbc.SybDriver","jdbc:sybase:Tds:<HOST>:<PORT>/<DB_NAME>","jdbc:sybase:Tds:<HOST>:<PORT>");		
		addDbDriver(4,4,"MS SQL SERVER (DataDirect)","com.ibm.guardium.jdbc.sqlserver.SQLServerDriver","jdbc:guardium:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>","jdbc:guardium:sqlserver://<HOST>:<PORT>");		
		addDbDriver(5,5,"Informix","com.informix.jdbc.IfxDriver","jdbc:informix-sqli://<HOST>:<PORT>/<DB_NAME>:informixserver=<DBSERVER_NAME>;DB_LOCALE=<LOCALE>","jdbc:informix-sqli://<HOST>:<PORT>/sysmaster:informixserver=<DBSERVER_NAME>");	
		addDbDriver(6,6,"MySQL","org.gjt.mm.mysql.Driver","jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?autoReconnect=true","jdbc:mysql://<HOST>:<PORT>?autoReconnect=true");		
		addDbDriver(7,7,"TEXT","com.hxtt.sql.text.TextDriver","jdbc:csv:////var/dump","jdbc:csv:////var/dump");		
		addDbDriver(8,8,"TEXT:HTTP","com.hxtt.sql.text.TextDriver","jdbc:csv:http://<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true","jdbc:csv:http://<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true");		
		addDbDriver(9,9,"TEXT:FTP","com.hxtt.sql.text.TextDriver","jdbc:csv:ftp://<USER>:<PASSWD>@<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true","jdbc:csv:ftp://<USER>:<PASSWD>@<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true");		
		addDbDriver(10,10,"TEXT:SAMBA","com.hxtt.sql.text.TextDriver","jdbc:csv:smb://<USER>:<PASSWD>@<HOST><PORT>/<SERVICE_NAME>/","jdbc:csv:smb://<USER>:<PASSWD>@<HOST><PORT>/<SERVICE_NAME>/");		
		addDbDriver(11,11,"TEXT:HTTPS","com.hxtt.sql.text.TextDriver","jdbc:csv:https://<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true","jdbc:csv:https://<HOST><PORT>/<SERVICE_NAME>?tmpdir=/usr/local/jakarta-tomcat/webapps/ROOT/dynamic/images;ignoreDirtyData=true");		
		addDbDriver(12,12,"TERADATA","com.teradata.jdbc.TeraDriver","jdbc:teradata://<HOST>/DBS_PORT=<PORT>,DATABASE=<DB_NAME>","jdbc:teradata://<HOST>/DBS_PORT=<PORT>");		
		addDbDriver(13,13,"NA","none","none","none");		
		addDbDriver(14,14,"IBM ISERIES","com.ibm.as400.access.AS400JDBCDriver","jdbc:as400://<HOST>:<PORT>/<DB_NAME>","jdbc:as400://<HOST>:<PORT>/<DB_NAME>");		
		addDbDriver(15,15,"PostgreSQL","org.postgresql.Driver","jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>","jdbc:postgresql://<HOST>");		
		addDbDriver(16,16,"Netezza","org.netezza.Driver","jdbc:netezza://<HOST>:<PORT>/<DB_NAME>","jdbc:netezza://<HOST>/<DB_NAME>");		
		addDbDriver(17,17,"DB2 z/OS","com.ibm.db2.jcc.DB2Driver","jdbc:db2://<HOST>:<PORT>/<SERVICE_NAME>","jdbc:db2://<HOST>:<PORT>/<SERVICE_NAME>");		
		addDbDriver(18,18,"Sybase IQ","com.sybase.jdbc3.jdbc.SybDriver","jdbc:sybase:Tds:<HOST>:<PORT>/<DB_NAME>","jdbc:sybase:Tds:<HOST>:<PORT>");		
		addDbDriver(19,19,"Greenplum","org.postgresql.Driver","jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>","jdbc:postgresql://<HOST>");		
		addDbDriver(20,20,"Aster","com.asterdata.ncluster.Driver","jdbc:ncluster://<HOST>:<PORT>/<DB_NAME>","jdbc:ncluster://<HOST>/<DB_NAME>");
		addDbDriver(21,21,"Mongodb","","","");
		addDbDriver(22,1,"Oracle (DataDirect - Service Name)","com.ibm.guardium.jdbc.oracle.OracleDriver","jdbc:guardium:oracle://<HOST>:<PORT>;ServiceName=<SERVICE_NAME>","jdbc:guardium:oracle://<HOST>:<PORT>");
		addDbDriver(23,22,"SAP HANA","com.sap.db.jdbc.Driver","jdbc:sap://<HOST>:<PORT>/?autocommit=false","jdbc:sap://<HOST>:<PORT>/?autocommit=false");
		// oracle/jdbc/driver/OracleDriver.class
		// "jdbc:oracle:thin:hr/hr@//localhost:1521/orcl.oracle.com
		
		// Oracle(Service Name)
		addDbDriver(24,1,"Oracle(Service Name)","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>/<SERVICE_NAME>","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>");
		// Oracle(SID)
		addDbDriver(25,1,"Oracle(SID)","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>;SID=<SERVICE_NAME>","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>");
		
		// MS SQL SERVER
		// jtds-1.2.8.jar
		//addDbDriver(4,"Oracle (thin)","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>/<SERVICE_NAME>","jdbc:oracle:thin:hr/hr@//<HOST>:<PORT>");
		//addDbDriver(4,"MS SQL SERVER (DataDirect)","com.ibm.guardium.jdbc.sqlserver.SQLServerDriver","jdbc:guardium:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>","jdbc:guardium:sqlserver://<HOST>:<PORT>");
		addDbDriver(26,4,"MS SQL SERVER","net.sourceforge.jtds.jdbc.Driver","jdbc:jtds:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>","jdbc:jtds:sqlserver://<HOST>:<PORT>");
	}
	*/
	public boolean IsDriverMsServerStored () {
		/*
		db driver id: 31
		db type id: 4
		db driver name: MS SQL Server
		db driver class: net.sourceforge.jtds.jdbc.Driver
		db driver url template:      jdbc:jtds:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>
		db driver base url template: jdbc:jtds:sqlserver://<HOST>:<PORT>
		db driver timestamp: Wed Oct 15 20:14:36 PDT 2014
		*/
        boolean stored = false;
	    String name = "MS SQL Server";
		for (DbDriver s : dtlist) {
			if (s.getName().equalsIgnoreCase(name)) {
				stored = s.isDriverStored();
				break;
			}
	    }	    	    
	    return stored;
	}
	
	
	public boolean IsDriverOracleStored () {
		/*
		db driver id: 31
		db type id: 4
		db driver name: MS SQL Server
		db driver class: net.sourceforge.jtds.jdbc.Driver
		db driver url template:      jdbc:jtds:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>
		db driver base url template: jdbc:jtds:sqlserver://<HOST>:<PORT>
		db driver timestamp: Wed Oct 15 20:14:36 PDT 2014
		*/
        boolean stored = false;
		String name1 = "Oracle (SID)";
		String name2 = "Oracle (Service Name)";

		for (DbDriver s : dtlist) {
			if (s.getName().equalsIgnoreCase(name1)) {
				stored = s.isDriverStored();
				break;
			}
	    }	    	    
	    return stored;
	}
	
	// use by GUI to add Oracle driver
	public void setDriverOracleStored () {
		/*
		db driver id: 30
		db type id: 1
		db driver name: Oracle(SID)
		db driver class: oracle.jdbc.driver.OracleDriver
		db driver url template:      jdbc:oracle:thin:@<HOST>:<PORT>:<SERVICE_NAME>
		db driver base url template: jdbc:oracle:thin:@<HOST>:<PORT>:<SERVICE_NAME>
		db driver timestamp: Wed Oct 15 20:14:36 PDT 2014
		*/
		
		String name1 = "Oracle (SID)";
		String name2 = "Oracle (Service Name)";

		for (DbDriver s : dtlist) {
			if (s.getName().equalsIgnoreCase(name1) || s.getName().equalsIgnoreCase(name2)) {
				s.setDriverStored(true);
			}
	    }
			    
	    savetofile ();
	}		

	// use by GUI to add ms server driver
	public void setDriverMsServerStored () {
		/*
		db driver id: 31
		db type id: 4
		db driver name: MS SQL Server
		db driver class: net.sourceforge.jtds.jdbc.Driver
		db driver url template:      jdbc:jtds:sqlserver://<HOST>:<PORT>;DatabaseName=<DB_NAME>
		db driver base url template: jdbc:jtds:sqlserver://<HOST>:<PORT>
		db driver timestamp: Wed Oct 15 20:14:36 PDT 2014
		*/

	    String name = "MS SQL Server";
		for (DbDriver s : dtlist) {
			if (s.getName().equalsIgnoreCase(name)) {
				s.setDriverStored(true);
				break;
			}
	    }
	    	    
	    savetofile ();
	}

	
	public List<DbDriver> getDbDriverByType (int datasource_type_id) {
		List <DbDriver> rlist = new ArrayList<DbDriver>();
		for (DbDriver s : dtlist) {
			if (s.getDatasourceTypeId() == datasource_type_id) {				
				rlist.add(s);
			}
	    }
		return rlist;
	}
	
	public DbDriver getDbDriverById (int db_driver_id) {
		for (DbDriver s : dtlist) {
			if (s.getDbDriverId() == db_driver_id) {
				return s;
			}
	    }
		return null;
	}	

	
	public DbDriver getDbDriverByName (String name) {
		for (DbDriver s : dtlist) {
			if (s.getName().equalsIgnoreCase(name)) {
				return s;
			}
	    }
		return null;
	}	
	
	public static void initMap () {
		//System.out.println("initMap for the DbDriver class");
		// Read data from the xml dump
		String  resourceFile = "dbdriver.dump";
		
		String dfileName = TestUtils.getDirectoryPath() + System.getProperty("file.separator") + resourceFile;
		
		ReadDumpFile rdf = new ReadDumpFile();
		boolean readok = true;
		
		File f = new File(dfileName);
		if(f.exists()) {
			readok = rdf.readFile(dfileName);
			if (!readok) {
				return;
			}
		}
		else {
			// read from the source
			TestUtils tutils = new TestUtils();
			String fileName = tutils.readDataFile(resourceFile);
			if (fileName.isEmpty()) {
				return;
			}
		
			readok = rdf.readFile(fileName);
			// move .dump_new to .dump
			// don't know why we move this file here, we delete others. 
			//tutils.moveDataFile(fileName, dfileName);
			tutils.removeDataFile(fileName);
			if (!readok) {
				return;
			}
		}
	
		// init the list
		dtlist = new ArrayList<DbDriver>();
		List<List> tbList = rdf.getTableList();
		String tbName = rdf.getTableName();
			
		for (List rlist: tbList) {
			//System.out.println("rlist size " + rlist.size());	

			// init dbdriver infor
			int driver_id = 0;
			int type_id = 0;
			String db_driver_name ="";
			String driver_class = "";
			String url_template = "";
			String base_url_template = "";
			boolean stored_flag = false;
			//timestamp = ts;
			
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
                <field name="DB_DRIVER_ID">21</field>
                <field name="DATASOURCE_TYPE_ID">21</field>
                <field name="NAME">Mongodb</field>
                <field name="DRIVER_CLASS"></field>
                <field name="URL_TEMPLATE"></field>
                <field name="BASE_URL_TEMPLATE"></field>
                <field name="DRIVER_STORED">1</field>
                <field name="TIMESTAMP">2014-10-15 07:23:03</field>
        		</row>

	             */
	                
	            if (name.equalsIgnoreCase("DB_DRIVER_ID")) {
	               	if (!value.isEmpty())
	               		driver_id = Integer.parseInt(value);
	            }
	            else if  (name.equals("datasource_type_id")) {
	               	if (!value.isEmpty())
	               		type_id = Integer.parseInt(value);
	            }              
	            else if  (name.equals("name")) {
	               	if (!value.isEmpty())
	               		db_driver_name = value;
	            }                
	            else if  (name.equals("driver_class")) {
	               	if (!value.isEmpty())
	               		driver_class = value;
	            }
	            else if  (name.equals("url_template")) {
	               	if (!value.isEmpty())
	               		url_template = value;
	            }    
	            else if  (name.equals("base_url_template")) {
	               	if (!value.isEmpty())
	               		base_url_template = value;
	            }
	            else if  (name.equals("driver_stored")) {
	               	if (!value.isEmpty()) {
	               		int tmp = Integer.parseInt(value);
	               		stored_flag = false;
	               		if (tmp == 1)
	               			stored_flag = true;
	               	}
	            }
	        }
							
			// create db driver
			addDbDriver (driver_id, type_id, db_driver_name, driver_class, url_template, base_url_template, stored_flag);
		}
		/*
		System.out.println("End of initMap");
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("dtlist size is " + dtlist.size());
		*/
		return;
	}
	   
	
	public void savetofile () {
		   
			//for (DbDriver s : dtlist) {

			String  resourceFile = "dbdriver.dump";
			if (dtlist.size() <= 0) {
		        //No datasource type defined
		        return;
			}

		   String filename = TestUtils.getDirectoryPath() +System.getProperty("file.separator")  + resourceFile;
		   BufferedWriter output = null;
		   output = createfile(filename);
		   writeHeader(output);
		   writeList(output);
		   writeEnd(output);
		   
	}
	   
	   private BufferedWriter createfile (String filename) {
		   BufferedWriter output = null;
		   try {
			   File file = new File(filename);
			   output = new BufferedWriter(new FileWriter(file));

		   } catch ( IOException e ) {
			   e.printStackTrace();
		   }	   	   
		   return output;
	   }
	   
	   
	   private void writeHeader(BufferedWriter output) {
	   	   
	/*
	<?xml version="1.0"?>
	<mysqldump xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<database name="TURBINE">
        <table_structure name="DB_DRIVER">
                <field Field="DB_DRIVER_ID" Type="int(11)" Null="NO" Key="PRI" Default="0" Extra="" Comment="" />
                <field Field="DATASOURCE_TYPE_ID" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
                <field Field="NAME" Type="varchar(50)" Null="NO" Key="" Default="" Extra="" Comment="" />
                <field Field="DRIVER_CLASS" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
                <field Field="URL_TEMPLATE" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
                <field Field="BASE_URL_TEMPLATE" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
                <field Field="TIMESTAMP" Type="timestamp" Null="NO" Key="" Default="CURRENT_TIMESTAMP" Extra="on update CURRENT_TIMESTAMP" Comment="" />
                <key Table="DB_DRIVER" Non_unique="0" Key_name="PRIMARY" Seq_in_index="1" Column_name="DB_DRIVER_ID" Collation="A" Cardinality="26" Null="" Index_type="BTREE" Comment="" Index_comment="" />
                <key Table="DB_DRIVER" Non_unique="0" Key_name="DB_DRIVER_ID" Seq_in_index="1" Column_name="DB_DRIVER_ID" Collation="A" Null="" Index_type="BTREE" Comment="" Index_comment="" />
                <key Table="DB_DRIVER" Non_unique="0" Key_name="DB_DRIVER_ID" Seq_in_index="2" Column_name="DATASOURCE_TYPE_ID" Collation="A" Cardinality="26" Null="" Index_type="BTREE" Comment="" Index_comment="" />
                <options Name="DB_DRIVER" Engine="MyISAM" Version="10" Row_format="Dynamic" Rows="26" Avg_row_length="159" Data_length="4140" Max_data_length="281474976710655" Index_length="12288" Data_free="0" Create_time="2014-10-10 14:33:48" Update_time="2014-10-10 16:45:46" Collation="utf8_general_ci" Create_options="" Comment="" />
        </table_structure>
        <table_data name="DB_DRIVER">
        <row>
                <field name="DB_DRIVER_ID">1</field>
                <field name="DATASOURCE_TYPE_ID">1</field>
                <field name="NAME">Oracle (DataDirect - SID)</field>
                <field name="DRIVER_CLASS">com.ibm.guardium.jdbc.oracle.OracleDriver</field>
                <field name="URL_TEMPLATE">jdbc:guardium:oracle://&lt;HOST&gt;:&lt;PORT&gt;;SID=&lt;SERVICE_NAME&gt;</field>
                <field name="BASE_URL_TEMPLATE">jdbc:guardium:oracle://&lt;HOST&gt;:&lt;PORT&gt;</field>
                <field name="DRIVER_STORED">1</field>
                <field name="TIMESTAMP">2014-10-10 14:33:43</field>
        </row>


	 */
		   

		   try {
	    	   String text = "<?xml version=\"1.0\"?>\n";
	           output.write(text);
	           text = "<mysqldump xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
	           output.write(text);
	           text = "<database name=\"TURBINE\">\n";
	           output.write(text); 
	           String str = "DB_DRIVER";
	           text = "<table_structure name=\"" + str + "\">\n";
	           output.write(text); 
	           /*
               <field Field="DB_DRIVER_ID" Type="int(11)" Null="NO" Key="PRI" Default="0" Extra="" Comment="" />
               <field Field="DATASOURCE_TYPE_ID" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
               <field Field="NAME" Type="varchar(50)" Null="NO" Key="" Default="" Extra="" Comment="" />
               <field Field="DRIVER_CLASS" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
               <field Field="URL_TEMPLATE" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
               <field Field="BASE_URL_TEMPLATE" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
               <field Field="TIMESTAMP" Type="timestamp" Null="NO" Key="" Default="CURRENT_TIMESTAMP" Extra="on update CURRENT_TIMESTAMP" Comment="" />
               <key Table="DB_DRIVER" Non_unique="0" Key_name="PRIMARY" Seq_in_index="1" Column_name="DB_DRIVER_ID" Collation="A" Cardinality="26" Null="" Index_type="BTREE" Comment="" Index_comment="" />
               <key Table="DB_DRIVER" Non_unique="0" Key_name="DB_DRIVER_ID" Seq_in_index="1" Column_name="DB_DRIVER_ID" Collation="A" Null="" Index_type="BTREE" Comment="" Index_comment="" />
               <key Table="DB_DRIVER" Non_unique="0" Key_name="DB_DRIVER_ID" Seq_in_index="2" Column_name="DATASOURCE_TYPE_ID" Collation="A" Cardinality="26" Null="" Index_type="BTREE" Comment="" Index_comment="" />
               <options Name="DB_DRIVER" Engine="MyISAM" Version="10" Row_format="Dynamic" Rows="26" Avg_row_length="159" Data_length="4140" Max_data_length="281474976710655" Index_length="12288" Data_free="0" Create_time="2014-10-10 14:33:48" Update_time="2014-10-10 16:45:46" Collation="utf8_general_ci" Create_options="" Comment="" />
       			</table_structure>
       
	            */	           
	           text = "\t<field Field=\"DB_DRIVER_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"PRI\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DATASOURCE_TYPE_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"MUL\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);          
	           text = "\t<field Field=\"NAME\" Type=\"varchar(50)\" Null=\"NO\" Key=\"UNI\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DRIVER_CLASS\" Type=\"varchar(255)\" Null=\"NO\" Key=\"\" Default=\"\"  Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"URL_TEMPLATE\" Type=\"varchar(255)\" Null=\"NO\" Key=\"\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"BASE_URL_TEMPLATE\" Type=\"varchar(255)\" Null=\"NO\" Key=\"\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DRIVER_STORED\" Type=\"int(1)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"TIMESTAMP\" Type=\"timestamp\" Null=\"NO\" Key=\"\" Default=\"CURRENT_TIMESTAMP\" Extra=\"on update CURRENT_TIMESTAMP\" Comment=\"\" />\n";
	           output.write(text);
           
	           text = "</table_structure>\n";
	           output.write(text); 

	           
		   } catch ( IOException e ) {
	            e.printStackTrace();
		   }	   
	       
	       
	       
	   }
	   
	   private String formstr (String fname, boolean use_string, String svalue, int dvalue) {
		   String ret = "";
	       String sfield = "<field ";
	       String mfield = ">";
	       String efield = "</field>";
	       String namefield = "name=";
	       String dquote = "\"";
	       if (use_string) {
	    	   ret = "\t" + sfield + namefield + dquote + fname + dquote + mfield + svalue + efield + "\n";
	       }
	       else {
	    	   ret = "\t" + sfield + namefield + dquote + fname + dquote + mfield + dvalue + efield + "\n";
	       }
		   return ret;
	   }
	   
	   
	   private String formurl (String str) {
		   // <HOST>
		   // &lt;HOST&gt;
		   String nstr = str.replaceAll("<", "&lt;");
		   nstr = nstr.replaceAll(">", "&gt;");
	        System.out.println(nstr);
	        
	       return nstr;
	   }
	   
	   private void writeList(BufferedWriter output) {
		   /*
			<table_data name="DB_DRIVER">
        	<row>
                <field name="DB_DRIVER_ID">1</field>
                <field name="DATASOURCE_TYPE_ID">1</field>
                <field name="NAME">Oracle (DataDirect - SID)</field>
                <field name="DRIVER_CLASS">com.ibm.guardium.jdbc.oracle.OracleDriver</field>
                <field name="URL_TEMPLATE">jdbc:guardium:oracle://&lt;HOST&gt;:&lt;PORT&gt;;SID=&lt;SERVICE_NAME&gt;</field>
                <field name="BASE_URL_TEMPLATE">jdbc:guardium:oracle://&lt;HOST&gt;:&lt;PORT&gt;</field>
                <field name="DRIVER_STORED">1</field>
                <field name="TIMESTAMP">2014-10-10 14:33:43</field>
        	</row>
			</table_data>
		    */

		   	int dsize = dtlist.size();
		   	System.out.println("DB Driver list size is " + dsize);
		   
		   	try {
	    	   String text = "<table_data name=\"DB_DRIVER\">\n";
	           output.write(text);
	           
	           // list
	   		   for (DbDriver ds2: dtlist) {
	   			   text = "<row>\n";
	   			   output.write(text);
	   			   
	   			   //System.out.println("\n\nDatasource info:");
	   			   //ds2.dump();
	   			   
	   			   String fname = "";
	   			   String svalue = "";
	   			   int dvalue = 0;
	   			   
	   			   fname = "DB_DRIVER_ID";
	   			   dvalue = ds2.getDbDriverId();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "DATASOURCE_TYPE_ID";
	   			   dvalue = ds2.getDatasourceTypeId();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);  			   
	   			   
	   			   fname = "NAME";
	   			   svalue = ds2.getName();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "DRIVER_CLASS";
	   			   svalue = ds2.getDriverClass();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "URL_TEMPLATE";
	   			   svalue = ds2.getUrlTemplate();
	   			   //String tmp = "&quot;" + svalue + "&quot;";
	   			   String tmp = formurl(svalue);
	   			   text = formstr(fname, true, tmp, 0);
	   			   output.write(text);  			   
	 
	   			   fname = "BASE_URL_TEMPLATE";
	   			   svalue = ds2.getBaseUrlTemplate();
	   			   //tmp = "&quot;" + svalue + "&quot;";
	   			   tmp = formurl(svalue);
	   			   text = formstr(fname, true, tmp, 0);
	   			   output.write(text); 

	   			   fname = "DRIVER_STORED";
	   			   if (ds2.isDriverStored())
	   				   dvalue = 1;
	   			   else
	   				   dvalue = 0;
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "TIMESTAMP";
	   			   svalue = ds2.getTimestamp().toString();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   text = "</row>\n";
	   			   output.write(text);
	   		   }
			           
	           
	           text = "</table_data>\n";
	           output.write(text);           

		    } catch ( IOException e ) {
	            e.printStackTrace();
		    }	   
		   
	   }
	   
	   private void writeEnd (BufferedWriter output) {
		   /*
			</database>
			</mysqldump>
		   */
		   
		   try {
	    	   String text = "</database>\n";
	           output.write(text);
	           text = "</mysqldump>\n";
	           output.write(text);           
	           output.close();
		   } catch ( IOException e ) {
	            e.printStackTrace();
		   }	   
	  	   	   
	   }
}
