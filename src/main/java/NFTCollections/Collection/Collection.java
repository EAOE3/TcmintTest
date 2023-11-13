package NFTCollections.Collection;

import Database.DBM;
import Main.ByteArrayWrapper;
import Main.Utils;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import org.bouncycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collection extends DBM {

    private List<NFT> nfts = new ArrayList<>();
    private Map<ByteArrayWrapper, Integer> balances = new HashMap<>();

    public Collection(String collectionAddress) {
        super(collectionAddress.toLowerCase(), false, false);

        Collections.add(this);
    }

    public String getCollectionAddress() {
        return id;
    }

    //Setters ==========================================================================================================
    public void addNft(NFT nft) {
        nfts.add(nft);
        increaseOwnerBalance(nft.getOwner());
    }

    public void transfer(int nftId, ByteArrayWrapper to) {
        NFT nft = NFTs.getNFTByCollectionAddressAndNftId(getCollectionAddress(), nftId);

        if(nft.getOwner().equals(to)) return;

        decreaseOwnerBalance(nft.getOwner());
        increaseOwnerBalance(to);

        nft.setOwner(to);
    }

    //Getters ==========================================================================================================

    public int getBalance(ByteArrayWrapper user) {
        Integer balance = balances.get(user);
        if(balance == null) return 0;
        else return balance;
    }
    public int getBalance(byte[] user) {
        return getBalance(new ByteArrayWrapper(user));
    }

    public int getBalance(String user) {
        return getBalance(new ByteArrayWrapper(Hex.decode(user.substring(2))));
    }

    public List<NFT> getAllNFTs() {
        return NFTs.getAllNftsInACollection(getCollectionAddress());
    }

    //getNftCount
    public int getNftCount() {
        return nfts.size();
    }

    //Internals ========================================================================================================
    private void increaseOwnerBalance(ByteArrayWrapper owner) {
        Integer balance = balances.get(owner);
        if(balance == null) balance = 0;
        balances.put(owner, balance + 1);
    }

    private void decreaseOwnerBalance(ByteArrayWrapper owner) {
        Integer balance = balances.get(owner);
        if(balance == null) balance = 0;
        balances.put(owner, balance - 1);
    }
}
