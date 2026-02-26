/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.util.List;

//import com.guardium.utils.AliasDictionary;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
import com.guardium.map.GroupMemberMap;
import com.guardium.data.GroupMember;
import com.guardium.map.GroupTypeMap;

public class ReplaceGroupsAndAliases {
	static public  final String Prefix = "~~";
	static private final String PrefixFirstCh = "~";
	static private final int    PrefixLen = Prefix.length();
	static private final String TypeGroup = "G";
	static private final String TypeAlias = "A";
	
	static GroupMemberMap GroupMemberPeer = GroupMemberMap.getGroupMemberMapObject();
	static GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	static GroupTypeMap GroupTypePeer = GroupTypeMap.getGroupTypeMapObject();
	
	// prevent it from being instantiated or subclassed
	private ReplaceGroupsAndAliases() { throw new AssertionError(); } 
	
	static public String replaceGroupsAndAliases(String inStr) throws Exception {
		StringBuilder ret = new StringBuilder(inStr);

		for (int indFrom = 0; (indFrom = ret.indexOf(Prefix)) != -1; ) {
			int indTo = ret.indexOf(Prefix, indFrom + PrefixLen);
			String type = ret.substring(indFrom + PrefixLen, indFrom + PrefixLen + 1).toUpperCase();
			
			if (type.equals(TypeGroup)) { // Group Members
				String groupName = ret.substring(indFrom + 4, indTo);
				GroupDesc gd = GroupDescPeer.getByDesc(groupName);
				
				if (gd == null)
					return inStr;
				
				List<GroupMember> members = GroupMemberPeer.doSelectOnGrpId(gd.getGroupId());  // gd.getGroupMembers();
				
				StringBuilder membersList = new StringBuilder();
				for (GroupMember m : members) {
					if (membersList.length() > 0)
						membersList.append(",");
					membersList.append("'").append(m.getGroupMember()).append("'");
				}
				
				if (membersList.toString().trim().equals("")) membersList.append("''");
				ret.replace(indFrom, indTo + PrefixLen, membersList.toString());
			}
			else 
			if (type.equals(TypeAlias)) { // Alias
				String groupTypeDescription = ret.substring(indFrom + 4, ret.indexOf(PrefixFirstCh, indFrom + 4));
				String aliasValue = null;
				
				try {
					int groupTypeId = GroupTypePeer.getByDesc(groupTypeDescription).getGroupTypeId();
					String aliasDBValue = ret.substring(ret.indexOf(PrefixFirstCh, indFrom + 4) + 1, indTo);
					aliasValue = AliasDictionary.getAliasDictionary().translate(groupTypeId, aliasDBValue);
				} catch (Exception e) {}
				
				ret.replace(indFrom, indTo + PrefixLen, aliasValue == null ? "" : aliasValue);
			}
		}
		
		return ret.toString();
	}
}
