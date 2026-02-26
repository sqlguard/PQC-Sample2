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


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SecureConnectorPluginStatistics {
	/*
	   AllStats :
	   [classification] => [ 
			<[tables] => <SecureConnectorPluginStatisticsElement>, 
			<[errors] => <SecureConnectorPluginStatisticsElement> ...
			]
	   [va] => [ 
			<[tests] => <SecureConnectorPluginStatisticsElement>, 
			<[errors] => <SecureConnectorPluginStatisticsElement> ...
			]
	 */	
	private HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>> allStats = 
			new HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>>();
		
	/*
	 * Total (if statType is NULL) or single task type (if statType <> NULL) progress (%).
	 * For total progress : Calculated as the "average progress"  (Sum of all task type (e.g. classification,va) progress divide by the number of task types).
	 */
	public int getProgress(String statType) {
		int statTypeProgress = 0;
		int statTypesNum = 0;
		HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>>  allStatsValues = null;
		if(statType == null)
			allStatsValues = allStats;
		else {
			allStatsValues = new HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>> ();
			allStatsValues.put(statType, allStats.get(statType));
		}
		
			for(HashMap<String,SecureConnectorPluginStatisticsElement> typedStats:allStatsValues.values()) {
				for(String statName:typedStats.keySet()) {
					SecureConnectorPluginStatisticsElement statObj = typedStats.get(statName);
					if(statObj.getProgressPrecentage() == -1)
						continue;
					statTypeProgress+=statObj.getProgressPrecentage();
					statTypesNum++;
				}
			}
			
			return statTypesNum ==0 ? 0 : (int)((((float)statTypeProgress /statTypesNum )));
	}
	
	/*
	 * Total errors (if statType is NULL) or single task type (if statType <> NULL) progress (%).
	 */
	public int getErrors(String statType) {
		int errors = 0;

		HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>>  allStatsValues = null;
		if(statType == null)
			allStatsValues = allStats;
		else {
			allStatsValues = new HashMap<String,HashMap<String,SecureConnectorPluginStatisticsElement>> ();
			allStatsValues.put(statType, allStats.get(statType));
		}

		for(HashMap<String,SecureConnectorPluginStatisticsElement> typedStats:allStatsValues.values()) {
			if(typedStats.containsKey("errors")) {
				SecureConnectorPluginStatisticsElement statObj = typedStats.get("errors");
				errors+=statObj.getTotal();
			}
		}

		return errors;
	}
	
	/*
	 * Total progress (%) over all this SecureConnectorPluginStatistics object recorded statistics.
	 * Calculated as the "average progress" : Sum of all task type (e.g. classification,va) progress divide by the number of task types.
	 */
	public int getProgress() {
		return getProgress(null);
	}
	
	public int getErrors() {
		return getErrors(null);
	}

	public class SecureConnectorPluginStatisticsElement {
		private int progressPrecentage = -1;
		private String statName = null;
		private int total = 0;
		private int current = 0;
		private JsonObject statData = null;
		
		
		public int getProgressPrecentage() {
			return progressPrecentage;
		}


		public String getStatName() {
			return statName;
		}


		public int getTotal() {
			return total;
		}


		public int getCurrent() {
			return current;
		}


		public JsonObject getStatData() {
			return statData;
		}


		public SecureConnectorPluginStatisticsElement(String statName, boolean isProgressMetrics, int total,
				int current, JsonObject statData) throws Exception {
			if (statName == null)
				throw new Exception ("statName must be initialized");
			
			if(isProgressMetrics && (total < 0 || current < 0 || (current > total)))
					throw new Exception ("total or current : out of range. total = "+total+", current="+current);
			
			this.statName = statName;
			this.progressPrecentage = (isProgressMetrics ? (int)((((float)current/total))*100) : progressPrecentage);
			this.total = total;
			this.current = current;
			this.statData = statData;
		}

		
		public JsonObject toJson() {
			JsonObject jo1 = new JsonObject();
			jo1.addProperty("stat_name",statName);
			jo1.addProperty("completion_precentage",progressPrecentage);
			JsonObject jo1Inner = new JsonObject();
			jo1Inner.addProperty("total", total);
			jo1Inner.addProperty("current", current);
			jo1Inner.add("data", statData == null ? new JsonObject() : statData);
			jo1.add("stat_content", jo1Inner);
			return jo1;
		}
	}
	
	public List<String> getAllStatTypes() {
		return Arrays.asList((allStats.keySet().toArray(new String[allStats.size()])));
	}
	
	public SecureConnectorPluginStatistics() {
	}
	
	/*
	 * Converts task statistics (as a JsonArray) to a SecureConnectorPluginStatistics object.
	 * Assumes the following structure:
	   [
	 	{
		 "task_stats": [
		                {
		                	"stat_name": "some name",
		                	"completion_precentage": 100,
		                	"stat_content": {
		                	"total": 3,
		                	"current": 3,
		                	"data": {}
		                }
		                },
		                {
		                	"stat_name": "some name",
		                	"completion_precentage": -1,
		                	"stat_content": {
		                	"total": 0,
		                	"current": 0,
		                	"data": {}
		                }
		                }
		                ],
		 "task_type": "some name"
	 	}
	   ]
	 *
	 */ 
	public static SecureConnectorPluginStatistics CreateSecureConnectorPluginStatisticsObject(JsonArray statsObj) throws Exception {
		SecureConnectorPluginStatistics ret = new SecureConnectorPluginStatistics();
		for (JsonElement so : statsObj) {
		    JsonObject sjo = so.getAsJsonObject();
		    String taskType = sjo.get("task_type").getAsString();
		    JsonArray taskStats = sjo.get("task_stats").getAsJsonArray();
		    for (JsonElement st : taskStats) {
			    JsonObject sto = st.getAsJsonObject();
			    JsonObject stoStatContent = sto.getAsJsonObject("stat_content").getAsJsonObject();
			    ret.addStats(taskType,sto.get("stat_name").getAsString(),
			    		sto.get("completion_precentage").getAsInt() == -1 ? false : true,
			    				stoStatContent.get("total").getAsInt(),
			    				stoStatContent.get("current").getAsInt(),
			    				stoStatContent.get("data").getAsJsonObject());
		    }	    
		}
		return ret;
	}

	/*
	 * Adds a single statistic.
	 */
	public void addStats(String statType, String statName,boolean isProgressMetics,int total, int current, JsonObject statData) throws Exception {		
		HashMap<String,SecureConnectorPluginStatisticsElement> typedStats = null;
		if(allStats.containsKey(statType)) {
			typedStats= allStats.get(statType);
		} else {
			typedStats= new HashMap<String,SecureConnectorPluginStatisticsElement>();
		}
		typedStats.put(statName, new SecureConnectorPluginStatisticsElement(statName,isProgressMetics,total,current,statData));
		allStats.put(statType, typedStats);
	}
	
	/*
	 * Adds a single statistic.
	 */
	public void addStats(SecureConnectorPluginStatistics other) throws Exception {
		for(String taskType: other.allStats.keySet()) {
			HashMap<String,SecureConnectorPluginStatisticsElement> typedStats = other.allStats.get(taskType);
			for(String statName:typedStats.keySet()) {
				SecureConnectorPluginStatisticsElement statObj = typedStats.get(statName);
				this.addStats(
						taskType, 
						statName, 
						statObj.progressPrecentage == -1 ? false : true, 
								statObj.total, 
								statObj.current, 
								statObj.statData != null ? new JsonParser().parse(statObj.statData.toString()).getAsJsonObject() : null);
			}
		}
	}
	
	
	public JsonArray toJson() {
		JsonArray statsArrayObj = new JsonArray();
		
		for(String taskType: allStats.keySet()) {
			JsonArray ja = new JsonArray();
			HashMap<String,SecureConnectorPluginStatisticsElement> typedStats = allStats.get(taskType);
			for(String statName:typedStats.keySet()) {
				SecureConnectorPluginStatisticsElement statObj = typedStats.get(statName);
				ja.add(statObj.toJson());
			}
			JsonObject typedStatsJsonObj = new JsonObject();
			typedStatsJsonObj.add("task_stats", ja);
			typedStatsJsonObj.addProperty("task_type", taskType);
			statsArrayObj.add(typedStatsJsonObj);
			
		}

		return statsArrayObj;

	}
	
	public String toString() {
		return Utils.toPrettyFormat(toJson(), false);
	}
	
	public static void test() throws Exception {
		SecureConnectorPluginStatistics t = new SecureConnectorPluginStatistics();
		t.addStats("classification","tables", true, 115, 21, null);
		t.addStats("classification","errors", false, 2, 0, null);
		SecureConnectorPluginStatistics t1 = new SecureConnectorPluginStatistics();
		t.addStats("va","tests", true, 20, 15, null);
		t.addStats("va","errors", false, 4, 0, null);
		t.addStats(t1);
		SecureConnectorPluginStatistics t2 = new SecureConnectorPluginStatistics();
		t.addStats("classification1","tables", true, 116, 22, null);
		t.addStats("classification1","errors", false, 3, 0, null);
		t.addStats(t2);
		System.out.println(t);
		
		SecureConnectorPluginStatistics t3 = SecureConnectorPluginStatistics.CreateSecureConnectorPluginStatisticsObject(t.toJson());
		System.out.println(t);
		System.out.println(t.getProgress("classification"));
		System.out.println(t.getProgress());
		System.out.println(t.getErrors("classification"));
		System.out.println(t.getErrors());
		
	}
	
	public static void main(String [] args) throws Exception {
		test();
	}
	

}
