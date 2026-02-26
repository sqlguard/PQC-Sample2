/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.sql.*;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.torque.Torque;

import com.guardium.utils.i18n.Say;
import com.guardium.data.Stopwatch;

/**
 * Automates routine activities that cause endless re-typeing of the same stanzas.
 * @author dtoland on Nov 1, 2005 at 1:35:48 PM
 */
public final class Check {
  /** Local static logger for the class */
  //static final transient Logger LOG = Logger.getLogger(Check.class);

  /** Constant for the default level that will be used by {@link #freeMemory()} to trigger GC. */
  public static final transient int MIN_FREE_MEM = 512*1024;

  /**
   * Logs disposal operations that could not close their object.
   * @param object
   * @param t
   */
  private static void badClose(Object object, String classname, Throwable t) {
  	//if ( LOG.isDebugEnabled() ) {
  		/*
  		LOG.debug(
  				"Could not close: " + classname + ": '" + String.valueOf(object) + "'"
  				+ Say.NL + Informer.causality(t)
  		);
  		*/
  		String msg = "Could not close: " + classname + ": '" + String.valueOf(object) + "'"
  				+ Say.NL + Informer.causality(t);
  	//}
  }

  /**
	 * @param cls
	 * @return Just the class name with the package stripped.
	 */
	public static final String className(Class<?> cls) {
		if (cls==null) return null;
		return className( cls.getName() );
	}

  /**
	 * @param obj
	 * @return Just the class name with the package stripped.
	 */
	public static final String className(Object obj) {
		if (obj==null) return null;
		return className( obj.getClass() );
	}

  /**
	 * @param classname
	 * @return Just the class name with the package stripped.
	 */
	public static final String className(String classname) {
		if (classname==null) return null;
		return classname.substring( classname.lastIndexOf(".")+1 );
	}

  /**
   * @param source The value that is being searced.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(Collection<?> source, String value) {
  	for ( Object obj : source ) {
  		if ( String.valueOf(obj).equalsIgnoreCase(value) ) {
  			return true;
  		}
  	}
  	return false;
  }

  /**
   * @param source The value that is being searced.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(Object[] source, String value) {
  	for ( Object obj : source ) {
  		if ( String.valueOf(obj).equalsIgnoreCase(value) ) {
  			return true;
  		}
  	}
  	return false;
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, boolean value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, double value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, float value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, int value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, long value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(Object source, Object value) {
  	return contains( String.valueOf(source), String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, Object value) {
  	return contains(source, String.valueOf(value) );
  }

  /**
   * Uses a case insensitive comparison.
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, String value) {
  	return contains(source, value, false);
  }

  /**
   * @param source The value that is being searched.
   * @param value The value that is sought.
   * @param caseSensitive Whether the case of the strings should be considered in the comparison.
   * @return Whether the value was found in the source.
   */
  public static boolean contains(String source, String value, boolean caseSensitive) {
  	if ( isEmpty(source) || isEmpty(value) ) {
  		return false;
  	}
  	if (caseSensitive) {
  		return source.indexOf(value) >=0;
  	}
		return source.toLowerCase().indexOf( value.toLowerCase() ) >=0;
  }

  /**
   * Non-case-sensitive search.
   * @param source
   * @param value
   * @return The number of times that a value is found in the source.
   */
  public static int occurances(String source, String value) {
  	return occurances(source, value, false);
  }

  /**
   * @param source
   * @param value
   * @param caseSensitive
   * @return The number of times that a value is found in the source.
   */
  public static int occurances(String source, String value, boolean caseSensitive) {
  	if ( isEmpty(source) || isEmpty(value) ) {
  		return 0;
  	}

  	if (!caseSensitive) {
  		return occurances(source.toLowerCase(), value.toLowerCase(), true);
  	}

  	int count = 0;
  	int ndx = source.indexOf(value);
  	while (ndx >= 0) {
  		count++;
  		ndx = source.indexOf(value, ndx + 1);
  	}
  	return count;
  }

  /**
   * Finds the full file path for a path relative to the current directory.
   * Corrects the slash direction for the current operating system.
   * If the path is missing a file seperator at the start, one will be added.
   * <p><strong>Note:</strong> Does NOT test for existance of the finished path.</p>
   * @param path The path to be found.
   * @return The full file path for the passed in filename.
   */
  public static String currentDirectory(String path) {
  	String result;
    if ( isEmpty(path) ) {
    	result = File.separator;
    } else {
    	result = fileSeperation(path);
    }

    // see if we need a file seperator
    String sep = result.startsWith(File.separator) ? "" : File.separator;
    result = System.getProperty("user.dir") + sep + result;



    //if ( LOG.isDebugEnabled() ) LOG.debug("Found file path: '" + result + "' for: '" + path + "'.");
    return result;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.BufferedReader disposal(java.io.BufferedReader object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.BufferedReader", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.InputStream disposal(java.io.InputStream object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.InputStream", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.OutputStream disposal(java.io.OutputStream object) {
    try {
    	if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.OutputStream", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.Reader disposal(java.io.Reader object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.Reader", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.PrintWriter disposal(java.io.PrintWriter object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.PrintWriter", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.FileWriter disposal(java.io.FileWriter object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.FileWriter", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.Writer disposal(java.io.Writer object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.Writer", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.io.BufferedWriter disposal(java.io.BufferedWriter object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.io.BufferedWriter", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.net.Socket disposal(java.net.Socket object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.net.Socket", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.sql.CallableStatement disposal(java.sql.CallableStatement object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.sql.CallableStatement", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.sql.Connection disposal(java.sql.Connection object) {
    try {
      if (object!=null) { object.close();
      }
      goodClose(object);
    } catch (Throwable t) {
       badClose(object, "java.sql.Connection", t);
    }
    return null;
 }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.sql.PreparedStatement disposal(java.sql.PreparedStatement object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.sql.PreparedStatement", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.sql.ResultSet disposal(java.sql.ResultSet object) {
    try {
      if (object!=null) { object.close(); }
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.sql.ResultSet", t);
    }
    return null;
  }
  
  public static void disposal(ResultSet object,Map problem) {
	    try {
	      if (object!=null) { object.close(); }
	    } catch (Throwable t) {
	      problem.put("error",t.getMessage());
	    }
  }
  
  public static void disposal(Statement object,Map problem) {
	    try {
	      if (object!=null) { object.close(); }
	    } catch (Throwable t) {
	      problem.put("error",t.getMessage());
	    }
  }
  
  public static void disposal(Connection object,Map problem) {
	    try {
	      if (object!=null) { object.close(); }
	    } catch (Throwable t) {
	      problem.put("error",t.getMessage());
	    }
  }
  
  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static javax.sql.rowset.CachedRowSet disposal(javax.sql.rowset.CachedRowSet object) {
    try {
      if (object!=null) { object.close(); }
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "javax.sql.rowset.CachedRowSet", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.sql.Statement disposal(java.sql.Statement object) {
    try {
      if (object!=null) { object.close(); }
      goodClose(object);

    } catch (Throwable t) {
      badClose(object, "java.sql.Statement", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.util.Collection<?> disposal(java.util.Collection<?> object) {
    try {
    	if ( !isEmpty(object) ) {
    		object.clear();
    	}
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.util.Collection", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.util.List<?> disposal(java.util.List<?> object) {
    try {
    	if ( !isEmpty(object) ) {
    		object.clear();
    	}
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.util.List", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.util.Map<?,?> disposal(java.util.Map<?,?> object) {
    try {
    	if ( !isEmpty(object) ) {
     		object.clear();
    	}
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.util.Map", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static java.util.zip.ZipFile disposal(java.util.zip.ZipFile object) {
    try {
      if (object!=null) object.close();
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "java.util.zip.ZipFile", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static javax.naming.Context disposal(javax.naming.Context object) {
    try {
      if (object!=null) { object.close(); }
      goodClose(object);
    } catch (Throwable t) {
      badClose(object, "javax.naming.Context", t);
    }
    return null;
  }

  /**
   * Disposes of objects so they can be object collected.
   * @param object The object to be disposed.
   * @return The input object set to null.
   */
  public static Object disposal(Object object) {
    goodClose(object);
    return null;
  }

  /**
   * Null safe comparison of two strings.
   * @param a
   * @param b
   * @return whether the strings are not null and equal.
   */
  public static boolean equal(String a, String b) {
  	if (a==null || b==null) { return false; }
  	return a.equals(b);
  }

  /**
   * Sets forwards and backwards slashes to the proper system file seperator.
   * @param path Must not be used for URL's, only file system paths.
   *  URL slash direction is not file system dependant.
   * @return The incoming path with the file seperators corrected.
   *  If the path is empty, the input path is returned.
   */
  public static final String fileSeperation(String path) {
    if ( isEmpty(path) ) return path;

    String result = path;
    String slash = "/";
    String backslash = "\\";

    if ( !File.separator.equals(backslash) ) {
      result = result.replaceAll("\\" + backslash, File.separator);
    }

    if ( !File.separator.equals(slash) ) {
    	result = result.replaceAll(slash, File.separator);
    }

    return result;
  }

  /**
   * Checks to see if free memory is above the minimum.
   * If it is not, it requests garbage collection and waits a short while for the GC to occur.
   * @return The amount of free memory available. in KB.
   */
  public static long freeMemory() {
  	return freeMemory(MIN_FREE_MEM);
  }

  /**
   * Checks to see if free memory is above the minimum.
   * If it is not, it requests garbage collection and waits a short while for the GC to occur.
   * @param minMem The minimum amount of memory.
   * @return The amount of free memory available. in KB.
   */
  public static long freeMemory(int minMem) {
  	int maxTries = 30;
  	long delay = Stopwatch.SECOND.inMillis(2);
  	return freeMemory(minMem, maxTries, delay);
  }

  /**
   * Checks to see if free memory is above the minimum.
   * If it is not, it requests garbage collection and waits a short while for the GC to occur.
   * @param minMem The minimum amount of free memory to trigger a garbage collection request.
   * @param maxTries The number of tries it should wait for memory to free up
   * @param delay The amount of time each try should wait for memory to free up.
   * @return The amount of free memory available. in KB.
   */
  public static long freeMemory(int minMem, int maxTries, long delay) {
  	Stopwatch watch = new Stopwatch();
  	String beforeMem = null;

  	try {
  		long threshold = minMem;
  		long free = getFree();
  		int tries = 0;

  		// loop while memory is low, snoozing until some frees up
	  	while (free<=threshold && tries<maxTries) {
	  		threshold = free;
	  		if (tries==0) {
	  			beforeMem = Informer.memoryInfo();

	  		} else {
	  			/*
	  			if ( LOG.isDebugEnabled() ) {
	  				LOG.debug(
	  						"Try=" + tries + " Min=" + minMem/1024
	  						+ " Free=" + free/1024 + " Snooze=" + (tries * delay)
	  				);
	  			}
	  			*/
	  			String msg = "Try=" + tries + " Min=" + minMem/1024
  						+ " Free=" + free/1024 + " Snooze=" + (tries * delay);
	  		}

	  		Runtime.getRuntime().gc();
	  		tries++;
				Stopwatch.snooze(tries * delay);
				free = getFree();
	  	}

	  	// write a log entry if GC was requested
			//if ( (tries!=0) && LOG.isEnabledFor(Level.INFO) ) {
				String msg = Say.what(
						Say.INFO_MSG_GC_REQUEST,
						Say.INFO_SUB_COUNT, String.valueOf(tries)
				);
				msg =
						Informer.locationInfo(Check.class, 1)
						+ Say.NL + beforeMem
						+ Say.NL + watch.checkElapsed(msg)
						+ Say.NL + Informer.memoryInfo()
				;
				//LOG.info(msg);
			//}

			return getFree();

  	} catch (OutOfMemoryError e) {
  		//LOG.fatal("Out of Memory");
   		throw e;
  	}
  }

	 /**
   * @return The amount of free memory available.
   */
  public static long getFree() {
  	return Runtime.getRuntime().freeMemory();
  }

	/**
   * House cleaning after the object is closed.
   * @param object The object being closed
   */
  private static void goodClose(Object object) {
    /* do nothing at this time */
  }

	/**
   * Tests an object to see if it is null or empty.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(Collection<?> value) {
    if (value==null) return true;
    return value.isEmpty();
  }

	/**
   * Tests a file to see if it is null, doesn't exist or an empty file.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(File value) {
    if (value==null) return true;
    if ( !value.exists() ) return true;
    if ( value.length() ==0) return true;
    return false;
  }

	/**
   * Tests a connection to see if it is null, is closed.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(java.sql.Connection value) {
    if (value==null) return true;
    try {
			if ( value.isClosed() ) return true;
		} catch (SQLException e) {
			/*
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Could not check if the connection is closed.", e);
			}
			*/
			return true;
		}
    return false;
  }

  /**
   * Tests and object to see if it is null or an empty string.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(java.sql.Statement value) {
    if (value==null) return true;
    try {
    	// throws an exception if Statement, or statement's connectin is closed.
    	value.clearWarnings();
    } catch (Throwable t) {
    	return true;
    }
    return false;
  }

  /**
   * Tests an object to see if it is null or empty.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(Map<?,?> value) {
    if (value==null) return true;
    return value.isEmpty();
  }

  /**
   * Tests and object to see if it is null or an empty string (excluding whitespace).
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(Object value) {
    if (value==null) return true;
    return Check.isEmpty( value.toString() );
  }

  /**
   * Tests an object to see if it is null or an empty string (excluding whitespace).
   * @param array The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(Object[] array) {
    if (array==null) return true;
    for (int i=0; i<array.length; i++) {
        if ( !Check.isEmpty(array[i]) ) return false;
    }
    return true;
  }

  /**
   * Tests a String to see if it is null or empty (excluding whitespace).
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(String value) {
    if (value==null) return true;
    if (value.trim().length()==0) return true;
    return false;
  }
  /**
   * Tests a String to see if it is null or empty (including whitespace).
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isTrulyEmpty(String value) {
    if (value==null) return true;
    if (value.length()==0) return true;
    return false;
  }

  /**
   * Tests a StringBuffer to see if it is null or empty, excluding whitespace.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(StringBuffer value) {
    if (value==null) return true;
    return Check.isEmpty( value.toString() );
  }

  /**
   * Tests a StringBuilder to see if it is null or empty, excluding whitespace.
   * @param value The object to test.
   * @return Whether the value is empty or not.
   */
  public static boolean isEmpty(StringBuilder value) {
    if (value==null) return true;
    return Check.isEmpty( value.toString() );
  }

  /**
   * @param cls
   * @return The package converted to a path for the file system.
   */
  public static final String packagePath(Class<?> cls) {
  	return packagePath( cls.getPackage() );
  }

  /**
   * @param obj
   * @return The package converted to a path for the file system.
   */
  public static final String packagePath(Object obj) {
  	return packagePath( obj.getClass() );
  }

  /**
   * @param pkg
   * @return The package converted to a path for the file system.
   */
  public static final String packagePath(Package pkg) {
  	// package string is prefixed with "package "
  	return packagePath( pkg.toString().substring( "package ".length() ) );
  }

  /**
	 * @param pkg
	 * @return The package converted to a path for the file system.
	 */
	public static final String packagePath(String pkg) {
		return pkg.replaceAll("\\.", File.separator) + File.separator;
	}

  /**
   * Tests that a value is found within a range.
   * @param value The value being compared
   * @param min The minimum value;
   * @param max If max is less than min, there is no maximum.
   * @return Whether the value is in the range defined by min and max, inclusive.
   */
  public static boolean range(double value, double min, double max) {
		return value>=min && (min>max || value<=max);
  }

  /**
   * Tests that a value is found within a range.
   * Range is inclusive.
   * @param value The value being compared
   * @param min The minimum value;
   * @param max If max is less than min, there is no maximum.
   * @return Whether the value is in the range defined by min and max, inclusive.
   */
  public static boolean range(long value, long min, long max) {
		return value>=min && (min>max || value<=max);
  }

	public static boolean same(Object o1, Object o2)
	{
		if(o1==null||o2==null){
			if(o1!= o2)
				return false;
		}
		else
		{
			if(!o1.equals(o2))
				return false;
		}
		return true;
	}
	
	public static boolean isValidIp(String s) {
		//return Utils.isIpAddress(s);
		return true;
	}
	
	public static boolean isInRange(Integer val, int minVal, int maxVal) {
		if (val<minVal && val>maxVal)
			return false;
		else
			return true;
	}
	
	public static boolean isValidPort(Integer port) {
		return isInRange(port, 0, 65535);
	}
	
	public static boolean isValidPortOrRange(String portStr) {
		if (portStr == null)
			return false;
		String[] ports = portStr.split("-");
		if (ports.length > 2)
			return false;
		for (int i = 0; i< ports.length; i++){
			try{
				int port = Integer.valueOf(ports[i]);
				if (!isValidPort(port))
					return false;
			}catch(NumberFormatException ne){
				return false;
			}
		}
		return true;
	}


	public static boolean isValidPortList(String portList) {
		String[] ports = portList.split(",");
		for (int i = 0; i< ports.length; i++){
			if (!isValidPortOrRange(ports[i]))
				return false;
		}
		return true;
	}
}
