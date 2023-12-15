package Marketplace.API;

import static spark.Spark.post;


import Marketplace.Webhook.Sender;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.TicketType.TicketType;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import Main.OWeb3j;
import Main.Response;
import Main.Settings;
import Main.Signature;
import Marketplace.Offer.Offer;
import Marketplace.Offer.Offers;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import Wallet.Wallet.Wallets;
import Wallet.Wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.time.Instant;

public class POST {


    public static void run() {

        //Write POST function for users to create an offer

        post("/createOffer/", (request, response) -> {
            try {
                // Extract the parameters from the request
                String buyOrSell = request.queryParams("buyOrSell").toLowerCase();
                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));
                byte[] offerorsSignature = Hex.decode(request.queryParams("offerorsSignature"));
                String signer;

                Collection c = Collections.getCollectionByAddress(collectionAddress);
                if(c == null) {
                    return getFail("message", "Collection not found");
                }

                NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);
                if(n == null) return getFail("message", "NFT not found");

                if(c instanceof EventNFTCollection) {
                    EventNFTCollection collection = (EventNFTCollection) c;

                    //If event hasn't ended, all revealed NFTs can't be traded
                    if(!collection.isEnded()) {
                        if(((Ticket) n).getRandomHash() != null) {
                            return getFail("message", "Ticket has already revealed the qr code for entry");
                        }
                    }

                    TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(collectionAddress, ((Ticket) n).getTicketType());

                    //If price is greater than allowed price before event end time
                    if(Instant.now().getEpochSecond() < collection.getEventEndTime()
                            && tokenAmount.compareTo(tt.getSecondaryMarketPriceCap()) == 1) {
                        return getFail("message", "Price exceeds the secondary market cap for this ticket type");
                    }

                }

                if(buyOrSell.equalsIgnoreCase("buy")) {
                    String agreement = Offers.getBuyOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                    signer = Signature.recoverPersonalSigner(agreement, Hex.toHexString(offerorsSignature));

                    //TODO check that signer has sufficient balance
                }
                else if(buyOrSell.equalsIgnoreCase("sell")) {
                    String agreement = Offers.getSellOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                    signer = Signature.recoverPersonalSigner(agreement, Hex.toHexString(offerorsSignature));

                    String ticketOwner = "0x" + Hex.toHexString(n.getOwner().data());
                    if(!signer.equalsIgnoreCase(ticketOwner)) {
                        return getFail("message", "Invalid Signer", "signer", signer, "ticketOwner", ticketOwner);
                    }
                }
                else {
                    return getFail("message", "Invalid buyOrSell parameter");
                }

                try {
                    // Create an instance of the Offer class
                    new Offer(buyOrSell, collectionAddress, nftId, tokenAmount, deadline, signer, offerorsSignature);
                } catch (Exception e) {
                    return getFail("message", e.getMessage());
                }

                return getSuccess("message", "Offer created successfully");
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/createAndSignOffer/", (request, response) -> {
            try {
                // Extract the parameters from the request
                String buyOrSell = request.queryParams("buyOrSell").toLowerCase();
                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                BigInteger tokenAmount = new BigInteger(request.queryParams("tokenAmount"));
                long deadline = Long.parseLong(request.queryParams("deadline"));
                String signer = request.queryParams("signer").toLowerCase();
                byte[] offerorsSignature;
                Wallet w = Wallets.getWalletByAddress(signer);

                if(w == null) {
                    return getFail("message", "Wallet not found");
                }

                Collection c = Collections.getCollectionByAddress(collectionAddress);
                if(c == null) {
                    return getFail("message", "Collection not found");
                }

                NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);
                if(n == null) return getFail("message", "NFT not found");

                if(c instanceof EventNFTCollection) {
                    EventNFTCollection collection = (EventNFTCollection) c;

                    //If event hasn't ended, all revealed NFTs can't be traded
                    if(!collection.isEnded()) {
                        if(((Ticket) n).getRandomHash() != null) {
                            return getFail("message", "Ticket has already revealed the qr code for entry");
                        }
                    }

                    TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(collectionAddress, ((Ticket) n).getTicketType());

                    //If price is greater than allowed price before event end time
                    if(Instant.now().getEpochSecond() < collection.getEventEndTime()
                            && tokenAmount.compareTo(tt.getSecondaryMarketPriceCap()) == 1) {
                        return getFail("message", "Price exceeds the secondary market cap for this ticket type");
                    }

                }

                String agreement;
                if(buyOrSell.equalsIgnoreCase("buy")) {
                    agreement = Offers.getBuyOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                    //TODO check that signer has sufficient balance
                }
                else if(buyOrSell.equalsIgnoreCase("sell")) {
                    agreement = Offers.getSellOfferAgreement(collectionAddress, nftId, tokenAmount, deadline);
                }
                else {
                    return getFail("message", "Invalid buyOrSell parameter");
                }

                offerorsSignature = w.personalSignMessage(agreement);

                try {
                    // Create an instance of the Offer class
                    new Offer(buyOrSell, collectionAddress, nftId, tokenAmount, deadline, signer, offerorsSignature);
                } catch (Exception e) {
                    return getFail("message", e.getMessage());
                }

                return getSuccess("message", "Offer created successfully");
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });


//        //Write POST function for wallet users to accept an offer
        post("/takeOffer", (request, response) -> {
            try {
                // Extract the parameters from the request
                String offereesAddress = request.queryParams("offeree").toLowerCase(); //Person accepting the offer
                String offerId = request.queryParams("offerId").toLowerCase();
                String endpointUrl = request.queryParams("endpointUrl");

                Wallet w = Wallets.getWalletByAddress(offereesAddress);
                if(w == null) return getFail("message", "Wallet not found");

                Offer offer = Offers.getOfferById(offerId);
                if(offer == null) return getFail("message", "Offer not found");

                Response r;
                if(offer.buyOrSell.equalsIgnoreCase("buy")) {
                    r = w.takeBuyOffer(offer);
                }
                else {
                    r = w.takeSellOffer(offer);
                }

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/takeSellOfferViaMotherWallet", (request, response) -> {
            try {
                // Extract the parameters from the request
                String offereesAddress = request.queryParams("offeree").toLowerCase(); //Person accepting the offer
                String offerId = request.queryParams("offerId").toLowerCase();
                String endpointUrl = request.queryParams("endpointUrl");

                Wallet w = Wallets.getWalletByAddress(offereesAddress);
                if(w == null) return getFail("message", "Wallet not found");

                Offer offer = Offers.getOfferById(offerId);
                if(offer == null) return getFail("message", "Offer not found");

                //takeSellOffer(address to, address nftContract , uint256 nftId, uint256 amount, uint256 deadline, bytes calldata signature)
                Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.marketplaceAddress, Settings.getGasPrice(), BigInteger.ZERO,
                        "takeSellOffer", new Address(offereesAddress), new Address(offer.collectionAddress), new Uint256(offer.nftId), new Uint256(offer.tokenAmount), new Uint256(offer.deadline), new DynamicBytes(offer.offerorsSignature));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        post("/transferNft", (request, response) -> {
            try {
                // Extract the parameters from the request
                String from = request.queryParams("from").toLowerCase(); //Person sending the NFT
                String to = request.queryParams("to").toLowerCase();
                String collectionAddress = request.queryParams("collectionAddress").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));
                String endpointUrl = request.queryParams("endpointUrl");

                Wallet w = Wallets.getWalletByAddress(from);
                if(w == null) return getFail("message", "Wallet not found");

                NFT n = NFTs.getNFTByCollectionAddressAndNftId(collectionAddress, nftId);
                if(n == null) return getFail("message", "NFT not found");
                String owner = "0x" + Hex.toHexString(n.getOwner().data());
                if(!owner.equalsIgnoreCase(from)) return getFail("message", "Sender does not own ticket");

                Response r = OWeb3j.fuelAndSendScTxn(Settings.web3j, w.getCredentials(), Settings.chainId, from, Settings.marketplaceAddress, Settings.getGasPrice(), BigInteger.ZERO, "transferNft", new Address(to), new Address(collectionAddress), new Uint256(nftId));

                if(r.success) {
                    Sender.addTxnHashAndEndpointUrl(r.message, endpointUrl);
                    return getSuccess("txnHash", r.message);
                } else {
                    return getFail("message", r.message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });
    }

    // There was a problem with the data submitted, or some pre-condition of the API
    // call wasn't satisfied
    public static JSONObject getFail(Object... variables) throws Exception {
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();

        int size = variables.length;
        if (size % 2 != 0)
            throw new Exception("Provided variables length should be even when using getSuccess");

        for (int t = 0; t < size; t += 2) {
            data.put(variables[t].toString(), variables[t + 1]);
        }

        object.put("status", "fail");
        object.put("data", data);

        return object;
    }

    // There was a problem on the server side
    public static JSONObject getError(String message) {
        JSONObject object = new JSONObject();

        object.put("status", "error");
        object.put("message", message);

        return object;
    }

    public static JSONObject getSuccess(Object... variables) throws Exception {
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();

        int size = variables.length;
        if (size % 2 != 0)
            throw new Exception("Provided variables length should be even when using getSuccess");

        for (int t = 0; t < size; t += 2) {
            data.put(variables[t].toString(), variables[t + 1]);
        }

        object.put("status", "success");
        object.put("data", data);

        return object;
    }



}

