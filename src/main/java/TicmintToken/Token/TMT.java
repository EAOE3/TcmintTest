package TicmintToken.Token;

import Database.DBM;
import Main.ByteArrayWrapper;
import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TMT extends DBM {

    private Map<ByteArrayWrapper /*Users address*/, BigInteger> balanceByUserAddress = new HashMap<>();
    private Map<ByteArrayWrapper /*Users address*/, BigInteger> lockedBalanceByUserAddress = new HashMap<>();
    private final ByteArrayWrapper zeroAddress = new ByteArrayWrapper(new byte[20]);

    public TMT() {
        super("TMT", false, false);

        //Fetch all balances
        File balancesFolder = new File(rootPath + "balance/");
        File[] files = balancesFolder.listFiles();
        if(files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String userAddress = fileName.substring(0, fileName.length() - 4);
                BigInteger balance = loadBigInt("balance/" + userAddress);
                balanceByUserAddress.put(new ByteArrayWrapper(Hex.decode(userAddress)), balance);
            }
        }

        //Load all locked balances
        File lockedBalancesFolder = new File(rootPath + "locked/");
        files = lockedBalancesFolder.listFiles();
        if(files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String userAddress = fileName.substring(0, fileName.length() - 4);
                BigInteger lockedBalance = loadBigInt("locked/" + userAddress);
                lockedBalanceByUserAddress.put(new ByteArrayWrapper(Hex.decode(userAddress)), lockedBalance);
            }
        }
    }
    public void transfer(ByteArrayWrapper from, ByteArrayWrapper to, BigInteger amount) {
        //This is a sign of withdrawal. We already deducted the users balance in lockInPreparationForWithdrawal
        //The transfer to address 0 event is for regular wallets to update the users balance

        BigInteger toBalance = balanceByUserAddress.getOrDefault(to, BigInteger.ZERO);
        BigInteger fromBalance = balanceByUserAddress.get(from);

        BigInteger toNewBalance = toBalance.add(amount);

        balanceByUserAddress.put(to, toNewBalance);
        if(fromBalance != null) balanceByUserAddress.put(from, fromBalance.subtract(amount));

        store("balance/" + Hex.toHexString(to.data()), toBalance.add(amount));
        if(fromBalance != null) store("balance/" + Hex.toHexString(from.data()), fromBalance.subtract(amount));

    }

    public void deposit(String userAddress, BigInteger amount) {
//        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
//        BigInteger balance = balanceByUserAddress.get(user);
//        if(balance == null) balanceByUserAddress.put(user, amount);
//        else balanceByUserAddress.put(user, balance.add(amount));
    }
    public void lockInPreparationForWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger balance = balanceByUserAddress.get(user);
        if(balance == null) return;
        else {
            balanceByUserAddress.put(user, balance.subtract(amount));

            BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
            if(lockedBalance == null) {
                lockedBalanceByUserAddress.put(user, amount);
                store("locked/" + Hex.toHexString(user.data()), amount);
            }
            else {
                lockedBalanceByUserAddress.put(user, lockedBalance.add(amount));
                store("locked/" + Hex.toHexString(user.data()), lockedBalance.add(amount));
            }

            store("balance/" + Hex.toHexString(user.data()), balance.subtract(amount));
        }
    }

    public void completeWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return;
        else {
            lockedBalanceByUserAddress.put(user, lockedBalance.subtract(amount));

            store("locked/" + Hex.toHexString(user.data()), lockedBalance.subtract(amount));
        }
    }

    public void unlockFromWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return;
        else {
            lockedBalanceByUserAddress.put(user, lockedBalance.subtract(amount));

            BigInteger balance = balanceByUserAddress.get(user);
            if(balance == null) {
                balanceByUserAddress.put(user, amount);
                store("balance/" + Hex.toHexString(user.data()), amount);
            }
            else {
                balanceByUserAddress.put(user, balance.add(amount));
                store("balance/" + Hex.toHexString(user.data()), balance.add(amount));
            }
        }
    }

    public BigInteger getUserBalance(ByteArrayWrapper user) {
        BigInteger balance = balanceByUserAddress.get(user);
        if(balance == null) return BigInteger.ZERO;
        else return balance;
    }

    public BigInteger getUserBalance(String userAddress) {
        return getUserBalance(new ByteArrayWrapper(Hex.decode(userAddress.substring(2))));
    }
    public BigInteger getUserBalance(byte[] user) {
        return getUserBalance(new ByteArrayWrapper(user));
    }

    //getUserLockedBalance
    public BigInteger getUserLockedBalance(ByteArrayWrapper user) {
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return BigInteger.ZERO;
        else return lockedBalance;
    }

    public BigInteger getUserLockedBalance(String user) {
        return getUserLockedBalance(new ByteArrayWrapper(Hex.decode(user.substring(2))));
    }

}
