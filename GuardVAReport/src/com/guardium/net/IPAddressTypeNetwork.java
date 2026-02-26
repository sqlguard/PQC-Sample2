/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.net;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import com.guardium.net.IPAddress.IpVersion;
import com.guardium.net.IPAddressSegment.IPAddressSegmentCreator;

public abstract class IPAddressTypeNetwork<T extends IPAddress, S extends IPAddressSegment> extends IPAddressNetwork {
	private final T subnets[];
	private final T subnetMasks[];
	private final T hostMasks[];
	
	interface IPAddressCreator<T extends IPAddress, S extends IPAddressSegment> extends IPAddressSegmentCreator<S> {
		T createAddress(S segments[]);
	}
	
	IPAddressCreator<T, S> creator;
	
	IPAddressTypeNetwork(Class<T> addressType, IPAddressCreator<T, S> creator) {
		int bitSize = IPAddress.bitCount(getIpVersion());
		this.subnets = IPAddressSection.cast(Array.newInstance(addressType, bitSize + 1));
		this.subnetMasks = this.subnets.clone();
		this.hostMasks = this.subnets.clone();
		this.creator = creator;
	}
	
	public boolean isIpv4() {
		return false;
	}
	
	public boolean isIpv6() {
		return false;
	}
	
	public abstract IpVersion getIpVersion();
	
	@Override
	public T getNetworkMask(int cidrPrefix) {
		return getNetworkMask(cidrPrefix, true);
	}
	
	@Override
	public T getNetworkMask(int cidrPrefix, boolean withPrefixLength) {
		return getMask(cidrPrefix, withPrefixLength ? subnets : subnetMasks, true, creator, withPrefixLength);
	}
	
	@Override
	public T getHostMask(int prefixBits) {
		return getMask(prefixBits, hostMasks, false, creator, false);
	}
	
	private T getMask(int cidrPrefixBits, T cache[], boolean network, IPAddressCreator<T, S> creator, boolean withPrefixLength) {
		int bits = cidrPrefixBits;
		IpVersion version = getIpVersion();
		int addressBitLength = IPAddress.bitCount(version);
		if(bits > addressBitLength) {
			bits = addressBitLength;
		}
		int prefixBits = bits;
		int cacheIndex = bits;
		int segmentCount = IPAddress.segmentCount(version);
		int bitsPerSegment = IPAddress.bitsPerSegment(version);
		int onesSubnetIndex = network ? addressBitLength : 0;
		int zerosSubnetIndex = network ? 0 : addressBitLength;
		
		S onesSegment, zerosSegment;
		T onesSubnet = cache[onesSubnetIndex];
		if(onesSubnet == null) {
			synchronized(cache) {
				onesSubnet = cache[onesSubnetIndex];
				if(onesSubnet == null) {
					S newSegments[] = creator.createAddressSegmentArray(segmentCount);
					int maxSegmentValue = IPAddress.maxSegmentValue(version);
					S seg;
					if(network && withPrefixLength) {
						seg = creator.createAddressSegment(maxSegmentValue, IPAddressSection.getSegmentPrefixBits(bitsPerSegment, addressBitLength) /* null */ );//all bits in this segment plus at least 1 from the next
					} else {
						seg = creator.createAddressSegment(maxSegmentValue);
					}
					Arrays.fill(newSegments, seg);
					onesSubnet = cache[onesSubnetIndex] = creator.createAddress(newSegments);
				}
			}
		}
		T zerosSubnet = cache[zerosSubnetIndex];
		if(zerosSubnet == null) {
			synchronized(cache) {
				zerosSubnet = cache[zerosSubnetIndex];
				if(zerosSubnet == null) {
					S newSegments[] = creator.createAddressSegmentArray(segmentCount);
					S seg;
					if(network && withPrefixLength) {
						seg = creator.createAddressSegment(0, IPAddressSection.getSegmentPrefixBits(bitsPerSegment, 0) /* 0 */);
					} else {
						seg = creator.createAddressSegment(0);
					}
					Arrays.fill(newSegments, seg);
					zerosSubnet = cache[zerosSubnetIndex] = creator.createAddress(newSegments);
				}
			}
		}
		onesSegment = IPAddressSection.cast(onesSubnet.getSegments()[0]);
		zerosSegment = IPAddressSection.cast(zerosSubnet.getSegments()[0]);
		
		T subnet = cache[cacheIndex];
		if(subnet == null) {
			synchronized(cache) {
				subnet = cache[cacheIndex];
				if(subnet == null) {
					ArrayList<S> segmentList = new ArrayList<S>(segmentCount);
					int i = 0;
					for(; bits > 0; i++, bits -= bitsPerSegment) {
						boolean bitsExtendsToAddressEnd = network && (bits == bitsPerSegment && i >= segmentCount - 1);  // don't create a segment with a prefix if we are the last segment of the last network mask 
						if(bits <= bitsPerSegment && !bitsExtendsToAddressEnd) {
							S segment = null;
							
							//first do a check whether we have already created a segment like the one we need
							int offset = ((bits - 1) % bitsPerSegment) + 1;
							for(int j = 0, entry = offset; j < segmentCount; j++, entry += bitsPerSegment) {
								if(entry != cacheIndex) { //we already know that the entry at cacheIndex is null
									T prev = cache[entry];
									if(prev != null) {
										bitsExtendsToAddressEnd = network && (offset == bitsPerSegment && j >= segmentCount - 1); // don't reuse the last segment of the last network mask, since it has no prefix bits
										if(!bitsExtendsToAddressEnd) { 
											segment = IPAddressSection.cast(prev.getSegments()[j]);
											break;
										}
									}
								}
							}
							
							//if none of the other addresses with a similar segment are created yet, we need a new segment.
							if(segment == null) {
								int mask = IPAddressSegment.getSegmentCIDRNetworkMask(bits, version);
								if(network) {
									if(withPrefixLength) {
										segment = creator.createAddressSegment(mask, IPAddressSection.getSegmentPrefixBits(bitsPerSegment, bits));
									} else {
										segment = creator.createAddressSegment(mask);
									}
								} else {
									segment = creator.createAddressSegment(IPAddressSegment.getSegmentCIDRHostMask(mask, version));
								}
							}
							segmentList.add(segment);
						} else {
							segmentList.add(network ? onesSegment : zerosSegment);
						}
					}
					for(; i<segmentCount; i++) {
						segmentList.add(network ? zerosSegment : onesSegment);
					}
					S newSegments[] = creator.createAddressSegmentArray(segmentList.size());
					segmentList.toArray(newSegments);
					subnet = cache[cacheIndex] = creator.createAddress(newSegments);
					subnet.addressSegments.setMaskPrefix(prefixBits, network);
				}
			} //end synchronized
		}
		return subnet;
	}
}
