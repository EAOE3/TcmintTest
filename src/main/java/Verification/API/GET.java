package Verification.API;

import static spark.Spark.get;


import TicketingEvent.Blockchain.NFT.Ticket;
import Marketplace.Offer.Offers;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import org.json.JSONObject;

import Main.Sha3;

public class GET {

	public static void run() {

		get("/test", (request, response) -> {
			JSONObject test1 = new JSONObject();
			test1.put("test", "ok");
			return test1;
		});

		// GET https://endpoint/ticketEntryMessage/?contractAddress=<string>&nftId=<int>
		get("/ticketEntryMessage/", (request, response) -> {
			try {
				response.header("Content-Type", "application/json");

				String contractAddress = request.queryParams("contractAddress").toLowerCase();
				Integer nftId = Integer.parseInt(request.queryParams("nftId"));

				NFT n = NFTs.getNFTByCollectionAddressAndNftId(contractAddress, nftId);
				if(n == null || !(n instanceof  Ticket)) return getFail("message", "Invalid Contract Address Or Nft Id");

				Ticket ticket = (Ticket) n;

				String randomHash = ticket.getRandomHash();
				if(ticket.getRandomHash() == null) {
					randomHash = Sha3.getRandomHash();
					ticket.setRandomHash(randomHash);
				}

				String message = "Check in to event " + contractAddress + " with nft " + nftId + " and random " + randomHash;

				Offers.removeAllOffersOfAnNft(contractAddress, nftId);

				return getSuccess("message", message);
			} catch (Exception e) {
				return getError(e.getLocalizedMessage());
			}
		});

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
}
