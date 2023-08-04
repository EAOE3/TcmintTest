package Collection;

import java.math.BigInteger;

import Database.DBM;

public class TicketType extends DBM {

	private int availableSpaces;

    public final String ticketTypeId;
	public final BigInteger price;
	public final boolean seated;
	
    public TicketType(String collectionAddress, String ticketTypeId, int availableSpaces, BigInteger price, boolean seated) {
    	super(collectionAddress + "-" + ticketTypeId);
    	
        this.availableSpaces = availableSpaces;
        this.ticketTypeId = ticketTypeId;
        this.price = price;
        this.seated = seated;
        
        store("availableSpaces", availableSpaces);
        store("price", price);
        store("seated", seated);
        
        TicketTypes.add(this);
    }
    
    public TicketType(String databaseId) {
    	super(databaseId);
    	
        availableSpaces = loadInt("availableSpaces");
        ticketTypeId = loadString("ticketTypeId");
        price = loadBigInt("price");
        seated = loadBoolean("seated");
        
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

    public String getTicketTypeId() {
    	return ticketTypeId;
    }
    public int getAvailableSpaces() {
        return availableSpaces;
    }

    public boolean isSeated() {
        return seated;
    }
    


}
