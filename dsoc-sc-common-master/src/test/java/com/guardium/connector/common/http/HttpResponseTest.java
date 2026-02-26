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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import com.guardium.test.helpers.TestWithLog;

public class HttpResponseTest extends TestWithLog{
	
	@Test //tests getResponse and getResponseCode for this constructor
	void TestHttpResponseWithCode() {
		HttpResponse response = new HttpResponse(200);
		assertTrue(response.getResponseCode() == 200);
		assertTrue(response.getResponse() == null);
	}
	
	@Test //tests getResponse and getResponseCode for this constructor
	void TestHttpResponseWithCodeAndMessage() {
		HttpResponse response = new HttpResponse(200, "Ok");
		assertEquals("Ok", response.getResponse());
		assertTrue(response.getResponseCode() == 200);
	}
	
	@Test
	void TestHttpResonseJsonObject() {
		HttpResponse response = new HttpResponse(200, "Ok");
		JsonObject testJson = new JsonObject();
		testJson.addProperty("response", "Ok");
		assertThrows(IllegalStateException.class, () ->
		{
			response.getResponseAsJsonObject();
		});
		
		//valid response text
		HttpResponse response2 = new HttpResponse(200, "{ \"response\": \"Ok\" }");
		assertEquals("{ \"response\": \"Ok\" }", response2.getResponse());
		assertEquals(testJson, response2.getResponseAsJsonObject());
		
	}
	
	@Test
	void TestGettersAndSetters() {
		HttpResponse response = new HttpResponse(200, "{ \"response\": \"Ok\" }");
		assertEquals("{ \"response\": \"Ok\" }", response.getResponse());
		assertTrue(response.getResponseCode() == 200);
		JsonObject testObject1 = new JsonObject();
		testObject1.addProperty("response","Ok");
		assertEquals(testObject1, response.getResponseAsJsonObject());
		assertNull(response.getError());
		assertThrows(NullPointerException.class, () ->
		{
			response.getErrorAsJsonObject();
		});
		assertNull(response.getException());
		assertTrue(response.hasAnyResponse());
		assertTrue(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
		
		//set response message
		response.setResponse("{ \"response\": \"Not found\" }");
		assertEquals("{ \"response\": \"Not found\" }", response.getResponse());
		JsonObject testObject2 = new JsonObject();
		testObject2.addProperty("response", "Not found");
		assertEquals(testObject2, response.getResponseAsJsonObject());
		assertNull(response.getError());
		assertThrows(NullPointerException.class, () ->
		{
			response.getErrorAsJsonObject();
		});
		assertNull(response.getException());
		assertTrue(response.hasAnyResponse());
		assertTrue(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
		
		//set response code
		response.setResponseCode(404);
		assertTrue(response.getResponseCode() == 404);
		assertEquals("{ \"response\": \"Not found\" }", response.getResponse());
		assertEquals(testObject2, response.getResponseAsJsonObject());
		assertNull(response.getError());
		assertThrows(NullPointerException.class, () ->
		{
			response.getErrorAsJsonObject();
		});
		assertNull(response.getException());
		assertTrue(response.hasAnyResponse());
		assertTrue(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
		
		//set error
		response.setError("{ \"Error\": \"Invalid URL\" }");
		assertEquals("{ \"Error\": \"Invalid URL\" }", response.getError());
		JsonObject testErrorObject = new JsonObject();
		testErrorObject.addProperty("Error", "Invalid URL");
		assertEquals(testErrorObject, response.getErrorAsJsonObject());
		assertEquals("{ \"response\": \"Not found\" }", response.getResponse());
		assertEquals(testObject2, response.getResponseAsJsonObject());
		assertNull(response.getException());
		assertTrue(response.hasAnyResponse());
		assertTrue(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertTrue(response.hasError());
		assertFalse(response.hasException());
		
		//set exception
		JsonSyntaxException except = new JsonSyntaxException("Invalid format");
		response.setException(except);
		assertEquals("{ \"Error\": \"Invalid URL\" }", response.getError());
		assertEquals(testErrorObject, response.getErrorAsJsonObject());
		assertEquals("{ \"response\": \"Not found\" }", response.getResponse());
		assertEquals(testObject2, response.getResponseAsJsonObject());
		assertEquals(except, response.getException());
		assertTrue(response.hasAnyResponse());
		assertTrue(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertTrue(response.hasError());
		assertTrue(response.hasException());
	}
	
	@Test //test for no response present
	void testHttpResponseWithNoMessage() {
		HttpResponse response = new HttpResponse(200);
		assertNull(response.getError());
		assertNull(response.getException());
		assertNull(response.getResponse());
		assertThrows(NullPointerException.class, () -> 
		{
			response.getResponseAsJsonObject();
		
		});
		assertNull(response.getException());
		assertTrue(response.hasAnyResponse());
		assertFalse(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
	}
	
	@Test //test for empty HttpResponse
	void testEmptyHttpResponse() {
		HttpResponse response = new HttpResponse();
		assertThrows(NullPointerException.class, () -> 
		{
			response.getResponseAsJsonObject();
		
		});
		assertNull(response.getResponse());
		assertNull(response.getResponseCode());
		assertNull(response.getError());
		assertThrows(NullPointerException.class, () -> 
		{
			response.getErrorAsJsonObject();
		
		});
		assertNull(response.getException());
		assertFalse(response.hasAnyResponse());
		assertFalse(response.hasResponse());
		assertFalse(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
	}
	
	@Test //test success function - any failures here indicate the value range is not 
	//being handled correctly 
	void testSuccessIndicator() {
		HttpResponse testResponse = new HttpResponse(100);
		assertFalse(testResponse.success());
		for(int code=101; code < 200; code++) {
			testResponse.setResponseCode(code);
			assertFalse(testResponse.success());
		}
		for(int code=200; code < 300; code++) {
			testResponse.setResponseCode(code);
			assertTrue(testResponse.success());
		}
		for(int code=300; code < 600; code++) {
			testResponse.setResponseCode(code);
			assertFalse(testResponse.success());
		}
	}
	
	@Test
	void testIntegerConstructors() {
		HttpResponse response = new HttpResponse(new Integer(200));
		assertNull(response.getError());
		assertNull(response.getException());
		assertNull(response.getResponse());
		assertThrows(NullPointerException.class, () -> 
		{
			response.getResponseAsJsonObject();
		
		});
		assertTrue(response.hasAnyResponse());
		assertFalse(response.hasResponse());
		assertTrue(response.hasResponseCode());
		assertFalse(response.hasError());
		assertFalse(response.hasException());
		
		//constructor with response message
		HttpResponse response2 = new HttpResponse(new Integer(200), "{ \"response\": \"Ok\" }");
		JsonObject testJson = new JsonObject();
		testJson.addProperty("response", "Ok");
		assertEquals(new Integer(200), response2.getResponseCode());
		assertTrue(response2.getResponseCode() == 200);
		assertEquals("{ \"response\": \"Ok\" }", response2.getResponse());
		assertEquals(testJson, response2.getResponseAsJsonObject());
		assertNull(response2.getError());
		assertNull(response2.getException());
		assertTrue(response2.hasAnyResponse());
		assertTrue(response2.hasResponse());
		assertTrue(response2.hasResponseCode());
		assertFalse(response2.hasError());
		assertFalse(response2.hasException());		
	}
	
	@Test
	void testToString() {
		HttpResponse response = new HttpResponse(new Integer(200));
		assertEquals("200: ", response.toString());
		
		response.setResponse("{ \"response\": \"Ok\" }");
		assertEquals("200: { \"response\": \"Ok\" }", response.toString());
		
		response.setError("Error message");
		assertEquals("200: { \"response\": \"Ok\" }: Error message", response.toString());
		
		response.setException(new JsonSyntaxException("Invalid format"));
		assertEquals("200: { \"response\": \"Ok\" }: Error message", response.toString());
		
		response.setError(null);
		assertEquals("200: { \"response\": \"Ok\" }: Invalid format", response.toString());
		
		response.setResponseCode(null);
		assertEquals("{ \"response\": \"Ok\" }: Invalid format", response.toString());
	}
}
