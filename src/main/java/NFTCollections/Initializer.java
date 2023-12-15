package NFTCollections;

import Database.DBM;

public class Initializer {

    public static void init() {
        try {
            DBM.loadAllObjectsFromDatabase(NFTCollections.Collection.Collection.class);
            DBM.loadAllObjectsFromDatabase(NFTCollections.NFT.NFT.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
