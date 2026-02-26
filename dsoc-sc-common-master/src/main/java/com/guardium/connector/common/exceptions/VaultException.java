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

public class VaultException extends SecureConnectorException
{
    private static final long serialVersionUID = 3241671252182237813L;

    public VaultException(String key)
    {
        super(key);
    }

    public VaultException(String key, String details)
    {
        super(key, details);
    }
}
