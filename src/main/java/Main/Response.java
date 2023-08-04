package Main;

public class Response {

	public final boolean success;
	public final String message;
	
	public Response(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "{seccuess: " + success + ", " + message + "}";
	}
	
	public static Response success(String message) {
		return new Response(true, message);
	}
	
	public static Response failure(String message) {
		return new Response(false, message);
	}
	
}
