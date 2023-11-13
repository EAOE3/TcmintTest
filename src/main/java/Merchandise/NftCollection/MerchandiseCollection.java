package Merchandise.NftCollection;

import NFTCollections.Collection.Collection;

import java.math.BigInteger;

public class MerchandiseCollection extends Collection {

//    uint256 private _maxSupply; //Limit of mechandise that can be sold
//    uint256 private _price;
//    uint256 private _saleStartTime;
//    uint256 private _saleEndTime;

    private int maxSupply;
    private BigInteger price;
    private long saleStartTime;
    private long saleEndTime;

    private int sold = 0;

    public MerchandiseCollection(String contractAddress, int maxSupply, BigInteger price, long saleStartTime, long saleEndTime) {
        super(contractAddress);

        this.maxSupply = maxSupply;
        this.price = price;
        this.saleStartTime = saleStartTime;
        this.saleEndTime = saleEndTime;

        store("maxSupply", maxSupply);
        store("price", price);
        store("saleStartTime", saleStartTime);
        store("saleEndTime", saleEndTime);
    }
    public MerchandiseCollection(String contractAddress) {
        super(contractAddress);

        this.maxSupply = loadInt("maxSupply");
        this.price = loadBigInt("price");
        this.saleStartTime = loadLong("saleStartTime");
        this.saleEndTime = loadLong("saleEndTime");
    }

    //Setters ==========================================================================================================

    public void setMaxSupply(int maxSupply) {
        this.maxSupply = maxSupply;
        store("maxSupply", maxSupply);
    }

    public void setPrice(BigInteger price) {
        this.price = price;
        store("price", price);
    }

    public void setSaleStartTime(long saleStartTime) {
        this.saleStartTime = saleStartTime;
        store("saleStartTime", saleStartTime);
    }

    public void setSaleEndTime(long saleEndTime) {
        this.saleEndTime = saleEndTime;
        store("saleEndTime", saleEndTime);
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    //Getters ==========================================================================================================

    public int getMaxSupply() {
        return maxSupply;
    }

    public BigInteger getPrice() {
        return price;
    }

    public long getSaleStartTime() {
        return saleStartTime;
    }

    public long getSaleEndTime() {
        return saleEndTime;
    }

    public int getSold() {
        return sold;
    }
}
