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


public class IPV6AddressSection extends IPAddressSection {
	static final IPV6AddressSegment emptySegments[] = new IPV6AddressSegment[0];
	
	//a set of pre-defined string types
	private static final AddressStringNormalizationParams mixedParams = new AddressStringNormalizationParams(true, true, true, true, false, false);
	private static final AddressStringNormalizationParams fullParams = new AddressStringNormalizationParams(false, false, false, false, true, false);
	
	private static final AddressStringNormalizationParams normalizedParams = new AddressStringNormalizationParams(false, false, false, false, false, false);
	private static final AddressStringNormalizationParams canonicalParams = new AddressStringNormalizationParams(false, true, false, true, false, false);
	private static final AddressStringNormalizationParams compressedParams = new AddressStringNormalizationParams(false, true, true, true, false, false);
	
	private static final AddressStringNormalizationParams wildcardNormalizedParams = new AddressStringNormalizationParams(false, false, false, false, false, true);
	private static final AddressStringNormalizationParams wildcardCanonicalParams = new AddressStringNormalizationParams(false, true, false, true, false, true);
	private static final AddressStringNormalizationParams wildcardCompressedParams = new AddressStringNormalizationParams(false, true, true, true, false, true);
	
	private final IPV4AddressSection mixedSection;
		
	IPV6AddressSection(IPV6AddressSegment segments[]) {
		this(segments, IPV4AddressSection.emptySegments);
	}
	
	IPV6AddressSection(IPV6AddressSegment segments[], IPV4AddressSegment mixedSegments[]) {
		super(segments);
		if(mixedSegments != null && mixedSegments.length > 0) {
			this.mixedSection = new IPV4AddressSection(mixedSegments);
		} else {
			this.mixedSection = null;
		}
	}
	
	@Override
	public Iterator<IPV6AddressSection> sectionIterator() {
		return new SectionIterator<IPV6AddressSection>() {
			@Override
			public IPV6AddressSection next() {
		    	if(!hasNext()) {
		    		throw new NoSuchElementException();
		    	}
		    	return new IPV6AddressSection((IPV6AddressSegment[]) iterator.next());
		    }
		};
	}
	
	@Override
	public Iterator<IPV6AddressSegment[]> iterator() {
		return iterator(IPV6Address.network.creator);
	}
	
	@Override
	public IPV6AddressSegment[] getSegments() {
		return (IPV6AddressSegment[]) segments;
	}
	
	@Override
	public int getBitsPerSegment() {
		return IPV6Address.BITS_PER_SEGMENT;
	}
	
	@Override
	public int getBytesPerSegment() {
		return IPV6Address.BYTES_PER_SEGMENT;
	}
	
	@Override
	public char getSegmentSeparator() {
		return IPV6Address.SEGMENT_SEPARATOR;
	}
	
	@Override
	public boolean hasAlphabeticDigits() {
		return hasAlphabeticDigits(false);
	}
	
	public boolean hasAlphabeticDigits(boolean mixed) {
		IPV6AddressSegment segments[] = getSegments();
		int mixedCount = mixed ? getMixedIPV6SegmentCount() : 0;
		for(int i=0; i<segments.length - mixedCount; i++) {
			IPV6AddressSegment seg = segments[i];
			if(seg.hasAlphabeticDigits()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isIpv6() {
		return true;
	}
	
	@Override
	public IpVersion getIpVersion() {
		return IpVersion.IPV6;
	}
	
	@Override
	public IPV6AddressSection toIpv6() {
		return this;
	}
	
	public IPV4AddressSegment[] getMixedIPV4Segments() {
		return mixedSection != null ? mixedSection.getSegments() : IPV4AddressSection.emptySegments;
	}
		
	/**
	 * returns how many of the original segments are covered by the ipv4 segments
	 * @return
	 */
	public int getMixedIPV6SegmentCount() {
		if(mixedSection == null) {
			return 0;
		}
		int ipv4Segs = getMixedIPV4SegmentCount();
		int bytes = ipv4Segs * IPV4Address.BYTES_PER_SEGMENT;
		int roundUp = IPV6Address.BYTES_PER_SEGMENT - 1;
		int ipv6Segs = (bytes + roundUp) / IPV6Address.BYTES_PER_SEGMENT;
		return ipv6Segs;
	}
		
	public int getMixedIPV4SegmentCount() {
		return mixedSection == null ? 0 : mixedSection.getSegmentCount();
	}
	
	/**
	 * 
	 * @return whether this section appears the same as its own mixed segment
	 * 	This is true if it is just a single IPV4 segment (so the mixed part has no '.' separator)
	 *  and the value lies between 0 and 9 (so the hex and decimal values are the same)
	 */
	public boolean isSameAsMixed(boolean checkRange) {
		if(getMixedIPV4SegmentCount() == 1) {
			IPV6AddressSegment seg = getSegments()[0];
			return checkRange ? seg.rangeIsWithinRange(0, 9) : seg.valueIsWithinRange(0, 9);
		}
		return false;	
	}
	
	
	@Override
	public IPV6AddressSection getNetworkSection(int cidrBits) {
		IPV6AddressSegment segs[] = getNetworkSegments(cidrBits, IPV6Address.network.creator);
		int mixedCount = getMixedIPV6SegmentCount();
		if(mixedCount > 0) {
			int firstMixedSegment = segments.length - mixedCount;
			int firstMixedBits = firstMixedSegment * IPV6Address.BITS_PER_SEGMENT;
			int mixedCidrBits = cidrBits - firstMixedBits;
			if(mixedCidrBits > 0) {
				return new IPV6AddressSection(segs, mixedSection.getNetworkSegments(mixedCidrBits, IPV4Address.network.creator));
			}
		}
		return new IPV6AddressSection(segs);
	}
	
	@Override
	public IPV6AddressSection getHostSection(int cidrBits) {
		IPV6AddressSegment segs[] = getHostSegments(cidrBits, IPV6Address.network.creator);
		int mixedCount = getMixedIPV6SegmentCount();
		if(mixedCount > 0) {
			int firstMixedSegment = segments.length - mixedCount;
			int firstMixedBits = firstMixedSegment * IPV6Address.BITS_PER_SEGMENT;
			int mixedCidrBits = cidrBits - firstMixedBits;
			return new IPV6AddressSection(segs, mixedSection.getHostSegments(mixedCidrBits, IPV4Address.network.creator));
		}
		return new IPV6AddressSection(segs);
	}
	
	
	/**
	 * This produces the shortest valid string for the address.
	 * 
	 * Each address has a unique compressed string.
	 */
	@Override
	public String toCompressedStringImpl() {
		return toNormalizedString(compressedParams);
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
	@Override
	public String toCanonicalStringImpl() {
		return toNormalizedString(canonicalParams);
	}
	
	/**
	 * This produces mixed IPV6/IPV4 string.  It is the shortest such string.
	 * 
	 * This string is unique for each address.
	 */
	public String toMixedString() {
		return toNormalizedString(mixedParams);
	}

	/**
	 * This produces a string with no compressed segments and all segments of full length,
	 * which is 4 characters for IPV6 segments and 3 characters for IPV4 segments.
	 * 
	  * Each address has a unique full string.
	 */
	@Override
	public String toFullStringImpl() {
		return toNormalizedString(fullParams);
	}
	
	@Override
	public String toCompressedWildcardStringImpl() {
		return toNormalizedString(wildcardCompressedParams);
	}
	
	@Override
	public String toCanonicalWildcardStringImpl() {
		return toNormalizedString(wildcardCanonicalParams);
	}
	
	@Override
	public String toNormalizedWildcardStringImpl() {
		return toNormalizedString(wildcardNormalizedParams);
	} 
	
	/**
	 * The normalized string returned by this method is consistent with java.net.Inet6address.
	 * IPs are not compressed nor mixed in this representation.
	 * 
	 * The string returned by this method is unique for each address.
	 */
	@Override
	public String toNormalizedStringImpl() {
		return toNormalizedString(normalizedParams);
	}
	
	public String toNormalizedString(boolean makeMixed, boolean compress, boolean compressSingle) {
		return toNormalizedString(makeMixed, compress, compressSingle, true, false, false);
	}
	
	/**
	 * 
	 * @param makeMixed whether to write in mixed format
	 * @param compress whether to compress the largest zero-segment in IPV6 addresses
	 * @param compressSingle whether to compress a single-zero-segment in IPV6 addresses (ignored if compress is false)
	 * @param compressCIDR if false, then segments outside the CIDR prefix are not compressed
	 * @param expandSegments whether to expand segments with leading zeros to make each segment the same length of 4 characters
	 * @return the string representing the address
	 */
	public String toNormalizedString(boolean makeMixed, boolean compress, boolean compressSingle, boolean compressCIDR, boolean expandSegments, boolean makeWildcards) {
		return toNormalizedString(new AddressStringNormalizationParams(makeMixed, compress, compressSingle, compressCIDR, expandSegments, makeWildcards));
	}
	
	public String toNormalizedString(AddressStringNormalizationParams params) {
		return params.toString(this);
	}
	
	public String[] toStrings(IPV6StringBuilderOptions options) {
		return toStrings((IPStringBuilderOptions) options);
	}
	
	@Override
	public String[] toStrings(IPStringBuilderOptions options) {
		IPV6StringBuilder builder = new IPV6StringBuilder(this, options);
		return builder.getStrings();
	}
	
	public static class AddressStringNormalizationParams {
		boolean makeMixed;
		boolean keepMixed;
		boolean compress;
		boolean compressSingle;
		boolean expandSegments;
		boolean makeWildcards;
		boolean compressWithCIDR;
		
		AddressStringNormalizationParams(boolean makeMixed, boolean compress, boolean compressSingle) {
			this(makeMixed, compress, compressSingle, true, false, false);
		}
		
		/**
		 * @param makeMixed whether to write in mixed format
		 * @param compress whether to compress the largest zero-segment in IPV6 addresses
		 * @param compressSingle whether to compress a single-zero-segment in IPV6 addresses (ignored if compress is false)
		 * @param compressCIDR if false, then segments outside the CIDR prefix are not compressed
		 * @param expandSegments whether to expand segments with leading zeros to make each segment the same length of 4 characters
		 * @param makeWildcards whether to use the wildcards '*' or '-' to denote ranges
		 * @return the string representing the address
		 */
		AddressStringNormalizationParams(boolean makeMixed, boolean compress, boolean compressSingle, boolean compressWithCIDR, boolean expandSegments, boolean makeWildcards) {
			this.makeMixed = makeMixed;
			this.compress = compress;
			this.compressSingle = compressSingle;
			this.expandSegments = expandSegments;
			this.makeWildcards = makeWildcards;
			this.compressWithCIDR = compressWithCIDR;
		}
		
		private IPV6StringParams from(IPV6AddressSection addr) {
			IPV6StringParams result = new IPV6StringParams();
			boolean useMixed = makeMixed;
			if(compress) {
				int indexes[] = addr.getCompressIndexAndCount(compressWithCIDR, useMixed);
				int maxIndex = indexes[0];
				int maxCount = indexes[1];
				if(maxIndex >= 0 && (compressSingle || maxCount > 1)) {
					result.firstCompressedSegmentIndex = maxIndex;
					result.nextUncompressedIndex = maxIndex + maxCount;
					result.wildcardsCompressed = compressWithCIDR && 
							(result.nextUncompressedIndex > IPAddress.getSegmentIndex(addr.getNetworkPrefixBits(), 
									IPV6Address.BYTE_COUNT, IPV6Address.BYTES_PER_SEGMENT));
				}
			}
			result.createMixed = useMixed;
			result.makeWildcards = makeWildcards;
			return result;
		}
		
		public String toString(IPV6AddressSection addr) {
			return from(addr).toString(addr);
		}
	}
	
	int[] getCompressIndexAndCount(boolean compressWithCIDR, boolean mixed) {
		int[][] compressibleSegs = compressWithCIDR ? getZeroRangeSegments() : getZeroSegments();
		int maxIndex = -1, maxCount = 0;
		for(int seg[] : compressibleSegs) {
			int index = seg[0];
			int count = seg[1];
			if(mixed) {
				int mixedIndex = IPV6Address.getMixedOriginalSegments();
				count = Math.min(count, mixedIndex - index);
			}
			if(count > maxCount) {
				maxIndex = index;
				maxCount = count;
			}
		}
		return new int[] {maxIndex, maxCount};
	}
}
