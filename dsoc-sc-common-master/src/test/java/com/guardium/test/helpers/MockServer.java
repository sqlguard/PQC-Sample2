/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2018, 2019                                  */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.test.helpers;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.guardium.connector.common.exceptions.MessengerException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

/**
 * <pre>
 * Modified URLStreamHandler that fails when requests are sent to "https://failedrequest.com".
 * Also allows us to reset connections between tests.
 * </pre>
 */
class HttpUrlStreamHandler extends URLStreamHandler
{
    private static Map<URL, HttpURLConnection> connections = new HashMap<URL, HttpURLConnection>();
    final static String FAILURE_URL = "https://failedrequest.com";

    @Override
    protected HttpURLConnection openConnection(URL url) throws IOException
    {
        return connections.get(url);
    }

    public void resetConnections()
    {
        connections = new HashMap<URL, HttpURLConnection>();
    }

    public HttpUrlStreamHandler addConnection(URL url, HttpURLConnection connection) throws MessengerException
    {
    	if(url.toString().equals(FAILURE_URL)) {
    		throw new MessengerException("messenger.connection.error", "Could not open connection to " + url.toString());
    	}
        connections.put(url, connection);
        return this;
    }
}

/**
 * <pre>
 * Use for stubbing a server for the dataconnector to connect to for testing purposes.
 * Contains several custom server functionality functions that are very useful for testing purposes.
 * All "normal" requests must be sent to "https://mockserver/api/v1/connectors" in order
 * to use this object.
 * </pre>
 */
public class MockServer
{
    private static HttpUrlStreamHandler httpUrlStreamHandler;
    protected static final String serverUrl = "https://mockserver/";

    static
    {
        SecureConnectorDefaultProperties.setServerUrl(serverUrl);
        SecureConnectorDefaultProperties.setAuthorization("bad token");
        SecureConnectorDefaultProperties.setAuthorization("bad token");
        
        // Allows for mocking URL connections
        URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
        try
        {
            URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);
        }
        catch (Error e)
        {
            // ignore this.  URL will throw when the URLStreamHandlerFactory
            // is already set...in our case we will just keep using the old one
        }

        httpUrlStreamHandler = new HttpUrlStreamHandler();
        given(urlStreamHandlerFactory.createURLStreamHandler("https")).willReturn(httpUrlStreamHandler);
    }

    public void reset() throws Exception
    {
        httpUrlStreamHandler.resetConnections();
    }

    public void addConnection(URL url, HttpURLConnection connection) throws MessengerException
    {
        httpUrlStreamHandler.addConnection(url, connection);
    }
    
    /**
     * Simulates all needed functionality for an HTTPS server for testing.
     * @param responseCode
     * @param response
     * @return
     * @throws IOException
     * @throws MessengerException
     */
    public void expectHttpsGetConnectors(int responseCode, String response) throws IOException, MessengerException
    {
    	HttpsURLConnection connection = mock(HttpsURLConnection.class);
        addConnection(new URL(serverUrl + "api/v1/connectors"), connection);
        
        OutputStream output = new ByteArrayOutputStream();
        given(connection.getOutputStream()).willReturn(output);
        given(connection.getResponseCode()).willReturn(responseCode);
        given(connection.getResponseMessage()).willReturn(response);
        
        //actually perform connection properties modification
        doCallRealMethod().when(connection).setRequestProperty(any(String.class), any(String.class));
        doCallRealMethod().when(connection).getRequestProperty(any(String.class));
        doCallRealMethod().when(connection).getRequestProperties();
        doCallRealMethod().when(connection).setHostnameVerifier(any(HostnameVerifier.class));
        doCallRealMethod().when(connection).addRequestProperty(any(String.class), any(String.class));
        doCallRealMethod().when(connection).getURL();
        doCallRealMethod().when(connection).getConnectTimeout();
        doCallRealMethod().when(connection).getContent();
        doCallRealMethod().when(connection).getContentEncoding();
        doCallRealMethod().when(connection).getContentLength();
        doCallRealMethod().when(connection).getContentType();
        doCallRealMethod().when(connection).getRequestMethod();
        doCallRealMethod().when(connection).getAllowUserInteraction();
        doCallRealMethod().when(connection).toString();
        doCallRealMethod().when(connection).getConnectTimeout();
        doCallRealMethod().when(connection).getContent();
        doCallRealMethod().when(connection).getContentEncoding();
        doCallRealMethod().when(connection).getContentLength();
        doCallRealMethod().when(connection).getRequestMethod();
        doCallRealMethod().when(connection).setRequestMethod(any(String.class));
        doCallRealMethod().when(connection).getHeaderField(any(String.class));
        doCallRealMethod().when(connection).setUseCaches(anyBoolean());
        doCallRealMethod().when(connection).getUseCaches();
        doCallRealMethod().when(connection).setDoInput(anyBoolean());
        doCallRealMethod().when(connection).getDoInput();
        doCallRealMethod().when(connection).setDoOutput(anyBoolean());
        doCallRealMethod().when(connection).getDoOutput();
        doCallRealMethod().when(connection).setConnectTimeout(anyInt());
        doCallRealMethod().when(connection).getConnectTimeout();
        doCallRealMethod().when(connection).setReadTimeout(anyInt());
        doCallRealMethod().when(connection).getReadTimeout();
        doCallRealMethod().when(connection).setAllowUserInteraction(anyBoolean());
    }
    
    /**
     * Used to simulate connection timeouts (hanging connections). See if expectHttpsGetConnectors can help as well.
     * @throws IOException
     * @throws InterruptedException
     * @throws MessengerException
     */
    public void expectHangingGetConnectors() throws IOException, InterruptedException, MessengerException
    {
    	HttpURLConnection connection = mock(HttpsURLConnection.class);
        
        OutputStream output = new ByteArrayOutputStream();
        given(connection.getOutputStream()).willReturn(output);
        
        given(connection.getResponseCode()).willThrow(new SocketTimeoutException("Connection timeout simulated."));
    }
    
    /**
     * Simulates normal web server activity.
     * @param responseCode
     * @param response
     * @return
     * @throws IOException
     * @throws MessengerException
     */
    public OutputStream expectGetConnectors(int responseCode, String response) throws IOException, MessengerException
    {
    	HttpURLConnection connection = mock(HttpsURLConnection.class);
        addConnection(new URL(serverUrl + "api/v1/connectors"), connection);
        
        OutputStream output = new ByteArrayOutputStream();
        given(connection.getOutputStream()).willReturn(output);
        
        given(connection.getResponseCode()).willReturn(responseCode);
        given(connection.getResponseMessage()).willReturn(response);
        return output;
    }

    public void expectGetConnectorsThrows(Exception exceptionToThrow) throws IOException, MessengerException
    {
        HttpURLConnection connection = mock(HttpsURLConnection.class);
        addConnection(new URL(serverUrl + "api/v1/connectors"), connection);

        willThrow(exceptionToThrow).given(connection).connect();
    }
    
    public OutputStream expectGetConnector(String name, int responseCode, String response) throws IOException, MessengerException
    {
        HttpURLConnection connection = mock(HttpsURLConnection.class);
        addConnection(new URL(serverUrl + "api/v1/connectors" + name), connection);
        OutputStream output = new ByteArrayOutputStream();
        given(connection.getOutputStream()).willReturn(output);
        given(connection.getResponseCode()).willReturn(responseCode);
        given(connection.getResponseMessage()).willReturn(response);
        
        return output;
    }
    
    public void expectGetExists(int responseCode, String response) throws IOException, MessengerException
    {
        HttpURLConnection getExists = mock(HttpsURLConnection.class);
        addConnection(new URL(serverUrl + "api/v1/connectors/search?exists=true"), getExists);

        OutputStream getExistsReceiveStream = new ByteArrayOutputStream();
        given(getExists.getOutputStream()).willReturn(getExistsReceiveStream);

        InputStream getExistsSendStream = new ByteArrayInputStream(new byte[4096]);
        given(getExists.getInputStream()).willReturn(getExistsSendStream);

        given(getExists.getResponseCode()).willReturn(responseCode);
        given(getExists.getResponseMessage()).willReturn(response);
    }
    
    public void expectFileDownload() throws MessengerException, IOException
    {
    	HttpURLConnection connection = mock(HttpsURLConnection.class);
    	addConnection(new URL(serverUrl + "download"), connection);
    	
    	//create file to be sent to requester
    	File testFile = new File("test_download_file.txt");
    	if(testFile.exists())
    	{
    		testFile.delete();
    	}
    	PrintWriter filewriter = new PrintWriter(testFile);
    	filewriter.write("This is sample text that should be present in the downloaded file.");
    	filewriter.flush();
    	filewriter.close();
    	
    	//setup mockserver to send to requester
    	InputStream instream = new FileInputStream(testFile);
    	given(connection.getInputStream()).willReturn(instream);
    }
}