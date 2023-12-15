package TicmintToken.Webhook;

import Main.Settings;
import org.json.JSONObject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class Sender {


    private static Map<String /*Txn Hash*/, String /*Endpoint URL*/> endpointUrlByTxnHash = new HashMap<>();

    public static void addTxnHashAndEndpointUrl(String txnHash, String endpointUrl) {
        endpointUrlByTxnHash.put(txnHash, endpointUrl);
        checkTxnAndNotifyServerIfFailed(endpointUrl, txnHash);
    }

    public static void checkTxnAndNotifyServerIfFailed(String endpoint, String txnHash) {
        new Thread() {
            public void run() {
                try{Thread.sleep(Settings.blockTime);} catch (Exception e) {e.printStackTrace();}
                try {
                    TransactionReceipt txnReceipt = null;

                    while (txnReceipt == null) {
                        txnReceipt = Settings.web3j.ethGetTransactionReceipt(txnHash).send().getTransactionReceipt()
                                .orElse(null);
                        System.out.println("Txn receipt is still null, sleeping");
                        Thread.sleep(Settings.blockTime);
                    }

                    // Failed Txn
                    if (txnReceipt.getStatus().equalsIgnoreCase("0x0")) {
                        JSONObject body = new JSONObject();
                        System.out.println("failed");

                        body.put("success", false);
                        body.put("transactionHash", txnHash);

                        String url = endpoint + "/txnFailNotification";

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

    public static void notifyOfDeposit(String txnHash, String userAddress, BigInteger amount, String data) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("transactionHash", txnHash);
            body.put("userAddress", userAddress);
            body.put("amount", amount);
            body.put("data", data);

            String url = endpointUrlByTxnHash.get(txnHash) + "/TicmintToken/notifyOfDeposit";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        endpointUrlByTxnHash.remove(txnHash);
    }
    public static void notifyOfLockInPreparationForWithdrawal(String txnHash, String userAddress, BigInteger amount, String data) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("transactionHash", txnHash);
            body.put("userAddress", userAddress);
            body.put("amount", amount);
            body.put("data", data);

            String url = endpointUrlByTxnHash.get(txnHash) + "/TicmintToken/notifyOfLockInPreparationForWithdrawal";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        endpointUrlByTxnHash.remove(txnHash);
    }
    public static void notifyOfUnlockFromWithdrawal(String txnHash, String userAddress, BigInteger amount, String data) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("transactionHash", txnHash);
            body.put("userAddress", userAddress);
            body.put("amount", amount);
            body.put("data", data);

            String url = endpointUrlByTxnHash.get(txnHash) + "/TicmintToken/notifyOfUnlockFromWithdrawal";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        endpointUrlByTxnHash.remove(txnHash);
    }
    public static void notifyOfWithdrawal(String txnHash, String userAddress, BigInteger amount, String data) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("transactionHash", txnHash);
            body.put("userAddress", userAddress);
            body.put("amount", amount);
            body.put("data", data);

            String url = endpointUrlByTxnHash.get(txnHash) + "/TicmintToken/notifyOfWithdrawal";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        endpointUrlByTxnHash.remove(txnHash);
    }

}
