package Verification.SmartContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;


public class SignatureVerification {

	public static final Web3j web3j = Web3j.build(new HttpService("https://data-seed-prebsc-1-s1.binance.org:8545"));;

	public static String getSigner(String message, String signature) {
		List<Type> inputParameters = new ArrayList<>();
		inputParameters.add(new Utf8String(message));
		inputParameters.add(new DynamicBytes(Hex.decode(signature)));

		Function function = new Function("recoverSigner", // function we're calling
				inputParameters, // Parameters to pass as Solidity Types
				Arrays.asList(new TypeReference<Address>() {
				}));
		String encodedFunction = FunctionEncoder.encode(function);
		EthCall ethCall = null;
		try {
			ethCall = web3j.ethCall(
					Transaction.createEthCallTransaction("0xB8aA45290f0fCcD4126A582384617d5a18f82A31",
							"0xB8aA45290f0fCcD4126A582384617d5a18f82A31", encodedFunction),
					DefaultBlockParameterName.LATEST).send();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (String) FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()).get(0).getValue();
	}
	
	public static void main(String[] args) {
		System.out.println(getSigner(
				"PLEASE READ THE FOLLOWING INFORMATION CAREFULLY BEFORE PROCEEDING WITH YOUR OFFER:\n"
				+ "\n"
				+ "As a prospective buyer on the Ticmint Marketplace, you are initiating a legally binding offer to purchase a specific Non-Fungible Token (NFT). The details of the NFT and the offer are as follows:\n"
				+ "\n"
				+ "1. NFT Identification: The NFT you intend to purchase is identified by the ID number 5.\n"
				+ "2. NFT Collection: The NFT is part of a collection hosted at the contract address 0x6BB625112d370e536E1485a8CD2dA2DbdeAfE6FD.\n"
				+ "3. Offer Price: You are proposing to purchase the above-mentioned NFT for a total consideration of 100000000 tokens of contract address 0x6BB625112d370e536E1485a8CD2dA2DbdeAfE6FD.\n"
				+ "4. Offer Deadline: The current owner of the NFT has until epoch second 87875465498789 to accept your proposal.\n"
				+ "\n"
				+ "By proceeding, you are asserting that you understand the terms of your offer, the item in question, and that you are prepared to finalize the purchase under these conditions should the current owner accept within the stated timeframe. Proceed with your offer only if you agree to these terms.\n"
				+ "\n"
				+ "The completion of your offer signifies your understanding, acceptance, and willingness to comply with the aforementioned terms and conditions.", 
				"a32154f510eae482eb06a983797fe6aa404dbce5c0754f85f066061191ccc71e503a8d9f556d48ffeebb4d1904a9c73596a55a0d0f8ea0f2f963689787858c4d1b"));
	}
}
