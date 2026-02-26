/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

import com.guardium.data.AvailableTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.runtest.SingletonClass;
import com.guardium.utils.ReadDumpFile;


public class AvailableTestMap {

   final public static int QUERY_TEST_TYPE = 4;
   final public static int CVE_TEST_TYPE = 6;
   
   private static List<Integer> excluded_id_list;
   
   private void createExculdeList () {
	   excluded_id_list = new ArrayList <Integer>();
	   excluded_id_list.add(2267);
	   excluded_id_list.add(2268);
	   excluded_id_list.add(2269);
	   excluded_id_list.add(2270);
	   excluded_id_list.add(2271);
	   excluded_id_list.add(2272);
   }
   
   private boolean checkInExcludeList (int id) {
	   return excluded_id_list.contains(id);
   }
   
   // Create a hash map
   private static HashMap hm_query = new HashMap();
   private static HashMap hm_cve = new HashMap();
     
   private static AvailableTestMap AvailableTestMapObject;
	/** A private Constructor prevents any other class from instantiating. */

   // constructor
   private AvailableTestMap () {
	   //createExculdeList ();
	   initMap();
   }
	
   
   public static synchronized AvailableTestMap getAvailableTestMapObject() {
		if (AvailableTestMapObject == null) {
			AvailableTestMapObject = new AvailableTestMap();
		}
		return AvailableTestMapObject;
   }
	
   public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
   }
	
   
   public int getQueryTestType () {
	   return QUERY_TEST_TYPE;
   }
   
   public int getCveTestType () {
	   return CVE_TEST_TYPE;
   }  
   
   public void printMap (int typeid) {
	   System.out.println("printMap start for type " + typeid);
	   // Get a set of the entries
	   
	   Set set = null;
	   if (typeid == QUERY_TEST_TYPE) {
		   set = hm_query.entrySet();
	   }
	   else if (typeid == CVE_TEST_TYPE) {
		   set = hm_cve.entrySet();
	   }	   

	   // Get an iterator
	   Iterator i = set.iterator();
	   // Display elements
	   while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(me.getKey() + ": ");
	         System.out.println(me.getValue());
	   }
	   System.out.println("End of printMap"); 
   }
   
   public int getMapSize (int typeid) {
	   int siz = 0;
	   if (typeid == QUERY_TEST_TYPE) {
		   siz = hm_query.size();
	   }
	   else if (typeid == CVE_TEST_TYPE) {
		   siz = hm_cve.size();
	   }
	   return siz;   
   }

   public int getQueryMapSize () {
	   return hm_query.size();   
   }
   
   public int getCveMapSize () {
	   return hm_cve.size();   
   }  
   
   public AvailableTest retrieveByPK (int id) {
	   AvailableTest t = null;
	   t = getAvailableTestByKey(QUERY_TEST_TYPE, id);
	   if (t == null) {
		   t = getAvailableTestByKey(CVE_TEST_TYPE, id);
	   }
	   return t;
   }
   
   // get available test by key
   public AvailableTest getAvailableTestByKey (int typeid, int key) {
	   AvailableTest t = null;
	   if (typeid == QUERY_TEST_TYPE) {
		   t = (AvailableTest)hm_query.get(key);
	   }
	   else if (typeid == CVE_TEST_TYPE) {
		   t = (AvailableTest)hm_cve.get(key);
	   }
	   
	    return t;
   }
 
   // get available test by data source type
   public List<AvailableTest> getTestListByDsType(int test_type_id, int datasource_typeid) {
	   List <AvailableTest> alist = new ArrayList<AvailableTest>();
	   
	   Iterator it = null;
	   if (test_type_id == QUERY_TEST_TYPE) {
		   
		   Map<Integer, AvailableTest> sortedMap = new TreeMap<Integer,AvailableTest >(hm_query);
		   it = sortedMap.entrySet().iterator();
	   }
	   else if (test_type_id == CVE_TEST_TYPE) {
		   Map<Integer, AvailableTest> sortedMap2 = new TreeMap<Integer,AvailableTest >(hm_cve);
		   it = sortedMap2.entrySet().iterator();
	   }
	   
	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        AvailableTest t = (AvailableTest)pairs.getValue();
	        if (t.getDatasourceTypeId() == datasource_typeid) {
	        	alist.add(t);
	        }
	   }
	   return alist;
   }
   
   private static void initMap () {
	   //System.out.println("initMap in the AvailableTest class");
	   // Read data from the xml dump
	   
		String  resourceFile = "avail_test.dump";
		TestUtils tutils = new TestUtils();
		String fileName = tutils.readDataFile(resourceFile);
		if (fileName.isEmpty()) {
			return;
		}
		
		ReadDumpFile rdf = new ReadDumpFile();
		boolean readok = rdf.readFile(fileName);
		tutils.removeDataFile(fileName);
		if (!readok) {
			return;
		}

	   List<List> tbList = rdf.getTableList();
	   String tbName = rdf.getTableName();
		
	   for (List rlist: tbList) {
			//System.out.println("rlist size " + rlist.size());	
			
			// init all fields
			int id = 0;
			String desc = "";
			int rid = -1;
			String clsname = ""; 
			byte ttype = 0;
			int audit_id = -1;
			int dtypeid = -1; 
			boolean thold = false;
			String tprompt = ""; 
			double dtvalue = 0.0;
			String sver = "INFO";
			String cname = ""; 
			String ts = ""; 
			String sdesc = "";
			String extref = "";
			String os_name = "";
			boolean excp_flag = false;
			double app_from_ver = 0.0;
			double app_to_ver = 0.0;
			String stig_ref = "";
			String stig_severity = "";
			String stig_iacontrols = "";
			String stig_srg = "";
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
                name = str[0];
                value = str[1];
                //System.out.println(name + "=" + value);
                
                if (name.equals("test_id")) {
                	if (!value.isEmpty())
                		id = Integer.parseInt(value);
                }
                else if  (name.equals("test_desc")) {
                	if (!value.isEmpty())
                		desc = value;
                }
                else if  (name.equals("report_id")) {
                	if (!value.isEmpty())
                		rid = Integer.parseInt(value);
                }               
                else if  (name.equals("class_name")) {
                	if (!value.isEmpty())
                		clsname = value;
                }                
                else if  (name.equals("test_type")) {
                	if (!value.isEmpty())
                		ttype = (byte)Integer.parseInt(value);
                }     
                else if  (name.equals("audit_config_template_id")) {
                	if (!value.isEmpty())
                		audit_id = Integer.parseInt(value);
                }                           
                else if  (name.equals("datasource_type_id")) {
                	if (!value.isEmpty())
                	dtypeid = Integer.parseInt(value);
                }    
                else if  (name.equals("threshold_required")) {
                	if (!value.isEmpty()) {
                		int tmp = Integer.parseInt(value);
                		if (tmp == 0) 
                			thold = false;
                		else 
                			thold = true;
                	}
                }                   
                else if  (name.equals("threshold_prompt")) {
                	if (!value.isEmpty())
                		tprompt = value;
                }                 		
                else if  (name.equals("default_threshold_value")) {
                	if (!value.isEmpty())
                		dtvalue = Double.parseDouble(value);
                }
                else if  (name.equals("severity")) {
                	if (!value.isEmpty())
                		sver = value;
                }                 
                else if  (name.equals("category_name")) {
                	if (!value.isEmpty())
                		cname = value;
                }                     
                else if  (name.equals("timestamp")) {
                	if (!value.isEmpty())
                		ts = value;
                }                		
                else if  (name.equals("short_description")) {
                	if (!value.isEmpty())
                		sdesc = value;
                }		
                else if  (name.equals("external_reference")) {
                	if (!value.isEmpty())
                		extref = value;
                }
                else if  (name.equals("os")) {
                	if (!value.isEmpty())
                		os_name = value;
                }
                else if  (name.equals("can_have_exceptions_group")) {
                	if (!value.isEmpty()) {
                		int tmp = Integer.parseInt(value);
                		if (tmp == 0) 
                			excp_flag = false;
                		else 
                			excp_flag = true;
                	}
                }                      
                else if  (name.equals("applicable_from_version")) {
                	if (!value.isEmpty())
                		app_from_ver = Double.parseDouble(value);
                }
                else if  (name.equals("applicable_to_version")) {
                	if (!value.isEmpty())
                		app_to_ver = Double.parseDouble(value);
                }
                else if  (name.equals("stig_reference")) {
                	if (!value.isEmpty())
                		stig_ref = value;
                }
                else if  (name.equals("stig_severity")) {
                	if (!value.isEmpty())
                		stig_severity = value;
                }
                else if  (name.equals("stig_iacontrols")) {
                	if (!value.isEmpty())
                		stig_iacontrols = value;
                }
                else if  (name.equals("stig_srg")) {
                	if (!value.isEmpty())
                		stig_srg = value;
                }
			}
			// select only query based and cve tests
			
			AvailableTest t = new AvailableTest (id, desc, rid, clsname, ttype,
				       audit_id, dtypeid, thold, tprompt, dtvalue, sver,
				       cname, ts, sdesc, extref, os_name, excp_flag,
				       app_from_ver, app_to_ver, stig_ref, stig_severity, stig_iacontrols, stig_srg);
			if (ttype == QUERY_TEST_TYPE) {
				hm_query.put(id, t);
			}
			else if (ttype == CVE_TEST_TYPE) {
				hm_cve.put(id, t);
			}
		}
	    /*
	    System.out.println("End of initMap");
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm query size is " + hm_query.size());
		System.out.println("hm cve size is " + hm_cve.size());
 		*/
   }
   
   
}