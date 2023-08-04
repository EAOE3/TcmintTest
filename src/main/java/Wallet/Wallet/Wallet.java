package Wallet.Wallet;

import java.math.BigInteger;
import java.util.Arrays;

import Main.Signature;
import Marketplace.Offer.Offer;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.util.ArrayList;
import java.util.List;

import Main.Response;

import Database.AES256;
import Database.DBM;
import Main.Settings;

public class Wallet extends DBM {
	public final String address;
	
	//Used for creating and loading
	public Wallet(String email) throws Exception {
		super(email);
		
		String address = loadString("address");
		if(address == null) {
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			address = Keys.toChecksumAddress(Credentials.create(ecKeyPair).getAddress()).toLowerCase();
			
			//Extra encryption for the key (Other than the one that the database already offers)
			byte[] key = AES256.encrypt(ecKeyPair.getPrivateKey().toByteArray(), address + Settings.haha);
			
			store("key", key);
		}
		
		this.address = address;
		Wallets.add(this);
	}
	
	//Setters==============================================================================================================

	public byte[] signMessage(String message) {

		// make sure the message is in the correct format
		String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
		String messageHash = prefix+message;

		try {
			// sign the message
			Sign.SignatureData signatureData = Sign.signMessage(messageHash.getBytes(), getKeyPair());

			// Combine the R, S, and V components of the signature into one byte array
			byte[] signature = new byte[65];  // 32 bytes for R, 32 bytes for S, 1 byte for V

			System.arraycopy(signatureData.getR(), 0, signature, 0, 32);
			System.arraycopy(signatureData.getS(), 0, signature, 32, 32);
			signature[64] = signatureData.getV()[0];

			System.out.println("Signature: " + Hex.toHexString(signature));
			return signature;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Response transferETH(Web3j web3j, long chainId, String to, BigInteger amount, BigInteger gas, BigInteger gasPrice) throws Exception {
		TransactionManager tm = new RawTransactionManager(web3j, Credentials.create(getKeyPair()), chainId);
		EthSendTransaction tr = tm.sendTransaction(gasPrice, gas, to, "", amount);
		
		if(tr.getError() != null) {
			return Response.failure(tr.getError().toString());
		}

		return Response.success(tr.getTransactionHash());
	}

	// takeBuyOffer(address nftContract , uint256 nftId, address crypto, uint256 amount, uint256 deadline, bytes calldata signature)
	public Response takeBuyOffer(Offer offer) throws Exception {
		RawTransactionManager manager = new RawTransactionManager(Settings.web3j, Credentials.create(getKeyPair()), Settings.chainId);
		List<Type> inputParameters = new ArrayList<>();
		inputParameters.add(new Address(offer.collectionAddress));
		inputParameters.add(new Uint256(offer.nftId));
		inputParameters.add(new Address(offer.tokenAddress));
		inputParameters.add(new Uint256(offer.tokenAmount));
		inputParameters.add(new Uint256(offer.deadline));
		inputParameters.add(new DynamicBytes(offer.offerorsSignature));

		Function function = new Function(
				"takeBuyOffer",  // function we're calling
				inputParameters,   // Parameters to pass as Solidity Types
				Arrays.asList());
		String encodedFunction = FunctionEncoder.encode(function);

		BigInteger gas = getBuyOfferGasEstimate(offer);
		BigInteger gasPrice = Settings.getGasPrice();
		BigInteger ETHGasFees = gas.multiply(gasPrice);
		if(MotherWallet.transferETH(address, ETHGasFees)) {
			EthSendTransaction tr = manager.sendTransaction(gasPrice, gas, Settings.marketplaceAddress, encodedFunction, BigInteger.valueOf(0));

			if(tr.getError() != null) {
				return Response.failure(tr.getError().toString());
			}
			else {
				return Response.success(tr.getTransactionHash());
			}
		}
		else {
			return Response.failure("Failed to transfer ETH to wallet");
		}

	}

	public Response takeSellOffer(Offer offer) throws Exception {
		RawTransactionManager manager = new RawTransactionManager(Settings.web3j, Credentials.create(getKeyPair()), Settings.chainId);
		List<Type> inputParameters = new ArrayList<>();
		inputParameters.add(new Address(offer.collectionAddress));
		inputParameters.add(new Uint256(offer.nftId));
		inputParameters.add(new Address(offer.tokenAddress));
		inputParameters.add(new Uint256(offer.tokenAmount));
		inputParameters.add(new Uint256(offer.deadline));
		inputParameters.add(new DynamicBytes(offer.offerorsSignature));

		Function function = new Function(
				"takeSellOffer",  // function we're calling
				inputParameters,   // Parameters to pass as Solidity Types
				Arrays.asList());
		String encodedFunction = FunctionEncoder.encode(function);

		BigInteger gas = getSellOfferGasEstimate(offer);
		BigInteger gasPrice = Settings.getGasPrice();
		BigInteger ETHGasFees = gas.multiply(gasPrice);
		if(MotherWallet.transferETH(address, ETHGasFees)) {
			EthSendTransaction tr = manager.sendTransaction(gasPrice, gas, Settings.marketplaceAddress, encodedFunction, BigInteger.valueOf(0));

			if(tr.getError() != null) {
				return Response.failure(tr.getError().toString());
			}
			else {
				return Response.success(tr.getTransactionHash());
			}
		}
		else {
			return Response.failure("Failed to transfer ETH to wallet");
		}

	}

	public Response transferERC20(Web3j web3j, long chainId, String token, String to, BigInteger amount, BigInteger gas, BigInteger gasPrice) throws Exception {
		RawTransactionManager manager = new RawTransactionManager(web3j, Credentials.create(getKeyPair()), chainId);
		
		Function function = new Function(
	            "transfer",  // function we're calling
	            Arrays.asList(new Address(to), new Uint256(amount)),   // Parameters to pass as Solidity Types
	            Arrays.asList());
	    String encodedFunction = FunctionEncoder.encode(function);
		
	    EthSendTransaction tr = manager.sendTransaction(gasPrice, gas, token, encodedFunction, BigInteger.valueOf(0));
	    
	    if(tr.getError() != null) {
	    	return Response.failure(tr.getError().toString());
		}

	    return Response.success(tr.getTransactionHash());
	}
	
	public Response transferNFT(Web3j web3j, long chainId, String tokenAddress, String to, BigInteger tokenId, BigInteger gas, BigInteger gasPrice) throws Exception {
	    RawTransactionManager manager = new RawTransactionManager(web3j, Credentials.create(getKeyPair()), chainId);

	    Function function = new Function(
	        "safeTransferFrom",
	        Arrays.asList(new Address(address), new Address(to), new Uint256(tokenId)),
	        Arrays.asList()
	    );
	    String encodedFunction = FunctionEncoder.encode(function);

	    EthSendTransaction tr = manager.sendTransaction(gasPrice, gas, tokenAddress, encodedFunction, BigInteger.valueOf(0));
	    if(tr.getError() != null) {
	        return Response.failure(tr.getError().getMessage());
	    }

	    return Response.success(tr.getTransactionHash());
	}
	
	//Getters=======================================================================================================================
	public BigInteger getERC20TransferGasEstimate(Web3j web3j, long chainId, String token, String to, BigInteger amount) throws Exception {
	    RawTransactionManager manager = new RawTransactionManager(web3j, Credentials.create(getKeyPair()), chainId);
	    
	    Function function = new Function(
	            "transfer",
	            Arrays.asList(new Address(to), new Uint256(amount)),
	            Arrays.asList());
	    String encodedFunction = FunctionEncoder.encode(function);

	    return web3j.ethEstimateGas(
	            Transaction.createEthCallTransaction(manager.getFromAddress(), token, encodedFunction))
	            .send()
	            .getAmountUsed();
	}

	public BigInteger getNFTTransferGasEstimate(Web3j web3j, long chainId, String tokenAddress, String to, BigInteger tokenId) throws Exception {
	    RawTransactionManager manager = new RawTransactionManager(web3j, Credentials.create(getKeyPair()), chainId);

	    Function function = new Function(
	        "safeTransferFrom",
	        Arrays.asList(new Address(manager.getFromAddress()), new Address(to), new Uint256(tokenId)),
	        Arrays.asList()
	    );
	    String encodedFunction = FunctionEncoder.encode(function);

	    return web3j.ethEstimateGas(
	            Transaction.createEthCallTransaction(manager.getFromAddress(), tokenAddress, encodedFunction))
	            .send()
	            .getAmountUsed();
	}

	public BigInteger getBuyOfferGasEstimate(Offer offer) throws Exception {
		RawTransactionManager manager = new RawTransactionManager(Settings.web3j, Credentials.create(getKeyPair()), Settings.chainId);
		List<Type> inputParameters = new ArrayList<>();
		inputParameters.add(new Address(offer.collectionAddress));
		inputParameters.add(new Uint256(offer.nftId));
		inputParameters.add(new Address(offer.tokenAddress));
		inputParameters.add(new Uint256(offer.tokenAmount));
		inputParameters.add(new Uint256(offer.deadline));
		inputParameters.add(new DynamicBytes(offer.offerorsSignature));

		Function function = new Function(
				"takeBuyOffer",  // function we're calling
				inputParameters,   // Parameters to pass as Solidity Types
				Arrays.asList());
		String encodedFunction = FunctionEncoder.encode(function);

		return Settings.web3j.ethEstimateGas(
				Transaction.createEthCallTransaction(manager.getFromAddress(), Settings.marketplaceAddress, encodedFunction))
				.send()
				.getAmountUsed();
	}

	public BigInteger getSellOfferGasEstimate(Offer offer) throws Exception {
		RawTransactionManager manager = new RawTransactionManager(Settings.web3j, Credentials.create(getKeyPair()), Settings.chainId);
		List<Type> inputParameters = new ArrayList<>();
		inputParameters.add(new Address(offer.collectionAddress));
		inputParameters.add(new Uint256(offer.nftId));
		inputParameters.add(new Address(offer.tokenAddress));
		inputParameters.add(new Uint256(offer.tokenAmount));
		inputParameters.add(new Uint256(offer.deadline));
		inputParameters.add(new DynamicBytes(offer.offerorsSignature));

		Function function = new Function(
				"takeSellOffer",  // function we're calling
				inputParameters,   // Parameters to pass as Solidity Types
				Arrays.asList());
		String encodedFunction = FunctionEncoder.encode(function);

		return Settings.web3j.ethEstimateGas(
						Transaction.createEthCallTransaction(manager.getFromAddress(), Settings.marketplaceAddress, encodedFunction))
				.send()
				.getAmountUsed();
	}


	//Internal Functions============================================================================================================
	
	private ECKeyPair getKeyPair() throws Exception {
		byte[] encryptedKey = load("key");
		byte[] key = AES256.decrypt(encryptedKey, address + Settings.haha);
		
		return ECKeyPair.create(new BigInteger(key));
	}
}
