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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.guardium.net.IPAddress.RangeOptions;

public class IPAddressTest {
	
	public static void main(String args[]) {
		testAll();
		//new IPAddressTest().test();
	}
	
	static class Failure {
		boolean pass;
		IPAddressString addr;
		IPAddress addrValue;
		String str;
		
		Failure(boolean pass, IPAddressString addr) {
			this.pass = pass;
			this.addr = addr;
		}
		
		Failure(String str, IPAddressString addr) {
			this.str = str;
			this.addr = addr;
		}
		
		Failure(String str, IPAddress addrValue) {
			this.str = str;
			this.addrValue = addrValue;
		}
	}
	
	ArrayList<IPAddressTest.Failure> failures = new ArrayList<IPAddressTest.Failure>();
	int numFailed;
	int numTested;
	
	protected IPAddressString createAddress(String x) {
		return new IPAddressString(x);
	}
	
	protected IPAddressString createAddress(String x, RangeOptions options) {
		return new IPAddressString(x);
	}
	
	protected IPAddress createAddress(byte bytes[]) {
		if(bytes.length == 4) {
			return new IPV4Address(bytes);
		}
		return new IPV6Address(bytes);
	}
	
	void addFailure(Failure failure) {
		failures.add(failure);
		//throw new RuntimeException();
	}
	
	void testResolved(String original, String expected) {
		IPAddressString origAddress = createAddress(original);
		IPAddress resolvedAddress = origAddress.resolve();
		IPAddressString expectedAddress = createAddress(expected);
		boolean result = (resolvedAddress == null) ? (expected == null) : resolvedAddress.equals(expectedAddress);
		if(!result) {
			numFailed++;
			addFailure(new Failure("resolved was " + resolvedAddress + " original was " + original, origAddress));
		}
		numTested++;
	}
	
	void testCount(String original, int number) {
		IPAddressString w = createAddress(original);
		testCount(w, number);
	}
	
	void testCount(IPAddressString w, int number) {
		IPAddress val = w.getValue();
		BigInteger count = val.getCount();
		if(!count.equals(BigInteger.valueOf(number))) {
			numFailed++;
			addFailure(new Failure("count was " + count, w));
		} else {
			Iterator<? extends IPAddress> addrIterator = val.iterator();
			int counter = 0;
			Set<IPAddress> set = new HashSet<IPAddress>();
			while(addrIterator.hasNext()) {
				IPAddress next = addrIterator.next();
				//int sz = set.size();
				set.add(next);
				//if(set.size() == sz || true) {
				//	System.out.println("dup " + next);
				//}
				//System.out.println("" + next);
				counter++;
			}
			//System.out.println();
			if(set.size() != number || counter != number) {
				//System.out.println("expected count " + number + " but was " + counter + " and set size was " + set.size());
				numFailed++;
				addFailure(new Failure("set count was " + set.size(), w));
			}
		}
		numTested++;
	}
	
	void testNormalized(String original, String expected) {
		testNormalized(original, expected, false, true);
	}
	
	void testMask(String original, String mask, String expected) {
		IPAddressString w = createAddress(original);
		IPAddress orig = w.getValue();
		IPAddressString maskString = createAddress(mask);
		IPAddress maskAddr = maskString.getValue();
		IPAddress masked = orig.toSubnet(maskAddr);
		IPAddressString expectedStr = createAddress(expected);
		IPAddress expectedAddr = expectedStr.getValue();
		if(!masked.equals(expectedAddr)) {
			numFailed++;
			addFailure(new Failure("mask was " + mask + " and masked was " + masked, w));
		}
		numTested++;
	}
	
	void testNormalized(String original, String expected, boolean keepMixed, boolean compress) {
		IPAddressString w = createAddress(original);
		String normalized;
		if(w.isIpv6()) {
			IPV6Address val = (IPV6Address) w.getValue();
			normalized = val.toNormalizedString(keepMixed, new IPV6AddressSection.AddressStringNormalizationParams(false, compress, true));
		} else {
			IPV4Address val = (IPV4Address) w.getValue();
			normalized = val.toNormalizedString();
		}
		if(!normalized.equals(expected)) {
			numFailed++;
			addFailure(new Failure("normalization was " + normalized, w));
		}
		numTested++;
	}
	
	void testCompressed(String original, String expected) {
		IPAddressString w = createAddress(original);
		String normalized;
		if(w.isIpv6()) {
			IPV6Address val = (IPV6Address) w.getValue();
			normalized = val.toCompressedString();
		} else if(w.isIpv4()) {
			IPV4Address val = (IPV4Address) w.getValue();
			normalized = val.toNormalizedString();
		} else {
			normalized = w.toString();
		}
		if(!normalized.equals(expected)) {
			numFailed++;
			addFailure(new Failure("canonical was " + normalized, w));
		}
		numTested++;
	}
	
	void testCanonical(String original, String expected) {
		IPAddressString w = createAddress(original);
		String normalized = w.getValue().toCanonicalString();
		if(!normalized.equals(expected)) {
			numFailed++;
			addFailure(new Failure("canonical was " + normalized, w));
		}
		numTested++;
	}
	
	void testMixed(String original, String expected) {
		IPAddressString w = createAddress(original);
		String normalized;
		if(w.isIpv6()) {
			IPV6Address val = (IPV6Address) w.getValue();
			normalized = val.toNormalizedString(false, new IPV6AddressSection.AddressStringNormalizationParams(true, true, true));
		} else {
			IPV4Address val = (IPV4Address) w.getValue();
			normalized = val.toNormalizedString();
		}
		if(!normalized.equals(expected)) {
			numFailed++;
			addFailure(new Failure("canonical was " + normalized, w));
		}
		numTested++;
	}
	
	
	
	void iptest(boolean pass, String x, boolean isZero, boolean notBoth, boolean ipV4Test, RangeOptions rangeOptions) {
		IPAddressString addr = createAddress(x, rangeOptions);
		iptest(pass, addr, isZero, notBoth, ipV4Test);
		//do it a second time to test the caching
		iptest(pass, addr, isZero, notBoth, ipV4Test);
	}
	
	void iptest(boolean pass, String x, boolean isZero, boolean notBoth, boolean ipV4Test) {
		IPAddressString addr = createAddress(x);
		iptest(pass, addr, isZero, notBoth, ipV4Test);
		//do it a second time to test the caching
		iptest(pass, addr, isZero, notBoth, ipV4Test);
	}
	
	void iptest(boolean pass, IPAddressString addr, boolean isZero, boolean notBoth, boolean ipV4Test) {
		boolean pass2 = notBoth ? !pass : pass;
		
		
		if(isNotExpected(pass, addr, ipV4Test, !ipV4Test) || isNotExpected(pass2, addr)) {
			numFailed++;
			addFailure(new Failure(pass, addr));
			
			//this part just for debugging
			if(isNotExpected(pass, addr, ipV4Test, !ipV4Test)) {
				isNotExpected(pass, addr, ipV4Test, !ipV4Test);
			} else {
				isNotExpected(pass2, addr);
			}
		} else {
			boolean zeroPass;
			if(notBoth) {
				zeroPass = !isZero;
			} else {
				zeroPass = pass && !isZero;
			}
			if(isNotExpectedNonZero(zeroPass, addr)) {
				numFailed++;
				addFailure(new Failure(zeroPass, addr));
				
				//this part just for debugging
				//boolean val = isNotExpectedNonZero(zeroPass, addr);
				//val = isNotExpectedNonZero(zeroPass, addr);
			} else {
				//test the bytes
				if(pass && addr.toString().length() > 0 && !addr.hasZone() && !addr.isNetworkPrefix() && addr.getValue() != null) { //only for valid addresses
					testBytes(addr.getValue());
				}
			}
		} 
		numTested++;
	}

	void testBytes(IPAddress addr) {
		try {
			String addrString = addr.toString();
			int index = addrString.indexOf('/');
			if(index >=0) {
				addrString = addrString.substring(0, index);
			}
			InetAddress inetAddress = InetAddress.getByName(addrString);
			byte[] b = inetAddress.getAddress();
			byte[] b2 = addr.getBytes();
			if(!Arrays.equals(b, b2)) {
				byte[] b3 = addr.getIPV4MappedBytes();
				if(!Arrays.equals(b, b3)) {
					addFailure(new Failure("bytes on addr " + inetAddress, addr));
					addr.getIPV4MappedBytes();
					numFailed++;
				}
			}
		} catch(UnknownHostException e) {
			numFailed++;
			addFailure(new Failure("bytes on addr " + e, addr));
		} 
	}
	
	void testMaskBytes(String cidr2, IPAddressString w2)
			throws IPAddressException {
		int index = cidr2.indexOf('/');
		IPAddressString w3 = createAddress(cidr2.substring(0, index));
		try {
			InetAddress inetAddress = null;
			inetAddress = InetAddress.getByName(w3.toString());//xxx wildcards
			byte[] b = inetAddress.getAddress();
			byte[] b2 = w3.toValue().getBytes();
			if(!Arrays.equals(b, b2)) {
				addFailure(new Failure("bytes on addr " + inetAddress, w3));
				numFailed++;
			} else {
				byte b3[] = w2.toValue().getLowestBytes();
				if(!Arrays.equals(b3, b2)) {
					addFailure(new Failure("bytes on addr " + w3, w2));
					numFailed++;
				}
			}
		} catch(UnknownHostException e) {
			addFailure(new Failure("bytes on addr " + w3, w3));
			numFailed++;
		}
	}
	
	void testFromBytes(byte bytes[], String expected) {
		IPAddress addr = createAddress(bytes);
		IPAddressString addr2 = createAddress(expected);
		boolean result = addr.equals(addr2);
		if(!result) {
			numFailed++;
			addFailure(new Failure("created was " + addr + " expected was " + addr2, addr));
		}
		numTested++;
	}
	
	boolean isNotExpected(boolean expectedPass, IPAddressString addr) {
		return isNotExpected(expectedPass, addr, false, false);
	}
	
	boolean isNotExpected(boolean expectedPass, IPAddressString addr, boolean isIpV4, boolean isIpV6) {
		try {
			if(isIpV4) {
				addr.validateIpv4();
			} else if(isIpV6) {
				addr.validateIpv6();
			} else {
				addr.validate();
			}
			return !expectedPass;
		} catch(IPAddressException e) {
			return expectedPass;
		}
	}
	
	boolean isNotExpectedNonZero(boolean expectedPass, IPAddressString addr) {
		if(addr.isInvalid()) {
			return expectedPass;
		}
		//if expectedPass is true, we are expecting a non-zero address
		//return true to indicate we have gotten something not expected
		if(addr.isZero()) {
			return expectedPass;
		}
		return !expectedPass;
	}
	
	void ipv4testOnly(boolean pass, String x) {
		iptest(pass, x, false, true, true);
	}
	
	void ipv4test(boolean pass, String x) {
		ipv4test(pass, x, false);
	}
	
	void ipv4test(boolean pass, String x, RangeOptions rangeOptions) {
		ipv4test(pass, x, false, rangeOptions);
	}
	
	void ipv4test(boolean pass, String x, boolean isZero) {
		iptest(pass, x, isZero, false, true);
	}
	
	void ipv4test(boolean pass, String x, boolean isZero, RangeOptions rangeOptions) {
		iptest(pass, x, isZero, false, true, rangeOptions);
	}
	
	void ipv6testOnly(int pass, String x) {
		iptest(pass == 0 ? false : true, x, false, true, false);
	}
	
	void ipv6testWithZone(int pass, String x) {//only here so subclass can override
		ipv6test(pass, x);
	}
	
	void ipv6test(int pass, String x) {
		ipv6test(pass == 0 ? false : true, x);
	}
	
	void ipv6testWithZone(boolean pass, String x) {
		ipv6test(pass, x);
	}
	
	void ipv6test(boolean pass, String x) {
		ipv6test(pass, x, false);
	}
	
	void ipv6test(boolean pass, String x, RangeOptions options) {
		ipv6test(pass, x, false, options);
	}
	
	void ipv6test(int pass, String x, boolean isZero) {
		ipv6test(pass == 0 ? false : true, x, isZero);
	}
	
	void ipv6test(boolean pass, String x, boolean isZero, RangeOptions options) {
		iptest(pass, x, isZero, false, false, options);
	}
	
	void ipv6test(boolean pass, String x, boolean isZero) {
		iptest(pass, x, isZero, false, false);
	}
	
	void testVariants(String addr, int expectedBasic, int expectedStandard, int expectedAll) {
		IPAddressString address = createAddress(addr);
		String basicStrs[] = address.getValue().toBasicStrings();
		testStrings(basicStrs, expectedBasic, address);
		String standardStrs[] = address.getValue().toStandardStrings();
		testStrings(standardStrs, expectedStandard, address);
		String allStrs[] = address.getValue().toAllStrings();
		testStrings(allStrs, expectedAll, address);
	}

	private void testStrings(String[] strs, int expectedCount, IPAddressString addr) {
		testStrings(strs, expectedCount, addr, false);
	}
	
	private void testStrings(String[] strs, int expectedCount, IPAddressString addr, boolean writeList) {
		if(writeList) {
			listVariants(strs);
		}
		if(expectedCount != strs.length) {
			numFailed++;
			addFailure(new Failure("String count " + strs.length + " doesn't match expected count " + expectedCount, addr));
		} else {
			Set<String> set = new HashSet<String>();
			Collections.addAll(set, strs);
			if(set.size() != strs.length) {
				numFailed++;
				addFailure(new Failure((strs.length - set.size()) + " duplicates for " + addr, addr));
				set.clear();
				for(String str: strs) {
					if(set.contains(str)) {
						System.out.println("dup " + str);
					}
					set.add(str);
				}
			} else for(String str: strs) {
				if(str.length() > 45) {
					numFailed++;
					addFailure(new Failure("excessive length " + str + " for " + addr, addr));
					break;
				}
			}
		}
		numTested++;
	}
	
	private void listVariants(String[] strs) {
		System.out.println("list count is " + strs.length);
		for(String str: strs) {
			System.out.println(str);
		}
		System.out.println();
	}
	
	private boolean checkNotMask(IPAddress address, boolean network) {
		Integer maskPrefix = address.getCIDRMaskPrefixLength(network);
		Integer otherMaskPrefix = address.getCIDRMaskPrefixLength(!network);
		if(maskPrefix != null || otherMaskPrefix != null) {
			numFailed++;
			addFailure(new Failure("failed not mask", address));
			return false;
		}
		numTested++;
		return true;
	}
	
	private void checkNotMask(String addr) {
		IPAddressString addressStr = createAddress(addr);
		IPAddress address = addressStr.getValue();
		boolean val = ((address.getBytes()[0] % 2) == 0);
		if(checkNotMask(address, val)) {
			checkNotMask(address, !val);
		}
	}
	
	boolean secondTry;
	
	private synchronized boolean checkMask(IPAddress address, int prefixBits, boolean network) {
		Integer maskPrefix = address.getCIDRMaskPrefixLength(network);
		Integer otherMaskPrefix = address.getCIDRMaskPrefixLength(!network);
		if(maskPrefix != Math.min(prefixBits, address.getBitCount()) || otherMaskPrefix != null) {
			numFailed++;
			addFailure(new Failure("failed mask", address));
			return false;
		}
		
		if(network) {
			try {
				String originalPrefixStr = "/" + prefixBits;
				String originalChoppedStr = prefixBits <= address.getBitCount() ? originalPrefixStr : "/" + address.getBitCount();
				IPAddressString prefix = new IPAddressString(originalPrefixStr);
				String maskStr = prefix.convertToMask(address.getIpVersion());
				if(!secondTry && prefixBits < address.getBitCount()) {
					maskStr += originalPrefixStr;
				}
				String normalStr = address.toNormalizedString();
				if(!maskStr.equals(normalStr)) {
					numFailed++;
					addFailure(new Failure("failed prefix conversion " + maskStr, prefix));
					return false;
				} else {
					IPAddressString maskStr2 = new IPAddressString(maskStr);
					String prefixStr = maskStr2.convertToPrefixLength();
					if(prefixStr == null || !prefixStr.equals(originalChoppedStr)) {
						numFailed++;
						maskStr2 = new IPAddressString(maskStr);
						maskStr2.convertToPrefixLength();
						addFailure(new Failure("failed mask converstion " + prefixStr, maskStr2));
						return false;
					}
				}
			} catch(IPAddressException | RuntimeException e) {
				numFailed++;
				addFailure(new Failure("failed conversion: " + e.getMessage(), address));
				return false;
			}
		}
		
		numTested++;
		if(!secondTry) {
			secondTry = true;
			byte bytes[] = address.getLowestBytes();
			IPAddress another = IPAddress.from(bytes);
			boolean result = checkMask(another, prefixBits, network);
			secondTry = false;
			
			//now check the prefix in the mask
			if(result) {
				boolean prefixBitsMismatch = false;
				Integer addrPrefixBits = address.getNetworkPrefixBits();
				if(prefixBits >= address.getBitCount() || !network) {
					prefixBitsMismatch = addrPrefixBits != null;
				} else {
					prefixBitsMismatch = addrPrefixBits == null || (prefixBits != addrPrefixBits);
				}
				if(prefixBitsMismatch) {
					numFailed++;
					addFailure(new Failure("prefix incorrect", address));
					return false;
				}
			}
		}
		return true;
	}
	
	void testMasks() {
		for(int i=0; i<=129; i++) {
			IPV6Address ipv6HostMask = IPV6Address.getHostMask(i);
			if(checkMask(ipv6HostMask, i, false)) {
				IPV6Address ipv6NetworkMask = IPV6Address.getNetworkMask(i);
				if(checkMask(ipv6NetworkMask, i, true)) {
					IPV4Address ipv4HostMask = IPV4Address.getHostMask(i);
					if(checkMask(ipv4HostMask, i, false)) {
						IPV4Address ipv4NetworkMask = IPV4Address.getNetworkMask(i);
						checkMask(ipv4NetworkMask, i, true);		
//						System.out.println(ipv6HostMask);
//						System.out.println(ipv6NetworkMask);
//						System.out.println(ipv4HostMask);
//						System.out.println(ipv4NetworkMask);
//						System.out.println();
					}
				}
			}
		}
	}
	
	void testMasks(String cidr1, String cidr2) {
		testMasks(cidr1, cidr2, false);
	}
	
	void testMasks(String cidr1, String cidr2, boolean prefixExceedsBitSize) {
		IPAddressString w = createAddress(cidr1);
		IPAddressString w2 = createAddress(cidr2);
		try {
			boolean first = w.equals(w2);
			IPAddress v = w.toValue();
			IPAddress v2 = w2.toValue();
			boolean second = v.equals(v2);
			if(!first || !second) {
				numFailed++;
				addFailure(new Failure("failed " + w2, w));
			} else {
				String str = w2.getValue().toNormalizedString();
				if(prefixExceedsBitSize ? cidr2.equals(str) : !cidr2.equals(str)) {
					numFailed++;
					addFailure(new Failure("failed " + w2, w2));
				} else {
					testMaskBytes(cidr2, w2);
				}
			}
		} catch(IPAddressException e) {
			numFailed++;
			addFailure(new Failure("failed " + w2, w));
		}
		numTested++;
	}
	
	void printStrings(IPAddressSection section) {
		String strs[] = section.toStandardStrings();
		int count = 0;
		System.out.println(section);
		for(String str: strs) {
			System.out.println("\t" + ++count + ": " + str);
		}
		
	}
	void testSplit(String address, int bits, String network, String host) {
		IPAddressString w = createAddress(address);
		IPAddress v = w.getValue();
		IPAddressSection section = v.getNetworkSection(bits);
		String sectionStr = section.toNormalizedString();
		printStrings(section);
		if(!sectionStr.equals(network)) {
			numFailed++;
			addFailure(new Failure("failed " + section + " expected " + network, w));
		} else {
			section = v.getHostSection(bits);
			printStrings(section);
			sectionStr = section.toNormalizedString();
			if(!sectionStr.equals(host)) {
				numFailed++;
				addFailure(new Failure("failed " + section + " expected " + host, w));
			}
		}
		numTested++;
	}
	void testSplit(String address, int bits, String network, int networkStringCount, String host, int hostStringCount) {
		IPAddressString w = createAddress(address);
		IPAddress v = w.getValue();
		IPAddressSection section = v.getNetworkSection(bits);
		String sectionStr = section.toNormalizedString();
		//printStrings(section);
		if(!sectionStr.equals(network)) {
			numFailed++;
			addFailure(new Failure("failed " + section + " expected " + network, w));
		} else {
			
			//section.toAllStrings();
			//System.out.println("got this many:" + section.toAllStrings().length);
			String standards[] = section.toStandardStrings();
			if(standards.length != networkStringCount) {
				numFailed++;
				addFailure(new Failure("failed " + section + " expected count " + networkStringCount + " was " + section.toStandardStrings().length, w));
			} else {
				section = v.getHostSection(bits);
				//printStrings(section);
				sectionStr = section.toNormalizedString();
				if(!sectionStr.equals(host)) {
					numFailed++;
					addFailure(new Failure("failed " + section + " expected " + host, w));
				} else {
					//System.out.println("got this many:" + section.toAllStrings().length);
					String standardStrs[] = section.toStandardStrings();
					if(standardStrs.length != hostStringCount) {
						numFailed++;
						addFailure(new Failure("failed " + section + " expected count " + hostStringCount + " was " + standardStrs.length, w));
						standardStrs = section.toStandardStrings();
					}
				}
			}
		}
		numTested++;
	}
	
	void testSubnet(int prefix, String ipv4Networkaddress, String ipv4HostAddress, String ipv6Networkaddress, String ipv6HostAddress) {
		IPV4Address addr4 = IPV4Address.getNetworkMask(prefix);
		IPAddressString w = createAddress(ipv4Networkaddress);
		
		try {
			IPAddress wValue = w.toValue();
			if(!wValue.equals(addr4)) {
				numFailed++;
				addFailure(new Failure("failed " + addr4, w));
			} else {
				IPV6Address addr6 = IPV6Address.getNetworkMask(prefix);
				IPAddressString w2 = createAddress(ipv6Networkaddress);
				try {
					IPAddress w2Value = w2.toValue();
					if(!w2Value.equals(addr6)) {
						numFailed++;
						addFailure(new Failure("failed " + addr6, w2));
					}
					addr4 = IPV4Address.getHostMask(prefix);
					w = createAddress(ipv4HostAddress);
					try {
						wValue = w.toValue();
						if(!wValue.equals(addr4)) {
							numFailed++;
							addFailure(new Failure("failed " + addr4, w));
						} else {
							addr6 = IPV6Address.getHostMask(prefix);
							w2 = createAddress(ipv6HostAddress);
							try {
								w2Value = w2.toValue();
								if(!w2Value.equals(addr6)) {
									numFailed++;
									addFailure(new Failure("failed " + addr6, w2));
								}
							} catch(IPAddressException e) {
								numFailed++;
								addFailure(new Failure("failed " + addr6, w2));
							}
						}
					} catch(IPAddressException e) {
						numFailed++;
						addFailure(new Failure("failed " + addr4, w));
					}
				} catch(IPAddressException e) {
					numFailed++;
					addFailure(new Failure("failed " + addr6, w2));
				}
			}
		} catch(IPAddressException e) {
			numFailed++;
			addFailure(new Failure("failed " + addr4, w));
		}
		numTested++;
	}
	
	static int count(String str, String match) {
		int count = 0;
		for(int index = -1; (index = str.indexOf(match, index + 1)) >= 0; count++);
		return count;
	}
	
	void testURL(String url) {
		IPAddressString w = createAddress(url);
		try {
			w.toValue();
			numFailed++;
			addFailure(new Failure("failed: " + "URL " + url, w));
		} catch(IPAddressException e) {
			//pass
			e.getMessage();
		}
	}
	
	void testSections(String address, int bits, int count) {
		IPAddressString w = createAddress(address);
		IPAddress v = w.getValue();
		IPAddressSection section = v.getNetworkSection(bits);
		StringBuilder builder = new StringBuilder();
		section.getStartsWithSQLClause(builder, "XXX");
		String clause = builder.toString();
		int found = count(clause, "OR") + 1;
		if(found != count) {
			numFailed++;
			addFailure(new Failure("failed: " + "Finding first " + (bits / v.getBitsPerSegment()) + " segments of " + v, w));
		}
		numTested++;
	}
	
	public static void testAll() {
		System.out.println(IPAddressTest.class.getSimpleName());
		new IPAddressTest().test();
		System.out.println();
		System.out.println(HostTest.class.getSimpleName());
		new HostTest().test();
		System.out.println();
		System.out.println(IPAddressRangeTest.class.getSimpleName());
		new IPAddressRangeTest().test();
		System.out.println();
		System.out.println("Done");
	}
	
	void test() {
		testMasks("9.129.237.26/0", "0.0.0.0/0"); //compare the two for equality.  compare the bytes of the second one with the bytes of the second one having no mask.
		testMasks("9.129.237.26/1", "0.0.0.0/1");
		testMasks("9.129.237.26/4", "0.0.0.0/4");
		testMasks("9.129.237.26/5", "8.0.0.0/5");
		testMasks("9.129.237.26/7", "8.0.0.0/7");
		testMasks("9.129.237.26/8", "9.0.0.0/8");
		testMasks("9.129.237.26/9", "9.128.0.0/9");
		testMasks("9.129.237.26/15", "9.128.0.0/15");
		testMasks("9.129.237.26/16", "9.129.0.0/16");
		testMasks("9.129.237.26/30", "9.129.237.24/30");
		testMasks("9.129.237.26/32", "9.129.237.26/32", true);
		
		testMasks();
		checkNotMask("254.255.0.0");
		checkNotMask("255.255.0.1");
		checkNotMask("0.1.0.0");
		checkNotMask("0::10");
		checkNotMask("1::0");
		
		//xx mire tests, check non-segment boundarues, ipv6, and standard string counts
		//if I do a non-segment boundary, do I need to mask the part of the segmetn I do not want?  I think so
		testSplit("9.129.237.26", 0, "", 1, "9.129.237.26", 2); //compare the two for equality.  compare the bytes of the second one with the bytes of the second one having no mask.
//		testSplit("9.129.237.26", 1, "0.0.0.0", 1");
//		testSplit("9.129.237.26", 4, "0.0.0.0", 4");
//		testSplit("9.129.237.26", 5, "8.0.0.0", 5");
//		testSplit("9.129.237.26", 7, "8.0.0.0", 7");
		testSplit("9.129.237.26", 8, "9", 2, "129.237.26", 2);
//		testSplit("9.129.237.26", 9, "9.128.0.0", 9");
//		testSplit("9.129.237.26", 15, "9.128.0.0", 15");
		testSplit("9.129.237.26", 16, "9.129", 2, "237.26", 2);
//		testSplit("9.129.237.26", 30, "9.129.237.24", 30");
		testSplit("9.129.237.26", 31, "9.129.237.26/31", 2, "0", 2);
		testSplit("9.129.237.26", 32, "9.129.237.26", 2, "", 1);
		
		testSplit("1.2.3.4", 4, "0/4", 2, "1.2.3.4", 2);
		testSplit("255.2.3.4", 4, "240/4", 1, "15.2.3.4", 2);
		
		
		
		testSplit("9:129::237:26", 0, "", 1, "9:129:0:0:0:0:237:26", 12); //compare the two for equality.  compare the bytes of the second one with the bytes of the second one having no mask.
//		testSplit("9.129.237.26", 1, "0.0.0.0", 1");
//		testSplit("9.129.237.26", 4, "0.0.0.0", 4");
//		testSplit("9.129.237.26", 5, "8.0.0.0", 5");
//		testSplit("9.129.237.26", 7, "8.0.0.0", 7");
		//testSplit("9:129::237:26", 8, "9", "129:0:0:0:0:237:26");
//		testSplit("9.129.237.26", 9, "9.128.0.0", 9");
//		testSplit("9.129.237.26", 15, "9.128.0.0", 15");
		testSplit("9:129::237:26", 16, "9", 2, "129:0:0:0:0:237:26", 12);
		testSplit("9:129::237:26", 31, "9:128/31", 2, "1:0:0:0:0:237:26", 12);
		testSplit("9:129::237:26", 32, "9:129", 2, "0:0:0:0:237:26", 10);
		testSplit("9:129::237:26", 33, "9:129:0/33", 2, "0:0:0:0:237:26", 10);
		testSplit("9:129::237:26", 64, "9:129:0:0", 4, "0:0:237:26", 10);
		testSplit("9:129::237:26", 96, "9:129:0:0:0:0", 4, "237:26", 4);
		testSplit("9:129::237:26", 111, "9:129:0:0:0:0:236/111", 12, "1:26", 4);
		testSplit("9:129::237:26", 112, "9:129:0:0:0:0:237", 12, "26", 4);
		testSplit("9:129::237:26", 113, "9:129:0:0:0:0:237:0/113", 12, "26", 4);
		testSplit("9:129::237:26", 127, "9:129:0:0:0:0:237:26/127", 12, "0", 4);
		testSplit("9:129::237:26", 128, "9:129:0:0:0:0:237:26", 12, "", 1);
		
		int USE_UPPERCASE = 2;
		
		testSplit("a:b:c:d:e:f:a:b", 4, "0/4", 2, "a:b:c:d:e:f:a:b", 6 * USE_UPPERCASE);
		testSplit("ffff:b:c:d:e:f:a:b", 4, "f000/4", 1 * USE_UPPERCASE, "fff:b:c:d:e:f:a:b", 6 * USE_UPPERCASE);
		testSplit("ffff:b:c:d:e:f:a:b", 2, "c000/2", 1 * USE_UPPERCASE, "3fff:b:c:d:e:f:a:b", 6 * USE_UPPERCASE);
		
		testURL("http://1.2.3.4");
		testURL("http://[a:a:a:a:b:b:b:b]");
		testURL("http://a:a:a:a:b:b:b:b");
		
		testSubnet(0, "0.0.0.0/0", "255.255.255.255", "::/0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"); //test that the given prefix gives an ipv4 and ipv6 address matching the two netmasks
		testSubnet(1, "128.0.0.0/1", "127.255.255.255", "8000::/1", "7fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(15, "255.254.0.0/15", "0.1.255.255", "fffe::/15", "1:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(16, "255.255.0.0/16", "0.0.255.255", "ffff::/16", "::ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(17, "255.255.128.0/17", "0.0.127.255", "ffff:8000::/17", "::7fff:ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(31, "255.255.255.254/31", "0.0.0.1", "ffff:fffe::/31", "::1:ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(32, "255.255.255.255/32", "0.0.0.0", "ffff:ffff::/32", "::ffff:ffff:ffff:ffff:ffff:ffff");
		testSubnet(127, "255.255.255.255/32", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe/127", "::1");
		testSubnet(128, "255.255.255.255/32", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128", "::");
		testSubnet(129, "255.255.255.255/32", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128", "::");
		
		testSections("9.129.237.26", 0, 1);//xxx figure out the 0 cidr prefix case xxx;
		testSections("9.129.237.26", 8, 2);
		testSections("9.129.237.26", 16, 2);
		testSections("9.129.237.26", 24, 2);
		testSections("9.129.237.26", 32, 2);
		testSections("9:129::237:26", 0, 1);
		testSections("9:129::237:26", 16, 2);
		testSections("9:129::237:26", 64, 4);
		testSections("9:129::237:26", 80, 4);
		testSections("9:129::237:26", 96, 4);
		testSections("9:129::237:26", 112, 12);
		testSections("9:129::237:26", 128, 12);
		
		testSections("9.129.237.26", 7, 4);
		testSections("9.129.237.26", 9, 256); //129 is 10000001
		testSections("9.129.237.26", 10, 128);
		testSections("9.129.237.26", 11, 64);
		testSections("9.129.237.26", 12, 32);
		testSections("9.129.237.26", 13, 16);
		testSections("9.129.237.26", 14, 8); //10000000 to 10000011 (128 to 131)
		testSections("9.129.237.26", 15, 4); //10000000 to 10000001 (128 to 129)
		
//		testSubnet(0, "0.0.0.0", "255.255.255.255", "::", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"); //test that the given prefix gives an ipv4 and ipv6 address matching the two netmasks
//		testSubnet(1, "128.0.0.0", "127.255.255.255", "8000::", "7fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(15, "255.254.0.0", "0.1.255.255", "fffe::", "1:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(16, "255.255.0.0", "0.0.255.255", "ffff::", "::ffff:ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(17, "255.255.128.0", "0.0.127.255", "ffff:8000::", "::7fff:ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(31, "255.255.255.254", "0.0.0.1", "ffff:fffe::", "::1:ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(32, "255.255.255.255", "0.0.0.0", "ffff:ffff::", "::ffff:ffff:ffff:ffff:ffff:ffff");
//		testSubnet(127, "255.255.255.255", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe", "::1");
//		testSubnet(128, "255.255.255.255", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "::");
//		testSubnet(129, "255.255.255.255", "0.0.0.0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "::");
		
		//test some valid and invalid prefixes
		ipv4test(true, "1.2.3.4/1");
		ipv4test(false, "1.2.3.4/-1");
		ipv4test(false, "1.2.3.4/");
		ipv4test(false, "1.2.3.4/x");
		ipv4test(true, "1.2.3.4/33");//we are allowing extra-large prefixes
		ipv6test(true, "1::1/1");
		ipv6test(false, "1::1/-1");
		ipv6test(false, "1::1/");
		ipv6test(false, "1::1/x");
		ipv6test(true, "1::1/129");//we are allowing extra-large prefixes
		
		
		
		//test that the given address has the given number of standard variants and total variants
		testVariants("::", 2, 9, 1297);
		testVariants("2:2:2:2:2:2:2:2", 1, 6, 1280);
		testVariants("2:0:0:2:0:2:2:2", 2, 18, 2240);
		testVariants("a:b:c:0:d:e:f:1", 4, 12 * USE_UPPERCASE, 1920 * USE_UPPERCASE);
		testVariants("a:b:c:0:0:d:e:f", 4, 12 * USE_UPPERCASE, 1600 * USE_UPPERCASE);
		testVariants("a:b:c:d:e:f:0:1", 4, 8 * USE_UPPERCASE, 1408 * USE_UPPERCASE);
		testVariants("a:b:c:d:e:f:0:0", 4, 8 * USE_UPPERCASE, 1344 * USE_UPPERCASE);
		testVariants("a:b:c:d:e:f:a:b", 2, 6 * USE_UPPERCASE, 1280 * USE_UPPERCASE);
		testVariants("aaaa:bbbb:cccc:dddd:eeee:ffff:aaaa:bbbb", 2, 2 * USE_UPPERCASE, 2 * USE_UPPERCASE);
		testVariants("a111:1111:1111:1111:1111:1111:9999:9999", 2, 2 * USE_UPPERCASE, 2 * USE_UPPERCASE);
		testVariants("1a11:1111:1111:1111:1111:1111:9999:9999", 2, 2 * USE_UPPERCASE, 2 * USE_UPPERCASE);
		testVariants("11a1:1111:1111:1111:1111:1111:9999:9999", 2, 2 * USE_UPPERCASE, 2 * USE_UPPERCASE);
		testVariants("111a:1111:1111:1111:1111:1111:9999:9999", 2, 2 * USE_UPPERCASE, 2 * USE_UPPERCASE);
		testVariants("aaaa:b:cccc:dddd:eeee:ffff:aaaa:bbbb", 2, 4 * USE_UPPERCASE, 4 * USE_UPPERCASE);
		testVariants("aaaa:b:cc:dddd:eeee:ffff:aaaa:bbbb", 2, 4 * USE_UPPERCASE, 8 * USE_UPPERCASE);
		testVariants("1.2.3.4", 1, 2, 16);
		testVariants("0.0.0.0", 1, 2, 16);
		testVariants("1111:2222:aaaa:4444:5555:6666:7070:700a",  1 * USE_UPPERCASE, 1 * USE_UPPERCASE + 2 * USE_UPPERCASE, 1 * USE_UPPERCASE + 2 * USE_UPPERCASE);//this one can be capitalized when mixed 
		testVariants("1111:2222:3333:4444:5555:6666:7070:700a", 2, 1 * USE_UPPERCASE + 2, 1 * USE_UPPERCASE + 2);//this one can only be capitalized when not mixed, so the 2 mixed cases are not doubled
		
		
		testFromBytes(new byte[] {-1, -1, -1, -1}, "255.255.255.255");
		testFromBytes(new byte[] {1, 2, 3, 4}, "1.2.3.4");
		testFromBytes(new byte[16], "::");
		testFromBytes(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, "::1");
		testFromBytes(new byte[] {0, 10, 0, 11, 0, 12, 0, 13, 0, 14, 0, 15, 0, 1, 0, 2}, "a:b:c:d:e:f:1:2");
		
		testResolved("sfoley1.guard.swg.usma.ibm.com", "9.32.237.26");//obviously this test will eventually fail
		testResolved("sfoley1.guard.swg.usma.ibm.com", "009.032.237.026");//obviously this test will eventually fail
		testResolved("vx38.guard.swg.usma.ibm.com", "9.70.146.84");//obviously this test will eventually fail
		testResolved("9.32.237.26", "9.32.237.26");
		testResolved("9.70.146.84", "9.70.146.84");
		
		testNormalized("1.2.3.4", "1.2.3.4");
		testNormalized("1.2.00.4", "1.2.0.4");
		testNormalized("000.2.00.4", "0.2.0.4");
		testNormalized("00.2.00.000", "0.2.0.0");
		testNormalized("000.000.000.000", "0.0.0.0");
		
		testNormalized("A:B:C:D:E:F:A:B", "a:b:c:d:e:f:a:b");
		testNormalized("ABCD:ABCD:CCCC:Dddd:EeEe:fFfF:aAAA:Bbbb", "abcd:abcd:cccc:dddd:eeee:ffff:aaaa:bbbb");
		testNormalized("AB12:12CD:CCCC:Dddd:EeEe:fFfF:aAAA:Bbbb", "ab12:12cd:cccc:dddd:eeee:ffff:aaaa:bbbb");
		testNormalized("ABCD::CCCC:Dddd:EeEe:fFfF:aAAA:Bbbb", "abcd::cccc:dddd:eeee:ffff:aaaa:bbbb");
		testNormalized("::ABCD:CCCC:Dddd:EeEe:fFfF:aAAA:Bbbb", "::abcd:cccc:dddd:eeee:ffff:aaaa:bbbb");
		testNormalized("ABCD:ABCD:CCCC:Dddd:EeEe:fFfF:aAAA::", "abcd:abcd:cccc:dddd:eeee:ffff:aaaa::");
		testNormalized("::ABCD:Dddd:EeEe:fFfF:aAAA:Bbbb", "::abcd:dddd:eeee:ffff:aaaa:bbbb");
		testNormalized("ABCD:ABCD:CCCC:Dddd:fFfF:aAAA::", "abcd:abcd:cccc:dddd:ffff:aaaa::");
		testNormalized("::ABCD", "::abcd");
		testNormalized("aAAA::", "aaaa::");
		
		testNormalized("0:0:0:0:0:0:0:0", "::");
		testNormalized("0000:0000:0000:0000:0000:0000:0000:0000", "::");
		testNormalized("0000:0000:0000:0000:0000:0000:0000:0000", "0:0:0:0:0:0:0:0", true, false);
		testNormalized("0:0:0:0:0:0:0:1", "::1");
		testNormalized("0:0:0:0:0:0:0:1", "0:0:0:0:0:0:0:1", true, false);
		testNormalized("0:0:0:0::0:0:1", "0:0:0:0:0:0:0:1", true, false);
		testNormalized("0000:0000:0000:0000:0000:0000:0000:0001", "::1");
		testNormalized("1:0:0:0:0:0:0:0", "1::");
		testNormalized("0001:0000:0000:0000:0000:0000:0000:0000", "1::");
		testNormalized("1:0:0:0:0:0:0:1", "1::1");
		testNormalized("0001:0000:0000:0000:0000:0000:0000:0001", "1::1");
		testNormalized("1:0:0:0::0:0:1", "1::1");
		testNormalized("0001::0000:0000:0000:0000:0000:0001", "1::1");
		testNormalized("0001:0000:0000:0000:0000:0000::0001", "1::1");
		testNormalized("::0000:0000:0000:0000:0000:0001", "::1");
		testNormalized("0001:0000:0000:0000:0000:0000::", "1::");
		testNormalized("1:0::1", "1::1");
		testNormalized("0001:0000::0001", "1::1");
		testNormalized("0::", "::");
		testNormalized("0000::", "::");
		testNormalized("::0", "::");
		testNormalized("::0000", "::");
		testNormalized("0:0:0:0:1:0:0:0", "::1:0:0:0");
		testNormalized("0000:0000:0000:0000:0001:0000:0000:0000", "::1:0:0:0");
		testNormalized("0:0:0:1:0:0:0:0", "0:0:0:1::");
		testNormalized("0000:0000:0000:0001:0000:0000:0000:0000", "0:0:0:1::");
		testNormalized("0:1:0:1:0:1:0:1", "::1:0:1:0:1:0:1");
		testNormalized("0000:0001:0000:0001:0000:0001:0000:0001", "::1:0:1:0:1:0:1");
		testNormalized("1:1:0:1:0:1:0:1", "1:1::1:0:1:0:1");
		testNormalized("0001:0001:0000:0001:0000:0001:0000:0001", "1:1::1:0:1:0:1");
		
		testCanonical("0001:0000:0000:000F:0000:0000:0001:0001", "1::f:0:0:1:1");//must be leftmost
		testCanonical("0001:0001:0000:000F:0000:0001:0000:0001", "1:1:0:f:0:1:0:1");//but singles not compressed
		testMixed("0001:0001:0000:000F:0000:0001:0000:0001", "1:1::f:0:1:0.0.0.1");//singles compressed in mixed
		testCompressed("a.b.c.d", "a.b.c.d");
		
		testCompressed("1:0:1:1:1:1:1:1", "1::1:1:1:1:1:1");
		testCanonical("1:0:1:1:1:1:1:1", "1:0:1:1:1:1:1:1");
		testMixed("1:0:1:1:1:1:1:1", "1::1:1:1:1:0.1.0.1");
		
		testMixed("::", "::0.0.0.0");
		
		testNormalized("A:B:C:D:E:F:000.000.000.000", "a:b:c:d:e:f:0.0.0.0", true, true);
		testNormalized("A:B:C:D:E::000.000.000.000", "a:b:c:d:e::0.0.0.0", true, true);
		testNormalized("::B:C:D:E:F:000.000.000.000", "::b:c:d:e:f:0.0.0.0", true, true);
		testNormalized("A:B:C:D::000.000.000.000", "a:b:c:d::0.0.0.0", true, true);
		testNormalized("::C:D:E:F:000.000.000.000", "::c:d:e:f:0.0.0.0", true, true);
		testNormalized("::C:D:E:F:000.000.000.000", "0:0:c:d:e:f:0.0.0.0", true, false);
		testNormalized("A:B:C::E:F:000.000.000.000", "a:b:c::e:f:0.0.0.0", true, true);
		testNormalized("A:B::E:F:000.000.000.000", "a:b::e:f:0.0.0.0", true, true);
		
		
				//"ABCD:EF12:**:*%:%%:%A%%:%%A%:BBBB", "abcd:ef12:*:*:%:%a%:%a%:bbbb");
		testMask("1.2.3.4", "0.0.2.0", "0.0.2.0");
		testMask("1.2.3.4", "0.0.1.0", "0.0.1.0");
		testMask("A:B:C:D:E:F:A:B", "A:0:C:0:E:0:A:0", "A:0:C:0:E:0:A:0");
		testMask("A:B:C:D:E:F:A:B", "FFFF:FFFF:FFFF:FFFF::", "A:B:C:D::");
		testMask("A:B:C:D:E:F:A:B", "::FFFF:FFFF:FFFF:FFFF", "::E:F:A:B");
		
		
		ipv4test(false, "");;
		ipv4test(true, "1.2.3.4");
		ipv4test(false, "[1.2.3.4]");//only ipv6 can be in the square brackets
		
		ipv4test(!true, "a");
		
		ipv4test(!true, "1.2.3");
		
		ipv4test(!true, "a.2.3.4");
		ipv4test(!true, "1.a.3.4");
		ipv4test(!true, "1.2.a.4");
		ipv4test(!true, "1.2.3.a");
		
		ipv4test(!true, ".2.3.4");
		ipv4test(!true, "1..3.4");
		ipv4test(!true, "1.2..4");
		ipv4test(!true, "1.2.3.");
		
		ipv4test(!true, "256.2.3.4");
		ipv4test(!true, "1.256.3.4");
		ipv4test(!true, "1.2.256.4");
		ipv4test(!true, "1.2.3.256");
		
		
		ipv4test(true, "0.0.0.0", true);
		ipv4test(true, "00.0.0.0", true);
		ipv4test(true, "0.00.0.0", true);
		ipv4test(true, "0.0.00.0", true);
		ipv4test(true, "0.0.0.00", true);
		ipv4test(true, "000.0.0.0", true);
		ipv4test(true, "0.000.0.0", true);
		ipv4test(true, "0.0.000.0", true);
		ipv4test(true, "0.0.0.000", true);
		
		ipv4test(true, "000.000.000.000", true);
		
		ipv4test(!true, "0000.0.0.0");
		ipv4test(!true, "0.0000.0.0");
		ipv4test(!true, "0.0.0000.0");
		ipv4test(!true, "0.0.0.0000");
		
		ipv4test(!true, ".0.0.0");
		ipv4test(!true, "0..0.0");
		ipv4test(!true, "0.0..0");
		ipv4test(!true, "0.0.0.");
		
		ipv4testOnly(!true, "1:2:3:4:5:6:7:8"); //xxx//fixed
		ipv4testOnly(!true, "::1"); //xxx//fixed
		
		ipv6test(0, ""); // empty string
		
		ipv6test(1,"::1");// loopback, compressed, non-routable
		ipv6test(1,"::", true);// unspecified, compressed, non-routable
		ipv6test(1,"0:0:0:0:0:0:0:1");// loopback, full
		ipv6test(1,"0:0:0:0:0:0:0:0", true);// unspecified, full
		ipv6test(1,"2001:DB8:0:0:8:800:200C:417A");// unicast, full
		ipv6test(1,"FF01:0:0:0:0:0:0:101");// multicast, full
		ipv6test(1,"2001:DB8::8:800:200C:417A");// unicast, compressed
		ipv6test(1,"FF01::101");// multicast, compressed
		ipv6test(0,"2001:DB8:0:0:8:800:200C:417A:221");// unicast, full
		ipv6test(0,"FF01::101::2");// multicast, compressed
		ipv6test(1,"fe80::217:f2ff:fe07:ed62");
		
		ipv6test(0,"[a::b:c:d:1.2.3.4]");//square brackets can enclose ipv6 in host names but not addresses
		ipv6testWithZone(0,"[a::b:c:d:1.2.3.4%x]");//zones not allowed when using []
		ipv6testWithZone(1,"a::b:c:d:1.2.3.4%x");//zones allowed
		ipv6test(0,"[2001:0000:1234:0000:0000:C1C0:ABCD:0876]");//square brackets can enclose ipv6 in host names but not addresses
		ipv6testWithZone(1,"2001:0000:1234:0000:0000:C1C0:ABCD:0876%x");//zones allowed
		ipv6testWithZone(0,"[2001:0000:1234:0000:0000:C1C0:ABCD:0876%x]");//zones not allowed when using []
		
		ipv6test(1,"2001:0000:1234:0000:0000:C1C0:ABCD:0876");
		ipv6test(1,"3ffe:0b00:0000:0000:0001:0000:0000:000a");
		ipv6test(1,"FF02:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(1,"0000:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(1,"0000:0000:0000:0000:0000:0000:0000:0000", true);
		ipv6test(0,"02001:0000:1234:0000:0000:C1C0:ABCD:0876"); // extra 0 not allowed!
		ipv6test(0,"2001:0000:1234:0000:00001:C1C0:ABCD:0876"); // extra 0 not allowed!
		//ipv6test(1," 2001:0000:1234:0000:0000:C1C0:ABCD:0876"); // leading space
		//ipv6test(1,"2001:0000:1234:0000:0000:C1C0:ABCD:0876 "); // trailing space
		//ipv6test(1," 2001:0000:1234:0000:0000:C1C0:ABCD:0876  "); // leading and trailing space
		ipv6test(0,"2001:0000:1234:0000:0000:C1C0:ABCD:0876  0"); // junk after valid address
		ipv6test(0,"0 2001:0000:1234:0000:0000:C1C0:ABCD:0876"); // junk before valid address
		ipv6test(0,"2001:0000:1234: 0000:0000:C1C0:ABCD:0876"); // internal space
		
		ipv6test(0,"3ffe:0b00:0000:0001:0000:0000:000a"); // seven segments
		ipv6test(0,"FF02:0000:0000:0000:0000:0000:0000:0000:0001"); // nine segments
		ipv6test(0,"3ffe:b00::1::a"); // double "::"
		ipv6test(0,"::1111:2222:3333:4444:5555:6666::"); // double "::"
		ipv6test(1,"2::10");
		ipv6test(1,"ff02::1");
		ipv6test(1,"fe80::");
		ipv6test(1,"2002::");
		ipv6test(1,"2001:db8::");
		ipv6test(1,"2001:0db8:1234::");
		ipv6test(1,"::ffff:0:0");
		ipv6test(1,"::1");
		ipv6test(1,"1:2:3:4:5:6:7:8");
		ipv6test(1,"1:2:3:4:5:6::8");
		ipv6test(1,"1:2:3:4:5::8");
		ipv6test(1,"1:2:3:4::8");
		ipv6test(1,"1:2:3::8");
		ipv6test(1,"1:2::8");
		ipv6test(1,"1::8");
		ipv6test(1,"1::2:3:4:5:6:7");
		ipv6test(1,"1::2:3:4:5:6");
		ipv6test(1,"1::2:3:4:5");
		ipv6test(1,"1::2:3:4");
		ipv6test(1,"1::2:3");
		ipv6test(1,"1::8");
		
		ipv6test(1,"::2:3:4:5:6:7:8");
		ipv6test(1,"::2:3:4:5:6:7");
		ipv6test(1,"::2:3:4:5:6");
		ipv6test(1,"::2:3:4:5");
		ipv6test(1,"::2:3:4");
		ipv6test(1,"::2:3");
		ipv6test(1,"::8");
		ipv6test(1,"1:2:3:4:5:6::");
		ipv6test(1,"1:2:3:4:5::");
		ipv6test(1,"1:2:3:4::");
		ipv6test(1,"1:2:3::");
		ipv6test(1,"1:2::");
		ipv6test(1,"1::");
		ipv6test(1,"1:2:3:4:5::7:8");
		ipv6test(0,"1:2:3::4:5::7:8"); // Double "::"
		ipv6test(0,"12345::6:7:8");
		ipv6test(1,"1:2:3:4::7:8");
		ipv6test(1,"1:2:3::7:8");
		ipv6test(1,"1:2::7:8");
		ipv6test(1,"1::7:8");
		
		
		// IPv4 addresses as dotted-quads
		ipv6test(1,"1:2:3:4:5:6:1.2.3.4");
		ipv6test(1,"0:0:0:0:0:0:0.0.0.0", true);
		
		ipv6test(1,"1:2:3:4:5::1.2.3.4");
		ipv6test(1,"0:0:0:0:0::0.0.0.0", true);
		
		ipv6test(1,"0::0.0.0.0", true);
		ipv6test(1,"::0.0.0.0", true);
		
		ipv6test(1,"1:2:3:4::1.2.3.4");
		ipv6test(1,"1:2:3::1.2.3.4");
		ipv6test(1,"1:2::1.2.3.4");
		ipv6test(1,"1::1.2.3.4");
		ipv6test(1,"1:2:3:4::5:1.2.3.4");
		ipv6test(1,"1:2:3::5:1.2.3.4");
		ipv6test(1,"1:2::5:1.2.3.4");
		ipv6test(1,"1::5:1.2.3.4");
		ipv6test(1,"1::5:11.22.33.44");
		ipv6test(0,"1::5:400.2.3.4");
		ipv6test(0,"1::5:260.2.3.4");
		ipv6test(0,"1::5:256.2.3.4");
		ipv6test(0,"1::5:1.256.3.4");
		ipv6test(0,"1::5:1.2.256.4");
		ipv6test(0,"1::5:1.2.3.256");
		ipv6test(0,"1::5:300.2.3.4");
		ipv6test(0,"1::5:1.300.3.4");
		ipv6test(0,"1::5:1.2.300.4");
		ipv6test(0,"1::5:1.2.3.300");
		ipv6test(0,"1::5:900.2.3.4");
		ipv6test(0,"1::5:1.900.3.4");
		ipv6test(0,"1::5:1.2.900.4");
		ipv6test(0,"1::5:1.2.3.900");
		ipv6test(0,"1::5:300.300.300.300");
		ipv6test(0,"1::5:3000.30.30.30");
		ipv6test(0,"1::400.2.3.4");
		ipv6test(0,"1::260.2.3.4");
		ipv6test(0,"1::256.2.3.4");
		ipv6test(0,"1::1.256.3.4");
		ipv6test(0,"1::1.2.256.4");
		ipv6test(0,"1::1.2.3.256");
		ipv6test(0,"1::300.2.3.4");
		ipv6test(0,"1::1.300.3.4");
		ipv6test(0,"1::1.2.300.4");
		ipv6test(0,"1::1.2.3.300");
		ipv6test(0,"1::900.2.3.4");
		ipv6test(0,"1::1.900.3.4");
		ipv6test(0,"1::1.2.900.4");
		ipv6test(0,"1::1.2.3.900");
		ipv6test(0,"1::300.300.300.300");
		ipv6test(0,"1::3000.30.30.30");
		ipv6test(0,"::400.2.3.4");
		ipv6test(0,"::260.2.3.4");
		ipv6test(0,"::256.2.3.4");
		ipv6test(0,"::1.256.3.4");
		ipv6test(0,"::1.2.256.4");
		ipv6test(0,"::1.2.3.256");
		ipv6test(0,"::300.2.3.4");
		ipv6test(0,"::1.300.3.4");
		ipv6test(0,"::1.2.300.4");
		ipv6test(0,"::1.2.3.300");
		ipv6test(0,"::900.2.3.4");
		ipv6test(0,"::1.900.3.4");
		ipv6test(0,"::1.2.900.4");
		ipv6test(0,"::1.2.3.900");
		ipv6test(0,"::300.300.300.300");
		ipv6test(0,"::3000.30.30.30");
		ipv6test(1,"fe80::217:f2ff:254.7.237.98");
		ipv6test(1,"::ffff:192.168.1.26");
		ipv6test(0,"2001:1:1:1:1:1:255Z255X255Y255"); // garbage instead of "." in IPv4
		ipv6test(0,"::ffff:192x168.1.26"); // ditto
		ipv6test(1,"::ffff:192.168.1.1");
		ipv6test(1,"0:0:0:0:0:0:13.1.68.3");// IPv4-compatible IPv6 address, full, deprecated
		ipv6test(1,"0:0:0:0:0:FFFF:129.144.52.38");// IPv4-mapped IPv6 address, full
		ipv6test(1,"::13.1.68.3");// IPv4-compatible IPv6 address, compressed, deprecated
		ipv6test(1,"::FFFF:129.144.52.38");// IPv4-mapped IPv6 address, compressed
		ipv6test(1,"fe80:0:0:0:204:61ff:254.157.241.86");
		ipv6test(1,"fe80::204:61ff:254.157.241.86");
		ipv6test(1,"::ffff:12.34.56.78");
		ipv6test(0,"::ffff:2.3.4");
		ipv6test(0,"::ffff:257.1.2.3");
		ipv6testOnly(0,"1.2.3.4"); //fixed
		
		
		/*
		ipv6test(0,"1.2.3.4:1111:2222:3333:4444::5555");  // Aeron
		ipv6test(0,"1.2.3.4:1111:2222:3333::5555");
		ipv6test(0,"1.2.3.4:1111:2222::5555");
		ipv6test(0,"1.2.3.4:1111::5555");
		ipv6test(0,"1.2.3.4::5555");
		ipv6test(0,"1.2.3.4::");
		*/
		
		
		// Testing IPv4 addresses represented as dotted-quads
		// Leading zero's in IPv4 addresses not allowed: some systems treat the leading "0" in ".086" as the start of an octal number
		// Update: The BNF in RFC-3986 explicitly defines the dec-octet (for IPv4 addresses) not to have a leading zero
		//ipv6test(0,"fe80:0000:0000:0000:0204:61ff:254.157.241.086");
		ipv6test(1,"fe80:0000:0000:0000:0204:61ff:254.157.241.086");//while leading zeros disallowed as indicated, it is something Guardium has always allowed
		ipv6test(1,"::ffff:192.0.2.128");   // this is always OK, since there's a single digit
		ipv6test(0,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:1.2.3.4");
		//ipv6test(0,"1111:2222:3333:4444:5555:6666:00.00.00.00");
		ipv6test(1,"1111:2222:3333:4444:5555:6666:00.00.00.00");//while leadingg zeros disallowed as indicated, it is something Guardium has always allowed
		//ipv6test(0,"1111:2222:3333:4444:5555:6666:000.000.000.000");
		ipv6test(1,"1111:2222:3333:4444:5555:6666:000.000.000.000");//while leadingg zeros disallowed as indicated, it is something Guardium has always allowed
		ipv6test(0,"1111:2222:3333:4444:5555:6666:256.256.256.256");
		
		
		// Not testing address with subnet mask
		// ipv6test(1,"2001:0DB8:0000:CD30:0000:0000:0000:0000/60");// full, with prefix
		// ipv6test(1,"2001:0DB8::CD30:0:0:0:0/60");// compressed, with prefix
		// ipv6test(1,"2001:0DB8:0:CD30::/60");// compressed, with prefix //2
		// ipv6test(1,"::/128");// compressed, unspecified address type, non-routable
		// ipv6test(1,"::1/128");// compressed, loopback address type, non-routable
		// ipv6test(1,"FF00::/8");// compressed, multicast address type
		// ipv6test(1,"FE80::/10");// compressed, link-local unicast, non-routable
		// ipv6test(1,"FEC0::/10");// compressed, site-local unicast, deprecated
		// ipv6test(0,"124.15.6.89/60");// standard IPv4, prefix not allowed
		
		ipv6test(1,"fe80:0000:0000:0000:0204:61ff:fe9d:f156");
		ipv6test(1,"fe80:0:0:0:204:61ff:fe9d:f156");
		ipv6test(1,"fe80::204:61ff:fe9d:f156");
		ipv6test(1,"::1");
		ipv6test(1,"fe80::");
		ipv6test(1,"fe80::1");
		ipv6test(0,":");
		ipv6test(1,"::ffff:c000:280");
		
		// Aeron supplied these test cases
		
		ipv6test(0,"1111:2222:3333:4444::5555:");
		ipv6test(0,"1111:2222:3333::5555:");
		ipv6test(0,"1111:2222::5555:");
		ipv6test(0,"1111::5555:");
		ipv6test(0,"::5555:");
		
		
		ipv6test(0,":::");
		ipv6test(0,"1111:");
		ipv6test(0,":");
		
		
		ipv6test(0,":1111:2222:3333:4444::5555");
		ipv6test(0,":1111:2222:3333::5555");
		ipv6test(0,":1111:2222::5555");
		ipv6test(0,":1111::5555");
		
		
		ipv6test(0,":::5555");
		ipv6test(0,":::");
		
		
		// Additional test cases
		// from http://rt.cpan.org/Public/Bug/Display.html?id=50693
		
		ipv6test(1,"2001:0db8:85a3:0000:0000:8a2e:0370:7334");
		ipv6test(1,"2001:db8:85a3:0:0:8a2e:370:7334");
		ipv6test(1,"2001:db8:85a3::8a2e:370:7334");
		ipv6test(1,"2001:0db8:0000:0000:0000:0000:1428:57ab");
		ipv6test(1,"2001:0db8:0000:0000:0000::1428:57ab");
		ipv6test(1,"2001:0db8:0:0:0:0:1428:57ab");
		ipv6test(1,"2001:0db8:0:0::1428:57ab");
		ipv6test(1,"2001:0db8::1428:57ab");
		ipv6test(1,"2001:db8::1428:57ab");
		ipv6test(1,"0000:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(1,"::1");
		ipv6test(1,"::ffff:0c22:384e");
		ipv6test(1,"2001:0db8:1234:0000:0000:0000:0000:0000");
		ipv6test(1,"2001:0db8:1234:ffff:ffff:ffff:ffff:ffff");
		ipv6test(1,"2001:db8:a::123");
		ipv6test(1,"fe80::");
		
		ipv6test(0,"123");
		ipv6test(0,"ldkfj");
		ipv6test(0,"2001::FFD3::57ab");
		ipv6test(0,"2001:db8:85a3::8a2e:37023:7334");
		ipv6test(0,"2001:db8:85a3::8a2e:370k:7334");
		ipv6test(0,"1:2:3:4:5:6:7:8:9");
		ipv6test(0,"1::2::3");
		ipv6test(0,"1:::3:4:5");
		ipv6test(0,"1:2:3::4:5:6:7:8:9");
		
		// New from Aeron
		ipv6test(1,"1111:2222:3333:4444:5555:6666:7777:8888");
		ipv6test(1,"1111:2222:3333:4444:5555:6666:7777::");
		ipv6test(1,"1111:2222:3333:4444:5555:6666::");
		ipv6test(1,"1111:2222:3333:4444:5555::");
		ipv6test(1,"1111:2222:3333:4444::");
		ipv6test(1,"1111:2222:3333::");
		ipv6test(1,"1111:2222::");
		ipv6test(1,"1111::");
		// ipv6test(1,"::");     //duplicate
		ipv6test(1,"1111:2222:3333:4444:5555:6666::8888");
		ipv6test(1,"1111:2222:3333:4444:5555::8888");
		ipv6test(1,"1111:2222:3333:4444::8888");
		ipv6test(1,"1111:2222:3333::8888");
		ipv6test(1,"1111:2222::8888");
		ipv6test(1,"1111::8888");
		ipv6test(1,"::8888");
		ipv6test(1,"1111:2222:3333:4444:5555::7777:8888");
		ipv6test(1,"1111:2222:3333:4444::7777:8888");
		ipv6test(1,"1111:2222:3333::7777:8888");
		ipv6test(1,"1111:2222::7777:8888");
		ipv6test(1,"1111::7777:8888");
		ipv6test(1,"::7777:8888");
		ipv6test(1,"1111:2222:3333:4444::6666:7777:8888");
		ipv6test(1,"1111:2222:3333::6666:7777:8888");
		ipv6test(1,"1111:2222::6666:7777:8888");
		ipv6test(1,"1111::6666:7777:8888");
		ipv6test(1,"::6666:7777:8888");
		ipv6test(1,"1111:2222:3333::5555:6666:7777:8888");
		ipv6test(1,"1111:2222::5555:6666:7777:8888");
		ipv6test(1,"1111::5555:6666:7777:8888");
		ipv6test(1,"::5555:6666:7777:8888");
		ipv6test(1,"1111:2222::4444:5555:6666:7777:8888");
		ipv6test(1,"1111::4444:5555:6666:7777:8888");
		ipv6test(1,"::4444:5555:6666:7777:8888");
		ipv6test(1,"1111::3333:4444:5555:6666:7777:8888");
		ipv6test(1,"::3333:4444:5555:6666:7777:8888");
		ipv6test(1,"::2222:3333:4444:5555:6666:7777:8888");
		
		
		ipv6test(1,"1111:2222:3333:4444:5555:6666:123.123.123.123");
		ipv6test(1,"1111:2222:3333:4444:5555::123.123.123.123");
		ipv6test(1,"1111:2222:3333:4444::123.123.123.123");
		ipv6test(1,"1111:2222:3333::123.123.123.123");
		ipv6test(1,"1111:2222::123.123.123.123");
		ipv6test(1,"1111::123.123.123.123");
		ipv6test(1,"::123.123.123.123");
		ipv6test(1,"1111:2222:3333:4444::6666:123.123.123.123");
		ipv6test(1,"1111:2222:3333::6666:123.123.123.123");
		ipv6test(1,"1111:2222::6666:123.123.123.123");
		ipv6test(1,"1111::6666:123.123.123.123");
		ipv6test(1,"::6666:123.123.123.123");
		ipv6test(1,"1111:2222:3333::5555:6666:123.123.123.123");
		ipv6test(1,"1111:2222::5555:6666:123.123.123.123");
		ipv6test(1,"1111::5555:6666:123.123.123.123");
		ipv6test(1,"::5555:6666:123.123.123.123");
		ipv6test(1,"1111:2222::4444:5555:6666:123.123.123.123");
		ipv6test(1,"1111::4444:5555:6666:123.123.123.123");
		ipv6test(1,"::4444:5555:6666:123.123.123.123");
		ipv6test(1,"1111::3333:4444:5555:6666:123.123.123.123");
		ipv6test(1,"::2222:3333:4444:5555:6666:123.123.123.123");
		
		ipv6test(0,"1::2:3:4:5:6:1.2.3.4");
		
		ipv6test(1,"::", true);
		ipv6test(1,"0:0:0:0:0:0:0:0", true);
		
		// Playing with combinations of "0" and "::"
		// NB: these are all sytactically correct, but are bad form
		//   because "0" adjacent to "::" should be combined into "::"
		ipv6test(1,"::0:0:0:0:0:0:0", true);
		ipv6test(1,"::0:0:0:0:0:0", true);
		ipv6test(1,"::0:0:0:0:0", true);
		ipv6test(1,"::0:0:0:0", true);
		ipv6test(1,"::0:0:0", true);
		ipv6test(1,"::0:0", true);
		ipv6test(1,"::0", true);
		ipv6test(1,"0:0:0:0:0:0:0::", true);
		ipv6test(1,"0:0:0:0:0:0::", true);
		ipv6test(1,"0:0:0:0:0::", true);
		ipv6test(1,"0:0:0:0::", true);
		ipv6test(1,"0:0:0::", true);
		ipv6test(1,"0:0::", true);
		ipv6test(1,"0::", true);
		
		// New invalid from Aeron
		// Invalid data
		ipv6test(0,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX");
		
		// Too many components
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:9999");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888::");
		ipv6test(0,"::2222:3333:4444:5555:6666:7777:8888:9999");
		
		// Too few components
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777");
		ipv6test(0,"1111:2222:3333:4444:5555:6666");
		ipv6test(0,"1111:2222:3333:4444:5555");
		ipv6test(0,"1111:2222:3333:4444");
		ipv6test(0,"1111:2222:3333");
		ipv6test(0,"1111:2222");
		ipv6test(0,"1111");
		
		// Missing :
		ipv6test(0,"11112222:3333:4444:5555:6666:7777:8888");
		ipv6test(0,"1111:22223333:4444:5555:6666:7777:8888");
		ipv6test(0,"1111:2222:33334444:5555:6666:7777:8888");
		ipv6test(0,"1111:2222:3333:44445555:6666:7777:8888");
		ipv6test(0,"1111:2222:3333:4444:55556666:7777:8888");
		ipv6test(0,"1111:2222:3333:4444:5555:66667777:8888");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:77778888");
		
		// Missing : intended for ::
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:");
		ipv6test(0,"1111:2222:3333:4444:5555:");
		ipv6test(0,"1111:2222:3333:4444:");
		ipv6test(0,"1111:2222:3333:");
		ipv6test(0,"1111:2222:");
		ipv6test(0,"1111:");
		ipv6test(0,":");
		ipv6test(0,":8888");
		ipv6test(0,":7777:8888");
		ipv6test(0,":6666:7777:8888");
		ipv6test(0,":5555:6666:7777:8888");
		ipv6test(0,":4444:5555:6666:7777:8888");
		ipv6test(0,":3333:4444:5555:6666:7777:8888");
		ipv6test(0,":2222:3333:4444:5555:6666:7777:8888");
		ipv6test(0,":1111:2222:3333:4444:5555:6666:7777:8888");
		
		// :::
		ipv6test(0,":::2222:3333:4444:5555:6666:7777:8888");
		ipv6test(0,"1111:::3333:4444:5555:6666:7777:8888");
		ipv6test(0,"1111:2222:::4444:5555:6666:7777:8888");
		ipv6test(0,"1111:2222:3333:::5555:6666:7777:8888");
		ipv6test(0,"1111:2222:3333:4444:::6666:7777:8888");
		ipv6test(0,"1111:2222:3333:4444:5555:::7777:8888");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:::8888");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:::");
		
		// Double ::");
		ipv6test(0,"::2222::4444:5555:6666:7777:8888");
		ipv6test(0,"::2222:3333::5555:6666:7777:8888");
		ipv6test(0,"::2222:3333:4444::6666:7777:8888");
		ipv6test(0,"::2222:3333:4444:5555::7777:8888");
		ipv6test(0,"::2222:3333:4444:5555:7777::8888");
		ipv6test(0,"::2222:3333:4444:5555:7777:8888::");
		
		ipv6test(0,"1111::3333::5555:6666:7777:8888");
		ipv6test(0,"1111::3333:4444::6666:7777:8888");
		ipv6test(0,"1111::3333:4444:5555::7777:8888");
		ipv6test(0,"1111::3333:4444:5555:6666::8888");
		ipv6test(0,"1111::3333:4444:5555:6666:7777::");
		
		ipv6test(0,"1111:2222::4444::6666:7777:8888");
		ipv6test(0,"1111:2222::4444:5555::7777:8888");
		ipv6test(0,"1111:2222::4444:5555:6666::8888");
		ipv6test(0,"1111:2222::4444:5555:6666:7777::");
		
		ipv6test(0,"1111:2222:3333::5555::7777:8888");
		ipv6test(0,"1111:2222:3333::5555:6666::8888");
		ipv6test(0,"1111:2222:3333::5555:6666:7777::");
		
		ipv6test(0,"1111:2222:3333:4444::6666::8888");
		ipv6test(0,"1111:2222:3333:4444::6666:7777::");
		
		ipv6test(0,"1111:2222:3333:4444:5555::7777::");
		
		
		
		// Too many components"
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:5555:6666::1.2.3.4");
		ipv6test(0,"::2222:3333:4444:5555:6666:7777:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:1.2.3.4.5");
		
		// Too few components
		ipv6test(0,"1111:2222:3333:4444:5555:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:1.2.3.4");
		ipv6test(0,"1111:2222:3333:1.2.3.4");
		ipv6test(0,"1111:2222:1.2.3.4");
		ipv6test(0,"1111:1.2.3.4");
		ipv6testOnly(0,"1.2.3.4"); //fixed
		
		// Missing :
		ipv6test(0,"11112222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:22223333:4444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:33334444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:44445555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:55556666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:5555:66661.2.3.4");
		
		// Missing .
		ipv6test(0,"1111:2222:3333:4444:5555:6666:255255.255.255");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:255.255255.255");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:255.255.255255");
		
		
		// Missing : intended for ::
		ipv6test(0,":1.2.3.4");
		ipv6test(0,":6666:1.2.3.4");
		ipv6test(0,":5555:6666:1.2.3.4");
		ipv6test(0,":4444:5555:6666:1.2.3.4");
		ipv6test(0,":3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,":2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,":1111:2222:3333:4444:5555:6666:1.2.3.4");
		
		// :::
		ipv6test(0,":::2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:::3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:::4444:5555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:::5555:6666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:::6666:1.2.3.4");
		ipv6test(0,"1111:2222:3333:4444:5555:::1.2.3.4");
		
		// Double ::
		ipv6test(0,"::2222::4444:5555:6666:1.2.3.4");
		ipv6test(0,"::2222:3333::5555:6666:1.2.3.4");
		ipv6test(0,"::2222:3333:4444::6666:1.2.3.4");
		ipv6test(0,"::2222:3333:4444:5555::1.2.3.4");
		
		ipv6test(0,"1111::3333::5555:6666:1.2.3.4");
		ipv6test(0,"1111::3333:4444::6666:1.2.3.4");
		ipv6test(0,"1111::3333:4444:5555::1.2.3.4");
		
		ipv6test(0,"1111:2222::4444::6666:1.2.3.4");
		ipv6test(0,"1111:2222::4444:5555::1.2.3.4");
		
		ipv6test(0,"1111:2222:3333::5555::1.2.3.4");
		
		
		
		// Missing parts
		ipv6test(0,"::.");
		ipv6test(0,"::..");
		ipv6test(0,"::...");
		ipv6test(0,"::1...");
		ipv6test(0,"::1.2..");
		ipv6test(0,"::1.2.3.");
		ipv6test(0,"::.2..");
		ipv6test(0,"::.2.3.");
		ipv6test(0,"::.2.3.4");
		ipv6test(0,"::..3.");
		ipv6test(0,"::..3.4");
		ipv6test(0,"::...4");
		
		
		// Extra : in front
		ipv6test(0,":1111:2222:3333:4444:5555:6666:7777::");
		ipv6test(0,":1111:2222:3333:4444:5555:6666::");
		ipv6test(0,":1111:2222:3333:4444:5555::");
		ipv6test(0,":1111:2222:3333:4444::");
		ipv6test(0,":1111:2222:3333::");
		ipv6test(0,":1111:2222::");
		ipv6test(0,":1111::");
		ipv6test(0,":::");
		ipv6test(0,":1111:2222:3333:4444:5555:6666::8888");
		ipv6test(0,":1111:2222:3333:4444:5555::8888");
		ipv6test(0,":1111:2222:3333:4444::8888");
		ipv6test(0,":1111:2222:3333::8888");
		ipv6test(0,":1111:2222::8888");
		ipv6test(0,":1111::8888");
		ipv6test(0,":::8888");
		ipv6test(0,":1111:2222:3333:4444:5555::7777:8888");
		ipv6test(0,":1111:2222:3333:4444::7777:8888");
		ipv6test(0,":1111:2222:3333::7777:8888");
		ipv6test(0,":1111:2222::7777:8888");
		ipv6test(0,":1111::7777:8888");
		ipv6test(0,":::7777:8888");
		ipv6test(0,":1111:2222:3333:4444::6666:7777:8888");
		ipv6test(0,":1111:2222:3333::6666:7777:8888");
		ipv6test(0,":1111:2222::6666:7777:8888");
		ipv6test(0,":1111::6666:7777:8888");
		ipv6test(0,":::6666:7777:8888");
		ipv6test(0,":1111:2222:3333::5555:6666:7777:8888");
		ipv6test(0,":1111:2222::5555:6666:7777:8888");
		ipv6test(0,":1111::5555:6666:7777:8888");
		ipv6test(0,":::5555:6666:7777:8888");
		ipv6test(0,":1111:2222::4444:5555:6666:7777:8888");
		ipv6test(0,":1111::4444:5555:6666:7777:8888");
		ipv6test(0,":::4444:5555:6666:7777:8888");
		ipv6test(0,":1111::3333:4444:5555:6666:7777:8888");
		ipv6test(0,":::3333:4444:5555:6666:7777:8888");
		ipv6test(0,":::2222:3333:4444:5555:6666:7777:8888");
		
		
		ipv6test(0,":1111:2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,":1111:2222:3333:4444:5555::1.2.3.4");
		ipv6test(0,":1111:2222:3333:4444::1.2.3.4");
		ipv6test(0,":1111:2222:3333::1.2.3.4");
		ipv6test(0,":1111:2222::1.2.3.4");
		ipv6test(0,":1111::1.2.3.4");
		ipv6test(0,":::1.2.3.4");
		ipv6test(0,":1111:2222:3333:4444::6666:1.2.3.4");
		ipv6test(0,":1111:2222:3333::6666:1.2.3.4");
		ipv6test(0,":1111:2222::6666:1.2.3.4");
		ipv6test(0,":1111::6666:1.2.3.4");
		ipv6test(0,":::6666:1.2.3.4");
		ipv6test(0,":1111:2222:3333::5555:6666:1.2.3.4");
		ipv6test(0,":1111:2222::5555:6666:1.2.3.4");
		ipv6test(0,":1111::5555:6666:1.2.3.4");
		ipv6test(0,":::5555:6666:1.2.3.4");
		ipv6test(0,":1111:2222::4444:5555:6666:1.2.3.4");
		ipv6test(0,":1111::4444:5555:6666:1.2.3.4");
		ipv6test(0,":::4444:5555:6666:1.2.3.4");
		ipv6test(0,":1111::3333:4444:5555:6666:1.2.3.4");
		ipv6test(0,":::2222:3333:4444:5555:6666:1.2.3.4");
		
		
		// Extra : at end
		ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:::");
		ipv6test(0,"1111:2222:3333:4444:5555:6666:::");
		ipv6test(0,"1111:2222:3333:4444:5555:::");
		ipv6test(0,"1111:2222:3333:4444:::");
		ipv6test(0,"1111:2222:3333:::");
		ipv6test(0,"1111:2222:::");
		ipv6test(0,"1111:::");
		ipv6test(0,":::");
		ipv6test(0,"1111:2222:3333:4444:5555:6666::8888:");
		ipv6test(0,"1111:2222:3333:4444:5555::8888:");
		ipv6test(0,"1111:2222:3333:4444::8888:");
		ipv6test(0,"1111:2222:3333::8888:");
		ipv6test(0,"1111:2222::8888:");
		ipv6test(0,"1111::8888:");
		ipv6test(0,"::8888:");
		ipv6test(0,"1111:2222:3333:4444:5555::7777:8888:");
		ipv6test(0,"1111:2222:3333:4444::7777:8888:");
		ipv6test(0,"1111:2222:3333::7777:8888:");
		ipv6test(0,"1111:2222::7777:8888:");
		ipv6test(0,"1111::7777:8888:");
		ipv6test(0,"::7777:8888:");
		ipv6test(0,"1111:2222:3333:4444::6666:7777:8888:");
		ipv6test(0,"1111:2222:3333::6666:7777:8888:");
		ipv6test(0,"1111:2222::6666:7777:8888:");
		ipv6test(0,"1111::6666:7777:8888:");
		ipv6test(0,"::6666:7777:8888:");
		ipv6test(0,"1111:2222:3333::5555:6666:7777:8888:");
		ipv6test(0,"1111:2222::5555:6666:7777:8888:");
		ipv6test(0,"1111::5555:6666:7777:8888:");
		ipv6test(0,"::5555:6666:7777:8888:");
		ipv6test(0,"1111:2222::4444:5555:6666:7777:8888:");
		ipv6test(0,"1111::4444:5555:6666:7777:8888:");
		ipv6test(0,"::4444:5555:6666:7777:8888:");
		ipv6test(0,"1111::3333:4444:5555:6666:7777:8888:");
		ipv6test(0,"::3333:4444:5555:6666:7777:8888:");
		ipv6test(0,"::2222:3333:4444:5555:6666:7777:8888:");
		
		// Additional cases: http://crisp.tweakblogs.net/blog/2031/ipv6-validation-%28and-caveats%29.html
		ipv6test(1,"0:a:b:c:d:e:f::");
		ipv6test(1,"::0:a:b:c:d:e:f"); // syntactically correct, but bad form (::0:... could be combined)
		ipv6test(1,"a:b:c:d:e:f:0::");
		ipv6test(0,"':10.0.0.1");


		showMessage("pass count: " + (numTested - numFailed));
		showMessage("fail count: " + numFailed);
		String falseRejects = "";
		String failurestr = "";
		int falseRejectCount = 0;
		int failurestrCount = 0;
		
		for(IPAddressTest.Failure f : failures) {
			String addrStrng = f.addr == null ? f.addrValue.toString() : f.addr.toString();
			if(f.pass) {
				if(f.str != null && f.str.length() > 0) {
					falseRejects += " " + f.str +  ": " + addrStrng;
					falseRejectCount++;
				} else {
					falseRejects += ' ' + addrStrng;
					falseRejectCount++;
				}
			} else {
				failurestr += ' ' + addrStrng;
				failurestrCount++;
			}
		}
		
		if(falseRejectCount > 0) {
			//showMessage("False Rejects:\n" + falseRejects);
			showMessage("Failed:\n" + falseRejects);
		}
		if(failurestrCount > 0) {
			//showMessage("Failed to Reject:\n" + failurestr);
			showMessage("Failed:\n" + failurestr);
		}
	}
	
	void showMessage(String s) {
		System.out.println(s);
	}
}