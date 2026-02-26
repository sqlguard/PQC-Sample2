/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Aug 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.guardium.data;


/**
 * @author msanayei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataSourceConnectException extends DataSourceException {

	public static final int CANNOT_CONNECTTO_LOCALHOST = 200;
	
	private int messageId = 0;

	/**
	 * 
	 */
	public DataSourceConnectException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public DataSourceConnectException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor for DatabaseConnectException.
	 * @param message
	 * @param message Id
	 */
	public DataSourceConnectException(String message, int mId) {
		super(message);
		messageId = mId;
	}

	/**
	 * Constructor for DatabaseConnectException.
	 * @param message
	 * @param cause
	 * @param message Id
	 */
	public DataSourceConnectException(String message, Throwable cause, int mId) {
		super(message, cause);
		messageId = mId;
	}


	/**
	 * @param cause
	 */
	public DataSourceConnectException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataSourceConnectException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public DataSourceConnectException(int mId) {
		super();
		messageId = mId;
	}

	public int getMessageId() {
		return messageId;
	}	

}
