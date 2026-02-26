/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.awt.HeadlessException;
import javax.swing.SwingUtilities;

public class VATest
{
	//public static JFrame dsFind;
	public static DatasourceFinder dsFind;
	public static String VAOutputDir_win  = "c:\\temp\\";
	public static String VAOutputDir_unix = "/var/tmp/";
	public static String VAOutputDir = VAOutputDir_unix;
	public static String VACurrentOS = "unix";
    public static void main(String[] args)
    {
    	   
    	if (System.getProperty("os.name").startsWith("Windows")) {
    	    // includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
    		VAOutputDir = VAOutputDir_win;
    		VACurrentOS = "window";
    	}

    	   
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run()
            {   
                createGUI();
            }
        });
    }

    private static void createGUI() throws HeadlessException
    {
        dsFind = new DatasourceFinder();
        dsFind.setVisible(true);
    }
}

