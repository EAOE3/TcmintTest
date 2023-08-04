package Main;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Sha3 {
	
	public static String getRandomHash() {
		MessageDigest digest = null;
		try {digest = MessageDigest.getInstance("SHA-256");} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		
		Random rand = new Random();
		
		for(int t=0; t < 5; ++t) {
			digest.update(java.math.BigInteger.valueOf(rand.nextLong()).toByteArray());
		}
		
		byte[] encodedhash = digest.digest();
		return bytesToHex(encodedhash);
	}
	
	public static String hash(long number) {
		MessageDigest digest = null;
		try {digest = MessageDigest.getInstance("SHA-256");} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		byte[] encodedhash = digest.digest(java.math.BigInteger.valueOf(number).toByteArray());
		
		return bytesToHex(encodedhash);
	}
	
	public static String hash(String text) {
		MessageDigest digest = null;
		try {digest = MessageDigest.getInstance("SHA-256");} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		byte[] encodedhash = digest.digest(text.getBytes());
		
		return bytesToHex(encodedhash);
	}
	
	private static String bytesToHex(byte[] hash) {
	    StringBuilder hexString = new StringBuilder(2 * hash.length);
	    for (int i = 0; i < hash.length; i++) {
	        String hex = Integer.toHexString(0xff & hash[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
}
