/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2011, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.util;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import com.guardium.connector.common.LoggerUtils;

/**
 * A convenience class for dealing with resource bundles and formatting messages
 */
public class ResourceBundleHelper
{
    private static final Class<ResourceBundleHelper> clazz = ResourceBundleHelper.class;
    private static final String CLASS_NAME = clazz.getName();
    private static final Logger logger = LoggerUtils.getLogger(clazz);

    private ResourceBundle _bundle = null;

    private MessageFormat  _formatter = null;

    private String         _bundleName = "NULL";

    private static class StackCrawler extends SecurityManager
    {
        public StackCrawler() {}

        public Class<?>[] getClassContext()
        {
            return super.getClassContext();
        }

        public ClassLoader getCallingLoader()
        {
            return getClassContext()[3].getClassLoader();
        }
    }

    private static StackCrawler crawler = new StackCrawler();

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     */
    public ResourceBundleHelper(String bundleName)
    {
        loadResourceBundle(bundleName);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param clazz
     *            a class used to determine the fully-qualified bundle name
     *            for the class package
     */
    public ResourceBundleHelper(Class<?> clazz)
    {
        String bundleName = getResourceBundleName(clazz);
        if (logger.isDebugEnabled())
        {
            logger.debug(CLASS_NAME + "(Class clazz) entry: loading bundle: " + bundleName);
        }         
        loadResourceBundle(bundleName);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     * @param locale
     *            the Locale
     */
    public ResourceBundleHelper(String bundleName, Locale locale)
    {
        ResourceBundle bundle = null;
        try {
            ClassLoader cl = crawler.getCallingLoader();
            bundle = ResourceBundle.getBundle(bundleName, locale, cl);
            _bundleName = bundleName;
        }
        catch (Throwable t)
        {
            System.out.println("Unable to load bundle " + bundleName);
            System.out.println("Continuing with bundle=null");
            System.out.println("Printing StackTrace from " + CLASS_NAME + "(String bundleName, Locale locale)");
            t.printStackTrace();
        }
        _bundleName = bundleName;
        init(bundle);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     * @param locale
     *            the Locale
     * @param cl
     *            the class loader
     */
    public ResourceBundleHelper(String bundleName, Locale locale, ClassLoader cl)
    {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, cl);
        _bundleName = bundleName;
        init(bundle);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     * @param locales
     *            the array of Locale
     * @param cl
     *            the class loader
     */
    public ResourceBundleHelper(String bundleName, Locale[] locales, ClassLoader cl)
    {
        ResourceBundle bundle = null;
        boolean found = false;
        int i = 0;
        while ((! found) && (locales[i] != null))
        {
            bundle = ResourceBundle.getBundle(bundleName, locales[i], cl);

            if (locales[i].equals(bundle.getLocale()))
                found = true;
            else
                i++;
        }
        _bundleName = bundleName;
        init(bundle);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     * @param locales
     *            the array of Locale
     */
    public ResourceBundleHelper(String bundleName, Locale[] locales)
    {
        ResourceBundle bundle = null;
        boolean found = false;
        int i = 0;

        while ((! found) && (locales[i] != null))
        {
            ClassLoader cl = crawler.getCallingLoader();
            bundle = ResourceBundle.getBundle(bundleName, locales[i], cl);

            if (locales[i].equals(bundle.getLocale()))
                found = true;
            else
                i++;
        }
        _bundleName = bundleName;
        init(bundle);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundleName
     *            the fully-qualified bundle name
     * @param cl
     *            the class loader
     */
    public ResourceBundleHelper(String bundleName, ClassLoader cl)
    {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), cl);
        _bundleName = bundleName;
        init(bundle);
    }

    /**
     * Constructor for ResourceBundleHelper.
     *
     * @param bundle
     *            the ResourceBundle we're helping
     */
    public ResourceBundleHelper(ResourceBundle bundle)
    {
        init(bundle);
        _bundleName = bundle.getClass().getName();
    }

    /**
     * Get the resource bundle using the caller's classloader and default locale
     *
     * @param bundleName
     */
    private void loadResourceBundle (String bundleName)
    {
        ResourceBundle bundle = null;
        try {
            Locale locale = Locale.getDefault();
            if (logger.isDebugEnabled())
            {
                logger.debug("Loading bundle " + bundleName);
                logger.debug(" - Locale is " + ((null == locale) ? "NULL" : locale.toString()));
            }
            if (logger.isTraceEnabled())
            {
                logger.trace(" - Crawler is : " + crawler.getClass().getCanonicalName());
            }
            ClassLoader cl = crawler.getCallingLoader();
            if (logger.isTraceEnabled())
            {
                logger.trace(" - ClassLoader is " + ((null == cl) ? "NULL" : cl.getClass().getCanonicalName()));
            }

            bundle = ResourceBundle.getBundle(bundleName, locale, cl);
            _bundleName = bundleName;
        }
        catch (Throwable t)
        {
            System.out.println("Unable to load bundle " + bundleName);
            System.out.println("Continuing with bundle=null");
            System.out.println("Printing StackTrace from " + CLASS_NAME + "(String bundleName)");
            t.printStackTrace();
        }
        init(bundle);
    }

    /**
     * Given a class, return the default bundle name following WPLC message resource file
     * naming (message bundle name must match its package name.  For example, message file
     * for package com.ibm.workplace.foo must be foo.properties)
     *
     * @param clazz
     * @return
     */
    private String getResourceBundleName(Class<?> clazz) 
    {
        String pkg = null;
        String className = clazz.getName();
        int i = className.lastIndexOf(".");
        pkg = className.substring(0, i);
        i = pkg.lastIndexOf(".");
        pkg = pkg + pkg.substring(i);
        return pkg;
    }

    private void init(ResourceBundle bundle)
    {
        if (null != bundle) {
            _bundle = bundle;
            _formatter = new MessageFormat("");
            _formatter.setLocale(_bundle.getLocale());
        }
        else {
            _bundle = null;
            _formatter = new MessageFormat("");
        }
    }

    /**
     * Constructor for ResourceBundleHelper.
     */
    @SuppressWarnings("unused")
    private ResourceBundleHelper()
    {
        super();
    }

    /**
     * Get the name of this bundle
     *
     * @return BundleName
     */

    public String getBundleName()
    {
        return _bundleName;
    }

    /**
     * Allows the _bundleName to be set if the ResourceBundleHelper(ResourceBundle) <br>
     * constructor was used because the ResourceBundle.name is not accessible
     */

    public void setBundleName(String bn)
    {
        if (_bundleName == null)
        {
            _bundleName = bn;
        }
    }

    public Locale getLocale()
    {
        return _bundle.getLocale();
    }

//    public Enumeration<String> getKeys()
//    {
//        return _bundle.getKeys();
//    }
//

    /**
     * Convenience method to return a resource string
     *
     * @param key
     *            the resource key
     * @return String
     */
    public String getString(String key) throws MissingResourceException
    {
        return _bundle.getString(key);
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param params
     *            an array of arguments
     * @return String
     */
    public String getString(String key, Object[] params) throws MissingResourceException
    {
        String message = _bundle.getString(key);
        String formattedMsg = MessageFormat.format(message, params);
        return formattedMsg;
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param param1
     * @return String
     */
    public String getString(String key, String param1) throws MissingResourceException
    {
        String params[] = new String[] { param1 };
        return getString(key, (Object[]) params);
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param param1
     * @param param2
     * @return String
     */
    public String getString(String key, String param1, String param2) throws MissingResourceException
    {
        String params[] = new String[] { param1, param2 };
        return getString(key, (Object[]) params);
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param param1
     * @param param2
     * @param param3
     * @return String
     */
    public String getString(String key, String param1, String param2, String param3) throws MissingResourceException
    {
        String params[] = new String[] { param1, param2, param3 };
        return getString(key, (Object[]) params);
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param param1
     * @param param2
     * @param param3
     * @param param4
     * @return String
     */
    public String getString(String key, String param1, String param2, String param3, String param4) throws MissingResourceException
    {
        String params[] = new String[] { param1, param2, param3, param4 };
        return getString(key, (Object[]) params);
    }

    /**
     * Convenience method to return a resource string with argument replacement
     *
     * @param key
     *            the resource key
     * @param params
     * @return String
     */
    public String getString(String key, String[] params) throws MissingResourceException
    {
        return getString(key, (Object[]) params);
    }

}
