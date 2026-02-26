/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;




/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.guardium.data.SecurityAssessment;
import com.guardium.map.AssessmentResultHeaderMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.SecurityAssessmentMap;
import com.guardium.map.TestResultMap;
import com.guardium.runtest.AssessmentRunner;
import com.guardium.runtest.VATestRun;
import com.guardium.utils.AdHocLogger;
//import com.guardium.utils.CreatePdfFile;
import com.guardium.utils.WriteResult;

import java.beans.*;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestProgress extends JPanel
                             implements ActionListener, 
                                        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
	private VATestRun dsRun;

    public class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
    	int progress = 0;
    	
        @Override        
        public Void doInBackground() {
            Random random = new Random();
            //int progress = 0;
            //Initialize progress property.
            setProgress(progress);
            
            //System.out.println("doInBackground start");
            dsRun = new VATestRun();
            dsRun.run(taskOutput);
            
            // real run the test and generate the result
            real_run();
            
            return null;
        }
        
        public void new_gui_run_test (int idx) {
        	// set up test to run
            dsRun = new VATestRun();
            dsRun.run(idx);
            
            // real run the test and generate the result
            real_run();
            
            return;	
        }
        
        public void real_run () {
    		SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
    		TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
    		AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
    		
    		// create Datasource, put in Datasource list
    		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();

            List<SecurityAssessment> SecurityAssessmentList = new ArrayList<SecurityAssessment>();
    		
            progress += 10;
            setProgress(progress);
            
    		WriteResult.writeOutput("Run the assessment tests;");
    		// Run the tests
    		SecurityAssessmentList = SecurityAssessmentPeer.getAllList();
    		
    		int sstSize = SecurityAssessmentList.size();
    		WriteResult.writeOutput("Run the assessment tests size is " + sstSize);
    		int incr = 90/sstSize;
    		String outputDir = VATest.VAOutputDir + "result";
     		if (VATest.VACurrentOS.equals("window")) {
     			outputDir = outputDir + "\\";
     		}
     		else {
     			outputDir = outputDir + "/";
     		}

     		String outputFile = "";
    		
    		//OuterClass.InnerClass innerObject = outerObject.new InnerClass()                                                                                                                                                                                               
    		// GUI - list SecurityAssessment name
    		for (SecurityAssessment sst : SecurityAssessmentList) {  
    			
    			WriteResult.writeOutput("\n\n");
    			sst.dump();
    			
    			// create output result file for each sst
    			outputFile = outputDir + sst.getAssessmentDesc();
                Writer outputwrite = WriteResult.createLogFile(outputFile);
                
    			TestResultPeer.cleanMap();
    			
    			// run the tests
    			AssessmentRunner assrun = new AssessmentRunner();
    			assrun.setSecurityAssessment(sst);
    			
    			//
    			try {
    				assrun.run();
    			} catch (Exception e) {
    				AdHocLogger.logException(e);
    				//LOG.error(e);
    				// setStringDetail( Informer.causality(e) );
    			}
    			
    			// test result
    			WriteResult.writeOutput("TestResult map size is "
    					+ TestResultPeer.getMapSize());
    			WriteResult.writeOutput("");
    			if (TestResultPeer.getMapSize() > 0) {
    				
    				// GUI - use this list to display the result list
    				List <String> alist = new ArrayList<String>();
    				alist = TestResultPeer.getTestDescList();
    				
    				// GUI - generate pdf file for each test result
    				TestResultPeer.dumpMap(outputwrite);
    				
    				// GUI - when click the item in the list, it will display pdf result
    				
    				
    				// GUI - generate pdf file for test summary
    				WriteResult.writeOutput("\nTestResult summary is ");
    				int t[] = TestResultPeer.getSummary (4);
    				sst.setTestSummary(t);
    				
    				WriteResult.writeOutput("");
        			sst.dumpResult(outputwrite);
        			
        			int tt[] = TestResultPeer.getSummary (6);
    				sst.setTestSummary(tt);
    				
    				WriteResult.writeOutput("");
        			sst.dumpResult(outputwrite);
        			
        			WriteResult.closeLogFile(outputwrite);
    			}
    			
    			progress += incr;
                setProgress(Math.min(progress, 100));    			
    		}

    		setProgress(100);
    		
    		WriteResult.writeOutput("SecurityAssessment list size is "
    				+ SecurityAssessmentList.size());

    		//AssessmentLogPeer.dumpMap();
    		//taskOutput.append(String.format("AssessmentLog map size is "
    		//		+ AssessmentLogPeergetMapSize()));		
    		
    		// result header
    		WriteResult.writeOutput("Assessment Result Header map size is "
    				+ AssessmentResultHeaderPeer.getMapSize());
    		//AssessmentResultHeaderPeer.dumpMap();
    		
    		
    		
    		//WriteResult.writeOutput("DS list size is " + datasourceList.size());

            //CreatePdfFile cf = new CreatePdfFile();
            //cf.create();
            
            
            
            
            //System.out.println("doInBackground before while");
            //while (progress < 100) {
                //Sleep for up to one second.
            //    try {
            //        Thread.sleep(random.nextInt(1000));
            //    } catch (InterruptedException ignore) {}
                //Make random progress.
            //    progress += random.nextInt(10);
            //    setProgress(Math.min(progress, 100));
            //}
            

            return;
        }    

        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            taskOutput.append("Done!\n");
        }
    }

    public TestProgress() {
        super(new BorderLayout());

        //Create the demo's UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        } 
    }


    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    /*
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressBarDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new TestProgress();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    */
}