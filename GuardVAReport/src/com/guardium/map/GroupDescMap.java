/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.guardium.data.AvailableTest;
import com.guardium.data.DbDriver;
import com.guardium.data.GroupDesc;
import com.guardium.data.SecurityAssessment;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.utils.ReadDumpFile;

public class GroupDescMap {
	
	
	public static void main(String args[]) {
		GroupDescMap vmap = new GroupDescMap();

		// vmap.process();


		int key = 1;
		GroupDesc t = vmap.getGroupDescById(key);

		if (t != null) {
			String sd = t.getGroupDescription();

			System.out.println("id " + key + " group desc is: " + sd);

			String loopdbs = t.getGroupContentType();
			System.out.println("id " + key + " content type is: " + loopdbs);
		}
		else {
			System.out.println("id " + key + " GroupDesc is null");
		}
		
		vmap.dumpMap();
		System.out.println("\ngroup desc map size is " + vmap.getMapSize());
	}

	
	private static GroupDescMap GroupDescMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private GroupDescMap () {

	}
		
	public static synchronized GroupDescMap getGroupDescMapObject() {
		if (GroupDescMapObject == null) {
			GroupDescMapObject = new GroupDescMap();
			initMap();
		}
		return GroupDescMapObject;
	}
		
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
			
	// Create a hash map
	private static HashMap hm = new HashMap();

	private static boolean initFlag = false;

	public int getMapSize() {
		return hm.size();
	}
	
	public GroupDesc retrieveByPK (int id) {
		return getGroupDescById(id);
	}

	public GroupDesc findByTestId (int id) {
		return getGroupDescById(id);
	}
	
	public GroupDesc getGroupDescById(int key) {
		if (hm.containsKey(key)) {
			GroupDesc t = (GroupDesc) hm.get(key);
			return t;
		}
		else {
			return null;
		}
	}
	
	public GroupDesc getByDesc(String desc) {

		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupDesc t = (GroupDesc)pairs.getValue();
		    if (t.getGroupDescription().equals(desc)) {
		    	return t;
		    }
		}
		return null;
	}
	
	private static void initMap() {
		// Read data from the xml dump
		String  resourceFile = "group_desc.dump";
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
			int gid = 0;
			int gtid = 0;
			int appid = 0;
			String gdesc = "";
			String gsubtype = "";
			String catgName = "";
			String className = "";
			Date ts = new Date();
			String gcontype = "";
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String[]) rlist.get(i);
				String name = null;
				String value = null;
				name = str[0];
				value = str[1];

				/*
                <field name="GROUP_ID">1</field>
                <field name="GROUP_TYPE_ID">1</field>
                <field name="APPLICATION_ID">0</field>
                <field name="GROUP_DESCRIPTION">Admin Users</field>
                <field name="GROUP_SUBTYPE" xsi:nil="true" />
                <field name="CATEGORY_NAME" xsi:nil="true" />
                <field name="CLASSIFICATION_NAME" xsi:nil="true" />
                <field name="TIMESTAMP">2014-06-13 06:37:52</field>
                <field name="GROUP_CONTENT_TYPE">M</field>
				*/

				
				if (name.equals("group_id")) {
					if (!value.isEmpty())
						gid = Integer.parseInt(value);
				} else if (name.equals("group_type_id")) {
					if (!value.isEmpty())
						gtid = Integer.parseInt(value);
				} else if (name.equals("application_id")) {
					if (!value.isEmpty())
						appid = Integer.parseInt(value);
				} else if (name.equals("group_description")) {
					if (!value.isEmpty())
						gdesc = value;
				} else if (name.equals("group_subtype")) {
					if (!value.isEmpty())
						gsubtype = value;
				} else if (name.equals("category_name")) {
					if (!value.isEmpty())
						catgName = value;					
				} else if (name.equals("classification_name")) {
					if (!value.isEmpty())
						className = value;
				} else if (name.equals("group_content_type")) {
					if (!value.isEmpty())
						gcontype = value;					
				} 
			}
								
			GroupDesc t = new GroupDesc(gid, gtid, appid, gdesc, gsubtype, catgName, className, ts, gcontype);
			//t.dump();
			
			hm.put(gid, t);

		}

		/*
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
	}
	
	public void dumpMap() {
		Map<Integer, GroupDesc> sortedMap = new TreeMap<Integer, GroupDesc >(hm);
		
		Iterator it = sortedMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupDesc t = (GroupDesc)pairs.getValue();
		    t.dump();
		}		
		return;
	}
}