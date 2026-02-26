/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;

import com.guardium.runtest.CVETest;
//import com.guardium.assessment.tests.dbversion.AbstractDatabaseVersionTest;
//import com.guardium.datamodel.assessment.TestAlternativeTemplatePeer;
//import com.guardium.datamodel.changeaudit.CasAuditConfig;
//import com.guardium.datamodel.changeaudit.CasAuditConfigPeer;
//import com.guardium.datamodel.changeaudit.CasAuditStatePeer;
//import com.guardium.datamodel.changeaudit.CasAuditTarget;
//import com.guardium.datamodel.changeaudit.CasAuditTargetPeer;
//import com.guardium.datamodel.dbSource.CASRequiredException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceVersionHistory;
//import com.guardium.map.DatasourceVersionHistoryPeer;
import com.guardium.data.DataSourceConnectException;
import com.guardium.utils.Check;
import com.guardium.utils.Regexer;
//import com.mongodb.MongoClient;

public abstract class DataSourceInfoGeter 
{
	//private static final transient Logger LOG = Logger.getLogger(DataSourceInfoGeter.class);
	public String os = null;
	//public String dbVersion = null;
	public String refVersion = null;
	//public String dbPatchLevel = null;
	public abstract String getOs(Datasource ds) throws Exception;
	protected abstract long[] getAllIds();
	public abstract int  evaluateDbPatch(List<String> referencePatches, String patch, String version); 
	/** CAS data collection error indicator */
	protected boolean[] casErrorFlag = new boolean[] { false };

    // This flag is to support Oracle OJVM. should not affect other db type.
    protected boolean ojvmFlag = false;
    public void setOjvm (boolean tmp) {
            ojvmFlag = tmp;
            return;
    }

    public boolean getOjvm() {
            return ojvmFlag;
    }
	
	public String getOneTryValue(Connection con, String sql, int resultInd) throws SQLException 
	{
		Statement stmt = null;
		ResultSet res = null;
		String ret = null;
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			res = stmt.executeQuery(sql);
			if ( res.next() ) 
			{
				ret = res.getString(resultInd);
			}
			return ret;
		} 
		finally
		{
			Check.disposal(res);
			Check.disposal(stmt);
		}
	}
	
	public String getDbVersion(Datasource ds) throws Exception 
	{
			/*String version = null;
			DatabaseMetaData	databaseMetaData = connection.getMetaData();
			int					databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
			int					databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();
			version = databaseMajorVersion+"."+databaseMinorVersion;
			return version; */
			return ds.getVersionLevel();
	}

	public  String getDbPatchLevel(Datasource ds, Connection con) throws Exception
	{
		
		return ds.getPatchLevel(con);
	}	
		
	public  String getDbPatchLevel(Datasource ds) throws Exception
	{
		return ds.getPatchLevel();
	}
	
	public  String getDbPatch(Datasource ds) throws Exception
	{
		return getDbPatchLevel(ds);
	}
	/*
	 * compare version of database to list of reference versions in CVE_FIX for this test.
	 * first split version by "." so that 10.0.1.7 is split to {10,0,1,7}
	 * then compare from right to left
	 * if exact match, go through the list, if found exact match, return 0, else return -1 or 1.
	 * if not exact match, go through the list, if found one match or greater than, return 0 or 1, else return -1.
	 * 
	 * return -1 if the tested version is bellow the lowest reference version.
	 * return 1 if the tested version is above the largest reference version.
	 * return 0 if the tested version matches a reference version - 
	 * the comparison stops on the length of the reference version so 1.0.1 matches 1.0.1.7 
	 */
	public int evaluateDbVersion(List<String> referenceVersions, String version) 
	{
		boolean exact_match = false;
		return evaluateDbVersion(referenceVersions, version, exact_match);
	}
	
	public int evaluateDbVersion(List<String> referenceVersions, String version, boolean exact_match) 
	{
		int ret = -1;
		String rvitem;
		//LOG.warn("evaluateDbVersion start with version " + version);
		String [] vItems = version.split("\\.");
		for (String refVer : referenceVersions) 
		{
			// reset for each comparision
            ret = -1;
			//LOG.warn("refVersion " + refVer);
			String [] rvItems = refVer.split("\\.");
            int rvLen = rvItems.length;
            int vLen = vItems.length;
            int minLen = vLen;
            if (rvLen < vLen) {
                    minLen = rvLen;
            }
            int i = 0;
			for (i = 0; i < minLen; i++) 
			{
				//LOG.warn("i=" + i + " ver=" + vItems[i] + " refV=" + rvItems[i]);
				rvitem = rvItems[i];
				if(rvitem.endsWith("*"))
					rvitem = rvitem.replace("*","");
				if(rvitem.equalsIgnoreCase(vItems[i])) {
					ret = 0;
					//LOG.warn("v equal rv");
					// keep processing
				}
				// if not all numeric like oracle 9.2.0.8DV
				else if(!Pattern.matches("\\d+", rvitem.trim())||!Pattern.matches("\\d+", vItems[i].trim()))
				{
					//LOG.warn("can not compare");
					ret = -1; // if not numeric and does not match the version -1 will cause a result of "unsupported version"
					break;
				}
				else
				{
					int v  = Integer.parseInt(vItems[i]);
					int rv = Integer.parseInt(rvItems[i]);
					if(v > rv)
					{
						//LOG.warn("v > rv");
						ret = 1;
						break;
					
						// don't need to keep on processingq!
					}
					else if (v == rv) {
						// if string match not good, will fall here and do integer match
						// if equal, it is ok, like to compare "0" and "00"
						ret = 0;
						break;
					}
					else { 
						//LOG.warn("v < rv");
						ret = -1;
						break;
					}
				}
				/*
				if(i==rvItems.length-1)
				{	
					LOG.warn("i = length -1" + i);
					refVersion = refVer;
					return ret;	
				}
				*/
			}
			
			// need to check return value to see if need to continue
			if (exact_match) {
				if (ret == 0) {
				    // get exact match
                    //LOG.warn("found match - i =" +i + " rvLen=" + rvLen + " vLen=" + vLen);
                    // found match ref
                    // 15.5 = 15.5, 15.5.0 = 15.5, 15.5 = 15.5.0
                    if (rvItems.length == i ||
                        ((rvItems.length == i+1) && (rvItems[i].equals("0"))) ||
                        ((vItems.length == i+1) && (vItems[i].equals("0")))
                       ) {
                            //LOG.warn("found match");
                            refVersion = refVer;
                            break;
                    }
				}
				// else continue
			}
			else {
				if (ret >= 0) {
				    // not exact match, find one, equal or greater than
				    //LOG.warn("ret = 1 - ret is " + ret);
				    refVersion = refVer;
				    break;
				}
			}
		}
		//LOG.warn("final result ret is " + ret);
		return ret;
	}

	/**
	 * @param datasource
	 * @param con
	 * @return The latest patch data for the template / datasource
	 * @throws DataSourceConnectException 
	 */
	
	/*
	public String findCasPatchData(Datasource datasource, int testId, long templateId) throws DataSourceConnectException {
		String patch = DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
		String fullInfo = null;
		String version = null;

		List<CasAuditTarget> targets = CasAuditTargetPeer.findTargets(datasource);
		if (targets == null) {
			targets = new ArrayList<CasAuditTarget>();
		}

		List<CasAuditConfig> configs = new ArrayList<CasAuditConfig>();

		// Find all the alternative template ids for this test
		long[] allIds = TestAlternativeTemplatePeer.findAlternateTemplateIds(testId, templateId);
		for (int i = 0; i < allIds.length; i++) {
			for ( CasAuditTarget target : targets ) {
				List<CasAuditConfig> list = CasAuditConfigPeer.getConfigsByTargetAndTemplate(target, allIds[i]);
				configs.addAll(list);
			}
		}

		for ( CasAuditConfig config : configs) {
			patch = CasAuditStatePeer.getLatestAuditStateDatum(config, casErrorFlag);
			if ( !Check.isEmpty(patch) ) {
				if(datasource.isDB2()) {
					Matcher match = AbstractDatabaseVersionTest.DB2versionPattern.matcher(patch);
					if(! match.find()) 
						continue;
					else {
						patch = match.group();
						patch = patch.replaceAll("\\s+", " ");
					}
				}
				if(datasource.isTeradata()) {
					Matcher match = AbstractDatabaseVersionTest.TERADATAPattern.matcher(patch);
					if(! match.find()) 
						continue;
					else {
						fullInfo = match.group();
						version = match.group(1);
						patch = match.group(2);
					}
				}
				break;
			}
		}

		// update the datasource version history
		if ( !Check.isEmpty(patch) ) 
		{
			Connection con = null;
			try
			{
				con = datasource.getConnection();	
				fullInfo = (fullInfo == null) ? datasource.getFullVersionInfo(con) : fullInfo;
				version = (version == null) ? datasource.getVersionLevel(con) : version;
				DatasourceVersionHistory history = new DatasourceVersionHistory(
						datasource, fullInfo, patch, version
						);
				datasource.addVersionHistory(history);
			}
			finally
			{
				Check.disposal(con);
			}
			
		} else {
			patch = DatasourceVersionHistoryPeer.VERSION_UNKNOWN;
		}
		return patch;
	}
	*/
	
	private static Pattern DB2versionPattern = Pattern.compile("(?m)Informational tokens are .DB2 v\\d+\\.\\d+.*Fix\\D+\\d+\\D");
	/** constant pattern to check for content of just numbers and dots */
	protected static final Pattern PROD_VER_PATTERN = Regexer.compilePattern("\\d\\.\\d");
	/**
	 * Extracts the version information out of a Product version string that has
	 * extraneous information in it.
	 * Scrolls through all the words looking for a a word with just numbers and dots,
	 * like: <code>8.1.7.0.0</code>
	 * <dl>
	 * <dt>Typical Oracle Output:</dt>
	 * <dd>Oracle8i Enterprise Edition Release 8.1.7.0.0 - Production JServer Release 8.1.7.0.0 - Production</dd>
	 * <dt>Typical Sybase Output:</dt>
	 * <dd>Adaptive Server Enterprise/12.5.2/EBF 11795/P/HP9000-879/HP-UX 11.0/ase1252/1831/64-bit/FBO/Fri Apr  9 12:36:59 2004</dd>
	 * <dt>Typical MySql output:</dt>
	 * <dd>5.1.19-beta-community-nt-debug</dd>
	 * </dl>
	 * @param version The product version string.
	 * @param delim
	 * @return Only version number portion of the string. If the pattern is not found, the input.
	 */
	protected String parseProductVersion(String version, String delim) {
		String word;
		StringTokenizer tok = new StringTokenizer(version, delim);
		while ( tok.hasMoreTokens() ) {
			word = tok.nextToken();
			boolean match = Regexer.matchRegex(word, PROD_VER_PATTERN);
			if (match) {
				return word;
			}
		}
		// no hits, return the incoming version
		return version;
	}
	public static DataSourceInfoGeter initDatasourceInfoGetter(DatasourceType datasourceType) throws Exception
	{
		String className =  null;
		try
		{
			//String tmp = datasourceType.toString();
			String tmp = datasourceType.getName();
			// replace space to _, so we can use as key to find the right infogetter module
			tmp = tmp.replaceAll("\\s", "_");
			tmp = tmp + ".info-getter";
			//className = CVETest.cveAssessmentResources.getString(datasourceType.toString().replaceAll(" ","_")+".info-getter");
			className = CVETest.cveAssessmentResources.getString(tmp);
			Class <DataSourceInfoGeter> getterClass = (Class <DataSourceInfoGeter>) Class.forName(className);
			return (DataSourceInfoGeter) getterClass.newInstance();
		}
		catch (MissingResourceException rne)
		{
			return new GenericDatasourceInfoGetter();
		}
	}
	
	

}
