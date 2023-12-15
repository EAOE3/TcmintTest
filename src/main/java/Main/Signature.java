package Main;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import Wallet.Wallet.Wallet;

public class Signature {

    public static String recoverSigner(String message, String signature) {
        byte[] messageHash = Hash.sha3(message.getBytes());

        // The signature is in "Hex" format, it needs to be converted to binary format first
        byte[] signatureBytes = Hex.decode(signature);

        // Now we need to split the signature into v, r, s
        int v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        SignatureData sd = new SignatureData(
            (byte) v, 
            (byte[]) Arrays.copyOfRange(signatureBytes, 0, 32), 
            (byte[]) Arrays.copyOfRange(signatureBytes, 32, 64));

        // And then recover the address
        String addressRecovered = null;
        try {
            BigInteger keyRecovered = Sign.signedMessageHashToKey(messageHash, sd);
            addressRecovered = "0x" + Keys.getAddress(keyRecovered);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        
        return addressRecovered;
    }

    public static String recoverPersonalSigner(String message, String signature) {
        String formattedMessage = "\u0019Ethereum Signed Message:\n" + message.length() + message;
        byte[] messageHash = Hash.sha3(formattedMessage.getBytes());

        // The signature is in "Hex" format, it needs to be converted to binary format first
        byte[] signatureBytes = Hex.decode(signature);

        // Now we need to split the signature into v, r, s
        int v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        SignatureData sd = new SignatureData(
                (byte) v,
                (byte[]) Arrays.copyOfRange(signatureBytes, 0, 32),
                (byte[]) Arrays.copyOfRange(signatureBytes, 32, 64));

        // And then recover the address
        String addressRecovered = null;
        try {
            BigInteger keyRecovered = Sign.signedMessageHashToKey(messageHash, sd);
            addressRecovered = "0x" + Keys.getAddress(keyRecovered);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return addressRecovered;
    }

    public static byte[] personalSignMessage(byte[] message, ECKeyPair ecKeyPair) {

        // make sure the message is in the correct format
        String prefix = "\u0019Ethereum Signed Message:\n" + message.length;
        String messageHash = prefix + message.toString();

        try {
            // sign the message
            Sign.SignatureData signatureData = Sign.signMessage(messageHash.getBytes(), ecKeyPair);

            // Combine the R, S, and V components of the signature into one byte array
            byte[] signature = new byte[65];  // 32 bytes for R, 32 bytes for S, 1 byte for V

            System.arraycopy(signatureData.getR(), 0, signature, 0, 32);
            System.arraycopy(signatureData.getS(), 0, signature, 32, 32);
            signature[64] = signatureData.getV()[0];

            System.out.println("Signature: " + Hex.toHexString(signature));
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(recoverPersonalSigner("hello", "2c6b49690b316740a1ffc71d5473b319fac272502f346741c252a7706dd01bc818e69ec7518e9eb50e95ee0300dd3867d142b4a37982fc9c48248a105bf399c21c"));
    }
}
