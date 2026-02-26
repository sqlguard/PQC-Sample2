/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Dec 30, 2003
 *
 * © Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material may not                        
 * be copied, modified, altered, published, distributed, or otherwise displayed without the                        
 * express written consent of Guardium, Inc. 
 * 
 */
package com.guardium.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.guardium.data.GroupType;
import com.guardium.map.AliasMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.GroupTypeMap;
import com.guardium.utils.AdHocLogger;

/**
 * @author dario
 *
 * Singleton, can be instantiated only once, and contains the aliases dictionary on a Map
 * for drill down and tabluar reports use.
 */
public class AliasDictionary {
	
	private static AliasDictionary aliasDictionary = null;
	private static Map translations = new HashMap();
	private boolean needSync = false;
	
	AliasMap AliasPeer = AliasMap.getAliasMapObject();

	
	//private static final AtomicBoolean alreadyRefresh = new AtomicBoolean(false);
	private AliasDictionary() {
	}

	public static AliasDictionary getAliasDictionary() {
		if (aliasDictionary == null) {
			aliasDictionary = new AliasDictionary();
		}
		return aliasDictionary;
	}
	
	public ArrayList <String> getDBValues(int groupType, String alias) {
		// reverse translation, return all the dbValues with an alias as the
		// given alias.
		ArrayList ret = new ArrayList<String>();
		Map dict = (Map)translations.get(new Integer(groupType));
		if (dict == null) {
			addtoTranslations(groupType);
			dict = (Map)translations.get(new Integer(groupType));
		}
		if (dict != null) {
			Object aliases[] = dict.values().toArray();
			Object dbValues[] = dict.keySet().toArray();
			for (int i=0 ; i < aliases.length ; i++) {
				if ( ((String) aliases[i]).equals(alias) ) {
					ret.add((String)dbValues[i]);
				}
			}
		}	
		return ret;
	}
	
	public String translate(int groupType, String dbValue) {
		String ret = null;
		Map dict = (Map)translations.get(new Integer(groupType));
		if (dict == null) {
			addtoTranslations(groupType);
			dict = (Map)translations.get(new Integer(groupType));
		}
		if (dict != null)
			ret = (String)dict.get(dbValue.toUpperCase());
		return ret;
	}
	
	public void addtoTranslations(int groupType) {
		Map m = AliasPeer.getAliases(groupType);
		translations.put(new Integer(groupType),m);	
		setNeedSync(true);
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setNeedSync(boolean sync) {
		this.needSync = sync;
	}

	public void refreshAll()
	{
		List types = GroupTypeMap.doSelectExclueInternal();
		for (Iterator iter = types.iterator(); iter.hasNext();) 
		{
			GroupType grt = (GroupType) iter.next();
			addtoTranslations(grt.getGroupTypeId());
		}
		
		
	}
	
	protected static void refreshCache(){
		//if(!alreadyRefresh.getAndSet(true))
		//{
		try {
			AliasDictionary.getAliasDictionary().refreshAll();
			AliasDictionary.getAliasDictionary().setNeedSync(false);
		} catch (Throwable e) {				
			AdHocLogger.logException(e);
		}
		//	alreadyRefresh.getAndSet(false);
		//}
		
	}
	
	public static void runCheckAndRefresh(){
		
		AdHocLogger.logDebug("COMPONENT:ALIAS running refresh",AdHocLogger.LOG_DEBUG);
		
		Thread a = new Thread(new Runnable(){
			public void run() {
				refreshCache();				
			}			
			},"RefreshAlias");
		a.start();
	}
}
