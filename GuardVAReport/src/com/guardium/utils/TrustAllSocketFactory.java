/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class TrustAllSocketFactory extends SSLSocketFactory 
{
	/*
	static
	{
		   AdHocLogger.logDebug("TrustAllSocketFactory loading", AdHocLogger.LOG_DEBUG);
	}
	*/
	 private SSLSocketFactory factory;
	 
		protected static KeyManager[] getKeyManagers()
		  throws IOException, GeneralSecurityException
		{
		  // First, get the default KeyManagerFactory.
		  String alg=KeyManagerFactory.getDefaultAlgorithm();

		  KeyManagerFactory kmFact=KeyManagerFactory.getInstance(alg);
		    
		  // Next, set up the KeyStore to use. We need to load the file into
		  // a KeyStore instance.
		  
		  KeyStore ks=KeyStore.getInstance("jks");
		  ks.load(null,null);
		  kmFact.init(ks, null);
		 
		  // And now get the TrustManagers
		  KeyManager[] kms=kmFact.getKeyManagers();

		  return kms;
		}
		
	 public TrustAllSocketFactory() 
	 {
	     //AdHocLogger.logDebug("TrustAllSocketFactory instantiating", AdHocLogger.LOG_DEBUG);
	      try 
	      {
	        SSLContext sslcontext = SSLContext.getInstance( "SSL");
	        sslcontext.init( getKeyManagers(), // No KeyManager required
	            new TrustManager[] { new TrustAllTrustManager()},
	            null);
	        factory = ( SSLSocketFactory) sslcontext.getSocketFactory();
	      } 
	      catch( Exception ex) 
	      {
	    	  AdHocLogger.logException(ex);
	    	  //ex.printStackTrace();
	      }
	     //AdHocLogger.logDebug("TrustAllSocketFactory instantiated", AdHocLogger.LOG_DEBUG);
	  }
	 
	 //Called by getTLS()
	 public TrustAllSocketFactory(boolean tls) 
	 {
	     //AdHocLogger.logDebug("TrustAllSocketFactory instantiating", AdHocLogger.LOG_DEBUG);
	      try 
	      {
	        SSLContext sslcontext = SSLContext.getInstance( "TLS"); 
	        sslcontext.init( getKeyManagers(), // No KeyManager required
	            new TrustManager[] { new TrustAllTrustManager()},
	            null);
	        factory = ( SSLSocketFactory) sslcontext.getSocketFactory();
	      } 
	      catch( Exception ex) 
	      {
	    	  AdHocLogger.logException(ex);
	    	  //ex.printStackTrace();
	      }
	     //AdHocLogger.logDebug("TrustAllSocketFactory instantiated", AdHocLogger.LOG_DEBUG);
	  }
	    public static SocketFactory getDefault()
	    {
	    	 //AdHocLogger.logDebug("TrustAllSocketFactory get default", AdHocLogger.LOG_DEBUG);
	    	 return new TrustAllSocketFactory();
	    }
	    
	    public static SocketFactory getTLS()
	    {
	    	 //AdHocLogger.logDebug("TrustAllSocketFactory get default", AdHocLogger.LOG_DEBUG);
	    	 return new TrustAllSocketFactory(true);
	    }
	    
	    public Socket createSocket( Socket socket, String s, int i, boolean	flag)
	        throws IOException 
	    {
	    	//AdHocLogger.logDebug("TrustAllSocketFactory create socket - "+s+","+i+","+flag , AdHocLogger.LOG_DEBUG);
			return factory.createSocket( socket, s, i, flag);
	    }
	    public Socket createSocket( InetAddress inaddr, int i,
	        InetAddress inaddr1, int j) throws IOException 
	    {
	    	//AdHocLogger.logDebug("TrustAllSocketFactory create socket - " +i+","+inaddr+","+inaddr1+","+j , AdHocLogger.LOG_DEBUG);
	    	return factory.createSocket( inaddr, i, inaddr1, j);
	    }
	    public Socket createSocket( InetAddress inaddr, int i) 
	    throws IOException 
	    {
	    	//AdHocLogger.logDebug("TrustAllSocketFactory create socket - "+inaddr+","+i , AdHocLogger.LOG_DEBUG);
	    	return factory.createSocket( inaddr, i);
	    }
	    public Socket createSocket( String s, int i, InetAddress inaddr, int j)
	        throws IOException 
	    {
	    	//AdHocLogger.logDebug("TrustAllSocketFactory create socket - "+s+","+i+","+inaddr+","+j , AdHocLogger.LOG_DEBUG);
	    	return factory.createSocket( s, i, inaddr, j);
	    }
	    public Socket createSocket( String s, int i) 
	    throws IOException 
	    {
			//AdHocLogger.logDebug("TrustAllSocketFactory create socket - "+s+","+i , AdHocLogger.LOG_DEBUG);
			return factory.createSocket( s, i);
	    }
	    public String[] getDefaultCipherSuites() 
	    {
	      return factory.getSupportedCipherSuites();
	    }
	    public String[] getSupportedCipherSuites() 
	    {
	      return factory.getSupportedCipherSuites();
	    }
		@Override
		public Socket createSocket() throws IOException 
		{
			//AdHocLogger.logDebug("TrustAllSocketFactory create socket", AdHocLogger.LOG_DEBUG);
			return factory.createSocket();
		}
}
