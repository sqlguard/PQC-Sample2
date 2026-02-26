/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils.i18n;

/**
 * Common Message Category
 * @author dtoland on Aug 2, 2006 at 10:01:59 AM
 */
public interface InformationalMessages {

	/** Constant for unknown literal */
	public String INFO_LIT_LIST_SEP = "info.lit.list.separator";

	/** Constant for Location literal */
	public String INFO_LIT_LOCATION = "info.location.literal";

	/** Constant for the no message literal */
	public String INFO_LIT_NO_MSG = "info.no.message";

	/**
	 * Constant for garbage collection request literal
	 * @see #INFO_SUB_COUNT
	 */
	public String INFO_MSG_GC_REQUEST = "info.gc.request";

	/**
	 * Constant for the Location info message
	 * @see #INFO_SUB_CLS_NAME
	 * @see #INFO_SUB_MTHD_NAME
	 * @see #INFO_SUB_EXEC_LINE
	 */
	public String INFO_MSG_LOCATION = "info.location";

	/**
	 * Constant for the Location Line info message
	 * @see #INFO_SUB_CLS_NAME
	 * @see #INFO_SUB_MTHD_NAME
	 * @see #INFO_SUB_EXEC_LINE
	 */
	public String INFO_MSG_LOCATION_LINE = "info.location.line";

	/**
	 * Constant for the memory info message
	 * @see #INFO_SUB_MEM_MAX
	 * @see #INFO_SUB_MEM_MAX
	 * @see #INFO_SUB_MEM_FREE
	 * @see #INFO_SUB_DATETIME
	 */
	public String INFO_MSG_MEMORY = "info.memory";

	/** Constant for the Class Name symbolic */
	public String INFO_SUB_CLS_NAME = "className";

	/** Constant for the Count symbolic */
	public String INFO_SUB_COUNT = "count";

	/** Constant for the Count symbolic */
	public String INFO_SUB_DATETIME = "datetime";

	/** Constant for the Execution Line symbolic */
	public String INFO_SUB_EXEC_LINE = "executionLine";

	/** Constant for the free memory symbolic */
	public String INFO_SUB_MEM_FREE = "memFree";

	/** Constant for the maximum memory symbolic */
	public String INFO_SUB_MEM_MAX = "memMax";

	/** Constant for the total memory symbolic */
	public String INFO_SUB_MEM_TOTAL = "memTotal";

	/** Constant for the total memory symbolic */
	public String INFO_SUB_MEM_USED = "memUsed";
	
	/** Constant for the Method Name symbolic */
	public String INFO_SUB_MTHD_NAME = "methodName";
}
