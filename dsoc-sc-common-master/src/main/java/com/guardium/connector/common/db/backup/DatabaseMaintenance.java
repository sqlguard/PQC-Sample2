/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                 */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.db.backup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.h2.tools.RunScript;
import org.h2.util.ScriptReader;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.db.H2Url;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public abstract class DatabaseMaintenance extends DBHandler
{
    private static Logger log = LoggerUtils.getLogger(DatabaseMaintenance.class);

    private static final String DB_MAINTENCE_SCHEMA = "DB_MAINTENANCE";
    private static final String DROP_SCHEMA = "DROP SCHEMA IF EXISTS " + DB_MAINTENCE_SCHEMA + " CASCADE;";
    private static final String CREATE_SCHEMA = "CREATE SCHEMA IF NOT EXISTS " + DB_MAINTENCE_SCHEMA + ";";
    private static final String SET_SCHEMA = "SET SCHEMA " + DB_MAINTENCE_SCHEMA + ";";

    private static final String TABLE_CATALOG = SecureConnectorDefaultProperties.getDBName().toString().toUpperCase();

    private static final String QUERY_NEW_TABLES = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = '"
            + TABLE_CATALOG + "' AND TABLE_SCHEMA = '" + DB_MAINTENCE_SCHEMA
            + "' AND TABLE_NAME NOT IN (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = '"
            + TABLE_CATALOG + "' AND TABLE_SCHEMA ='PUBLIC')";

    private static final String QUERY_NEW_COLUMNS = "SELECT A.TABLE_NAME, A.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS A WHERE A.TABLE_CATALOG = '"
            + TABLE_CATALOG + "' AND A.TABLE_SCHEMA = '" + DB_MAINTENCE_SCHEMA
            + "' AND A.COLUMN_NAME NOT IN (SELECT B.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS B WHERE B.TABLE_CATALOG = '"
            + TABLE_CATALOG + "' AND B.TABLE_SCHEMA ='PUBLIC' AND A.TABLE_NAME = B.TABLE_NAME)";

    protected static final String CLEAN_UP_DATA = "DELETE FROM SECURE_CONNECTOR_TASK_INFO WHERE TASK_TYPE LIKE 'DOWNLOAD_%'";
    protected String cleanUpCommand;  // Clean up data before backup. Put sql here

    protected Path schemaFile;
    protected Path dataFile;
    protected String[] backupDDLAndData;  // table names to backup; both ddl and data
    protected String[] backupDDLNoData;   // table names to backup; just ddl, no data

    public DatabaseMaintenance() throws DbException
    {
    }

    public DatabaseMaintenance(Path schemaFile, Path dataFile, String[] backupDDLAndData, String[] backupDDLNoData)
            throws DbException
    {
        this.schemaFile = schemaFile;
        this.dataFile = dataFile;
        this.backupDDLAndData = backupDDLAndData;
        this.backupDDLNoData = backupDDLNoData;
        this.cleanUpCommand = CLEAN_UP_DATA;
    }

    protected abstract InputStreamReader getInputStreamReader(String fileName) throws IOException;

    public String getTableNameListForBackup(H2Url url, List<String> tableNamesForDataBackup)
    {
        // We don't know how many tables we need to back up because the customer installed different versions
        String tableNames = "", name = "";
        String tableNameSql = "SELECT UPPER(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = '"
                + SecureConnectorDefaultProperties.getDBName().toString().toUpperCase()
                + "' AND TABLE_SCHEMA ='PUBLIC'";

        try (Connection conn = DriverManager.getConnection(url.toString(), SecureConnectorDefaultProperties.getDBUser(),
                SecureConnectorDefaultProperties.getDBPassword());
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(tableNameSql);)
        {
            while (rs.next())
            {
                name = rs.getString(1);
                if (Arrays.asList(this.backupDDLAndData).contains(name))
                {
                    tableNames = tableNames + name + ",";
                    tableNamesForDataBackup.add(name);
                }

                if (Arrays.asList(this.backupDDLNoData).contains(name))
                    tableNames = tableNames + name + ",";

            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        if (!tableNames.equals(""))
        {
            tableNames = tableNames.substring(0, tableNames.length() - 1);
        }

        return tableNames;
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

        log.debug("[DatabaseMaintenance::rmDir] " + dir + " deleted");
    }

    public void backupDatabase(H2Url backupDbUrl) throws DbException
    {
        Path backupDir = SecureConnectorDefaultProperties.getBackupDir();
        log.info("[DatabaseMaintenance::backupDatabase] Backing up database " + backupDbUrl + " to "
                + backupDir);
        String command = "";
        List<String> tableNamesForDataBackup = new ArrayList<String>();

        String dbUser = SecureConnectorDefaultProperties.getDBUser();
        String dbPassword = SecureConnectorDefaultProperties.getDBPassword();

        // 1. Backup the table DDL as it is tableName.sql
        // 2. Backup the data into csv file tableName.csv
        try (Connection conn = DriverManager.getConnection(backupDbUrl.toString(), dbUser, dbPassword);
                Statement stmt = conn.createStatement();)
        {
            // cleanup data before backup
            stmt.execute(cleanUpCommand);

            String tableNameList = getTableNameListForBackup(backupDbUrl, tableNamesForDataBackup);

            // backup ddl
            command = "SCRIPT NODATA TO '" + backupDir.resolve("backup.sql") + "' TABLE " + tableNameList;
            stmt.execute(command);
            log.debug("[DatabaseMaintenance::backupDatabase] Successfully backed up DDL for " + tableNameList);

            // backup data to csv files
            for (String tableName : tableNamesForDataBackup)
            {
                command = "CALL CSVWRITE('" + backupDir.resolve(tableName + ".csv") + "', 'SELECT * FROM "
                        + tableName + "')";
                stmt.execute(command);
                log.debug("[DatabaseMaintenance::backupDatabase] " + tableName + " successfully backed up");
            }
        }
        catch (SQLException e)
        {
            log.error("Upgrade error: " + e.getMessage());
            throw new DbException("db.upgrade.error", "Problem backing up database: " + e
                    .getMessage());
        }

        try
        {
            fixDDL();
        }
        catch (IOException e1)
        {
            log.error("Error fixing DDL: " + e1.getMessage());
            throw new DbException("db.upgrade.error", "Error fixing DDL: " + e1.getMessage());
        }

        // we need to do this so that we can cleanup the db files -- if we don't then H2 may hang onto or
        // recreate the trace file which may cause the restore to think that the tables are still there
        shutdownDB(backupDbUrl, dbUser, dbPassword);

        if (verifyBackup(backupDir))
        {

            log.info("[DatabaseMaintenance::backupDatabase] Backup complete");
        }
        else
        {
            log.error("[DatabaseMaintenance::backupDatabase] Backup incomplete");
        }
    }

    /*
     * Check to make sure that csv files exist for all the tables we expect to backup
     */
    public boolean verifyBackup(Path backupDir)
    {
        boolean valid = true;

        Path backupSql = backupDir.resolve("backup.sql");
        if (Files.notExists(backupSql))
        {
            log.error("The backup is incomplete.  The file \"backup.sql\" was not found.");
            valid = false;
        }

        List<String> tableNames = new ArrayList<String>(Arrays.asList(backupDDLAndData));
        for (String table : tableNames)
        {
            log.trace("Checking backup for table " + table);
            Path tableCsv = backupDir.resolve(Paths.get(table + ".csv"));
            try
            {
                FileChannel tableFile = FileChannel.open(tableCsv);
                if (Files.notExists(tableCsv))
                {
                    log.error("The backup is incomplete.  The file \"" + tableCsv + "\" was not found.");
                    valid = false;
                }
                else if (0 == tableFile.size())
                {
                    log.warn("The backup may be incomplete.  The file \"" + tableCsv + "\" is empty.");
                }
            }
            catch (IOException e)
            {
                valid = false;
            }
        }

        return valid;
    }

    protected void shutdownDB(H2Url dbUrl, String dbUser, String dbPassword)
    {
        try (Connection conn = DriverManager.getConnection(dbUrl.toString(), dbUser, dbPassword);
                Statement stmt = conn.createStatement();)
        {
            stmt.execute("SHUTDOWN;");
        }
        catch (SQLException sql)
        {
            // log errors here and move on
            log.error("Could not shutdown database: " + sql.getMessage());
        }

        closeConnectionPool();
    }

    private void deleteDbFiles() throws IOException
    {
        // delete the PRE 1.0.422 db files to cover the conversion case - this will only 
        // happen once in a connector's lifetime
        Path dbPath = SecureConnectorDefaultProperties.getDBPath();
        Path h2File = dbPath.resolveSibling(dbPath.getFileName() + ".h2.db");

        if (h2File.toFile().delete())
        {
            log.debug("Deleted " + h2File);
        }

        // delete the db file - main upgrade path
        Path mvFile = dbPath.resolveSibling(dbPath.getFileName() + ".mv.db");
        if (mvFile.toFile().delete())
        {
            log.debug("Deleted " + mvFile);
        }

        Path traceFile = dbPath.resolveSibling(dbPath.getFileName() + ".trace.db");
        if (traceFile.toFile().delete())
        {
            log.debug("Deleted " + traceFile);
        }
    }

    public void restoreDatabase(H2Url restoreDBURL) throws DbException, IOException
    {
        Path backupDir = SecureConnectorDefaultProperties.getBackupDir();

        // don't delete the old files unless the backup exists
        if (verifyBackup(backupDir))
        {
            // we are going to create a new db using and then restore the data using the backup sql files,
            // so delete the current db as it is in the way
            deleteDbFiles();
        }
        else
        {
            log.error("Backup missing or incomplete, restore aborted");
            throw new DbException("db.upgrade.error", "Backup missing or incomplete, restore aborted");
        }

        log.info("[DatabaseMaintenance::restoreDatabase] Restoring to " + SecureConnectorDefaultProperties
                .getDBCreationUrl());

        String command = "";
        try (Connection conn = DriverManager.getConnection(restoreDBURL.toString(), SecureConnectorDefaultProperties
                .getDBUser(), SecureConnectorDefaultProperties.getDBPassword());
                Statement stmt = conn.createStatement();)
        {
            // restore the schema
            command = "RUNSCRIPT FROM '" + backupDir.resolve("backup.sql") + "';";
            stmt.execute(command);

            // restore the data from the csv files
            for (String tableName : this.backupDDLAndData)
            {
                try
                {
                    command = "INSERT INTO " + tableName + " SELECT * FROM CSVREAD('" + backupDir.resolve(
                            tableName + ".csv") + "', null, 'charset=UTF-8')";
                    stmt.execute(command);

                    log.debug("[DatabaseMaintenance::restoreDatabase] Table " + tableName + " successfully restored");
                }
                catch (SQLException e)
                {
                    if (e.getErrorCode() == 42102)
                    {
                        log.error("[DatabaseMaintenance::restoreDatabase] Table not found: " + e.getMessage());
                    }
                    else
                    {
                        throw e;
                    }
                }
            }

            rmDir(backupDir);
        }
        catch (SQLException e)
        {
            log.error("Database upgrade error: " + e.getMessage());
            throw new DbException("db.upgrade.error", "Problem during restore: " + e.getMessage());
        }

        log.info("[DatabaseMaintenance::restoreDatabase] Restore complete");
    }

    public void upgradeDatabase() throws DbException
    {
        boolean isDebugEnabled = log.isDebugEnabled();

        List<String> newDBTables = new ArrayList<String>();
        Map<String, String> newColumns = new HashMap<String, String>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                InputStreamReader reader = getInputStreamReader(this.schemaFile.toString());)
        {
            // Build the new DB in a separate schema and compare tables to get new tables and columns
            stmt.execute(DROP_SCHEMA);
            stmt.execute(CREATE_SCHEMA);
            stmt.execute(SET_SCHEMA);

            if (isDebugEnabled)
                log.debug("[DatabaseMaintenance::upgradeDatabase] Building the new db in the DB_MAINTENANCE schema");
            RunScript.execute(conn, reader);

            stmt.execute("SET SCHEMA PUBLIC");
            newDBTables = getNewTableNames();
            if (!newDBTables.isEmpty())
            {
                createNewTables(newDBTables);
            }

            newColumns = getNewColumnNames();
            if (!newColumns.isEmpty())
            {
                createNewColumns(newColumns);
            }

            stmt.execute(DROP_SCHEMA);
            populateData();

            log.info(
                    "[DatabaseMaintenance::upgradeDatabase] Schema upgrade complete including initial data population");
        }
        catch (SQLException | IOException e)
        {
            log.error("Database upgrade error: " + e.getMessage());
            throw new DbException("db.upgrade.error", "Problem upgrading database: " + e.getMessage());
        }
    }

    private List<String> getNewTableNames() throws DbException
    {
        List<String> newDBTables = new ArrayList<String>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(QUERY_NEW_TABLES);)
        {
            while (rs.next())
            {
                newDBTables.add(rs.getString(1));
            }
        }
        catch (SQLException sql)
        {
            log.error("Database upgrade error when getting the new table name: " + sql.getMessage());
            throw new DbException("db.init.error", sql.getMessage());
        }

        return newDBTables;
    }

    private Map<String, String> getNewColumnNames() throws DbException
    {
        Map<String, String> newColumns = new HashMap<String, String>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(QUERY_NEW_COLUMNS);)
        {
            String columnName = "";
            String tmp = "";
            String tableName = "";
            while (rs.next())
            {
                tableName = rs.getString(1);
                columnName = rs.getString(2);
                tmp = newColumns.get(tableName);

                if (tmp == null || tmp.equals(""))
                    newColumns.put(tableName, columnName);
                else
                    newColumns.put(tableName, tmp + "," + columnName);
            }
        }
        catch (SQLException sql)
        {
            log.error("Database upgrade error when getting the new column name: " + sql.getMessage());
            throw new DbException("db.init.error", sql.getMessage());
        }

        return newColumns;
    }

    private void createNewTables(List<String> newDBTables) throws DbException
    {
        String tableString = "CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+";
        String indexString = "CREATE\\s+INDEX\\s+IF\\s+NOT\\s+EXISTS\\s+";

        for (String tbName : newDBTables)
        {
            try (Connection conn = getConnection();
                    Statement stmt = conn.createStatement();
                    InputStreamReader rder = getInputStreamReader(this.schemaFile.toString());
                    ScriptReader r = new ScriptReader(rder);)
            {
                while (true)
                {
                    String sql = r.readStatement();
                    if (sql == null)
                    {
                        break;
                    }
                    if (sql.trim().length() == 0)
                    {
                        continue;
                    }

                    Pattern tablePattern = Pattern.compile(tableString + tbName);
                    Matcher tableMatcher = tablePattern.matcher(sql);

                    Pattern indexPattern = Pattern.compile(indexString + tbName);
                    Matcher indexMatcher = indexPattern.matcher(sql);

                    if (tableMatcher.find() || indexMatcher.find())
                    {
                        if (log.isDebugEnabled())
                            log.debug("[DatabaseMaintenance::createNewTables] Creating table/index: " + sql);
                        stmt.execute(sql);
                    }
                }
            }
            catch (SQLException | IOException e)
            {
                log.error("Error creating table: " + e.getMessage());
                throw new DbException("db.init.error", e.getMessage());
            }
        }
    }

    private void fixDDL() throws IOException
    {
        // Massage the backup.sql - reuse the sequence if it exists
        log.debug("[DatabaseMaintenance::fixDDl] Fixing DDL");

        Path path = SecureConnectorDefaultProperties.getBackupDir().resolve("backup.sql");
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);

        content = content.replaceAll("IF NOT EXISTS", "");
        content = content.replaceAll("CREATE USER", "CREATE USER IF NOT EXISTS");
        content = content.replaceAll("CREATE SEQUENCE", "CREATE SEQUENCE IF NOT EXISTS");

        Files.write(path, content.getBytes(charset));
    }

    private void createNewColumns(Map<String, String> newColumns) throws DbException
    {
        // Create new columns
        String n = "CREATE TABLE IF NOT EXISTS ";
        String addColumnStmt = "";
        for (String tbName : newColumns.keySet())
        {
            try (Connection conn = getConnection();
                    Statement stmt = conn.createStatement();
                    InputStreamReader rder = getInputStreamReader(this.schemaFile.toString());
                    ScriptReader r = new ScriptReader(rder);)
            {
                String[] columnNames = ((String) newColumns.get(tbName)).split(",");

                while (true)
                {
                    String sql = r.readStatement();
                    if (sql == null)
                    {
                        break;
                    }
                    if (sql.trim().length() == 0)
                    {
                        continue;
                    }

                    if (sql.indexOf(n + tbName) > -1)
                    {
                        sql = sql.toUpperCase();
                        String[] columnStatements = sql.trim().split("\\n");
                        for (String colStatement : columnStatements)
                        {
                            if (colStatement.equals("\n") || colStatement.indexOf("CREATE TABLE") > -1
                                    || colStatement.indexOf("CREATE INDEX") > -1)
                                continue;

                            for (String c : columnNames)
                            {
                                if (colStatement.indexOf(c) > -1)
                                {
                                    colStatement = colStatement.trim().replaceAll(",", "");
                                    addColumnStmt = "ALTER TABLE " + tbName + " ADD COLUMN IF NOT EXISTS "
                                            + colStatement;
                                    stmt.execute(addColumnStmt);
                                    if (log.isDebugEnabled())
                                        log.debug("[DatabaseMaintenance::createNewColumns] Adding columns for table "
                                                + tbName + ": " + addColumnStmt);
                                }
                            }
                        }
                    }
                }
            }
            catch (SQLException | IOException e)
            {
                log.error("Database upgrade error when getting the new column name: " + e.getMessage());
                throw new DbException("db.init.error", e.getMessage());
            }
        }
    }

    private void populateData() throws SQLException, IOException
    {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                InputStreamReader reader = getInputStreamReader(this.dataFile.toString());)
        {
            RunScript.execute(conn, reader);
        }
    }
}
