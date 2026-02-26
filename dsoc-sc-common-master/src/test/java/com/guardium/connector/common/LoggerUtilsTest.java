/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2018, 2019                                  */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.test.helpers.TestWithLog;

class LoggerUtilsTest extends TestWithLog
{
    @Test
    void testGetLoggerFileLocation()
    {
        LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");
        Path loggerFileLocation = LoggerUtils.getLoggerFileLocation(); 
        assertTrue(loggerFileLocation.equals(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)));
    }

    @RepeatedTest(2)
    void testGetLoggerIsIdempotent()
    {
        Logger log = LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");
        assertTrue(LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*/test/test.log4j.properties") == log);
    }

    @Test
    void logFileNameError()
    {
        Logger log = LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");

        log.error("testGetLogger");

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testGetLogger")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 1);
    }

    @Test
    void testSetDebugFalse()
    {
        Logger log = LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");

        // must be called after getLogger
        LoggerUtils.setDebug(false);

        log.debug("testSetDebugFalse");

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testSetDebugFalse")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 0);
    }

    @Test
    void testSetDebugTrue()
    {
        Logger log = LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");

        // must be called after getLogger
        LoggerUtils.setDebug();

        log.debug("testSetDebugTrue");

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testSetDebugTrue")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 1);
    }

    @Test
    void testSetLevelWarn()
    {
        Logger log = LoggerUtils.getLogger(LoggerUtilsTest.class, logFileName, ".*LoggerUtilsTest.log4j.properties");

        // the first half of this test turns off debug level (i.e. sets level to ERROR)
        // and verifies that a DEBUG message is not logged

        // must be called after getLogger
        LoggerUtils.setDebug(false);

        log.debug("testSetLevelWarn");

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testSetLevelWarn")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 0);
        list.clear();

        // the second part of this test sets the logging level to WARN
        // and verifies that a warning message is logged
        LoggerUtils.setLevel(Level.WARN);
        log.warn("testSetLevelWarn");

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testSetLevelWarn")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 1);
        list.clear();

        // the third part of this test sets log level back to ERROR
        // and verifies that a warn message is not logged
        LoggerUtils.setLevel(Level.ERROR);

        log.debug("testSetLevelWarn2");

        try (Stream<String> stream = Files.lines(SecureConnectorDefaultProperties.getLogDir().resolve(logFileName)))
        {
            list = stream.filter(line -> line.contains("testSetLevelWarn2")).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            fail("IOException reading log file");
        }

        assertTrue(list.size() == 0);
    }
}
