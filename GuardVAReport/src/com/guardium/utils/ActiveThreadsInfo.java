/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Jan 27, 2004
 *
 * ?? Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material may not                        
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
import java.util.WeakHashMap;

/**
 * @author dario
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ActiveThreadsInfo {
	
    public static final String USER_KEY = "user";
    public static final String JS_USER_KEY = "jsuser";
    public static final String AUDIT_RUN_NO = "run_no";
    public static final String AUDIT_THREAD = "audit_thread";
	private static ActiveThreadsInfo activeThreadsInfo = null;
	private static WeakHashMap threadsList = null;

	private ActiveThreadsInfo() {
	}

	public static ActiveThreadsInfo getActiveThreadsInfo() {
		if (activeThreadsInfo == null) {
			activeThreadsInfo = new ActiveThreadsInfo();
		}
		return activeThreadsInfo;
	}
	
	public void addThread(Thread t , String userName) 
    {
		putThreadProperty(t,USER_KEY, userName);
	}
	
	public void addAuditThread(Thread t , String userName) 
    {
		putThreadProperty(t,USER_KEY, userName);
		putThreadProperty(t,AUDIT_THREAD, AUDIT_THREAD);
	}
	
	private boolean isAuditThread(Thread t)
	{
		return (getThreadProperty(t,AUDIT_THREAD) != null);
	}
	public void removeAuditThread(Thread t) 
    {
		if(isAuditThread(t))
			threadsList.remove(t);
	}
	/**
	 * Remove dead threads - a safety method to cleanup orphans that might remain after user logged out and audit processes completed.
	 */
	public void clearDeadThreads()
	{
		List<Thread> toRemove = new ArrayList<Thread>(threadsList.size());
		for (Iterator iter = threadsList.keySet().iterator(); iter.hasNext();) 
		{
			Thread thread = (Thread) iter.next();
			if(!thread.isAlive())
				toRemove.add(thread);
		}
		for (int i = 0; i < toRemove.size(); i++)
		{
			threadsList.remove(toRemove.get(i));
		}	
	}
	/**
	 * remove all threads associated with a username, don't remove audit threads to allow audits to complete after session timeout
	 * called when a user loggs out
	 * @param userName
	 */
	public void removeUserThreads(String userName) 
    {
		List<Thread> toRemove = new ArrayList<Thread>(threadsList.size());
		for (Iterator iter = threadsList.keySet().iterator(); iter.hasNext();) 
		{
			Thread thread = (Thread) iter.next();
			// don't remove audit threads when user logs out
			if(userName.equals(getUserName(thread)) && !isAuditThread(thread))
			{
				toRemove.add(thread);
			}
		}
		for (int i = 0; i < toRemove.size(); i++)
		{
			threadsList.remove(toRemove.get(i));
		}
	}

	public String getUserName(Thread t) 
    {
		return (String)getThreadProperty(t, USER_KEY);
	}
	public Object getJSUser(Thread t) 
    {
		return getThreadProperty(t, JS_USER_KEY);
	}
    public void putJSUser(Thread t, Object jsUser)
    {
    	putThreadProperty(t,  JS_USER_KEY,jsUser);
    }
	
    public void putThreadProperty(String key, Object value)
    {
        putThreadProperty(Thread.currentThread(), key, value);
    }
    
    public void putThreadProperty(Thread t, String key, Object value)
    {
        if (threadsList == null) 
            threadsList = new WeakHashMap();
        System.gc();
        Map m = (Map)threadsList.get(t);
        if(m == null)
            m = new HashMap();
        m.put(key, value);
        threadsList.put(t,m);
    }
    
    public Object getThreadProperty(String key) 
    {
        return getThreadProperty(Thread.currentThread(), key); 
    }
    public Object getThreadProperty(Thread t, String key) {	
    	if(threadsList != null) {
    		Object ret = threadsList.get(t);
    		if(ret != null && ret instanceof Map) {
    			Map map = (Map) ret;
    			return map.get(key);
    		}
    	}
    	return null;
    }
    public static boolean isQuartzThread(){
    	try{
    	if(Thread.currentThread().getName().startsWith("QuartzWorkerThread"))
    		return true;
    	else
    		return false;
    	}catch(Exception e){
    		return false;
    	}
    }
}
