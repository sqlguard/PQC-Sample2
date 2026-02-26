/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import com.guardium.net.IPAddress.IpVersion;


public class IPV6AddressNetwork extends IPAddressTypeNetwork<IPV6Address, IPV6AddressSegment> {
	
private static final IPV6AddressSegment emptySegments[] = {};
	
	static class IPV6AddressCreator implements IPAddressCreator<IPV6Address, IPV6AddressSegment> {
		@Override
		public IPV6Address createAddress(IPV6AddressSegment segments[]) {
			return new IPV6Address(segments, false, null);
		}
		
		@Override
		public IPV6AddressSegment[] createAddressSegmentArray(int length) {
			if(length == 0) {
				return emptySegments;
			}
			return new IPV6AddressSegment[length];
		}
		
		@Override
		public IPV6AddressSegment createAddressSegment(int value) {
			return new IPV6AddressSegment(value);
		}
		
		@Override
		public IPV6AddressSegment createAddressSegment(int value, Integer prefixBits) {
			return new IPV6AddressSegment(value, prefixBits);
		}
		
		@Override
		public IPV6AddressSegment createAddressSegment(String originalString, Integer prefixBits, int lower, int upper){
			return new IPV6AddressSegment(originalString, prefixBits, lower, upper);
		}
	};
	
	IPV6AddressNetwork() {
		super(IPV6Address.class, new IPV6AddressCreator());
	}
	
	@Override
	public boolean isIpv6() {
		return true;
	}
	
	@Override
	public IpVersion getIpVersion() {
		return IpVersion.IPV6;
	}
	
}
