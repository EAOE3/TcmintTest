package TicketSaleCreation.API;

import static spark.Spark.post;

import Collection.Ticket;
import Collection.Tickets;
import Main.Response;
import Main.Settings;
import TicketSaleCreation.Management.ManagementContract;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Verification.SmartContract.SignatureVerification;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class POST {

    public static final ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger("03a5240936d67dc18dca348e793010a14c5eba86a73d0c9e45764681295a73df", 16));
    public static final String address = "0x61Bd8fc1e30526Aaf1C4706Ada595d6d236d9883";


    public static void run() {

        post("/createEvent/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                System.out.println(request.body());
                JSONObject object = new JSONObject(request.body());
                String veririficationToken = request.queryParams("veririficationToken");
                //TODO verify verification token

                Response res = createNewEvent(object);

                if (res.success) {
                    checkTxnAndNotifyServerIfFailed(object.getString("endpointUrl"), res.message);
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

    }

    private static Response functionExecute(String encodedFunction, BigInteger GAS_LIMIT) {
        try {

            Credentials creds = Credentials.create(ecKeyPair);
            RawTransactionManager manager = new RawTransactionManager(Settings.web3j, creds, Settings.chainId);
            EthSendTransaction transaction = null;
            transaction = manager.sendTransaction(Settings.getGasPrice(), GAS_LIMIT, Settings.managementContractAddress, encodedFunction,
                    BigInteger.valueOf(0));

            String txnHash = transaction.getTransactionHash();
            System.out.println("Txn Hash: " + txnHash);

            if (txnHash == null) {
                return Response.failure(transaction.getError().getMessage());
            }

            return Response.success(txnHash);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure("Unexpected Error");
        }

    }

    public static Response createNewEvent(JSONObject object) throws Exception {

        List<Utf8String> stringData = new LinkedList<>();
        stringData.add(new Utf8String("empty"));
        stringData.add(new Utf8String(object.getString("eventId")));
        stringData.add(new Utf8String(object.getString("name")));
        stringData.add(new Utf8String(object.getString("symbol")));
        stringData.add(new Utf8String(object.getString("baseUrl")));

        List<Uint256> data = new LinkedList<>();
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

        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new DynamicArray(stringData));
        inputParameters.add(new DynamicArray(data));
        inputParameters.add(new DynamicArray(primaryShareHoldersList));
        inputParameters.add(new DynamicArray(primarySharesList));
        inputParameters.add(new DynamicArray(secondaryShareHoldersList));
        inputParameters.add(new DynamicArray(secondarySharesList));
        inputParameters.add(new Uint256(object.getInt("royalties")));
        inputParameters.add(new Uint256(object.getLong("eventEndTime")));

        Function function = new Function("createTicketSale", // function we're calling
                inputParameters, // Parameters to pass as Solidity Types
                Arrays.asList());
        String encodedFunction = FunctionEncoder.encode(function);

        // Create a transaction object
        Transaction transaction = Transaction.createFunctionCallTransaction(address, BigInteger.valueOf(100), Settings.getGasPrice(),
                BigInteger.valueOf(15_000_000), Settings.managementContractAddress, encodedFunction);

        //BigInteger estimatedGas = Settings.web3j.ethEstimateGas(transaction).send().getAmountUsed();

        return functionExecute(encodedFunction, BigInteger.valueOf(10_000_000));
    }

    public static void checkTxnAndNotifyServerIfFailed(String endpoint, String txnHash) {
        ManagementContract.serverEnpoint = endpoint;
        new Thread() {
            public void run() {
                try {
                    TransactionReceipt txnReceipt = null;

                    while (txnReceipt == null) {
                        txnReceipt = Settings.web3j.ethGetTransactionReceipt(txnHash).send().getTransactionReceipt()
                                .orElse(null);
                        System.out.println("Txn receipt is still null, sleeping");
                        Thread.sleep(2000); // pause for 2 seconds before checking again
                    }

                    // Failed Txn
                    if (txnReceipt.getStatus().equalsIgnoreCase("0x0")) {
                        JSONObject body = new JSONObject();
                        JSONObject event = new JSONObject();
                        System.out.println("failed");

                        event.put("success", false);
                        event.put("transactionHash", txnHash);

                        body.put("event", event);

                        String url = endpoint + "/web3EventUpdate";

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                                .build();
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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


