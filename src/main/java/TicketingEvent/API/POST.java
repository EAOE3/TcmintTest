package TicketingEvent.API;

import TicmintToken.Token.TMT;
import TicketingEvent.Blockchain.TicketType.TicketType;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import TicketingEvent.Webhook.Sender;
import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Wallet.Wallet.Wallet;
import Wallet.Wallet.Wallets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import static spark.Spark.post;

public class POST {

    public static void run() {
        post("/createEvent/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                System.out.println(request.body());
                JSONObject object = new JSONObject(request.body());

                Response res = createNewEvent(object);

                if (res.success) {
                    Sender.addTxnHashAndEndpointUrl(res.message, object.getString("endpointUrl"));
                    Sender.checkTxnAndNotifyServerIfFailed(object.getString("endpointUrl"), res.message);
                    System.out.println("Success");
                    return getSuccess("txnHash", res.message);
                } else {
                    return getFail("message", res.message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        } );

        //TODO testing
        post("/buyTicketsViaMotherWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                JSONObject data = new JSONObject(request.body());

                String serverEndpoint = data.getString("serverEndpoint");
                String usersAddress = data.getString("usersAddress").toLowerCase();
                String collectionAddress = data.getString("collectionAddress").toLowerCase();
                List<Utf8String> ticketTypes = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();
                List<Utf8String> seatNumber = new LinkedList<>();

                JSONArray ticketTypesArray = data.getJSONArray("ticketTypes");
                JSONArray amountArray = data.getJSONArray("amount");
                JSONArray seatNumberArray = data.getJSONArray("seatNumber");

                int size = ticketTypesArray.length();
                if(size != amountArray.length()) {
                    return getFail("message", "Invalid data, ticketTypes and amount should be of same length");
                }

                int totalSeatsTaken = 0;
                for(int t=0; t < size; ++t) {
                    TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(collectionAddress, ticketTypesArray.getString(t));
                    if(tt == null) {
                        return getFail("message", "Invalid ticket type");
                    }

                    if(tt.isSeated()) {
                        int seatsAmount = amountArray.getInt(t);
                        if(seatsAmount > tt.getAvailableSpaces()) {
                            return getFail("message", "Not enough available spaces for ticket type " + tt.getTicketTypeId() );
                        }
                        totalSeatsTaken += seatsAmount;
                        amount.add(new Uint256(BigInteger.valueOf(seatsAmount)));
                    }

                    if(totalSeatsTaken > seatNumberArray.length()) {
                        return getFail("message", "Not enough seat numbers provided");
                    }

                    ticketTypes.add(new Utf8String(ticketTypesArray.getString(t)));
                }

                for (Object o : data.getJSONArray("seatNumber")) {
                    seatNumber.add(new Utf8String(o.toString()));
                }

                //TODO verify verification token

//                Wallet w = Wallets.getWalletByAddress(usersAddress);
//                if (w == null) return getFail("message", "Invalid User");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, collectionAddress, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(usersAddress), new DynamicArray(ticketTypes), new DynamicArray(amount), new DynamicArray(seatNumber));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, serverEndpoint);
                    return getSuccess("transactionHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getMessage());
            }
        });

        post("/buyTicketsViaUserWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                JSONObject data = new JSONObject(request.body());

                String serverEndpoint = data.getString("serverEndpoint");
                String usersAddress = data.getString("usersAddress").toLowerCase();
                String collectionAddress = data.getString("collectionAddress").toLowerCase();
                List<Utf8String> ticketTypes = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();
                List<Utf8String> seatNumber = new LinkedList<>();

                for (Object o : data.getJSONArray("ticketTypes")) {
                    ticketTypes.add(new Utf8String(o.toString()));
                }

                for (Object o : data.getJSONArray("amount")) {
                    amount.add(new Uint256(BigInteger.valueOf((Integer) o)));
                }

                for (Object o : data.getJSONArray("seatNumber")) {
                    seatNumber.add(new Utf8String(o.toString()));
                }

                //TODO verify verification token

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if (w == null) return getFail("message", "Invalid User");

                BigInteger totalPrice = BigInteger.valueOf(0);
                for(int t=0; t < amount.size(); ++t) {
                    TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(collectionAddress, ticketTypes.get(t).getValue());
                    totalPrice = totalPrice.add(amount.get(t).getValue().multiply(tt.price));
                }

                if(TMT.getUserBalance(usersAddress).compareTo(totalPrice) < 0) {
                    return getFail("message", "Insufficient TMT balance");
                }

                Response r = OWeb3j.fuelAndSendScTxn(Settings.web3j, w.getCredentials(), Settings.chainId, usersAddress, collectionAddress, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(usersAddress), new DynamicArray(ticketTypes), new DynamicArray(amount), new DynamicArray(seatNumber));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, serverEndpoint);
                    return getSuccess("transactionHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getMessage());
            }
        });

    }

    public static Response createNewEvent(JSONObject object) throws Exception {

        List<Utf8String> stringData = new LinkedList<>();
        stringData.add(new Utf8String("empty"));
        stringData.add(new Utf8String(object.getString("eventId")));
        stringData.add(new Utf8String(object.getString("name")));
        stringData.add(new Utf8String(object.getString("symbol")));
        stringData.add(new Utf8String(object.getString("baseUrl")));

        List<Uint256> data = new LinkedList<>();
        data.add(new Uint256(object.getLong("eventEndTime")));
        data.add(new Uint256(object.getInt("royalties")));
        data.add(new Uint256(object.getLong("tax")));

        JSONArray ticketTypes = object.getJSONArray("ticketTypes");
        for(Object t: ticketTypes) {
            JSONObject ticketType = (JSONObject) t;

            stringData.add(new Utf8String(ticketType.getString("ticketTypeId")));

            data.add(new Uint256(ticketType.getInt("availableSpaces")));

            if (ticketType.getBoolean("seated"))
                data.add(new Uint256(BigInteger.valueOf(1)));
            else
                data.add(new Uint256(BigInteger.valueOf(0)));

            data.add(new Uint256(ticketType.getLong("saleStartTime")));
            data.add(new Uint256(ticketType.getLong("saleEndTime")));
            data.add(new Uint256(ticketType.getBigInteger("price")));
            data.add(new Uint256(ticketType.getBigInteger("secondaryMarketPriceCap")));
        }

        List<Address> primaryShareHoldersList = new ArrayList<>();
        for (Object primaryShareHolder : object.getJSONArray("primaryShareHolders")) {
            primaryShareHoldersList.add(new Address(primaryShareHolder.toString()));
        }

        List<Uint256> primarySharesList = new ArrayList<>();
        for (Object primaryShare : object.getJSONArray("primaryShares")) {
            primarySharesList.add(new Uint256((int)primaryShare));
        }

        List<Address> secondaryShareHoldersList = new ArrayList<>();
        for (Object secondaryShareHolder : object.getJSONArray("secondaryShareHolders")) {
            secondaryShareHoldersList.add(new Address(secondaryShareHolder.toString()));
        }

        List<Uint256> secondarySharesList = new ArrayList<>();
        for (Object secondaryShare : object.getJSONArray("secondaryShares")) {
            secondarySharesList.add(new Uint256((int)secondaryShare));
        }

        return OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.eventManagementContractAddress, Settings.getGasPrice(), BigInteger.valueOf(0), "createTicketSale", new Address(object.getString("taxAccount")), new DynamicArray(stringData), new DynamicArray(data), new DynamicArray(primaryShareHoldersList), new DynamicArray(primarySharesList), new DynamicArray(secondaryShareHoldersList), new DynamicArray(secondarySharesList));
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
