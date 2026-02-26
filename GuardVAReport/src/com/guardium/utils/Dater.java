/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 *
 * @author dtoland on Nov 2, 2007 at 5:33:49 PM
 */
public class Dater {
	/** Local static logger for class */
	private static final transient Logger LOG = Logger.getLogger(Dater.class);

	private static final int PRECISION = 2;
	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  /**
   * Convenience method will will modify today's date value by wrapping the
   * add() method in java.util.Calendar.
   * Date fields are constants defined in java.util.Calendar.
   * Adding a negative number will subtract.
   * @param field The date field as defined in java.util.Calendar.
   * @param amount The amount of to change the field by.
   * @return The date with the field modified. Null if the incoming date is null;
   * @see java.util.Calendar#add
   */
  public static Date add(int field, int amount) {
  	Date date = new Date();
    return add(date, field, amount);
  }

  /**
   * Convenience method will will modify a date field value by wrapping the
   * add() method in java.util.Calendar.
   * Date fields are constants defined in java.util.Calendar.
   * Adding a negative number will subtract.
   * @param date The date to be modified.
   * @param field The date field as defined in java.util.Calendar.
   * @param amount The amount of to change the field by.
   * @return The date with the field modified. Null if the incoming date is null;
   * @see java.util.Calendar#add
   */
  public static Date add(java.util.Date date, int field, int amount) {
    if (date==null) {
      LOG.error("Cannot add " + amount + " to empty date: \"" + date + "\"");
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(field, amount);

    if ( LOG.isDebugEnabled() ) {
    	LOG.debug("Added " + amount + " to "+ date + " and got: " + cal.getTime() );
    }
    Date result = cal.getTime();
    return result;
  }
  
  /**
   * @param date
   * @param amount
   * @return The date with the amount of minutes added.
   */
  public static Date addMinutes(Date date, int amount) {
  	Date result = add(date, Calendar.MINUTE, amount);
  	return result;
  }  
  
  /**
   * @param amount
   * @return The current time with the amount of minutes added.
   */
  public static Date addMinutes(int amount) {
  	Date result = add(Calendar.MINUTE, amount);
  	return result;
  }
  
  /**
   * @param date
   * @param amount
   * @return The date with the amount of seconds added.
   */
  public static Date addSeconds(Date date, int amount) {
  	Date result = add(date, Calendar.SECOND, amount);
  	return result;
  }  
  
  /**
   * @param amount
   * @return The current time with the amount of seconds added.
   */
  public static Date addSeconds(int amount) {
  	Date result = add(Calendar.SECOND, amount);
  	return result;
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double daysAfter(Date start, Date end) {
  	return hoursAfter( start.getTime(), end.getTime() );
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double daysAfter(long start, long end) {
  	long difference = end-start;
  	return millisToDays(difference, PRECISION);
  }

  /**
   * @param start The start time.
   * @return The difference to four decimal places.
   */
  public static double hoursAfter(Date start) {
  	Date end = new Date();
  	return hoursAfter(start, end);
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double hoursAfter(Date start, Date end) {
  	return hoursAfter( start.getTime(), end.getTime() );
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double hoursAfter(long start, long end) {
  	long difference = end-start;
  	return millisToHours(difference, PRECISION);
  }

  /**
   * @param start The start time.
   * @return The difference to four decimal places.
   */
  public static double minutesAfter(Date start) {
  	Date end = new Date();
  	return minutesAfter(start, end);
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double minutesAfter(Date start, Date end) {
  	return minutesAfter( start.getTime(), end.getTime() );
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double minutesAfter(long start, long end) {
  	long difference = end-start;
  	return millisToMinutes(difference, PRECISION);
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double secondsAfter(Date start, Date end) {
  	return secondsAfter( start.getTime(), end.getTime() );
  }

  /**
   * @param start The start time.
   * @param end The end time.
   * @return The difference to four decimal places.
   */
  public static double secondsAfter(long start, long end) {
  	long difference = end-start;
  	return millisToSeconds(difference, PRECISION);
  }

  /**
   * @param time The amount of milliseconds to convert to seconds.
   * @param decimalPlaces The number of decimal places to show.
   * @return the number of seconds for the number of milliseconds
   */
  public static double millisToDays(long time, int decimalPlaces) {
  	double divisor = 24*60*60*1000;
    return millisTo(time, divisor, decimalPlaces);
  }

  /**
   * @param time The amount of milliseconds to convert to seconds.
   * @param decimalPlaces The number of decimal places to show.
   * @return the number of seconds for the number of milliseconds
   */
  public static double millisToHours(long time, int decimalPlaces) {
  	double divisor = 60*60*1000;
    return millisTo(time, divisor, decimalPlaces);
  }

  /**
   * @param time The amount of milliseconds to convert to seconds.
   * @param decimalPlaces The number of decimal places to show.
   * @return the number of seconds for the number of milliseconds
   */
  public static double millisToMinutes(long time, int decimalPlaces) {
  	double divisor = 60*1000;
    return millisTo(time, divisor, decimalPlaces);
  }

  /**
   * @param time The amount of milliseconds to convert to seconds.
   * @param decimalPlaces The number of decimal places to show.
   * @return the number of seconds for the number of milliseconds
   */
  public static double millisToSeconds(long time, int decimalPlaces) {
  	double divisor = 1000;
    return millisTo(time, divisor, decimalPlaces);
  }

  /**
   * @param time The amount of milliseconds to convert to seconds.
   * @param divisor The divisor to get the value to millis
   * @param decimalPlaces The number of decimal places to show.
   * @return the number of seconds for the number of milliseconds
   */
  private static double millisTo(long time, double divisor, int decimalPlaces) {
  	BigDecimal bigTime = new BigDecimal(time);
  	BigDecimal bigDivisor = new BigDecimal(divisor);
  	bigTime = bigTime.divide(bigDivisor, decimalPlaces, ROUNDING_MODE);
    return bigTime.doubleValue();
  }

  /**
   * Sets the time component to a date so that the hours,
   * minutes, seconds and milliseconds equal zero.
   * @param date The date to have the time components zeroed.
   * @return The beginning-of-day date.  Null if the input was null.
   */
  public static Date toDayStart(Date date) {
    if (date==null) {
      LOG.error("Cannot set empty date: \"" + date + "\"");
      return null;
    }

    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND) );
    cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND) );
    cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE) );
    cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY) );

    if ( LOG.isDebugEnabled() ) {
    	LOG.debug("Set: " + date + " to: " + cal.getTime() );
    }
    return cal.getTime();
  }

  /**
   * Sets the time component to a date so that the hours,
   * minutes, seconds and milliseconds are their maximum.
   * @param date The date to have the time components maxed.
   * @return The end-of-day date.  Null if the input was null.
   */
  public static Date toDayEnd(Date date) {
    if (date==null) {
      LOG.error("Cannot set empty date: \"" + date + "\"");
      return null;
    }

    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY) );
    cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE) );
    cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND) );
    cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND) );

    if ( LOG.isDebugEnabled() ) {
    	LOG.debug("Set: " + date + " to: " + cal.getTime() );
    }
    return cal.getTime();
  }
}
