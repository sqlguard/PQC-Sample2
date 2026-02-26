/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.guardium.data.AssessmentTest;
import com.guardium.data.AvailableTest;
import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceVersionHistory;
import com.guardium.data.DbDriver;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.SqlbasedAssessmentDefinition;
import com.guardium.data.TestResult;
import com.guardium.map.AvailableTestMap;
import com.guardium.map.CveFixMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.DbDriverMap;
import com.guardium.map.SecurityAssessmentMap;
import com.guardium.map.SqlbasedAssessmentDefinitionMap;
import com.guardium.map.TestResultMap;
import com.guardium.runtest.AssessmentRunner;
import com.guardium.utils.AdHocLogger;


public class MainPane extends JPanel implements ActionListener{
	
	
	public static final String EMAIL_ADDRESS = "guardva@us.ibm.com";
	
	public JButton btnNew;
	public JButton btnEdit;
	public JButton btnDelete;
	public JButton btnRun;
	public JButton btnStop;
	public JButton btnIntro;
	public JButton btnBuy;
	public JButton btnVideoLink;
	JButton btnFullVersion;
	JProgressBar progressBar;
	
	JList<Datasource> dsList;
	//JTable tableResults;
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();

	// init DatasourceType map
	DatasourceTypeMap dsTypeMap = new DatasourceTypeMap();


	private Task task;
	// Global variables
	static int AssessmentID = 20000;
	static int AssessmentTestID = 20000;
	
	private AssessmentRunner assrun = new AssessmentRunner();
		
	public MainPane() {
		this.setSize(990, 600);
		this.setBackground(ColorUtil.PANEL_BG_COLOR);
		setLayout(new BorderLayout(0, 0));		
		createTopSection();
		createBottomSection();
		if (AppMain.evalDaysLeft < 0) {
			btnNew.setEnabled(false);
			btnEdit.setEnabled(false);
			btnDelete.setEnabled(false);
			btnRun.setEnabled(false);
			dsList.setEnabled(false);
		}
	}
	
	private DefaultListModel<Datasource> getCreatedDatasources(){
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
		
		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		System.out.println("getList DS list size is " + dsize);
		
       DefaultListModel<Datasource> jdlistModel = new DefaultListModel<Datasource>();
   	   
       for (Datasource ds2: datasourceList) {
			System.out.println("\n\nDatasource info:");
			ds2.dump();
			jdlistModel.addElement(ds2);
		}
		
		return jdlistModel;
	}
	
	private void createTopSection(){
		JPanel panelTop = new JPanel();
		panelTop.setSize(990, 200);
		panelTop.setBackground(ColorUtil.PANEL_BG_COLOR);
		add(panelTop, BorderLayout.NORTH);
		panelTop.setLayout(new BorderLayout(0, 0));
		
		JPanel panelTopLeft = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panelTopLeft.getLayout();
		flowLayout_1.setHgap(25);
		panelTopLeft.setBounds(0, 0, 300, 200);
		panelTopLeft.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelTop.add(panelTopLeft, BorderLayout.WEST);
		
		JPanel panelTopCenter = new JPanel();
		panelTopCenter.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelTop.add(panelTopCenter, BorderLayout.CENTER);
		panelTopCenter.setLayout(new BorderLayout(0, 5));
		
		JPanel panelTopLabels = new JPanel();
		panelTopLabels.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelTopLabels.setLayout(new BorderLayout(0, 5));
		panelTopCenter.add(panelTopLabels, BorderLayout.NORTH);		
		
		JLabel lblDatasources = new JLabel("Select database to assess:");
		lblDatasources.setBackground(ColorUtil.PANEL_BG_COLOR);
		lblDatasources.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelTopLabels.add(lblDatasources, BorderLayout.WEST);

		JPanel panelTopRight = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panelTopRight.getLayout();
		flowLayout_2.setHgap(25);
		panelTopRight.setSize(300, 200);
		panelTopRight.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelTop.add(panelTopRight, BorderLayout.EAST);		
		
		dsList = new JList<Datasource>();
		JScrollPane listPane = new JScrollPane(dsList);
		listPane.setBorder(new MatteBorder(3, 0, 1, 0, (Color) new Color(0, 175, 221)));
		dsList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		//list.setBorder(new MatteBorder(3, 0, 0, 0, (Color) new Color(0, 175, 221)));
		dsList.setBackground(Color.WHITE);
		lblDatasources.setLabelFor(dsList);
		dsList.setCellRenderer(new DatasourceRenderer());
		dsList.setModel(getCreatedDatasources());		
		dsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dsList.setBounds(0, 0, 900, 300);
		dsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting() == false) {
					JList lst = (JList)evt.getSource();					
					if (lst.getSelectedIndex() == -1) {
						btnEdit.setEnabled(false);	
						btnDelete.setEnabled(false);	
						btnRun.setEnabled(false);	
			        } else {
			        	btnEdit.setEnabled(true);	
						btnDelete.setEnabled(true);	
						btnRun.setEnabled(true);		        	
			        }
			    }
			}
		});
		
		panelTopCenter.add(listPane,  BorderLayout.CENTER);
		
		JPanel panelDSButtons = new JPanel();
		panelDSButtons.setLayout(new BorderLayout(0,3));
		panelDSButtons.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelTopCenter.add(panelDSButtons, BorderLayout.SOUTH);	
		
		btnRun = new JButton("Run Assessment");
		btnRun.setEnabled(false);
		btnRun.setBackground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnRun.setForeground(Color.WHITE);		
		btnRun.addActionListener(this);
		
		btnEdit = new JButton("Edit");
		btnEdit.setEnabled(false);
		btnEdit.setBackground(ColorUtil.BUTTON_BG_COLOR);		
		
		btnDelete = new JButton("Delete");
		btnDelete.setEnabled(false);
		btnDelete.setBackground(ColorUtil.BUTTON_BG_COLOR);	
		btnDelete.addActionListener(this);
		
		btnNew = new JButton("New");
		btnNew.setBackground(ColorUtil.BUTTON_BG_COLOR);				
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setForeground(ColorUtil.BUTTON_BG_GREEN);
		progressBar.setVisible(false);
		
		btnStop = new JButton("");
		btnStop.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnStop.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStop.setIcon(new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconStopJob2.gif")));
		btnStop.setBorder(BorderFactory.createEmptyBorder());
		btnStop.setContentAreaFilled(false);
		btnStop.addActionListener(this);
		btnStop.setVisible(false);		
		
		JPanel panelDSButtonsRight = new JPanel();
		panelDSButtonsRight.setBackground(ColorUtil.PANEL_BG_COLOR);		
		panelDSButtonsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));		
		
		JPanel panelDSButtonsLeft = new JPanel();
		panelDSButtonsLeft.setBackground(ColorUtil.PANEL_BG_COLOR);		
		panelDSButtonsLeft.setLayout(new BorderLayout(0, 5));
		
		btnIntro = new JButton();
		btnIntro.setText("<html><div style='text-decoration:underline;'>Step by step instructions</div></html>");
		btnIntro.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnIntro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnIntro.setBorder(BorderFactory.createEmptyBorder());
		btnIntro.setForeground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnIntro.setHorizontalAlignment(SwingConstants.LEFT);
		btnIntro.addActionListener(this);
		panelDSButtonsLeft.add(btnIntro, BorderLayout.NORTH);
		
		btnBuy = new JButton();
		btnBuy.setText("<html><div style='text-decoration:underline;'>Top 10 reasons to buy the full version</div></html>");
		btnBuy.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnBuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBuy.setBorder(BorderFactory.createEmptyBorder());
		btnBuy.setForeground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnBuy.setHorizontalAlignment(SwingConstants.LEFT);
		btnBuy.addActionListener(this);
		panelDSButtonsLeft.add(btnBuy, BorderLayout.CENTER);
		
		btnVideoLink = new JButton();
		btnVideoLink.setText("<html><div style='text-decoration:underline;'>Product demonstration</div></html>");
		btnVideoLink.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnVideoLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnVideoLink.setBorder(BorderFactory.createEmptyBorder());
		btnVideoLink.setForeground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnVideoLink.setHorizontalAlignment(SwingConstants.LEFT);
		btnVideoLink.addActionListener(this);
		panelDSButtonsLeft.add(btnVideoLink, BorderLayout.SOUTH);
		
		panelDSButtons.add(panelDSButtonsRight, BorderLayout.EAST);
		panelDSButtons.add(panelDSButtonsLeft, BorderLayout.WEST);
		
		panelDSButtonsRight.add(btnDelete, BorderLayout.EAST);
		panelDSButtonsRight.add(btnEdit, BorderLayout.EAST);
		panelDSButtonsRight.add(btnNew, BorderLayout.EAST);		
		panelDSButtonsRight.add(btnRun, BorderLayout.EAST);	
		panelDSButtonsRight.add(progressBar, BorderLayout.EAST);
		panelDSButtonsRight.add(btnStop, BorderLayout.EAST);
		
		JPanel panelDSButtonsBottom = new JPanel();
		panelDSButtonsBottom.setLayout((new BorderLayout(0, 0)));
		panelDSButtonsBottom.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelDSButtons.add(panelDSButtonsBottom, BorderLayout.SOUTH);
		
		String evalDaysLeftText = "";
		if (AppMain.evalDaysLeft > -1) {
			evalDaysLeftText = "" + AppMain.evalDaysLeft + " days left on evaluation license";
		} else {
			evalDaysLeftText = "Evaluation license expired!";
		}
		
		JLabel lblEvalDaysLeft = new JLabel(evalDaysLeftText);
		lblEvalDaysLeft.setBackground(ColorUtil.PANEL_BG_COLOR);
		lblEvalDaysLeft.setForeground(Color.RED);
		lblEvalDaysLeft.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelTopLabels.add(lblEvalDaysLeft, BorderLayout.EAST);
		//panelDSButtonsBottom.add(lblEvalDaysLeft, BorderLayout.NORTH);	
		
		JPanel panelBottomCenterEmail = new JPanel();		
		panelBottomCenterEmail.setLayout(new BorderLayout(0, 0));
		panelBottomCenterEmail.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelDSButtonsBottom.add(panelBottomCenterEmail, BorderLayout.SOUTH);	
		
		JPanel panelBottomEmail = new JPanel();
		panelBottomEmail.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
		panelBottomEmail.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelBottomCenterEmail.add(panelBottomEmail, BorderLayout.SOUTH);
		
		JLabel lblEmailAddress = new JLabel("For the full product version contact IBM at: ");
		lblEmailAddress.setBackground(ColorUtil.PANEL_BG_COLOR);	
		lblEmailAddress.setHorizontalAlignment(SwingConstants.LEADING);
		panelBottomEmail.add(lblEmailAddress);
		lblEmailAddress.setVerticalAlignment(SwingConstants.BOTTOM);
		
		JTextArea txtGuardEmailAddBottom = new JTextArea();
		txtGuardEmailAddBottom.setBackground(Color.WHITE);
		txtGuardEmailAddBottom.setFont(new Font("Monospaced", Font.BOLD, 13));
		txtGuardEmailAddBottom.setText(MainPane.EMAIL_ADDRESS);
		txtGuardEmailAddBottom.setEditable(false);
		panelBottomEmail.add(txtGuardEmailAddBottom);
		
		
		
		
		dsList.setSelectedIndex(0);
	}
	
	private void createBottomSection(){
		JPanel panelMainCenter = new JPanel();
		panelMainCenter.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelMainCenter.setSize(990, 400);
		add(panelMainCenter, BorderLayout.CENTER);
		panelMainCenter.setLayout(new BorderLayout(0, 0));
		
		JPanel panelFullVersionText = new JPanel();
		panelFullVersionText.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelFullVersionText.setLayout(new FlowLayout());
		panelMainCenter.add(panelFullVersionText, BorderLayout.SOUTH);		
		
		JLabel lblFullVersionText = new JLabel();
		lblFullVersionText.setText("For complete information on the full product, go to");
		lblFullVersionText.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFullVersionText.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelFullVersionText.add(lblFullVersionText, BorderLayout.WEST);
		
		btnFullVersion = new JButton();
		btnFullVersion.setText("<html><div style='text-decoration:underline;'>http://www.ibm.com/software/products/en/infoguarvulnasse</div></html>");
		btnFullVersion.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnFullVersion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFullVersion.setBorder(BorderFactory.createEmptyBorder());
		btnFullVersion.setForeground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnFullVersion.addActionListener(this);
		panelFullVersionText.add(btnFullVersion, BorderLayout.EAST);	
		
		JPanel panelBottomCenterEmail = new JPanel();
		panelBottomCenterEmail.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelMainCenter.add(panelBottomCenterEmail, BorderLayout.CENTER);
		panelBottomCenterEmail.setLayout(new BorderLayout(0, 0));
		
		JPanel panelBottomEmail = new JPanel();
		panelBottomEmail.setBackground(ColorUtil.PANEL_BG_COLOR);
		panelBottomCenterEmail.add(panelBottomEmail, BorderLayout.SOUTH);	
		
	}
	
	public Datasource getSelectedDatasource(){
		return dsList.getSelectedValue();
	}
	
	public void refreshDatasourceList(){
		DefaultListModel<Datasource> jdlistModel = (DefaultListModel<Datasource>)this.dsList.getModel();
		jdlistModel.clear();
		dsList.setModel(getCreatedDatasources());
	}
	
	public void removeSelectedDatasource(){
		Datasource ds = getSelectedDatasource();
		//int index = dsList.getSelectedIndex();
		if(ds != null){
			DatasourcePeer.deleteDatasourceByName(ds.getName());
			DefaultListModel<Datasource> jdlistModel = (DefaultListModel<Datasource>)this.dsList.getModel();
			jdlistModel.removeElement(ds);
			if(ds != null){
				TestUtils ddt = new TestUtils();
				int dsize = ddt.savetofile();
   			    if (dsize <= 0) {
   			    	JOptionPane.showMessageDialog(null, "No datasource defined. Please define the datasources to continue.");
   			    	// return;
   			    }
			}
			dsList.revalidate();
			dsList.repaint();
		}		
	}	
	
	private void executeTests(File fileHtml) throws IOException{
		System.out.println("");
		System.out.println("Starting execution.");
		Datasource ds = this.dsList.getSelectedValue();
		mapAssessmentCount = new Hashtable<String, Hashtable<String, Integer>>();
		
		if(this.task.isCancelled()) return; //The user has canceled the task
		AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();		
		SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionPeer = SqlbasedAssessmentDefinitionMap.getSqlbasedAssessmentDefinitionMapObject();
		SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
		CveFixMap CveFixPeer = CveFixMap.getCveFixMapObject();
		
		DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
		
		// init DatasourceType map
		DatasourceTypeMap dsTypeMap = new DatasourceTypeMap();
		
		List<AvailableTest> queryAvailableTestList = new ArrayList<AvailableTest>();
		List<AvailableTest> cveAvailableTestList   = new ArrayList<AvailableTest>();
		List<SecurityAssessment> SecurityAssessmentList = new ArrayList<SecurityAssessment>();
		
		if(this.task.isCancelled()) return; //The user has canceled the task
		DbDriver sdriver = DbDriverPeer.getDbDriverById(ds.getDbDriverId());
		ds.setDbDriver(sdriver);
		DatasourceType dst = dsTypeMap.getDatasourceType(ds.getDatasourceTypeId());
		ds.setDatasourceType(dst);
		
		/*QUERY TESTS*/
		if(this.task.isCancelled()) return; //The user has canceled the task
		queryAvailableTestList = AvailableTestPeer.getTestListByDsType(AvailableTestMap.QUERY_TEST_TYPE, ds.getDatasourceTypeId());
		System.out.println("Gathering Query Tests - size " + queryAvailableTestList.size());
		if(queryAvailableTestList.size() > 0){
			for (AvailableTest avt : queryAvailableTestList) {
				avt.setClassName("com.guardium.runtest.QueryBasedTest");
				avt.setDatasourceType(dst);
				SqlbasedAssessmentDefinition sd = SqlbasedAssessmentDefinitionPeer.getSqlbasedAssessmentDefinitionById(avt.getTestId());
				if (sd != null) {
					avt.setSqlDefinition(sd);
					sd.setAvailableTest(avt);
				}
			}
		}
		
		/*CVE TESTS*/
		if(this.task.isCancelled()) return; //The user has canceled the task
		cveAvailableTestList = AvailableTestPeer.getTestListByDsType(AvailableTestMap.CVE_TEST_TYPE, ds.getDatasourceTypeId());
		System.out.println("Gathering CVE Tests - size " + cveAvailableTestList.size() );
		if(cveAvailableTestList.size() > 0){
			for (AvailableTest avt : cveAvailableTestList) {
				avt.setClassName("com.guardium.runtest.CVETest");
				avt.setDatasourceType(dst);
				avt.setCveFixs(CveFixPeer.getListByTestId(avt.getTestId()));
			}
		}

		System.out.println("Total VA Tests " + (cveAvailableTestList.size() + queryAvailableTestList.size()) + " for datasource type " + ds.getDatasourceTypeId()); 

		Date startDate = new Date();       
		
		Connection con;
		System.out.println("Establishing Connection for " + ds.getName() );
		if(this.task.isCancelled()) return; //The user has canceled the task
		try {
			progressBar.setString("Connecting...");
			con = ds.getConnection();
			String tmp = con.getCatalog();
			DatasourceVersionHistory dhistory  = ds.findVersionHistory(con);
			if (dhistory != null) {
				ds.setVersionLevel(dhistory.getVersionLevel());
				ds.setPatchLevel(dhistory.getPatchLevel());			
			}
		} catch (DataSourceConnectException | SQLException e) {
			System.out.println("Error getting connection: " + e.getMessage());
			//AdHocLogger.logException(e);
			// reason - connection failed
			// fail end routine
			return;
		}
		
		String secass_desc = "";
		SecurityAssessment secass = null; 
		List<AssessmentTest> dTestList = new ArrayList<AssessmentTest>();
		Iterator<AvailableTest> itr = null;
		int testType = 1;  // 1 for quest test, 2 for CVE test
		
		// NO test to run
		if ( queryAvailableTestList.size() == 0 && cveAvailableTestList.size() == 0) {
			// reason - no test found to run
			// fail end routine			
			return;
		}
		
		secass_desc = "Test_Query_" + ds.getDbType() + "_" + String.valueOf(AssessmentID);
		secass = new SecurityAssessment(AssessmentID, secass_desc, testType, "", "", false, "", "");
		
		// create a assessment test
		dTestList = new ArrayList<AssessmentTest>();

		
		if ( queryAvailableTestList.size() > 0) {
			// create assessment test for each test
			itr = queryAvailableTestList.iterator();

			while (itr.hasNext()) {
				if(this.task.isCancelled()) return; //The user has canceled the task
				AvailableTest tv = itr.next();
				AssessmentTest asstest = new AssessmentTest(AssessmentTestID, AssessmentID, tv.getTestId(), false, 0, "MAJOR", -1, false);
				
				dTestList.add(asstest);
				AssessmentTestID++;
			}
		}

		if (cveAvailableTestList.size() > 0) {
			itr = cveAvailableTestList.iterator();

			while (itr.hasNext()) {
				if(this.task.isCancelled()) return; //The user has canceled the task
				AvailableTest tv = itr.next();
				AssessmentTest asstest = new AssessmentTest(AssessmentTestID, AssessmentID, tv.getTestId(), false, 0, "MAJOR", -1, false);
				
				dTestList.add(asstest);
				AssessmentTestID++;
			}		
		}

		secass.setAssessmentTests(dTestList);
		System.out.println("Testlist size " + dTestList.size());
		secass.addDatasources(ds);
		secass.save();
		AssessmentID++;
	
		
		
		progressBar.setString("Executing tests...");
		SecurityAssessmentList = SecurityAssessmentPeer.getAllList();
		TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
		for (SecurityAssessment sst : SecurityAssessmentList) {  
			if(this.task.isCancelled()) return; //The user has canceled the task
			TestResultPeer.cleanMap();
			assrun = new AssessmentRunner();
			assrun.setSecurityAssessment(sst);
			try {
				assrun.run();				
			} catch (Exception e) {
				AdHocLogger.logException(e);				
			}
		}
		
		// after run, remove the test list
		SecurityAssessmentPeer.removeAll();
		
		Date endDate = new Date();				
		long diff = endDate.getTime() - startDate.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff); 
		
		int exec_seconds = (int)seconds % 60;
		int exec_minutes = ((int)seconds - exec_seconds)/60;
		String executionTime = "" + exec_minutes + " minutes " + exec_seconds + " seconds";
		
		if(this.task.isCancelled()) return; //The user has canceled the task
		TestResultPeer.dumpSummary();
		List<TestResult> testResults = TestResultPeer.getTestResultList();		
		TestResult tmpResult = testResults.get(0);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileHtml)));
		System.out.println("Printing results to html");
		if(this.task.isCancelled()) return; //The user has canceled the task
		out.println("<!DOCTYPE html><html><head><title>IBM Security Guardium Vulnerability Assessment Evaluation Edition</title>");
		out.println("<style>"+getCSSStyle()+"</style>");
		out.println("<script>"+getJavascript()+"</script>");
		out.println("</head><body>");
		out.println("<div class='title'>IBM Security&reg; Guardium&reg; Vulnerability Assessment Evaluation Edition</div>"); //LOGO			
		
		String tableTAG = getTableData(testResults);		
		
		//Summary Section
		out.println("<div style='margin-top:20px'>");//main section
		out.println("<div style='display:inline-block; width:30%; vertical-align:top; padding: 0px 80px 0px 40px'>");//left section
		out.println("<div class='summary'>");//left-top section
		out.printf("<div class='heading'>%s</div>", ds.getName());
		out.printf("<div style='padding:15px 0px 5px 20px'><span style='display:inline-block;width:150px;color:#444;font-weight:bold;'>Database Type:</span> %s </div>", tmpResult.getDatasourceType());
		out.printf("<div style='padding:5px 0px 5px 20px'><span style='display:inline-block;width:150px;color:#444;font-weight:bold;'>Host:</span> %s </div>", ds.getHost());
		out.printf("<div style='padding:5px 0px 5px 20px'><span style='display:inline-block;width:150px;color:#444;font-weight:bold;'>Database Version:</span> %s </div>", tmpResult.getDatasourceVersion());
		out.printf("<div style='padding:5px 0px 5px 20px'><span style='display:inline-block;width:150px;color:#444;font-weight:bold;'>Execution Time:</span> %s </div>", executionTime);
		out.printf("<div style='padding:5px 0px 5px 20px'><span style='display:inline-block;width:150px;color:#444;font-weight:bold;'>Executed on:</span> %s </div>", this.getExecutionDate());
		out.println("</div>"); //close left top section
		//left-bottom section
		int totalExecutedTests = testResults.size();
		int diffTestInFullVersion = getDiffNumberOfTestFullVersion(tmpResult.getDatasourceType(), totalExecutedTests);
		int sumOfOtherTests = this.sumOfOtherTests(TestResultPeer.getSummary());		
		out.println("<div class='summary' style='margin-top:50px;'>");
		//double percentPassed = (((double)TestResultPeer.getSummary()[0])/((double)testResults.size()));
		int hardenSummary[] = TestResultPeer.getSummary(4);
		int cveSummary[] = TestResultPeer.getSummary(6);
		double percentPassed = ((double)hardenSummary[0]+(double)cveSummary[0]) / ((double)hardenSummary[0]+(double)cveSummary[0]+(double)hardenSummary[1]+(double)cveSummary[1]);
		percentPassed = Double.isNaN(percentPassed) ? 0.0 : percentPassed;
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(1);
		String result = percentFormat.format(percentPassed);
		out.printf("<div class='heading'>Test Results: %s passed</div>", result);
		//out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#008A52'>%s</div><div class='categoryLabel'>Passed</div></div>", TestResultPeer.getSummary()[0]);
		//out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#A91024'>%s</div><div class='categoryLabel'>Failed</div></div>", TestResultPeer.getSummary()[1]);
		//out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#F19027'>%s</div><div class='categoryLabel'>Others</div></div>", sumOfOtherTests);
		//out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:black'>%s</div><div class='categoryLabel'>Tests Executed</div></div>", testResults.size());
		out.printf("<div class='categoryLabel' style='font-size:14px'>Hardening Tests</div>");
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#008A52'>%s</div><div class='categoryLabel'>Passed</div></div>", hardenSummary[0]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#A91024'>%s</div><div class='categoryLabel'>Failed</div></div>", hardenSummary[1]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#F19027'>%s</div><div class='categoryLabel'>Others</div></div>", hardenSummary[16]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:black'>%s</div><div class='categoryLabel'>Tests Executed</div></div>", hardenSummary[17]);
		out.printf("<div class='categoryLabel' style='font-size:14px'>CVE Tests</div>");
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#008A52'>%s</div><div class='categoryLabel'>Passed</div></div>", cveSummary[0]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#A91024'>%s</div><div class='categoryLabel'>Failed</div></div>", cveSummary[1]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:#F19027'>%s</div><div class='categoryLabel'>Others</div></div>", cveSummary[16]);
		out.printf("<div class='section' style='display:inline-block;'><div class='intValue' style='color:black'>%s</div><div class='categoryLabel'>Tests Executed</div></div>", cveSummary[17]);
		if(diffTestInFullVersion > 0){//only show additional section if there is a difference
			out.printf("<div class='section' style='display:inline-block;max-width:200px;'><div class='intValue' style='color:#00648D;'>%s</div><div class='categoryLabel'>Additional Tests Available in Full Version</div></div>", diffTestInFullVersion);
		}	
		//if(sumOfOtherTests > 0){
		//	out.println("<div onclick='testsDetails(event)' class='section' style='cursor:pointer; padding-bottom:10px; color:#0000EE; text-align:left;'>");
		//	out.printf("<div class='intValue' style='display: inline-block;color:#444;'>%s</div><div class='categoryLabel' style='text-decoration:underline;display: inline-block; vertical-align:super;padding:0px 0px 0px 10px;'>Others</div>",sumOfOtherTests);
		//	out.println("</div>");
		//}
		//Other Tests Sections
		int[] otherTests = TestResultPeer.getSummary();
		String[] otherTestsLabels = {"Passed", "Failed", "Error", "No CAS Data", "No Report Data", "Unsupported OS Version", "Special Error", "CAS Data Collection Error", 
				"Obsolete Parameter", "Deprecated Parameter", "CVE Not Reported", "No User Data", "Modules Not Present", "Pre-test Check Failed", "Execution Test Routine Check Failed"};
		out.println("<div id='divDetails' style='display:none; text-align:left; margin-left:20px;'>");
		for(int index = 2; index < otherTests.length-2; index++){//first and second posistion are already shown in passed/failed
			int value = otherTests[index];
			if(value > 0){
				out.println("<div style='display: inline-block; padding:10px 20px'>");
				out.printf("<div class='categoryLabel' style='font-size:12px; display: inline-block;'>%s</div><div style='color: black; font-size:12px; display: inline-block; margin-left:3px;'> (%s) </div>", otherTestsLabels[index], value);
				out.print("</div>");
				if(index % 2 == 0) out.print("<br/>");
			}
		}
		out.print("</div>");
		out.println("</div>");//close summary left bottom section		
		out.println("</div>");//close summary left section
		//right top section
		if(mapAssessmentCount.size() > 0){
			out.println("<div style='display:inline-block; width:49%;' class='summary'>");
			out.printf("<div class='heading'>Failed Tests: <span style='color:#A91024'>%s</span></div>", TestResultPeer.getSummary()[1]);
			Hashtable<String, Integer> criticalMap = mapAssessmentCount.get("critical");
			if(criticalMap != null){
				int total = getTotalSeverityCategory(criticalMap);
				int privTotal = (criticalMap.get("priv") != null) ? criticalMap.get("priv") : 0;
				int confTotal = (criticalMap.get("conf") != null) ? criticalMap.get("conf") : 0;
				int authTotal = (criticalMap.get("auth") != null) ? criticalMap.get("auth") : 0;
				int verTotal = (criticalMap.get("ver") != null) ? criticalMap.get("ver") : 0;
				int otherTotal = (criticalMap.get("other") != null) ? criticalMap.get("other") : 0;
				
				out.printf("<div class='assessment'><div class='assessmentSection'><div class='intValue' style='color:red'>%s</div><div class='categoryLabel'>Critical</div></div>", total);
				out.printf("<div class='categories critical'><span>Privilege</span> <span>(%s)</span><span>Authentication </span><span>(%s)</span><span>Configuration </span><span>(%s)</span>" +
						"<span>Version</span><span>(%s)</span><span>Other</span><span>(%s)</span>", privTotal, authTotal, confTotal, verTotal, otherTotal);
				out.println("</div></div>");
			}
			Hashtable<String, Integer> majorMap = mapAssessmentCount.get("major");
			if(majorMap != null){
				int total = getTotalSeverityCategory(majorMap);
				int privTotal = (majorMap.get("priv") != null) ? majorMap.get("priv") : 0;
				int confTotal = (majorMap.get("conf") != null) ? majorMap.get("conf") : 0;
				int authTotal = (majorMap.get("auth") != null) ? majorMap.get("auth") : 0;
				int verTotal = (majorMap.get("ver") != null) ? majorMap.get("ver") : 0;
				int otherTotal = (majorMap.get("other") != null) ? majorMap.get("other") : 0;
				
				out.printf("<div class='assessment'><div class='assessmentSection'><div class='intValue' style='color:#A91024'>%s</div><div class='categoryLabel'>Major</div></div>", total);
				out.printf("<div class='categories major'><span>Privilege</span> <span>(%s)</span><span>Authentication </span><span>(%s)</span><span>Configuration </span><span>(%s)</span>" +
						"<span>Version</span><span>(%s)</span><span>Other</span><span>(%s)</span>", privTotal, authTotal, confTotal, verTotal, otherTotal);
				out.println("</div></div>");
			}
			Hashtable<String, Integer> minorMap = mapAssessmentCount.get("minor");
			if(minorMap != null){
				int total = getTotalSeverityCategory(minorMap);
				int privTotal = (minorMap.get("priv") != null) ? minorMap.get("priv") : 0;
				int confTotal = (minorMap.get("conf") != null) ? minorMap.get("conf") : 0;
				int authTotal = (minorMap.get("auth") != null) ? minorMap.get("auth") : 0;
				int verTotal = (minorMap.get("ver") != null) ? minorMap.get("ver") : 0;
				int otherTotal = (minorMap.get("other") != null) ? minorMap.get("other") : 0;
				
				out.printf("<div class='assessment'><div class='assessmentSection'><div class='intValue' style='color:#B8461B'>%s</div><div class='categoryLabel'>Minor</div></div>", total);
				out.printf("<div class='categories minor'><span>Privilege</span> <span>(%s)</span><span>Authentication </span><span>(%s)</span><span>Configuration </span><span>(%s)</span>" +
						"<span>Version</span><span>(%s)</span><span>Other</span><span>(%s)</span>", privTotal, authTotal, confTotal, verTotal, otherTotal);
				out.println("</div></div>");
			}
			Hashtable<String, Integer> cautionMap = mapAssessmentCount.get("caution");
			if(cautionMap != null){
				int total = getTotalSeverityCategory(cautionMap);
				int privTotal = (cautionMap.get("priv") != null) ? cautionMap.get("priv") : 0;
				int confTotal = (cautionMap.get("conf") != null) ? cautionMap.get("conf") : 0;
				int authTotal = (cautionMap.get("auth") != null) ? cautionMap.get("auth") : 0;
				int verTotal = (cautionMap.get("ver") != null) ? cautionMap.get("ver") : 0;
				int otherTotal = (cautionMap.get("other") != null) ? cautionMap.get("other") : 0;
				
				out.printf("<div class='assessment'><div class='assessmentSection'><div class='intValue' style='color:#F19027'>%s</div><div class='categoryLabel'>Caution</div></div>", total);
				out.printf("<div class='categories caution'><span>Privilege</span> <span>(%s)</span><span>Authentication </span><span>(%s)</span><span>Configuration </span><span>(%s)</span>" +
						"<span>Version</span><span>(%s)</span><span>Other</span><span>(%s)</span>", privTotal, authTotal, confTotal, verTotal, otherTotal);
				out.println("</div></div>");
			}
			Hashtable<String, Integer> infoMap = mapAssessmentCount.get("info");
			if(infoMap != null){
				int total = getTotalSeverityCategory(infoMap);
				int privTotal = (infoMap.get("priv") != null) ? infoMap.get("priv") : 0;
				int confTotal = (infoMap.get("conf") != null) ? infoMap.get("conf") : 0;
				int authTotal = (infoMap.get("auth") != null) ? infoMap.get("auth") : 0;
				int verTotal = (infoMap.get("ver") != null) ? infoMap.get("ver") : 0;
				int otherTotal = (infoMap.get("other") != null) ? infoMap.get("other") : 0;
				
				out.printf("<div class='assessment'><div class='assessmentSection'><div class='intValue' style='color:#00648D'>%s</div><div class='categoryLabel'>Info</div></div>", total);
				out.printf("<div class='categories info'><span>Privilege</span> <span>(%s)</span><span>Authentication </span><span>(%s)</span><span>Configuration </span><span>(%s)</span>" +
						"<span>Version</span><span>(%s)</span><span>Other</span><span>(%s)</span>", privTotal, authTotal, confTotal, verTotal, otherTotal);
				out.println("</div></div>");
			}	
			out.println("</div>");//close right top section
			out.println("</div>");//close summary main section
		}
		//Link to top reasons to buy pdf
		String tempFilePath = createTempPdfFile("reasonsToBuy");
		out.println("<div class='categoryLabel' style='padding:20px 0px 0px 40px;'><span style='padding:3px;font-size:14px'>" +
				"<a href='file:///" + tempFilePath + "' target='_blank'>Top 10 reasons to buy the full Guardium Vulnerability Assessment product.</a></span><span style='padding-left:10px;font-size:14px'>" +
				"<a href='mailto:"+ MainPane.EMAIL_ADDRESS + "?Subject=Guardium%20Vulnerability%20Assessment' target='_blank'>Email Contact: "+ MainPane.EMAIL_ADDRESS + "</a></span></div>");
		//Link to full version online
		out.println("<div class='categoryLabel' style='padding:20px 0px 0px 40px;'><span style='padding:3px;font-size:14px'>For complete information on the full Guardium Vulnerability Assessment product, go to " +
				"<a href='http://www.ibm.com/software/products/en/infoguarvulnasse' target='_blank'>http://www.ibm.com/software/products/en/infoguarvulnasse</a></span></div>");
		//print table
		out.println(tableTAG);			
		out.println("<div style='font-weight:bold;margin-top:10px;'>&copy;Copyright IBM Corporation 2016.</div>");
		out.println("</body></html>");
		out.close();
		
		// after run, remove the test result list
		TestResultPeer.cleanMap();
		
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
			Desktop.getDesktop().open(fileHtml);
		}
	}
	
	private String getExecutionDate(){
		String date = "";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = sdf.format(dt);
		return date;
	}
	
	private int sumOfOtherTests(int[] tests){
		int sum = 0;
		for(int index = 2; index < tests.length-2; index++){
			sum += tests[index];
		}		
		return sum;
	}
	
	private int getDiffNumberOfTestFullVersion(String databaseType, int totalExecuted){		
		int diff = 0;
		if(databaseType != null && !databaseType.trim().isEmpty()){
			String dbType = databaseType.toLowerCase();
			if(dbType.contains("db2")){
				diff = 241 - totalExecuted;
			}else if(dbType.contains("informix")){
				diff = 65 - totalExecuted;
			}else if(dbType.contains("ms sql")){
				diff = 114 - totalExecuted;
			}else if(dbType.contains("netezza")){
				diff = 21 - totalExecuted;
			}else if(dbType.contains("oracle")){
				diff = 462 - totalExecuted;
			// interesting case, check "sybase iq" first, to make "sybase iq" type work.
			// logic is correct, but execution sequence make difference.
			}else if(dbType.contains("sybase iq")){
				diff = 39 - totalExecuted;		
			}else if(dbType.contains("sybase")){
				diff = 70 - totalExecuted;
			}else if(dbType.contains("teradata")){
				diff = 39 - totalExecuted;
			}			
		}		
		return diff;
	}
	
	private int getTotalSeverityCategory(Hashtable<String, Integer> map){
		int count = 0;
		if(map != null){
			Enumeration<String> keys = map.keys();
			while(keys.hasMoreElements()){
				String key = keys.nextElement();
				count += map.get(key);
			}
		}		
		return count;
	}
	
	private Hashtable<String, Hashtable<String, Integer>> mapAssessmentCount = new Hashtable<String, Hashtable<String, Integer>>();
	
	private String getTableData(List<TestResult> testResults){
		StringBuilder builder = new StringBuilder();		
		Object[] item;
		builder.append("<table>");
		builder.append("<thead><th>Result</th><th>Test</th><th>Description</th><th>Recommendation</th></thead><tbody>");
		for(TestResult result: testResults){
			boolean showFullDetails = showFullResults(result);
			boolean isStatusError = false;
			//Free information
			String status = result.getScoreDesc();
			String severity = result.getSeverity();
			String category = result.getCategoryName();
			String testName = result.getTestDesc();
			severity = (severity != null) ? severity.toLowerCase() : "";
			category = (category != null) ? category.toLowerCase() : "";
			if("fail".equalsIgnoreCase(status)){
				computeSeverity(severity, category);
			}	
			if(!"pass".equalsIgnoreCase(status) && !"fail".equalsIgnoreCase(status)){
				isStatusError = true;
			}
			//Restricted info
			// Combine external reference and STIG reference into one string
			String tmp_ref = result.getExternalReference();
			if (!result.getStig_ref().equals("")) {
				if (tmp_ref.equals(""))
					tmp_ref += "STIG " + result.getStig_ref();
				else
					tmp_ref += ", STIG " + result.getStig_ref();
			}

			String externalReference = (showFullDetails) ? tmp_ref : "";
			String shortDesc = "";
			String recommendation = "";
			if(showFullDetails){
				shortDesc = result.getShortDescription();
				recommendation = result.getRecommendationText();
			} else if (!isCVE(result)) {
				shortDesc = "More information is available in the full version of Guardium Vulnerability Assessment: " +
						"<a href='http://www-03.ibm.com/software/products/en/infoguarvulnasse' target='_blank'>http://www-03.ibm.com/software/products/en/infoguarvulnasse</a>";
			}
			
			//If the test is QueryTest and we do not want to show full details but it's status is an ERROR then we want to show the contents of recommendation since it will contain
			//information on why the error happen to resolve by the user and re-run the tests to see an actual fail/pass bug #42971
			if(!isCVE(result) && isStatusError){
				recommendation = result.getRecommendationText();
			}
			
			String statusColor = getStatusScoreColor(status);
			item = new Object[]{statusColor, status, severity, category, testName, externalReference, shortDesc, recommendation};
			String trRow = "<tr><td style='width:200px; vertical-align:top; font-weight:bold; font-size:14px;'>" + getStatusElement(status) + 					
					"<span style='padding:5px 10px; text-transform:capitalize;'>%s</span><span style='padding:5px 10px; text-transform:capitalize;'>%s</span>" +
					"</td>" +
					"<td style='width:300px; vertical-align:top;'><div class='categoryLabel' style='padding:0px 0px 5px 0px;font-weight:bold; font-size:14px;'>%s</div><div style='padding:5px'>%s</div></td>" +
					"<td style='vertical-align:top;'>%s</td><td style='vertical-align:top;'>%s</td></tr>";
			builder.append(String.format(trRow, item));			
		}
		builder.append("</tbody></table>");
		
		return builder.toString();
	}
	
	private String getStatusElement(String status){
		String element = "<span style='padding:5px 10px; vertical-align:top; color:%s;'>%s</span>";		
		if(!"pass".equalsIgnoreCase(status) && !"fail".equalsIgnoreCase(status) && !"error".equalsIgnoreCase(status)){
			element = "<div style='padding:5px 10px; vertical-align:top; color:%s; font-size:12px;'>%s</div>";
		}		
		return element;
	}
	
	private String getStatusScoreColor(String status){
		String color = "#F19027"; //body default
		if(status != null){
			String tmpStatus = status.toLowerCase();
			switch (tmpStatus) {
		        case "pass":
		        	color = "#008A52";//green
		            break;
		        case "fail":
		        	color = "#A91024"; //red
		            break; 
		        case "error":
		        	color = "#F19027"; //amber
		            break;
		        default:
		        	color = "#F19027"; //amber
			}		
		}
		return color;
	}

	private boolean isCVE(TestResult test){
		return (test.getTestId() >= 1000 && test.getTestId() < 2000)? true : false;
	}
	
	private boolean showFullResults(TestResult test){
		boolean showFullResults = false;
		int testid = test.getTestId();
		//if (testid >= 1000 && testid < 2000) {
		if (this.isCVE(test)) {
			// CVE tests
			showFullResults = true;
		}else if (testid >= 2000) {
			// it is query based test, check the list to see if we need to give more information
			if( Arrays.asList(2086,2111,2113,2114,2115,2198,2201,2251,2258,2259, 
			          2273,2276,2278,2279,2280,2281,2282,2284,2286,2308,
			          2013,2011,2009,2004,2194,2289,2296,2298,2301,2313,
			          2053,2052,2051,2050,2049,
			          2022,2021,2015,2016,2311,2312,2373,2378,2381,2453,
			          2062,2063,2067,2068,2069,2070,2072,2075,2079,2081,
			          2214,2215,2216,2217,2219,2220,2222,2224,2227,2229,
			          2048,2046,2036,2035,2032,2034,2029,2026,2023,2024
					).contains(testid) ) {
				showFullResults = true;
			}			
		}		
		return showFullResults;
	}	
	
	private void computeSeverity(String severity, String category){
		if(severity != null && !severity.isEmpty() && category != null && !category.isEmpty()){
			Hashtable<String, Integer> map = mapAssessmentCount.get(severity);
			if(map != null){
				if(map.containsKey(category)){
					Integer catNumber = map.get(category);
					map.put(category, catNumber + 1);
				}else{
					map.put(category, 1);
				}
			}else{
				map = new Hashtable<String, Integer>();
				map.put(category, 1);
				mapAssessmentCount.put(severity, map);
			}
		}
	}
	
	private String getCSSStyle(){
		return "body{color:#222;background:0 0;padding:10px;font:12px Myriad,Helvetica,Tahoma,Arial,clean,sans-serif}table{border-collapse:collapse;border-spacing:0;margin-top:10px;width:100%;font-size:12px}" +
				"th{color:#444;min-width:100px;font-weight:400;font-size:18px;text-align:left;border-width:3px;border-style:solid;border-color:transparent transparent #09c;padding:10px 5px}" +
				"td{padding:10px;border-width:1px;border-style:solid;border-color:transparent transparent #ccc}tbody>tr:nth-child(odd){background-color:#f7f7f7}h2{padding:10px 0 5px;margin:0}.title{font-size:24px;color:#09c;padding:0}" +
				".heading{font-size:18px;border-bottom:3px solid #09c;font-weight:700;color:#444}.summary{border-bottom:3px solid #DCDCDC}.section{padding:10px 20px;text-align:center;vertical-align:top;}.intValue{color:#036b93;font-size:24px}" +
				".categories{vertical-align:middle;height:100%}.categories span{line-height:20px;color:#444}.categories span:nth-child(even){padding:0 20px 0 5px}.categories.critical span:nth-child(even){color:red}" +
				".categories.major span:nth-child(even){color:#A91024}.categories.minor span:nth-child(even){color:#B8461B}.categories.caution span:nth-child(even){color:#F19027}.categories.info span:nth-child(even){color:#00648D}" +
				".categoryLabel{font-weight:700;color:#444}.assessment{border-bottom:1px solid #DCDCDC;display:inline-block;width:100%;height:50px}div.assessment>div{display:inline-block}.assessmentSection{width:100px;text-align:center;padding:5px 0}";
	}
	
	private String getJavascript(){
		return "function testsDetails(e){var t=document.getElementById('divDetails');var n=t.style.display=='none';if(n===true){t.style.display='block'}else{t.style.display='none'}}";
	}
	
	class DatasourceRenderer extends JLabel implements ListCellRenderer<Datasource>{
		
		public DatasourceRenderer(){
			setOpaque(true);
		}
		
		public Component getListCellRendererComponent(JList<? extends Datasource> list, Datasource datasource, int index, boolean isSelected, boolean cellHasFocus) {			
			String name = datasource.getName();
			setText(name);
			
			if (isSelected) {
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else {
	            setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }
			
			return this;
		}		
	}
	
	class Task extends SwingWorker<Void, Void> {
		
		File fileHTML;
		public Task(File file){
			this.fileHTML = file;
		}

		protected Void doInBackground() throws Exception {
			enableActionButtons(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			progressBar.setVisible(true);
			btnStop.setVisible(true);
			executeTests(fileHTML);
			return null;
		}
		public void done() {
			assrun.doCancel();
			enableActionButtons(true);
			setCursor(null); //turn off the wait cursor
			progressBar.setString("");
			progressBar.setVisible(false);
			btnStop.setVisible(false);
		}		
	}
	
	private void enableActionButtons(boolean enable){
		this.dsList.setEnabled(enable);
		this.btnDelete.setEnabled(enable);
		this.btnEdit.setEnabled(enable);
		this.btnNew.setEnabled(enable);
		this.btnRun.setEnabled(enable);
	}
	
	// this module is the same as in DatasourcePane.
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

	
	private String promptForPWD(){
		JPanel pane = new JPanel();
		JLabel label = new JLabel("Enter password: ");
		pane.add(label);
		JPasswordField txtPwd = new JPasswordField(20);
		pane.add(txtPwd);
		String pwd = null;
		while (pwd == null || pwd.trim().isEmpty()) {
			ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconQuestion.png"));
			int retBtn = JOptionPane.showConfirmDialog(this, pane, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);

			if (retBtn == JOptionPane.OK_OPTION) {
				pwd = new String(txtPwd.getPassword());		  
			}
			else {
				pwd = "";
				break;
			}
		}
		return pwd;
	}
	
	private void showErrorMessage(String msg){
		ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
		JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.INFORMATION_MESSAGE, icon);
	}

	private String createTempPdfFile(String filename) {
		InputStream is = null;
		FileOutputStream fos = null;
		String tempFileName = "";
		try {
			is = AppMain.class.getResourceAsStream(filename + ".pdf");
			File pdfFile = File.createTempFile("guardium_va_" + tempFileName, ".pdf");
			fos = new FileOutputStream(pdfFile);
			int read = 0;
			byte[] bytes = new byte[1024];			 
			while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}           
            pdfFile.deleteOnExit();
            tempFileName = pdfFile.getPath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
	 
			}
		}
		return tempFileName;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource () == this.btnDelete){
			removeSelectedDatasource();
		}else if(e.getSource() == this.btnRun){		
			Datasource ds = this.dsList.getSelectedValue();
			String pwd = ds.getPassword();
			if(pwd == null || pwd.trim().isEmpty()){
				pwd = this.promptForPWD();
				if(pwd != null && !pwd.isEmpty()){
					ds.setPassword(pwd);
					
					// test connection
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));//show busy cursor while testing connection
					boolean isSuccessful = testDatasourceConnection(ds, false);
					if (!isSuccessful) {
						setCursor(null); //turn off the wait cursor
						ds.setPassword("");
						return;
					}	
				}else{
					setCursor(null); //turn off the wait cursor
					return;
				}
			}		
			String defaultFileName = ds.getDbType()+"_vulnerability_assessment_report.html";
			String defaultDir = "var/temp/";//"C:\\"; //program to work on windows only, not to run on linux so no worries about OS path
			JFileChooser dlgDialog = new JFileChooser();			
			dlgDialog.setFileFilter(new FileNameExtensionFilter("HTML", new String[] {"html","htm"}));			
			dlgDialog.setSelectedFile(new File(defaultDir+defaultFileName));
			dlgDialog.setAcceptAllFileFilterUsed(false);
			int retValue = dlgDialog.showSaveDialog(this);
			if(retValue == JFileChooser.APPROVE_OPTION){
				File file = dlgDialog.getSelectedFile();	
				if(file != null){
					task = new Task(file);	       
			        task.execute();
				}				
			}else{
				setCursor(null); //turn off the wait cursor, cancel was pressed on file chooser
			}
		}else if(e.getSource() == this.btnStop){
			task.cancel(true);			
		}else if(e.getSource() == this.btnFullVersion){
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					URI uri = new URI("http://www.ibm.com/software/products/en/infoguarvulnasse");
					desktop.browse(uri);
				} catch (Exception ex) {
					ex.printStackTrace(); //cannot open help in a browser
				}
			}
		}else if(e.getSource() == this.btnVideoLink){
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					URI uri = new URI("http://youtu.be/AI7VDTBTs3E");
					desktop.browse(uri);
				} catch (Exception ex) {
					ex.printStackTrace(); //cannot open help in a browser
				}
			}
		}else if(e.getSource() == this.btnIntro){
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					is = AppMain.class.getResourceAsStream("introSteps.pdf");
					File pdfFile = File.createTempFile("guardium_stepbystep_intro", ".pdf");
					fos = new FileOutputStream(pdfFile);
					int read = 0;
					byte[] bytes = new byte[1024];			 
					while ((read = is.read(bytes)) != -1) {
						fos.write(bytes, 0, read);
					}           
		            pdfFile.deleteOnExit();					
			        Desktop.getDesktop().open(pdfFile);
				} catch (Exception ex) {
					System.out.println("Error opening pdf file: " + ex.getMessage());
					String errMessage = "<html>Error opening file. <br/>Ensure that you have a PDF reader installed and try again.</html>";
					showErrorMessage(errMessage);
				}finally{
					if (is != null) {
						try {
							is.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
			 
					}
				}
			}	
		}else if(e.getSource() == this.btnBuy){
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					is = AppMain.class.getResourceAsStream("reasonsToBuy.pdf");
					File pdfFile = File.createTempFile("guardium_reasons_tobuy", ".pdf");
					fos = new FileOutputStream(pdfFile);
					int read = 0;
					byte[] bytes = new byte[1024];			 
					while ((read = is.read(bytes)) != -1) {
						fos.write(bytes, 0, read);
					}           
		            pdfFile.deleteOnExit();					
			        Desktop.getDesktop().open(pdfFile);
				} catch (Exception ex) {
					System.out.println("Error opening pdf file: " + ex.getMessage());
					String errMessage = "<html>Error opening file. <br/>Ensure that you have a PDF reader installed and try again.</html>";
					showErrorMessage(errMessage);
				}finally{
					if (is != null) {
						try {
							is.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
			 
					}
				}
			}	
		}
	}	
}
