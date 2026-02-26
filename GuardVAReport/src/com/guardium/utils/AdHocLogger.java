/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;

import com.guardium.date.ThreadSafe_SimpleDateFormat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

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
public class AdHocLogger
{
	public static final int LOG_TRACE = 10;
	public static final int LOG_DEBUG = 9;
	public static final int LOG_INFO = 7;
	public static final int LOG_WARN = 6;
	public static final int LOG_ERRORS = 5;
	public static final int LOG_DIAG = 3;
	private static final Level DIAG_LEVEL = Level.forName("DIAG", 150);
	public static final int LOG_FATAL_ERRORS = 0;
	// time out is in minutes - default is 24 hours
	public static final int LOG_DEFAULT_TIME_OUT = 60*24;

	public static final String logDir = FileUtils.getLog("debug-logs/");
	public static final String baseName = "-debug.log";
	public static boolean log = false;
	public static int logLevel = 0;
	public static int timeOut = LOG_DEFAULT_TIME_OUT;
	public static boolean logThreadName = false;
	public static boolean logStackTrace = false;
	public static boolean logAllExceptions = false;
	public static String filePrefix = "";

	private static final ThreadSafe_SimpleDateFormat filenameDateFormat = new ThreadSafe_SimpleDateFormat("yyyy-MM-dd");
	private static Logger rotatingLoggger = null;
	static
	{
		try {
			// initialize rotatingLoggger only when running under tomcat (main class is Bootstrap)
			// and under the GUI app (/opt/IBM/Guardium/tomcat/webapps/ROOT)
			if (GlobalProperties .getAppRoot().equals("/opt/IBM/Guardium/tomcat/webapps/ROOT")
					&& System.getProperty("sun.java.command").startsWith("org.apache.catalina.startup.Bootstrap"))
			{
				LoggerContext context = (LoggerContext) LogManager.getContext(false);
				File propertiesFile = new File(FileUtils.getTomcatRoot("/webapps/ROOT/WEB-INF/conf/RotatingAdHocLog.log4j2.properties"));
				context.setConfigLocation(propertiesFile.toURI());
				rotatingLoggger = LogManager.getLogger(AdHocLogger.class);
			}
		}
		catch (Exception e)
		{
			System.err.println("Failed to load log4j configuration "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void logFatal(String message)
	{
		logDebug(message,LOG_FATAL_ERRORS);
	}
	public static void logDiag(String message)
	{
		logDebug(message,LOG_DIAG);
	}
	public static void logError(String message)
	{
		logDebug(message,LOG_ERRORS);
	}
	public static void logWarning(String message)
	{
		logDebug(message, LOG_WARN);
	}
	public static void logInfo(String message)
	{
		logDebug(message, LOG_INFO);
	}
	public static void logDebug(String message) {
		logDebug(message,LOG_DEBUG);
	}
	public static void logTrace(String message)
	{
		logDebug(message, LOG_TRACE);
	}


	public static String formatFilenameDateString(Date date)
	{
		return filenameDateFormat.format(date);
	}
	public static int getLogLevel()
	{
		return logLevel;
	}

	public static void setlogLevel(int logLevel)
	{
		AdHocLogger.logLevel = logLevel;
	}

	public static boolean isLog()
	{
		return log;
	}

	public static void setLog(boolean log)
	{
		if(log)
		{
			lazy.loggingStarted = System.currentTimeMillis();
			if(isLog()) {
				logDebug(String.format("logging timeout reset"),0);
			}
		}
		AdHocLogger.log = log;
	}

	public static void main(String[] args)
	{
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
	}

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
			lazy.loggingStarted = loggingStarted;
		}
	}
	private static LogThread lazy = new LogThread("Lazy Logger");

	private static void writeToRotatingLogger(String message, int level)
	{
		if(rotatingLoggger!=null)
		{
			switch (level) {
				case LOG_FATAL_ERRORS: rotatingLoggger.fatal(message);
					break;
				case LOG_DIAG: rotatingLoggger.log(DIAG_LEVEL,message);
					break;
				case LOG_ERRORS: rotatingLoggger.error(message);
					break;
				case LOG_WARN: rotatingLoggger.warn(message);
					break;
				case LOG_INFO: rotatingLoggger.info(message);
					break;
				case LOG_DEBUG: rotatingLoggger.debug(message);
					break;
				case LOG_TRACE:
				default: rotatingLoggger.trace(message);
			}
		}
	}

	public static void logDebug(String text, int level)
	{
		writeToRotatingLogger(text,level);
		if(!log)
			return;
		if(level > logLevel)
			return;
		if(logStackTrace)
			text = text+"\n"+Utils.getStackTraceAsString(new Exception("Dummy Stack Trace"));
		writeToMessageQueue(text);
		return;
	}
	private static void writeToMessageQueue(String text)
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
		logError("", t);
	}

	public static void logError(String message,Throwable t)
	{
		if(rotatingLoggger!=null)
		{
			rotatingLoggger.error(message, t);
		}
		if(!log)
			return;
		if (message != null && !message.isEmpty()){
			if(logAllExceptions || LOG_ERRORS <= logLevel){
				writeToMessageQueue(message);
			}
		}
		if(logAllExceptions)
		{
			writeToMessageQueue(Utils.getStackTraceAsString(t));
		}
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
		AdHocLogger.filePrefix = filePrefix;
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
		AdHocLogger.logStackTrace = logStackTrace;
	}
	public static boolean isLogThreadName()
	{
		return logThreadName;
	}
	public static void setLogThreadName(boolean logThreadName)
	{
		AdHocLogger.logThreadName = logThreadName;
	}
	public static void setLogLevel(int logLevel)
	{
		AdHocLogger.logLevel = logLevel;
	}
	public static void reset()
	{
		logDebug("AdHocLogger was reset", 9);
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

	public static void setTimeOut(int timeout) {
		setTimeOut(timeout, false);
	}

	public static void setTimeOut(int timeOut, boolean force)
	{
		// If not forced - don't decrease timeout
		if ((!force) && (timeOut < AdHocLogger.timeOut)) {
			return;
		}

		// Log new timeout
		logDebug(String.format("AdHocLogger new timeout: %d minutes", timeOut), 9);

		// Set new timeout
		lazy.loggingStarted = System.currentTimeMillis();
		AdHocLogger.timeOut = timeOut;
	}
}
