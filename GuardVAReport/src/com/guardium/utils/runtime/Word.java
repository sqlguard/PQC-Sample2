/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

/**
 * Similar to a "word" in bash
 * @author unknown
 *
 */
class Word extends RegexAlternative {
	final char encloser;
	
	Word(int count, char encloser) {
		super(count, encloser + "([^" + encloser + "]*)" + encloser);//could add support for escaped quotes eg \"
		this.encloser = encloser;
	}
	
	Word(int count) {
		super(count, "(\\S+)");//a sequence of non-whitespace characters
		encloser = ' ';
	}
}