/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
public class NullHostnameVerifier implements HostnameVerifier
{
	  static NullHostnameVerifier instance = new NullHostnameVerifier();
	  public boolean verify(String urlHostname,String certHostname) 
	  {
			return true;
	  }

	  public boolean verify(String arg0, SSLSession arg1) 
	  {
		return true;
	  }

	public static NullHostnameVerifier getInstance()
	{
		return instance;
	}

	public static void setInstance(NullHostnameVerifier instance)
	{
		NullHostnameVerifier.instance = instance;
	}
}