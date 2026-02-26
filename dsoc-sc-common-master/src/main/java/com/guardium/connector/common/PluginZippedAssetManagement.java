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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public abstract class PluginZippedAssetManagement implements PluginAssetManagementInterface
{
    protected Logger logger = LoggerUtils.getLogger(PluginZippedAssetManagement.class);

    public HashMap<Path, String> processAssets(byte[] assetAsByteArray) throws Exception
    {
        return processAssets(assetAsByteArray, "0");
    }

    public HashMap<Path, String> processAssets(byte[] assetAsByteArray, String sum) throws Exception
    {
        HashMap<Path, String> ret = null;
        Connection conn = null;
        try
        {
            logger.debug("[PluginZippedAssetManagement::processAssets] Creating connection for asset type "
                    + getAssetType());
            conn = getAssetConnection();
            logger.debug("[PluginZippedAssetManagement::processAssets] Initializng plugin handler for asset type "
                    + getAssetType());
            init(conn, sum);
            logger.debug("[PluginZippedAssetManagement::processAssets] Processing plugin handler for asset type "
                    + getAssetType());
            ret = processAssets(assetAsByteArray, Paths.get(""), conn);
            logger.debug("[PluginZippedAssetManagement::processAssets] Commiting assets for asset type "
                    + getAssetType());
            commitAssets(conn);
        }
        catch (Exception ex)
        {
            logger.error("[PluginZippedAssetManagement::processAssets] Can't process assets", ex);
            throw ex;
        }
        finally
        {
            if (conn != null)
                try
                {
                    conn.close();
                }
                catch (Exception e2)
                {
                }
        }
        return ret;
    }

    /*
     * Extracts content of a zipped file passed as a byte array.
     * Extraction is "flat" in a sense that it does not unzip nested zip files.
     * 
     * Given input byte array of a.zip
     * Where the structure of a.zip is :
     * a.zip
     *  -->  aa.txt
     *  -->  b.zip
     *  -->  aa.txt
     *  -->  bb.exe
     *  
     *  --> es/ 
     *  	-->  aa.txt
     *  	-->  b.zip
     *       	-->  aa.txt
     *       	-->  bb.exe
     *       
     * The extracted content will be:
     * aa.txt =>  as byte array
     * b.zip  =>  as byte array
     * es/aa.txt =>  as byte array
     * es/b.zip =>  as byte array
     * 
     */
    private HashMap<Path, String> processAssets(byte[] assetAsByteArray, Path entryName, final Connection conn)
            throws Exception
    {
        HashMap<Path, String> ret = new HashMap<Path, String>();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(assetAsByteArray);
                ZipInputStream zipIn = new ZipInputStream(bis))
        {
            ZipEntry entry = null;
            try
            {
                entry = zipIn.getNextEntry();
            }
            catch (Exception exp)
            {
                logger.debug(exp);
            }

            if (entry != null)
            {
                while (entry != null)
                {
                    Path newEntryName = entryName.equals(Paths.get("")) ? Paths.get(entry.getName())
                            : entryName.resolve(entry.getName());
                    logger.debug("[PluginZippedAssetManagement::processAssets] Processing entry : " + newEntryName);
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream())
                    {
                        writeToStream((int) entry.getSize(), zipIn, out);

                        if (!entry.isDirectory())
                        {
                            Asset as = new Asset(newEntryName, out.toByteArray(), getAssetType());
                            logger.debug("[PluginZippedAssetManagement::processAssets] Saving Asset " + as);
                            saveAsset(as, conn);
                            ret.put(newEntryName, bytesToHex(MessageDigest.getInstance("SHA-256").digest(out
                                    .toByteArray())));
                        }
                        else
                        {
                            logger.debug("[PluginZippedAssetManagement::processAssets] " + newEntryName
                                    + " is a directory. Analyzing nested structure");
                            ret.putAll(processAssets(out.toByteArray(), newEntryName, conn));
                        }

                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
        }
        catch (Exception e)
        {
            logger.error("[PluginZippedAssetManagement::processAssets] Failed processing asset" +
                    (entryName != null && !entryName.toString().isEmpty() ? " " + entryName : ""), e);
            throw e;
        }
        return ret;
    }

    /*
     * Recursively extracts content of a zipped file passed as a byte array.
     * Returns of a HashMap of "Zipped Entry Name" => "Zipped Entry content as byte array"
     * 
     * Given input byte array of a.zip
     * Where the structure of a.zip is :
     * a.zip 
     *  -->  aa.txt
     *  -->  b.zip
     *       -->  aa.txt
     *       -->  bb.exe
     * 
     * The output will be:
     * aa.txt => content of aa.txt as byte array
     * b.zip/aa.txt => content of b.zip/aa.txt as byte array
     * b.zip/bb.exe => content of b.zip/bb.exe as byte array
     * 
     * Note: If the passed byte array is not a zip file content, the passed bytes will be returned as is
     *       with a hash key "UNKNOWN"
     */
    protected HashMap<Path, byte[]> getAssetContent(byte[] assetAsByteArray) throws Exception
    {
        return getAssetContent(assetAsByteArray, Paths.get(""));
    }

    private HashMap<Path, byte[]> getAssetContent(byte[] assetAsByteArray, Path entryName) throws Exception
    {
        HashMap<Path, byte[]> ret = new HashMap<Path, byte[]>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(assetAsByteArray);
                ZipInputStream zipIn = new ZipInputStream(bis))
        {
            ZipEntry entry = zipIn.getNextEntry();
            if (entry != null)
            {
                while (entry != null)
                {
                    Path newEntryName = entryName.equals(Paths.get("")) ? Paths.get(entry.getName())
                            : entryName.resolve(entry.getName());
                    logger.debug(
                            "[PluginZippedAssetManagement::getAssetContent] Read entry from zipped input byte stream: "
                                    + newEntryName);
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream())
                    {
                        writeToStream((int) entry.getSize(), zipIn, out);

                        // If the entry is a "leaf" node and can't be expanded/unzipped anymore ... get the bytes
                        if (!entry.isDirectory() && !isZippedEntry(newEntryName))
                        {
                            logger.debug("[PluginZippedAssetManagement::getAssetContent] " + newEntryName
                                    + " is a leaf node, getting its content");
                            ret.put(newEntryName, out.toByteArray());
                        }
                        // If the entry is a directory or a nested zip file ... extract it
                        else
                        {
                            logger.debug("[PluginZippedAssetManagement::getAssetContent] " + newEntryName
                                    + " is a not a leaf node, keep digging");
                            ret.putAll(getAssetContent(out.toByteArray(), newEntryName));
                        }

                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
            // If input byte array is not a zip entry ... return the incoming byte array
            else
            {
                logger.debug(
                        "[PluginZippedAssetManagement::getAssetContent] Input asset byte stream is not a zipped content");
                ret.put(Paths.get("UNKNOWN"), assetAsByteArray);
            }
        }
        catch (Exception e)
        {
            logger.error("[PluginZippedAssetManagement::getAssetContent] Failed getting zipped content", e);
            throw e;
        }

        return ret;
    }

    /*
     * Writes an asset (as a byte array) to targetFilePath+File.separatorChar+assetName
     */
    public void writeAssetContentToFile(byte[] asset, Path targetFilePath, Path assetName) throws Exception
    {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(asset);
                FileOutputStream out = new FileOutputStream(targetFilePath.resolve(assetName).toString()))
        {
            logger.debug("[PluginZippedAssetManagement::writeAssetContentToFile] Target File is : " + targetFilePath
                    .resolve(assetName));
            ZipInputStream zipIn = null;
            try
            {
                zipIn = new ZipInputStream(bis);
                ZipEntry entry = zipIn.getNextEntry();
                if (entry == null)
                {
                    logger.debug(
                            "[PluginZippedAssetManagement::writeAssetContentToFile] Asset is not a zip file content");
                    bis.reset();
                    writeToStream(-1, bis, out);
                }
                else
                {
                    try (ZipOutputStream zipOut = new ZipOutputStream(out))
                    {
                        logger.debug(
                                "[PluginZippedAssetManagement::writeAssetContentToFile] Asset is a zip file content");
                        while (entry != null)
                        {
                            logger.debug(
                                    "[PluginZippedAssetManagement::writeAssetContentToFile] Processing zip entry : "
                                            + entry.getName());
                            ZipEntry newEntry = entry;
                            zipOut.putNextEntry(newEntry);
                            writeToStream((int) entry.getSize(), zipIn, zipOut);
                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                    }
                }
            }
            finally
            {
                if (zipIn != null)
                    zipIn.close();
            }
        }
        catch (Exception e)
        {
            logger.error("[PluginZippedAssetManagement::writeAssetContentToFile] Failed writing asset to file :"
                    + targetFilePath + File.separatorChar + assetName, e);
            throw e;
        }
    }

    private void writeToStream(int maxBytesToRead, InputStream inputStream, OutputStream outputStream) throws Exception
    {
        int bytesRead = 0;
        int totalByesRead = 0;
        byte[] tempBuffer = new byte[1024];
        maxBytesToRead = (int) maxBytesToRead == -1 ? Integer.MAX_VALUE : (int) maxBytesToRead;
        while (totalByesRead <= maxBytesToRead && (bytesRead = inputStream.read(tempBuffer)) != -1)
        {
            outputStream.write(tempBuffer, 0, bytesRead);
            totalByesRead += bytesRead;
        }
    }

    private static String bytesToHex(byte[] hash)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++)
        {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /*
     * (non-Javadoc)
     * @see com.guardium.connector.common.PluginAssetManagementInterface#getRawAssetData(java.lang.String)
     * Returns the asset's raw content "as-is" (i.e. as it is read from the asset storage / DB)
     */
    public byte[] getRawAssetData(Path assetName) throws Exception
    {
        return (byte[]) getAsset(assetName).getAssetData();
    }

    protected abstract String getAssetType();

    protected abstract void saveAsset(Asset asset, Connection conn) throws Exception;

    protected abstract Connection getAssetConnection() throws Exception;

    protected abstract void commitAssets(Connection conn) throws Exception;

    protected abstract void init(Connection conn, String sum) throws Exception;

    protected abstract boolean isZippedEntry(Path entryName);

    public abstract Asset getAsset(Path assetName) throws Exception;
}
