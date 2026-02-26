/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.utils;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractDb2InfoSaxParser extends DefaultHandler 
{

	protected static final String sstring = "string";
	protected static final String skey = "key";
	protected static final String svalue = "value";
	protected static final String sinteger = "integer";
	
	private String element = null;
	private String value = null;
	Map<String, String> nameValueMap = null;

	public AbstractDb2InfoSaxParser(Map<String, String> nvm) 
	{
		nameValueMap = nvm;
	}
	
	abstract void setProperty(String element, String value) throws Exception; 
	
	public void endDocument() throws SAXException 
	{
		super.endDocument();	
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException 
	{
				super.startElement(uri, localName, qName, attributes);
				element = localName;
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException 
	{
				/* this implementation of characters takes care of the fact that the method 
				*  might be called a few times for a long element with chunks of the element's text
				*/
				super.characters(ch, start, length);
			    if (value == null)
			        value = String.valueOf(ch, start, length);
			    else
			        value += String.valueOf(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException 
	{
			    if (element != null && value != null)
			    {
			        try
			        {
			        	setProperty(element.trim(), value.trim());
			        }
			        catch (Exception ex)
					{
						ex.printStackTrace();
						throw new SAXException(ex.getMessage());
					}
			        element = null;
			        value = null;
			    }
	}

	

	public void error(SAXParseException e) throws SAXException 
	{
		// TODO Auto-generated method stub
		super.error(e);
		e.printStackTrace();
	}

	public void fatalError(SAXParseException e) throws SAXException 
	{
		// TODO Auto-generated method stub
		super.fatalError(e);
		e.printStackTrace();
	}

	public void warning(SAXParseException e) throws SAXException 
	{
		// TODO Auto-generated method stub
		super.warning(e);
		e.printStackTrace();
	}

}