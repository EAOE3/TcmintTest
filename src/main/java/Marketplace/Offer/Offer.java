package Marketplace.Offer;

import Collection.Ticket;
import Collection.Tickets;
import Database.DBM;
import Main.ERC20;
import Main.Sha3;

import java.math.BigInteger;
import java.time.Instant;

public class Offer extends DBM {

    public final String buyOrSell;
    public final String collectionAddress;
    public final int nftId;
    public final String tokenAddress;
    public final BigInteger tokenAmount;
    public final long deadline;
    public final String offerorsAddress;
    public final byte[] offerorsSignature;

    public Offer(String buyOrSell, String collectionAddress, int nftId, String tokenAddress, BigInteger tokenAmount, long deadline, String offerorsAddress, byte[] offerorsSignature) throws Exception {
        super(Sha3.getRandomHash());

        if(buyOrSell.equalsIgnoreCase("buy")) {
            if (ERC20.getBalance(tokenAddress, offerorsAddress).compareTo(tokenAmount) < 0) {
                throw new Exception("Insufficient balance for this operation");
            }
        }
        else if(buyOrSell.equalsIgnoreCase("sell")) {
            Ticket ticket = Tickets.getTicketByCollectionAddressAndNftId(collectionAddress, nftId);
            if(ticket == null || !ticket.getOwner().equalsIgnoreCase(offerorsAddress)) {
                throw new Exception("Not Owner of Ticket");
            }
        }
        else {
            throw new Exception("Invalid buyOrSell parameter");
        }

        this.buyOrSell = buyOrSell;
        this.collectionAddress = collectionAddress;
        this.nftId = nftId;
        this.tokenAddress = tokenAddress;
        this.tokenAmount = tokenAmount;
        this.deadline = deadline;
        this.offerorsAddress = offerorsAddress;
        this.offerorsSignature = offerorsSignature;

        store("buyOrSell", buyOrSell);
        store("collectionAddress", collectionAddress);
        store("nftId", nftId);
        store("tokenAddress", tokenAddress);
        store("tokenAmount", tokenAmount);
        store("deadline", deadline);
        store("offerorsAddress", offerorsAddress);
        store("offerorsSignature", offerorsSignature);

    }

    //Loading Constructor
    public Offer(String id) {
        super(id);

        buyOrSell = loadString("buyOrSell");
        collectionAddress = loadString("collectionAddress");
        nftId = loadInt("nftId");
        tokenAddress = loadString("tokenAddress");
        tokenAmount = loadBigInt("tokenAmount");
        deadline = loadLong("deadline");
        offerorsAddress = loadString("offerorsAddress");
        offerorsSignature = load("offerorsSignature");

        if(Instant.now().getEpochSecond() < deadline) {
            Offers.add(this);
        }
    }

    public boolean isValid() {
        try {
            if (buyOrSell.equalsIgnoreCase("buy")) {
                if (ERC20.getBalance(tokenAddress, offerorsAddress).compareTo(tokenAmount) < 0) {
                    return false;
                }
            } else if (buyOrSell.equalsIgnoreCase("sell")) {
                Ticket ticket = Tickets.getTicketByCollectionAddressAndNftId(collectionAddress, nftId);
                if (ticket == null || !ticket.getOwner().equalsIgnoreCase(offerorsAddress)) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
