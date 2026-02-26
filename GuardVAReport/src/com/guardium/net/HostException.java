/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import com.guardium.utils.i18n.SayAppRes;

@SuppressWarnings("serial")
public class HostException extends Exception {
	private Host address;
	
	public HostException(Host address, String key) {
		super(address + " " + SayAppRes.what("ipaddress.host.error") + " " + SayAppRes.what(key));
		this.address = address;
	}
	
	public HostException(Host address, IPAddressException e, String key) {
		super(address + " " + SayAppRes.what("ipaddress.host.error") + " " + SayAppRes.what(key), e);
		this.address = address;
	}

	public String getAddress() {
		return address.toString();
	}
}
