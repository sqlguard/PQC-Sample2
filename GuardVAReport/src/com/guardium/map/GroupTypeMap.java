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

//import org.apache.torque.TorqueException;
//import org.apache.torque.util.Criteria;

import com.guardium.data.Datasource;
import com.guardium.data.GroupType;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
//import com.guardium.map.GroupTypeMap;
import com.guardium.utils.ReadDumpFile;

public class GroupTypeMap {
	
	public final static int USERS = 1;
	public final static int COMMANDS = 2;
	public final static int OBJECTS = 3;
	public final static int YEAR = 4;
	public final static int WEEKDAY = 5;
	public final static int FIELDS = 6;
	public final static int SOURCE_PROGRAM = 7;
	public final static int DB_PROTOCOL_VERSION = 8;
	public final static int DB_PROTOCOL = 9;
	public final static int NET_PROTOCOL = 10;
	public final static int PORT = 11;
	public final static int Client_IP = 12;
	public final static int SERVER_TYPE = 13;
	public final static int SENTENCE_DEPTH = 14;
	public final static int SCHEMA = 15;
	public final static int TTL = 16;
	public final static int EXCEPTION_TYPE = 17;
	public final static int APPLICATION_USER = 18;
	public final static int Server_IP = 19;
	public final static int Server_OS = 20;
	public final static int Client_OS = 21;
	public final static int OS_User = 22;
	public final static int Server_Hostname = 23;
	public final static int Server_Description = 24;
	public final static int Client_MAC_Address = 25;
	public final static int Client_Hostname = 26;
	public final static int Service_Name = 27;
	public final static int Database_Name = 28;
	public final static int SQL_Guard_Audit_Categories = 29;
	public final static int ERROR_CODE = 30;
	public final static int Application_Module = 31;
	public final static int Records_Affected = 32;
	public final static int Object_Command = 33;
	public final static int Object_Field = 34;
	public final static int DB_Role = 35;
	public final static int Application_Event_Value_Type = 36;
	public final static int Application_Event_Value_String = 37;
	public final static int Application_Event_Value_NumberL = 38;
	public final static int SQL_GUARD_USERS = 49;
	public final static int IMS_TARGET_DB_SEGMENT = 71;
	private final static int MAXLISTSIZE = 200;

	private final static int INTERNAL_ONLY_ID_1 = 51;
	private final static int INTERNAL_ONLY_ID_2=52;
	public final static int VA_TEST_EXCEPTION=55;
	private final static int INTERNAL_ONLY_ID_3=66;
	
	public static void main(String args[]) {
		GroupTypeMap vmap = new GroupTypeMap();
		
		int key = 1;
		GroupType t = vmap.getGroupTypeById(key);

		if (t != null) {
			String sd = t.getTypeDescription();

			System.out.println("id " + key + " group type desc is: " + sd);

			boolean loopdbs = t.isTupleFlag();
			System.out.println("id " + key + " tuple flag is: " + loopdbs);
		}
		else {
			System.out.println("id " + key + " GroupType is null");
		}
		
		System.out.println("\ngroup type map size is " + vmap.getMapSize());
	}

	private static GroupTypeMap GroupTypeMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private GroupTypeMap () {

	}
		
	public static synchronized GroupTypeMap getGroupTypeMapObject() {
		if (GroupTypeMapObject == null) {
			GroupTypeMapObject = new GroupTypeMap();
			initMap();
		}
		return GroupTypeMapObject;
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

	public GroupType findByTestId (int id) {
		return getGroupTypeById(id);
	}
	
	public GroupType getGroupTypeById(int key) {
		if (hm.containsKey(key)) {
			GroupType t = (GroupType) hm.get(key);
			return t;
		}
		else {
			return null;
		}
	}
	
	public GroupType getByDesc(String desc) {

		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupType t = (GroupType)pairs.getValue();
		    if (t.getTypeDescription() == desc) {
		    	return t;
		    }
		}
		return null;
	}
	
	public static List<GroupType>doSelectExclueInternal() {
		
		List <GroupType> gtlist = new ArrayList<GroupType>();
		
		//c=addExcludeInternalToCriteria(c);
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupType t = (GroupType)pairs.getValue();
		    if (t.getGroupTypeId() != INTERNAL_ONLY_ID_1 && t.getGroupTypeId() != INTERNAL_ONLY_ID_2 && t.getGroupTypeId() != INTERNAL_ONLY_ID_3) {
		    	gtlist.add(t);
		    }
		}		
		
		// sort by TEST_DESCRIPTION
		// ?
		
		return gtlist;
	}
	
	/*
	public static List<GroupType>doSelectExclueInternal(Criteria c) {
		c=addExcludeInternalToCriteria(c);
		return doSelect(c);
	}

	private static Criteria addExcludeInternalToCriteria(Criteria c) {
		if (c!=null) {
			c.and(GROUP_TYPE_ID, INTERNAL_ONLY_ID_1, Criteria.ALT_NOT_EQUAL);
			c.and(GROUP_TYPE_ID, INTERNAL_ONLY_ID_2, Criteria.ALT_NOT_EQUAL);
			c.and(GROUP_TYPE_ID, INTERNAL_ONLY_ID_3, Criteria.ALT_NOT_EQUAL);
			c.addAscendingOrderByColumn(GroupTypePeer.TYPE_DESCRIPTION);
		}
		return c;
	}
	*/
	
	
	private static void initMap() {
		// Read data from the xml dump
		String  resourceFile = "group_type.dump";
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
			int gtid = 0;
			String gtdesc = "";
			boolean tflag = false;
			boolean allreg = false;
			Date ts = new Date();
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String[]) rlist.get(i);
				String name = null;
				String value = null;
				name = str[0];
				value = str[1];
				
				if (name.equals("group_type_id")) {
					if (!value.isEmpty())
						gtid = Integer.parseInt(value);
				} else if (name.equals("type_description")) {
					if (!value.isEmpty())
						gtdesc = value;
				} else if (name.equals("tuple_flag")) {
					if (!value.isEmpty()) {
                		int tmp = Integer.parseInt(value);
                		if (tmp == 0) 
                			tflag = false;
                		else 
                			tflag = true;
					}
				} else if (name.equals("allow_regex")) {
					if (!value.isEmpty()) {
                		int tmp = Integer.parseInt(value);
                		if (tmp == 0) 
                			allreg = false;
                		else 
                			allreg = true;	
					}
				}
			}
								
			GroupType t = new GroupType(gtid, gtdesc, ts, tflag, allreg); 
			
			hm.put(gtid, t);

		}

		/*
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
	}
}