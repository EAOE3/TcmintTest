package Marketplace.API;

import static spark.Spark.get;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Collection.Ticket;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import org.json.JSONArray;
import org.json.JSONObject;

import Wallet.Wallet.Wallet;
import Wallet.Wallet.Wallets;

public class GET {

    public static void run() {

        get("/allOffers/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress");
                int nftId = Integer.parseInt(request.queryParams("nftId"));

                List<Offer> offers = Offers.getOffersByCollectionAndNft(collectionAddress, nftId);
                JSONArray array = new JSONArray();
                for(Offer o: offers) {
                    JSONObject offerObject = new JSONObject();

                    offerObject.put("buyOrSell", o.buyOrSell);
                    offerObject.put("offerId", o.id);
                    offerObject.put("collectionAddress", o.collectionAddress);
                    offerObject.put("nftId", o.nftId);
                    offerObject.put("tokenAddress", o.tokenAddress);
                    offerObject.put("tokenAmount", o.tokenAmount);
                    offerObject.put("deadline", o.deadline);
                    offerObject.put("signer", o.offerorsAddress);
                    offerObject.put("offerorsSignature", o.offerorsSignature);

                    array.put(offerObject);
                }

                return getSuccess("offers", array);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/allCollectionOffers/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress");

                EventNFTCollection collection = EventNFTCollections.getEventByAddress(collectionAddress);
                if(collection == null) {
                    return getFail("message", "Invalid collectionAddress");
                }

                List<Offer> allOffers = new LinkedList<>();

                for(Ticket t: collection.getAllTickets()) {
                    List<Offer> offers = Offers.getOffersByCollectionAndNft(collectionAddress, t.getNftId());
                    allOffers.addAll(offers);
                }

                JSONArray array = new JSONArray();
                for(Offer o: allOffers) {
                    JSONObject offerObject = new JSONObject();

                    offerObject.put("buyOrSell", o.buyOrSell);
                    offerObject.put("offerId", o.id);
                    offerObject.put("collectionAddress", o.collectionAddress);
                    offerObject.put("nftId", o.nftId);
                    offerObject.put("tokenAddress", o.tokenAddress);
                    offerObject.put("tokenAmount", o.tokenAmount);
                    offerObject.put("deadline", o.deadline);
                    offerObject.put("signer", o.offerorsAddress);
                    offerObject.put("offerorsSignature", o.offerorsSignature);

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

                String collectionAddress = request.queryParams("collectionAddress");
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                String tokenAddress = request.queryParams("tokenAddress");
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));

                String agreement = Offers.getBuyOfferAgreement(collectionAddress, nftId, tokenAddress, tokenAmount, deadline);
                return getSuccess("agreement", agreement);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/sellAgreement/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String collectionAddress = request.queryParams("collectionAddress");
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                String tokenAddress = request.queryParams("tokenAddress");
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));

                String agreement = Offers.getSellOfferAgreement(collectionAddress, nftId, tokenAddress, tokenAmount, deadline);
                return getSuccess("agreement", agreement);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        get("/isOfferStillValid/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String offerId = request.queryParams("offerId");

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

