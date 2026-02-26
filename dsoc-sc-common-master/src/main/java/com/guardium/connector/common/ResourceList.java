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

package com.guardium.connector.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ResourceList
{
    public class NonJarFileFilter implements FileFilter
    {
        Pattern pattern = null;

        public NonJarFileFilter(Pattern lookFor)
        {
            this.pattern = lookFor;
        }

        public boolean accept(File pathname)
        {
            return pattern.matcher(pathname.getName()).matches();
        }
    }

    public static Map<String, String> getResourcesFromClassPath(final Pattern lookFor, Pattern inJars)
    {
        final HashMap<String, String> retval = new HashMap<String, String>();

        ClassLoader classLoader = ResourceList.class.getClassLoader();
        URL[] classPathUrls = ((URLClassLoader) classLoader).getURLs();

        // go through the classpath in reverse order -- this is useful for callers who want to
        // use Properties.putAll (which overwrites values) and who want to make the first resources 
        // file in found in the the class path immutable
        for (int i = classPathUrls.length - 1; i >= 0; i--)
        {
            retval.putAll(getResourcesFromClassPath(classPathUrls[i], lookFor, inJars));
        }
        
        return retval;
    }

    private static Map<String, String> getResourcesFromClassPath(
            final URL url, final Pattern lookFor, final Pattern inJars)
    {
        final HashMap<String, String> retval = new HashMap<String, String>();

        File file;
        try
        {
            file = Paths.get(url.toURI()).toFile();
        }
        catch (URISyntaxException e)
        {
            // if the URI is bad just skip it
            return retval;
        }
        
        if (file.isDirectory())
        {
            retval.putAll(getResourcesFromDirectory(file, lookFor, inJars));
        }
        else if (inJars.matcher(file.getName()).matches())
        {
            retval.putAll(getResourcesFromJarFile(file, lookFor));
        }

        return retval;
    }

    private static Map<String, String> getResourcesFromDirectory(final File directory, final Pattern lookFor,
            final Pattern inJars)
    {
        Map<String, String> retval = new HashMap<String, String>();
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (lookFor.matcher(file.getAbsolutePath()).matches())
            {
                // System.out.println("Found " + file.getAbsolutePath());
                retval.put(file.getAbsolutePath(), file.getName());
            }
            else if (file.isDirectory())
            {
                retval.putAll(getResourcesFromDirectory(file, lookFor, inJars));
            }
            else if (inJars.matcher(file.getName()).matches())
            {
                retval.putAll(getResourcesFromJarFile(file, lookFor));
            }
        }
        return retval;
    }

    public static Map<String, String> getResources(
            final Pattern lookFor, final Pattern inJars)
    {
        final HashMap<String, String> retval = new HashMap<String, String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements)
        {
            retval.putAll(getResources(element, lookFor, inJars));
        }
        return retval;
    }

    private static Map<String, String> getResources(
            final String element,
            final Pattern lookFor,
            final Pattern inJars)
    {
        final HashMap<String, String> retval = new HashMap<String, String>();
        final File file = new File(element);
        if (!file.isDirectory() && inJars.matcher(file.getName()).matches())
        {
            retval.putAll(getResourcesFromJarFile(file, lookFor));
        }

        return retval;
    }

    private static Map<String, String> getResourcesFromJarFile(
            final File file,
            final Pattern lookFor)
    {
        final HashMap<String, String> retval = new HashMap<String, String>();
        ZipFile zf;

        try
        {
            zf = new ZipFile(file);
        }
        catch (final ZipException e)
        {
            throw new Error(e);
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }
        final Enumeration<?> e = zf.entries();
        while (e.hasMoreElements())
        {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName().replaceAll(".*" + "/", "");
            final boolean accept = lookFor.matcher(fileName).matches();
            if (accept)
            {
                try
                {
                    // System.out.println("Found " + file.getAbsolutePath());
                	if(retval.containsKey(file.getAbsolutePath()))
                		retval.put(file.getAbsolutePath(), ze.getName()+"|"+retval.get(file.getAbsolutePath()));
                	else
                		retval.put(file.getAbsolutePath(), ze.getName());
                }
                catch (Exception ee)
                {

                }
            }
        }
        try
        {
            zf.close();
        }
        catch (final IOException e1)
        {
            throw new Error(e1);
        }
        return retval;
    }

    public static void main(String[] srgs) throws Exception
    {
        // System.out.println("dsoc-sc-classifier-1.0.jar.jar".matches("dsoc-sc-.*-\\d+.*\\.jar"));
        System.out.println(getResources(Pattern.compile(".*\\.plugin.properties"), Pattern.compile(
                "dsoc-sc-.*-\\d+.*?\\.jar")));
        // System.out.println(getResources(Pattern.compile("ClassifierResources.properties"), Pattern.compile(".*")));
    }
}
