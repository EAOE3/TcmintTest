package Merchandise;

import Database.DBM;
import Merchandise.API.POST;
import Merchandise.API.PUT;
import Merchandise.NftCollection.MerchandiseCollection;

public class Initializer {

    public static void init() {
        POST.run();
        PUT.run();

        try { DBM.loadAllObjectsFromDatabase(MerchandiseCollection.class); } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
