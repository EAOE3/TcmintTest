package Marketplace.API;

import static spark.Spark.get;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import Marketplace.Analytics.Analytics;
import Marketplace.CompletedTrades.CompletedTrade;
import Marketplace.CompletedTrades.CompletedTrades;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.NFT.Ticket;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;


public class GET {

    public static void run() {

        get("/allOffers/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));

                List<Offer> offers = Offers.getOffersByCollectionAndNft(collectionAddress, nftId);
                JSONArray array = new JSONArray();
                if(offers != null) {
                    for (Offer o : offers) {
                        JSONObject offerObject = new JSONObject();

                        offerObject.put("buyOrSell", o.buyOrSell);
                        offerObject.put("offerId", o.id);
                        offerObject.put("collectionAddress", o.collectionAddress);
                        offerObject.put("nftId", o.nftId);
                        offerObject.put("tokenAmount", o.tokenAmount);
                        offerObject.put("deadline", o.deadline);
                        offerObject.put("signer", o.offerorsAddress);
                        offerObject.put("offerorsSignature", Hex.toHexString(o.offerorsSignature));

                        array.put(offerObject);
                    }
                }

                return getSuccess("offers", array);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/allOffersForAnNFT/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                JSONObject object = new JSONObject(request.body());

                JSONArray collectionAddresses = object.getJSONArray("collectionAddresses");
                JSONArray nftIds = object.getJSONArray("nftIds");

                int size = collectionAddresses.length();
                JSONArray returnObject = new JSONArray();
                for(int t=0; t < size; ++t) {
                    String collectionAddress = collectionAddresses.getString(t).toLowerCase();
                    int nftId = nftIds.getInt(t);

                    List<Offer> offers = Offers.getOffersByCollectionAndNft(collectionAddress, nftId);
                    JSONArray array = new JSONArray();
                    if(offers != null) {
                        for (Offer o : offers) {
                            JSONObject offerObject = new JSONObject();

                            offerObject.put("buyOrSell", o.buyOrSell);
                            offerObject.put("offerId", o.id);
                            offerObject.put("collectionAddress", o.collectionAddress);
                            offerObject.put("nftId", o.nftId);
                            offerObject.put("tokenAmount", o.tokenAmount);
                            offerObject.put("deadline", o.deadline);
                            offerObject.put("signer", o.offerorsAddress);
                            offerObject.put("offerorsSignature", Hex.toHexString(o.offerorsSignature));

                            array.put(offerObject);
                        }
                    }

                    JSONObject nftObject = new JSONObject();
                    nftObject.put("offers", array);
                    nftObject.put("collectionAddress", collectionAddress);
                    nftObject.put("nftId", nftId);

                    returnObject.put(nftObject);
                }
                return getSuccess("nftOffers", returnObject);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/allCollectionOffers/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();

                Collection c = Collections.getCollectionByAddress(collectionAddress);
                if(c == null || !(c instanceof EventNFTCollection)) {
                    return getFail("message", "Invalid collectionAddress");
                }
                EventNFTCollection collection = (EventNFTCollection) c;

                List<Offer> allOffers = new LinkedList<>();

                for(Ticket t: collection.getAllTickets()) {
                    List<Offer> offers = Offers.getOffersByCollectionAndNft(collectionAddress, t.getNftId());
                    if(offers != null) {allOffers.addAll(offers);}
                }

                JSONArray array = new JSONArray();
                for(Offer o: allOffers) {
                    JSONObject offerObject = new JSONObject();

                    offerObject.put("buyOrSell", o.buyOrSell);
                    offerObject.put("offerId", o.id);
                    offerObject.put("collectionAddress", o.collectionAddress);
                    offerObject.put("nftId", o.nftId);
                    offerObject.put("tokenAmount", o.tokenAmount);
                    offerObject.put("deadline", o.deadline);
                    offerObject.put("signer", o.offerorsAddress);
                    offerObject.put("offerorsSignature", Hex.toHexString(o.offerorsSignature));

                    array.put(offerObject);
                }

                return getSuccess("offers", array);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/buyAgreement/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));

                String agreement = Offers.getBuyOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                return getSuccess("agreement", agreement);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/sellAgreement/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));

                String agreement = Offers.getSellOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                return getSuccess("agreement", agreement);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/isOfferStillValid/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String offerId = request.queryParams("offerId").toLowerCase();

                Offer offer = Offers.getOfferById(offerId);

                if(offer == null) {
                    return getSuccess("isOfferStillValid", false);
                }
                else if(!offer.isValid()) {
                    Offers.remove(offer);
                    return getSuccess("isOfferStillValid", false);
                }

                return getSuccess("isOfferStillValid", true);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/allContractsWithListedNfts/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                List<Offer> allOffers = Offers.getAllOffers();
                List<String> addedAddressesToTheArray = new LinkedList<>();

                JSONArray contractsWithListedNfts = new JSONArray();

                if(allOffers != null) {
                    for (Offer o : allOffers) {
                        if (addedAddressesToTheArray.contains(o.collectionAddress)) continue;

                        contractsWithListedNfts.put(o.collectionAddress);
                        addedAddressesToTheArray.add(o.collectionAddress);
                    }
                }

                return getSuccess("collectionsWithListedNfts", contractsWithListedNfts);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/allContractsWithNfts/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                List<Collection> collections = Collections.getAllCollections();

                JSONArray contractsWithNfts = new JSONArray();

                for(Collection c: collections) {
                    if(c.getNftCount() > 0) contractsWithNfts.put(c.getCollectionAddress());
                }

                return getSuccess("colelctionsWithNfts", contractsWithNfts);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/tradingHistory/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));

                List<CompletedTrade> completedTrades = CompletedTrades.getCompletedTrades(collectionAddress, nftId);

                JSONArray array = new JSONArray();

                //String collectionAddress, int nftId, String buyerAddress, String sellerAddress, BigInteger price, long time, String transactionHash
                for (CompletedTrade ct : completedTrades) {
                    JSONObject object = new JSONObject();

                    object.put("collectionAddress", ct.getCollectionAddress());
                    object.put("nftId", ct.getNftId());
                    object.put("buyerAddress", ct.getBuyerAddress());
                    object.put("sellerAddress", ct.getSellerAddress());
                    object.put("price", ct.getPrice());
                    object.put("time", ct.getTime());
                    object.put("transactionHash", ct.getTransactionHash());

                    array.put(object);
                }

                return getSuccess("tradingHistory", array);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/collectionTradingVolume/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();

                BigInteger tradingVolume = Analytics.getTradingVolume(collectionAddress);

                return getSuccess("tradingVolume", tradingVolume);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/collectionFloorPrice/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();

                BigInteger floorPrice = Analytics.getFloorPrice(collectionAddress);

                return getSuccess("floorPrice", floorPrice);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/percentageOfListedNftsInACollection/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();

                int amountOfNftsInThisCollection = Collections.getCollectionByAddress(collectionAddress).getNftCount();
                int amountOfListedNftsInThisCollection;

                List<Offer> allOfferss = Offers.getAllOffersByCollection(collectionAddress);
                List<Integer> listedNfts = new LinkedList<>();

                for(Offer o: allOfferss) {
                    if(!listedNfts.contains(o.nftId)) {
                        listedNfts.add(o.nftId);
                    }
                }

                amountOfListedNftsInThisCollection = listedNfts.size();

                double percentageWithTwoDecimals = Math.round(((double)amountOfListedNftsInThisCollection / (double)amountOfNftsInThisCollection) * 10000.0) / 100.0;

                return getSuccess("percentageOfListedNfts", percentageWithTwoDecimals);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

    }

    // There was a problem with the data submitted, or some pre-condition of the API
    // call wasn't satisfied
    public static JSONObject getFail(Object... variables) throws Exception {
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();

        int size = variables.length;
        if (size % 2 != 0)
            throw new Exception("Provided variables length should be even when using getSuccess");

        for (int t = 0; t < size; t += 2) {
            data.put(variables[t].toString(), variables[t + 1]);
        }

        object.put("status", "fail");
        object.put("data", data);

        return object;
    }

    // There was a problem on the server side
    public static JSONObject getError(String message) {
        JSONObject object = new JSONObject();

        object.put("status", "error");
        object.put("message", message);

        return object;
    }

    public static JSONObject getSuccess(Object... variables) throws Exception {
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();

        int size = variables.length;
        if (size % 2 != 0)
            throw new Exception("Provided variables length should be even when using getSuccess");

        for (int t = 0; t < size; t += 2) {
            data.put(variables[t].toString(), variables[t + 1]);
        }

        object.put("status", "success");
        object.put("data", data);

        return object;
    }

    //Internal Functions=======================================================================================================================
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}

