package REST_API;

import static spark.Spark.get;

import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Collection.Ticket;
import Collection.TicketType;

public class GET {
	
	public static void run() {
		
		//600: Invalid Event ID
		//601: Invalid Address
		
		// GET https://endpoint/verified/?email=<userEmail>
		get("/test", (request, response) -> {
			boolean[] test = new boolean[25];

			JSONObject test1 = new JSONObject();
			test1.put("test", "ok");
			test1.put("thearray", test);

			response.header("Content-Type", "application/json");
			return test1;

		});

		// GET https://endpoint/eventInfo/?eventId=<String>
		get("/eventInfo/", (request, response) -> {
			try {
				response.header("Content-Type", "application/json");
				
				String eventId = request.queryParams("eventId");
				
				EventNFTCollection event = Collection.EventNFTCollections.getEventById(eventId);
				if(event == null) return getError("Invalid Event ID");
				
				JSONArray tickets = new JSONArray();
				for(TicketType t: event.getAllTicketTypes()) {
					JSONObject ticketTypeJson = new JSONObject();
					
					boolean seated = t.isSeated();
					
					ticketTypeJson.put("ticketType", t.getTicketTypeId());					
					ticketTypeJson.put("seated", seated);
					
					tickets.put(ticketTypeJson);
				}

				return getSuccess("contractAddress", event.address, "tickets", tickets);
			} catch (Exception e) {
				e.printStackTrace();
				return getError(e.getLocalizedMessage());
			}
		});
		
		// GET https://endpoint/userPortfolio/?userAddress=<string>
		get("/userPortfolio/", (request, response) -> {
			try {
				response.header("Content-Type", "application/json");
				
				String userAddress = request.queryParams("userAddress").toLowerCase();
				if(!isValidEthereumAddress(userAddress)) return getError("Invalid Address");
				
				JSONArray tickets = new JSONArray();
				for(EventNFTCollection event: EventNFTCollections.getAllEvents()) {
					if(event.getBalance(userAddress) == 0) continue;
					
					for(Ticket t: event.getAllTickets()) {
						if(t.getOwner().equalsIgnoreCase(userAddress)) {
							JSONObject ticket = new JSONObject();
							TicketType tt = Collection.TicketTypes.getTicketTypeByDatabaseId(event.address + "-" + t.ticketType);
							
							ticket.put("contractAddress", event.address);
							ticket.put("nftId", t.getNftId());
							ticket.put("ticketType", t.ticketType);
							ticket.put("seated", tt.seated);
							ticket.put("seat", t.seat);
							
							tickets.put(ticket);
						}
					}
				}

				return getSuccess("tickets", tickets);

			} catch (Exception e) {
				return getError(e.getLocalizedMessage());
			}
		});

	}
	
    public static boolean isValidEthereumAddress(String address) {
        String regex = "^0x[0-9a-fA-F]{40}$";
        
        // Create a pattern object
        Pattern pattern = Pattern.compile(regex);
        
        // Match the address against the pattern
        return pattern.matcher(address).matches();
    }
    
	//There was a problem with the data submitted, or some pre-condition of the API call wasn't satisfied
	public static JSONObject getFail() {
		JSONObject object = new JSONObject();

		object.put("status", "fail");

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
