package Wallet.Wallet;

import java.math.BigInteger;
import java.util.Arrays;

import Main.Signature;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import Main.Settings;

public class MotherWallet {


    private static final ECKeyPair ecKeyPair = ECKeyPair.create(Hex.decode("03a5240936d67dc18dca348e793010a14c5eba86a73d0c9e45764681295a73df"));
    public final String address = Keys.toChecksumAddress(Credentials.create(ecKeyPair).getAddress()).toLowerCase();;

    //Setters==============================================================================================================

    public static boolean transferETH(String to, BigInteger amount) throws Exception {
        TransactionManager tm = new RawTransactionManager(Settings.web3j, Credentials.create(ecKeyPair), Settings.chainId);
        EthSendTransaction tr = tm.sendTransaction(Settings.getGasPrice(), BigInteger.valueOf(21000), to, "", amount);

        if(tr.getTransactionHash() == null) return false;

        // Poll for the transaction response
        EthGetTransactionReceipt transactionReceipt;
        do {
            transactionReceipt = Settings.web3j.ethGetTransactionReceipt(tr.getTransactionHash()).send();
            Thread.sleep(2000); // sleep for a while before polling again
        } while (transactionReceipt.getTransactionReceipt().isEmpty());

        TransactionReceipt receipt = transactionReceipt.getResult();

        if ("0x1".equals(receipt.getStatus())) {
            System.out.println("Transaction was successful.");
            return true;
        } else {
            System.out.println("Transaction failed.");
            return false;
        }

    }

}

