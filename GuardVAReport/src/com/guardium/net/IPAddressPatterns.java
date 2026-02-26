/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guardium.net.IPAddress.IpVersion;
import com.guardium.net.IPAddress.RangeOptions;

/**
 * 
 * @author sfoley
 *
 */
public class IPAddressPatterns {
	
	private final static String ipv4SegmentCapture;
	private final static String ipv6SegmentCapture;
	
	private final static String ipV4RegexSeparator = "\\" + IPV4Address.SEGMENT_SEPARATOR;
	private final static char ipV6RegexSeparator = IPV6Address.SEGMENT_SEPARATOR;
	
	private final static String ipv4WildcardSegmentCapture;
	private final static String ipv4RangeOrWildcardSegmentCapture;
	
	private final static String ipv6WildcardSegmentCapture;
	private final static String ipv6RangeOrWildcardSegmentCapture;
	
	private final static Pattern ipv6RangePattern;
	private final static Pattern ipv4RangePattern;
	private final static Pattern wildcardStrPattern;
	private final static Pattern wildcardPattern;
	
	static  {
		String wildcardRegex = "\\" + IPAddress.SEGMENT_WILDCARD;
		char otherWildcardRegex = IPAddress.EQUIVALENT_SEGMENT_WILDCARD;
		String rangeRegex = "\\" + IPAddress.RANGE_SEPARATOR;
		
		String ipv4Segment = "\\d?\\d|[01]\\d\\d|2[0-4]\\d|25[0-5]";
		String ipv6Segment = "[\\da-fA-F]?[\\da-fA-F]?[\\da-fA-F]?[\\da-fA-F]?";
		ipv4SegmentCapture = '(' + ipv4Segment + ')';
		ipv6SegmentCapture = '(' + ipv6Segment + ')';
		
		String ipv4SegmentGrouped = "(?:" + ipv4Segment + ')';
		String ipv4Range = ipv4SegmentGrouped + rangeRegex + ipv4SegmentGrouped;
		ipv4RangeOrWildcardSegmentCapture = '(' + ipv4SegmentGrouped + '|' + ipv4Range + '|' + wildcardRegex + '|' + otherWildcardRegex + ')';
		
		String ipv6Range = ipv6Segment + rangeRegex + ipv6Segment;
		ipv6RangeOrWildcardSegmentCapture = '(' + ipv6Segment + '|' + ipv6Range + '|' + wildcardRegex + '|' + otherWildcardRegex + ')';
		ipv4WildcardSegmentCapture = '(' + ipv4SegmentGrouped + '|' + wildcardRegex + '|' + otherWildcardRegex + ')';
		
		ipv6WildcardSegmentCapture = '(' + ipv6Segment + '|' + wildcardRegex + '|' + otherWildcardRegex + ')';
		ipv4RangePattern = Pattern.compile("^(" + ipv4Segment + ')' + rangeRegex + '(' + ipv4Segment + ")$");
		ipv6RangePattern = Pattern.compile("^(" + ipv6Segment + ')' + rangeRegex + '(' + ipv6Segment + ")$");
		String wildCards = "(" + wildcardRegex + '|' + otherWildcardRegex + ")";
		wildcardPattern = Pattern.compile(wildCards);
		wildcardStrPattern = Pattern.compile("^" + wildCards + "$");
	}
	
	private final static Pattern decimalNumberPattern = Pattern.compile("^[\\-\\+]{0,1}[0-9]+$");
	
	static class IPV6PatternPair {
		private static int maxSeparatorCount = IPV6Address.SEGMENT_COUNT + 1;
		private static int maxMixedSeparatorCount;
		
		static {
			int mixedReplacedSegs = IPV6Address.getMixedReplacedSegments();
			maxMixedSeparatorCount = maxSeparatorCount - (mixedReplacedSegs - 1);
		}
		
		final Pattern cache[] = new Pattern[maxSeparatorCount];
		final Pattern mixedCache[]= new Pattern[maxMixedSeparatorCount];
	}
	
	private final static IPV6PatternPair IPV6_REGULAR_CACHE = new IPV6PatternPair();
	private final static IPV6PatternPair IPV6_WILDCARD_ONLY_CACHE = new IPV6PatternPair();
	private final static IPV6PatternPair IPV6_WILDCARD_AND_RANGE_CACHE = new IPV6PatternPair();
	
	private final static Pattern IPV4_REGULAR_CACHE[] = new Pattern[IPV4Address.SEGMENT_COUNT];
	private final static Pattern IPV4_WILDCARD_ONLY_CACHE[] = new Pattern[IPV4Address.SEGMENT_COUNT];
	private final static Pattern IPV4_WILDCARD_AND_RANGE_CACHE[] = new Pattern[IPV4Address.SEGMENT_COUNT];
	
	private final RangeOptions rangeOptions;
	private final boolean mixed;
	private final int separators;
	final IpVersion ipVersion;
	
	/**
	 * constructor for IPV4 addresses
	 * 
	 * @param rangeOptions
	 */
	IPAddressPatterns(int separatorCount, RangeOptions rangeOptions) {
		this(IpVersion.IPV4, separatorCount, false, rangeOptions);
	}
	
	/**
	 * constructor for IPV6 addresses
	 * 
	 * @param rangeOptions
	 */
	IPAddressPatterns(int separatorCount, boolean mixed, RangeOptions rangeOptions) {
		this(IpVersion.IPV6, separatorCount, mixed, rangeOptions);
	}
	
	private IPAddressPatterns(IpVersion version, int separatorCount, boolean mixed, RangeOptions rangeOptions) {
		this.ipVersion = version;
		this.separators = separatorCount;
		this.mixed = mixed;
		this.rangeOptions = rangeOptions;
	}
	
	public static Pattern getRangePattern(IpVersion version) {
		return version == IpVersion.IPV4 ? ipv4RangePattern : ipv6RangePattern;
	}
	
	public static Pattern getWildcardPattern(boolean entire) {
		return entire ? wildcardStrPattern : wildcardPattern;
	}
	
	public static boolean matchesWildcardPattern(boolean entire, String str) {
		Pattern p = entire ? wildcardStrPattern : wildcardPattern;
		return p.matcher(str).matches();
	}
	
	public static boolean includesWildcardPattern(boolean entire, String str) {
		Pattern p = entire ? wildcardStrPattern : wildcardPattern;
		return p.matcher(str).find();
	}
	
	public static String provideSegmentPatternStr(IpVersion version, RangeOptions rangeOptions) {
		if(version == IpVersion.IPV4) {
			switch(rangeOptions) {
				case WILDCARD_ONLY:
					return ipv4WildcardSegmentCapture;
				case WILDCARD_AND_RANGE:
					return ipv4RangeOrWildcardSegmentCapture;
				case NO_RANGE:
				default:
					return ipv4SegmentCapture;
			}		
		} else {
			switch(rangeOptions) {
				case WILDCARD_ONLY:
					return ipv6WildcardSegmentCapture;
				case WILDCARD_AND_RANGE:
					return ipv6RangeOrWildcardSegmentCapture;
				case NO_RANGE:
				default:
					return ipv6SegmentCapture;
			}
		}
	}
	
	public static String[] matchPattern(Pattern pattern, String str) {
		Matcher matcher = pattern.matcher(str);
		if(matcher.matches()) {
			String groups[] = new String[matcher.groupCount()];
			for(int i = 1; i <= matcher.groupCount(); i++) {
				groups[i - 1] = matcher.group(i);
			}
			return groups;
		}
		return null;
	}
	
	public String[] match(String str) {
		Pattern pattern = ipVersion == IpVersion.IPV4 ?  provideIPV4Pattern() : provideIPV6Pattern();
		return matchPattern(pattern, str);
	}
	
	public static boolean isDecimalNumber(String s) {
		return decimalNumberPattern.matcher(s).matches();
	}
	
	private Pattern provideIPV4Pattern() {
		Pattern cache[];
		switch(rangeOptions) {
			case WILDCARD_ONLY:
				cache = IPV4_WILDCARD_ONLY_CACHE;
				break;
			case WILDCARD_AND_RANGE:
				cache = IPV4_WILDCARD_AND_RANGE_CACHE;
				break;
			case NO_RANGE:
			default:
				cache = IPV4_REGULAR_CACHE;
				break;
		}
		int cacheIndex = separators;
		Pattern result = cache[cacheIndex];
		if(result == null) {
			result = cache[cacheIndex] = compileIPV4Pattern();
		}
		return result;
	}
	
	private Pattern compileIPV4Pattern() {
		String ipPattern = constructPattern(separators, IpVersion.IPV4);
		ipPattern = '^' + ipPattern + '$';
		return Pattern.compile(ipPattern);
	}
	
	private Pattern provideIPV6Pattern() {
		IPV6PatternPair cachePair;
		switch(rangeOptions) {
			case WILDCARD_ONLY:
				cachePair = IPV6_WILDCARD_ONLY_CACHE;
				break;
			case WILDCARD_AND_RANGE:
				cachePair = IPV6_WILDCARD_AND_RANGE_CACHE;
				break;
			case NO_RANGE:
			default:
				cachePair = IPV6_REGULAR_CACHE;
				break;
		}
		Pattern[] cache = mixed ? cachePair.mixedCache : cachePair.cache; //TODO can probably do away with the mixed distinction here and then be rid of the mixed variable
		int cacheIndex = separators;
		Pattern result = cache[cacheIndex];
		if(result == null) {
			result = cache[cacheIndex] = compileIPV6Pattern();
		}
		return result;
	}
	
	private Pattern compileIPV6Pattern() {
		String ipPattern = constructPattern(separators - (mixed ? (IPV6Address.getMixedReplacedSegments() - 1) : 0), IpVersion.IPV6);
		ipPattern = '^' + ipPattern + '$';
		return Pattern.compile(ipPattern);
	}
	
	private String constructPattern(int separators, IpVersion version) {
		String ipPattern = provideSegmentPatternStr(version, rangeOptions);
		String additionalSection = (version == IpVersion.IPV4 ? ipV4RegexSeparator : ipV6RegexSeparator) + ipPattern;
		for(int i=0; i<separators; i++) {
			ipPattern = ipPattern + additionalSection;
		}
		return ipPattern;
	}

	class Range {
		final Pattern pattern;
		final int lower;
		final int upper;
		
		Range(Pattern pattern, int lower, int upper) {
			this.pattern = pattern;
			this.lower = lower;
			this.upper = upper;
		}

		public boolean isRange() {
			return pattern.equals(getRangePattern(ipVersion));
		}
		
		public boolean isWildcard() {
			return pattern.equals(getWildcardPattern(true));
		}
	}
	
	public Range getSegmentRange(String string) {
		Range result = null;
		try {
			Pattern rangePattern = getRangePattern(ipVersion);
			String range[] = matchPattern(rangePattern, string);
			if(range != null) {
				int lower;
				int upper;
				if(ipVersion == IpVersion.IPV4) {
					lower = Integer.parseInt(range[0], IPAddressString.IPV4_RADIX);
					upper = Integer.parseInt(range[1], IPAddressString.IPV4_RADIX);
				} else {
					lower = Integer.parseInt(range[0], IPAddressString.IPV6_RADIX);
					upper = Integer.parseInt(range[1], IPAddressString.IPV6_RADIX);
				}	
				result = new Range(rangePattern, lower, upper);
			} else {
				rangePattern = getWildcardPattern(true);
				range = matchPattern(rangePattern, string);
				if(range != null) {
					result = new Range(rangePattern, 0, IPAddressSegment.getMaxSegmentValue(ipVersion));
				}
			}
		} catch(NumberFormatException e) {
			//should never happen
			//but if it does, we will treat it as if there was no range 
		}
		return result;
	}
}
