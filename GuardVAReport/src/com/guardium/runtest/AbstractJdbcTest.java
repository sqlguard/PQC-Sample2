/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

//import org.apache.log4j.Logger;

import com.guardium.assessment.i18n.Say;
import com.guardium.assessment.tests.GuardAssessmentException;
import com.guardium.assessment.tests.TestScore;
//import com.guardium.data.AvailableTest;
//import com.guardium.data.RecommendationText;
//import com.guardium.map.RecommendationTextMap;

//import com.guardium.assessment.tests.TestScoreDefinition;
import com.guardium.data.Datasource;
import com.guardium.data.DataSourceConnectException;
import com.guardium.data.TestResult;

import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
//import com.guardium.utils.JdbcUtils;
import com.guardium.utils.Stringer;
import com.mongodb.MongoClient;

/**
 * Tests that the base version and the patch version of the database is up-to-date and supported.
 * Since the criteria for this test is highly dynamic for all of the DBMS vendors,
 * The valid values are stored in the AVAILABLE_TEST_EXPRESSION table as regex expressions.
 * This way the values can be easily updated in data without installing new code.
 * @author dtoland on Apr 16, 2007 at 11:58:03 AM
 */
public abstract class AbstractJdbcTest extends GenericTest {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(AbstractJdbcTest.class);
	private final static Logger LOG = Logger.getLogger(AbstractJdbcTest.class.getName());
	
	/** Constant name of the guardium database role */
	protected static final String ROLE_GDMMONITOR = "gdmmonitor";

	/** holds the temporary test result text */
	private StringBuilder resultText = new StringBuilder();

	/** convenience reference to the test result */
	private TestResult testResult = null;

	/** tracks whether each requested catalog has been tested */
	protected final Map<String, String> catalogTestStatus = new HashMap<String, String>();

	private final static String TESTED = "Tested";
	protected final static String NOT_TESTED = "Not Tested";

	private int majorVersion = 0;
	
	private boolean AfterSkipCatologEmptyFlag = false;

	private List<String> detail = null;
	
	private String forDetail = null;

	public String getForDetail() {
		return forDetail;
	}

	public void appendForDetail(String forDetail) {
		if(this.forDetail==null)
		this.forDetail = forDetail;
		else
			this.forDetail += forDetail;
	}

	public void setForDetail(String forDetail) {
		this.forDetail = forDetail;
	}

	public List<String> getDetail() {
		return detail;
	}

	public void setDetail(List<String> detail) {
		this.detail = detail;
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * Tests this Datasource.
	 * The result string is stored and can be retrieved with its accessor.
	 * @param connection A connection to use
	 * @return One of the constant SCORE values from TestScoreDefinitionPeer
	 * @throws SQLException
	 * @throws GuardAssessmentException
	 * @throws TorqueException
	 * @throws GuardAssessmentException 
	 */
	
	// Remove abstract so we can have two separate methods: one for jdbc connection and one for mongo connection.
	// So we don't need to change the existed code.
	// Each assessment need to implement one of these methods.
	// If this routine not defined, the error is EXECUTION_TEST_ROUTINE_CHECK_FAILED (-14).
	
	public TestScore calculateScore(Connection connection)
	throws SQLException, GuardAssessmentException, GuardAssessmentException {
		TestScore ts = TestScore.EXECUTION_TEST_ROUTINE_CHECK_FAILED;
		return ts;
	}

	public TestScore calculateScore(MongoClient mconnection)
	throws SQLException, GuardAssessmentException, GuardAssessmentException, DataSourceConnectException  {
		TestScore ts = TestScore.EXECUTION_TEST_ROUTINE_CHECK_FAILED;		
		return ts;		
	}

	
	/**
	 * None of the inherited arguments are used.
	 * @return One of the constant SCORE values from TestScoreDefinitionPeer
	 */
	@Override
	public final TestScore executeTest(
			String assessmentFromDate, String assessmentToDate,
			String filterClientIP, String filterServerIP,
			double timeFactor
	) {

		TestScore score;
		String result = null;

		try {
			Datasource ds = this.getDatasource();
			if (ds.isSqltype()) {
				// should only happen if there is a bug in the Runner
				if (this.connection == null) {
					String msg = Say.what("Connection.unavailable");
					throw new GuardAssessmentException(msg);
				}

				// run the test
				score = this.calculateScore(this.connection);
			}
			else {
				if (this.mconnection == null) {
					String msg = Say.what("Mongo Connection.unavailable");
					throw new GuardAssessmentException(msg);
				}

				// run the Mongo test
				score = this.calculateScore(this.mconnection);
			}
			result = this.getResultText();
			try {
				if( getDetail() == null && score == TestScore.FAIL) {
				    setDetail( fetchDetail() );
				}
				
			} catch (Exception e) {
				//AdHocLogger.logException(e);
				LOG.severe(e.getMessage());
				setStringDetail( Informer.causality(e) );
			}
			
		} catch (SQLException e) {
			//AdHocLogger.logException(e);
			// Can happen if connection user does not have access to parameter tables.  Use i18n.
			score = TestScore.ERROR;
			result = Say.what(
					Say.ASMT_MSG_CAT_ACC_FAIL,
					Say.ASMT_SUB_DS, String.valueOf( this.getDatasource() )
			);
			String dtl = Informer.thrownMessage(e);
			// LOG.error(result + Say.NL + dtl, e);
			this.logError(result, dtl);
			result += Say.NL + e.getLocalizedMessage();

		} catch (DataSourceConnectException e) {
			//AdHocLogger.logException(e);
			// Can happen if connection user does not have access to parameter tables.  Use i18n.
			score = TestScore.ERROR;
			result = Say.what(
					Say.ASMT_MSG_CAT_ACC_FAIL,
					Say.ASMT_SUB_DS, String.valueOf( this.getDatasource() )
			);
			String dtl = Informer.thrownMessage(e);
			// LOG.error(result + Say.NL + dtl, e);
			this.logError(result, dtl);
			result += Say.NL + e.getLocalizedMessage();

		} catch (GuardAssessmentException e) {
			/*
			AdHocLogger.setLog(true);
			AdHocLogger.setlogLevel(AdHocLogger.LOG_DEBUG);
			AdHocLogger.setLogAllExceptions(true);
			AdHocLogger.setTimeOut(60);
			AdHocLogger.logException(e);
			*/
			score = TestScore.ERROR;

			result = e.getLocalizedMessage();
			if ( Check.isEmpty(result) ) {
				result = Informer.thrownMessage(e);
			}

			String detail = "";
			if ( e.getCause() != null) {
				detail = Informer.thrownMessage( e.getCause() );
			}

			// LOG.error(result + Say.NL + detail, e);
			this.logError(result, detail);

		}
		// if there are catalogs involved, get details about access errors, if any
		String catalogErrors;
		if ( this.hasCatalogs() ) {
			catalogErrors = this.getCatalogErrors();		
			if ( !Check.isEmpty(catalogErrors) ) {
				result = result + Say.SP + Say.NL + catalogErrors;
			}
		}		
		
		// record the results and return the score
		String recommend = this.getRecommendationText(score);

		this.testResult = this.recordResult(score, result, recommend, this.getDatasource() );

		/*
		if ( LOG.isDebugEnabled() ) {
			LOG.debug(
					"[" + score + "] " + this.testResult.getResultText()
					+ Say.NL + "Detail: " + this.testResult.getDetail()
					+ Say.NL + "Recommend: " + recommend
			);
		}

		// recordResult above checks exeception groups and sets the score to pass if
		// all detail records are execptions. In this case must reset the score to pass 
		if (score == TestScore.FAIL && testResult.getTestScore() == TestScore.PASS)
			score = TestScore.PASS;
		
		*/
		
		return score;
	}

	/**
	 * @param testScore
	 * @return The recommendation text for the score.
	 */
	public String getRecommendationText(TestScore testScore) {
		String recommend = "good or bad";
		// need to do later
		/*
		AvailableTest availableTest = this.getAvailableTest();
	
		String recommend = RecommendationTextPeer.findRecommendation(availableTest, testScore);
		*/
		return recommend;
	}

	/**
	 * Get the test result
	 * @return The test result message.
	 */
	public String getResultText() {
		return this.resultText.toString();
	}
	
	/**
	 * @param value Appends to the test result
	 */
	protected void appendResultText(String value) {
		this.resultText.append(value);
	}
	
	/**
	 * Appends a list separator (", ") if the result is not empty.
	 * @param value The value to add after the list separator
	 */
	protected void appendResultTextListSep(String value) {
		Stringer.listSep(this.resultText).append(value);
	}
	
	/**
	 * @return A String with catalog by catalog access error detail.
	 */
	protected String getCatalogErrors() {
		String detail = null;

		// find if any catalogs were not able to be tested
		if ( this.hasCatalogs() ) {
			Map<String, String> notTested = new HashMap<String, String>();
			for ( String catalog : this.catalogTestStatus.keySet() ) {
				if ( !this.isCatalogTested( this.connection, catalog) ) {
					String msg = this.catalogTestStatus.get(catalog);
					notTested.put(catalog, msg);
				}
			}

			// if there are untested catalogs add them to the result text.
			if ( !Check.isEmpty(notTested) ) {
				detail = Stringer.mapToString(notTested, Say.LIST_SEP + Say.NL);
				// add detail to the result
				detail = Say.what(
						Say.ASMT_MSG_DB_NOT_TESTED,
						Say.ASMT_SUB_OBJ_LIST, detail
				);
			}
		}
		return detail;
	}

	/**
	 * @param resultText Replaces the existing test result
	 */
	protected void setResultText(String resultText) {
		this.resultText = new StringBuilder(resultText);
	}

	/**
	 * @return Whether the test result has anything in it.
	 */
	protected boolean hasResultText() {
		return this.resultText.length() != 0;
	}

	/**
	 * @param query
	 * @param con A connection to use.
	 * @return The object value from the first row, first column of the query's result.
	 * @throws SQLException
	 */
	protected Object executeSqlForSingleVal(Connection con, String query) throws SQLException{
		return this.executeSqlForSingleVal(con, query, 1);
	}

	/**
	 * @param query
	 * @param columnNumber
	 * @param con A connection to use.
	 * @return The object value from the first row and numbered column of the query's result
	 * @throws SQLException
	 */
	protected Object executeSqlForSingleVal(Connection con, String query, int columnNumber)
	throws SQLException{
		Object result = null;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				result = rs.getObject(columnNumber);
			}
			return result;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	/**
	 * @param query
	 * @param colName
	 * @param con
	 * @return The object int the first row of the named column
	 * @throws SQLException
	 */
	protected Object executeSqlForSingleVal(Connection con, String query, String colName)
	throws SQLException{

		Object value = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			rs = stmt.executeQuery(query);
			if ( rs.next() ) {
				value = rs.getObject(colName);
			}
			return value;

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	/**
	 * @param con A connection to use.
	 * @param query
	 * @return Whether at least one row was returned by the query.
	 * @throws SQLException
	 */
	protected boolean executeSqlForPositiveCount(Connection con, String query) throws SQLException{
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			return rs.next();

		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}
	}

	/**
	 * @param query
	 * @param con
	 * @return A number in the first row, first column of of the result set of the query.
	 * @throws SQLException
	 */
	protected Long executeSqlForNumber(Connection con, String query) throws SQLException{
		Object value = this.executeSqlForSingleVal(con, query);
		return this.getNumber(value);
	}

	/**
	 * @param con A connection to use.
	 * @param query
	 * @return A list of values from the first column of the result set.
	 * @throws SQLException
	 */
	protected List<String> executeSqlForList(Connection con, String query) throws SQLException {
		return this.executeSqlForList(con, query, 1);
	}

	/**
	 * @param con A connection to use.
	 * @param query
	 * @param columnNumber
	 * @return A list of values from a column of the result set.
	 * @throws SQLException
	 */
	protected List<String> executeSqlForList(Connection con, String query, int columnNumber)
	throws SQLException {

		List<String> result = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			while ( rs.next() ) {
				result.add( rs.getString(columnNumber) );
			}

			return result;

		} catch (SQLException e) {
			// LOG.error("Could not execute query: '" + query + "'. " + Informer.thrownMessage(e) );
			throw e;

		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
		}
	}

	/**
	 * @param query
	 * @param con A Connection to use.
	 * @param columnName
	 * @return A list of values from a column of the result set.
	 * @throws SQLException
	 */
	protected List<String> executeSqlForList(Connection con, String query, String columnName)
	throws SQLException {

		List<String> result = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			while ( rs.next() ) {
				result.add( rs.getString(columnName) );
			}
			return result;

		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
		}
	}

	/**
	 * @param value
	 * @return Converts an object's string value to a long.
	 */
	protected Long getNumber(Object value){
		Long result = null;
		if ( !Check.isEmpty(value) ) {
			try {
				result = Long.parseLong(value.toString().trim());
			} catch (NumberFormatException e) {
				String msg = "Could not convert: '" + value + "' to a number.";
				// LOG.warn(msg, e);
				//AdHocLogger.logException(e);
			}
		}
		return result;
	}


	/**
	 * Sets the test result text to a single i18n value
	 * @param value The key to the resource
	 */
	protected void sayTestResult(String value) {
		this.resultText = new StringBuilder(Say.what(value));
	}

	/**
	 * @param version
	 * @param threshold
	 * @return Whether the db version is at or above the threshold.
	 */
	protected static boolean isVersionOrAbove(String version, String threshold) {

		String[] versionElements = version.split("\\.");
		String[] thresholdElements = threshold.split("\\.");

		int ndx = 0;
		for ( String thresholdElement : thresholdElements ) {
			if (versionElements.length > ndx) {
				try {
					Integer threshValue = new Integer(thresholdElement);
					Integer versionValue = new Integer(versionElements[ndx++]);
					if (threshValue > versionValue) {
						return false;
					} else if (threshValue < versionValue) {
						return true;
					}
				} catch (NumberFormatException e) {
					// LOG.error("Could not interpret version: '" + version + "'.");
					return false;
				}

			} else {
				return false;
			}
		}
		// tie goes to the runner
		return true;
	}

	/**
	 * Specialized result recording for when the datasource cannot connect
	 * @param exception The exception that occurred when connecting.
	 * @return The populated and saved Test Result
	 */
	public TestResult recordConnectionFailureResult(Exception exception) {
		int reportResultId = -1;
		TestScore score = TestScore.ERROR;
		String recommend = findRecommendation(score);

		// create the message
		Datasource datasource = this.getDatasource();
		String usr = datasource!=null ? datasource.getUserName() : null;
		String result = Say.what(
				Say.ASMT_MSG_CONNECT_FAIL,
				Say.ASMT_SUB_DS, String.valueOf(datasource),
				Say.ASMT_SUB_USER, usr
		);
		String dtl = Informer.thrownMessage(exception);
		//if ( LOG.isInfoEnabled() ) { LOG.info(result + Say.NL + dtl); }
		this.logError(result, dtl);

		return this.recordResult(reportResultId, score, result, recommend, datasource);
	}

	/**
	 * @param con A connection to use.
	 * @return A list of databases for this datasource.
	 */
	protected Set<String> listCatalogs(Connection con) {

		if ( Check.isEmpty(this.catalogTestStatus) && ! this.AfterSkipCatologEmptyFlag) {
			ResultSet rs = null;
			try {
				DatabaseMetaData meta = con.getMetaData();
				rs = meta.getCatalogs();
				while( rs.next() ) {
					String name = rs.getString(1);
					if ( !Check.isEmpty(name) ) {
						this.catalogTestStatus.put(name.trim(), NOT_TESTED);
					}
				}

			} catch (SQLException e) {
				String msg = Say.what(
						Say.ASMT_MSG_CAT_ACC_FAIL,
						Say.ASMT_SUB_DS, String.valueOf( this.getDatasource() )
				);
				String dtl = Informer.thrownMessage(e);
				this.logError(msg, dtl);

			} finally {
				Check.disposal(rs);
			}
		}
		return this.catalogTestStatus.keySet();
	}

	/**
	 * @return Whether any databases have been tested.
	 */
	protected boolean hasCatalogs() {
		return !Check.isEmpty(this.catalogTestStatus);
	}

	/**
	 * @param con A connection to use.
	 * @param databaseName
	 * @return Whether the database has been tested.
	 */
	protected boolean isCatalogTested(Connection con, String databaseName) {
		if ( this.listCatalogs(con).contains(databaseName) ) {
			String message = this.catalogTestStatus.get(databaseName);
			return TESTED.equals(message);
		}
		return false;
	}

	/**
	 * @return Whether any databases have been tested.
	 */
	protected boolean isAnyCatalogTested() {
		return this.catalogTestStatus.values().contains(TESTED);
	}

	/**
	 * @return Whether any databases have been tested.
	 */
	protected boolean isEveryCatalogTested() {
		for ( String value : this.catalogTestStatus.values() ) {
			if ( !TESTED.equals(value) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param con A connection to use.
	 * @param databaseName The name of the database.
	 * @param status Whether the database has been tested.
	 * @return Whether the database was found in the list and had its status updated.
	 */
	protected String setCatalogTestStatus(Connection con, String databaseName, String status) {
		if ( this.listCatalogs(con).contains(databaseName) ) {
			return this.catalogTestStatus.put(databaseName, status);
		}
		return null;
	}

	/**
	 * @param con A connection to use.
	 * @param catalogName
	 * @return Whether the database was found in the list and had its status updated.
	 */
	protected String markCatalogTested(Connection con, String catalogName) {
		return this.setCatalogTestStatus(con, catalogName, TESTED);
	}

	/**
	 * @param con A connection to use.
	 * @param catalogName
	 * @param throwable The reason the catalog is not being tested.
	 * @return Whether the database was found in the list and had its status updated.
	 */
	protected String markCatalogNotTested(Connection con, String catalogName, Throwable throwable) {
		String status = throwable.getMessage();
		if (status != null) {
			status = status.trim();
		}
		return this.setCatalogTestStatus(con, catalogName, status);
	}

	/**
	 * @param con A connection to use.
	 * @return The list of tested databases.
	 */
	protected List<String> listTestedCatalogs(Connection con) {
		List<String> result = new ArrayList<String>();
		for ( String database : this.listCatalogs(con) ) {
			if ( this.isCatalogTested(con, database) ) {
				result.add(database);
			}
		}
		return result;
	}

	/**
	 * @param con A connection to use.
	 * @return The list of NOT tested databases.
	 */
	protected List<String> listNotTestedCatalogs(Connection con) {
		List<String> result = new ArrayList<String>();
		for ( String database : this.listCatalogs(con) ) {
			if ( !this.isCatalogTested(con, database) ) {
				result.add(database);
			}
		}
		return result;
	}

	/**
	 * @param con A connection to use.
	 * @param databaseName
	 */
	protected void skipCatalog(Connection con, String databaseName) {
		if ( this.listCatalogs(con).contains(databaseName) ) {
			this.catalogTestStatus.remove(databaseName);
			
			if (Check.isEmpty(this.catalogTestStatus)) {
				this.AfterSkipCatologEmptyFlag = true;
			}
		}
	}

	/**
	 * @param catalogResultMap
	 * @return A String with the counts by database.  Databases with a zero count are ignored
	 */
	protected String formatCatalogResultMap(Map<String,String> catalogResultMap) {
		List <String> detail = new ArrayList<String>();
		
		// check through the results to find any databases with a count greater than zero
		StringBuilder buf = new StringBuilder();
		for ( String database : catalogResultMap.keySet() ) {
			String results = catalogResultMap.get(database);
			results = Say.what(
					Say.ASMT_MSG_DB_VALUES,
					Say.ASMT_SUB_OBJ_LIST, results,
					Say.ASMT_SUB_DATABASE, database
			);
			Stringer.listSep(buf).append(results);
			detail.add(results);
		}
		return buf.toString();
		//return detail;
	}

	protected List<String> formatDetailCatalogResultMap(Map<String,String> catalogResultMap) {
		// check through the results to find any databases with a count greater than zero
		//StringBuilder buf = new StringBuilder();
		List <String> tmpList = new ArrayList<String>();
		
		for ( String database : catalogResultMap.keySet() ) {
			String results = catalogResultMap.get(database);
			results = Say.what(
					Say.ASMT_MSG_DB_VALUES,
					Say.ASMT_SUB_OBJ_LIST, results,
					Say.ASMT_SUB_DATABASE, database
			);
			//Stringer.listSep(buf,Say.DETAIL_SEP).append(results);
			tmpList.add(results);
		}
		//return buf.toString();
		return tmpList;
	}
	/**
	 * Changes the current database.
	 * @param con
	 * @param catalogName
	 * @throws SQLException
	 */
	protected void useCatalog(Connection con, String catalogName) throws SQLException {
		try {
			con.setCatalog(catalogName);

		} catch (SQLException e) {
			String msg = Say.what(
						Say.ASMT_MSG_DB_CAT_ACC_FAIL,
						Say.ASMT_SUB_DS, String.valueOf( this.getDatasource() ),
						Say.ASMT_SUB_DATABASE, catalogName
			);
			String dtl = Informer.thrownMessage(e);
			this.logWarn(msg, dtl);
			markCatalogNotTested(con, catalogName, e);
			throw e;
		}
	}

	/**
	 * @param con A connection to use.
	 * @param sql The sql statement to execute
	 * @return A string of the databases and users that have been granted the privilege.
	 *  Empty if there are none.
	 * @throws SQLException
	 * @throws GuardAssessmentException
	 */
	protected Map<String,String> findCatalogResults(Connection con, String sql)
	throws SQLException, GuardAssessmentException {
		Qualifier qualifier = null;
		Collection<String> catalogs = this.listCatalogs(con);
		return this.findCatalogResults(con, sql, catalogs, qualifier);
	}

	/**
	 * @param con A connection to use.
	 * @param sql The sql statement to execute
	 * @param qualifier A class that will validate result set values.
	 * @return A string of the databases and users that have been granted the privilege.
	 *  Empty if there are none.
	 * @throws SQLException
	 * @throws GuardAssessmentException
	 */
	protected Map<String,String> findCatalogResults(Connection con, String sql, Qualifier qualifier)
	throws SQLException, GuardAssessmentException {
		Collection<String> catalogs = this.listCatalogs(con);
		return this.findCatalogResults(con, sql, catalogs, qualifier);
	}

	/**
	 * Iterates through the databases of a MSSql Datasource looking for results from the sql query.
	 * Returns them in a string by database. Uses certain fixed i18n messages to format the results.
	 * Sets the test results and returns the score.
	 * @param con A connection to use.
	 * @param sql The Sql should return its value in column 1.
	 * @param dbNames A list of database names.
	 * @return A string of the databases and users that have been granted the privilege.
	 *  Empty if there are none.
	 * @throws SQLException
	 * @throws GuardAssessmentException
	 */
	protected Map<String,String> findCatalogResults(
			Connection con, String sql, Collection<String> dbNames
	) throws SQLException, GuardAssessmentException {
		Qualifier qualifier = null;
		return this.findCatalogResults(con, sql, dbNames, qualifier);
	}

	/**
	 * Iterates through the databases of a MSSql Datasource looking for results from the sql query.
	 * Returns them in a string by database. Uses certain fixed i18n messages to format the results.
	 * Sets the test results and returns the score.
	 * @param con A connection to use.
	 * @param sql The Sql should return its value in column 1.
	 * @param dbNames A list of database names.
	 * @param qualifier A class that will validate result set values.
	 * @return A string of the databases and users that have been granted the privilege.
	 *  Empty if there are none.
	 * @throws SQLException
	 * @throws GuardAssessmentException
	 */
	protected Map<String,String> findCatalogResults(
			Connection con, String sql, Collection<String> dbNames, Qualifier qualifier
	) throws SQLException, GuardAssessmentException {

		// stores the database by database results
		Map<String,String> result = new HashMap<String,String>();
		
		String originalCatalog = con.getCatalog();
		// loop through the databases
		for (String dbName : dbNames) {
			// change to the database and establish whether the login is a user.  If not, skip.
			try {
				this.useCatalog(con, dbName);
				// LOG.info("Changed database to: '" + dbName + "'.");
			} catch (SQLException e) {
				/*
				LOG.info(
						"Could not change to database: '" + dbName + "'. Skipping..."
						+ Say.NL + Informer.thrownMessage(e)
				);
				*/
				continue;
			}

			boolean qualify =  (qualifier != null);
			Statement stmt = null;
			ResultSet rs = null;
			StringBuilder values = new StringBuilder();
			try {
				// execute the Sql and loop through the result and accumulate the values
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(sql);
				while ( rs.next() ) {
					String value = rs.getString(1);

					// if we have a qualifier, validate the value, skip it if it does not pass the test
					if (qualify && !qualifier.isQualified(value) ) {
						continue;
					}
					Stringer.listSep(values).append(value);
				}

			} finally {
				Check.disposal(rs);
				Check.disposal(stmt);
			}

			// if there were any results, put them in the database / value map
			if ( !Check.isEmpty(values) ) {
				result.put(dbName, String.valueOf(values) );
				//if ( LOG.isDebugEnabled() ) {
				//	LOG.debug("Found: '" + values);
				//}
			}

			// check this one off the list
			this.markCatalogTested(con, dbName);
		}

		con.setCatalog(originalCatalog);
		// only return a result if at least one database was tested.
		if ( this.isAnyCatalogTested() ) {
			return result;
		}

		// no databases were tested, report an error
		String msg = Say.what(
				Say.ASMT_MSG_CAT_ACC_FAIL,
				Say.ASMT_SUB_DS, String.valueOf( this.getDatasource() )
		);
		throw new GuardAssessmentException(msg);
	}
	/*
	protected List<String> findDetailCatalogResults(
			Connection con, String sql, Collection<String> dbNames, Qualifier qualifier
	) throws SQLException, GuardAssessmentException {

		// stores the database by database results
		//Map<String,String> result = new HashMap<String,String>();
		List<String> l = new ArrayList<String>();

		// loop through the databases
		for (String dbName : dbNames) {
			// change to the database and establish whether the login is a user.  If not, skip.
			try {
				this.useCatalog(con, dbName);
				// LOG.info("Changed database to: '" + dbName + "'.");
			} catch (SQLException e) {
				
				//LOG.info(
				//		"Could not change to database: '" + dbName + "'. Skipping..."
				//		+ Say.NL + Informer.thrownMessage(e)
				//);
				
				continue;
			}

			//boolean qualify =  (qualifier != null);
			Statement stmt = null;
			ResultSet rs = null;
			//StringBuilder values = new StringBuilder();
			try {
				// execute the Sql and loop through the result and accumulate the values
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(sql);
				List<String> oneDblist =JdbcUtils.getListFromRowSet(rs,3);
				for (int i=0; i< oneDblist.size(); i++)
					l.add(dbName + "-" + oneDblist.get(i));
			} finally {
				Check.disposal(rs);
				Check.disposal(stmt);
			}

			// if there were any results, put them in the database / value map
			//if ( !Check.isEmpty(values) ) {
			//	result.put(dbName, String.valueOf(values) );
			//	if ( LOG.isDebugEnabled() ) {
			//		LOG.debug("Found: '" + values);
			//	}
			//}
		}

		return l;
	}
	*/
	
	/*
	protected String findResult(
			Connection con, String sql
	) throws SQLException {

		// stores the database by database results
		Map<String,String> result = new HashMap<String,String>();

		Statement stmt = null;
		ResultSet rs = null;
		StringBuilder values = new StringBuilder();
		try {
			// execute the Sql and loop through the result and accumulate the values
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);
			List<String> l =JdbcUtils.getListFromRowSet(rs,3);
			values.append(Stringer.collectionToString(l,"\n"));
		} finally {
			Check.disposal(rs);
			Check.disposal(stmt);
		}

		return values.toString();
	}
	*/
	
	/**
	 * @see com.guardium.assessment.GenericTest#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + Say.SP + this.getDatasource();
	}

	/**
	 * Interface for passing a qualifier for the result set.
	 * This qualifier can provide further qualification of result set values.
	 * Result set values will only be counted towards the overall result
	 * if they pass the is qualified method
	 * @author dtoland on Feb 7, 2008 at 11:12:39 AM
	 */
	protected static interface Qualifier {
		/**
		 * @param resultValue
		 * @return Whether the individual result is valid.
		 */
		public boolean isQualified(String resultValue);
	}

	/**
	 * Implementation of Qualifier that will reject numeric values
	 * that are not greater than the threshold
	 * @author dtoland on Feb 7, 2008 at 12:06:51 PM
	 */
	protected static class ThresholdQualifier implements Qualifier {
		private final BigDecimal threshold;
		/**
		 * @param threshold
		 */
		public ThresholdQualifier(double threshold) {
			this.threshold = new BigDecimal(threshold);
		}
		/**
		 * Creates a threshold of zero.
		 */
		public ThresholdQualifier() {
			this.threshold = new BigDecimal("0");
		}
		/**
		 * @see com.guardium.assessment.AbstractJdbcTest.Qualifier#isQualified(java.lang.String)
		 */
		@SuppressWarnings("synthetic-access")
		public boolean isQualified(String resultValue) {
			boolean result = true;
			try {
				BigDecimal value = new BigDecimal(resultValue);
				result = ( value.compareTo(this.threshold) > 0 );
			} catch (NumberFormatException e) {
				String msg = "Could not parse '" + resultValue + "' as a number.";
				// LOG.error(msg, e);
				//AdHocLogger.logException(e);
			}
			return result;
		}
	}
	protected String formatList(Collection c)
	{
		return Stringer.collectionToString(c, Say.SP + Say.NL);
	}
	protected void cacheDetailInfo(Collection c)
	{
		try{
			setForDetail(Stringer.collectionToString(c));	
		}catch(Throwable t)
		{
			//AdHocLogger.logException(t);
		}
	}
	
	public List<String> fetchDetail() throws Exception
	{		
		return calculateDetail(this.connection);
	}
	
//	public String fetchDetail() throws Exception
//	{		
//		return calculateDetail(this.connection);
//	}
	
	public List<String> calculateDetail(Connection con) throws Exception
	{
		List<String> l = new ArrayList<String>();
		return l;
	}
	
//	public String calculateDetail(Connection con) throws Exception
//	{
//		return "N/A";
//	}
	
	/**
	 * @param con A connection to use.
	 * @param query The query to execute
	 * @param catalog The catalog in which to execute the query.
	 * @return A List of each result set row as a space-delimited string.
	 * @throws SQLException
	 */
	protected List<String> queryDetail(Connection con, String query, String catalog) throws SQLException {
		this.useCatalog(con, catalog);
		return this.queryDetail(con, query);
	}
	
	/**
	 * @param con A connection to use.
	 * @param query The query to execute
	 * @return A List of each result set row as a space-delimited string.
	 * @throws SQLException
	 */
	protected List<String> queryDetail(Connection con, String query) throws SQLException {
		List<String> result = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			
			int cols = rs.getMetaData().getColumnCount();
			if (cols > 0) {			
				while ( rs.next() ) {
					StringBuilder buf = new StringBuilder();
					for ( int i = 1; i <= cols; i++) {
						String value = rs.getString(i);
						if ( !Check.isEmpty(value) ) {
							Stringer.delimit(buf, Say.SP).append( value.trim() );
						}
					}
					
					if ( !Check.isEmpty(buf) ) {
						result.add( buf.toString() );
					}
				}
			}
			return result;

		} finally {
			rs = Check.disposal(rs);
			stmt = Check.disposal(stmt);
		}
	}

	/**
	 * Executes the detail query and sets the detail attribute with the results.
	 * @param con
	 * @param query 
	 * @return The value used to set the detail attribute.
	 */
	protected List<String> findAndSetDetail(Connection con, String query) {
		String detail;
		List<String> rows = new ArrayList<String>();
		try {
			 rows = this.queryDetail(con, query);
			//detail = Stringer.collectionToString(rows, Say.SP + Say.NL);
			
		} catch (SQLException e) {
			//detail = Say.what(Say.ASMT_GENERIC_ERROR + Say.SP + Say.NL + Informer.thrownMessage(e) );
		}
		
		this.setDetail(rows);
		return rows;
	}
	
	
}
