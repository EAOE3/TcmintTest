package AddOnsAndAdditionalServices.Webhook;

import Main.Settings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
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

    //BuyAdditionalServices
    public static void notifyOfAdditionalServicesPurchase(String txnHash, String eventCollectionAddress, String buyerAddress, List<String> service, List<BigInteger> amount) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();
            JSONArray services = new JSONArray();

            body.put("success", true);
            body.put("transactionHash", txnHash);
            body.put("eventCollectionAddress", eventCollectionAddress);
            body.put("buyerAddress", buyerAddress);

            for(int t=0; t < service.size(); t++) {
                JSONObject serviceJson = new JSONObject();
                serviceJson.put("name", service.get(t));
                serviceJson.put("amount", amount.get(t));
                services.put(serviceJson);
            }

            body.put("services", services);

            String url = endpointUrlByTxnHash.get(txnHash) + "/additionalServicesPurchase";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //BuyAddOn
    public static void notifyOfAddOnPurchase(String txnHash, String eventCollectionAddress, String buyerAddress, List<String> service, List<BigInteger> amount) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if (endpoint == null) return;

            JSONObject body = new JSONObject();
            JSONArray addons = new JSONArray();

            body.put("success", true);
            body.put("transactionHash", txnHash);
            body.put("eventCollectionAddress", eventCollectionAddress);
            body.put("buyerAddress", buyerAddress);

            for (int t = 0; t < service.size(); t++) {
                JSONObject addonJson = new JSONObject();
                addonJson.put("name", service.get(t));
                addonJson.put("amount", amount.get(t));
                addons.put(addonJson);
            }

            body.put("addons", addons);

            String url = endpointUrlByTxnHash.get(txnHash) + "/addonsServicesPurchase";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
