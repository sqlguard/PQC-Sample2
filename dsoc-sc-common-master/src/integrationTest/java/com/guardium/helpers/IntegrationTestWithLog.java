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

package com.guardium.helpers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.BeforeMethod;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class IntegrationTestWithLog extends IntegrationTest
{
    protected Path logFileName;

    @BeforeMethod
    public void setupEachTestLog(Method method) throws IOException
    {
        // name the log file for the test
        logFileName = Paths.get(method.getName() + ".log");

        // set the log file for the test
        SecureConnectorDefaultProperties.setLogFileName(logFileName);

        // delete the log file from the last test run
        SecureConnectorDefaultProperties.getLogDir().resolve(logFileName).toFile().delete();
    }
}
