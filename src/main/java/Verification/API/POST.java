package Verification.API;

import static spark.Spark.post;


import TicketingEvent.Blockchain.NFT.Ticket;
import Main.ByteArrayWrapper;
import Main.Signature;
import Marketplace.Offer.Offers;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class POST {

	
	public static void run() {
		
		// GET https://endpoint/ticketEntryMessage/?contractAddress=<string>&nftId=<int>
		//POST https://endpoint/verifyEntry/?contractAddress=<string>&nftId=<int>&signature
		post("/verifyEntry/", (request, response) ->
		{
			try {
				response.header("Content-Type", "application/json");
				String contractAddress = request.queryParams("contractAddress").toLowerCase(); //Provided by the guards phone
				Integer nftId = Integer.parseInt(request.queryParams("nftId")); //Provided from the users qr code
				String signature = request.queryParams("signature"); //Provided from the users qr code

				//NFT n = NFTs.getNFTByCollectionAddressAndNftId(contractAddress, nftId);
				NFT n = NFTs.getNFTByCollectionAddressAndNftId(contractAddress, nftId);
				if(n == null || !(n instanceof  Ticket)) return getFail("message", "Invalid Contract Address Or Nft Id");

				Ticket ticket = (Ticket) n;
				if (ticket.isUsed()) {
					return getFail("message", "This ticket has been used");
				}

				String message = "Check in to event " + contractAddress + " with nft " + nftId + " and random " + ticket.getRandomHash();

				ByteArrayWrapper signer = new ByteArrayWrapper(Hex.decode(Signature.recoverSigner(message, signature).substring(2)));

				if (signer.equals(ticket.getOwner())) {
					//TODO Add the POST call to inform nasru's backend that this ticket is used
					ticket.setUsed(true);
					Offers.removeAllOffersOfAnNft(contractAddress, nftId);
					notifyServerOfTicketEntry(contractAddress, nftId);
					return getSuccess("message", "Ticket Checked In");
				} else {
					return getFail("message", "Invalid Signer", "Agreement", message, "owner", "0x" + Hex.toHexString(ticket.getOwner().data()), "signer", "0x" + Hex.toHexString(signer.data()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return getError(e.getLocalizedMessage());
			}

		} );

	}
	
	// There was a problem with the data submitted, or some pre-condition of the API
	// call wasn't satisfied
	public static JSONObject getFail(Object... variables) throws Exception {
		JSONObject object = new JSONObject();
		JSONObject data = new JSONObject();

		int size = variables.length;
		if (size % 2 != 0)
			throw new Exception("Provided variables length should be even when using getSuccess");

		for (int t = 0; t < size; t += 2) {
			data.put(variables[t].toString(), variables[t + 1]);
		}

		object.put("status", "fail");
		object.put("data", data);

		return object;
	}

	// There was a problem on the server side
	public static JSONObject getError(String message) {
		JSONObject object = new JSONObject();

		object.put("status", "error");
		object.put("message", message);

		return object;
	}

	public static JSONObject getSuccess(Object... variables) throws Exception {
		JSONObject object = new JSONObject();
		JSONObject data = new JSONObject();

		int size = variables.length;
		if (size % 2 != 0)
			throw new Exception("Provided variables length should be even when using getSuccess");

		for (int t = 0; t < size; t += 2) {
			data.put(variables[t].toString(), variables[t + 1]);
		}

		object.put("status", "success");
		object.put("data", data);

		return object;
	}

	public static void notifyServerOfTicketEntry(String contractAddress, int nftId) {
		try {
			String url = "http://13.200.97.144/v1/events/updateNftTicketState?auth_token=erer";

			JSONObject body = new JSONObject();
			body.put("contractAddress", contractAddress);
			body.put("nftId", nftId);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.POST(HttpRequest.BodyPublishers.ofString(body.toString())).header("Content-Type", "application/json")
					.build();

			client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




}
