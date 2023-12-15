package TicketingEvent.Blockchain.NFTCollection;

import TicketingEvent.Blockchain.TicketType.TicketType;
import NFTCollections.Collection.Collection;
import NFTCollections.NFT.NFT;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import TicketingEvent.Blockchain.NFT.Ticket;

public class EventNFTCollection extends Collection {
	private long eventEndTime; //The time when the event ends
	private List<TicketType> ticketTypes = new ArrayList<>();

    
	public EventNFTCollection(String address, long eventEndTime) {
		super(address);

		this.eventEndTime = eventEndTime;

		store("eventEndTime", eventEndTime);
	}
	
	public EventNFTCollection(String address) {
		super(address);

		eventEndTime = loadLong("eventEndTime");
	}
	
    // setters

	public void addTicketType(TicketType ticketType) {
		ticketTypes.add(ticketType);
	}

	public void setEndTime(long endTime) {
		eventEndTime = endTime;
		store("eventEndTime", endTime);
	}

    //Getters ============================================================================================================
    public List<TicketType> getAllTicketTypes() {
    	return ticketTypes;
    }
    
    public List<Ticket> getAllTickets() {
		List<NFT> allNfts = getAllNFTs();
		List<Ticket> tickets = new ArrayList<>(allNfts.size());

		for(NFT n: allNfts) {
			tickets.add((Ticket) n);
		}

    	return tickets;
    }

	public boolean isEnded() {
		return Instant.now().getEpochSecond() > eventEndTime;
	}

	public long getEventEndTime() {
		return eventEndTime;
	}
}
