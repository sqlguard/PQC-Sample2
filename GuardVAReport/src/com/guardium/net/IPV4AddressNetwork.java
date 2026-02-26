/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import com.guardium.net.IPAddress.IpVersion;


public class IPV4AddressNetwork extends IPAddressTypeNetwork<IPV4Address, IPV4AddressSegment> {
	
	IPV4AddressNetwork() {
		super(IPV4Address.class, new IPAddressCreator<IPV4Address, IPV4AddressSegment>() {
			IPV4AddressSegment emptySegments[] = new IPV4AddressSegment[0];
			
			@Override
			public IPV4Address createAddress(IPV4AddressSegment segments[]) {
				return new IPV4Address(segments);
			}
			
			@Override
			public IPV4AddressSegment[] createAddressSegmentArray(int length) {
				if(length == 0) {
					return emptySegments;
				}
				return new IPV4AddressSegment[length];
			}
			
			@Override
			public IPV4AddressSegment createAddressSegment(int value) {
				return new IPV4AddressSegment(value);
			}
			
			@Override
			public IPV4AddressSegment createAddressSegment(int value, Integer prefixBits) {
				return new IPV4AddressSegment(value, prefixBits);
			}
			
			@Override
			public IPV4AddressSegment createAddressSegment(String originalString, Integer prefixBits, int lower, int upper){
				return new IPV4AddressSegment(originalString, prefixBits, lower, upper);
			}
		});
	}
	
	@Override
	public boolean isIpv4() {
		return true;
	}
	
	@Override
	public IpVersion getIpVersion() {
		return IpVersion.IPV4;
	}
}
