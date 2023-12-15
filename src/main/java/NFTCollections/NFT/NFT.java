package NFTCollections.NFT;

import Database.DBM;
import Main.ByteArrayWrapper;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.time.Instant;

public class NFT extends DBM {

    public final int nftId;
    public final String collectionAddress;
    public final long mintingTime;
    public final BigInteger purchasePrice;
    private ByteArrayWrapper owner;
    public final String txnHash;

    //timeOfAcquiring;
    //This time doesn't have to be 100% accurate, it just has to show if the user got this ticket before other tickets he owns or after them to properly send them back in the portfolio call
    //So we set the time of aquiring based on the server time, and not the block time
    //The server synchronizes in a synchronus manner with the blockchain, so the time of aquiring will be suitable enough for our purposes

    public NFT(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash) {
        super(collectionAddress + "-" + nftId, false, false);

        this.collectionAddress = collectionAddress;
        this.nftId = nftId;
        this.mintingTime = mintingTime;
        this.purchasePrice = purchasePrice;
        this.owner = owner;
        this.txnHash = txnHash;

        store("collectionAddress", collectionAddress);
        store("nftId", nftId);
        store("mintingTime", mintingTime);
        store("purchasePrice", purchasePrice);
        store("owner", owner.data());
        store("txnHash", txnHash);
        store("timeOfAcquiring", Instant.now().getEpochSecond());

        Collection collection = Collections.getCollectionByAddress(collectionAddress);
        if (collection == null) collection.addNft(this);

        NFTs.add(collectionAddress, this);
    }

    //Loading Constructor
    public NFT(String collectionAddressAndId) {
        super(collectionAddressAndId, false, false);

        collectionAddress = loadString("collectionAddress");
        nftId = loadInt("nftId");
        mintingTime = loadLong("mintingTime");
        purchasePrice = loadBigInt("purchasePrice");
        owner = new ByteArrayWrapper(load("owner"));
        txnHash = loadString("txnHash");

        Collection collection = Collections.getCollectionByAddress(collectionAddress);
        if (collection == null) collection.addNft(this);

        NFTs.add(collectionAddress, this);
    }

    //Setters ==========================================================================================================

    public void setOwner(ByteArrayWrapper owner) {
        this.owner = owner;

        store("owner", owner.data());
        store("timeOfAcquiring", Instant.now().getEpochSecond());
    }

    public void setRefunded(boolean refunded) {
        store("refunded", refunded);
    }

    //Getters ==========================================================================================================

    public ByteArrayWrapper getOwner() {
        return owner;
    }

    public String getHexOwner() {
        return "0x" + Hex.toHexString(owner.data());
    }
    public BigInteger getPurchasePrice() {
        return purchasePrice;
    }

    public boolean isRefunded() {
        return loadBoolean("refunded");
    }

    public long getTimeOfAcquiring() {
        return loadLong("timeOfAcquiring");
    }

    public String getCollectionAddress() {
        return collectionAddress;
    }

}
