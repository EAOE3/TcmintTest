package Marketplace.CompletedTrades;

import Database.DBM;
import Marketplace.Analytics.Analytics;

import java.math.BigInteger;

public class CompletedTrade extends DBM {

    public final String COLLECTION_ADDRESS;
    public final int NFT_ID;
    public final String BUYER_ADDRESS;
    public final String SELLER_ADDRESS;
    public final BigInteger PRICE;
    public final long TIME;
    public final String TRANSACTION_HASH ;

    public CompletedTrade(String collectionAddress, int nftId, String buyerAddress, String sellerAddress, BigInteger price, long time, String transactionHash) {
        super(transactionHash, false, false);

        COLLECTION_ADDRESS = collectionAddress;
        this.NFT_ID = nftId;
        this.BUYER_ADDRESS = buyerAddress;
        this.SELLER_ADDRESS = sellerAddress;
        this.PRICE = price;
        this.TIME = time;
        this.TRANSACTION_HASH = transactionHash;

        //Store in DB
        store("collectionAddress", collectionAddress);
        store("nftId", nftId);
        store("buyerAddress", buyerAddress);
        store("sellerAddress", sellerAddress);
        store("price", price);
        store("time", time);
        store("transactionHash", transactionHash);

        CompletedTrades.addCompletedTrade(collectionAddress, nftId, this);
        Analytics.increaseTradingVolume(collectionAddress, price);
    }

    //Load from DB constructor
    public CompletedTrade(String transactionHash) {
        super(transactionHash, false, false);

        this.COLLECTION_ADDRESS = loadString("collectionAddress");
        this.NFT_ID = loadInt("nftId");
        this.BUYER_ADDRESS = loadString("buyerAddress");
        this.SELLER_ADDRESS = loadString("sellerAddress");
        this.PRICE = loadBigInt("price");
        this.TIME = loadLong("time");
        this.TRANSACTION_HASH = loadString("transactionHash");

        CompletedTrades.addCompletedTrade(COLLECTION_ADDRESS, NFT_ID, this);
        Analytics.increaseTradingVolume(COLLECTION_ADDRESS, PRICE);
    }

    //Getters ==========================================================================================================

    public String getCollectionAddress() {
        return COLLECTION_ADDRESS;
    }

    public int getNftId() {
        return NFT_ID;
    }

    public String getBuyerAddress() {
        return BUYER_ADDRESS;
    }

    public String getSellerAddress() {
        return SELLER_ADDRESS;
    }

    public BigInteger getPrice() {
        return PRICE;
    }

    public long getTime() {
        return TIME;
    }

    public String getTransactionHash() {
        return TRANSACTION_HASH;
    }

}
