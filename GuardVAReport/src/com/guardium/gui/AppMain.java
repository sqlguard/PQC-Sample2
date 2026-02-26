/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import com.guardium.data.Datasource;
import com.guardium.gui.MainRegistry;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class AppMain extends JFrame {

	private JPanel contentPane;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppMain frame = new AppMain();
					frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	final JFrame appMain = this;
	JPanel paneCenter;
	JButton btnHelp;
	static TestUtils ddt;
	static long evalDaysLeft = 30;
	
	private MainPane mainPane;
	private DatasourcePane datasourcePane;
	private LicenseAgreementPane paneLicense;
	
	public AppMain() {
		
		// Get current directory and set up resource directory
    	ddt = new TestUtils();
    	ddt.getDirectoryPath();
    	
    	// set encrypt and decrypt info 
    	try {
    		Crypto.init();
    	}
    	catch (Exception e) {
    		// can not init the crypto stuff
    		// should exit
    	}
    	
    	// check if we need to load the driver
    	ddt.checkLoadDriver();
    	
		setTitle("Guardium Vulnerability Assessment");
		setIconImage(Toolkit.getDefaultToolkit().getImage(AppMain.class.getResource("/com/guardium/gui/ibmabout.png")));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 700);
		contentPane = new JPanel();
		contentPane.setBackground(ColorUtil.PANEL_BG_COLOR);
		contentPane.setBorder(new EmptyBorder(0,15, 15, 15));	//padding
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel paneBanner = new JPanel();
		paneBanner.setBounds(0, 0, 500, 50);
		paneBanner.setBackground(ColorUtil.PANEL_BG_COLOR);
		contentPane.add(paneBanner, BorderLayout.NORTH);
		paneBanner.setLayout(new BorderLayout(0, 0));
		
		JLabel lblBanner = new JLabel("");
		lblBanner.setIcon(new ImageIcon(AppMain.class.getResource("/com/guardium/gui/guardium_logo.png")));
		paneBanner.add(lblBanner, BorderLayout.WEST);
		
		btnHelp = new JButton("");
		btnHelp.setBackground(ColorUtil.PANEL_BG_COLOR);
		btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnHelp.setIcon(new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconHelp.png")));
		btnHelp.setBorder(BorderFactory.createEmptyBorder());
		btnHelp.setContentAreaFilled(false);
		btnHelp.setHorizontalAlignment(SwingConstants.LEFT);
		paneBanner.add(btnHelp, BorderLayout.EAST);
		btnHelp.addActionListener(new AppMainActionListener());
		
		paneCenter = new JPanel(new CardLayout());
		paneCenter.setBackground(ColorUtil.PANEL_BG_COLOR);
		contentPane.add(paneCenter, BorderLayout.CENTER);		

		if (System.getProperty("os.name").startsWith("Windows")) {
			if (MainRegistry.GetNumberOfDays() < 0) {
				paneLicense = new LicenseAgreementPane();	
				paneLicense.btnAccept.addActionListener(new AppMainActionListener());
				paneLicense.btnDecline.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						appMain.dispatchEvent(new WindowEvent(appMain, WindowEvent.WINDOW_CLOSING));
					}
				});
				paneCenter.add(paneLicense);
			} else {
				evalDaysLeft = 30 - MainRegistry.GetNumberOfDays();
				//evalDaysLeft = -1; // Make it expired
			}
		}
		// to work on the code on unix, need to comment out this else statement
		else {
			// only allow this application to run on window so we can check the expiration correctly.
			ImageIcon icon = new ImageIcon(AppMain.class.getResource("/com/guardium/gui/iconError.png"));
			JOptionPane.showMessageDialog(this, "Guardium VA Evaluation is only supported on Windows OS", "Error", JOptionPane.INFORMATION_MESSAGE, icon);
			appMain.dispatchEvent(new WindowEvent(appMain, WindowEvent.WINDOW_CLOSING));
			//return;
		}
		
		mainPane = new MainPane();
		mainPane.btnNew.addActionListener(new AppMainActionListener());
		mainPane.btnEdit.addActionListener(new AppMainActionListener());		
		
		datasourcePane = new DatasourcePane();
		datasourcePane.btnCancel.addActionListener(new AppMainActionListener());
		datasourcePane.btnApply.addActionListener(new AppMainActionListener());
		
		//paneCenter.add(paneLicense);
		paneCenter.add(mainPane);		
		paneCenter.add(datasourcePane);		
	}	
	
	class AppMainActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == mainPane.btnNew){
				CardLayout cl = (CardLayout) paneCenter.getLayout();
				datasourcePane.clear();				
				cl.next(paneCenter); //move to the Edit/Create datasource				
			}else if(e.getSource() == mainPane.btnEdit){
				CardLayout cl = (CardLayout) paneCenter.getLayout();
				cl.next(paneCenter); //move to the Edit/Create datasource	
				Datasource ds = mainPane.getSelectedDatasource();
				datasourcePane.clear();
				datasourcePane.setDatasource(ds);							
				System.out.println(ds.getName());
			}else if(e.getSource() == datasourcePane.btnCancel){
				CardLayout cl = (CardLayout) paneCenter.getLayout();
				cl.previous(paneCenter);
			}else if(e.getSource() == datasourcePane.btnApply){
				datasourcePane.clearErrIndicators();
				Datasource ds = datasourcePane.persistDatasouces();
				if(ds != null){					
					mainPane.refreshDatasourceList();
					mainPane.dsList.setSelectedValue(ds, true);
					CardLayout cl = (CardLayout) paneCenter.getLayout();
					cl.previous(paneCenter);
				}								
			}else if(paneLicense != null && e.getSource() == paneLicense.btnAccept){
				MainRegistry.WriteInstallDateToRegistry();
				CardLayout cl = (CardLayout) paneCenter.getLayout();
				cl.next(paneCenter);
			}else if(e.getSource() == btnHelp){
				JDialog dlgHelp = new JDialog(appMain, "Help", false);
				dlgHelp.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				dlgHelp.getContentPane().setSize(990, 600);
				dlgHelp.getContentPane().setPreferredSize(new Dimension(1000, 600));
				dlgHelp.getContentPane().setBackground(ColorUtil.PANEL_BG_COLOR);
				dlgHelp.getContentPane().setLayout(new BorderLayout(0, 0));
				
				JEditorPane editorPane = new JEditorPane();
				JScrollPane editorScrollPane = new JScrollPane(editorPane);
				editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				dlgHelp.getContentPane().add(editorScrollPane, BorderLayout.CENTER);	
				editorPane.setEditable(false);
				java.net.URL helpURL = AppMain.class.getResource("VA_evaluation_help.html");
				if (helpURL != null) { 
				    try {
				        editorPane.setPage(helpURL);
				    } catch (IOException ex) {
				        System.err.println("Attempted to read a bad URL: " + helpURL);
				    }
				} else {
				    System.err.println("en.html");
				}								
				dlgHelp.pack();
				dlgHelp.setVisible(true);
			}			
		}
	}
}
