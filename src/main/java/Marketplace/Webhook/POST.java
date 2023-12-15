package Marketplace.Webhook;


import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import org.json.JSONObject;

import static spark.Spark.post;

public class POST {


    public static void run() {

        //Write POST function for users to create an offer

        post("/endEvent/", (request, response) -> {
            try {
                // Extract the parameters from the request
                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                String verificationToken = request.queryParams("verificationToken");

                Collection c = Collections.getCollectionByAddress(collectionAddress);
                if(c == null || !(c instanceof EventNFTCollection)) return getFail("message", "Collection not found");

                //collection.end();
                return getSuccess("message", "This function is disabeled");
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