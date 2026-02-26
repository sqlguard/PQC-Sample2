/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Mar 5, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.guardium.assessment.tests;

/**
 * @author msanayei
 *
 */
public class Constants 
{	
	public static final int    ADMIN_USERS_ID		= 1;
	public static final String AFTER_HOURS_WORK 	= "AFTER HOURS WORK";
	public static final String BEFORE_HOURS_WORK 	= "BEFORE HOURS WORK";
	public static final String SATURDAY_WORK 		= "SUNDAY";
	public static final String SUNDAY_WORK 			= "SATURDAY";
	public static final int TOTAL_REQUESTS_REPORT_ID = 163;
	public static final String MONITOR_ROLE 		= "GDMMONITOR";
	
	protected static enum Comparison {
		EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, NOT_EQUAL, EXISTS;
		public boolean compare(double x, double y) {
			switch (this) {
			case EQUAL:                 return x == y;
			case GREATER_THAN:          return x > y;
			case LESS_THAN:             return x < y;
			case GREATER_THAN_OR_EQUAL: return x >= y;
			case LESS_THAN_OR_EQUAL:    return x <= y;
			case NOT_EQUAL:	            return x != y;
			case EXISTS:                return true;
			default: throw new AssertionError(this);
			}
		}

		public String toString() {
			switch (this) {
			case EQUAL:                 return "=";
			case GREATER_THAN:          return ">";
			case LESS_THAN:             return "<";
			case GREATER_THAN_OR_EQUAL: return ">=";
			case LESS_THAN_OR_EQUAL:    return "<=";
			case NOT_EQUAL:             return "!=";
			case EXISTS:                return "can be";
			default: throw new AssertionError(this);
			}
		}
	}
}

