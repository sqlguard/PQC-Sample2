/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.guardium.net.IPAddress.IpVersion;

/**
 * This represents a segment of an IP address.  For IPV4, segments are 1 byte.  For IPV6, they are two bytes.
 * 
 * Like String and Integer and various others, segments are immutable, which also makes them thread-safe.
 * 
 * @author unknown
 *
 */
abstract class IPAddressSegment implements Comparable<IPAddressSegment> {
	
	interface IPAddressSegmentCreator<T extends IPAddressSegment> {
		T[] createAddressSegmentArray(int length);
		
		T createAddressSegment(int value);
		
		T createAddressSegment(int value, Integer prefixBits);
		
		T createAddressSegment(String originalString, Integer prefixBits, int lower, int upper);
	}
	
	private static final String zeroes[] = new String[] {
		"",
		"0",
		"00",
		"000",
		"0000"
	};
	
	final boolean isRange; //whether this segment originated from a range of numbers (not a CIDR range)
	final int value;
	final int upperValue;//the upper value of a CIDR or other type of range, if not a range it is the same as value
	final String originalString;
	final Integer segmentPrefixBits;//the prefix length for this segment, or null if there is none
	
	//strings cached for performance reasons
	private String cachedString;
	private String cachedWildcardString;
	private String cachedStandardString;
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address with the given value.
	 * 
	 * @param value the value of the segment
	 */
	IPAddressSegment(int value) {
		this(value, null, null);
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address.
	 * 
	 * @param value the value of the segment.  If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix.
	 * @param segmentPrefixBits the segment prefix bits, which can be null
	 */
	IPAddressSegment(int value, Integer segmentPrefixBits) {
		this(value, null, segmentPrefixBits);
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents. 
	 * @param originalString a string describing the segment.  
	 * 		If value is null, this string must describe either a single value, a range (a-b) or all values (*)
	 */
	IPAddressSegment(Integer value, String originalString) {
		this(value, originalString, null);
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address.
	 * 
	 * @param value the value of the segment.  This can be null if originalString is non-null and describes the segment contents.  
	 * 		If the segmentPrefixBits is non-null, the network prefix of the value is used, and the segment represents all segment values with the same network prefix (all network or subnet segments, in other words).
	 * @param originalString a string describing the segment.  If value is null, this string must describe either a single value, a single range (a-b) or the full range of all possible values (*).  
	 * 		If the segmentPrefixBits is non-null, the network prefix of all segment values is used, and the segment represents all additional segment values with the same network prefix (all network or subnet segments, in other words).
	 * @param segmentPrefixBits the segment prefix bits, which can be null
	 */
	IPAddressSegment(Integer value, String originalString, Integer segmentPrefixBits) {
		this.isRange = false;
		if(value == null) {
			value = 0;
		}
		if(segmentPrefixBits == null) {
			this.value = this.upperValue = value;
		} else {
			int mask = getSegmentNetworkMask(segmentPrefixBits);
			this.value = value & mask;
			this.upperValue = this.value | getSegmentHostMask(mask);
		}
		this.originalString = originalString;
		this.segmentPrefixBits = segmentPrefixBits;
	}
	
	/**
	 * Represents a segment of an IPV4 or IPV6 address that represents a range of values or a range of network prefixes.
	 * 
	 * @param originalString a string describing the segment.  This string must describe the range of values indicated by lower and upper.
	 * @param segmentPrefixBits the segment prefix bits, which can be null.  If segmentPrefixBits is non-null, this segment represents a range of network prefixes.
	 * @param lower the lower value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the lower value becomes the smallest value with the same network prefix.
	 * @param upper the upper value of the range of values represented by the segment.  If segmentPrefixBits is non-null, the upper value becomes the largest value with the same network prefix.
	 */
	IPAddressSegment(String originalString, Integer segmentPrefixBits, int lower, int upper) {
		this.isRange = lower != upper;
		if(lower > upper) {
			int tmp = lower;
			lower = upper;
			upper = tmp;
		}
		if(segmentPrefixBits == null) {
			this.value = lower;
			this.upperValue = upper;
		} else {
			int mask = getSegmentNetworkMask(segmentPrefixBits);
			this.value = lower & mask;
			this.upperValue = upper | getSegmentHostMask(mask);
		}
		this.originalString = originalString;
		this.segmentPrefixBits = segmentPrefixBits;
	}
	
	public boolean isIpv4() {
		return false;
	}
	
	public boolean isIpv6() {
		return false;
	}
	
	/**
	 * If this segment is IPV4, return this object cast to IPV4AddressSegment.  Otherwise, returns null.
	 * @return
	 */
	public IPV4AddressSegment toIpv4() {
		return null;
	}
	
	/**
	 * If this segment is IPV6, return this object cast to IPV6AddressSegment.  Otherwise, returns null.
	 * @return
	 */
	public IPV6AddressSegment toIpv6() {
		return null;
	}
	
	int getSegmentNetworkMask(int bits) {
		int fullMask = getMaxSegmentValue();
		int mask = fullMask & (fullMask << (getBitCount() - bits));
		return mask;
	}
	
	int getSegmentHostMask(int segmentCIDRNetworkMask) {
		return ~segmentCIDRNetworkMask & getMaxSegmentValue();
	}
	
	boolean isPrefixedSegment() {
		return segmentPrefixBits != null;
	}
	
	Integer getSegmentPrefixBits() {
		return segmentPrefixBits;
	}
	
	/**
	 * @param segmentValue
	 * @return whether the range of this segment matches the range of a segment with the given value and the same CIDR prefix length
	 */
	public boolean rangeMatches(int segmentValue) {
		return (!isPrefixedSegment()) ? (segmentValue == value && !isMultiple()) : rangeMatchesPrefix(segmentValue, segmentPrefixBits);
	}
	
	/**
	 * @param segmentValue
	 * @param segmentPrefix
	 * @return whether the range of this segment matches the range of a segment with the given value and CIDR prefix length
	 */
	public boolean rangeMatchesPrefix(int segmentValue, int segmentPrefixLen) {
		int mask = getSegmentNetworkMask(segmentPrefixLen);
		int expectedValue = segmentValue & mask;
		return value == expectedValue
			&& upperValue == (segmentValue | getSegmentHostMask(mask));
	}
	
	/**
	 * @param segmentValue
	 * @param segmentPrefixLen
	 * @return whether a segment with the given value and prefix length would have the same upper value
	 */
	public boolean rangeMatchesPrefixLength(int segmentValue, int upperValue, int segmentPrefixLen) {
		int upperMask = getSegmentHostMask(getSegmentNetworkMask(segmentPrefixLen));
		int upperCIDRValue = segmentValue | upperMask;
		int expectedUpperValue = upperValue | upperMask;
		return upperCIDRValue == expectedUpperValue;
	}
	
	public abstract int getMaxSegmentValue();
	
	public static int getMaxSegmentValue(IpVersion version) {
		return version == IpVersion.IPV4 ? 0xff : 0xffff;
	}
	
	boolean isNetworkChangedByPrefix(Integer bits) {
		//previously, we used to store the prefix length in the section or full address, 
		//in which case a segment could be shared more easily because the prefix length did not always need to match exactly,
		//which is why this method existed.
		
		//but now, the prefix is store in each segment.  So to reuse this segment, it must match the new prefix length exactly.
		return bits == null ? segmentPrefixBits != null : (segmentPrefixBits == null || bits.intValue() != segmentPrefixBits.intValue());
	}
	
	public IPAddressSegment toNetworkSegment(Integer bits) {
		if(isNetworkChangedByPrefix(bits)) {
			int mask = getSegmentNetworkMask(bits);
			return toNetworkSegment(mask, bits);
		}
		return this;
	}
	
	abstract IPAddressSegment toNetworkSegment(int mask, Integer prefixBits);
	
	<T extends IPAddressSegment> T toNetworkSegment(int mask, Integer prefixBits, IPAddressSegmentCreator<T> creator) {
		String originalString;
		int newLower = value & mask;
		int newUpper = (upperValue & mask) | getSegmentHostMask(mask);
		if(newLower != newUpper) {
			if(!rangeMatchesPrefixLength(newLower, newUpper, prefixBits)) {
				if(newLower == 0 && newUpper == getMaxSegmentValue()) {
					originalString = IPAddress.SEGMENT_WILDCARD_STR;
				} else {
					originalString = getRangeString(newLower, newUpper);
				}
				return creator.createAddressSegment(originalString, prefixBits, newLower, newUpper);
			} //else don't need to specify the range, the prefix length is enough to do that
		}
		return creator.createAddressSegment(newLower, prefixBits);
	}
	
	public IPAddressSegment toHostSegment(Integer bits) {
		if(isHostChangedByPrefix(bits)) {
			int mask = getSegmentHostMask(getSegmentNetworkMask(bits));
			return toHostSegmentFromMask(mask);
		}
		return this;
	}
	
	abstract IPAddressSegment toHostSegmentFromMask(int mask);
	
	<T extends IPAddressSegment> T toHostSegmentFromMask(int mask, IPAddressSegmentCreator<T> creator) {
		String originalString;
		int newLower = value & mask;
		int newUpper = upperValue & mask;
		if(newLower != newUpper) {
			if(newLower == 0 && newUpper == getMaxSegmentValue()) {
				originalString = IPAddress.SEGMENT_WILDCARD_STR;
			} else {
				originalString = getRangeString(newLower, newUpper);
			}
			return creator.createAddressSegment(originalString, null, newLower, newUpper);
		}
		return creator.createAddressSegment(newLower);
	}
	
	boolean isHostChangedByPrefix(Integer bits) {
		//previously, we used to store the prefix length in the section or full address, 
		//in which case a segment could be shared more easily because the prefix length did not always need to match exactly,
		//which is why this method existed.
		
		//but now, the prefix is stored in each segment.  So to reuse this segment, it must match the new prefix length exactly.
		
		//a host segment has no prefix, so if this remains unchanged it must have no prefix length
		if(segmentPrefixBits != null) {
			return true;
		}
		//additionally, the value must match the value for the given network prefix length
		int mask = getSegmentHostMask(getSegmentNetworkMask(bits));
		return value != (value & mask) || upperValue != (upperValue & mask);
	}
	
	/* returns a new segment masked by the given mask */
	abstract IPAddressSegment toMaskedSegment(IPAddressSegment maskSegment, Integer segmentPrefix) throws IPAddressTypeException;
	
	boolean isChangedByMask(IPAddressSegment maskSegment, Integer prefixBits) throws IPAddressTypeException {
		//note that the mask can represent a range (for example a CIDR mask), 
		//but we use the lowest value (maskSegment.value) in the range when masking (ie we discard the range)
		
		//also, we pay no attention to this.upperValue because we have checked elsewhere that the part of this segment being masked is not multiple.
		
		return this.value != (this.value & maskSegment.value) ||
				(isPrefixedSegment() ? !this.segmentPrefixBits.equals(prefixBits) : prefixBits != null);
	}
	
	/**
	 * @return whether this segment represents multiple values
	 */
	public boolean isMultiple() {
		return value != upperValue;
	}
	
	public abstract Iterator<? extends IPAddressSegment> iterator();
	
	<S extends IPAddressSegment> Iterator<S> iterator(final IPAddressSegmentCreator<S> creator) {
		if(!isMultiple()) {
			return new Iterator<S>() {
				boolean done;
				
				@Override
				public boolean hasNext() {
					return !done;
				}

			   @Override
				public S next() {
			    	if(!hasNext()) {
			    		throw new NoSuchElementException();
			    	}
			    	done = true;
			    	return IPAddressSection.cast(IPAddressSegment.this);
			    }

			    @Override
				public void remove() {
			    	throw new UnsupportedOperationException();
			    }
			};
		}
		return new Iterator<S>() {
			boolean done;
			int current = value;
			
			@Override
			public boolean hasNext() {
				return !done;
			}

		    @Override
			public S next() {
		    	if(done) {
		    		throw new NoSuchElementException();
		    	}
		    	S result = creator.createAddressSegment(current);
		    	done = ++current > upperValue;
		    	return result;
		    }

		    @Override
			public void remove() {
		    	throw new UnsupportedOperationException();
		    }
		};
	}
	
	public int getCount() {
		return upperValue - value + 1;
	}
	
	public abstract int getBitCount();
	
	public abstract int getByteCount();
	
	public static int getBitCount(IpVersion version) {
		return version == IpVersion.IPV4 ? IPV4Address.BITS_PER_SEGMENT : IPV6Address.BITS_PER_SEGMENT;
	}
	
	public static int getByteCount(IpVersion version) {
		return version == IpVersion.IPV4 ? IPV4Address.BYTES_PER_SEGMENT : IPV6Address.BYTES_PER_SEGMENT;
	}
	
	public static int getPrintableRadix(IpVersion version) {
		return version == IpVersion.IPV4 ? IPAddressString.IPV4_RADIX : IPAddressString.IPV6_RADIX;
	}
	
	public abstract int getPrintableRadix();
	
	public abstract int getPrintableCharsPerSegment();
	
	public boolean hasAlphabeticDigits() {
		return false;
	}
	
	static int getSegmentCIDRNetworkMask(int bits, IpVersion version) {
		int fullMask = getMaxSegmentValue(version);
		int mask = fullMask & (fullMask << (getBitCount(version) - bits));
		return mask;
	}
	
	static int getSegmentCIDRHostMask(int segmentCIDRNetworkMask, IpVersion version) {
		return ~segmentCIDRNetworkMask & getMaxSegmentValue(version);
	}
	
	public boolean matches(int value) {
		return !isMultiple() && value == this.value;
	}
	
	public boolean matchesWithMask(int value, int mask) {
		return !isMultiple() && value == (this.value & mask);
	}
	
	public boolean rangeIsWithinRange(int lower, int upper) {
		return value >= lower && upperValue <= upper;
	}
	
	public boolean valueIsWithinRange(int lower, int upper) {
		return value >= lower && value <= upper;
	}
	
	public boolean isZero() {
		return !isMultiple() && value == 0;
	}
	
	int highByte() {
		return highByte(value);
	}
	
	int lowByte() {
		return lowByte(value);
	}
	
	static int highByte(int value) {
		return value >> 8;
	}
	
	static int lowByte(int value) {
		return value & 0xff;
	}
	
	boolean isRange() {
		return isRange;
	}
	
	int getLowerValue() {
		return value;
	}
	
	int getUpperValue() {
		return upperValue;
	}
	
	String getCharPrefix() {
		if(isRange) {
			return "";
		}
		int width = getCharWidth();
		int expansion = Math.max(0, getPrintableCharsPerSegment() - width);
		if(expansion > 0) {
			return getCharPrefix(expansion);
		}
		return "";
	}
	
	String getCharPrefix(int digits) {
		if(isRange) {
			return "";
		}
		if(digits >= zeroes.length) {
			throw new IllegalArgumentException("increase size of zeroes static field if you need bigger strings");
		}
		return zeroes[digits];
	}
	
	int getMaxCharPrefixLength() {
		if(isRange) {
			return 0;
		}
		int width = getCharWidth();
		return Math.max(0,  getPrintableCharsPerSegment() - width);
	}
	
	boolean isCharPrefixable() {
		return getMaxCharPrefixLength() > 0;
	}

	@Override
	public int compareTo(IPAddressSegment other) {
		if(isMultiple()) {
			int result = value - other.value;
			if(result == 0) {
				result = upperValue - other.upperValue;
				if(result == 0) {
					if(segmentPrefixBits == null ? other.segmentPrefixBits != null : other.segmentPrefixBits == null) {
						result = segmentPrefixBits == null ? -1 : 1;
					}
				}
			}
			return result;
		}
		return value - other.value;
	}
	
	boolean isSameValues(IPAddressSegment otherSegment) {
		return value == otherSegment.value 
				&& upperValue == otherSegment.upperValue
				&& ((segmentPrefixBits != null) ? segmentPrefixBits.equals(otherSegment.segmentPrefixBits) : (otherSegment.segmentPrefixBits == null));
	}
	
	@Override
	public abstract boolean equals(Object other);
	
	public int getCharWidth() {
		if(value == 0) {
			return 1;
		}
		int radix = getPrintableRadix();
		int digits = 0;
		int val = value;
		while(val > 0) {
			val = val / radix;
			digits++;
		}
		return digits;
	}
	
	class CharNormalizer {
		private StringBuilder builder;
		private int currentChar;
		private int nextCharIndex;
		private int currentCharIndex;
		private final String str;
		private boolean skip;
		
		CharNormalizer(String s) {
			this.str = s;
		}
		
		void lowercase() {
			currentChar = Character.toLowerCase(currentChar);
		}
		
		int getCurrentChar() {
			return currentChar;
		}
		
		boolean normalize() {
			for(int i=0; i < str.length(); i = nextCharIndex) {
				int c = str.charAt(i);
				currentCharIndex = i;
				nextCharIndex = i + 1;
				currentChar = c;
				if(!IPAddressSegment.this.checkChar(this)) {
					return false;
				} 
				if(skip) {
					skip = false;
				} else {
					boolean hasChanged = (currentChar != c || nextCharIndex != i + 1);
					if(hasChanged) {
						initializeBuilder(i);
					}
					if(builder != null) {
						builder.append((char) currentChar);
					}
				}
			}
			return true;
		}
		
		void eliminateLeadingZeros() {
			int index = currentCharIndex;
			if(index == 0 && index < str.length() - 1) {
				do {
					currentChar = str.charAt(nextCharIndex++);
				} while(nextCharIndex < str.length() && currentChar == '0');
				nextCharIndex--;
				initializeBuilder(index);
				skip = true;
			}
		}
		
		void normalizeWildcards() {
			while(nextCharIndex < str.length()) {
				int c2 = str.charAt(nextCharIndex);
				if(c2 == IPAddress.EQUIVALENT_SEGMENT_WILDCARD) { //skip it
					nextCharIndex++;
				} else if(c2 == IPAddress.SEGMENT_WILDCARD) { //skip it and use '*' as current instead 
					currentChar = c2;
					nextCharIndex++;
				} else {
					return;
				}
			}
		}
		
		void initializeBuilder(int index) {
			if(builder == null) {
				builder = new StringBuilder();
				builder.append(str.substring(0, index));
			}
		}
		
		@Override
		public String toString() {
			return builder == null ? str : builder.toString();
		}
	}
	
	/**
	 * If the segment looks OK as is, returns the segment.
	 * If it needs changing, returns the new segment.
	 * If it is invalid for some reason, such as being zero-length, null, or containing an invalid character, it returns null.
	 * Subclasses can override this to allow special ip addresses.
	 * 
	 * @param s
	 * @return
	 */
	private String normalizeSegment(String s) {
		if(s == null || s.length() == 0) {
			return null;
		}
		CharNormalizer charNormalizer = new CharNormalizer(s);
		if(!charNormalizer.normalize()) {
			return null;
		}
		return charNormalizer.toString();
	}

	boolean checkChar(CharNormalizer charNormalizer) {
		switch(charNormalizer.getCurrentChar()) {
			case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				break;
			case '0':
				charNormalizer.eliminateLeadingZeros();
				break;
			case IPAddress.EQUIVALENT_SEGMENT_WILDCARD:
			case IPAddress.SEGMENT_WILDCARD:
				charNormalizer.normalizeWildcards();
				break;
			case IPAddress.RANGE_SEPARATOR:
				break;
			default:
				return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return getString();
	}
	
	/**
	 * Produces a string by normalizing the string used to construct the segment.
	 * If that string was null, then uses the string from getStandardString().
	 * @return
	 */
	public String getString() {
		if(cachedString == null) {
			synchronized(this) {
				if(cachedString == null) {
					cachedString = normalizeSegment(originalString);
					if(cachedString == null) {
						cachedString = getStandardString();
					}
				}
			}
		}
		return cachedString;
	}
	
	/**
	 * Produces a string to represent the segment.
	 * If the segment CIDR prefix length covers the range, then it is assumed to be a CIDR, and the string has only the lower value of the CIDR range.
	 * Otherwise, the explicit range will be printed.
	 * @return
	 */
	public String getStandardString() {
		if(cachedStandardString == null) {
			synchronized(this) {
				if(cachedStandardString == null) {
					if((!isPrefixedSegment()) ? isMultiple() : !rangeMatchesPrefixLength(value, upperValue, segmentPrefixBits)) {
						cachedStandardString = getRangeString();
					} else {
						cachedStandardString = Integer.toString(value, getPrintableRadix());
					}
				}
			}
		}
		return cachedStandardString;
	}

	/**
	 * Produces a string to represent the segment.
	 * The segment CIDR prefix is ignored and the explicit range is printed.
	 * @return
	 */
	public String getWildcardString() {
		if(cachedWildcardString == null) {
			synchronized(this) {
				if(cachedWildcardString == null) {
					if(!isMultiple()) {
						cachedWildcardString = getString();
					} else {
						cachedWildcardString = getRangeString();
					}
				}
			}
		}
		return cachedWildcardString;
	}

	private String getRangeString() {
		if(value == 0 && upperValue == getMaxSegmentValue()) {
			return IPAddress.SEGMENT_WILDCARD_STR;
		}
		return getRangeString(value, upperValue);
	}
	
	private String getRangeString(int lower, int upper) {
		return Integer.toString(lower, getPrintableRadix()) + IPAddress.RANGE_SEPARATOR + Integer.toString(upper, getPrintableRadix());
	}
}