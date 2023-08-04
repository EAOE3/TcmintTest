package Collection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Database.DBM;

public class EventNFTCollection extends DBM {
	public final String address;
	
	private List<Ticket> tickets = new ArrayList<>();
	private List<TicketType> ticketTypes = new ArrayList<>();
	private HashMap<String /*Address*/, Integer> balances = new HashMap<>();

    
	public EventNFTCollection(String eventId, String address, List<String> ticketTypeIds) {
		super(eventId);

		System.out.println("eventId: " + eventId);

		this.address = address;	
		for(String ticketTypeId: ticketTypeIds) {
			ticketTypes.add(TicketTypes.getTicketTypeByDatabaseId(address + "-" + ticketTypeId));
		}
		
		store("address", address);
		for(String ticketTypeId: ticketTypeIds) {
			store("ticketTypes/" + ticketTypeId, "");
		}
		
		EventNFTCollections.add(this);
	}
	
	public EventNFTCollection(String eventId) {
		super(eventId);
		File ticketTypesFolder = new File(rootPath + "ticketTypes");
		
		address = loadString("address");	
		String[] ticketTypeIds = ticketTypesFolder.list();
		
		int nftId = 1;
		while(true) {
			Ticket ticket = Tickets.getTicketByDatabaseId(address + "-" + nftId);
			if(ticket == null) break;
			
			tickets.add(ticket);
			incrementBalance(ticket.getOwner());
			nftId++;
		}
		
		for(String ticketTypeId: ticketTypeIds) {
			ticketTypes.add(TicketTypes.getTicketTypeByDatabaseId(address + "-" + ticketTypeId));
		}
		
		EventNFTCollections.add(this);
	}
	
    // setters
	public void transferNFT(int nftId, String to) {
		Ticket ticket = Tickets.getTicketByDatabaseId(address + "-" + id);
		if(ticket == null) return;
		
		decrementBalance(ticket.getOwner());
		incrementBalance(to);
		ticket.setOwner(to);
	}
	
    public void addTicket(Ticket t) {
    	if(t == null) return;
    	
    	tickets.add(t);
    	incrementBalance(t.getOwner());
    }
    
    public void incrementBalance(String owner) {
    	Integer balance = balances.get(owner);
    	
    	if(balance == null) {
    		balances.put(owner, 1);
    	}
    	else {
    		balances.put(owner, balance + 1);
    	}
    }
    
    public void decrementBalance(String owner) {
    	Integer balance = balances.get(owner);
    	
    	if(balance != null) {
    		balances.put(owner, balance - 1);
    	}
    }
    
    //Getters ============================================================================================================
    public List<TicketType> getAllTicketTypes() {
    	return ticketTypes;
    }
    
    public List<Ticket> getAllTickets() {
    	return tickets;
    }
    public Integer getBalance(String address) {
    	if(balances.get(address) == null) {
    		return 0;
    	}
    	else {
    		return balances.get(address);
    	}
    }

}
