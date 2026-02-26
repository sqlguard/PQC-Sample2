/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.sql.Connection;
import java.util.List;

import com.guardium.data.Datasource;
import com.guardium.utils.Check;

public class PostgreSqlInfoGetter extends DataSourceInfoGeter 
{

	/*
	 * Usage of show \"data_directory\" requires superuser permissions - we are opting for a different route
	 * private static final String OS_SQL =  "show \"data_directory\"";
	 */
	private static final String OS_SQL = "select spclocation from pg_tablespace where spclocation <> '' and spclocation	is not null";
	private static final String VERSION_SQL =  "select version()";
	

	public String getOs(Datasource ds) throws Exception
	{
		Connection connection = ds.getConnection();
		try
		{
			String s = getOneTryValue(connection, OS_SQL, 1);
			if(!Check.isEmpty(s))
			{
				os = s.contains("/")?"Unix":"Windows";
			}
			else // default tablespace is used - path is empty
			{
				s = getOneTryValue(connection, VERSION_SQL, 1);
				// on a Windows server version is in the format "PostgreSQL 8.3.6, compiled by Visual C++ build 1400"
				// Note: This is not bullet proof - someone might compile PostgreSql for Windows using gcc  
				os = (s.indexOf("Visual C++")>-1) ? "Windows":"Unix";
			}
			return os;
		} 
		finally
		{
			Check.disposal(connection);
		}
	}



	@Override
	public int evaluateDbPatch(List<String> referencePatches, String patch, String version) 
	{
		int ret = -1;
		// remove any non numeric characters from patch
		patch = patch.replaceAll("\\D","");
		int ipatch = Integer.parseInt(patch);
		for (String refP : referencePatches) 
		{
			refP= refP.replaceAll("\\D","");
			int irefP = Integer.parseInt(refP);
			if(irefP <= ipatch)
				ret = 1;			
		}
		return ret;
	}
	protected long[] getAllIds() 
	{
		return null;
	}
}
