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

import com.guardium.data.CveFix;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.runtest.SingletonClass;
import com.guardium.utils.ReadDumpFile;


public class CveFixMap {

   
   // Create a hash map
   private static HashMap hm = new HashMap();

   private static CveFixMap CveFixMapObject;
	/** A private Constructor prevents any other class from instantiating. */

   // constructor
   private CveFixMap () {
		initMap();
	}
	
	public static synchronized CveFixMap getCveFixMapObject() {
		if (CveFixMapObject == null) {
			CveFixMapObject = new CveFixMap();
		}
		return CveFixMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	   
   public void printMap () {
	   System.out.println("printMap start");

	   // Get an iterator
	   Iterator i = hm.entrySet().iterator();
	   // Display elements
	   while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(me.getKey() + ": ");
	         System.out.println(me.getValue());
	   }
	   System.out.println("End of printMap"); 
   }
   
   public int getMapSize () {
	   return hm.size();   
   }

   public CveFix retrieveByPK (int id) {
	   return (CveFix)hm.get(id);
   }
   
   // get available test by key
   public CveFix getCveFixByKey (int key) {
	   CveFix t = null;

	   t = (CveFix)hm.get(key);
	   
	    return t;
   }
 
   // get available test by data source type
   public List<CveFix> getListByTestId(int test_id) {
	   List <CveFix> alist = new ArrayList<CveFix>();
	   
	   Iterator it = null;
	   it = hm.entrySet().iterator();

	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        CveFix t = (CveFix)pairs.getValue();
	        if (t.getAvailableTestId() == test_id) {
	        	alist.add(t);
	        }
	   }
	   return alist;
   }
   
   private static void initMap () {
	   //System.out.println("initMap in the CveFix class");
	   // Read data from the xml dump
		String  resourceFile = "cve_fix.dump";
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
			int cid = 0;
			int testid = 0;
			String aver = "";
			String apat = "";
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
                name = str[0];
                value = str[1];
                //System.out.println(name + "=" + value);
                
                /*
                <field name="CVE_FIX_ID">9720</field>
                <field name="AVAILABLE_TEST_ID">1000</field>
                <field name="VERSION">8.00</field>
                <field name="PATCH">384</field>
                <field name="PATCH_TO"></field>
                 */
                
                
                if (name.equals("cve_fix_id")) {
                	if (!value.isEmpty())
                		cid = Integer.parseInt(value);
                }
                else if  (name.equals("available_test_id")) {
                	if (!value.isEmpty())
                		testid = Integer.parseInt(value);
                }              
                else if  (name.equals("version")) {
                	if (!value.isEmpty())
                		aver = value;
                }                
                else if  (name.equals("patch")) {
                	if (!value.isEmpty())
                		apat = value;
                }     

                else if  (name.equals("patch_to")) {
                	if (!value.isEmpty())
                		apat = value;
                }                    
			}
			// select only query based and cve tests
			
			CveFix t = new CveFix (cid, testid, aver, apat);
			hm.put(cid, t);

		}
	    /*
	    System.out.println("End of initMap");
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
   }
   
   
}