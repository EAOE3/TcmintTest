package BuyingVerification.API;

import Main.Settings;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.RawTransactionManager;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.post;

public class POST {

    //Manager key
    private static final ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger("03a5240936d67dc18dca348e793010a14c5eba86a73d0c9e45764681295a73df", 16));

    //TODO Testing
    public static void run() {

        post("/signBuyOrder/", (request, response) ->
        {
            try {
                response.header("Content-Type", "application/json");
                JSONObject data = new JSONObject(request.body());

                String buyer = data.getString("buyer"); //Buyer
                String eventNFTCollectionAddress = data.getString("eventNFTCollectionAddress"); //NFT collection address
                String verificationToken = data.getString("verificationToken"); //Verification token
                JSONArray ticketTypeArray = data.getJSONArray("ticketType");
                JSONArray amountArray = data.getJSONArray("amount");
                JSONArray seatNumbersArray = data.getJSONArray("seatNumbers");

                // Convert JSONArray to List
                List<Utf8String> ticketType = new ArrayList<>();
                for (int i = 0; i < ticketTypeArray.length(); i++) {
                    ticketType.add(new Utf8String(ticketTypeArray.getString(i)));
                }

                List<Uint256> amount = new ArrayList<>();
                for (int i = 0; i < amountArray.length(); i++) {
                    amount.add(new Uint256(amountArray.getInt(i)));
                }

                List<Utf8String> seatNumber = new ArrayList<>();
                for (int i = 0; i < seatNumbersArray.length(); i++) {
                    seatNumber.add(new Utf8String(seatNumbersArray.getString(i)));
                }

                //TODO verify verification token

                byte[] hash;
                try {
                    hash = getBuyRequestHash(buyer, eventNFTCollectionAddress, ticketType, amount, seatNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                    return getError(e.getLocalizedMessage());
                }

                String signature = Hex.toHexString(signMessage(hash, ecKeyPair));

                return getSuccess("serverSignature", signature);
            } catch (Exception e) {
                e.printStackTrace();
                return getFail("message", e.getLocalizedMessage());
            }
        });

    }

    //address to, address EventNFTCollectionAddress, string[] calldata ticketType, uint256[] calldata amount, string[][] memory seatNumbers
    public static byte[] getBuyRequestHash(String buyer, String collectionAddress, List<Utf8String> ticketType, List<Uint256> amount, List<Utf8String> seatNumbers) throws Exception {
        RawTransactionManager manager = new RawTransactionManager(Settings.web3j, Credentials.create(ecKeyPair), Settings.chainId);
        // Initialize the inputParameters list
        List<Type> inputParameters = new ArrayList<>();

        // Convert and add the parameters
        inputParameters.add(new Address(buyer));
        inputParameters.add(new Address(collectionAddress));
        inputParameters.add(new DynamicArray<Utf8String>(Utf8String.class, ticketType));
        inputParameters.add(new DynamicArray<Uint256>(Uint256.class, amount));
        inputParameters.add(new DynamicArray<Utf8String>(Utf8String.class, seatNumbers));

        // Define your function call here
        String functionName = "getBuyRequestHash"; // replace with your function name
        Function function = new Function(
                functionName,
                inputParameters,
                Arrays.asList(new TypeReference<Bytes32>(){}));

        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = Settings.web3j.ethCall(
                        Transaction.createEthCallTransaction(manager.getFromAddress(), "0x344a778e91E4d7ae9bD0f189986e9783614B61b9", encodedFunction),
                        DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        if (response.hasError()) {
            throw new Exception("Error processing request: " + response.getError().getMessage());
        }

        System.out.println(response.getValue());

        List<Type> responseParams = FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());
        Bytes32 responseHash = (Bytes32) responseParams.get(0);

        return responseHash.getValue();
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

    private static byte[] signMessage(byte[] message, ECKeyPair ecKeyPair) {
        Sign.SignatureData sig = Sign.signMessage(message, ecKeyPair);
        byte[] output = new byte[65];

        System.arraycopy(sig.getR(), 0, output, 0, 32);
        System.arraycopy(sig.getS(), 0, output, 32, 32);
        System.arraycopy(sig.getV(), 0, output, 64, 1);

        return output;
    }


}
