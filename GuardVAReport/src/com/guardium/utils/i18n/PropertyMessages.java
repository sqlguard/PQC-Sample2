/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils.i18n;

/**
 * @author dtoland on Aug 2, 2006 at 10:04:54 AM
 */
public interface PropertyMessages {

	/**
	 * Constant for missing key in properties message.
	 * @see PropertyMessages#PROP_SUB_FILE_NAME
	 * @see PropertyMessages#PROP_SUB_KEY
	 */
	public String PROP_MSG_NO_KEY = "props.no.key";

	/**
	 * Constant for missing properties for key message.
	 * @see PropertyMessages#PROP_SUB_FILE_NAME
	 * @see PropertyMessages#PROP_SUB_KEY
	 */
	public String PROP_MSG_NO_PROPS_FOR_KEY = "props.no.props.for.key";

	/** Constant for the properties file name symbolic */
	public String PROP_SUB_FILE_NAME = "propFileName";

	/** Constant for the property key symbolic */
	public String PROP_SUB_KEY = "propKey";

}
