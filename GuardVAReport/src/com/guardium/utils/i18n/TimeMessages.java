/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils.i18n;

/**
 * @author dtoland on Aug 2, 2006 at 11:32:02 AM
 */
public interface TimeMessages {

	/** Constant literal for singlular day */
	public String TIME_LIT_DAY = "com.time.day";

	/** Constant literal for the day abbreviation */
	public String TIME_LIT_DAY_SYMBOL = "com.time.symbol.day";

	/** Constant literal for plural days */
	public String TIME_LIT_DAYS = "com.time.days";

	/** Constant literal for singlular hour */
	public String TIME_LIT_HR = "com.time.hour";

	/** Constant literal for the hour abbreviation */
	public String TIME_LIT_HR_SYMBOL = "com.time.symbol.hour";

	/** Constant literal for plural hours */
	public String TIME_LIT_HRS = "com.time.hour";

	/** Constant literal for singlular millisecond */
	public String TIME_LIT_MILLI = "com.time.milli";

	/** Constant literal for the second abbreviation */
	public String TIME_LIT_MILLI_SYMBOL = "com.time.symbol.milli";

	/** Constant literal for plural milliseconds */
	public String TIME_LIT_MILLIS = "com.time.millis";

	/** Constant literal for singlular minute */
	public String TIME_LIT_MIN = "com.time.minute";

	/** Constant literal for the minute abbreviation */
	public String TIME_LIT_MIN_SYMBOL = "com.time.symbol.minute";

	/** Constant literal for plural minutes */
	public String TIME_LIT_MINS = "com.time.minutes";

	/** Constant literal for singlular month */
	public String TIME_LIT_MON = "com.time.month";

	/** Constant literal for the second abbreviation */
	public String TIME_LIT_MON_SYMBOL = "com.time.symbol.month";

	/** Constant literal for plural months */
	public String TIME_LIT_MONS = "com.time.months";

	/** Constant literal for singlular second */
	public String TIME_LIT_SEC = "com.time.second";

	/** Constant literal for the second abbreviation */
	public String TIME_LIT_SEC_SYMBOL = "com.time.symbol.second";

	/** Constant literal for plural seconds */
	public String TIME_LIT_SECS = "com.time.seconds";

	/** Constant literal for singlular week */
	public String TIME_LIT_WK = "com.time.week";

	/** Constant literal for the second abbreviation */
	public String TIME_LIT_WK_SYMBOL = "com.time.symbol.week";

	/** Constant literal for plural weeks */
	public String TIME_LIT_WKS = "com.time.weeks";

	/** Constant literal for singlular year */
	public String TIME_LIT_YR = "com.time.year";

	/** Constant literal for plural years */
	public String TIME_LIT_YRS = "com.time.years";

	/** Constant literal for the second abbreviation */
	public String TIME_LIT_YR_SYMBOL = "com.time.symbol.year";

	/**
	 * Constant for elapsed time message.
	 * @see #TIME_SUB_ELAPSED
	 * @see #TIME_SUB_SCALE
	 * @see #TIME_SUB_UNIT_OF_MEASURE
	 */
	public String TIME_MSG_ELAPSED = "com.time.elapsed";

	/**
	 * Constant for speed rate message.
	 * @see #TIME_SUB_RATE
	 * @see #TIME_SUB_UNIT_OF_MEASURE
	 */
	public String TIME_MSG_RATE = "com.time.speed";

	/**
	 * Constant for speed elapsed time message
	 * @see #TIME_SUB_RATE_UNITS
	 * @see #TIME_SUB_UNIT_OF_MEASURE
	 */
	public String TIME_MSG_RATE_ELAPSED = "com.time.speed.elapsed.msg";

	/** Constant for elapsed time symbol  */
	public  String TIME_SUB_ELAPSED = "timeElapsed";

	/** Constant for rate symbol */
	public  String TIME_SUB_RATE = "rate";

	/** Constant for units symbol */
	public  String TIME_SUB_RATE_UNITS = "rateUnits";

	/** Constant for scale name symbol */
	public  String TIME_SUB_SCALE = "timeScale";

	/** Constant for units symbol */
	public  String TIME_SUB_UNIT_OF_MEASURE = "unitOfMeasure";

}
