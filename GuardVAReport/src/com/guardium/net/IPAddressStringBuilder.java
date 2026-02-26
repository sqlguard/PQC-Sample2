/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.util.ArrayList;
import java.util.Arrays;


abstract class IPAddressStringBuilder <T extends IPAddressSection, O extends IPStringBuilderOptions> {
	final StringBuilder builder = new StringBuilder();
	final ArrayList<String> strings = new ArrayList<String>();
	final T address;
	final O options;
	private int expandableSegs[];
	
	IPAddressStringBuilder(T address, O options) {
		this.address = address;
		this.options = options;
	}
	
	abstract String[] getStrings();
	
	void addString(IPStringParams<T> stringParams) {
		builder.setLength(0);
		stringParams.append(builder, address);
		String str = builder.toString();
		strings.add(str);
	}
	
	boolean isExpandable() {
		IPAddressSegment segments[] = address.getSegments();
		return isExpandable(segments, segments.length);
	}
	
	boolean isExpandableOutsideRange(int segmentIndex, int count, int segmentCount) {
		return isExpandableOutsideRange(address.getSegments(), segmentIndex, count, segmentCount);
	}

	static boolean isExpandable(IPAddressSegment segs[], int segmentCount) {
		return isExpandableOutsideRange(segs, -1, 0, segmentCount);
	}
	
	private static boolean isExpandableOutsideRange(IPAddressSegment segs[], int segmentIndex, int count, int segmentCount) {
		int nextSegmentIndex = segmentIndex + count;
		for(int i=0; i<segmentCount; i++) {
			if(i >= segmentIndex && i < nextSegmentIndex) {
				continue;
			}
			IPAddressSegment seg = segs[i];
			if(seg.isCharPrefixable()) {
				return true;
			}
		}
		return false;
	}
	
	int[] getExpandableSegments() {
		if(expandableSegs == null) {
			expandableSegs = getExpandableSegments(address.getSegments());
		}
		return expandableSegs;
	}
	
	static int[] getExpandableSegments(IPAddressSegment segs[]) {
		int expandables[] = new int[segs.length];
		for(int i=0; i<segs.length; i++) {
			expandables[i] = segs[i].getMaxCharPrefixLength();
		}
		return expandables;
	}
}

/**
 * Each IPStringParams has settings to write exactly one IP address string.
 * 
 * @author sfoley
 */
abstract class IPStringParams<T extends IPAddressSection> implements Cloneable {
	boolean makeWildcards;
	boolean expandSegments;
	int expandSegment[];
			
	IPStringParams(boolean expandSegments) {
		this.expandSegments = expandSegments;
	}
	
	abstract StringBuilder append(StringBuilder builder, T addr);
	
	abstract String toString(T addr);
	
	abstract void resetExpansions(int segmentCount);
	
	String getCharPrefix(IPAddressSegment seg, int i) {
		if(expandSegments) {
			return seg.getCharPrefix();
		} else if(expandSegment != null) {
			int expansion = expandSegment[i];
			if(expansion > 0) {
				return seg.getCharPrefix(expansion);
			}
		}
		return "";
	}
	
	@Override
	public IPStringParams<T> clone() {
		IPStringParams<T> params = null;
		try {
			params = IPAddressSection.cast(super.clone());
			if(expandSegment != null) {
				params.expandSegment = expandSegment.clone();
			}
		} catch(CloneNotSupportedException e) {}
		return params;
	}
}

/**
 * Each IPV4StringParams has settings to write exactly one IPV4 address string according to the settings.
 * 
 * @author sfoley
 *
 */
class IPV4StringParams extends IPStringParams<IPV4AddressSection> {
	IPV4StringParams(boolean expandSegments) {
		super(expandSegments);
	}
	
	@Override
	public StringBuilder append(StringBuilder builder, IPV4AddressSection addr) {
		IPV4AddressSegment segs[] = addr.getSegments();
		append(builder, segs);
		if(addr.isNetworkPrefix() && !makeWildcards) {
			builder.append(IPAddressString.PREFIX_LEN_SEPARATOR).append(addr.getNetworkPrefixBits());
		}
		return builder;
	}

	public void append(StringBuilder builder, IPV4AddressSegment[] segs) {
		for(int i = 0; i < segs.length; i++) {
			IPV4AddressSegment seg = segs[i];
			String segStr, prefix;
			if(makeWildcards) {
				segStr = seg.getWildcardString();
				prefix = "";
			} else {
				segStr = seg.getString();
				prefix = getCharPrefix(seg, i);
			}
			builder.append(prefix).append(segStr).append(IPV4Address.SEGMENT_SEPARATOR);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
	}
	
	@Override
	public String toString(IPV4AddressSection addr) {
		return append(new StringBuilder(IPV6Address.MAX_STRING_LEN), addr).toString();
	}
	
	@Override
	public IPV4StringParams clone() {
		return (IPV4StringParams) super.clone();
	}
	
	@Override
	void resetExpansions(int segmentCount) {
		if(expandSegment == null) {
			expandSegment = new int[segmentCount];
		} else {
			Arrays.fill(expandSegment, 0);
		}
	}
}

/**
 * Capable of building any and all possible representations of IP V4 addresses.
 * Not all such representations are necessarily something you might consider valid.
 * For example: 001.02.3.04
 * This string has the number '2' and '4' expanded partially to 02 (a partial expansion), rather than left as is, or expanded to the full 3 chars 002.
 * The number '1' is fully expanded to 3 characters.
 * 
 * With the default settings of this class, a single address can have 16 variations.  If partial expansions are allowed, there are many more.
 * 
 * @author sfoley
 */
class IPV4StringBuilder extends IPAddressStringBuilder<IPV4AddressSection, IPStringBuilderOptions> {
	
	IPV4StringBuilder(IPV4AddressSection address, IPStringBuilderOptions options) {
		super(address, options);
	}

	@Override
	String[] getStrings() {
		ArrayList<IPV4StringParams> allParams = new ArrayList<IPV4StringParams>();
		IPV4StringParams stringParams = new IPV4StringParams(false);
		allParams.add(stringParams);
		if(options.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_SOME_SEGMENTS)) {
			stringParams.resetExpansions(address.getSegmentCount());
			int expandables[] = getExpandableSegments();
			for(int i=0; i< address.getSegmentCount(); i++) {
				int expansionLength = expandables[i];
				int len = allParams.size();
				while(expansionLength > 0) {
					for(int j=0; j<len; j++) {
						IPV4StringParams clone = allParams.get(j);
						clone = clone.clone();
						clone.expandSegment[i] = expansionLength;
						allParams.add(clone);
					}
					if(!options.includes(IPStringBuilderOptions.LEADING_ZEROS_PARTIAL_SOME_SEGMENTS)) {
						break;
					}
					expansionLength--;
				}
			}
		} else if(options.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_ALL_SEGMENTS)) {
			boolean allExpandable = isExpandable();
			if(allExpandable) {
				allParams.add(new IPV4StringParams(true));
			}
		}
		for(int i=0; i<allParams.size(); i++) {
			IPV4StringParams param = allParams.get(i);
			addString(param);
		}
		return strings.toArray(new String[strings.size()]);
	}
}

/**
 * Each IPV6StringParams has settings to write exactly one IPV6 address string according to the settings.
 * 
 * @author sfoley
 *
 */
class IPV6StringParams extends IPStringParams<IPV6AddressSection> {
	int firstCompressedSegmentIndex;
	int nextUncompressedIndex;
	
	boolean wildcardsCompressed;
	
	boolean createMixed;
	IPV4StringParams mixedParams;
	
	boolean uppercase;
	
	IPV6StringParams() {
		this(false);
	}
	
	IPV6StringParams(boolean createMixed) {
		this(createMixed, false, false);
	}
	
	IPV6StringParams(boolean createMixed, int firstCompressedSegmentIndex, int compressedCount) {
		this(createMixed, false, false, firstCompressedSegmentIndex, compressedCount);
	}
	
	IPV6StringParams(boolean createMixed, boolean expandSegments) {
		this(createMixed, expandSegments, false);
	}
	
	IPV6StringParams(boolean createMixed, boolean expandSegments, boolean expandMixedSegments) {
		this(createMixed, expandSegments, expandMixedSegments, -1, 0);
	}
	
	IPV6StringParams(boolean createMixed, boolean expandSegments, boolean expandMixedSegments, int firstCompressedSegmentIndex, int compressedCount) {
		this(createMixed, expandSegments, expandMixedSegments, firstCompressedSegmentIndex, compressedCount, false);
	}
	
	IPV6StringParams(boolean createMixed, boolean expandSegments, boolean expandMixedSegments, int firstCompressedSegmentIndex, int compressedCount, boolean uppercase) {
		super(expandSegments);
		this.createMixed = createMixed;
		this.firstCompressedSegmentIndex = firstCompressedSegmentIndex;
		this.nextUncompressedIndex = firstCompressedSegmentIndex + compressedCount;
		this.mixedParams = new IPV4StringParams(expandMixedSegments);
		this.uppercase = uppercase;
	}
	
	@Override
	public StringBuilder append(StringBuilder builder, IPV6AddressSection addr) {
		int lastIPV6Index = addr.getSegmentCount() - (createMixed ? addr.getMixedIPV6SegmentCount() : 0) - 1;
		IPV6AddressSegment segs[] = addr.getSegments();
		char separator = IPV6Address.SEGMENT_SEPARATOR;
		for(int i=0; i<=lastIPV6Index; i++) {
			if(i < firstCompressedSegmentIndex || i >= nextUncompressedIndex) {
				IPV6AddressSegment seg = segs[i];
				String segStr, prefix;
				if(makeWildcards) {
					segStr = seg.getWildcardString();
					prefix = "";
				} else {
					segStr = seg.getString();
					prefix = getCharPrefix(seg, i);
				}
				if(uppercase) {
					segStr = segStr.toUpperCase();
				}
				builder.append(prefix).append(segStr).append(separator);
			} else if(i == firstCompressedSegmentIndex) {
				builder.append(separator);
				if(i == 0) {
					builder.append(separator);
				}
			}
		}
		if(createMixed) {
			mixedParams.append(builder, addr.getMixedIPV4Segments());
		} else if(nextUncompressedIndex <= lastIPV6Index) {//delete the extra separator at the end
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
		}
		if(addr.isNetworkPrefix()) {
			if(!makeWildcards || wildcardsCompressed) {
				builder.append(IPAddressString.PREFIX_LEN_SEPARATOR).append(addr.getNetworkPrefixBits());
			}
		} 
		return builder;
	}
	
	@Override
	public String toString(IPV6AddressSection addr) {
		return append(new StringBuilder(IPV6Address.MAX_STRING_LEN), addr).toString();
	}
	
	@Override
	public IPV6StringParams clone() {
		IPV6StringParams params = (IPV6StringParams) super.clone();
		mixedParams = mixedParams.clone();
		return params;
	}
	
	@Override
	void resetExpansions(int segmentCount) {
		if(expandSegment == null) {
			expandSegment = new int[segmentCount];
		} else {
			Arrays.fill(expandSegment, 0);
		}
	}
}

/**
 * Capable of building any and all possible representations of IP V6 addresses.
 * Not all such representations are necessarily something you might consider valid.
 * For example: a:0::b:0c:d:001.02.3.04
 * This string has a single zero segment compressed rather than two consecutive (a partial compression),
 * it has the number 'c' expanded partially to 0c (a partial expansion), rather than left as is, or expanded to the full 4 chars 000c,
 * it has mixed representation (the last two segments written as IPV4), and in the mixed part it has IPV4 segments that are partially expanded.
 * 
 * The one type of variation not produced by this class are mixed case, containing both upper and lower case characters: A-F vs a-f.
 * That would result in gazillions of possible representations.  
 * But such variations are easy to work with for comparison purposes because you can easily convert strings to lowercase,
 * so in general there is no need to cover such variations.
 * However, this does provide the option to have either all uppercase or all lowercase strings.
 * 
 * A single address can have hundreds of thousands, even millions, of possible variations.
 * The default settings for this class will produce at most a couple thousand possible variations.
 * 
 * @author sfoley
 */
class IPV6StringBuilder extends IPAddressStringBuilder<IPV6AddressSection, IPV6StringBuilderOptions> {
	private int mixedExpandableSegs[];
	
	IPV6StringBuilder(IPV6AddressSection address, IPStringBuilderOptions opts) {
		super(address, (opts instanceof IPV6StringBuilderOptions) ? ((IPV6StringBuilderOptions) opts) : new IPV6StringBuilderOptions(opts.options));
	}
	
	int[] getMixedExpandableSegments() {
		if(mixedExpandableSegs == null) {
			mixedExpandableSegs = getExpandableSegments(address.getMixedIPV4Segments());
		}
		return mixedExpandableSegs;
	}
	
	private void addUppercaseVariations(ArrayList<IPV6StringParams> allParams, boolean mixed) {
		if(options.includes(IPV6StringBuilderOptions.UPPERCASE) && address.hasAlphabeticDigits(mixed)) {
			int len = allParams.size();
			for(int j=0; j<len; j++) {
				IPV6StringParams clone = allParams.get(j);
				clone = clone.clone();
				clone.uppercase = true;
				allParams.add(clone);
			}
		}
	}
	
	private void addAllExpansions(boolean mixed, int firstCompressedIndex, int count, int segmentCount) {
		IPV6StringParams stringParams = new IPV6StringParams(mixed, firstCompressedIndex, count);

		ArrayList<IPV6StringParams> allParams = new ArrayList<IPV6StringParams>();
		allParams.add(stringParams);
		
		//we need to do this here before we start cloning stringParams
		boolean addMixedExpansions = options.mixedOptions.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_SOME_SEGMENTS);
		if(mixed && addMixedExpansions) {
			stringParams.mixedParams.resetExpansions(address.getMixedIPV4Segments().length);
		}
		
		if(options.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_SOME_SEGMENTS)) {
			stringParams.resetExpansions(address.getSegmentCount());
			int expandables[] = getExpandableSegments();
			int nextUncompressedIndex = firstCompressedIndex + count;
			int ipv6SegmentEnd = address.getSegmentCount() - (mixed ? address.getMixedIPV6SegmentCount() : 0);
			for(int i=0; i < ipv6SegmentEnd; i++) {
				if(i < firstCompressedIndex || i >= nextUncompressedIndex) {
					int expansionLength = expandables[i];
					int len = allParams.size();
					while(expansionLength > 0) {		
						for(int j=0; j<len; j++) {
							IPV6StringParams clone = allParams.get(j);
							clone = clone.clone();
							clone.expandSegment[i] = expansionLength;
							allParams.add(clone);
						}
						if(!options.includes(IPStringBuilderOptions.LEADING_ZEROS_PARTIAL_SOME_SEGMENTS)) {
							break;
						}
						expansionLength--;
					}
				}
			}
		} else if(options.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_ALL_SEGMENTS)) {
			boolean isExpandable = isExpandableOutsideRange(firstCompressedIndex, count, segmentCount);
			if(isExpandable) {
				int len = allParams.size();
				for(int j=0; j<len; j++) {
					IPV6StringParams clone = allParams.get(j);
					clone = clone.clone();
					clone.expandSegments = true;
					allParams.add(clone);
				}
			}
		}
		if(mixed) {
			if(addMixedExpansions) {
				int expandables[] = getMixedExpandableSegments();
				IPV4AddressSegment[] segs = address.getMixedIPV4Segments();
				for(int i=0; i < segs.length; i++) {
					int expansionLength = expandables[i];
					int len = allParams.size();
					while(expansionLength > 0) {
						for(int j=0; j<len; j++) {
							IPV6StringParams clone = allParams.get(j);
							clone = clone.clone();
							clone.mixedParams.expandSegment[i] = expansionLength;
							allParams.add(clone);
						}
						if(!options.mixedOptions.includes(IPStringBuilderOptions.LEADING_ZEROS_PARTIAL_SOME_SEGMENTS)) {
							break;
						}
						expansionLength--;
					}
				}
		    } else if(options.mixedOptions.includes(IPStringBuilderOptions.LEADING_ZEROS_FULL_ALL_SEGMENTS)) {
		    	IPV4AddressSegment[] segs = address.getMixedIPV4Segments();
		    	boolean isExpandable = isExpandable(segs, segs.length);
				if(isExpandable) {
			    	int len = allParams.size();
					for(int j=0; j<len; j++) {
						IPV6StringParams clone = allParams.get(j);
						clone = clone.clone();
						clone.mixedParams.expandSegments = true;
						allParams.add(clone);
					}
				}
		    }
		}
		
		if(mixed && address.isSameAsMixed(false)) {
			//the original is a duplicate, but any expanding we have done is not a duplicate
			allParams.remove(0);
		}
		
		addUppercaseVariations(allParams, mixed);
		
		for(int i=0; i<allParams.size(); i++) {
			IPV6StringParams param = allParams.get(i);
			addString(param);
		}	
	}

	private void addAllCompressedStrings(boolean mixed, int zeroStartIndex, int count, boolean partial, int segmentCount) {
		int end = zeroStartIndex + count;
		if(mixed) {
			int ipv6SegmentEnd = address.getSegmentCount() - address.getMixedIPV6SegmentCount();
			end = Math.min(end, ipv6SegmentEnd);
		}
		if(partial) {
			for(int i = zeroStartIndex; i < end; i++) {
				for(int j = i + 1; j <= end; j++) {
					addAllExpansions(mixed, i, j - i, segmentCount);
				}	
			}
		} else {
			int len = end - zeroStartIndex;
			if(len > 0) {
				addAllExpansions(mixed, zeroStartIndex, len, segmentCount);
			}
		}
	}
	
	private void addAllCompressionVariations(boolean mixed) {
		int segmentCount = mixed ? address.getSegmentCount() - address.getMixedIPV6SegmentCount() : address.getSegmentCount();
		
		//start with the case of compressing nothing
		addAllExpansions(mixed, -1, 0, segmentCount);
		
		//now do the compressed strings
		if(options.includes(IPV6StringBuilderOptions.COMPRESSION_ALL_FULL)) {
			int[][] zeroSegs = address.getZeroSegments();
			for(int seg[] : zeroSegs) {
				addAllCompressedStrings(mixed, seg[0], seg[1], options.includes(IPV6StringBuilderOptions.COMPRESSION_ALL_PARTIAL), segmentCount);
			}
		} else if(options.includes(IPV6StringBuilderOptions.COMPRESSION_CANONICAL)) {
			int indexes[] = address.getCompressIndexAndCount(false, mixed);
			int maxIndex = indexes[0];
			int maxCount = indexes[1];
			if(maxIndex >= 0) { 
				if(maxCount > 1 || options.includes(IPV6StringBuilderOptions.COMPRESSION_SINGLE)) {
					addAllCompressedStrings(mixed, maxIndex, maxCount, false, segmentCount);
				} else {
					//nothing to compress, and this case already handled
				}
			}
		}
	}
	
	/*
	Here is how we get all potential strings:
		//for each of the non-mixed case and the mixed case
			//for each zero-segment we choose, including the one case of choosing no zero segment
				//for each sub-segment of that zero-segment compressed (only front end can be compressed for mixed) (this loop is skipped for the no-zero segment case)
					//for each potential expansion of a non-compressed segment
						//we write the string
	 */
	@Override
	String[] getStrings() {
		strings.clear();
		addAllCompressionVariations(false);
		if(options.includes(IPV6StringBuilderOptions.MIXED) && address.getMixedIPV6SegmentCount() > 0) {
			addAllCompressionVariations(true);
		}
		return strings.toArray(new String[strings.size()]);
	}
}
