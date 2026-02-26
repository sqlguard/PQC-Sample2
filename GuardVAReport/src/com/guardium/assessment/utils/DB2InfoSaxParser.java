/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.util.Map;


/**
 * This  class is used to parse the XML returned form the database and create a Map of module/ptf level.
 * The XML is formatted as a plist schema which is XML from hell - there is a project from Apache commons to parse a plist XML
 * but for what I need it is an overkill and it requires that we upgrade a few of our Apache jar files.
 * Instead I use sax to get the specific information I need making a few assumptions on the order of the xml nodes 
 * and ignoring everything else that is in the document
 * The logic is in the setProperty method bellow.
 * @author guyga
 *
 */
class DB2InfoSaxParser extends AbstractDb2InfoSaxParser
{
		static final String sPtfLevel = "PTF Level";
		static final String sDb2Mepl = "DB2 MEPL";
		
		private boolean inDB2_MEPL = false;
		private boolean gotModule = false;
		private boolean gotPTF = false;
		private String previousStringValue;
		private String module;
		private String ptf;
		
		public DB2InfoSaxParser(Map<String, String> nvm) 
		{
			super(nvm);
		}

	

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
			if(element != null)
			{
				// skip all element until we get to DB2 MEPL
				if(!inDB2_MEPL)
				{
					if(element.equalsIgnoreCase(sstring) && value.equalsIgnoreCase(sDb2Mepl))
						inDB2_MEPL = true;
					return;
				}
				if(!gotModule)
				{
					// rely on the module name to be the last string element before a "PTF Level" key element.
					if(element.equalsIgnoreCase(skey) && value.equalsIgnoreCase(sPtfLevel))
					{
						module = previousStringValue;
						previousStringValue = "";
						gotModule = true;
						return;
					}
					if(element.equalsIgnoreCase(sstring))
						previousStringValue = value;
					return;
				}
				if(!gotPTF)
				{
					if(element.equalsIgnoreCase(skey) && value.equalsIgnoreCase(svalue))
						gotPTF = true;
					return;
				}
				if(element.equalsIgnoreCase(sstring))
				{
					ptf = value;
					nameValueMap.put(module, ptf);
					gotModule = gotPTF = false;
					module = ptf = previousStringValue = "";
					return;
				}
					
			}
		}

}