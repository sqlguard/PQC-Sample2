/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import com.guardium.utils.i18n.Say;
import com.guardium.utils.AbstractInnerType;

/**
 * @author dtoland on May 19, 2006 at 2:03:55 PM
 */
public class Stopwatch implements java.io.Serializable{

	/** Constant value for zero */
	protected static final transient BigDecimal ZERO = new BigDecimal("0");

	/** Constant value for one */
	protected static final transient BigDecimal ONE = new BigDecimal("1");

	/** Constant value for the rounding of division operations */
	protected static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

	/** Constant precision and rounding context for calculations */
	protected static final MathContext CALC_CONTEXT = new MathContext(12, ROUNDING);

	/** Constant precision and rounding context for calculations */
	protected static final MathContext DISPLAY_CONTEXT = new MathContext(2, ROUNDING);

	/** Constant Scale for the number of milliseconds in a millisecond */
	public static final transient Scale MILLI = new Scale(
			ONE, Say.what(Say.TIME_LIT_MILLI), Say.what(Say.TIME_LIT_MILLIS)
	);

	/** Constant Scale for the number of milliseconds in a second */
	public static final transient Scale SECOND = new Scale(
			MILLI.multiply("1000"), Say.what(Say.TIME_LIT_SEC), Say.what(Say.TIME_LIT_SECS)
	);

	/** Constant Scale for the number of milliseconds in a minute */
	public static final transient Scale MINUTE = new Scale(
			SECOND.multiply("60"), Say.what(Say.TIME_LIT_MIN), Say.what(Say.TIME_LIT_MINS)
	);

	/** Constant Scale for the number of milliseconds in a hour */
	public static final transient Scale HOUR = new Scale(
			MINUTE.multiply("60"), Say.what(Say.TIME_LIT_HR), Say.what(Say.TIME_LIT_HRS)
	);

	/** Constant Scale for the number of milliseconds in a day */
	public static final transient Scale DAY = new Scale(
			HOUR.multiply("60"), Say.what(Say.TIME_LIT_DAY), Say.what(Say.TIME_LIT_DAYS)
	);

	/** Constant Scale for the number of milliseconds in a week */
	public static final transient Scale WEEK = new Scale(
			DAY.multiply("7"), Say.what(Say.TIME_LIT_WK), Say.what(Say.TIME_LIT_WKS)
	);

	/** Constant Scale for the number of milliseconds in a year */
	public static final transient Scale YEAR = new Scale(
			DAY.multiply("365.25"), Say.what(Say.TIME_LIT_YR), Say.what(Say.TIME_LIT_YRS)
	);

	/** Constant Scale for the number of milliseconds in a year */
	public static final transient Scale MONTH = new Scale(
			DAY.multiply(365.25/12), Say.what(Say.TIME_LIT_MON), Say.what(Say.TIME_LIT_MONS)
	);

	/** Constant array of all the available scales */
	public static final Scale[] SCALES = { MILLI, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR };

	private long start = 0L;
	private long stop = 0L;
	private long rateUnits = 0L;
	private long lastAlarm = 0L;

	/**
	 * Creates a stopwatch and starts it.
	 */
	public Stopwatch() {
		this.start();
	}

	/**
	 * Creates a stopwatch and starts it.
	 * @param date Initializes the stopwatch to the time found in the date.
	 */
	public Stopwatch(Date date) {
		this.start = date.getTime();
	}

	/**
	 * Sets the stop time.
	 * Can be called repeatedly without starting.
	 * @return The millis that have elapsed since start
	 */
	public long stop() {
		if ( this.isRunning() ) {
			this.stop = System.currentTimeMillis();
		}
		return this.stop - this.start;
	}

	/**
	 * Resets the rate units and sets the start time to now.
	 * @return The start time in millis.
	 */
	public long start() {
		this.start = System.currentTimeMillis();
		this.stop = 0L;
		this.rateUnits = 0L;
		return this.start;
	}

	/**
	 * @return Whether the Stopwatch is running.
	 */
	public boolean isRunning() {
		return this.start!=0L && this.stop==0L;
	}

	/**
	 * Increments the current rate units by one.
	 * @return The current count of rate units.
	 */
	public long bump() {
		return this.bump(1L);
	}

	/**
	 * @param count The number to incriment the rate units.
	 * @return The current count of rate units.
	 */
	public long bump(long count) {
		this.rateUnits += count;
		return this.rateUnits;
	}

	/**
	 * @return The elapsed millisconds.
	 */
	public BigDecimal elapsedTime() {
		long end;
		if ( this.isRunning() ) {
			end = System.currentTimeMillis();
		} else {
			end = this.stop;
		}
		return bd(end - this.start);
	}

	/**
	 * @return The elapsed milliseconds.
	 */
	public long elapsed() {
		return this.elapsedTime().longValue();
	}

	/**
	 * @param units
	 * @param time
	 * @return The unit per timeframe.
	 */
	public static BigDecimal rate(BigDecimal time, BigDecimal units) {
		if ( isZero(time) ) return ZERO;
		return units.divide(time, CALC_CONTEXT);
	}

	/**
	 * @param units
	 * @param time
	 * @param scale
	 * @return The unit per timeframe.
	 */
	public static BigDecimal rate(BigDecimal time, BigDecimal units, Scale scale) {
		return rate( scale.divide(time), units);
	}

	/**
	 * @param units
	 * @param time
	 * @return The unit per timeframe.
	 */
	public static BigDecimal rate(long time, long units) {
		return rate( bd(time), bd(units) );
	}

	/**
	 * @return Whether any rate units have been recorded.
	 */
	public boolean hasRateUnits() {
		return this.getRateUnits()!=0;
	}

	/**
	 * @return The Rate Units.
	 */
	public long getRateUnits() {
		return this.rateUnits;
	}

	/**
	 * @return The Rate Units.
	 */
	public int getRateUnitsAsInt() {
		BigDecimal units = bd(this.rateUnits);
		return units.intValueExact();
	}

	/**
	 * @param period The length of time.
	 * @param scale The scale of time
	 * @return Whether the amount of time has elapsed since the start or the last alarm.
	 */
	public boolean isTime(int period, Scale scale) {
		long millis = scale.inMillis(period);
		return isTime(millis);
	}

	/**
	 * @param date The start time date to check.
	 * @param period The length of time.
	 * @param scale The scale of time
	 * @return Whether the amount of time has elapsed since the start or the last alarm.
	 */
	public static boolean isTime(Date date, int period, Scale scale) {
		long start = date.getTime();
		long end = System.currentTimeMillis();
		long millis = scale.inMillis(period);
		return isTime(start, end, millis);
	}

	/**
	 * @param period The length of time in milliseconds.
	 * @return Whether the amount of time has elapsed since the start or the last alarm.
	 */
	public boolean isTime(long period) {
		long start = 0L;
		if (this.getLastAlarm()!=0) {
			start = this.getLastAlarm();
		} else {
			start = this.getStart();
		}

		long end = 0L;
		if ( this.isRunning() ) {
			end = System.currentTimeMillis();
		} else {
			end = this.getStop();
		}

		boolean time = isTime(start, end, period);
		if (time) {
			this.setLastAlarm();
		}
		return time;
	}

	/**
	 * @param start The start time in millis
	 * @param end The end time in millis
	 * @param period The length of time in milliseconds.
	 * @return Whether the amount of time has elapsed since the start or the last alarm.
	 */
	protected static boolean isTime(long start, long end, long period) {
		long elapsed = end - start;
		boolean time = elapsed > period;
		return time;
	}

	/**
	 * @return The milliseconds of the start time.
	 */
	public long getStart() {
		return this.start;
	}

	/**
	 * @return The Date and Time of the start time.
	 */
	public Date getStartTime() {
		return new Date( this.getStart() );
	}

	/**
	 * @return The milliseconds of the stop time.
	 */
	public long getStop() {
		return this.stop;
	}

	/**
	 * @return The Date and Time of the stop time.
	 */
	public Date getStopTime() {
		return new Date( this.getStop() );
	}

	/**
	 * Sets the last alarm to now
	 */
	public void setLastAlarm() {
		this.lastAlarm = System.currentTimeMillis();
	}

	/**
	 * @return The Date and Time of the stop time.
	 */
	public long getLastAlarm() {
		return this.lastAlarm;
	}

	/**
	 * @return The Date and Time of the stop time.
	 */
	public Date getLastAlarmTime() {
		return new Date( this.getLastAlarm() );
	}

	public static String getDisplayTime(double time, Scale scale) {
		long millis = scale.inMillis(time);
		return  getDisplayTime(millis);
	}

	/**
	 * @param time
	 * @param scale
	 * @return
	 */
	public static String getDisplayTime(long time, Scale scale) {
		long millis = scale.inMillis(time);
		return  getDisplayTime(millis);
	}

	/**
	 * @param millis
	 * @return The number of millis scaled to the most readable range followed by the label.
	 */
	public static String getDisplayTime(long millis) {
		Scale scale = scaleTime(millis);
		BigDecimal time = formatForDisplay( scale.divide(millis) );
		return time + " " + scale.findLabel(time);
	}

	/**
	 * Stops the Stopwatch and reports on elapsed time and rate.
	 * Uses the millisecond scale.
	 * @param msg A description of what occurred.
	 * @return A string describing the elapsed time and rate since the Stopwatch was started.
	 */
	public String checkElapsed(String msg) {
		long time = this.stop();
		Scale scale = scaleTime(time);
		BigDecimal elapsed = formatForDisplay( scale.divide(time) );
		String details = Say.what(
				Say.TIME_MSG_ELAPSED,
				Say.TIME_SUB_UNIT_OF_MEASURE, msg,
				Say.TIME_SUB_ELAPSED, String.valueOf(elapsed),
				Say.TIME_SUB_SCALE, scale.findLabel(elapsed)
		);
		return details;
	}
	public String getElapsed() {
		long time = this.stop();
		Scale scale = scaleTime(time);
		BigDecimal elapsed = formatForDisplay( scale.divide(time) );
		String details =  String.valueOf(elapsed);
		return details;
	}

	/**
	 * Stops the Stopwatch and reports on elapsed time and rate.
	 * @param uom The string  describig the rate units.
	 * @return A string describing the elapsed time and rate since the Stopwatch was started.
	 */
	public String checkSpeed(String uom) {
		return this.checkSpeed( this.rateUnits, uom);
	}

	/**
	 * Stops the Stopwatch and reports on elapsed time and rate.
	 * @param units The number of units that were processed.
	 * @param uom The string  describig the rate units.
	 * @return A string describing the elapsed time and rate since the Stopwatch was started.
	 */
	public String checkSpeed(long units, String uom) {
		return this.checkSpeed( bd(units), uom);
	}

	/**
	 * Stops the Stopwatch and reports on elapsed time and rate.
	 * @param units The number of units that were processed.
	 * @param uom The string  describig the rate units.
	 * @return A string describing the elapsed time and rate since the Stopwatch was started.
	 */
	public String checkSpeed(BigDecimal units, String uom) {
		this.stop();
		String elapsedMsg = Say.what(
				Say.TIME_MSG_RATE_ELAPSED,
				Say.TIME_SUB_RATE_UNITS, String.valueOf( formatForDisplay(units, 0)),
				Say.TIME_SUB_UNIT_OF_MEASURE, uom
		);
		elapsedMsg = this.checkElapsed(elapsedMsg);

		String speedMsg = Say.DOT;
		if ( !isZero(units) ) {
			BigDecimal elapsed = this.elapsedTime();
			Scale scale = scaleRate(elapsed, units);
			BigDecimal rate = rate(elapsed, units, scale);
			rate = formatForDisplay(rate);
			speedMsg = Say.what(
					Say.TIME_MSG_RATE,
					Say.TIME_SUB_RATE, String.valueOf(rate),
					Say.TIME_SUB_UNIT_OF_MEASURE, uom,
					Say.TIME_SUB_SCALE, scale.getSingular()
			);
		}
		return elapsedMsg + speedMsg;
	}

	/**
	 * Automatically calculates the largest scale that results in a number larger than zero.
	 * @param time The number for which to calculate the scale in milliseconds.
	 * @return The calculated scale.
	 */
	public static Scale scaleTime(BigDecimal time) {
		Scale result = MILLI;
		for (int i=0; i<SCALES.length; i++) {
			if ( SCALES[i].isGreaterThan(time) ) {
				return result;
			}
			result = SCALES[i];
		}
		return result;
	}

	/**
	 * Automatically calculates the largest scale that results in a number larger than zero.
	 * @param time The number for which to calculate the scale in milliseconds.
	 * @param units The number of units processed in that time
	 * @return The calculated scale.
	 */
	public static Scale scaleRate(BigDecimal time, BigDecimal units) {
		Scale result = MILLI;
		BigDecimal rate;
		for (int i=0; i<SCALES.length; i++) {
			rate = rate(time, units, SCALES[i]);
			if ( ONE.compareTo(rate) <=0) {
				result = SCALES[i];
				break;
			}
		}
		return result;
	}

	/**
	 * Automatically calculates the largest scale that results in a number larger than zero.
	 * @param time The number of millisconds for which to calculate the scale.
	 * @return The calculated scale.
	 */
	public static Scale scaleTime(long time) {
		return scaleTime( bd(time) );
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Start: " + this.start + " Stop: " + this.stop + " Units: " + this.rateUnits;
	}

	/**
	 * Pauses for the specified amount of time in millis.
	 * @param wait The length of time to wait.
	 * @param scale The scale of the length of time.
	 */
	public static void snooze(double wait, Scale scale) {
		snooze( scale.inMillis(wait) );
	}

	/**
	 * Pauses for the specified amount of time
	 * @param wait The length of time to wait in millis
	 */
	public static void snooze(long wait) {
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// Interupted, but don't care
		}
	}

	/**
	 * @param value
	 * @return a double with the number of decimal points for easy viewing.
	 */
	private static BigDecimal formatForDisplay(BigDecimal value) {
		return formatForDisplay(value, DISPLAY_CONTEXT);
	}

	/**
	 * @param value
	 * @return a double with the number of decimal points for easy viewing.
	 */
	private static BigDecimal formatForDisplay(BigDecimal value, int scale) {
		MathContext ctx = new MathContext(scale, ROUNDING);
		return formatForDisplay(value, ctx);
	}

	/**
	 * @return a double with the number of decimal points for easy viewing.
	 */
	private static BigDecimal formatForDisplay(BigDecimal value, MathContext ctx) {
		BigDecimal result = value.setScale(ctx.getPrecision(), ctx.getRoundingMode() );
		int ext = 1;
		while( isZero(result) && ext<=9) {
			result = value.setScale(ctx.getPrecision() +ext, ROUNDING);
			ext++;
		}

		// still zero, get rid of all those decimal places
		if ( isZero(result) ) {
			result = value.setScale(1, ROUNDING);
		}
		return result;
	}

	/**
	 * @param value
	 * @return BigDecimal that represents the long value.
	 */
	public static BigDecimal bd(long value) {
		return BigDecimal.valueOf(value);
	}

	/**
	 * @param value
	 * @return BigDecimal that represents the long value.
	 */
	public static BigDecimal bd(double value) {
		return BigDecimal.valueOf(value);
	}

	/**
	 * @param value
	 * @return Whether the BigDecimal value is zero.
	 */
	public static boolean isZero(BigDecimal value) {
		return value.compareTo(ZERO)==0;
	}

	/**
	 * @param value
	 * @return Whether the BigDecimal value is zero.
	 */
	public static boolean isOne(BigDecimal value) {
		return value.compareTo(ONE)==0;
	}

	/**
	 * Used to type time scales
	 * @author dtoland on Oct 3, 2006 at 12:45:24 PM
	 */
	public static class Scale extends AbstractInnerType {
		private final BigDecimal multiplier;
		private final String singular;
		private final String plural;

		/**
		 * @param multiplier The number of milliseconds that equals this scale.
		 * @param singular The singular name of the scale (ex. 'Second').
		 * @param plural The plural name of the scale (ex. 'Seconds').
		 */
		protected Scale(BigDecimal multiplier, String singular, String plural) {
			super(plural);
			this.multiplier = multiplier;
			this.singular = singular;
			this.plural = plural;
		}

		/**
		 * @param amount The amount to convert.
		 * @return The amount of this scale converted to milliseconds.
		 */
		public long inMillis(double amount) {
			BigDecimal multiplicand = bd(amount);
			MathContext ctx = new MathContext(0, ROUNDING);
			return this.getMultiplier().multiply(multiplicand, ctx).longValue();
		}

		/**
		 * @param amount The amount to convert.
		 * @return The amount of this scale converted to milliseconds.
		 */
		public long inMillis(long amount) {
			return this.getMultiplier().longValue() * amount;
		}

		/**
		 * @return The number of milliseconds in this scale.
		 */
		public BigDecimal getMultiplier() {
			return this.multiplier;
		}

		/**
		 * @return The singular name of the scale.
		 */
		public String getSingular() {
			return this.singular;
		}

		/**
		 * @return The plural name of the scale
		 */
		public String getPlural() {
			return this.plural;
		}

		/**
		 * Returns the singular name if the value is exactly one.
		 * Otherwise, returns the plural name.
		 * @param value
		 * @return The label for this scale / value combination.
		 */
		public String findLabel(long value) {
			return this.findLabel( String.valueOf(value) );
		}

		/**
		 * Returns the singular name if the value is exactly one.
		 * Otherwise, returns the plural name.
		 * @param value
		 * @return The label for this scale / value combination.
		 */
		public String findLabel(double value) {
			return this.findLabel( String.valueOf(value) );
		}

		/**
		 * Returns the singular name if the value is exactly one.
		 * Otherwise, returns the plural name.
		 * @param value
		 * @return The label for this scale / value combination.
		 */
		public String findLabel(String value) {
			return this.findLabel( new BigDecimal(value) );
		}

		/**
		 * Returns the singular name if the value is exactly one.
		 * Otherwise, returns the plural name.
		 * @param value
		 * @return The label for this scale / value combination.
		 */
		public String findLabel(BigDecimal value) {
			if ( isOne(value) ) {
				return this.getSingular();
			}
			return this.getPlural();
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are less than a unit of this scale.
		 */
		public boolean isLessThan(long millis) {
			return this.compareTo(millis) <0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are less than a unit of this scale.
		 */
		public boolean isLessThan(double millis) {
			return this.compareTo(millis) <0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are less than a unit of this scale.
		 */
		public boolean isLessThan(String millis) {
			return this.compareTo(millis) <0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are less than a unit of this scale.
		 */
		public boolean isLessThan(BigDecimal millis) {
			return this.compareTo(millis) <0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are more than a unit of this scale.
		 */
		public boolean isGreaterThan(long millis) {
			return this.compareTo(millis) >0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are more than a unit of this scale.
		 */
		public boolean isGreaterThan(double millis) {
			return this.compareTo(millis) >0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are more than a unit of this scale.
		 */
		public boolean isGreaterThan(String millis) {
			return this.compareTo(millis) >0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis are more than a unit of this scale.
		 */
		public boolean isGreaterThan(BigDecimal millis) {
			return this.compareTo(millis) >0;
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis less than, equal, or greater than this scale.
		 */
		public int compareTo(double millis) {
			return this.compareTo( String.valueOf(millis) );
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis less than, equal, or greater than this scale.
		 */
		public int compareTo(long millis) {
			return this.compareTo( String.valueOf(millis) );
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis less than, equal, or greater than this scale.
		 */
		public int compareTo(String millis) {
			return this.compareTo( new BigDecimal(millis) );
		}

		/**
		 * @param millis The number of milliseconds to compare.
		 * @return Whether the millis less than, equal, or greater than this scale.
		 */
		public int compareTo(BigDecimal millis) {
			return this.multiplier.compareTo(millis);
		}

		/**
		 * <code> dividend / this.getMultiplier()</code>
		 * @param dividend The number to be divided.
		 * @return The quotient of dividing the dividend by this scale.
		 */
		public BigDecimal divide(long dividend) {
			return this.divide( String.valueOf(dividend) );
		}

		/**
		 * <code> dividend / this.getMultiplier()</code>
		 * @param dividend The number to be divided.
		 * @return The quotient of dividing the dividend by this scale.
		 */
		public BigDecimal divide(double dividend) {
			return this.divide( String.valueOf(dividend) );
		}

		/**
		 * <code> dividend / this.getMultiplier()</code>
		 * @param dividend The number to be divided.
		 * @return The quotient of dividing the dividend by this scale.
		 */
		public BigDecimal divide(String dividend) {
			return this.divide( new BigDecimal(dividend) );
		}

		/**
		 * <code> dividend / this.getMultiplier()</code>
		 * @param dividend The number to be divided.
		 * @return The quotient of dividing the dividend by this scale.
		 */
		public BigDecimal divide(BigDecimal dividend) {
			return dividend.divide( this.getMultiplier(), CALC_CONTEXT);
		}

		/**
		 * @param multiplicand The number by which to multiply this scale.
		 * @return The product of this scale and the multiplcand.
		 */
		public BigDecimal multiply(long multiplicand) {
			return this.multiply( String.valueOf(multiplicand) );
		}

		/**
		 * @param multiplicand The number by which to multiply this scale.
		 * @return The product of this scale and the multiplcand.
		 */
		public BigDecimal multiply(double multiplicand) {
			return this.multiply( String.valueOf(multiplicand) );
		}

		/**
		 * @param multiplicand The number by which to multiply this scale.
		 * @return The product of this scale and the multiplcand.
		 */
		public BigDecimal multiply(String multiplicand) {
			return this.multiply( new BigDecimal(multiplicand) );
		}

		/**
		 * @param multiplicand The number by which to multiply this scale.
		 * @return The product of this scale and the multiplcand.
		 */
		public BigDecimal multiply(BigDecimal multiplicand) {
			return multiplicand.multiply( this.getMultiplier() );
		}
	}
}
