/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;


//import com.guardium.data.SingletonObjectDemo.SingletonClass;
//import com.guardium.data.SingletonObjectDemo.is;



public class SingletonClass {

	private static SingletonClass singletonObject;
	/** A private Constructor prevents any other class from instantiating. */
	
	private int i = 100;
	
	private SingletonClass() {
		//	 Optional Code
		System.out.println("do we go here once");
		i = 10;
	}
	public static synchronized SingletonClass getSingletonObject() {
		if (singletonObject == null) {
			singletonObject = new SingletonClass();
		}
		return singletonObject;
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public int getValue() {
		return i;
	}
}

