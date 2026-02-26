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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SecureConnectorPluginStatus
{

    public PluginStatus getStatusType()
    {
        return statusType;
    }

    public JsonObject getStatus()
    {
        return status;
    }

    public static enum PluginStatus
    {
        KILL_ME_NOW, CURRENT_STATE
    }

    private PluginStatus statusType = PluginStatus.CURRENT_STATE;
    private JsonObject status = null;
    private SecureConnectorPluginStatistics stats = null;
    private String overallTaskStatus = null;
    private String source = null;
    private String sourceAssetId = null;
    public String getSourceAssetId() {
		return sourceAssetId;
	}

	private int taskId = -1;

    public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setOverallTaskStatus(String overallTaskStatus) {
		this.overallTaskStatus = overallTaskStatus;
	}
    
	public String getOverallTaskStatus() {
		return this.overallTaskStatus;
	}

	public SecureConnectorPluginStatistics getStats() {
		return stats;
	}

	public void setStats(SecureConnectorPluginStatistics stats) {
		this.stats = stats;
	}
	
	public void setStatus(JsonObject status) {
		this.status = status;
	}
	
	/*
	 * Use this constructor to either populate an internal plugin status ... or ...
	 * To initialize the whole object with whatever we read from the DB
	 */
	public SecureConnectorPluginStatus(JsonObject status) throws Exception
    {
			if(status.has("task_id"))
				this.taskId = status.get("task_id").getAsInt();
			if(status.has("internal_task_status"))
				this.status = status.get("internal_task_status").getAsJsonObject();
			else
				this.status = status;
			if(status.has("overall_task_status"))
				overallTaskStatus = status.get("overall_task_status").getAsString();
			if(status.has("source"))
				source = status.get("source").getAsString();
			if(status.has("sourceAssetId"))
				sourceAssetId = status.get("sourceAssetId").getAsString();
			if(status.has("stats"))
				stats = SecureConnectorPluginStatistics.CreateSecureConnectorPluginStatisticsObject(status.get("stats").getAsJsonArray());
    }	

    public SecureConnectorPluginStatus(JsonObject status, PluginStatus statusType)
    {
        this.status = status;
        this.statusType = statusType;
    }
    
    public SecureConnectorPluginStatus(JsonObject status, PluginStatus statusType,SecureConnectorPluginStatistics stats)
    {
        this.status = status;
        this.statusType = statusType;
        this.stats = stats;
    }
        

    public JsonObject toJsonSummary()
    {
        JsonObject jo = new JsonObject();
       	jo.addProperty("overall_task_status", overallTaskStatus == null ? "" : overallTaskStatus);
       	jo.addProperty("source", source == null ? "" : source);
       	jo.addProperty("source_asset_id", sourceAssetId == null ? "" : sourceAssetId);
        if	(stats != null) {
        	jo.addProperty("overall_task_progress", stats.getProgress());
        	jo.addProperty("overall_task_errors", stats.getErrors());
        } else {
        	jo.addProperty("overall_task_progress", 0);
        	jo.addProperty("overall_task_errors", 0);
        }
        return jo;
    }
    
    public JsonObject toJson()
    {
        JsonObject jo = new JsonObject();
        jo.addProperty("task_id", taskId+"");
        jo.add("internal_task_status", status);  
       	jo.addProperty("overall_task_status", overallTaskStatus == null ? "" : overallTaskStatus);
       	jo.addProperty("source", source == null ? "" : source);
       	jo.addProperty("source_asset_id", sourceAssetId == null ? "" : sourceAssetId);
        if	(stats != null) {
        	jo.add("stats", stats.toJson());
        	jo.addProperty("overall_task_progress", stats.getProgress());
        	jo.addProperty("overall_task_errors", stats.getErrors());
        } else {
        	jo.add("stats", new JsonArray());
        	jo.addProperty("overall_task_progress", 0);
        	jo.addProperty("overall_task_errors", 0);
        }
        return jo;
    }

    public String toString()
    {
        return Utils.toPrettyFormat(toJson(), true);
    }

	public void setSourceAssetId(String assetId) {
		this.sourceAssetId = assetId;	
	}

}
