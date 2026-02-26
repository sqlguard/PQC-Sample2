/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

/**
 * This class relies heavily on static initialization; therefore, it cannot use any other class which tries to log (i.e.
 * cyclic dependency)
 */
public class LoggerUtils
{
    // we don't want to put this in any other java file because it might try to
    // initialize a logger which would create dependency problems during static initialization
    private static final String DEFAULT_LOG4J_PROPERTIES_FILE_PATTERN = ".*log4j.properties";
    private static Path logFile = null;
    private static Properties log4jProperties = null;

    public synchronized static Path getLoggerFileLocation()
    {
        return Paths.get(log4jProperties.getProperty("log4j.appender.TOP.File"));
    }

    private synchronized static void init(Path logFileName, String propFileName) throws IOException
    {
        if (null == log4jProperties)
        {
            Path logDir = SecureConnectorDefaultProperties.getLogDir();
            if (Files.notExists(logDir))
            {
                Files.createDirectories(logDir);
            }

            log4jProperties = setLoggers(propFileName);
        }

        Path dataDir = SecureConnectorDefaultProperties.getLogDir();
        LoggerUtils.logFile = dataDir.resolve(logFileName);
        setLogFile(LoggerUtils.logFile);
    }

    private synchronized static Properties setlog4jProperties(Properties props)
    {
        if (null == log4jProperties)
        {
            log4jProperties = new Properties();
        }

        log4jProperties.putAll(props);
        return log4jProperties;
    }

    public synchronized static void setDebug()
    {
        setDebug(true);
    }

    public synchronized static void setDebug(boolean isEnabled)
    {
        if (isEnabled)
        {
            setLevel(Level.DEBUG);
        }
        else
        {
            setLevel(Level.ERROR);
        }
    }

    public synchronized static void setLevel(Level level)
    {
        if (null == log4jProperties)
        {
            return;
        }

        // this regex will look for any of the level and group them for replacement
        String regex = ".*(" + Level.ALL.toString() + ")|.*(" +
                Level.DEBUG.toString() + ")|.*(" +
                Level.ERROR.toString() + ")|.*(" +
                Level.FATAL.toString() + ")|.*(" +
                Level.INFO.toString() + ")|.*(" +
                Level.OFF.toString() + ")|.*(" +
                Level.TRACE.toString() + ")|.*(" +
                Level.WARN.toString() + ")";

        Properties newProps = new Properties();
        for (Entry<Object, Object> e : log4jProperties.entrySet())
        {
            if (e.getKey() != null && e.getValue() != null)
            {
                newProps.put(e.getKey(), e.getValue().toString().replaceAll(regex, level.toString()));
            }
        }

        log4jProperties = newProps;
        setLogFile(LoggerUtils.logFile);
    }

    public synchronized static Logger getLogger(Class<?> clazz)
    {
        return getLogger(clazz.getName());
    }

    public synchronized static Logger getLogger(Class<?> clazz, Path logFileName)
    {
        return getLogger(clazz.getName(), logFileName, DEFAULT_LOG4J_PROPERTIES_FILE_PATTERN);
    }

    public synchronized static Logger getLogger(Class<?> clazz, Path logFileName, String propFileName)
    {
        return getLogger(clazz.getName(), logFileName, propFileName);
    }

    public synchronized static Logger getLogger(String logger)
    {
        return getLogger(logger, SecureConnectorDefaultProperties.getLogFileName(),
                DEFAULT_LOG4J_PROPERTIES_FILE_PATTERN);
    }

    public synchronized static Logger getLogger(String logger, Path logFileName)
    {
        return getLogger(logger, logFileName, DEFAULT_LOG4J_PROPERTIES_FILE_PATTERN);
    }

    public synchronized static Logger getLogger(String logger, Path logFileName, String propFileName)
    {
        try
        {
            init(logFileName, propFileName);
        }
        catch (IOException e)
        {
            // TODO declares "throws IOException" and fix other classes instead of returning null
            return null;            
        }
        return Logger.getLogger(logger);
    }

    public synchronized static void setLogToConsole(Level level)
    {
        ConsoleAppender console = new ConsoleAppender();
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(level);
        console.activateOptions();

        Logger.getRootLogger().addAppender(console);
    }

    private synchronized static void setLogFile(Path logFile)
    {
        LoggerUtils.logFile = logFile;
        log4jProperties.setProperty("log4j.appender.TOP.File", LoggerUtils.logFile.toString());
        PropertyConfigurator.configure(log4jProperties);
    }

    private synchronized static Properties setLoggers(String propFileName)
    {
        Properties allProps = new Properties();

        Map<String, String> resourceFiles = ResourceList.getResourcesFromClassPath(Pattern.compile(propFileName),
                Pattern.compile(".*dsoc.*.jar|.*ras-va.*.jar"));

        for (Properties props : loadProperties(resourceFiles))
        {
            allProps.putAll(props);
        }

        return setlog4jProperties(allProps);
    }

    private synchronized static List<Properties> loadProperties(Map<String, String> elements)
    {
        JarFile jarFile = null;
        List<Properties> ret = new ArrayList<Properties>();
        for (Map.Entry<String, String> entry : elements.entrySet())
        {
            for (String fileX : entry.getValue().split("\\|"))
            {
                InputStream inputStream = null;
                try
                {
                    if (((String) entry.getKey()).endsWith(".jar"))
                    {
                        jarFile = new JarFile(entry.getKey());
                        inputStream = jarFile.getInputStream(jarFile.getEntry(fileX));
                    }
                    else
                    {
                        inputStream = new FileInputStream(entry.getKey());
                    }

                    if (inputStream != null)
                    {
                        // The next line can be uncommented by a developer to see what properties files are being used.
                        // This is important in understanding why messages may be logged (or not) because this code 
                        // essentially reads all of the *log4j.properties files it finds in the classpath and uses the 
                        // the contents of all of them.  Duplicate properties, such as "log4j.rootLogger=DEBUG, TOP" will be 
                        // overwritten by the last file, meaning that the setting in the last file read is the effective 
                        // setting for the whole process
                        // TODO Fix logging
                        //System.out.println("[LoggerUtils::loadProperties] Loading properities from " + entry.getKey());
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        ret.add(properties);
                    }
                    else
                    {
                        System.out.println("[LoggerUtils::loadProperties] Can't get input stream from " + fileX);
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Failed loading properties from input:\n" + elements + " " + e.getMessage());
                }
                finally
                {
                    if (null != jarFile)
                    {
                        try
                        {
                            jarFile.close();
                        }
                        catch (IOException e)
                        {
                        }
                    }

                    if (inputStream != null)
                    {
                        try
                        {
                            inputStream.close();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                }
            }
        }
        return ret;
    }
}
