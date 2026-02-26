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

package com.guardium.connector.common.util;

import org.apache.log4j.Logger;

public class LoggingUtils
{
    private Logger logger = null;

    public LoggingUtils(Logger callerLogger)
    {
        logger = callerLogger;
    }

    public void reportError(String errMsg, Exception e)
    {
        reportError(errMsg, e, null);
    }
    public void reportError(String errMsg, Exception e, String caller)
    {
        logger.error(errMsg);
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder("[");
            if (null != caller)
            {
                sb.append(caller);
                sb.append(":: ");
            }
            sb.append("exception] : ");
            sb.append(e.getMessage());
            logger.debug(sb.toString());
        }
        if (logger.isTraceEnabled())
            e.printStackTrace();
    }

}
