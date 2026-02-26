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

public class IPV4AddressSection extends IPAddressSection {
	static final IPV4AddressSegment emptySegments[] = new IPV4AddressSegment[0];
	
	//a set of pre-defined string types
	private static final AddressStringNormalizationParams fullParams = new AddressStringNormalizationParams(true, false);
	private static final AddressStringNormalizationParams canonicalParams = new AddressStringNormalizationParams(false, false);
	private static final AddressStringNormalizationParams canonicalWildcardParams = new AddressStringNormalizationParams(false, true);
		
	IPV4AddressSection(IPV4AddressSegment segments[]) {
		super(segments);
	}
	
	@Override
	public Iterator<IPV4AddressSection> sectionIterator() {
		return new SectionIterator<IPV4AddressSection>() {
		    @Override
			public IPV4AddressSection next() {
		    	if(!hasNext()) {
		    		throw new NoSuchElementException();
		    	}
		    	return new IPV4AddressSection((IPV4AddressSegment[]) iterator.next());
		    }
		};
	}
	
	@Override
	public Iterator<IPV4AddressSegment[]> iterator() {
		return iterator(IPV4Address.network.creator);
	}
	
	@Override
	public IPV4AddressSegment[] getSegments() {
		return (IPV4AddressSegment[]) segments;
	}
	
	@Override
	public int getBitsPerSegment() {
		return IPV4Address.BITS_PER_SEGMENT;
	}
	
	@Override
	public int getBytesPerSegment() {
		return IPV4Address.BYTES_PER_SEGMENT;
	}
	
	@Override
	public char getSegmentSeparator() {
		return IPV4Address.SEGMENT_SEPARATOR;
	}
	
	@Override
	public boolean isIpv4() {
		return true;
	}
	
	@Override
	public IpVersion getIpVersion() {
		return IpVersion.IPV4;
	}
	
	@Override
	public IPV4AddressSection toIpv4() {
		return this;
	}
	
	@Override
	public IPV4AddressSection getNetworkSection(int cidrBits) {
		IPV4AddressSegment segs[] = getNetworkSegments(cidrBits, IPV4Address.network.creator);
		return new IPV4AddressSection(segs);
	}
	
	@Override
	public IPV4AddressSection getHostSection(int cidrBits) {
		IPV4AddressSegment segs[] = getHostSegments(cidrBits, IPV4Address.network.creator);
		return new IPV4AddressSection(segs);
	}
		
	/**
	 * This produces a canonical string.
	 * 
	 * Each address has a unique canonical string.
	 */
	@Override
	public String toCanonicalStringImpl() {
		return toNormalizedString(canonicalParams);
	}

	/**
	 * This produces a string with no compressed segments and all segments of full length,
	 * which is 3 characters for IPV4 segments.
	 * 
	 * Each address has a unique compressed string.
	 */
	@Override
	public String toFullStringImpl() {
		return toNormalizedString(fullParams);
	}
	
	/**
	 * The shortest string for IPV4 addresses is the same as the canonical string.
	 */
	@Override
	public String toCompressedStringImpl() {
		return toCanonicalString();
	}
	
	/**
	 * The normalized string returned by this method is consistent with java.net.Inet4Address,
	 * and is the same as the canonical string.
	 */
	@Override
	public String toNormalizedStringImpl() {
		return toCanonicalString();
	}
	
	@Override
	public String toCompressedWildcardStringImpl() {
		return toCanonicalWildcardString();
	}
	
	@Override
	public String toCanonicalWildcardStringImpl() {
		return toNormalizedString(canonicalWildcardParams);
	}
	
	@Override
	public String toNormalizedWildcardStringImpl() {
		return toCanonicalWildcardString();
	}
	
	public String toNormalizedString(AddressStringNormalizationParams params) {
		return params.toString(this);
	}
	
	@Override
	public String[] toStrings(IPStringBuilderOptions options) {
		IPV4StringBuilder builder = new IPV4StringBuilder(this, options);
		return builder.getStrings();
	}
	
	public static class AddressStringNormalizationParams {
		boolean expandSegments;
		boolean makeWildcards;
		
		AddressStringNormalizationParams(boolean expandSegments, boolean makeWildcards) {
			this.expandSegments = expandSegments;
			this.makeWildcards = makeWildcards;
			if(expandSegments && makeWildcards) {
				//at this time we don't support both expandSegments and makeWildcards at the same time.  New ranges like "a-b" are not expanded.
				throw new IllegalArgumentException();
			}
		}
		
		private IPV4StringParams from(IPV4AddressSection addr) {
			IPV4StringParams result = new IPV4StringParams(expandSegments);
			result.makeWildcards = makeWildcards;
			return result;
		}
		
		public String toString(IPV4AddressSection addr){
			return from(addr).toString(addr);
		}
	}
}
