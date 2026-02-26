/*
* � Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material
* may not be copied, modified, altered, published, distributed, or otherwise
* displayed without the express written consent of Guardium, Inc.
*/
/*
 * Created on Nov 8, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.guardium.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.CachedRowSet;

import org.apache.log4j.Category;
//import org.apache.torque.Torque;
//import org.apache.torque.TorqueException;
//import org.apache.torque.util.Criteria;

import com.guardium.utils.GuardGeneralException;
//import com.guardium.GuardGeneralException;
import com.guardium.data.DataSourceConnectException;
import com.guardium.data.OracleResultSetWrapper;
import com.guardium.date.ThreadSafe_SimpleDateFormat;
//import com.guardium.dbSource.OracleResultSetWrapper;
import com.guardium.net.Host;
import com.guardium.net.IPAddress;
import com.guardium.net.IPAddressString;
import com.guardium.utils.i18n.SayAppRes;
import com.guardium.utils.runtime.CommandExecutor;
import com.guardium.utils.runtime.SequentialCharSequence;
//import com.ibm.xml.crypto.util.Base64;
//import com.sun.rowset.CachedRowSetImpl;


/**
 * @author guy
 *
 * A place for general purpose utility methods
 */
public class Utils
{
	private static String pId = null;
	private static final String CASE_INSENSITIVE="1";
	private static final String EXCLUDED_CHARACTERS="[\\\\$|&;'\"()!]";
	private static final String EXCLUDED_DIR_CHARACTERS="[$|&;'\"()! ]";
	
	// 2012-05-16 sbuschman 29619
	static public final Pattern nameSpecialCharacters;

	static {
		String chars = SayAppRes.say("name.specialCharacters");
		nameSpecialCharacters = Pattern.compile("[" + chars + "]");
	}
	

	// Date formatting / un-formatting
	private static final ThreadSafe_SimpleDateFormat sortableDateFormat = new ThreadSafe_SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ThreadSafe_SimpleDateFormat filenameDateFormat = new ThreadSafe_SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss's'");

	public static final ThreadSafe_SimpleDateFormat screenDataFormat =  new ThreadSafe_SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// public static final ThreadSafe_SimpleDateFormat screenDataFormatSimple =  new ThreadSafe_SimpleDateFormat(); // not used

	public static final ThreadSafe_SimpleDateFormat simpleDateFormat =  new ThreadSafe_SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static final ThreadSafe_SimpleDateFormat numDateFormat = new ThreadSafe_SimpleDateFormat("yyMMddHHmmss");

//	private static TableLoggerIfc auditLogger = null;
	
	public static String formatFilenameFriendlyDateString(Date date)	{
		return filenameDateFormat.format(date);
	}
	public static String formatSortFriendlyDateString(Date date)	{
		return sortableDateFormat.format(date);
	}
	public static Date parseFilenameFriendlyDateString(String dateString) throws ParseException	{
		return filenameDateFormat.parse(dateString);
	}
	public static Date parseSortFriendlyDateString(String dateString) throws ParseException	{
		return sortableDateFormat.parse(dateString);
	}

    /**
     *  Update the input table's(remote) input column  with  the input value <b>String</b>
     *  as an AES_ENCRYPT value, using the input where clause to determine which
     *  row(s) get updated.
     * @param value <b>String</b>
     * @param columnName <b>String</b>  In MySql form Case counts!!
     * @param tableName <b>String</b>  In MySql form Case counts!!
     * @param whereClause <b>String</b> everything after "WHERE".  Can be null or empty
     * @throws Exception
     */
	
	/*
    public static void encryptAndSaveRemote(String value, String columnName, String tableName,
            String whereClause)
        throws Exception
    {
        Connection c = null;
        Statement st = null;
        ResultSet res = null;
        
        value = value.replaceAll("'","''");
        value = replaceBackSlashes(value);
        
        whereClause = (whereClause==null)?"":whereClause;
        if (!whereClause.equals(""))
            whereClause = " WHERE " + whereClause;
        if (value!=null && !value.equals("") &&
                columnName!=null && !columnName.equals("") &&
                tableName!= null && !tableName.equals(""))
        {
            String statement = "UPDATE " + tableName + " SET " + columnName +
                " = AES_ENCRYPT('" + value + "','" +
                GlobalProperties.getGlobalProperties().getSharedSecret()+"') "
                + whereClause;
            try
            {
                c = Torque.getConnection("guard_remote");
                st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                int rows = st.executeUpdate(statement);
                if (rows == 0)
                    throw new Exception ("encryptAndSave nothing updated on statement '" +
                            statement + "'");
             } catch (Exception e)
            {
            	//--RUI still feel dangerous to call .replaceAll("AES_ENCRYPT\\(.*\\)","...");
             	throw new Exception("error execute encryptAndSave");
            }
            finally{
            	if(st!=null)
            		Check.disposal(st);
            	if(c!=null)
            		Check.disposal(c);
            }

        }
        else
            throw new Exception("Unable to encrypt and save  value: " + value +
                    " column: " + columnName + " table: " + tableName);
    }
    */
    


    /**
	 *  Update the input table's input column  with  the input value <b>String</b>
	 *  as an AES_ENCRYPT value, using the input where clause to determine which
	 *  row(s) get updated.
	 * @param value	<b>String</b>
	 * @param columnName <b>String</b>  In MySql form Case counts!!
	 * @param tableName <b>String</b>  In MySql form Case counts!!
	 * @param whereClause <b>String</b> everything after "WHERE".  Can be null or empty
	 * @throws Exception
	 */
	
	/*
	public static void encryptAndSave(String value, String columnName, String tableName,
	        String whereClause)
		throws Exception
	{
		Connection c = null;
		Statement st = null;
		ResultSet res = null;
		whereClause = (whereClause==null)?"":whereClause;
		value = value.replaceAll("\"", "\\\\\"");
		value = value.replaceAll("'", "\\\\\'");
		if (!whereClause.equals(""))
		    whereClause = " WHERE " + whereClause;
		if (value!=null && !value.equals("") &&
		        columnName!=null && !columnName.equals("") &&
		        tableName!= null && !tableName.equals(""))
		{
			String statement = "UPDATE " + tableName + " SET " + columnName +
		        " = AES_ENCRYPT('" + value + "','" +
		        GlobalProperties.getGlobalProperties().getSharedSecret()+"') "
		        + whereClause;
		    try
            {
                c = Torque.getConnection("guard_local");
                st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                int rows = st.executeUpdate(statement);
                if (rows == 0)
                    throw new Exception ("encryptAndSave nothing updated on statement '" +
                            statement + "'");
             }
            finally
            {
            	if(st!=null)
            		Check.disposal(st);
            	if(c!=null)
            		Check.disposal(c);
            }

		}
		else
		    throw new Exception("Unable to encrypt and save  value: " + value +
		            " column: " + columnName + " table: " + tableName);
	}
	*/
	
	/**
	 * check if regEx matches paragraph
	 */
	public static boolean jRegExSimpleCheck(String regEx, String paragraph, String caseSen)
	throws Exception
	{	boolean caseSensitive=true;
		boolean matchResult = false;
		int searchFlag=Pattern.DOTALL;

		if (caseSen!=null && CASE_INSENSITIVE.equals(caseSen)) {
			caseSensitive=false;
			searchFlag=searchFlag | Pattern.CASE_INSENSITIVE;
		}
		Pattern p = Pattern.compile(regEx,searchFlag);
		Matcher m = p.matcher(paragraph);
		matchResult = m.find();
		return matchResult;
	}
	/**
	 * check if regEx matches paragraph syy apr. 2006
	 */
	
	/*
	public static boolean mySQLregExSimpleCheck(String regEx, String paragraph)
		throws Exception
	{
		Connection c = null;
		Statement st = null;
		ResultSet res = null;
		boolean matchResult = false;

		if (regEx!=null && !regEx.equals("") &&
				paragraph!= null && !paragraph.equals("")) {

			String query = "SELECT " + "'" +paragraph.trim() + "' REGEXP '" +
							regEx.trim() +"'";
			try
            {

				c = Torque.getConnection("guard_local");
				st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                res = st.executeQuery(query);
                res.next();
                matchResult =  res.getBoolean(1);
            } catch (Exception e)
            {
                throw e;
            }
            finally{
            	if(st!=null)
            		st.close();
            	if(c!=null)
            		c.close();
            }
		}
		return matchResult;
	}
	*/
	
	/**
	 * Fetch an encrypted value out of the given column and table.  If there
	 * are more than one row in the ResultSet, takes the first one
	 * @param columnName <b>String</b>  In MySql form Case counts!!
	 * @param tableName <b>String</b>  In MySql form Case counts!!
	 * @param whereClause <b>String</b> everything after "WHERE".  Can be null or empty
	 * @return a <b>String</b>
	 * @throws Exception
	 */
	
	/*
	public static String fetchAndDecrypt(String columnName, String tableName,
	        String whereClause)
		throws Exception
	{
	    String decryptedString = "";
		Connection c = null;
		Statement st = null;
		ResultSet res = null;
		whereClause = (whereClause==null)?"":whereClause;
		if (!whereClause.equals(""))
		    whereClause = " WHERE " + whereClause + " and " + columnName + " <> ''";
		else 
			whereClause = " WHERE " + columnName + " <> ''";
		if (columnName!=null && !columnName.equals("") &&
		        tableName!= null && !tableName.equals(""))
		{
			String query = "SELECT AES_DECRYPT(" + columnName + ",'";
            query +=  GlobalProperties.getGlobalProperties().getSharedSecret() +
        		"') FROM " + tableName + " " + whereClause;
			try
            {

				c = Torque.getConnection("guard_local");
				st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                res = st.executeQuery(query);
                if(res.next())
                	decryptedString =  res.getString(1);
                else
                	decryptedString="";
            } catch (Exception e)
            {
            	//--RUI don' dispaly guard key
              	throw new Exception("error execute fetchDecrypted");
            }
            finally{
            	if(st!=null)
            		Check.disposal(st);
            	if(c!=null)
            		Check.disposal(c);            	
            }
		}
		return decryptedString;
	}
	*/

    /**
     * Fetch an encrypted from a remote table value out of the given column and table.  If there
     * are more than one row in the ResultSet, takes the first one
     * @param columnName <b>String</b>  In MySql form Case counts!!
     * @param tableName <b>String</b>  In MySql form Case counts!!
     * @param whereClause <b>String</b> everything after "WHERE".  Can be null or empty
     * @return a <b>String</b>
     * @throws Exception
     */
	
	/*
    public static String fetchAndDecryptRemote(String columnName, String tableName,
            String whereClause)
        throws Exception
    {
        String decryptedString = "";
        Connection c = null;
        Statement st = null;
        ResultSet res = null;
        whereClause = (whereClause==null)?"":whereClause;
        if (!whereClause.equals(""))
            whereClause = " WHERE " + whereClause;
        if (columnName!=null && !columnName.equals("") &&
                tableName!= null && !tableName.equals(""))
        {
            String query = "SELECT AES_DECRYPT(" + columnName + ",'";
            query +=  GlobalProperties.getGlobalProperties().getSharedSecret() +
                "') FROM " + tableName + " " + whereClause;
            try
            {

                c = Torque.getConnection("guard_remote");
                st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                res = st.executeQuery(query);
                res.next();
                decryptedString =  res.getString(1);
//            } catch (Exception e)
//            {
//            	//--RUI don' dispaly guard key
//              	throw new Exception("error execute fetchDecrypted");
            }
            finally{
            	if(st!=null)
            		Check.disposal(st);
            	if(c!=null)
            		Check.disposal(c);
            }
        }
        return decryptedString;
    }
    */
	
    /*
 	public static String decrypt (String encryptedText)throws Exception
 	{
 		String plainText = null;
 		if(!Check.isEmpty(encryptedText))
 		{
 			 AdHocLogger.logDebug("Start decryption", AdHocLogger.LOG_DEBUG);
 			// Convert encryptedText from Base64 string to a byte array.
 			 byte[] encryptedBytes = Base64.decode(encryptedText); 
 			 plainText = new String(encryptedBytes, "UTF8"); 
 		}//if(encryptedText != null && encryptedText.length()>0)		
 		return plainText;
 	}
	*/
    /*
 	public static String encrypt (String text)throws Exception
 	{
 		String encryptedTxt = "";
 		if(!Check.isEmpty(text))
 		{
 			byte[] plainText =text.getBytes("UTF8");
 			 encryptedTxt = Base64.encode(plainText);
 			
 		}//if(text != null && text.length()>0)
 		
 		return encryptedTxt;
 	}
	*/
    /**
	 * Converts to an IP address (returns null if host not valid or DNS fails)
	 */
    public static IPAddress resolve(Host host) {
    	IPAddress address = host.resolve();
    	return address;
	}
    
    /**
	 * Converts to an IP address string
	 */
    public static String convertHostToIP(Host host) {
		IPAddress address = host.resolve();
		return (address == null) ? "" : address.toNormalizedString();
	}

	/**
	 * Converts a hostname string into an IP address string
	 */
	public static String convertHostToIP(String hostStr) {
		if(hostStr == null || hostStr.length() == 0) {
			return "";
		}
		return convertHostToIP(new Host(hostStr));
	}

	public static boolean isValidHost(String s) {
		return new Host(s).isValid();
	}
	
	/*
	 * returns an IPAddress with the default validation options:
	 * -an empty string "" is not considered a valid address
	 * -segments with leading zeros are allowed (eg 01.02.003.04 or 001:a:b:000c:d:e:f:a)
	 * -mixed ipv6 addresses are allowed (eg a:b:c:d:e:f:1.2.3.4)
	 */
	public static IPAddressString getIpAddress(String s) {
		return new IPAddressString(s);
	}
	
	public static boolean isValidIpAddress(String s){
		IPAddressString address = getIpAddress(s);
		return address.isValid();
	}
	
	/**
	 *
	 * Use this method to format a date using the pattern
	 *
	 * @param date		Date to format
	 * @param pattern	Pattern used to format the date
	 * @return 	String presentation of the formatted date
	 */
   public static String formatDate(Date date,String pattern)
   {
   	String formatedDate = new String();
   	if(date != null)
   	{
           SimpleDateFormat formatter = new SimpleDateFormat(pattern);
           formatedDate =  formatter.format(date);

   	}
   	return formatedDate;
   }
   public static Date toDate(String formattedDate,String pattern) throws ParseException
   {
   	Date date = null;
   	if(Check.isEmpty(formattedDate))
   		return null;
       SimpleDateFormat formatter = new SimpleDateFormat(pattern);
       date =  formatter.parse(formattedDate);
       return date;

   }

	/**
	 *
	 * Use this method to convert a list of strings into a comma separated string.
	 *
	 * @param strList		A list of strings
	 * @return 	a string representing content of strList separated by comma
	 */
   public static String convertStrListToCommaSeparatedStr(List strList)
   {
   	String strValue = new String();;
   	if(strList != null)
   	{
   		for(int i=0; i<strList.size(); i++)
   		{
   			String value = (String)strList.get(i);
   			if(i>0)
   				strValue += ",";
   			strValue += value;
   		}
   	}
   	return strValue;
   }
   /**
    * Use this method to convert a list of Integers into a comma separated string
    * @param objList
    * @return
    */
   public static String converttIntegerListToCommaSeparatedStr(List<Integer> objList)
   {
   	String strValue = new String();
   	if(!Check.isEmpty(objList))
   	{
   		for(int i=0; i<objList.size(); i++)
   		{
   			Integer value = objList.get(i);
   			if(i>0)
   				strValue += ",";
   			strValue += value;
   		}
   	}
   	return strValue;
   }

	/**
	 *
	 * Use this method to convert a list of strings into a string each value seperated by sepStr.
	 *
	 * @param strList		A list of strings
	 * @return 	a string representing content of strList separated by value in sepStr
	 */
  public static String convertStrListToStr(List strList, String sepStr)
  {
  	String strValue = new String();;
  	if(strList != null)
  	{
  		for(int i=0; i<strList.size(); i++)
  		{
  			String value = (String)strList.get(i);
  			if(i>0)
  				strValue += sepStr;
  			strValue += value;
  		}
  	}
  	return strValue;
  }
	
	/**
	 * (Bobbi)
	 * Execute a command-line command.  Puts anything written by the executed command,
	 *  either the stdout or stderr into the input StringBuffer <i>sb</i> if the input
	 * StringBuffer <i>eb</i> is null; otherwise puts the errors in <i>eb</i>.
	 *
	 * @param commandArr		A <strong>String[]</strong> containing each token of the command to be executed
	 * @param envp				<strong>null</strong>or a  <strong>String[]</strong> of environmental parameters. Each element of which has environment variable settings in format <i>name=value.</i>
	 * @param dir				<strong> null</strong> or a <strong>File</strong> of the directory in which to run the command/
	 * @param sb				<strong>StringBuffer</strong> to contain any stdout (and stderr generated by the process, if <i>eb</i> is <b>null</b>
	 * @param eb				<strong>StringBuffer</strong> to contain any stderr, if not null
	 * @param input				<strong>String</strong>
	 * @return <i>true</i> if the command-line status is 0, <i>false</i> otherwise
	 * @throws Exception
	 */
	static public boolean executeCommand(String [] commandArr, String[] envp,
	        File dir,StringBuffer sb, StringBuffer eb, String input)
		throws IOException
	{
	    int procStatus = executeCommand(commandArr, envp, dir, sb, eb, input, null);
	    return (procStatus == 0);
	}
	
	
	/**
	 * (Bobbi)
	 * Execute a command-line command.  Puts anything written by the executed command,
	 *  either the stdout or stderr into the StringBuffer <i>sb</i> if the
	 * StringBuffer <i>eb</i> is null; otherwise puts the errors in <i>eb</i>.
	 *   If the <strong>String</strong> <i>input</i> is not null, will feed that into the command.
	 * This is what you use for calling a command, such as scp, that requires a password.
	 *
	 * @param commandArr		A <strong>String[]</strong> containing each token of the command to be executed
	 * @param envp				<strong>null</strong>or a  <strong>String[]</strong> of environmental parameters. Each element of which has environment variable settings in format <i>name=value.</i>
	 * @param dir				<strong> null</strong> or a <strong>File</strong> of the directory in which to run the command/
	 * @param sb				<strong>StringBuffer</strong> to contain any stdout (and stderr generated by the process, if <i>eb</i> is <b>null</b>
	 * @param eb				<strong>StringBuffer</strong> to contain any stderr, if not null
	 * @param input				<strong>String</strong>
	 * @param finalStatus		<strong>Integer</strong> the actual number returned by the system call
	 * @return 	<strong>Integer</strong> The actual exit value (<i>0</i> == success)
	 * @throws Exception
	 */
	 static public int executeCommand(String [] commandArr, String[] envp,
	        File dir, StringBuffer sb, StringBuffer eb, CharSequence input, Integer processStatus)
		throws IOException
	{
		
		CommandExecutor executor = new CommandExecutor(commandArr);
		executor.setEnvironment(envp);
		executor.setWorkingDir(dir);
    	StringBuffer err = (eb == null) ? new StringBuffer() : eb;
    	int status;
    	try {
    		status = executor.exec(sb, err, input);
    	} catch (InterruptedException e) {
			status = 1;
		}
    	
    	if (sb.length() > 0) {
			sb.append("\n");
		}
    	
		if(eb == null) {
			if(err.length() > 0) {
				sb.append(err);
				sb.append("\n");
			}
		} else if (eb.length() > 0) {
			eb.append("\n");
		}
		
		return status;
	}
	
	
	

	/**
	 * (Bobbi)
	 *
	 * Use this method in cases where the command-line command expects input from STDIN; for
	 * example, /usr/bin/tr.
	 * Execute a command-line command.  Puts anything written by the executed command,
	 *  either the stdout or stderr into the StringBuffer <i>sb</i> if the
	 * StringBuffer <i>eb</i> is null; otherwise puts the errors in <i>eb</i>.
	 *
	 * This method differs from <strong>static public Integer executeCommand(String [] commandArr, String[] envp,
	       File dir,StringBuffer sb, StringBuffer eb, String input, Integer processStatus)</strong>
	 * because it effectively issues a <i>CNTRL-D</i> to tell the command-line command that it's done with STDIN.
	 *
	 *
	 * @param commandArr		A <strong>String[]</strong> containing each token of the command to be executed
	 * @param envp				<strong>null</strong>or a  <strong>String[]</strong> of environmental parameters. Each element of which has environment variable settings in format <i>name=value.</i>
	 * @param dir				<strong> null</strong> or a <strong>File</strong> of the directory in which to run the command/
	 * @param sb				<strong>StringBuffer</strong> to contain any stdout (and stderr generated by the process, if <i>eb</i> is <b>null</b>
	 * @param eb				<strong>StringBuffer</strong> to contain any stderr, if not null
	 * @param stdIn			<strong>StringBuffer</strong> A string
	 * @param finalStatus		<strong>Integer</strong> the actual number returned by the system call
	 * @return 	<strong>Integer</strong> The actual exit value (<i>0</i> == success)
	 * @throws Exception
	 */
	static public Integer executeCommandWithStdIn(String [] commandArr, String[] envp,
	        File dir,StringBuffer sb, StringBuffer eb, StringBuffer stdIn, Integer processStatus)
		throws IOException
	{
		return executeCommand(commandArr, envp, dir, sb, eb, stdIn, null);
	}
	
	/**
	 * (Bobbi)
	 *
	 * Use this method in cases where the command-line command expects input from STDIN; for
	 * example, /usr/bin/tr.
	 * Execute a command-line command.  Puts anything written by the executed command,
	 *  either the stdout or stderr into the StringBuffer <i>sb</i> if the
	 * StringBuffer <i>eb</i> is null; otherwise puts the errors in <i>eb</i>.
	 *
	 * This method differs from <strong>static public Integer executeCommand(String [] commandArr, String[] envp,
	       File dir,StringBuffer sb, StringBuffer eb, String input, Integer processStatus)</strong>
	 * because it effectively issues a <i>CNTRL-D</i> to tell the command-line command that it's done with STDIN.
	 *
	 *
	 * @param commandArr		A <strong>String[]</strong> containing each token of the command to be executed
	 * @param envp				<strong>null</strong>or a  <strong>String[]</strong> of environmental parameters. Each element of which has environment variable settings in format <i>name=value.</i>
	 * @param dir				<strong> null</strong> or a <strong>File</strong> of the directory in which to run the command/
	 * @param sb				<strong>StringBuffer</strong> to contain any stdout (and stderr generated by the process, if <i>eb</i> is <b>null</b>
	 * @param eb				<strong>StringBuffer</strong> to contain any stderr, if not null
	 * @param stdIn			<strong>String[]</strong> An array of strings
	 * @param finalStatus		<strong>Integer</strong> the actual number returned by the system call
	 * @return 	<strong>Integer</strong> The actual exit value (<i>0</i> == success)
	 * @throws Exception
	 */
	static public Integer executeCommandWithStdIn(String [] commandArr, String[] envp,
	        File dir,StringBuffer sb, StringBuffer eb, String[] stdInArr, Integer processStatus)
		throws IOException
	{
		return executeCommand(commandArr, envp, dir, sb, eb, stdInArr == null ? null : new SequentialCharSequence(stdInArr), null);
	}
	
	/**
	 * It replaces one instance of backslash with 2 instances of backslashes
	 *
	 * @param originalStr		A <strong>String</strong> that may contain one instance of backslash
	 * @return <i>A string</i> in which one instance of backslash is replaced with 2.
	 */

	   public static String handleBackslash(String originalStr)
	   {
			String str = new String();
			if(originalStr != null && originalStr.length()>0)
			{

				StringTokenizer st = new StringTokenizer(originalStr,"\\");
				while(st.hasMoreTokens())
				{
					String txt = st.nextToken();
					if(txt != null)
					{
						if(str.length()>0)
							str += "\\\\";
						str += txt;
					}
				}
				if(str.length() == 0)
				{
					str=originalStr;
				}
			}
			return str;
		}

	   public static int getScreenInputSize(int max)
		{
			if(max>0)
				return (int)(Math.log(max)/Math.log(10))+1;
			else
				return 1;

		}

	//The following three getSelectList methods are only used by custom domain builder now --RUI
	/*Get a list from database, it's used for beans to get custom tables, custom domains*/
	   
	/*
	public static List getSelectList (String selectSql, String[] selectFields) throws Exception{
		List l = new ArrayList();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = Torque.getConnection("guard_remote");
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {

				rs.beforeFirst();
				while (rs.next()) {
					if(selectFields!=null)
					{
						if (selectFields.length>1)
						{
							List row = new ArrayList();
							for(int i=0;i<selectFields.length;i++)
								row.add(""+rs.getObject(selectFields[i]));
							l.add(row);
						}
						else{

							l.add(rs.getObject(selectFields[0]).toString());
						}
					}
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
			Check.disposal(con);
		}
		return l;
 	}
	*/
	   
	/*
	public static List getSelectList (String selectSql) throws Exception{
		return getSelectList(selectSql,"guard_remote");
 	}

	public static List getSelectList(String selectSql,String conName) throws Exception, SQLException {
		List l = new ArrayList();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = Torque.getConnection(conName);
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {

				rs.beforeFirst();
				int colNum = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					List row = new ArrayList();
					for(int i=1; i<=colNum; i++)
					{
						row.add(""+rs.getObject(i));
					}
					l.add(row);
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
			Check.disposal(con);
		}
		return l;
 	}
	*/
	   
	/*
	public static List getSelectListForOneField(String selectSql,String conName) throws Exception, SQLException {
		List l = new ArrayList();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = Torque.getConnection(conName);
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(selectSql);
			if (rs != null) {
				rs.beforeFirst();
				int colNum = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					l.add(rs.getObject(1));
				}
			}
		} finally {
			Check.disposal(rs);
			Check.disposal(st);
			Check.disposal(con);
		}
		return l;
 	}
	*/
	/*   
	public static int executeUpdateSqlinRemote(String stmt) throws Exception, SQLException
	{
        Connection con = null;
        try
        {
            // Get a connection to the db.
            con = Torque.getConnection("guard_remote");
            return executeUpdateSql(con,stmt);
        }
        finally
        {
            Torque.closeConnection(con);
        }
	}
	*/
	   
	/*   
	public static int executeUpdateSqlinTurbine(String stmt) throws Exception, SQLException
	{
        Connection con = null;
        try
        {
            // Get a connection to the db.
            con = Torque.getConnection("guard_local");
            return executeUpdateSql(con,stmt);
        }
        finally
        {
            Torque.closeConnection(con);
        }
	}
	*/
	   
	/*   
	public static int executeUpdateSql(String stmt, String conName) throws Exception, SQLException
	{
        Connection con = null;
        try
        {
            // Get a connection to the db.
            con = Torque.getConnection(conName);
            return executeUpdateSql(con,stmt);
        }
        finally
        {
            Torque.closeConnection(con);
        }
	}
	*/
	   
	/*
	public static int executeUpdateSqlIgnoreErr(String stmt, String conName, int errCode) throws Exception, SQLException
	{
        Connection con = null;
        try
        {
            // Get a connection to the db.
            con = Torque.getConnection(conName);
            return executeUpdateSqlIgnoreErr( con, stmt, errCode);
        }
        finally
        {
            Torque.closeConnection(con);
        }
	}
	*/
	   
	/*   
	public static int executeUpdateSqlIgnoreErr(Connection con,String stmt, int errCode) throws Exception, SQLException
	{
		Statement statement = null;
        try
        {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            statement.executeUpdate(stmt);
            int ret = statement.getUpdateCount();
            return ret;
        }
        catch (SQLException e)
        {
        	if (e.getErrorCode() == errCode)
        	{
        		return 0 ;
        	}
        	throw e;
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException ignored)
                {
                }
            }
        }
	}
	*/
	   
	public static int executeUpdateSql(Connection con,String stmt) throws SQLException
	{
		Statement statement = null;
        try
        {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            StringBuffer query = new StringBuffer();
            query.append(stmt);

            statement.executeUpdate(query.toString());
            int ret = statement.getUpdateCount();
            return ret;
        }
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException ignored)
                {
                }
            }
        }
	}
	
	/*
    public static int executeSingleSql(String sql, String dbConn)
	throws Exception
	{
	    Connection con = null;
	    Statement st = null;
	    int retVal = 0;

		try
		{
		   	con = Torque.getConnection(dbConn);


	        st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

	        boolean isRs = st.execute(sql);

	                retVal = st.getUpdateCount();

		}
		finally
		{
			if(st != null)
				st.close();
			Torque.closeConnection(con);
		}
		return retVal;
	}
	*/
	
    static public Object executeSqlForSingleVal(String query, int colNum,Connection con) throws SQLException{
		Object value = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				value = rs.getObject(colNum);
			}
			return value;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	static public Long getNumber(Object value){
		if(value!=null){
			return Long.parseLong(value.toString().trim());
		}
		return null;
	}

	static public Long executeSqlForNumber(String query,Connection con) throws DataSourceConnectException, SQLException{
		Object value = executeSqlForSingleVal(query,1,con);
		return getNumber(value);
	}
    /*
    static public ResultSet executeSqlForResultSet(String query, Connection con) throws DataSourceConnectException, SQLException{

		Statement stmt = null;
		ResultSet rs = null;
		CachedRowSet crs = null;

		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			rs = stmt.executeQuery(query);
			
			crs = new CachedRowSetImpl();
			if(con.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle"))
			{
				crs.populate(new OracleResultSetWrapper(rs));
			}else
				
			crs.populate(rs);
			return crs;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}
	*/
	
    public static String executeCommand(String command_line) {
        try {
        	StringBuffer err = new StringBuffer();
        	StringBuffer out = new StringBuffer();
        	execCommand(command_line, null, null, out, err, null);
            if ( err.length() != 0) {
                return err.toString();
            }
            return out.toString();
        }
        catch (IOException e) {
            AdHocLogger.logException(e);
            return e.getMessage();
        } catch (InterruptedException e) {
            AdHocLogger.logException(e);
            return e.getMessage();
        }
    }
  
    public static String executeCommand(String[] cmd) {
        try {
        	StringBuffer err = new StringBuffer();
        	int ret = execCommand( cmd, null, null, null, err, null);
        	if(err.length() > 0)
        		return err.toString();
        	if(ret!=0)
        		return "exit value="+ret;
        } catch (IOException e) {
            AdHocLogger.logException(e);
            return e.getMessage();
        } catch (InterruptedException e) {
            AdHocLogger.logException(e);
            return e.getMessage();
        }
		return null;
    }
    
	static public int execCommand(String commandArr, String[] envp,
	        File dir,StringBuffer sb, StringBuffer eb, StringBuffer stdIn) throws IOException, InterruptedException
	{
		return execCommand(new String[] {commandArr}, envp, dir, sb, eb, stdIn);
	}
	
	static public int execCommand(String [] commandArr, String[] envp,
	        File dir, StringBuffer sb, StringBuffer eb, StringBuffer stdIn) throws IOException, InterruptedException
	{
		LimitedAppendable err = (eb == null) ? null : new LimitedAppendable(eb, 5000);
    	LimitedAppendable out = (sb == null) ? null : new LimitedAppendable(sb, 5000);
    	try {
			CommandExecutor executor = commandArr.length == 1 ? new CommandExecutor(commandArr[0]) : new CommandExecutor(commandArr);
			executor.setEnvironment(envp);
			executor.setWorkingDir(dir);
			return executor.exec(out, err, stdIn);
    	} finally {
    		if(err != null) {
    			err.dump();
    		}
    		if(out != null) {
    			out.dump();
    		}
    	}
	}

    public static String getDiplayErrorMsg(Throwable e)
    {
    	String msg = e.getMessage();
    	if(msg==null)
    		return null;
    	msg = msg.replaceAll("\"","\\\\\"");
    	msg = msg.replaceAll("\\n","\\\\n");
    	return msg;
    }

    public static String getDiplayErrorMsg(String msg)
    {
    	if(msg==null)
    		return null;
    	msg = msg.replaceAll("\"","\\\\\"");
    	msg = msg.replaceAll("\\n","\\\\n");
    	return msg;
    }

    public static String getDisplayMsg(Object o)
    {
    	if(o==null)
    		return "null";
    	StringBuffer sb = new StringBuffer();
    	if(o instanceof String[])
    	{
    		String[] a = (String[]) o;
    		if(a!=null&&a.length>0)
    		{
    			for(int i =0; i<a.length; i++)
    			{
    				sb.append(o).append(',');
    			}
    		}
    	}else if(o instanceof List)
    	{
    		List l =(List) o;
    		for(Iterator ite=l.iterator();ite.hasNext();)
    		{
    			Object o1 = ite.next();
    			sb.append(o1).append(',');
    		}
    	}
    	if(sb.length()>0)
    		sb.deleteCharAt(sb.length()-1);
    	return sb.toString();
    }
	public static String getColumnsAsString(List<String> columnList, String table)
	{
		String columnsAsStr = new String();
		if(Check.isEmpty(columnList))
			return columnsAsStr;
		
		for(String column: columnList)
		{
			if(columnsAsStr.length() > 0)
				columnsAsStr += ",";
			if(!Check.isEmpty(table))
				columnsAsStr += table + ".";
			columnsAsStr += column;
		}

		return columnsAsStr;
	}
	
	public static boolean compareString(String str1, String str2)
	{
		if(str1==null||str2==null){
			if(str1!=str2)
				return false;
		}
		else
		{
			if(!str1.equals(str2))
				return false;
		}
		return true;
	}

	public static boolean isSame(Object o1, Object o2)
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

	public static boolean inList(Object o, Object[] os)
	{
		if(os==null){
			return false;
		}
		for(int i=0; i<os.length; i++)
		{
			if(isSame(o,os[i]))
				return true;
		}
		return false;
	}

	public static String getSubStringAfterLastChar(String s, char c){
		if(s==null)
			return null;
		int ind = s.lastIndexOf(c);
		if(ind>-1);
			return s.substring(ind+1);
	}

	public static HashMap getCharactersetMap()
	{
		//must define this in GlobalUtil
		HashMap charactersetMap = new HashMap();
		charactersetMap.put("819", "ISO_8859_1");
		charactersetMap.put("912", "ISO_8859_2");
		charactersetMap.put("57346", "ISO_8859_3");
		charactersetMap.put("57347", "ISO_ISO_8859_4");
		charactersetMap.put("915", "ISO_8859_5");
		charactersetMap.put("1089", "ISO_8859_6");
		charactersetMap.put("813", "ISO_8859_7");
		charactersetMap.put("916", "ISO_8859_8");
		charactersetMap.put("920", "ISO_8859_9");
		charactersetMap.put("364", "US-ASCII");
		charactersetMap.put("932", "SJIS");
		charactersetMap.put("57350", "SJIS");
		charactersetMap.put("57372", "UTF-8");
		charactersetMap.put("57352", "Big5");
		charactersetMap.put("1250",	"Cp1250");
		charactersetMap.put("1251", "Cp1251");
		charactersetMap.put("1252",	"Cp1252");
		charactersetMap.put("1253",	"Cp1253");
		charactersetMap.put("1254",	"Cp1254");
		charactersetMap.put("1255",	"Cp1255");
		charactersetMap.put("1256",	"Cp1256");
		charactersetMap.put("1257",	"Cp1257");
		charactersetMap.put("57356","Cp949");
		charactersetMap.put("57356", "Cp949");
		charactersetMap.put("57356", "Cp949");
		charactersetMap.put("57351", "EUC_JP");
		charactersetMap.put("57357", "ISO2022CN_GB");
		return charactersetMap;
	}

	/**
	 * Changes characterset of text from <strong>fromCharacterset</strong> to <strong>toCharacterset</strong>
	 *
	 * @param text		A <strong>String</strong> which is encoded in <strong>fromCharacterset</strong>
	 * @param fromCharacterset
	 * @param toCharacterset
	 * @return <i>A string</i> which represent the text in <strong>toCharacterset</strong>
	 */
	public static String convertCharacterset(String text,String fromCharacterset,String toCharacterset)
	{
		String convertedText = new String();
		if(text != null && text.length() > 0)
		{
			convertedText = text;
			if(fromCharacterset != null && toCharacterset != null &&
					fromCharacterset.length() > 0 && toCharacterset.length() > 0 &&
					!fromCharacterset.equalsIgnoreCase(toCharacterset))
			{
				try {
					byte[] byteArray = new byte[256];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(baos,fromCharacterset);
					osw.write(text);
					osw.close();
					byteArray = baos.toByteArray();
					ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
					InputStreamReader isr = new InputStreamReader(bais, toCharacterset);
					char[] cbuf = new char[101];
					isr.read(cbuf);
					convertedText = new String(cbuf);
					isr.close();
				} catch (FileNotFoundException e) {
					AdHocLogger.logException(e);
				} catch (UnsupportedEncodingException e) {
					AdHocLogger.logException(e);
				} catch (IOException e) {
					AdHocLogger.logException(e);
				}

			}
		}//if(text != null && text.length() > 0)
		return convertedText;
	}

	public static String bytesToString(byte[] bytearray) {
		String s = null;
		if (bytearray!=null) {
			s="";
			for(int i=0; i<bytearray.length; i++){
				s+=( char )bytearray[ i ];
			}
		}
		return s;
	}
	

	public static String arrayToString(List list)
	{
		String s ="";
		if(list==null)
			return s;
		for(int i =0;i <list.size();i++)
		{
			if(s.length() > 0)
				s+=",";
			s+=list.get(i).toString();
		}
		return s;
	}

	public static List cloneArray(List oldArray)
	{
		if(oldArray==null)
			return null;
		List newArray = new ArrayList();

		if(oldArray!=null)
		{
			for(int i =0;i<oldArray.size();i++)
				newArray.add(oldArray.get(i));
		}

		return newArray;
	}
	/*
	public static  void addLike(String filterString, String columnName, Criteria criteria)
	{
		if( filterString != null )
		{
			filterString = filterString.trim();
			if( filterString.length() > 0)
			{
				filterString = filterString.replace('*','%');	// star char (or 'splat') is allowed as alt. wildcard but must be
																						// converted to percent sign before use in DB criteria

				filterString = filterString.replaceAll("\\\\","\\\\\\\\");	// Backslashes will be doubled for us on form submit
				criteria.add(
						columnName,
						(Object) filterString,
						Criteria.LIKE );
			}
		}
	}
	*/
	/*
	public static  void addLikeOrEmpty(String filterString, String columnName, Criteria criteria)
	{
		if( filterString != null )
		{
			filterString = filterString.trim();
			if( filterString.length() > 0)
			{
				filterString = filterString.replace('*','%');	// star char (or 'splat') is allowed as alt. wildcard but must be
																						// converted to percent sign before use in DB criteria

				filterString = filterString.replaceAll("\\\\","\\\\\\\\");	// Backslashes will be doubled for us on form submit
				criteria.add(
						columnName,
						(Object) filterString,
						Criteria.LIKE );
//				criteria.or(AccessRulePeer.SERVER_IP,(Object)(" SERVER_IP is null or LENGTH(SERVER_IP)= 0 "),Criteria.CUSTOM);
				criteria.or(columnName,(Object)( columnName + " is null or LENGTH(" + columnName + ")= 0 "),Criteria.CUSTOM);

			}
		}
	}
	*/
	/**
	 * For usage in a jdbc query, converts the  filterString by replacing * with % and doubling up the backslashes 
	 * @param filterString
	 * @return
	 */
	public static String prepareForDbUsage(String filterString)
	{
		if(Check.isEmpty(filterString))
			return filterString;
		// convert wild cards to percent sign before use in DB
		filterString = filterString.replace('*','%');	
		
		// Double up Backslashes 
		filterString = filterString.replaceAll("\\\\","\\\\\\\\");	

		return filterString;
		
	}
	
	public static String bytesToHexaString(byte[] byteArray)
	{
		StringBuffer str=new StringBuffer(byteArray.length*2);
		for (int i=0;i<byteArray.length;i++)
		{
		 int currentByte=byteArray[i];
		 if (currentByte<0)
		 	currentByte+=256;
		 if (currentByte<16)
			str.append('0');
			str.append(Integer.toHexString(currentByte));
			}
		return str.toString();
	}

	public static String getUniqueNameByUsingMessageDigest(String name)
	{
		String uniqueName = new String();
		if(name != null && name.length()>0)
		{
			MessageDigest digest1;
			try {
				digest1 = MessageDigest.getInstance("MD5");
				byte[] re = digest1.digest(name.getBytes());
				//using Gilads
				uniqueName = bytesToHexaString(re);
			} catch (NoSuchAlgorithmException e) {
				Category cat = Category.getInstance("system");
				String msg = e.getMessage();
				if(msg != null && msg.length()>0)
					cat.error(msg);
			}
		}//if(name != null && name.length()>0)
		return uniqueName;
	}
	public static String getStackTraceAsString(Throwable e)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bytes, true);
		e.printStackTrace(writer);
		return bytes.toString();
	}
	/**
	 * Verifies if an input contains any special charcters.
	 * @param input
	 * @return returns true if input contains any of special characters. Otherwise, it returns false
	 */
	public static boolean verifyNoSpecialChar(String input)
	{
		return !Regexer.matchRegex(input,EXCLUDED_CHARACTERS);
	}
	/**
	 * Verifies if an input contains any special charcters for directory
	 * @param input
	 * @return returns true if input contains any of special characters for directory. Otherwise, it returns false
	 */
	public static boolean verifyDirNoSpecialChar(String input)
	{
		return !Regexer.matchRegex(input,EXCLUDED_DIR_CHARACTERS);
	}
	
	/**
	 * 2012-05-16 sbuschman 29579
	 * @return true if input matches nameSpecialCharacters
	 */
	public static boolean verifyNameSpecialChar(String input) {
		return input != null && nameSpecialCharacters.matcher(input).find();		
	}	
	
	public static Date getCurrentTime()
	{
		return Calendar.getInstance().getTime();

	}
	
	/*
	public static TableLoggerIfc getAuditLogger() {
		return auditLogger;
	}
	public static void setAuditLogger(TableLoggerIfc auditLogger) {
		Utils.auditLogger = auditLogger;
	}
    */
	
	private static String replaceBackSlashes(String v) {
    	   StringBuffer sb =  new StringBuffer(v);
           String rep = "\\\\\\";
           if(sb.length()>0){
             int from = 0;
             while(true){
                int index = sb.indexOf("\\",from);
                if(index==-1)
                    break;
                else{
                    sb.replace(index, index+1, rep);
                    from = index+rep.length()+1;
                }   
             }   
           }   
           return sb.toString();
       }   
	static Long pre = System.currentTimeMillis();
	
	public static synchronized String getUniqueId() throws Exception 
	{
		long cur = System.currentTimeMillis();
		int i = 0;
		while(cur == pre&&i<3)
		{
			try {
				i++;
				Thread.sleep(1);
			} catch (InterruptedException e) {
				AdHocLogger.logException(e);
			}
		}
		if(pre != cur)
		{	pre = cur;
			return Long.toString(cur);
		}
		else
			throw new Exception("This should never happen: cannot get unique id");
	}

    public static String escapeSpecialChar(String cmd)
    {
	    String specialChars = "\\$|&;`'\"()!";
	    StringBuilder sb = new StringBuilder("\\ ");
	    for (int i =0 ; i<specialChars.length(); i++)
	    {
	        sb.setCharAt(1, specialChars.charAt(i));
	    	cmd = cmd.replace(specialChars.subSequence(i,i+1), sb);
	    }
	    return cmd;
    }
    
    /**
     * Return sorted input based upon value - case insensitive. 
     */
    public static Map<String, String> sortOnValue(Map<String, String> input){
   	 Map<String, String> m = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

   	 // Reverse and sort on value
   	 for (Entry<String, String> kv : input.entrySet()) {
   		 if (m.containsKey(kv.getValue())) {
   			 throw new RuntimeException("Value is not unqiue: " + kv.getValue());
   		 }
   		 
   		 m.put(kv.getValue(), kv.getKey());
   	 }

   	 /*
   	  * LinkedHashMap is a variant on HashMap. Its entries are kept in a
   	  * doubly-linked list. The iteration order is, by default, the order in which
   	  * keys were inserted.
   	  */
   	 Map<String,String> result = new LinkedHashMap<String,String>();

   	 // Reverse again 
   	 for (Entry<String, String> kv : m.entrySet()) {
 			result.put(kv.getValue(), kv.getKey());
   	 }

   	 return result;
    }
    /**
     * Returns number of times searchChar occurs in the text
     * @param searchChar
     * @param text
     * @return
     */
	public static int getCharCountInText(char searchChar, String text)
	{
		int count = 0;
		if(Check.isEmpty(text))
			return count;
		
		String regex = "[^" + searchChar + "]";
		
		count = text.replaceAll(regex,"").length();
		return count;
	}
	
	public static String[][] getMetaData(String file) throws IOException, Exception
	{
		return getMetaData(file,15);
	}
	public static String[][] getMetaData(String file,int numOfCol) throws IOException, Exception
	{
		String fileName = GlobalProperties.getAppRoot()+file;
	    FileInputStream fstream = new FileInputStream(fileName);
	    // Get the object of DataInputStream
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    List<String[]> l= new ArrayList<String[]>();
	    while ((strLine = br.readLine()) != null)   {
	      String[] line=ParsingUtils.parseCsvLine(strLine,numOfCol,false);
	      l.add(line);
	    }
	    String[][] meta = new String[l.size()][];
	    l.toArray(meta);
	    in.close();
	    return meta;
	}
	public static String getExternalizedBooleanValue(boolean value)
	{
		String externalized = "";
		if(value)
			externalized = SayAppRes.say("baseline.value.boolean.true");
		else
			externalized = SayAppRes.say("baseline.value.boolean.false");
		
		return externalized;
	}
	
	public static String getPid() {
		if (pId != null)
			return pId;
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();  
		int p = nameOfRunningVM.indexOf('@');  
		pId = nameOfRunningVM.substring(0, p);
		return pId;
	}
	
	public static boolean isIpAddress(String addr) 
	{
		return isValidIpAddress(addr);
	}
}
