package Marketplace.Offer;

import Marketplace.Analytics.Analytics;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Offers {
    private static Map<String /*Offer Id*/, Offer> offerById = new HashMap<>();
    private static Map<String /*collectionAddress-nftId*/, List<Offer>> offersByCollectionAndNft= new HashMap<>();
    private static Map<String /*Collection Address*/, List<Offer>> offersByCollection = new HashMap<>();
    private static List<Offer> allOffers = new LinkedList<>();

    public static void add(Offer o) {
        offerById.put(o.id, o);

        if(offersByCollectionAndNft.containsKey(o.collectionAddress + "-" + o.nftId)) {
            offersByCollectionAndNft.get(o.collectionAddress + "-" + o.nftId).add(o);
        } else {
            List<Offer> offersList = new LinkedList<>();
            offersList.add(o);
            offersByCollectionAndNft.put(o.collectionAddress + "-" + o.nftId, offersList);
        }

        if(offersByCollection.containsKey(o.collectionAddress.toLowerCase())) {
            offersByCollection.get(o.collectionAddress.toLowerCase()).add(o);
        } else {
            List<Offer> offersList = new LinkedList<>();
            offersList.add(o);
            offersByCollection.put(o.collectionAddress.toLowerCase(), offersList);
        }

        allOffers.add(o);
    }
    public static void remove(Offer o) {
        offerById.remove(o.id);

        List<Offer> offers = offersByCollectionAndNft.get(o.collectionAddress + "-" + o.nftId);
        if(offers == null) return;
        offers.remove(o);

        List<Offer> offersInCollection = offersByCollection.get(o.collectionAddress.toLowerCase());
        offersInCollection.remove(o);

        allOffers.remove(o);

        if(o.buyOrSell.equalsIgnoreCase("sell") && Analytics.getFloorPrice(o.collectionAddress).compareTo(o.tokenAmount) == 0) {
            BigInteger newFloorPrice = BigInteger.ZERO;
            for(Offer offer: offersInCollection) {
                if(offer.buyOrSell.equalsIgnoreCase("sell")) {
                    if(newFloorPrice.compareTo(BigInteger.ZERO) == 0) {
                        newFloorPrice = offer.tokenAmount;
                    }
                    else if(newFloorPrice.compareTo(offer.tokenAmount) == 1) {
                        newFloorPrice = offer.tokenAmount;
                    }
                }
            }
            Analytics.setFloorPrice(o.collectionAddress, newFloorPrice);
        }
    }

    //Used when a Ticket gets used for entry
    public static void removeAllOffersOfAnNft(String collectionAddress, int nftId) {
        List<Offer> offers = offersByCollectionAndNft.get(collectionAddress + "-" + nftId);
        if(offers == null) return;

        List<Offer> allOffersInTheCollection = offersByCollection.get(collectionAddress.toLowerCase());
        for(Offer o: offers) {
            offerById.remove(o.id);
            allOffers.remove(o);
            allOffersInTheCollection.remove(o);
        }
        offersByCollectionAndNft.remove(collectionAddress + "-" + nftId);

        for(Offer o: offers) {
            if(o.buyOrSell.equalsIgnoreCase("sell") && Analytics.getFloorPrice(o.collectionAddress).compareTo(o.tokenAmount) == 0) {
                BigInteger newFloorPrice = BigInteger.ZERO;
                for(Offer offer: allOffersInTheCollection) {
                    if(offer.buyOrSell.equalsIgnoreCase("sell")) {
                        if(newFloorPrice.compareTo(BigInteger.ZERO) == 0) {
                            newFloorPrice = offer.tokenAmount;
                        }
                        else if(newFloorPrice.compareTo(offer.tokenAmount) == 1) {
                            newFloorPrice = offer.tokenAmount;
                        }
                    }
                }
                Analytics.setFloorPrice(o.collectionAddress, newFloorPrice);
                return;
            }
        }

    }
    public static Offer getOfferById(String id) {
        return offerById.get(id);
    }

    public static List<Offer> getOffersByCollectionAndNft(String collectionAddress, int nftId) {
        return offersByCollectionAndNft.get(collectionAddress + "-" + nftId);
    }

    public static List<Offer> getAllOffers() {
        return allOffers;
    }

    //getAllOffersByCOllection
    public static List<Offer> getAllOffersByCollection(String collectionAddress) {
        return offersByCollection.get(collectionAddress.toLowerCase());
    }

    public static String getBuyOfferAgreement(String collectionAddress, int nftId, BigInteger tokenAmount, long deadline) {
        String agreement = "PLEASE READ THE FOLLOWING INFORMATION CAREFULLY BEFORE PROCEEDING WITH YOUR LISTING:\n\n" +
                "As a prospective seller on the Ticmint Marketplace, you are initiating a legally binding offer to sell a specific Non-Fungible Token (NFT). The details of the NFT and your listing are as follows:\n\n" +
                "1. NFT Identification: The NFT you intend to sell is identified by the ID number " + nftId + ".\n" +
                "2. NFT Collection: The NFT is part of a collection hosted at the contract address " + collectionAddress.toLowerCase() + ".\n" +
                "3. Listing Price: You are proposing to sell the above-mentioned NFT for a total consideration of " + tokenAmount + " Ticmint Tokens.\n" +
                "4. Listing Deadline: Prospective buyers have until epoch second " + deadline + " to accept your listing.\n\n" +
                "By proceeding, you are asserting that you understand the terms of your listing, the item in question, and that you are prepared to finalize the sale under these conditions should a buyer accept within the stated timeframe. Proceed with your listing only if you agree to these terms.\n\n" +
                "The completion of your listing signifies your understanding, acceptance, and willingness to comply with the aforementioned terms and conditions.";

        return agreement;
    }

    public static String getSellOfferAgreement(String collectionAddress, int nftId, BigInteger tokenAmount, long deadline) {
        String agreement = "PLEASE READ THE FOLLOWING INFORMATION CAREFULLY BEFORE PROCEEDING WITH YOUR LISTING:\n\n" +
                "As a prospective seller on the Ticmint Marketplace, you are initiating a legally binding offer to sell a specific Non-Fungible Token (NFT). The details of the NFT and your listing are as follows:\n\n" +
                "1. NFT Identification: The NFT you intend to sell is identified by the ID number " + nftId + ".\n" +
                "2. NFT Collection: The NFT is part of a collection hosted at the contract address " + collectionAddress.toLowerCase() + ".\n" +
                "3. Listing Price: You are proposing to sell the above-mentioned NFT for a total consideration of " + tokenAmount + " Ticmint Tokens.\n" +
                "4. Listing Deadline: Prospective buyers have until epoch second " + deadline + " to accept your listing.\n\n" +
                "By proceeding, you are asserting that you understand the terms of your listing, the item in question, and that you are prepared to finalize the sale under these conditions should a buyer accept within the stated timeframe. Proceed with your listing only if you agree to these terms.\n\n" +
                "The completion of your listing signifies your understanding, acceptance, and willingness to comply with the aforementioned terms and conditions.";

        return agreement;
    }

    public static void initOffersCleanup() {
        new Thread() {
            public void run() {
                while(true) {
                    try{Thread.sleep(1000);}catch(Exception e){}

                    long timeNow = Instant.now().getEpochSecond();

                    for (Offer offer : allOffers) {
                        if(offer.deadline < timeNow) {
                            remove(offer);
                        }
                        else if (offer.buyOrSell.equalsIgnoreCase("sell")) {
                            NFT n = NFTs.getNFTByCollectionAddressAndNftId(offer.collectionAddress, offer.nftId);
                            if(n == null) remove(offer);
                            else if(!n.getHexOwner().equalsIgnoreCase(offer.offerorsAddress)) remove(offer);
                        }
                    }
                }
            }
        }.start();
    }
}
