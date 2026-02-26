/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadDumpFile {

	String table_name = "";
	List<List> table_list = new ArrayList<List>();
	List<String[]> field_list = new ArrayList<String[]>();
	boolean table_start = false;
	boolean row_start = false;
	boolean field_start = false;
	
	public List getTableList () {
		return table_list;
	}
	
	public String getTableName () {
		return table_name;
	}
	
	public boolean readFile (String fileName) {
		boolean readOK = false;
		try {
			File file = new File(fileName);
			
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                             .newDocumentBuilder();

			Document doc = dBuilder.parse(file);

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			if (doc.hasChildNodes()) {

				printNote(doc.getChildNodes());

			}
			readOK = true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return readOK;
		}
		return readOK;
	}
	
	private void printNote(NodeList nodeList) {

		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE ) {

			    // if (tempNode.getNodeName().equals("row"))
				String tmp_property_name = "";
				String tmp_property_value = "";
				
				// get node name and value
				// System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("Node Value =" + tempNode.getTextContent());

				
				if (tempNode.getNodeName().equals("table_data")) {
					//table_name = tempNode.getNodeValue(); // tempNode.getTextContent();
					table_start = true;
					if (tempNode.hasAttributes()) {
						// get attributes names and values
	                    NamedNodeMap nodeMap2 = tempNode.getAttributes();
	                    //for (int i = 0; i < nodeMap.getLength(); i++) {
	                    // only need the first one
	                    for (int i = 0; i < 1; i++) {
	                    	Node node = nodeMap2.item(i);
	                        //System.out.println("attr name : " + node.getNodeName());
	                        //System.out.println("attr value : " + node.getNodeValue());
	                        table_name =  node.getNodeValue();
	                    }
					}
					
				}
				else {
					if (tempNode.getNodeName().equals("row")) {
						row_start = true;
					}
					else {
						if (tempNode.getNodeName().equals("field")) {
							field_start = true;
						}
					}
				}

				
				tmp_property_value = tempNode.getTextContent();
				
				if (tempNode.hasAttributes()) {
					// get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    //for (int i = 0; i < nodeMap.getLength(); i++) {
                    // only need the first one
                    for (int i = 0; i < 1; i++) {
                    	Node node = nodeMap.item(i);
                        //System.out.println("attr name : " + node.getNodeName());
                        //System.out.println("attr value : " + node.getNodeValue());
                        tmp_property_name =  node.getNodeValue().toLowerCase();
                    }
				}
				if (tempNode.hasChildNodes()) {
					// loop again if has child nodes
                    printNote(tempNode.getChildNodes());
				}
				
				
				//System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
				
				if (table_start) {
					if (tempNode.getNodeName().equals("table_data")) {
						table_start = false;
					}
					else if (tempNode.getNodeName().equals("row")) {
						table_list.add(field_list);
						field_list = new ArrayList<String[]>();
						row_start = false;
					}
					else if (tempNode.getNodeName().equals("field")) {
							//System.out.println("property name = " + tmp_property_name);
							//System.out.println("property valu = " + tmp_property_value);
							String[] prot_setting=new String[2];
							prot_setting[0] = tmp_property_name;
							prot_setting[1] = tmp_property_value;
							field_list.add(prot_setting);
							field_start = false;
					}
				}
				
			}
		}
	}
	
	public void dumpTable () {
		for (List rlist: table_list) {
			System.out.println("rlist size " + rlist.size());	
			
			int len = rlist.size();

			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
                name = str[0];
                value = str[1];
                System.out.println(name + "=" + value);
			}
		}
		
	}
	
	private void dumpTestId () {
		for (List rlist: table_list) {
			System.out.println("rlist size " + rlist.size());	
			
			int len = rlist.size();
            String test_id = "";
            String test_desc = "";
            String test_type = "";
			for (int i = 0; i < len; i++) {
				String str[] = (String [])rlist.get(i);
				String name  = null;
				String value = null;				
                name = str[0];
                value = str[1];
                if (name.equals("prot_test_id")) {
                    test_id = value;
                }
                else if (name.equals("prot_test_desc")) {
                    test_desc = value;
                }
                else if (name.equals("prot_test_type")) {
                    test_type = value;
                }               
			}
			System.out.println( "test_id = " + test_id);
			System.out.println( "test_desc = " + test_desc);
			System.out.println( "test_type = " + test_type);
		}
		
	}
	
	
}
