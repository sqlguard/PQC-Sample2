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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.ResourceList;
import com.guardium.connector.common.SecureConnectorPluginTaskInterface;
import com.guardium.connector.common.Utils;
import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class IntegrationTestWithDatabase extends IntegrationTestWithLog
{
    protected static String dbUser = "testuser";
    protected static String dbPassword = "test";
    protected boolean autoCreateDB = true;

    public IntegrationTestWithDatabase()
    {
    }

    public IntegrationTestWithDatabase(boolean autoCreateDB)
    {
        this.autoCreateDB = autoCreateDB;
    }

    @BeforeClass
    public static synchronized void setupDatabase() throws SQLException, DbException, IOException
    {
        SecureConnectorDefaultProperties.setDBUser(dbUser);
        SecureConnectorDefaultProperties.setDBPassword(dbPassword);
    }

    @BeforeMethod
    public void setupEachTestDatabase(Method method) throws ClassNotFoundException, SQLException,
            IllegalAccessException,
            InstantiationException, DbException, IOException
    {
        // delete the backup dir from the previous test
        rmDir(SecureConnectorDefaultProperties.getDBDir());
        rmDir(SecureConnectorDefaultProperties.getBackupDir());

        if (autoCreateDB)
        {
            installDb();
        }
    }

    // override this and provide the correct DBHandler implementation for the layer or plugin
    protected DBHandler getInstallationDBHandler() throws DbException
    {
        return null;
    }

    protected void installDb() throws IllegalAccessException, InstantiationException, ClassNotFoundException,
            DbException, IOException, SQLException
    {
        // Create the database - H2 will do this automagically
        DBHandler.createDB();

        // Instantiating the DBHandler will create a connection pool to the db that was just created above
        DBHandler dbInstaller = getInstallationDBHandler();

        // Install the client tables and data first
        dbInstaller.preInit();
        dbInstaller.init();
        dbInstaller.postInit();

        // This will install DB of all the plugins
        Pattern jarMatcher = Pattern.compile(".*dsoc.*.jar|.*ras-va.*.jar");
        Map<String, String> elements = ResourceList.getResourcesFromClassPath(
                Pattern.compile(".*\\.plugin.properties"), jarMatcher);

        Logger log = LoggerUtils.getLogger(IntegrationTestWithDatabase.class);
        String list = new String("Looking for plugins in the following places:");

        ClassLoader classLoader = this.getClass().getClassLoader();
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        for (URL url : urls)
        {
            File file;
            try
            {
                file = Paths.get(url.toURI()).toFile();
            }
            catch (URISyntaxException e)
            {
                // if we got a bad URI log it and move on to the next entry
                log.debug("Bad URI: " + e.getMessage());
                continue;
            }

            if (jarMatcher.matcher(file.getName()).matches())
            {
                list += System.lineSeparator() + file.getAbsolutePath();
            }
        }
        log.debug(list);

        for (Properties prop : Utils.loadProperties(elements))
        {
            if (prop.getProperty("taskName") != null && prop.getProperty("taskExecutor") != null)
            {
                SecureConnectorPluginTaskInterface pluginTask = (SecureConnectorPluginTaskInterface) Class.forName(
                        prop
                                .getProperty("taskExecutor")).newInstance();

                List<DBHandler> dbHandlers = pluginTask.getDBInstallers();
                // not all plugins have a db installer
                if (dbHandlers != null)
                {
                    log.debug("Found " + dbHandlers.size() + " DBHandlers for " + pluginTask.getClass()
                            .getCanonicalName());
                    for (DBHandler pluginDBHandler : dbHandlers)
                    {
                        pluginDBHandler.preInit();
                        pluginDBHandler.init();
                        pluginDBHandler.postInit();
                    }
                }
            }
        }
    }
}
