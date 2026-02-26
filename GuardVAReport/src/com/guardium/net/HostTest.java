/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.util.ArrayList;

import com.guardium.net.Host.HostValidationOptions;


public class HostTest {
	
	public static void main(String args[]) {
		new HostTest().test();
	}
	
	static class Failure {
		boolean pass;
		Host addr;
		String str;
		
		Failure(boolean pass, Host addr) {
			this.pass = pass;
			this.addr = addr;
		}
		
		Failure(String str, Host addr) {
			this.str = str;
			this.addr = addr;
		}
	}
	
	ArrayList<HostTest.Failure> failures = new ArrayList<HostTest.Failure>();
	int numFailed;
	int numTested;
	
	void testResolved(String original, String expectedResolved) {
		Host origAddress = new Host(original);
		IPAddress resolvedAddress = origAddress.resolve();
		IPAddressString expectedAddress = new IPAddressString(expectedResolved);
		boolean result = (resolvedAddress == null) ? (expectedResolved == null) : resolvedAddress.equals(expectedAddress);
		if(!result) {
			numFailed++;
			failures.add(new Failure("resolved was " + resolvedAddress + " original was " + original, origAddress));
		}
		numTested++;
	}
	
	void testNormalized(boolean expectMatch, String original, String expected) {
		Host w = new Host(original);
		String normalized = w.toNormalizedString();
		if(!(normalized.equals(expected) == expectMatch)) {
			numFailed++;
			failures.add(new Failure("normalization was " + normalized, w));
		}
		numTested++;
	}
	
	void testCanonical(String original, String expected) {
		Host w = new Host(original);
		String canonical = w.toCanonicalString();
		if(!canonical.equals(expected)) {
			numFailed++;
			failures.add(new Failure("normalization was " + canonical, w));
		}
		numTested++;
	}
	
	void hostTest(boolean pass, String x) {
		Host addr = new Host(x);
		hostTest(pass, addr);
		//do it a second time to test the caching
		hostTest(pass, addr);
	}
	
	void hostTest(boolean pass, Host addr) {
		if(isNotExpected(pass, addr)) {
			numFailed++;
			failures.add(new Failure(pass, addr));
			
			//this part just for debugging
			isNotExpected(pass, addr);
		}
		numTested++;
	}
	
	boolean isNotExpected(boolean expectedPass, Host addr) {
		try {
			addr.validate();
			return !expectedPass;
		} catch(HostException e) {
			return expectedPass;
		}
	}
	
	void testURL(String url) {
		Host w = new Host(url);
		try {
			w.validate();
			numFailed++;
			failures.add(new Failure("failed: " + "URL " + url, w));
		} catch(HostException e) {
			//pass
			e.getMessage();
		}
	}

	void test()
	{
		testResolved("a::b:c:d:1.2.3.4%x", null);//ipv6 should be enclosed in [],the zone x does not exist
		testResolved("[a::b:c:d:1.2.3.4%x]", null);//the zone x does not exist
		testResolved("[a::b:c:d:1.2.3.4]", "a::b:c:d:1.2.3.4");//square brackets can enclose ipv6 in host names but not addresses 
		testResolved("2001:0000:1234:0000:0000:C1C0:ABCD:0876%x", null);//ipv6 must be enclosed in []
		testResolved("[2001:0000:1234:0000:0000:C1C0:ABCD:0876%x]", null);//zones not allowed when using []
		testResolved("[2001:0000:1234:0000:0000:C1C0:ABCD:0876]", "2001:0:1234::C1C0:abcd:876");//square brackets can enclose ipv6 in host names but not addresses
		testResolved("1.2.3.04", "1.2.3.4");
		testResolved("1.2.3", null);
		testResolved("[1.2.3.4]", null);//square brackets are for ipv6, not ipv4
		
		testNormalized(true, "[A::b:c:d:1.2.03.4]", "[a:0:0:b:c:d:102:304]");//square brackets can enclose ipv6 in host names but not addresses
		testNormalized(true, "[2001:0000:1234:0000:0000:C1C0:ABCD:0876]", "[2001:0:1234:0:0:c1c0:abcd:876]");//square brackets can enclose ipv6 in host names but not addresses
		testNormalized(true, "1.2.3.04", "1.2.3.4");
		
		testCanonical("[A:0::c:d:1.2.03.4]", "[a::c:d:102:304]");//square brackets can enclose ipv6 in host names but not addresses
		testCanonical("[2001:0000:1234:0000:0000:C1C0:ABCD:0876]", "[2001:0:1234::c1c0:abcd:876]");//square brackets can enclose ipv6 in host names but not addresses
		testCanonical("1.2.3.04", "1.2.3.4");
		
		testResolved("sfoley1.guard.swg.usma.ibm.com", "9.32.237.26");
		testResolved("vx38.guard.swg.usma.ibm.com", "9.70.146.84");
		testResolved("9.32.237.26", "9.32.237.26");
		testResolved("9.70.146.84", "9.70.146.84");
		
		testNormalized(HostValidationOptions.NORMALIZE_TO_LOWER_CASE_DEFAULT, "WWW.ABC.COM", "www.abc.com");
		testNormalized(HostValidationOptions.NORMALIZE_TO_LOWER_CASE_DEFAULT, "WWW.AB-C.COM", "www.ab-c.com");

		testURL("http://1.2.3.4");
		testURL("http://[a:a:a:a:b:b:b:b]");
		testURL("http://a:a:a:a:b:b:b:b");
		
		hostTest(true,"[a::b:c:d:1.2.3.4]");//square brackets can enclose ipv6 in host names but not addresses
		hostTest(HostValidationOptions.ALLOW_ZONE_DEFAULT,"[a::b:c:d:1.2.3.4%x]");//zones not allowed when using []
		hostTest(HostValidationOptions.ALLOW_ZONE_DEFAULT,"a::b:c:d:1.2.3.4%x");//no zones in hosts at all
		hostTest(true,"[2001:0000:1234:0000:0000:C1C0:ABCD:0876]");//square brackets can enclose ipv6 in host names but not addresses
		hostTest(HostValidationOptions.ALLOW_ZONE_DEFAULT,"2001:0000:1234:0000:0000:C1C0:ABCD:0876%x");//ipv6 must be enclosed in []
		hostTest(HostValidationOptions.ALLOW_ZONE_DEFAULT,"[2001:0000:1234:0000:0000:C1C0:ABCD:0876%x]");//zones not allowed when using []
		
		hostTest(true,"1.2.3.4");
		hostTest(false,"1.2.3");
		hostTest(false,"[1.2.3.4]");//square brackets are for ipv6, not ipv4
		
		hostTest(true, "a_b.com");
		hostTest(true, "_ab.com");
		hostTest(true, "_ab_.com");
		hostTest(false, "-ab-.com");
		hostTest(false, "-ab-.com");
		hostTest(false, "ab-.com");
		hostTest(false, "-ab.com");
		hostTest(false, "ab.-com");
		hostTest(false, "ab.com-");
		
		hostTest(true, "a9b.com");
		hostTest(true, "9ab.com");
		hostTest(true, "999.com");
		hostTest(true, "ab9.com");
		hostTest(true, "ab9.com9");
		hostTest(false, "999");
		hostTest(false, "999.111");
		
		hostTest(false, "a*b.com");
		hostTest(false, "ab.co&m");
		hostTest(false, "#.ab.com");
		hostTest(false, "cd.ab.com.~");
		hostTest(false, "#x.ab.com");
		hostTest(false, "cd.ab.com.x~");
		hostTest(false, "x#.ab.com");
		hostTest(false, "cd.ab.com.~x");
		hostTest(true, "xx.ab.com.xx");
		
		hostTest(true, "ab.cde.fgh.com");
		hostTest(true, "aB.cDE.fgh.COm");
		
		hostTest(true, "123-123456789-123456789-123456789-123456789-123456789-123456789.com"); //label 63 chars
		hostTest(false, "1234-123456789-123456789-123456789-123456789-123456789-123456789.com"); //label 64 chars
		hostTest(false, "123.123456789.123456789.123456789.123456789.123456789.123456789.123");//all numbers
		hostTest(true, "aaa.123456789.123456789.123456789.123456789.123456789.123456789.123");//numbers everywhere but first label
		
		hostTest(false, "a11" +
			"-123456789-123456789-123456789-123456789-12345678." +
			"-123456789-123456789-123456789-123456789-12345678." +
			"-123456789-123456789-123456789-123456789-12345678." +
			"-123456789-123456789-123456789-123456789-12345678." +
			"-123456789-123456789-123456789-123456789-123456789"); //253 chars, but segments start with -
		
		hostTest(true, "a11" +
				"-123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-123456789"); //253 chars
			
		hostTest(false, "111" +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"01234567890123456789012345678901234567890123456789"); //all number
		
		hostTest(true, "222" +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678f"); //not all number, 253 chars
		
		hostTest(false, "a222" +
				"-123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-12345678." +
				"0123456789-123456789-123456789-123456789-123456789"); //254 chars
		
		hostTest(true, "a33" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789"); //253 chars
			
		hostTest(false, "444" +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"01234567890123456789012345678901234567890123456789"); //all number
		
		hostTest(true, "555" +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678." +
				"0123456789012345678901234567890123456789012345678f"); //not all number
		
		hostTest(true, "777" +
				"01234567890123456789012345678901234567890123456789" +
				"0123456789.123456789012345678901234567890123456789" +
				"012345678901234567890123.5678901234567890123456789" +
				"01234567890123456789012345678901234567.90123456789" +
				"0123456789012345678901234567890123456789012345678f"); //first 3 segments are 63 chars
		
		hostTest(false, "777" +
				"01234567890123456789012345678901234567890123456789" +
				"01234567890.23456789012345678901234567890123456789" +
				"012345678901234567890123.5678901234567890123456789" +
				"01234567890123456789012345678901234567.90123456789" +
				"0123456789012345678901234567890123456789012345678f"); //first segment 64 chars
		
		hostTest(false, "a666" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789" +
				".123456789.123456789.123456789.123456789.123456789"); //254 chars
		
		hostTest(true, "a.9." +	
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5"); //252 chars, 127 segments
		
		hostTest(false, "a.8." +	
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5."); //252 chars, 127 segments, extra dot at end
		
		hostTest(false, ".a.7." +	
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5"); //252 chars, 127 segments, extra dot at front
		
		hostTest(false, "a.6." +	
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5." +
				"1.1.1.1.1.2.2.2.2.2.3.3.3.3.3.4.4.4.4.4.5.5.5.5.5.8"); //254 chars, 128 segments
		
		hostTest(false, "a:b:com");
		hostTest(Host.HostValidationOptions.ACCEPT_UNBRACKETED_IPV6_DEFAULT, "a:b::ccc");
		hostTest(Host.HostValidationOptions.ACCEPT_UNBRACKETED_IPV6_DEFAULT, "a:b:c:d:e:f:a:b");
		
		hostTest(false, ".as.b.com");//starts with dot
		hostTest(false, "as.b.com.");//ends with dot
		hostTest(false, "aas.b.com.");//starts and ends with dot
		hostTest(false, "as..b.com");//double dot
		hostTest(false, "as.b..com");//double dot
		hostTest(false, "..as.b.com");//starts with dots
		hostTest(false, "as.b.com..");//ends with dots	

		showMessage("pass count: " + (numTested - numFailed));
		showMessage("fail count: " + numFailed);
		String falseRejects = "";
		String failurestr = "";
		int falseRejectCount = 0;
		int failurestrCount = 0;
		
		for(HostTest.Failure f : failures) {
			if(f.pass) {
				if(f.str != null && f.str.length() > 0) {
					falseRejects += " " + f.str +  ": " + f.addr.toString();
					falseRejectCount++;
				} else {
					falseRejects += ' ' + f.addr.toString();
					falseRejectCount++;
				}
			} else {
				failurestr += ' ' + f.addr.toString();
				failurestrCount++;
			}
		}
		
		if(falseRejectCount > 0) {
			showMessage("False Rejects:\n" + falseRejects);
		}
		if(failurestrCount > 0) {
			showMessage("Failed to Reject:\n" + failurestr);
		}
	}
	
	void showMessage(String s) {
		System.out.println(s);
	}
}