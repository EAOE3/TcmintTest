package Main;

import Database.AES256;
import Database.SDBM;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

//Key Generationg encryption and decryption
public class KeyGenEncDec {

    public static void main(String[] args) throws Exception {
        //String password = "ticmintarelolmyswimfudgenokacoyes";

//        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
//        String address = Keys.toChecksumAddress(Credentials.create(ecKeyPair).getAddress()).toLowerCase();
//        System.out.println("Address: " + address);
//
//        //Extra encryption for the key (Other than the one that the database already offers)
//        byte[] key = encrypt(ecKeyPair.getPrivateKey().toByteArray(), password);
//
//        SDBM.store("motherWallet", key);

//        //load key from disk
//        byte[] encryptedKey = SDBM.load("motherWallet");
//        byte[] decryptedKey = decrypt(encryptedKey, password);
//
//        //Create wallet and output address
//        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(decryptedKey));
//        String address = Keys.toChecksumAddress(Credentials.create(ecKeyPair).getAddress()).toLowerCase();
//        System.out.println("Address: " + address);

    }

    public static byte[] encrypt(byte[] data, String password) throws Exception {
        byte[] password1 = Hash.sha3((password + Settings.haha).getBytes(StandardCharsets.UTF_8));
        BigInteger password2 = new BigInteger(password1).multiply(new BigInteger(password.getBytes(StandardCharsets.UTF_8)));
        String finalEncryptionKey = Hex.toHexString(password2.toByteArray());

        return AES256.encrypt(data, finalEncryptionKey);
    }
    public static byte[] decrypt(byte[] encryptedData, String password) throws Exception {
        byte[] password1 = Hash.sha3((password + Settings.haha).getBytes(StandardCharsets.UTF_8));
        BigInteger password2 = new BigInteger(password1).multiply(new BigInteger(password.getBytes(StandardCharsets.UTF_8)));
        String finalDecryptionKey = Hex.toHexString(password2.toByteArray());

        return AES256.decrypt(encryptedData, finalDecryptionKey);
    }
}
