/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import com.guardium.net.IPAddress.IpVersion;
import com.guardium.net.IPAddress.RangeOptions;
import com.guardium.net.IPAddressPatterns.Range;

/**
 * Represents an IPAddress constructed from a string.
 * 
 * We could also use InetAddress.getByName to validate IP addresses.
 * 
 * However, this is much more flexible, separating IPV4 validation from IPV6 validation, 
 * providing specific error messages, and allowing more specific configuration.
 * 
 * Additionally, in some cases where you supply an invalid IP address to InetAddress.getByName, such as "a.2.3.4", 
 * InetAddress.getByName will try to do a DNS lookup, mistaking the invalid address for a host name.
 * 
 * This also provides lots of normalization functionality so that we display and store addresses consistently.
 * 
 * Finally, it is also designed to work with wildcards '*' and ranges '-' for the data security, auto discovery, and vulnerability assessment features.
 * 
 * The test class IPAddressTest can be used to validate any changes to this class.
 * 
 * This class supports CIDR IP addresses and IPV6 zones. 
 * 
 * RFCs of interest are 3986, 4291, 5952, 2765, 1918, 3513 (ipv4 rfcs 1123 0953)
 * 
 * This class is thread-safe.
 * 
 * @author sfoley
 *
 */
public class IPAddressString implements Comparable<IPAddressString> {

	public static final int IPV6_RADIX = 16;
	public static final int IPV4_RADIX = 10;
	public static final char PREFIX_LEN_SEPARATOR = '/';
	
	public static IPAddressValidationOptions DEFAULT_VALIDATION_OPTIONS = 
			new IPAddressValidationOptions(false, true, true, new IPAddressValidationOptions(false, true, false), true, RangeOptions.NO_RANGE, false);
	
	public static IPAddressValidationOptions DEFAULT_WILDCARD_OPTIONS = 
			new IPAddressValidationOptions(false, true, true, new IPAddressValidationOptions(false, true, false, RangeOptions.WILDCARD_ONLY, true, false), false, RangeOptions.WILDCARD_ONLY, true);
	
	public static IPAddressValidationOptions DEFAULT_WILDCARD_AND_RANGE_OPTIONS = 
			new IPAddressValidationOptions(false, true, true, new IPAddressValidationOptions(false, true, false, RangeOptions.WILDCARD_AND_RANGE, true, false), false, RangeOptions.WILDCARD_AND_RANGE, true);
	
	public static IPAddressValidationOptions DEFAULT_RANGE_VALIDATION_OPTIONS = DEFAULT_WILDCARD_OPTIONS;
			
	
	private final IPAddressValidationOptions validationOptions;
	
	/* the full original string address  */
	private final String fullAddr;
	
	/* the original address, prefix length and zone extracted from fullAddr. 
	* prefix length and zone are optional.
	*/
	private final String str;
	private final String zone;
	private final String networkPrefixLength;
	
	/* if there is a prefix length, this is its numeric value */
	private Integer networkPrefixBits;
	
	/* patterns for validation and parsing */
	private IPAddressPatterns patterns;
	
	/* the segments in the original str (does not include ipv6 omitted segments, omitted because they are 0). */
	private String ipArrayStr[];
	
	/* the segments as lined up with the original string.  ipArrayInt[i] is the int value of ipArrayStr[i] */
	private Integer ipArrayInt[];
	private boolean cachedArrayInt[];
	
	/* the ranges as lined up with the original string.  ipArrayRange[i] is the range of values of ipArrayStr[i] */
	private Range ipArrayRange[];
	
	/* for IPV6, the number of segments omitted from the original address string */
	private int missingSegmentCount;
	
	/* for valid addresses, this will be the address itself. */
	private IPAddress value;
	
	//EMPTY means a zero-length string (useful for validation, we can set validation to allow empty strings)
	//INVALID means it is known that it is not IPV4, IPV6, or EMPTY (if empty is allowed)
	private enum IPType {
		INVALID, IPV4, IPV6, PREFIX, EMPTY
	}
		
	/* the address type */
	private IPType type;
	
	/* if the address is IPV6, whether the last two segments were displayed in IPV4 format like a:b:c:d:e:f:1.2.3.4 */
	private boolean mixed;
	
	/* for mixed IPV4/IPV6 addresses like a:b:c:d:e:f:1.2.3.4, this is the a:b:c:d:e:f part of the address */
	private String ipV6Part;
	
	/* for mixed IPV4/IPV6 addresses like a:b:c:d:e:f:1.2.3.4, this is the 1.2.3.4 part of the address */
	private String mixedPart;
	private IPAddressString mixedAddress;
	
	/* exceptions and booleans for validation */
	private IPAddressException ipV6Exception, ipV4Exception, prefixException;
	private boolean validatedIpv6, validatedIpv4, validatedPrefix;
	
	/* an invalid IP address may in fact be a host name */
	private Host host;

	/**
	 * @param addr the address in string format, either IPV4 like a.b.c.d or IPV6 like a:b:c:d:e:f:g:h or a:b:c:d:e:f:h.i.j.k or a::b or some other valid IPV6 form.
	 * 		IPV6 addresses are also allowed to terminate with a scope id which starts with a % symbol.
	 */
	public IPAddressString(String addr) {
		this(addr, DEFAULT_VALIDATION_OPTIONS);
	}
	
	public IPAddressString(String addr, IPAddressValidationOptions valOptions) {
		if(addr == null) {
			fullAddr = addr = "";
		} else {
			fullAddr = addr;
			addr = addr.trim();
		}
		
		String zone = "";
		
		if(valOptions.allowIPV6Zone) {
			int zoneIndexIndex = addr.indexOf(IPV6Address.ZONE_SEPARATOR);
			if(zoneIndexIndex >= 0) {
				zone = addr.substring(zoneIndexIndex + 1);
				addr = addr.substring(0, zoneIndexIndex);
			}
		}
		String prefixLen = "";
		int prefixLenIndex = addr.indexOf(PREFIX_LEN_SEPARATOR);
		if(prefixLenIndex >= 0) {
			try { //before assuming we have a CIDR prefix, check if we have a URL
				new URL(addr);
				ipV4Exception = ipV6Exception = new IPAddressException(this, "ipaddress.error.url");
				validatedIpv6 = validatedIpv4 = true;
			} catch(MalformedURLException e) {
				if(zone.length() > 0) {
					prefixException = new IPAddressException(this, "ipaddress.error.zoneAndCIDRPrefix");
					validatedPrefix = true;
				} else {
					prefixLen = addr.substring(prefixLenIndex + 1);
					if(prefixLen.trim().length() == 0) {
						prefixException = new IPAddressException(this, "ipaddress.error.invalidCIDRPrefix");
						validatedPrefix = true;
						addr = addr.substring(0, prefixLenIndex);
					} else {
						if(!IPAddressPatterns.isDecimalNumber(prefixLen)) {//negative sign or digits allowed (negative numbers will be invalidated later)
							prefixLen = "";
						} else {
							addr = addr.substring(0, prefixLenIndex);
						}
					}
				}
			}
		}
		this.networkPrefixLength = prefixLen;
		this.zone = zone;
		this.str = addr;
		this.validationOptions = valOptions;
	}
	
	public static class IPAddressValidationOptions implements Cloneable {
		public final boolean allowMixedIPV4V6Mode;
		public final boolean allowLeadingZerosIPV4;
		public final boolean allowEmpty;
		public final boolean allowIPV6Zone;
		public final RangeOptions rangeOptions;
		public final boolean allowPrefixesBeyondAddressSize;
		public final boolean allowWildcardedSeparator;
		
		public final IPAddressValidationOptions mixedOptions;
		
		IPAddressValidationOptions() {
			this(true);
		}
		
		IPAddressValidationOptions(boolean allowEmpty) {
			this(allowEmpty, true, true);
		}
		
		IPAddressValidationOptions(boolean allowEmpty, boolean allowLeadingZerosIPV4, boolean allowMixedIPV4V6Mode) {
			this(allowEmpty, allowLeadingZerosIPV4, allowMixedIPV4V6Mode, RangeOptions.NO_RANGE, false, true);
		}
		
		IPAddressValidationOptions(boolean allowEmpty, boolean allowLeadingZerosIPV4, boolean allowMixedIPV4V6Mode, RangeOptions rangeOptions, boolean allowWildcardedSeparator, boolean allowIPV6Zone) {
			this(allowEmpty, allowLeadingZerosIPV4, allowMixedIPV4V6Mode, allowMixedIPV4V6Mode ? new IPAddressValidationOptions(false, true, false) : null, allowIPV6Zone, rangeOptions, allowWildcardedSeparator);
		}
		
		IPAddressValidationOptions(boolean allowEmpty, boolean allowLeadingZerosIPV4, boolean allowMixedIPV4V6Mode, IPAddressValidationOptions mixedOptions, boolean allowIPV6Zone, RangeOptions rangeOptions, boolean allowWildcardedSeparator) {
			this(allowEmpty, allowLeadingZerosIPV4, allowMixedIPV4V6Mode, mixedOptions, allowIPV6Zone, rangeOptions, allowWildcardedSeparator, true);
		}
		
		IPAddressValidationOptions(boolean allowEmpty, boolean allowLeadingZerosIPV4, boolean allowMixedIPV4V6Mode, IPAddressValidationOptions mixedOptions, boolean allowIPV6Zone, RangeOptions rangeOptions, boolean allowWildcardedSeparator, boolean allowPrefixesBeyondAddressSize) {
			this.allowEmpty = allowEmpty;
			this.allowLeadingZerosIPV4 = allowLeadingZerosIPV4;
			this.allowMixedIPV4V6Mode = allowMixedIPV4V6Mode;
			this.mixedOptions = mixedOptions;
			this.allowIPV6Zone = allowIPV6Zone;
			this.rangeOptions = rangeOptions;
			this.allowWildcardedSeparator = allowWildcardedSeparator;
			this.allowPrefixesBeyondAddressSize = allowPrefixesBeyondAddressSize;
		}
		
		@Override
		public IPAddressValidationOptions clone() {
			try {
				return (IPAddressValidationOptions) super.clone();
			} catch (CloneNotSupportedException e) {}
			return null;
		}
		
		public IPAddressValidationOptions getMixedOptions() {
			if(mixedOptions == null) {
				return this;
			}
			return mixedOptions;
		}
	}
	
	public boolean hasZone() {
		return zone.length() > 0;
	}
	
	/**
	 * @return whether this address represents a network prefix or the set of all addresses with the same network prefix
	 */
	public boolean isNetworkPrefix() {
		return networkPrefixLength.length() > 0;
	}
	
	/*
	 * create an IPV6 segment by joining two IPV4 segments
	 */
	private IPV6AddressSegment createSegment(Integer value1, Integer value2, String string1, String string2, Integer segmentPrefixBits) {
		return new IPV6AddressSegment((value1 << IPV4Address.BITS_PER_SEGMENT) | value2, string1 + IPV4Address.SEGMENT_SEPARATOR + string2, segmentPrefixBits);
	}
	
	/*
	 * create an IPV6 segment by joining two IPV4 segments
	 */
	private IPV6AddressSegment createSegment(String string1, String string2, int upperRangeLower, int upperRangeUpper, int lowerRangeLower, int lowerRangeUpper, Integer segmentPrefixBits) {
		int ipv4BitsPerSegment = IPV4Address.BITS_PER_SEGMENT;
		int lower = (upperRangeLower << ipv4BitsPerSegment) | lowerRangeLower;
		int upper = (upperRangeUpper << ipv4BitsPerSegment) | lowerRangeUpper;
		return new IPV6AddressSegment(string1 + IPV4Address.SEGMENT_SEPARATOR + string2, segmentPrefixBits, lower, upper);
	}
	
	private <T extends IPAddressSegment> T createSegment(IpVersion version, Integer val, String string, Range range, Integer segmentPrefixBits) {
		if(val == null) { //val is null if the segment has a range so it could not be parsed to a value
			if(range != null) {
				return IPAddressSection.cast(version == IpVersion.IPV4 ? new IPV4AddressSegment(string, segmentPrefixBits, range.lower, range.upper) :
					new IPV6AddressSegment(string, segmentPrefixBits, range.lower, range.upper));
			}
			//Note: we currently do not support the case where value is null and string is not a range that we can understand.
			throw new IllegalArgumentException();
		}
		return IPAddressSection.cast(version == IpVersion.IPV4 ? new IPV4AddressSegment(val, string, segmentPrefixBits) :
			new IPV6AddressSegment(val, string, segmentPrefixBits));
	}
	 
	
	public boolean isIpv4() {
		isValid();//validate first
		return type == IPType.IPV4;
	}
	
	public boolean isIpv6() {
		isValid();//validate first
		return type == IPType.IPV6;
	}
	
	public IpVersion getIpVersion() {
		return isIpv4() ? IpVersion.IPV4 : (isIpv6() ? IpVersion.IPV6 : null);
	}
	
	/**
	 * @return whether the address represents a valid IP address.
	 * If it does not, and you want more details, call validate() and examine the thrown exception.
	 */
	public boolean isInvalid() {
		if(type == null) {
			try {
				validate();
			} catch(IPAddressException e) {
				return true;
			}
		}
		return type == IPType.INVALID;
	}
	
	/**
	 * @return whether the address represents a valid IP address.
	 * If it does not, and you want more details, call validate() and examine the thrown exception.
	 */
	public boolean isValid() {
		return !isInvalid();
	}
	
	/**
	 * @return whether the address represents a valid IP address (as opposed to an empty string, a prefix length, or an invalid format).
	 * If it does not, and you want more details, call validate() and examine the thrown exception.
	 */
	public boolean isValidAddress() {
		return !isInvalid() && (type == IPType.IPV4 || type == IPType.IPV6);
	}
	
	/**
	 * Returns true if the address is empty (zero-length).
	 * @return
	 */
	public boolean isEmpty() {
		isValid();//validate first
		return type == IPType.EMPTY;
	}

	public boolean hasSegmentRange() {
		isValid();//validate first
		if(ipArrayRange != null) { 
			for(Range range : ipArrayRange) {
				if(range.isRange()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasSegmentWildcard() {
		isValid();//validate first
		if(ipArrayRange != null) { 
			for(Range range : ipArrayRange) {
				if(range.isWildcard()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @see {@link java.net.InetAddress#isLoopbackAddress()}
	 */
	public boolean isLoopback() {
		return getValue() != null && getValue().isLoopback();
	}
	
	public InetAddress toInetAddress() throws IPAddressException {
		if(getValue() != null) {
			return getValue().toInetAddress();
		}
		return null;
	}
	
	private static class Counter {
		/**
		 * the number of matches ('aaabaa' has three matches of 'aa' at indexes 0, 1 and 4)
		 */
		int count;
		
		/**
		 * the number of matches that were consecutive ('aaabaa' has two matches of 'aa' that were consecutive at index 0 and 1, the third match of 'aa' is not consecutive)
		 */
		int consecutiveCount;
		
		/**
		 * the number of groups of consecutive matches  ('aaabaa' has one group of consecutive matches of 'aa' at index 0) 
		 */
		@SuppressWarnings("unused")
		int consecutiveGroupCount;
		
		/**
		 * whether it starts with a match ('aaabaa' starts with a match of 'aa')
		 */
		boolean starts;
		
		/**
		 * whether it ends with a match ('aaabaa' ends with a match of 'aa')
		 */
		boolean ends;
		
		/**
		 * whether it starts with consecutive matches  ('aaabaa' starts with consecutive matches of 'aa' but not ends)
		 */
		boolean startsConsecutive;
		
		/**
		 * whether it ends with consecutive matches ('aabaaa' ends with consecutive matches of 'aa' but not starts)
		 */
		boolean endsConsecutive;
		
		Counter(String str, String searchStr) {
			int pos;
			boolean multi = true;
			while ((pos = str.indexOf(searchStr)) >= 0) {
				str = str.substring(pos + 1);
				boolean atEnd = str.length() < searchStr.length();
				if(pos == 0) { //this separator occurred right after the last one
					if(count > 0) { //there actually was a last one (otherwise we are at the start)
						if(multi) { //we are starting a new group 
							multi = false;
							if(count == 1 && starts) {
								startsConsecutive = true;
							}
							if(atEnd) {
								endsConsecutive = true;
							}
							consecutiveCount += 2; //we count this separator and the previous one as both being consecutive
							consecutiveGroupCount++;
						} else {
							consecutiveCount++;
						}
					} else {
						starts = true;
					}
				} else if(!multi) {
					multi = true;
				}
				count++;
				if(atEnd) {
					ends = true;
					break;
				}
			}
		}
	}
	
	/** 
	 * Give a string x and a substring separator,
	 * @return instance of Counter for the given string and separator
	 */ 
	private static Counter countMatches(String separator, String sub) {
		return new Counter(sub, separator);
	}
	
	static int countSubstrMatches(String substring, String str) {
		return countMatches(substring, str).count;
	}

	/*
	 * do not call this unless the address has been successfully validated.
	 * Note that subclasses may override and return null for segments that have wildcards or other specific meaning but not specific integer value.
	 */
	Integer getSegment(int index, String segmentStr) {
		if(ipArrayInt == null) {
			ipArrayInt = new Integer[ipArrayStr.length];
			this.cachedArrayInt = new boolean[ipArrayStr.length];
		}
		if(!this.cachedArrayInt[index]) {
			this.cachedArrayInt[index] = true;
			if(segmentStr != null && segmentStr.length() > 0) {
				int radix = (type == IPType.IPV4) ? IPV4_RADIX : IPV6_RADIX;
				try {
					ipArrayInt[index] = Integer.valueOf(segmentStr, radix);
				} catch(NumberFormatException e) { /* the segment contains wildcards */ }
			} else {
				ipArrayInt[index] = 0;
			}
		}
		return ipArrayInt[index];
	}
	
	private void checkSegments() throws IPAddressException {
		for (int i = 0; i < ipArrayStr.length; i++) {
			checkSegment(i, ipArrayStr[i]);
		}
	}
	
	/**
	 * The segment indices here correspond to the original string, not to the address.
	 * For instance, ::* has three segments in the original string:
	 * 	segment 0: ""
	 * 	segment 1: ""
	 *  segment 2: "*"
	 *  
	 *  But of course the IPV6 address has eight segments of value 0, with the segment at index 7 being "*"
	 */
	void checkSegment(int segmentIndex, String segment) throws IPAddressException {
		Integer val = getSegment(segmentIndex, segment);
		Range range = null;
		boolean isIpv4 = (patterns.ipVersion == IpVersion.IPV4);
		if(val == null) {	
			range = patterns.getSegmentRange(segment);
			if(ipArrayRange == null) {
				ipArrayRange = new Range[ipArrayStr.length];
			}
			ipArrayRange[segmentIndex] = range;
		}
		if(range == null) {
			if(isIpv4) {
				if(!validationOptions.allowLeadingZerosIPV4 && segment.length() > 1 && segment.charAt(0) == '0') {
					throw new IPAddressException(this, "ipaddress.error.leadingzero");
				}
			}
		} else {
			if(isIpv4) {
				String front = segment;
				if(!validationOptions.allowLeadingZerosIPV4 && front.length() > 1 && front.charAt(0) == '0') {
					throw new IPAddressException(this, "ipaddress.error.leadingzero");
				}
				int index = front.indexOf(IPAddress.RANGE_SEPARATOR);
				if(index >= 0) {
					String back = front.substring(index + 1);
					if(!validationOptions.allowLeadingZerosIPV4 && back.length() > 1 && back.charAt(0) == '0') {
						throw new IPAddressException(this, "ipaddress.error.leadingzero");
					}
				}
			}
			if(range.lower >= range.upper) {
				throw new IPAddressException(this, "ipaddress.error.invalidRange");
			}
		}
	}
	
	private boolean checkStrForWildcard(String string) {
		return IPAddressPatterns.includesWildcardPattern(false, string);
	}
	
	private int checkIPV6Segments(Counter separatorMatches) throws IPAddressException {
		final int separatorCount = separatorMatches.count;
		int addedSeparators = 0;
		final int hiddenSeparatorsFromMixed = mixed ? (IPV6Address.getMixedReplacedSegments() - 1) : 0;
		final int expectedSeparators = IPV6Address.SEGMENT_COUNT - 1;
		final int doubleSeparatorAtStartOrEnd = (separatorMatches.endsConsecutive || separatorMatches.startsConsecutive) ? 1 : 0;
		final int virtualSeparators = separatorCount + hiddenSeparatorsFromMixed - doubleSeparatorAtStartOrEnd;
		
		boolean hasIpV6PartWildcard, hasWildcard, noWildcardSeparators;
		
		if(mixed) {
			int ipv4Index = str.lastIndexOf(IPV6Address.SEGMENT_SEPARATOR);
			ipV6Part = str.substring(0, ipv4Index);
			mixedPart = str.substring(ipv4Index + 1);
			hasIpV6PartWildcard = checkStrForWildcard(ipV6Part);
			hasWildcard = hasIpV6PartWildcard || checkStrForWildcard(mixedPart);
			
			//this handles the case 1:2:*.3.4 - the wildcard covers both the IPV4 and IPV6 sections
			if(virtualSeparators < expectedSeparators && separatorMatches.consecutiveCount == 0 && this.validationOptions.allowWildcardedSeparator) {
				//we extend the IPV6 part if a wildcard follows the separator and there is no IPV6 compression
				int nextIndex = mixedPart.indexOf(IPV4Address.SEGMENT_SEPARATOR);
				String border = mixedPart.substring(0, nextIndex);
				if(IPAddressPatterns.matchesWildcardPattern(true, border)) {
					ipV6Part += IPV6Address.SEGMENT_SEPARATOR + border;
					hasIpV6PartWildcard = true;
					addedSeparators++;
				}
			}
			noWildcardSeparators = !(this.validationOptions.allowWildcardedSeparator && hasIpV6PartWildcard);
		} else {
			ipV6Part = str;
			hasIpV6PartWildcard = hasWildcard = checkStrForWildcard(ipV6Part);
			noWildcardSeparators = !(this.validationOptions.allowWildcardedSeparator && hasWildcard);
		}
			
		//apparently, ending or starting with '::' counts as just one more segment instead of 2, replacing just a single 0
		//so ::2:3:4:5:6:7:8 or 1:2:3:4:5:6:7:: are both OK 
		//Meanwhile :2:3:4:5:6:7:8 or  1:2:3:4:5:6:7: are not OK 
		//I'd have guessed otherwise on both counts
		
		if(separatorCount + hiddenSeparatorsFromMixed < expectedSeparators && separatorMatches.consecutiveCount == 0  
				&& (!validationOptions.allowEmpty || str.length() > 0) && noWildcardSeparators) {
			throw new IPAddressException(this, "ipaddress.error.too.few.segments");
		}
		if(virtualSeparators > expectedSeparators) { //too many segments, but ::2:3:4:5:6:7:8 or 1:2:3:4:5:6:7:: are both OK
			throw new IPAddressException(this, "ipaddress.error.too.many.segments");
		}
		boolean singleSeparatorAtStart = separatorMatches.starts & !separatorMatches.startsConsecutive;
		if(singleSeparatorAtStart) { //:2:3:4:5:6:7:8 not OK
			throw new IPAddressException(this, "ipaddress.error.cannot.start.with.single.separator");
		}
		boolean singleSeparatorAtEnd = separatorMatches.ends & !separatorMatches.endsConsecutive;
		if(singleSeparatorAtEnd) { //1:2:3:4:5:6:7: not OK
			throw new IPAddressException(this, "ipaddress.error.cannot.end.with.single.separator");
		}
		if(separatorMatches.consecutiveCount > 2 ) { //1::2::3 is not OK.  1:::2:3, while not ambiguous, is not OK, the triple ':::' not allowed - it could be either 1::2:3 or 1:0:0:0:0:0:0:2:3
				//|| matches.consecutiveGroupCount > 1 //ambiguous like 1::2::3, but this test not needed since covered by previous test
			throw new IPAddressException(this, "ipaddress.error.ipv6.ambiguous");
		}
		return separatorCount + addedSeparators;
	}

	private boolean isIpv4Validated() throws IPAddressException {
		if(validatedIpv4) {
			if(ipV4Exception != null) {
				throw ipV4Exception;
			}
			return true;
		}
		return false;
	}
	
	public void validateIpv4() throws IPAddressException {
		if(isIpv4Validated()) {
			return;
		}
		
		synchronized(this) {
			if(isIpv4Validated()) {
				return;
			}
			
			//even if ipv6 validation succeeded, we continue on to generate an appropriate exception for ipv4 that will be cached
			validatedIpv4 = true;
			try {
				boolean hasWildcard = checkStrForWildcard(str);
						
				int ipv4SepCount = countMatches(IPV4Address.SEGMENT_SEPARATOR_STR, str).count;	
				if (ipv4SepCount != IPV4Address.SEGMENT_COUNT - 1  && 
						!(hasWildcard && this.validationOptions.allowWildcardedSeparator && ipv4SepCount < IPV4Address.SEGMENT_COUNT - 1)) {
					if(str.length() > 0) {
						throw new IPAddressException(this, "ipaddress.error.ipv4.format");
					}
					if(networkPrefixLength.length() > 0) {
						type = IPType.PREFIX;
					} else {
						if(!validationOptions.allowEmpty) {
							throw new IPAddressException(this, "ipaddress.error.empty");
						}
						type = IPType.EMPTY; //empty address
					}
				} else {
					IPAddressPatterns patterns = new IPAddressPatterns(ipv4SepCount, validationOptions.rangeOptions);
					ipArrayStr = patterns.match(str);
					if (ipArrayStr == null) {
						throw new IPAddressException(this, "ipaddress.error.ipv4.format");
					}
					this.patterns = patterns;
					type = IPType.IPV4;
					mixed = false;
					checkSegments();
					if(hasZone()) {
						throw new IPAddressException(this, "ipaddress.error.only.ipv6.has.zone");
					}
				}
				validateNetworkPrefix();
			} catch(IPAddressException e) {
				if(validatedIpv6) {
					if(ipV6Exception != null) {
						type = IPType.INVALID;
					} //else type already set when v6 validation succeeded
				}
				ipV4Exception = e;
				throw e;
			}
		}
	}

	public boolean isZero() {
		return getValue() != null && getValue().isZero();
	}
	
	private boolean isIpv6Validated() throws IPAddressException {
		if(validatedIpv6) {
			if(ipV6Exception != null) {
				throw ipV6Exception;
			}
			return true;
		}
		return false;
	}
	
	public void validateIpv6() throws IPAddressException {
		if(isIpv6Validated()) {
			return;
		}
		
		synchronized(this) {
			if(isIpv6Validated()) {
				return;
			}
				
			//even if ipv4 validation succeeded, we continue on here to generate an appropriate exception
			validatedIpv6 = true;
			try {
				int ipV4Separators = countMatches(IPV4Address.SEGMENT_SEPARATOR_STR, str).count;
				Counter matches = countMatches(IPV6Address.SEGMENT_SEPARATOR_STR, str);
				mixed = validationOptions.allowMixedIPV4V6Mode ? matches.count > 0 && ipV4Separators > 0 : false;
				int actualSeparators = checkIPV6Segments(matches);
				if(str.length() > 0) {
					IPAddressPatterns patterns = new IPAddressPatterns(actualSeparators, mixed, validationOptions.rangeOptions);
					ipArrayStr = patterns.match(ipV6Part);
					if (ipArrayStr == null) {
						throw new IPAddressException(this, "ipaddress.error.ipv6.segment.format");
					}
					this.patterns = patterns;
					type = IPType.IPV6;
					if(mixed) {
						mixedAddress = new IPAddressString(mixedPart, validationOptions.getMixedOptions());
						mixedAddress.validateIpv4();
					}
					checkSegments();
				} else {
					if(networkPrefixLength.length() > 0) {
						type = IPType.PREFIX;
					} else {
						if(!validationOptions.allowEmpty ) {
							throw new IPAddressException(this, "ipaddress.error.empty");
						}
						type = IPType.EMPTY;
					}
				}
				validateNetworkPrefix();
			} catch(IPAddressException e) {
				mixed = false; //reset mixed
				if(validatedIpv4) {
					if(ipV4Exception != null) {
						type = IPType.INVALID;
					} //else type already set when v6 validation succeeded
				}
				ipV6Exception = e;
				throw e;
			}
		}
	}
	
	private void validateNetworkPrefix() throws IPAddressException {
		if(validatedPrefix) {
			if(prefixException != null) {
				throw prefixException;
			}
		} else {
			validatedPrefix = true;
			try {
				networkPrefixBits = validateCIDRPrefix(getIpVersion(), networkPrefixLength, validationOptions.allowPrefixesBeyondAddressSize);
			} catch(NumberFormatException e) {
				throw prefixException = new IPAddressException(this, "ipaddress.error.invalidCIDRPrefix");
			}
		}
	}

	public static Integer validateCIDRPrefix(String networkPrefix, boolean allowPrefixesBeyondAddressSize) throws NumberFormatException {
		return validateCIDRPrefix(IpVersion.IPV6, networkPrefix, allowPrefixesBeyondAddressSize);
	}
	
	public static Integer validateCIDRPrefix(IpVersion ipVersion, String networkPrefix, boolean allowPrefixesBeyondAddressSize) throws NumberFormatException {
		if(networkPrefix.length() > 0) {
			int val = Integer.valueOf(networkPrefix);
			int maxSize = ipVersion == null ? IPAddress.bitCount(IpVersion.IPV6) : IPAddress.bitCount(ipVersion);
			if(val >= 0 && (allowPrefixesBeyondAddressSize || val <= maxSize)) {
				return val;
			} else {
				throw new NumberFormatException();
			}
		}
		return null;
	}
	
	private void throwValidationException() throws IPAddressException {
		//if ipv4Exception is null, it is a valid ipv4 address
		//if ipv6Exception is null, it is a valid ipv6 address
		//if neither are null, it is not a valid ipv4 or ipv6 address, and we could throw either exception 
		//but we try to determine the better one to throw
		if(ipV6Exception != null && ipV4Exception != null) {
			if(str.indexOf(IPV6Address.SEGMENT_SEPARATOR) >= 0) {//throw ipv6 exception if it looks ipv6, throw ipv4 exception otherwise
				throw ipV6Exception;
			}
			throw ipV4Exception;
		}
		//if either one is not null, then the address is ok
	}
	
	private boolean isValidated() throws IPAddressException {
		if(validatedIpv6) {
			if(validatedIpv4) {
				throwValidationException();
			} else if(ipV6Exception != null) {
				//we already know it is not a valid ipv6 address, check if it is ipv4
				try {
					validateIpv4();
				} catch(IPAddressException e) {
					throwValidationException();
				}
			} //else it is a valid ipv6 address
			return true;
		} else if(validatedIpv4) {
			if(ipV4Exception != null) {
				//we already know it is not a valid ipv4 address, check if it is ipv6
				try {
					validateIpv6();
				} catch(IPAddressException e) {
					throwValidationException();
				}
			} //else it is a valid ipv4 address
			return true;
		}
		return false;
	}
	
	public void validate() throws IPAddressException {
		if(isValidated()) {
			return;
		}
		synchronized(this) {
			if(isValidated()) {
				return;
			}
		
			//we know nothing about this address.  See what it is.
			validatedIpv4 = validatedIpv6 = true;
			try {
				int ipV4Separators = countMatches(IPV4Address.SEGMENT_SEPARATOR_STR, str).count;
				Counter matches = countMatches(IPV6Address.SEGMENT_SEPARATOR_STR, str);
				int ipV6Separators = matches.count;
				boolean hasNoIpv6Separators = (ipV6Separators == 0);
				if(hasNoIpv6Separators) {	
					mixed = false;
					boolean hasWildcard = checkStrForWildcard(str);
					boolean hasIpv4Separators = (ipV4Separators == IPV4Address.SEGMENT_COUNT - 1) || (ipV4Separators < IPV4Address.SEGMENT_COUNT - 1 && hasWildcard && this.validationOptions.allowWildcardedSeparator);
					if(!hasIpv4Separators) {
						if(str.length() > 0) {
							throw new IPAddressException(this, ipV4Separators > 0 ? "ipaddress.error.ipv4.format" : "ipaddress.error.ip.format");
						}
						if(networkPrefixLength.length() > 0) {
							type = IPType.PREFIX;
						} else {
							if(!validationOptions.allowEmpty ) {
								throw new IPAddressException(this, "ipaddress.error.empty");
							}
							type = IPType.EMPTY;
						}
					} else {
						IPAddressPatterns patterns = new IPAddressPatterns(ipV4Separators, validationOptions.rangeOptions);
						ipArrayStr = patterns.match(str);
						type = IPType.IPV4;
						if (ipArrayStr == null) {
							throw new IPAddressException(this, "ipaddress.error.ipv4.format");
						}
						this.patterns = patterns;
						checkSegments();
						if(hasZone()) {
							throw new IPAddressException(this, "ipaddress.error.only.ipv6.has.zone");
						}
					}
				} else {
					mixed = validationOptions.allowMixedIPV4V6Mode ? ipV4Separators > 0 : false;
					int actualSeparators = checkIPV6Segments(matches);
					IPAddressPatterns patterns = new IPAddressPatterns(actualSeparators, mixed, validationOptions.rangeOptions);
					ipArrayStr = patterns.match(ipV6Part);
					type = IPType.IPV6;
					if (ipArrayStr == null) {
						throw new IPAddressException(this, "ipaddress.error.ipv6.segment.format");
					}
					this.patterns = patterns;
					if(mixed) {
						mixedAddress = new IPAddressString(mixedPart, validationOptions.getMixedOptions());	
						mixedAddress.validateIpv4();
					}
					checkSegments();
				}
				validateNetworkPrefix();
			} catch(IPAddressException e) {
				mixed = false;
				ipV6Exception = ipV4Exception = e;
				type = IPType.INVALID;
				throw e;
			}
		}
	}
	
	@Override
	public int hashCode() {
		IPAddress value = getValue();
		if(value == null) {
			return toString().hashCode();
		}
		return value.hashCode();
	}
	
	public boolean isValidHost() {
		return !isValid() && getHost().isValid();
	}
	
	public IPAddress resolve() {
		IPAddress value = getValue();
		if(value == null) {
			value = getHost().resolve();
		}
		return value;
	}
	
	public Host getHost() {
		if(host == null) {
			synchronized(this) {
				if(host == null) {
					host = new Host(this);
				}
			}
		}
		return host;
	}
	
	@Override
	public int compareTo(IPAddressString other) {
		try {
			IPAddress value = toValue();
			try {
				IPAddress otherValue = other.toValue();
				if(value == null) {
					if(otherValue != null) {
						return -1;
					}
					if(type == IPType.PREFIX) {
						if(other.type != IPType.PREFIX) {
							return 1;
						}
						return other.networkPrefixBits - networkPrefixBits;
					}
					if(other.type == IPType.PREFIX) {
						return -1;
					}
					return toString().compareTo(other.toString());
				} else if(other.value == null) {
					return 1;
				} else {
					return value.compareTo(otherValue);
				}
			} catch(IPAddressException e) {
				return -1;
			}
		} catch(IPAddressException e) {
			try {
				other.toValue();
				return 1;
			} catch(IPAddressException e2) {
				//both are invalid
				//two invalid addresses are equal if they have the same strings
				return toString().compareTo(other.toString());
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof IPAddressString) {
			IPAddressString other = (IPAddressString) o;	
			//if they have the same string, they must be the same,
			//but the converse is not true, if they have different strings, they can
			//still be the same because IPV6 addresses have many representations
			if(toString().equals(other.toString())) {
				return true;
			}
			//this compares segment values
			return compareTo(other) == 0;
		}
		if(o instanceof IPAddress) {
			try {
				IPAddress me = toValue();
				return me.compareTo((IPAddress) o) == 0;
			} catch(IPAddressException e) {
				return false;
			}
		}
		return false;
	}
	
	public IPAddress getValue(IpVersion version) {
		try {
			return toValue(version);
		} catch(IPAddressException e) { /* note that this exception is cached */ }
		return null;
	}
	
	public IPAddress getValue() {
		try {
			return toValue();
		} catch(IPAddressException e) { /* note that this exception is cached */ }
		return null;
	}
	
	private Integer getSegmentPrefixBits(int segmentIndex, int segmentCount, IpVersion version) {
		if(!isNetworkPrefix()) {
			return null;
		}
		Integer segmentPrefixBits = IPAddressSection.getSegmentPrefixBits(IPAddressSection.bitsPerSegment(version), networkPrefixBits, segmentIndex, segmentCount);
		return segmentPrefixBits;
	}
	
	/**
	 * Converts this address of the specified address type.
	 * 
	 * In particular, when this object represents a network prefix length, specifying the address type allows the conversion to take place to the associated mask.
	 * 
	 * If this object is valid but does not represent a specific IPAddress, null is returned.
	 * 
	 * If the string used to construct this object is an invalid format, 
	 * or the format does not match any known valid format for the provided type, then this method throws IPAddressException.
	 * 
	 * @param type the address type that this address should represent.
	 * @return
	 * @throws IPAddressException
	 */
	public IPAddress toValue(IpVersion version) throws IPAddressException {
		toValue();
		if(value == null) {
			if(type == IPType.PREFIX) {
				IPAddressNetwork network = version == IpVersion.IPV4 ? IPV4Address.network : IPV6Address.network;
				return network.getNetworkMask(networkPrefixBits, false);
			}
		} else {
			if(!value.getIpVersion().equals(version)) {
				throw new IPAddressTypeException(this, version, "ipaddress.error.typeMismatch");
			}
		}
		return value;
	}
	
	/**
	 * Converts this object to an IPAddress.  If this object does not represent a specific IPAddress or a ranged IPAddress, null is returned,
	 * which may be the case if this object represents a network prefix or if it represents the empty address string.
	 * 
	 * If the string used to construct this object is not a known format (empty string, address, range of addresses, or prefix) then this method throws IPAddressException.
	 * 
	 * As long as this object represents a valid address (but not necessarily a specific address), this method does not throw.
	 * 
	 */
	public IPAddress toValue() throws IPAddressException {
		validate();//always call validate so that we throw consistently
		if(value != null) {
			return value;
		}
		if(type == IPType.EMPTY || type == IPType.PREFIX) {
			return null;
		}
		if(value == null) {
			synchronized(this) {
				if(value == null) {
					if(isIpv4()) {
						int ipv4SegmentCount = IPV4Address.SEGMENT_COUNT;
						IPV4AddressSegment segments[] = new IPV4AddressSegment[ipv4SegmentCount];
						boolean expandedSegments = false;
						
						for(int i = 0, normalizedSegmentIndex = 0; i < ipArrayStr.length; i++, normalizedSegmentIndex++) {
							String segmentStr = ipArrayStr[i];
							Range range = ipArrayRange != null ? ipArrayRange[i] : null;
							Integer segment = getSegment(i, segmentStr);
							segments[normalizedSegmentIndex] = createSegment(IpVersion.IPV4, segment, segmentStr, range, getSegmentPrefixBits(normalizedSegmentIndex, ipv4SegmentCount, IpVersion.IPV4));
							if(!expandedSegments) {
								//check for any missing segments that we should account for here
								if(range != null && range.isWildcard()) {
									boolean expandSegments = true;
									for(int j = i + 1; j < ipArrayStr.length; j++) {
										Range laterRange = ipArrayRange[j];
										if(laterRange != null && laterRange.isWildcard()) {//another wildcard further down
											expandSegments = false;
											break;
										}
									}
									if(expandSegments) {
										expandedSegments = true;
										int count = missingSegmentCount =  ipv4SegmentCount - ipArrayStr.length;
										while(count-- > 0) { //add the missing segments
											++normalizedSegmentIndex;
											segments[normalizedSegmentIndex] = createSegment(IpVersion.IPV4, null, segmentStr, range, getSegmentPrefixBits(normalizedSegmentIndex, ipv4SegmentCount, IpVersion.IPV4));
										}
									}
								}
							}
						}
						value = new IPV4Address(segments);
					} else { //ipv6
						int ipv6SegmentCount = IPV6Address.SEGMENT_COUNT;
						IPV6AddressSegment segments[] = new IPV6AddressSegment[ipv6SegmentCount];
						int normalizedSegmentIndex = 0;
						int lastIndex = ipArrayStr.length - 1;
						String first = ipArrayStr[0];
						String last = ipArrayStr[lastIndex];
						boolean hasExtraFirstSegment = (first.length() == 0);
						int startIndex = hasExtraFirstSegment ? 1 : 0;
						boolean hasExtraLastSegment = !mixed && (last.length() == 0);
						if(hasExtraLastSegment) {
							lastIndex--;
						}
						boolean expandedSegments = false;
						
						//get the segments for IPV6
						for(int i = startIndex; i <= lastIndex; i++) {
							String segmentStr = ipArrayStr[i];
							Range range = ipArrayRange != null ? ipArrayRange[i] : null;
							Integer segmentInt = getSegment(i, segmentStr);
							segments[normalizedSegmentIndex] = createSegment(IpVersion.IPV6, segmentInt, segmentStr, range, getSegmentPrefixBits(normalizedSegmentIndex, ipv6SegmentCount, IpVersion.IPV6));
							normalizedSegmentIndex++;
							Integer expandValue = null;
							if(!expandedSegments) {
								//check for any missing segments that we should account for here
								boolean expandSegments = false;
								if(range != null && range.isWildcard()) {
									expandValue = null;
									expandSegments = true;
									for(int j = i + 1; j <= lastIndex; j++) {
										Range laterRange = ipArrayRange[j];
										if(laterRange != null && laterRange.isWildcard()) {//another wildcard further down
											expandSegments = false;
											break;
										} else {
											String laterSegmentStr = ipArrayStr[j];
											if(laterSegmentStr.length() == 0) {//a compressed segment further down
												expandSegments = false;
												break;
											}
										}
									}
								} else {
									//compressed ipv6?
									if(segmentStr.length() == 0) {
										expandSegments = true;
										expandValue = 0;
									}
								}
								
								//fill in missing segments
								if(expandSegments) {
									expandedSegments = true;
									int totalSegments = lastIndex - startIndex + 1;
									if(mixed) {
										missingSegmentCount = IPV6Address.getMixedOriginalSegments() - totalSegments;
									} else {
										missingSegmentCount = IPV6Address.SEGMENT_COUNT - totalSegments;
									}
									int count = missingSegmentCount;
									while(count-- > 0) { //add the missing segments
										segments[normalizedSegmentIndex] = createSegment(IpVersion.IPV6, expandValue, segmentStr, range, getSegmentPrefixBits(normalizedSegmentIndex, ipv6SegmentCount, IpVersion.IPV6));
										normalizedSegmentIndex++;
									}
								}
							}
						}
						
						if(mixed) {
							IPAddressSegment ipv4Segs[] = mixedAddress.toValue().getSegments();
							for(int n = 0; n < 4; n += 2) {
								IPAddressSegment one = ipv4Segs[n];
								IPAddressSegment two = ipv4Segs[n + 1];
								if(!one.isRange() && !two.isRange()) {
									segments[normalizedSegmentIndex] = createSegment(one.getLowerValue(), two.getLowerValue(), one.originalString, two.originalString, getSegmentPrefixBits(normalizedSegmentIndex, ipv6SegmentCount, IpVersion.IPV6));
								} else {
									segments[normalizedSegmentIndex] = createSegment(one.originalString, two.originalString, one.getLowerValue(), one.getUpperValue(), two.getLowerValue(), two.getUpperValue(), getSegmentPrefixBits(normalizedSegmentIndex, ipv6SegmentCount, IpVersion.IPV6));
								}
								normalizedSegmentIndex++;
							}
						}
						value = new IPV6Address(segments, mixed, zone);
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * toString() gives us the original string.  For variations, call getValue and then use other string methods on the address object.
	 */
	@Override
	public String toString() {
		return fullAddr;
	}
	
	/**
	 * Converts this address to a prefix length
	 * 
	 * @return the prefix of the indicated IP type represented by this address or null if this address is valid but cannot be represented by a network prefix length
	 * @throws IPAddressException if the address is invalid
	 */
	public String convertToPrefixLength() throws IPAddressException {
		IPAddress address = toValue();
		Integer prefix = (address == null) ? ((type == IPType.PREFIX) ? networkPrefixBits : null) : address.getCIDRMaskPrefixLength(true);
		if(prefix != null) {
			StringBuilder builder = new StringBuilder(4);
			return builder.append(PREFIX_LEN_SEPARATOR).append(prefix).toString();
		}
		return null;
	}
	
	/**
	 * Converts this address to a mask
	 * 
	 * @param version the ip type
	 * @return the mask of the indicated IP type represented by this address or null
	 * @throws IPAddressException if the address does not match the indicated type
	 */
	public String convertToMask(IpVersion version) throws IPAddressException {
		IPAddress address = toValue(version);
		if(address != null) {
			return address.toNormalizedString();
		}
		return null;
	}
}
