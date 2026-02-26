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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.guardium.data.AvailableTest;
import com.guardium.data.GroupMember;

import com.guardium.data.GroupDesc;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.map.GroupDescMap;
import com.guardium.map.GroupMemberMap;
import com.guardium.map.GroupTypeMap;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.ReadDumpFile;

public class GroupMemberMap {
	
	
	public static void main(String args[]) {
		GroupMemberMap vmap = new GroupMemberMap();

		int key = 10;
		GroupMember t = vmap.getGroupMemberById(key);

		if (t != null) {
			String sd = t.getGroupMember();
			System.out.println("id " + key + " group member is: " + sd + "\n");
			
			t.dump();
		}
		else {
			System.out.println("id " + key + " GroupMember is null");
		}
		
		System.out.println("\ngroup member map size is " + vmap.getMapSize());
	}
	
	//GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	//private GroupTypeMap GroupTypePeer = new GroupTypeMap();
	
	private static GroupMemberMap GroupMemberMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private GroupMemberMap () {

	}
		
	public static synchronized GroupMemberMap getGroupMemberMapObject() {
		if (GroupMemberMapObject == null) {
			GroupMemberMapObject = new GroupMemberMap();
			initMap();
		}
		return GroupMemberMapObject;
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

	public GroupMember findByGroupId (int id) {
		return getGroupMemberById(id);
	}
	
	public GroupMember getGroupMemberById(int key) {
		if (hm.containsKey(key)) {
			GroupMember t = (GroupMember) hm.get(key);
			return t;
		}
		else {
			return null;
		}
	}
	
	private static void initMap() {
		// Read data from the xml dump
		String  resourceFile = "group_member.dump";
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
			int mid = 0;
			int gid = 0;
			String gmem = "";
			Date ts = new Date();

			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String[]) rlist.get(i);
				String name = null;
				String value = null;
				name = str[0];
				value = str[1];

				/*
        		<row>
                	<field name="MEMBER_ID">6</field>
                	<field name="GROUP_ID">1</field>
                	<field name="GROUP_MEMBER">SA</field>
                	<field name="TIMESTAMP">2014-06-13 06:37:51</field>
        		</row>
				*/
				
				if (name.equals("member_id")) {
					if (!value.isEmpty())
						mid = Integer.parseInt(value);
				} else if (name.equals("group_id")) {
					if (!value.isEmpty())
						gid = Integer.parseInt(value);
				} else if (name.equals("group_member")) {
					if (!value.isEmpty())
						gmem = value;
				}
			}
								
			GroupMember t = new GroupMember(mid, gid, gmem, ts);
			
			hm.put(mid, t);

		}

		/*
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
	}
	
	/**
	 * The value that separates elements in a tuple-based group member value
	 */
	public static final String TUPLE_DELIM = "+";
	
	/*
	public static void doDelete(Criteria criteria) {
		BaseGroupMemberPeer.doDelete(criteria);
		GroupTypePeer.handleGroupMemberChangeFlag(criteria);
	}
	*/
	
    /**
     * @param obj the data object to delete in the database.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doDelete(GroupMember obj)
    {
    	hm.remove(obj.getMemberId());
    	return;
    }
	
    //return true if a GroupMember belongs to a group of Guardium User Type
	private static boolean checkOfGuardUserGroupFlag(GroupMember obj) {
		boolean retVal=false;
		GroupDesc gp=null;

		gp=obj.getGroupDesc();
		if(gp!=null) {
			int typeOfGroupType=gp.getGroupTypeId();
			if(typeOfGroupType==GroupTypeMap.SQL_GUARD_USERS) {
				retVal=true;
			}
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public static List<GroupMember> doSelectOnGrpId(int groupId) {
		List <GroupMember> l = new ArrayList<GroupMember>();

		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupMember gm = (GroupMember)pairs.getValue();
			
			if (gm.getGroupId() == groupId) {
				l.add(gm);
			}
		}
		return l;
	}
	
	public static List<GroupMember> doSelect(List<Integer> groupIDs)  {
		List <GroupMember> l = new ArrayList<GroupMember>();

		if(Check.isEmpty(groupIDs)) {
			return l;
		}

		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    GroupMember gm = (GroupMember)pairs.getValue();
			
			for (Integer dd : groupIDs) {
				if (gm.getGroupId() == dd) {
					l.add(gm);
				}
			}
		}
		
		return l;
	}
	
	/*
	public static List doSelectGrpMemOnGrp(int groupTypeId, int appId, boolean includePublic, String member)  {
		List l=null;
		Criteria c = new Criteria();
		int[] gtypes = includePublic ? new int[]{0,groupTypeId}:new int[]{0};
		c.addIn(GroupDescPeer.GROUP_TYPE_ID,gtypes);
		l=GroupMemberPeer.doSelectJoinGroupDesc(c);

		
		return l;
	}
	*/
	
	/**
	 * @param userList
	 * A list of users
	 * @param grpId
	 * Group Id from GROUP_MEMBER
	 * @return if every item in userList belongs to group_member of the group in question
	 * @throws TorqueException
	 */
	public static boolean isAllMemberOfAGroup(Set<String> userList, int grpId)  {
		List <GroupMember> members=doSelectOnGrpId(grpId);
		Set<String> memberSet=new HashSet<String>();
		if(members!=null && !members.isEmpty()) {
			for(GroupMember member:members) {
				memberSet.add(member.getGroupMember());
			}
		}
		return memberSet.containsAll(userList);
	}

	/**
	 * Checks whether memberName is a member of the group specified by groupId
	 * 
	 * @param memberName
	 * @param groupId
	 * @return
	 * @throws TorqueException
	 */
	public static boolean memberExistsInGroup(String memberName, int groupId)  {
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			GroupMember gm = (GroupMember)(it.next());
			
			if ((gm.getGroupId() == groupId) && (gm.getGroupMember().equalsIgnoreCase(memberName))) {
				return true;
			}
		}
		return false;
	}

	
	
}