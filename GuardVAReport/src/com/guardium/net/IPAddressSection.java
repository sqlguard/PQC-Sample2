/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.guardium.net.IPAddress.IpVersion;
import com.guardium.net.IPAddressSegment.IPAddressSegmentCreator;

/**
 * Represents part of an IPAddress.
 * 
 * IPAddressSection objects are immutable, which also makes them thread-safe.
 */
public abstract class IPAddressSection {
	/* the segments.  For IPV4, each element is actually just 1 byte and the array has 4 elements, while for IPV6, each element is 2 bytes and the array has 8 elements. */
	final IPAddressSegment segments[];
	
	/* various string representations - these fields are for caching */
	private String canonicalString;
	private String normalizedString;
	private String compressedString;
	private String fullString;
	private String compressedWildcardString;									
	private String canonicalWildcardString;
	private String normalizedWildcardString;
	
	/* also for caching */
	private boolean checkedCIDRNetworkMaskPrefixLen;
	private Integer cidrNetworkMaskPrefixLen;
	private boolean checkedCIDRHostMaskPrefixLen;
	private Integer cidrHostMaskPrefixLen;
	
	/* index of segments that are zero, and the number of consecutive zeros for each. */
	private int[][] zeroSegments;
	
	/* index of segments that are zero or any value due to CIDR prefix, and the number of consecutive segments for each. */
	private int[][] zeroRanges;
	
	IPAddressSection(IPAddressSegment segments[]) {
		this.segments = segments;
	}
	
	public int getSegmentCount() {
		return segments.length;
	}
	
	public abstract int getBitsPerSegment();
	
	public static int bitsPerSegment(IpVersion version) {
		return IPAddressSegment.getBitCount(version);
	}
	
	public abstract int getBytesPerSegment();
	
	public static int bytesPerSegment(IpVersion version) {
		return IPAddressSegment.getBitCount(version);
	}
	
	public boolean isIpv4() {
		return false;
	}
	
	public boolean isIpv6() {
		return false;
	}
	
	public abstract IpVersion getIpVersion();
	
	/**
	 * If this section is IPV4, return this object cast to IPV4AddressSection.  Otherwise, returns null.
	 * @return
	 */
	public IPV4AddressSection toIpv4() {
		return null;
	}
	
	/**
	 * If this section is IPV6, return this object cast to IPV6AddressSection.  Otherwise, returns null.
	 * @return
	 */
	public IPV6AddressSection toIpv6() {
		return null;
	}
	
	public boolean hasAlphabeticDigits() {
		return false;
	}
	
	public abstract char getSegmentSeparator();
	
	public abstract IPAddressSection getNetworkSection(int cidrBits);
	
	public abstract IPAddressSection getHostSection(int cidrBits);
	
	//all unchecked casts in the IP* classes call this method so unchecked casts always happens in one place
	//I've ensured that users of the IP* classes cannot generate an unsafe unchecked cast, 
	//so all callers to this method perform valid casts
	@SuppressWarnings("unchecked")
    static <T> T cast(Object obj) {
		//casting to a generic type cannot be checked at runtime,
		//so the ClassCastException will happen somewhere else
        return (T) obj;
    }

	private Integer checkForPrefixMask(int front, int back) {
		int prefixLen = 0;
		for(int i=0; i<segments.length; i++) {
			IPAddressSegment seg = segments[i];
			int value = seg.getLowerValue();
			int bits = seg.getBitCount();
			if(value != front) {
				int frontBit = front & 0x1;
				int backBit = back & 0x1;
				for(int k = 1; k <= bits; k++) {
					int bit = (value >> (bits - k)) & 0x1;
					if(bit != frontBit) {
						for(k++; k <= bits; k++) {
							bit = (value >> (bits - k)) & 0x1;
							if(bit != backBit) {
								return null;
							}
						}
					} else {
						prefixLen++;
					}
				}
				for(i++; i<segments.length; i++) {
					value = segments[i].getLowerValue();
					if(value != back) {
						return null;
					}
				}
			} else {
				prefixLen += bits;
			}
		}
		//note that when segments.length == 0, we return 0 as well, since both the host mask and prefix mask are empty (length of 0 bits)
		return prefixLen;
	}
	
	/**
	 * If this address section is equivalent to the mask for a CIDR prefix, it returns that prefix length.
	 * Otherwise, it returns null.
	 * A CIDR network mask is an address with all 1s in the network section and then all 0s in the host section.
	 * A CIDR host mask is an address with all 0s in the network section and then all 1s in the host section.
	 * The prefix length is the length of the network section.
	 * 
	 * Also, keep in mind that the prefix length returned by this method is not equivalent to the prefix length used to construct this object.
	 * The prefix length used to construct indicates the network and host portion of this address.  
	 * The prefix length returned here indicates the whether the value of this address can be used as a mask for the network and host
	 * portion of any other address.  Therefore the two values can be different values, or one can be null while the other is not.
	 * 
	 * @param network whether to check for a network mask or a host mask
	 * @return the prefix length corresponding to this mask, or null if this address is not a CIDR prefix mask
	 */
	public Integer getCIDRMaskPrefixLength(boolean network) {
		if(network) {
			if(checkedCIDRNetworkMaskPrefixLen) {
				return cidrNetworkMaskPrefixLen;
			}
			Integer prefixLen = checkForPrefixMask(segments[0].getMaxSegmentValue(), 0);
			return setNetworkMaskPrefix(prefixLen);
		} else {
			if(checkedCIDRHostMaskPrefixLen) {
				return cidrHostMaskPrefixLen;
			}
			Integer prefixLen = checkForPrefixMask(0, segments[0].getMaxSegmentValue());
			return setHostMaskPrefix(prefixLen);
		}
	}
	
	Integer setMaskPrefix(Integer prefixLen, boolean network) {
		if(network) {
			return setNetworkMaskPrefix(prefixLen);
		} else {
			return setHostMaskPrefix(prefixLen);
		}
	}
	
	Integer setHostMaskPrefix(Integer prefixLen) {
		checkedCIDRHostMaskPrefixLen = true;
		cidrHostMaskPrefixLen = prefixLen;
		if(prefixLen != null) {//cannot be both network and host mask
			checkedCIDRNetworkMaskPrefixLen = true; 
			cidrNetworkMaskPrefixLen = null;
		}
		return prefixLen;
	}
	
	Integer setNetworkMaskPrefix(Integer prefixLen) {
		checkedCIDRNetworkMaskPrefixLen = true;
		cidrNetworkMaskPrefixLen = prefixLen;
		if(prefixLen != null) {//cannot be both network and host mask
			checkedCIDRHostMaskPrefixLen = true; 
			cidrHostMaskPrefixLen = null;
		}
		return prefixLen;
	}
	
	<T extends IPAddressSegment> T[] getNetworkSegments(int cidrBits, IPAddressSegmentCreator<T> creator) {
		T segments[] = cast(this.segments);
		int segmentCount = segments.length;
		if(segmentCount == 0) {
			return segments;
		}
		IpVersion version = getIpVersion();
		int totalBits;
		if(segmentCount < IPAddress.segmentCount(version)) {
			totalBits = segmentCount * IPAddress.bitsPerSegment(version);
		} else {
			totalBits = IPAddress.bitCount(version);
		}
		if(cidrBits >= totalBits) {
			return segments;
		} else if(cidrBits <= 0) {
			return creator.createAddressSegmentArray(0);
		}
		int bitsPerSegment = segments[0].getBitCount();
		int segCount = (cidrBits + bitsPerSegment - 1) / bitsPerSegment;
		T result[] = creator.createAddressSegmentArray(segCount);
		int i = 0;
		for(; i < segCount; i++) {
			Integer prefixBits = getSegmentPrefixBits(bitsPerSegment(version), cidrBits, i, segCount);
			result[i] = cast(segments[i].toNetworkSegment(prefixBits));
		}
		return result;
	}
	
	<T extends IPAddressSegment> T[] getHostSegments(int cidrBits, IPAddressSegmentCreator<T> creator) {
		T segments[] = cast(this.segments);
		int segmentCount = segments.length;
		if(segmentCount == 0) {
			return segments;
		}
		IpVersion version = getIpVersion();
		int totalBits;
		if(segmentCount < IPAddress.segmentCount(version)) {
			totalBits = segmentCount * IPAddress.bitsPerSegment(version);
		} else {
			totalBits = IPAddress.bitCount(version);
		}
		int hostBits = totalBits - cidrBits;
		if(cidrBits <= 0) {
			return segments;
		} else if(hostBits <= 0) {
			return creator.createAddressSegmentArray(0);
		}
		int bitsPerSegment = segments[0].getBitCount();
		int segCount = (hostBits + bitsPerSegment - 1) / bitsPerSegment;
		T result[] = creator.createAddressSegmentArray(segCount);
		for(int i = segCount - 1, j = segments.length - 1; i >= 0; i--, j--) {
			result[i] = cast(segments[j].toHostSegment(getSegmentPrefixBits(bitsPerSegment(version), cidrBits, j, segCount)));
		}
		return result;
	}
	
	static <T extends IPAddressSegment> T[] getSubnetSegments(IpVersion version, T originalSegments[], T maskSegments[], Integer cidrPrefixBits, IPAddressSegmentCreator<T> creator)  throws IPAddressTypeException {
		boolean different = false;
		for(int i=0; i < originalSegments.length; i++) {
			Integer segmentPrefixBits = getSegmentPrefixBits(bitsPerSegment(version), cidrPrefixBits, i, originalSegments.length);
			T seg = originalSegments[i];
			T mask = maskSegments[i];
			if(seg.isChangedByMask(mask, segmentPrefixBits)) {
				different = true;
				break;
			}
		}
		if(!different) {
			return originalSegments;
		}
		T newSegments[] = creator.createAddressSegmentArray(originalSegments.length);
		for(int i = 0; i < originalSegments.length; i++) {
			Integer segmentPrefixBits = getSegmentPrefixBits(bitsPerSegment(version), cidrPrefixBits, i, originalSegments.length);
			T seg = originalSegments[i];
			T mask = maskSegments[i];
			newSegments[i] = cast(seg.toMaskedSegment(mask, segmentPrefixBits));
		}
		return newSegments;
	}
	
	//call this instead of the method below if you already know the networkPrefixBits doesn't extend to the end of the last segment of the section or address
	static Integer getSplitSegmentPrefixBits(int bitsPerSegment, Integer networkPrefixBits, int segmentIndex) {
		if(networkPrefixBits != null) {
			int segmentPrefixBits = networkPrefixBits - (segmentIndex * bitsPerSegment); 
			return getSegmentPrefixBits(bitsPerSegment, segmentPrefixBits);
		}
		return null;
	}
		
	static Integer getSegmentPrefixBits(int bitsPerSegment, Integer networkPrefixBits, int segmentIndex, int segmentCount) {
		if(networkPrefixBits != null) {
			int segmentPrefixBits = networkPrefixBits - (segmentIndex * bitsPerSegment);
			if(segmentIndex < segmentCount - 1 || segmentPrefixBits < bitsPerSegment) { //not the last segment or the prefix does not extend to the end of last segment
				return getSegmentPrefixBits(bitsPerSegment, segmentPrefixBits);
			}
		}
		return null;
	}
	
	static Integer getSegmentPrefixBits(int bitsPerSegment, int segmentBits) {
		if(segmentBits <= 0) {
			return 0; //none of the bits in this segment matter
		} else if(segmentBits <= bitsPerSegment) {
			return segmentBits;//some of the bits in this segment matter
		}
		return null; //all the bits in this segment matter
	}
	
	/**
	 * @return whether this address represents a network prefix or the set of all addresses with the same network prefix
	 */
	public boolean isNetworkPrefix() {
		//across the address prefixes are none::0 to 128::0, see getSegmentPrefixBits
		//so it is enough to check just the last one
		return segments.length > 0 && segments[segments.length - 1].isPrefixedSegment();
	}
	
	public Integer getNetworkPrefixBits() {
		//across the address prefixes are none::0 to 128::0
		//so it is enought to check the last one
		if(!isNetworkPrefix()) {
			return null;
		}
		int result = 0;
		for(int i=0; i < segments.length; i++) {
			IPAddressSegment seg = segments[i];
			Integer prefix = seg.getSegmentPrefixBits();
			if(prefix != null) {
				result += prefix;
				if(prefix < seg.getBitCount()) {
					break; //the rest will be 0
				}
			} else {
				result += seg.getBitCount();
			}
		}
		return result;
	}
	
	public IPAddressSegment[] getSegments() {
		return segments;
	}
	
	public int[][] getZeroSegments() {
		if(zeroSegments == null) {
			zeroSegments = getZeroSegments(false);
		}
		return zeroSegments;
	}
	
	public int[][] getZeroRangeSegments() {
		if(zeroRanges == null) {
			if(!isNetworkPrefix()) {
				zeroRanges = getZeroSegments();
			} else {
				zeroRanges = getZeroSegments(true);
			}
		}
		return zeroRanges;
	}
	
	private int[][] getZeroSegments(boolean includeRanges) {
		ArrayList<int[]> segs = new ArrayList<int[]>(segments.length / 2 + 1);
		int currentIndex = -1, currentCount = 0;
		for(int i = 0; i < segments.length; i++) {
			boolean isCompressible = segments[i].isZero() || (includeRanges && segments[i].rangeMatches(0));
			if(isCompressible) {
				if(++currentCount == 1) {
					currentIndex = i;
				}
				if(i == segments.length - 1) {
					segs.add(new int[] {currentIndex, currentCount});
				}
			} else if(currentCount > 0) {
				segs.add(new int[] {currentIndex, currentCount});
				currentCount = 0;
			}
		}
		return segs.toArray(new int[segs.size()][]);
	}
	
	public boolean isZero() {
		boolean isZero = true;
		for(IPAddressSegment segment : segments)	{
			isZero = segment.isZero();
			if (!isZero) {
				break;
			}
		}
		return isZero;
	}
	
	public int compareTo(IPAddressSection other) {
		IPAddressSegment otherSegs[] = other.segments;
		if(segments.length != otherSegs.length) {
			return segments.length - otherSegs.length;
		}
		for(int i=0; i<segments.length; i++) {
			int result = segments[i].compareTo(otherSegs[i]);
			if(result != 0) {
				return result;
			}
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof IPAddressSection) {
			IPAddressSection other = (IPAddressSection) o;
			return compareTo(other) == 0;
		}
		return false;
	}
	
	/**
	 * @return whether this address represents more than one address.
	 * Such addresses include CIDR/IP addresses (eg 1.2.3.4/11) or wildcard addresses (eg 1.2.*.4) or range addresses (eg 1.2.3-4.5)
	 */
	public boolean isMultiple() {
		for(IPAddressSegment seg : segments) {
			if(seg.isMultiple()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * gets the count of addresses that this address may represent
	 * 
	 * If this address is not a CIDR and it has no range, then there is only one such address.
	 * 
	 * @return
	 */
	public BigInteger getCount() {
		BigInteger result = BigInteger.ONE;
		if(!isMultiple()) {
			return result;
		}
		for(int i=0; i<segments.length; i++) {
			int segCount = segments[i].getCount();
			result = result.multiply(BigInteger.valueOf(segCount));
		}
		return result;
	}
	
	public abstract Iterator<? extends IPAddressSection> sectionIterator();
	
	abstract class SectionIterator<T extends IPAddressSection> implements Iterator<T> {
		Iterator<? extends IPAddressSegment[]> iterator = iterator();
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
	    @Override
		public void remove() {
	    	throw new UnsupportedOperationException();
	    }
	};
	
	public abstract Iterator<? extends IPAddressSegment[]> iterator();
	
	<S extends IPAddressSegment> Iterator<S[]> iterator(final IPAddressSegmentCreator<S> segmentCreator) {
		if(!isMultiple()) {
			return new Iterator<S[]>() {
				boolean done;
				
				@Override
				public boolean hasNext() {
					return !done;
				}

			    @Override
				public S[] next() {
			    	if(!hasNext()) {
			    		throw new NoSuchElementException();
			    	}
			    	done = true;
			    	return cast(segments);
			    }

			    @Override
				public void remove() {
			    	throw new UnsupportedOperationException();
			    }
			};
		}

		return new Iterator<S[]>() {
			private boolean done;
			final int segmentCount = segments.length;
			
			private final Iterator<S> variations[] = cast(new Iterator[segmentCount]);
			
			private S nextSet[] = segmentCreator.createAddressSegmentArray(segmentCount);  {
				for(int i=0; i<segmentCount; i++) {
					variations[i] = cast(segments[i].iterator());
					nextSet[i] = variations[i].next();
				}
			}
			
			@Override
			public boolean hasNext() {
				return !done;
			}
			
		    @Override
			public S[] next() {
		    	if(done) {
		    		throw new NoSuchElementException();
		    	}
		    	S segs[] = nextSet.clone();
		    	increment();
		    	return segs;
		    }
		    
		    private void increment() {
		    	for(int j = segmentCount - 1; j >= 0; j--) {
		    		if(variations[j].hasNext()) {
		    			nextSet[j] = variations[j].next();
		    			for(int k = j + 1; k < segmentCount; k++) {
		    				variations[k] = cast(segments[k].iterator());
			    			nextSet[k] = variations[k].next();
		    			}
		    			return;
		    		}
		    	}
		    	done = true;
		    }

		    @Override
			public void remove() {
		    	throw new UnsupportedOperationException();
		    }
		};
	}
	
	////////////////string creation below ///////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String toString() {
		return toCanonicalString();
	}

	/**
	 * This produces a canonical string.
	 * 
	 * RFC 5952 describes canonical representations.
	 * http://en.wikipedia.org/wiki/IPv6_address#Recommended_representation_as_text
	 * http://tools.ietf.org/html/rfc5952
	 * 
	 * Each address has a unique canonical string.
	 */
	public String toCanonicalString() {
		if(canonicalString == null) {
			canonicalString = toCanonicalStringImpl();
		}
		return canonicalString;
	}
	
	abstract String toCanonicalStringImpl();

	/**
	 * This produces a string with no compressed segments and all segments of full length,
	 * which is 4 characters for IPV6 segments and 3 characters for IPV4 segments.
	 * 
	  * Each address has a unique full string.
	 */
	public String toFullString() {
		if(fullString == null) {
			fullString = toFullStringImpl();
		}
		return fullString;
	}
	
	abstract String toFullStringImpl();
	
	/**
	 * The normalized string returned by this method is consistent with java.net.Inet4Address and java.net.Inet6address.
	 * IPs are not compressed nor mixed in this representation.
	 * 
	 * The string returned by this method is unique for each address.
	 */
	public String toNormalizedString() {
		if(normalizedString == null) {
			normalizedString = toNormalizedStringImpl();
		}
		return normalizedString;
	}
	
	abstract String toNormalizedStringImpl();
	
	/**
	 * This produces the shortest valid string for the address.
	 * 
	 * Each address has a unique compressed string.
	 */
	public String toCompressedString() {
		if(compressedString == null) {
			compressedString = toCompressedStringImpl();
		}
		return compressedString;
	}
	
	abstract String toCompressedStringImpl();
	
	/**
	 * This produces a string similar to the canonical string,
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toCompressedWildcardString() {
		if(compressedWildcardString == null) {
			compressedWildcardString = toCompressedWildcardStringImpl();
		}
		return compressedWildcardString;
	}
	
	abstract String toCompressedWildcardStringImpl();
	
	/**
	 * This produces a string similar to the canonical string,
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toCanonicalWildcardString() {
		if(canonicalWildcardString == null) {
			canonicalWildcardString = toCanonicalWildcardStringImpl();
		}
		return canonicalWildcardString;
	}
	
	abstract String toCanonicalWildcardStringImpl();
	
	/**
	 * This produces a string similar to the normalized string, 
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toNormalizedWildcardString() {
		if(normalizedWildcardString == null) {
			normalizedWildcardString = toNormalizedWildcardStringImpl();
		}
		return normalizedWildcardString;
	}
	
	abstract String toNormalizedWildcardStringImpl();
	
	/**
	 * Returns just a few string representations:
	 * 
	 * -either compressed or not - when compressing it uses the canonical string representation or it compresses the leftmost zero-segment if the canonical representation has no compression.
	 * -either lower or uppercase
	 * -combinations thereof
	 * 
	 * So the maximum number of strings returned for IPV6 is 4, while for IPV4 it is 1.
	 * 
	 * @return
	 */
	public String[] toBasicStrings() {
		return toStrings(new IPV6StringBuilderOptions(
				IPV6StringBuilderOptions.UPPERCASE | 
				IPV6StringBuilderOptions.COMPRESSION_SINGLE));
	}
	
	/**
	 * Returns at most a couple dozen string representations:
	 * 
	 * -mixed (1:2:3:4:5:6:1.2.3.4)
	 * -full compressions (a:0:b:c:d:0:e:f or a::b:c:d:0:e:f or a:0:b:c:d::e:f) or no compression
	 * -full leading zeros (000a:0000:000b:000c:000d:0000:000e:000f)
	 * -combinations thereof
	 * 
	 * @return
	 */
	public String[] toStandardStrings() {
		return toStrings(new IPV6StringBuilderOptions(
				IPV6StringBuilderOptions.UPPERCASE |
				IPV6StringBuilderOptions.LEADING_ZEROS_FULL_ALL_SEGMENTS |
				IPV6StringBuilderOptions.COMPRESSION_ALL_FULL, 
			new IPStringBuilderOptions(IPV6StringBuilderOptions.LEADING_ZEROS_FULL_ALL_SEGMENTS)));
		
	}
	
	/**
	 * Use this method with care...  a single IPV6 address can have thousands of string representations.
	 * 
	 * Examples: 
	 * "::" has 1297 such variations, but only 9 are considered standard
	 * "a:b:c:0:d:e:f:1" has 1920 variations, but only 12 are standard
	 * 
	 * Variations included in this method:
	 * -all standard variations
	 * -adding a variable number of leading zeros (::a can be ::0a, ::00a, ::000a)
	 * -choosing any number of zero-segments to compress (:: can be 0:0:0::0:0)
	 * -mixed representation of all variations (1:2:3:4:5:6:1.2.3.4)
	 * -all combinations of such variations
	 * 
	 * Variations omitted from this method: mixed case of a-f, which you can easily handle yourself with String.equalsIgnoreCase
	 * 
	 * @return
	 */
	public String[] toAllStrings() {
		return toStrings(new IPV6StringBuilderOptions(
				IPV6StringBuilderOptions.UPPERCASE | 
				IPV6StringBuilderOptions.LEADING_ZEROS_FULL_SOME_SEGMENTS |
				IPV6StringBuilderOptions.COMPRESSION_ALL_FULL, 
			new IPStringBuilderOptions(IPV6StringBuilderOptions.LEADING_ZEROS_FULL_SOME_SEGMENTS)));
	}
	
	public abstract String[] toStrings(IPStringBuilderOptions options);
	
	public boolean isEntireAddress() {
		return getSegmentCount() == IPAddress.segmentCount(getIpVersion());
	}
	
	/**
	 * This method gives you an SQL clause that allows you to search the database for the front part of an address or 
	 * addresses in a given network.
	 * 
	 * This is not as simple as it sounds, because the same address can be written in different ways (especially for IPV6)
	 * and in addition, addresses in the same network can have different beginnings (eg 1.0.0.0/7 are all addresses from 0.0.0.0 to 1.255.255.255),
	 * so you can see they start with both 1 and 0.  You can reduce the number of possible beginnings by choosing a segment
	 * boundary as the network prefix.
	 * 
	 * @param builder
	 * @param columnName
	 */
	public void getStartsWithSQLClause(StringBuilder builder, String columnName) {
		if(isMultiple()) {
			Iterator<? extends IPAddressSection> sectionIterator = sectionIterator();
			while(sectionIterator.hasNext()) {
				IPAddressSection next = sectionIterator.next();
				next.getStartsWithSQLClause(builder, columnName);
			}
		} else if(getSegmentCount() > 0) { //there is something to match
			boolean isEntireAddress = isEntireAddress();
			String strs[] = toStandardStrings();
			for(String str : strs) {
				if(builder.length() > 0) {
					builder.append(" OR ");
				}
				if(isEntireAddress) {
					builder.append(columnName).append(" = '").append(str).append("'");
				} else {
					char separator = getSegmentSeparator();
					int separatorCount = count(str, separator);
					String searchStr;
					if(str.endsWith(String.valueOf(separator))) {
						//network ends with ":", which means it ends with "::" (e.g.1:2:3::), so we search for the same ending separator in the full address
						searchStr = str.substring(0, str.length() - 1);
					} else {
						//network ends with something than separator (eg 1.2.3), so we search for the next separator in the full address,
						//which should immediately follow the search string
						separatorCount++;
						searchStr = str;
					}
					builder.append("substring_index(").append(columnName).append(",'").append(separator).append("',").append(separatorCount).append(") = ").
						append('\'').append(searchStr).append('\'');
				}
			}
		}
	}
	
	private static int count(String str, char match) {
		int count = 0;
		for(int index = -1; (index = str.indexOf(match, index + 1)) >= 0; count++);
		return count;
	}
}
