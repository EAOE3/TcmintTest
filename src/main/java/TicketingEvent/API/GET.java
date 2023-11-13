package TicketingEvent.API;

import Main.ByteArrayWrapper;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.TicketType.TicketType;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static spark.Spark.get;


public class GET {
    public static void run() {

        //600: Invalid Event ID
        //601: Invalid Address

        // GET https://endpoint/verified/?email=<userEmail>
//        get("/test", (request, response) -> {
//            LinkedList<String> test = new LinkedList<>();
//
//            test.add("test1");
//            test.add("test2");
//
//            JSONObject test1 = new JSONObject();
//            test1.put("test", "ok");
//            test1.put("thearray", test);
//
//            response.header("Content-Type", "application/json");
//            return test1;
//
//        });

        // GET https://endpoint/eventInfo/?eventId=<String>
        get("/eventInfo/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String contractAddress = request.queryParams("address");

                Collection c = Collections.getCollectionByAddress(contractAddress);

                if(c == null || !(c instanceof EventNFTCollection)) return getError("Invalid Event ID");

                EventNFTCollection e = (EventNFTCollection) c;

                JSONArray tickets = new JSONArray();
                for(TicketType t: e.getAllTicketTypes()) {
                    JSONObject ticketTypeJson = new JSONObject();

                    boolean seated = t.isSeated();

                    ticketTypeJson.put("ticketType", t.getTicketTypeId());
                    ticketTypeJson.put("seated", seated);

                    tickets.put(ticketTypeJson);
                }

                return getSuccess("contractAddress", e.getCollectionAddress(), "tickets", tickets);
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });

        // GET https://endpoint/userPortfolio/?userAddress=<string>
        get("/userPortfolio/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String userAddress = request.queryParams("userAddress").toLowerCase();
                if(!isValidEthereumAddress(userAddress)) return getError("Invalid Address");
                ByteArrayWrapper user = new ByteArrayWrapper(Hex.decode(request.queryParams("userAddress").toLowerCase().substring(2)));

                JSONArray nfts = new JSONArray();
                List<NFT> allNFTsOwnedByUser = new LinkedList<>();

                for(Collection c: Collections.getAllCollections()) {
                    if(c.getBalance(userAddress) == 0) continue;

                    for(NFT n: c.getAllNFTs()) {
                        if(n.getOwner().equals(user)) {
                            allNFTsOwnedByUser.add(n);
                        }
                    }
                }

                //Sort NFTs, placing the ones with the biggest timeOfAcquiring first
                List<NFT> allNFTsOwnedByUserSorted = new LinkedList<>();
                for(NFT n: allNFTsOwnedByUser) {
                    if(allNFTsOwnedByUserSorted.size() == 0) {
                        allNFTsOwnedByUserSorted.add(n);
                        continue;
                    }

                    int index = 0;
                    for(NFT n2: allNFTsOwnedByUserSorted) {
                        if(n.getTimeOfAcquiring() > n2.getTimeOfAcquiring()) {
                            break;
                        }
                        index++;
                    }
                    allNFTsOwnedByUserSorted.add(index, n);
                }

                //Place all NFTs in json array
                for(NFT n: allNFTsOwnedByUserSorted) {
                    JSONObject nft = new JSONObject();

                    if(n instanceof Ticket) {
                        Ticket t = (Ticket) n;
                        TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(n.getCollectionAddress(), t.ticketType);

                        nft.put("contractAddress", t.getCollectionAddress());
                        nft.put("nftId", t.getNftId());
                        nft.put("ticketType", t.ticketType);
                        nft.put("seated", tt.seated);
                        nft.put("seat", t.seat);
                        nft.put("nftType", "ticket");
                        nft.put("collectable", t.isCollectable());

                        nfts.put(nft);
                    } else {
                        nft.put("contractAddress", n.getCollectionAddress());
                        nft.put("nftId", n.nftId);
                        nft.put("nftType", "merchandise");

                        nfts.put(nft);
                    }
                }

                return getSuccess("nfts", nfts);

            } catch (Exception e) {
                return getError(e.getLocalizedMessage());
            }
        });


        // GET https://endpoint/userPortfolio/?userAddress=<string>
        get("/allNftsInACollection/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String contractAddress = request.queryParams("contractAddress").toLowerCase();

                Collection c = Collections.getCollectionByAddress(contractAddress);
                if(c == null || !(c instanceof EventNFTCollection)) return getError("Invalid Contract Address");
                EventNFTCollection event = (EventNFTCollection) c;


                JSONArray tickets = new JSONArray();
                for(Ticket t: event.getAllTickets()) {
                    TicketType tt = TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(contractAddress, t.ticketType);
                    JSONObject ticket = new JSONObject();

                    ticket.put("contractAddress", event.getCollectionAddress());
                    ticket.put("nftId", t.getNftId());
                    ticket.put("ticketType", t.ticketType);
                    ticket.put("seated", tt.seated);
                    ticket.put("seat", t.seat);
                    ticket.put("collectable", t.isCollectable());
                    ticket.put("owner", "0x" + Hex.toHexString(t.getOwner().data()));

                    tickets.put(ticket);
                }

                return getSuccess("tickets", tickets);

            } catch (Exception e) {
                return getError(e.getLocalizedMessage());
            }
        });

        get("/isTicketUsed/", (request, response) -> {
            try {
                response.header("Content-Type", "application/json");

                String contractAddress = request.queryParams("address").toLowerCase();
                int nftId = Integer.parseInt(request.queryParams("nftId"));

                Collection c = Collections.getCollectionByAddress(contractAddress);
                if(c == null || !(c instanceof EventNFTCollection)) return getError("Invalid Event address");

                NFT n = NFTs.getNFTByCollectionAddressAndNftId(contractAddress, nftId);
                if(n == null || !(n instanceof Ticket)) return getError("Invalid Ticket ID");

                return  getSuccess("used", ((Ticket) n).isUsed());
            } catch (Exception e) {
                e.printStackTrace();
                return getError(e.getLocalizedMessage());
            }
        });


    }

    public static boolean isValidEthereumAddress(String address) {
        String regex = "^0x[0-9a-fA-F]{40}$";

        // Create a pattern object
        Pattern pattern = Pattern.compile(regex);

        // Match the address against the pattern
        return pattern.matcher(address).matches();
    }

    //There was a problem with the data submitted, or some pre-condition of the API call wasn't satisfied
    public static JSONObject getFail() {
        JSONObject object = new JSONObject();

        object.put("status", "fail");

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
