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

import java.nio.file.Path;
import java.util.HashMap;

public interface PluginAssetManagementInterface
{
    public HashMap<Path, String> processAssets(byte[] source, String sum) throws Exception;

    public HashMap<Path, byte[]> getAssetContent(Path assetName) throws Exception;

    public HashMap<Path, byte[]> getAssetContentLike(Path assetName) throws Exception;

    public void writeAssetContentToFile(Path assetName, Path outputFilePath) throws Exception;

    public Asset getAsset(Path assetName) throws Exception;

    public byte[] getRawAssetData(Path assetName) throws Exception;

    public String getAssetSum() throws Exception;
}
