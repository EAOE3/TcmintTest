package Main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Database.DBM;
import Marketplace.Offer.Offers;
import TicketSaleCreation.Management.ManagementContract;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import Collection.EventNFTCollection;
import Collection.EventNFTCollections;
import Collection.Ticket;
import Collection.TicketType;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.Web3j;

import static spark.Spark.*;

public class Main {

    public static BigInteger startingBlockNumber = BigInteger.valueOf(38630776); // Block we start checking from
    public static BigInteger currentBlockNumber = startingBlockNumber; // Block we are checking now

    // Holds the address of the management contract and all NFT contracts made by us
    public static List<String> ourContracts = new ArrayList<>();

    public static void init() {
        Settings.initGasPriceUpdate();
        Offers.initOffersCleanup();

        Verification.Initializer.Initializer.initialize();
        Wallet.Initializer.Initializer.initialize();
        Marketplace.Initializer.Initializer.initialize();
        BuyingVerification.Initializer.Initializer.initialize();
        TicketSaleCreation.Initializer.Initializer.initialize();
    }

    public static void main(String[] args) throws Exception {
        port(8080);

        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        DBM.loadAllObjectsFromDatabase(TicketType.class);
        DBM.loadAllObjectsFromDatabase(Ticket.class);
        DBM.loadAllObjectsFromDatabase(EventNFTCollection.class);

        REST_API.GET.run();

        ourContracts.add(Settings.managementContractAddress.toLowerCase());
        syncWithTheBlockchain();

        init();
    }

    private static void syncWithTheBlockchain() {
        new Thread() {
            public void run() {

                currentBlockNumber = SDBM.loadBigInt("currentBlockNumber");
                if (currentBlockNumber.compareTo(BigInteger.valueOf(0)) != 1) currentBlockNumber = startingBlockNumber;

                while (true) {
                    try {
                        BigInteger latestBlock = Settings.web3j.ethBlockNumber().send().getBlockNumber();

                        if (currentBlockNumber.compareTo(latestBlock) != -1) {
                            //System.out.println("Sleeping");
                            Thread.sleep(4000);
                            continue;
                        }

                        //System.out.println("Checking block: " + currentBlockNumber);
//				if(currentBlockNumber.add(BigInteger.valueOf(500)).compareTo(latestBlock) == 1) {
//					latestBlock = currentBlockNumber.add(BigInteger.valueOf(500));
//				}

                        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(currentBlockNumber),
                                DefaultBlockParameter.valueOf(latestBlock), ourContracts);
                        EthLog ethLog = Settings.web3j.ethGetLogs(filter).send();
                        List<EthLog.LogResult> logs = ethLog.getLogs();

                        for (EthLog.LogResult logResult : logs) {
                            Log log = ((EthLog.LogObject) logResult).get();

                            // Creating New Event
                            if (log.getTopics().get(0).equalsIgnoreCase("0x1975ac781f3138bb4e4148490410b523c82e550fcd572ea762272bd753302c21")) {

                                List<Type> results = FunctionReturnDecoder.decode(log.getData(),
                                        NEW_TICKET_SALE_EVENT.getNonIndexedParameters());

                                String address = "0x" + log.getTopics().get(1).substring(26);
                                List<Uint256> dataArray = (List<Uint256>) results.get(0).getValue();
                                List<Utf8String> stringData = (List<Utf8String>) results.get(1).getValue();

                                //stringData: name, symbol, baseUrl, [ticket type, ticket type... and repeats]
                                //data: eventId, [available spaces, seated, sale start time, sale end time, price, secondary market price cap] and repeats

                                ArrayList<String> ticketTypeIds = new ArrayList<>();
                                ArrayList<Integer> availableSpaces = new ArrayList<>();
                                ArrayList<BigInteger> price = new ArrayList<>();
                                ArrayList<Boolean> seated = new ArrayList<>();

                                for (int t = 0; t < stringData.size(); ++t) {
                                    System.out.println("String data " + t + ": " + stringData.get(t).getValue());
                                }

                                for (int t = 0; t < dataArray.size(); ++t) {
                                    System.out.println("Uint256 data " + t + ": " + dataArray.get(t).getValue());
                                    System.out.println("Uint256 data " + t + ": " + dataArray.get(t).getValue());
                                }
                                for (int t = 5; t < stringData.size(); ++t) {
                                    System.out.println("Ticket Type: " + stringData.get(t));
                                    ticketTypeIds.add(stringData.get(t).getValue());
                                }

                                for (int t = 0; t < dataArray.size(); t += 6) {
                                    //data: eventId, [available spaces, seated, sale start time, sale end time, price, secondary market price cap] and repeats
                                    availableSpaces.add(dataArray.get(t).getValue().intValue());
                                    seated.add(dataArray.get(t + 1).getValue().intValue() == 1);
                                    price.add(dataArray.get(t + 4).getValue());
                                }

                                for (int t = 0; t < ticketTypeIds.size(); ++t) {
                                    new Collection.TicketType(address, ticketTypeIds.get(t), availableSpaces.get(t), price.get(t), seated.get(t));
                                }

                                new EventNFTCollection(stringData.get(1).getValue(), address, ticketTypeIds);

                                ourContracts.add(address.toLowerCase());
                                syncCollectionWithTheBlockchain(address.toLowerCase(), currentBlockNumber, latestBlock);
                                ManagementContract.notifyServerOfSuccess(log.getTransactionHash(), address.toLowerCase());
                                System.out.println("New Event Created");
                            }
                            // NFT Transfer
                            else if (log.getTopics().get(0).equalsIgnoreCase(
                                    "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                                String to = "0x" + log.getTopics().get(2).substring(26);
                                int nftId = new BigInteger(log.getTopics().get(3).substring(27), 16).intValue();

                                EventNFTCollection event = EventNFTCollections.getEventByAddress(log.getAddress());
                                event.transferNFT(nftId, to);

                                System.out.println("NFT Transfered");
                                System.out.println("NFT receiver: " + to);
                            }
                            // NFT Mint (Only specified ticket type and seat number of newly minted nft)
                            else if (log.getTopics().get(0).equalsIgnoreCase(
                                    "0xd113c872a0d148e963f842767b1f364bf6d826e6cbbc9917c498a9b94f9c7280")) {
                                List<Type> results = FunctionReturnDecoder.decode(log.getData(), MINT_EVENT.getNonIndexedParameters());
                                int nftId = new BigInteger(log.getTopics().get(1).substring(2), 16).intValue();
                                String owner = (String) results.get(0).getValue();
                                String ticketType = (String) results.get(1).getValue();
                                String seatNumber = (String) results.get(2).getValue();

                                //   public Ticket(String collectionAddress, int nftId, String owner, String ticketType, String seat) {
                                Ticket t = new Ticket(log.getAddress(), nftId, owner, ticketType, seatNumber);

                                EventNFTCollection event = EventNFTCollections.getEventByAddress(log.getAddress());
                                event.addTicket(t);

                                TicketType tt = Collection.TicketTypes.getTicketTypeByDatabaseId(log.getAddress() + "-" + ticketType);
                                tt.decrementAvailableSpaces();

                                System.out.println("NFT Minted");
                            }

                        }
                        currentBlockNumber = latestBlock;
                        SDBM.store("currentBlockNumber", currentBlockNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
    }

    private static void syncCollectionWithTheBlockchain(String collectionAddress, BigInteger startingBlockNumber, BigInteger finishBlockNumber) {
        while (true) {
            try {

                EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(startingBlockNumber),
                        DefaultBlockParameter.valueOf(finishBlockNumber), collectionAddress);
                EthLog ethLog = Settings.web3j.ethGetLogs(filter).send();
                List<EthLog.LogResult> logs = ethLog.getLogs();

                for (EthLog.LogResult logResult : logs) {
                    Log log = ((EthLog.LogObject) logResult).get();

                    // NFT Transfer
                    if (log.getTopics().get(0).equalsIgnoreCase("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                        String to = "0x" + log.getTopics().get(2).substring(26);
                        int nftId = new BigInteger(log.getTopics().get(3).substring(27), 16).intValue();

                        EventNFTCollection event = EventNFTCollections.getEventByAddress(log.getAddress());
                        event.transferNFT(nftId, to);

                        System.out.println("NFT Transfered");
                        System.out.println("NFT receiver: " + to);
                    }
                    // NFT Mint (Only specified ticket type and seat number of newly minted nft)
                    else if (log.getTopics().get(0).equalsIgnoreCase(
                            "0xd113c872a0d148e963f842767b1f364bf6d826e6cbbc9917c498a9b94f9c7280")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), MINT_EVENT.getNonIndexedParameters());
                        int nftId = new BigInteger(log.getTopics().get(1).substring(2), 16).intValue();
                        String owner = (String) results.get(0).getValue();
                        String ticketType = (String) results.get(1).getValue();
                        String seatNumber = (String) results.get(2).getValue();

                        //   public Ticket(String collectionAddress, int nftId, String owner, String ticketType, String seat) {
                        Ticket t = new Ticket(log.getAddress(), nftId, owner, ticketType, seatNumber);

                        EventNFTCollection event = EventNFTCollections.getEventByAddress(log.getAddress());
                        event.addTicket(t);

                        TicketType tt = Collection.TicketTypes.getTicketTypeByDatabaseId(log.getAddress() + "-" + ticketType);
                        tt.decrementAvailableSpaces();

                        System.out.println("NFT Minted");
                    }

                }

                return;

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(4000);
            } catch (Exception e) {
            }
        }
    }

    public static final Event NEW_TICKET_SALE_EVENT = new Event("newTicketSale",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<DynamicArray<Uint256>>(false) {
                    },
                    new TypeReference<DynamicArray<Utf8String>>(false) {
                    }
            )
    );

    public static final Event MINT_EVENT = new Event("Mint",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {
                    },
                    new TypeReference<Address>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    }
            )
    );

}
