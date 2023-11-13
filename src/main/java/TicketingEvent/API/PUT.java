package TicketingEvent.API;

import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.TicketType.TicketTypes;

import static spark.Spark.put;

public class PUT {

    public static void run() {

        put("/setAvailableSpaces", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Uint256 spaces = new Uint256(Integer.parseInt(request.queryParams("spaces")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setAvailableSpaces", ticketType, spaces);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setSeated", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Bool isSeated = new Bool(Boolean.parseBoolean(request.queryParams("isSeated")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSeated", ticketType, isSeated);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setSaleStartTime", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Uint256 saleStartTime = new Uint256(Long.parseLong(request.queryParams("saleStartTime")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSaleStartTime", ticketType, saleStartTime);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setSaleEndTime", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Uint256 saleEndTime = new Uint256(Long.parseLong(request.queryParams("saleEndTime")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSaleEndTime", ticketType, saleEndTime);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setPrice", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Uint256 newPrice = new Uint256(new BigInteger(request.queryParams("newPrice")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setPrice", ticketType, newPrice);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setSecondaryMarketPriceCap", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Utf8String ticketType = new Utf8String(request.queryParams("ticketType"));
                Uint256 cap = new Uint256(new BigInteger(request.queryParams("cap")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSecondaryMarketPriceCap", ticketType, cap);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/setEventEndTime", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                Uint256 endTime = new Uint256(new BigInteger(request.queryParams("endTime")));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if (Collections.getCollectionByAddress(eventAddress) == null)
                    return getFail("eventAddress", "Invalid Event");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setEventEndTime", endTime);

                if (r.success) {
                    TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        //TODO testing
        put("/refund", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String eventAddress = request.queryParams("eventAddress").toLowerCase();
                String ticketType = request.queryParams("ticketType");
                boolean refundUsedTickets = Boolean.parseBoolean(request.queryParams("refundUsedTickets"));
                System.out.println("Refund fee: " + request.queryParams("refundFee"));
                BigInteger refundFee = new BigDecimal(request.queryParams("refundFee")).multiply(BigDecimal.valueOf(100)).toBigInteger();
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                if(TicketTypes.getTicketTypeByDatabaseId(eventAddress + "-" + ticketType) == null)
                    return getFail("ticketType", "Invalid Ticket Type");

                Collection c = Collections.getCollectionByAddress(eventAddress);
                if(c == null || !(c instanceof EventNFTCollection)) return getFail("Event not found");
                EventNFTCollection event = (EventNFTCollection) c;

                List<Ticket> ticketsToRefund = new LinkedList<>();
                for(Ticket t: event.getAllTickets()) {
                    if(t.ticketType.equals(ticketType) && !t.isRefunded()) {
                        if(!refundUsedTickets && t.isUsed()) continue;
                        ticketsToRefund.add(t);
                    }
                }

                if(ticketsToRefund.size() == 0) return getSuccess("message", "No tickets to refund");

                List<Address> addresses = new ArrayList<>(ticketsToRefund.size());
                List<Uint256> amount = new ArrayList<>(ticketsToRefund.size());
                BigInteger totalFee = BigInteger.valueOf(0);

                for(Ticket t: ticketsToRefund) {
                    BigInteger purchasePrice = t.getPurchasePrice();
                    BigInteger fee = purchasePrice.multiply(refundFee).divide(BigInteger.valueOf(10000));
                    BigInteger userRefundAmount = purchasePrice.subtract(fee);

                    addresses.add(new Address(t.getHexOwner()));
                    amount.add(new Uint256(userRefundAmount));
                    totalFee = totalFee.add(fee);
                }
                addresses.add(new Address(Settings.ticmintFeeAccount));
                amount.add(new Uint256(totalFee));

                for(Ticket t: ticketsToRefund) {
                    addresses.add(new Address(t.getHexOwner()));
                    amount.add(new Uint256(t.getPurchasePrice()));
                }

                List<String> transactionHashes = new LinkedList<>();
                List<String> errors = new LinkedList<>();

                refund(eventAddress, addresses, amount, ticketsToRefund, transactionHashes, errors);

                if(transactionHashes.size() == 0 && errors.size() > 0) {
                    return getFail("message", "All transactions failed", "errors", errors);
                }
                else if(transactionHashes.size() > 0 && errors.size() > 0) {
                    for(String txnHash: transactionHashes) {
                        TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(txnHash, endpointUrl);
                    }
                    return getFail("message", "Some transactions failed", "errors", errors, "transactionHashes", transactionHashes);
                }
                else if(transactionHashes.size() > 0 && errors.size() == 0) {
                    for(String txnHash: transactionHashes) {
                        TicketingEvent.Webhook.Sender.addTxnHashAndEndpointUrl(txnHash, endpointUrl);
                    }
                    return getSuccess("message", "All transactions succeeded", "transactionHashes", transactionHashes);
                }
                else {
                    return getFail("message", "No transactions succeeded");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getMessage());
            }
        });

    }

    public static void refund(String eventAddress, List<Address> addresses, List<Uint256> amount, List<Ticket> ticketsToRefund, List<String> transactionHashes, List<String> errors) {
        Response r = OWeb3j.sendScTxnCaptureFail(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, eventAddress, Settings.getGasPrice(), BigInteger.ZERO, "refund", new DynamicArray(addresses), new DynamicArray(amount));

        /*If it's failing to distribute to only 100 addresses then it's not a gas issues*/
        if(!r.success && r.message.equalsIgnoreCase("Gas limit exceeded") && addresses.size() > 100) {
            addresses.remove(addresses.size() - 1);
            amount.remove(amount.size() - 1);

            int addressMiddleIndex = addresses.size() / 2;

            List<Address> addressFirstHalf = new ArrayList<>(addresses.subList(0, addressMiddleIndex));
            List<Address> addressSecondHalf = new ArrayList<>(addresses.subList(addressMiddleIndex, addresses.size()));

            List<Uint256> amountFirstHalf = new ArrayList<>(amount.subList(0, addressMiddleIndex));
            List<Uint256> amountSecondHalf = new ArrayList<>(amount.subList(addressMiddleIndex, amount.size()));

            List<Ticket> ticketsFirstHalf = new ArrayList<>(ticketsToRefund.subList(0, addressMiddleIndex));
            List<Ticket> ticketsSecondHalf = new ArrayList<>(ticketsToRefund.subList(addressMiddleIndex, ticketsToRefund.size()));

            //Recalculate fee
            BigInteger totalFee1 = BigInteger.valueOf(0);
            for(int t=0; t < addressFirstHalf.size(); ++t) {
                totalFee1 = totalFee1.add(ticketsFirstHalf.get(t).getPurchasePrice().subtract(amountFirstHalf.get(t).getValue()));
            }
            addressFirstHalf.add(new Address(Settings.ticmintFeeAccount));
            amountFirstHalf.add(new Uint256(totalFee1));


            BigInteger totalFee2 = BigInteger.valueOf(0);
            for(int t=0; t < addressSecondHalf.size(); ++t) {
                totalFee2 = totalFee2.add(ticketsSecondHalf.get(t).getPurchasePrice().subtract(amountSecondHalf.get(t).getValue()));
            }
            addressSecondHalf.add(new Address(Settings.ticmintFeeAccount));
            amountSecondHalf.add(new Uint256(totalFee2));

            if(addressFirstHalf.size() > 0) refund(eventAddress, addressFirstHalf, amountFirstHalf, ticketsFirstHalf, transactionHashes, errors);
            if(addressSecondHalf.size() > 0) refund(eventAddress, addressSecondHalf, amountSecondHalf, ticketsSecondHalf, transactionHashes, errors);
        }
        else if(r.success) {
            for(Ticket t: ticketsToRefund) {
                t.setRefunded(true);
            }

            transactionHashes.add(r.message);
        }
        else {
            errors.add(r.message);
        }
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
