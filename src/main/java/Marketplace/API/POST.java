package Marketplace.API;

import static spark.Spark.post;

import Collection.Ticket;
import Collection.Tickets;
import Main.Response;
import Main.Signature;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Verification.SmartContract.SignatureVerification;
import org.web3j.crypto.Sign;

import java.math.BigInteger;

public class POST {


    public static void run() {

        //Write POST function for users to create an offer

        post("/createOffer", (request, response) -> {
            try {
                // Extract the parameters from the request
                String buyOrSell = request.queryParams("buyOrSell");
                String collectionAddress = request.queryParams("collectionAddress");
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                String tokenAddress = request.queryParams("tokenAddress");
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));
                byte[] offerorsSignature = request.queryParams("offerorsSignature").getBytes();
                String signer;

                if(buyOrSell.equalsIgnoreCase("buy")) {
                    String agreement = Offers.getBuyOfferAgreement(collectionAddress, nftId, tokenAddress, tokenAmount, deadline);
                    signer = Signature.recoverSigner(agreement, Hex.toHexString(offerorsSignature));

                    //TODO check that signer has sufficient balance
                }
                else if(buyOrSell.equalsIgnoreCase("sell")) {
                    String agreement = Offers.getSellOfferAgreement(collectionAddress, nftId, tokenAddress, tokenAmount, deadline);
                    signer = Signature.recoverSigner(agreement, Hex.toHexString(offerorsSignature));

                    String ticketOwner = Tickets.getTicketByCollectionAddressAndNftId(collectionAddress, nftId).getOwner();
                    if(!signer.equalsIgnoreCase(ticketOwner)) {
                        return getFail("message", "Invalid Signer");
                    }
                }
                else {
                    return getFail("message", "Invalid buyOrSell parameter");
                }

                try {
                    // Create an instance of the Offer class
                    new Offer(buyOrSell, collectionAddress, nftId, tokenAddress, tokenAmount, deadline, signer, offerorsSignature);
                } catch (Exception e) {
                    return getFail("message", e.getMessage());
                }

                return getSuccess("message", "Offer created successfully");
            } catch (Exception e) {
                response.status(400); // Bad Request
                return "Error creating the offer: " + e.getMessage();
            }
        });

//        //Write POST function for wallet users to accept an offer
        post("/takeOffer", (request, response) -> {
            try {
                // Extract the parameters from the request
                String offereesAddress = request.queryParams("offeree"); //Person accepting the offer
                String offerId = request.queryParams("offerId");
                String verificationToken = request.queryParams("verificationToken");

                Wallet w = Wallets.getWalletByAddress(offereesAddress);
                if(w == null) return getFail("message", "Wallet not found");

                Offer offer = Offers.getOfferById(offerId);
                if(offer == null) return getFail("message", "Offer not found");

                Response r;
                if(offer.buyOrSell.equalsIgnoreCase("buy")) {
                    r = w.takeBuyOffer(offer);
                }
                else {
                    r = w.takeSellOffer(offer);
                }

                if(r.success) {
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                response.status(400); // Bad Request
                return "Error creating the offer: " + e.getMessage();
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



}

