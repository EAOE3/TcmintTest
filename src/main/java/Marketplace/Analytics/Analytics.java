package Marketplace.Analytics;

import Marketplace.CompletedTrades.CompletedTrade;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Analytics {

    private static ConcurrentHashMap<String /*Contract Address*/, BigInteger /*Trading Volume*/> tradingVolumeByCollectionAddress = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String /*Contract Address*/, BigInteger /*Floor Price*/> floorPriceByCollectionAddress = new ConcurrentHashMap<>();

    public static void increaseTradingVolume(String contractAddress, BigInteger amount) {
        if(tradingVolumeByCollectionAddress.containsKey(contractAddress)) {
            tradingVolumeByCollectionAddress.put(contractAddress, tradingVolumeByCollectionAddress.get(contractAddress).add(amount));
        } else {
            tradingVolumeByCollectionAddress.put(contractAddress, amount);
        }
    }

    public static void setFloorPrice(String contractAddress, BigInteger floorPrice) {
        floorPriceByCollectionAddress.put(contractAddress, floorPrice);
    }

    public static BigInteger getTradingVolume(String contractAddress) {
        if(tradingVolumeByCollectionAddress.containsKey(contractAddress)) {
            return tradingVolumeByCollectionAddress.get(contractAddress);
        } else {
            return BigInteger.ZERO;
        }
    }

    public static BigInteger getFloorPrice(String contractAddress) {
        if(floorPriceByCollectionAddress.containsKey(contractAddress)) {
            return floorPriceByCollectionAddress.get(contractAddress);
        } else {
            return BigInteger.ZERO;
        }
    }
}
