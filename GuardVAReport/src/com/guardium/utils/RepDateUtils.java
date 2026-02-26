/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Jan 11, 2004
 *
 * ?? Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material may not                        
 * be copied, modified, altered, published, distributed, or otherwise displayed without the                        
 * express written consent of Guardium, Inc. 
 * 
 */
package com.guardium.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.guardium.utils.GuardRepGeneralException;
import com.guardium.date.ThreadSafe_SimpleDateFormat;
import com.guardium.utils.Check;
import com.guardium.utils.i18n.SayAppRes;

/**
 * @author dario
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RepDateUtils {
	public static final ThreadSafe_SimpleDateFormat dateFormat = new ThreadSafe_SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final String mySqlLatestDateStr = "2037-12-31 23:59:59" ;
	public static Date mySqlLatestDate;
	static { 
	         try {
	         	//mySql has a timestamp limitation for December 31st 2037
	         	mySqlLatestDate = dateFormat.parse(mySqlLatestDateStr);
	         } catch (ParseException e){
	         	e.printStackTrace();
	         }
	       }
	
	
	public static Calendar getCalendarRelativeDate(String relativeDate)	throws GuardRepGeneralException {
		Calendar cal = new GregorianCalendar();
		Calendar cal1 = new GregorianCalendar();
		try {
			StringTokenizer stoken = new StringTokenizer(relativeDate, " ");
			if (stoken.countTokens() != 4)
				throw (new GuardRepGeneralException("Wrong parameter String for Relative Date", 213));
			String s1 = stoken.nextToken();
			String of = stoken.nextToken();
			String s2 = stoken.nextToken();
			String unit = stoken.nextToken();
			// Step 1 Set the date
			if (unit.equalsIgnoreCase("day")) {
				if (s2.equalsIgnoreCase("last"))
					cal.add(Calendar.DATE, -1);
				if (s2.equalsIgnoreCase("previous"))
					cal.add(Calendar.DATE, -2);
			} else if (unit.equalsIgnoreCase("month")) {
				if (s2.equalsIgnoreCase("last"))
					cal.add(Calendar.MONTH, -1);
				if (s2.equalsIgnoreCase("previous"))
					cal.add(Calendar.MONTH, -2);
				if (s1.equalsIgnoreCase("start"))
					cal.set(Calendar.DAY_OF_MONTH, 1);
				if (s1.equalsIgnoreCase("end"))
					cal.set(
						Calendar.DAY_OF_MONTH,
						cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else if (unit.equalsIgnoreCase("week")) {
				if (s1.equalsIgnoreCase("start"))
					cal.set(
						Calendar.DAY_OF_WEEK,
						cal.getActualMinimum(Calendar.DAY_OF_WEEK));
				if (s1.equalsIgnoreCase("end"))
					cal.set(
						Calendar.DAY_OF_WEEK,
						cal.getActualMaximum(Calendar.DAY_OF_WEEK));
				if (s2.equalsIgnoreCase("last"))
					cal.add(Calendar.DATE, -7);
				if (s2.equalsIgnoreCase("previous"))
					cal.add(Calendar.DATE, -14);
			} else { // Specific week day
				if (unit.equalsIgnoreCase("SUNDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				}
				if (unit.equalsIgnoreCase("MONDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				}
				if (unit.equalsIgnoreCase("TUESDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
				}
				if (unit.equalsIgnoreCase("WEDNESDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
				}
				if (unit.equalsIgnoreCase("THURSDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
				}
				if (unit.equalsIgnoreCase("FRIDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
				}
				if (unit.equalsIgnoreCase("SATURDAY")) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				}
				// If the day is in the future (i.e. wednesday greater than today), take the previous one
				// >> "last" is now defined, "this" is not supported for specific week days. Treated by this 
				// method as "last"
				if (cal.getTimeInMillis() >= cal1.getTimeInMillis())
					cal.add(Calendar.DATE, -7);
				if (s2.equalsIgnoreCase("previous"))
					cal.add(Calendar.DATE, -7);
			}
			// Set the time
			if (s1.equalsIgnoreCase("start")) {
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			if (s1.equalsIgnoreCase("End")) {
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
			}
			return cal;
		} catch (Exception ex) {
			throw (new GuardRepGeneralException("Failed to Covert Relative Date to Real Date (input = " + relativeDate + ")", 214));
		}
	}
	public static String getFormatedDate(Calendar cal) {
		String dat = "";
		String year   = new Integer(cal.get(Calendar.YEAR)).toString();
		String month  = new Integer(cal.get(Calendar.MONTH) + 1).toString();
		String day    = new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString();
		String hour   = new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString();
		String minute = new Integer(cal.get(Calendar.MINUTE)).toString();
		String second = new Integer(cal.get(Calendar.SECOND)).toString();
		if (month.length() == 1) month = "0" + month ;
		if (day.length() == 1) day = "0" + day ;
		if (hour.length() == 1) hour = "0" + hour ;
		if (minute.length() == 1) minute = "0" + minute ;
		if (second.length() == 1) second = "0" + second ;
		dat = year +"-"+ month +"-"+ day +" "+ hour +":"+ minute +":"+ second;
		return dat;
	}
	// Added by Rosa on Sep, 2013
	public static String getShortFormatedDate(Calendar cal) {
		String dat = "";
		String year   = new Integer(cal.get(Calendar.YEAR)).toString();
		String month  = new Integer(cal.get(Calendar.MONTH) + 1).toString();
		String day    = new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString();
		String hour   = new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString();
		String minute = new Integer(cal.get(Calendar.MINUTE)).toString();
		String second = new Integer(cal.get(Calendar.SECOND)).toString();
		if (month.length() == 1) month = "0" + month ;
		if (day.length() == 1) day = "0" + day ;
		if (hour.length() == 1) hour = "0" + hour ;
		if (minute.length() == 1) minute = "0" + minute ;
		if (second.length() == 1) second = "0" + second ;
		dat = year + month + day + hour + minute + second;
		return dat;
	}
	public static Calendar getRelativeCalander(Calendar cal, String interval, String units) {
	
		if ( interval.indexOf('+') != -1 )
			interval = interval.replace('+',' ').trim();
		if (units.equalsIgnoreCase("HOUR")) 
			cal.add(Calendar.HOUR_OF_DAY, new Integer(interval).intValue() );
		if (units.equalsIgnoreCase("MONTH")) 
			cal.add(Calendar.MONTH, new Integer(interval).intValue() );
		if (units.equalsIgnoreCase("MINUTE")) 
			cal.add(Calendar.MINUTE, new Integer(interval).intValue() );
		if (units.equalsIgnoreCase("DAY")) 
			cal.add(Calendar.DATE, new Integer(interval).intValue() );
		if (units.equalsIgnoreCase("WEEK")) 
			cal.add(Calendar.DATE, new Integer(interval).intValue() *7 );
		return cal;	
	}
	
	public static String getFormatedDate(Calendar cal, String interval, String units)
	{
	
		return getFormatedDate(getRelativeCalander(cal, interval, units));
	}
	
	public static boolean checkDate(String date, String pattern) {
		// create one instance of SimpleDateFormat per thread as SimpleDateFormat is not thread safe.
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		// set Lenient(false) make it not accept spillover dataes such as "2012-10-25 13:59:78" or "2012-09-32 00:00:00"
		sdf.setLenient(false);

		Date testDate = null;
				
		try {
			testDate = sdf.parse(date);
		}
		catch (ParseException e) {
			return false;
		}
			    
		if (!sdf.format(testDate).equals(date)) {
	    	   return false;
    	}
		return true;
	}
	
	
	public static Date getRealDate(String inpDate) throws GuardRepGeneralException, ParseException {
		String retDate = "";
		Date outDate = null;
		if (inpDate.length() >= 3 && inpDate.substring(0, 3).equalsIgnoreCase("NOW")) { // Start with "NOW"
			Calendar cal = new GregorianCalendar();
			if (inpDate.trim().equalsIgnoreCase("NOW")) // Equals "NOW", get formated date: "yyyymmddhhmmss"
				retDate = RepDateUtils.getFormatedDate(cal);
			else { // input Date like NOW +/- UNITS
				StringTokenizer stoken =
					new StringTokenizer(inpDate.substring(3), " ");
				if (stoken.countTokens() != 2) // Wrong number of parameters, throw exception
					throw (	new GuardRepGeneralException( "Wrong parameter String for datetime",208));
				// Else inpDate like NOW +/- UNITS and parameters OK, get formated date "yyyymmddhhmmss"
				String interval = stoken.nextToken();
				String units = stoken.nextToken();
				
				// getRelativeCalander  support only the following units: adding defensive code for cases where the date format is not
				// validated in the screen (for example api calls)
				if (!units.equalsIgnoreCase("DAY") && !units.equalsIgnoreCase("HOUR") && !units.equalsIgnoreCase("MONTH") && !units.equalsIgnoreCase("MINUTE") && !units.equalsIgnoreCase("WEEK") )
					throw new GuardRepGeneralException("Invalid Unit");
				
				retDate = RepDateUtils.getFormatedDate(cal, interval, units);
			}
		}
		// Else if really Relative Date (Start/End Of last/previous... day/month...)
		else if ( (inpDate.length() >= 3 && 
		           inpDate.substring(0, 3).equalsIgnoreCase("END")) || 
		          (inpDate.length() >= 5 && 
		           inpDate.substring(0, 5).equalsIgnoreCase("START"))) {
			try {
				Calendar cal = RepDateUtils.getCalendarRelativeDate(inpDate);
				retDate = RepDateUtils.getFormatedDate(cal);
			} 
			catch (GuardRepGeneralException ge) {
				throw ge;
			}
		} 
		else { // Real date (inpDate like "yyyy-MM-dd HH:mm:ss"
			retDate = inpDate;
			try {
				outDate = dateFormat.parse(retDate);
			} 
			catch (ParseException ex) {
				//04-04-2013 myang 33648
				throw new ParseException(SayAppRes.say("datetime.error.format" , dateFormat.toPattern()) , ex.getErrorOffset());
			}
			return outDate;
		}
		
		// ret Date define above (from relative format) as "yyyy-MM-dd HH:mm:ss"		
		try {
			outDate = dateFormat.parse(retDate);
		} 
		catch (ParseException ex) {
			throw ex;
		}
		return outDate;
	}
	
	
	
	/**
	 *
	 * Use this method to format a date using the pattern
	 *
	 * @param date		Date to format
	 * @param pattern	Pattern used to format the date
	 * @return 	String presentation of the formatted date
	 */
	public static String getFormatedDate(String date,String pattern) throws Exception
	{
		String formatedDate = new String();
		if(!Check.isEmpty(date))
		{
			Date relDate = getRealDate(date);
			if(Check.isEmpty(pattern))
			{
				pattern = "yyyy-MM-dd HH:mm:ss";
			}
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			formatedDate = sdf.format(relDate);
		}
		return formatedDate;
	}

}
