/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.guardium.data.Datasource;
import com.guardium.utils.Check;

public class MysqlInfoGetter extends DataSourceInfoGeter 
{

	private static final String OS_SQL =  "show variables like \'version_compile_os\'";
	private static final String PATCH_SQL =  "show variables like \'version\'";

	
	

	/*public String getDbPatchLevel(Connection connection) throws Exception
	{
		String patch = getOneTryValue(connection, PATCH_SQL, 1);
		String[] elements = patch.split(Say.DASH);
		return elements[0].substring(patch.lastIndexOf(".")+1);
		
	}*/

	

	public String getOs(Datasource ds) throws Exception
	{
		Connection connection = ds.getConnection();
		try
		{
			return getOneTryValue(connection, OS_SQL, 2);

		} 
		finally
		{
			Check.disposal(connection);
		}
	}


	/*public static void main(String[] args) 
	{
		List <String> l = new ArrayList<String>();
		l.add("31*");
		l.add("31b");
		l.add("32");
		MysqlInfoGetter mig = new MysqlInfoGetter();
		int i = mig.evaluateDbPatch(l,"31bb");
		
	}*/

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
	
	protected long[] getAllIds() 
	{
		return null;
	}

}
