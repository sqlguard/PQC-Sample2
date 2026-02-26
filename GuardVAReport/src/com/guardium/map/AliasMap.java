/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.guardium.data.Alias;
import com.guardium.data.AssessmentLog;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.utils.ReadDumpFile;

public class AliasMap {
	
	
	public static void main(String args[]) {
		AliasMap vmap = new AliasMap();
		
		int key = 1;
		Alias t = vmap.getAliasById(key);

		int gtid = 0;
		if (t != null) {
			String sd = t.getAliasValue();

			System.out.println("id " + key + " alias value is: " + sd);

			gtid = t.getGroupTypeId();
			System.out.println("id " + key + " group type id is: " + gtid);
		}
		else {
			System.out.println("id " + key + " Alias is null");
		}
		
		List<String> ll = new ArrayList<String>();
		ll = vmap.getGroupTypeList(gtid);
		System.out.println ("\nPrint out the alias value list for: " + gtid);
		for (String dd : ll) {
			System.out.println(dd + " ");
		}
		System.out.println("\nalias value list size is " + ll.size());
		
		System.out.println("\nalias map size is " + vmap.getMapSize());
	}
	
	   
	// Create a hash map
	private static HashMap hm = new HashMap();

	private static AliasMap AliasMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private AliasMap () {
		initMap();
	}
		
	public static synchronized AliasMap getAliasMapObject() {
			if (AliasMapObject == null) {
				AliasMapObject = new AliasMap();
			}
			return AliasMapObject;
	}
	
	public int getMapSize() {
		return hm.size();
	}
	
	public Alias getAliasById(int key) {
		if (hm.containsKey(key)) {
			Alias t = (Alias) hm.get(key);
			return t;
		}
		else {
			return null;
		}
	}
	
	public Map getAliases (int grouptype) {
		return (Map)getGroupTypeList(grouptype);
	}
	
	public List<String> getGroupTypeList(int gtid) {

		List<String> ll = new ArrayList<String>();
		String str = "";
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    Alias t = (Alias)pairs.getValue();
		    if (t.getGroupTypeId() == gtid) {
		    	str = t.getAliasValue();
		    	ll.add(str);
		    }
		}
		return ll;
	}
	
	private static void initMap() {
		// Read data from the xml dump
		String  resourceFile = "alias.dump";
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

		for (List rlist : tbList) {
			//System.out.println("rlist size " + rlist.size());

			// init all fields		    
			int aid = 0;
			int gtid = 0;
			String dval = "";
			String aval = "";
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String[]) rlist.get(i);
				String name = null;
				String value = null;
				name = str[0];
				value = str[1];
				
				if (name.equals("alias_id")) {
					if (!value.isEmpty())
						aid = Integer.parseInt(value);
				} else if (name.equals("group_type_id")) {
					if (!value.isEmpty())
						gtid = Integer.parseInt(value);
				} else if (name.equals("db_value")) {
					if (!value.isEmpty())
						dval = value;
				} else if (name.equals("alias_value")) {
					if (!value.isEmpty())
						aval = value;
				}
			}
								
			Alias t = new Alias(aid, gtid, dval, aval); 
			
			hm.put(aid, t);

		}

		/*
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
	}
}