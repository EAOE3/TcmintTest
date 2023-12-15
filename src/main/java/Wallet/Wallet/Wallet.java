package Wallet.Wallet;

import java.math.BigInteger;

import Main.OWeb3j;
import Main.Signature;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import NFTCollections.NFT.NFTs;
import TicmintToken.Token.TMT;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
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
		super(email, true, true);
		
		String address = loadString("address");
		if(address == null) {
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			address = Keys.toChecksumAddress(Credentials.create(ecKeyPair).getAddress()).toLowerCase();
			
			//Extra encryption for the key (Other than the one that the database already offers)
			byte[] key = AES256.encrypt(ecKeyPair.getPrivateKey().toByteArray(), address + Settings.haha);
			
			store("key", key);
			store("address", address);
		}
		
		this.address = address;
		Wallets.add(this);
	}
	
	//Setters==============================================================================================================

	public byte[] personalSignMessage(String message) {

		try {
			// sign the message
			Sign.SignatureData signatureData = Sign.signPrefixedMessage(message.getBytes(), getKeyPair());

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

	public byte[] signMessage(String message) {

		try {
			// sign the message
			Sign.SignatureData signatureData = Sign.signMessage(message.getBytes(), getKeyPair());

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
		if(!NFTs.getNFTByCollectionAddressAndNftId(offer.collectionAddress, offer.nftId).getHexOwner().equalsIgnoreCase(address)) {
			return Response.failure("You're Not Owner Of The NFT");
		}

		return OWeb3j.fuelAndSendScTxn(Settings.web3j, getCredentials(), Settings.chainId, address, Settings.marketplaceAddress, Settings.getGasPrice(), BigInteger.ZERO, "takeBuyOffer", new Address(offer.collectionAddress), new Uint256(offer.nftId), new Address(address), new Uint256(offer.tokenAmount), new Uint256(offer.deadline), new DynamicBytes(offer.offerorsSignature));
	}

	public Response takeSellOffer(Offer offer) throws Exception {
		if(TMT.getUserBalance(address).compareTo(offer.tokenAmount) == -1) {
			return Response.failure("Insufficient TMT Balance");
		}

		return OWeb3j.fuelAndSendScTxn(Settings.web3j, getCredentials(), Settings.chainId, address, Settings.marketplaceAddress, Settings.getGasPrice(), BigInteger.ZERO, "takeSellOffer", new Address(offer.collectionAddress), new Uint256(offer.nftId), new Uint256(offer.tokenAmount), new Uint256(offer.deadline), new DynamicBytes(offer.offerorsSignature));
	}

	//buy string[] calldata ticketTypes, uint256[] calldata amounts, string[] calldata _seatNumbers
	//public static Response sendScTxn(Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger value, String functionName, Object... inputParams)
	public Response buyTickets(String collectionContract, List<String> tikcetType, List<BigInteger> amount, List<String> seatNumber) throws Exception {
		List<Utf8String> ticketTypeUtf8String = new ArrayList<>();
		List<Uint256> amountUint256 = new ArrayList<>();
		List<Utf8String> seatNumberUtf8String = new ArrayList<>();

		for(String s: tikcetType) {
			ticketTypeUtf8String.add(new Utf8String(s));
		}

		for(BigInteger i: amount) {
			amountUint256.add(new Uint256(i));
		}

		for(String s: seatNumber) {
			seatNumberUtf8String.add(new Utf8String(s));
		}

		return OWeb3j.fuelAndSendScTxn(Settings.web3j, getCredentials(), Settings.chainId, address, collectionContract, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(address), new DynamicArray(ticketTypeUtf8String), new DynamicArray(amountUint256), new DynamicArray(seatNumberUtf8String));
	}

	//buy(address to, uint256 amount)
	public Response buyMerchandise(String merchandiseContract, int amount) throws Exception {
		return OWeb3j.fuelAndSendScTxn(Settings.web3j, getCredentials(), Settings.chainId, address, merchandiseContract, Settings.getGasPrice(), BigInteger.ZERO, "buy", new Address(address), new Uint256(amount));
	}

	//Internal Functions============================================================================================================
	
	private ECKeyPair getKeyPair() throws Exception {
		byte[] encryptedKey = load("key");
		byte[] key = AES256.decrypt(encryptedKey, address + Settings.haha);
		
		return ECKeyPair.create(new BigInteger(key));
	}

	public Credentials getCredentials() throws Exception {
		return Credentials.create(getKeyPair());
	}

}
