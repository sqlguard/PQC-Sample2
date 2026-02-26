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

import java.util.List;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.exceptions.DbException;

public interface SecureConnectorPluginTaskInterface
{
    void validate() throws Exception;

    int getTaskMonitorScanInterval();

    void execute() throws Exception;

    public SecureConnectorPluginStatus status() throws Exception;

    public void cancel() throws Exception;

    public void delete() throws Exception;

    public JsonObject getResults() throws Exception;

    public List<DBHandler> getDBInstallers() throws DbException;
    
    public List<DBHandler> getDBUpgraders(Version oldVersion, Version newVersion) throws DbException;
    
    public boolean hasTaskCompletedSuccessfully();
    
    public JsonObject getResultsErrors();
    
    public boolean canRun();
    
    public Properties getTaskMetadata();
}
