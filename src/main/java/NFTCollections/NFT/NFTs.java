package NFTCollections.NFT;

import java.util.*;

public class NFTs {

    private static Map<String /*Collection Address*/, Map<Integer, NFT>> nftByCollectionAddressAndNFtId = new HashMap<>();

    public static void add(String collectionAddress, NFT nft) {
        Map<Integer, NFT> nfts = nftByCollectionAddressAndNFtId.get(collectionAddress);
        if(nfts == null) nfts = new HashMap<>();
        nfts.put(nft.nftId, nft);
        nftByCollectionAddressAndNFtId.put(collectionAddress, nfts);
    }

    public static NFT getNFTByCollectionAddressAndNftId(String collectionAddress, int nftId) {
        Map<Integer, NFT> nfts = nftByCollectionAddressAndNFtId.get(collectionAddress);
        if(nfts == null) return null;
        return nfts.get(nftId);
    }

    public static List<NFT> getAllNftsInACollection(String collectionAddress) {
        Map<Integer, NFT> nftById = nftByCollectionAddressAndNFtId.get(collectionAddress);
        if(nftById == null) return new ArrayList<>();
        else return new ArrayList<>(nftById.values());
    }


}
