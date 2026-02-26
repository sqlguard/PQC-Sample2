/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.TreeMap;

public class IPStringBuilderOptions {
	public static final int SIMPLE = 0;//no compressions, lowercase only, no leading zeros, no mixed, no nothing
	
	public static final int LEADING_ZEROS_FULL_ALL_SEGMENTS = 0x10; //0001:0002:00ab:0abc::
	public static final int LEADING_ZEROS_FULL_SOME_SEGMENTS = 0x20 | LEADING_ZEROS_FULL_ALL_SEGMENTS; //1:0002:00ab:0abc::, 0001:2:00ab:0abc::, ...
	public static final int LEADING_ZEROS_PARTIAL_SOME_SEGMENTS = 0x40 | LEADING_ZEROS_FULL_SOME_SEGMENTS; //1:02:00ab:0abc::, 01:2:00ab:0abc::, ...

	final int options;
	
	public IPStringBuilderOptions(int options) {
		this.options = options;
	}
	
	public IPStringBuilderOptions() {
		this(SIMPLE);
	}
	
	boolean includes(int option) {
		return (option & options) == option;
	}
	
	@Override
	public String toString() {
		TreeMap<Integer, String> options = new TreeMap<Integer, String>();
		Field fields[] = getClass().getFields();
		for(Field field: fields) {
			int modifiers = field.getModifiers();
			if(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
				try {
					int constant = field.getInt(null);
					String option = field.getName() + ": " + includes(constant) + System.lineSeparator();
					options.put(constant, option);
				} catch(IllegalAccessException e) {}
			}
		}
		Collection<String> values = options.values(); //the iterator for this Collection is sorted since we use a SortedMap
		StringBuilder builder = new StringBuilder();
		for(String val : values) {
			builder.append(val);
		}
		return builder.toString();
	}
}