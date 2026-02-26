/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.guardium.runtest.CVETest;
import com.guardium.data.Datasource;
import com.guardium.utils.Check;

public class SybaseInfoGetter extends DataSourceInfoGeter 
{
	
	/** Local static logger for class */
	private static final transient Logger LOG = Logger.getLogger(CVETest.class);


	private static final String OS_SQL = "select @@version";

	public static String PATCH_TYPE_ESD = "ESD";
	public static String PATCH_TYPE_SP  = "SP";
	
	public static int PATCH_TYPE_ESD_LEVEL = 1;
	public static int PATCH_TYPE_SP_LEVEL  = 2;

	/*
    	ret  1 - pass
    	ret -1 - failed
    	ret  0 - not reported
	*/
	
	private int RET_PASS = 1;
	private int RET_FAIL = -1;
	private int RET_NOT_REPORTED = 0;

	public String getOs(Datasource ds) throws Exception
	{
		Connection connection = ds.getConnection();
		try
		{
			return getOneTryValue(connection, OS_SQL, 1);

		} 
		finally
		{
			Check.disposal(connection);
		}
	}



@Override

	public int evaluateDbPatch(List<String> referencePatches, String patch_orig, String version) 
	{
		int ret = RET_FAIL;
	
		/*
		ret = -1, not found the patch_type
		ret =  0, found the patch_type, but value is less than reference number.
		ret =  1, found the patch_type, and value is greater or equal to the reference number.
		 */
	
		String msg = "";
		String patch = patch_orig.toUpperCase();

		msg = "Input patch name is " + patch;
		LOG.warn(msg);
		
		// get word after SMP
		String [] patchS = patch.split("\\s+");
		// dsig patch is EBF 21517 SMP SP102
		// dsig patch is EBF 21770 SMP SP103
		// dsig patch is EBF 19494 SMP
		// dsig patch is EBF 20475 SMP ESD#02 ONE-OFF

		// we should get the patch after SMP
		boolean found = false;
		int len = patchS.length;
		int idx = 0;
		for (String p: patchS) {
			if (p.equals("SMP")) {
				found = true;
				break;
			}
			idx++;
		}
		if (found) {
			if (idx + 1 <= len -1) {
				// get word after SMP
				patch = patchS[idx+1];
			}
			else {
				// no words after SMP
				patch = "";
			}
		}
		else {
			// no SMP found, do the original way
			patch = patchS[patchS.length-1];
		}
		LOG.warn("after SMP check patch is " + patch);
		
		
		
		//Pattern p = Pattern.compile("^(\\D+)(\\d+)$");
		// allow decimal 1.5
		Pattern p = Pattern.compile("^(\\D+)([.\\d]+)$");
		Matcher m = p.matcher(patch);

		String patch_name = "";
		String patch_num  = "";
		int patch_level = 0;
        
		while (m.find()) {
			msg = "Pat Name   " + m.group(1) + " number " + m.group(2);
			LOG.warn(msg);
			patch_name = m.group(1);
			patch_num  = m.group(2);
		
			if (patch_name.startsWith(PATCH_TYPE_ESD)) {
				patch_level = PATCH_TYPE_ESD_LEVEL;
			}
			else {
				if (patch_name.startsWith(PATCH_TYPE_SP)) {
					patch_level = PATCH_TYPE_SP_LEVEL;
				}
				else {
					msg = "patch name is not in the support list: " + patch_name;
					LOG.warn(msg);
					ret = RET_NOT_REPORTED;
					return ret;
				}
			}
		}
	
		if (patch_name.equals("")) {
			msg = "patch name is empty string";
			LOG.warn(msg);
			if (referencePatches.isEmpty()) {
				msg = "Patch is empty, and reference list is empty";
				LOG.warn(msg);
				
				// non-applicable
				// ret = RET_NOT_REPORTED;
				ret = RET_PASS;
				return ret;
			}
			else {
				msg = "Patch is empty, but reference list is not empty. Need to check more";
				LOG.warn(msg);
			}
		}
		else {
			if (referencePatches.isEmpty()) {
				msg = "Patch is not empty, but reference list is empty";
				LOG.warn(msg);
				// non-applicable
				// ret = RET_NOT_REPORTED;
				ret = RET_PASS;
				
				return ret;
			}
		}
		
		double dpatch = 0.0;
		if (! patch_num.isEmpty()) {
			// patch not empty and ref list not empty, need to compare
		    dpatch = Double.valueOf(patch_num);
		    msg = "Pat number " + dpatch;
		    LOG.warn(msg);
		}
		
		found = false;
		for (String refP : referencePatches) 
		{
			int ref_patch_level = 0;    
			String ref_patch_name = "";
			String ref_patch_num  = "";
			
			m = p.matcher(refP);
			while (m.find()) {
				found = true;
				msg = "Ref Name   " + m.group(1) + " number " + m.group(2);
				LOG.warn(msg);
				ref_patch_name = m.group(1);
				ref_patch_num  = m.group(2);
				
				if (ref_patch_name.startsWith(PATCH_TYPE_ESD)) {
					ref_patch_level = PATCH_TYPE_ESD_LEVEL;
				}
				else {
					if (ref_patch_name.startsWith(PATCH_TYPE_SP)) {
						ref_patch_level = PATCH_TYPE_SP_LEVEL;
					}
					else {
						msg = "patch name is not in the support list: " + ref_patch_name;
						LOG.warn(msg);
						ret = RET_NOT_REPORTED;
						return ret;
					}
				}
				found = true;
			}
			double drefP = 0.0;
		    if (found) {
			    drefP = Double.valueOf(ref_patch_num);
			    msg = "ref number " + drefP;
			    LOG.warn(msg);
		    }
		    else {
		    	// not found the name and version format, may be empty string
		    	if (refP.isEmpty() || refP.equals("NULL")) {
		    		// if empty, don't need to compare.
		    		msg = "reference patch name is empty string or NULL. Don't need to compare";
					LOG.warn(msg);
		    		ret = RET_PASS;
		    	}
		    	else {
		    		msg = "reference patch name is not in the support format: " + refP;
					LOG.warn(msg);
					ret = RET_NOT_REPORTED;
		    	}
		    	return ret;
		    } 
		    
			msg = "Patch level " + patch_level + " refPatch level " + ref_patch_level;			
			LOG.warn(msg);
			msg = "Patch number " + dpatch + " refPatch number " + drefP;			
			LOG.warn(msg);			
			
			if (patch_level > ref_patch_level) {
				// if level higher, don't need to compare with the number
				LOG.warn("patch level check pass " + patch_level + " " + ref_patch_level);
				ret = RET_PASS;
			}
			else {
				if (patch_level < ref_patch_level) {
					// if level lower, don't need to compare with the number
					LOG.warn("patch level check fail " + patch_level + " " + ref_patch_level);
					ret = RET_FAIL;
				}
				else {
					// the same level, need to compare the number
					if(dpatch >= drefP) {
						LOG.warn("patch number check pass " + dpatch + " " + drefP);
						ret = RET_PASS;		
					}
					else {
						LOG.warn("patch number check fail " + dpatch + " " + drefP);
						ret = RET_FAIL;
					}
				}
			}
		}		
		
		return ret;
	}



	protected long[] getAllIds() 
	{
		return null;
	}
}
