/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.IOException;

/*
 * Wraps a StringBuffer or StringBuilder so that when its length exceeds a given limit,
 * the bugger is cleared.
 */
public class LimitedAppendable implements Appendable {
	private StringBuffer buffer;
	private StringBuilder builder;
	private Appendable appendable;
	private int limit;
	
	public LimitedAppendable(int limit) {
		this(new StringBuilder(), limit);
	}
	
	public LimitedAppendable(StringBuffer buffer, int limit) {
		this.appendable = this.buffer = buffer;
		this.limit = limit;
	}
	
	public LimitedAppendable(StringBuilder builder, int limit) {
		this.appendable = this.builder = builder;
		this.limit = limit;
	}
	
	@Override
	public Appendable append(CharSequence csq) throws IOException {
		return append(csq, 0, csq.length());
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		int total = end - start;
		int available = limit - length() + 1;
		while(available <= total) {
			end = start + available;
			appendable.append(csq, start, end);
			dump();
			clear();
			start = end;
			total -= available;
			available = limit + 1;
		}
		appendable.append(csq, start, start + total);
		return this;
    }

	@Override
	public Appendable append(char c) throws IOException {
    	appendable.append(c);
    	if(length() > limit) {
    		dump();
    		clear();
    	}
    	return this;
    }
	
	public void dump() {
		if(length() > 0) {
			AdHocLogger.logDebug("LimitedAppendable reads: " + appendable, AdHocLogger.LOG_DEBUG);
		}
	}
	
	public void clear() {
		if(length() > 0) {
			if(buffer == null) {
				builder.setLength(0);
			} else {
				buffer.setLength(0);
			}
		}
	}
	
	public int length() {
		return buffer == null ? builder.length() : buffer.length();
	}
	
	@Override
	public String toString() {
		return appendable.toString();
	}
}