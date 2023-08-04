package Main;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

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

    public static void main(String[] args) {
        String message = "testo";
        String signature = "0x96426d76c4e41c186e99ec822a105c7221b3be9bb4fcf6d2e16224f569079f9f54df7d37e5fd90f5b1f2fa01d1d9dc9f4fb701d79a0c83e969efb0e7e9488fb01b";

        System.out.println(recoverSigner(message, signature));
    }
}
