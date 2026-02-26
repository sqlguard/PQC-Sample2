/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import com.guardium.net.IPAddress.IpVersion;
import com.guardium.utils.i18n.SayAppRes;

/**
 * Represents situations when an object represents a valid type or format but that type does not match the required type or format for a given operation.
 * 
 * @author sfoley
 *
 */
@SuppressWarnings("serial")
public class IPAddressTypeException extends RuntimeException {
	
	public IPAddressTypeException(IPAddressString one, IpVersion version, String key) {
		super(one + ", " + version.name() + ", " + SayAppRes.what("ipaddress.address.error") + " " + SayAppRes.what(key));
	}
	
	public IPAddressTypeException(IPAddress one, IPAddress two, String key) {
		super(one + ", " + two + ", " + SayAppRes.what("ipaddress.address.error") + " " + SayAppRes.what(key));
	}
	
	public IPAddressTypeException(IPAddressSegment one, IPAddressSegment two, String key) {
		super(one + ", " + two + ", " + SayAppRes.what("ipaddress.address.error") + " " + SayAppRes.what(key));
	}
	
	public IPAddressTypeException(IPAddressSection one, IPAddressSection two, String key) {
		super(one + ", " + two + ", " + SayAppRes.what("ipaddress.address.error") + " " + SayAppRes.what(key));
	}
}
