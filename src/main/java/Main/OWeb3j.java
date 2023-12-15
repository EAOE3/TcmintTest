package Main;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Orcania Web3j
public class OWeb3j {

    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(10000000);

    public static Response sendScTxn(Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams) {
        RawTransactionManager manager = new RawTransactionManager(web3j, cr, chainId);
        List<Type> inputParameters = new ArrayList<>();
        for(Object o: inputParams) {
            inputParameters.add((Type) o);
        }

        Function function = new Function(
                functionName,  // function we're calling
                inputParameters,   // Parameters to pass as Solidity Types
                Arrays.asList());
        String encodedFunction = FunctionEncoder.encode(function);

        try {
            EthSendTransaction tr = manager.sendTransaction(gasPrice, gas, contractAddress, encodedFunction, value);

            if (tr.getError() != null) {
                return Response.failure(tr.getError().toString());
            } else {
                return Response.success(tr.getTransactionHash());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure(e.toString());
        }

    }

    //Auto calculates needed gas
    public static Response sendScTxn(Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger value, String functionName, Object... inputParams) {
        RawTransactionManager manager = new RawTransactionManager(web3j, cr, chainId);
        List<Type> inputParameters = new ArrayList<>();
        for(Object o: inputParams) {
            inputParameters.add((Type) o);
        }

        Function function = new Function(
                functionName,  // function we're calling
                inputParameters,   // Parameters to pass as Solidity Types
                Arrays.asList());
        String encodedFunction = FunctionEncoder.encode(function);

        BigInteger gasEstimate = BigInteger.valueOf(9000000);
        try {
            // Estimating the gas for the transaction
            gasEstimate = web3j.ethEstimateGas(
                    Transaction.createEthCallTransaction(
                            cr.getAddress(), contractAddress, encodedFunction
                    )
            ).send().getAmountUsed();
        } catch (Exception e) {
            e.printStackTrace();
            //return Response.failure("Failed to estimate transaction gas usage, this is a sign of the transaction failing");
        }

        try {

            // It's a good practice to add some buffer to the estimated gas.
            BigInteger gasBuffer = BigInteger.valueOf(10000);
            BigInteger totalGas = gasEstimate.add(gasBuffer);

            if(totalGas.compareTo(GAS_LIMIT) > 0) {
                return Response.failure("Gas limit exceeded");
            }

            EthSendTransaction tr = manager.sendTransaction(gasPrice, totalGas, contractAddress, encodedFunction, value);

            if (tr.getError() != null) {
                System.out.println("Failed tp submit txn after fueling");
                System.out.println(tr.getError().getMessage().toString());
                return Response.failure(tr.getError().toString());
            } else {
                return Response.success(tr.getTransactionHash());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure(e.toString());
        }
    }

    //Auto calculates needed gas
    public static Response sendScTxnCaptureFail(Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger value, String functionName, Object... inputParams) {
        RawTransactionManager manager = new RawTransactionManager(web3j, cr, chainId);
        List<Type> inputParameters = new ArrayList<>();
        for(Object o: inputParams) {
            inputParameters.add((Type) o);
        }

        Function function = new Function(
                functionName,  // function we're calling
                inputParameters,   // Parameters to pass as Solidity Types
                Arrays.asList());
        String encodedFunction = FunctionEncoder.encode(function);

        BigInteger gasEstimate = BigInteger.valueOf(9000000);
        try {
            // Estimating the gas for the transaction
            gasEstimate = web3j.ethEstimateGas(
                    Transaction.createEthCallTransaction(
                            cr.getAddress(), contractAddress, encodedFunction
                    )
            ).send().getAmountUsed();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure("Failed to estimate transaction gas usage, this is a sign of the transaction failing");
        }

        try {

            // It's a good practice to add some buffer to the estimated gas.
            BigInteger gasBuffer = BigInteger.valueOf(10000);
            BigInteger totalGas = gasEstimate.add(gasBuffer);

            if(totalGas.compareTo(GAS_LIMIT) > 0) {
                return Response.failure("Gas limit exceeded");
            }

            EthSendTransaction tr = manager.sendTransaction(gasPrice, totalGas, contractAddress, encodedFunction, value);

            if (tr.getError() != null) {
                System.out.println("Failed tp submit txn after fueling");
                System.out.println(tr.getError().getMessage().toString());
                return Response.failure(tr.getError().toString());
            } else {
                return Response.success(tr.getTransactionHash());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure(e.toString());
        }
    }

    public static Response fuelAndSendScTxn(Web3j web3j, Credentials cr, long chainId, String from, String contractAddress, BigInteger gasPrice, BigInteger value, String functionName, Object... inputParams) {
        RawTransactionManager manager = new RawTransactionManager(web3j, cr, chainId);
        List<Type> inputParameters = new ArrayList<>();
        for (Object o : inputParams) {
            inputParameters.add((Type) o);
        }

        Function function = new Function(
                functionName,  // function we're calling
                inputParameters,   // Parameters to pass as Solidity Types
                Arrays.asList());
        String encodedFunction = FunctionEncoder.encode(function);

        BigInteger gasEstimate = BigInteger.valueOf(9000000);
        try {
            // Estimating the gas for the transaction
            gasEstimate = web3j.ethEstimateGas(
                    Transaction.createEthCallTransaction(
                            cr.getAddress(), contractAddress, encodedFunction
                    )
            ).send().getAmountUsed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            // It's a good practice to add some buffer to the estimated gas.
            BigInteger gasBuffer = BigInteger.valueOf(10000);
            BigInteger totalGas = gasEstimate.add(gasBuffer);

            if (totalGas.compareTo(GAS_LIMIT) > 0) {
                return Response.failure("Gas limit exceeded");
            }

            Response r = fuel(from, gasPrice.multiply(totalGas));

            if (!r.success) return r;

            EthSendTransaction tr = manager.sendTransaction(gasPrice, totalGas, contractAddress, encodedFunction, value);

            if (tr.getError() != null) {
                System.out.println("Failed tp submit txn after fueling");
                System.out.println(tr.getError().getMessage().toString());
                return Response.failure(tr.getError().getMessage().toString());
            } else {
                return Response.success(tr.getTransactionHash());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure(e.toString());
        }
    }

//    public static List<Type> HashesCheck(Web3j web3j, String from, String contractAddress, String functionName, Object... inputParams) {
//        List<Type> inputParameters = new ArrayList<>();
//        for(Object o: inputParams) {
//            inputParameters.add((Type) o);
//        }
//
//        Function function = new Function(
//                functionName,  // function we're calling
//                inputParameters,   // Parameters to pass as Solidity Types
//                Arrays.asList(new TypeReference<DynamicArray<Uint256>>(){}, new TypeReference<DynamicArray<Uint256>>(){}));
//        String encodedFunction = FunctionEncoder.encode(function);
//        EthCall ethCall = null;
//        try {ethCall = Main.web3j.ethCall(Transaction.createEthCallTransaction(Main.address, Main.beaconContract, encodedFunction),DefaultBlockParameterName.LATEST).send();} catch (IOException e) {e.printStackTrace(); }
//
//        return  FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
//    }

    public static Response fuel(String to, BigInteger amount) {
        try {
            TransactionManager tm = new RawTransactionManager(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId);
            EthSendTransaction tr = tm.sendTransaction(Settings.getGasPrice(), BigInteger.valueOf(21000), to, "", amount);

            if (tr.getTransactionHash() == null) return Response.failure(tr.getError().getMessage());

            // Poll for the transaction response
            EthGetTransactionReceipt transactionReceipt;
            do {
                transactionReceipt = Settings.web3j.ethGetTransactionReceipt(tr.getTransactionHash()).send();
                Thread.sleep(2000); // sleep for a while before polling again
            } while (transactionReceipt.getTransactionReceipt().isEmpty());

            TransactionReceipt receipt = transactionReceipt.getResult();

            if ("0x1".equals(receipt.getStatus())) {
                System.out.println("Transaction was successful.");
                return Response.success(tr.getTransactionHash());
            } else {
                System.out.println("Transaction failed.");
                return Response.failure("Transaction failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.failure(e.toString());
        }

    }

    //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger gas, BigInteger value, String functionName, Object... inputParams
    public static void main(String[] args) {
        Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, "0x45e561E6d6A5e7fce6BcAf55Fe9Cc6eA7b3d4880", BigInteger.valueOf(5000000000L), BigInteger.valueOf(300000), BigInteger.ZERO, "initialize" );
        System.out.println(r.success);
        System.out.println(r.message);

        r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, "0x45e561E6d6A5e7fce6BcAf55Fe9Cc6eA7b3d4880", BigInteger.valueOf(5000000000L), BigInteger.valueOf(300000), BigInteger.ZERO, "deposit", new Address("0x61Bd8fc1e30526Aaf1C4706Ada595d6d236d9883"), new Uint256(new BigInteger("100000000000000000000")), new Utf8String(("")) );

        System.out.println(r.success);
        System.out.println(r.message);
    }
}
