/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import com.guardium.net.IPAddress.RangeOptions;
import com.guardium.net.IPAddressString.IPAddressValidationOptions;


public class IPAddressRangeTest extends IPAddressTest {
		
	public static void main(String args[]) {
		new IPAddressRangeTest().test();
	}
	
	@Override
	protected IPAddressString createAddress(String x) {
		return createAddress(x, RangeOptions.WILDCARD_ONLY);
	}
	
	@Override
	protected IPAddressString createAddress(String x, RangeOptions options) {
		if(x.indexOf('%') != -1) {//do not treat the '%' like a zone, treat it like a wildcard
			return new IPAddressString(x, IPAddressString.DEFAULT_RANGE_VALIDATION_OPTIONS);
		}
		IPAddressValidationOptions mixedOptions = new IPAddressValidationOptions(false, true, false, options, true, false);
		IPAddressValidationOptions opts = new IPAddressValidationOptions(false, true, true, mixedOptions, true, options, true);
		return new IPAddressString(x, opts);
	}
	
	@Override
	void ipv6testWithZone(int pass, String x) {
		return;
	}
	
	@Override
	void ipv6testWithZone(boolean pass, String x) {
		return;
	}
	
	@Override
	void testBytes(IPAddress origAddr) {
		if(origAddr.isMultiple()) {
			try {
				origAddr.getBytes();
				numFailed++;
				addFailure(new Failure("wildcard bytes on addr ", origAddr));
			} catch(IllegalStateException e) {
				//pass
				//wild addresses have no bytes
			}
		} else {
			super.testBytes(origAddr);
		}
	}
	
	@Override
	void testMaskBytes(String cidr2, IPAddressString w2)
			throws IPAddressException {
		IPAddress addr = w2.toValue();
		testBytes(addr);
	}
	
	void testCount(String original, int number, RangeOptions rangeOptions) {
		IPAddressString w = createAddress(original, rangeOptions);
		testCount(w, number);
	}
	
	void testWildcarded(String original, int bits, String expected) {
		testWildcarded(original, bits, expected, expected);
	}
	
	void testWildcarded(String original, int bits, String expectedNormalized, String expectedCompressed) {
		IPAddressString w = createAddress(original);
		IPAddress addr = w.getValue();
		addr = addr.toSubnet(bits);
		String string = addr.toCompressedWildcardString();
		if(!string.equals(expectedCompressed)) {
			numFailed++;
			addFailure(new Failure("failed expected: " + expectedCompressed + " actual: " + string, w));
		} else {
			IPAddressString w2 = createAddress(original + '/' + bits);
			IPAddress addr2 = w2.getValue();
			string = addr2.toCompressedWildcardString();
			if(!string.equals(expectedCompressed)) {
				numFailed++;
				addFailure(new Failure("failed expected: " + expectedCompressed + " actual: " + string, w));
			}
			string = addr.toNormalizedWildcardString();
			if(!string.equals(expectedNormalized)) {
				numFailed++;
				addFailure(new Failure("failed expected: " + expectedNormalized + " actual: " + string, w));
			} else {
				w2 = createAddress(original + '/' + bits);
				addr2 = w2.getValue();
				string = addr2.toNormalizedWildcardString();
				if(!string.equals(expectedNormalized)) {
					numFailed++;
					addFailure(new Failure("failed expected: " + expectedNormalized + " actual: " + string, w));
				}
			}
		}
		numTested++;
	}
	
	 
	
	@Override
	void test() {
		testMasks("9.*.237.26/0", "0.0.0.0/0");
		testMasks("9.*.237.26/1", "0.0.0.0/1");
		testMasks("9.*.237.26/4", "0.0.0.0/4");
		testMasks("9.*.237.26/5", "8.0.0.0/5");
		testMasks("9.*.237.26/7", "8.0.0.0/7");
		testMasks("9.*.237.26/8", "9.0.0.0/8");
		testMasks("9.*.237.26/9", "9.*.0.0/9");
		testMasks("9.*.237.26/16", "9.*.0.0/16");
		testMasks("9.*.237.26/30", "9.*.237.24/30");//the mask makes these two the same
		testMasks("9.*.237.26/32", "9.*.237.26/32", true);
		
		ipv4test(true, "1.2.*.4/1");
		ipv4test(false, "1.2.*.4/-1");
		ipv4test(false, "1.2.*.4/");
		ipv4test(false, "1.2.*.4/x");
		ipv4test(true, "1.2.*.4/33");//we are allowing extra-large prefixes
		ipv6test(true, "1:*::1/1");
		ipv6test(false, "1:*::1/-1");
		ipv6test(false, "1:*::1/");
		ipv6test(false, "1:*::1/x");
		ipv6test(true, "1:*::1/129");//we are allowing extra-large prefixes
		
		testResolved("sfoley1.guard.swg.usma.ibm.com", "9.32.237.26");
		testResolved("vx38.guard.swg.usma.ibm.com", "9.70.146.84");
		testResolved("9.*.237.26", "9.*.237.26");
		
		testResolved("2001:*:0:0:8:800:200C:417A", "2001:*:0:0:8:800:200C:417A");
		
		testNormalized("ABCD:EF12:*:*:%:A:%:BBBB", "abcd:ef12:*:*:%:a:%:bbbb");
		
		testNormalized("1.*", "1.*.*.*");
		testNormalized("*.1.*", "*.1.*.*");
		testNormalized("*:1::*", "*:1::*");
		testNormalized("*:1:*", "*:1:*:*:*:*:*:*");
		
		
		testWildcarded("1.2.3.4", 8, "1.*.*.*");
		testWildcarded("1.2.3.4", 9, "1.0-127.*.*");
		testWildcarded("1.2.3.4", 15, "1.2-3.*.*");
		testWildcarded("1.3.3.4", 15, "1.2-3.*.*");
		testWildcarded("1.2.3.4", 16, "1.2.*.*");
		testWildcarded("1::", 16, "1:*:*:*:*:*:*:*", "1::/16");
		testWildcarded("1::", 17, "1:0-7fff:*:*:*:*:*:*", "1::/17");
		testWildcarded("1::", 31, "1:0-1:*:*:*:*:*:*", "1::/31");
		
		testCount("1.2.3.4/32", 1);
		testCount("1.2.3.4/31", 2);
		testCount("1.2.3.4/30", 4);
		testCount("1.1-2.3.4", 2, RangeOptions.WILDCARD_AND_RANGE);
		testCount("1.*.3.4", 256);
		
		//these can take a while, since they generate 48640, 65536, and 32758 addresses respectively
		testCount("1.*.11-200.4", 190 * 256, RangeOptions.WILDCARD_AND_RANGE);
		testCount("1.3.*.4/16", 256 * 256);
		testCount("1.2.*.1-3/25", 256 * 128, RangeOptions.WILDCARD_AND_RANGE);
		
		ipv4test(true, "1.1.*.100-101", RangeOptions.WILDCARD_AND_RANGE);
		ipv4test(false, "1.2.*.101-100", RangeOptions.WILDCARD_AND_RANGE);//downwards range
		ipv4test(false, "1.2.*.101-101", RangeOptions.WILDCARD_AND_RANGE);//downwards range
		ipv6test(true, "1:2:4:a-ff:0-2::1", RangeOptions.WILDCARD_AND_RANGE);
		ipv6test(false, "1:2:4:ff-a:0-2::1", RangeOptions.WILDCARD_AND_RANGE);//downwards range
		ipv4test(false, "1.2.*.101-100/24", RangeOptions.WILDCARD_AND_RANGE);//downwards range but ignored due to CIDR
		
		
		ipv4test(true, "*");
		
		ipv4test(true, "1.*.3");
		
		ipv4test(!true, "a.*.3.4");
		ipv4test(!true, "*.a.3.4");
		ipv4test(!true, "1.*.a.4");
		ipv4test(!true, "1.*.3.a");
		
		ipv4test(!true, ".2.3.*");
		ipv4test(!true, "1..*.4");
		ipv4test(!true, "1.*..4");
		ipv4test(!true, "*.2.3.");
		
		ipv4test(!true, "256.*.3.4");
		ipv4test(!true, "1.256.*.4");
		ipv4test(!true, "*.2.256.4");
		ipv4test(!true, "1.*.3.256");
		
		
		ipv4test(true, "0.0.*.0", false);
		ipv4test(true, "00.*.0.0", false);
		ipv4test(true, "0.00.*.0", false);
		ipv4test(true, "0.*.00.0", false);
		ipv4test(true, "*.0.0.00", false);
		ipv4test(true, "000.0.*.0", false);
		ipv4test(true, "0.000.0.*", false);
		ipv4test(true, "*.0.000.0", false);
		ipv4test(true, "0.0.*.000", false);
		
		ipv4test(true, "0.0.*.0", false);
		ipv4test(true, "00.*.0.0", false);
		ipv4test(true, "0.00.*.0", false);
		ipv4test(true, "0.*.00.0", false);
		ipv4test(true, "*.0.0.00", false); 
		ipv4test(true, "000.0.*.0", false);
		ipv4test(true, "0.000.0.*", false);
		ipv4test(true, "*.0.000.0", false);
		ipv4test(true, "0.0.*.000", false);
		
		ipv4test(true, "000.000.000.*", false);
		
		ipv4test(!true, "0000.0.*.0");
		ipv4test(!true, "*.0000.0.0");
		ipv4test(!true, "0.*.0000.0");
		ipv4test(!true, "*.0.0.0000");
		
		ipv4test(!true, ".0.*.0");
		ipv4test(!true, "0..*.0");
		ipv4test(!true, "0.*..0");
		ipv4test(!true, "*.0.0.");
		

		ipv4testOnly(!true, "1:2:3:4:5:*:7:8"); //xxx//fixed
		ipv4testOnly(!true, "*::1"); //fixed
		
		ipv6test(1, "*");// empty string
		
		ipv6test(1,"*::1");// loopback, compressed, non-routable
		
		//this one test can take a while, since it generates (0xffff + 1) = 65536 addresses
		testCount("*::1", 0xffff + 1);
		
		testCount("1-3::1", 3, RangeOptions.WILDCARD_AND_RANGE);
		testCount("0-299::1", 0x299 + 1, RangeOptions.WILDCARD_AND_RANGE);
		
		//this one test can take a while, since it generates 3 * (0xffff + 1) = 196606 addresses
		testCount("1:2:4:*:0-2::1", 3 * (0xffff + 1), RangeOptions.WILDCARD_AND_RANGE);
		
		testCount("1:2:4:0-2:0-2::1", 3 * 3, RangeOptions.WILDCARD_AND_RANGE);
		
		ipv6test(1,"::*", false);// unspecified, compressed, non-routable
		ipv6test(1,"0:0:*:0:0:0:0:1");// loopback, full
		ipv6test(1,"0:0:*:0:0:0:0:0", false);// unspecified, full
		ipv6test(1,"2001:*:0:0:8:800:200C:417A");// unicast, full
		ipv6test(1,"FF01:*:0:0:0:0:0:101");// multicast, full
		ipv6test(1,"2001:DB8::8:800:200C:*");// unicast, compressed
		ipv6test(1,"FF01::*:101");// multicast, compressed
		ipv6test(0,"2001:DB8:0:0:8:*:200C:417A:221");// unicast, full
		ipv6test(0,"FF01::101::*");// multicast, compressed
		ipv6test(1,"fe80::217:f2ff:*:ed62");
		
		
		
		ipv6test(1,"2001:*:1234:0000:0000:C1C0:ABCD:0876");
		ipv6test(1,"3ffe:0b00:0000:0000:0001:0000:*:000a");
		ipv6test(1,"FF02:0000:0000:0000:0000:0000:*:0001");
		ipv6test(1,"*:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(0,"0000:0000:0000:0000:*0000:0000:0000:*0", true);
		ipv6test(0,"02001:*:1234:0000:0000:C1C0:ABCD:0876"); // extra 0 not allowed!
		ipv6test(0,"2001:0000:1234:0000:0*:C1C0:ABCD:0876"); // extra 0 not allowed!
		ipv6test(1,"2001:0000:1234:0000:*:C1C0:ABCD:0876"); 
		
		//ipv6test(1," 2001:0000:1234:0000:0000:C1C0:ABCD:0876"); // leading space
		//ipv6test(1,"2001:0000:1234:0000:0000:C1C0:ABCD:0876 "); // trailing space
		//ipv6test(1," 2001:0000:1234:0000:0000:C1C0:ABCD:0876  "); // leading and trailing space
		
		ipv6test(0,"2001:0000:1234:0000:0000:C1C0*:ABCD:0876  0"); // junk after valid address
		ipv6test(0,"0 2001:0000:123*:0000:0000:C1C0:ABCD:0876"); // junk before valid address
		ipv6test(0,"2001:0000:1234: 0000:0000:C1C0:*:0876"); // internal space
		
		
		
		ipv6test(1,"3ffe:0b00:*:0001:0000:0000:000a");
		ipv6test(0,"3ffe:0b00:1:0001:0000:0000:000a"); // seven segments
		ipv6test(0,"FF02:0000:0000:0000:0000:0000:0000:*:0001"); // nine segments
		ipv6test(0,"3ffe:*::1::a"); // double "::"
		ipv6test(0,"::1111:2222:3333:4444:5555:*::"); // double "::"
		ipv6test(1,"2::10");
		ipv6test(1,"ff02::1");
		ipv6test(1,"fe80:*::");
		ipv6test(1,"2002:*::");
		ipv6test(1,"2001:*::");
		ipv6test(1,"*:0db8:1234::");
		ipv6test(1,"::ffff:*:0");
		ipv6test(1,"*::1");
		ipv6test(1,"1:2:3:4:*:6:7:8");
		ipv6test(1,"1:2:*:4:5:6::8");
		ipv6test(1,"1:2:3:4:5::*");
		ipv6test(1,"1:2:3:*::8");
		ipv6test(1,"1:2:3::8");
		ipv6test(1,"*:2::8");
		ipv6test(1,"1::*");
		ipv6test(1,"*::2:3:4:5:6:7");
		ipv6test(1,"*::2:3:4:5:6");
		ipv6test(1,"1::2:3:4:*");
		ipv6test(1,"1::2:*:4");
		ipv6test(1,"1::*:3");
		ipv6test(1,"1::*");
		
		ipv6test(1,"::*:3:4:5:6:7:8");
		ipv6test(1,"*::2:3:4:5:6:7");
		ipv6test(1,"::*:3:4:5:6");
		ipv6test(1,"::*:3:4:5");
		ipv6test(1,"::2:3:*");
		ipv6test(1,"*::2:3");
		ipv6test(1,"::*");
		ipv6test(1,"1:*:3:4:5:6::");
		ipv6test(1,"1:2:3:4:*::");
		ipv6test(1,"1:2:3:*::");
		ipv6test(1,"1:2:3::*");
		ipv6test(1,"*:2::");
		ipv6test(1,"*::");
		ipv6test(1,"*:2:3:4:5::7:8");
		ipv6test(0,"1:2:3::4:5::7:*"); // Double "::"
		ipv6test(0,"12345::6:7:*");
		ipv6test(1,"1:2:3:4::*:*");
		ipv6test(1,"1:*:3::7:8");
		ipv6test(1,"*:*::7:8");
		ipv6test(1,"*::*:8");
//			
//			
//			// IPv4 addresses as dotted-quads
//			ipv6test(1,"1:2:3:4:5:6:1.2.3.4");
//			ipv6test(1,"0:0:0:0:0:0:0.0.0.0", true);
//			
//			ipv6test(1,"1:2:3:4:5::1.2.3.4");
//			ipv6test(1,"0:0:0:0:0::0.0.0.0", true);
//			
//			ipv6test(1,"0::0.0.0.0", true);
//			ipv6test(1,"::0.0.0.0", true);
//			
//			ipv6test(1,"1:2:3:4::1.2.3.4");
//			ipv6test(1,"1:2:3::1.2.3.4");
//			ipv6test(1,"1:2::1.2.3.4");
//			ipv6test(1,"1::1.2.3.4");
//			ipv6test(1,"1:2:3:4::5:1.2.3.4");
//			ipv6test(1,"1:2:3::5:1.2.3.4");
//			ipv6test(1,"1:2::5:1.2.3.4");
//			ipv6test(1,"1::5:1.2.3.4");
//			ipv6test(1,"1::5:11.22.33.44");
//			ipv6test(0,"1::5:400.2.3.4");
//			ipv6test(0,"1::5:260.2.3.4");
//			ipv6test(0,"1::5:256.2.3.4");
//			ipv6test(0,"1::5:1.256.3.4");
//			ipv6test(0,"1::5:1.2.256.4");
//			ipv6test(0,"1::5:1.2.3.256");
//			ipv6test(0,"1::5:300.2.3.4");
//			ipv6test(0,"1::5:1.300.3.4");
//			ipv6test(0,"1::5:1.2.300.4");
//			ipv6test(0,"1::5:1.2.3.300");
//			ipv6test(0,"1::5:900.2.3.4");
//			ipv6test(0,"1::5:1.900.3.4");
//			ipv6test(0,"1::5:1.2.900.4");
//			ipv6test(0,"1::5:1.2.3.900");
//			ipv6test(0,"1::5:300.300.300.300");
//			ipv6test(0,"1::5:3000.30.30.30");
//			ipv6test(0,"1::400.2.3.4");
//			ipv6test(0,"1::260.2.3.4");
//			ipv6test(0,"1::256.2.3.4");
//			ipv6test(0,"1::1.256.3.4");
//			ipv6test(0,"1::1.2.256.4");
//			ipv6test(0,"1::1.2.3.256");
//			ipv6test(0,"1::300.2.3.4");
//			ipv6test(0,"1::1.300.3.4");
//			ipv6test(0,"1::1.2.300.4");
//			ipv6test(0,"1::1.2.3.300");
//			ipv6test(0,"1::900.2.3.4");
//			ipv6test(0,"1::1.900.3.4");
//			ipv6test(0,"1::1.2.900.4");
//			ipv6test(0,"1::1.2.3.900");
//			ipv6test(0,"1::300.300.300.300");
//			ipv6test(0,"1::3000.30.30.30");
//			ipv6test(0,"::400.2.3.4");
//			ipv6test(0,"::260.2.3.4");
//			ipv6test(0,"::256.2.3.4");
//			ipv6test(0,"::1.256.3.4");
//			ipv6test(0,"::1.2.256.4");
//			ipv6test(0,"::1.2.3.256");
//			ipv6test(0,"::300.2.3.4");
//			ipv6test(0,"::1.300.3.4");
//			ipv6test(0,"::1.2.300.4");
//			ipv6test(0,"::1.2.3.300");
//			ipv6test(0,"::900.2.3.4");
//			ipv6test(0,"::1.900.3.4");
//			ipv6test(0,"::1.2.900.4");
//			ipv6test(0,"::1.2.3.900");
//			ipv6test(0,"::300.300.300.300");
//			ipv6test(0,"::3000.30.30.30");
//			ipv6test(1,"fe80::217:f2ff:254.7.237.98");
//			ipv6test(1,"::ffff:192.168.1.26");
//			ipv6test(0,"2001:1:1:1:1:1:255Z255X255Y255"); // garbage instead of "." in IPv4
//			ipv6test(0,"::ffff:192x168.1.26"); // ditto
//			ipv6test(1,"::ffff:192.168.1.1");
//			ipv6test(1,"0:0:0:0:0:0:13.1.68.3");// IPv4-compatible IPv6 address, full, deprecated
//			ipv6test(1,"0:0:0:0:0:FFFF:129.144.52.38");// IPv4-mapped IPv6 address, full
//			ipv6test(1,"::13.1.68.3");// IPv4-compatible IPv6 address, compressed, deprecated
//			ipv6test(1,"::FFFF:129.144.52.38");// IPv4-mapped IPv6 address, compressed
//			ipv6test(1,"fe80:0:0:0:204:61ff:254.157.241.86");
//			ipv6test(1,"fe80::204:61ff:254.157.241.86");
//			ipv6test(1,"::ffff:12.34.56.78");
//			ipv6test(0,"::ffff:2.3.4");
//			ipv6test(0,"::ffff:257.1.2.3");
//			ipv6testOnly(0,"1.2.3.4"); //fixed
//			
//			
//			
////			ipv6test(0,"1.2.3.4:1111:2222:3333:4444::5555");  // Aeron
////			ipv6test(0,"1.2.3.4:1111:2222:3333::5555");
////			ipv6test(0,"1.2.3.4:1111:2222::5555");
////			ipv6test(0,"1.2.3.4:1111::5555");
////			ipv6test(0,"1.2.3.4::5555");
////			ipv6test(0,"1.2.3.4::");
//			
//			
//			
		// Testing IPv4 addresses represented as dotted-quads
		// Leading zero's in IPv4 addresses not allowed: some systems treat the leading "0" in ".086" as the start of an octal number
		// Update: The BNF in RFC-3986 explicitly defines the dec-octet (for IPv4 addresses) not to have a leading zero
		//ipv6test(0,"fe80:0000:0000:*:0204:61ff:254.157.241.086");
		ipv6test(1,"fe80:0000:0000:*:0204:61ff:254.157.241.086");//Guardium has always allowed leading zeros
		ipv6test(1,"::*:192.0.*.128");   // but this is OK, since there's a single digit
		ipv6test(0,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:1.2.3.4");
		//ipv6test(0,"1111:2222:*:4444:5555:6666:00.00.00.00");
		ipv6test(1,"1111:2222:*:4444:5555:6666:00.00.00.00");//Guardium has always allowed leading zeros
		//ipv6test(0,"1111:2222:3333:4444:5555:6666:000.*.000.000");
		ipv6test(1,"1111:2222:3333:4444:5555:6666:000.*.000.000");//Guardium has always allowed leading zeros
		ipv6test(0,"*:2222:3333:4444:5555:6666:256.256.256.256");
//			
//			
//			// Not testing address with subnet mask
//			// ipv6test(1,"2001:0DB8:0000:CD30:0000:0000:0000:0000/60");// full, with prefix
//			// ipv6test(1,"2001:0DB8::CD30:0:0:0:0/60");// compressed, with prefix
//			// ipv6test(1,"2001:0DB8:0:CD30::/60");// compressed, with prefix //2
//			// ipv6test(1,"::/128");// compressed, unspecified address type, non-routable
//			// ipv6test(1,"::1/128");// compressed, loopback address type, non-routable
//			// ipv6test(1,"FF00::/8");// compressed, multicast address type
//			// ipv6test(1,"FE80::/10");// compressed, link-local unicast, non-routable
//			// ipv6test(1,"FEC0::/10");// compressed, site-local unicast, deprecated
//			// ipv6test(0,"124.15.6.89/60");// standard IPv4, prefix not allowed
//			
//			ipv6test(1,"fe80:0000:0000:0000:0204:61ff:fe9d:f156");
//			ipv6test(1,"fe80:0:0:0:204:61ff:fe9d:f156");
//			ipv6test(1,"fe80::204:61ff:fe9d:f156");
//			ipv6test(1,"::1");
//			ipv6test(1,"fe80::");
//			ipv6test(1,"fe80::1");
//			ipv6test(0,":");
//			ipv6test(1,"::ffff:c000:280");
//			
//			// Aeron supplied these test cases
//			
//			ipv6test(0,"1111:2222:3333:4444::5555:");
//			ipv6test(0,"1111:2222:3333::5555:");
//			ipv6test(0,"1111:2222::5555:");
//			ipv6test(0,"1111::5555:");
//			ipv6test(0,"::5555:");
//			
//			
//			ipv6test(0,":::");
//			ipv6test(0,"1111:");
//			ipv6test(0,":");
//			
//			
//			ipv6test(0,":1111:2222:3333:4444::5555");
//			ipv6test(0,":1111:2222:3333::5555");
//			ipv6test(0,":1111:2222::5555");
//			ipv6test(0,":1111::5555");
//			
//			
//			ipv6test(0,":::5555");
//			ipv6test(0,":::");
//			
//			
//			// Additional test cases
//			// from http://rt.cpan.org/Public/Bug/Display.html?id=50693
//			
//			ipv6test(1,"2001:0db8:85a3:0000:0000:8a2e:0370:7334");
//			ipv6test(1,"2001:db8:85a3:0:0:8a2e:370:7334");
//			ipv6test(1,"2001:db8:85a3::8a2e:370:7334");
//			ipv6test(1,"2001:0db8:0000:0000:0000:0000:1428:57ab");
//			ipv6test(1,"2001:0db8:0000:0000:0000::1428:57ab");
//			ipv6test(1,"2001:0db8:0:0:0:0:1428:57ab");
//			ipv6test(1,"2001:0db8:0:0::1428:57ab");
//			ipv6test(1,"2001:0db8::1428:57ab");
//			ipv6test(1,"2001:db8::1428:57ab");
//			ipv6test(1,"0000:0000:0000:0000:0000:0000:0000:0001");
//			ipv6test(1,"::1");
//			ipv6test(1,"::ffff:0c22:384e");
//			ipv6test(1,"2001:0db8:1234:0000:0000:0000:0000:0000");
//			ipv6test(1,"2001:0db8:1234:ffff:ffff:ffff:ffff:ffff");
//			ipv6test(1,"2001:db8:a::123");
//			ipv6test(1,"fe80::");
//			
//			ipv6test(0,"123");
//			ipv6test(0,"ldkfj");
//			ipv6test(0,"2001::FFD3::57ab");
//			ipv6test(0,"2001:db8:85a3::8a2e:37023:7334");
//			ipv6test(0,"2001:db8:85a3::8a2e:370k:7334");
//			ipv6test(0,"1:2:3:4:5:6:7:8:9");
//			ipv6test(0,"1::2::3");
//			ipv6test(0,"1:::3:4:5");
//			ipv6test(0,"1:2:3::4:5:6:7:8:9");
//			
//			// New from Aeron
//			ipv6test(1,"1111:2222:3333:4444:5555:6666:7777:8888");
//			ipv6test(1,"1111:2222:3333:4444:5555:6666:7777::");
//			ipv6test(1,"1111:2222:3333:4444:5555:6666::");
//			ipv6test(1,"1111:2222:3333:4444:5555::");
//			ipv6test(1,"1111:2222:3333:4444::");
//			ipv6test(1,"1111:2222:3333::");
//			ipv6test(1,"1111:2222::");
//			ipv6test(1,"1111::");
//			// ipv6test(1,"::");     //duplicate
//			ipv6test(1,"1111:2222:3333:4444:5555:6666::8888");
//			ipv6test(1,"1111:2222:3333:4444:5555::8888");
//			ipv6test(1,"1111:2222:3333:4444::8888");
//			ipv6test(1,"1111:2222:3333::8888");
//			ipv6test(1,"1111:2222::8888");
//			ipv6test(1,"1111::8888");
//			ipv6test(1,"::8888");
//			ipv6test(1,"1111:2222:3333:4444:5555::7777:8888");
//			ipv6test(1,"1111:2222:3333:4444::7777:8888");
//			ipv6test(1,"1111:2222:3333::7777:8888");
//			ipv6test(1,"1111:2222::7777:8888");
//			ipv6test(1,"1111::7777:8888");
//			ipv6test(1,"::7777:8888");
//			ipv6test(1,"1111:2222:3333:4444::6666:7777:8888");
//			ipv6test(1,"1111:2222:3333::6666:7777:8888");
//			ipv6test(1,"1111:2222::6666:7777:8888");
//			ipv6test(1,"1111::6666:7777:8888");
//			ipv6test(1,"::6666:7777:8888");
//			ipv6test(1,"1111:2222:3333::5555:6666:7777:8888");
//			ipv6test(1,"1111:2222::5555:6666:7777:8888");
//			ipv6test(1,"1111::5555:6666:7777:8888");
//			ipv6test(1,"::5555:6666:7777:8888");
//			ipv6test(1,"1111:2222::4444:5555:6666:7777:8888");
//			ipv6test(1,"1111::4444:5555:6666:7777:8888");
//			ipv6test(1,"::4444:5555:6666:7777:8888");
//			ipv6test(1,"1111::3333:4444:5555:6666:7777:8888");
//			ipv6test(1,"::3333:4444:5555:6666:7777:8888");
//			ipv6test(1,"::2222:3333:4444:5555:6666:7777:8888");
//			
//			
		ipv6test(1,"*:2222:3333:4444:5555:6666:123.123.123.123");
		ipv6test(1,"1111:*:3333:4444:5555::123.123.123.123");
		ipv6test(1,"1111:2222:*:4444::123.123.123.123");
		ipv6test(1,"1111:2222:3333::*.123.123.123");
		ipv6test(1,"1111:2222::123.123.*.123");
		ipv6test(1,"1111::123.*.123.123");
		ipv6test(1,"::123.123.123.*");
		ipv6test(1,"1111:2222:3333:4444::*:123.123.123.123");
		ipv6test(1,"1111:2222:*::6666:123.123.123.123");
		ipv6test(1,"*:2222::6666:123.123.123.123");
		ipv6test(1,"1111::6666:*.123.123.*");
		ipv6test(1,"::6666:123.123.*.123");
		ipv6test(1,"1111:*:3333::5555:6666:*.123.123.123");
		ipv6test(1,"1111:2222::*:6666:123.123.*.123");
		ipv6test(1,"1111::*:6666:*.123.123.123");
		ipv6test(1,"::5555:6666:123.123.123.123");
		ipv6test(1,"1111:2222::4444:5555:*:123.123.123.123");
		ipv6test(1,"1111::4444:5555:6666:123.*.123.123");
		ipv6test(1,"*::4444:5555:6666:123.123.123.123");
		ipv6test(1,"1111::*:4444:5555:6666:123.123.123.123");
		ipv6test(1,"::2222:*:4444:5555:6666:123.123.123.123");
		ipv6test(1,"::*:*:*:*:*:*.*.*.*");
		ipv6test(1,"*::*:*:*:*:*.*.*.*");
		ipv6test(0,"*:::*:*:*:*.*.*.*");
		ipv6test(0,"*:::*:*:*:*:*.*.*.*");
		ipv6test(1,"*::*:*:*:*:*.*.*.*");
		ipv6test(0,"*::*:*:*:*:*:*.*.*.*");
		ipv6test(0,"*:*:*:*:*:*:*:*:*.*.*.*");
		ipv6test(0,"*:*:*:*:*:*:*::*.*.*.*");
		ipv6test(0,"*:*:*:*:*:*::*:*.*.*.*");
		ipv6test(1,"*:*:*:*:*:*:*.*.*.*");
		ipv6test(1,"*:*:*:*:*::*.*.*.*");
		ipv6test(1,"*:*:*:*::*:*.*.*.*");
		
		ipv6test(1,"::*", false);
		ipv6test(1,"*:0:0:0:0:0:0:*", false);
//			
//			// Playing with combinations of "0" and "::"
//			// NB: these are all sytactically correct, but are bad form
//			//   because "0" adjacent to "::" should be combined into "::"
//			ipv6test(1,"::0:0:0:0:0:0:0", true);
//			ipv6test(1,"::0:0:0:0:0:0", true);
//			ipv6test(1,"::0:0:0:0:0", true);
//			ipv6test(1,"::0:0:0:0", true);
//			ipv6test(1,"::0:0:0", true);
//			ipv6test(1,"::0:0", true);
//			ipv6test(1,"::0", true);
//			ipv6test(1,"0:0:0:0:0:0:0::", true);
//			ipv6test(1,"0:0:0:0:0:0::", true);
//			ipv6test(1,"0:0:0:0:0::", true);
//			ipv6test(1,"0:0:0:0::", true);
//			ipv6test(1,"0:0:0::", true);
//			ipv6test(1,"0:0::", true);
//			ipv6test(1,"0::", true);
//			
//			// New invalid from Aeron
//			// Invalid data
//			ipv6test(0,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX");
//			
//			// Too many components
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:9999");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888::");
//			ipv6test(0,"::2222:3333:4444:5555:6666:7777:8888:9999");
//			
//			// Too few components
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666");
//			ipv6test(0,"1111:2222:3333:4444:5555");
//			ipv6test(0,"1111:2222:3333:4444");
//			ipv6test(0,"1111:2222:3333");
//			ipv6test(0,"1111:2222");
//			ipv6test(0,"1111");
//			
//			// Missing :
//			ipv6test(0,"11112222:3333:4444:5555:6666:7777:8888");
//			ipv6test(0,"1111:22223333:4444:5555:6666:7777:8888");
//			ipv6test(0,"1111:2222:33334444:5555:6666:7777:8888");
//			ipv6test(0,"1111:2222:3333:44445555:6666:7777:8888");
//			ipv6test(0,"1111:2222:3333:4444:55556666:7777:8888");
//			ipv6test(0,"1111:2222:3333:4444:5555:66667777:8888");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:77778888");
//			
//			// Missing : intended for ::
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:");
//			ipv6test(0,"1111:2222:3333:4444:5555:");
//			ipv6test(0,"1111:2222:3333:4444:");
//			ipv6test(0,"1111:2222:3333:");
//			ipv6test(0,"1111:2222:");
//			ipv6test(0,"1111:");
//			ipv6test(0,":");
//			ipv6test(0,":8888");
//			ipv6test(0,":7777:8888");
//			ipv6test(0,":6666:7777:8888");
//			ipv6test(0,":5555:6666:7777:8888");
//			ipv6test(0,":4444:5555:6666:7777:8888");
//			ipv6test(0,":3333:4444:5555:6666:7777:8888");
//			ipv6test(0,":2222:3333:4444:5555:6666:7777:8888");
//			ipv6test(0,":1111:2222:3333:4444:5555:6666:7777:8888");
//			
//			// :::
//			ipv6test(0,":::2222:3333:4444:5555:6666:7777:8888");
//			ipv6test(0,"1111:::3333:4444:5555:6666:7777:8888");
//			ipv6test(0,"1111:2222:::4444:5555:6666:7777:8888");
//			ipv6test(0,"1111:2222:3333:::5555:6666:7777:8888");
//			ipv6test(0,"1111:2222:3333:4444:::6666:7777:8888");
//			ipv6test(0,"1111:2222:3333:4444:5555:::7777:8888");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:::8888");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:::");
//			
//			// Double ::");
//			ipv6test(0,"::2222::4444:5555:6666:7777:8888");
//			ipv6test(0,"::2222:3333::5555:6666:7777:8888");
//			ipv6test(0,"::2222:3333:4444::6666:7777:8888");
//			ipv6test(0,"::2222:3333:4444:5555::7777:8888");
//			ipv6test(0,"::2222:3333:4444:5555:7777::8888");
//			ipv6test(0,"::2222:3333:4444:5555:7777:8888::");
//			
//			ipv6test(0,"1111::3333::5555:6666:7777:8888");
//			ipv6test(0,"1111::3333:4444::6666:7777:8888");
//			ipv6test(0,"1111::3333:4444:5555::7777:8888");
//			ipv6test(0,"1111::3333:4444:5555:6666::8888");
//			ipv6test(0,"1111::3333:4444:5555:6666:7777::");
//			
//			ipv6test(0,"1111:2222::4444::6666:7777:8888");
//			ipv6test(0,"1111:2222::4444:5555::7777:8888");
//			ipv6test(0,"1111:2222::4444:5555:6666::8888");
//			ipv6test(0,"1111:2222::4444:5555:6666:7777::");
//			
//			ipv6test(0,"1111:2222:3333::5555::7777:8888");
//			ipv6test(0,"1111:2222:3333::5555:6666::8888");
//			ipv6test(0,"1111:2222:3333::5555:6666:7777::");
//			
//			ipv6test(0,"1111:2222:3333:4444::6666::8888");
//			ipv6test(0,"1111:2222:3333:4444::6666:7777::");
//			
//			ipv6test(0,"1111:2222:3333:4444:5555::7777::");
//			
//			
//			
//			// Too many components"
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:8888:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666::1.2.3.4");
//			ipv6test(0,"::2222:3333:4444:5555:6666:7777:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:1.2.3.4.5");
//			
//			// Too few components
//			ipv6test(0,"1111:2222:3333:4444:5555:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:1.2.3.4");
//			ipv6test(0,"1111:2222:1.2.3.4");
//			ipv6test(0,"1111:1.2.3.4");
//			ipv6testOnly(0,"1.2.3.4"); //fixed
//			
//			// Missing :
//			ipv6test(0,"11112222:3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:22223333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:33334444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:44445555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:55556666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:5555:66661.2.3.4");
//			
//			// Missing .
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:255255.255.255");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:255.255255.255");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:255.255.255255");
//			
//			
//			// Missing : intended for ::
//			ipv6test(0,":1.2.3.4");
//			ipv6test(0,":6666:1.2.3.4");
//			ipv6test(0,":5555:6666:1.2.3.4");
//			ipv6test(0,":4444:5555:6666:1.2.3.4");
//			ipv6test(0,":3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,":2222:3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,":1111:2222:3333:4444:5555:6666:1.2.3.4");
//			
//			// :::
//			ipv6test(0,":::2222:3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:::3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:::4444:5555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:::5555:6666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:::6666:1.2.3.4");
//			ipv6test(0,"1111:2222:3333:4444:5555:::1.2.3.4");
//			
//			// Double ::
//			ipv6test(0,"::2222::4444:5555:6666:1.2.3.4");
//			ipv6test(0,"::2222:3333::5555:6666:1.2.3.4");
//			ipv6test(0,"::2222:3333:4444::6666:1.2.3.4");
//			ipv6test(0,"::2222:3333:4444:5555::1.2.3.4");
//			
//			ipv6test(0,"1111::3333::5555:6666:1.2.3.4");
//			ipv6test(0,"1111::3333:4444::6666:1.2.3.4");
//			ipv6test(0,"1111::3333:4444:5555::1.2.3.4");
//			
//			ipv6test(0,"1111:2222::4444::6666:1.2.3.4");
//			ipv6test(0,"1111:2222::4444:5555::1.2.3.4");
//			
//			ipv6test(0,"1111:2222:3333::5555::1.2.3.4");
//			
//			
//			
//			// Missing parts
//			ipv6test(0,"::.");
//			ipv6test(0,"::..");
//			ipv6test(0,"::...");
//			ipv6test(0,"::1...");
//			ipv6test(0,"::1.2..");
//			ipv6test(0,"::1.2.3.");
//			ipv6test(0,"::.2..");
//			ipv6test(0,"::.2.3.");
//			ipv6test(0,"::.2.3.4");
//			ipv6test(0,"::..3.");
//			ipv6test(0,"::..3.4");
//			ipv6test(0,"::...4");
//			
//			
//			// Extra : in front
//			ipv6test(0,":1111:2222:3333:4444:5555:6666:7777::");
//			ipv6test(0,":1111:2222:3333:4444:5555:6666::");
//			ipv6test(0,":1111:2222:3333:4444:5555::");
//			ipv6test(0,":1111:2222:3333:4444::");
//			ipv6test(0,":1111:2222:3333::");
//			ipv6test(0,":1111:2222::");
//			ipv6test(0,":1111::");
//			ipv6test(0,":::");
//			ipv6test(0,":1111:2222:3333:4444:5555:6666::8888");
//			ipv6test(0,":1111:2222:3333:4444:5555::8888");
//			ipv6test(0,":1111:2222:3333:4444::8888");
//			ipv6test(0,":1111:2222:3333::8888");
//			ipv6test(0,":1111:2222::8888");
//			ipv6test(0,":1111::8888");
//			ipv6test(0,":::8888");
//			ipv6test(0,":1111:2222:3333:4444:5555::7777:8888");
//			ipv6test(0,":1111:2222:3333:4444::7777:8888");
//			ipv6test(0,":1111:2222:3333::7777:8888");
//			ipv6test(0,":1111:2222::7777:8888");
//			ipv6test(0,":1111::7777:8888");
//			ipv6test(0,":::7777:8888");
//			ipv6test(0,":1111:2222:3333:4444::6666:7777:8888");
//			ipv6test(0,":1111:2222:3333::6666:7777:8888");
//			ipv6test(0,":1111:2222::6666:7777:8888");
//			ipv6test(0,":1111::6666:7777:8888");
//			ipv6test(0,":::6666:7777:8888");
//			ipv6test(0,":1111:2222:3333::5555:6666:7777:8888");
//			ipv6test(0,":1111:2222::5555:6666:7777:8888");
//			ipv6test(0,":1111::5555:6666:7777:8888");
//			ipv6test(0,":::5555:6666:7777:8888");
//			ipv6test(0,":1111:2222::4444:5555:6666:7777:8888");
//			ipv6test(0,":1111::4444:5555:6666:7777:8888");
//			ipv6test(0,":::4444:5555:6666:7777:8888");
//			ipv6test(0,":1111::3333:4444:5555:6666:7777:8888");
//			ipv6test(0,":::3333:4444:5555:6666:7777:8888");
//			ipv6test(0,":::2222:3333:4444:5555:6666:7777:8888");
//			
//			
//			ipv6test(0,":1111:2222:3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,":1111:2222:3333:4444:5555::1.2.3.4");
//			ipv6test(0,":1111:2222:3333:4444::1.2.3.4");
//			ipv6test(0,":1111:2222:3333::1.2.3.4");
//			ipv6test(0,":1111:2222::1.2.3.4");
//			ipv6test(0,":1111::1.2.3.4");
//			ipv6test(0,":::1.2.3.4");
//			ipv6test(0,":1111:2222:3333:4444::6666:1.2.3.4");
//			ipv6test(0,":1111:2222:3333::6666:1.2.3.4");
//			ipv6test(0,":1111:2222::6666:1.2.3.4");
//			ipv6test(0,":1111::6666:1.2.3.4");
//			ipv6test(0,":::6666:1.2.3.4");
//			ipv6test(0,":1111:2222:3333::5555:6666:1.2.3.4");
//			ipv6test(0,":1111:2222::5555:6666:1.2.3.4");
//			ipv6test(0,":1111::5555:6666:1.2.3.4");
//			ipv6test(0,":::5555:6666:1.2.3.4");
//			ipv6test(0,":1111:2222::4444:5555:6666:1.2.3.4");
//			ipv6test(0,":1111::4444:5555:6666:1.2.3.4");
//			ipv6test(0,":::4444:5555:6666:1.2.3.4");
//			ipv6test(0,":1111::3333:4444:5555:6666:1.2.3.4");
//			ipv6test(0,":::2222:3333:4444:5555:6666:1.2.3.4");
//			
//			
//			// Extra : at end
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:7777:::");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666:::");
//			ipv6test(0,"1111:2222:3333:4444:5555:::");
//			ipv6test(0,"1111:2222:3333:4444:::");
//			ipv6test(0,"1111:2222:3333:::");
//			ipv6test(0,"1111:2222:::");
//			ipv6test(0,"1111:::");
//			ipv6test(0,":::");
//			ipv6test(0,"1111:2222:3333:4444:5555:6666::8888:");
//			ipv6test(0,"1111:2222:3333:4444:5555::8888:");
//			ipv6test(0,"1111:2222:3333:4444::8888:");
//			ipv6test(0,"1111:2222:3333::8888:");
//			ipv6test(0,"1111:2222::8888:");
//			ipv6test(0,"1111::8888:");
//			ipv6test(0,"::8888:");
//			ipv6test(0,"1111:2222:3333:4444:5555::7777:8888:");
//			ipv6test(0,"1111:2222:3333:4444::7777:8888:");
//			ipv6test(0,"1111:2222:3333::7777:8888:");
//			ipv6test(0,"1111:2222::7777:8888:");
//			ipv6test(0,"1111::7777:8888:");
//			ipv6test(0,"::7777:8888:");
//			ipv6test(0,"1111:2222:3333:4444::6666:7777:8888:");
//			ipv6test(0,"1111:2222:3333::6666:7777:8888:");
//			ipv6test(0,"1111:2222::6666:7777:8888:");
//			ipv6test(0,"1111::6666:7777:8888:");
//			ipv6test(0,"::6666:7777:8888:");
//			ipv6test(0,"1111:2222:3333::5555:6666:7777:8888:");
//			ipv6test(0,"1111:2222::5555:6666:7777:8888:");
//			ipv6test(0,"1111::5555:6666:7777:8888:");
//			ipv6test(0,"::5555:6666:7777:8888:");
//			ipv6test(0,"1111:2222::4444:5555:6666:7777:8888:");
//			ipv6test(0,"1111::4444:5555:6666:7777:8888:");
//			ipv6test(0,"::4444:5555:6666:7777:8888:");
//			ipv6test(0,"1111::3333:4444:5555:6666:7777:8888:");
//			ipv6test(0,"::3333:4444:5555:6666:7777:8888:");
//			ipv6test(0,"::2222:3333:4444:5555:6666:7777:8888:");
//		 */
		// Additional cases: http://crisp.tweakblogs.net/blog/2031/ipv6-validation-%28and-caveats%29.html
		ipv6test(1,"0:a:b:*:d:e:f::");
		ipv6test(1,"::0:a:*:*:d:e:f"); // syntactically correct, but bad form (::0:... could be combined)
		ipv6test(1,"a:b:c:*:*:f:0::");
		ipv6test(0,"':10.*.0.1");
		
		
		ipv4test(true, "1.*.4");
		ipv4test(true, "1.2.*");
		ipv4test(true, "*");
		ipv4test(true, "*.1");
		ipv4test(true, "1.*");
		ipv4test(true, "1.*.1");
		ipv4test(true, "1.*.*");
		ipv4test(true, "*.*.1");
		ipv4test(true, "*.1.*");
		ipv4test(false, "1");
		ipv4test(false, "1.1");
		ipv4test(false, "1.1.1");
		
		ipv4test(true, "*.1.2.*");
		ipv4test(true, "*.1.*.2");
		ipv4test(true, "*.*.*.2");
		ipv4test(true, "*.*.*.*");
		ipv4test(true, "1.*.2.*");
		ipv4test(true, "1.2.*.*");
		
		ipv4test(true, "*");
		ipv4test(true, "%.%"); 
		ipv6test(true, "1::1.2.%");
		ipv6test(true, "1::1.2.3.4");
		ipv6test(true, "1:%:1");
		ipv4test(true, "1.2.%");
		
		ipv6test(1, "*");
		ipv6test(1, "1:*");
		ipv6test(1, "*:1:*");
		ipv6test(1, "*:1");
		
		ipv6test(1, "*:1:1.*.1");
		ipv6test(1, "*:1:*.1");
		ipv6test(1, "*:1:1.*");
		
		ipv6test(0, "1:1:1.*.1");
		ipv6test(1, "1:1:*.1");
		ipv6test(0, "1:1:1.*");
		
		
		ipv6test(1, "1::1:1.*.1");
		ipv6test(1, "1::1:*.1");
		ipv6test(1, "1::1:1.*");
		
		ipv6test(1, "1:*.2");//in this one, the wildcard covers both ipv6 and ipv4 parts
		ipv6test(1, "1::*.2");//compression takes precedence so the wildcard does not cover both ipv6 and ipv4 parts
		ipv6test(1, "1::2:*.2");//compression takes precedence so the wildcard does not cover both ipv6 and ipv4 parts
		ipv6test(1, "::2:*.2");//compression takes precedence so the wildcard does not cover both ipv6 and ipv4 parts
		ipv6test(0, "1:1.*.2");
		ipv6test(0, "1:*:1.2");
		
		
		ipv6test(1, "*:1:1.*.3");
		ipv6test(0, "*:1:1.2.3");
		ipv6test(1, "::1:1.*.3");
		ipv6test(0, "::1:1.2.3");
		
		ipv6test(1, "1:*:1");
		ipv6test(1, "1:*:1:1.*.1");
		ipv6test(1, "1:*:1:*.1");
		ipv6test(1, "1:*:1:1.*");
		ipv6test(0, "1:*:1:1.2.3");
		
		ipv6test(0, "1:*:1:2:3:4:5:6:7");
		ipv6test(0, "1:*:1:2:3:4:5:1.2.3.4");
		ipv6test(1, "1:*:2:3:4:5:1.2.3.4");
		ipv6test(0, "1:*:2:3:4:5:1.2.3.4.5");
		ipv6test(0, "1:1:2:3:4:5:1.2.3.4.5");
		ipv6test(0, "1:1:2:3:4:5:6:1.2.3.4");
		ipv6test(0, "1:1:2:3:4:5:6:1.*.3.4");
		ipv6test(1, "1:2:3:4:5:6:1.2.3.4");
		ipv6test(1, "1:2:3:4:5:6:1.*.3.4");
		
			
		super.test();
	}
	
}