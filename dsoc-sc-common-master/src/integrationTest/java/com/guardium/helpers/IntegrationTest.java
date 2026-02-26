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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class IntegrationTest
{
    protected static final Path testDir = Paths.get(".", "integration-test");
    protected Path testPath;

    @BeforeClass
    public void cleanupLastRun() throws IOException
    {
        rmDir(testDir.resolve(getTestBasePath(this.getClass())));
    }
    
    @BeforeMethod
    public void dataDir(Method method) throws SQLException, DbException, IOException
    {
        testPath = testDir.resolve(getTestPath(method));

        SecureConnectorDefaultProperties.setHomeDir(testPath);
        rmDir(testPath);

        makeVersionFile("99.99.99");
    }

    protected static void makeVersionFile(String version) throws IOException
    {
        Files.write(SecureConnectorDefaultProperties.getConfDir().resolve("VERSION"), version.getBytes());
    }

    protected static Path getTestBasePath(Class<?> clazz)
    {
        return Paths.get(clazz.getSimpleName());
    }
    
    protected Path getTestPath(Method method)
    {
        return getTestBasePath(this.getClass()).resolve(method.getName());
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
