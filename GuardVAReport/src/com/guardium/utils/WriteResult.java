/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;

import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;

/*
 * Created on Aug 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author guy
 *
 * An asynchronous logger.
 * messages are queued and written to file by a low priority thread.
 * the log goes to a file {date}-debug.log, a new file is created for a new day.
 * two static flags control what gets log:
 * - if log is false nothing is logged
 * - any call to debug has a level parameter, only if the static logLevel is equal or higher then
 *   this parameter then the message is logged.
 * - exceptions are logged if logAllException is set.   
 * Other options are to log the thread name , and/or the stack trace for each message.
 * 
 *  
 */
public class WriteResult 
{
	public static int writeMethod = 1;
	
	private static JTextArea jTaskOutput;
	
	private static String logfile = TestUtils.getDirectoryPath() +System.getProperty("file.separator")  + "VAResultFile";
	
	private static String pdffile;
	
	public static int getWriteMethod () {
		return writeMethod;
	}
	
	public static void setWriteMethod (int met) {
		writeMethod = met;
		return;
	}
	
	public static JTextArea getJTextArea () {
		return jTaskOutput;
	}
	
	public static void setJTextArea (JTextArea j) {
		jTaskOutput = j;
		return;
	}
	
	public static String getPdfFile () {
		return pdffile;
	}
	
	public static void setPdfFile (String j) {
		pdffile = j;
		return;
	}	

	public static String getLogFile () {
		return logfile;
	}
	
	public static void setLogFile (String j) {
		logfile = j;
		return;
	}
	
	public static void writeOutput (String str) {
		// write to textfield
		//String tmpstr = str + "\n";
		//taskOutput.append(String.format(tmpstr));
		

		//if (writeMethod == 1) {
			// system output
			System.out.println(str);
		//}
		/*
		else if (writeMethod == 2) {
			// write to textfield
			// don't need to write on screen
			String tmpstr = str + "\n";
		    // this line can not handle string with "\%"
			jTaskOutput.append(String.format(tmpstr));	
		}
		else if (writeMethod == 3) {
			// pdf file
			
		}
		*/
		
		return;
	}
    public static Writer createLogFile(String filePath) {
    	Writer writer = null;
	 
	   	try {
	 
	   		// Using OutputStreamWriter you don't have to convert the String to byte[]
	        writer = new BufferedWriter(new OutputStreamWriter(
	                    new FileOutputStream(filePath), "utf-8"));

	    } catch (IOException e) {
	 
	    } finally {
	        /*
	        if (writer != null) {
	            try {
	                writer.close();
	            } catch (Exception e) {
	 
	            }
	        }
	        */
	    }
	   	return writer;
    }

    public static void closeLogFile(Writer wr) {
        if (wr != null) {
            try {
                wr.close();
            } catch (Exception e) {
 
            }
        }        	
    }
    
    public static void writeToLogFile(List<String> content, Writer wr) {
        if (wr == null) {
        	return;
        }
        
    	try {
            for (String line : content) {
                line += System.getProperty("line.separator");
                wr.write(line);
            }
	    } catch (IOException e) {
	   	 
	    } finally {
	        /*
	        if (wr != null) {
	            try {
	                wr.close();
	            } catch (Exception e) {
	 
	            }
	        }
	        */
	    }       
    }
    
    public static void createTestResultFile(List<String> content, String filePath) {
    	Writer writer = null;
	 
	   	try {
	 
	   		// Using OutputStreamWriter you don't have to convert the String to byte[]
	        writer = new BufferedWriter(new OutputStreamWriter(
	                    new FileOutputStream(filePath), "utf-8"));
	 
	        for (String line : content) {
	            line += System.getProperty("line.separator");
	            writer.write(line);
	        }
	 
	    } catch (IOException e) {
	 
	    } finally {
	 
	        if (writer != null) {
	            try {
	                writer.close();
	            } catch (Exception e) {
	 
	            }
	        }
	    }
    }
    
	public static void main(String[] args) 
	{
		/*
		setLog(true);
		setlogLevel(LOG_INFO);
		logDebug("level INFO",LOG_INFO);
		setLogThreadName(true);
		logDebug("with thread name", LOG_INFO);
		setLogStackTrace(true);
		logDebug("with stack trace", LOG_INFO);
		setLogLevel(0);
		try
		{
			String s = null;
			// NullPointerException 
			s.length();
		}
		catch (Exception e) 
		{
				logException(e);					
		}
		flush();
		*/
	}
	
	/*
	private static LinkedList que = new LinkedList();
	
	private static class LogThread extends Thread 
	{
		boolean run = true;
		long loggingStarted = (long)0;
		
		public LogThread(String string)
		{
			super(string);
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}
		public  void run()
		{
			while(run)
			{
				Object o;
				
				synchronized(que)
				{
					while(que.size() == 0 )
					{
						try
						{
							que.wait(1000*60);
						}
						catch(InterruptedException ex)
						{
						}
						if(isLog() && 
								((loggingStarted + getTimeOut()) <= System.currentTimeMillis()))
						{
							logDebug("Logging timed out", 0);
							setLog(false);
						}
					}
					o = que.removeFirst();
					que.notifyAll();
				}
				write(o);
			}
			// added this because a thread cannot start more then once so if thread was terminated 
			// create a new thread object ready to run
			lazy = new LogThread("Lazy Logger"); 
		 }
	 }
	 private static LogThread lazy = new LogThread("Lazy Logger");
	
	 public static void logDebug(String text, int level) 
	 {
		 if(!log) 
			 return;
		 if(level > logLevel) 
			 return;
		 
		 //if(logStackTrace)
		 //	text = text+"\n"+Utils.getStackTraceAsString(new Exception("Dummy Stack Trace"));
		 text = text+"\n"+"Dummy Stack Trace";
		 logDebug(text);
	 }
	private static void logDebug(String text) 
	{
		synchronized(que)
		{
		 StringBuffer sb = new StringBuffer();
		 sb.append("=============="+new Date()+"===================\n");
		 if(logThreadName)
			 sb.append("Thread: "+Thread.currentThread().getName()+ " - ");
		 sb.append(text);
		 sb.append("\n=============================================================\n");
		 que.addLast(sb);
		 if (!lazy.isAlive())
		 {
			synchronized(lazy)
			{
				if (!lazy.isAlive())
				{
					lazy.start();
				}
			}
		 }
		 que.notifyAll();
		}
	}
	
	
	public static void logException(Throwable t) 
	{
		if(!log) 
			return;
		if(logAllExceptions)
			//logDebug(Utils.getStackTraceAsString(t));
			logDebug(t.getMessage());
	}
	
	private static boolean write()
	{
		Object o;
		synchronized (que)
		{
			if( que.isEmpty() )
				return false;
			o = que.removeFirst();
		}
		write(o);
		return true;
	}
	private static synchronized void write(Object o)
	{
		String fileName = logDir +filePrefix+ formatFilenameDateString(new Date()) + baseName;
		write(o, fileName);
	}
	
	public static void setFilePrefix(String filePrefix) 
	{
		WriteResult.filePrefix = filePrefix;
	}

	private static synchronized void write(Object o, String fileName)
	{
		try
		{
			PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
			pw.write(o.toString());
			pw.flush();
			pw.close();	
		}
		catch (Exception e)
		{
			System.out.println("FAILED LOGGING TO FILE: \n"+o.toString());
		}
	}
	public static void flush() 
	{
		lazy.run = false;
		while(write())
			;
	}
	public static boolean isLogStackTrace()
	{
		return logStackTrace;
	}
	public static void setLogStackTrace(boolean logStackTrace)
	{
		WriteResult.logStackTrace = logStackTrace;
	}
	public static boolean isLogThreadName()
	{
		return logThreadName;
	}
	public static void setLogThreadName(boolean logThreadName)
	{
		WriteResult.logThreadName = logThreadName;
	}
	public static void setLogLevel(int logLevel)
	{
		WriteResult.logLevel = logLevel;
	}
	public static void reset()
	{
		log = false;
		logLevel = LOG_FATAL_ERRORS;
		logThreadName = false;
		logStackTrace = false;
		logAllExceptions = false;
	}
	public static void setLogAllExceptions(boolean b)
	{
		logAllExceptions = b;
		
	}
	public static boolean isLogAllExceptions()
	{
		return logAllExceptions;
	}
	public static int getTimeOut()
	{
		return timeOut*60*1000;
	}
	public static void setTimeOut(int timeOut)
	{
		WriteResult.timeOut = timeOut;
	}
	*/
	
}
