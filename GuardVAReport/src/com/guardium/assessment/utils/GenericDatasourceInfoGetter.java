/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.util.List;

import com.guardium.data.Datasource;

public class GenericDatasourceInfoGetter extends DataSourceInfoGeter 
{

	
	@Override
	public int evaluateDbPatch(List<String> referencePatches, String patch, String version) {
		throw new UnsupportedOperationException("No implementation for this method in the generic class");
	}

	@Override
	protected long[] getAllIds() {
		throw new UnsupportedOperationException("No implementation for this method in the generic class");
	}

	@Override
	public String getOs(Datasource ds) throws Exception {
		throw new UnsupportedOperationException("No implementation for this method in the generic class");
	}

}
