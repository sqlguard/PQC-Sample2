/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceVersionHistory;
import com.guardium.data.DbDriver;
//import com.guardium.data.DbDriver;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.DbDriverMap;


public class DatasourceCreator extends JDialog implements ActionListener
{
	
	// datasource map
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	// init DbDriver map
	DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();

	// init DatasourceType map
	DatasourceTypeMap dsTypeMap = new DatasourceTypeMap();

	JDialog dsDialog;
	
   	//JFrame dsFrame;
   	
   	JLabel jlbDsName, jlbDsType, jlbDsDesc, jlbDsSharedPwd, jlbLoginName, jlbLoginPwd, jlbHostName, jlbHostPort, 
   			jlbServiceName, jlbDbName, jlbInfServer, jlbConnectProperties, jlbCustomerUrl;
   	JTextField  jtfDsName, jtfDsType, jtfDsDesc, jtfDsSharedPwd, jtfLoginName, jtfLoginPwd, jtfHostName, jtfHostPort, 
   			jtfServiceName, jtfDbName, jtfInfServer, jtfConnectProperties, jtfCustomerUrl;
   
   	
   	JLabel jlbName, jlbAddress, jlbPhone, jlbEmail;
   	JTextField jtfName, jtfAddress, jtfPhone, jtfEmail;
   	
   	JButton jbnApply, jbnBack, jbnTest;
   	
   	JList jlistType;
   	DefaultListModel jdlistModel;
   	
   	int recordNumber;	 // used to naviagate using >> and << buttons 
   	Container cPane;
   	Container cButtonPane;
   	
   	public static void main(String args[]){
   		DatasourceCreator ddt = new DatasourceCreator(null);
   		ddt.setOperationType("new");
   		//ddt.createGUI();
   	}

   	public DatasourceCreator(JFrame parent)
   	{ 	
   		super(parent, "Datasource Definition", true);   		
   		//createGUI();
   	}

   	// "new", "clone", "modify"
   	private String opType = "new"; 
   	public void setOperationType(String op) {
   		opType = op;
   		return;
   	}
   	
   	public String getOperationType () {
   		return opType;
   	}
   	
   	private Datasource default_ds = null;
   	
   	public void setDefaultDatasource (Datasource dds) {
   		default_ds = dds;
   		return;
   	}
   	
   	public Datasource getDefaultDatasource () {
   		return default_ds;
   	}
   	
   	public void createGUI(){

   		/*Create a frame, get its contentpane and set layout*/
   		//dsFrame = new JFrame("Datasource Definition");
   		
        dsDialog = new JDialog(); //"Datasource Create");
        setLayout(new FlowLayout());
        dsDialog.setTitle("Datasource Definition");
        
   		cPane = dsDialog.getContentPane();
   		cPane.setLayout(new GridBagLayout());
   		
   		//Arrange components on contentPane and set Action Listeners to each JButton
   		arrangeComponents();
   		
   		if (this.getOperationType().equals("clone") || this.getOperationType().equals("modify")) {
   			extractDatasource();
   		}
   		//dsFrame.setSize(240,300);
   		dsDialog.setSize(720,900);
   		dsDialog.setResizable(false);
   		dsDialog.setVisible(true);
   		//dsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

   	}
   	
   	public void arrangeComponents(){

   		JLabel jlbDsDef, jlbAuthentication, jlbLocation;
   		/*
   	   	JLabel jlbDsName, jlbDsType, jlbDsDesc, jlbDsSharedPwd, jlbLoginName, jlbLoginPwd, jlbHostName, jlbHostPort, 
			jlbServiceName, jlbDbName, jlbInfServer, jlbConnectProperties, jlbCustomerUrl;
   	   	JTextField  jtfDsName, jtfDsType, jtfDsDesc, jtfDsSharedPwd, jtfLoginName, jtfLoginPwd, jtfHostName, jtfHostPort, 
   	   		jtfServiceName, jtfDbName, jtfInfServer, jtfConnectProperties, jtfCustomerUrl;
		*/
   		
   	   	// DS
   	    jlbDsDef = new JLabel("Datasource Definition");
   	   	jlbDsName = new JLabel("Name");
   	   	jlbDsType = new JLabel("Database Type");
   	   	jlbDsDesc = new JLabel("Description");
   	   	// Share Datasource
   	   	
   	   	// checkbox to load the jdbc jar file
   	    //add(new Checkbox("one", null, true));
   	    JCheckBox jb = new JCheckBox("Load JDBC driver", false);
   	   	// jdbc driver name
   	    // browse the file
   	    
   	    // confirm to load the driver
   	    
   	    
   	   	// Authentication
   	   	jlbAuthentication = new JLabel("Authentication");
   	   	jlbDsSharedPwd = new JLabel("Save Password");
   	   	jlbLoginName = new JLabel("Login Name");
   	   	jlbLoginPwd = new JLabel("Password");
   	   	
   	   	// Location
   	   	jlbLocation = new JLabel("Location");
   	   	jlbHostName = new JLabel("Host Name/IP");
   	   	jlbHostPort = new JLabel("Port"); 
		jlbServiceName = new JLabel("Service Name"); 
		jlbInfServer = new JLabel("Informix Server");
		jlbDbName = new JLabel("Database");
		jlbConnectProperties = new JLabel("Connection Property");
		jlbCustomerUrl = new JLabel("Custom Url");   		
   		
   	   	jtfDsName = new JTextField(30);
   	   	
   	   	//jtfDsType = new JTextField(30);
   	   	
        //Create and populate the list model.
   	   	jdlistModel = new DefaultListModel();
   	    //jdlistModel.addElement("--------------");
   	   	
   	   	DSType dd = new DSType();
   	   	List<String> strlist = dd.getDSTypeList();
   	   	for (String str: strlist){
   	   		jdlistModel.addElement(str);
   	   	}
   	   	
        //listModel.addListDataListener(new MyListDataListener());

        //Create the list and put it in a scroll pane.
        jlistType = new JList(jdlistModel);
        jlistType.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jlistType.setSelectedIndex(0);
        //list.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(jlistType);
        
        // text field
   	   	jtfDsDesc = new JTextField(30);
   	   	jtfDsSharedPwd = new JTextField(30);
   	   	jtfLoginName = new JTextField(30);
   	   	
   	   	jtfLoginPwd = new JPasswordField(10);
   	   	//jtfLoginPwd.setActionCommand(OK);
   	   	jtfLoginPwd.addActionListener(this);
     
   	   	jtfHostName = new JTextField(30);
   	   	jtfHostPort = new JTextField(30); 
	   	jtfServiceName = new JTextField(30);
	   	jtfDbName = new JTextField(30);
	   	jtfInfServer = new JTextField(30);
	   	jtfConnectProperties = new JTextField(30);
	   	jtfCustomerUrl = new JTextField(30);

   		jbnApply   = new JButton("Apply");
   		jbnTest = new JButton("Test Connection");
   		jbnBack  = new JButton("Back");
   		//jbnExit    = new JButton("Exit");

   		int rowidx = 0;
   		int labelidx = 0;
   		int fieldidx = 1;
   		int labelfieldwidth = 2;
   		
   		/*add all initialized components to the container*/
   		GridBagConstraints gridBagConstraintsx00 = new GridBagConstraints();
        gridBagConstraintsx00.gridx = labelidx;
        gridBagConstraintsx00.gridy = rowidx;
        gridBagConstraintsx00.insets = new Insets(5,5,5,5); 
        cPane.add(jlbDsDef, gridBagConstraintsx00);   		
   		rowidx++;
   		
   		GridBagConstraints gridBagConstraintsx01 = new GridBagConstraints();
        gridBagConstraintsx01.gridx = labelidx;
        gridBagConstraintsx01.gridy = rowidx;
        gridBagConstraintsx01.insets = new Insets(5,5,5,5); 
        cPane.add(jlbDsName, gridBagConstraintsx01);
        
        GridBagConstraints gridBagConstraintsx02 = new GridBagConstraints();
        gridBagConstraintsx02.gridx = fieldidx;
        gridBagConstraintsx02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx02.gridy = rowidx;
        gridBagConstraintsx02.gridwidth = labelfieldwidth;
        gridBagConstraintsx02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfDsName, gridBagConstraintsx02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsx03 = new GridBagConstraints();
        gridBagConstraintsx03.gridx = labelidx;
        gridBagConstraintsx03.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx03.gridy = rowidx;
        cPane.add(jlbDsType, gridBagConstraintsx03);
        
        GridBagConstraints gridBagConstraintsx04 = new GridBagConstraints();
        gridBagConstraintsx04.gridx = fieldidx;
        gridBagConstraintsx04.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx04.gridy = rowidx;
        gridBagConstraintsx04.gridwidth = labelfieldwidth;
        gridBagConstraintsx04.fill = GridBagConstraints.BOTH;
        cPane.add(listScrollPane, gridBagConstraintsx04);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsx05 = new GridBagConstraints();
        gridBagConstraintsx05.gridx = labelidx;
        gridBagConstraintsx05.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx05.gridy = rowidx;
        cPane.add(jlbDsDesc, gridBagConstraintsx05);
        
        GridBagConstraints gridBagConstraintsx06 = new GridBagConstraints();
        gridBagConstraintsx06.gridx = fieldidx;
        gridBagConstraintsx06.gridy = rowidx;
        gridBagConstraintsx06.insets = new Insets(5,5,5,5); 
        gridBagConstraintsx06.gridwidth = labelfieldwidth;
        gridBagConstraintsx06.fill = GridBagConstraints.BOTH;
        cPane.add(jtfDsDesc, gridBagConstraintsx06);
        rowidx++;
        
        // authentication
   		GridBagConstraints gridBagConstraintsa00 = new GridBagConstraints();
        gridBagConstraintsa00.gridx = labelidx;
        gridBagConstraintsa00.gridy = rowidx;
        gridBagConstraintsa00.insets = new Insets(5,5,5,5); 
        cPane.add(jlbAuthentication, gridBagConstraintsa00);  
        rowidx++;

        /*
        GridBagConstraints gridBagConstraintsa01 = new GridBagConstraints();
        gridBagConstraintsa01.gridx = labelidx;
        gridBagConstraintsa01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa01.gridy = rowidx;
        cPane.add(jlbDsSharedPwd, gridBagConstraintsa01);
        
        GridBagConstraints gridBagConstraintsa02 = new GridBagConstraints();
        gridBagConstraintsa02.gridx = fieldidx;
        gridBagConstraintsa02.gridy = rowidx;
        gridBagConstraintsa02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa02.gridwidth = labelfieldwidth;
        gridBagConstraintsa02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfDsSharedPwd, gridBagConstraintsa02);
        rowidx++;
        */
        
        GridBagConstraints gridBagConstraintsa03 = new GridBagConstraints();
        gridBagConstraintsa03.gridx = labelidx;
        gridBagConstraintsa03.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa03.gridy = rowidx;
        cPane.add(jlbLoginName, gridBagConstraintsa03);
        
        GridBagConstraints gridBagConstraintsa04 = new GridBagConstraints();
        gridBagConstraintsa04.gridx = fieldidx;
        gridBagConstraintsa04.gridy = rowidx;
        gridBagConstraintsa04.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa04.gridwidth = labelfieldwidth;
        gridBagConstraintsa04.fill = GridBagConstraints.BOTH;
        cPane.add(jtfLoginName, gridBagConstraintsa04);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsa05 = new GridBagConstraints();
        gridBagConstraintsa05.gridx = labelidx;
        gridBagConstraintsa05.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa05.gridy = rowidx;
        cPane.add(jlbLoginPwd, gridBagConstraintsa05);
        
        GridBagConstraints gridBagConstraintsa06 = new GridBagConstraints();
        gridBagConstraintsa06.gridx = fieldidx;
        gridBagConstraintsa06.gridy = rowidx;
        gridBagConstraintsa06.insets = new Insets(5,5,5,5); 
        gridBagConstraintsa06.gridwidth = labelfieldwidth;
        gridBagConstraintsa06.fill = GridBagConstraints.BOTH;
        cPane.add(jtfLoginPwd, gridBagConstraintsa06);
        rowidx++;
        
        // location
   		GridBagConstraints gridBagConstraintsc00 = new GridBagConstraints();
        gridBagConstraintsc00.gridx = labelidx;
        gridBagConstraintsc00.gridy = rowidx;
        gridBagConstraintsc00.insets = new Insets(5,5,5,5); 
        cPane.add(jlbLocation, gridBagConstraintsc00);  
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc01 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbHostName, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc02 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfHostName, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc03 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbHostPort, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc04 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfHostPort, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc05 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbServiceName, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc06 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfServiceName, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc07 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbInfServer, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc08 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfInfServer, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc09 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbDbName, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc10 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfDbName, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc11 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbConnectProperties, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc12 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfConnectProperties, gridBagConstraintsc02);
        rowidx++;
        
        GridBagConstraints gridBagConstraintsc13 = new GridBagConstraints();
        gridBagConstraintsc01.gridx = labelidx;
        gridBagConstraintsc01.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc01.gridy = rowidx;
        cPane.add(jlbCustomerUrl, gridBagConstraintsc01);
        
        GridBagConstraints gridBagConstraintsc14 = new GridBagConstraints();
        gridBagConstraintsc02.gridx = fieldidx;
        gridBagConstraintsc02.gridy = rowidx;
        gridBagConstraintsc02.insets = new Insets(5,5,5,5); 
        gridBagConstraintsc02.gridwidth = labelfieldwidth;
        gridBagConstraintsc02.fill = GridBagConstraints.BOTH;
        cPane.add(jtfCustomerUrl, gridBagConstraintsc02);
        rowidx++;
        
        // buttons
        GridBagConstraints gridBagConstraintsx09 = new GridBagConstraints();
        gridBagConstraintsx09.gridx = 0;
        gridBagConstraintsx09.gridy = rowidx;
        gridBagConstraintsx09.insets = new Insets(5,5,5,5); 
        cPane.add(jbnApply, gridBagConstraintsx09);
        
        GridBagConstraints gridBagConstraintsx10 = new GridBagConstraints();
        gridBagConstraintsx10.gridx = 1;
        gridBagConstraintsx10.gridy = rowidx;
        gridBagConstraintsx10.insets = new Insets(5,5,5,5); 
        cPane.add(jbnTest, gridBagConstraintsx10);
        
        GridBagConstraints gridBagConstraintsx11 = new GridBagConstraints();
        gridBagConstraintsx11.gridx = 2;
        gridBagConstraintsx11.gridy = rowidx;
        gridBagConstraintsx11.insets = new Insets(5,5,5,5); 
        cPane.add(jbnBack, gridBagConstraintsx11);
        rowidx++;
        
   		jbnApply.addActionListener(this);
   		jbnTest.addActionListener(this);
   		jbnBack.addActionListener(this);
   	}
   	
   	/*
    jb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          model.add(0, "First");
        }
      });
   	*/
   	
   	public void actionPerformed (ActionEvent e){
   		
   		if (e.getSource () == jbnApply){
   			Datasource ds = saveDatasource();
   			 if (ds != null) {
   				VATest.dsFind.updateList (ds); 
   				//clear(); 
   			 }
        }

   		else if (e.getSource() == jbnTest){
             testDatasource();
        }

   		else if (e.getSource() == jbnBack){
             //updateDatasource();
             //clear();e
   			//this.setVisible(false);
   			/*
   			// back to find frame
   			DatasourceFinder ddt = (DatasourceFinder)VATest.dsFind;
   	   		ddt.updateList();
   			ddt.repaint();
   			*/
   			//VATest.dsFind.setVisible(true);
   			
   			//VATest.dsFind.revalidate();
   			//VATest.dsFind.repaint();
   			
   			this.setVisible(false);
   			this.dispose();
   			
        }
   	}
   	
   	// Save Datasource definition into a datasource
   	public Datasource createDatasource () {
   		Datasource dt = null;
   	   	
   		int ret = 0;
    	/*get values from text fields*/ 
    	String dsName = jtfDsName.getText();
    	//String dsType = jtfDsType.getText();
    	String dsDesc = jtfDsDesc.getText();
    	//String dsSharedPwd = jtfDsSharedPwd.getText();
    	String dsLoginName = jtfLoginName.getText();
    	String dsLoginPwd = jtfLoginPwd.getText();
    	String dsHostName = jtfHostName.getText();
    	String dsHostPort = jtfHostPort.getText();
    	String dsServiceName = jtfServiceName.getText();
    	String dsDbName = jtfDbName.getText();
    	String dsInfServer = jtfInfServer.getText();
    	String dsConProperty = jtfConnectProperties.getText();
    	String dsCustomerUrl = jtfCustomerUrl.getText();
    	
    	int dsid = 0;
    	
    	int dsTypeId = 1;
    	int port = 0;
    	boolean password_stored_flag = true;
		Date lconnect = new Date();
		Date ts = new Date();
		boolean shared_flag = false;
		String constr = dsConProperty;
		String os_uname = "";
		String db_dir = "";
		String curl = dsCustomerUrl;
		int applicationid = 8;
		int severity = 2;
		int db_driver_id = 0;
		String cmode = "";
		boolean use_ssl_flag = false;
		boolean import_flag = false;
 	
		String typeName = jdlistModel.getElementAt(
                jlistType.getSelectedIndex()).toString();
		
		DSType dd = new DSType();
		int [] retarr = dd.getDsTypeId(typeName);
		dsTypeId = retarr[0];
		db_driver_id = retarr[1];
		
		System.out.println("type name is " + typeName + " type id is " + dsTypeId + " driiver id " + db_driver_id);
		
	   	// check required field
	   	if (dsName.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Datasource Name");
	   		return dt;
	   	}
	   	/*
	   	if (typeName.equals(DSType.DSTYPE_DEFAULT)) {
	   		JOptionPane.showMessageDialog(null, "Please choose Datasource Type");
	   		return dt;
	   	}
	   	*/
	   	if (dsLoginName.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Login Name");
	   		return dt;
	   	}	   	
	   	
	   	if (dsHostName.isEmpty() || dsHostPort.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Host Name and Port number");
	   		return dt;
	   	}
		//
	   	try{
	   		port = Integer.parseInt(""+dsHostPort);
	   	}catch(Exception e){
	   		/*System.out.print("Input is a string"); */
	   		JOptionPane.showMessageDialog(null, "Please enter Port Number");
	   		return dt;
	   	}
	   	
	   	// verify datasource
	   	
	   	// create datasource
		dt = new Datasource (dsid, dsTypeId, dsName, dsDesc, dsHostName, port,
				dsServiceName, dsLoginName, dsLoginPwd, password_stored_flag, dsDbName, 
				lconnect, ts, applicationid, shared_flag, constr, os_uname,
				db_dir, curl, severity, db_driver_id, cmode, use_ssl_flag, import_flag );
		   		
   		return dt;
   	}
   	
    public Datasource saveDatasource() {

    	int ret = 0;

	   	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
		Datasource ds = createDatasource ();
		
		if (ds != null) {
			// test connection
	        if (testDatasource(ds) == 1) {		
			    // create Datasource, put in Datasource list
	        	DatasourceMap.add(ds);
	        }
	        else {
	        	ds = null;
	        	return ds;
	        }
		}

		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		System.out.println("DS list size is " + dsize);		
		for (Datasource ds2: datasourceList) {

			System.out.println("\n\nDatasource info:");
			ds2.dump();		
		}
		
		String msg = "Datasource Saved " + dsize;
		JOptionPane.showMessageDialog(null, msg);
		ret = 1;
		return ds;
    }
    
    public int testDatasource () {
    	int ret = 0;
    	
    	Datasource ds = createDatasource ();
		return testDatasource(ds);
    }
    
	public int testDatasource (Datasource ds) {
		int ret = 0;
		if (ds != null) {
			// test connection
			//ret = ds.testConnection();
			
			// set db driver
			DbDriver sdriver = DbDriverPeer.getDbDriverById(ds.getDbDriverId());
			ds.setDbDriver(sdriver);

			// set datasource type
			DatasourceType dst = dsTypeMap.getDatasourceType(ds
					.getDatasourceTypeId());
			ds.setDatasourceType(dst);
			
			// try to connect
			Connection con;
			try {
				con = ds.getConnection();
				String tmp = con.getCatalog();
				System.out.println("connection OK get catalog " + tmp);
				ret = 1;

			} catch (DataSourceConnectException | SQLException e) {
				// DataSourceConnectException dsce =
				// datasource.expandConnectionException(e);
				// throw dsce;
				System.out.println("connection failed " + e.getMessage());
			}
			
			if (ret == 1)
				JOptionPane.showMessageDialog(null, "Connection succeeded");
			else 
				JOptionPane.showMessageDialog(null, "Connection failed");
		}    	
    	
    	return ret;
    }
	
   	// Save Datasource definition into DatasourceMap 
    public int extractDatasource() {

    	int ret = 0;
    	/*
	   	// create datasource
		Datasource ds = new Datasource (dsid, dsTypeId, dsName, dsDesc, dsHostName, port,
				dsServiceName, dsLoginName, dsLoginPwd, password_stored_flag, dsDbName, 
				lconnect, ts, applicationid, shared_flag, constr, os_uname,
				db_dir, curl, severity, db_driver_id, cmode, use_ssl_flag, import_flag );
		*/
    	
    	Datasource dds = this.getDefaultDatasource();
    	if (dds == null) {
    		JOptionPane.showMessageDialog(null, "No default Datasource defined");
    		return ret;
    	}
    	
    	
    	/*set values from datasource*/ 
    	jtfDsName.setText(dds.getName());
    	
    	DSType dd = new DSType();
    	//String dsType = dd.getDsTypeString(dds.getDatasourceTypeId(), dds.getDbDriverId());
    	String dsType = dd.getDsTypeString(dds.getDbDriverId());
    	int idx = dd.getDsTypeIndex(dsType);
    	
    	jlistType.setSelectedIndex(idx);
    	
    	//String dsType = jtfDsType.getText();
    	//jtfDsType.setText(dsType);
    	
    	jtfDsDesc.setText(dds.getDescription());
    	int tmp_flag = 0;
    	if (dds.getPasswordStored()) {
    		tmp_flag = 1;
    	}
    	jtfDsSharedPwd.setText(Integer.toString(tmp_flag));
    	jtfLoginName.setText(dds.getUserName());
    	jtfLoginPwd.setText(dds.getPassword());
    	jtfHostName.setText(dds.getHost());
    	int tmp_int = dds.getPort();
    	
    	jtfHostPort.setText(Integer.toString(tmp_int));
    	jtfServiceName.setText(dds.getServiceName());
    	jtfDbName.setText(dds.getDbName());
    	
    	//jtfInfServer.setText(dds.getUsingInformixDbName());
    	jtfConnectProperties.setText(dds.getConProperty());
    	jtfCustomerUrl.setText(dds.getCustomUrl());
    	
    	/*
    	int dsid = 0;
    	
    	int dsTypeId = 1;
    	int port = 0;
    	boolean password_stored_flag = true;
		Date lconnect = new Date();
		Date ts = new Date();
		boolean shared_flag = false;
		String constr = dsConProperty;
		String os_uname = "";
		String db_dir = "";
		String curl = dsCustomerUrl;
		int applicationid = 8;
		int severity = 2;
		int db_driver_id = 0;
		String cmode = "";
		boolean use_ssl_flag = false;
		boolean import_flag = false;
 	
		String typeName = jdlistModel.getElementAt(
                jlistType.getSelectedIndex()).toString();
		
		int [] retarr = getDsTypeId(typeName);
		dsTypeId = retarr[0];
		db_driver_id = retarr[1];
		
		System.out.println("type name is " + typeName + " type id is " + dsTypeId + " driiver id " + db_driver_id);
		
	   	// check required field
	   	if (dsName.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Datasource Name");
	   		return ret;
	   	}
	   	
	   	if (typeName.equals(DSTYPE_DEFAULT)) {
	   		JOptionPane.showMessageDialog(null, "Please choose Datasource Type");
	   		return ret;
	   	}
	   	
	   	if (dsLoginName.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Login Name");
	   		return ret;
	   	}	   	
	   	
	   	if (dsHostName.isEmpty() || dsHostPort.isEmpty()) {
	   		JOptionPane.showMessageDialog(null, "Please enter Host Name and Port number");
	   		return ret;
	   	}
		//
	   	try{
	   		port = Integer.parseInt(""+dsHostPort);
	   	}catch(Exception e){
	   		//System.out.print("Input is a string");
	   		JOptionPane.showMessageDialog(null, "Please enter Port Number");
	   		return ret;
	   	}
	   	
	   	// verify datasource
	   	
	   	// create datasource
		Datasource ds = new Datasource (dsid, dsTypeId, dsName, dsDesc, dsHostName, port,
				dsServiceName, dsLoginName, dsLoginPwd, password_stored_flag, dsDbName, 
				lconnect, ts, applicationid, shared_flag, constr, os_uname,
				db_dir, curl, severity, db_driver_id, cmode, use_ssl_flag, import_flag );
		
		// create Datasource, put in Datasource list
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
        
		// create datasources
		DatasourceMap.add(ds);
		
		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		System.out.println("DS list size is " + dsize);
		
		for (Datasource ds2: datasourceList) {

			System.out.println("\n\nDatasource info:");
			ds2.dump();		
		}
		
		String msg = "Datasource Saved " + dsize;
		JOptionPane.showMessageDialog(null, msg);
		ret = 1;
		return ret;
		
	    */
    	return ret;
    }
   	
   	
     public void deleteDatasource(){

	   	String name = jtfDsName.getText();
	   	name = name.toUpperCase();
	   	if(name.equals("")){
	   		JOptionPane.showMessageDialog(null,"Please enter Datasource name to delete.");
	   	}
	   	else{
	   		//remove datasource
	   		//int numberOfDeleted = pDAO.removePerson(name);
	   		int numberOfDeleted = 0;
	   		JOptionPane.showMessageDialog(null, numberOfDeleted + " Record(s) deleted.");
	   	}
     }



    public void updateDatasource(){
    	
	   	String name = jtfDsName.getText();
	   	name = name.toUpperCase();
	   	if(name.equals("")){
	   		JOptionPane.showMessageDialog(null,"Please enter Datasource name to update.");
	   	}
	   	else{
	   		//remove datasource
	   		//int numberOfDeleted = pDAO.removePerson(name);
	   		int numberOfDeleted = 0;
	   		JOptionPane.showMessageDialog(null, numberOfDeleted + " Record(s) found.");
	   	}
    	
    	/*
         if (recordNumber >= 0 && recordNumber < personsList.size())
         {
            PersonInfo person = (PersonInfo)personsList.get(recordNumber);

            int id = person.getId();

   	   //get values from text fields            
   	   name    = jtfName.getText();
   	   address = jtfAddress.getText();
   	   phone   = Integer.parseInt(jtfPhone.getText());
       email   = jtfEmail.getText();

   	   //update data of the given person name
   	   person = new PersonInfo(id, name, address, phone, email);
            pDAO.updatePerson(person);

   	   JOptionPane.showMessageDialog(null, "Person info record updated successfully.");         
         }
         else
         {   
              JOptionPane.showMessageDialog(null, "No record to Update");  
         }
         */
    } 
    
    //Perform a Case-Insensitive Search to find the Person

    public void searchDatasource() {
    	
	   	String name = jtfDsName.getText();
	   	name = name.toUpperCase();
	   	if(name.equals("")){
	   		JOptionPane.showMessageDialog(null,"Please enter Datasource name to search.");
	   	}
	   	else{
	   		//remove datasource
	   		//int numberOfDeleted = pDAO.removePerson(name);
	   		int numberOfDeleted = 0;
	   		JOptionPane.showMessageDialog(null, numberOfDeleted + " Record(s) searched.");
	   	}
 
    }


   public void clear(){
   	   	jtfDsName.setText("");
   	   	jtfDsDesc.setText("");
   	   	jtfDsSharedPwd.setText("");
   	   	jtfLoginName.setText("");
   	   	jtfLoginPwd.setText("");
   	   	jtfHostName.setText("");
   	   	jtfHostPort.setText(""); 
	   	jtfServiceName.setText("");
	   	jtfDbName.setText("");
	   	jtfInfServer.setText("");
	   	jtfConnectProperties.setText("");
	   	jtfCustomerUrl.setText("");

	   	/*clear contents of arraylist*/
	   	/*
	    recordNumber = -1;
	   	datasourceList.clear();
	   	jbnForward.setEnabled(true);
	   	jbnBack.setEnabled(true);
	   	*/
   }
          
 
  
}
