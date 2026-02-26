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

package com.guardium.test.helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class TestWithLog extends Test
{
    protected Path logFileName;

    @BeforeEach
    void setupEachTestLog(TestInfo testInfo) throws IOException
    {   
        // name the log file for the test
        logFileName = Paths.get(getTestPath(testInfo).getFileName() + ".log");

        // set the log file for the test
        SecureConnectorDefaultProperties.setLogFileName(logFileName);

        // delete the log file from the last test run
        Path logFile = SecureConnectorDefaultProperties.getLogDir().resolve(logFileName);
        logFile.toFile().delete();
    }
}
