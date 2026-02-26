/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import com.guardium.assessment.i18n.Say;
import com.guardium.data.AssessmentLog;
import com.guardium.data.AssessmentLogType;

//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
import com.guardium.map.AssessmentLogMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class AssessmentLogMap {
    
	private static AssessmentLogMap AssessmentLogMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private AssessmentLogMap () {
		//initMap();
	}
	
	public static synchronized AssessmentLogMap getAssessmentLogMapObject() {
		if (AssessmentLogMapObject == null) {
			AssessmentLogMapObject = new AssessmentLogMap();
		}
		return AssessmentLogMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static long currentAssessmentLogId = 20000;
	
	private static ConcurrentHashMap hm = new ConcurrentHashMap();

	/*
	private static List <AssessmentLog> dtlist = new ArrayList<AssessmentLog>();
	
	public List<AssessmentLog> getList() {
		return dtlist;
	}

	public void setList(List<AssessmentLog> tlist) {
		this.dtlist = tlist;
	}
	*/
	public static void add (AssessmentLog v) {
		v.setAssessmentLogId(currentAssessmentLogId);
		hm.put(currentAssessmentLogId, v);
		currentAssessmentLogId++;
		return;
	}	
	
	/*
	public static void add (long id, AssessmentLog v) {
		hm.put(id, v);
		return;
	}
	*/
	
	public static void remove (long id) {
		hm.remove(id);
		return;
	}
	
	public AssessmentLog getAssessmentLog (long id) {
		return (AssessmentLog)hm.get(id);
	}
	
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(AssessmentLogMap.class);

	/** Constant for the Log Type */
	final public static  int TYPE_ASSESSMENT_ERROR = 4;
	/** Constant for the Log Type */
	final public static  int TYPE_ASSESSMENT_START = 201;
	/** Constant for the Log Type */
	final public static  int TYPE_ASSESSMENT_COMPLETE = 203;
	/** Constant for the Log Type */
	final public static  int TYPE_ASSESSMENT_FATAL = 5;
	/** Constant for the Log Type */
	final public static  int TYPE_START_TEST = 211;
	/** Constant for the Log Type */
	final public static  int TYPE_TEST_END = 212;
	/** Constant for the Log Type */
	final public static  int TYPE_INFO = 2;
	/** Constant for the Log Type */
	final public static  int TYPE_WARNING = 3;
	/** Constant for the Log Type */
	final public static  int TYPE_LISTENER_EXIT = 6;

	/**
	 * Logs the assessment start
	 * @param resultHeader The result header
	 */
	//public static void logStart(AssessmentResultHeader resultHeader) {
	public static void logStart(AssessmentResultHeader resultHeader) {
		// get the result header id and the assessment description
		long id = -1;
		String desc = null;
		StringBuilder detail = new StringBuilder();
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
			desc = resultHeader.getAssessmentDesc();

			try {
				SecurityAssessment assessment = resultHeader.getSecurityAssessment();
				if (assessment != null) {
					String tests = Stringer.collectionToString( assessment.getAssessmentTests(), Say.LIST_SEP);
					Stringer.newLn(detail).append( Say.what(Say.ASMT_LIT_TESTS) + Say.SP + tests);
					Stringer.newLn(detail).append(Say.NL);
					String datasources = Stringer.collectionToString( assessment.listDatasources(), Say.LIST_SEP);
					Stringer.newLn(detail).append( Say.what(Say.ASMT_LIT_DATASOURCES) + Say.SP + datasources);
				}
			} catch (Exception e) {
				//AdHocLogger.logException(e);
			}
		}

		// create the log message
		String msg = Say.what(
				Say.ASMT_MSG_LOG_START,
				Say.ASMT_SUB_ASMT, desc
		);
		/*
		if ( LOG.isInfoEnabled() ) {
			LOG.info(msg + Say.NL + detail);
		}
		*/
		log(TYPE_ASSESSMENT_START, id, msg, detail.toString() );
	}

	/**
	 * Logs the assessment completion
	 * @param resultHeader The result header
	 * @param details
	 */
	public static void logComplete(AssessmentResultHeader resultHeader, String details) {
	//public void logComplete(AssessmentResultHeader resultHeader, String details) {
		long id = -1;
		String desc = null;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
			desc = resultHeader.getAssessmentDesc();
		}

		String msg = Say.what(
				Say.ASMT_MSG_LOG_COMPLETE,
				Say.ASMT_SUB_ASMT, desc
		);
		/*
		if ( LOG.isInfoEnabled() ) {
			LOG.info(msg + Say.NL + details);
		}
		*/
		log(TYPE_ASSESSMENT_COMPLETE, id, msg, details);
	}

	/**
	 * Logs the a non-recoverable assessment error.
	 * @param securityAssessment The Security Assessment.
	 * @param resultHeader The result header
	 * @param throwable
	 */
	//public static void logFatal(
	public static void logFatal(
			SecurityAssessment securityAssessment, AssessmentResultHeader resultHeader, Throwable throwable
	) {
		logFatal(securityAssessment, resultHeader, "", throwable);
	}

	/**
	 * Logs the a non-recoverable assessment error.
	 * @param securityAssessment The Security Assessment.
	 * @param resultHeader The result header
	 * @param detail
	 * @param throwable
	 */
	public static void logFatal(
			SecurityAssessment securityAssessment, AssessmentResultHeader resultHeader, String detail,
			Throwable throwable
	) {
		long id = -1;
		String desc = null;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
			desc = resultHeader.getAssessmentDesc();
		} else if (securityAssessment != null) {
			desc = securityAssessment.getAssessmentDesc();
		}

		String msg = Say.what(
				Say.ASMT_MSG_LOG_FATAL,
				Say.ASMT_SUB_ASMT, desc
		);

		StringBuilder buf = new StringBuilder();
		if (detail != null) {
			buf.append(detail + Say.NL);
		}
		buf.append( Informer.thrownMessage(throwable) );

		// LOG.fatal(msg + Say.NL + buf.toString(), throwable);
		log(TYPE_ASSESSMENT_FATAL, id, msg, buf.toString() );
	}

	/**
	 * Logs the assessment completion
	 * @param resultHeader The result header
	 */
	public static void logCancel(AssessmentResultHeader resultHeader) {
		long id = -1;
		String desc = null;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
			desc = resultHeader.getAssessmentDesc();
		}

		String msg = Say.what(
				Say.ASMT_MSG_LOG_CANCEL,
				Say.ASMT_SUB_ASMT, desc
		);
		String detail = "";
		/*
		if ( LOG.isInfoEnabled() ) {
			LOG.info(msg + Say.NL + detail);
		}
		*/
		log(TYPE_TEST_END, id, msg, detail);
	}

	/**
	 * @param resultHeader
	 * @param assessmentTest
	 * @param t
	 */
	public static void logTestError(
			AssessmentResultHeader resultHeader, AssessmentTest assessmentTest, Throwable t
	) {
		Datasource ds = null;
		logTestError(resultHeader, assessmentTest, ds, t);
	}

	/**
	 * Logs the an assessment error.
	 * @param resultHeader The result header
	 * @param assessmentTest
	 * @param datasource
	 */
	public static void logTestError(
			AssessmentResultHeader resultHeader, AssessmentTest assessmentTest, Datasource datasource
	) {
		Throwable t = null;
		logTestError(resultHeader, assessmentTest, datasource, t);
	}

	/**
	 * Logs the an assessment error.
	 * @param resultHeader The result header
	 * @param msg
	 * @param detail
	 */
	public static void logTestError(
			AssessmentResultHeader resultHeader, String msg, String detail
	) {
		long id = -1;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
		}
		log(TYPE_ASSESSMENT_ERROR, id, msg, detail);
	}

	/**
	 * Logs the an assessment error.
	 * @param resultHeader The result header
	 * @param assessmentTest
	 * @param datasource
	 * @param throwable
	 */
	public static void logTestError(
			AssessmentResultHeader resultHeader, AssessmentTest assessmentTest,
			Datasource datasource, Throwable throwable
	) {

		String testdesc = null;
		if (assessmentTest != null ) {
			testdesc = String.valueOf(assessmentTest);
		}

		String msg;
		if (datasource != null) {
			msg = Say.what(
					Say.ASMT_MSG_LOG_ERROR_TEST,
					Say.ASMT_SUB_TEST, testdesc,
					Say.ASMT_SUB_DS, String.valueOf(datasource)
			);

		} else {
			msg = Say.what(
						Say.ASMT_MSG_LOG_ERROR_TEST_NO_DS,
						Say.ASMT_SUB_TEST, testdesc
			);
		}

		String detail = "";
		if (throwable != null) {
			detail = Informer.thrownMessage(throwable);
		}

		// LOG.error(msg + Say.NL + detail);
		logTestError(resultHeader, msg, detail);
	}

	/**
	 * @param resultHeader
	 * @param serverType
	 * @param servers
	 */
	public static void logCorrelation(
			AssessmentResultHeader resultHeader, String serverType, String servers
	) {
		long id = -1;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
		}

		String msg;
		int type;
		if( servers.length() > 0) {
			type = TYPE_WARNING;
			msg = Say.what(
					Say.ASMT_MSG_CORRELATE_PART,
					Say.ASMT_SUB_TYPE, serverType
			);

		} else {
			type = TYPE_INFO;
			msg = Say.what(
					Say.ASMT_MSG_CORRELATE_FULL,
					Say.ASMT_SUB_TYPE, serverType
			);
		}

		// LOG.warn(msg + Say.NL + servers);
		//AssessmentLogPeer.log(type, id, msg, servers);
		log(type, id, msg, servers);
	}

	/**
	 * Logs the an assessment test error.
	 * @param resultHeader The result header
	 * @param detail
	 */
	public static void logWarn(AssessmentResultHeader resultHeader, String detail) {
		Throwable throwable = null;
		logWarn(resultHeader, detail, throwable);
	}

	/**
	 * Logs the an assessment test error.
	 * @param resultHeader The result header
	 * @param msg
	 * @param detail
	 */
	public static void logWarn(AssessmentResultHeader resultHeader, String msg, String detail) {
		Throwable throwable = null;
		long id = -1;
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
		}

		// LOG.warn(msg + Say.NL + detail, throwable);
		log(TYPE_WARNING, id, msg, detail);
	}

	/**
	 * Logs the an assessment test error.
	 * @param resultHeader The result header
	 * @param detail
	 * @param throwable
	 */
	public static void logWarn(
			AssessmentResultHeader resultHeader, String detail, Throwable throwable
	) {
		long id = -1;
		String desc = "";
		String dsNames = "";
		if (resultHeader != null) {
			id = resultHeader.getAssessmentResultId();
			desc = resultHeader.getAssessmentDesc();
			dsNames = resultHeader.getDatasourceNames();
		}

		String msg = Say.what(
				Say.ASMT_MSG_LOG_ERROR_TEST,
				Say.ASMT_SUB_TEST, desc,
				Say.ASMT_SUB_DS, dsNames
		);

		StringBuilder buf = new StringBuilder(detail);
		if (throwable != null) {
			Stringer.newLn(buf).append( Informer.thrownMessage(throwable) );
		}

		// LOG.warn(msg + Say.NL + buf, throwable);
		log(TYPE_WARNING, id, msg, buf.toString() );
	}

	/**
	 * The raw logging method
	 * @param type
	 * @param resultId
	 * @param msg
	 * @param detail
	 */
	public static void log(int type, long resultId, String msg, String detail) {
		//try{
			AssessmentLog rec = new AssessmentLog();
			rec.setAssessmentLogTypeId(type);
			rec.setAssessmentResultId(resultId);
			rec.setMessage(msg==null?"":msg);
			rec.setDetails(detail==null?"":detail);
			rec.save();

			// LOG.warn( String.valueOf(rec) );

		//} catch (Exception e) {
		//	processLoggingException(e,msg,detail);
		//}
	}

	/*
	private static void processLoggingException(Exception e, String msg, String detail){
		String err = Say.what(Say.ASMT_MSG_LOG_FAIL);
		GdmException gdmEx = new GdmException();
		gdmEx.setDescription(err);
		gdmEx.setGId( AdminconsoleParameterPeer.getActiveConfiguration().getGlobalId() );
		gdmEx.setSqlString(Say.NL+msg + Say.NL + detail+e.getMessage());

		try {
			gdmEx.setExceptionTypeId("ASMT_ERROR");
			gdmEx.save();
			AdHocLogger.logException(e);
		} catch (Exception gdme) {
			AdHocLogger.logException(gdme);
		}
	}
	*/
	
	 /**
     * override retrieve by pk to use criteria for inv databases - must be done in all results classes
     *
     * @param pk the primary key
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    //public static AssessmentLog retrieveByPK(long pk)
    public AssessmentLog retrieveByPK(long pk)
    {
    	return getAssessmentLog (pk);

    }
}

