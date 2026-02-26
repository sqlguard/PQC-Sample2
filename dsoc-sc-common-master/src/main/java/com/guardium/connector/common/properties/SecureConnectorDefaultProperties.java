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

package com.guardium.connector.common.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.guardium.connector.common.Version;
import com.guardium.connector.common.crypto.Vault;
import com.guardium.connector.common.db.H2Url;
import com.guardium.connector.common.env.Environment;
import com.guardium.connector.common.exceptions.InvalidVersionException;
import com.guardium.connector.common.exceptions.VaultException;

/**
 * This class is the central place to keep the default settings for Secure Connector. The settings are read from
 * environment variables the first time they are used. If the environment variable is not set, a default value is used
 * as shown in the list below.
 */
public class SecureConnectorDefaultProperties
{
    private static boolean IS_WIN = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            .indexOf("win") >= 0 ? true : false;

    // default values
    private static final String VERSION_FILE = "VERSION";
    private static final String DEFAULT_DSOC_URL = "https://datarisk.dsoc.ibm.com/";
    private static final Path DEFAULT_HOME_DIR = Paths.get(".");
    private static final Path DEFAULT_CONF_DIR = Paths.get("conf");
    private static final Path DEFAULT_LOG_DIR = Paths.get("Logs");
    private static final Path DEFAULT_LOG_FILE = Paths.get("secure-connector.log");
    private static final Path DEFAULT_UPDATE_DIR = Paths.get("..", "NewUpdate");
    private static final Path DEFAULT_UPDATE_FILE = Paths.get("ibm-secure-connector.zip");
    private static final Path DEFAULT_BACKUP_DIR = Paths.get("backup.auto");
    private static final Path DEFAULT_ENTROPY_FILE = Paths.get("entropy.bin");
    private static final Path DEFAULT_VAULT_FILE = Paths.get("journal.bin");
    private static final Path DEFAULT_DB_DIR = Paths.get("db");
    private static final Path DEFAULT_DB_NAME = Paths.get("secure-connector-db");
    private static final String DEFAULT_DB_DRIVER = "org.h2.Driver";
    private static final String DEFAULT_DB_USER = "GUARDIUM";
    private static final String DEFAULT_MAX_POOL_CONNECTIONS = "250";
    private static final String DEFAULT_AUTO_UPDATE_INTERVAL = "43200";
    private static final String DEFAULT_AUTO_UPDATE_FORCE_DOWNLOAD = "false";
    private static final String DEFAULT_CONNECTION_TIMEOUT = "30000"; // 30 seconds
    private static final String DEFAULT_DISABLE_UPDATES = "false"; // 30 seconds
    private static final String DEFAULT_LOCAL_ONLY = "false";
    private static final String DEFAULT_UPDATE_CHECK_INTERVAL = "1800"; //30 mins
    private static final String DEFAULT_CSV_UPLOAD_INTERVAL = "120"; //2 mins
    private static final String DEFAULT_SEND_PLUGIN_PROPERTIES_ON_REQUEST = "true";
    private static final String DEFAULT_DSOC_HASH_VERSION = "v2";
    private static final Path DEFAULT_KS_VAULT_FILE = Paths.get("journal1.bin");
    private static final Path DEFAULT_KEYSTORE_NAME = Paths.get("dsoc.jks");
    private static final Path DEFAULT_KEYSTORE_DIR = Paths.get("conf");
    private static final Path DEFAULT_SCRIPTS_DIR = Paths.get("scripts");

    // system property/environment variable names
    private static final String HOME_DIR = "DSOC_HOME_DIR";
    private static final String CONF_DIR = "DSOC_CONF_DIR";
    private static final String LOG_DIR = "DSOC_LOG_DIR";
    private static final String LOG_FILE = "DSOC_LOG_FILE";
    private static final String UPDATE_DIR = "DSOC_UPDATE_DIR";
    private static final String UPDATE_FILE = "DSOC_UPDATE_FILE";
    private static final String BACKUP_DIR = "DSOC_BACKUP_DIR";
    private static final String ENTROPY_FILE = "DSOC_ENTROPY_FILE";
    private static final String VAULT_FILE = "DSOC_VAULT_FILE";
    private static final String DB_DIR = "DSOC_DB_DIR";
    private static final String DB_NAME = "DSOC_DB_NAME";
    private static final String DSOC_URL = "DSOC_URL";
    private static final String AUTH_TOKEN = "DSOC_AUTH_TOKEN";
    private static final String DB_PATH = "DSOC_DB_PATH";
    private static final String DB_DRIVER = "DSOC_DB_DRIVER";
    private static final String DB_USER = "DSOC_DB_USER";
    private static final String DB_PASSWORD = "DSOC_DB_PASSWORD";
    private static final String MAX_POOL_CONNECTIONS = "MAX_POOL_CONNECTIONS";
    private static final String AUTO_UPDATE_INTERVAL = "DSOC_AUTO_UPDATE_INTERVAL";
    private static final String AUTO_UPDATE_FORCE_DOWNLOAD = "DSOC_AUTO_UPDATE_FORCE_DOWNLOAD";
    private static final String CONNECTION_TIMEOUT = "DSOC_CONNECTION_TIMEOUT";
    private static final String DISABLE_UPDATES = "DSOC_DISABLE_UPDATES";
    private static final String LOCAL_ONLY = "DSOC_LOCAL_ONLY";
    private static final String UPDATE_CHECK_INTERVAL = "DSOC_UPDATE_CHECK_INTERVAL";
    private static final String CSV_UPLOAD_INTERVAL = "DSOC_CSV_UPLOAD_INTERVAL";
    private static final String SEND_PLUGIN_PROPERTIES_ON_REQUEST = "DSOC_SEND_PLUGIN_PROPERTIES_ON_REQUEST";
    private static final String DSOC_HASH_VERSION = "DSOC_HASH_VERSION";
    private static final String KEYSTORE_NAME = "DSOC_KEYSTORE_NAME";
    private static final String KEYSTORE_PASSWORD = "DSOC_KEYSTORE_PASSWORD";
    private static final String KS_VAULT_FILE = "DSOC_VAULT_KS_FILE";
    private static final String KEYSTORE_DIR = "DSOC_KEYSTORE_DIR";
    private static final String SCRIPTS_DIR = "DSOC_SCRIPTS_DIR";

    public static synchronized String getServerUrl()
    {
        String url = System.getProperty(DSOC_URL);
        if (null == url)
        {
            url = System.getenv(DSOC_URL);
            if (null == url)
            {
                url = DEFAULT_DSOC_URL;
            }
        }

        if (!url.endsWith("/"))
        {
            url += "/";
        }

        return url;
    }

    private static String getValue(String value)
    {
        // first try to get the value from the system properties. doing this first
        // allows us to override the environment variables when starting the jvm
        String retval = System.getProperty(value);
        if (null == retval)
        {
            // if no system property was set, check for an environment variable
            retval = System.getenv(value);
        }
        // if neither is set this returns null
        return retval;
    }

    private static void setValue(String property, String value)
    {
        System.setProperty(property, value);
    }

    /**
     * Returns the desired system property (first) or environment variable. If not set, returns the default.
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static String getValue(String value, String defaultValue)
    {
        String retval = getValue(value);
        if (null == retval)
        {
            retval = defaultValue;
        }
        return retval;
    }

    private static Path getValue(String value, Path defaultValue)
    {
        return Paths.get(getValue(value, defaultValue.toString()));
    }

    private static Path makePath(Path path)
    {
        try
        {
            Files.createDirectories(path);
        }
        catch (IOException e)
        {
            // OK this looks bad.  However, this will succeed almost always. If it doesn't
            // there is a major problem with the filesystem, or permissions are wrong
            // or something.  If we throw here we have to declare the exception and that
            // will muck up a lot of code that isn't currently expecting to handle one.
            // We could do a couple of things.  One, change the code so that all callers are
            // prepared for an IOException even when just trying to get a directory or
            // filename, or two, switch the code so that directories are only created when 
            // they are set (by calling a setter method in this class).  In the latter
            // case we would need to rework the connector startup sequence so that we
            // explicitly call the setters; however, that is quite problematic since we
            // are loading in Tomcat and it is difficult to control the creation order of the
            // various controllers, etc.  So for now we are just going to silently ignore
            // this exception and let the caller bomb if there is a problem.  The logs
            // should indicate immediately that some file or directory was not found.
        }

        return path;
    }

    public static synchronized void setServerUrl(String url)
    {
        if (!url.endsWith("/"))
        {
            url += "/";
        }
        System.setProperty(DSOC_URL, url);
    }

    public static synchronized String getAuthorization()
    {
        return getValue(AUTH_TOKEN);
    }

    public static synchronized void setAuthorization(String authToken)
    {
        setValue(AUTH_TOKEN, authToken);
    }

    public static synchronized Version getVersion() throws IOException, InvalidVersionException
    {
        return getVersion(getConfDir().resolve(VERSION_FILE));
    }

    public static synchronized Version getPrevVersion() throws IOException, InvalidVersionException
    {
        return getVersion(getPrevVersionFile());
    }

    public static synchronized Version getNewVersion() throws IOException, InvalidVersionException
    {
        return getVersion(getNewVersionFile());
    }

    public static synchronized Version getVersion(Path versionFile) throws InvalidVersionException, IOException
    {
        if (null == versionFile)
        {
            throw new InvalidVersionException("invalid.file", "null file arg");
        }

        String version = "";
        try (BufferedReader versionReader = Files.newBufferedReader(versionFile))
        {
            version = versionReader.readLine();
        }
        catch (IOException e)
        {
            throw e;
        }
        return new Version(version);
    }

    public static synchronized boolean getIsWindows()
    {
        return IS_WIN;
    }

    public static synchronized Path getHomeDir()
    {
        return makePath(getValue(HOME_DIR, DEFAULT_HOME_DIR));
    }

    public static synchronized void setHomeDir(Path dir)
    {
        setValue(HOME_DIR, dir.toString());
    }

    public static synchronized Path getConfDir()
    {
        return makePath(getHomeDir().resolve(getValue(CONF_DIR, DEFAULT_CONF_DIR)));
    }

    public static synchronized void setConfDir(Path dir)
    {
        setValue(CONF_DIR, dir.toString());
    }

    public static synchronized Path getLogDir()
    {
        return makePath(getHomeDir().resolve(getValue(LOG_DIR, DEFAULT_LOG_DIR)));
    }

    public static synchronized void setLogDir(Path dir)
    {
        setValue(LOG_DIR, dir.toString());
    }

    public synchronized static Path getLogFileName()
    {
        return getValue(LOG_FILE, DEFAULT_LOG_FILE);
    }

    public synchronized static void setLogFileName(Path logFile)
    {
        setValue(LOG_FILE, logFile.toString());
    }

    public static synchronized Path getScriptsDir()
    {
        return makePath(getHomeDir().resolve(getValue(SCRIPTS_DIR, DEFAULT_SCRIPTS_DIR)));
    }

    public static synchronized void setScriptsDir(Path dir)
    {
        setValue(SCRIPTS_DIR, dir.toString());
    }

    public static synchronized Path getUpdateDir()
    {
        return makePath(getHomeDir().resolve(getValue(UPDATE_DIR, DEFAULT_UPDATE_DIR)));
    }

    public static synchronized void setUpdateDir(Path dir)
    {
        setValue(UPDATE_DIR, dir.toString());
    }

    public synchronized static Path getUpdateFileName()
    {
        return getValue(UPDATE_FILE, DEFAULT_UPDATE_FILE);
    }

    public synchronized static void setUpdateFileName(Path logFile)
    {
        setValue(UPDATE_FILE, logFile.toString());
    }

    public static synchronized Path getBackupDir()
    {
        return makePath(getDBDir().resolve(getValue(BACKUP_DIR, DEFAULT_BACKUP_DIR)));
    }

    public static synchronized void setBackupDir(Path dir)
    {
        setValue(BACKUP_DIR, dir.toString());
    }

    public static synchronized Path getBackupDirName()
    {
        return getValue(BACKUP_DIR, DEFAULT_BACKUP_DIR);
    }

    public synchronized static void setBackupDirName(Path backupDirName)
    {
        setValue(BACKUP_DIR, backupDirName.toString());
    }

    public synchronized static Path getEntropyFileName()
    {
        return getValue(ENTROPY_FILE, DEFAULT_ENTROPY_FILE);
    }

    public synchronized static void setEntropyFileName(Path entropyFile)
    {
        setValue(ENTROPY_FILE, entropyFile.toString());
    }

    public synchronized static Path getVaultFileName()
    {
        return getValue(VAULT_FILE, DEFAULT_VAULT_FILE);
    }

    public synchronized static Path getKSVaultFileName()
    {
        return getValue(KS_VAULT_FILE, DEFAULT_KS_VAULT_FILE);
    }

    public synchronized static void setVaultFileName(Path vaultFile)
    {
        setValue(VAULT_FILE, vaultFile.toString());
    }

    public synchronized static void setKSVaultFileName(Path vaultFile)
    {
        setValue(KS_VAULT_FILE, vaultFile.toString());
    }

    public synchronized static Path getDBDir()
    {
        return makePath(getHomeDir().resolve(getValue(DB_DIR, DEFAULT_DB_DIR)));
    }

    public synchronized static void setDBDir(Path dir)
    {
        setValue(DB_DIR, dir.toString());
    }

    public synchronized static Path getDBName()
    {
        return getValue(DB_NAME, DEFAULT_DB_NAME);
    }

    public synchronized static void setDBName(Path dbName)
    {
        setValue(DB_NAME, dbName.toString());
    }

    public synchronized static Path getDBPath()
    {
        return Paths.get(getValue(DB_PATH, getDBDir().resolve(getDBName()).toString()));
    }

    public synchronized static Path getFullDBPath()
    {
        Path dbPath = getDBPath();
        return dbPath.resolveSibling(dbPath.getFileName() + ".mv.db");
    }

    public synchronized static Path getFullDBTracePath()
    {
        Path dbPath = getDBPath();
        return dbPath.resolveSibling(dbPath.getFileName() + ".mv.db");
    }

    public synchronized static void setDBPath(Path dbPath)
    {
        System.setProperty(DB_PATH, dbPath.toString());
    }

    public synchronized static String getDBDriver()
    {
        return getValue(DB_DRIVER, DEFAULT_DB_DRIVER);
    }

    public synchronized static void setDBDriver(String dbDriver)
    {
        setValue(DB_DRIVER, dbDriver);
    }

    public synchronized static String getDBUser()
    {
        return getValue(DB_USER, DEFAULT_DB_USER);
    }

    public synchronized static void setDBUser(String dbUser)
    {
        setValue(DB_USER, dbUser);
    }

    public synchronized static H2Url getDBUrl()
    {
        return getDBUrl(getDBPath());
    }

    public synchronized static H2Url getDBCreationUrl()
    {
        return getDBCreationUrl(getDBPath());
    }

    public synchronized static H2Url getDBCreationUrl(Path dbPath)
    {
        // this url used H2 in embedded mode with the MVSTORE engine and compressed storage
        // the MAX_COMPACT_TIME settings gives H2 20 seconds to compact the db when the 
        // connector is shutdown
        H2Url url = new H2Url(dbPath);
        url.setOption("CIPHER", "AES");
        url.setOption("COMPRESS", "TRUE");
        url.setOption("MAX_COMPACT_TIME", "20000");
        return url;
    }

    public synchronized static H2Url getDBUrl(Path dbPath)
    {
        // by default we only want to connect if the db exists.  that avoids unintentional
        // database creation.  we need to control it so that we can initialize the schema
        H2Url url = getDBCreationUrl(dbPath);
        url.setOption("IFEXISTS", "TRUE");
        return url;
    }

    /**
     * This method is a utility for this class only. If the raw secret is needed one can use Vault.getKSSecret()
     * directly.
     * 
     * @return The generated secret
     */
    private synchronized static String getKeystoreSecret()
    {
        String secret = null;
        try
        {
            secret = Vault.getKeyStoreSecret();
        }
        catch (VaultException e)
        {
            // just return null
        }
        return secret;
    }

    /**
     * This method is a utility for this class only. If the raw secret is needed one can use Vault.getSecret() directly.
     * 
     * @return The generated secret
     */
    private synchronized static String getDBSecret()
    {
        String secret = null;
        try
        {
            secret = Vault.getDBSecret();
        }
        catch (VaultException e)
        {
            // just return null
        }
        return secret;
    }

    public synchronized static String getDBPassword()
    {
        String secret = getDBSecret();
        return secret + " " + secret;
    }

    public synchronized static void setDBPassword(String dbPassword)
    {
        setValue(DB_PASSWORD, dbPassword);
    }

    public synchronized static int getAutoUpdateInterval()
    {
        return Integer.parseInt(getValue(AUTO_UPDATE_INTERVAL, DEFAULT_AUTO_UPDATE_INTERVAL));
    }

    public synchronized static void setAutoUpdateInterval(int autoUpdateInterval)
    {
        setValue(AUTO_UPDATE_INTERVAL, Integer.toString(autoUpdateInterval));
    }

    public synchronized static boolean getAutoUpdateForceDownload()
    {
        return Boolean.getBoolean(getValue(AUTO_UPDATE_FORCE_DOWNLOAD, DEFAULT_AUTO_UPDATE_FORCE_DOWNLOAD));
    }

    public synchronized static void setAutoUpdateForceDownload(boolean autoUpdateForceDownload)
    {
        setValue(AUTO_UPDATE_FORCE_DOWNLOAD, Boolean.toString(autoUpdateForceDownload));
    }

    public synchronized static int getMaxPoolConnections()
    {
        return Integer.parseInt(getValue(MAX_POOL_CONNECTIONS, DEFAULT_MAX_POOL_CONNECTIONS));
    }

    public synchronized static void setMaxPoolConnections(int maxPoolConnections)
    {
        setValue(MAX_POOL_CONNECTIONS, Integer.toString(maxPoolConnections));
    }

    public static boolean getAllowSelfSignedCerts()
    {
        return Environment.ALLOW_SELF_SIGNED_CERTS;
    }

    public synchronized static int getConnectionTimeout()
    {
        return Integer.parseInt(getValue(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
    }

    public synchronized static void setConnectionTimeout(int timeout)
    {
        setValue(CONNECTION_TIMEOUT, Integer.toString(timeout));
    }

    public synchronized static boolean getSendPluginPropertiesOnRequest()
    {
        return Boolean.valueOf(getValue(SEND_PLUGIN_PROPERTIES_ON_REQUEST,
                DEFAULT_SEND_PLUGIN_PROPERTIES_ON_REQUEST));
    }

    public synchronized static void setSendPluginPropertiesOnRequest(boolean sendPluginPropertiesOnRequest)
    {
        setValue(SEND_PLUGIN_PROPERTIES_ON_REQUEST, Boolean.toString(sendPluginPropertiesOnRequest));
    }

    public static boolean getDisableUpdates()
    {
        String disable = getValue(DISABLE_UPDATES, DEFAULT_DISABLE_UPDATES);
        if (disable.equalsIgnoreCase("true") || disable.equalsIgnoreCase("1"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static void setDisableUpdates(boolean disable)
    {
        if (disable)
        {
            setValue(DISABLE_UPDATES, "true");
        }
        else
        {
            setValue(DISABLE_UPDATES, "false");
        }
    }

    public static boolean getLocalOnly()
    {
        return Boolean.parseBoolean(getValue(LOCAL_ONLY, DEFAULT_LOCAL_ONLY));
    }

    public static void setLocalOnly(boolean value)
    {
        if (value)
        {
            setValue(LOCAL_ONLY, "true");
        }
        else
        {
            setValue(LOCAL_ONLY, "false");
        }
    }

    public static int getUpdateCheckInterval()
    {
        return Integer.parseInt(getValue(UPDATE_CHECK_INTERVAL, DEFAULT_UPDATE_CHECK_INTERVAL));
    }

    public static void setUpdateCheckInterval(int interval)
    {
        setValue(UPDATE_CHECK_INTERVAL, Integer.toString(interval));
    }

    public static int getCSVUploadInterval()
    {
        return Integer.parseInt(getValue(CSV_UPLOAD_INTERVAL, DEFAULT_CSV_UPLOAD_INTERVAL));
    }

    public static void setCSVUploadInterval(int interval)
    {
        setValue(CSV_UPLOAD_INTERVAL, Integer.toString(interval));
    }


    public static String printValuestoString()
    {
        // @formatter:off
        return HOME_DIR + ": " + getHomeDir() + System.lineSeparator() +
                CONF_DIR + ": " + getConfDir() + System.lineSeparator() +
                LOG_DIR + ": " + getLogDir() + System.lineSeparator() +
                LOG_FILE + ": " + getLogFileName() + System.lineSeparator() +
                UPDATE_DIR + ": " + getUpdateDir() + System.lineSeparator() +
                UPDATE_FILE + ": " + getUpdateFileName() + System.lineSeparator() +
                DB_DIR + ": " + getDBDir() + System.lineSeparator() +
                DB_NAME + ": " + getDBName() + System.lineSeparator() +
                BACKUP_DIR + ": " + getBackupDirName() + System.lineSeparator() +
                DSOC_URL + ": " + getServerUrl() + System.lineSeparator() +
                DB_PATH + ": " + getDBPath() + System.lineSeparator() +
                DB_DRIVER + ": " + getDBDriver() + System.lineSeparator() +
                MAX_POOL_CONNECTIONS + ": " + getMaxPoolConnections() + System.lineSeparator() +
                AUTO_UPDATE_INTERVAL + ": " + getAutoUpdateInterval() + System.lineSeparator() +
                AUTO_UPDATE_FORCE_DOWNLOAD + ": " + getAutoUpdateForceDownload() + System.lineSeparator() +
                SEND_PLUGIN_PROPERTIES_ON_REQUEST + ": " + getSendPluginPropertiesOnRequest() + System.lineSeparator() +
                DISABLE_UPDATES + ": " + getDisableUpdates() + System.lineSeparator() +
                CONNECTION_TIMEOUT + ": " + getConnectionTimeout() + System.lineSeparator() +
                LOCAL_ONLY + ": " + getLocalOnly() + System.lineSeparator() +
                UPDATE_CHECK_INTERVAL + ": " + getUpdateCheckInterval() + System.lineSeparator() +
                CSV_UPLOAD_INTERVAL + ": " + getCSVUploadInterval() + System.lineSeparator() +
                DSOC_HASH_VERSION + ": " + getDownloadHashVersion();
        // @formatter:on
    }

    public synchronized static Path getNewVersionFile()
    {
        return getConfDir().resolve(VERSION_FILE + ".new");
    }

    public synchronized static Path getPrevVersionFile()
    {
        return getConfDir().resolve(VERSION_FILE + ".prev");
    }

    public static String getDownloadHashVersion()
    {
        String version = getValue(DSOC_HASH_VERSION, DEFAULT_DSOC_HASH_VERSION);
        return version.toLowerCase();
    }

    public static void setDownloadHashVersion(String version)
    {
        setValue(DSOC_HASH_VERSION, version);
    }

    public static Path getKeystorePath()
    {
        return getKeystoreDir().resolve(getKeystoreName());
    }

    public static Path getKeystoreDir()
    {
        return makePath(getHomeDir().resolve(getValue(KEYSTORE_DIR, DEFAULT_KEYSTORE_DIR)));
    }

    public static void setKeystoreDir(Path dir)
    {
        setValue(KEYSTORE_DIR, dir.toString());
    }

    public static Path getKeystoreName()
    {
        return getValue(KEYSTORE_NAME, DEFAULT_KEYSTORE_NAME);
    }

    public static void setKeystoreName(Path name)
    {
        setValue(KEYSTORE_NAME, name.toString());
    }

    public static void setkeystorePw(String password)
    {
        setValue(KEYSTORE_PASSWORD, password);
    }

    public static String getKeystorePw()
    {
        return getValue(KEYSTORE_PASSWORD, getKeystoreSecret());
    }
}
