package Wallet.Wallet;

import java.util.HashMap;
import java.util.Map;

public class Wallets {

	private static Map<String, Wallet> walletsByEmail = new HashMap<>();
	private static Map<String, Wallet> walletsByAddress = new HashMap<>();
	
	public static void add(Wallet w) {
		walletsByEmail.put(w.id, w);
		walletsByAddress.put(w.address, w);
	}
	
	public static Wallet getWalletByEmail(String email) {
		return walletsByEmail.get(email);
	}
	
	public static Wallet getWalletByAddress(String address) {
		return walletsByAddress.get(address);
	}
	
	
}
