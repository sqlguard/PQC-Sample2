/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.http;

import java.net.HttpURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class HttpResponse
{
    private Integer responseCode;
    private String response = null;
    private String error = null;
    private Exception exception = null;

    public HttpResponse()
    {
    }

    public HttpResponse(int responseCode)
    {
        this.responseCode = responseCode;
    }

    public HttpResponse(int responseCode, String response)
    {
        this.responseCode = responseCode;
        this.response = response;
    }

    public HttpResponse(Integer responseCode, String response)
    {
        this.responseCode = responseCode;
        this.response = response;
    }

    public Integer getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode)
    {
        this.responseCode = responseCode;
    }

    public boolean hasResponseCode()
    {
        return this.responseCode != null;
    }

    public String getResponse()
    {
        return response;
    }

    public void setResponse(String response)
    {
        this.response = response;
    }

    public boolean hasResponse()
    {
        return this.response != null;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public boolean hasError()
    {
        return this.error != null;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    public boolean hasException()
    {
        return this.exception != null;
    }

    public boolean hasAnyResponse()
    {
        return hasResponseCode() || hasResponse() || hasError() || hasException();
    }

    public boolean success()
    {
        // any response code between 200 and 299 inclusive indicates success (HTTP_OK is 200 and HTTP_MULT_CHOICE is 300)
        return (hasResponseCode() && (getResponseCode() >= HttpURLConnection.HTTP_OK
                && getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE));
    }

    public JsonObject getResponseAsJsonObject() throws JsonSyntaxException
    {
        JsonObject json = null;
        if (!response.isEmpty())
        {
            json = new JsonParser().parse(response.toString()).getAsJsonObject();
        }
        return json;
    }

    public JsonObject getErrorAsJsonObject() throws JsonSyntaxException
    {
        JsonObject json = null;
        if (!error.isEmpty())
        {
            json = new JsonParser().parse(error.toString()).getAsJsonObject();
        }
        return json;
    }

    public String toString()
    {
        String out = new String();

        if (hasResponseCode())
        {
            out += getResponseCode().toString();
            if (hasAnyResponse())
            {
                out += ": ";
            }
        }
        if (hasResponse())
        {
            out += getResponse();
        }
        if (hasError())
        {
            out += ": " + getError();
        }
        else if (hasException())
        {
            out += ": " + getException().getMessage();
        }
        return out;
    }
}
