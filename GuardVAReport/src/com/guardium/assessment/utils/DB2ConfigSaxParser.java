/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

//import com.sun.xml.internal.ws.api.DistributedPropertySet;


public class DB2ConfigSaxParser extends AbstractDb2InfoSaxParser implements
		ContentHandler 
{
	public static final List<String> subsystemParameters = Arrays.asList(new String[]{"CACHEDYN", "AUTHCACH", "AUTH","SEPARATE_SECURITY","BINDNV",
		"DBACRVW","REVOKE_DEP_PRIVILEGES","EXTSEC","TCPALVER","SYSADM","SYSADM2","SECADM1","SECADM2","SYSOPR1","SYSOPR2","SECADM1_TYPE","SECADM2_TYPE"});
	public static final List<String> distributedAccessParameters = Arrays.asList(new String[]{"TCP/IP Port","Secure Port"});
	
	private boolean gotElement = false;
	private boolean gotParamValue = false;
	private boolean gotValue = false;
	private String paramName = null;
	
	public DB2ConfigSaxParser(Map<String, String> configParams) 
	{
		super(configParams);
	}

	@Override
	/* *
	 * Map is populated according to the xml element
	 * being processed.
	 * Rely on the following format :
	   ...
	    <string>DSNAA</string>
  	 	<key>PTF Level</key>
  		<dict>
     	<key>Display Name</key>
     	<string>PTF Level</string>
     	<key>Value</key>
     	<string>UK16433</string>
     	...
	 */
	public void setProperty(String element, String value) throws Exception
	{
		String nextTag = null;
		if(element != null)
		{
			if(!gotElement)
			{
				if(element.equalsIgnoreCase(skey) && (subsystemParameters.contains(value)||distributedAccessParameters.contains(value)))
				{
					gotElement = true;
					paramName = value;
				}
				return;
			}
			if(subsystemParameters.contains(paramName))
			{
				nextTag = sstring;
				if(!gotParamValue)
				{
					if(element.equalsIgnoreCase(skey) && value.equalsIgnoreCase("Subsystem Parameter Value"))
					{
						gotParamValue = true;
					}
					return;
				}
			}
			else if (distributedAccessParameters.contains(paramName))
			{
				nextTag = sinteger;
				gotParamValue = true;
			}
			if(gotParamValue && !gotValue)
			{
				if(element.equalsIgnoreCase(skey) && value.equalsIgnoreCase("Value"))
				{
					gotValue = true;
				}
				return;
			}
			if(nextTag != null && element.equalsIgnoreCase(nextTag))
			{
				nameValueMap.put(paramName, value);
				paramName = "";
				gotElement = gotParamValue = gotValue = false;
				nextTag = null;
				return;
			}
		}
	}

}
