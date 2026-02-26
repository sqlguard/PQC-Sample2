/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.SpringLayout;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.UIManager;

import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DbDriver;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.DbDriverMap;
import com.guardium.utils.FileUtils;

public class DatasourcePane extends JPanel implements ActionListener, ListSelectionListener {
	
	public JButton btnCancel;
	public JButton btnApply;
	public JButton btnTestCon;
	
	private JLabel lblName;
	JTextField txtName;
	
	JLabel lblType;
	JList<String> dsList;
	
	JLabel lblDesc;
	JTextField txtDesc;
	
	JLabel lblUsername;
	JTextField txtUsername;
	
	JLabel lblPassword;
	JPasswordField txtPassword;
	
	JLabel lblHost;
	JTextField txtHost;
	
	JLabel lblPort;
	JTextField txtPort;
	
	JLabel lblServiceName;
	JTextField txtServiceName;
	
	JTextField txtCustomUrl;
	JLabel lblCustomUrl;
	
	JTextField txtConProp;
	JLabel lblConProp;
	
	JTextField txtDatabase;
	JLabel lblDatabase;
	
	JLabel lblInformixServer;
	JTextField txtInformixServer;	
	
	JButton btnDriverPath;
	JRadioButton rdOracleDriver;
	JRadioButton rdMSSQLDriver;
	
	ArrayList<JLabel> errorLabel = new ArrayList<JLabel>(); 
	ArrayList<JTextField> requiredFields = new ArrayList<JTextField>();
	
	// datasource map
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	
	// init DbDriver map
	//DbDriverMap driverMap = new DbDriverMap();
	DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();

	// init DatasourceType map
	DatasourceTypeMap dsTypeMap = new DatasourceTypeMap();
		
	public DatasourcePane() {
		this.setSize(990, 600);
		this.setBackground(ColorUtil.PANEL_BG_COLOR);
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel lblMessage = new JLabel("<html>Please refer to documentation on granting read only privilege in executing vulnerability assessment tests. Guardium requires a set of minimal privileges to execute vulnerability assessment tests.</html>");
		springLayout.putConstraint(SpringLayout.NORTH, lblMessage, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblMessage, 200, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, lblMessage, 70, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, lblMessage, 750, SpringLayout.WEST, this);
		add(lblMessage);
		
		lblName = new JLabel("Name:");		
		springLayout.putConstraint(SpringLayout.WEST, lblName, 200, SpringLayout.WEST, this);		
		add(lblName);
		
		txtName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtName, 10, SpringLayout.SOUTH, lblMessage);
		springLayout.putConstraint(SpringLayout.NORTH, lblName, 0, SpringLayout.NORTH, txtName);
		springLayout.putConstraint(SpringLayout.SOUTH, lblName, 0, SpringLayout.SOUTH, txtName);
		springLayout.putConstraint(SpringLayout.WEST, txtName, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtName, 750, SpringLayout.WEST, this);
		add(txtName);
		txtName.setColumns(30);
		
		lblType = new JLabel("Database Type:");
		springLayout.putConstraint(SpringLayout.WEST, lblType, 200, SpringLayout.WEST, this);
		add(lblType);	
		
		dsList = new JList<String>(getDatasourceList());
		JScrollPane listPane = new JScrollPane(dsList);
		dsList.setBorder(new LineBorder(UIManager.getColor("TextField.inactiveBackground")));
		springLayout.putConstraint(SpringLayout.NORTH, lblType, 0, SpringLayout.NORTH, listPane);
		springLayout.putConstraint(SpringLayout.SOUTH, lblType, 0, SpringLayout.SOUTH, listPane);
		springLayout.putConstraint(SpringLayout.NORTH, listPane, 5, SpringLayout.SOUTH, txtName);
		springLayout.putConstraint(SpringLayout.WEST, listPane, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, listPane, 750, SpringLayout.WEST, this);		
		//dsList.setSelectedIndex(0);
		dsList.addListSelectionListener(this);
		add(listPane);
		
		lblDesc = new JLabel("Description:");
		springLayout.putConstraint(SpringLayout.NORTH, lblDesc, 5, SpringLayout.SOUTH, listPane);
		springLayout.putConstraint(SpringLayout.WEST, lblDesc, 200, SpringLayout.WEST, this);
		add(lblDesc);		
		
		txtDesc = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtDesc, 5, SpringLayout.SOUTH, listPane);
		springLayout.putConstraint(SpringLayout.WEST, txtDesc, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtDesc, 750, SpringLayout.WEST, this);
		add(txtDesc);
		txtDesc.setColumns(30);
		lblDesc.setVisible(false);
		txtDesc.setVisible(false);
		
		lblUsername = new JLabel("Username:");
		springLayout.putConstraint(SpringLayout.WEST, lblUsername, 200, SpringLayout.WEST, this);
		add(lblUsername);		
		
		txtUsername = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblUsername, 0, SpringLayout.NORTH, txtUsername);
		springLayout.putConstraint(SpringLayout.SOUTH, lblUsername, 0, SpringLayout.SOUTH, txtUsername);
		springLayout.putConstraint(SpringLayout.NORTH, txtUsername, 5, SpringLayout.SOUTH, txtDesc);
		springLayout.putConstraint(SpringLayout.WEST, txtUsername, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtUsername, 750, SpringLayout.WEST, this);
		add(txtUsername);
		txtUsername.setColumns(30);
		
		lblPassword = new JLabel("Password:");
		springLayout.putConstraint(SpringLayout.WEST, lblPassword, 200, SpringLayout.WEST, this);
		add(lblPassword);
		
		txtPassword = new JPasswordField();
		springLayout.putConstraint(SpringLayout.NORTH, lblPassword, 0, SpringLayout.NORTH, txtPassword);
		springLayout.putConstraint(SpringLayout.SOUTH, lblPassword, 0, SpringLayout.SOUTH, txtPassword);
		springLayout.putConstraint(SpringLayout.NORTH, txtPassword, 5, SpringLayout.SOUTH, txtUsername);
		springLayout.putConstraint(SpringLayout.WEST, txtPassword, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtPassword, 750, SpringLayout.WEST, this);
		txtPassword.setColumns(30);
		add(txtPassword);
		
		lblHost = new JLabel("Host Name/IP:");
		springLayout.putConstraint(SpringLayout.WEST, lblHost, 200, SpringLayout.WEST, this);
		add(lblHost);		
		
		txtHost = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblHost, 0, SpringLayout.NORTH, txtHost);
		springLayout.putConstraint(SpringLayout.SOUTH, lblHost, 0, SpringLayout.SOUTH, txtHost);
		springLayout.putConstraint(SpringLayout.NORTH, txtHost, 5, SpringLayout.SOUTH, txtPassword);
		springLayout.putConstraint(SpringLayout.WEST, txtHost, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtHost, 750, SpringLayout.WEST, this);
		add(txtHost);
		txtHost.setColumns(30);
		
		lblPort = new JLabel("Port:");
		springLayout.putConstraint(SpringLayout.WEST, lblPort, 200, SpringLayout.WEST, this);
		add(lblPort);		
		
		txtPort = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblPort, 0, SpringLayout.NORTH, txtPort);
		springLayout.putConstraint(SpringLayout.SOUTH, lblPort, 0, SpringLayout.SOUTH, txtPort);
		springLayout.putConstraint(SpringLayout.NORTH, txtPort, 5, SpringLayout.SOUTH, txtHost);
		springLayout.putConstraint(SpringLayout.WEST, txtPort, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtPort, 750, SpringLayout.WEST, this);
		add(txtPort);
		txtPort.setColumns(30);
		
		lblServiceName = new JLabel("Service Name:");
		springLayout.putConstraint(SpringLayout.WEST, lblServiceName, 200, SpringLayout.WEST, this);
		add(lblServiceName);		
		
		txtServiceName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblServiceName, 0, SpringLayout.NORTH, txtServiceName);
		springLayout.putConstraint(SpringLayout.SOUTH, lblServiceName, 0, SpringLayout.SOUTH, txtServiceName);
		springLayout.putConstraint(SpringLayout.NORTH, txtServiceName, 5, SpringLayout.SOUTH, txtPort);
		springLayout.putConstraint(SpringLayout.WEST, txtServiceName, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtServiceName, 750, SpringLayout.WEST, this);
		add(txtServiceName);
		txtServiceName.setColumns(30);
		
		lblInformixServer = new JLabel("Informix Server:");
		springLayout.putConstraint(SpringLayout.WEST, lblInformixServer, 200, SpringLayout.WEST, this);
		add(lblInformixServer);		
		
		txtInformixServer = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblInformixServer, 0, SpringLayout.NORTH, txtInformixServer);
		springLayout.putConstraint(SpringLayout.SOUTH, lblInformixServer, 0, SpringLayout.SOUTH, txtInformixServer);
		springLayout.putConstraint(SpringLayout.NORTH, txtInformixServer, 5, SpringLayout.SOUTH, txtServiceName);
		springLayout.putConstraint(SpringLayout.WEST, txtInformixServer, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtInformixServer, 750, SpringLayout.WEST, this);
		add(txtInformixServer);
		txtPort.setColumns(30);
		
		lblDatabase = new JLabel("Database:");
		springLayout.putConstraint(SpringLayout.WEST, lblDatabase, 200, SpringLayout.WEST, this);
		add(lblDatabase);		
		
		txtDatabase = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblDatabase, 0, SpringLayout.NORTH, txtDatabase);
		springLayout.putConstraint(SpringLayout.SOUTH, lblDatabase, 0, SpringLayout.SOUTH, txtDatabase);
		springLayout.putConstraint(SpringLayout.NORTH, txtDatabase, 5, SpringLayout.SOUTH, txtInformixServer);
		springLayout.putConstraint(SpringLayout.WEST, txtDatabase, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtDatabase, 750, SpringLayout.WEST, this);
		add(txtDatabase);
		txtDatabase.setColumns(30);
		
		lblConProp = new JLabel("Connection Property:");
		springLayout.putConstraint(SpringLayout.WEST, lblConProp, 200, SpringLayout.WEST, this);
		add(lblConProp);		
		
		txtConProp = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblConProp, 0, SpringLayout.NORTH, txtConProp);
		springLayout.putConstraint(SpringLayout.SOUTH, lblConProp, 0, SpringLayout.SOUTH, txtConProp);
		springLayout.putConstraint(SpringLayout.NORTH, txtConProp, 5, SpringLayout.SOUTH, txtDatabase);
		springLayout.putConstraint(SpringLayout.WEST, txtConProp, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtConProp, 750, SpringLayout.WEST, this);
		add(txtConProp);
		txtConProp.setColumns(30);
		
		lblCustomUrl = new JLabel("Custom Url:");
		springLayout.putConstraint(SpringLayout.WEST, lblCustomUrl, 200, SpringLayout.WEST, this);
		add(lblCustomUrl);		
		
		txtCustomUrl = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblCustomUrl, 0, SpringLayout.NORTH, txtCustomUrl);
		springLayout.putConstraint(SpringLayout.SOUTH, lblCustomUrl, 0, SpringLayout.SOUTH, txtCustomUrl);
		springLayout.putConstraint(SpringLayout.NORTH, txtCustomUrl, 5, SpringLayout.SOUTH, txtConProp);
		springLayout.putConstraint(SpringLayout.WEST, txtCustomUrl, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, txtCustomUrl, 750, SpringLayout.WEST, this);
		add(txtCustomUrl);
		txtCustomUrl.setColumns(30);
		lblCustomUrl.setVisible(false);
		txtCustomUrl.setVisible(false);
		
		JLabel lblDriverPath = new JLabel("Additional Database Drivers:");
		springLayout.putConstraint(SpringLayout.WEST, lblDriverPath, 200, SpringLayout.WEST, this);
		add(lblDriverPath);		
		
		btnDriverPath = new JButton("Browse...");	
		btnDriverPath.setBackground(ColorUtil.BUTTON_BG_COLOR);
		btnDriverPath.addActionListener(this);
		springLayout.putConstraint(SpringLayout.NORTH, lblDriverPath, 0, SpringLayout.NORTH, btnDriverPath);
		springLayout.putConstraint(SpringLayout.SOUTH, lblDriverPath, 0, SpringLayout.SOUTH, btnDriverPath);
		springLayout.putConstraint(SpringLayout.NORTH, btnDriverPath, 5, SpringLayout.SOUTH, txtCustomUrl);
		springLayout.putConstraint(SpringLayout.WEST, btnDriverPath, 400, SpringLayout.WEST, this);
		add(btnDriverPath);	
		
		btnTestCon = new JButton("Test Connection");
		btnTestCon.setBackground(ColorUtil.BUTTON_BG_COLOR);
		btnTestCon.addActionListener(this);
		
		JPanel panelButtons = new JPanel();
		panelButtons.setBackground(ColorUtil.PANEL_BG_COLOR);
		springLayout.putConstraint(SpringLayout.NORTH, panelButtons, -70, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, panelButtons, 400, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, panelButtons, -20, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, panelButtons, 750, SpringLayout.WEST, this);
		add(panelButtons);
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5)); 
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBackground(ColorUtil.BUTTON_BG_COLOR);
		btnApply = new JButton("Apply");
		btnApply.setBackground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnApply.setForeground(Color.WHITE);
		
		panelButtons.add(btnTestCon);
		panelButtons.add(btnCancel);
		panelButtons.add(btnApply);						
	}
	
	public void clear(){
		clearErrIndicators();
		this.txtName.setText("");
		this.txtDesc.setText("");
		this.txtUsername.setText("");
		this.txtHost.setText("");
		this.txtPassword.setText("");
		this.txtPort.setText("");
		this.txtServiceName.setText("");
		this.txtInformixServer.setText("");
		this.txtDatabase.setText("");
		this.txtConProp.setText("");
		this.txtCustomUrl.setText("");
		
		dsList.setSelectedIndex(0);
	}
	
	public void clearErrIndicators(){
		for(JLabel lbl : errorLabel){
			this.remove(lbl);
			this.revalidate();
			this.repaint();
		}
		//requiredFields.clear();
	}
	
	private void resetDefaultFields(){
		
		clearErrIndicators();
		
		this.lblPort.setText("Port:");
		this.lblServiceName.setText("Service Name:");
		this.lblDatabase.setText("Database:");
		
		this.txtInformixServer.setVisible(true);
		this.lblInformixServer.setVisible(true);
		this.txtServiceName.setVisible(true);
		this.lblServiceName.setVisible(true);
	}
	
	public void setDatasource(Datasource ds){
		DSType dd = new DSType();
    	String dsType = dd.getDsTypeString(ds.getDbDriverId());
		
		this.txtName.setText(ds.getName());
		this.txtDesc.setText(ds.getDescription());
		this.txtUsername.setText(ds.getUserName());
		this.txtHost.setText(ds.getHost());
		this.txtPort.setText(String.valueOf(ds.getPort()));
		
		String dsServiceName = ds.getServiceName();
    	if(dsType.contains("Informix")){
    		this.txtInformixServer.setText(dsServiceName);
    		this.txtDatabase.setText(ds.getDbName());
    	}else if(dsType.contains("Netezza")){
    		this.txtDatabase.setText(dsServiceName);
    	}else{
    		this.txtServiceName.setText(dsServiceName);
    		this.txtDatabase.setText(ds.getDbName());
    	}    	
		this.txtConProp.setText(ds.getConProperty());		
    	dsList.setSelectedValue(dsType, true);
	}
	
	private boolean isControlValid(JTextField txtInput){
		boolean isValid = true;
		String tooltipMessage = "This value is required.";
		
		if(txtInput.getText().isEmpty()){
			isValid=false;
			setErrorLabel(txtInput, tooltipMessage);			
		}else if(txtInput == this.txtPort){
			//Check if it is the port that is not numerical to add a different label than "is not required"
			String dsHostPort = txtInput.getText();
			String regex = "[0-9]+"; 
			boolean isNumber = dsHostPort.matches(regex);
			if(!isNumber){
				isValid = false;
				tooltipMessage = (isNumber) ? tooltipMessage : "Port must be a numeric value";
				setErrorLabel(txtInput, tooltipMessage);
			}			
		}	
		return isValid;
	}
	
	private void setErrorLabel(JTextField txtInput, String tooltipMessage){
		ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconErrorSmall.png"));
		JLabel errorIcon = new JLabel(icon);					
		errorIcon.setToolTipText(tooltipMessage);
		Rectangle rect = txtInput.getBounds();
		SpringLayout springLayout = (SpringLayout)this.getLayout();
		int y = (int) (rect.getY());
		int x = (int) (rect.getMaxX());
		springLayout.putConstraint(SpringLayout.NORTH, errorIcon, y, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, errorIcon, x, SpringLayout.WEST, this);
		this.add(errorIcon);
		this.revalidate();
		this.repaint();		
		errorLabel.add(errorIcon);
	}
	
	private boolean validatePane(){
		boolean isValid = true;		
		this.clearErrIndicators();//clear indicators as we re-testing all
		for(JTextField txtField:this.requiredFields){
			if(!isControlValid(txtField) && isValid){
				isValid = false;
			}
		}			
		return isValid;
	}
	
	private boolean testDatasourceConnection(Datasource ds, boolean promptIfSuccess){		
		boolean isSuccessful = false;
		String errMsg = "";
		if (ds != null) {
			try {
				
				// set db driver
				DbDriver sdriver = DbDriverPeer.getDbDriverById(ds.getDbDriverId());
				ds.setDbDriver(sdriver);

				// set datasource type
				DatasourceType dst = dsTypeMap.getDatasourceType(ds.getDatasourceTypeId());
				ds.setDatasourceType(dst);
				
				// try to connect
				Connection con = null;
				
				isSuccessful = ds.testConnection();
			} catch (DataSourceConnectException e) {
				errMsg = e.getMessage();
			}
			
			if (isSuccessful){
				if(promptIfSuccess){
					ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconSuccess.png"));
					JOptionPane.showMessageDialog(this, "Connection succeeded", "Success", JOptionPane.INFORMATION_MESSAGE, icon);
				}				
			}else{
				ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
				JOptionPane.showMessageDialog(this, errMsg, "Error", JOptionPane.INFORMATION_MESSAGE, icon);
			}	
		}
		
		return isSuccessful;
	}

	
	private Datasource storeDatasource(){
		Datasource ds = createDatasource ();		
		if (ds != null) {
			boolean isValid = testDatasourceConnection(ds, false);
			if(isValid){
				DatasourceMap.add(ds);
			}else{
				ds = null;	        	
			}
		}
		return ds;		
	}
	
	public Datasource persistDatasouces(){
		Datasource ds = storeDatasource();		
		if(ds != null){
			TestUtils ddt = new TestUtils();
			int dsize = ddt.savetofile();
			if (dsize <= 0) {
   			   	JOptionPane.showMessageDialog(null, "No datasource defined. Please define the datasources to continue.");
   			   	// return;
   			}
		}
		return ds;
	}
	
	private Datasource createDatasource(){	
		if(!validatePane()) return null;
		Datasource ds = null;
		
		String dsName = txtName.getText();
		String dsDesc = txtDesc.getText();
		String dsLoginName = txtUsername.getText();
		String dsPwd = new String(this.txtPassword.getPassword());
				
		String dsHostName = txtHost.getText();
    	String dsHostPort = txtPort.getText();
    	String dsServiceName = this.txtServiceName.getText();
    	
    	String selectedDBName = dsList.getSelectedValue();
    	if(selectedDBName.contains("Informix")){
    		dsServiceName = txtInformixServer.getText();
    	}else if(selectedDBName.contains("Netezza")){
    		dsServiceName = this.txtDatabase.getText();
    	}
    	
    	String dsDbName = txtDatabase.getText();
    	String dsConProperty = txtConProp.getText();
    	// not support custom url. 
    	String dsCustomUrl = ""; 		//txtCustomUrl.getText();
    	String dbType = dsList.getSelectedValue();
    	
    	DSType dd = new DSType();
		int [] retarr = dd.getDsTypeId(dbType);
		int dsTypeId = retarr[0];
		int db_driver_id = retarr[1];
		
		// check digit
		//String regex = "[0-9]+"; 
		int portNumber = -1;
		try{
			portNumber = Integer.parseInt(dsHostPort); 
		}catch(NumberFormatException ex){
			//ignore, will be checked when testing connection
			//return ds;
		}
		/*if (dsHostPort.matches(regex)) {
			portNumber = Integer.parseInt(dsHostPort);
		}
		else {
			// invalid input
			ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
			JOptionPane.showMessageDialog(this, "Port should be a number", "Error", JOptionPane.INFORMATION_MESSAGE, icon);
			return ds;
		}*/
		boolean password_stored_flag = false;
		Date lconnect = new Date();
		Date ts = new Date();
		int applicationid = 8;
		int severity = 2;
		boolean use_ssl_flag = false;
		boolean import_flag = false;
		boolean shared_flag = false;
		String os_uname = "";
		String db_dir = "";
		String cmode = "";
		ds = new Datasource (0, dsTypeId, dsName, dsDesc, dsHostName, portNumber,
				dsServiceName, dsLoginName, dsPwd, password_stored_flag, dsDbName, 
				lconnect, ts, applicationid, shared_flag, dsConProperty, os_uname,
				db_dir, dsCustomUrl, severity, db_driver_id, cmode, use_ssl_flag, import_flag );
		
		return ds;
	}
	
	private DefaultListModel<String> getDatasourceList(){
		DefaultListModel<String> jdlistModel = new DefaultListModel<String>();
		DSType dd = new DSType();
		List<String> strlist = dd.getDSTypeList();
		for (String str: strlist){
			jdlistModel.addElement(str);
   	   	}
   	   	return jdlistModel;
	}
	
	private void refreshDataTypesList(){
		DefaultListModel<String> listModel = (DefaultListModel<String>) this.dsList.getModel();		
		listModel.removeAllElements();    
		
        DSType dd = new DSType();
		List<String> strlist = dd.getDSTypeList();
		for (String str: strlist){
			listModel.addElement(str);
   	   	}     
	}

	private void copyJarToAppPath(File jarFile, String driverType){
		boolean isSuccess = true;
		String newFileName = "";
		TestUtils ts = new TestUtils();
		System.out.println("Copying jar");
		try {
			//Path path = Paths.get(DatasourcePane.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			//Path rootPath = path.getParent();
			//System.out.println("Root Path: " + rootPath);
			//if(!rootPath.equals(jarFile.getAbsolutePath())){
				//newFileName = rootPath.toAbsolutePath() + System.getProperty("file.separator") + jarFile.getName();
				
				// Using the fixed file name and save in the our saved directory
				// so we can loaded when application is up
				
				String outdir = ts.getDirectoryPath();
				String outfile = "";
				if(driverType.equals("Oracle")){
					outfile = TestUtils.LOADED_DRIVER_ORACLE;
				}else if(driverType.equalsIgnoreCase("MS SQL Server")){
					outfile = TestUtils.LOADED_DRIVER_MSSERVER;
				}
				newFileName = outdir + System.getProperty("file.separator") + outfile;
				
				System.out.println("New file name: " + newFileName);
				FileUtils.copyFile(jarFile, new File(newFileName));
			//}			
		} catch (URISyntaxException e) {
			isSuccess = false;
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean loadSuccess = false;
		if(!isSuccess){
			ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
			JOptionPane.showMessageDialog(this, "Failed copying the driver file into the same folder where your application is running.", "Error", JOptionPane.INFORMATION_MESSAGE, icon);
		} else {
			// load the driver and set the driver stored flag
			loadSuccess = ts.loadJarToAppPath(new File(newFileName), driverType);
			if (!loadSuccess) {
				ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
				JOptionPane.showMessageDialog(this, "Failed loading driver.", "Error", JOptionPane.INFORMATION_MESSAGE, icon);
			}
			else {
				if(driverType.equals("Oracle")){
					DbDriverPeer.setDriverOracleStored();
				}else if(driverType.equalsIgnoreCase("MS SQL Server")){
					DbDriverPeer.setDriverMsServerStored();
				}
				btnDriverPath.setEnabled(false);
			}
		}
		
		//refreshDataTypesList();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource () == btnTestCon){
			Datasource ds = createDatasource();
			if(ds != null){
				boolean isSuccessful = testDatasourceConnection(ds, true);
			}			
		}else if(e.getSource() == btnDriverPath){
			JFileChooser dlgDialog = new JFileChooser();		
			dlgDialog.setCurrentDirectory(new java.io.File("."));
			dlgDialog.setFileFilter(new FileNameExtensionFilter("JAR", new String[] {"jar"}));	
			dlgDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			dlgDialog.setAcceptAllFileFilterUsed(false);
			int retValue = dlgDialog.showOpenDialog(this);
			if(retValue == JFileChooser.APPROVE_OPTION){
				File fileJar = dlgDialog.getSelectedFile();
				System.out.println(fileJar);
				//Prompt for driver type
				//Object[] types = {"Oracle", "MS SQL Server"};
				//String retType = (String)JOptionPane.showInputDialog(this, "Select driver type:", "Driver Type", JOptionPane.PLAIN_MESSAGE, null, types, "Oracle");
				//if(retType != null && retType.length() > 0){
				//	copyJarToAppPath(fileJar, retType);
				//}
				String dbName = dsList.getSelectedValue();
				if (dbName.startsWith("Oracle")) {
					dbName = "Oracle";
				}
				else if (dbName.startsWith("MS SQL")) {
					dbName = "MS SQL SERVER";
				}
				copyJarToAppPath(fileJar, dbName);
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())return;
		resetDefaultFields();
		btnDriverPath.setEnabled(false);
		String selectedDBName = dsList.getSelectedValue();
		requiredFields.clear();//clear the list we are re-populating it
		if(selectedDBName != null){
			if(selectedDBName.contains("Oracle")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtServiceName));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[1521]</span></html>");
				this.lblServiceName.setText("<html>Service Name:<span style='color:#0A7AA8'>[ORCL]</span></html>");
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);
				
				if (selectedDBName.equals("Oracle (SID)") || selectedDBName.equals("Oracle (Service Name)")){
					if (!DbDriverPeer.IsDriverOracleStored()) {
						btnDriverPath.setEnabled(true);
					}
				}
			}else if(selectedDBName.contains("MS SQL")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[1433]</span></html>");
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
				
				if (selectedDBName.equals("MS SQL Server")){
					if (!DbDriverPeer.IsDriverMsServerStored()){
						btnDriverPath.setEnabled(true);
					}
				}
			}else if(selectedDBName.contains("DB2 FOR i")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtDatabase, this.txtServiceName));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[50000]</span></html>");
				this.lblDatabase.setText("<html>Database:<span style='color:#0A7AA8'>[SAMPLE]</span></html>");
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				
			}else if(selectedDBName.contains("DB2")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[50000]</span></html>");
				this.lblDatabase.setText("<html>Database:<span style='color:#0A7AA8'>[SAMPLE]</span></html>");
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				//this.txtServiceName.setVisible(false);
				//this.lblServiceName.setVisible(false);
				
			}else if(selectedDBName.contains("Informix")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtInformixServer));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[1526]</span></html>");	
				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
				
			}else if(selectedDBName.contains("Netezza")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtDatabase));				
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[5480]</span></html>");
				this.lblDatabase.setText("<html>Database:<span style='color:#0A7AA8'>[SYSTEM]</span></html>");
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
				
			}else if(selectedDBName.equals("Sybase")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtDatabase));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[2048]</span></html>");	
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
				
			}else if(selectedDBName.equals("Sybase IQ")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort, this.txtDatabase));				
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[2638]</span></html>");
				this.lblDatabase.setText("<html>Database:<span style='color:#0A7AA8'>[iqdemo]</span></html>");
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
				
			}else if(selectedDBName.equals("Teradata")){
				requiredFields.addAll(Arrays.asList(this.txtName, this.txtUsername, this.txtPassword, this.txtHost, this.txtPort));
				lblPort.setText("<html>Port:<span style='color:#0A7AA8'>[1028]</span></html>");		
				
				this.txtInformixServer.setVisible(false);
				this.lblInformixServer.setVisible(false);				
				this.txtServiceName.setVisible(false);
				this.lblServiceName.setVisible(false);
			}
		}
	}
}
