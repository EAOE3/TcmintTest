package TicmintToken.API;

import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Main.Signature;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.TicketType.TicketType;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import TicmintToken.Token.TMT;
import TicmintToken.Webhook.Sender;
import Wallet.Wallet.Wallet;
import Wallet.Wallet.Wallets;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.time.Instant;

import static spark.Spark.post;

public class POST {

    public static void run() {

        post("/deposit/", (request, response) -> {
            try {
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                BigInteger amount = new BigInteger(request.queryParams("amount"));
                String data = request.queryParams("data");
                String endpointUrl = request.queryParams("endpointUrl");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice().multiply(BigInteger.TWO), BigInteger.ZERO,
                        "deposit", new Address(usersAddress), new Uint256(amount), new Utf8String(data));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/lockInPreparationForWithdrawal/", (request, response) -> {
            try {
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                BigInteger amount = new BigInteger(request.queryParams("amount"));
                String data = request.queryParams("data");
                String endpointUrl = request.queryParams("endpointUrl");

                if(TMT.getUserBalance(usersAddress).compareTo(amount) < 0) return getFail("message", "Not enough balance");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.ZERO,
                        "lockInPreparationForWithdrawal", new Address(usersAddress), new Uint256(amount), new Utf8String(data));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/unlockFromWithdrawal/", (request, response) -> {
            try {
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                BigInteger amount = new BigInteger(request.queryParams("amount"));
                String data = request.queryParams("data");
                String endpointUrl = request.queryParams("endpointUrl");

                if(TMT.getUserLockedBalance(usersAddress).compareTo(amount) < 0) return getFail("message", "Not enough locked balance");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.ZERO,
                        "unlockFromWithdrawal", new Address(usersAddress), new Uint256(amount), new Utf8String(data));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/withdraw/", (request, response) -> {
            try {
                String usersAddress = request.queryParams("usersAddress").toLowerCase();
                BigInteger amount = new BigInteger(request.queryParams("amount"));
                String data = request.queryParams("data");
                String endpointUrl = request.queryParams("endpointUrl");

                if(TMT.getUserLockedBalance(usersAddress).compareTo(amount) < 0) return getFail("message", "Not enough locked balance");

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.ZERO,
                        "withdraw", new Address(usersAddress), new Uint256(amount), new Utf8String(data));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
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
