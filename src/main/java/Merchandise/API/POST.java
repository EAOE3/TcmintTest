package Merchandise.API;

import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Merchandise.NftCollection.MerchandiseCollection;
import Merchandise.Webhook.Sender;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import TicmintToken.Token.TMT;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.post;

public class POST {

    public static void run() {
        // Define your API endpoints
        post("/Merchandise/createNewMerchandise", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                JSONObject body = new JSONObject(request.body());

//                uint256 maxSupply, uint256 price, uint256 saleStartTime, uint256 saleEndTime, uint256 secondaryMarketPriceCap,
//                        address[] memory primaryShareHolders, address[] memory secondaryShareHolders, uint256[] memory primaryShares, uint256[] memory secondaryShares

                String serverEndpoint = body.getString("serverEndpoint");
                Uint256 maxSupply = new Uint256(body.getBigInteger("maxSupply"));
                Uint256 price = new Uint256(body.getBigInteger("price"));
                Uint256 saleStartTime = new Uint256(body.getBigInteger("saleStartTime"));
                Uint256 saleEndTime = new Uint256(body.getBigInteger("saleEndTime"));
                Uint256 royalties = new Uint256(body.getBigInteger("royalties"));

                List<Address> primaryShareHolders = new LinkedList<>();
                List<Address> secondaryShareHolders = new LinkedList<>();
                List<Uint256> primaryShares = new LinkedList<>();
                List<Uint256> secondaryShares = new LinkedList<>();

                for(Object o: body.getJSONArray("primaryShareHolders")) {
                    primaryShareHolders.add(new Address(o.toString()));
                }

                for(Object o: body.getJSONArray("secondaryShareHolders")) {
                    secondaryShareHolders.add(new Address(o.toString()));
                }

                for(Object o: body.getJSONArray("primaryShares")) {
                    primaryShares.add(new Uint256(BigInteger.valueOf((Integer) o)));
                }

                for(Object o: body.getJSONArray("secondaryShares")) {
                    secondaryShares.add(new Uint256(BigInteger.valueOf((Integer) o)));
                }

//                function createMerchandise(
//                        uint256 maxSupply,
//                        uint256 price,
//                        uint256 saleStartTime,
//                        uint256 saleEndTime,
//                        address[] memory primaryShareHolders,
//                address[] memory secondaryShareHolders,
//                        uint256[] memory primaryShares,
//                uint256[] memory secondaryShares) external Manager
//                {

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.merchandiseManagementContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "createMerchandise", maxSupply, price, saleStartTime, saleEndTime, royalties, new org.web3j.abi.datatypes.DynamicArray(primaryShareHolders), new org.web3j.abi.datatypes.DynamicArray(secondaryShareHolders), new org.web3j.abi.datatypes.DynamicArray(primaryShares), new org.web3j.abi.datatypes.DynamicArray(secondaryShares));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, serverEndpoint);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        post("/Merchandise/buyViaMotherWallet", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String merchandiseAddress = request.queryParams("merchandiseAddress").toLowerCase();
                String to = request.queryParams("toAddress").toLowerCase();
                int amount = Integer.parseInt(request.queryParams("amount"));
                String serverEndpoint = request.queryParams("serverEndpoint");

                Collection c = Collections.getCollectionByAddress(merchandiseAddress);
                if(c == null || !(c instanceof MerchandiseCollection)) {
                    return getFail("message", "Merchandise address not found");
                }
                MerchandiseCollection m = (MerchandiseCollection) c;
                long now = Instant.now().getEpochSecond();

                if(now < m.getSaleStartTime() || now > m.getSaleEndTime()) {
                    return getFail("message", "Sale is not active");
                }

                if(m.getMaxSupply() - amount < 0) {
                    return getFail("message", "Not enough supply");
                }

                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, merchandiseAddress, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(to), new Uint256(amount));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, serverEndpoint);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        post("/Merchandise/buyViaUserWallet", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String merchandiseAddress = request.queryParams("merchandiseAddress").toLowerCase();
                String to = request.queryParams("toAddress").toLowerCase();
                int amount = Integer.parseInt(request.queryParams("amount"));
                String serverEndpoint = request.queryParams("serverEndpoint");

                Wallet w = Wallets.getWalletByAddress(to);
                if(w == null) {
                    return getFail("message", "Wallet not found");
                }

                Collection c = Collections.getCollectionByAddress(merchandiseAddress);
                if(c == null || !(c instanceof MerchandiseCollection)) {
                    return getFail("message", "Merchandise address not found");
                }
                MerchandiseCollection m = (MerchandiseCollection) c;
                long now = Instant.now().getEpochSecond();

                if(now < m.getSaleStartTime() || now > m.getSaleEndTime()) {
                    return getFail("message", "Sale is not active");
                }

                if(m.getMaxSupply() - amount < 0) {
                    return getFail("message", "Not enough supply");
                }

                if(Settings.tmt.getUserBalance(to).compareTo(m.getPrice().multiply(new BigInteger(String.valueOf(amount)))) < 0) {
                    return getFail("message", "Not enough TMT");
                }

                Response r = OWeb3j.fuelAndSendScTxn(Settings.web3j, w.getCredentials(), Settings.chainId, to, merchandiseAddress, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(to), new Uint256(amount));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, serverEndpoint);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                return getError(e.getMessage());
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
