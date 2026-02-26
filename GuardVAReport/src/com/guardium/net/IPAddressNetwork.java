/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;


public abstract class IPAddressNetwork {
			
	public IPAddress getNetworkMask(int prefixBits) {
		return getNetworkMask(prefixBits, true);
	}
	
	public abstract IPAddress getNetworkMask(int prefixBits, boolean withPrefixLength);
	
	public abstract IPAddress getHostMask(int prefixBits);
}
