package Main;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class Settings {

	public final static Web3j web3j = Web3j.build(new HttpService("https://polygon-mumbai.infura.io/v3/e830260b3f774c058e2d424ca16b8e40"));
	public static final long chainId = 80001;
	public static final long blockTime = 2000;
	private static BigInteger gasPrice;
	public static final String marketplaceAddress = "0x19B82a36392bB9850837A495F4C992AfAAC505e3";
	public static final String managementContractAddress = "0xD440436209B5C9821A05D81e1F11D222406190E3";
	public static BigInteger getGasPrice() {
		return gasPrice;
	}
	public final static String haha = "57wtrsgfuhcxn83TOIHEWGSBJL";

	public static void initGasPriceUpdate() {
		new Thread() {
			public void run() {
				while (true) {
						try {
							gasPrice = web3j.ethGasPrice().send().getGasPrice();
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
