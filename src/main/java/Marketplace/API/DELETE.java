package Marketplace.API;

import static spark.Spark.delete;

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

public class DELETE {


    public static void run() {

        delete("/offer", (request, response) -> {
            try {
                // Extract the parameters from the request
                String offerId = request.queryParams("offerId");
                String verificationToken = request.queryParams("verificationToken");

                //TODO: check the verification token

                Offer offer = Offers.getOfferById(offerId);
                if(offer == null) {
                    return getFail("message", "Offer not found");
                }

                Offers.remove(offer);

                return getSuccess("message", "Offer removed");
            } catch (Exception e) {
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



}


