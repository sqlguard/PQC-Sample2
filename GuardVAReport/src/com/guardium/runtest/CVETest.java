/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.guardium.data.TestResult;

//import javax.sound.midi.Patch;

import org.apache.log4j.Logger;

import com.guardium.runtest.GenericTest;
import com.guardium.assessment.i18n.Say;
import com.guardium.assessment.tests.TestScore;
import com.guardium.assessment.utils.DataSourceInfoGeter;
import com.guardium.map.AvailableTestMap;
import com.guardium.map.CveFixMap;
import com.guardium.data.CveFix;
//import com.guardium.datamodel.dbSource.CASRequiredException;
import com.guardium.data.Datasource;
import com.guardium.map.DatasourceMap;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
//import com.guardium.utils.DbUtils;
//import com.guardium.utils.Informer;


public class CVETest extends GenericTest {
	
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(CVETest.class);

	private static final String NON_WINDOWS = "NON-WINDOWS";
	private static final String WINDOWS = "WINDOWS";
	private static Locale locale = getLocale();
	public static final ResourceBundle cveAssessmentResources = !"ww".equalsIgnoreCase(locale.getLanguage()) ? ResourceBundle.getBundle("com.guardium.assessment.tests.CVE.cveAssessmentResources") : ResourceBundle.getBundle("com.guardium.assessment.tests.CVE.cveAssessmentResources", locale);
	
	private static Locale getLocale() {
		
		try {
			Locale.Builder wwLocaleBuilder = new Locale.Builder();
			wwLocaleBuilder.setLanguage("ww");
			wwLocaleBuilder.setRegion("CN");
			
			final String fname = "com.guardium.portal.admin.InstallationLanguage";
			ResourceBundle res = ResourceBundle.getBundle(fname, wwLocaleBuilder.build());
			
			if (res != null) {
				String country = res.getString("locale.country") ;
				String language = res.getString("locale.language") ;
				
				Locale.Builder builder = new Locale.Builder();
				if (!Check.isEmpty(language)) {
					language = language.trim();
					builder.setLanguage(language);
				}
				if (!Check.isEmpty(country)) {
					country = country.trim();
					builder.setRegion(country);
				}
				
				Locale aLocale = builder.build();
				return aLocale ;
			}
		} catch (Exception e) {
			//Do nothing
		}
		
		//Default to en-US
		return new Locale("en", "US");
	}

	private String osName = null;
	private DataSourceInfoGeter dsig = null;
	private String os = null;
	private Datasource ds = null;
	TestScore score = null;
	String resultText = null;
	String reccommendationText = null;
	String version = null;
	String versionPatchString = "";
        String versionCDH = null;
        String patchCM = null;
        String patchCDH = null;
		 
	@Override
	public TestScore executeTest(String assessmentFromDate,
			String assessmentToDate, String filterClientIP,
			String filterServerIP, double timeFactor)
	{
		List <CveFix> cveFixRecords = null;
		if(os==null)
			os = getAvailableTest().getOs();
		if(ds==null)
			ds = getDatasource();
		try
		{
			if(dsig == null)
				dsig = 	DataSourceInfoGeter.initDatasourceInfoGetter(ds.getDatasourceType());
			//System.out.println("dbsource="+ds.getName());
			
			if(!Check.isEmpty(os))
			{
				try
				{
					if(!matchDbOs(os))
					{
						score = TestScore.CVE_NOT_REPORTED;
						resultText = Say.what(Say.ASMT_CVE_OS_NA, "${os}", osName);
					}
				}
				catch (SQLException se)
				{
					String msg = Say.what(Say.ASMT_ERROR_RETRIEVING_OS);
					// LOG.warn(msg, se);
					score = TestScore.ERROR;
					resultText = msg +" - "+ se.getLocalizedMessage();
					AdHocLogger.logException(se);
				}
			}
			
            // get version once
			version = dsig.getDbVersion(ds);
			
			String msg = "";
			if(score == null)
			{	
				cveFixRecords = getAvailableTest().getCveFixs();
                msg = "1 cveFixRecords size is " + cveFixRecords.size();
				// LOG.warn(msg);
				for (CveFix cf  : cveFixRecords) 
				{
					String refV = cf.getVersion();
					String refP = cf.getPatch();
					
					msg = "1 cvefix version " + refV + " patch " + refP;
					// LOG.warn(msg);
				}
				evaluateDbVersion(cveFixRecords);
			}
			if(score == null)
			{	
				cveFixRecords = filterByVersion(cveFixRecords);
				msg = "2 cveFixRecords size is " + cveFixRecords.size();
				// LOG.warn(msg);
				for (CveFix cf  : cveFixRecords) 
				{
					String refV = cf.getVersion();
					String refP = cf.getPatch();
					
					msg = "2 cvefix version " + refV + " patch " + refP;
					// LOG.warn(msg);
				}
				try
				{
					evaluateDbPatchLevel(cveFixRecords);
				}
				catch (SQLException se)
				{
					msg =Say.what(Say.ASMT_ERROR_RETRIEVING_PATCH);
					// LOG.warn(msg, se);
					score = TestScore.ERROR;
					resultText = msg + se.getLocalizedMessage();
					AdHocLogger.logException(se);
				}
			}
			if(score == TestScore.FAIL)
			{
				for (CveFix cveFix : cveFixRecords) { 
					versionPatchString += " Version-"+cveFix.getVersion()+", Patch-"+cveFix.getPatch();
                                        if (!cveFix.getPatchTo().isEmpty()) {
                                                versionPatchString += " to " + cveFix.getPatchTo();
                                        }
				}
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			score = TestScore.ERROR;
			resultText = Say.what(Say.ASMT_GENERIC_ERROR);
			AdHocLogger.logException(ex);

		}
		reccommendationText = getReccommendationForScore(score);
		TestResult tr = recordResult(score, resultText, reccommendationText, ds);
		tr.save();
		
		/*
		// record the results and return the score
		String recommend = this.getRecommendationText(score);
		this.testResult = this.recordResult(score, result, recommend, this.getDatasource() );
		if ( // LOG.isDebugEnabled() ) {
			LOG.debug(
					"[" + score + "] " + this.testResult.getResultText()
					+ Say.NL + "Detail: " + this.testResult.getDetail()
					+ Say.NL + "Recommend: " + recommend
			);
		}
		*/
		
		//System.out.println("score= "+score+" , text= "+resultText);
		return score;
	}

	private String getReccommendationForScore(TestScore score)
	{
		String ret = null;
		switch (TestScore.findTestScore(score.getScoreValue()))
		{
		case PASS:
			ret =  Say.what(Say.ASMT_REC_CVE_PASSED,"CVE", getAvailableTest().getExternalReference());
			break;
		case FAIL:
			ret = Say.what(Say.ASMT_REC_CVE_FAILED,"CVE", getAvailableTest().getExternalReference(), "v-p", getVersionPatchString());
			break;
		case ERROR:
			ret = Say.what(Say.ASMT_REC_CVE_ERROR);	
		case CVE_NOT_REPORTED:
			ret = "N/A";
		}
		return ret;
	}

	private String getVersionPatchString() 
	{
		return versionPatchString;
	}

	private List<CveFix> filterByVersion(List<CveFix> cveFixRecords) throws Exception 
	{
		List <CveFix> l = new ArrayList <CveFix> ();
		String msg = "";
		String refversion = "";
		refversion = dsig.refVersion;
		msg = "1 filter cvefix size " + cveFixRecords.size() + " refVersion is " + refversion;
		// LOG.warn(msg);
		
		// we already got version at beginning
		//version = dsig.getDbVersion(ds);

		msg = "2 filter cvefix size " + cveFixRecords.size() + " version is " + version;
		// LOG.warn(msg);
		
		for (Iterator<CveFix> iterator = cveFixRecords.iterator(); iterator.hasNext();) 
		{
			CveFix cveFix = (CveFix) iterator.next();
		
			// need to handle 15.7.0 and 15.7 as the same version
			List <String> referenceVersions = new ArrayList<String>();
			referenceVersions.add(cveFix.getVersion());
			boolean exact_match = true;
			if (dsig.evaluateDbVersion(referenceVersions, version, exact_match) == 0) {
				l.add(cveFix);
			}
		}
		
		msg = "end of filter cvefix size " + l.size();
		// LOG.warn(msg);
		return l;
	}

	private void evaluateDbPatchLevel(List<CveFix> cveFixRecords) throws  Exception
	{
        	boolean found_ojvm = false;
        	String ojvm = "OJVM";
		String patch = null;
		List <String> referencePatches = new ArrayList<String>(cveFixRecords.size());
		for (CveFix cveFix : cveFixRecords) 
		{
			String separator = "-";
                        String patchTo = cveFix.getPatchTo();
			String tmpstr = cveFix.getPatch();
                        if (!patchTo.isEmpty())
                                tmpstr = tmpstr + separator + patchTo;

			referencePatches.add(tmpstr);
			if (tmpstr.indexOf(ojvm) > 0) {
                    		// found OJVM patch
                    		found_ojvm = true;
			}
		}
		
        	if (found_ojvm) {
            		dsig.setOjvm(true);
        	}

		
		/* for debug
		for (String refP : referencePatches) 
		{
			//LOG.warn("ref patch string " + refP);
			System.out.println("ref patch string " + refP);
		}
		*/
		
		try 
		{
			patch = dsig.getDbPatch(ds);
			//System.out.println("dsig patch is " + patch);
		}
		// catch (CASRequiredException cre)
		catch (Exception cre)
		{
			//patch = dsig.findCasPatchData(ds, testId, getCasId());
		
			// LOG.warn("findCasPatchData patch is " + patch);
			if(patch.equalsIgnoreCase("Unknown"))
			{
				score = TestScore.ERROR;
				resultText = Say.what(Say.ASMT_CVE_CAS_REQUIRED);
				return;
			}

			// patch = patch.substring(patch.indexOf("FixPak")+7);
			// patch = patch.substring(patch.indexOf('"')+1);
			//  patch = patch.substring(0, patch.indexOf('"'));
			//LOG.warn("patch from cas= "+patch);
			patch = patch.substring(patch.indexOf("Fix")+7);
			while(patch.startsWith(" "))
				patch = patch.substring(1);
			String [] patchS = patch.split(" ");
			patch = patchS[patchS.length-1].replaceAll("\"", "");
			// LOG.warn("patch after trim = "+patch);
		}
		
		// LOG.warn("before evaluateDbPatch patch is " + patch);
		//int tmp =  dsig.evaluateDbPatch(referencePatches, patch);
		//System.out.println("dsig evaluate DbPatch retrun " + tmp);
		
		switch (dsig.evaluateDbPatch(referencePatches, patch, version)) 
		{
		case 1:
			score = TestScore.PASS;
			resultText = Say.what(Say.ASMT_CVE_TEXT_PASS,"CVE", getAvailableTest().getExternalReference());
			break;
		case 0:
			score = TestScore.CVE_NOT_REPORTED;
			resultText = Say.what(Say.ASMT_CVE_NOT_REPORTED,"CVE", getAvailableTest().getExternalReference(),"DBTYPE",ds.getDbType(),"DBVERSION", dsig.getDbVersion(ds)+" "+patch);
			break;
		case -1:
			score = TestScore.FAIL;
			resultText = Say.what(Say.ASMT_CVE_TEXT_FAILED,"CVE", getAvailableTest().getExternalReference());
			break;
        	case -15:
            		score = TestScore.UNSUPPORT_CVE_PATCH_DETECTED;
            		resultText = Say.what(Say.ASMT_CVE_PATCH_NOT_DETECTED,"CVE", getAvailableTest().getExternalReference(),"DBTYPE",ds.getDbType(),"DBVERSION", dsig.getDbVersion(ds)+" "+patch);
            		break;
		}
	}

	private void evaluateDbVersion(List<CveFix> cveFixRecords) throws Exception 
	{
		List <String> referenceVersions = new ArrayList<String>(cveFixRecords.size());
		version = dsig.getDbVersion(ds);
		// LOG.warn("evaluateDbVersion version is " + version);
		
		for (CveFix cveFix : cveFixRecords) 
		{
			referenceVersions.add(cveFix.getVersion());
		}
		boolean exact_match = true;
		switch (dsig.evaluateDbVersion(referenceVersions, version, exact_match)) 
		{
		/* if no match */
		case 1:
		case -1:
			score = TestScore.CVE_NOT_REPORTED;
			resultText = Say.what(Say.ASMT_CVE_NOT_REPORTED,"CVE", getAvailableTest().getExternalReference(),"DBTYPE",ds.getDbType(),"DBVERSION", dsig.getDbVersion(ds));
			break;
		case 0: 
			// continue to evaluate patch level
			break;
		}
	}

	private boolean matchDbOs(String os) throws Exception 
	{
		boolean ret = false;
		if(ds == null)
			ds = getDatasource();
		osName = dsig.getOs(ds);
		String [] winOsNames = cveAssessmentResources.getString("windows.os.names").split(",");
		if(os.equalsIgnoreCase(WINDOWS))
		{
			for (int i = 0; i < winOsNames.length; i++) 
			{
				if(osName.toUpperCase().contains(winOsNames[i].toUpperCase()))
				{
					ret = true;
					break;
				}
			}
		}
		else if(os.equalsIgnoreCase(NON_WINDOWS))
		{
			ret = true;
			for (int i = 0; i < winOsNames.length; i++) 
			{
				if(osName.toUpperCase().contains(winOsNames[i].toUpperCase()))
				{
					ret = false;
					break;
				}
			}
			
		}
		else
		{
			ret = osName.toUpperCase().contains(os.toUpperCase());
		}
		return ret;
	}

}
