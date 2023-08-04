package Wallet.API;

import static spark.Spark.get;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import Wallet.Wallet.Wallet;
import Wallet.Wallet.Wallets;

public class GET {

	public static void run() {

		// GET https://endpoint/userAddress/?email=<String>
		get("/userAddress/", (request, response) -> {
			try {
				response.header("Content-Type", "application/json");

				String email = request.queryParams("email");
				
				if(!isValidEmail(email)) return getFail("Message", "Invalid Email");
				
				Wallet wallet = Wallets.getWalletByEmail(email);
				if(wallet == null) {
					wallet = new Wallet(email);
				}
				
				return getSuccess("address", wallet.address);
			} catch (Exception e) {
				e.printStackTrace();
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
	
	//Internal Functions=======================================================================================================================
	private static boolean isValidEmail(String email) {
	    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
	                        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

	    Pattern pattern = Pattern.compile(emailRegex);
	    Matcher matcher = pattern.matcher(email);
	    return matcher.matches();
	}

}
