package Marketplace.CompletedTrades;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CompletedTrades {

    private static ConcurrentHashMap<String /* Contract Address - Nft Id*/, List<CompletedTrade>> completedTradesByCollectionAndNftId = new ConcurrentHashMap<>();

    public static void addCompletedTrade(String contractAddress, int nftId, CompletedTrade completedTrade) {
        if(completedTradesByCollectionAndNftId.containsKey(contractAddress + "-" + nftId)) {
            completedTradesByCollectionAndNftId.get(contractAddress + "-" + nftId).add(completedTrade);
        } else {
            List<CompletedTrade> completedTrades = new LinkedList<>();
            completedTrades.add(completedTrade);
            completedTradesByCollectionAndNftId.put(contractAddress + "-" + nftId, completedTrades);
        }
    }

    public static List<CompletedTrade> getCompletedTrades(String contractAddress, int nftId) {
        if(completedTradesByCollectionAndNftId.containsKey(contractAddress + "-" + nftId)) {
            return completedTradesByCollectionAndNftId.get(contractAddress + "-" + nftId);
        } else {
            return new LinkedList<>();
        }
    }
}
