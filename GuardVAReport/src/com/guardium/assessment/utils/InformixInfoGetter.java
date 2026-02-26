/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.guardium.data.Datasource;
import com.guardium.utils.Check;

public class InformixInfoGetter extends DataSourceInfoGeter 
{

	private static final String OS_SQL = "select * from sysmachineinfo ";
	private static final String OS_PATH_SQL = "select cf_effective from sysconfig where cf_name = \'ROOTPATH\'";
	private static final String VERSION_SQL = "select unique dbinfo('version', 'major')||'.'||  dbinfo('version', 'minor') from systables";
	private static final String PATCH_SQL = "select unique dbinfo('version', 'full') from systables";
	


	/*public String getDbPatchLevel(Connection connection) throws Exception
	{
		String patch = getOneTryValue(connection, PATCH_SQL, 1);
		return patch.substring(patch.lastIndexOf(".")+1);
		
	}*/

	/*
	 * public String getDbVersion(Connection connection) throws Exception 
	 *
	{
		return getOneTryValue(connection, VERSION_SQL, 1);
	}
	*/
	
	public String getOs(Datasource ds) throws Exception
	{
		Statement stmt = null;
		ResultSet res = null;
		Connection connection = ds.getConnection();
		try
		{
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			try
			{
				res = stmt.executeQuery(OS_SQL);
				if ( res.next() ) 
				{
					os = res.getString(1);
				}
				return os;
			}
			catch (SQLException se)
			{	
				String path = null;
				res = stmt.executeQuery(OS_PATH_SQL);
				if ( res.next() ) 
				{
					path = res.getString(1);
					os = path.contains("/")?"Unix":"Windows";
				}
				return os;
			}

		} 
		finally
		{
			Check.disposal(res);
			Check.disposal(stmt);
			Check.disposal(connection);
		}
	}


	@Override
	public int evaluateDbPatch(List<String> referencePatches, String patch, String version)
	{
		// split patch by non numerics and then evaluate from left to right
		// UC2W1 is higher then XD2 and lower then ND3
		int ret = -1;
		String [] pItems = patch.split("\\D+");
	  Refloop:	
		for (String refpatch : referencePatches) 
		{
			String [] rpItems = refpatch.split("\\D+");
			for (int i = 1; i < pItems.length; i++) 
			{
				int rp = Integer.parseInt(rpItems[i]);
				int p  = Integer.parseInt(pItems[i]);
				if(rp > p)
				{
					ret = -1;
					break;
				}
				else if(rp < p)
				{
					ret = 1;
					break;
				}
				else if(rp == p)
					ret = 1;
				if(i == rpItems.length-1)
					return ret;
					
			}
		}
		return ret;
	}
	protected long[] getAllIds() 
	{
		return null;
	}
}
