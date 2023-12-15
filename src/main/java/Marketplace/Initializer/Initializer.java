package Marketplace.Initializer;

import Database.DBM;
import Marketplace.API.DELETE;
import Marketplace.API.GET;
import Marketplace.API.POST;
import Marketplace.Offer.Offer;

public class Initializer {

    public static void initialize() {
        try {
            DBM.loadAllObjectsFromDatabase(Offer.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load offers from database");
        }

        GET.run();
        POST.run();
        DELETE.run();

        Marketplace.Webhook.POST.run();
    }
}
