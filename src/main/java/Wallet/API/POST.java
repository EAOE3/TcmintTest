package Wallet.API;

import static spark.Spark.post;


import Main.Settings;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import Main.Response;
import TicmintToken.Token.TMT;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;


import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class POST {


    public static void run() {

        post("/signMessage/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                String message = request.queryParams("message");
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                String verificationToken = request.queryParams("verificationToken");

                //TODO verify verification token

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if (w == null) return getFail("message", "Invalid User");

                byte[] signature = w.signMessage(message);

                return getSuccess("signature", Hex.toHexString(signature));
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        } );

        post("/personalSignMessage/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                String message = request.queryParams("message");
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                String verificationToken = request.queryParams("verificationToken");

                //TODO verify verification token

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if (w == null) return getFail("message", "Invalid User");

                byte[] signature = w.personalSignMessage(message);

                return getSuccess("signature", Hex.toHexString(signature));
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        } );

        //TODO testing
        post("/buyTickets/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                JSONObject data = new JSONObject(request.body());

                String usersAddress = data.getString("usersAddress").toLowerCase();
                String verificationToken = data.getString("verificationToken");
                String collectionAddress = data.getString("collectionAddress").toLowerCase();
                List<String> ticketTypes = new LinkedList<>();
                List<BigInteger> amount = new LinkedList<>();
                List<String> seatNumber = new LinkedList<>();

                for(Object o: data.getJSONArray("ticketTypes")) {
                    ticketTypes.add(o.toString());
                }

                for(Object o: data.getJSONArray("amount")) {
                    amount.add(BigInteger.valueOf((Integer) o));
                }

                for(Object o: data.getJSONArray("seatNumber")) {
                    seatNumber.add(o.toString());
                }

                //TODO verify verification token

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if (w == null) return getFail("message", "Invalid User");

                BigInteger totalPrice = BigInteger.valueOf(0);
                for(int t=0; t < ticketTypes.size(); ++t) {
                    String ticketType = ticketTypes.get(t);
                    BigInteger amountOfTickets = amount.get(t);

                    totalPrice = totalPrice.add(TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(collectionAddress, ticketType).getPrice().multiply(amountOfTickets));
                }

                if(Settings.tmt.getUserBalance(usersAddress).compareTo(totalPrice) < 0) {
                    return getFail("message", "Insufficient Balance");
                }

                Response r = w.buyTickets(collectionAddress, ticketTypes, amount, seatNumber);

                if(r.success) {
                    return getSuccess("transactionHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                return getError(e.getMessage());
            }
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

