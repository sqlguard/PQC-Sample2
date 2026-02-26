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
 * Like String and Integer and various others basic objects, segments are immutable, which also makes them thread-safe.
 * 
 * @author sfoley
 *
 */
class IPV6AddressSegment extends IPAddressSegment {
	
	public static final int MAX_CHARS = Integer.toString(getMaxSegmentValue(IpVersion.IPV6), getPrintableRadix(IpVersion.IPV6)).length();
	
	private static final IPV6AddressSegment ZERO_SEGMENT = new IPV6AddressSegment(0);
	
	
	/**
	 * Represents a segment of an IPV6 address with the given value.
	 * 
	 * @param value the value of the segment
	 */
	IPV6AddressSegment(int value) {
		super(value);
	}
	
	/**
	 * Represents a segment of an IPV6 address.
	 * 
	 * @param value the value of the segment.  If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix.
	 * @param segmentPrefix the segment prefix length, which can be null
	 */
	IPV6AddressSegment(int value, Integer segmentPrefixBits) {
		super(value, segmentPrefixBits);
	}
	
	/**
	 * Represents a segment of an IPV6 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents. 
	 * @param originalString a string describing the segment.  
	 * 		If value is null, this string must describe either a single value, a range (a-b) or all values (*)
	 */
	IPV6AddressSegment(Integer value, String originalString) {
		super(value, originalString);
	}
	
	/**
	 * Represents a segment of an IPV6 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents.  
	 * 		If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix.
	 * @param originalString a string describing the segment.  If value is null, this string must describe either a single value, a single range (a-b) or the full range of all possible values (*).  
	 * 		If the segmentPrefixBits is non-null, the network prefix of all segment values is used, and the segment represents all additional segment values with the same network prefix.
	 * @param segmentPrefixBits the segment prefix length, which can be null
	 */
	IPV6AddressSegment(Integer value, String originalString, Integer segmentPrefixBits) {
		super(value, originalString, segmentPrefixBits);
	}
	
	/**
	 * Represents a segment of an IPV6 address that represents a range of values.
	 * 
	 * @param originalString a string describing the segment.  This string must describe the range of values indicated by lower and upper.
	 * @param segmentPrefixBits the segment prefix length, which can be null.    If segmentPrefixBits is non-null, this segment represents a range of network prefixes.
	 * @param lower the lower value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the lower value becomes the smallest value with the same network prefix.
	 * @param upper the upper value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the upper value becomes the largest value with the same network prefix.
	 */
	IPV6AddressSegment(String originalString, Integer segmentPrefixBits, int lower, int upper) {
		super(originalString, segmentPrefixBits, lower, upper);
	}
	
	@Override
	public boolean isIpv6() {
		return true;
	}
	
	@Override
	public IPV6AddressSegment toIpv6() {
		return this;
	}
	
	@Override
	public int getMaxSegmentValue() {
		return getMaxSegmentValue(IpVersion.IPV6);
	}
	
	@Override
	public IPV6AddressSegment toNetworkSegment(Integer prefixBits) {
		return (IPV6AddressSegment) super.toNetworkSegment(prefixBits);
	}

	@Override
	IPV6AddressSegment toNetworkSegment(int mask, Integer prefixBits) {
		return super.toNetworkSegment(mask, prefixBits, IPV6Address.network.creator);
	}
	
	@Override
	public IPV6AddressSegment toHostSegment(Integer prefixBits) {
		return (IPV6AddressSegment) super.toHostSegment(prefixBits);
	}
	
	@Override
	IPV6AddressSegment toHostSegmentFromMask(int mask) {
		return super.toHostSegmentFromMask(mask, IPV6Address.network.creator);
	}
	
	/* returns a new segment masked by the given mask */
	@Override
	IPV6AddressSegment toMaskedSegment(IPAddressSegment maskSegment, Integer segmentPrefixBits) throws IPAddressTypeException {
		if(isChangedByMask(maskSegment, segmentPrefixBits)) {
			return new IPV6AddressSegment(value & maskSegment.value, segmentPrefixBits);
		}
		return this;
	}
	
	@Override
	boolean isChangedByMask(IPAddressSegment maskSegment, Integer segmentPrefixBits) throws IPAddressTypeException {
		if(!(maskSegment instanceof IPV6AddressSegment)) {
			throw new IPAddressTypeException(this, maskSegment, "ipaddress.error.typeMismatch");
		}
		return super.isChangedByMask(maskSegment, segmentPrefixBits);
	}
	
	@Override
	public Iterator<IPV6AddressSegment> iterator() {
		return iterator(IPV6Address.network.creator);
	}
	
	static IPV6AddressSegment getZeroSegment() {
		return ZERO_SEGMENT;
	}
	
	@Override
	public int getBitCount() {
		return IPV6Address.BITS_PER_SEGMENT;
	}
	
	@Override
	public int getByteCount() {
		return IPV6Address.BYTES_PER_SEGMENT;
	}
	
	@Override
	public int getPrintableRadix() {
		return IPAddressString.IPV6_RADIX;
	}
	
	@Override
	public int getPrintableCharsPerSegment() {
		return MAX_CHARS;
	}
	
	private static boolean isAlphabetic(int i) {
		return i >= 0xa;
	}
	
	@Override
	public boolean hasAlphabeticDigits() {
		int high = highByte();
		int low = lowByte();
		return isAlphabetic(0xf & high) || isAlphabetic(0xf & low) || isAlphabetic(0xf & (high >> 4)) || isAlphabetic(0xf & (low >> 4));
	}
	
	/**
	 * Splits this address segment into one-byte segments
	 * @return
	 */
	public IPV4AddressSegment[] split() {
		if(!isMultiple()) {
			Integer highPrefixBits = IPAddressSection.getSplitSegmentPrefixBits(IPV4Address.BITS_PER_SEGMENT, segmentPrefixBits, 0);
			Integer lowPrefixBits = IPAddressSection.getSplitSegmentPrefixBits(IPV4Address.BITS_PER_SEGMENT, segmentPrefixBits, 1);
			return new IPV4AddressSegment[] {
					new IPV4AddressSegment(highByte(), null, highPrefixBits),
					new IPV4AddressSegment(lowByte(), null, lowPrefixBits)
			};
		}
		return splitMultiple();
	}
	
	private IPV4AddressSegment[] splitMultiple() {
		int highLower = highByte(value);
		int highUpper = highByte(upperValue);
		int lowLower = lowByte(value);
		int lowUpper = lowByte(upperValue);
		
		IPV4AddressSegment one, two;
		Integer highPrefixBits = IPAddressSection.getSplitSegmentPrefixBits(IPV4Address.BITS_PER_SEGMENT, segmentPrefixBits, 0);
		Integer lowPrefixBits = IPAddressSection.getSplitSegmentPrefixBits(IPV4Address.BITS_PER_SEGMENT, segmentPrefixBits, 1);
		if(highLower == highUpper) {
			one = new IPV4AddressSegment(highLower, Integer.toString(highLower), highPrefixBits);
		} else {
			String str;
			if(highLower == 0 && highUpper == getMaxSegmentValue(IpVersion.IPV4)) {
				str = IPAddress.SEGMENT_WILDCARD_STR;
			} else {
				str = highLower + IPAddress.RANGE_SEPARATOR_STR + highUpper;
			}
			one = new IPV4AddressSegment(str, highPrefixBits, highLower, highUpper);
		}
		if(lowLower == lowUpper) {
			two = new IPV4AddressSegment(lowLower, Integer.toString(lowLower), lowPrefixBits);
		} else {
			String str;
			if(lowLower == 0 && lowUpper == getMaxSegmentValue(IpVersion.IPV4)) {
				str = IPAddress.SEGMENT_WILDCARD_STR;
			} else {
				str = lowLower + IPAddress.RANGE_SEPARATOR_STR + lowUpper;
			}
			two = new IPV4AddressSegment(str, lowPrefixBits, lowLower, lowUpper);
		}
		return new IPV4AddressSegment[] { one, two };
	}
	
	/**
	 * Splits two IPV6 segments into four IPV4 segments.
	 * 
	 * @param high
	 * @param low
	 * @return
	 */
	static IPV4AddressSegment[] split(IPV6AddressSegment high, IPV6AddressSegment low) {
		IPV4AddressSegment[] oneSplit = high.split();
		IPV4AddressSegment[] twoSplit = low.split();
		return new IPV4AddressSegment[] { oneSplit[0], oneSplit[1], twoSplit[0], twoSplit[1] };
	}

	@Override
	public int compareTo(IPAddressSegment other) {
		if(other instanceof IPV4AddressSegment) {
			return 1;
		}
		return super.compareTo(other);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof IPV6AddressSegment)) {
			return false;
		}
		IPV6AddressSegment otherSegment = (IPV6AddressSegment) other;
		return isSameValues(otherSegment);
	}
	
	@Override
	boolean checkChar(CharNormalizer charNormalizer) {
		switch(charNormalizer.getCurrentChar()) {
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': 
				charNormalizer.lowercase();
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
				break;
			default:
				return super.checkChar(charNormalizer);
		}
		return true;
	}
}