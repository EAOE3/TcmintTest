package TicketingEvent.Blockchain.TicketType;


import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import Database.DBM;
import NFTCollections.Collection.Collections;

import java.math.BigInteger;

public class TicketType extends DBM {

	private int availableSpaces;

    public final String ticketTypeId;
	public BigInteger price;
    private BigInteger secondaryMarketPriceCap;
	public boolean seated;
	
    public TicketType(String collectionAddress, String ticketTypeId, int availableSpaces, BigInteger price, BigInteger secondaryMarketPriceCap, boolean seated) {
    	super(collectionAddress + "-" + ticketTypeId, false, false);
    	
        this.availableSpaces = availableSpaces;
        this.ticketTypeId = ticketTypeId;
        this.price = price;
        this.secondaryMarketPriceCap = secondaryMarketPriceCap;
        this.seated = seated;
        
        store("availableSpaces", availableSpaces);
        store("price", price);
        store("secondaryMarketPriceCap", secondaryMarketPriceCap);
        store("seated", seated);

        EventNFTCollection collection = (EventNFTCollection) Collections.getCollectionByAddress(collectionAddress);
        collection.addTicketType(this);

        TicketTypes.add(this);
    }
    
    public TicketType(String databaseId) {
    	super(databaseId, false, false);
    	
        availableSpaces = loadInt("availableSpaces");
        ticketTypeId = loadString("ticketTypeId");
        price = loadBigInt("price");
        secondaryMarketPriceCap = loadBigInt("secondaryMarketPriceCap");
        seated = loadBoolean("seated");

        String collectionAddress = databaseId.substring(0, databaseId.indexOf("-"));
        EventNFTCollection collection = (EventNFTCollection) Collections.getCollectionByAddress(collectionAddress);
        collection.addTicketType(this);

        TicketTypes.add(this);
    }
	
    public boolean decrementAvailableSpaces() {
    	if(availableSpaces == 0) return false;
    	
    	--availableSpaces;
    	return true;
    }
    
    public void setAvailableSpaces(int availableSpaces) {

        this.availableSpaces = availableSpaces;
    }

    public void setSeated(boolean seated) {
        this.seated = seated;
    	store("seated", seated);
    }

    public void setPrice(BigInteger price) {
        this.price = price;
        store("price", price);
    }

    public void setSecondaryMarketPriceCap(BigInteger secondaryMarketPriceCap) {
        this.secondaryMarketPriceCap = secondaryMarketPriceCap;
        store("secondaryMarketPriceCap", secondaryMarketPriceCap);
    }

    public String getTicketTypeId() {
    	return ticketTypeId;
    }
    public int getAvailableSpaces() {
        return availableSpaces;
    }

    public BigInteger getPrice() {
    	return price;
    }

    public BigInteger getSecondaryMarketPriceCap() {
    	return secondaryMarketPriceCap;
    }

    public boolean isSeated() {
        return seated;
    }
    


}
