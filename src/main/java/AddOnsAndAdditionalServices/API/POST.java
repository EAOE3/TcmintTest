package AddOnsAndAdditionalServices.API;

import AddOnsAndAdditionalServices.Webhook.Sender;
import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Wallet.Wallet.Wallet;
import Wallet.Wallet.Wallets;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.post;

public class POST {

    public static void run() {
        post("/buyAddOnViaMotherWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");

                JSONObject object = new JSONObject(request.body());

                String usersAddress = object.getString("usersAddress").toLowerCase();
                String collectionAddress = object.getString("eventCollectionAddress").toLowerCase();
                BigInteger totalCost = BigInteger.valueOf(object.getInt("totalCost"));
                List<Utf8String> nameAndId = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();

                for (Object o : object.getJSONArray("addOns")) {
                    JSONObject addOn = (JSONObject) o;
                    nameAndId.add(new Utf8String(addOn.getString("name")));
                    amount.add(new Uint256(BigInteger.valueOf(addOn.getInt("amount"))));
                }

                Response res = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.addOnsAndAdditonalServicesContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "buyAddOn", new Address(usersAddress), new Address(collectionAddress), new DynamicArray(nameAndId), new DynamicArray(amount), new Uint256(totalCost));

                if (res.success) {
                    Sender.addTxnHashAndEndpointUrl(res.message, object.getString("endpointUrl"));
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

        post("/buyAddOnViaUserWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");

                JSONObject object = new JSONObject(request.body());

                String usersAddress = object.getString("usersAddress").toLowerCase();
                String collectionAddress = object.getString("eventCollectionAddress").toLowerCase();
                BigInteger totalCost = BigInteger.valueOf(object.getInt("totalCost"));
                List<Utf8String> nameAndId = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if(w == null) return getFail("message", "Wallet not found");

                for (Object o : object.getJSONArray("addOns")) {
                    JSONObject addOn = (JSONObject) o;
                    nameAndId.add(new Utf8String(addOn.getString("name")));
                    amount.add(new Uint256(BigInteger.valueOf(addOn.getInt("amount"))));
                }

                Response res = OWeb3j.sendScTxn(Settings.web3j, w.getCredentials(), Settings.chainId, Settings.addOnsAndAdditonalServicesContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "buyAddOn", new Address(usersAddress), new Address(collectionAddress), new DynamicArray(nameAndId), new DynamicArray(amount), new Uint256(totalCost));

                if (res.success) {
                    Sender.addTxnHashAndEndpointUrl(res.message, object.getString("endpointUrl"));
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

        post("/buyAdditionalServicesViaMotherWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");

                JSONObject object = new JSONObject(request.body());

                String usersAddress = object.getString("usersAddress").toLowerCase();
                String collectionAddress = object.getString("eventCollectionAddress").toLowerCase();
                BigInteger totalCost = BigInteger.valueOf(object.getInt("totalCost"));
                List<Utf8String> nameAndId = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();

                for (Object o : object.getJSONArray("additionalServices")) {
                    JSONObject addOn = (JSONObject) o;
                    nameAndId.add(new Utf8String(addOn.getString("name")));
                    amount.add(new Uint256(BigInteger.valueOf(addOn.getInt("amount"))));
                }

                Response res = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.addOnsAndAdditonalServicesContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "buyAdditionalServices", new Address(usersAddress), new Address(collectionAddress), new DynamicArray(nameAndId), new DynamicArray(amount), new Uint256(totalCost));

                if (res.success) {
                    Sender.addTxnHashAndEndpointUrl(res.message, object.getString("endpointUrl"));
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

        post("/buyAdditionalServicesViaUserWallet/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");

                JSONObject object = new JSONObject(request.body());

                String usersAddress = object.getString("usersAddress").toLowerCase();
                String collectionAddress = object.getString("eventCollectionAddress").toLowerCase();
                BigInteger totalCost = BigInteger.valueOf(object.getInt("totalCost"));
                List<Utf8String> nameAndId = new LinkedList<>();
                List<Uint256> amount = new LinkedList<>();

                Wallet w = Wallets.getWalletByAddress(usersAddress);
                if(w == null) return getFail("message", "Wallet not found");

                for (Object o : object.getJSONArray("additionalServices")) {
                    JSONObject addOn = (JSONObject) o;
                    nameAndId.add(new Utf8String(addOn.getString("name")));
                    amount.add(new Uint256(BigInteger.valueOf(addOn.getInt("amount"))));
                }

                Response res = OWeb3j.sendScTxn(Settings.web3j, w.getCredentials(), Settings.chainId, Settings.addOnsAndAdditonalServicesContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "buyAdditionalServices", new Address(usersAddress), new Address(collectionAddress), new DynamicArray(nameAndId), new DynamicArray(amount), new Uint256(totalCost));

                if (res.success) {
                    Sender.addTxnHashAndEndpointUrl(res.message, object.getString("endpointUrl"));
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
