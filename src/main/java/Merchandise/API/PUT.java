package Merchandise.API;

import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Merchandise.NftCollection.MerchandiseCollection;
import Merchandise.Webhook.Sender;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import org.json.JSONObject;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;

import static spark.Spark.put;

public class PUT {

    public static void run() {
        put("/Merchandise/setMaxSupply", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String merchandiseCollectionAddress = request.queryParams("merchandiseCollectionAddress").toLowerCase();
                BigInteger maxSupply = new BigInteger(request.queryParams("maxSupply"));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                Collection c = Collections.getCollectionByAddress(merchandiseCollectionAddress);
                if(c == null || !(c instanceof MerchandiseCollection))
                    return getFail("eventAddress", "Invalid Collection");

                //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, merchandiseCollectionAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setMaxSupply", new Uint256(maxSupply));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/Merchandise/setPrice", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String merchandiseCollectionAddress = request.queryParams("merchandiseCollectionAddress").toLowerCase();
                BigInteger price = new BigInteger(request.queryParams("price"));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                Collection c = Collections.getCollectionByAddress(merchandiseCollectionAddress);
                if(c == null || !(c instanceof MerchandiseCollection))
                    return getFail("eventAddress", "Invalid Collection");

                //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, merchandiseCollectionAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setPrice", new Uint256(price));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/Merchandise/setSaleStartTime", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String merchandiseCollectionAddress = request.queryParams("merchandiseCollectionAddress").toLowerCase();
                BigInteger saleStartTime = new BigInteger(request.queryParams("saleStartTime"));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                Collection c = Collections.getCollectionByAddress(merchandiseCollectionAddress);
                if(c == null || !(c instanceof MerchandiseCollection))
                    return getFail("eventAddress", "Invalid Collection");

                //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, merchandiseCollectionAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSaleStartTime", new Uint256(saleStartTime));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

        put("/Merchandise/setSaleEndTime", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");
                String merchandiseCollectionAddress = request.queryParams("merchandiseCollectionAddress").toLowerCase();
                BigInteger saleEndTime = new BigInteger(request.queryParams("saleEndTime"));
                String endpointUrl = request.queryParams("endpointUrl");
                String verificationToken = request.queryParams("verificationToken");
                //TODO verify verification token

                Collection c = Collections.getCollectionByAddress(merchandiseCollectionAddress);
                if(c == null || !(c instanceof MerchandiseCollection))
                    return getFail("eventAddress", "Invalid Collection");

                //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, merchandiseCollectionAddress, Settings.getGasPrice(), BigInteger.valueOf(50000), BigInteger.ZERO, "setSaleEndTime", new Uint256(saleEndTime));

                if (r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("transactionHash", r.message);
                }
                else return getFail("error", r.message);
            } catch (Exception e) {
                return getError(e.getMessage());
            }
        });

    }

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
