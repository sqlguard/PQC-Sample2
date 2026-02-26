/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

//import java.util.Date;
import java.util.List;

import com.guardium.data.AvailableTest;
import com.guardium.data.DatasourceType;
import com.guardium.map.AvailableTestMap;
//import com.guardium.utils.AdHocLogger;

public class AvailableTest {
	
	// constructor
	public AvailableTest () {
		
	}
	
	public AvailableTest (int id, String desc, int rid, String clsname, byte ttype,
			       int audit_id, int dtypeid, boolean thold, String tprompt, double dtvalue, String sver,
			       String cname, String ts, String sdesc, String extref, String os_name, boolean excp_flag,
			       double app_from_ver, double app_to_ver, String s_ref, String s_severity, String s_iacontrols, String s_srg) {
		
		test_id = id;
		test_desc = desc;
		report_id = rid;
		class_name = clsname;
		test_type = ttype;
	    audit_config_template_id = -1;
	    datasource_type_id = dtypeid;                                                                                                      
	    threshold_required = thold;
	    threshold_prompt = tprompt;
	    default_threshold_value = dtvalue;
	    severity = sver;
	    category_name = cname;
	    timestamp = ts;
	    short_description = sdesc;
	    external_reference = extref;
	    os = os_name;
	    can_have_exceptions_group = excp_flag;
	    applicable_from_version = app_from_ver;
	    applicable_to_version = app_to_ver;
	    stig_ref = s_ref;
		stig_severity = s_severity;
		stig_iacontrols = s_iacontrols;
		stig_srg = s_srg;	    
	    
	    // sqlbased_definition
	    //mysql> select * from AVAILABLE_TEST join SQLBASED_ASSESSMENT_DEFINITION where AVAILABLE_TEST.TEST_ID = 2001 and SQLBASED_ASSESSMENT_DEFINITION.TEST_ID = 2001;
	    
	}
	
    /**
     * The value for the test_id field
     */
    private int test_id;
          
    /**
     * The value for the test_desc field
     */
    private String test_desc;
                                                                        
    /**
     * The value for the report_id field
     */
    private int report_id = -1;
          
    /**
     * The value for the class_name field
     */
    private String class_name;
                                                                        
    /**
     * The value for the test_type field
     */
    private byte test_type = 0;
                                                                        
    /**
     * The value for the audit_config_template_id field
     */
    private long audit_config_template_id = -1;
                                                                        
    /**
     * The value for the datasource_type_id field
     */
    private int datasource_type_id = -1;
                                                                                                          
    /**
     * The value for the threshold_required field
     */
    private boolean threshold_required = false;
          
    /**
     * The value for the threshold_prompt field
     */
    private String threshold_prompt;
          
    /**
     * The value for the default_threshold_value field
     */
    private double default_threshold_value;
                                                                                  
    /**
     * The value for the severity field
     */
    private String severity = "INFO";
                                                                                  
    /**
     * The value for the category_name field
     */
    private String category_name = "";
          
    /**
     * The value for the timestamp field
     */
    //private Date timestamp;
    private String timestamp;
    
    /**
     * The value for the short_description field
     */
    private String short_description = "";
                                                                                  
    /**
     * The value for the external_reference field
     */
    private String external_reference = "";
          
    /**
     * The value for the os field
     */
    private String os;
                                                                                                          
    /**
     * The value for the can_have_exceptions_group field
     */
    private boolean can_have_exceptions_group = false;
          
    /**
     * The value for the applicable_from_version field
     */
    private double applicable_from_version;
          
    /**
     * The value for the applicable_to_version field
     */
    private double applicable_to_version;
  
    private String stig_ref;
	private String stig_severity;
    private String stig_iacontrols;
    private String stig_srg;
    
    public String getStig_ref() {
		return stig_ref;
	}

	public void setStig_ref(String stig_ref) {
		this.stig_ref = stig_ref;
	}

	public String getStig_severity() {
		return stig_severity;
	}

	public void setStig_severity(String stig_severity) {
		this.stig_severity = stig_severity;
	}

	public String getStig_iacontrols() {
		return stig_iacontrols;
	}

	public void setStig_iacontrols(String stig_iacontrols) {
		this.stig_iacontrols = stig_iacontrols;
	}

	public String getStig_srg() {
		return stig_srg;
	}

	public void setStig_srg(String stig_srg) {
		this.stig_srg = stig_srg;
	}

  
    private SqlbasedAssessmentDefinition sqldef;
    
	public SqlbasedAssessmentDefinition getSqlDefinition () {
		return sqldef;
	}
	
	public void setSqlDefinition (SqlbasedAssessmentDefinition sd) {
		sqldef = sd;
	}
    
    /**
     * Get the TestId
     *
     * @return int
     */
    public int getTestId()
    {
        return test_id;
    }

    public void setTestId(int v)
    {
    	test_id = v;
    }
	
    /**
     * Get the Test desc
     *
     * @return String
     */
    public String getTestDesc()
    {
        return test_desc;
    }

    public void setTestDesc(String v)
    {
    	test_desc = v;
    }
    
    public int getReportId () {
    	return report_id;
    }

    public void setReportId (int t) {
    	report_id = t;
    }    
    
    /**
     * The value for the class_name field
     */
    public String getClassName () {
    	return class_name;
    }

    public void setClassName (String t) {
    	class_name = t;
    }                            
    
    /**
     * The value for the test_type field
     */
    public byte getTestType () {
    	return test_type;
    }

    public void setTestType (byte t) {
    	test_type = t;
    }
    
    /**
     * The value for the audit_config_template_id field
     */
    public long getAuditConfigTemplateId() {
		return audit_config_template_id;
	}

	public void setAuditConfigTemplateId(long audit_config_template_id) {
		this.audit_config_template_id = audit_config_template_id;
	}
    
    /**
     * The value for the datasource_type_id field
     */                                                                                                   
    public int getDatasourceTypeId() {
		return datasource_type_id;
	}

	public void setDatasourceTypeId(int datasource_type_id) {
		this.datasource_type_id = datasource_type_id;
	}

	/**
     * The value for the threshold_required field
     */   
    public boolean isThresholdRequired() {
		return threshold_required;
	}

    public boolean getThresholdRequired() {
		return threshold_required;
	}
    
	public void setThresholdRequired(boolean threshold_required) {
		this.threshold_required = threshold_required;
	}

	/**
     * The value for the threshold_prompt field
     */   
    public String getThresholdPrompt() {
		return threshold_prompt;
	}

	public void setThresholdPrompt(String threshold_prompt) {
		this.threshold_prompt = threshold_prompt;
	}

	/**
     * The value for the default_threshold_value field
     */                                                                           
    public double getDefaultThresholdValue() {
		return default_threshold_value;
	}

	public void setDefaultThresholdValue(double default_threshold_value) {
		this.default_threshold_value = default_threshold_value;
	}

	/**
     * The value for the severity field
     */                                                                           
    public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
     * The value for the category_name field
     */
    public String getCategoryName() {
		return category_name;
	}

	public void setCategoryName(String category_name) {
		this.category_name = category_name;
	}

	/**
     * The value for the timestamp field
     */                                                                           
    public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
     * The value for the short_description field
     */                                                                           
    public String getShortDescription() {
		return short_description;
	}

	public void setShortDescription(String short_description) {
		this.short_description = short_description;
	}

	/**
     * The value for the external_reference field
     */   
    public String getExternalReference() {
		return external_reference;
	}

	public void setExternalReference(String external_reference) {
		this.external_reference = external_reference;
	}

	/**
     * The value for the os field
     */                                                                                                   
    public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	/**
     * The value for the can_have_exceptions_group field
     */   
    public boolean isCanHaveExceptionsGroup() {
		return can_have_exceptions_group;
	}

	public void setCanHaveExceptionsGroup(boolean can_have_exceptions_group) {
		this.can_have_exceptions_group = can_have_exceptions_group;
	}

	/**
     * The value for the applicable_from_version field
     */   
    public double getApplicableFromVersion() {
		return applicable_from_version;
	}

	public void setApplicableFromVersion(double version) {
		this.applicable_from_version = version;
	}

	/**
     * The value for the applicable_to_version field
     */
    public double getApplicableToVersion() {
		return applicable_to_version;
	}

	public void setApplicableToVersion(double version) {
		this.applicable_to_version = version;
	}
	
    
    // sqlbased definition
    /**
     * The value for the sqlbased_assessment_definition_id field
     */
    //private int sqlbased_assessment_definition_id;
          

	/**
     * The value for the recommendation_text_pass field
     */
    private String recommendationTextPass;
          
    /**
     * The value for the recommendation_text_fail field
     */
    private String recommendationTextFail;
          
    /**
     * The value for the result_text_pass field
     */
    private String resultTextPass;
          
    /**
     * The value for the result_text_fail field
     */
    private String resultTextFail;
          
    /**
     * The value for the sql_stmt field
     */
    private String sqlStmt;
                                                                                  
    /**
     * The value for the return_type field
     */
    private String returnType = "";
                                                                        
    /**
     * The value for the operator_id field
     */
    private int operatorId = 0;
                                                                                  
    /**
     * The value for the compare_to_value field
     */
    private String compareToValue = "";
                                                                                                          
    /**
     * The value for the is_callable_statement field
     */
    private boolean isCallableStatement = false;
          
    /**
     * The value for the detail_text field
     */
    private String detailText;
          
    /**
     * The value for the detail_sql field
     */
    private String detailSql;
          
    /**
     * The value for the pre_test_check_sql field
     */
    private String preTestCheckSql;
          
    /**
     * The value for the pre_test_fail_message field
     */
    private String preTestFailMessage;
                                                                                                          
    /**
     * The value for the db_loop_flag field
     */
    private boolean dbLoopFlag = false;
          
    /**
     * The value for the loop_databases field
     */
    private String loopDatabases;
  
    
	/** Stores the Datasource Type for this Available test */
	private DatasourceType aDatasourceType = null;

	public DatasourceType getDatasourceType() {
		return this.aDatasourceType;
	}

	public void setDatasourceType(DatasourceType datasourceType) {
		this.aDatasourceType = datasourceType;
	}

	public String getDatasourceTypeName() {
		String typeName = "";
		DatasourceType datasourceType = this.getDatasourceType();
		if (datasourceType != null) {
			typeName = datasourceType.getName();
		}
		return typeName;
	}
	
    public boolean isJdbcTest() {
    	if ((int)this.getTestType() == AvailableTestMap.QUERY_TEST_TYPE) {
    		return true;
    	}
    	else if ((int)this.getTestType() == AvailableTestMap.CVE_TEST_TYPE) {
    		return false;
    	}
    	return false;
    }
    
    public boolean isQueryBasedTest() {
    	if ((int)this.getTestType() == AvailableTestMap.QUERY_TEST_TYPE) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isCveTest() {
    	if ((int)this.getTestType() == AvailableTestMap.CVE_TEST_TYPE) {
    		return true;
    	}
    	return false;
    }
    
    private List <CveFix> cveFixRecords = null;
    
    public List <CveFix> getCveFixs () {
    	return cveFixRecords;
    }
   
    public void setCveFixs (List<CveFix> cv) {
    	cveFixRecords = cv;
    	return;
    }
    
    public void dump () {
    	System.out.println("test id   " + test_id);
    	System.out.println("test desc " + test_desc);
    	System.out.println("report id " + report_id);
    	System.out.println("clas name " + class_name);
    	System.out.println("test type " + test_type);
    	System.out.println("audit config id    " + audit_config_template_id);
    	System.out.println("datasource type id " + datasource_type_id);                                                                                                      
    	System.out.println("threshold required " + threshold_required);
    	System.out.println("threshold prompt   " + threshold_prompt);
    	System.out.println("default threshold  " + default_threshold_value);
    	System.out.println("severity     " + severity);
    	System.out.println("category     " + category_name);
    	System.out.println("timestamp    " + timestamp);
    	System.out.println("short desc   " + short_description);
    	System.out.println("externam ref " + external_reference);
    	System.out.println("os " + os);
    	System.out.println("have exception group " + can_have_exceptions_group);
    	System.out.println("applicable from " + applicable_from_version);
    	System.out.println("applcable to    " + applicable_to_version);
    	System.out.println("stig_ref    " + stig_ref);
    	System.out.println("stig_severity    " + stig_severity);
    	System.out.println("stig_iacontrols    " + stig_iacontrols);
    	System.out.println("stig_srg    " + stig_srg);
    	
    	return;
    }
}
