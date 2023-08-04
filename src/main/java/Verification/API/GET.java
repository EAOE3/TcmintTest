package Verification.API;

import static spark.Spark.get;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Collection.Ticket;
import Collection.Tickets;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Main.Sha3;

public class GET {

	public static void run() {

		// GET https://endpoint/ticketEntryMessage/?contractAddress=<string>&nftId=<int>
		get("/ticketEntryMessage/", (request, response) -> {
			try {
				response.header("Content-Type", "application/json");

				String contractAddress = request.queryParams("contractAddress");
				Integer nftId = Integer.parseInt(request.queryParams("nftId"));

				Ticket ticket = Tickets.getTicketByCollectionAddressAndNftId(contractAddress, nftId);
				if(ticket == null) {
					return getFail("message", "Invalid Contract Address Or Nft Id");
				}

				String randomHash = Sha3.getRandomHash();
				String message = "Check in to event " + contractAddress + " with nft " + nftId.toString() + " and random " + randomHash;
				ticket.setRandomHash(randomHash);

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
