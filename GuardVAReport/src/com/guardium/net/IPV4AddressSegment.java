/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.util.Iterator;

import com.guardium.net.IPAddress.IpVersion;

/**
 * This represents a segment of an IP address.  For IPV4, segments are 1 byte.  For IPV6, they are two bytes.
 * 
 * Like String and Integer and various others, segments are immutable, in the sense that its value will not change.
 * 
 * It is also thread-safe.
 * 
 * @author unknown
 *
 */
class IPV4AddressSegment extends IPAddressSegment {
	
	public static final int MAX_CHARS = Integer.toString(getMaxSegmentValue(IpVersion.IPV4), getPrintableRadix(IpVersion.IPV4)).length();

	private static final IPV4AddressSegment ZERO_SEGMENT = new IPV4AddressSegment(0);
	
	/**
	 * Represents a segment of an IPV4 address with the given value.
	 * 
	 * @param value the value of the segment
	 */
	IPV4AddressSegment(int value) {
		super(value);
	}
	
	/**
	 * Represents a segment of an IPV4 address.
	 * 
	 * @param value the value of the segment.  If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix.
	 * @param segmentPrefix the segment prefix, which can be null
	 */
	IPV4AddressSegment(int value, Integer segmentPrefixBits) {
		super(value, segmentPrefixBits);
	}
	
	/**
	 * Represents a segment of an IPV4 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents. 
	 * @param originalString a string describing the segment.  
	 * 		If value is null, this string must describe either a single value, a range (a-b) or all values (*)
	 */
	IPV4AddressSegment(Integer value, String originalString) {
		super(value, originalString);
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents.  
	 * 		If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix.
	 * @param originalString a string describing the segment.  If value is null, this string must describe either a single value, a single range (a-b) or the full range of all possible values (*).  
	 * 		If the segmentPrefixBits is non-null, the network prefix of all segment values is used, and the segment represents all additional segment values with the same network prefix.
	 * @param segmentPrefixBits the segment prefix length, which can be null
	 */
	IPV4AddressSegment(Integer value, String originalString, Integer segmentPrefixBits) {
		super(value, originalString, segmentPrefixBits);
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address that represents a range of values.
	 * 
	 * @param originalString a string describing the segment.  This string must describe the range of values indicated by lower and upper.
	 * @param segmentPrefixBits the segment prefix length, which can be null.    If segmentPrefixBits is non-null, this segment represents a range of network prefixes.
	 * @param lower the lower value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the lower value becomes the smallest value with the same network prefix.
	 * @param upper the upper value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the upper value becomes the largest value with the same network prefix.
	 */
	IPV4AddressSegment(String originalString, Integer segmentPrefixBits, int lower, int upper) {
		super(originalString, segmentPrefixBits, lower, upper);
	}
	
	@Override
	public boolean isIpv4() {
		return true;
	}
	
	@Override
	public IPV4AddressSegment toIpv4() {
		return this;
	}
	
	@Override
	public int getMaxSegmentValue() {
		return getMaxSegmentValue(IpVersion.IPV4);
	}
	
	@Override
	public IPV4AddressSegment toNetworkSegment(Integer bits) {
		return (IPV4AddressSegment) super.toNetworkSegment(bits);
	}
	
	@Override
	IPV4AddressSegment toNetworkSegment(int mask, Integer prefixBits) {
		return super.toNetworkSegment(mask, prefixBits, IPV4Address.network.creator);
	}
	
	@Override
	public IPV4AddressSegment toHostSegment(Integer prefixBits) {
		return (IPV4AddressSegment) super.toHostSegment(prefixBits);
	}
	
	@Override
	IPV4AddressSegment toHostSegmentFromMask(int mask) {
		return super.toHostSegmentFromMask(mask, IPV4Address.network.creator);
	}
	
	/* returns a new segment masked by the given mask */
	@Override
	IPV4AddressSegment toMaskedSegment(IPAddressSegment maskSegment, Integer segmentPrefixBits) throws IPAddressTypeException {
		if(isChangedByMask(maskSegment, segmentPrefixBits)) {
			return new IPV4AddressSegment(value & maskSegment.value, segmentPrefixBits);
		}
		return this;
	}
	
	@Override
	boolean isChangedByMask(IPAddressSegment maskSegment, Integer segmentPrefixBits) throws IPAddressTypeException {
		if(!(maskSegment instanceof IPV4AddressSegment)) {
			throw new IPAddressTypeException(this, maskSegment, "ipaddress.error.typeMismatch");
		}
		return super.isChangedByMask(maskSegment, segmentPrefixBits);
	}
	
	@Override
	public Iterator<IPV4AddressSegment> iterator() {
		return iterator(IPV4Address.network.creator);
	}
	
	static IPV4AddressSegment getZeroSegment() {
		return ZERO_SEGMENT;
	}
	
	@Override
	public int getBitCount() {
		return IPV4Address.BITS_PER_SEGMENT;
	}
	
	@Override
	public int getByteCount() {
		return IPV4Address.BYTES_PER_SEGMENT;
	}
	
	@Override
	public int getPrintableRadix() {
		return IPAddressString.IPV4_RADIX;
	}
	
	@Override
	public int getPrintableCharsPerSegment() {
		return MAX_CHARS;
	}

	@Override
	public int compareTo(IPAddressSegment other) {
		if(other instanceof IPV6AddressSegment) {
			return -1;
		}
		return super.compareTo(other);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof IPV4AddressSegment)) {
			return false;
		}
		IPV4AddressSegment otherSegment = (IPV4AddressSegment) other;
		return isSameValues(otherSegment);
	}
}