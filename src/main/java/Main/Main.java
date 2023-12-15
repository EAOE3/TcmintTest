package Main;

import java.io.Console;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static spark.Spark.*;

import Database.DBM;
import Database.SDBM;
import Marketplace.Analytics.Analytics;
import Marketplace.CompletedTrades.CompletedTrade;
import Marketplace.Offer.Offers;
import Merchandise.NftCollection.MerchandiseCollection;
import Merchandise.Webhook.Sender;
import NFTCollections.Collection.Collection;
import NFTCollections.Collection.Collections;
import NFTCollections.NFT.NFT;
import NFTCollections.NFT.NFTs;
import TicmintToken.Blockchain.Synchroniser;
import TicketingEvent.Initializer;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.Log;
import TicketingEvent.Blockchain.NFTCollection.EventNFTCollection;
import TicketingEvent.Blockchain.NFT.Ticket;
import TicketingEvent.Blockchain.TicketType.TicketType;
import TicketingEvent.Blockchain.TicketType.TicketTypes;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

public class Main {

    public static BigInteger startingBlockNumber = BigInteger.valueOf(41922209); // Block we start checking from
    public static BigInteger currentBlockNumber = startingBlockNumber; // Block we are checking now

    // Holds the address of the management contract and all NFT contracts made by us
    public static List<String> ourContracts = new ArrayList<>();

    public static void initServer() {
        Settings.initGasPriceUpdate();
        Offers.initOffersCleanup();

        Verification.Initializer.Initializer.initialize();
        Wallet.Initializer.Initializer.initialize();
        Marketplace.Initializer.Initializer.initialize();
        Initializer.init();
        Merchandise.Initializer.init();
        NFTCollections.Initializer.init();
        TicmintToken.Initializer.init();
        AddOnsAndAdditionalServices.Initializer.init();
    }

    public static void initializeContracts() {
        if(SDBM.loadBoolean("contractsInitialized")) return;

        //Web3j web3j, Credentials cr, long chainId, String contractAddress, BigInteger gasPrice, BigInteger value, String functionName, Object... inputParams) {
        Response r = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize");
        Response r1 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.eventManagementContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize", new Address(Settings.ticmintFeeAccount), new Address(Settings.ticmintTokenAddress));
        Response r2 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.merchandiseManagementContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize", new Address(Settings.ticmintFeeAccount), new Address(Settings.ticmintTokenAddress));
        Response r3 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.marketplaceAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize", new Address(Settings.eventManagementContractAddress), new Address(Settings.ticmintTokenAddress));
        Response r4 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.addOnsAndAdditonalServicesContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize", new Address(Settings.ticmintTokenAddress), new Address(Settings.eventManagementContractAddress));


        Response r5 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.eventManagementContractAddress), new Bool(true));
        Response r6 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.merchandiseManagementContractAddress), new Bool(true));
        Response r7 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.addOnsAndAdditonalServicesContractAddress), new Bool(true));
        Response r8 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.ticmintTokenAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.marketplaceAddress), new Bool(true));

        Response r9 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.eventManagementContractAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.marketplaceAddress), new Bool(true));
        Response r10 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.merchandiseManagementContractAddress, Settings.getGasPrice(), BigInteger.valueOf(300000), BigInteger.ZERO, "setGoldList", new Address(Settings.marketplaceAddress), new Bool(true));

        System.out.println(r.success);
        System.out.println(r.message);

        System.out.println(r1.success);
        System.out.println(r1.message);

        System.out.println(r2.success);
        System.out.println(r2.message);

        System.out.println(r3.success);
        System.out.println(r3.message);

        System.out.println(r4.success);
        System.out.println(r4.message);

        System.out.println(r9.success);
        System.out.println(r9.message);

        System.out.println(r10.success);
        System.out.println(r10.message);

        SDBM.store("contractsInitialized", true);
    }



    public static void main(String[] args) throws Exception {
        Settings.initGasPriceUpdate();
        Thread.sleep(10000);
        System.out.println(Settings.getGasPrice());
        System.exit(0);
//        SDBM.store("yuirtekhjvbc", "ticmintarelolmyswimfudgenokacoyes");
//        System.exit(0);
//        Response r1 = OWeb3j.sendScTxn(Settings.web3j, Settings.getMotherWalletCredentials(), Settings.chainId, Settings.eventManagementContractAddress, Settings.getGasPrice(), BigInteger.ZERO, "initialize", new Address(Settings.ticmintFeeAccount), new Address(Settings.ticmintTokenAddress));
//
//        Thread.sleep(5000);
//        List<Type> inputParameters = new ArrayList<>();
//        //inputParameters.add(new Address("0x32842C66FA5d7199838e8ED12830b36f8D641677"));
//
//        Function function = new Function(
//                "getTicmintTokenAddress",  // function we're calling
//                inputParameters,   // Parameters to pass as Solidity Types
//                Arrays.asList(new TypeReference<Address>(){}));
//
//        String encodedFunction = FunctionEncoder.encode(function);
//        EthCall ethCall = null;
//        try {ethCall = Settings.web3j.ethCall(Transaction.createEthCallTransaction("0x68Dc507aCffb0B47bba74d46d46fE91cd42044F8", Settings.eventManagementContractAddress, encodedFunction), DefaultBlockParameterName.LATEST).send();} catch (
//                IOException e) {e.printStackTrace(); }
//
//        System.out.println(FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()));
//
//        System.exit(0);\

//        Console console = System.console();
//        if (console == null) {
//            System.out.println("No console available");
//            return;
//        }
//
//        char[] password = console.readPassword("Enter your password: ");

        // Use the password as needed (convert to String if required)
        String passwordStr = SDBM.loadString("yuirtekhjvbc");

        // It's a good practice to clear the password from memory after use
        //java.util.Arrays.fill(password, ' ');

        Settings.setPassword(passwordStr);
        if(Settings.getMotherWalletCredentials() == null) {
            System.out.println("Invalid password");
            System.exit(0);
        }

        //Output address
        System.out.println("Mother wallet address: " + Settings.getMotherWalletCredentials().getAddress());

        threadPool(1000, 0, (int)(Settings.blockTime * 3));
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

        DBM.loadAllObjectsFromDatabase(EventNFTCollection.class);
        DBM.loadAllObjectsFromDatabase(NFT.class);
        DBM.loadAllObjectsFromDatabase(TicketType.class);
        DBM.loadAllObjectsFromDatabase(Ticket.class);

        ourContracts.add(Settings.eventManagementContractAddress.toLowerCase());
        ourContracts.add(Settings.merchandiseManagementContractAddress.toLowerCase());
        ourContracts.add(Settings.ticmintTokenAddress.toLowerCase());
        ourContracts.add(Settings.addOnsAndAdditonalServicesContractAddress.toLowerCase());

        for (String address : Collections.getAllCollectionsAddresses()) {
            ourContracts.add(address.toLowerCase());
        }

        syncWithTheBlockchain();

        initServer();

        Thread.sleep(2000);
        initializeContracts();
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

                        if(latestBlock.subtract(currentBlockNumber).compareTo(BigInteger.valueOf(25000)) == 1) {
                            latestBlock = currentBlockNumber.add(BigInteger.valueOf(25000));
                        }

                        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(currentBlockNumber),
                                DefaultBlockParameter.valueOf(latestBlock), ourContracts);
                        EthLog ethLog = Settings.web3j.ethGetLogs(filter).send();
                        List<EthLog.LogResult> logs = ethLog.getLogs();

                        Map<String /*Txn Hash*/, List<String>> ticketTypesInTxn = new HashMap<>();
                        Map<String /*Txn Hash*/, List<Integer>> nftIdInTxn = new HashMap<>();
                        Map<String /*Txn Hash*/, String /*Contract Address*/> contractAddressByTxnHash = new HashMap<>();

                        try { Synchroniser.synchronise(logs); } catch (Exception e) { e.printStackTrace(); }

                        for (EthLog.LogResult logResult : logs) {
                            try {
                                Log log = ((EthLog.LogObject) logResult).get();

                                // Creating New Event
                                if (log.getTopics().get(0).equalsIgnoreCase("0x9f0cb8c9412df0c0e8d1ce5a9cd61832236384582281c225386c0a32a75ec2f8")) {

                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(),
                                            NEW_TICKET_SALE_EVENT.getNonIndexedParameters());

                                    String address = "0x" + log.getTopics().get(1).substring(26);
                                    List<Uint256> dataArray = (List<Uint256>) results.get(0).getValue();
                                    List<Utf8String> stringData = (List<Utf8String>) results.get(1).getValue();

                                    System.out.println("Contract address: " + address);
                                    //stringData: empty, eventId, name, symbol, baseUrl, [ticket type, ticket type... and repeats]
                                    //data: endtime, royalty, [available spaces, seated, sale start time, sale end time, price, secondary market price cap] and repeats

                                    long endTime = dataArray.get(0).getValue().longValue();
                                    ArrayList<String> ticketTypeIds = new ArrayList<>();
                                    ArrayList<Integer> availableSpaces = new ArrayList<>();
                                    ArrayList<BigInteger> price = new ArrayList<>();
                                    ArrayList<Boolean> seated = new ArrayList<>();
                                    ArrayList<BigInteger> secondaryMarketPriceCap = new ArrayList<>();

                                    for (int t = 0; t < stringData.size(); ++t) {
                                        System.out.println("String data " + t + ": " + stringData.get(t).getValue());
                                    }

                                    for (int t = 0; t < dataArray.size(); ++t) {
                                        System.out.println("Uint256 data " + t + ": " + dataArray.get(t).getValue());
                                    }
                                    for (int t = 5; t < stringData.size(); ++t) {
                                        System.out.println("Ticket Type: " + stringData.get(t));
                                        ticketTypeIds.add(stringData.get(t).getValue());
                                    }

                                    for (int t = 3; t < dataArray.size(); t += 6) {
                                        //data: eventId, [available spaces, seated, sale start time, sale end time, price, secondary market price cap] and repeats
                                        availableSpaces.add(dataArray.get(t).getValue().intValue());
                                        seated.add(dataArray.get(t + 1).getValue().intValue() == 1);
                                        price.add(dataArray.get(t + 4).getValue());
                                        secondaryMarketPriceCap.add(dataArray.get(t + 5).getValue());
                                    }

                                    new EventNFTCollection(address, endTime);

                                    for (int t = 0; t < ticketTypeIds.size(); ++t) {
                                        new TicketType(address, ticketTypeIds.get(t), availableSpaces.get(t), price.get(t), secondaryMarketPriceCap.get(t), seated.get(t));
                                    }

                                    ourContracts.add(address.toLowerCase());
                                    syncCollectionWithTheBlockchain(address.toLowerCase(), currentBlockNumber, latestBlock);
                                    TicketingEvent.Webhook.Sender.notifyOfEventCreationSuccess(log.getTransactionHash(), address.toLowerCase());
                                    System.out.println("New Event Created");
                                }
                                //Create new Merchandise
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x264ed3ad42aa5f07a7b713cf4477270541b58081992fc95e6f905bd11e70f55d")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), NEW_MERCHANDISE.getNonIndexedParameters());
                                    //NewMerchandise(address merchandiseContract, uint256 maxSupply, uint256 price, uint256 saleStartTime, uint256 saleEndTime);
                                    String address = (String) results.get(0).getValue();
                                    BigInteger maxSupply = (BigInteger) results.get(1).getValue();
                                    BigInteger price = (BigInteger) results.get(2).getValue();
                                    BigInteger saleStartTime = (BigInteger) results.get(3).getValue();
                                    BigInteger saleEndTime = (BigInteger) results.get(4).getValue();

                                    System.out.println("creating new merchandise: " + address);

                                    new MerchandiseCollection(address, maxSupply.intValue(), price, saleStartTime.longValue(), saleEndTime.longValue());
                                    Sender.notifyOfMerchandiseContractCreation(log.getTransactionHash(), address.toLowerCase());
                                    syncCollectionWithTheBlockchain(address.toLowerCase(), currentBlockNumber, latestBlock);
                                }
                                //Trade Complete
                                else if(log.getTopics().get(0).equalsIgnoreCase("0x1fc20263ea6c7198e6774cbcae3334337bad95846d4bec486740c929a22d88a3")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), TradeComplete.getNonIndexedParameters());

                                    String seller = (String) results.get(0).getValue();
                                    String buyer = (String) results.get(1).getValue();
                                    String collectionAddress = (String) results.get(2).getValue();
                                    BigInteger nftId = (BigInteger) results.get(3).getValue();
                                    BigInteger price = (BigInteger) results.get(4).getValue();

                                    //CompletedTrade(String collectionAddress, int nftId, String buyerAddress, String sellerAddress, BigInteger price, long time, String transactionHash)
                                    new CompletedTrade(collectionAddress, nftId.intValue(), buyer, seller, price, Instant.now().getEpochSecond(), log.getTransactionHash());
                                    Analytics.increaseTradingVolume(collectionAddress, price);
                                    Marketplace.Webhook.Sender.notifyOfTakeOffer(log.getTransactionHash(), collectionAddress, nftId.intValue(), buyer, seller, price);
                                }
                                // NFT Transfer
                                if (log.getTopics().get(0).equalsIgnoreCase("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                                    System.out.println("Txn hash: " + log.getTransactionHash()) ;
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), TRANSFER.getNonIndexedParameters());
                                    ByteArrayWrapper to = new ByteArrayWrapper(Hex.decode(log.getTopics().get(2).substring(26)));
                                    System.out.println("To test: 0x" + log.getTopics().get(2).substring(26));


                                    //NFT transfer
                                    if (!log.getAddress().equalsIgnoreCase(Settings.ticmintTokenAddress)) {
                                        BigInteger nftId = (BigInteger) results.get(0).getValue();
                                        Collection c = Collections.getCollectionByAddress(log.getAddress());

                                        System.out.println("NFT Transferred from: 0x" + Hex.encode(NFTs.getNFTByCollectionAddressAndNftId(log.getAddress(), nftId.intValue()).getOwner().data()));
                                        System.out.println("NFT receiver: 0x" + log.getTopics().get(2).substring(26));

                                        c.transfer(nftId.intValue(), to);
                                        Marketplace.Webhook.Sender.notifyOfNftTransfer(log.getTransactionHash(), log.getAddress(), "0x" + Hex.toHexString(to.data()), nftId.intValue());
                                    }

                                }
                                // Ticket Mint
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xf1c13eb9fe4a39e476b8941315f9e7dc53628d2bb40b8bbf2208aa7af0aee3e8")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), TICKET_MINT.getNonIndexedParameters());
                                    int nftId = Integer.parseInt(log.getTopics().get(1).substring(26), 16);

                                    ByteArrayWrapper owner = new ByteArrayWrapper(Hex.decode(((String) results.get(0).getValue()).substring(2)));
                                    String ticketType = (String) results.get(1).getValue();
                                    String seatNumber = (String) results.get(2).getValue();
                                    BigInteger purchasePrice = (BigInteger) results.get(3).getValue();
                                    long purchaseTime = ((BigInteger) results.get(4).getValue()).longValue();

                                    //     public Ticket(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash, String ticketType, String seat)
                                    new Ticket(log.getAddress(), nftId, purchaseTime, purchasePrice, owner, log.getTransactionHash(), ticketType, seatNumber);

                                    TicketType tt = TicketTypes.getTicketTypeByDatabaseId(log.getAddress() + "-" + ticketType);
                                    tt.decrementAvailableSpaces();

                                    //TicketingEvent.Webhook.Sender.notifyOfSuccessfulPurchase(log.getTransactionHash());

                                    System.out.println("Adding ticket details to maps");
                                    contractAddressByTxnHash.put(log.getTransactionHash(), log.getAddress());
                                    List<String> ticketTypes = ticketTypesInTxn.get(log.getTransactionHash());
                                    if (ticketTypes == null) ticketTypes = new ArrayList<>();
                                    ticketTypes.add(ticketType);
                                    ticketTypesInTxn.put(log.getTransactionHash(), ticketTypes);

                                    List<Integer> nftIds = nftIdInTxn.get(log.getTransactionHash());
                                    if (nftIds == null) nftIds = new ArrayList<>();
                                    nftIds.add(nftId);
                                    nftIdInTxn.put(log.getTransactionHash(), nftIds);
                                    System.out.println("Done adding ticket details to map");

                                    System.out.println("NFT Minted");
                                }
                                // Merchandise Mint
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x715e3ee0f5646157a56af644beeef944041fede0d1438f31ddb079d26fc222b7")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), MERCHANDISE_MINT.getNonIndexedParameters());
                                    int nftId = new BigInteger(log.getTopics().get(1).substring(26), 16).intValue();
                                    ByteArrayWrapper to = new ByteArrayWrapper(Hex.decode(((String) results.get(0).getValue()).substring(2)));
                                    BigInteger purchasePrice = (BigInteger) results.get(1).getValue();
                                    long purchaseTime = ((BigInteger) results.get(2).getValue()).longValue();

                                    //    public NFT(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash) {
                                    new NFT(log.getAddress(), nftId, purchaseTime, purchasePrice, to, log.getTransactionHash());
                                }
                                //Event End Time Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xc762ca24c3b6273f5c0b4d337ad2d0eb259d04155669eef2b2c593da2e455225")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), EVENT_END_TIME_UPDATE.getNonIndexedParameters());
                                    BigInteger newEndTime = (BigInteger) results.get(0).getValue();

                                    EventNFTCollection e = (EventNFTCollection) Collections.getCollectionByAddress(log.getAddress());
                                    if (e == null) {
                                        System.out.println("Event End Time Updated, but event not found");
                                        continue;
                                    }

                                    e.setEndTime(newEndTime.longValue());
                                    TicketingEvent.Webhook.Sender.notifyOfEventEndTimeUpdate(log.getAddress(), newEndTime.longValue(), log.getTransactionHash());
                                    System.out.println("Event End Time Updated");
                                }
                                //Royalties Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x382d6d457eaa3c84d586de142a0d72bac72f2a514a1691f8ccf48feae833ff09")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), ROYALTIES_UPDATED.getNonIndexedParameters());
                                    BigInteger newRoyalties = (BigInteger) results.get(0).getValue();

                                    System.out.println("Royalties Updated, new royalties: " + newRoyalties);
                                    //TODO webhook notification
                                    //Royalties is not allowed to be updated, but i made it updatable just in case for the future
                                }
                                //Available Spaces Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xe93e137ad9e7cbdfd2055f0965a6c43e212982ee555a27b95450b6776e53da87")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), AVAILABLE_SPACES_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    BigInteger newAvailableSpaces = (BigInteger) results.get(1).getValue();

                                    System.out.println("Available Spaces Updated for ticket type:" + ticketType + ", new available spaces: " + newAvailableSpaces);
                                    try {TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setAvailableSpaces(newAvailableSpaces.intValue());} catch (Exception e) { e.printStackTrace(); }
                                    TicketingEvent.Webhook.Sender.notifyOfAvailableSpacesUpdate(log.getAddress(), ticketType, newAvailableSpaces, log.getTransactionHash());
                                }
                                //Seating Info Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x598954af727868b90b48325457400db17e5821c505825df20ddcf174ff5ce33e")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SEATING_INFO_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    boolean seated = (boolean) results.get(1).getValue();

                                    System.out.println("Seating Info Updated for ticket type: " + ticketType + ", new seating info: " + seated);
                                    try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setSeated(seated); } catch (Exception e) { e.printStackTrace(); }
                                    TicketingEvent.Webhook.Sender.notifyOfSeatedStateUpdate(log.getAddress(), ticketType, seated, log.getTransactionHash());
                                }
                                //Sale Start Time Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xa95ead81fecc85c7f12ead89d84ec37ff47b01c8ebc61538ccf5b54971dbc61a")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_START_TIME_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    BigInteger newStartTime = (BigInteger) results.get(1).getValue();

                                    System.out.println("Sale Start Time Updated for ticket type: " + ticketType + ", new start time: " + newStartTime);
                                    TicketingEvent.Webhook.Sender.notifyOfSaleStartTimeUpdate(log.getAddress(), ticketType, newStartTime, log.getTransactionHash());
                                }
                                //Sale End Time Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x9051da9e0ca64ffe3b17d2c730fcf58420dbb89066de41d504d14b74d1032245")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_END_TIME_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    BigInteger newEndTime = (BigInteger) results.get(1).getValue();

                                    System.out.println("Sale End Time Updated for ticket type: " + ticketType + ", new end time: " + newEndTime);
                                    TicketingEvent.Webhook.Sender.notifyOfSaleEndTimeUpdate(log.getAddress(), ticketType, newEndTime, log.getTransactionHash());
                                }
                                //Price Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x159e83f4712ba2552e68be9d848e49bf6dd35c24f19564ffd523b6549450a2f4")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), PRICE_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    BigInteger newPrice = (BigInteger) results.get(1).getValue();

                                    System.out.println("Price Updated for ticket type: " + ticketType + ", new price: " + newPrice);
                                    try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setPrice(newPrice); } catch (Exception e) { e.printStackTrace(); }
                                    TicketingEvent.Webhook.Sender.notifyOfPriceUpdate(log.getAddress(), ticketType, newPrice, log.getTransactionHash());
                                }
                                //Secondary Market Price Cap Update
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x8350ddaff072473660b5b8f2b9fbab8ad16905e35a66d6000d898a675ae6cee3")) {
                                    //TODO testing
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SECONDARY_MARKET_PRICE_CAP_UPDATED.getNonIndexedParameters());
                                    String ticketType = (String) results.get(0).getValue();
                                    BigInteger newPriceCap = (BigInteger) results.get(1).getValue();

                                    System.out.println("Secondary Market Price Cap Updated for ticket type: " + ticketType + ", new price cap: " + newPriceCap);
                                    try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setSecondaryMarketPriceCap(newPriceCap); } catch (Exception e) { e.printStackTrace(); }
                                    TicketingEvent.Webhook.Sender.notifyOfSecondaryMarketPriceCapUpdate(log.getAddress(), ticketType, newPriceCap, log.getTransactionHash());
                                }
                                //Refund Complete
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xa02c9ec5aecd7764ac4d99b6772ea8297bec9ad448f9da221c7b2329400c3653")) {
                                    //TODO testing
                                    TicketingEvent.Webhook.Sender.notifyOfRefundCompletion(log.getTransactionHash());
                                }
                                //Merchandise Max Supply Changed
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x28a10a2e0b5582da7164754cb994f6214b8af6aa7f7e003305fbc09e7106c513")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), MAX_SUPPLY_CHANGED.getNonIndexedParameters());
                                    BigInteger newMaxSupply = (BigInteger) results.get(0).getValue();

                                    System.out.println("Max Supply Updated to: " + newMaxSupply);


                                    MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                                    m.setMaxSupply(newMaxSupply.intValue());

                                    Sender.notifyOfMaxSupplyChange(log.getAddress(), newMaxSupply.intValue(), log.getTransactionHash());
                                }
                                //Merchandise Price Changed
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xa6dc15bdb68da224c66db4b3838d9a2b205138e8cff6774e57d0af91e196d622")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), PRICE_CHANGED.getNonIndexedParameters());
                                    BigInteger newPrice = (BigInteger) results.get(0).getValue();

                                    System.out.println("Price Updated to: " + newPrice);

                                    MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                                    m.setPrice(newPrice);

                                    Sender.notifyOfPriceChange(log.getAddress(), newPrice, log.getTransactionHash());
                                }
                                //Merchandise Sale Start Time Changed
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xf9aef9ba2f8a1311aed781e9d5eb6e91969cf38d9699207a9fe2e5d3357bba12")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_START_TIME_CHANGED.getNonIndexedParameters());
                                    BigInteger newSaleStartTime = (BigInteger) results.get(0).getValue();

                                    System.out.println("Sale Start Time Updated to: " + newSaleStartTime);

                                    MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                                    m.setSaleStartTime(newSaleStartTime.longValue());

                                    Sender.notifyOfSaleStartTimeChange(log.getAddress(), newSaleStartTime, log.getTransactionHash());
                                }
                                //Merchandise Sale End Time Changed
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x6578ad4f74a92966ef7c4f4956e0a576592b0ac12c8c541cfb3974adc2b0b561")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_END_TIME_CHANGED.getNonIndexedParameters());
                                    BigInteger newSaleEndTime = (BigInteger) results.get(0).getValue();

                                    System.out.println("Sale End Time Updated to: " + newSaleEndTime);

                                    MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                                    m.setSaleEndTime(newSaleEndTime.longValue());

                                    Sender.notifyOfSaleEndTimeChange(log.getAddress(), newSaleEndTime, log.getTransactionHash());
                                }

                                //Addons & Additional Services

                                //BuyAdditionalServices
                                else if (log.getTopics().get(0).equalsIgnoreCase("0x9bc37d5ba88a007e4d478ff23078de529ae4bcbdc496c1c14702bb12609c6ce0")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), BuyAdditionalServices.getNonIndexedParameters());
                                    String collectionAddress = results.get(0).getValue().toString();
                                    String buyerAddress = results.get(1).getValue().toString();
                                    List<String> service = (List<String>) results.get(2).getValue();
                                    List<BigInteger> amount = (List<BigInteger>) results.get(3).getValue();

                                    AddOnsAndAdditionalServices.Webhook.Sender.notifyOfAdditionalServicesPurchase(log.getTransactionHash(), collectionAddress, buyerAddress, service, amount);
                                }
                                //BuyAddOn
                                else if (log.getTopics().get(0).equalsIgnoreCase("0xb6791ef7224ca7106c30c1e8524cca3635fd8080aedc64cacf30e96851990b7f")) {
                                    List<Type> results = FunctionReturnDecoder.decode(log.getData(), BuyAddOn.getNonIndexedParameters());
                                    String collectionAddress = results.get(0).getValue().toString();
                                    String buyerAddress = results.get(1).getValue().toString();
                                    List<String> service = (List<String>) results.get(2).getValue();
                                    List<BigInteger> amount = (List<BigInteger>) results.get(3).getValue();

                                    AddOnsAndAdditionalServices.Webhook.Sender.notifyOfAddOnPurchase(log.getTransactionHash(), collectionAddress, buyerAddress, service, amount);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                        if(contractAddressByTxnHash.keySet().size() > 0) {
                            for (String txnHash : ticketTypesInTxn.keySet()) {
                                System.out.println("Notifying of ticket creation");
                                TicketingEvent.Webhook.Sender.notifyOfTicketCreation(txnHash, contractAddressByTxnHash.get(txnHash), nftIdInTxn.get(txnHash), ticketTypesInTxn.get(txnHash));
                            }
                        } else {
                            //System.out.println("No new tickets created in this new block fetch");
                        }

                        Thread.sleep(100); //Sleeping time for the sake of file writing
                        currentBlockNumber = latestBlock.add(BigInteger.valueOf(1));
                        SDBM.store("currentBlockNumber", currentBlockNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //try {Thread.sleep(4000);} catch (Exception e) {}
                }
            }
        }.start();
    }

    private static void syncCollectionWithTheBlockchain(String collectionAddress, BigInteger startingBlockNumber, BigInteger finishBlockNumber) {
        try {

            EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(startingBlockNumber),
                    DefaultBlockParameter.valueOf(finishBlockNumber), collectionAddress);
            EthLog ethLog = Settings.web3j.ethGetLogs(filter).send();
            List<EthLog.LogResult> logs = ethLog.getLogs();

            Map<String /*Txn Hash*/, List<String>> ticketTypesInTxn = new HashMap<>();
            Map<String /*Txn Hash*/, List<Integer>> nftIdInTxn = new HashMap<>();
            Map<String /*Txn Hash*/, String /*Contract Address*/> contractAddressByTxnHash = new HashMap<>();

            for (EthLog.LogResult logResult : logs) {
                try {
                    Log log = ((EthLog.LogObject) logResult).get();
                    // NFT Transfer
                    if (log.getTopics().get(0).equalsIgnoreCase("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                        System.out.println("Txn hash: " + log.getTransactionHash()) ;
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), TRANSFER.getNonIndexedParameters());
                        ByteArrayWrapper to = new ByteArrayWrapper(Hex.decode(log.getTopics().get(2).substring(26)));
                        System.out.println("To test: 0x" + log.getTopics().get(2).substring(26));


                        //NFT transfer
                        if (!log.getAddress().equalsIgnoreCase(Settings.ticmintTokenAddress)) {
                            BigInteger nftId = (BigInteger) results.get(0).getValue();
                            Collection c = Collections.getCollectionByAddress(log.getAddress());

                            System.out.println("NFT Transferred from: 0x" + Hex.encode(NFTs.getNFTByCollectionAddressAndNftId(log.getAddress(), nftId.intValue()).getOwner().data()));
                            System.out.println("NFT receiver: 0x" + log.getTopics().get(2).substring(26));

                            c.transfer(nftId.intValue(), to);
                            Marketplace.Webhook.Sender.notifyOfNftTransfer(log.getTransactionHash(), log.getAddress(), "0x" + Hex.toHexString(to.data()), nftId.intValue());
                        }

                    }
                    // Ticket Mint
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xf1c13eb9fe4a39e476b8941315f9e7dc53628d2bb40b8bbf2208aa7af0aee3e8")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), TICKET_MINT.getNonIndexedParameters());
                        int nftId = Integer.parseInt(log.getTopics().get(1).substring(26), 16);

                        ByteArrayWrapper owner = new ByteArrayWrapper(Hex.decode(((String) results.get(0).getValue()).substring(2)));
                        String ticketType = (String) results.get(1).getValue();
                        String seatNumber = (String) results.get(2).getValue();
                        BigInteger purchasePrice = (BigInteger) results.get(3).getValue();
                        long purchaseTime = ((BigInteger) results.get(4).getValue()).longValue();

                        //     public Ticket(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash, String ticketType, String seat)
                        new Ticket(log.getAddress(), nftId, purchaseTime, purchasePrice, owner, log.getTransactionHash(), ticketType, seatNumber);

                        TicketType tt = TicketTypes.getTicketTypeByDatabaseId(log.getAddress() + "-" + ticketType);
                        tt.decrementAvailableSpaces();

                        //TicketingEvent.Webhook.Sender.notifyOfSuccessfulPurchase(log.getTransactionHash());

                        System.out.println("Adding ticket details to maps");
                        contractAddressByTxnHash.put(log.getTransactionHash(), log.getAddress());
                        List<String> ticketTypes = ticketTypesInTxn.get(log.getTransactionHash());
                        if (ticketTypes == null) ticketTypes = new ArrayList<>();
                        ticketTypes.add(ticketType);
                        ticketTypesInTxn.put(log.getTransactionHash(), ticketTypes);

                        List<Integer> nftIds = nftIdInTxn.get(log.getTransactionHash());
                        if (nftIds == null) nftIds = new ArrayList<>();
                        nftIds.add(nftId);
                        nftIdInTxn.put(log.getTransactionHash(), nftIds);
                        System.out.println("Done adding ticket details to map");

                        System.out.println("NFT Minted");
                    }
                    // Merchandise Mint
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x715e3ee0f5646157a56af644beeef944041fede0d1438f31ddb079d26fc222b7")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), MERCHANDISE_MINT.getNonIndexedParameters());
                        int nftId = new BigInteger(log.getTopics().get(1).substring(26), 16).intValue();
                        ByteArrayWrapper to = new ByteArrayWrapper(Hex.decode(((String) results.get(0).getValue()).substring(2)));
                        BigInteger purchasePrice = (BigInteger) results.get(1).getValue();
                        long purchaseTime = ((BigInteger) results.get(2).getValue()).longValue();

                        //    public NFT(String collectionAddress, int nftId, long mintingTime, BigInteger purchasePrice, ByteArrayWrapper owner, String txnHash) {
                        new NFT(log.getAddress(), nftId, purchaseTime, purchasePrice, to, log.getTransactionHash());
                    }
                    //Event End Time Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xc762ca24c3b6273f5c0b4d337ad2d0eb259d04155669eef2b2c593da2e455225")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), EVENT_END_TIME_UPDATE.getNonIndexedParameters());
                        BigInteger newEndTime = (BigInteger) results.get(0).getValue();

                        EventNFTCollection e = (EventNFTCollection) Collections.getCollectionByAddress(log.getAddress());
                        if (e == null) {
                            System.out.println("Event End Time Updated, but event not found");
                            continue;
                        }

                        e.setEndTime(newEndTime.longValue());
                        TicketingEvent.Webhook.Sender.notifyOfEventEndTimeUpdate(log.getAddress(), newEndTime.longValue(), log.getTransactionHash());
                        System.out.println("Event End Time Updated");
                    }
                    //Royalties Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x382d6d457eaa3c84d586de142a0d72bac72f2a514a1691f8ccf48feae833ff09")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), ROYALTIES_UPDATED.getNonIndexedParameters());
                        BigInteger newRoyalties = (BigInteger) results.get(0).getValue();

                        System.out.println("Royalties Updated, new royalties: " + newRoyalties);
                        //TODO webhook notification
                        //Royalties is not allowed to be updated, but i made it updatable just in case for the future
                    }
                    //Available Spaces Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xe93e137ad9e7cbdfd2055f0965a6c43e212982ee555a27b95450b6776e53da87")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), AVAILABLE_SPACES_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        BigInteger newAvailableSpaces = (BigInteger) results.get(1).getValue();

                        System.out.println("Available Spaces Updated for ticket type:" + ticketType + ", new available spaces: " + newAvailableSpaces);
                        try {TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setAvailableSpaces(newAvailableSpaces.intValue());} catch (Exception e) { e.printStackTrace(); }
                        TicketingEvent.Webhook.Sender.notifyOfAvailableSpacesUpdate(log.getAddress(), ticketType, newAvailableSpaces, log.getTransactionHash());
                    }
                    //Seating Info Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x598954af727868b90b48325457400db17e5821c505825df20ddcf174ff5ce33e")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SEATING_INFO_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        boolean seated = (boolean) results.get(1).getValue();

                        System.out.println("Seating Info Updated for ticket type: " + ticketType + ", new seating info: " + seated);
                        try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setSeated(seated); } catch (Exception e) { e.printStackTrace(); }
                        TicketingEvent.Webhook.Sender.notifyOfSeatedStateUpdate(log.getAddress(), ticketType, seated, log.getTransactionHash());
                    }
                    //Sale Start Time Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xa95ead81fecc85c7f12ead89d84ec37ff47b01c8ebc61538ccf5b54971dbc61a")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_START_TIME_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        BigInteger newStartTime = (BigInteger) results.get(1).getValue();

                        System.out.println("Sale Start Time Updated for ticket type: " + ticketType + ", new start time: " + newStartTime);
                        TicketingEvent.Webhook.Sender.notifyOfSaleStartTimeUpdate(log.getAddress(), ticketType, newStartTime, log.getTransactionHash());
                    }
                    //Sale End Time Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x9051da9e0ca64ffe3b17d2c730fcf58420dbb89066de41d504d14b74d1032245")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_END_TIME_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        BigInteger newEndTime = (BigInteger) results.get(1).getValue();

                        System.out.println("Sale End Time Updated for ticket type: " + ticketType + ", new end time: " + newEndTime);
                        TicketingEvent.Webhook.Sender.notifyOfSaleEndTimeUpdate(log.getAddress(), ticketType, newEndTime, log.getTransactionHash());
                    }
                    //Price Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x159e83f4712ba2552e68be9d848e49bf6dd35c24f19564ffd523b6549450a2f4")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), PRICE_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        BigInteger newPrice = (BigInteger) results.get(1).getValue();

                        System.out.println("Price Updated for ticket type: " + ticketType + ", new price: " + newPrice);
                        try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setPrice(newPrice); } catch (Exception e) { e.printStackTrace(); }
                        TicketingEvent.Webhook.Sender.notifyOfPriceUpdate(log.getAddress(), ticketType, newPrice, log.getTransactionHash());
                    }
                    //Secondary Market Price Cap Update
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x8350ddaff072473660b5b8f2b9fbab8ad16905e35a66d6000d898a675ae6cee3")) {
                        //TODO testing
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SECONDARY_MARKET_PRICE_CAP_UPDATED.getNonIndexedParameters());
                        String ticketType = (String) results.get(0).getValue();
                        BigInteger newPriceCap = (BigInteger) results.get(1).getValue();

                        System.out.println("Secondary Market Price Cap Updated for ticket type: " + ticketType + ", new price cap: " + newPriceCap);
                        try { TicketTypes.getTicketTypeByCollectionAddressAndTicketTypeId(log.getAddress(), ticketType).setSecondaryMarketPriceCap(newPriceCap); } catch (Exception e) { e.printStackTrace(); }
                        TicketingEvent.Webhook.Sender.notifyOfSecondaryMarketPriceCapUpdate(log.getAddress(), ticketType, newPriceCap, log.getTransactionHash());
                    }
                    //Refund Complete
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xa02c9ec5aecd7764ac4d99b6772ea8297bec9ad448f9da221c7b2329400c3653")) {
                        //TODO testing
                        TicketingEvent.Webhook.Sender.notifyOfRefundCompletion(log.getTransactionHash());
                    }
                    //Merchandise Max Supply Changed
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x28a10a2e0b5582da7164754cb994f6214b8af6aa7f7e003305fbc09e7106c513")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), MAX_SUPPLY_CHANGED.getNonIndexedParameters());
                        BigInteger newMaxSupply = (BigInteger) results.get(0).getValue();

                        System.out.println("Max Supply Updated to: " + newMaxSupply);


                        MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                        m.setMaxSupply(newMaxSupply.intValue());

                        Sender.notifyOfMaxSupplyChange(log.getAddress(), newMaxSupply.intValue(), log.getTransactionHash());
                    }
                    //Merchandise Price Changed
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xa6dc15bdb68da224c66db4b3838d9a2b205138e8cff6774e57d0af91e196d622")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), PRICE_CHANGED.getNonIndexedParameters());
                        BigInteger newPrice = (BigInteger) results.get(0).getValue();

                        System.out.println("Price Updated to: " + newPrice);

                        MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                        m.setPrice(newPrice);

                        Sender.notifyOfPriceChange(log.getAddress(), newPrice, log.getTransactionHash());
                    }
                    //Merchandise Sale Start Time Changed
                    else if (log.getTopics().get(0).equalsIgnoreCase("0xf9aef9ba2f8a1311aed781e9d5eb6e91969cf38d9699207a9fe2e5d3357bba12")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_START_TIME_CHANGED.getNonIndexedParameters());
                        BigInteger newSaleStartTime = (BigInteger) results.get(0).getValue();

                        System.out.println("Sale Start Time Updated to: " + newSaleStartTime);

                        MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                        m.setSaleStartTime(newSaleStartTime.longValue());

                        Sender.notifyOfSaleStartTimeChange(log.getAddress(), newSaleStartTime, log.getTransactionHash());
                    }
                    //Merchandise Sale End Time Changed
                    else if (log.getTopics().get(0).equalsIgnoreCase("0x6578ad4f74a92966ef7c4f4956e0a576592b0ac12c8c541cfb3974adc2b0b561")) {
                        List<Type> results = FunctionReturnDecoder.decode(log.getData(), SALE_END_TIME_CHANGED.getNonIndexedParameters());
                        BigInteger newSaleEndTime = (BigInteger) results.get(0).getValue();

                        System.out.println("Sale End Time Updated to: " + newSaleEndTime);

                        MerchandiseCollection m = (MerchandiseCollection) Collections.getCollectionByAddress(log.getAddress());
                        m.setSaleEndTime(newSaleEndTime.longValue());

                        Sender.notifyOfSaleEndTimeChange(log.getAddress(), newSaleEndTime, log.getTransactionHash());
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }

            }

            if(contractAddressByTxnHash.keySet().size() > 0) {
                for (String txnHash : ticketTypesInTxn.keySet()) {
                    System.out.println("Notifying of ticket creation");
                    TicketingEvent.Webhook.Sender.notifyOfTicketCreation(txnHash, contractAddressByTxnHash.get(txnHash), nftIdInTxn.get(txnHash), ticketTypesInTxn.get(txnHash));
                }
            } else {
                System.out.println("No new tickets created in this new block fetch");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final Event TRANSFER = new Event("Transfer",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event NEW_TICKET_SALE_EVENT = new Event("NewTicketSale",
            Arrays.asList(
                    new TypeReference<Address>(true) {
                    },
                    new TypeReference<DynamicArray<Uint256>>(false) {
                    },
                    new TypeReference<DynamicArray<Utf8String>>(false) {
                    }
            )
    );

    public static final Event NEW_MERCHANDISE = new Event("NewMerchandise",
            Arrays.asList(
                    new TypeReference<Address>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event TICKET_MINT = new Event("TicketMint",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {},     // uint256 indexed nftId
                    new TypeReference<Address>(false) {},    // address to
                    new TypeReference<Utf8String>(false) {}, // string ticketType
                    new TypeReference<Utf8String>(false) {}, // string seatId
                    new TypeReference<Uint256>(false) {},    // uint256 purchasePrice
                    new TypeReference<Uint256>(false) {}     // uint256 purchaseTime
            )
    );

    public static final Event MERCHANDISE_MINT = new Event("MerchandiseMint",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {},     // uint256 indexed nftId
                    new TypeReference<Address>(false) {},    // address to
                    new TypeReference<Uint256>(false) {},    // uint256 purchasePrice
                    new TypeReference<Uint256>(false) {}     // uint256 purchaseTime
            )
    );

    //   event EventEndTimeUpdated(uint256 newEndTime);
    public static final Event EVENT_END_TIME_UPDATE = new Event("EventEndTimeUpdated",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event ROYALTIES_UPDATED = new Event("RoyaltiesUpdated",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event AVAILABLE_SPACES_UPDATED = new Event("AvailableSpacesUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SEATING_INFO_UPDATED = new Event("SeatingInfoUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Bool>(false) {
                    }
            )
    );

    public static final Event SALE_START_TIME_UPDATED = new Event("SaleStartTimeUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SALE_END_TIME_UPDATED = new Event("SaleEndTimeUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event PRICE_UPDATED = new Event("PriceUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SECONDARY_MARKET_PRICE_CAP_UPDATED = new Event("SecondaryMarketPriceCapUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    //    event BuyAddOn(address to, string nameAndId, uint256 amount, uint256 price);
    public static final Event BUY_ADD_ON = new Event("BuyAddOn",
            Arrays.asList(
                    new TypeReference<Address>(false) {
                    },
                    new TypeReference<Utf8String>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    },
                    new TypeReference<Uint256>(false) {
                    }
            )
    );


    public static final Event MAX_SUPPLY_CHANGED = new Event("MaxSupplyChanged",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event PRICE_CHANGED = new Event("PriceChanged",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SALE_START_TIME_CHANGED = new Event("SaleStartTimeChanged",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SALE_END_TIME_CHANGED = new Event("SaleEndTimeChanged",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event SECONDARY_MARKET_PRICE_CAP_CHANGED = new Event("SecondaryMarketPriceCapChanged",
            Arrays.asList(
                    new TypeReference<Uint256>(false) {
                    }
            )
    );

    public static final Event BuyAdditionalServices = new Event("BuyAdditionalServices",
            Arrays.asList(
                    new TypeReference<Address>(false) {},
                    new TypeReference<Address>(false) {},
                    new TypeReference<DynamicArray<Utf8String>>(false) {},
                    new TypeReference<DynamicArray<Uint256>>(false) {}
            )
    );

    public static final Event BuyAddOn = new Event("BuyAddOn",
            Arrays.asList(
                    new TypeReference<Address>(false) {},
                    new TypeReference<Address>(false) {},
                    new TypeReference<DynamicArray<Utf8String>>(false) {},
                    new TypeReference<DynamicArray<Uint256>>(false) {}
            )
    );

    public static final Event TradeComplete = new Event("TradeComplete",
            Arrays.asList(
                    new TypeReference<Address>(false) {}, //Seller
                    new TypeReference<Address>(false) {}, //Buyer
                    new TypeReference<Address>(false) {}, //Collection Address
                    new TypeReference<Uint256>(false) {}, //Nft Id
                    new TypeReference<Uint256>(false) {} //Price
            )
    );


}
