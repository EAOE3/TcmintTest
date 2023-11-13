package Merchandise.Webhook;

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

    public static void notifyOfMerchandiseContractCreation(String contractAddress,String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("transactionHash", txnHash);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/merchandiseContractCreation";

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
    public static void notifyOfSuccessfulPurchase(String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("success", true);
            body.put("transactionHash", txnHash);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/merchandiseSuccessfulPurchase";

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
    public static void notifyOfMaxSupplyChange(String contractAddress, int maxSupply, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("maxSupply", maxSupply);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/updateMaxSupply";

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
    public static void notifyOfPriceChange(String contractAddress, BigInteger price, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("price", price);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/updatePrice";

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
    public static void notifyOfSaleStartTimeChange(String contractAddress, BigInteger startTime, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("startTime", startTime);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/updatesaleStartTime";

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
    public static void notifyOfSaleEndTimeChange(String contractAddress, BigInteger endTime, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("endTime", endTime);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/updatesaleEndTime";

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
    public static void notifyOfSecondaryMarketPriceCapChange(String contractAddress, BigInteger secondaryMarketPriceCap, String txnHash) {
        try {
            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("secondaryMarketPriceCap", secondaryMarketPriceCap);

            String url = endpointUrlByTxnHash.get(txnHash) + "/Merchandise/Webhook/updateSecondaryMarketPriceCap";

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
