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

import com.guardium.data.CveReference;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.runtest.SingletonClass;
import com.guardium.utils.ReadDumpFile;


public class CveReferenceMap {

   
   // Create a hash map
   private static HashMap hm = new HashMap();

   private static CveReferenceMap CveReferenceMapObject;
	/** A private Constructor prevents any other class from instantiating. */

   // constructor
   private CveReferenceMap () {
		initMap();
	}
	
	public static synchronized CveReferenceMap getCveReferenceMapObject() {
		if (CveReferenceMapObject == null) {
			CveReferenceMapObject = new CveReferenceMap();
		}
		return CveReferenceMapObject;
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

   public CveReference retrieveByPK (int id) {
	   return (CveReference)hm.get(id);
   }
   
   // get available test by key
   public CveReference getCveReferenceByKey (int key) {
	   CveReference t = null;

	   t = (CveReference)hm.get(key);
	   
	    return t;
   }
 
   // get available test by data source type
   public List getListByTestId(int test_id) {
	   List <CveReference> alist = new ArrayList<CveReference>();
	   
	   Iterator it = null;
	   it = hm.entrySet().iterator();

	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        CveReference t = (CveReference)pairs.getValue();
	        if (t.getTestId() == test_id) {
	        	alist.add(t);
	        }
	   }
	   return alist;
   }
   
   private static void initMap () {
	   // Read data from the xml dump
		String  resourceFile = "cve_reference.dump";
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
			Date ts = new Date();
			int testid = 0;
			String asource = "";
			String atype = "";
			String ahref = "";
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
                name = str[0];
                value = str[1];
                //System.out.println(name + "=" + value);
                
                /*
        		<table_data name="CVE_REFERENCE">
        		<row>
                <field name="CVE_REFERENCE_ID">14939</field>
                <field name="TIMESTAMP">2014-05-22 20:24:55</field>
                <field name="TEST_ID">1000</field>
                <field name="CVE_REFERENCE_SOURCE">MS</field>
                <field name="CVE_REFERENCE_TYPE">VENDOR_ADVISORY</field>
                <field name="CVE_REFERENCE_HREF">http://www.microsoft.com/technet/security/bulletin/MS01-041.asp</field>
        		</row>
                 */
                
                
                if (name.equals("cve_reference_id")) {
                	if (!value.isEmpty())
                		cid = Integer.parseInt(value);
                }
                else if  (name.equals("test_id")) {
                	if (!value.isEmpty())
                		testid = Integer.parseInt(value);
                }              
                else if  (name.equals("cve_reference_source")) {
                	if (!value.isEmpty())
                		asource = value;
                }                
                else if  (name.equals("cve_reference_type")) {
                	if (!value.isEmpty())
                		atype = value;
                }
                else if  (name.equals("cve_reference_href")) {
                	if (!value.isEmpty())
                		ahref = value;
                }                
         
			}
			// select only query based and cve tests
			
			CveReference t = new CveReference (cid, ts, testid, asource, atype, ahref);
			hm.put(cid, t);

		}
	    System.out.println("End of initMap");
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
   }
   
   
}