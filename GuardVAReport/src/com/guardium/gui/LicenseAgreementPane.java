/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;


import java.io.IOException;
import java.net.URL;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.JButton;

import com.guardium.gui.licenses.License;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LicenseAgreementPane extends JPanel implements ActionListener {
	
	public JButton btnDecline;
	public JButton btnAccept;
	
	JComboBox<String> ddlLicenses;
	JEditorPane editorPane;

	public LicenseAgreementPane() {
		this.setSize(990, 600);
		this.setBackground(ColorUtil.PANEL_BG_COLOR);
		setLayout(new BorderLayout(0, 0));
		
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		showLicenseText("en.html");
		
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(editorScrollPane);		
		
		JPanel paneBottom = new JPanel(new BorderLayout(0, 0));
		paneBottom.setBackground(ColorUtil.PANEL_BG_COLOR);		
		add(paneBottom, BorderLayout.SOUTH);
		
		JPanel paneButtons = new JPanel();
		paneButtons.setBackground(ColorUtil.PANEL_BG_COLOR);	
		paneButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		paneBottom.add(paneButtons, BorderLayout.EAST);
		
		JPanel paneDDLLicence = new JPanel(new BorderLayout(0, 0));
		paneDDLLicence.setBackground(ColorUtil.PANEL_BG_COLOR);		
		paneBottom.add(paneDDLLicence, BorderLayout.WEST);
		
		ddlLicenses = new JComboBox<String>();		
		paneDDLLicence.add(ddlLicenses);
		
		String[] languages = {"Chinese (Simplified)", "Chinese (Traditional)", "Czech", "English", "French", "German", "Greek", "Indonesian",
			"Italian", "Japanese", "Korean", "Lithuanian", "Polish", "Portuguese", "Russian", "Slovenian", "Spanish",  "Turkish"};
		for(String lang : languages){
			ddlLicenses.addItem(lang);
		}
		ddlLicenses.setSelectedIndex(3);
		ddlLicenses.setEditable(false);
		ddlLicenses.addActionListener(this);
		
		btnDecline = new JButton("Decline");
		btnDecline.setBackground(ColorUtil.BUTTON_BG_COLOR);
		paneButtons.add(btnDecline);
		
		btnAccept = new JButton("Accept");
		btnAccept.setBackground(ColorUtil.BUTTON_PRIMARY_BG_COLOR);
		btnAccept.setForeground(Color.WHITE);
		paneButtons.add(btnAccept);
	}
	
	private void showLicenseText(String filename){
		URL helpURL = License.class.getResource(filename);	//"xx.html"	
		if (helpURL != null) {
		    try {
		        editorPane.setPage(helpURL);
		    } catch (IOException ex) {
		        System.err.println("Attempted to read a bad URL: " + helpURL);
		    }
		} else {
		    System.err.println("en.html");
		}
	}

	public void actionPerformed(ActionEvent e) {
		String language = (String)ddlLicenses.getSelectedItem();
		String langCode = "en.html"; //english is default
		switch(language){
			case "Spanish":
				langCode = "es.html";
				break;
			case "French":
				langCode = "fr.html";
				break;
			case "Italian":
				langCode = "it.html";
				break;
			case "Czech":
				langCode = "cs.html";
				break;
			case "German":
				langCode = "de.html";
				break;
			case "Greek":
				langCode = "el.html";
				break;
			case "Indonesian":
				langCode = "in.html";
				break;
			case "Japanese":
				langCode = "ja.html";
				break;
			case "Korean":
				langCode = "ko.html";
				break;
			case "Lithuanian":
				langCode = "lt.html";
				break;
			case "Polish":
				langCode = "pl.html";
				break;
			case "Portuguese":
				langCode = "pt.html";
				break;
			case "Russian":
				langCode = "ru.html";
				break;
			case "Slovenian":
				langCode = "sl.html";
				break;
			case "Turkish":
				langCode = "tr.html";
				break;
			case "Chinese (Traditional)":
				langCode = "zh_TW.html";
				break;
			case "Chinese (Simplified)":
				langCode = "zh.html";
				break;
			default:
				langCode = "en.html";		
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		showLicenseText(langCode);
		setCursor(null); //turn off the wait cursor
	}
}
