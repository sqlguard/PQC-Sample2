/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import com.guardium.net.IPV6AddressNetwork.IPV6AddressCreator;
import com.guardium.net.IPV6AddressSection.AddressStringNormalizationParams;

/**
 * An IPV6 address
 * 
 * @author sfoley
 *
 */
public class IPV6Address extends IPAddress {

	public static final char SEGMENT_SEPARATOR = ':';
	public static final char ZONE_SEPARATOR = '%';
	
	public static final String SEGMENT_SEPARATOR_STR = String.valueOf(SEGMENT_SEPARATOR);
	public static final int BITS_PER_SEGMENT = 16;
	public static final int BYTES_PER_SEGMENT = 2;
	public static final int SEGMENT_COUNT = 8;
	public static final int BYTE_COUNT = 16;
	public static final int BIT_COUNT = 128;
	public static final int MAX_STRING_LEN = 50;
	
	static IPV6AddressNetwork network = new IPV6AddressNetwork();

	private final String zone;
	
	/* the lowest 4 bytes of the address */
	private IPV4Address mixedAddress;
	
	/* whether the last two segments were displayed in IPV4 format like a:b:c:d:e:f:1.2.3.4 in the string that constructed this object */
	private final boolean wasMixed;
	
	/**
	 * Represents an IPV6 address or a set of addresses.
	 * @param segments the address segments
	 * @param mixed whether to favour mixed addresses when printing this address
	 * @param zone the zone, which should be null if a cidr prefix length was specified
	 */
	public IPV6Address(IPV6AddressSegment[] segments, boolean wasMixed, String zone) {
		this(segments, wasMixed, zone, null);
	}
	
	/**
	 * Represents an IPV6 address or a set of addresses.
	 * When cidrPrefixBits is non-null, this object represents a network prefix or the set of addresses with the same network prefix (a network or subnet, in other words).
	 * @param segments the address segments
	 * @param cidrPrefixBits
	 * @param mixed whether to favour mixed addresses when printing this address
	 * @param zone the zone, which should be null if a cidr prefix length was specified
	 */
	public IPV6Address(IPV6AddressSegment[] segments, boolean mixed, String zone, Integer cidrPrefixBits) {
		super(createSection(segments, cidrPrefixBits));
		if(segments.length != SEGMENT_COUNT) {
			throw new IllegalArgumentException();
		}
		wasMixed = mixed;
		if(zone == null) {
			zone = "";
		}
		this.zone = zone;
		mixedAddress = new IPV4Address(getFullSection().getMixedIPV4Segments());
	}
	
	/**
	 * Represents an IPV6 address.
	 *
	 * @param bytes must be a 16 byte IPV6 address
	 */
	public IPV6Address(byte[] bytes) {
		this(bytes, null);
	}
	
	/**
	 * Represents an IPV6 address or a set of addresses.
	 * When cidrPrefixBits is non-null, this object represents a network prefix or the set of addresses with the same network prefix (a network or subnet, in other words).
	 * 
	 * @param bytes must be a 16 byte IPV6 address
	 * @param cidrPrefixBits the cidr prefix, which can be null for no prefix length
	 */
	public IPV6Address(byte[] bytes, Integer cidrPrefixBits) {
		super(bytes, createSection(bytes, cidrPrefixBits));
		if(bytes.length != BYTE_COUNT) {
			throw new IllegalArgumentException();
		}
		mixedAddress = new IPV4Address(getFullSection().getMixedIPV4Segments());
		wasMixed = false;
		zone = "";
	}
	
	@Override
	public IPV6AddressNetwork getNetwork() {
		return network;
	}
	
	@Override
	public IPV6AddressSection getFullSection() {
		return (IPV6AddressSection) addressSegments;
	}
	
	@Override
	public IPV6AddressSegment[] getSegments() {
		return (IPV6AddressSegment[]) super.getSegments();
	}
	
	public static int getMixedReplacedSegments() {
		return IPV4Address.BYTE_COUNT / BYTES_PER_SEGMENT;
	}
	
	public static int getMixedOriginalSegments() {
		return SEGMENT_COUNT - getMixedReplacedSegments();
	}
	
	private static IPV6AddressSection createSection(byte[] bytes, Integer cidrPrefixBits) {
		IPV6AddressSegment segments[] = toSegments(bytes, cidrPrefixBits);
		int mixedIndex = getMixedOriginalSegments();
		IPV4AddressSegment[] mixed = IPV6AddressSegment.split(segments[mixedIndex], segments[mixedIndex + 1]);
		return new IPV6AddressSection(segments, mixed);
	}
			
	private static IPV6AddressSection createSection(IPV6AddressSegment segments[], Integer cidrPrefixBits) {
		segments = toCIDRSegments(cidrPrefixBits, segments, network.creator);
		int mixedIndex = getMixedOriginalSegments();
		IPV4AddressSegment[] mixed = IPV6AddressSegment.split(segments[mixedIndex], segments[mixedIndex + 1]);
		return new IPV6AddressSection(segments, mixed);
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
	public Iterator<IPV6Address> iterator() {
		return super.iterator(new IPV6AddressCreator() {
			@Override
			public IPV6Address createAddress(IPV6AddressSegment segments[]) {
				return new IPV6Address(segments, wasMixed, zone);
			}
		});
	}
	
	@Override
	public IPV6Address toIpv6() {
		return this;
	}
	
	/**
	 * Creates a subnet address using the given mask. 
	 */
	@Override
	public IPV6Address toSubnet(IPAddress mask) throws IPAddressTypeException {
		return toSubnet(mask, null);
	}
	
	/**
	 * Creates a subnet address using the given mask.  If cidrPrefixBits is non-null, applies the prefix length as well.
	 * The mask can be a subnet itself, in which case the lowest value of the mask's range is used.
	 * "this" cannot be a subnet, it must be a single address (if we have a range, then applying the mask to the lower and the upper values
	 * of the range does not ensure that all addresses in the range will have the mask applied).
	 */
	@Override
	public IPV6Address toSubnet(IPAddress mask, Integer cidrPrefixBits) throws IPAddressTypeException {
		if(isMultiple()) {
			throw new IPAddressTypeException(this, mask, "ipaddress.error.maskMismatch");
		}
		return toSubnetImpl(mask, cidrPrefixBits);
	}
	
	IPV6Address toSubnetImpl(IPAddress mask, Integer cidrPrefixBits) throws IPAddressTypeException {
		if(!mask.isIpv6()) {
			throw new IPAddressTypeException(this, mask, "ipaddress.error.typeMismatch");
		}
		IPV6Address msk = (IPV6Address) mask;
		IPV6AddressSegment segments[] = getSegments();
		IPV6AddressSegment newSegments[] = IPAddressSection.getSubnetSegments(getIpVersion(), segments, msk.getSegments(), cidrPrefixBits, network.creator);
		if(newSegments == segments) {
			return this;
		}
		return new IPV6Address(newSegments, wasMixed, zone, cidrPrefixBits);
	}
	
	public static IPV6Address getNetworkMask(int prefixBits) {
		return network.getNetworkMask(prefixBits, true);
	}
	
	public static IPV6Address getNetworkMask(int prefixBits, boolean withCIDR) {
		return network.getNetworkMask(prefixBits, withCIDR);
	}
	
	public static IPV6Address getHostMask(int prefixBits) {
		return network.getHostMask(prefixBits);
	}
	
	@Override
	public IPV6Address toSubnet(int prefixBits) {
		if(prefixBits >= (isNetworkPrefix() ? getNetworkPrefixBits() : getBitCount())) {
			return this;
		}
		IPV6Address mask = getNetwork().getNetworkMask(prefixBits, false);
		IPV6Address result = toSubnetImpl(mask, prefixBits);
		return result;
	}

	static IPV6AddressSegment[] toSegments(byte bytes[], Integer cidrPrefix) {
		return toSegments(bytes, BYTE_COUNT, SEGMENT_COUNT, BYTES_PER_SEGMENT, BITS_PER_SEGMENT, network.creator, cidrPrefix);
	}
	
	@Override
	public IPV6AddressSection getNetworkSection(int cidrBits) {
		return getFullSection().getNetworkSection(cidrBits);
	}
	
	@Override
	public IPV6AddressSection getHostSection(int cidrBits) {
		return getFullSection().getHostSection(cidrBits);
	}
	
	boolean hasZone() {
		return zone.length() > 0;
	}
	
	public String getZone() {
		return zone;
	}
	
	@Override
	public byte[] getIPV4MappedBytes() {
		if(isIpv4Mapped()) {
			return mixedAddress.getBytes();
		}
		return null;
	}
	
	@Override
	public IPV4AddressSection toIPV4MappedSegments() {
		if(isIpv4Mapped()) {
			return mixedAddress.getFullSection();
		}
		return null;
	}
	
	@Override
	public boolean isIpv4Mapped() {
		IPV6AddressSegment segments[] = getSegments();
		//::ffff:0:0/96 indicates IPV6 address mapped to IPV4
		if(segments[5].matches(0xffff)) {
			for(int i=0; i<4; i++) {
				if(!segments[i].isZero()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean isIpv4Translated() {
		IPV6AddressSegment segments[] = getSegments();
		//::ffff:0:0:0/96 indicates IPV6 addresses translated from IPV4
		if(segments[4].matches(0xffff) && segments[5].isZero()) {
			for(int i=0; i<3; i++) {
				if(!segments[i].isZero()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @see java.net.InetAddress#isLinkLocalAddress()
	 */
	@Override
	public boolean isLinkLocal() {
		IPV6AddressSegment segments[] = getSegments();
		return segments[0].matches(0xfe80);
	}
	
	/**
	 * @see java.net.InetAddress#isSiteLocalAddress()
	 */
	@Override
	public boolean isSiteLocal() {
		IPV6AddressSegment segments[] = getSegments();
		return segments[0].matches(0xfec0);
	}
	
	/**
	 * @see {@link java.net.Inet6Address#isIPv4CompatibleAddress()}
	 */
	@Override
	public boolean isIPv4Compatible() {
		IPV6AddressSegment segments[] = getSegments();
		return segments[0].isZero() && segments[1].isZero() && segments[2].isZero() &&
				segments[3].isZero() && segments[4].isZero() && segments[5].isZero();
	}
	
	/**
	 * @see {@link java.net.InetAddress#isLoopbackAddress()}
	 */
	@Override
	public boolean isLoopback() {
		if(this.isIpv4Mapped()) { // || this.isIPv4Compatible()
			return mixedAddress.isLoopback();
		}
		IPV6AddressSegment segments[] = getSegments();
		//::1
		int i=0;
		for(; i<segments.length - 1; i++) {
			if(!segments[i].isZero()) {
				return false;
			}
		}
		return segments[i].matches(1);
	}
	
	@Override
	public boolean isIPV6ToIPV4Relay() {
		IPV6AddressSegment segments[] = getSegments();
		return segments[0].matches(0x2002);
	}
	
	public boolean is6To4() {
		IPV6AddressSegment segments[] = getSegments();
		//2002::/16
		return segments[0].matches(0x2002);
	}
	
	public boolean isWellKnown() {
		IPV6AddressSegment segments[] = getSegments();
		//64:ff9b::/96 well known prefix for auto ipv4/ipv6 translation
		if(segments[0].matches(0x64) && segments[1].matches(0xff9b)) {
			for(int i=2; i<=5; i++) {
				if(!segments[i].isZero()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/*
	 * We typically work with segment values, so we don't calculate bytes unless requested
	 */
	@Override
	byte[] getBytesImpl() {
		return toBytes(BYTE_COUNT, BYTES_PER_SEGMENT);
	}
	
	@Override
	public Inet6Address toInetAddress() {
		byte bytes[] = getBytes();
		try {
			if(hasZone()) {
				try {
					int scopeId = Integer.valueOf(zone);
					return Inet6Address.getByAddress(null, bytes, scopeId);
				} catch(NumberFormatException e) {
					//there is no related function that takes a string as third arg. Only other one takes a NetworkInterface.  we don't want to be looking up network interface objects.
					//public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) 
				
					//so we must go back to a string, even though we have the bytes available to us.  There appears to be no other alternative.
					return (Inet6Address) InetAddress.getByName(toNormalizedString());
				}
			}
			return (Inet6Address) InetAddress.getByAddress(bytes);
		} catch(UnknownHostException e) {
			return null;
		}
	}
	
	@Override
	public int compareTo(IPAddress other) {
		IPAddressSegment otherSegs[] = other.getSegments();
		if(getSegments().length > otherSegs.length) {
			if(isIpv4Mapped()) {
				IPV4AddressSection mapped = toIPV4MappedSegments();
				return mapped.compareTo(other.addressSegments);
			} else {
				return 1;
			}
		}
		return super.compareTo(other);
	}
	
	//////////////// string creation below ///////////////////////////////////////////////////////////////////////////////////////////
		
	/**
	 * Constructs a string representing this address according to the given parameters
	 * 
	 * @param params the parameters for the address string
	 */
	public String toNormalizedString(AddressStringNormalizationParams params) {
		return toNormalizedString(false, params);
	}
	
	/**
	 * Constructs a string representing this address according to the given parameters
	 * 
	 * @param keepMixed if this address was constructed from a string with mixed representation (a:b:c:d:e:f:1.2.3.4), whether to keep it that way (ignored if makeMixed is true in the params argument)
	 * @param params the parameters for the address string
	 */
	public String toNormalizedString(boolean keepMixed, AddressStringNormalizationParams params) {
		if(keepMixed && wasMixed) {
			params.makeMixed = true;
		}
		return getFullSection().toNormalizedString(params);
	}
	
	private String appendZone(String str) {
		if(!isMultiple() && hasZone()) {
			return str + ZONE_SEPARATOR + getZone();
		}
		return str;
	}
	
	private String[] appendZone(String strs[]) {
		if(!isMultiple() && hasZone()) {
			for(int i=0; i<strs.length; i++) {
				strs[i] = appendZone(strs[i]);
			}
		}
		return strs;
	}
	
	@Override
	public String toString() {
		return appendZone(super.toString());
	}
	
	public String toMixedString() {
		return appendZone(((IPV6AddressSection) addressSegments).toMixedString());
	}

	@Override
	public String toCanonicalString() {
		return appendZone(super.toCanonicalString());
	}

	@Override
	public String toFullString() {
		return appendZone(super.toFullString());
	}
	
	@Override
	public String toNormalizedString() {
		return appendZone(super.toNormalizedString());
	}
	
	@Override
	public String toCompressedString() {
		return appendZone(super.toCompressedString());
	}
	
	@Override
	public String toCompressedWildcardString() {
		return appendZone(super.toCompressedWildcardString());
	}
	
	@Override
	public String toCanonicalWildcardString() {
		return appendZone(super.toCanonicalWildcardString());
	}
	
	@Override
	public String toNormalizedWildcardString() {
		return appendZone(super.toNormalizedWildcardString());
	}
	
	@Override
	public String[] toStandardStrings() {
		return appendZone(super.toStandardStrings());
	}
	
	@Override
	public String[] toAllStrings() {
		return appendZone(super.toAllStrings());
	}
	
	@Override
	public String[] toBasicStrings() {
		return appendZone(super.toBasicStrings());
	}
	
	@Override
	public String[] toStrings(IPStringBuilderOptions options) {
		return appendZone(super.toStrings(options));
	}
}
