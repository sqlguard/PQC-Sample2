/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class TrustAllTrustManager implements X509TrustManager 
{
		   public void checkClientTrusted(
			   X509Certificate[] arg0, String arg1)
			   throws CertificateException
		   {
		   }

		   public void checkServerTrusted(
			   X509Certificate[] arg0,String arg1)
			   throws CertificateException
		   {
		   }

		   public X509Certificate[] getAcceptedIssuers()
		   {
			   return new X509Certificate[0];
		   }

}
