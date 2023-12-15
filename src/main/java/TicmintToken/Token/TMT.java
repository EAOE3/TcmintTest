package TicmintToken.Token;

import Main.ByteArrayWrapper;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TMT {

    private static Map<ByteArrayWrapper /*Users address*/, BigInteger> balanceByUserAddress = new HashMap<>();
    private static Map<ByteArrayWrapper /*Users address*/, BigInteger> lockedBalanceByUserAddress = new HashMap<>();
    private static final ByteArrayWrapper zeroAddress = new ByteArrayWrapper(new byte[20]);

    public static void transfer(ByteArrayWrapper from, ByteArrayWrapper to, BigInteger amount) {
        //This is a sign of withdrawal. We already deducted the users balance in lockInPreparationForWithdrawal
        //The transfer to address 0 event is for regular wallets to update the users balance
        if(to.equals(zeroAddress)) return;

        BigInteger toBalance = balanceByUserAddress.getOrDefault(to, BigInteger.ZERO);
        BigInteger fromBalance = balanceByUserAddress.get(from);

        balanceByUserAddress.put(to, toBalance.add(amount));
        if(fromBalance != null) balanceByUserAddress.put(from, fromBalance.subtract(amount));
    }

    public static void deposit(String userAddress, BigInteger amount) {
//        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
//        BigInteger balance = balanceByUserAddress.get(user);
//        if(balance == null) balanceByUserAddress.put(user, amount);
//        else balanceByUserAddress.put(user, balance.add(amount));
    }
    public static void lockInPreparationForWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger balance = balanceByUserAddress.get(user);
        if(balance == null) return;
        else {
            balanceByUserAddress.put(user, balance.subtract(amount));

            BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
            if(lockedBalance == null) lockedBalanceByUserAddress.put(user, amount);
            else lockedBalanceByUserAddress.put(user, lockedBalance.add(amount));
        }
    }

    public static void completeWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return;
        else {
            lockedBalanceByUserAddress.put(user, lockedBalance.subtract(amount));
        }
    }

    public static void unlockFromWithdrawal(String userAddress, BigInteger amount) {
        ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(userAddress.substring(2)));
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return;
        else {
            lockedBalanceByUserAddress.put(user, lockedBalance.subtract(amount));

            BigInteger balance = balanceByUserAddress.get(user);
            if(balance == null) balanceByUserAddress.put(user, amount);
            else balanceByUserAddress.put(user, balance.add(amount));
        }
    }

    public static BigInteger getUserBalance(ByteArrayWrapper user) {
        BigInteger balance = balanceByUserAddress.get(user);
        if(balance == null) return BigInteger.ZERO;
        else return balance;
    }

    public static BigInteger getUserBalance(String userAddress) {
        return getUserBalance(new ByteArrayWrapper(Hex.decode(userAddress.substring(2))));
    }
    public static BigInteger getUserBalance(byte[] user) {
        return getUserBalance(new ByteArrayWrapper(user));
    }

    //getUserLockedBalance
    public static BigInteger getUserLockedBalance(ByteArrayWrapper user) {
        BigInteger lockedBalance = lockedBalanceByUserAddress.get(user);
        if(lockedBalance == null) return BigInteger.ZERO;
        else return lockedBalance;
    }

    public static BigInteger getUserLockedBalance(String user) {
        return getUserLockedBalance(new ByteArrayWrapper(Hex.decode(user.substring(2))));
    }

}
