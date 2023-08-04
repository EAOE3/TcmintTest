package Verification.API;

import static spark.Spark.post;

import Collection.Ticket;
import Collection.Tickets;
import Marketplace.Offer.Offers;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Verification.SmartContract.SignatureVerification;

public class POST {

	
	public static void run() {
		
		// GET https://endpoint/ticketEntryMessage/?contractAddress=<string>&nftId=<int>
		//POST https://endpoint/verifyEntry/?contractAddress=<string>&nftId=<int>&signature
		post("/verifyEntry/", (request, response) ->
		{
			response.header("Content-Type", "application/json");
			String contractAddress = request.queryParams("contractAddress"); //Provided by the guards phone
			Integer nftId = Integer.parseInt(request.queryParams("nftId")); //Provided from the users qr code
			String signature = request.queryParams("signature"); //Provided from the users qr code

			Ticket ticket = Tickets.getTicketByCollectionAddressAndNftId(contractAddress, nftId);
			if(ticket == null) {
				return getFail("message", "Invalid Contract Address Or Nft Id");
			}
			if(ticket.getUsed()) {
				return getFail("message", "This ticket has been used");
			}

			String message = "Check in to event " + contractAddress + " with nft " + nftId.toString() + " and random " + ticket.getRandomHash();

			String signer = SignatureVerification.getSigner(message, signature.substring(2));

			if(signer.equalsIgnoreCase(ticket.getOwner())) {
				//TODO Add the POST call to inform nasru's backend that this ticket is used
				ticket.setUsed(true);
				Offers.removeAllOffersOfAnNft(contractAddress, nftId);
				return getSuccess("message", "Ticket Checked In");
			}
			else {
				return getFail("message", "Invalid Signer");
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

	
	
}
