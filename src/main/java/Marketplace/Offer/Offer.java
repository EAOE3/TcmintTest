package Marketplace.Offer;


import Marketplace.Analytics.Analytics;
import TicketingEvent.Blockchain.NFT.Ticket;
import Database.DBM;
import Main.Sha3;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;

import java.math.BigInteger;
import java.time.Instant;

public class Offer extends DBM {

    public final String buyOrSell;
    public final String collectionAddress;
    public final int nftId;
    public final BigInteger tokenAmount;
    public final long deadline;
    public final String offerorsAddress;
    public final byte[] offerorsSignature;

    public Offer(String buyOrSell, String collectionAddress, int nftId, BigInteger tokenAmount, long deadline, String offerorsAddress, byte[] offerorsSignature) throws Exception {
        super(Sha3.getRandomHash(), false, true);

        if(buyOrSell.equalsIgnoreCase("buy")) {
            //TODO check if buyer has enough TMT to buy the ticket
        }
        else if(buyOrSell.equalsIgnoreCase("sell")) {
            NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);
            if(n == null || !n.getHexOwner().equalsIgnoreCase(offerorsAddress)) {
                throw new Exception("Not Owner of Ticket");
            }

            //New Floor Price
            if(Analytics.getFloorPrice(collectionAddress).compareTo(tokenAmount) == 1) {
                Analytics.setFloorPrice(collectionAddress, tokenAmount);
            }
        }
        else {
            throw new Exception("Invalid buyOrSell parameter");
        }

        this.buyOrSell = buyOrSell;
        this.collectionAddress = collectionAddress;
        this.nftId = nftId;
        this.tokenAmount = tokenAmount;
        this.deadline = deadline;
        this.offerorsAddress = offerorsAddress;
        this.offerorsSignature = offerorsSignature;

        store("buyOrSell", buyOrSell);
        store("collectionAddress", collectionAddress);
        store("nftId", nftId);
        store("tokenAmount", tokenAmount);
        store("deadline", deadline);
        store("offerorsAddress", offerorsAddress);
        store("offerorsSignature", offerorsSignature);

        Offers.add(this);
    }

    //Loading Constructor
    public Offer(String id) throws Exception {
        super(id, false, true);

        deadline = loadLong("deadline");
        if(Instant.now().getEpochSecond() > deadline) {
            throw new Exception("Offer Expired");
        }

        buyOrSell = loadString("buyOrSell");
        collectionAddress = loadString("collectionAddress");
        nftId = loadInt("nftId");
        offerorsAddress = loadString("offerorsAddress");
        tokenAmount = loadBigInt("tokenAmount");

        if(buyOrSell.equalsIgnoreCase("buy")) {
            //TODO check if buyer has enough TMT to buy the ticket
        }
        else if(buyOrSell.equalsIgnoreCase("sell")) {
            NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);
            if(n == null || !n.getHexOwner().equalsIgnoreCase(offerorsAddress)) {
                throw new Exception("Not Owner of Ticket");
            }

            //New Floor Price
            if(Analytics.getFloorPrice(collectionAddress).compareTo(tokenAmount) == 1) {
                Analytics.setFloorPrice(collectionAddress, tokenAmount);
            }
        }
        else {
            deleteAll();
            throw new Exception("Invalid buyOrSell parameter");
        }

        offerorsSignature = load("offerorsSignature");

        Offers.add(this);
    }

    public boolean isValid() {
        try {
            if (buyOrSell.equalsIgnoreCase("buy")) {
                //TODO check if buyer has enough TMT to buy the ticket
            } else if (buyOrSell.equalsIgnoreCase("sell")) {
                NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);

                if(n == null || !(n instanceof Ticket)) return false;

                Ticket ticket = (Ticket) n;
                if (ticket == null || !ticket.getHexOwner().equalsIgnoreCase(offerorsAddress)) {
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
