package Database;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

public class AES256 {
  private static final int ITERATION_COUNT = 65536;
  private static final int KEY_LENGTH = 256;
  private static final String SALT = "your-salt-value"; // You should use a unique salt value

  public static byte[] encrypt(byte[] data, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(StandardCharsets.UTF_8), ITERATION_COUNT, KEY_LENGTH);
      SecretKey secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      IvParameterSpec ivSpec = new IvParameterSpec(generateIV());
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

      byte[] encryptedData = cipher.doFinal(data);
      byte[] iv = cipher.getIV();

      byte[] encryptedDataWithIV = new byte[iv.length + encryptedData.length];
      System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.length);
      System.arraycopy(encryptedData, 0, encryptedDataWithIV, iv.length, encryptedData.length);

      return encryptedDataWithIV;
  }

  public static byte[] decrypt(byte[] encryptedDataWithIV, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(StandardCharsets.UTF_8), ITERATION_COUNT, KEY_LENGTH);
      SecretKey secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      byte[] iv = new byte[16];
      System.arraycopy(encryptedDataWithIV, 0, iv, 0, iv.length);

      byte[] encryptedData = new byte[encryptedDataWithIV.length - iv.length];
      System.arraycopy(encryptedDataWithIV, iv.length, encryptedData, 0, encryptedData.length);

      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

      return cipher.doFinal(encryptedData);
  }

  private static byte[] generateIV() {
      SecureRandom random = new SecureRandom();
      byte[] iv = new byte[16];
      random.nextBytes(iv);
      return iv;
  }

  public static void main(String[] args) {
      java.math.BigInteger t = new java.math.BigInteger("9872156793");

      try {
          byte[] encryptedData = encrypt(t.toByteArray(), "testlol");

          java.math.BigInteger newT = new java.math.BigInteger(decrypt(encryptedData, "testlol"));

          System.out.println(newT);
      } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
               | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
  }
}
