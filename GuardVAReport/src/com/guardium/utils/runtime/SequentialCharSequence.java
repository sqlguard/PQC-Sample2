/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.util.Arrays;
import java.util.List;

/**
 * Treats a sequence of String, StringBuilder and StringBuffer like a single CharSequence
 * @author unknown
 *
 */
public class SequentialCharSequence implements CharSequence {
	private final CharSequence start;
	private final SequentialCharSequence next;
	
	public SequentialCharSequence(CharSequence start) {
		this(start, null);
	}
	
	public SequentialCharSequence(CharSequence start, SequentialCharSequence next) {
		this.start = start;
		this.next = next;
	}
	
	public SequentialCharSequence(List<CharSequence> multiple) {
		int startIndex = 0;
		for(; multiple.get(startIndex) == null && startIndex < multiple.size(); startIndex++);
		CharSequence first;
		if(startIndex < multiple.size()) {
			first = multiple.get(startIndex);
		} else {
			first = "";
		}
		SequentialCharSequence previous = null;
		for(int i=multiple.size() - 1; i > startIndex; i--) {
			CharSequence nextSequence = multiple.get(i);
			if(nextSequence != null) {
				previous = new SequentialCharSequence(nextSequence, previous);
			}
		}
		this.start = first;
		this.next = previous;
	}
	
	public SequentialCharSequence(CharSequence multiple[]) {
		this(Arrays.asList(multiple));
	}
	
	@Override
	public int length() {
		if(next != null) {
			return start.length() + next.length();
		}
    	return start.length();
	}

    @Override
	public char charAt(int index) {
    	int len = start.length();
    	if(index < len) {
    		return start.charAt(index);
    	}
    	return next.charAt(index - len);
    }

    @Override
	public CharSequence subSequence(int startIndex, int endIndex) {
    	int len = start.length();
    	if(startIndex >= len) {
    		if(startIndex == len && endIndex == len) {
    			return "";
    		}
    		if(next == null) {
				throw new IndexOutOfBoundsException();
			}
    		return next.subSequence(startIndex - len, endIndex - len);
    	} else {
    		if(endIndex > len) {
    			if(next == null) {
    				throw new IndexOutOfBoundsException();
    			}
    			return start.subSequence(startIndex, len).toString() + next.subSequence(0, endIndex - len);
    		} else {
    			return start.subSequence(startIndex, endIndex);
    		}
    	}
    }

    @Override
	public String toString() {
    	if(next != null) {
			return start.toString() + next;
		}
    	return start.toString();
    }
}
