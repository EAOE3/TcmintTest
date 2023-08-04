package Collection;

import Database.DBM;

public class Ticket extends DBM {
	
    private String owner;
    private boolean used = false;
    private String randomHash = null;

    public final int nftId;
    public final String ticketType;
    public final String seat;

    public Ticket(String collectionAddress, int nftId, String owner, String ticketType, String seat) {
    	super(collectionAddress + "-" + nftId);
    	
        this.owner = owner;
        this.nftId = nftId;
        this.ticketType = ticketType;   
        this.seat = seat;
        
        store("owner", owner);
        store("used", used);
        store("randomHash", randomHash);
        store("ticketType", ticketType);
        store("seat", seat);
        
        Tickets.add(this);
    }
    
    public Ticket(String collectionAddressAndId) {
    	super(collectionAddressAndId);
    	
        owner = loadString("owner"); 
        used = loadBoolean("used");
        randomHash = loadString("randomHash");
        nftId = loadInt("nftId");
        ticketType = loadString("ticketType");   
        seat = loadString("seat");
        
        Tickets.add(this);
    }
    
    public void setOwner(String owner) {
    	this.owner = owner;
    }

    public void setRandomHash(String randomHash) {
        this.randomHash = randomHash;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getNftId() {
    	return nftId;
    } 
    
    public String getOwner() {
    	return owner;
    }
    
    public boolean getUsed() {
    	return used;
    }
    
    public String getRandomHash() {
    	if(randomHash == null) {
    		randomHash = Main.Sha3.getRandomHash();
    		store("randomHash", randomHash);
    	}
    	
    	return randomHash;
    }
}
