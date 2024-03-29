package TicmintToken.API;

import Main.Settings;
import TicmintToken.Token.TMT;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import static spark.Spark.get;

public class GET {

    public static void run() {

        get("/userBalance/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String userAddress = request.queryParams("userAddress").toLowerCase();

                return getSuccess("balance", Settings.tmt.getUserBalance(userAddress), "lockedBalance", Settings.tmt.getUserLockedBalance(userAddress));
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

}
