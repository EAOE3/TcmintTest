package Main;

import Database.SDBM;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class Settings {

	public final static Web3j web3j = Web3j.build(new HttpService("https://polygon-mainnet.infura.io/v3/b29b303a86714dfa9a9ed28b247c0db3"));
	public static final String web2BackendUrl = "";
	public static final long chainId = 137;
	public static final long blockTime = 2000;
	private static BigInteger gasPrice;
	public static final String marketplaceAddress = "0x9aF0618be6836b5835d40A0e07CCbb7E71Adb17F";
	public static final String eventManagementContractAddress = "0xc02D3B120667cd247DA4DBD330266bb7EE381fDD";
	public static final String merchandiseManagementContractAddress = "0x8a40A723483C172ce0e6c292F36e72E5d71430F5";
	public static final String addOnsAndAdditonalServicesContractAddress = "0x3D9b1E1E6D941A4c30eC75928c06fC7B40c073Ba";
	public static final String ticmintFeeAccount = "0x61Bd8fc1e30526Aaf1C4706Ada595d6d236d9883"; //The account where all ticmint fee go
	public static final String ticmintTokenAddress = "0xb95e993511B825ECF25DbB88851912e2F8Df9dc0"; //The address of the TCMINT token
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
