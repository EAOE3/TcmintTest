package TicketingEvent;

import Database.DBM;
import TicketingEvent.API.GET;
import TicketingEvent.API.POST;
import TicketingEvent.API.PUT;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.TicketType.TicketType;

public class Initializer {

    public static void init() {
        GET.run();
        POST.run();
        PUT.run();

        try {
            DBM.loadAllObjectsFromDatabase(EventNFTCollection.class);
            DBM.loadAllObjectsFromDatabase(Ticket.class);
            DBM.loadAllObjectsFromDatabase(TicketType.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
