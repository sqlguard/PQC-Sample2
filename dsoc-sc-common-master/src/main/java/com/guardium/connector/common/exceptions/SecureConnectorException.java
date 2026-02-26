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

package com.guardium.connector.common.exceptions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.Utils;

abstract public class SecureConnectorException extends Exception
{
    private static final long serialVersionUID = 1L;
    private static ResourceBundle bundle;
    private String key;
    private String details;

    public static Logger logger;

    public SecureConnectorException(String key, String details)
    {
        super(key);
        this.key = key;
        this.details = details;
        logger = LoggerUtils.getLogger(SecureConnectorException.class);
    }

    public SecureConnectorException(String message)
    {
        super(message);
    }

    @Override
    public String getMessage()
    {
        if (null == details)
        {
            return super.getMessage();
        }
        else if (null == key)
        {
            return getString(super.getMessage()) + details;
        }
        else
        {
            String bundleString = getString(key);
            String space = !bundleString.isEmpty() && !bundleString.endsWith(" ") ? " " : "";
            return bundleString + space + details;
        }
    }

    public String getKey()
    {
        return super.getMessage();
    }

    public JsonObject toJson()
    {
        JsonObject jo = new JsonObject();
        jo.addProperty("error", getMessage());
        return jo;
    }

    public String toString()
    {
        return Utils.toPrettyFormat(toJson());
    }

    protected synchronized void initBundle(String bundleName) throws MissingResourceException
    {
        bundle = ResourceBundle.getBundle(bundleName);
    }

    /*
     * Get the resource bundle (file) for this exception. If one doesn't exist, check to see if one exists for the superclass and use that. This will allow for the easy use of subclasses of exceptions
     * without requiring that they have a separate resource file.
     */
    protected synchronized String getString(String key)
    {
        String res = "";

        if (null == bundle || (!bundle.getBaseBundleName().equalsIgnoreCase(this.getClass().getName())
                || !bundle.getBaseBundleName().equalsIgnoreCase(this.getClass().getSuperclass().getName())))
        {
            try
            {
                bundle = ResourceBundle.getBundle(this.getClass().getName());
            }
            catch (MissingResourceException e)
            {
                String superClass = this.getClass().getSuperclass().getName();
                if (null != superClass && !superClass.isEmpty())
                {
                    bundle = ResourceBundle.getBundle(superClass);
                }
                else
                {
                    throw e;
                }
            }
        }

        try
        {
            res = bundle.getString(key);
        }
        catch (ClassCastException | MissingResourceException | NullPointerException e)
        {
            logger.warn("Could not find key " + key + " in bundle " + bundle.getBaseBundleName());
        }

        return res;
    }
}
