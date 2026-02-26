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

package com.guardium.test.helpers.annotations;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.guardium.connector.common.env.Environment;

public class DisableForProdCertCondition implements ExecutionCondition
{
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
    {
        if (Environment.ALLOW_SELF_SIGNED_CERTS)
        {
            return ConditionEvaluationResult.enabled("Test enabled");
        }
        else
        {
            return ConditionEvaluationResult.disabled("Test disabled because ALLOW_SELF_SIGNED_CERTS is false");
        }
    }
}
