/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2019                                        */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

public class H2Url
{
    public static final String PROTOCOL = "jdbc:h2";
    private Path dbPath = Paths.get("");
    private Map<String, String> options = new Hashtable<String, String>();

    public H2Url(Path dbPath)
    {
        if (null != dbPath)
        {
            setDbPath(dbPath);
        }
    }

    public H2Url(Path dbPath, Map<String, String> options)
    {
        if (null != dbPath)
        {
            setDbPath(dbPath);
        }
        if (null != options)
        {
            setOptions(options);
        }
    }

    // supports only the following format:
    // "jdcb:h2:<path>;<options>, e.g. "jdbc:h2:/path/db/secure-connector-db;IFEXISTS=true;"
    public H2Url(String dbUrl)
    {
        if (null != dbUrl)
        {
            dbUrl.trim();
            if (!dbUrl.isEmpty())
            {
                String[] protAndPath = dbUrl.split(";");
                if (protAndPath.length >= 1)
                {
                    String[] parts = protAndPath[0].split(":");
                    if (parts.length == 3)
                    {
                        dbPath = Paths.get(parts[2]);
                        String optionString = dbUrl.substring(dbUrl.indexOf(";"), dbUrl.length());
                        options = parseOptionString(optionString);
                    }
                }
            }
        }
    }

    public H2Url()
    {
    }

    @Override
    public String toString()
    {
        return PROTOCOL + ":" + getDbPath() + ";" + getOptionString();
    }

    public Path getDbPath()
    {
        return dbPath;
    }

    public void setDbPath(String dbPath)
    {
        setDbPath(Paths.get(dbPath));
    }

    public void setDbPath(Path dbPath)
    {
        this.dbPath = dbPath;
    }

    public Map<String, String> getOptions()
    {
        return options;
    }

    public void setOptions(Map<String, String> options)
    {
        for (Map.Entry<String, String> entry : options.entrySet())
        {
            this.options.put(entry.getKey().toUpperCase(), entry.getValue().toUpperCase());
        }
    }

    public String getOption(String option)
    {
        return options.get(option.trim().toUpperCase());
    }

    public void setOption(String option, String value)
    {
        options.put(option.trim().toUpperCase(), value.toUpperCase());
    }

    public String removeOption(String option)
    {
        return options.remove(option.trim().toUpperCase());
    }

    public String getOptionString()
    {
        String optionString = "";
        for (Map.Entry<String, String> entry : options.entrySet())
        {
            optionString += entry.getKey().toUpperCase() + "=" + entry.getValue().toUpperCase() + ";";
        }
        return optionString;
    }

    /**
     * parseOptionString
     * 
     * @param optionString
     *            e.g. ";
     * @return a map of key value pairs
     */
    public static Map<String, String> parseOptionString(String optionString)
    {
        Map<String, String> map = new Hashtable<String, String>();

        if (null != optionString)
        {
            // trim whitespace
            optionString.trim();

            // strip ";" from begging and end of string
            if (optionString.startsWith(";"))
            {
                optionString = optionString.substring(1, optionString.length());
            }

            if (!optionString.isEmpty() && optionString.endsWith(";"))
            {
                optionString = optionString.substring(0, optionString.length() - 1);
            }

            if (!optionString.isEmpty())
            {
                String[] options = optionString.split(";");
                for (String option : options)
                {
                    String[] keyVal = option.split("=");
                    if (keyVal.length == 2)
                    {
                        map.put(keyVal[0].trim().toUpperCase(), keyVal[1].trim().toUpperCase());
                    }
                }
            }
        }

        return map;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbPath == null) ? 0 : dbPath.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        H2Url other = (H2Url) obj;
        if (dbPath == null)
        {
            if (other.dbPath != null)
                return false;
        }
        else if (!dbPath.equals(other.dbPath))
            return false;
        if (options == null)
        {
            if (other.options != null)
                return false;
        }
        else if (!options.equals(other.options))
            return false;
        return true;
    }
}
