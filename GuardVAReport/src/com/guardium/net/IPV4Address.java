/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.net.Inet4Address;
import java.util.Iterator;


/**
 * An IPV4 address
 * 
 * @author sfoley
 *
 */
public class IPV4Address extends IPAddress {
	
	public static final char SEGMENT_SEPARATOR = '.';
	public static final String SEGMENT_SEPARATOR_STR = String.valueOf(SEGMENT_SEPARATOR);
	public static final int BITS_PER_SEGMENT = 8;
	public static final int BYTES_PER_SEGMENT = 1;
	public static final int SEGMENT_COUNT = 4;
	public static final int BYTE_COUNT = 4;
	public static final int BIT_COUNT = 32;
	
	static IPV4AddressNetwork network = new IPV4AddressNetwork();
	
	/**
	 * Represents an IPV4 address or a set of addresses.
	 * @param segments the address segments
	 */
	public IPV4Address(IPV4AddressSegment[] segments) {
		this(segments, null);
	}
	
	/**
	 * Represents an IPV4 address or a set of addresses.
	 * When cidrPrefixBits is non-null, this object represents a network prefix or the set of addresses with the same network prefix (a network or subnet, in other words).
	 * @param segments the address segments
	 * @param cidrPrefixBits
	 */
	public IPV4Address(IPV4AddressSegment[] segments, Integer cidrPrefixBits) {
		super(new IPV4AddressSection(toCIDRSegments(cidrPrefixBits, segments, network.creator)));
		if(segments.length != SEGMENT_COUNT) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Represents an IPV4 address.
	 * 
	 * @param bytes must be a 4 byte IPV4 address
	 */
	public IPV4Address(byte[] bytes) {
		this(bytes, null);
	}
	
	/**
	 * Represents an IPV4 address or a set of addresses.
	 * When cidrPrefixBits is non-null, this object represents a network prefix or the set of addresses with the same network prefix (a network or subnet, in other words).
	 * 
	 * @param bytes must be a 4 byte IPV4 address
	 * @param cidrPrefixBits the cidr prefix length, which can be null for no prefix
	 */
	public IPV4Address(byte[] bytes, Integer cidrPrefixBits) {
		super(new IPV4AddressSection(toSegments(bytes, cidrPrefixBits)));
		if(bytes.length != BYTE_COUNT) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public IPV4AddressSection getFullSection() {
		return (IPV4AddressSection) addressSegments;
	}
	
	@Override
	public IPV4AddressSegment[] getSegments() {
		return (IPV4AddressSegment[]) super.getSegments();
	}
	
	@Override
	public int getSegmentCount() {
		return SEGMENT_COUNT;
	}
	
	@Override
	public int getByteCount() {
		return BYTE_COUNT;
	}
	
	@Override
	public int getBitCount() {
		return BIT_COUNT;
	}
	
	@Override
	public IPV4Address toIpv4() {
		return this;
	}
	
	@Override
	public Iterator<IPV4Address> iterator() {
		return iterator(network.creator);
	}
	
	@Override
	public IPV4AddressNetwork getNetwork() {
		return network;
	}
	
	public static IPV4Address getNetworkMask(int prefixBits) {
		return network.getNetworkMask(prefixBits, true);
	}
	
	public static IPV4Address getNetworkMask(int prefixBits, boolean withCIDR) {
		return network.getNetworkMask(prefixBits, withCIDR);
	}
	
	public static IPV4Address getHostMask(int prefixBits) {
		return network.getHostMask(prefixBits);
	}
	
	@Override
	public IPV4Address toSubnet(int prefixBits) {
		if(prefixBits >= (isNetworkPrefix() ? getNetworkPrefixBits() : getBitCount())) {
			return this;
		}
		IPV4Address mask = network.getNetworkMask(prefixBits, false);
		IPV4Address result = (IPV4Address) toSubnetImpl(mask, prefixBits); //note that we will never throw IPAddressTypeException here
		return result;
	}
	
	/**
	 * Creates a subnet address using the given mask. 
	 */
	@Override
	public IPV4Address toSubnet(IPAddress mask) throws IPAddressTypeException {
		return toSubnet(mask, null);
	}
	
	/**
	 * Creates a subnet address using the given mask.  If prefixBits is non-null, applies the prefix length as well.
	 */
	@Override
	public IPV4Address toSubnet(IPAddress mask, Integer prefixBits) throws IPAddressTypeException {
		if(isMultiple()) {
			throw new IPAddressTypeException(this, mask, "ipaddress.error.maskMismatch");
		}
		return toSubnetImpl(mask, prefixBits);
	}
	
	/**
	 * Creates a subnet address using the given mask.  If prefixBits is non-null, applies the prefix length as well.
	 */
	IPV4Address toSubnetImpl(IPAddress mask, Integer prefixBits) throws IPAddressTypeException {
		if(!mask.isIpv4()) {
			throw new IPAddressTypeException(this, mask, "ipaddress.error.typeMismatch");
		}
		IPV4Address msk = (IPV4Address) mask;
		IPV4AddressSegment segments[] = getSegments();
		IPV4AddressSegment newSegments[] = IPAddressSection.getSubnetSegments(getIpVersion(), segments, msk.getSegments(), prefixBits, network.creator);
		if(newSegments == segments) {
			return this;
		}
		return new IPV4Address(newSegments, prefixBits);
	}
	
	@Override
	public IPV4AddressSection getNetworkSection(int cidrBits) {
		return getFullSection().getNetworkSection(cidrBits);
	}
	
	@Override
	public IPV4AddressSection getHostSection(int cidrBits) {
		return getFullSection().getHostSection(cidrBits);
	}
	
	static IPV4AddressSegment[] toSegments(byte bytes[], Integer cidrPrefixBits) {
		return toSegments(bytes, BYTE_COUNT, SEGMENT_COUNT, BYTES_PER_SEGMENT, BITS_PER_SEGMENT, network.creator, cidrPrefixBits);
	}

	@Override
	public byte[] getIPV4MappedBytes() {
		return getBytes();
	}
	
	@Override
	public IPV4AddressSection toIPV4MappedSegments() {
		return getFullSection();
	}
	
	@Override
	public boolean isIpv4Mapped() {
		return true;
	}
	
	@Override
	public Inet4Address toInetAddress() {
		return (Inet4Address) super.toInetAddress();
	}
	
	/**
	 * @see {@link java.net.InetAddress#isLinkLocalAddress()}
	 */
	@Override
	public boolean isLinkLocal() {
		IPV4AddressSegment segments[] = getSegments();
		return segments[0].matches(169) && segments[1].matches(254);
	}
	
	/**
	 * @see {@link java.net.InetAddress#isSiteLocalAddress()}
	 */
	@Override
	public boolean isSiteLocal() {
		IPV4AddressSegment segments[] = getSegments();
		IPV4AddressSegment seg0 = segments[0];
		IPV4AddressSegment seg1 = segments[1];
		return seg0.matches(10)
			|| seg0.matches(172) && seg1.matchesWithMask(16, 0xF0)
			|| seg0.matches(192) && seg1.matches(168);
	}
	
	/**
	 * @see {@link java.net.Inet6Address#isIPv4CompatibleAddress()}
	 */
	@Override
	public boolean isIPv4Compatible() {
		return true;
	}
	
	/**
	 * @see {@link java.net.InetAddress#isLoopbackAddress()}
	 */
	@Override
	public boolean isLoopback() {
		IPV4AddressSegment segments[]= getSegments();
		return segments[0].matches(127); 
	}
	
	@Override
	public boolean isIPV6ToIPV4Relay() {
		IPV4AddressSegment segments[]= getSegments();
		return segments[0].matches(0x2002);
	}
	
	/*
	 * We typically work with segment values, so we don't calculate bytes unless requested
	 */
	@Override
	byte[] getBytesImpl() {
		return toBytes(BYTE_COUNT, BYTES_PER_SEGMENT);
	}
		
	@Override
	public int compareTo(IPAddress other) {
		IPAddressSegment otherSegs[] = other.getSegments();
		if(getSegments().length < otherSegs.length) {
			if(other.isIpv4Mapped()) {
				IPV4AddressSection otherMapped = other.toIPV4MappedSegments();
				return addressSegments.compareTo(otherMapped);
			} else {
				return -1;
			}
		} 
		return super.compareTo(other);
	}
}
