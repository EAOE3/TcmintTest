package TicmintToken.Blockchain;

import Database.SDBM;
import Main.ByteArrayWrapper;
import Main.Settings;
import TicmintToken.API.GET;
import TicmintToken.API.POST;
import TicmintToken.Token.TMT;
import TicmintToken.Webhook.Sender;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Synchroniser {

    public static void synchronise(List<EthLog.LogResult> logs) {
        //System.out.println("Synchronising " + logs.size() + " logs");
        for (EthLog.LogResult logResult : logs) {
            try {
                Log log = ((EthLog.LogObject) logResult).get();

                if(!log.getAddress().equalsIgnoreCase(Settings.ticmintTokenAddress)) continue;

                //System.out.println("Topic: " + log.getTopics().get(0));
                //Token Transfer
                if (log.getTopics().get(0).equalsIgnoreCase("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), TRANSFER.getNonIndexedParameters());

                    String from = log.getTopics().get(1).substring(26);
                    ByteArrayWrapper to = new ByteArrayWrapper(Hex.decode(log.getTopics().get(2).substring(26)));
                    BigInteger amount = (BigInteger) results.get(0).getValue();

                    TMT.transfer(new ByteArrayWrapper(Hex.decode(from)), to, amount);
                }
                //Deposit
                else if (log.getTopics().get(0).equalsIgnoreCase("0x643e927b32d5bfd08eccd2fcbd97057ad413850f857a2359639114e8e8dd3d7b")) {
                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), Deposit.getNonIndexedParameters());
                    String userAddress = "0x" + log.getTopics().get(1).substring(26);
                    BigInteger amount = (BigInteger) results.get(0).getValue();
                    String data = (String) results.get(1).getValue();

                    System.out.println("Deposit");
                    System.out.println("User: " + userAddress);
                    System.out.println("Amount: " + amount);
                    System.out.println("Data: " + data);

                    TMT.deposit(userAddress, amount);
                    Sender.notifyOfDeposit(log.getTransactionHash(), userAddress, amount, data);
                }
                //LockForWithdrawal
                else if (log.getTopics().get(0).equalsIgnoreCase("0x2e27ea959c7c6c73897d7b6471f796cef1f7a96541157fe62ad6495e3c77ebd0")) {
                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), LockForWithdrawal.getNonIndexedParameters());
                    String userAddress = "0x" + log.getTopics().get(1).substring(26);
                    BigInteger amount = (BigInteger) results.get(0).getValue();
                    String data = (String) results.get(1).getValue();

                    System.out.println("LockForWithdrawal");
                    System.out.println("User: " + userAddress);
                    System.out.println("Amount: " + amount);
                    System.out.println("Data: " + data);

                    TMT.lockInPreparationForWithdrawal(userAddress, amount);
                    Sender.notifyOfLockInPreparationForWithdrawal(log.getTransactionHash(), userAddress, amount, data);
                }
                //UnlockFromWithdrawal
                else if (log.getTopics().get(0).equalsIgnoreCase("0x4e9da3aef9d180cf0d0e0f20d56a349c4bea7f14d0bca60577736829c83b5a0c")) {
                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), UnlockFromWithdrawal.getNonIndexedParameters());
                    String userAddress = "0x" + log.getTopics().get(1).substring(26);
                    BigInteger amount = (BigInteger) results.get(0).getValue();
                    String data = (String) results.get(1).getValue();

                    System.out.println("UnlockFromWithdrawal");
                    System.out.println("User: " + userAddress);
                    System.out.println("Amount: " + amount);
                    System.out.println("Data: " + data);

                    TMT.unlockFromWithdrawal(userAddress, amount);
                    Sender.notifyOfUnlockFromWithdrawal(log.getTransactionHash(), userAddress, amount, data);
                }
                //Withdraw
                else if (log.getTopics().get(0).equalsIgnoreCase("0x485f1bb6524c663555797e00171a10f341656e59b02d6b557a0a38ba7d5d9751")) {
                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), Withdraw.getNonIndexedParameters());
                    String userAddress = "0x" + log.getTopics().get(1).substring(26);
                    BigInteger amount = (BigInteger) results.get(0).getValue();
                    String data = (String) results.get(1).getValue();

                    System.out.println("Withdraw");
                    System.out.println("User: " + userAddress);
                    System.out.println("Amount: " + amount);
                    System.out.println("Data: " + data);

                    TMT.completeWithdrawal(userAddress, amount);
                    Sender.notifyOfWithdrawal(log.getTransactionHash(), userAddress, amount, data);
                }

            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws Exception {

        SDBM.store("motherWalletKey", new BigInteger(Hex.decode("03a5240936d67dc18dca348e793010a14c5eba86a73d0c9e45764681295a73df")));
        Settings.initGasPriceUpdate();
        Thread.sleep(2000);
        POST.run();
        GET.run();

        BigInteger currentBlockNumber = BigInteger.valueOf(40384055);

        //Output  address of mother wallet
        System.out.println("Mother wallet address: " + Settings.getMotherWalletCredentials().getAddress());
        while (true) {
            System.out.println("Gas price: " + Settings.getGasPrice());
            try {
                BigInteger latestBlock = Settings.web3j.ethBlockNumber().send().getBlockNumber();
                //System.out.println("Synchronizing block " + latestBlock + " from " + currentBlockNumber + " to " + latestBlock);
                if (currentBlockNumber.compareTo(latestBlock) != -1) {
                    Thread.sleep(2000);
                    continue;
                }

                EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(currentBlockNumber), DefaultBlockParameter.valueOf(latestBlock), Settings.ticmintTokenAddress);
                EthLog ethLog = Settings.web3j.ethGetLogs(filter).send();
                List<EthLog.LogResult> logs = ethLog.getLogs();

                synchronise(logs);

                currentBlockNumber = latestBlock.add(BigInteger.ONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static final Event TRANSFER = new Event("Transfer",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event Deposit = new Event("Deposit",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    }
            )
    );

    public static final Event LockForWithdrawal = new Event("LockForWithdrawal",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    }
            )
    );

    public static final Event UnlockFromWithdrawal = new Event("UnlockFromWithdrawal",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    }
            )
    );

    public static final Event Withdraw = new Event("Withdraw",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    }
            )
    );
}
