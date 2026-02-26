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

package com.guardium.connector.common.properties;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.exceptions.PropertiesException;

public class SecureConnectorProperties implements PropertiesStoreInterface
{
    private static Logger logger = LoggerUtils.getLogger(SecureConnectorProperties.class);

    private static SecureConnectorProperties instance = null;
    private static PropertiesStoreInterface store = null;

    private static HashSet<String> maskedProperties = Stream.of("authorization", "authentication", "privKey").collect(
            Collectors.toCollection(HashSet::new));

    private Properties cache;

    private SecureConnectorProperties() throws PropertiesException
    {
        cache = store.getProperties();
    }

    public String getName() throws PropertiesException
    {
        return instance().getProperty("name");
    }

    public void setName(String name) throws PropertiesException
    {
        instance().saveProperty("name", name);
    }

    public String getDescription() throws PropertiesException
    {
        return instance().getProperty("description");
    }

    public void setDescription(String description) throws PropertiesException
    {
        instance().saveProperty("description", description);
    }

    public String getPublicKey() throws PropertiesException
    {
        return instance().getProperty("pubKey");
    }

    public void setPublicKey(String pubKey) throws PropertiesException
    {
        instance().saveProperty("pubKey", pubKey);
    }

    public String getPublicKeyModulus() throws PropertiesException
    {
        return instance().getProperty("pubKeyModulus");
    }

    public void setPublicKeyModulus(String pubKeyModulus) throws PropertiesException
    {
        instance().saveProperty("pubKeyModulus", pubKeyModulus);
    }

    public String getPublicKeyExponent() throws PropertiesException
    {
        return instance().getProperty("pubKeyExponent");
    }

    public void setPublicKeyExponent(String pubKeyExponent) throws PropertiesException
    {
        instance().saveProperty("pubKeyExponent", pubKeyExponent);
    }

    public String getPrivateKey() throws PropertiesException
    {
        return instance().getProperty("privKey");
    }

    public void setPrivateKey(String privKey) throws PropertiesException
    {
        instance().saveProperty("privKey", privKey);
    }

    public String getAuthentication() throws PropertiesException
    {
        return instance().getProperty("authentication");
    }

    public void setAuthentication(String authentication) throws PropertiesException
    {
        instance().saveProperty("authentication", authentication);
    }

    public String getAuthorization() throws PropertiesException
    {
        return instance().getProperty("authorization");
    }

    public void setAuthorization(String authorization) throws PropertiesException
    {
        // need to set the default property as well as saving it to the db
        SecureConnectorDefaultProperties.setAuthorization(authorization);
        instance().saveProperty("authorization", authorization);
    }

    public String getFirstName() throws PropertiesException
    {
        return instance().getProperty("firstName");
    }

    public void setFirstName(String firstName) throws PropertiesException
    {
        instance().saveProperty("firstName", firstName);
    }

    public String getLastName() throws PropertiesException
    {
        return instance().getProperty("lastName");
    }

    public void setLastName(String lastName) throws PropertiesException
    {
        instance().saveProperty("lastName", lastName);
    }

    public String getEmail() throws PropertiesException
    {
        return instance().getProperty("email");
    }

    public void setEmail(String email) throws PropertiesException
    {
        instance().saveProperty("email", email);
    }

    public Date getLoginTime() throws PropertiesException
    {
        Date res = null;
        String stringTime = instance().getProperty("lastLoginTime");
        try
        {
            if (null != stringTime && !stringTime.isEmpty())
            {
                res = new Date(Long.parseLong(instance().getProperty("lastLoginTime")));
            }
        }
        catch (NumberFormatException e)
        {
            // not much we can do here, just let the method return null
        }
        return res;
    }

    public void setLoginTime(Date time) throws PropertiesException
    {
        instance().saveProperty("lastLoginTime", Long.toString(time.getTime()));
    }

    public static synchronized SecureConnectorProperties instance() throws PropertiesException
    {
        if (null == store)
        {
            throw new PropertiesException("properties.store.error", "No properties store");
        }

        if (instance == null)
        {
            instance = new SecureConnectorProperties();
        }
        return instance;
    }

    public static synchronized void setPropertiesStore(PropertiesStoreInterface store)
    {
        if (null == store)
        {
            return;
        }

        // allow the old instance to be garbage collected
        instance = null;

        SecureConnectorProperties.store = store;
    }

    public Properties getMaskedProperties()
    {
        Properties masked = new Properties();

        if (cache != null)
        {
            Enumeration<?> enums = cache.propertyNames();
            while (enums.hasMoreElements())
            {
                String key = (String) enums.nextElement();
                String value = cache.getProperty(key);
                masked.setProperty(key, maskedProperties.contains(key) ? value.substring(0, Math.min(6, value.length()))
                        + "******" : value);
            }
        }

        return masked;
    }

    @Override
    public Properties getProperties()
    {
        return cache;
    }

    public Properties reloadProperties() throws PropertiesException
    {
        if (null == store)
        {
            logger.warn("Problem with persistent properites store.  Properites will not be saved.");
        }
        else
        {
            cache = store.getProperties();
        }
        return cache;
    }

    @Override
    public void saveProperties(Properties properties) throws PropertiesException
    {
        cache = properties;
        if (null == store)
        {
            logger.warn("Problem with persistent properites store.  Properites will not be saved.");
        }
        else
        {
            store.saveProperties(properties);
        }
    }

    @Override
    public String getProperty(String propName) throws PropertiesException
    {
        return cache.getProperty(propName);
    }

    @Override
    public void saveProperty(String propName, String value) throws PropertiesException
    {
        // logger.debug("Caching " + propName + "=[" + value + "]");
        cache.setProperty(propName, value);
        if (null == store)
        {
            logger.warn("Problem with persistent properites store.  Properites will not be saved.");
        }
        else
        {
            store.saveProperty(propName, value);
        }
    }

}
