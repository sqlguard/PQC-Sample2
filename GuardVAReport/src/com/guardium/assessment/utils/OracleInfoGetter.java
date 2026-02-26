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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;

import com.guardium.assessment.tests.TestScore;
//import com.guardium.datamodel.dbSource.CASRequiredException;
import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceVersionHistory;
import com.guardium.date.ThreadSafe_SimpleDateFormat;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
//import com.ibm.xtq.ast.parsers.xpath.tempconstructors.ILetClause;


public class OracleInfoGetter extends DataSourceInfoGeter 
{
	//private static final transient Logger LOG = Logger.getLogger(DataSourceInfoGeter.class);

	private static final String OS_SQL = "select PLATFORM_NAME from v$database";
	private static final String OS_PATH_SQL = "select f.name from v$tablespace t, v$datafile f	where t.ts# = f.ts#	and t.name = \'SYSTEM\'";
	//private static final String VERSION_SQL = "select VERSION from V$INSTANCE";
	private static final ThreadSafe_SimpleDateFormat sdf = new ThreadSafe_SimpleDateFormat("MMMyyyy");
	private static final String PATCH_SQL = "select COMMENTS from SYS.REGISTRY$HISTORY " +
			"where ACTION_TIME =" +
			 	"(" +
			 		" select max(ACTION_TIME) " +
			 		" from SYS.REGISTRY$HISTORY " +
			 		"	where upper(ACTION) in ('CPU','APPLY') " +
			 		"      and (upper(COMMENTS) like 'CPU%' or upper(COMMENTS) like 'PATCH%' or upper(COMMENTS) like 'PSU%' or upper(COMMENTS) like '%BP%' " +
			 		"      or upper(COMMENTS) like 'WINBUNDLE%'" +
			" /* see note #1 */" +
				")" +
			" and upper(COMMENTS) not like 'PATCHSET%' " +
			" /* see note #2 */" +
			" and version = " +
				" (select  substr(version,1,length(version) -2) from v$instance)" +
			" /* see note #3 */" +
			" ) ";

    private static final String NEW_PATCH_SQL = "" +
            "SELECT VERSION, FLAGS, DESCRIPTION, BUNDLE_ID, BUNDLE_SERIES " +
            "FROM DBA_REGISTRY_SQLPATCH " +
            "WHERE ACTION = 'APPLY' " +
            "AND STATUS = 'SUCCESS' " +
            "AND VERSION = (SELECT  SUBSTR(VERSION,1,LENGTH(VERSION) -2) FROM V$INSTANCE) " +
        "    AND FLAGS LIKE '%B%' " +
            "AND ACTION_TIME = (SELECT MAX(ACTION_TIME) " +
         "                  FROM DBA_REGISTRY_SQLPATCH " +
         "                  WHERE ACTION = 'APPLY' " +
         "                  AND STATUS = 'SUCCESS' " +
         "                  AND VERSION = (SELECT  SUBSTR(VERSION,1,LENGTH(VERSION) -2) FROM V$INSTANCE) " +
         "                  AND FLAGS LIKE '%B%') ";

/*	"select COMMENTS from SYS.REGISTRY$HISTORY  " +
			"where ACTION_TIME = (select	max(ACTION_TIME) from SYS.REGISTRY$HISTORY where upper(ACTION) in" +
			" ('CPU','APPLY') and (upper(COMMENTS) like 'CPU%' or upper(COMMENTS) like 'PATCH%' or upper(COMMENTS) like 'PSU%'))";*/
//"select COMMENTS from SYS.REGISTRY$HISTORY where ACTION_TIME = (select max(ACTION_TIME) from SYS.REGISTRY$HISTORY where upper(ACTION) in ('CPU','APPLY') and (upper(COMMENTS) like 'CPU%' or upper(COMMENTS) like 'PATCH%'))";
	

    private static final String PATCH_SQL_OJVM = "select VERSION from SYS.REGISTRY$HISTORY " +
            " where ACTION_TIME = " +
            " ( " +
            " select max(ACTION_TIME) " +
            " from SYS.REGISTRY$HISTORY " +
            " where ACTION = 'jvmpsu.sql' " +
            ")";

    private static final String NEW_PATCH_SQL_OJVM = "" +
            "SELECT VERSION, FLAGS, DESCRIPTION, BUNDLE_ID, BUNDLE_SERIES " +
            "FROM DBA_REGISTRY_SQLPATCH " +
            "WHERE ACTION = 'APPLY'  " +
            "AND STATUS = 'SUCCESS' " +
            "AND VERSION = (SELECT  SUBSTR(VERSION,1,LENGTH(VERSION) -2) FROM V$INSTANCE) " +
            "    AND FLAGS LIKE '%J%' " +
            "AND ACTION_TIME = (SELECT MAX(ACTION_TIME) " +
            "                   FROM DBA_REGISTRY_SQLPATCH  " +
            "                   WHERE ACTION = 'APPLY' " +
            "                   AND STATUS = 'SUCCESS' " +
            "                   AND VERSION = (SELECT  SUBSTR(VERSION,1,LENGTH(VERSION) -2) FROM V$INSTANCE) " +
            "                   AND FLAGS LIKE '%J%') ";


	


	public String getOs(Datasource ds) throws Exception
	{
		Statement stmt = null;
		ResultSet res = null;
		Connection connection = ds.getConnection();
		try
		{
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			try
			{
				res = stmt.executeQuery(OS_SQL);
				if ( res.next() ) 
				{
					os = res.getString(1);
				}
				return os;
			}
			catch (SQLException se)
			{	
				String path = null;
				res = stmt.executeQuery(OS_PATH_SQL);
				if ( res.next() ) 
				{
					path = res.getString(1);
					os = path.contains("/")?"Unix":"Windows";
				}
				return os;
			}

		} 
		finally
		{
			Check.disposal(res);
			Check.disposal(stmt);
			Check.disposal(connection);			
		}
	}








	@Override
    public String getDbPatchLevel(Datasource ds) throws Exception
    {
            String msg = "OracleInfoGetter getDbPatchLevel ";
            //LOG.warn(msg + " start");

            String ret = null;
            String patch = null;
            Connection con = null;
            Statement stmt = null;
            ResultSet res = null;
            String query = "";
            boolean use_newquery = false;

            String version = ds.getVersionLevel();
            boolean flag = getOjvm();

            //LOG.warn(msg + " version " + version + " flag " + flag);

            // if version is equal or later 12.1.0.2
    String checkVersion = "12.1.0.2";
            if (isLaterVersion (checkVersion, version)) {
                    // it is equal or later 12.1.0.2, need to do more check
                    query = NEW_PATCH_SQL;
                    if (flag ) {
                            query = NEW_PATCH_SQL_OJVM;
                    }
                    use_newquery = true;
            }
            else {
                query = PATCH_SQL;
                if (flag ) {
                    query = PATCH_SQL_OJVM;
                }
            }

            try
            {
                    con = ds.getConnection();
                    if (! use_newquery) {
                        patch = ret = getOneTryValue(con, query, 1);
                        //LOG.warn(msg + " use old query to get patch " + patch);
                    }
                    else {
                            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                            res = stmt.executeQuery(query);
                            //LOG.warn(msg + " use new query to get patch ");

                            if ( res.next() )
                            {
                                    // SELECT VERSION, FLAGS, DESCRIPTION, BUNDLE_ID, BUNDLE_SERIES
                                    String ver = res.getString(1);
                                    String flags = res.getString(2);
                                    String des = res.getString(3);
                                    String bundle_id  = res.getString(4);
                                    String bundle_series  = res.getString(5);

                                    /*
                                    LOG.warn(msg + " use new query to get patch res 1 " + ver);
                                    LOG.warn(msg + " use new query to get patch res 2 " + flags);
                                    LOG.warn(msg + " use new query to get patch res 3 " + des);
                                    LOG.warn(msg + " use new query to get patch res 4 " + bundle_id);
                                    LOG.warn(msg + " use new query to get patch res 5 " + bundle_series);
									*/
                                    
                                    String version_pattern = "^([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(\\.[0-9]+)$";

                                    // WINDOWS ORACLE JAVAVM COMPONENT BUNDLE PATCH 12.1.0.2.160119(64bit):22311086
                                    //String ojvm_pattern = "^(.* ORACLE JAVAVM COMPONENT BUNDLE PATCH )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";
                                    String ojvm_pattern_1 = "^(.* ORACLE JAVAVM COMPONENT.* )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";
                                    // Database PSU 12.1.0.2.160119, Oracle JavaVM Component (Jan2016)
                                    String ojvm_pattern_2 = "^(.* )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*ORACLE JAVAVM COMPONENT.*)$";

                                    // Database Patch Set Update : 12.1.0.2.160119 (21948354)
                                    // bundle_id - 160119
                                    // bundle_series - PSU
                                    //String psu_pattern = "^(Database Patch Set Update : )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";
                                    //String psu_pattern = "^(Database Patch Set Update.* )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";

                                    // DATABASE BUNDLE PATCH: 12.1.0.2.160119 (21949015)
                                    //String dbbp_pattern = "^(DATABASE BUNDLE PATCH: )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";            
                                    
                                    //String dbbp_pattern = "^(DATABASE BUNDLE PATCH.* )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";
                                    // WINDOWS DB BUNDLE PATCH 12.1.0.2.160119(64bit):22310559 
                                    //String winbundle_pattern = "^(WINDOWS DB BUNDLE PATCH )([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(.*)$";

                                    Pattern pattern;
                                    Matcher matcher;

                                    pattern = Pattern.compile(version_pattern);
                                    matcher = pattern.matcher(version);

                                    String real_version = version;
                                    if (matcher.find()) {
                                            String r1 = matcher.group(1);
                                            String r2 = matcher.group(2);
                                            real_version = r1;
                                    }

                                    if (flag) {
                                            pattern = Pattern.compile(ojvm_pattern_1, Pattern.CASE_INSENSITIVE);
                                            matcher = pattern.matcher(des);

                                            if (matcher.find()) {
                                                    String r1 = matcher.group(1);
                                                    String r2 = matcher.group(2);
                                                    String r3 = matcher.group(3);

                                                    patch = r2;
                                                    // 12.1.0.2.160119OJVMBP
                                                    patch = patch + "OJVMBP";
                                                    ret = patch;
                                            }
                                            else {
                                                    pattern = Pattern.compile(ojvm_pattern_2, Pattern.CASE_INSENSITIVE);
                                                    matcher = pattern.matcher(des);

                                                    if (matcher.find()) {
                                                            String r1 = matcher.group(1);
                                                            String r2 = matcher.group(2);
                                                            String r3 = matcher.group(3);

                                                            patch = r2;
                                                            // 12.1.0.2.160119OJVMBP
                                                            patch = patch + "OJVMBP";
                                                            ret = patch;
                                                    }
                                            }
                                    }
                                    else {
                                            // use bundle_id and bundle_series to format the patch string
                                            String psu_str = "PSU";
                                            String dbbp_str = "DBBP";
                                            String winbundle_str = "WinBundle";

                                            if (bundle_series.equalsIgnoreCase(psu_str)) {
                                                    // des has window, then change to WinBundle
                                                    String des_lower = des.toLowerCase();
                                                    int window_idx = des_lower.indexOf("window");
                                                    if (window_idx >= 0) {
                                                            // found window
                                                            // WinBundle 12.1.0.2.160119
                                                            patch = winbundle_str + " " + real_version + "." + bundle_id;
                                                            ret = patch;
                                                    }
                                                    else {
                                                            // PSU 12.1.0.2.160119
                                                            patch = psu_str + " " + real_version  + "." + bundle_id;
                                                            ret = patch;
                                                    }
                                            }
                                            else if (bundle_series.equalsIgnoreCase(dbbp_str)) {
                                                    // 12.1.0.2.160119DBBP
                                                    patch = real_version + "." + bundle_id + dbbp_str;
                                                    ret = patch;
                                            }
                                    }
                            }
                            /*
                            else {
                                LOG.warn(msg + " use new query to get patch res is null ");
                            }
                            */
                }
                if(patch == null) {
                        ret = "PATCH0";
                        patch = "NO Patch";
                }
                // TODO - later
                //ds.addVersionHistory(new DatasourceVersionHistory(ds, ds.getFullVersionInfo(),patch ,ds.getVersionLevel()));
        }
        catch (SQLException e) {
                // connection error

        }
        finally
        {
                Check.disposal(res);
                Check.disposal(stmt);
                Check.disposal(con);
        }

        return ret;
    }

	@Override
	public String getDbPatch(Datasource ds) throws Exception {
		// TODO Auto-generated method stub
		return getDbPatchLevel(ds);
	}








	@Override
	public String getDbPatchLevel(Datasource ds, Connection con)
			throws DataSourceConnectException
	{
        String ret = null;
        String query = PATCH_SQL;
        boolean flag = getOjvm();
        if (flag ) {
                query = PATCH_SQL_OJVM;
        }

        try
        {
                ret = getOneTryValue(con, query, 1);
                if(ret == null)
                        ret = "PATCH0";
        }
        catch (SQLException e)
        {
                //System.out.println("CAS required");
                throw new DataSourceConnectException();
        }
        finally
        {
                Check.disposal(con);
        }
        return ret;
	}



public static void main(String[] args) 
{
	DatasourceType dst = new DatasourceType();
	dst.setName("ORACLE");
	try {
		DataSourceInfoGeter dsig = 	DataSourceInfoGeter.initDatasourceInfoGetter(dst);
		dsig.refVersion = "12.1.0.1";
		List l = new ArrayList();
		l.add("CPUApr2010");
		l.add("PSU 11.2.0.1.1");
		l.add("PSU 11.1.0.3");
		l.add("Bp 27");
		l.add("WinBundle 11.2.0.1.3");
		//l.add("WinBundle 12.1.0.1.#");
		l.add("11.2.0.3 BP 28");
		l.add("PATCH29");
		l.add("11.2.0.1 BP 28");
		l.add("PSU 12.1.0.1.2");
		
		//int i = dsig.evaluateDbPatch(l, "WinBundle 11.2.0.2.9");
		//int i = dsig.evaluateDbPatch(l, "11.2.0.1 BP 27");
		//int i = dsig.evaluateDbPatch(l, "PSU 12.1.0.1.1");
		//int i = dsig.evaluateDbPatch(l, "28");
		String version = ""; 		//dsig.getVersionLevel();
		int i = dsig.evaluateDbPatch(l, "WinBundle 11.2.0.3.3", version);
		System.out.println(i);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}


	/**
	 * Oracle patches has 4 different notations:
	 * Patch6 or patch 6
	 * CPUApr2010 - is CPU issued on April 2010
	 * psu 11.2.0.1.3 is the third psu for version 11.2.0.1
	 * BP7 
	 * 
	 * Per Louis' comment on bug 23938 there are a few psu versions that are equivalent to CPU patches but are not documented in CVE
	 * for these I added method replacePsuWithEquivalentCpu(patch);
	 * If the database patch level is psu... but no psu patch level is found in the fix reference list I replace the psu patch number with the equivalent cpu patch and re-evaluate
	 *  
	 */
	@Override
	public int evaluateDbPatch(List<String> referencePatches, String patch, String version) 
	{
        //LOG.warn("Oracle evaluate patch " + patch);
        int ipatch =0;

        // Return value from this routine:
        //  1  - pass
        // -1  - fail
        // -15 - not detected
        int ret_pass = 1;
        int ret_fail = -1;
        int ret_not_detected = TestScore.UNSUPPORT_CVE_PATCH_DETECTED.getScoreValue();  //-15;
//int ret_special_error = TestScore.SPECIAL_ERROR.getScoreValue();                                      // -6

        // default should be not detected, means not in the supported list.
        // if found the list, then will compare, and return pass or fail.
        int ret = ret_not_detected;
        //int ret = -1;
        //int ret = ret_fail;

        // Supported patch type list:
        // 
        // patch start with versionI d
        //    11.2.0.3 BP 28
        //    12.1.0.2.4DBBP

        // patch start with PSU
        //    PSU 11.1.0.7.18

        // patch start with winbundle
        //    WinBundle 12.1.0.1.3

        // patch start CPU - evaluate patch as a date 
        //    CPUJAN2014

        // patch starts with BP - evaluate patch number
        //    BP3 
        // patch start with "PATCH"
        //    PATCH5

        // patch star with all number
        //    2

        /* Support patch type examples:
        
        11.1.0.7 | CPUJAN2014           |
        11.1.0.7 | PATCH55              |
        11.1.0.7 | PSU 11.1.0.7.18      |

        11.2.0.3 | BP22                 |
        11.2.0.3 | CPUJAN2014           |
        11.2.0.3 | PATCH28              |
        11.2.0.3 | PSU 11.2.0.3.9       |

        11.2.0.4 | BP3                  |
        11.2.0.4 | CPUJAN2014           |
        11.2.0.4 | PATCH1               |
        11.2.0.4 | PSU 11.2.0.4.1       |
        11.2.0.4 | WinBundle 11.2.0.4.1 |

        12.1.0.1 | PSU 12.1.0.1.2       |
        12.1.0.1 | WinBundle 12.1.0.1.3 |
        12.1.0.1 | 12.1.0.1.4DBBP       |
        
        12.1.0.1 | 12.1.0.1.1OJVMBP
        
        */

        String winbundle_str = "winbundle";
        int winbundle_len = winbundle_str.length();

        String patch_str = "patch";
        int patch_len = patch_str.length();

        String bp_str = "BP";
        String dbbp_str = "DBBP";
        String ojvm_str = "OJVM";

        // Basic logic is to use the input patch pattern to find the same reference patch 
        // with the same pattern, and compare with the patch number.

        //if patch is not all numeric
        if(!Pattern.matches("\\d+", patch.trim()))
        {
                // if patch start with version
                // 11.2.0.3 BP 28
            // 12.1.0.2.4DBBP
                boolean bp_type = false;
                boolean dbbp_type = false;
                boolean ojvm_type = false;

                //LOG.warn("refVersion is " + refVersion);
                if(patch.startsWith(refVersion))
                {
                        int irefP = -1;
                        //LOG.warn("patch start with refVersion " + refVersion);

                        String [] strs = patch.split("\\s+");
                        int len = strs.length;
                        if (len == 3 && strs[1].equalsIgnoreCase("BP")) {
                                // 11.2.0.1 BP 28
                                ipatch = Integer.parseInt(strs[2]);
                                bp_type = true;
                        }
                        else if (len == 1 && strs[0].endsWith(dbbp_str)) {
                            // 12.1.0.2.4DBBP
                            String tmp = strs[0];
                            int tlen = tmp.length();
                            tlen = tlen - dbbp_str.length();
                            int sind = refVersion.length()+1;
                            tmp = tmp.substring(sind, tlen);
                            if (!tmp.isEmpty()) {
                                    ipatch = Integer.parseInt(tmp);
                                    dbbp_type = true;
                            }
                            else {
                                    // not fit the pattern
                                    //ret = ret_special_error;
                                    ret = ret_not_detected;
                                    return ret;
                            }
                    }
                    else if ((len == 1) && (strs[0].indexOf(ojvm_str) > 0)) {
                            // 12.1.0.2.4OJVMBP
                            // 12.1.0.1.160419OJVMPSU
                            // try to get patch number 4
                            Pattern pattern = Pattern.compile("^([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\.([0-9]+)(.*)$");
                            Matcher matcher = pattern.matcher(strs[0]);

                            if (matcher.find()) {
                                // it's ok
                                    //System.out.println(matcher.group(1));
                                    String r1 = matcher.group(1);
                                    String r2 = matcher.group(2);
                                    String r3 = matcher.group(3);

                                    if (r1.equals(refVersion)) {
                                            ipatch = Integer.parseInt(r2);
                                            ojvm_type = true;
                                    }
                                    else {
                                            // not fit the pattern
                                            //ret = ret_special_error;
                                            ret = ret_not_detected;
                                            return ret;
                                    }
                            }
                    }
                    else {
                            // not fit the pattern
                            // ret = ret_special_error;
                            ret = ret_not_detected;
                            return ret;
                    }

                    for (String refP : referencePatches)
                    {
                            // BP can be in 2 cases:
                            // 11.2.0.3 BP 28
                            // 12.1.0.2.4DBBP
                            if(refP.startsWith(refVersion))
                            {
                                    // split by space
                                    strs = refP.split("\\s+");
                                    len = strs.length;

                                if (len == 3 && strs[1].equalsIgnoreCase("BP") ) {
                                    if (bp_type) {
                                            // only compare with the BP pattern
                                            irefP = Integer.parseInt(strs[2]);
                                                if(irefP <= ipatch) {
                                                    ret = ret_pass;
                                                    return ret;
                                                }
                                                else {
                                                    ret = ret_fail;
                                                    return ret;
                                                }
                                    }
                                }
                                else if (len == 1 && strs[0].endsWith(dbbp_str)) {
                                    if ( dbbp_type ) {
                                            // 12.1.0.2.4DBBP
                                            String tmp = strs[0];
                                            int tlen = tmp.length();
                                            tlen = tlen - dbbp_str.length();
                                            int sind = refVersion.length()+1;
                                            tmp = tmp.substring(sind, tlen);
                                            if (!tmp.isEmpty()) {
                                                    // try to get the patch number before DBBP
                                                    irefP = Integer.parseInt(tmp);
                                                    if(irefP <= ipatch) {
                                                            ret = ret_pass;
                                                            break;
                                                    }
                                                    else {
                                                            ret = ret_fail;
                                                            break;
                                                    }
                                            }
                                            else {
                                                    // not fit the pattern
                                                    ret = ret_not_detected;
                                            }
                                    }
                                    }
                                else if ((len == 1) && (strs[0].indexOf(ojvm_str) > 0)) {
                                    if ( ojvm_type ) {
                                            // 12.1.0.2.4OJVMBP
                                            // 12.1.0.2.160419OJVMPSU
                                                    Pattern pattern = Pattern.compile("^([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\.([0-9]+)(.*)$");
                                                    Matcher matcher = pattern.matcher(strs[0]);

                                                    if (matcher.find()) {
                                                        // it's ok
                                                            //System.out.println(matcher.group(1));
                                                            String r1 = matcher.group(1);
                                                            String r2 = matcher.group(2);
                                                            String r3 = matcher.group(3);

                                                            if (r1.equals(refVersion)) {                                                     
                                                            // try to get the patch number before OJVMBP
                                                            irefP = Integer.parseInt(r2);
                                                            if(irefP <= ipatch) {
                                                                    ret = ret_pass;
                                                                    break;
                                                            }
                                                            else {
                                                                    ret = ret_fail;
                                                                    break;
                                                            }
                                                            }
                                                            else {
                                                                    // not fit the pattern
                                                                    //ret = ret_special_error;
                                                                    ret = ret_not_detected;
                                                            }
                                                    }
                                    }
                                }
                            else {
                                // not fit the pattern
                                ret = ret_not_detected;
                            }
                        }
                }

                return ret;
        }

        // patch start with PSU
        // if patch is a psu patch evaluate last digit of version
        // eg. PSU 12.1.0.1.2
        if(patch.length() > 3 && patch.substring(0,3).equalsIgnoreCase("psu"))
        {
                boolean foundPsuRef = false;
                patch = patch.replaceFirst("(?m)^\\D*", "").trim();
                List <String> l = new ArrayList<String>(referencePatches.size());
                for (String s : referencePatches)
                {
                        if(s.length() > 3 && s.substring(0,3).equalsIgnoreCase("psu"))
                        {
                                foundPsuRef = true;
                                s = s.replaceFirst("(?m)^\\D*", "").trim();
                                if(s.startsWith(refVersion))
                                        l.add(s);
                        }
                }
                if(foundPsuRef)
                {
                        if(evaluateDbVersion(l, patch) > -1)
                                return ret_pass;
                        else
                                return ret_fail;
                }
                else
                {
                        try
                        {
                                patch = replacePsuWithEquivalentCpu(patch);
                        }
                        catch (Exception e)
                        {
                                AdHocLogger.logException(e);
                                // LOG.error("exception - ", e);
                                return ret_not_detected;
                        }
                }
                // need to continue because patch has changed to cpu
        }

        // patch start with winbundle
        // if patch is a winbundle patch evaluate last digit of version
        // eg. WinBundle 12.1.0.1.#
        if(patch.length() > winbundle_len && patch.substring(0,winbundle_len).equalsIgnoreCase(winbundle_str))
        {
                boolean foundWinbRef = false;
                patch = patch.replaceFirst("(?m)^\\D*", "").trim();
                List <String> l = new ArrayList<String>(referencePatches.size());
                for (String s : referencePatches)
                {
                        if(s.length() > winbundle_len && s.substring(0,winbundle_len).equalsIgnoreCase(winbundle_str))
                        {
                                foundWinbRef = true;
                                s = s.replaceFirst("(?m)^\\D*", "").trim();
                                if(s.startsWith(refVersion))
                                        l.add(s);
                        }
                }
                if(foundWinbRef)
                {
                        if(evaluateDbVersion(l, patch) > -1)
                                ret = ret_pass;
                        else
                                ret = ret_fail;
                }
                /*
                 * I don't think we need this code for winbundle
                else
                {
                        try
                        {
                                patch = replacePsuWithEquivalentCpu(patch);
                        }
                        catch (InvalidArgumentException e) 
                        {
                                AdHocLogger.logException(e);
                                LOG.error("exception - ", e);
                                return ret;
                        }
                }
                */
                return ret;
        }

        // patch start with CPU - evaluate patch as a date 
        // eg. CPUApr2010 is older then CPUApr2011
        if(patch.length() > 3 && patch.substring(0,3).equalsIgnoreCase("cpu"))
        {
                try
                {
                    Date pdate = sdf.parse(patch.substring(3));
                    for (String refP : referencePatches)
                    {
                            if(refP.length() > 3 && refP.substring(0,3).equalsIgnoreCase("cpu"))
                            {
                                    Date refPdate =  sdf.parse(refP.substring(3));
                                    if(!pdate.before(refPdate))
                                            ret = ret_pass;
                                    else
                                            ret = ret_fail;

                                    break;
                            }
                    }
            }
            catch (ParseException e)
            {
                    AdHocLogger.logException(e);
                    // LOG.error("exception - ", e);
            }
            return ret;
    }

    // patch starts with BP  - evaluate patch number - 
    // eg. BP6 > BP5
    if(patch.length() > 2 && patch.substring(0,2).equalsIgnoreCase("BP"))
    {
            int irefP = -1;
            ipatch = Integer.parseInt(patch.substring(2).trim());
            for (String refP : referencePatches)
            {
                    if(refP.length() > 2 && refP.substring(0,2).equalsIgnoreCase("BP"))
                    {
                            irefP = Integer.parseInt(refP.substring(2).trim());
                            if(irefP <= ipatch)
                                ret = ret_pass;
                        else
                                ret = ret_fail;

                        break;
                }
        }

        // if no BP in reference patch list
        return ret;
    }
    
    // patch start with "PATCH" 
    // if patch starts with patch evaluate patch number - 
    // eg. patch6 > Patch5
    if(patch.length() > 5 && patch.substring(0,5).equalsIgnoreCase("patch"))
    {
            try
            {
                    ipatch = Integer.parseInt(patch.substring(5).trim());

                    for (String refP : referencePatches)
                    {
                            if(refP.length() > patch_len && refP.substring(0,patch_len).equalsIgnoreCase(patch_str))
                            {
                                    int irefP = Integer.parseInt(refP.substring(patch_len).trim());
                                    if(irefP <= ipatch)
                                            ret = ret_pass;
                                    else
                                            ret = ret_fail;
                                    break;
                            }
                    }
            }
            catch (Exception e)
            {
                    AdHocLogger.logException(e);
                    //LOG.error("exception - ", e);
            }

            return ret;
    	}
    }
    else
    {
      	// patch is all digit
        // eg, 1 or 2
        try
        {
                ipatch = Integer.parseInt(patch.trim());

                for (String refP : referencePatches)
                {
                        // find the all digit one
                        if(Pattern.matches("\\d+", refP.trim()))
                        //if(refP.length() > patch_len && refP.substring(0,patch_len).equalsIgnoreCase(patch_str))
                        {
                                int irefP = Integer.parseInt(refP);
                                if(irefP <= ipatch)
                                        ret = ret_pass;
                                else
                                        ret = ret_fail;
                                break;
                        }
                }
        }
        catch (Exception e)
        {
                AdHocLogger.logException(e);
                //LOG.error("exception - ", e);
        }
    }

    return ret;
    }
    

	/**
	 * For bug 23938 - if the database returns a patch level of PSU type but the test is old and does not have PSU patches 
	 * in the fix list then translate the PSU to the equivalent CPU patch. 
	 * @param patch
	 * @return
	 * @throws InvalidArgumentException
	 */
	private String replacePsuWithEquivalentCpu(String patch) throws Exception	// InvalidArgumentException 
	{
		if(patch.startsWith("10.2.0.4.") && !patch.endsWith("0"))
			return "CPUJul2009";
		else if(patch.startsWith("10.2.0.5.") && !patch.endsWith("0"))
				return "CPUOct2010";
		else if(patch.startsWith("11.1.0.7.") && !patch.endsWith("0"))	
			return "CPUOct2009";
		else if(patch.startsWith("11.2.0.1.") && !patch.endsWith("0"))
			return "CPUApr2010";
		else if(patch.startsWith("11.2.0.2.") && !patch.endsWith("0"))
			return "CPUApr2011";
		else
			//throw new InvalidArgumentException("No Equivalent CPU for "+ patch);
			throw new Exception("No Equivalent CPU for "+ patch);
	}

	protected long[] getAllIds() 
	{
		long [] l =  {(long)764, (long)851};
		return l;
	}
	
    public boolean isLaterVersion (String sversion, String cversion) {
        boolean ret = false;
        // sversion is the standard version
        // cversion is the version we want to check if it is equal or later than standard version
        // return true if verersion is equal to later than the standard version, else return false
        // oracle version is like 12.0.1.2

        String [] sstr = sversion.split("\\.");
        String [] cstr = cversion.split("\\.");

        for (int i = 0; i< 4; i++) {
            int ci = Integer.parseInt(cstr[i]);
            int si = Integer.parseInt(sstr[i]);
            if (ci > si) {
                    ret = true;
                    return ret;
            }
            else {
                    if (ci < si) {
                            ret = false;
                            return ret;
                    }
                    else {
                        ret = true;
                    }
            }
        }
        return ret;
    }

}
