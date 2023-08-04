package TicketSaleCreation.Management;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import Main.Settings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;

import Main.Response;

public class ManagementContract {

	public static String serverEnpoint;

	public static void notifyServerOfSuccess(String txnHash, String contractAddress) {
		new Thread() {
			public void run() {
				try {
					System.out.println("Notifying server of success");
					JSONObject body = new JSONObject();
					JSONObject event = new JSONObject();

					event.put("success", true);
					event.put("contractAddress", contractAddress);
					event.put("transactionHash", txnHash);

					body.put("event", event);

					String url = serverEnpoint + "/web3EventUpdate";

					HttpClient client = HttpClient.newHttpClient();
					HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
							.POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
							.build();
					client.send(request, HttpResponse.BodyHandlers.ofString());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
