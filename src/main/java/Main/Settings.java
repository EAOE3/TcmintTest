package Main;

import Database.SDBM;
import TicmintToken.Token.TMT;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class Settings {

	public final static Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.maticvigil.com/"));
	public static final String web2BackendUrl = "";
	public static final long chainId = 80001;
	public static final long blockTime = 2000;
	private static BigInteger gasPrice;

	public static TMT tmt = new TMT();
	public static final String marketplaceAddress = "0x3364648b1d3786b451e59b9D3a6803d0b584384b";
	public static final String eventManagementContractAddress = "0xF7cC30B8fA7b68C726eCD38749A75D4Fde47D4f8";
	public static final String merchandiseManagementContractAddress = "0x1Fdd6b6b412D5Edd9BF5ADf72BA1B975FC6c8B7D";
	public static final String addOnsAndAdditonalServicesContractAddress = "0x01f05C7Ae2A3c987E712e938635742d7828718D2";
	public static final String ticmintFeeAccount = "0x61Bd8fc1e30526Aaf1C4706Ada595d6d236d9883"; //The account where all ticmint fee go
	public static final String ticmintTokenAddress = "0x1c0c53C08ABca5A3ab2B69fd7073B7A4Ea951d17"; //The address of the TCMINT token
	public static BigInteger getGasPrice() {
		return gasPrice;
	}
	public final static String haha = "57wtrsgfuhcxn83TOIHEWGSBJL";
	private static String password;
	public static void setPassword(String pass) {
		password = pass;
	}
	public static Credentials getMotherWalletCredentials() {
		try {
			return Credentials.create(Hex.toHexString(KeyGenEncDec.decrypt(SDBM.load("motherWallet"), password)));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void initGasPriceUpdate() {
		new Thread() {
			public void run() {
				while (true) {
						try {
							//Increase the gas price by 2 gwei for speed purposes
							gasPrice = web3j.ethGasPrice().send().getGasPrice().add(BigInteger.valueOf(2000000000));
						} catch (IOException e1) {
							e1.printStackTrace();
						}

					try {
						Thread.sleep(blockTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
