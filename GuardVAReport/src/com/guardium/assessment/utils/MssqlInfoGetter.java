/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.util.List;

import com.guardium.data.Datasource;



public class MssqlInfoGetter extends DataSourceInfoGeter 
{
	//private static final transient Logger LOG = Logger
	//		.getLogger(MssqlInfoGetter.class);
	
	/*public String getDbPatchLevel(Connection connection) throws Exception 
	{
		String patch = connection.getMetaData().getDatabaseProductVersion();
		return patch.substring(patch.lastIndexOf(".")+1);
	}*/

	public String getOs(Datasource ds)
	{
		return "Windows";
	}

	@Override
	public int evaluateDbPatch(List<String> referencePatches, String patch, String version) 
	{
		// loop through the reference list, 
		// return 1 if found patch is in the refernce patch and reference patch_to
		// or patch is greater or equal to reference patch
		// return -1 if not match the above criteria
		//LOG.warn("evaluateDbPatch verion " + version + " patch " + patch);
		
		int ret = -1;
		String separator = "-";

		int patchNum = Integer.parseInt(patch);
		
		for (String refpatch : referencePatches) 
		{
			String tmppatch = refpatch;
			String tmppatchto = "";
			
			if (refpatch.indexOf(separator) >=0) {
				tmppatch = refpatch.substring(0,refpatch.indexOf(separator));
				tmppatchto = refpatch.substring(refpatch.indexOf(separator)+1);
				//LOG.warn("evaluateDbPatch patch " + tmppatch + " patchto " + tmppatchto);
			}
		
			int tmppatchNum = Integer.parseInt(tmppatch);
			int tmppatchtoNum = 0;
			
			if (tmppatchto.isEmpty()) {
				// not in range
				// return 1 if greater or equal
				if (patchNum >= tmppatchNum) {
					//LOG.warn("evaluateDbPatch patchNum " + patchNum + " is greater than ref patchNum " + tmppatchNum);
					ret = 1;
					break;
				}
				/*
				else {
					LOG.warn("evaluateDbPatch patchNum " + patchNum + " is less than ref patchNum " + tmppatchNum);
				}
				*/
			}
			else {
				// have range, check if the patch in the range
				tmppatchtoNum = Integer.parseInt(tmppatchto);
				if (patchNum >= tmppatchNum && patchNum <= tmppatchtoNum) {
					// patch found in the range
					//LOG.warn("evaluateDbPatch patchNum " + patchNum + " is in the range of ref patchNum " + tmppatchNum + " and patchtoNum " + tmppatchtoNum);
					ret = 1;
					break;
				}
				/*
				else {
					LOG.warn("evaluateDbPatch patchNum " + patchNum + " is NOT in the range of ref patchNum " + tmppatchNum + " and patchtoNum " + tmppatchtoNum);
				}
				*/
			}
		}
		return ret;
	}
	
	protected long[] getAllIds() 
	{
		return null;
	}

}
