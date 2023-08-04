package Collection;

import java.util.HashMap;
import java.util.Map;

public class TicketTypes {

	private static Map<String /*id*/, TicketType> ticketTypeById = new HashMap<>();
	
	public static void add(TicketType t) {
		ticketTypeById.put(t.id, t);
	}
	
	public static TicketType getTicketTypeByDatabaseId(String id) {
		return ticketTypeById.get(id);
	}
}
