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

package com.guardium.connector.common.http;

import org.junit.jupiter.api.Test;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.net.ssl.HttpsURLConnection;
import java.util.Map.Entry;

import com.guardium.test.helpers.MockServer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.guardium.connector.common.crypto.EncryptionUtils;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.exceptions.EncryptionException;
import com.guardium.connector.common.exceptions.MessengerException;
import com.guardium.connector.common.exceptions.PropertiesException;
import com.guardium.connector.common.http.HttpRequest;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.connector.common.properties.SecureConnectorProperties;
import com.guardium.connector.common.properties.SecureConnectorPropertiesDBHandler;

import com.guardium.test.helpers.TestWithDatabase;

public class SecureConnectorMessengerTest extends TestWithDatabase
{
	
	private MockServer mockServer = new MockServer();
	
	/**
	 * Object used to quickly generate data needed to test SecureConnectorMessenger methods.
	 * @author Ryan Ramphal
	 * <pre>New objects contain the following members for convenience:
	 * - body: a JsonObject that can be used as an HttpRequest body
	 * - name: the name of the currently simulated dataconnector
	 * - handler: a database handler object that initializes the properties store
	 * - authorization: String for authorization parameter in the
	 * body object.
	 * - authentication: String for authentication parameter in the
	 * body object.
	 * - vault: String used in the body object.
	 * - modulus: String key modulus used in request simulation - used in the
	 * body object.
	 * - exponent: String key exponent used in request simulation - used in the
	 * body object.</pre>
	 */
	private class RequestBodyData
	{
		public JsonObject body;
		public String name;
		public SecureConnectorPropertiesDBHandler handler;
		public String authorization;
		public String authentication;
		public String vault;
		public String modulus;
		public String exponent;
		
		/**
		 * 
		 * @param name - The name of this dataconnector.
		 * @param testPropertyKey - The key of a property you set to add to the 
		 * request body JsonObject (useful in testing).
		 * @param testPropertyValue - The value of a property you set to add to the
		 * request body JsonObject (useful in testing).
		 * @throws PropertiesException
		 * @throws DbException
		 * @throws EncryptionException
		 */
		public RequestBodyData(String name, String testPropertyKey, String testPropertyValue)
				throws PropertiesException, DbException, EncryptionException
		{
			body = new JsonObject();
			this.name = name;
			
			body = new JsonObject();
			
			handler = new SecureConnectorPropertiesDBHandler();
	        handler.init();
	        SecureConnectorProperties.setPropertiesStore(handler);
	        
			authorization = SecureConnectorProperties.instance().getAuthorization();
			authentication = SecureConnectorProperties.instance().getAuthentication();
			
			// this is the message format we need to send to the server:
	        // {
		        // name: "some name unique per tenant",
		        // description" "some description string",
		        // key:
		        // {
			        // modulus: "base64-encoded public key modulus",
			        // exponent: "base64-encoded public key exponent"
		        // }
		        // vault: generated secret
	        // }
			
			EncryptionUtils crypto = EncryptionUtils.instance();
			modulus = crypto.getPublicKeyModulus();
			exponent = crypto.getPublicKeyExponent();
			this.name = name;
			vault = crypto.sign(name);
			
			body.addProperty("name", name);
	        body.addProperty("description", "test description");
	        JsonObject keyprop = new JsonObject();
	        keyprop.addProperty("modulus", modulus);
	        keyprop.addProperty("exponent", exponent);
	        body.add("key", keyprop);
	        body.addProperty("vault", vault);
		}
	}
	
	/**
	 * Automatically called before each test case to reset the server.
	 * @throws Exception
	 */
	@BeforeEach
	void resetMockServer() throws Exception
    {
		mockServer.reset();
//		props.useMockProps();
    }
	
	@AfterEach
	void deleteTemporaryFiles()
	{
		File file = new File("test_download_file.txt");
		if(file.exists())
		{
			file.delete();
		}
	}

	@Disabled //need to mock setting env var 'ALLOW_SELF_SIGNED_CERTS' to true for this test to work correctly. Direct setting env var is insufficient.
	@Test
	void testSendMessageSuccess() throws DbException, PropertiesException, IOException, URISyntaxException, EncryptionException, MessengerException
	{
		OutputStream outstream = mockServer.expectGetConnectors(200, "{ \"response\" : \"Success\" }");

		// this is the message format we need to send to the server:
        // {
        // name: "some name unique per tenant",
        // description" "some description string",
        // key:
        // {
        // modulus: "base64-encoded public key modulus",
        // exponent: "base64-encoded public key exponent"
        // }
        // vault: generated secret
        // }
		
		RequestBodyData reqdata = new RequestBodyData("test name", "test description", "description");
		
		HttpRequest testRequest = new HttpRequest(new URL(SecureConnectorDefaultProperties.getServerUrl() + "api/v1/connectors"), 
				HttpRequest.Method.GET, reqdata.body);
		HttpResponse testResponse = SecureConnectorMessenger.sendMessage(testRequest, reqdata.authorization, reqdata.authentication);
		
		assertNotNull(testResponse);
		
		JsonObject received = new JsonParser().parse(outstream.toString()).getAsJsonObject();
		
		assertEquals(reqdata.name, received.get("name").getAsString());
		assertEquals("test description", received.get("description").getAsString());
		assertEquals(reqdata.modulus, received.get("key").getAsJsonObject().get("modulus").getAsString());
		assertEquals(reqdata.exponent, received.get("key").getAsJsonObject().get("exponent").getAsString());
		assertEquals(reqdata.vault, received.get("vault").getAsString());
		assertTrue(testResponse.getResponseCode() == 200);
		
		//reset output stream for next case
		outstream.close();
		outstream = mockServer.expectGetConnectors(200, "{ \"response\" : \"Success\" }");
		
		
		//incorrect vault
		JsonObject body2 = new JsonObject();
        body2.addProperty("name", reqdata.name);
        body2.addProperty("description", "test description");
        JsonObject keyprop2 = new JsonObject();
        keyprop2.addProperty("modulus", reqdata.modulus);
        keyprop2.addProperty("exponent", reqdata.exponent);
        body2.add("key", keyprop2);
        body2.addProperty("vault", reqdata.vault.concat("_failure_case"));
        
		HttpRequest testRequest2 = new HttpRequest(new URL(SecureConnectorDefaultProperties.getServerUrl() + "api/v1/connectors"), 
				HttpRequest.Method.GET, body2);
		HttpResponse testResponse2 = SecureConnectorMessenger.sendMessage(testRequest2, reqdata.authorization, reqdata.authentication);
		
		assertNotNull(testResponse2);
		
		
		JsonObject received2 = new JsonParser().parse(outstream.toString()).getAsJsonObject();
		
		assertEquals(reqdata.name, received2.get("name").getAsString());
		assertEquals("test description", received2.get("description").getAsString());
		assertEquals(reqdata.modulus, received2.get("key").getAsJsonObject().get("modulus").getAsString());
		assertEquals(reqdata.exponent, received2.get("key").getAsJsonObject().get("exponent").getAsString());
		assertNotEquals(reqdata.vault, received2.get("vault").getAsString());
		assertTrue(testResponse2.getResponseCode() == 200);
	}
	
	/**
	 * Asserts that connection failure is handled as expected.
	 * @throws PropertiesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	void testGetHttpsConnectionThrowsOnConnectionFailure() throws PropertiesException, URISyntaxException, IOException, InterruptedException
	{
		HttpRequest msg = new HttpRequest(new URL("https://failedrequest.com"), HttpRequest.Method.GET);
		Exception e = assertThrows(MessengerException.class, () -> {
			SecureConnectorMessenger.getHttpsConnection(msg, "bad auth", "bad auth");
		});
		assertEquals("Cannot connect to server Could not open connection to https://failedrequest.com", e.getMessage());
	}
	
	@Test
	void testupdateconnection() throws IOException, PropertiesException, URISyntaxException, MessengerException
	{
		mockServer.expectHttpsGetConnectors(200, "{ \"status\" : \"Ok\" }");
		
		JsonObject reqObj = new JsonObject();
		reqObj.addProperty("p1", "value1");
		HttpRequest msg = new HttpRequest(new URL(SecureConnectorDefaultProperties.getServerUrl() + "api/v1/connectors"),
					HttpRequest.Method.GET, reqObj);
		
		String token = SecureConnectorDefaultProperties.getAuthorization();
		
		HttpsURLConnection con = SecureConnectorMessenger.getHttpsConnection(msg, token, token);
		
		//create connection to verify if property changes are being persisted correctly
		con.setRequestProperty("Content-Length", "348");
		assertEquals("348", con.getRequestProperty("Content-Length"));
	}
	
	@Test
	void testGetHttpsConnectionTimeoutFailure() throws IOException, InterruptedException, DbException,
		PropertiesException, EncryptionException, URISyntaxException, MessengerException
	{
		mockServer.expectHangingGetConnectors();
		
		RequestBodyData reqdata = new RequestBodyData("test name", "test description", "description");
		
		HttpRequest testRequest = new HttpRequest(new URL(SecureConnectorDefaultProperties.getServerUrl() + "api/v1/connectors"), 
				HttpRequest.Method.GET, reqdata.body);
		
		
		assertThrows(MessengerException.class, () -> {
			SecureConnectorMessenger.sendMessage(testRequest, reqdata.authorization, reqdata.authentication);
		});
		
	}
	
	@Test //FYI: no test for MalformedURLException - thrown in creation of HttpRequest parameter prior to execution
	void testGetHttpsConnectionSuccess() throws IOException, PropertiesException, URISyntaxException, MessengerException
	{
		mockServer.expectHttpsGetConnectors(200, "{ \"status\" : \"Ok\" }");
		
		JsonObject reqObj = new JsonObject();
		HttpRequest msg = new HttpRequest(new URL(SecureConnectorDefaultProperties.getServerUrl() + "api/v1/connectors"),
					HttpRequest.Method.GET, reqObj);
		
		String authorization = SecureConnectorDefaultProperties.getAuthorization();
		String authentication = "Test_Authentication_String";
		
		//create connection to verify if property changes are being persisted correctly.
		HttpsURLConnection con = SecureConnectorMessenger.getHttpsConnection(msg, authorization, authentication);
		
		assertEquals("application/json; charset=utf-8", con.getRequestProperty("Content-Type"));
		assertEquals("utf-8", con.getRequestProperty("Accept-Charset"));
		assertEquals(msg.getMethodString(), con.getRequestMethod());
		assertEquals(authorization, con.getRequestProperty("Authorization"));
		assertEquals(authentication, con.getRequestProperty("Authentication"));
		assertFalse(con.getUseCaches());
		assertTrue(con.getDoInput());
		assertTrue(con.getDoOutput());
		assertTrue(SecureConnectorDefaultProperties.getConnectionTimeout() == con.getConnectTimeout());
		assertTrue(SecureConnectorDefaultProperties.getConnectionTimeout() == con.getReadTimeout());
		for (Entry<String, String> entry : msg.getHeaders().entrySet())
        {
            assertEquals(entry.getValue(), con.getRequestProperty(entry.getKey()));
        }
	}
	
	@Test
	void testGetSSLContext() throws MessengerException
	{
		assertNotNull(SecureConnectorMessenger.getSSLConext());
		assertEquals("TLSv1.2", SecureConnectorMessenger.getSSLConext().getProtocol());
	}
	
	@Test
	void testDownloadFile() throws Exception
	{
		mockServer.expectFileDownload();
		
		//get file's SHA-256 digest for comparison later
		FileInputStream fis = new FileInputStream("test_download_file.txt");
		String digest = DigestUtils.sha256Hex(fis);
		fis.close();
		
		//simulate downloading the file to a new folder with the correct SHA-256 checksum
		File createFile = new File("tempdownload/test_download_file.txt");
		if(!createFile.exists())
		{
			createFile.getParentFile().mkdirs();
			createFile.createNewFile();
		}
		assertTrue(SecureConnectorMessenger.downloadFile("https://mockserver/download",
				Paths.get("tempdownload/test_download_file.txt"), digest));
		
		//delete downloaded file
		createFile.delete();
		createFile = new File("tempdownload");
		createFile.delete();
		
		//simulate with incorrect checksum
		mockServer.reset();
		mockServer.expectFileDownload();
		File createFile2 = new File("tempdownload/test_download_file.txt");
		if(!createFile2.exists())
		{
			createFile2.getParentFile().mkdirs();
			createFile2.createNewFile();
		}
		assertFalse(SecureConnectorMessenger.downloadFile("https://mockserver/download",
				Paths.get("tempdownload/test_download_file.txt"), "Incorrect-checksum"));
		
		//delete downloaded file
		createFile2.delete();
		createFile2 = new File("tempdownload");
		createFile2.delete();
	}
}
