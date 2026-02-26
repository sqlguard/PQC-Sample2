/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.guardium.runtest.AbstractJdbcTest;
import com.guardium.assessment.i18n.Say;
import com.guardium.utils.ReplaceGroupsAndAliases;
import com.guardium.data.AssessmentTest;
import com.guardium.assessment.tests.Constants;
import com.guardium.assessment.tests.TestScore;
import com.guardium.assessment.tests.QueryBasedException;
import com.guardium.assessment.tests.GuardAssessmentException;
import com.guardium.data.AvailableTest;
import com.guardium.data.RecommendationText;
import com.guardium.data.SqlbasedAssessmentDefinition;
import com.guardium.map.SqlbasedAssessmentDefinitionMap;
import com.guardium.data.SqlbasedAssessmentDefinition.DataType;
import com.guardium.map.RecommendationTextMap;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceEnum;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
import com.guardium.data.GroupMember;
import com.guardium.data.QueryConditionOperator.QueryOperator;
import com.guardium.utils.RepDateUtils;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
//import com.guardium.utils.Dater;
import com.guardium.utils.JdbcUtils;
import com.guardium.utils.Regexer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 *
 * @author dtoland on Nov 6, 2007 at 5:14:10 PM
 */
public class QueryBasedTest extends AbstractJdbcTest {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(QueryBasedTest.class);

	SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionPeer = SqlbasedAssessmentDefinitionMap.getSqlbasedAssessmentDefinitionMapObject();
	GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	
	/** Symbol for the query result value to be replaced in the result text */
	public static final String QUERY_RESULT_SYMBOL = "${query_result}";

	/** Defines the test's sql characteristics */
	private SqlbasedAssessmentDefinition sqlDefinition = null;

	/** Define List of Databases to Loop, databases accessed and databases skipped */
	List<String> databasesToLoop = null;
	List<String> databasesAccessed = null;
	List<String> databasesSkipped = null;

	/** Define whether should loop through the list of databases */
	boolean loopDB = false;
	
	/** save the origina DB to use in the calculateDetail **/
	String originalDB = "";
	
	/**
	 * @return The Sql Definition for this test.
	 */
	protected SqlbasedAssessmentDefinition getSqlDefinition() {
		if (this.sqlDefinition == null ) {
			int testId = this.getAvailableTest().getTestId();
			this.sqlDefinition = SqlbasedAssessmentDefinitionPeer.findByTestId(testId);
		}
		return this.sqlDefinition;
	}

	/**
	 * @throws SQLException
	 * @see com.guardium.assessment.AbstractJdbcTest#calculateScore(Connection con)
	 */
	@Override
	public TestScore calculateScore(Connection con) throws SQLException {
		return calculateScore(con, null);
	}

	public TestScore calculateScore(MongoClient mcon) throws SQLException {
		return calculateScore(null, mcon);
	}
	
	public TestScore calculateScore(Connection con, MongoClient mcon) throws SQLException {
		TestScore score;

		Statement stmt = null;
		ResultSet rs = null;
		Object queryResult = null;
		Datasource ds = null;
		
		if (this.getAvailableTest().getApplicableFromVersion() > 0 ) {
			try {
				double fromV = this.getAvailableTest().getApplicableFromVersion();
				double toV = this.getAvailableTest().getApplicableToVersion();
				
				ds = this.getDatasource();
				int dbMajorVersion = ds.getMajorVersion();
				int dbMinorVersion = ds.getMinorVersion();
				String ver = (new Integer(dbMajorVersion)).toString() + "." + (new Integer(dbMinorVersion)).toString();
				double dbVersion = new Double(ver);
				if ( (fromV > 0.0 && dbVersion < fromV) || (toV > 0.0 && dbVersion > toV) ) {
					this.setResultText(Say.what(Say.ASMT_MSG_DS_VERSION_UNSUPPORTED,Say.ASMT_SUB_VERSION, (new Float(dbVersion)).toString() ));
					return TestScore.UNSUPPORTED_DB_VERSION;
				}
			}
			catch (Exception e) {
				AdHocLogger.logException(e);
				this.setResultText(Say.what(Say.ASMT_ERROR_CHECK_VERSION));
				return TestScore.ERROR;
			}
		}
		// Pre Test Check
		boolean pretest = false;
		try {
			String preTestSql = this.getSqlDefinition().getPreTestCheckSql();
			if (preTestSql != null && !preTestSql.trim().equals("")) {
				//Must run pre-test check
				pretest = performPreTestCheck(preTestSql, con);
			}
			else pretest = true; // No Pre-test check then pre test = true
		}
		catch (Exception ex1) {
			pretest = false;
			score = TestScore.ERROR;
			this.setResultText(Say.what(Say.ASMT_ERROR_PRE_TEST_ERROR));
			return score;
		}
		if (!pretest) {
			score = TestScore.PRE_TEST_CHECK_FAILED;
			this.setResultText(this.getSqlDefinition().getPreTestFailMessage());
			return score;	
		}
		// End Of Pre Test check
		
		String sql = this.getSqlDefinition().getSqlStmt();
		sql = sql.replaceAll("\r"," ").replaceAll("\n"," ");
		if (sql.indexOf(ReplaceGroupsAndAliases.Prefix)>=0) {
			try {
				sql = ReplaceGroupsAndAliases.replaceGroupsAndAliases(sql);
			}
			catch (Exception e) {
				AdHocLogger.logException(e);
			}
		}
		try {
			sql = replaceThresholdValue(sql, this.getAvailableTest());
		}
		catch (Exception e) {
			AdHocLogger.logException(e);
		}
		
		// Check whether should Loop databases and determine the list of databases to loop through.
		if (	this.getSqlDefinition().getDbLoopFlag() && 
				this.getSqlDefinition().getLoopDatabases() != null &&
				!this.getSqlDefinition().getLoopDatabases().trim().equals("") &&
				this.getSqlDefinition().getDataType() == DataType.INT
				) {
			// If loop Flag true and databases list not empty and if return value is INT then set loop to true.
			loopDB = true;
		}

		// Get the list of databases to loop.
		databasesToLoop   = new ArrayList<String>();
		databasesSkipped  = new ArrayList<String>();
		databasesAccessed = new ArrayList<String>();
			
		if (loopDB) {
			try {
				if (ds.isSqltype())
					databasesToLoop = getDataBasesToLoop(this.getSqlDefinition().getLoopDatabases(), con);
				else
					databasesToLoop = getDataBasesToLoop(this.getSqlDefinition().getLoopDatabases(), mcon);
			}
			catch (Exception e ) {
				score = TestScore.ERROR;
				this.setResultText( e.getLocalizedMessage() );
				this.logError( e.getLocalizedMessage(), sql);
				return score;
			}
		}
		if (loopDB && databasesToLoop.size() == 0) {
			score = TestScore.ERROR;
			this.setResultText(Say.what(Say.ASMT_ERROR_NO_DBS_TO_LOOP));
			this.logError( "No databases to Loop retrieved while loop flag set to true" );
			return score;
		}
		// If list of databases is Empty then No loop
		if (databasesToLoop.size() == 0) loopDB = false;
		
		// If Looping then define whether databases that can not be accessed should be skipped or should Error
		boolean skipDbOnError = false;
		if (loopDB)
			skipDbOnError = assessmentTest.getSkipErrorDbInLoop() ;
			
		QueryOperator operator = this.getSqlDefinition().getOperator();

		if (loopDB) 
			originalDB = con.getCatalog();
		
		// LOG.warn("originalDB " + originalDB);
		
		try {
			// get the sql result - differentiate between a case where we're looking for a result set versus output variable
			
			Long longResult = new Long(0); // Result to accumulate if database Loop
			if (!loopDB || databasesToLoop.size()==0)	// If no databases or Not Loop then list of databases should contain
														// Only the original database. 
				databasesToLoop.add(originalDB);

			for (String db : databasesToLoop) { // If No DBLooping The Loop only on Original DB
				boolean dbOK = false;
				if (loopDB) {
					try {
						con = setDataBase(con, db);
						dbOK = true;
					}
					catch (Exception eDbAccess) {
						if (skipDbOnError) {
							this.logError( eDbAccess.getLocalizedMessage(), "Unable To Access DB: " + db + " Database Skipped.");
							databasesSkipped.add(db);
						}
						else {
							throw eDbAccess;
						}
					}
				}
				if (loopDB && !dbOK) // Skip If Need To Loop but did not gain access to the specific database
					continue;
				else { // Did gain access to the database (Or no need to Loop)
					databasesAccessed.add(db.trim());
					
					
					if (this.getSqlDefinition().getIsCallableStatement()) {
						CallableStatement cstmt = con.prepareCall(sql);
						stmt = cstmt; // so that it will be released
						DataType dataType = this.getSqlDefinition().getDataType();
						cstmt.registerOutParameter(1,dataType.getSqlType());
						cstmt.execute();
						queryResult = cstmt.getObject(1);
					} else {
						stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						rs = stmt.executeQuery(sql);
						if ( rs.next() ) {
							queryResult = rs.getObject(1);
						}
						else {
							score = TestScore.ERROR;
							this.setResultText(Say.what(Say.ASMT_ERROR_SQL_NO_RECS));
							return score;
						}
					}
					if (loopDB) // If looping accumulate the query results into longResult 
						longResult = longResult + getResultLong(queryResult);
				}
			}
			// After Loop:  1 - Set database back to the original database (for next test on this connection)
			//				2 - Set QueryResult (Object) to be the accumulated longResult if real loop 
			if (loopDB) {
				con = setDataBase(con, originalDB);
				queryResult = longResult;
			}
							
			// evaluate the comparison
			score = this.evaluateResult(queryResult, operator);
			this.setResultText(score, queryResult);
			//Setup the pass result text, will be used if all list of details is in the exception
			// group to reset the result to pass.
			setPassResultText(this.getSqlDefinition().getResultTextPass());
			if (loopDB) { 
				String evaluatedDbs = "";
				if (databasesAccessed.size() == 0)
					evaluatedDbs = "none";
				else
					for (String d : databasesAccessed)
					{
						if (!evaluatedDbs.equals(""))
							evaluatedDbs = evaluatedDbs + ", ";
						evaluatedDbs = evaluatedDbs + d;
					}
				this.setResultText( this.getResultText() + "   [ Databases Evaluated: " + evaluatedDbs + ".] ");
				String skippedDbs = "";
				if (databasesSkipped.size() == 0)
					skippedDbs = "none";
				else
					for (String d : databasesSkipped)
					{
						if (!skippedDbs.equals(""))
							skippedDbs = skippedDbs + ", ";
						skippedDbs = skippedDbs + d;
					}
				this.setResultText( this.getResultText() + "   [ Databases Skipped: " + skippedDbs + ".]");
			}

		} catch (Exception e) {
			// The evaluation threw an exception
			score = TestScore.ERROR;
			this.setResultText( e.getLocalizedMessage() );
			this.logError( e.getLocalizedMessage(), sql);

		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
			if (loopDB) 
				try {
					con = setDataBase(con, originalDB);
				}
				catch (Exception e1) {}
		}
        /*
		if ( LOG.isInfoEnabled() ) {
			String msg =
				"Score: " + score + Say.SP + this.getResultText() + " '" + sql + "'"
				+ Say.NL + queryResult
				+ Say.SP + operator + Say.SP + this.getSqlDefinition().getCompareToValue()
			;
			LOG.info(msg);
		}
		*/
		// return the score
		return score;
	}

	/**
	 * @param testScore
	 * @return The recommendation text based on the score
	 */
	@Override
	public String getRecommendationText(TestScore testScore) {
		String recommend;
		switch (testScore) {
			case PASS:
				recommend = this.getSqlDefinition().getRecommendationTextPass();
				break;

			case FAIL:
				recommend = this.getSqlDefinition().getRecommendationTextFail();
				break;

			default:
				recommend = RecommendationTextPeer.findErrorRecommendation(testScore);
				break;
		}
		return recommend;
	}

	/**
	 * Will set the proper result text.
	 * If the text has already been set by an error condition, that text will be retained
	 * @param score The test score.
	 * @param result The object from the result of the query
	 * @return  The current result text.
	 */
	protected String setResultText(TestScore score, Object queryResult) {
		String resultText = null;

		// set the result text
		switch (score) {
			case PASS:
				resultText = this.getSqlDefinition().getResultTextPass();
				break;

			case FAIL:
				resultText = this.getSqlDefinition().getResultTextFail();
				break;

			default:
				// didn't pass or fail, should have some sort of error message, this is a backstop
				if ( Check.isEmpty( this.getResultText() ) ) {
					resultText = Say.what(Say.ASMT_GENERIC_ERROR);
					this.setResultText(resultText);
				}
				break;
		}

		if ( !Check.isEmpty(resultText) ) {
			// replace the symbolic, if any with the value of the result
			resultText = Regexer.replaceSymbolic(
					resultText, QUERY_RESULT_SYMBOL, String.valueOf(queryResult)
			);

			// add a line break if there is something already in there
			if ( !Check.isEmpty( this.getResultText() ) ) {
				this.appendResultText(Say.NL + Say.SP);
			}
			this.appendResultText(resultText);
		}

		// return the current result text
		return this.getResultText();
	}

	/**
	 * @param expression
	 * @return Pass if the expression is true, Fail if it is not.
	 */
	protected static TestScore passFail(boolean expression) {
		if (expression) {
			return TestScore.PASS;
		}
		return TestScore.FAIL;
	}

	/**
	 * @param result
	 * @param operator
	 * @return The score after evaluating the expression.
	 * @throws QueryBasedException
	 */
	protected TestScore evaluateResult(Object result, QueryOperator operator)
	throws QueryBasedException {
		TestScore score;
		String compareString = this.getSqlDefinition().getCompareToValue();
		DataType dataType = this.getSqlDefinition().getDataType();

		switch (operator) {
			case EQUAL:
				score = passFail( equal(result, compareString, dataType) );
				break;

			case GREATER_THAN:
				score = passFail( compare(result, compareString, dataType) > 0);
				break;

			case GREATER_THAN_OR_EQUAL:
				score = passFail( compare(result, compareString, dataType) >= 0);
				break;
			/* later
			case IN_GROUP:
				score = passFail( inGroup(result, compareString, dataType) );
				break;
			*/
			case IS_NOT_NULL:
				score = passFail(result != null);
				break;

			case IS_NULL:
				score = passFail(result == null);
				break;

			case LESS_THAN:
				score = passFail( compare(result, compareString, dataType) < 0);
				break;

			case LESS_THAN_OR_EQUAL:
				score = passFail( compare(result, compareString, dataType) <= 0);
				break;

			case LIKE:
				score = passFail( like(result, compareString, dataType) );
				break;

			case NOT_EQUAL:
				score = passFail( !equal(result, compareString, dataType) );
				break;
			/* later
			case NOT_IN_GROUP:
				score = passFail( !inGroup(result, compareString, dataType) );
				break;
			*/
			case NOT_LIKE:
				score = passFail( !like(result, compareString, dataType) );
				break;

			case NOT_REGEXP:
				score = passFail( !regex(result, compareString, dataType) );
				break;

			case REGEXP:
				score = passFail( regex(result, compareString, dataType) );
				break;

			default:
				String msg = Say.what(
						Say.ASMT_MSG_UNSUPPORTED_OPERATOR,
						Say.ASMT_SUB_VALUE, String.valueOf(operator)
				);
				throw new QueryBasedException(msg);
		}

		return score;
	}

	/**
	 * @param resultObject
	 * @param dataType
	 * @return The result object as a string
	 * @throws QueryBasedException
	 */
	protected static String normalizeResultObject(Object resultObject, DataType dataType)
	throws QueryBasedException {
		String result;

		// get a normalized string for the result value.
		switch (dataType) {
			case INT:
			case STRING:
				result = String.valueOf(resultObject);
				break;

			case DATE:
				Date date = getResultDate(resultObject);
				result = String.valueOf(date);
				break;

			default:
				throw new QueryBasedException("Unsupported Data Type: '" + dataType + "'.");
		}
		return result;
	}

	/**
	 * @param resultObject
	 * @return Casts the result object to a date.
	 * @throws QueryBasedException
	 */
	protected static Long getResultLong(Object resultObject)
	throws QueryBasedException {
		try {
			Long lng = new Long( String.valueOf(resultObject) );
			return lng;

		} catch (Throwable t) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_NUMBER_FORMAT,
					Say.ASMT_SUB_VALUE, String.valueOf(resultObject)
			);
			throw new QueryBasedException(msg, t);
		}
	}

	/**
	 * @param resultObject
	 * @return Casts the result object to a date.
	 * @throws QueryBasedException
	 */
	protected static Date getResultDate(Object resultObject) throws QueryBasedException {
		if (resultObject == null) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_PARSE_DATE,
					Say.ASMT_SUB_VALUE, String.valueOf(resultObject)
			);
			throw new QueryBasedException(msg);
		}

		try {
			Date date = (Date)resultObject;
			return date;

		} catch (Throwable t) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_PARSE_DATE,
					Say.ASMT_SUB_VALUE, String.valueOf(resultObject)
			);
			throw new QueryBasedException(msg, t);
		}
	}

	/**
	 * @param compareString
	 * @return Converts to a date honoring the SQLGuard abstract date conventions.
	 * @throws QueryBasedException
	 */
	protected static Date getCompareDate(String compareString)
	throws QueryBasedException {
		if ( Check.isEmpty(compareString) ) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_PARSE_DATE,
					Say.ASMT_SUB_VALUE, String.valueOf(compareString)
			);
			throw new QueryBasedException(msg);
		}

		try {
			Date date = RepDateUtils.getRealDate(compareString);
			return date;

		} catch (Throwable t) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_PARSE_DATE,
					Say.ASMT_SUB_VALUE, compareString
			);
			throw new QueryBasedException(msg, t);
		}
	}

	/**
	 * @param compareString
	 * @param dataType
	 * @return The compare string for the data type
	 * @throws QueryBasedException
	 */
	protected static String normalizeCompareString(String compareString, DataType dataType)
	throws QueryBasedException {
		String result;

		// get a normalized string for the result value.
		switch (dataType) {
			case INT:
			case STRING:
				result = compareString;
				break;

			case DATE:
				Date date = getCompareDate(compareString);
				result = String.valueOf(date);
				break;

			default:
				throw new QueryBasedException("Unsupported Data Type: '" + dataType + "'.");
		}
		return result;

	}

	/**
	 * @param resultObject
	 * @param compareString
	 * @param dataType
	 * @return Whether the result object matches the expression in the compare string.
	 * @throws QueryBasedException
	 */
	protected static boolean regex(Object resultObject, String compareString, DataType dataType)
	throws QueryBasedException {

		String searchValue = normalizeResultObject(resultObject, dataType);
		boolean result = Regexer.matchRegex(searchValue, compareString);
		return result;
	}

	/**
	 * @param resultObject
	 * @param compareString
	 * @param dataType
	 * @return Whether the result and the compare value are equal
	 * @throws QueryBasedException
	 */
	protected static boolean like(Object resultObject, String compareString, DataType dataType)
	throws QueryBasedException {

		// get the group member name for which we are searching.
		String searchValue = normalizeResultObject(resultObject, dataType);
		boolean result = Regexer.matchWildcards(searchValue, compareString);
		return result;
	}

	/**
	 * @param resultObject
	 * @param compareString
	 * @param dataType
	 * @return Whether the result and the compare value are equal
	 * @throws QueryBasedException
	 */
	
	// TODO - later, not sure se need this
    // static
	protected  boolean inGroup(Object resultObject, String compareString, DataType dataType)
	throws QueryBasedException {

		// get the group
		GroupDesc group;
		try {
			int groupId = Integer.parseInt(compareString);
			group = GroupDescPeer.retrieveByPK(groupId);

		} catch (Throwable t) {
			String msg = Say.what(
					Say.ASMT_MSG_ERROR_GROUP_NOT_FOUND,
					Say.ASMT_SUB_VALUE, compareString
			);
			throw new QueryBasedException(msg, t);
		}

		// get the group member name for which we are searching.
		String searchValue = normalizeResultObject(resultObject, dataType);

		// get the list of group members
		List<GroupMember> list = new ArrayList<GroupMember>();
		
		// TODO - later
		//list.addAll( group.getGroupMembers() );

		// loop through the group looking for the search value
		for ( GroupMember member : list ) {
			if ( searchValue.equalsIgnoreCase( member.getGroupMember() ) ) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * @param resultObject
	 * @param compareString
	 * @param dataType
	 * @return Whether the result and the compare value are equal
	 * @throws QueryBasedException
	 */
	protected static boolean equal(Object resultObject, String compareString, DataType dataType)
	throws QueryBasedException {
		boolean result;
		switch (dataType) {
			case INT:
				Long resultLong = getResultLong(resultObject);
				Long compareLong = getResultLong(compareString);
				result = resultLong.equals(compareLong);
				break;

			case STRING:
				result = String.valueOf(resultObject).equalsIgnoreCase(compareString);
				break;

			case DATE:
				Date resultDate = getResultDate(resultObject);
				Date compareDate = getCompareDate(compareString);
				result = resultDate.equals(compareDate);
				break;

			default:
				throw new QueryBasedException("Unsupported Data Type: '" + dataType + "'.");
		}
		return result;
	}

	/**
	 * Converts the objects and compares them
	 * @param resultObject
	 * @param compareString
	 * @param dataType
	 * @return the comparison between the objects
	 * @throws QueryBasedException
	 */
	protected static int compare(Object resultObject, String compareString, DataType dataType)
	throws QueryBasedException {
		int result;
		switch (dataType) {
			case INT:
				Long resultLong = getResultLong(resultObject);
				Long compareLong = getResultLong(compareString);
				result = resultLong.compareTo(compareLong);
				break;

			case STRING:
				result = String.valueOf(resultObject).toLowerCase().compareTo( compareString.toLowerCase() );
				break;

			case DATE:
				Date resultDate = getResultDate(resultObject);
				Date compareDate = getCompareDate(compareString);

				// TODO - later
				/*
				// account for granularity of dates and processing time
				if (
						"NOW".equalsIgnoreCase(compareString)
						&& Math.abs( Dater.secondsAfter(resultDate, compareDate) ) < 10L
				) {
					result = 0;
				} else {
					result = resultDate.compareTo(compareDate);
				}
				*/
				result = resultDate.compareTo(compareDate);
				break;

			default:
				throw new QueryBasedException("Unsupported Data Type: '" + dataType + "'.");
		}
		return result;
	}

	/**
	 * @param testScore
	 * @return The recommendation for this test based on the score
	 */
	@Override
	protected String findRecommendation(TestScore testScore) {
		String recommend;
		switch (testScore) {
			case PASS:
				recommend = this.getSqlDefinition().getRecommendationTextPass();
				break;

			case FAIL:
				recommend = this.getSqlDefinition().getRecommendationTextFail();
				break;

			default:
				recommend = RecommendationTextPeer.findErrorRecommendation(testScore);
				break;
		}
		return recommend;
	}
	
//		public String calculateDetail(Connection con) throws Exception
//	{
//			String ret = "";       
//			Statement stmt = null;
//            ResultSet rs = null;
//		
//			try {
//				String detailText = this.getSqlDefinition().getDetailText();
//				String detailSQL = this.getSqlDefinition().getDetailSql();
//				if (detailSQL == null || detailSQL.trim().equals("")) {
//					if (detailText != null && !detailText.trim().equals("")) {
//						ret = detailText;
//						return ret;
//					}
//					else {
//						return "N/A";
//					}
//				}
//				
//				// SQL DETAIL IS NOT EMPTY
//				detailSQL = detailSQL.replaceAll("\r"," ").replaceAll("\n"," "); 
//                if (detailSQL.indexOf("~~")>=0) {
//                     detailSQL = replaceGroupWithMembers(detailSQL);
//                }
// 
//                stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                rs = stmt.executeQuery(detailSQL);
//				if (detailText != null && !detailText.trim().equals("")) 
//					ret = detailText + " ";
//				
//				boolean inFirstRec= true;
//				String val = "";
//                
//				while ( rs.next() ) {
//                	val = rs.getString(1);
//                	if (inFirstRec)
//                		inFirstRec = false;
//                	else
//                		ret = ret.concat(", ");
//                	ret = ret.concat(val);
//                }
//			}
//			catch(Exception e ) {
//				AdHocLogger.logException(e);
//				ret = Say.what(Say.ASMT_SQLBASED_DETAIL_ERROR);
//			}
//			finally {
//				rs = Check.disposal(rs);
//				stmt = Check.disposal(stmt);
//			}
//			return ret;
//	}
	
	public List<String> calculateDetail(Connection con) throws Exception
	{
			List<String> ret = new ArrayList<String>();       
			Statement stmt = null;
            ResultSet rs = null;

			try {
				String detailSQL = this.getSqlDefinition().getDetailSql();
				if (detailSQL == null || detailSQL.trim().equals("")) {
					return ret;
				}
				
				// SQL DETAIL IS NOT EMPTY
				detailSQL = detailSQL.replaceAll("\r"," ").replaceAll("\n"," "); 
                if (detailSQL.indexOf(ReplaceGroupsAndAliases.Prefix)>=0) {
                     detailSQL = ReplaceGroupsAndAliases.replaceGroupsAndAliases(detailSQL);
                }
                detailSQL = replaceThresholdValue(detailSQL, this.getAvailableTest());


        		// LOG.warn("after get originalDB " + originalDB);

        		for (String db : databasesAccessed) {
    				if (loopDB)
    					con = setDataBase(con, db);
                	stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                	rs = stmt.executeQuery(detailSQL);
                	String val = "";
                	while ( rs.next() ) {
                		val = rs.getString(1);
                		if (!loopDB)
                			ret.add(val);
                		else
                			ret.add(db+ ": " + val);
                	}
                }
    			if (loopDB)
    				con = setDataBase(con, originalDB);
			}
			catch(Exception e ) {
				AdHocLogger.logException(e);
				ret.add(Say.what(Say.ASMT_SQLBASED_DETAIL_ERROR));
			}
			finally {
				rs = Check.disposal(rs);
				stmt = Check.disposal(stmt);
	   			if (loopDB)
	   				con = setDataBase(con, originalDB);
			}
			return ret;
	}



	
	public String getStringDetail() {
			String ret = "";
			try {
				String detailText = this.getSqlDefinition().getDetailText();
			
				if (getDetail() == null || getDetail().isEmpty()) {
					if (detailText != null && !detailText.trim().equals("")) {
						ret = detailText;
						return ret;
					}
					else {
						return "N/A";
					}
				}
				else {
					boolean inFirstRec= true;
					String val = "";
					if (detailText != null && !detailText.trim().equals("")) 
						ret = detailText + "\n";
					List<String> l = getDetail();
					for (int i=0; i< l.size(); i++) {
						val = l.get(i);
						if (inFirstRec)
							inFirstRec = false;
						else
							ret = ret.concat("\n");
						ret = ret.concat(val);
					}
				
				}
			}
			catch(Exception e) {
				AdHocLogger.logException(e);
				ret = Say.what(Say.ASMT_SQLBASED_DETAIL_ERROR);
			}
			
		    return ret;
			
		}		

	static public String replaceThresholdValue(String inStr, AvailableTest t) throws Exception {
		String ret = inStr;
		if (!t.getThresholdRequired()) {
			return ret.toString();
		}
		double thresholdValue = t.getDefaultThresholdValue();
		ret = ret.replaceAll("%THRESHOLD%", (new Double(thresholdValue)).toString());
		return ret.toString();
	}

	
	private boolean performPreTestCheck(String preTestSql, Connection con) throws Exception {
		boolean ret = false;
		preTestSql = preTestSql.replaceAll("\r"," ").replaceAll("\n"," ");
		if (preTestSql.indexOf(ReplaceGroupsAndAliases.Prefix)>=0) {
			try {
				preTestSql = ReplaceGroupsAndAliases.replaceGroupsAndAliases(preTestSql);
			}
			catch (Exception e) {
				AdHocLogger.logException(e);
			}
		}
		try {
			preTestSql = replaceThresholdValue(preTestSql, this.getAvailableTest());
		}
		catch (Exception e) {
			AdHocLogger.logException(e);
		}

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(preTestSql);
			if ( rs.next() ) {
				int preTestResult = rs.getInt(1);
				if (preTestResult == 1)
					ret = true;
			}
		}
		catch (Exception e) {
			AdHocLogger.logException(e);
			throw e;
		}
		finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
		}		
		return ret;
	}
	
	/* don't do MONGO
	private boolean performPreTestCheck(String preTestSql, MongoClient mcon) throws Exception {
		boolean ret = false;
		preTestSql = preTestSql.replaceAll("\r"," ").replaceAll("\n"," ");
		if (preTestSql.indexOf(ReplaceGroupsAndAliases.Prefix)>=0) {
			try {
				preTestSql = ReplaceGroupsAndAliases.replaceGroupsAndAliases(preTestSql);
			}
			catch (Exception e) {
				AdHocLogger.logException(e);
			}
		}
		try {
			preTestSql = replaceThresholdValue(preTestSql, assessmentTest);
		}
		catch (Exception e) {
			AdHocLogger.logException(e);
		}

		/*
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(preTestSql);
			if ( rs.next() ) {
				int preTestResult = rs.getInt(1);
				if (preTestResult == 1)
					ret = true;
			}
		}
		catch (Exception e) {
			AdHocLogger.logException(e);
			throw e;
		}
		finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
		}
		*/
	
		/*
		List <String> l = mcon.getDatabaseNames();
		String dbname = "test";
		DB db = mcon.getDB(dbname);
		
		String cname = "grades";
		DBCollection dbc = db.getCollection(cname);
		
		
		long ct = dbc.count();
		
		System.out.println("count is " + ct);
		
		
		
		return ret;
	}
	*/
	
	private List<String> getDataBasesToLoop(String dbs, Connection con) throws Exception{
		List<String> ret = new ArrayList<String>();
		dbs= dbs.trim();
		if (dbs.toLowerCase().startsWith("select")) {
			// Select Statement will retrieve the list of databases
			ret = JdbcUtils.executeSqlForList(dbs,con);
		}
		else {
			// List of databases contains in dbs (comma separated).
			StringTokenizer t = new StringTokenizer(dbs,",");
			while (t.hasMoreTokens())
				ret.add(t.nextToken().trim());
		}
		return ret;
		
	}
	
	private List<String> getDataBasesToLoop(String dbs, MongoClient mcon) throws Exception{
		List<String> ret = new ArrayList<String>();
		dbs= dbs.trim();
		
		
		if (dbs.toLowerCase().startsWith("select")) {
			// Select Statement will retrieve the list of databases
			//ret = JdbcUtils.executeSqlForList(dbs,mcon);
			// mongo execute  ???
			
		}
		else {
			// List of databases contains in dbs (comma separated).
			StringTokenizer t = new StringTokenizer(dbs,",");
			while (t.hasMoreTokens())
				ret.add(t.nextToken().trim());
		}
		return ret;
		
	}
	
	
	
	private Connection setDataBase(Connection con, String dataBase) throws Exception {
		Statement st = null;
		try {
			if (this.getDatasource().getDatasourceTypeId() == DatasourceEnum.INFORMIX.getDatasourceTypeId()) {
				// Attempt to Close transactions (if open) if no transaction active it willreturn an exception and we do nothing
				// If transaction active it will be rolled back to allow switching to next database.
				// this is necessary since some databases (depending on the DB type in the informix sysdatabases) will always have a transaction open
				// See Bug 33048
				try {
					con.rollback();
				}
				catch (Exception eTmp) {}
				st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				st.execute("database "+dataBase);
			}
			else if (this.getDatasource().getDatasourceTypeId() == DatasourceEnum.POSTGRESQL.getDatasourceTypeId() ||
					 this.getDatasource().getDatasourceTypeId() == DatasourceEnum.ASTER.getDatasourceTypeId() ||
					 this.getDatasource().getDatasourceTypeId() == DatasourceEnum.GREENPLUM.getDatasourceTypeId() ) {
				try {
					Datasource ds = getDatasource();
					con.close();
					con = null;
					ds.setDbName(dataBase);
					ds.setUrl(null);
					con = ds.getConnection();
				}
				catch( Exception e2) {
					throw e2;
				}
			}
			else
				con.setCatalog(dataBase);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (st != null)
					st.close();
			}
			catch (Exception e) {}
		}	
		return con;
	}
	/*
	private MongoClient setDataBase(MongoClient mcon, String dataBase) throws Exception {

		if (this.getDatasource().getDatasourceTypeId() == DatasourceEnum.MONGODB.getDatasourceTypeId() ) {
			try {
				Datasource ds = getDatasource();
				mcon.close();
				mcon = null;
				ds.setDbName(dataBase);
				ds.setUrl(null);
				mcon = ds.getMongoDBConnection();
			}

			catch (Exception e) {
				throw e;
			}
			finally {

			}
		}
		return mcon;
	}
	*/
}
