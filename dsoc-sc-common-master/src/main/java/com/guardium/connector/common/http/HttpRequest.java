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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import com.google.gson.JsonElement;
import com.guardium.connector.common.Utils;
import com.guardium.connector.common.exceptions.PropertiesException;

public class HttpRequest
{
    public static enum Method
    {
        GET, POST, PUT, DELETE
    };

    private Method method = Method.GET;
    private JsonElement body = null;
    private Map<String, String> headers = new Hashtable<String, String>();
    private URL url;

    public HttpRequest(final URL url, final Method method) throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException
    {
        this(url, method, null);
    }
    
    public HttpRequest(final URL url, final Method method, final JsonElement body) throws PropertiesException, MalformedURLException, UnsupportedEncodingException, URISyntaxException
    {
        this.method = method;
        if (body != null)
        {
            this.body = body;
        }
        
        // Build a full URI based on url to make sure that the URL is correctly escaped
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        
        this.url = uri.toURL();
    }

    public JsonElement getBody()
    {
        return body;
    }

    public void setBody(JsonElement body)
    {
        this.body = body;
    }

    public boolean hasBody()
    {
        return null != body;
    }

    public String toString()
    {
        return Utils.toPrettyFormat(body, true);
    }

    public Method getMethod()
    {
        return method;
    }
    
    public String getMethodString()
    {
        return method.name();
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public URL getURL()
    {
        return url;
    }

    public void setURL(URL url)
    {
        this.url = url;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void addHeader(final String name, final String value)
    {
        headers.put(name, value);
    }

    public String getHeaderValue(final String name)
    {
        return headers.get(name);
    }

    public void removeHeader(final String name)
    {
        headers.remove(name);
    }
}
