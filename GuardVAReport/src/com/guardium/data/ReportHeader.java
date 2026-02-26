/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.data;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;

//import java.util.Date;
//import com.guardium.data.AssessmentLogType;
//import com.guardium.map.AssessmentLogMap;
import com.guardium.map.ReportHeaderMap;

public  class ReportHeader

{

	/*
	mysql> desc REPORT_HEADER;
	+-------------------------------+--------------+------+-----+---------+----------------+
	| Field                         | Type         | Null | Key | Default | Extra          |
	+-------------------------------+--------------+------+-----+---------+----------------+
	| REPORT_ID                     | int(11)      | NO   | PRI | NULL    | auto_increment |
	| REPORT_TITLE                  | varchar(150) | NO   |     |         |                |
	| QUERY_ID                      | int(11)      | NO   |     | 0       |                |
	| USER_ID                       | int(11)      | NO   |     | 0       |                |
	| HTML_HEADER                   | mediumtext   | YES  |     | NULL    |                |
	| HTML_FOOTER                   | mediumtext   | YES  |     | NULL    |                |
	| MONITOR_FLAG                  | int(11)      | NO   |     | 0       |                |
	| REFRESH_RATE                  | int(11)      | NO   |     | 0       |                |
	| MAX_NUM_OF_POINTS             | int(11)      | YES  |     | NULL    |                |
	| CHART_TYPE                    | varchar(20)  | YES  |     | NULL    |                |
	| SHARED                        | int(11)      | YES  |     | NULL    |                |
	| MODIFY_FLAG                   | int(11)      | NO   |     | 0       |                |
	| VISIBLE_FLAG                  | int(11)      | NO   |     | 1       |                |
	| IS_DEFAULT_DISTRIBUTED_REPORT | int(11)      | NO   |     | 0       |                |
	+-------------------------------+--------------+------+-----+---------+----------------+
	14 rows in set (0.00 sec)
	*/
	
	public ReportHeader () {
		
	}
	
	public ReportHeader(int reportId, String reportTitle, int queryId,
			int userId, String htmlHeader, String htmlFooter,
			boolean monitorFlag, int refreshRate, int maxNumOfPoints,
			String chartType, int shared, boolean modifyFlag,
			boolean visibleFlag, boolean isDefaultDistributedReport) {
		super();
		this.reportId = reportId;
		this.reportTitle = reportTitle;
		this.queryId = queryId;
		this.userId = userId;
		this.htmlHeader = htmlHeader;
		this.htmlFooter = htmlFooter;
		this.monitorFlag = monitorFlag;
		this.refreshRate = refreshRate;
		this.maxNumOfPoints = maxNumOfPoints;
		this.chartType = chartType;
		this.shared = shared;
		this.modifyFlag = modifyFlag;
		this.visibleFlag = visibleFlag;
		this.isDefaultDistributedReport = isDefaultDistributedReport;
	}



	private int reportId;
	private String reportTitle;
	private int queryId;
	private int userId;
	private String htmlHeader;
	private String htmlFooter;
	private boolean monitorFlag;
	private int refreshRate;
	private int maxNumOfPoints;
	private String chartType;
	private int shared;
	private boolean modifyFlag;
	private boolean visibleFlag;
	private boolean isDefaultDistributedReport;
	
	
	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public int getQueryId() {
		return queryId;
	}
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getHtmlHeader() {
		return htmlHeader;
	}
	public void setHtmlHeader(String htmlHeader) {
		this.htmlHeader = htmlHeader;
	}
	public String getHtmlFooter() {
		return htmlFooter;
	}
	public void setHtmlFooter(String htmlFooter) {
		this.htmlFooter = htmlFooter;
	}
	public boolean isMonitorFlag() {
		return monitorFlag;
	}
	public void setMonitorFlag(boolean monitorFlag) {
		this.monitorFlag = monitorFlag;
	}
	public int getRefreshRate() {
		return refreshRate;
	}
	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}
	public int getMaxNumOfPoints() {
		return maxNumOfPoints;
	}
	public void setMaxNumOfPoints(int maxNumOfPoints) {
		this.maxNumOfPoints = maxNumOfPoints;
	}
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public int getShared() {
		return shared;
	}
	public void setShared(int shared) {
		this.shared = shared;
	}
	public boolean isModifyFlag() {
		return modifyFlag;
	}
	public void setModifyFlag(boolean modifyFlag) {
		this.modifyFlag = modifyFlag;
	}
	public boolean isVisibleFlag() {
		return visibleFlag;
	}
	public void setVisibleFlag(boolean visibleFlag) {
		this.visibleFlag = visibleFlag;
	}
	public boolean isDefaultDistributedReport() {
		return isDefaultDistributedReport;
	}
	public void setDefaultDistributedReport(boolean isDefaultDistributedReport) {
		this.isDefaultDistributedReport = isDefaultDistributedReport;
	}

	ReportHeaderMap ReportHeaderPeer = ReportHeaderMap.getReportHeaderMapObject();
	
	public void save () {
		ReportHeaderPeer.add(this);
		return;
	}
}
