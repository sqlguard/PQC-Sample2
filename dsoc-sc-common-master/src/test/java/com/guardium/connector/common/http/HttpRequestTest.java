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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;

import com.google.gson.JsonObject;

import com.guardium.test.helpers.TestWithLog;
import com.guardium.connector.common.exceptions.PropertiesException;
import com.guardium.connector.common.http.HttpRequest;

public class HttpRequestTest extends TestWithLog{
	final static String TEST_URL = "http://user1:pass1@localhost:8000/test?query1=datum1#/reference1";
	
	@Test
	void testHttpURLParseSuccessGET() throws IOException, PropertiesException, URISyntaxException{
		
		//verify that the URL is parsed into different parts correctly, and resulting URL is identical to input URL
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		assertEquals(request.getURL(), new URL(TEST_URL));
		
		//no protocol
		HttpRequest request2 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.GET);
		assertEquals(request2.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"));
		
		//no user info
		HttpRequest request3 = new HttpRequest(new URL("http://localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.GET);
		assertEquals(request3.getURL(), new URL("http://localhost:8000/test?query1=datum1#/reference1"));
		
		//no explicit port
		HttpRequest request4 = new HttpRequest(new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"), HttpRequest.Method.GET);
		assertEquals(request4.getURL(), new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"));
		
		//no explicit path
		HttpRequest request5 = new HttpRequest(new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"), HttpRequest.Method.GET);
		assertEquals(request5.getURL(), new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"));
		
		//no query
		HttpRequest request6 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test#/reference1"), HttpRequest.Method.GET);
		assertEquals(request6.getURL(), new URL("http://user1:pass1@localhost:8000/test#/reference1"));
		
		//no reference
		HttpRequest request7 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1"), HttpRequest.Method.GET);
		assertEquals(request7.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1"));
		
		//no host
		assertThrows(URISyntaxException.class, () ->
		{
			new HttpRequest(new URL("http://user1:pass1@:8000/test?query1=datum1#/reference1"), HttpRequest.Method.GET);
		});
	}
	
	@Test
	void testHttpURLParseSuccessPOST() throws IOException, PropertiesException, URISyntaxException{
		
		//verify that the URL is parsed into different parts correctly, and resulting URL is identical to input URL
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.POST);
		assertEquals(request.getURL(), new URL(TEST_URL));
		
		//no protocol
		HttpRequest request2 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.POST);
		assertEquals(request2.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"));
		
		//no user info
		HttpRequest request3 = new HttpRequest(new URL("http://localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.POST);
		assertEquals(request3.getURL(), new URL("http://localhost:8000/test?query1=datum1#/reference1"));
		
		//no explicit port
		HttpRequest request4 = new HttpRequest(new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"), HttpRequest.Method.POST);
		assertEquals(request4.getURL(), new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"));
		
		//no explicit path
		HttpRequest request5 = new HttpRequest(new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"), HttpRequest.Method.POST);
		assertEquals(request5.getURL(), new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"));
		
		//no query
		HttpRequest request6 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test#/reference1"), HttpRequest.Method.POST);
		assertEquals(request6.getURL(), new URL("http://user1:pass1@localhost:8000/test#/reference1"));
		
		//no reference
		HttpRequest request7 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1"), HttpRequest.Method.POST);
		assertEquals(request7.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1"));
		
		//no host
		assertThrows(URISyntaxException.class, () ->
		{
			new HttpRequest(new URL("http://user1:pass1@:8000/test?query1=datum1#/reference1"), HttpRequest.Method.POST);
		});
	}
	
	@Test
	void testHttpURLParseSuccessPUT() throws IOException, PropertiesException, URISyntaxException{
		
		//verify that the URL is parsed into different parts correctly, and resulting URL is identical to input URL
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.PUT);
		assertEquals(request.getURL(), new URL(TEST_URL));
		
		//no protocol
		HttpRequest request2 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.PUT);
		assertEquals(request2.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"));
		
		//no user info
		HttpRequest request3 = new HttpRequest(new URL("http://localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.PUT);
		assertEquals(request3.getURL(), new URL("http://localhost:8000/test?query1=datum1#/reference1"));
		
		//no explicit port
		HttpRequest request4 = new HttpRequest(new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"), HttpRequest.Method.PUT);
		assertEquals(request4.getURL(), new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"));
		
		//no explicit path
		HttpRequest request5 = new HttpRequest(new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"), HttpRequest.Method.PUT);
		assertEquals(request5.getURL(), new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"));
		
		//no query
		HttpRequest request6 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test#/reference1"), HttpRequest.Method.PUT);
		assertEquals(request6.getURL(), new URL("http://user1:pass1@localhost:8000/test#/reference1"));
		
		//no reference
		HttpRequest request7 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1"), HttpRequest.Method.PUT);
		assertEquals(request7.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1"));
		
		//no host
		assertThrows(URISyntaxException.class, () ->
		{
			new HttpRequest(new URL("http://user1:pass1@:8000/test?query1=datum1#/reference1"), HttpRequest.Method.PUT);
		});
	}
	
	@Test
	void testHttpURLParseSuccessDELETE() throws IOException, PropertiesException, URISyntaxException{
		
		//verify that the URL is parsed into different parts correctly, and resulting URL is identical to input URL
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.DELETE);
		assertEquals(request.getURL(), new URL(TEST_URL));
		
		//no protocol
		HttpRequest request2 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.DELETE);
		assertEquals(request2.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1#/reference1"));
		
		//no user info
		HttpRequest request3 = new HttpRequest(new URL("http://localhost:8000/test?query1=datum1#/reference1"), HttpRequest.Method.DELETE);
		assertEquals(request3.getURL(), new URL("http://localhost:8000/test?query1=datum1#/reference1"));
		
		//no explicit port
		HttpRequest request4 = new HttpRequest(new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"), HttpRequest.Method.DELETE);
		assertEquals(request4.getURL(), new URL("http://user1:pass1@localhost/test?query1=datum1#/reference1"));
		
		//no explicit path
		HttpRequest request5 = new HttpRequest(new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"), HttpRequest.Method.DELETE);
		assertEquals(request5.getURL(), new URL("http://user1:pass1@localhost:8000?query1=datum1#/reference1"));
		
		//no query
		HttpRequest request6 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test#/reference1"), HttpRequest.Method.DELETE);
		assertEquals(request6.getURL(), new URL("http://user1:pass1@localhost:8000/test#/reference1"));
		
		//no reference
		HttpRequest request7 = new HttpRequest(new URL("http://user1:pass1@localhost:8000/test?query1=datum1"), HttpRequest.Method.DELETE);
		assertEquals(request7.getURL(), new URL("http://user1:pass1@localhost:8000/test?query1=datum1"));
		
		//no host
		assertThrows(URISyntaxException.class, () ->
		{
			new HttpRequest(new URL("http://user1:pass1@:8000/test?query1=datum1#/reference1"), HttpRequest.Method.DELETE);
		});
	}
	
	@Test
	void testBodyAssignment() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		JsonObject json = new JsonObject();
		json.addProperty("testkey", "testvalue");
		
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET, json);
		assertEquals(json, request.getBody());
		
		HttpRequest request2 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.PUT, json);
		assertEquals(json, request2.getBody());
		
		HttpRequest request3 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.POST, json);
		assertEquals(json, request3.getBody());
		
		HttpRequest request4 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.DELETE, json);
		assertEquals(json, request4.getBody());
	}
	
	@Test
	void testHasBody() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		JsonObject json = new JsonObject();
		json.addProperty("testkey", "testvalue");
		
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET, json);
		assertTrue(request.hasBody());
		
		HttpRequest request2 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.PUT, json);
		assertTrue(request2.hasBody());
		
		HttpRequest request3 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.POST, json);
		assertTrue(request3.hasBody());
		
		HttpRequest request4 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.DELETE, json);
		assertTrue(request4.hasBody());
	}
	
	@Test
	void testGetMethodAndString() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		assertEquals(request.getMethod(), HttpRequest.Method.GET);
		assertEquals(request.getMethodString(), "GET");
		
		HttpRequest request2 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.PUT);
		assertEquals(request2.getMethod(), HttpRequest.Method.PUT);
		assertEquals(request2.getMethodString(), "PUT");
		
		HttpRequest request3 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.POST);
		assertEquals(request3.getMethod(), HttpRequest.Method.POST);
		assertEquals(request3.getMethodString(), "POST");
		
		HttpRequest request4 = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.DELETE);
		assertEquals(request4.getMethod(), HttpRequest.Method.DELETE);
		assertEquals(request4.getMethodString(), "DELETE");
	}
	
	@Test
	void testSetMethod() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		assertEquals(request.getMethod(), HttpRequest.Method.GET);
		
		request.setMethod(HttpRequest.Method.PUT);
		assertEquals(request.getMethod(), HttpRequest.Method.PUT);
		
		request.setMethod(HttpRequest.Method.POST);
		assertEquals(request.getMethod(), HttpRequest.Method.POST);
		
		request.setMethod(HttpRequest.Method.DELETE);
		assertEquals(request.getMethod(), HttpRequest.Method.DELETE);
		
		request.setMethod(HttpRequest.Method.GET);
		assertEquals(request.getMethod(), HttpRequest.Method.GET);
	}
	
	@Test
	void testURLGetterAndSetter() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		assertEquals(request.getURL(), new URL(TEST_URL));
		
		request.setURL(new URL("http://localhost:8080"));
		assertEquals(request.getURL(), new URL("http://localhost:8080"));
	}
	
	@Test
	void testGetNullHeader() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		assertEquals(request.getHeaders(), new Hashtable<String, String>());
	}
	
	@Test
	@SuppressWarnings("serial")
	void testSetHeader() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		request.addHeader("header1", "value1");
		assertEquals(request.getHeaders(), new Hashtable<String, String>(){{put("header1","value1");}});
		assertEquals(request.getHeaderValue("header1"), "value1");
	}
	
	@Test
	void testGetIncorrectHeader() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		request.addHeader("header1", "value1");
		assertNull(request.getHeaderValue("badheader"));
	}
	
	@Test
	@SuppressWarnings("serial")
	void testRemoveHeader() throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpRequest request = new HttpRequest(new URL(TEST_URL), HttpRequest.Method.GET);
		request.addHeader("header1", "value1");
		request.addHeader("header2", "value2");
		request.removeHeader("header1");
		assertEquals(request.getHeaders(), new Hashtable<String, String>(){{put("header2","value2");}});
	}
}
