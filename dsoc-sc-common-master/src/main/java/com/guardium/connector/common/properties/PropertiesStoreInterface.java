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

import java.util.Properties;

import com.guardium.connector.common.exceptions.PropertiesException;

public interface PropertiesStoreInterface
{
    public Properties getProperties() throws PropertiesException;
    public void saveProperties(Properties properties) throws PropertiesException;

    public String getProperty(String propName) throws PropertiesException;
    public void saveProperty(String propName, String value) throws PropertiesException;
}
