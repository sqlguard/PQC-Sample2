/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainRegistry {

    static final String KEY_VALUE_NAME = "KeyFile";
    static final String KEY_NAME = "SOFTWARE\\IBM\\Guardium\\VA";
    static final String DATE_FORMAT = "dd/MM/yy";

    public static long GetNumberOfDays() {

        //get Today's date
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date dateObj = new Date();
        String strCurrDate = df.format(dateObj);
        String strInstallDate = GetInstallDateFromRegistry();
        if ( strInstallDate != null){
            try {
                    Date currDate = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(strCurrDate);
                    Date instDate = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(strInstallDate);
                    long cDate = currDate.getTime();
                    long iDate = instDate.getTime();
                    long diffDay = ( cDate - iDate );
                    if ( diffDay > 0 ) {
                        diffDay = diffDay / ( 1000 * 60 * 60 * 24);
                        return diffDay;
                    }
                    else {
                        //don't try to divide, just return 0 days
                        return 0;
                    }
            }
            catch ( Exception e) {
                return -1;
            }
        }
        return -1;
    }


/* Tries to read and decrypt the date from the registry
    Returns either the date as String, or null on failure
 */
    public static String GetInstallDateFromRegistry() {

        try {
            String keyValue = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, KEY_NAME, KEY_VALUE_NAME);
            //Crypto.init();
            return Crypto.decrypt(keyValue);
        } catch (Exception e) {
            return null;
        }
    }

    /*
    Write the encrypted current date to the registry, call only once during startup/license
    */
    public static Boolean WriteInstallDateToRegistry() {

        try {
            WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, KEY_NAME);
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            Date dateObj = new Date();
            String currDate = df.format(dateObj);
            String ciphertext = Crypto.encrypt(currDate);
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER,KEY_NAME,KEY_VALUE_NAME,ciphertext);
            return true;

        } catch (Exception e) {
            return false;
        }

    }


    public static void main(String[] args) {
    	try {
    		Crypto.init();
    	}
    	catch (Exception e) {
    		System.err.println("Init Crypto error");
    	}
        boolean wres = WriteInstallDateToRegistry();
        long res = GetNumberOfDays();
        System.err.println(res);
    }
}





