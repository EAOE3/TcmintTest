package Collection;

import java.util.HashMap;
import java.util.Map;

public class Tickets {

	private static Map<String /*contractaddress-id*/, Ticket> ticketsById = new HashMap<>();
	
	public static void add(Ticket t) {
		ticketsById.put(t.id, t);
	}
	
	public static Ticket getTicketByDatabaseId(String id) {
		return ticketsById.get(id);
	}

	public static Ticket getTicketByCollectionAddressAndNftId(String collectionAddress, int nftId) {
		return ticketsById.get(collectionAddress + "-" + nftId);
	}
}
