package TicketingEvent.Webhook;

import Main.Settings;
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
                        //System.out.println("Txn receipt is still null, sleeping");
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

    public static void notifyOfEventCreationSuccess(String txnHash, String contractAddress) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("success", true);
            body.put("contractAddress", contractAddress);
            body.put("transactionHash", txnHash);

            String url = endpointUrlByTxnHash.get(txnHash) + "/collectionSuccessfullCreation";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
              client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void notifyOfSuccessfulPurchase(String txnHash) {
//        try {
//            String endpoint = endpointUrlByTxnHash.get(txnHash);
//            if(endpoint == null) return;
//
//            JSONObject body = new JSONObject();
//
//            body.put("success", true);
//            body.put("transactionHash", txnHash);
//
//            String url = endpointUrlByTxnHash.get(txnHash) + "/ticketSuccessfulPurchase";
//
//            HttpClient client = HttpClient.newHttpClient();
//
//            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
//                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
//                    .build();
//              client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        endpointUrlByTxnHash.remove(txnHash);
//    }

    public static void notifyOfTicketCreation(String txnHash, String contractAddress, List<Integer> nftId, List<String> ticketType) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) {
                System.out.println("Endpoint for txn hash " + txnHash + " of type ticket creation is null");
                return;
            }

            JSONObject body = new JSONObject();

            body.put("success", true);
            body.put("transactionHash", txnHash);
            body.put("contractAddress", contractAddress);
            body.put("nftId", nftId);
            body.put("ticketType", ticketType);

            String url = endpointUrlByTxnHash.get(txnHash) + "/ticketCreation";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Ticket creation notification sent");
        } catch (Exception e) {
            e.printStackTrace();
        }

        endpointUrlByTxnHash.remove(txnHash);
    }

    public static void notifyOfAvailableSpacesUpdate(String contractAddress, String ticketType, BigInteger availableSpaces, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("availableSpaces", availableSpaces);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateAvailableSpaces";

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

    public static void notifyOfSeatedStateUpdate(String contractAddress, String ticketType, boolean seated, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("seated", seated);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateSeatedState";

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

    public static void notifyOfSaleStartTimeUpdate(String contractAddress, String ticketType, BigInteger saleStartTime, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("saleStartTime", saleStartTime);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateSaleStartTime";

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

    public static void notifyOfSaleEndTimeUpdate(String contractAddress, String ticketType, BigInteger saleEndTime, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("saleEndTime", saleEndTime);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateSaleEndTime";

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

    public static void notifyOfPriceUpdate(String contractAddress, String ticketType, BigInteger price, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("price", price);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updatePrice";

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

    public static void notifyOfSecondaryMarketPriceCapUpdate(String contractAddress, String ticketType, BigInteger price, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("ticketType", ticketType);
            body.put("price", price);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateSecondaryMarketPriceCap";

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

    public static void notifyOfEventEndTimeUpdate(String contractAddress, long time, String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("contractAddress", contractAddress);
            body.put("time", time);

            String url = endpointUrlByTxnHash.get(txnHash) + "/updateEventEndTime";

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

    public static void notifyOfRefundCompletion(String txnHash) {
        try {
            String endpoint = endpointUrlByTxnHash.get(txnHash);
            if(endpoint == null) return;

            JSONObject body = new JSONObject();

            body.put("transactionHash", txnHash);
            body.put("refundComplete", true);

            String url = endpointUrlByTxnHash.get(txnHash) + "/completeRefund";

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
