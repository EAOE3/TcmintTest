package NFTCollections.Collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collections {

    private static Map<String /*Collection Address*/, Collection> collections = new HashMap<>();

    public static void add(Collection c) {
        collections.put(c.getCollectionAddress(), c);
    }

    public static Collection getCollectionByAddress(String collectionAddress) {
        return collections.get(collectionAddress);
    }

    public static List<Collection> getAllCollections() {
        return new ArrayList<>(collections.values());
    }

    public static List<String> getAllCollectionsAddresses() {
        return new ArrayList<>(collections.keySet());
    }
}
