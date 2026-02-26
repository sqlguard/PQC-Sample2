/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;

public class GroupMember {


	public GroupMember () {
		
	}

	public GroupMember(int memberId, int groupId, String groupMember,
			Date timestamp) {
		super();
		this.memberId = memberId;
		this.groupId = groupId;
		this.groupMember = groupMember;
		this.timestamp = timestamp;
	}
	
	GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	
	private int memberId;
	private int groupId;
	private String groupMember;
	private Date timestamp;
	
	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getGroupMember() {
		return groupMember;
	}

	public void setGroupMember(String groupMember) {
		this.groupMember = groupMember;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void dump() {
        System.out.println("group memeber id: " + this.getMemberId());
        System.out.println("group group id: " + this.getGroupId());
        System.out.println("group group member: " + this.groupMember);
	}
	
    private GroupDesc aGroupDesc;

    /**
     * Declares an association between this object and a GroupDesc object
     *
     * @param v GroupDesc
     * @throws TorqueException
     */
    public void setGroupDesc(GroupDesc v) 
    {
        if (v == null)
        {
        	setGroupId(0);
        }
        else
        {
        	setGroupId(v.getGroupId());
        }
        aGroupDesc = v;
    }

              
    /**
     * Get the associated GroupDesc object
     *
     * @return the associated GroupDesc object
     * @throws TorqueException
     */
    public GroupDesc getGroupDesc()
    {
    	if (aGroupDesc == null && (this.getGroupId() > 0))
    	{
    		aGroupDesc = GroupDescPeer.getGroupDescById(this.getGroupId());
   
    		/* The following can be used instead of the line above to
            guarantee the related object contains a reference
            to this object, but this level of coupling
            may be undesirable in many circumstances.
            As it can lead to a db query with many results that may
            never be used.
            GroupDesc obj = GroupDescPeer.retrieveByPK(this.group_id);
            obj.addGroupMembers(this);
    		 */
     }
     return aGroupDesc;
 }

	
	
/*
	mysql> desc GROUP_MEMBER;
	+--------------+--------------+------+-----+-------------------+-----------------------------+
	| Field        | Type         | Null | Key | Default           | Extra                       |
	+--------------+--------------+------+-----+-------------------+-----------------------------+
	| MEMBER_ID    | int(11)      | NO   | PRI | NULL              | auto_increment              |
	| GROUP_ID     | int(11)      | NO   | MUL | 0                 |                             |
	| GROUP_MEMBER | varchar(255) | NO   |     |                   |                             |
	| TIMESTAMP    | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
	+--------------+--------------+------+-----+-------------------+-----------------------------+
	4 rows in set (0.00 sec)
*/ 
	
}
