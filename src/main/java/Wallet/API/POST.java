package Wallet.API;

import static spark.Spark.post;

import Collection.Ticket;
import Collection.Tickets;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Verification.SmartContract.SignatureVerification;

public class POST {


    public static void run() {

        post("/signMessage/", (request, response) ->
        {
            response.header("Content-Type", "application/json");
            String message = request.queryParams("message");
            String usersAddress = request.queryParams("usersAddress");
            String verificationToken = request.queryParams("verificationToken");

            //TODO verify verification token

            Wallet w = Wallets.getWalletByAddress(usersAddress);
            if(w == null) return getFail("message", "Invalid User");

            byte[] signature = w.signMessage(message);

            return getSuccess("signature", Hex.toHexString(signature));
        } );

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

