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
import java.util.Date;

public class Asset
{
    private Date timestamp = null;
    private Object assetData = null;
    private String assetType = null;
    private Path assetName = null;
    private int assetId = -1;

    public int getAssetId()
    {
        return assetId;
    }

    public void setAssetId(int assetId)
    {
        this.assetId = assetId;
    }

    public Asset(Path assetName, Object assetData, String assetType)
    {
        this.assetData = assetData;
        this.assetType = assetType;
        this.timestamp = new Date();
        this.assetName = assetName;
    }

    public Asset(Path assetName, Object assetData, String assetType, Date timestamp)
    {
        this.assetData = assetData;
        this.assetType = assetType;
        this.timestamp = timestamp;
        this.assetName = assetName;
    }

    public Path getAssetName()
    {
        return assetName;
    }

    public void setAssetName(Path assetName)
    {
        this.assetName = assetName;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public Object getAssetData()
    {
        return assetData;
    }

    public void setAssetData(Object assetData)
    {
        this.assetData = assetData;
    }

    public String getAssetType()
    {
        return assetType;
    }

    public void setAssetType(String assetType)
    {
        this.assetType = assetType;
    }

    public String toString()
    {
        return "assetId=" + assetId + "," + System.getProperty("line.separator") +
                "assetName=" + assetName + "," + System.getProperty("line.separator") +
                "assetType=" + assetType + "," + System.getProperty("line.separator") +
                "timestamp=" + timestamp + "," + System.getProperty("line.separator") +
                "assetData=" + ((byte[]) assetData).length + " bytes";
    }
}
