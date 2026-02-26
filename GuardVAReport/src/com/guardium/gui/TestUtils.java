/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.guardium.data.Datasource;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DbDriverMap;

// to run DsocMain
//import com.guardium.dsoc.DsocMain;

public class TestUtils {
	
		private static String DS_OUTPUT_PATH;
		public static String DS_OUTPUT_FILENAME = "datasource.dump";
	    public static String LOADED_DRIVER_ORACLE = "loaded_oracle.jar";
	    public static String LOADED_DRIVER_MSSERVER = "loaded_msserver.jar";
	    
	   public void setDirectoryPath (String dir) {
		   // if we need to set
		   DS_OUTPUT_PATH = dir;
		   return;
	   }

	   public static String getDirectoryPath(){
		   // set the directory for vatest output directory
		   // if not there, create one
		   if (System.getProperty("os.name").startsWith("Windows")) {
	    	    // includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
			   DS_OUTPUT_PATH = "c:\\temp\\vatest";
			   
		   }else{
			   DS_OUTPUT_PATH = "/var/tmp/vatest";
		   }
		   		   
			File f = new File(DS_OUTPUT_PATH);
			if(!f.exists()) {
				f.mkdirs();
			}

		   return DS_OUTPUT_PATH;
	   }
	   
	   public int savetofile () {
		   // create Datasource, put in Datasource list
			DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
			
			List<Datasource> datasourceList = new ArrayList<Datasource>();
			datasourceList = DatasourcePeer.getList();

			int dsize = datasourceList.size();
			System.out.println("DS list size is " + dsize);

			
			if (dsize <= 0) {
		        // JOptionPane.showMessageDialog(null, "No datasource defined. Please define the datasources to continue.");
		        return dsize;
			}
			
		  // String filename = VATest.VAOutputDir + "datasource.dump";
		   BufferedWriter output = null;
		   output = createfile(DS_OUTPUT_PATH +System.getProperty("file.separator")+ DS_OUTPUT_FILENAME);
		   writeHeader(output);
		   writeList(output);
		   writeEnd(output);
		   return dsize;
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
	        <table_structure name="DATASOURCE">
	                <field Field="DATASOURCE_ID" Type="int(11)" Null="NO" Key="PRI" Extra="auto_increment" Comment="" />
	                <field Field="DATASOURCE_TYPE_ID" Type="int(11)" Null="NO" Key="MUL" Default="0" Extra="" Comment="" />
	                <field Field="NAME" Type="varchar(50)" Null="NO" Key="UNI" Default="" Extra="" Comment="" />
	                <field Field="DESCRIPTION" Type="varchar(255)" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="HOST" Type="varchar(255)" Null="NO" Key="" Default="" Extra="" Comment="" />
	                <field Field="PORT" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
	                <field Field="SERVICE_NAME" Type="varchar(255)" Null="YES" Key="" Default="" Extra="" Comment="" />
	                <field Field="USER_NAME" Type="varchar(50)" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="PASSWORD" Type="mediumblob" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="PASSWORD_STORED" Type="int(1)" Null="NO" Key="" Default="0" Extra="" Comment="" />
	                <field Field="DB_NAME" Type="varchar(60)" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="LAST_CONNECT" Type="datetime" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="TIMESTAMP" Type="timestamp" Null="NO" Key="" Default="CURRENT_TIMESTAMP" Extra="on update CURRENT_TIMESTAMP" Comment="" />
	                <field Field="APPLICATION_ID" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
	                <field Field="SHARED" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
	                <field Field="CON_PROPERTY" Type="mediumtext" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="OS_USERNAME" Type="varchar(128)" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="DB_HOME_DIR" Type="mediumtext" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="CUSTOM_URL" Type="mediumtext" Null="YES" Key="" Extra="" Comment="" />
	                <field Field="SEVERITY" Type="int(11)" Null="NO" Key="" Default="2" Extra="" Comment="" />
	                <field Field="DB_DRIVER_ID" Type="int(11)" Null="NO" Key="" Default="0" Extra="" Comment="" />
	                <field Field="COMPATIBILITY_MODE" Type="varchar(50)" Null="YES" Key="" Default="" Extra="" Comment="" />
	                <key Table="DATASOURCE" Non_unique="0" Key_name="PRIMARY" Seq_in_index="1" Column_name="DATASOURCE_ID" Collation="A" Cardinality="295" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <key Table="DATASOURCE" Non_unique="0" Key_name="NAME" Seq_in_index="1" Column_name="NAME" Collation="A" Cardinality="295" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <key Table="DATASOURCE" Non_unique="1" Key_name="DATASOURCE_TYPE_ID" Seq_in_index="1" Column_name="DATASOURCE_TYPE_ID" Collation="A" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <options Name="DATASOURCE" Engine="MyISAM" Version="10" Row_format="Dynamic" Rows="295" Avg_row_length="151" Data_length="44648" Max_data_length="281474976710655" Index_length="28672" Data_free="0" Auto_increment="20295" Create_time="2014-07-17 21:40:34" Update_time="2014-07-18 16:55:10" Check_time="2014-07-17 21:55:59" Collation="utf8_general_ci" Create_options="" Comment="" />
	        </table_structure>
	        <table_data name="DATASOURCE">

	 */
		   

		   try {
	    	   String text = "<?xml version=\"1.0\"?>\n";
	           output.write(text);
	           text = "<mysqldump xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
	           output.write(text);
	           text = "<database name=\"TURBINE\">\n";
	           output.write(text); 
	           String str = "DATASOURCE";
	           text = "<table_structure name=\"" + str + "\">\n";
	           output.write(text); 

	           text = "\t<field Field=\"DATASOURCE_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"PRI\" Extra=\"auto_increment\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DATASOURCE_TYPE_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"MUL\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);          
	           text = "\t<field Field=\"NAME\" Type=\"varchar(50)\" Null=\"NO\" Key=\"UNI\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DESCRIPTION\" Type=\"varchar(255)\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"HOST\" Type=\"varchar(255)\" Null=\"NO\" Key=\"\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"PORT\" Type=\"int(11)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"SERVICE_NAME\" Type=\"varchar(255)\" Null=\"YES\" Key=\"\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text =  "\t<field Field=\"USER_NAME\" Type=\"varchar(50)\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"PASSWORD\" Type=\"mediumblob\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"PASSWORD_STORED\" Type=\"int(1)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DB_NAME\" Type=\"varchar(60)\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"LAST_CONNECT\" Type=\"datetime\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"TIMESTAMP\" Type=\"timestamp\" Null=\"NO\" Key=\"\" Default=\"CURRENT_TIMESTAMP\" Extra=\"on update CURRENT_TIMESTAMP\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"APPLICATION_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"SHARED\" Type=\"int(11)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"CON_PROPERTY\" Type=\"mediumtext\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"OS_USERNAME\" Type=\"varchar(128)\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DB_HOME_DIR\" Type=\"mediumtext\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"CUSTOM_URL\" Type=\"mediumtext\" Null=\"YES\" Key=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"SEVERITY\" Type=\"int(11)\" Null=\"NO\" Key=\"\" Default=\"2\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"DB_DRIVER_ID\" Type=\"int(11)\" Null=\"NO\" Key=\"\" Default=\"0\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	           text = "\t<field Field=\"COMPATIBILITY_MODE\" Type=\"varchar(50)\" Null=\"YES\" Key=\"\" Default=\"\" Extra=\"\" Comment=\"\" />\n";
	           output.write(text);
	 
	           /*
	                <key Table="DATASOURCE" Non_unique="0" Key_name="PRIMARY" Seq_in_index="1" Column_name="DATASOURCE_ID" Collation="A" Cardinality="295" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <key Table="DATASOURCE" Non_unique="0" Key_name="NAME" Seq_in_index="1" Column_name="NAME" Collation="A" Cardinality="295" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <key Table="DATASOURCE" Non_unique="1" Key_name="DATASOURCE_TYPE_ID" Seq_in_index="1" Column_name="DATASOURCE_TYPE_ID" Collation="A" Null="" Index_type="BTREE" Comment="" Index_comment="" />
	                <options Name="DATASOURCE" Engine="MyISAM" Version="10" Row_format="Dynamic" Rows="295" Avg_row_length="151" Data_length="44648" Max_data_length="281474976710655" Index_length="28672" Data_free="0" Auto_increment="20295" Create_time="2014-07-17 21:40:34" Update_time="2014-07-18 16:55:10" Check_time="2014-07-17 21:55:59" Collation="utf8_general_ci" Create_options="" Comment="" />

	            */
	           
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
	   
	   private void writeList(BufferedWriter output) {
		   /*
	        <table_data name="DATASOURCE">	    
	        <row>
	                <field name="DATASOURCE_ID">20000</field>
	                <field name="DATASOURCE_TYPE_ID">4</field>
	                <field name="NAME">DPS: MSSQL2012 FAIL on wi8ku2x64t2-va Datadirect</field>
	                <field name="DESCRIPTION"></field>
	                <field name="HOST">wi8ku2x64t2-va.guard.swg.usma.ibm.com</field>
	                <field name="PORT">1433</field>
	                <field name="SERVICE_NAME"></field>
	                <field name="USER_NAME">sqlguard-user</field>
	                <field name="PASSWORD">il^FJ^QY����#��^?<9c>s&amp;^M��������</field>
	                <field name="PASSWORD_STORED">1</field>
	                <field name="DB_NAME"></field>
	                <field name="LAST_CONNECT">2014-03-07 14:35:59</field>
	                <field name="TIMESTAMP">2014-03-07 19:35:59</field>
	                <field name="APPLICATION_ID">8</field>
	                <field name="SHARED">1</field>
	                <field name="CON_PROPERTY"></field>
	                <field name="OS_USERNAME"></field>
	                <field name="DB_HOME_DIR">&quot;C:\Program Files\Microsoft SQL Server\MSSQL11.SQLSERVER2012\MSSQL&quot;</field>
	                <field name="CUSTOM_URL"></field>
	                <field name="SEVERITY">2</field>
	                <field name="DB_DRIVER_ID">4</field>
	                <field name="COMPATIBILITY_MODE"></field>
	        </row>

		   </table_data>
		   */
		   
			// create Datasource, put in Datasource list
			DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
			
			List<Datasource> datasourceList = new ArrayList<Datasource>();
			datasourceList = DatasourcePeer.getList();

			int dsize = datasourceList.size();
			System.out.println("DS list size is " + dsize);

		   
		   try {
	    	   String text = "<table_data name=\"DATASOURCE\">\n";
	           output.write(text);
	           
	           // list
	   		   for (Datasource ds2: datasourceList) {
	   			   text = "<row>\n";
	   			   output.write(text);
	   			   
	   			   //System.out.println("\n\nDatasource info:");
	   			   //ds2.dump();
	   			   
	   			   String fname = "";
	   			   String svalue = "";
	   			   int dvalue = 0;
	   			   
	   			   fname = "DATASOURCE_ID";
	   			   dvalue = ds2.getDatasourceId();
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
	   			   
	   			   fname = "DESCRIPTION";
	   			   svalue = ds2.getDescription();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "HOST";
	   			   svalue = ds2.getHost();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);  			   
	 
	   			   fname = "PORT";
	   			   dvalue = ds2.getPort();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text); 
	   			   
	   			   fname = "SERVICE_NAME";
	   			   svalue = ds2.getServiceName();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);  
	   			   
	   			   fname = "USER_NAME";
	   			   svalue = ds2.getUserName();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);  
	   			   
	   			   // don't save the password into file
	   			   fname = "PASSWORD";
	   			   svalue = "";		// password not set, don't use ds2.getPassword();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);  
	   			   
	   			   fname = "PASSWORD_STORED";
	   			   if (ds2.getPasswordStored())
	   				   dvalue = 1;
	   			   else
	   				   dvalue = 0;
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "DB_NAME";
	   			   svalue = ds2.getDbName();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "LAST_CONNECT";
	   			   svalue = ds2.getLastConnect().toString();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			      			   
	   			   fname = "TIMESTAMP";
	   			   svalue = ds2.getTimestamp().toString();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);

	   			   fname = "APPLICATION_ID";
	   			   dvalue = ds2.getApplicationId();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "SHARED";
	   			   dvalue = 1;
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "CON_PROPERTY";
	   			   svalue = ds2.getConProperty();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "OS_USERNAME";
	   			   svalue = ds2.getOsUsername();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	   			   
	   			   fname = "DB_HOME_DIR";
	   			   svalue = ds2.getDbHomeDir();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	  
	   			   fname = "CUSTOM_URL";
	   			   svalue = ds2.getCustomUrl();
	   			   text = formstr(fname, true, svalue, 0);
	   			   output.write(text);
	  
	   			   fname = "SEVERITY";
	   			   dvalue = ds2.getSeverity();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "DB_DRIVER_ID";
	   			   dvalue = ds2.getDbDriverId();
	   			   text = formstr(fname, false, "", dvalue);
	   			   output.write(text);
	   			   
	   			   fname = "COMPATIBILITY_MODE";
	   			   svalue = ds2.getCompatibility_mode();
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
	   
	   public static String  readDataFile (String name) {
			  
			// Read data from the xml dump
			String fileName = TestUtils.getDirectoryPath() +System.getProperty("file.separator")  + name;
			String newfileName = fileName + "_new";
			
	    	String res = "/com/guardium/resource/" + name;
	    	System.out.println("readDataFile " + res);
	    	
	    	// to run AppMain
			InputStream ins = AppMain.class.getResourceAsStream(res);
	    	
			// to run DsosMain
	    	// InputStream ins = DsocMain.class.getResourceAsStream(res);
	    	
			if (ins != null) { 
		        try {
			        //System.out.println ("get resource OK " + res);
			    	//System.out.println("input stream size is " + ins.available());
			    		saveDataFile(ins, fileName);		// encrypted data file
			    		ins.close();
			    		
			    		// need to decrypt
			        	try {
			        		Crypto.decrypt(new FileInputStream(fileName), new FileOutputStream(newfileName));
			        		removeDataFile(fileName);
			        	}
			        	catch (FileNotFoundException e) {
			        		System.out.println("File Not Found:" + e.getMessage());
			        		removeDataFile(fileName);
			        		return "";
			        	}
			        	catch (Exception e) {
			        		System.out.println("Invalid Key:" + e.getMessage());
			        		removeDataFile(fileName);
			        		return "";
			        	}
		    	} catch (Exception ex) {
			        System.out.println("Attempted to read a bad input stream: ");
			        removeDataFile(fileName);
			        return "";
			    }        
			} else {
				System.out.println("get resource null " + res);
				return "";
			}					
			
		   return newfileName;
	   }
	   
	   public static void removeDataFile (String name) {
			File f = new File(name);
			if(f.exists()) {
				f.delete();
			}
	   }

	   public static void moveDataFile (String name, String newname) {
			File f = new File(name);
			File nf = new File(newname);

			if (f.exists()) {
				if(nf.exists()) {
					nf.delete();
				}
				f.renameTo(nf);
			}
	   }	   
	   
		//public void saveFormFile(FormFile file, String savePath) throws IOException
	   public static void saveDataFile(InputStream stream, String name) throws IOException
		{
			//InputStream stream = file.getInputStream();
			//write the file
			OutputStream bos = new FileOutputStream(name);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = stream.read(buffer, 0, 8192)) != -1)
			{
				  bos.write(buffer, 0, bytesRead);
			}
			bos.close();
			stream.close();
		}


		public static void addURL(URL u) throws IOException {
	        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	        Class sysclass = URLClassLoader.class;
	        try {
	            Method method = sysclass.getDeclaredMethod("addURL", new Class[] {URL.class});
	            method.setAccessible(true);
				method.invoke(sysloader, new Object[] {u});
			} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
	    }

		public boolean loadJarToAppPath(File jarFile, String driverType){
			boolean isSuccess = true;
			System.out.println("Loading jar");
			
	    	try {
	    		addURL(jarFile.toURI().toURL());
	    		//Class c = Class.forName("a.A");
	    	}
	    	catch (IOException e) {
				isSuccess = false;
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return isSuccess;
		}	
		
		public void checkLoadDriver () {
			DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
			String dbName = "";
			
			
			// Using the fixed file name and save in the our saved directory
			// so we can loaded when application is up
			String outdir = getDirectoryPath();
			String outfile = "";
			File fileJar;

			if (DbDriverPeer.IsDriverMsServerStored()) {
				dbName = "MS SQL SERVER";
				outfile = LOADED_DRIVER_MSSERVER;
				fileJar = new File (outdir + System.getProperty("file.separator") + outfile);
				if (loadJarToAppPath(fileJar, dbName)) {
					System.out.println("Load ms sql server driver successfully");
				}
				else {
					System.out.println("Load ms sql server driver failed");
				}
			}
			
			if (DbDriverPeer.IsDriverOracleStored()) {
				dbName = "Oracle";
				outfile = LOADED_DRIVER_ORACLE;
				fileJar = new File (outdir + System.getProperty("file.separator") + outfile);
				if (loadJarToAppPath(fileJar, dbName)) {
					System.out.println("Load oracle driver successfully");
				}
				else {
					System.out.println("Load oracle driver failed");
				}
			}
			
		}
	   
}
