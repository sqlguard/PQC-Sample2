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
import java.util.Iterator;
import java.util.List;

//import com.guardium.datamodel.dbSource.CASRequiredException;
import com.guardium.data.Datasource;
import com.guardium.data.DataSourceConnectException;
import com.guardium.utils.Check;

public class Db2InfoGetter extends DataSourceInfoGeter 
{

	private static final String OS_SQL = "SELECT OS_NAME  FROM TABLE(SYSPROC.ENV_GET_SYS_INFO()) AS SYSTEMINFO ";
	private static final String VERSION_SQL = "SELECT SERVICE_LEVEL FROM TABLE(SYSPROC.ENV_GET_INST_INFO()) AS INSTANCEINFO";
	private static final String PATCH_SQL = "SELECT  FIXPACK_NUM, BLD_LEVEL FROM TABLE(SYSPROC.ENV_GET_INST_INFO()) AS INSTANCEINFO";

	
	

	/*public String getDbVersionLevel(Datasource ds) throws CASRequiredException, DataSourceConnectException
	{
		String ret = null;
		Connection con = null;
		try
		{
			con = ds.getConnection();
			ret = getOneTryValue(con, VERSION_SQL, 1);
		}
		catch (SQLException e)
		{
			System.out.println("CAS required");
			throw new CASRequiredException();
		}
		finally
		{
			Check.disposal(con);
		}
		return ret;
	}*/

	public String getDbPatchLevel(Datasource ds) throws DataSourceConnectException
	{
		String ret = null;
		Connection con = null;
		try
		{
			con = ds.getConnection();
			String [] sa = getPatchValue(con, PATCH_SQL);
			ret = "Fix Pack "+ sa[0] + ", " + sa[1];
		}
		catch (SQLException e)
		{
			//System.out.println("CAS required");
			throw new DataSourceConnectException();
		}
		
		finally
		{
			Check.disposal(con);
		}
		return ret;
	}

	

	private String [] getPatchValue(Connection con, String patchSql) throws SQLException 
	{
			Statement stmt = null;
			ResultSet res = null;
			String [] ret = new String [2];
			try
			{
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				res = stmt.executeQuery(patchSql);
				if ( res.next() ) 
				{
					ret [0] = res.getString(1);
					ret [1] = res.getString(2);
				}
				return ret;
			} 
			finally
			{
				Check.disposal(res);
				Check.disposal(stmt);
			}
	}



	public String getOs(Datasource ds) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = ds.getConnection();
			return connection.getMetaData().getDatabaseProductName();//getOneTryValue(connection, OS_SQL, 1);
		}
		finally
		{
			Check.disposal(connection);
		}
	}


		/**
		 * evaluate patch level in the format 12ab3a
		 * the template might contain a * character to break comparison
		 * 13 is bigger then 12ab3a, 12aa is lower then 12ab3a etc.
		 */
		@Override
		public int evaluateDbPatch(List<String> referencePatches, String patch, String version) 
		{
			int ret = -1;
			boolean asterix=false;
			// Create the tokenizer
			RETokenizer  patchTokenizer = new RETokenizer(patch, "\\D", true);
			for (String refP : referencePatches) 
			{
				patchTokenizer.rewind();
				// Create the tokenizer
				Iterator <String> refTokenizer = new RETokenizer(refP, "[\\D]+", true);
				// Get the tokens (and delimiters)
				for (; patchTokenizer.hasNext(); ) 
				{
					boolean numeric = true;
					int ipatch = -1;
					patch = (String) patchTokenizer.next();
					if(!refTokenizer.hasNext())
					{
						ret = -1;
						break;
					}
					refP = refTokenizer.next();
					if(refP.endsWith("*"))
					{
						if(refP.equals("*"))
							return 1;
						else
						{	
							refP = refP.substring(0, refP.length()-1);
							asterix = true;
						}
					}
					try
					{
						ipatch = Integer.parseInt(patch);
					}
					catch (NumberFormatException e)
					{
						numeric = false;
					}
		        	if(numeric)
		        	{
		        		int irefP = Integer.parseInt(refP);
		        		if(irefP < ipatch)
		    			{
		    				ret = 1;
		    				break;
		    			}
		        		else if(irefP > ipatch)
		    			{
		    				ret = -1;
		    				break;
		    			}
		        		else if(irefP==ipatch)
		        			ret = 1;
		        		
		        	}
		        	else
		        	{
		        		int comp  = refP.compareToIgnoreCase(patch);
		        		if(comp < 0)
		        		{
		        			ret=-1;
		        			break;
		        		}
		        		else if(comp > 0)
		        		{
		        			ret = 1;
		        			break;
		        		}
		        		else if(comp==0)
		        			ret = 1;
		        	}	
		        	if(asterix)
		        		break;
				}
				if((!patchTokenizer.hasNext() && !refTokenizer.hasNext() && ret == 1)||asterix)
					return ret;
			}
			return ret;
		}		




	@Override
	protected long[] getAllIds() 
	{
		long [] l =  {(long)765, (long)767,(long)768};
		return l;
	}



	@Override
	public String getDbPatch(Datasource ds) throws Exception
	{

		String ret = null;
		Connection con = null;
		try
		{
			con = ds.getConnection();
			String [] sa = getPatchValue(con, PATCH_SQL);
			ret = sa[0];
		}
		catch (SQLException e)
		{
			//System.out.println("CAS required");
			throw new DataSourceConnectException();
		}
		finally
		{
			Check.disposal(con);
		}
		return ret;
	}

}
