/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dtoland on May 23, 2006 at 9:12:29 AM
 * Used for a discriminator.
 */
public abstract class AbstractInnerType implements Comparable<AbstractInnerType> {
	/** The type name */
	private final transient String typename;

	/** The type's list of equivalent names */
	private final transient List<String> equivalents = new ArrayList<String>();

	/**
	 * Initializing constructor.
	 * @param typename The type name.
	 * @param equivalents Names that are equivalent to this abstract type.
	 */
	protected AbstractInnerType(String typename, String[] equivalents) {
		this.typename = typename.toUpperCase();
		if (equivalents!=null) {
			for (int i=0; i<equivalents.length; i++) {
				this.equivalents.add( equivalents[i].trim().toUpperCase() );
			}
		}
	}

	/**
	 * Initializing constructor.
	 * @param typename The type name.
	 */
	protected AbstractInnerType(String typename) {
		this(typename, null);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.typename;
	}

	/**
	 * @param type
	 * @return Whether the passed in type is the same type.
	 */
	public boolean isEquivalent(String type) {
		if (type==null) return false;

		String value = type.trim().toUpperCase();
		if ( this.typename.equals(value) ) return true;
		return this.equivalents.contains(value);
	}

	/**
	 * @return The type's name
	 */
	public String getTypename() {
		return this.typename;
	}

	/**
	 * @param types The list of types to search
	 * @param name The name that may be a type name or equivalent.
	 * @return The type that matches the name.
	 * @throws TypeNotSupportedException It the name is not found in the types and their equivalents.
	 */
	public static AbstractInnerType findByName(AbstractInnerType[] types, String name)
	throws TypeNotSupportedException {
		for (int i=0; i<types.length; i++) {
			if ( types[i].isEquivalent(name) ) {
				return types[i];
			}
		}
		throw new TypeNotSupportedException("'" + name + "' is not a member of: " + types);
	}

	/**
	 * @return This type's equivalent names
	 */
	public List<String> getEquivalents() {
		return this.equivalents;
	}

	/**
	 * @param that
	 * @return The sort order of the two objects
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(AbstractInnerType that) {
		return that.getTypename().compareTo( this.getTypename() );
	}
}
