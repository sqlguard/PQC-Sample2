/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.guardium.assessment.i18n.Say;
import com.guardium.data.SqlbasedAssessmentDefinition.DataType;
import com.guardium.data.AvailableTest;
import com.guardium.map.AvailableTestMap;
import com.guardium.data.QueryConditionOperator.QueryOperator;


public class SqlbasedAssessmentDefinition {

	public static String OPERATOR_IN_GROUP  ="1";
	public static String OPERATOR_IS_NULL ="2";
	public static String OPERATOR_IS_NOT_NULL ="3";
	public static String OPERATOR_LESS  ="4";
	public static String OPERATOR_LESS_EQUAL ="5";
	public static String OPERATOR_GREATER ="6";
	public static String OPERATOR_GREATER_EQUAL  ="7";
	public static String OPERATOR_EQUAL ="8";
	public static String OPERATOR_NOTEQUAL  ="9";
	public static String OPERATOR_LIKE  ="10";
	public static String OPERATOR_REGEXP  ="11";
	public static String OPERATOR_NOT_IN_GROUP  ="12";
	public static String OPERATOR_NOT_REGEXP  ="21";

	/** Enumeration for the Return Type of the test*/
	public enum DataType {
		/** String data type */
		STRING("S"),
		/** Integer numeric data type */
		INT("I"),
		/** Date data type */
		DATE("D");

		private final String value;
		private DataType(String value) { this.value = value; }
		/**  @return The Return Type that would go on the database */
		public String getValue() { return this.value; }
		/**
		 * @param value The Return Type from the database.
		 * @return The enumerated type for the Return Type
		 */
		public static DataType findByValue(String value) {
			for ( DataType type : values() ) {
				if ( type.getValue().equalsIgnoreCase(value) ) { return type; }
			}
			return null;
		}
		
		public final int getSqlType() {
			if (this.value.equals("S")) return java.sql.Types.VARCHAR;
			if (this.value.equals("I")) return java.sql.Types.INTEGER;
			if (this.value.equals("D")) return java.sql.Types.DATE;
			return java.sql.Types.OTHER;
		}
	}

	
    /** the column name for the SQLBASED_ASSESSMENT_DEFINITION_ID field */
    public static final String SQLBASED_ASSESSMENT_DEFINITION_ID;
    /** the column name for the TEST_ID field */
    public static final String TEST_ID;
    /** the column name for the RECOMMENDATION_TEXT_PASS field */
    public static final String RECOMMENDATION_TEXT_PASS;
    /** the column name for the RECOMMENDATION_TEXT_FAIL field */
    public static final String RECOMMENDATION_TEXT_FAIL;
    /** the column name for the RESULT_TEXT_PASS field */
    public static final String RESULT_TEXT_PASS;
    /** the column name for the RESULT_TEXT_FAIL field */
    public static final String RESULT_TEXT_FAIL;
    /** the column name for the SQL_STMT field */
    public static final String SQL_STMT;
    /** the column name for the RETURN_TYPE field */
    public static final String RETURN_TYPE;
    /** the column name for the OPERATOR_ID field */
    public static final String OPERATOR_ID;
    /** the column name for the COMPARE_TO_VALUE field */
    public static final String COMPARE_TO_VALUE;
    /** the column name for the IS_CALLABLE_STATEMENT field */
    public static final String IS_CALLABLE_STATEMENT;
    /** the column name for the DETAIL_TEXT field */
    public static final String DETAIL_TEXT;
    /** the column name for the DETAIL_SQL field */
    public static final String DETAIL_SQL;
    /** the column name for the PRE_TEST_CHECK_SQL field */
    public static final String PRE_TEST_CHECK_SQL;
    /** the column name for the PRE_TEST_FAIL_MESSAGE field */
    public static final String PRE_TEST_FAIL_MESSAGE;
    /** the column name for the DB_LOOP_FLAG field */
    public static final String DB_LOOP_FLAG;
    /** the column name for the LOOP_DATABASES field */
    public static final String LOOP_DATABASES;

    static
    {
    SQLBASED_ASSESSMENT_DEFINITION_ID = "SQLBASED_ASSESSMENT_DEFINITION.SQLBASED_ASSESSMENT_DEFINITION_ID";
    TEST_ID = "SQLBASED_ASSESSMENT_DEFINITION.TEST_ID";
    RECOMMENDATION_TEXT_PASS = "SQLBASED_ASSESSMENT_DEFINITION.RECOMMENDATION_TEXT_PASS";
    RECOMMENDATION_TEXT_FAIL = "SQLBASED_ASSESSMENT_DEFINITION.RECOMMENDATION_TEXT_FAIL";
    RESULT_TEXT_PASS = "SQLBASED_ASSESSMENT_DEFINITION.RESULT_TEXT_PASS";
    RESULT_TEXT_FAIL = "SQLBASED_ASSESSMENT_DEFINITION.RESULT_TEXT_FAIL";
    SQL_STMT = "SQLBASED_ASSESSMENT_DEFINITION.SQL_STMT";
    RETURN_TYPE = "SQLBASED_ASSESSMENT_DEFINITION.RETURN_TYPE";
    OPERATOR_ID = "SQLBASED_ASSESSMENT_DEFINITION.OPERATOR_ID";
    COMPARE_TO_VALUE = "SQLBASED_ASSESSMENT_DEFINITION.COMPARE_TO_VALUE";
    IS_CALLABLE_STATEMENT = "SQLBASED_ASSESSMENT_DEFINITION.IS_CALLABLE_STATEMENT";
    DETAIL_TEXT = "SQLBASED_ASSESSMENT_DEFINITION.DETAIL_TEXT";
    DETAIL_SQL = "SQLBASED_ASSESSMENT_DEFINITION.DETAIL_SQL";
    PRE_TEST_CHECK_SQL = "SQLBASED_ASSESSMENT_DEFINITION.PRE_TEST_CHECK_SQL";
    PRE_TEST_FAIL_MESSAGE = "SQLBASED_ASSESSMENT_DEFINITION.PRE_TEST_FAIL_MESSAGE";
    DB_LOOP_FLAG = "SQLBASED_ASSESSMENT_DEFINITION.DB_LOOP_FLAG";
    LOOP_DATABASES = "SQLBASED_ASSESSMENT_DEFINITION.LOOP_DATABASES";
    }	

    // constructor
    public SqlbasedAssessmentDefinition () {
    	
    }
    
	public SqlbasedAssessmentDefinition (int sid,  int tid, String rcpass, String rcfail,
		       String rspass, String rsfail,
		       String sql, String returntype, int op_id, String compval, boolean call_flag,
		       String detail, String dsql, String pre_sql, String pre_msg, boolean loop_flag, String db) {
		
	    sqlbased_assessment_definition_id = sid;
	    test_id = tid;
	    recommendation_text_pass = rcpass;
	    recommendation_text_fail = rcfail;	          
	    result_text_pass = rspass;	          
	    result_text_fail = rsfail;	          
	    sql_stmt = sql;
	    return_type = returntype;	                                                                        
	    operator_id = op_id;
	    compare_to_value = compval;	                                                                                                        
	    is_callable_statement = call_flag;	          
	    detail_text = detail;
	    detail_sql = dsql;
	    pre_test_check_sql = pre_sql;
	    pre_test_fail_message = pre_msg;                                                                 
	    db_loop_flag = loop_flag;
	    loop_databases = db;
	}
	
    /*
mysql> desc SQLBASED_ASSESSMENT_DEFINITION;
+-----------------------------------+--------------+------+-----+---------+----------------+
| Field                             | Type         | Null | Key | Default | Extra          |
+-----------------------------------+--------------+------+-----+---------+----------------+
| SQLBASED_ASSESSMENT_DEFINITION_ID | int(11)      | NO   | PRI | NULL    | auto_increment |
| TEST_ID                           | int(11)      | NO   |     | 0       |                |
| RECOMMENDATION_TEXT_PASS          | mediumtext   | NO   |     | NULL    |                |
| RECOMMENDATION_TEXT_FAIL          | mediumtext   | NO   |     | NULL    |                |
| RESULT_TEXT_PASS                  | mediumtext   | NO   |     | NULL    |                |
| RESULT_TEXT_FAIL                  | mediumtext   | NO   |     | NULL    |                |
| SQL_STMT                          | mediumtext   | NO   |     | NULL    |                |
| RETURN_TYPE                       | varchar(1)   | NO   |     |         |                |
| OPERATOR_ID                       | int(11)      | NO   |     | 0       |                |
| COMPARE_TO_VALUE                  | varchar(255) | NO   |     |         |                |
| IS_CALLABLE_STATEMENT             | int(11)      | NO   |     | 0       |                |
| DETAIL_TEXT                       | varchar(255) | YES  |     | NULL    |                |
| DETAIL_SQL                        | mediumtext   | YES  |     | NULL    |                |
| PRE_TEST_CHECK_SQL                | mediumtext   | YES  |     | NULL    |                |
| PRE_TEST_FAIL_MESSAGE             | mediumtext   | YES  |     | NULL    |                |
| DB_LOOP_FLAG                      | int(11)      | NO   |     | 0       |                |
| LOOP_DATABASES                    | mediumtext   | YES  |     | NULL    |                |
| UPDATED_BY_V9X                    | int(11)      | NO   |     | 0       |                |
+-----------------------------------+--------------+------+-----+---------+----------------+
18 rows in set (0.00 sec)

mysql> 

   
     
     */
    private boolean modified;
    
    public boolean isModified() {
		return modified;
	}


	public void setModified(boolean modified) {
		this.modified = modified;
	}






	/**
     * The value for the sqlbased_assessment_definition_id field
     */
    private int sqlbased_assessment_definition_id;
          
    /**
     * The value for the test_id field
     */
    private int test_id;
          
    /**
     * The value for the recommendation_text_pass field
     */
    private String recommendation_text_pass;
          
    /**
     * The value for the recommendation_text_fail field
     */
    private String recommendation_text_fail;
          
    /**
     * The value for the result_text_pass field
     */
    private String result_text_pass;
          
    /**
     * The value for the result_text_fail field
     */
    private String result_text_fail;
          
    /**
     * The value for the sql_stmt field
     */
    private String sql_stmt;
                                                                                  
    /**
     * The value for the return_type field
     */
    private String return_type = "";
                                                                        
    /**
     * The value for the operator_id field
     */
    private int operator_id = 0;
                                                                                  
    /**
     * The value for the compare_to_value field
     */
    private String compare_to_value = "";
                                                                                                          
    /**
     * The value for the is_callable_statement field
     */
    private boolean is_callable_statement = false;
          
    /**
     * The value for the detail_text field
     */
    private String detail_text;
          
    /**
     * The value for the detail_sql field
     */
    private String detail_sql;
          
    /**
     * The value for the pre_test_check_sql field
     */
    private String pre_test_check_sql;
          
    /**
     * The value for the pre_test_fail_message field
     */
    private String pre_test_fail_message;
                                                                                                          
    /**
     * The value for the db_loop_flag field
     */
    private boolean db_loop_flag = false;
          
    /**
     * The value for the loop_databases field
     */
    private String loop_databases;
  
  
    /**
     * Get the SqlbasedAssessmentDefinitionId
     *
     * @return int
     */
    public int getSqlbasedAssessmentDefinitionId()
    {
        return sqlbased_assessment_definition_id;
    }

                                        
    /**
     * Set the value of SqlbasedAssessmentDefinitionId
     *
     * @param v new value
     */
    public void setSqlbasedAssessmentDefinitionId(int v) 
    {
    	if (this.sqlbased_assessment_definition_id != v)
    	{
    		this.sqlbased_assessment_definition_id = v;
    		setModified(true);
    	}
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

                                        
    /**
     * Set the value of TestId
     *
     * @param v new value
     */
    public void setTestId(int v) 
    {
    	if (this.test_id != v)
    	{
    		this.test_id = v;
    		setModified(true);
    	}
    }

    /**
     * Get the RecommendationTextPass
     *
     * @return String
     */
    public String getRecommendationTextPass()
    {
        return recommendation_text_pass;
    }

                                        
    /**
     * Set the value of RecommendationTextPass
     *
     * @param v new value
     */
    public void setRecommendationTextPass(String v) 
    {
    	if (!this.recommendation_text_pass.equals(v))
    	{
    		this.recommendation_text_pass = v;
    		setModified(true);
    	}          
    }

    /**
     * Get the RecommendationTextFail
     *
     * @return String
     */
    public String getRecommendationTextFail()
    {
        return recommendation_text_fail;
    }
                                        
    /**
     * Set the value of RecommendationTextFail
     *
     * @param v new value
     */
    public void setRecommendationTextFail(String v) 
    {
    	if (!this.recommendation_text_fail.equals(v))
    	{
    		this.recommendation_text_fail = v;
    		setModified(true);
    	}
    }

    /**
     * Get the ResultTextPass
     *
     * @return String
     */
    public String getResultTextPass()
    {
        return result_text_pass;
    }
                                        
    /**
     * Set the value of ResultTextPass
     *
     * @param v new value
     */
    public void setResultTextPass(String v) 
    {
    	if (!this.result_text_pass.equals(v))
    	{
    		this.result_text_pass = v;
    		setModified(true);
    	}
    }

    /**
     * Get the ResultTextFail
     *
     * @return String
     */
    public String getResultTextFail()
    {
        return result_text_fail;
    }
                                        
    /**
     * Set the value of ResultTextFail
     *
     * @param v new value
     */
    public void setResultTextFail(String v) 
    {
    	if (!this.result_text_fail.equals(v))
    	{
    		this.result_text_fail = v;
    		setModified(true);
    	}
    }

    /**
     * Get the SqlStmt
     *
     * @return String
     */
    public String getSqlStmt()
    {
        return sql_stmt;
    }
                                        
    /**
     * Set the value of SqlStmt
     *
     * @param v new value
     */
    public void setSqlStmt(String v) 
    {
    	if (!this.sql_stmt.equals(v))
    	{
    		this.sql_stmt = v;
    		setModified(true);
    	}
    }

    /**
     * Get the ReturnType
     *
     * @return String
     */
    public String getReturnType()
    {
        return return_type;
    }
                                        
    /**
     * Set the value of ReturnType
     *
     * @param v new value
     */
    public void setReturnType(String v) 
    {
    	if (!this.return_type.equals(v))
    	{
    		this.return_type = v;
    		setModified(true);
    	}
    }

    /**
     * Get the OperatorId
     *
     * @return int
     */
    public int getOperatorId()
    {
        return operator_id;
    }
                                        
    /**
     * Set the value of OperatorId
     *
     * @param v new value
     */
    public void setOperatorId(int v) 
    {
    	if (this.operator_id != v)
    	{
    		this.operator_id = v;
    		setModified(true);
    	}
    }

    /**
     * Get the CompareToValue
     *
     * @return String
     */
    public String getCompareToValue()
    {
        return compare_to_value;
    }
                                        
    /**
     * Set the value of CompareToValue
     *
     * @param v new value
     */
    public void setCompareToValue(String v) 
    {
    	if (!this.compare_to_value.equals(v))
    	{
    		this.compare_to_value = v;
    		setModified(true);
    	}
    }

    /**
     * Get the IsCallableStatement
     *
     * @return boolean
     */
    public boolean getIsCallableStatement()
    {
        return is_callable_statement;
    }
                                        
    /**
     * Set the value of IsCallableStatement
     *
     * @param v new value
     */
    public void setIsCallableStatement(boolean v) 
    {
    	//if (this.is_callable_statement != v)
    	//{
    		this.is_callable_statement = v;
    	//	setModified(true);
    	//}
    }

    /**
     * Get the DetailText
     *
     * @return String
     */
    public String getDetailText()
    {
        return detail_text;
    }
                                        
    /**
     * Set the value of DetailText
     *
     * @param v new value
     */
    public void setDetailText(String v) 
    {
    	if (!this.detail_text.equals(v))
    	{
    		this.detail_text = v;
    		setModified(true);
    	}
    }

    /**
     * Get the DetailSql
     *
     * @return String
     */
    public String getDetailSql()
    {
        return detail_sql;
    }
                                        
    /**
     * Set the value of DetailSql
     *
     * @param v new value
     */
    public void setDetailSql(String v) 
    {
    	if (!this.detail_sql.equals(v))
    	{
    		this.detail_sql = v;
    		setModified(true);
    	}
    }

    /**
     * Get the PreTestCheckSql
     *
     * @return String
     */
    public String getPreTestCheckSql()
    {
        return pre_test_check_sql;
    }
                                        
    /**
     * Set the value of PreTestCheckSql
     *
     * @param v new value
     */
    public void setPreTestCheckSql(String v) 
    {
    	if (!this.pre_test_check_sql.equals(v))
    	{
    		this.pre_test_check_sql = v;
    		setModified(true);
    	}
    }

    /**
     * Get the PreTestFailMessage
     *
     * @return String
     */
    public String getPreTestFailMessage()
    {
        return pre_test_fail_message;
    }
                                        
    /**
     * Set the value of PreTestFailMessage
     *
     * @param v new value
     */
    public void setPreTestFailMessage(String v) 
    {
    	if (!this.pre_test_fail_message.equals(v))
    	{
    		this.pre_test_fail_message = v;
    		setModified(true);
    	}
    }

    /**
     * Get the DbLoopFlag
     *
     * @return boolean
     */
    public boolean getDbLoopFlag()
    {
        return db_loop_flag;
    }
                                    
    /**
     * Set the value of DbLoopFlag
     *
     * @param v new value
     */
    public void setDbLoopFlag(boolean v) 
    {
    	if (this.db_loop_flag != v)
    	{
    		this.db_loop_flag = v;
    		setModified(true);
    	}
    }

    /**
     * Get the LoopDatabases
     *
     * @return String
     */
    public String getLoopDatabases()
    {
        return loop_databases;
    }
                                        
    /**
     * Set the value of LoopDatabases
     *
     * @param v new value
     */
    public void setLoopDatabases(String v) 
    {
    	if (!this.loop_databases.equals(v))
    	{
    		this.loop_databases = v;
    		setModified(true);
    	}
    }

    private static List fieldNames = null;

    /**
     * Generate a list of field names.
     *
     * @return a list of field names
     */
    public static synchronized List getFieldNames()
    {
    	if (fieldNames == null)
    	{
    		fieldNames = new ArrayList();
    		fieldNames.add("SqlbasedAssessmentDefinitionId");
    		fieldNames.add("TestId");
    		fieldNames.add("RecommendationTextPass");
    		fieldNames.add("RecommendationTextFail");
    		fieldNames.add("ResultTextPass");
    		fieldNames.add("ResultTextFail");
    		fieldNames.add("SqlStmt");
    		fieldNames.add("ReturnType");
    		fieldNames.add("OperatorId");
    		fieldNames.add("CompareToValue");
    		fieldNames.add("IsCallableStatement");
    		fieldNames.add("DetailText");
    		fieldNames.add("DetailSql");
    		fieldNames.add("PreTestCheckSql");
    		fieldNames.add("PreTestFailMessage");
    		fieldNames.add("DbLoopFlag");
    		fieldNames.add("LoopDatabases");
    		fieldNames = Collections.unmodifiableList(fieldNames);
    	}
    	return fieldNames;
    }

    /**
     * Retrieves a field from the object by name passed in as a String.
     *
     * @param name field name
     * @return value
     */
    public Object getByName(String name)
    {
        if (name.equals("SqlbasedAssessmentDefinitionId"))
        {
        	return new Integer(getSqlbasedAssessmentDefinitionId());
        }
        if (name.equals("TestId"))
        {
        	return new Integer(getTestId());
        }
        if (name.equals("RecommendationTextPass"))
        {
        	return getRecommendationTextPass();
        }
        if (name.equals("RecommendationTextFail"))
        {
        	return getRecommendationTextFail();
        }
        if (name.equals("ResultTextPass"))
        {
        	return getResultTextPass();
        }
        if (name.equals("ResultTextFail"))
        {
        	return getResultTextFail();
        }
        if (name.equals("SqlStmt"))
        {
        	return getSqlStmt();
        }
        if (name.equals("ReturnType"))
        {
        	return getReturnType();
        }
        if (name.equals("OperatorId"))
        {
        	return new Integer(getOperatorId());
        }
        if (name.equals("CompareToValue"))
        {
        	return getCompareToValue();
        }
        if (name.equals("IsCallableStatement"))
        {
        	return new Boolean(getIsCallableStatement());
        }
        if (name.equals("DetailText"))
        {
        	return getDetailText();
        }
        if (name.equals("DetailSql"))
        {	
        	return getDetailSql();
        }
        if (name.equals("PreTestCheckSql"))
        {
        	return getPreTestCheckSql();
        }
        if (name.equals("PreTestFailMessage"))
        {
        	return getPreTestFailMessage();
        }
        if (name.equals("DbLoopFlag"))
        {
        	return new Boolean(getDbLoopFlag());
        }
        if (name.equals("LoopDatabases"))
        {
        	return getLoopDatabases();
        }
        return getTableInfoByName(name);
    }
    
    /**
     * Retrieves a field from the object by name passed in
     * as a String.  The String must be one of the static
     * Strings defined in this Class' Peer.
     *
     * @param name peer name
     * @return value
     */
    public Object getByPeerName(String name)
    {
        if (name.equals(this.SQLBASED_ASSESSMENT_DEFINITION_ID))
        {
        	return new Integer(getSqlbasedAssessmentDefinitionId());
        }
        if (name.equals(this.TEST_ID))
        {
        	return new Integer(getTestId());
        }
        if (name.equals(this.RECOMMENDATION_TEXT_PASS))
        {
        	return getRecommendationTextPass();
        }
        if (name.equals(this.RECOMMENDATION_TEXT_FAIL))
        {
        	return getRecommendationTextFail();
        }
        if (name.equals(this.RESULT_TEXT_PASS))
        {
        	return getResultTextPass();
        }
        if (name.equals(this.RESULT_TEXT_FAIL))
        {
        	return getResultTextFail();
        }
        if (name.equals(this.SQL_STMT))
        {
        	return getSqlStmt();
        }
        if (name.equals(this.RETURN_TYPE))
        {
        	return getReturnType();
        }
        if (name.equals(this.OPERATOR_ID))
        {
        	return new Integer(getOperatorId());
        }
        if (name.equals(this.COMPARE_TO_VALUE))
        {
        	return getCompareToValue();
        }
        if (name.equals(this.IS_CALLABLE_STATEMENT))
        {
        	return new Boolean(getIsCallableStatement());
        }
        if (name.equals(this.DETAIL_TEXT))
        {
        	return getDetailText();
        }
        if (name.equals(this.DETAIL_SQL))
        {
        	return getDetailSql();
        }
        if (name.equals(this.PRE_TEST_CHECK_SQL))
        {
        	return getPreTestCheckSql();
        }
        if (name.equals(this.PRE_TEST_FAIL_MESSAGE))
        {
        	return getPreTestFailMessage();
        }
        if (name.equals(this.DB_LOOP_FLAG))
        {
        	return new Boolean(getDbLoopFlag());
        }
        if (name.equals(this.LOOP_DATABASES))
        {
        	return getLoopDatabases();
        }
        return null;
    }

    /**
     * Retrieves a field from the object by Position as specified
     * in the xml schema.  Zero-based.
     *
     * @param pos position in xml schema
     * @return value
     */
    public Object getByPosition(int pos)
    {
        if (pos == 0)
        {
        	return new Integer(getSqlbasedAssessmentDefinitionId());
        }
        if (pos == 1)
        {
        	return new Integer(getTestId());
        }
        if (pos == 2)
        {
        	return getRecommendationTextPass();
        }
        if (pos == 3)
        {
        	return getRecommendationTextFail();
        }
        if (pos == 4)
        {
        	return getResultTextPass();
        }
        if (pos == 5)
        {
        	return getResultTextFail();
        }
        if (pos == 6)
        {
        	return getSqlStmt();
        }
        if (pos == 7)
        {
        	return getReturnType();
        }
        if (pos == 8)
        {
        	return new Integer(getOperatorId());
        }
        if (pos == 9)
        {
        	return getCompareToValue();
        }
        if (pos == 10)
        {
        	return new Boolean(getIsCallableStatement());
        }
        if (pos == 11)
        {
        	return getDetailText();
        }
        if (pos == 12)
        {
        	return getDetailSql();
        }
        if (pos == 13)
        {
        	return getPreTestCheckSql();
        }
        if (pos == 14)
        {
        	return getPreTestFailMessage();
        }
        if (pos == 15)
        {
        	return new Boolean(getDbLoopFlag());
        }
        if (pos == 16)
        {
        	return getLoopDatabases();
        }
        return null;
    }

			    				   	 										       	 		 			        				   	 		 		    			 				        				   	 		 				    			 				        				   	 		 				    			 				        				   	 		 				    			 				        				   	 		 				    			 				        				   	 		 				    			 			        							 		 				    			 			        						 		 				    			 			        							 		 				    			 		   	        						 		 				    			 			        					   		 				    			 				        				   		 				    			 				        				   		 				    			 				        				   		 				    			 		   	        						 		 				    			 				        				   		 				    			    
    /**
     * Retrieves a String describing some piece of the table, 
     * based on the passed-in name
     *
     * @param name 
     * @return value or null
     */
    private Object getTableInfoByName(String name)
    {
    	if (name.equals("TABLETableName"))
    	{
    		return "SQLBASED_ASSESSMENT_DEFINITION";
    	}
    	if (name.equals("TABLETableDefinition"))
    	{
    		return "( SQLBASED_ASSESSMENT_DEFINITION_ID INTEGER NOT NULL AUTO_INCREMENT,  TEST_ID INTEGER NOT NULL,  RECOMMENDATION_TEXT_PASS MEDIUMTEXT NOT NULL,  RECOMMENDATION_TEXT_FAIL MEDIUMTEXT NOT NULL,  RESULT_TEXT_PASS MEDIUMTEXT NOT NULL,  RESULT_TEXT_FAIL MEDIUMTEXT NOT NULL,  SQL_STMT MEDIUMTEXT NOT NULL,  RETURN_TYPE VARCHAR(1) DEFAULT '' NOT NULL,  OPERATOR_ID INTEGER DEFAULT '0' NOT NULL,  COMPARE_TO_VALUE VARCHAR(255) DEFAULT '' NOT NULL,  IS_CALLABLE_STATEMENT INTEGER(1) DEFAULT '0' NOT NULL,  DETAIL_TEXT VARCHAR(255),  DETAIL_SQL MEDIUMTEXT,  PRE_TEST_CHECK_SQL MEDIUMTEXT,  PRE_TEST_FAIL_MESSAGE MEDIUMTEXT,  DB_LOOP_FLAG INTEGER(1) DEFAULT '0' NOT NULL,  LOOP_DATABASES MEDIUMTEXT, PRIMARY KEY(SQLBASED_ASSESSMENT_DEFINITION_ID))";
    	}
    	else if (name.equals("TABLETimestampColumn"))
    	{
    		return "";
    	}
    	else if (name.equals("TABLESelectStar"))
    	{
    		return " SQLBASED_ASSESSMENT_DEFINITION_ID,  TEST_ID,  RECOMMENDATION_TEXT_PASS,  RECOMMENDATION_TEXT_FAIL,  RESULT_TEXT_PASS,  RESULT_TEXT_FAIL,  SQL_STMT,  RETURN_TYPE,  OPERATOR_ID,  COMPARE_TO_VALUE,  IS_CALLABLE_STATEMENT,  DETAIL_TEXT,  DETAIL_SQL,  PRE_TEST_CHECK_SQL,  PRE_TEST_FAIL_MESSAGE,  DB_LOOP_FLAG,  LOOP_DATABASES";
    	}
    	else if (name.equals("TABLESelectFields"))
    	{
    		return " TEST_ID,  RECOMMENDATION_TEXT_PASS,  RECOMMENDATION_TEXT_FAIL,  RESULT_TEXT_PASS,  RESULT_TEXT_FAIL,  SQL_STMT,  RETURN_TYPE,  OPERATOR_ID,  COMPARE_TO_VALUE,  IS_CALLABLE_STATEMENT,  DETAIL_TEXT,  DETAIL_SQL,  PRE_TEST_CHECK_SQL,  PRE_TEST_FAIL_MESSAGE,  DB_LOOP_FLAG,  LOOP_DATABASES";
    	}
    	else if (name.equals("TABLEPrimaryKey"))
    	{
    		return "SQLBASED_ASSESSMENT_DEFINITION_ID";
    	}
    	return null;
    }
 
    /** flag to prevent endless save loop, if this object is referenced
    	by another object which falls in this transaction. */
    private boolean alreadyInSave = false;
           
    /**
     * Set the PrimaryKey using a String.
     *
     * @param key
     */
    public void setPrimaryKey(String key) 
    {
    	setSqlbasedAssessmentDefinitionId(Integer.parseInt(key));
    }


    /**
     * returns an id that differentiates this object from others
     * of its class.
     */

    /*
	public Integr getPrimaryKey()
	{
    	return SimpleKey.keyFor(getSqlbasedAssessmentDefinitionId());
	}
     */


    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     */
    public SqlbasedAssessmentDefinition copy() 
    {
    	return copyInto(new SqlbasedAssessmentDefinition());
    }
    
    protected SqlbasedAssessmentDefinition copyInto(SqlbasedAssessmentDefinition copyObj) 
    {
    	copyObj.setSqlbasedAssessmentDefinitionId(sqlbased_assessment_definition_id);
    	copyObj.setTestId(test_id);
    	copyObj.setRecommendationTextPass(recommendation_text_pass);
    	copyObj.setRecommendationTextFail(recommendation_text_fail);
    	copyObj.setResultTextPass(result_text_pass);
    	copyObj.setResultTextFail(result_text_fail);
    	copyObj.setSqlStmt(sql_stmt);
    	copyObj.setReturnType(return_type);
    	copyObj.setOperatorId(operator_id);
    	copyObj.setCompareToValue(compare_to_value);
    	copyObj.setIsCallableStatement(is_callable_statement);
    	copyObj.setDetailText(detail_text);
    	copyObj.setDetailSql(detail_sql);
    	copyObj.setPreTestCheckSql(pre_test_check_sql);
    	copyObj.setPreTestFailMessage(pre_test_fail_message);
    	copyObj.setDbLoopFlag(db_loop_flag);
    	copyObj.setLoopDatabases(loop_databases);
    	/*
    	copyObj.setNew(false);
    	copyObj.setNew(true);
    	 */
    	copyObj.setSqlbasedAssessmentDefinitionId(0);
    	return copyObj;
    }
    
    public void dump () {
    	
    	System.out.println("test id   " + test_id);
    	System.out.println("definiton id " + sqlbased_assessment_definition_id);
    	System.out.println("recomm pass " + recommendation_text_pass);
    	System.out.println("recomm fail " + recommendation_text_fail);
    	System.out.println("result text pass " + result_text_pass);
    	System.out.println("result text fail  " + result_text_fail);
    	System.out.println("sql stmt " + sql_stmt);                                                                                                      
    	System.out.println("return type " + return_type);
    	System.out.println("operator id   " + operator_id);
    	System.out.println("compare to value " + compare_to_value);
    	System.out.println("is callable       " + is_callable_statement);
    	System.out.println("detail text       " + detail_text);
    	System.out.println("detail sql        " + detail_sql);
    	System.out.println("pre test check    " + pre_test_check_sql);
    	System.out.println("pre test fail msg " + pre_test_fail_message);
    	System.out.println("db loop flag      " + db_loop_flag);
    	System.out.println("loop database     " + loop_databases);
 
    	return;
    }
	
	private AvailableTest aAvailableTest;

	/**
	 * Declares an association between this object and a AvailableTest object
	 *
	 * @param v AvailableTest
	 */
	public void setAvailableTest(AvailableTest v) {
		/*
		if (v == null) {
			this.setTestId(-1);
		} else {
			this.setTestId(v.getTestId());
		}
		*/
		
		this.aAvailableTest = v;
	}

	/**
	 * Get the associated AvailableTest object
	 *
	 * @return the associated AvailableTest object
	 * @throws TorqueException
	 */
	public AvailableTest getAvailableTest() {
		return this.aAvailableTest;
	}

//	/**
//	 * Provides convenient way to set a relationship based on a
//	 * ObjectKey.  e.g.
//	 * <code>bar.setFooKey(foo.getPrimaryKey())</code>
//	 *
//	 */
///*	public void setAvailableTestKey(ObjectKey key) throws TorqueException {
//
//		setTestId(((NumberKey) key).intValue());
//	}
//*/

	/**
	 * @return The Query Operator for this test.
	 */
	public QueryOperator getOperator() {
		int id = this.getOperatorId();
		return QueryOperator.findById(id);
	}

	/**
	 * @param operator The Query Operator to set.
	 */
	public void setOperator(QueryOperator operator) {
		int id = operator.getId();
		this.setOperatorId(id);
	}

	/**
	 * @return The Enumerated Data Type for the Return Value for this definition.
	 */
	public DataType getDataType() {
		String value = this.getReturnType();
		return DataType.findByValue(value);
	}

	/**
	 * @param dataType The Enumerated Data Type to set.
	 */
	public void setUpReturnType(DataType dataType) {
		String value = dataType.getValue();
		this.setReturnType(value);
	}

	@Override
	public String toString() {
		return
			"'" + this.getSqlStmt() + "'" + Say.SP + this.getOperator() + Say.SP + this.getCompareToValue()
		;
	}
    
}
