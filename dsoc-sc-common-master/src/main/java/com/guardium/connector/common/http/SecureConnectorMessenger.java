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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.Utils;
import com.guardium.connector.common.exceptions.MessengerException;
import com.guardium.connector.common.exceptions.PropertiesException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.connector.common.properties.SecureConnectorProperties;

public class SecureConnectorMessenger
{
    private final static Logger logger = LoggerUtils.getLogger(SecureConnectorMessenger.class);
    private final static String CN = ".datarisk.dsoc.ibm.com";
    private final static Pattern cnPattern = Pattern.compile(".*CN=(.*?)(?:,|$)");

    public static SSLContext getSSLConext() throws MessengerException
    {
        SSLContext sslcontext = null;
        try
        {
            sslcontext = SSLContext.getInstance("TLSv1.2", "IBMJSSE2");
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e)
        {
            // we can ignore this because we know that the alg and provider are good
        }

        TrustManager tm = new X509TrustManager()
        {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }
        };

        try
        {
            if (SecureConnectorDefaultProperties.getAllowSelfSignedCerts())
                sslcontext.init(null, new TrustManager[]
                {
                        tm
                }, null);
            else
                sslcontext.init(null, null, null);
        }
        catch (KeyManagementException e)
        {
            throw new MessengerException("messenger.ssl.context.error", e.getMessage());
        }
        return sslcontext;
    }

    private static void verifyCN(HttpsURLConnection con) throws MessengerException
    {
        boolean match = false;

        try
        {
            java.security.cert.Certificate[] cchain = con.getServerCertificates();

            for (int i = 0; i < cchain.length; i++)
            {
                String p = ((X509Certificate) cchain[i]).getSubjectX500Principal().getName();
                Matcher m = cnPattern.matcher(p);
                if (logger.isTraceEnabled())
                    logger.trace(
                            "[SecureConnectorMessenger::sendMessage::verifyCN] Trying to authenticate peer certificate: "
                                    + p);
                if (m.find() && m.groupCount() > 0 && m.group(1).endsWith(CN))
                {
                    if (logger.isTraceEnabled())
                        logger.trace(
                                "[SecureConnectorMessenger::sendMessage::verifyCN] Peer is authentic");
                    match = true;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Peer is not authentic", e);
        }
        finally
        {
            if (!match)
                throw new MessengerException("Peer is not authentic");
        }
    }

    public static HttpsURLConnection getHttpsConnection(HttpRequest msg, String authorization, String authentication)
            throws MessengerException
    {
        HttpsURLConnection con = null;

        try
        {
            HttpsURLConnection.setDefaultSSLSocketFactory((SSLSocketFactory) getSSLConext().getSocketFactory());

            if (SecureConnectorDefaultProperties.getAllowSelfSignedCerts())
            {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
                {
                    public boolean verify(String hostname, SSLSession session)
                    {
                        return true;
                    }
                });
            }

            con = (HttpsURLConnection) msg.getURL().openConnection();
            if (null == con)
            {
                throw new MessengerException("messenger.connection.failure", "Could not open connection to " + msg
                        .getURL());
            }
            con.setRequestMethod(msg.getMethodString());
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept-Charset", "utf-8");
            if (authorization != null)
            {
                con.setRequestProperty("Authorization", authorization);
            }
            if (authentication != null)
            {
                con.setRequestProperty("Authentication", authentication);
            }
            con.setUseCaches(false);
            con.setDoInput(true);
            if (msg.hasBody())
            {
                con.setDoOutput(true);
            }
            con.setConnectTimeout(SecureConnectorDefaultProperties.getConnectionTimeout());
            con.setReadTimeout(SecureConnectorDefaultProperties.getConnectionTimeout());

            for (Entry<String, String> entry : msg.getHeaders().entrySet())
            {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

            return con;
        }
        catch (MalformedURLException e)
        {
            if (logger.isDebugEnabled())
            {
                System.out.println(e);
                PrintStream ps = System.out;
                if (ps != null && ps != System.out && ps != System.err)
                    ps.close();
                System.setOut(System.out);
            }

            throw new MessengerException("messenger.url.error", e.getMessage());
        }
        catch (IOException e)
        {
            // problem with the http connection - cannot be opened
            if (logger.isDebugEnabled())
            {
                System.out.println(e);
                PrintStream ps = System.out;
                if (ps != null && ps != System.out && ps != System.err)
                    ps.close();
                System.setOut(System.out);
            }

            throw new MessengerException("messenger.connection.error", e.getMessage());
        }
        finally
        {
            if (con != null)
            {
                con.disconnect();
            }
        }
    }

    private static String getStringFromStream(InputStream stream) throws IOException
    {
        if (null == stream)
        {
            throw new IOException("Peer input stream is null. connection might be closed");
        }

        StringBuffer responseBuffer = new StringBuffer("");
        char[] buffer = new char[512];
        InputStreamReader inStream = new InputStreamReader(stream);
        int size = 0;

        while (((size = inStream.read(buffer, 0, 512)) != -1))
        {
            if (size > 0)
                responseBuffer.append(buffer, 0, size);
        }
        inStream.close();

        return responseBuffer.toString();
    }

    public static HttpResponse sendMessage(HttpRequest msg) throws MessengerException, IOException, PropertiesException
    {
        return sendMessage(msg, SecureConnectorProperties.instance().getAuthorization(), SecureConnectorProperties
                .instance().getAuthentication());
    }

    public static HttpResponse sendMessage(HttpRequest msg, String authorization, String authentication)
            throws MessengerException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug(msg.getMethodString() + ": " + msg.getURL().toString()
                    + " authentication: " + authentication + " authorization: " + authorization);

        HttpsURLConnection con = getHttpsConnection(msg, authorization, authentication);
        HttpResponse response = new HttpResponse();

        try
        {
            String message = new String("[SecureConnectorMessenger::sendMessage] " + con.getRequestMethod()
                    + " " + msg.getURL() + " " + con.getRequestProperties());
            if (msg.hasBody())
            {
                String eol = System.getProperty("line.separator");
                message += new String(" Body:" + eol + Utils.toPrettyFormat(msg.getBody(), true));
            }
            if (logger.isTraceEnabled())
                logger.trace(message);

            con.connect();

            if (!SecureConnectorDefaultProperties.getAllowSelfSignedCerts())
                verifyCN(con);

            if (msg.hasBody())
            {
                String body = msg.getBody().toString();
                Writer req = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                req.write(body);
                req.flush();
                req.close();
            }

            response.setResponseCode(con.getResponseCode());
            response.setResponse(con.getResponseMessage());
            if (!response.success())
            {
                if (response.hasError())
                {
                    response.setError(getStringFromStream(con.getErrorStream()));
                }
            }
            else if (null != con.getInputStream())
            {
                response.setResponse(getStringFromStream(con.getInputStream()));
            }
        }
        catch (IOException e)
        {
            response.setResponse(new MessengerException("messenger.connection.failure").getMessage());
            response.setException(e);

            try
            {
                response.setError(getStringFromStream(con.getErrorStream()));
            }
            catch (IOException e1)
            {
                // ignore this error; it means there was a problem getting the error stream, but there's really nothing we
                // can do about it and it could be normal
            }

            // need to rethrow in order to make it easier for the caller to handle errors
            throw e;
        }
        finally
        {
            if (con != null)
            {
                con.disconnect();
            }
            if (logger.isDebugEnabled())
            {
                PrintStream ps = System.out;
                if (ps != null && ps != System.out && ps != System.err)
                    ps.close();
                System.setOut(System.out);
            }
        }
        if (logger.isTraceEnabled())
        {
            // writing large strings to the console can crash eclipse
            if (response.toString().length() <= 4096)
            {
                logger.trace("[SecureConnectorMessenger::sendMessage] Response " + response.toString());
            }
            else
            {
                logger.trace("[SecureConnectorMessenger::sendMessage] Response longer than 4096 bytes");
            }
        }
        return response;
    }

    /**
     * Download a file from remote URL to local disk and then verify sha-256 checksum.
     * 
     * @param downloadURL
     *            - File Download URL
     * @param filename
     *            - Local file where the downloaded file should be stored
     * @param sum
     *            - SHA-256 Checksum validation
     * 
     * @return true if downloaded file and verified (if sum provided), false otherwise
     * 
     * @throws MessengerException
     * @throws IOException
     */
    public static boolean downloadFile(String downloadURL, Path filename, String sum) throws MessengerException,
            IOException
    {
        // Construct the download message
        URL url = new URL(downloadURL);

        // In the case that the file already exists delete it
        filename.toFile().delete();

        // File input and output streams
        InputStream is = null;
        FileOutputStream fos = null;

        try
        {
            // Open up the download URL connection
            URLConnection urlConn = url.openConnection();

            // Fetch the input stream for the download
            is = urlConn.getInputStream();

            // Setup the file output stream for local
            fos = new FileOutputStream(filename.toFile());

            byte[] buffer = new byte[4096];
            int len;

            // While we have available data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0)
            {
                fos.write(buffer, 0, len);
            }

            // Close the readers and writer so we can do check sum SHA-256 validation
            is.close();
            fos.close();

            if (logger.isTraceEnabled())
                logger.trace("[SecureConnectorMessenger::downloadFile] Downloaded file: " + downloadURL + " to "
                        + filename);

            if (!sum.isEmpty() && sum != null)
            {
                // Setup download file inputstream and run a sha256hex value fetch
                FileInputStream localFileInputStream = new FileInputStream(filename.toFile());
                String filehash = DigestUtils.sha256Hex(localFileInputStream);

                // Close things up
                localFileInputStream.close();

            	if (logger.isTraceEnabled())
                    logger.trace("[SecureConnectorMessenger::downloadFile] Calculated SHA-256: " + filehash);

                if (filehash.equals(sum))
                {
                    if (logger.isTraceEnabled())
                        logger.trace(
                            "[SecureConnectorMessenger::downloadFile] Calculated SHA-256 and expected SHA-256 match. Successfully downloaded.");
                    return true;
                }
                else
                {
                    logger.error(
                            "[SecureConnectorMessenger::downloadFile] Calculated SHA-256 and expected SHA-256 don't match. Failed download verification.");
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error("[SecureConnectorMessenger::downloadFile] Exception while downloading: " + downloadURL, e);
            throw new MessengerException("messenger.connection.error", e.getMessage());
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            finally
            {
                if (fos != null)
                {
                    fos.close();
                }
            }
        }
    }
}
