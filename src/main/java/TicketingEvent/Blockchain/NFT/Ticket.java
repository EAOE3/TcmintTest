package TicketingEvent.Blockchain.NFT;

import Main.ByteArrayWrapper;
import NFTCollections.Collection.Collections;
import NFTCollections.NFT.NFT;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;

import java.math.BigInteger;
import java.time.Instant;

public class Ticket extends NFT {

    private boolean used = false;
    private String randomHash = null;
    public final String ticketType;
    public final String seat;

    //    public NFT(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, byte[] owner) {
    public Ticket(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash, String ticketType, String seat) {
    	super(collectionAddress, nftId, mintingTime, purchasePrice, owner, txnHash);

        this.ticketType = ticketType;   
        this.seat = seat;

        store("ticketType", ticketType);
        store("seat", seat);

        Collections.getCollectionByAddress(collectionAddress).addNft(this);
    }
    
    public Ticket(String collectionAddressAndId) {
    	super(collectionAddressAndId);

        used = loadBoolean("used");
        randomHash = loadString("randomHash");
        ticketType = loadString("ticketType");   
        seat = loadString("seat");

        Collections.getCollectionByAddress(collectionAddress).addNft(this);
    }


    public void setRandomHash(String randomHash) {
        this.randomHash = randomHash;
        store("randomHash", randomHash);
    }

    public void setUsed(boolean used) {
        this.used = used;
    }


    public int getNftId() {
    	return nftId;
    }
    
    public boolean isUsed() {
    	return used;
    }

    public String getRandomHash() {
        return randomHash;
    }

    public String getTicketType() {
        return ticketType;
    }

    //It is a utility token when it can still be used to entre the event
    //It is a collectible once it can't be used to entre the event
    public boolean isCollectable() {
        if(used || randomHash != null) return true;
        else if(((EventNFTCollection)Collections.getCollectionByAddress(collectionAddress)).isEnded()) return true;
        else return false;
    }

}
