/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.guardium.net.IPAddressSegment.IPAddressSegmentCreator;
import com.guardium.net.IPAddressTypeNetwork.IPAddressCreator;
import com.guardium.utils.i18n.SayAppRes;

/**
 * An IP address, or a range of addresses.  A range can be specified by a CIDR/IP address, or by using wildcards '*' or ranges with '-'
 * 
 * IPAddress objects are immutable, which also makes them thread-safe.
 * 
 * @author sfoley
 *
 */
public abstract class IPAddress implements Comparable<IPAddress> {
	
	public enum IpVersion {
		IPV4,
		IPV6
	};
	
	public enum RangeOptions {
		NO_RANGE,
		WILDCARD_ONLY,
		WILDCARD_AND_RANGE
	};
	
	public static RangeOptions DEFAULT_RANGE_OPTIONS = RangeOptions.WILDCARD_ONLY;
	
	public static final char RANGE_SEPARATOR = '-';
	public static final String RANGE_SEPARATOR_STR = String.valueOf(RANGE_SEPARATOR);
	public static final char SEGMENT_WILDCARD = '*';
	public static final String SEGMENT_WILDCARD_STR = String.valueOf(SEGMENT_WILDCARD);
	public static final char EQUIVALENT_SEGMENT_WILDCARD = '%';
	public static final String EQUIVALENT_SEGMENT_WILDCARD_STR = String.valueOf(EQUIVALENT_SEGMENT_WILDCARD);
	
	/* the segments.  For IPV4, each element is actually just 1 byte and the array has 4 elements, while for IPV6, each element is 2 bytes and the array has 8 elements. */
	final IPAddressSection addressSegments;
	
	/* the address bytes */
	private byte[] bytes;
	
	/**
	 * Represents an IP address or a set of addresses.
	 * @param segments the address segments
	 */
	public IPAddress(IPAddressSection section) {
		this.addressSegments = section;
	}
	
	/**
	 * Represents an IP address.
	 * @param bytes must be either a 4 byte IPV4 address or a 16 byte IPV6 address
	 * @throws IllegalArgumentException if bytes is not length 4 or 16
	 */
	public IPAddress(byte[] bytes, IPAddressSection section) {
		this(section);
		setBytes(bytes);
	}
	
	static <T extends IPAddressSegment> T[] toSegments(byte bytes[], int byteCount, int segmentCount, int bytesPerSegment, int bitsPerSegment, IPAddressSegmentCreator<T> creator, Integer cidrPrefixBits) {
		if(bytes.length != byteCount) {
			throw new IllegalArgumentException();
		}
		int cidrByteIndex = getByteIndex(cidrPrefixBits, bytes.length);
		T segments[] = creator.createAddressSegmentArray(segmentCount);
		for(int i = 0; i < byteCount; i += bytesPerSegment) {
			int value = 0;
			int k = bytesPerSegment + i;
			for(int j = i; j < k; j++) {
				int byteValue;
				
				if(j >= cidrByteIndex) {
					//apply the CIDR to the bytes
					if(j == cidrByteIndex) {
						int startBits = cidrPrefixBits % 8;
						if(startBits != 0) {
							byte mask = (byte) (0xff << (8 - startBits));
							byteValue = (byte) (mask & bytes[j]);
						} else {
							byteValue = bytes[j];
						}
					} else {
						byteValue = 0;
					}
				} else {
					byteValue = bytes[j];
				}
				
				value <<= 8;
				value |= 0xff & byteValue;
			}
			int segmentIndex = i / bytesPerSegment;
			Integer prefixBits = IPAddressSection.getSegmentPrefixBits(bitsPerSegment, cidrPrefixBits, segmentIndex, segmentCount);
			segments[segmentIndex] = creator.createAddressSegment(value, prefixBits);
		}
		return segments;
	}
	
	byte[] toBytes(int byteCount, int bytesPerSegment) {
	 	IPAddressSegment segments[] = getSegments();
		byte bytes[] = new byte[byteCount];
		for(int i = 0, n = 0; i < byteCount; i += bytesPerSegment, n++) {
			int segmentValue = segments[n].getLowerValue();
			int k = bytesPerSegment + i;
			for(int j = k - 1; ; j--) {
				bytes[j] = (byte) (0xff & segmentValue);
				if(j <= i) {
					break;
				}
				segmentValue >>= 8;
			}
		}
		return bytes;
	}
	
	public abstract IPAddressNetwork getNetwork();
	
	public IPAddressSection getFullSection() {
		return addressSegments;
	}
	
	public int getMaxSegmentValue() {
		return IPAddressSegment.getMaxSegmentValue(getIpVersion());
	}
	
	public static int maxSegmentValue(IpVersion version) {
		return IPAddressSegment.getMaxSegmentValue(version);
	}
	
	public int getBytesPerSegment() {
		return IPAddressSegment.getByteCount(getIpVersion());
	}
	
	public int getBitsPerSegment() {
		return IPAddressSegment.getBitCount(getIpVersion());
	}
	
	public static int bitsPerSegment(IpVersion version) {
		return IPAddressSegment.getBitCount(version);
	}
	
	public abstract int getByteCount();
	
	public static int byteCount(IpVersion version) {
		return version == IpVersion.IPV4 ? IPV4Address.BYTE_COUNT : IPV6Address.BYTE_COUNT;
	}
	
	public abstract int getSegmentCount();
	
	public static int segmentCount(IpVersion version) {
		return version == IpVersion.IPV4 ? IPV4Address.SEGMENT_COUNT : IPV6Address.SEGMENT_COUNT;
	}
	
	public boolean hasAlphabeticDigits() {
		return addressSegments.hasAlphabeticDigits();
	}
	
	public char getSeparator() {
		return addressSegments.getSegmentSeparator();
	}
	
	public abstract int getBitCount();
	
	public static int bitCount(IpVersion version) {
		return version == IpVersion.IPV4 ? IPV4Address.BIT_COUNT : IPV6Address.BIT_COUNT;
	}
	
	public boolean isMultipleCIDRAddresses() {
		if(!isNetworkPrefix()) {
			return false;
		}
		IPAddressSegment segments[] = getSegments();
		return getNetworkPrefixBits() < (segments.length * segments[0].getBitCount());
	}
	
	/**
	 * @return whether this address represents more than one address.
	 * Such addresses include CIDR/IP addresses (eg 1.2.3.4/11) or wildcard addresses (eg 1.2.*.4) or range addresses (eg 1.2.3-4.5)
	 */
	public boolean isMultiple() {
		if(isMultipleCIDRAddresses()) {
			return true;
		}
		return addressSegments.isMultiple();
	}
	
	/**
	 * @return whether this address represents a network prefix or the set of all addresses with the same network prefix
	 */
	public boolean isNetworkPrefix() {
		return addressSegments.isNetworkPrefix();
	}
	
	public Integer getNetworkPrefixBits() {
		return addressSegments.getNetworkPrefixBits();
	}
	
	public IPAddressSegment[] getSegments() {
		return addressSegments.getSegments();
	}
	
	/**
	 * gets the count of addresses that this address may represent
	 * 
	 * If this address is not a CIDR and it has no range, then there is only one such address.
	 * 
	 * @return
	 */
	public BigInteger getCount() {
		if(!isMultiple()) {
			return BigInteger.ONE;
		}
		return addressSegments.getCount();
	}
	
	public abstract Iterator<? extends IPAddress> iterator();
	
	<T extends IPAddress, S extends IPAddressSegment> Iterator<T> iterator(final IPAddressCreator<T, S> creator) {
		return new Iterator<T>() {
			Iterator<S[]> iterator = IPAddressSection.cast(addressSegments.iterator());
			
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

		    @Override
			public T next() {
		    	if(!hasNext()) {
		    		throw new NoSuchElementException();
		    	}
		    	S[] next = iterator.next();
		    	T result = creator.createAddress(next);
		    	return result;
		    }

		    @Override
			public void remove() {
		    	throw new UnsupportedOperationException();
		    }
		};
	}

	public boolean isIpv4() {
		return addressSegments.isIpv4();
	}
	
	public boolean isIpv6() {
		return addressSegments.isIpv6();
	}
	
	public IpVersion getIpVersion() {
		return addressSegments.getIpVersion();
	}
	
	/**
	 * If this address is IPV4, return this object cast to IPV4Address.  Otherwise, returns null.
	 * @return
	 */
	public IPV4Address toIpv4() {
		return null;
	}
	
	/**
	 * If this address is IPV6, return this object cast to IPV6Address.  Otherwise, returns null.
	 * @return
	 */
	public IPV6Address toIpv6() {
		return null;
	}
	
	public static IPAddress from(byte bytes[]) {
		if(bytes.length == 4) {
			return new IPV4Address(bytes);
		}
		return new IPV6Address(bytes);
	}

	public abstract byte[] getIPV4MappedBytes();
	
	abstract IPV4AddressSection toIPV4MappedSegments();
	
	public abstract boolean isIpv4Mapped();

	/**
	 * @see java.net.InetAddress#isLinkLocalAddress()
	 */
	public abstract boolean isLinkLocal();
	
	/**
	 * @see java.net.InetAddress#isSiteLocalAddress()
	 */
	public abstract boolean isSiteLocal();
	
	/**
	 * @see {@link java.net.InetAddress#isAnyLocalAddress()}
	 */
	public boolean isAnyLocal() {
		return isZero();
	}
	
	/**
	 * @see {@link java.net.Inet6Address#isIPv4CompatibleAddress()}
	 */
	public abstract boolean isIPv4Compatible();
	
	/**
	 * @see {@link java.net.InetAddress#isLoopbackAddress()}
	 */
	public abstract boolean isLoopback();
	
	public abstract boolean isIPV6ToIPV4Relay();
	
	abstract byte[] getBytesImpl();
	
	/**
	 * @throws IllegalStateException if this address does not map to a single address
	 */
	public byte[] getBytes() {
		if(isMultiple()) {
			throw new IllegalStateException(
					toString() + " " + SayAppRes.what("ipaddress.address.error") + " " + SayAppRes.what("ipaddress.error.unavailable.numeric"));
		}
		return getLowestBytes();
	}
	
	/**
	 * Gets the bytes for the lowest address in the range represented by this address.
	 * 
	 * @return
	 */
	public byte[] getLowestBytes() {
		if(bytes == null) {
			setBytes(getBytesImpl());
		}
		return bytes;
	}
	
	private void setBytes(byte bytes[]) {
		this.bytes = bytes;
	}
	
	/**
	 * @throws IllegalStateException if this address does not map to a single address
	 */
	public InetAddress toInetAddress() {
		byte bytes[] = getBytes();
		try {
			return InetAddress.getByAddress(bytes);
		} catch(UnknownHostException e) {
			return null;
		}
	}
	
	public boolean isZero() {
		if(isMultipleCIDRAddresses()) {
			return false;
		}
		return addressSegments.isZero();
	}
	
	@Override
	public int hashCode() {
		return toNormalizedString().hashCode();
	}
	
	@Override
	public int compareTo(IPAddress other) {
		int result = addressSegments.compareTo(other.addressSegments);
		if(result == 0) {
			if(!isNetworkPrefix()) {
				if(other.isNetworkPrefix()) {
					result = -1;
				}
			} else {
				if(!other.isNetworkPrefix()) {
					result = 1;
				} else {
					result = getNetworkPrefixBits() - other.getNetworkPrefixBits();
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof IPAddressString) {
			try {
				IPAddress other = ((IPAddressString) o).toValue();
				if(other != null) {
					return equals(other);
				}
			} catch(IPAddressException e) {
				return false;
			}
		}
		if(o instanceof IPAddress) {
			IPAddress other = (IPAddress) o;
			return compareTo(other) == 0;
		}
		return false;
	}
	
	//////////////// string creation below ///////////////////////////////////////////////////////////////////////////////////////////
	
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
		return addressSegments.toCanonicalString();
	}

	/**
	 * This produces a string with no compressed segments and all segments of full length,
	 * which is 4 characters for IPV6 segments and 3 characters for IPV4 segments.
	 * 
	  * Each address has a unique full string.
	 */
	public String toFullString() {
		return addressSegments.toFullString();
	}
	
	/**
	 * The normalized string returned by this method is consistent with java.net.Inet4Address and java.net.Inet6Address.
	 * IPs are not compressed nor mixed in this representation.
	 * 
	 * The string returned by this method is unique for each address.
	 */
	public String toNormalizedString() {
		return addressSegments.toNormalizedString();
	}
	
	/**
	 * This produces the shortest valid string for the address.
	 * 
	 * Each address has a unique compressed string.
	 */
	public String toCompressedString() {
		return addressSegments.toCompressedString();
	}
	
	/**
	 * This produces a string similar to the canonical string,
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toCompressedWildcardString() {
		return addressSegments.toCompressedWildcardString();
	}
	
	/**
	 * This produces a string similar to the canonical string,
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toCanonicalWildcardString() {
		return addressSegments.toCanonicalWildcardString();
	}
	
	/**
	 * This produces a string similar to the normalized string, 
	 * except that CIDR addresses will be shown with wildcards and ranges instead,
	 * and no CIDR prefix length will be shown.
	 */
	public String toNormalizedWildcardString() {
		return addressSegments.toNormalizedWildcardString();
	}
	
	/**
	 * Returns at most a few dozen string representations:
	 * 
	 * -mixed (1:2:3:4:5:6:1.2.3.4)
	 * -full compressions (a:0:b:c:d:0:e:f or a::b:c:d:0:e:f or a:0:b:c:d::e:f)
	 * -full leading zeros (000a:0000:000b:000c:000d:0000:000e:000f)
	 * -all uppercase and all lowercase (a::a can be A::A)
	 * -combinations thereof
	 * 
	 * @return
	 */
	public String[] toStandardStrings() {
		return addressSegments.toStandardStrings();
	}
	
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
		return addressSegments.toBasicStrings();
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
	 * -all uppercase and all lowercase (a::a can be A::A)
	 * -all combinations of such variations
	 * 
	 * Variations omitted from this method: mixed case of a-f, which you can easily handle yourself with String.equalsIgnoreCase
	 * 
	 * @return
	 */
	public String[] toAllStrings() {
		return addressSegments.toAllStrings();
	}
	
	/**
	 * Rather than using toAllStrings, toBasicStrings or toStandardStrings, 
	 * you can use this method to customize the list of strings produced for this address
	 */
	public String[] toStrings(IPStringBuilderOptions options) {
		return addressSegments.toStrings(options);
	}
	
	public static String toDelimitedSQLStrs(String strs[]) {
		if(strs.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(String str : strs) {
			builder.append('\'').append(str).append('\'').append(',');
		}
		return builder.substring(0, builder.length() - 1);
	}
	
	///////////////////// masks and subnets below ///////////////////////
	
	
	
	/**
	 * If this address is equivalent to the mask for a CIDR prefix, it returns that prefix length.
	 * Otherwise, it returns null.
	 * A CIDR network mask is all 1s in the network section and then all 0s in the host section.
	 * A CIDR host mask is all 0s in the network section and then all 1s in the host section.
	 * The prefix is the length of the network section.
	 * 
	 * Also, keep in mind that the prefix length returned by this method is not equivalent to the prefix length used to construct this object.
	 * The prefix length used to construct indicates the network and host portion of this address.  
	 * The prefix length returned here indicates the whether the value of this address can be used as a mask for the network and host
	 * portion of any other address.  Therefore the two values can be different values, or one can be null while the other is not.
	 *
	 * @param network whether to check if we are a network mask or a host mask
	 * @return the prefix length corresponding to this mask, or null if this address is not a CIDR prefix mask
	 */
	public Integer getCIDRMaskPrefixLength(boolean network) {
		return addressSegments.getCIDRMaskPrefixLength(network);
	}
	
	static <S extends IPAddressSegment> S[] toCIDRSegments(Integer bits, S segments[], IPAddressSegmentCreator<S> segmentCreator) {
		if(bits == null) {
			return segments;
		}
		boolean different = false;
		for(int i=0; i < segments.length; i++) {
			IPAddressSegment seg = segments[i];
			int bitCount = seg.getBitCount();
			Integer segmentPrefixBits = IPAddressSection.getSegmentPrefixBits(bitCount, bits, i, segments.length);
			if(seg.isNetworkChangedByPrefix(segmentPrefixBits)) {
				different = true;
				break;
			}
		}
		if(!different) {
			return segments;
		}
		S newSegments[] = segmentCreator.createAddressSegmentArray(segments.length);
		for(int i = 0; i < segments.length; i++) {
			S seg = segments[i];
			int bitCount = seg.getBitCount();
			Integer segmentPrefixBits = IPAddressSection.getSegmentPrefixBits(bitCount, bits, i, segments.length);
			newSegments[i] = IPAddressSection.cast(seg.toNetworkSegment(segmentPrefixBits));
		}
		return newSegments;
	}
	
	static int getSegmentIndex(Integer prefixBits, int byteLength, int bytesPerSegment) {
		int byteIndex = getByteIndex(prefixBits, byteLength);
		return byteIndex / bytesPerSegment;
	}
	
	static int getByteIndex(Integer prefixBits, int byteLength) {
		if(prefixBits == null) {
			return byteLength;
		}
		return Math.min((prefixBits - 1) >> 3, byteLength);
	}
	
	/**
	 * Creates a subnet address using the given mask. 
	 */
	public abstract IPAddress toSubnet(IPAddress mask) throws IPAddressTypeException;
	
	/**
	 * Creates a subnet address using the given mask.  If cidrPrefixBits is non-null, applies the prefix as well.
	 */
	public abstract IPAddress toSubnet(IPAddress mask, Integer cidrPrefixBits) throws IPAddressTypeException;
	
	/**
	 * Creates a subnet address using the given CIDR prefix bits.
	 */
	public abstract IPAddress toSubnet(int cidrPrefixBits);
	
	/**
	 * Generates the network section of the address.  The network section will remember the CIDR prefix bit length that was used to create it.
	 * 
	 * Note:  This generates only a part of an address.  If you wish to generate a full address representing all potential hosts in 
	 * the network, use an IPV6Address or IPV4Address constructor that takes the CIDR prefix bits.
	 * 
	 * @param cidrBits
	 * @return
	 */
	public abstract IPAddressSection getNetworkSection(int cidrBits);
	
	/**
	 * Generates the host section of the address.
	 * 
	 * @param cidrBits
	 * @return
	 */
	public abstract IPAddressSection getHostSection(int cidrBits);
	
	public void getStartsWithSQLClause(StringBuilder builder, String columnName) {
		addressSegments.getStartsWithSQLClause(builder, columnName);
	}
}
