/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2018, 2019                                 */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.test.helpers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class Test
{
    protected static final Path testDir = Paths.get(".", "test");
    protected static final String authToken = "not a token";

    protected Path testPath;

    @BeforeAll
    public static void cleanupLastRun(TestInfo testInfo) throws IOException
    {
        // cleanup everything from the previous test class run
        rmDir(testDir.resolve(getTestBasePath(testInfo)));
    }

    @BeforeEach
    public void dataDir(TestInfo testInfo) throws SQLException, DbException, IOException
    {
        testPath = testDir.resolve(getTestPath(testInfo));

        // cleanup everything from this previous test method run
        rmDir(testPath);

        SecureConnectorDefaultProperties.setHomeDir(testPath);
        SecureConnectorDefaultProperties.setAuthorization(authToken);

        makeVersionFile("99.99.99.test");
    }

    protected static void makeVersionFile(String version) throws IOException
    {
        Files.write(SecureConnectorDefaultProperties.getConfDir().resolve("VERSION"), version.getBytes());
    }

    protected static Path getTestBasePath(TestInfo testInfo)
    {
        return Paths.get(testInfo.getTestClass().get().getSimpleName());
    }

    protected Path getTestPath(TestInfo testInfo)
    {
        return getTestBasePath(testInfo).resolve(testInfo.getTestMethod().get().getName());
    }

    protected static void rmDir(Path dir) throws IOException
    {
        try
        {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    // Files.delete(file) will not work on windows because it throws and exception if the
                    // file is marked as read only. that is the case for the secret files (journal.bin)
                    file.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    dir.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (NoSuchFileException e)
        {
            // ok to ignore this
        }
    }
}
