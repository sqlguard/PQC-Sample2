/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

import java.util.HashMap;
import java.util.List;

import com.guardium.data.AvailableTest;
import com.guardium.data.SqlbasedAssessmentDefinition;
import com.guardium.gui.TestUtils;
import com.guardium.gui.VATest;
import com.guardium.utils.ReadDumpFile;

public class SqlbasedAssessmentDefinitionMap {
	
	private static SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private SqlbasedAssessmentDefinitionMap () {
		initMap();
	}
		
	public static synchronized SqlbasedAssessmentDefinitionMap getSqlbasedAssessmentDefinitionMapObject() {
		if (SqlbasedAssessmentDefinitionMapObject == null) {
			SqlbasedAssessmentDefinitionMapObject = new SqlbasedAssessmentDefinitionMap();
		}
		return SqlbasedAssessmentDefinitionMapObject;
	}
		
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	// Create a hash map
	private static HashMap hm = new HashMap();

	
	public int getMapSize() {
		return hm.size();
	}

	public SqlbasedAssessmentDefinition findByTestId (int id) {
		return getSqlbasedAssessmentDefinitionById(id);
	}
	
	public SqlbasedAssessmentDefinition getSqlbasedAssessmentDefinitionById(int key) {
		if (hm.containsKey(key)) {
			SqlbasedAssessmentDefinition t = (SqlbasedAssessmentDefinition) hm.get(key);
			return t;
		}
		else {
			return null;
		}
	}
	
	private static void initMap() {
		// Read data from the xml dump
		String  resourceFile = "sqlbased.dump";
		TestUtils tutils = new TestUtils();
		String fileName = tutils.readDataFile(resourceFile);
		if (fileName.isEmpty()) {
			return;
		}
		
		ReadDumpFile rdf = new ReadDumpFile();
		boolean readok = rdf.readFile(fileName);
		tutils.removeDataFile(fileName);
		if (!readok) {
			return;
		}

		List<List> tbList = rdf.getTableList();
		String tbName = rdf.getTableName();

		for (List rlist : tbList) {
			//System.out.println("rlist size " + rlist.size());

			// init all fields
			int sid = -1;  
			int tid = -1;
			String rcpass = "";
			String rcfail = "";
		    String rspass = "";
		    String rsfail = "";
		    String sql = "";
		    String returntype = ""; 
		    int op_id = 0;
		    String compval = "";
		    boolean call_flag = false;
		    String detail = "";
		    String dsql = "";
		    String pre_sql = "";
		    String pre_msg = "";
		    boolean loop_flag = false; 
		    String db = "";
		

			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String[]) rlist.get(i);
				String name = null;
				String value = null;
				name = str[0];
				value = str[1];
				// System.out.println(name + "=" + value);

				/*
						sqlbased_assessment_definition_id=2478
						test_id=2478
						recommendation_text_pass=No action required.
						recommendation_text_fail=We recommend that you revoke view privileges granted with the grant option. We recommend that you restructure your view privilege grants so that they do not include the grant option. If you need to exclude certain grantees or view privileges that require the grant option in your environment, you can create an exception group and populate it with the names of authorized grantees and/or objects, and link your group to this test.  You can revoke grant option via a command similar to this example: REVOKE ALL ON <schema_name>.<view_name> FROM <grantee>.
						result_text_pass=View privileges have not been granted with the grant option, as recommended.
						result_text_fail=One or more view privileges have been granted with the grant option.
						sql_stmt=select count(*)
						from nc_system.nc_all_view_privs priv 
						join nc_system.nc_all_views v on priv.tableid = v.viewid 
						join nc_system.nc_all_schemas s on v.schemaid = s.schemaid
						where lower(priv.grantor) <> 'db_admin'   
						and priv.grantee <> 'db_admin'
						and priv.grantor <> priv.grantee
						and priv.grantee not in (select u.username from nc_system.nc_all_group_members m join nc_system.nc_all_roles r on m.groupid = r.roleid join nc_system.nc_all_users u on  m.memberid = u.userid where r.rolename = 'db_admin')
						and priv.isgrantable = 'true'
						return_type=I
						operator_id=8
						compare_to_value=0
						is_callable_statement=0
						detail_text=View with grant option privileges found:
						detail_sql=select 'granto				
				*/

				
				if (name.equals("sqlbased_assessment_definition_id")) {
					if (!value.isEmpty())
						sid = Integer.parseInt(value);
				} else if (name.equals("test_id")) {
					if (!value.isEmpty())
						tid = Integer.parseInt(value);
				} else if (name.equals("recommendation_text_pass")) {
					if (!value.isEmpty())
						rcpass = value;
				} else if (name.equals("recommendation_text_fail")) {
					if (!value.isEmpty())
						rcfail = value;
				} else if (name.equals("result_text_pass")) {
					if (!value.isEmpty())
						rspass = value;					
				} else if (name.equals("result_text_fail")) {
					if (!value.isEmpty())
						rsfail = value;
				} else if (name.equals("sql_stmt")) {
					if (!value.isEmpty())
						sql = value;					
				} else if (name.equals("return_type")) {
					if (!value.isEmpty())
						returntype = value;					
				} else if (name.equals("operator_id")) {
					if (!value.isEmpty())
						op_id = Integer.parseInt(value);
				} else if (name.equals("compare_to_value")) {
					if (!value.isEmpty())
						compval = value;						
				} else if (name.equals("is_callable_statement")) {
					if (!value.isEmpty()) {
						int tmp = Integer.parseInt(value);
						if (tmp == 0)
							call_flag = false;
						else
							call_flag = true;
					}
				} else if (name.equals("detail_text")) {
					if (!value.isEmpty())
						detail = value;
				} else if (name.equals("detail_sql")) {
					if (!value.isEmpty())
						dsql = value;
				} else if (name.equals("pre_test_check_sql")) {
					if (!value.isEmpty())
						pre_sql = value;
				} else if (name.equals("pre_test_fail_message")) {
					if (!value.isEmpty())
						pre_msg = value;
				} else if (name.equals("db_loop_flag")) {
					if (!value.isEmpty()) {
						int tmp = Integer.parseInt(value);
						if (tmp == 0)
							loop_flag = false;
						else
							loop_flag = true;
					}					
				} else if (name.equals("loop_databases")) {
					if (!value.isEmpty())
						db = value;
				} 
			}
									
			SqlbasedAssessmentDefinition t = new SqlbasedAssessmentDefinition(sid, tid, rcpass, rcfail,
					rspass, rsfail, sql, returntype, op_id, compval, call_flag,
				    detail, dsql, pre_sql, pre_msg, loop_flag, db);

			hm.put(sid, t);

		}

		/*
		System.out.println("table size is " + tbList.size());
		System.out.println("table name is " + tbName);
		System.out.println("hm size is " + hm.size());
		*/
	}

	/*
	 * SQLBASED_ASSESSMENT_DEFINITION.2014-05-06-11-40-00.csv
	 * 

SQLBASED_ASSESSMENT_DEFINITION_ID,TEST_ID,RECOMMENDATION_TEXT_PASS,RECOMMENDATION_TEXT_FAIL,RESULT_TEXT_PASS,RESULT_TEXT_FAIL,SQL,SQL_STMT,RETURN_TYPE,OPERATOR_ID,COMPARE_TO_VALUE,IS_CALLABLE_STATEMENT,DETAIL_TEXT,DETAIL_SQL,PRE_TEST_CHECK_SQL,PRE_TEST_FAIL_MESSAGE,DB_LOOP_FLAG,LOOP_DATABASES,UPDATED_BY_V9X


"2239","2239","No action required.","We recommend that you revoke READFILE authority from unauthorized grantees.  You can use this example command to revoke this authority: REVOKE READFILE FROM UNAUTHORIZE_GRANTEE.  To exclude authorized grantees from this test,you can populate an exception group with your authorized grantees and link the group to this test.","READFILE authority has not been granted to unauthorized grantee.","READFILE authority has been granted to unauthorized grantee.","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');      ^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');      ^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","I","8","0","0","Grantee with READFILE authority:","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  'Grantee=' || ^M\
           u.user_name || ^M\
           ' : Grantee_type=' ||^M\
           CASE^M\
               WHEN g.auth IS NULL THEN 'User'^M\
               ELSE 'Group'^M\
           END^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');   ^M\
ELSE^M\
  SELECT 'Guardium does not support this Sybase IQ release, Please upgrade to Sybase IQ 15 or higher to use Guardium VA test';^M\
END IF;^M\
^M\
END;","","","0","","1"


"2238","2238","No action required.","We recommend that you revoke READCLIENTFILE authority from unauthorized grantee.  You can use this example command to revoke: REVOKE READCLIENTFILE FROM UNAUTHORIZE_GRANTEE.  To exclude authorized grantees from this test,you can populate an exception group with your authorized grantees and link the group to this test.","READCLIENTFILE authority has not been granted to unauthorized grantees.","READCLIENTFILE authority has been granted to unauthorized grantees.","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READCLIENTFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');    ^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READCLIENTFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');    ^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","I","8","0","0","Grantee with READCLIENTFILE authority:","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  'Grantee=' || ^M\
           u.user_name || ^M\
           ' : Grantee_type=' ||^M\
           CASE^M\
               WHEN g.auth IS NULL THEN 'User'^M\
               ELSE 'Group'^M\
           END^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'READCLIENTFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');    ^M\
ELSE^M\
  SELECT 'Guardium does not support this Sybase IQ release, Please upgrade to Sybase IQ 15 or higher to use Guardium VA test';^M\
END IF;^M\
^M\
END;","","","0","","1"


"2237","2237","No action required.","We recommend that you revoke PROFILE authority from unauthorized grantees.  You can use this example command to revoke this authority: REVOKE PROFILE FROM UNAUTHORIZE_GRANTEE.  To exclude authorized grantees from this test,you can populate an exception group with your authorized grantees and link the group to this test.","PROFILE authority has not been granted to unauthorized grantees.","PROFILE authority has been granted to unauthorized grantees.","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'PROFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  COUNT(*)^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'PROFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');^M\
ELSE^M\
  SELECT 1;^M\
END IF;^M\
^M\
END;","I","8","0","0","Grantee with PROFILE authority:","BEGIN^M\
^M\
DECLARE  dbversion VARCHAR(255);^M\
DECLARE  dbnumber DECIMAL;^M\
^M\
SELECT @@VERSION INTO dbversion;^M\
^M\
IF SUBSTRING(dbversion,1,10) = 'Sybase IQ/'  THEN^M\
    SELECT SUBSTRING(dbversion,11,4) INTO dbnumber;^M\
ELSEIF SUBSTRING(dbversion,1,19) = 'Adaptive Server IQ/' THEN^M\
    SELECT SUBSTRING(dbversion,20,4) INTO dbnumber;^M\
ELSE^M\
    SELECT 0 INTO dbnumber;^M\
END IF;^M\
^M\
IF dbnumber >= 15.0 THEN^M\
   SELECT  'Grantee=' || ^M\
           u.user_name || ^M\
           ' : Grantee_type=' ||^M\
           CASE^M\
               WHEN g.auth IS NULL THEN 'User'^M\
               ELSE 'Group'^M\
           END^M\
   FROM SYS.SYSUSERAUTHORITY a^M\
   INNER JOIN SYS.SYSUSER u ON u.user_id = a.user_id^M\
   LEFT JOIN SYS.SYSUSERAUTHORITY g ON a.user_id = g.user_id AND g.auth = 'GROUP'^M\
   WHERE UPPER(a.auth) = 'PROFILE'^M\
   AND u.user_id NOT IN (SELECT user_id FROM SYS.SYSUSERAUTHORITY where auth = 'DBA');^M\
ELSE^M\
  SELECT 'Guardium does not support this Sybase IQ release, Please upgrade to Sybase IQ 15 or higher to use Guardium VA test';^M\
END IF;^M\
^M\
END;","","","0","","1"

	 * 
	 */
	
	

}
