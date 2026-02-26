/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

//import java.awt.BorderLayout;
import java.awt.Container;
//import java.awt.Dimension;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
//import javax.swing.JComponent;
//import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
//import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.guardium.data.Datasource;
//import com.guardium.data.DbDriver;
import com.guardium.map.DatasourceMap;
//import com.guardium.runtest.VATestRun;
import com.guardium.gui.DatasourceCreator;
import java.awt.BorderLayout;
//import java.io.File;


public class DatasourceFinder extends JFrame implements ActionListener
{
	// create Datasource, put in Datasource list
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	
   	private JFrame dsFindFrame;
   	//private JFrame dsCreateFrame;
   	//private JFrame dsExecuteFrame;
   	
   	JLabel jlbDsFinder;
   
  
   	JButton jbnNew, jbnDelete, jbnModify, jbnExecute, jbnExit;
   	
   	JList jlistType;
   	DefaultListModel jdlistModel;
   	JScrollPane listScrollPane;
   	
   	Container cPane;
   	
   	JPanel dsPanel;
   	JPanel progPanel;
   	JPanel outputPanel;
   	
   	public DefaultListModel getListModel () {
   		return jdlistModel;
   	}
   	
   	public static void main(String args[]){
   		new DatasourceFinder(); 
   	}

   	public DatasourceFinder()
   	{ 		
   		createGUI();
   	}

   	public void createGUI(){

   		/*Create a frame, get its contentpane and set layout*/
   		dsFindFrame = VATest.dsFind;
   		dsFindFrame = new JFrame("Datasource Finder");

   		//cPane = dsFindFrame.getContentPane();
   		//cPane.setLayout(new GridBagLayout());
   		
   		dsPanel = new JPanel();
   		dsPanel.setLayout(new GridBagLayout());
   		
   		//Arrange components on contentPane and set Action Listeners to each JButton
   		arrangeDSComponents();
   		//arrangeProgressComponents();
   		//arrangeOutputComponents();
   		
        //Create and set up the content pane.
        progPanel = new TestProgress();
        progPanel.setOpaque(true); //content panes must be opaque
        //dsFindFrame.setContentPane(progPanel);

   		
   		//dsFindFrame.setSize(240,300);
   		//dsFindFrame.setSize(260, 200);
   		dsFindFrame.setSize(720,900);
   		dsFindFrame.setResizable(false);
   		dsFindFrame.setVisible(true);
   		dsFindFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   		
   	    //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        //dsFindFrame.add(dsPanel, BorderLayout.NORTH);
        //dsFindFrame.add(progressPanel, BorderLayout.CENTER);
        //dsFindFrame.add(outputPanel,BorderLayout.SOUTH);
        
   		//dsFindFrame.add(dsPanel, BorderLayout.NORTH);
   		dsFindFrame.add(dsPanel, BorderLayout.PAGE_START);
   		dsPanel.setVisible(true);
   		
   		//dsFindFrame.add(progPanel, BorderLayout.SOUTH);
   		dsFindFrame.add(progPanel, BorderLayout.CENTER);
   		progPanel.setVisible(true);
   		
   		//dsFindFrame.setSize(240,300);
   		//dsFindFrame.setSize(260, 200);
   		dsFindFrame.setSize(720,900);
   		dsFindFrame.setResizable(false);
   		dsFindFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   		
   		dsFindFrame.setLocationRelativeTo(null);
   		//dsFindFrame.pack();
   		dsFindFrame.setVisible(true);
   		
   	}
   	
   	public void getList () {
   	    // datasource list
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
		
		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		System.out.println("getList DS list size is " + dsize);
		
        //Create and populate the list model.
   	   	jdlistModel = new DefaultListModel();
   	   	//jdlistModel.setSize(15);

		for (Datasource ds2: datasourceList) {

			System.out.println("\n\nDatasource info:");
			ds2.dump();
			String str = ds2.getName();
			jdlistModel.addElement(str);
		}

		return;
   	}
   	
   	private void initList () {

   		getList();
   		
        //Create the list and put it in a scroll pane.
        jlistType = new JList(jdlistModel);
        jlistType.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jlistType.setSelectedIndex(0);
        
        int hi = jlistType.getFixedCellHeight();
        int wd = jlistType.getFixedCellWidth();
        //System.out.println("\n\ncell width :" + wd + " hight " + hi);

        // set cell size
        jlistType.setFixedCellHeight(20);
        jlistType.setFixedCellWidth(300);
        listScrollPane = new JScrollPane(jlistType);      
        
        jdlistModel.addListDataListener(listDataListener);
        
   	}
   	
   	public void updateList (Datasource newds) {
   		
   		String newstr = newds.getName();
   		jdlistModel.addElement(newstr);
   		
   		listScrollPane.validate();
   		listScrollPane.repaint();
   		
   		//cPane = dsFindFrame.getContentPane();
   		dsPanel.validate();
   		dsPanel.repaint();
   		
   		dsFindFrame.validate();
   		dsFindFrame.repaint();

   		return;
   	}
   	
   	public void removeFromList (int idx) {
   		// remove from the datasourcelist 
   		String dsName = jdlistModel.getElementAt(idx).toString();
		DatasourcePeer.deleteDatasourceByName(dsName);
		
		// remove from the screen
   		jdlistModel.remove(idx);
   		listScrollPane.validate();
   		listScrollPane.repaint();
   		
   		//cPane = dsFindFrame.getContentPane();
   		dsPanel.validate();
   		dsPanel.repaint();
   		
   		dsFindFrame.validate();
   		dsFindFrame.repaint();

   		return;
   	}   	
   	
   	
   	private void arrangeProgressComponents(){

   		//JLabel jlbProgress = new JLabel("VA Test progress");
   		//TestProgress tp = new TestProgress();
   		
		progPanel = new JPanel();
   		
		progPanel.setPreferredSize( new Dimension( 310, 130 ) );
		getContentPane().add( progPanel );

		// Create a label and progress bar
		JLabel label1 = new JLabel( "Waiting to start tasks..." );
		label1.setPreferredSize( new Dimension( 280, 24 ) );
		progPanel.add( label1 );

		JProgressBar progress = new JProgressBar();
		progress.setPreferredSize( new Dimension( 300, 20 ) );
		progress.setMinimum( 0 );
		progress.setMaximum( 20 );
		progress.setValue( 0 );
		progress.setBounds( 20, 35, 260, 20 );
		progPanel.add( progress );
   		
		//progPanel.setVisible(true);
   		
   	}
   	
   	private void arrangeOutputComponents(){

		outputPanel = new JPanel();   		
   		JLabel jlbOutput = new JLabel("VA Test output");
   		outputPanel.add(jlbOutput);
   		
		outputPanel.setPreferredSize( new Dimension( 900, 600 ) );
		getContentPane().add( outputPanel );	
		outputPanel.setVisible(false);
   		
   		
   	}
   	
   	private void arrangeDSComponents(){

   		JLabel jlbDsFinder;

   	   	// DS finder
   	    jlbDsFinder = new JLabel("Datasource Finder");
   	    
   	    // datasource list
   	    initList();
   	    
   		// button
   		jbnNew   = new JButton("New");
   		jbnModify   = new JButton("Modify");
   		jbnDelete = new JButton("Delete");
   		//jbnExecute  = new JButton("Execute");
   		jbnExit  = new JButton("Exit");
   		
   		int rowidx = 0;
   		int labelidx = 0;
   		int fieldidx = 0;
   		int labelfieldwidth = 0;
   		
   		/*add all initialized components to the container*/
   		
   		// label
   		GridBagConstraints gridBagConstraintsx00 = new GridBagConstraints();
        gridBagConstraintsx00.gridx = labelidx;
        gridBagConstraintsx00.gridy = rowidx;
        gridBagConstraintsx00.insets = new Insets(5,5,5,5); 
        dsPanel.add(jlbDsFinder, gridBagConstraintsx00);   		
   		rowidx++;
   		
   		// list
        GridBagConstraints gridBagConstraintsx04 = new GridBagConstraints();
        gridBagConstraintsx04.gridx = fieldidx;
        gridBagConstraintsx04.gridy = rowidx;
        gridBagConstraintsx04.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx04.gridwidth = labelfieldwidth;
        gridBagConstraintsx04.fill = GridBagConstraints.BOTH;
        dsPanel.add(listScrollPane, gridBagConstraintsx04);
        rowidx++;
        
        // buttons
        GridBagConstraints gridBagConstraintsx09 = new GridBagConstraints();
        gridBagConstraintsx09.gridx = 0;
        gridBagConstraintsx09.gridy = rowidx;
        gridBagConstraintsx09.insets = new Insets(5,5,5,5); 
        dsPanel.add(jbnNew, gridBagConstraintsx09);
        
        GridBagConstraints gridBagConstraintsx11 = new GridBagConstraints();
        gridBagConstraintsx11.gridx = 1;
        gridBagConstraintsx11.gridy = rowidx;
        gridBagConstraintsx11.insets = new Insets(5,5,5,5); 
        dsPanel.add(jbnModify, gridBagConstraintsx11);
        
        GridBagConstraints gridBagConstraintsx12 = new GridBagConstraints();
        gridBagConstraintsx12.gridx = 3;
        gridBagConstraintsx12.gridy = rowidx;
        gridBagConstraintsx12.insets = new Insets(5,5,5,5);
        dsPanel.add(jbnDelete, gridBagConstraintsx12);

        /*
        GridBagConstraints gridBagConstraintsx13 = new GridBagConstraints();
        gridBagConstraintsx13.gridx = 0;
        gridBagConstraintsx13.gridy = rowidx;
        gridBagConstraintsx13.insets = new Insets(5,5,5,5); 
        dsPanel.add(jbnExecute, gridBagConstraintsx13);
        */
        
        GridBagConstraints gridBagConstraintsx15 = new GridBagConstraints();
        gridBagConstraintsx15.gridx = 4;
        gridBagConstraintsx15.gridy = rowidx;
        gridBagConstraintsx15.insets = new Insets(5,5,5,5); 
        dsPanel.add(jbnExit, gridBagConstraintsx15);
        
   		jbnNew.addActionListener(this);
   		jbnModify.addActionListener(this);
   		jbnDelete.addActionListener(this);
   		//jbnExecute.addActionListener(this);
   		jbnExit.addActionListener(this);
   	}
   	
   	public void actionPerformed (ActionEvent e){
	   	// datasource list
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
			   		
   		if (e.getSource () == jbnNew){
   			//dsFindFrame.setVisible(false);
   			VATest.dsFind.setVisible(false);
  	   		//DatasourceCreator ddt = (DatasourceCreator)VATest.dsCreate;
   			DatasourceCreator ddt = new DatasourceCreator(null);
   	   		ddt.setOperationType("new");
   	   		ddt.createGUI();
   	   		
   	   		ddt.setVisible(false);
   	   		ddt.dispose();
        }
   		else if (e.getSource() == jbnModify){
   			//dsFindFrame.setVisible(false);
   			VATest.dsFind.setVisible(false);
   			
   			String dsName = jdlistModel.getElementAt(
   	                jlistType.getSelectedIndex()).toString();
   			
   			Datasource ds = DatasourcePeer.getDatasourceByName(dsName);
   			
  	   		//DatasourceCreator ddt = (DatasourceCreator)VATest.dsCreate;
   			//DatasourceCreator ddt = new DatasourceCreator(VATest.dsFind);
   			//DatasourceCreator ddt = new DatasourceCreator(this);
   			DatasourceCreator ddt = new DatasourceCreator(null);
   	   		ddt.setOperationType("clone");
   	   		//ds.setConProperty("");
   	   		ds.setUrl("");
   	   		ddt.setDefaultDatasource(ds);
   	   		ddt.createGUI();
   	   		
   	   		ddt.setVisible(false);
   	   		ddt.dispose();
        }

   		else if (e.getSource() == jbnDelete){
   			int idx = jlistType.getSelectedIndex();
   			removeFromList(idx);
  			JOptionPane.showMessageDialog(null, "Datasource Deleted");  
        } 
   		else if (e.getSource() == jbnExit){
   			
   			// prompt to ask to save the datasource into a file,
   			// if yes, ask for the directory and file name to saved.
   			JOptionPane pane = new JOptionPane();
   			int reply = pane.showConfirmDialog(null, "Do you want to save datasources into a file?", "Save to File", JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
   	        if(reply == JOptionPane.YES_OPTION) {
   	            //JOptionPane.CLOSED_OPTION;
   	        	// get user input file name or let user browse 
   	        	//JOptionPane.showMessageDialog(null, "yes option"); 
   	        	
   	         /*	
   	         // set up a file picker component
   	         FilePicker filePicker = new FilePicker("Choose a file", "Browse...");
   	         filePicker.setMode(FilePicker.MODE_SAVE);
   	         filePicker.addFileTypeFilter(".jpg", "JPEG Images");
   	         filePicker.addFileTypeFilter(".mp4", "MPEG-4 Videos");
   	          
   	         // access JFileChooser class directly
   	         JFileChooser fileChooser = filePicker.getFileChooser();
   	         //fileChooser.setCurrentDirectory(new File("D:/"));
   	         fileChooser.setCurrentDirectory(new File("/"));
   	         filePicker.setVisible(true);
   	         // add the component to the frame
   	         VATest.dsFind.add(filePicker);
   	         */ 
   	        	
   	        	
   			    TestUtils ddt = new TestUtils();
   			    int dsize = ddt.savetofile();
   			    if (dsize <= 0) {
   			    	JOptionPane.showMessageDialog(null, "No datasource defined. Please define the datasources to continue.");
   			    	// return;
   			    }
   	        }
   	        //JOptionPane.showMessageDialog(null, "before exit");
   			System.exit(0);
   		}
   	}
   	
   	
    ListDataListener listDataListener = new ListDataListener() {
        public void contentsChanged(ListDataEvent listDataEvent) {
          appendEvent(listDataEvent);
        }

        public void intervalAdded(ListDataEvent listDataEvent) {
          appendEvent(listDataEvent);
        }

        public void intervalRemoved(ListDataEvent listDataEvent) {
          appendEvent(listDataEvent);
        }

        private void appendEvent(ListDataEvent listDataEvent) {
          switch (listDataEvent.getType()) {
          case ListDataEvent.CONTENTS_CHANGED:
            System.out.println("Type: Contents Changed");
            break;
          case ListDataEvent.INTERVAL_ADDED:
            System.out.println("Type: Interval Added");
            break;
          case ListDataEvent.INTERVAL_REMOVED:
            System.out.println("Type: Interval Removed");
            break;
          }
          System.out.println(", Index0: " + listDataEvent.getIndex0());
          System.out.println(", Index1: " + listDataEvent.getIndex1());
          DefaultListModel theModel = (DefaultListModel) listDataEvent.getSource();
          System.out.println(theModel);
        }
      };

         
}
