package Collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventNFTCollections {

	private static Map<String /*Event Id*/, EventNFTCollection> EventById = new HashMap<>();
	private static Map<String /*Address*/, EventNFTCollection> EventByAddress = new HashMap<>();
	
	public static void add(EventNFTCollection t) {
		EventById.put(t.id, t);
		EventByAddress.put(t.address, t);
	}
	
	public static EventNFTCollection getEventById(String eventId) {
		return EventById.get(eventId);
	}
	
	public static EventNFTCollection getEventByAddress(String address) {
		return EventByAddress.get(address);
	}
	
	public static Collection<EventNFTCollection> getAllEvents() {
		return EventById.values();
	}
}
