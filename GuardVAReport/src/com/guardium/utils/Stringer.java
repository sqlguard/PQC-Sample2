/*
* � Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material
* may not be copied, modified, altered, published, distributed, or otherwise
* displayed without the express written consent of Guardium, Inc.
*/

package com.guardium.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import com.guardium.net.Host;
import com.guardium.net.IPAddress;
import com.guardium.utils.i18n.Say;

/**
 * @author dtoland on Aug 10, 2006 at 1:53:50 PM
 */
public class Stringer {

  /** constant key for creating weak unique descriptions */
	private static final AtomicLong atomBase = new AtomicLong();
	private static final AtomicLong atomWork = new AtomicLong();

	private static long getDiscriminator() {
		GregorianCalendar cal = new GregorianCalendar();
		StringBuilder buf = new StringBuilder();
		buf.append( cal.get(GregorianCalendar.YEAR) );
		buf.append( cal.get(GregorianCalendar.MONTH) +1);
		buf.append( Stringer.padLeftZeros( cal.get(GregorianCalendar.DAY_OF_MONTH), 2) );
		buf.append( Stringer.padLeftZeros( cal.get(GregorianCalendar.HOUR_OF_DAY), 2) );
		buf.append( Stringer.padLeftZeros( cal.get(GregorianCalendar.MINUTE), 2) );
		buf.append( Stringer.padLeftZeros( cal.get(GregorianCalendar.SECOND), 2) );
		buf.append("000");

		long base = Long.parseLong( buf.toString() );
		if (base == atomBase.get() ) {
			return atomWork.incrementAndGet();
		}

		atomBase.set(base);
		atomWork.set( atomBase.get() );
		return atomWork.incrementAndGet();
	}

	/**
	 * @param input The string to escape
	 * @return The string escaped for use in javascript.
	 */
	public static String escapeJavascript(String input) {
		if ( Check.isEmpty(input) ) {
			return input;
		}

		boolean escaped = false;
		StringBuilder out = new StringBuilder();
		char[] seq = input.toCharArray();
		for ( char chr : seq ) {
			switch (chr) {
				case '\b':
					out.append( (escaped ? "\\\\\\b" : "\\b") );
					escaped = false;
					break;

				case 'b':
					out.append( (escaped ? "\\b" : "b") );
					escaped = false;
					break;

				case '\n':
					out.append( (escaped ? "\\\\\\n" : "\\n") );
					escaped = false;
					break;

				case 'n':
					out.append( (escaped ? "\\n" : "n") );
					escaped = false;
					break;

				case '\t':
					out.append( (escaped ? "\\\\\\t" : "\\t") );
					escaped = false;
					break;

				case 't':
					out.append( (escaped ? "\\t" : "t") );
					escaped = false;
					break;

				case '\f':
					out.append( (escaped ? "\\\\\\f" : "\\f") );
					escaped = false;
					break;

				case 'f':
					out.append( (escaped ? "\\f" : "f") );
					escaped = false;
					break;

				case '\r':
					out.append( (escaped ? "\\\\\\r" : "\\r") );
					escaped = false;
					break;

				case 'r':
					out.append( (escaped ? "\\r" : "r") );
					escaped = false;
					break;

				case '\'':
					out.append("\\'");
					escaped = false;
					break;

				case '\"':
					out.append("\\\"");
					escaped = false;
					break;

				case '\\':
					if (escaped) {
						out.append("\\\\");
						escaped = false;
					} else {
						escaped = true;
					}
					break;

				default:
					if (escaped) {
						out.append("\\\\");
						escaped = false;
					}
					out.append(chr);
					break;
			}
		}
		if (escaped) {
			out.append("\\\\");
			escaped = false;
		}
		return  out.toString();

	}

	private static boolean swap(StringBuilder buf, char orig, String replace, boolean prescape) {
		boolean result = false;
		if (!prescape) {
			buf.append(replace);
		} else {
			buf.append(orig);
		}
		return result;
	}

	/**
	 * Appends a list separator to the buffer if it needs it.
	 * @param buffer The StringBuffer to affect.
	 * @return The StringBuffer with a new line if necessary
	 */
	public static StringBuilder listSep(StringBuilder buffer) {
		return delimit(buffer, Say.LIST_SEP);
	}
	public static StringBuilder listSep(StringBuilder buffer,String delim) {
		// buffer is empty or already ends with a new line
		if ( !Check.isEmpty(buffer) && !buffer.toString().endsWith(delim) ) {
			buffer.append(delim);
		}
		return buffer;
	}
	/**
	 * Checks the buffer to see if it is empty and appends a space and a new line.
	 * Will not append the space and newline if they already end the contained char array.
	 * The space makes the string readable if viewed in a format that does not honor newline,
	 * like html or the debugger.
	 * @param buffer The StringBuffer to affect.
	 * @return The StringBuffer with a new line if necessary
	 */
	public static StringBuilder newLn(StringBuilder buffer) {
		return delimit(buffer, Say.SP + Say.NL);
	}

	/**
	 * Checks the buffer to see if it is empty and appends a space and a new line.
	 * Will not append the space and newline if they already end the contained char array.
	 * The space makes the string readable if viewed in a format that does not honor newline,
	 * like html or the debugger.
	 * @param buffer The StringBuffer to affect.
	 * @return The StringBuffer with a new line if necessary
	 */
	public static StringBuffer newLn(StringBuffer buffer) {
		return delimit(buffer, Say.SP + Say.NL);
	}
	
	/**
	 * Adds a delimiter to the end of the string if it is not already their.
	 * @param buffer
	 * @param delimiter
	 * @return The buffer with the delimiter at the end
	 */
	public static StringBuffer delimit(StringBuffer buffer, String delimiter) {
		// buffer is empty or already ends with a new line
		if ( !Check.isEmpty(buffer) && !buffer.toString().endsWith(delimiter) ) {
			buffer.append(delimiter);
		}
		return buffer;
	}
		
	/**
	 * Adds a delimiter to the end of the string if it is not already their.
	 * @param buffer
	 * @param delimiter
	 * @return The buffer with the delimiter at the end
	 */
	public static StringBuilder delimit(StringBuilder buffer, String delimiter) {
		// buffer is empty or already ends with a new line
		if ( !Check.isEmpty(buffer) && !buffer.toString().endsWith(delimiter) ) {
			buffer.append(delimiter);
		}
		return buffer;
	}

  /**
   * Converts a collection of objects to an array of string.
   * @param collection The collection to be converted.
   * @return an array with the string value of each collection element.
   */
  public static String[] collectionToStringArray(Collection<?> collection) {
    String[] result = new String[collection.size()];
    int ndx = 0;

    for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
      result[ndx++] = String.valueOf( it.next() );
    }
    return result;
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param enumeration The Collection to be turned into a delimited string
   * @return The list turned to a delimited string.
   */
  public static String enumerationToString(Enumeration<?> enumeration) {
    return enumerationToString(enumeration, Say.LIST_SEP, Say.EMPTY);
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param enumer The Enumeration to be turned into a delimited string
   * @param delim The delimiter used to separate values.
   * @param quote The delimiter used to surround Strings.
   * @return The list turned to a delimited string.
   */
  public static String enumerationToString(Enumeration<?> enumer, String delim, String quote) {
    StringBuffer buf = new StringBuffer();

    while ( enumer.hasMoreElements() ) {
    	Object value = enumer.nextElement();
    	buf.append( stringify(value, quote) );
     	if ( enumer.hasMoreElements() ) buf.append(delim);
    }

    return buf.toString();
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
   * @return The list turned to a delimited string.
   */
  public static String collectionToString(Collection<?> collect) {
    return collectionToString(collect, Say.LIST_SEP, Say.EMPTY);
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
   * @param delim The delimiter to use between elements.
   * @return The list turned to a delimited string.
   */
  public static String collectionToString(Collection<?> collect, String delim) {
    return collectionToString(collect, delim, Say.EMPTY);
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
   * @param delim The delimiter used to separate values.
   * @param quote The delimiter used to surround Strings.
   * @return The list turned to a delimited string.
   */
  public static String collectionToString(Collection<?> collect, String delim, String quote) {
    StringBuilder buf = new StringBuilder();
    for ( Object value : collect) {    
     	delimit(buf, delim);
     	buf.append( stringify(value, quote) );
    }
    return buf.toString();
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
    * @return The list turned to a delimited string.
   */
  public static String mapToString(Map<?, ?> collect) {
  	return mapToString(collect, Say.LIST_SEP, Say.EMPTY);
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
   * @param delim The delimiter used to separate values.
   * @return The list turned to a delimited string.
   */
  public static String mapToString(Map<?, ?> collect, String delim) {
   	return mapToString(collect, delim, Say.EMPTY);
  }

  /**
   * Takes a List and turns it into a delimited string.
   * @param collect The Collection to be turned into a delimited string
   * @param delim The delimiter used to separate values.
   * @param quote The delimiter used to surround Strings.
   * @return The list turned to a delimited string.
   */
  public static String mapToString(Map<?, ?> collect, String delim, String quote) {
  	Collection<?> entries = collect.entrySet();
  	return collectionToString(entries, delim, quote);
  }

  /**
   * @param collect The collection being described.
   * @return A bean description of all the properties of all the objects in the collection.
   * @see #describeCollection(Collection, String)
   */
  public static String describeCollection(Collection<?> collect) {
  	return describeCollection(collect, Say.NL);
  }

  /**
   * @param collect The collection being described.
   * @param recSep A delimiting string to put between each member of the collection's output.
   * @return A bean description of all the properties of all the objects in the collection.
   */
  public static String describeCollection(Collection<?> collect, String recSep) {
    StringBuffer buf = new StringBuffer();

    for (Iterator<?> it = collect.iterator(); it.hasNext(); ) {
      Object value = it.next();
    	//buf.append( Beaner.describe(value) );
    	if ( it.hasNext() ) buf.append(recSep);
    }

    return buf.toString();
  }

  /**
   * @param value The delimited string.
   * @return A list of tokens.
   */
  public static List<String> stringToList(String value) {
    List<String> result = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer(value, ":,;\"' \t\n\r\f");
    while ( tok.hasMoreTokens() ) {
    	result.add( tok.nextToken() );
    }
    return result;
  }

  /**
   * Takes the incoming Object and turns is into a String and adds the proper delimiters.
   * @param value The Object to convert
   * @param delim The delimiter used to separate values.
   * @param quote The delimiter used to surround Strings.
   * @return The Object turned into a delimited String element.
   */
  private static String stringify(Object value, String quote) {
  	if ( value==null ) {
  		return Say.EMPTY;
  	}

  	// if the quote is null, replace it with an empty string
  	String qt = (quote==null) ?  Say.EMPTY : quote;
  	
  	// no quoting, just return the string
  	if ( Check.isEmpty(qt) ) {
  		return String.valueOf(value);
  	}

    // numbers are not quoted
    if (Number.class.isAssignableFrom( value.getClass() ) ) {
      return String.valueOf(value);
    }

    // Strings are quoted
    return qt + String.valueOf(value) + qt;
  }

  /**
   * Converts a array into a delimited String.
   * Uses the default delimiters.
   * @param array The array to convert
   * @param delim The delimiter used to separate values.
   * @return The array flattened to a delimited String.
   */
  public static String arrayToString(Object[] array, String delim){
  	return arrayToString(array, delim, Say.EMPTY);
  }

  /**
   * Converts a array into a delimited String.
   * Uses the default delimiters.
   * @param array The array to convert
   * @return The array flattened to a delimited String.
   */
  public static String arrayToString(Object[] array){
  	return arrayToString(array, Say.LIST_SEP, Say.EMPTY);
  }

  /**
   * Converts a array into a delimited String.
   * @param array The array to convert
   * @param delim The delimiter used to separate values.
   * @param quote The delimiter used to surround Strings.
   * @return The array flattened to a delimited String.
   */
  public static String arrayToString(Object[] array, String delim, String quote) {
    StringBuffer buf = new StringBuffer();
    for (Object value : array) {
     	delimit(buf, delim);
     	buf.append( stringify(value, quote) );
    }
    return buf.toString();
  }

  public static String arrayToString(Object[] array, String delim, int start){
	StringBuilder sb = new StringBuilder();
	for (int i = start; i < array.length; i++)
	{
		if(i!=start)
			sb.append(delim);
		sb.append(array[i]);
	}
  	return sb.toString();
  }
  
  /**
   * @param data The string to evaluate.
   * @return The number of lines separated by line separators.
   *  If the string is empty, 0 will be returned.
   *  If the string has values, but no new lines, 1 will be returned
   */
  public static int countLines(String data) {
  	if ( Check.isEmpty(data) ) {
  		return 0;
  	}
  	String[] array = data.split(Say.NL);
  	return array.length;
  }

  /**
   * Takes a string with line breaks and turns them into html line breaks.
   * WARNING: DOESN'T WORK THROUGH STRUTS TAGS (like bean:write) --
   * the html break tags will be converted into lt br gt tags, then that
   * text just displayed to the user
   * @param value
   * @return The string as html.
   */
  public static String htmlify(String value) {
  	String result = new String(value);
  	if ( !Check.isEmpty(result) ) {
  		result=result.replaceAll("\\n", "<br>");
  	}
  	return result;
  }

  /**
   * @param value The value to pad.
   * @param length the length of the resulting string.
   * @return The value padded with zeros.
   */
  public static String padLeftZeros(double value, int length) {
  	return fillLeft( String.valueOf(value), length, "0");
  }

  /**
   * @param value The value to pad.
   * @param length the length of the resulting string.
   * @return The value padded with zeros.
   */
  public static String padRightZeros(double value, int length) {
  	return fillRight( String.valueOf(value), length, "0");
  }

  /**
   * @param value The value to pad.
   * @param length the length of the resulting string.
   * @return The value padded with zeros.
   */
  public static String padLeftZeros(long value, int length) {
  	return fillLeft( String.valueOf(value), length, "0");
  }

  /**
   * @param value The value to pad.
   * @param length the length of the resulting string.
   * @return The value padded with zeros.
   */
  public static String padRightZeros(long value, int length) {
  	return fillRight( String.valueOf(value), length, "0");
  }

  /**
   * @param target
   * @param value
   * @return The target string with all occurrences of the value removed from the beginning.
   */
  public static String stripLeft(String target, char value) {
  	if ( Check.isEmpty(target) ) {
  		return target;
  	}

  	StringBuilder result = new StringBuilder();
  	char[] stream = target.toCharArray();
  	boolean on = true;
  	for ( char chr : stream ) {
  		if (chr == value) {
  			if (on) { continue; }
  		} else if (on) {
  			on = false;
  		}
			result.append(chr);
  	}
  	return result.toString();
  }

  /**
   * @param target
   * @param value
   * @return The target string with all occurrences of the value removed from the end.
   */
  public static String stripRight(String target, char value) {
  	String reverse = new StringBuilder(target).reverse().toString();
  	reverse = stripLeft(reverse, value);
  	reverse = new StringBuilder(reverse).reverse().toString();
  	return reverse;
  }

  /**
   * @param length The length of the result string.
   * @param filler The set of characters to repeat until the length is accomplished.
   * @return The original value, right justified, with the rest of the string filled with the filler.
   */
  public static String fill(int length, String filler) {
  	StringBuffer buf = new StringBuffer();
  	while ( buf.length() <length) {
  		buf.append(filler);
  	}
  	return buf.substring( buf.length() - length);
  }

  /**
   * @param value The original value.
   * @param length The length of the result string.
   * @param filler The set of characters to repeat until the length is accomplished.
   * @return The original value, right justified, with the rest of the string filled with the filler.
   */
  public static String fillLeft(String value, int length, String filler) {
  	int len = Math.max(length - value.length(), 0);
  	return fill(len, filler) + value;
  }

  /**
   * @param value The original value.
   * @param length The length of the result string.
   * @param filler The set of characters to repeat until the length is accomplished.
   * @return The original value, right justified, with the rest of the string filled with the filler.
   */
  public static String fillRight(String value, int length, String filler) {
  	int len = Math.max(length - value.length(), 0);
  	return value + fill(len, filler);
  }

  /**
   * @param hostname The hostname.
   * @return The array of bytes returned from an InetAddress as a IP String.
   */
  public static String ip(String hostname) {
	  Host host = new Host(hostname);
	  IPAddress addr = host.resolve();
	  if(addr == null || addr.getBytes() == null) {
		  return Say.UNKNOWN;
	  }
	  return ip(addr.getBytes());
  }

  /**
   * @param inetAddress The address.
   * @return The array of bytes returned from an InetAddress as a IP String.
   */
  public static String ip(InetAddress inetAddress) {
  	byte[] bytes = inetAddress.getAddress();
  	return ip(bytes);
  }

  /**
   * @param bytes The array of bytes returned from an InetAddress.
   * @return The array of bytes as a IP String.
   */
  public static String ip(byte[] bytes) {
	return IPAddress.from(bytes).toString();
  }

	/**
	 * @param cls
	 * @return a unique description.
	 */
	public static String uniqueName(Class<?> cls) {
		return uniqueName( Check.className(cls) );
	}

	/**
	 * @param obj
	 * @return a unique description.
	 */
	public static String uniqueName(Object obj) {
		return uniqueName( Check.className(obj) );
	}

	/**
	 * @param suffix
	 * @return a unique description.
	 */
	public static String uniqueName(String suffix) {
		return uniqueName("", suffix, 0);
	}

	/**
	 * @param value
	 * @param maxLength The maximum length of the string. Zero is no maximum.
	 * @return a unique description.
	 */
	public static String uniqueName(String value, int maxLength) {
		return uniqueName(value, "", maxLength);
	}

	/**
	 * @param prefix
	 * @param suffix
	 * @return a unique description.
	 */
	public static String uniqueName(String prefix, String suffix) {
		return uniqueName(prefix, suffix, 0);
	}

	/**
	 * @param prefix
	 * @param suffix
	 * @param maxLength The maximum length of the string. Zero is no maximum.
	 * @return The most possible uniqueness and with the most possible value.
	 */
	public static String uniqueName(String prefix, String suffix, int maxLength) {
		String head = emptyNull(prefix);
		String tail = emptyNull(suffix);
		String headDlm = Check.isEmpty(head) ? Say.EMPTY : Say.DASH;
		String tailDlm = Check.isEmpty(tail) ? Say.EMPTY : Say.DASH;
		String unique = String.valueOf( getDiscriminator() );

		// no limit or within limit
		int len = head.length() + headDlm.length() + unique.length() + tailDlm.length() + tail.length();
		if (maxLength==0 || maxLength>=len) {
			return head + headDlm + unique + tailDlm + tail;
		}

		// get the prefix and some suffix
		len = head.length() + headDlm.length() + unique.length() + tailDlm.length();
		if (maxLength>len) {
			return (head + headDlm + unique + tailDlm + tail).substring(0, maxLength);
		}

		// get some prefix
		len = headDlm.length() + unique.length();
		if (maxLength>len) {
			len = Math.min( head.length(), (maxLength-len) );
			head = head.substring(0, len) + headDlm + unique + tailDlm + tail;
			return head.substring(0, maxLength);
		}

		// get the tail of unique
		len = unique.length() - Math.min(maxLength, unique.length() );
		return unique.substring(len);
	}

	/**
	 * @param value
	 * @param maxLen
	 * @return A string value of the object with a maximum length.
	 */
	public static String limit(Object value, int maxLen) {
		return maxLen( String.valueOf(value), maxLen);
	}

	/**
	 * @param value
	 * @param maxLen
	 * @return A string value of the object with a maximum length.
	 */
	public static String maxLen(String value, int maxLen) {
		if ( value.length() <maxLen)  {
			return value;
		}
		return value.substring(0, maxLen);
	}

	/**
	 * @param value The string to check
	 * @return if the value is null, returns an empty string.
	 *  Otherwise returns the value;
	 */
	public static String emptyNull(String value) {
		if (value==null) {
			return Say.EMPTY;
		}
		return value;
	}

	/**
	 * @param buffer
	 * @param prefix
	 * @return Whether the buffer starts with the suffix
	 */
	public static boolean startsWith(StringBuffer buffer, String prefix) {
		return buffer.toString().startsWith(prefix);
	}

	/**
	 * @param buffer
	 * @param prefix
	 * @return Whether the buffer starts with the suffix
	 */
	public static boolean startsWith(StringBuilder buffer, String prefix) {
		return buffer.toString().startsWith(prefix);
	}

	/**
	 * @param buffer
	 * @param suffix
	 * @return Whether the buffer ends with the suffix
	 */
	public static boolean endsWith(StringBuffer buffer, String suffix) {
		return buffer.toString().endsWith(suffix);
	}

	/**
	 * @param buffer
	 * @param suffix
	 * @return Whether the buffer ends with the suffix
	 */
	public static boolean endsWith(StringBuilder buffer, String suffix) {
		return buffer.toString().endsWith(suffix);
	}

	public static String displayMillisToDayHourMinSec(long time) {
		String display="";

		int division_sec=1000;
		int division_min=division_sec*60;
		int division_hour=division_min*60;
		int division_day=division_hour*24;


		long day=(long)Math.floor(time/division_day);
		if(day>0) {
			String dayStr=day>1?Say.what("com.time.days"):Say.what("com.time.day");
			display=display.concat(day + " " +dayStr);
		}

		double mod=time%division_day;
		if (mod>0) {
			long hour=(long)Math.floor(mod/division_hour);
			if(hour>0) {
				String hStr=hour>1 ? Say.what("com.time.hours"):Say.what("com.time.hour");
				display=display.concat(" "+hour+ " "+ hStr);
			}
			mod=mod%division_hour;
			if (mod>0) {		// minute display
				long min=(long)Math.floor(mod/division_min);
				if(min>0) {
					String mStr=min>1 ? Say.what("com.time.minutes"):Say.what("com.time.minute");
					display=display.concat(" "+min + " "+ mStr);
				}
				mod=mod%division_min;
				if (mod>0) {	// second display
					long sec=(long)Math.floor(mod/division_sec);
					if(sec>=1) {
						String sStr=sec >1? Say.what("com.time.seconds"):Say.what("com.time.second");
						display=display.concat(" " +sec + " "+ sStr);
					} else {	//additional
						String msStr=" <1 "+Say.what("com.time.second");
						if (display.length()>2) {
							msStr="";
						}

						display=display.concat(msStr);
					}
				}
			}

		} else {
			if (time<=0) {
				String zeroStr="0 " +Say.what("com.time.second");
				display=display.concat(zeroStr);
			}
		}

		return display;
	}
    public static String convertToMysqlStmtVal(String str)
    {
    	String ret = str.replaceAll("\\\\", "\\\\\\\\");
    	return ret.replaceAll("'","''");
    }
    public static String removeSqlInjection(String str)
    {
    	return str.replaceAll(";", " ");
    }
    
    public static boolean replaceParOnce(StringBuilder sb, String parName, String parVal)
	{
		int ind = sb.indexOf("?"+parName);
		if(ind==-1)
			return false;
		sb.replace(ind, ind + parName.length() + 1, parVal);
		return true;
	}

    public static String getConnectionName(String connectionName)
    {
    	return connectionName.replaceAll("\\.","_");
    }

	public static String getAccessorMethodStr(String parameterName){
		String [] splitStr = parameterName.split("_");
		String methodName = "";
		for (int i=0 ; i < splitStr.length ; i++ )
			methodName+=splitStr[i].toLowerCase().replaceFirst("^.", ""+Character.toUpperCase(splitStr[i].charAt(0)));
		return methodName;
	}
}
