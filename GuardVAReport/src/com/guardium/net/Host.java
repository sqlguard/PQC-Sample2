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
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guardium.utils.AdHocLogger;



/**
 * Represents an internet host name to identify a device.  Can be a fully qualified domain name.
 * 
 * See rfc 3513, 2181, 1035, 1034, 1123 or the list of rfcs for IPAddress
 * 
 * @author unknown
 */
public class Host implements Comparable<Host> {

	private static final int MAX_LENGTH = 253;
	private static final int MAX_SEGMENTS = 127;
	private static Pattern hostPattern[] = new Pattern[MAX_SEGMENTS];
	private static Pattern numericDotPattern = Pattern.compile("^[0-9\\.]+$");
	
	public static HostValidationOptions DEFAULT_VALIDATION_OPTIONS = new HostValidationOptions();
	
	/* the original host in string format */
	private final String host;
	
	/* the original host converted to normalized format */
	private String normalizedHost;
	
	/* the original host converted to canonical format */
	private String canonicalHost;
	
	/* the individual labels */
	private String labels[];
	
	/* the individual labels normalized */
	private String normalizedLabels[];
	
	/* the same host but with labels in reverse order (matching the way they are mapped to IP segments */
	private String reversedDomainStr;
	
	private HostException exception;
	private boolean validated;
	
	/* the ip address represented by this host */
	private IPAddressString ipAddressString;
	
	/* The address if this host represents an ip address.  Otherwise the address obtained when this host is resolved. */
	private IPAddress resolvedAddress;

	private boolean isResolved;
	
	/* validation options */
	HostValidationOptions validationOptions;
	
	public Host(IPAddressString addr) {
		this(addr, DEFAULT_VALIDATION_OPTIONS);
	}
	
	public Host(InetAddress addr) {
		this(addr.getHostName(), DEFAULT_VALIDATION_OPTIONS);
	}
	
	public Host(IPAddressString addr, HostValidationOptions options) {
		this(getHostString(addr), options);
		if(addr.isValid()) {
			ipAddressString = addr;
			resolvedAddress = addr.getValue();
			validated = isResolved = true;
		}
	}
	
	public Host(String host) {
		this(host, DEFAULT_VALIDATION_OPTIONS);
	}
	
	public Host(String host, HostValidationOptions options) {
		if(host == null) {
			host = "";
		} else {
			host = host.trim();
		}
		this.validationOptions = options;
		this.host = host;
	}
	
	private static String getHostString(IPAddressString addr) {
		if(addr.isIpv6()) {
			return '[' + addr.getValue().toNormalizedString() + ']';
		}
		if(addr.isValid()) {
			return addr.getValue().toNormalizedString();
		}
		return addr.toString();
	}
	
	public static class HostValidationOptions implements Cloneable {
		public static final boolean ALLOW_EMPTY_DEFAULT = false;
		public static final boolean ACCEPT_UNBRACKETED_IPV6_DEFAULT = true;
		public static final boolean NORMALIZE_TO_LOWER_CASE_DEFAULT = false;
		public static final boolean ALLOW_ZONE_DEFAULT = true;
		
		public final boolean allowEmpty;
		public final boolean allowUnbracketedIPV6;
		public final boolean normalizeToLowercase;
		public final boolean allowZone;
		
		HostValidationOptions() {
			this(ALLOW_EMPTY_DEFAULT);
		}
		
		HostValidationOptions(boolean allowEmpty) {
			this(allowEmpty, ACCEPT_UNBRACKETED_IPV6_DEFAULT);
		}
		
		HostValidationOptions(boolean allowEmpty, boolean allowUnbracketedIPV6) {
			this(allowEmpty, allowUnbracketedIPV6, NORMALIZE_TO_LOWER_CASE_DEFAULT);
		}
		
		HostValidationOptions(boolean allowEmpty, boolean allowUnbracketedIPV6, boolean normalizeToLowercase) {
			this(allowEmpty, allowUnbracketedIPV6, normalizeToLowercase, ALLOW_ZONE_DEFAULT);
		}
		
		HostValidationOptions(boolean allowEmpty, boolean allowUnbracketedIPV6, boolean normalizeToLowercase, boolean allowZone) {
			this.allowEmpty = allowEmpty;
			this.allowUnbracketedIPV6 = allowUnbracketedIPV6;
			this.normalizeToLowercase = normalizeToLowercase;
			this.allowZone = allowZone;
		}
		
		@Override
		public HostValidationOptions clone() {
			try {
				return (HostValidationOptions) super.clone();
			} catch (CloneNotSupportedException e) {}
			return null;
		}
	}
	
	private static Pattern makeHostPattern(int segmentCount) {
		//en.wikipedia.org/wiki/Domain_Name_System#Domain_name_syntax
		//en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names
		
		//max length is 63, cannot start or end with hyphen
		//strictly speaking, the underscore is not allowed anywhere, but it seems that rule is sometimes broken
		//also, underscores seem to be a part of dns names that are not part of host names, so we allow it here to be safe
		
		//networkadminkb.com/KB/a156/windows-2003-dns-and-the-underscore.aspx
		
		//It's a little confusing.  rfs 2181 https://www.ietf.org/rfc/rfc2181.txt in section 11 on name syntax says that any chars are allowed in dns.
		//However, it also says internet host names might have restrictions of their own, and this was defined in rfc 1035.  
		//rfc 1035 defines the restrictions on internet host names, in section 2.3.1 http://www.ietf.org/rfc/rfc1035.txt
		
		//So we will follow rfc 1035 and in addition allow the underscore, which is consistent with what Guardium has done in the past.
		
		//String label = "([a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]|[a-zA-Z0-9])";
		String label = "([a-zA-Z0-9_][a-zA-Z0-9_\\-]{0,61}[a-zA-Z0-9_]|[a-zA-Z0-9_])";
		String dot = "\\.";
		
		StringBuilder full = new StringBuilder();
		full.append('^').append(label);
		for(int i = 1; i < segmentCount; i++) {
			full.append(dot).append(label);
		}
		full.append('$');
		String s = full.toString();
		return Pattern.compile(s);
	}
	
	public synchronized void validate() throws HostException {
		if(validated) {
			if(exception != null) {
				throw exception;
			}
			return;
		}
		try {
			validated = true;
			if(host.length() > MAX_LENGTH) {
				throw new HostException(this, "ipaddress.host.error.invalid.length");
			}
			int count = IPAddressString.countSubstrMatches(".", host);
			if(count >= MAX_SEGMENTS) {
				//this would normally be covered by the length check, unless they had consecutive separators,
				//in which case the array access below to hostPattern would fail. So we need this second check.
				throw new HostException(this, "ipaddress.host.error.too.many.segments");
			}
			Pattern pattern = hostPattern[count];
			if(pattern == null) {
				pattern = hostPattern[count] = makeHostPattern(count + 1);
			}
			labels = IPAddressPatterns.matchPattern(pattern, host);
			if(labels == null) {
				if(host.length() == 0) {
					if(!validationOptions.allowEmpty) {
						throw new HostException(this, "ipaddress.host.error.invalid");
					} 
				} else {
					boolean mightContainIpv6 = IPAddressString.countSubstrMatches(":", host) >= 2;
					if(mightContainIpv6) {
						//check if it is an ipv6 address (eg [1::2]) or an unbracketed IPV6 address (eg 1::2) or a URL with an IPV6 address inside (eg http://[::])
						int hostEnd = host.length() - 1;
						try {
							if (hostEnd > 1 && host.charAt(0) == '[' && host.charAt(hostEnd) == ']') {
								String addr = host.substring(1, hostEnd);
								validateIPV6Address(addr);
							} else if(validationOptions.allowUnbracketedIPV6) {
								validateIPV6Address(host);
							} else {
								throw new HostException(this, "ipaddress.host.error.invalid");
							}
						} catch(IPAddressException e) {
							try { //before assuming we have an invalid unbracketed IPV6 address, check if we have a URL
								new URL(host);
								throw new HostException(this, "ipaddress.host.error.url");//it's a url
							} catch(MalformedURLException e2) {
								throw new HostException(this, e, "ipaddress.host.error.invalid");
							}
						}
					} else {
						try { //check if we have a URL for a more appropriate error message
							new URL(host);
							throw new HostException(this, "ipaddress.host.error.url");
						} catch(MalformedURLException e2) {
							throw new HostException(this,"ipaddress.host.error.invalid");
						}
					}
				}
			} else {
				//check if it is an IPV4 address
				pattern = numericDotPattern;
				Matcher matcher = pattern.matcher(host);
				if(matcher.matches()) {
					IPAddressString ipAddress = new IPAddressString(host);
					try {
						ipAddress.validateIpv4();
						this.labels = null; //when it comes to string representations, use the ip address
						this.ipAddressString = ipAddress;
						resolvedAddress = ipAddress.getValue();
						isResolved = true;
					} catch(IPAddressException e) {
						//this might happen with addresses like 1.2.3.4.5 or 1234.2.3.4 which are not valid hosts since they are all numeric,
						//but they are not valid ip addresses either
						throw new HostException(this, e, "ipaddress.host.error.invalid");
					}
				} //else it's a valid non-ip host name
			}
		} catch(HostException e) {
			exception = e;
			throw e;
		}
	}
	
	/**
	 * Returns the host without the domain name.
	 * 
	 * If this host represents an IP address, then returns this host.
	 * 
	 * If this is not a valid host, returns null.
	 * 
	 * @return
	 */
	public Host removeDomain() {
		if(isValid()) {
			if(isIpAddress()) {
				return this;
			}
			if(labels.length == 1) {
				return this;
			}
			String host = labels[0];
			Host result = new Host(host);
			result.labels = new String[] {host};
			result.reversedDomainStr = host;
			result.validated = true;
			return result;
		}
		return null;
	}

	private void validateIPV6Address(String addr) throws HostException, IPAddressException {
		IPAddressString ipAddress = new IPAddressString(addr);
		ipAddress.validateIpv6();
		if(!validationOptions.allowZone && ipAddress.hasZone()) {//zones are not allowed in host names
			throw new HostException(this, "ipaddress.error.ipv6.has.zone");
		}
		this.ipAddressString = ipAddress;
		resolvedAddress = ipAddress.getValue();
		isResolved = true;
	}
	
	public boolean isInvalid() {
		if(validated) {
			return exception != null;
		}
		try {
			validate();
		} catch(HostException e) {
			return true;
		}
		return false;
	}

	public boolean isValid() {
		return !isInvalid();
	}
	
	public boolean resolvesToSelf() {
		return isSelf() || (resolve() != null && resolvedAddress.isLoopback());
	}
	
	public boolean isSelf() {
		return isLocalHost() || isLoopback();
	}
	
	/*
	 * localhost
	 */
	public boolean isLocalHost() {
		return isValid() && host.equalsIgnoreCase("localhost");
	}
	
	/*
	 * [::1] (aka [0:0:0:0:0:0:0:1]) or 127.0.0.1
	 */
	public boolean isLoopback() {
		return isIpAddress() && ipAddressString.isLoopback();
	}
	
	public InetAddress toInetAddress() throws HostException, UnknownHostException {
		validate();
		return InetAddress.getByName(host);
	}
	
	public String toReversedDomainString() throws HostException {
		if(reversedDomainStr != null) {
			return reversedDomainStr;
		}
		validate();
		if(labels != null && labels.length > 0) {
			StringBuilder s = new StringBuilder();
			for(int i=labels.length - 1; i>=0; i--) {
				s.append(labels[i]).append('.');
			}
			s.deleteCharAt(s.length() - 1);
			return reversedDomainStr = s.toString();
		}
		return reversedDomainStr = host;
	}
	
	public String[] toNormalizedLabels() throws HostException {
		if(normalizedLabels != null) {
			return normalizedLabels;
		}
		validate();
		if(labels != null && labels.length > 0) {
			normalizedLabels = new String[labels.length];
			for(int i = 0; i<labels.length; i++) {
				normalizedLabels[i] = toNormalized(labels[i]);
			}
		} else {
			normalizedLabels = new String[] {host};
		}
		return normalizedLabels;
	}
	
	private String toNormalized(String s) {
		if(validationOptions.normalizeToLowercase) {
			StringBuilder builder = null;
			for(int i = 0; i < s.length(); i++) {
				int c = s.charAt(i);
				int c2 = Character.toLowerCase(c);
				if(c2 != c && builder == null) {
					builder = new StringBuilder(s.substring(0, i));
				}
				if(builder != null) {
					builder.append((char) c2);
				}
			}
			return (builder == null) ? s : builder.toString();
		}
		return s;
	}
	
	public String toCanonicalString() {
		if(canonicalHost != null) {
			return canonicalHost;
		}
		try {
			validate();
			if(isIpAddress()) {
				//ipAddressString was validated so we know calling getValue() returns non-null
				if(ipAddressString.isIpv6()) {
					return canonicalHost = '[' + ipAddressString.getValue().toCanonicalString() + ']';
				}
				return canonicalHost = ipAddressString.getValue().toCanonicalString();
			}
		} catch(HostException e) {
			//for invalid hosts, don't normalize, just return it as is
			return canonicalHost = host;
		}
		return canonicalHost = toNormalized(host);
	}
	
	public String toNormalizedString() {
		if(normalizedHost != null) {
			return normalizedHost;
		}
		try {
			validate();
			if(isIpAddress()) {
				//ipAddressString was validated so we know calling getValue() returns non-null
				if(ipAddressString.isIpv6()) {
					return normalizedHost = '[' + ipAddressString.getValue().toNormalizedString() + ']';
				}
				return normalizedHost = ipAddressString.getValue().toNormalizedString();
			}
		} catch(HostException e) {
			//for invalid hosts, don't normalize, just return it as is
			return normalizedHost = host;
		}
		return normalizedHost = toNormalized(host);
	}
	
	@Override
	public String toString() {
		return host;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Host) {
			Host other = (Host) o;
			return toNormalizedString().equals(other.toNormalizedString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toNormalizedString().hashCode();
	}
	
	@Override
	public int compareTo(Host other) {
		if(isIpAddress() && other.isIpAddress()) {
			return ipAddressString.compareTo(other.ipAddressString);
		}
		try {
			String normalizedLabels[] = toNormalizedLabels();
			String otherNormalizedLabels[] = other.toNormalizedLabels();
			
			for(int i=1, max = Math.min(normalizedLabels.length, otherNormalizedLabels.length); i>=max; i--) {
				String one = normalizedLabels[normalizedLabels.length - i];
				String two = otherNormalizedLabels[otherNormalizedLabels.length - i];
				int result = one.compareTo(two);
				if(result != 0) {
					return result;
				}
			}
			return labels.length - other.labels.length;
		} catch(HostException e) {
			String one = new StringBuilder(toNormalizedString()).reverse().toString();
			String two = new StringBuilder(other.toNormalizedString()).reverse().toString();
			return one.compareTo(two);
		}
	}
	
	public boolean isIpAddress() {
		return isValid() && ipAddressString != null;
	}
	
	/**
	 * If this represents an ip address, returns that address.
	 * Otherwise, returns null.
	 * @return
	 */
	public IPAddress asAddress() {
		if(isIpAddress()) {
			return ipAddressString.getValue();
		}
		return null;
	}
	
	/**
	 * If this represents an ip address, returns that address.
	 * If this represents a host, returns the resolved ip address of that host.
	 * Otherwise, returns null.
	 * @return
	 */
	public synchronized IPAddress resolve() {
		if(isResolved) {
			return resolvedAddress;
		}
		isResolved = true;
		try {
			validate();
			if(host.length() == 0) {//depending on how this object was constructed, empty strings are sometimes considered valid
				return null;
			}
			try {
				InetAddress netAddress = InetAddress.getByName(host);
				if(netAddress != null) {
					String ipstr = netAddress.toString();
					int index = ipstr.indexOf(IPAddressString.PREFIX_LEN_SEPARATOR);
					if(index >= 0) {
						String addr = ipstr.substring(index + 1);
						//addr is what is returned by return numericToTextFormat(getAddress()) for both IPV4 and IPV6
						//this means that the address is already normalized
						return resolvedAddress = new IPAddressString(addr).getValue();
					}
				    //getAddress() returns the IP address in a byte[]. There is a problem
				    //converting it to string. byte[] ipaddress= netAddress.getAddress(); IP = new String(netAddress.getAddress(),"UTF-8");
				}
			} catch(UnknownHostException e) {
				AdHocLogger.logException(e);
				AdHocLogger.logDebug("Problem converting host to IP Address: " + e.getMessage(), AdHocLogger.LOG_ERRORS);
			}
		} catch(HostException e) {
			AdHocLogger.logException(e);
			AdHocLogger.logDebug("Problem converting host to IP Address: " + e.getMessage(), AdHocLogger.LOG_ERRORS);
		}
		return null;
	}
}
