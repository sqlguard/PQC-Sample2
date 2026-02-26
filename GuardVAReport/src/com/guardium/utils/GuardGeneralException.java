/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

public class GuardGeneralException extends Exception{

	/** 
	 * I  created this class following GuardRepGeneralException, GuardAuthenticationException --RUI
	 */
	public static final int INSUFFICIENT_RUNTIME_PARAMETERS = 200;
	public static final int AUDIT_CEF_GEN_RESULT_UNPARSABLE = 201;
	public static final int INVALID_CSV_FILE = 300;
	public static final int TRANSFER_CTRL = 400;
	public static final int USER_INPUT_INVALID = 500;
	
	int type=0;
	
	private Object info = null;
	
	public GuardGeneralException(int type)
	{
		super();
		this.type = type;
	}
	
	public GuardGeneralException(String msg, int type)
	{
		super(msg);
		this.type = type;
	}
	
	public GuardGeneralException(Throwable e, int type)
	{
		super(e);
		this.type = type;
	}
	

	public int getType() {
		return type;
	}
	
	public Object getInfo(){
		return info;
	}
	
	public String getName(){
	    switch(type){   
	    	case INSUFFICIENT_RUNTIME_PARAMETERS: return "Insufficient Runtime Parameters";
	        default: return "GuardGeneralException";
	    }
	}
}
