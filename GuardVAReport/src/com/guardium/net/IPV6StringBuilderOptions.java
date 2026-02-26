/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

public class IPV6StringBuilderOptions extends IPStringBuilderOptions {
	public static final int MIXED = 0x1;

	public static final int UPPERCASE = 0x2;

	public static final int COMPRESSION_CANONICAL = 0x1000; //use the compression that is part of the canonical string format
	public static final int COMPRESSION_SINGLE = COMPRESSION_CANONICAL | 0x2000; //compress a single segment.  If more than one is compressible, choose the largest, and if multiple are largest, choose the most leftward.
	public static final int COMPRESSION_ALL_FULL = COMPRESSION_SINGLE | 0x4000; //compress fully any section that can be compressed
	public static final int COMPRESSION_ALL_PARTIAL = COMPRESSION_ALL_FULL | 0x8000;
	
	final IPStringBuilderOptions mixedOptions;

	public IPV6StringBuilderOptions(int options) {
		this(options, null);
	}
	
	public IPV6StringBuilderOptions(int options, IPStringBuilderOptions mixedOptions) {
		super(options | ((mixedOptions == null || mixedOptions.options == 0) ? 0 : MIXED));
		this.mixedOptions = (mixedOptions == null) ? new IPStringBuilderOptions() : mixedOptions;
	}
	
	public IPV6StringBuilderOptions() {
		this(UPPERCASE | COMPRESSION_ALL_FULL | LEADING_ZEROS_FULL_ALL_SEGMENTS, new IPStringBuilderOptions(LEADING_ZEROS_FULL_ALL_SEGMENTS ));
	}
}