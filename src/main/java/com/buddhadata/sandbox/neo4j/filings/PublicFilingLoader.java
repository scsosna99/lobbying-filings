/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings;

import com.buddhadata.sandbox.neo4j.filings.node.*;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import generated.*;

import org.neo4j.driver.Driver;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Scott C Sosna
 */
public class PublicFilingLoader {

    /**
     * Cache for tracking what clients and retrieving from database only when necessary.
     */
    private LoadingCache<String,Client> clientCache;

    /**
     * Cache for tracking what government entities already exist and retrieving from database only when necessary.
     */
    private LoadingCache<String,GovernmentEntity> gentCache;

    /**
     * Cache for tracking what issues already exist and retrieving from database only when necessary.
     */
    private LoadingCache<String, Issue> issueCache;

    /**
     * Cache for tracking what lobbyists already exist and retrieving from database only when necessary.
     */
    private LoadingCache<LobbyistKey, Lobbyist> lobbyistCache;

    /**
     * Cacghe for tracking what registrants already exist and retrieving from database only when necessary.
     */
    private LoadingCache<Long, Registrant> registrantCache;

    /**
     * Neo4J session factory for the entire processing.  It's a member variable to allow easy access to it from whereever, in
     * particular when using the Guava caching to retrieve values.
     */
    private final SessionFactory sessionFactory;

    /**
     * Unmarshaller to use.  Unmarshallers are not thread-safe but we are single-threaded here
     */
    private Unmarshaller unmarshaller;

    /**
     * Level of concurrency, how many threads in executor are running at once
     */
    static private final int CONCURRENCY_THREAD_COUNT = 1;

    //  Configuration info for connecting to the Neo4J database
    static private final String SERVER_URI = "bolt://127.0.0.1";
    static private final String SERVER_USERNAME = "neo4j";
    static private final String SERVER_PASSWORD = "benchmark";

    //  Client query
    static private final String CLIENT_INDEX = "CREATE INDEX ON :Client (name)";
    static private final String CLIENT_QUERY = "MATCH (c:Client {name:$name}) RETURN c";
    static private final String CLIENT_PARAM_NAME = "name";

    //  Issue query
    static private final String ISSUE_QUERY = "MATCH (i:Issue {code:$code}) RETURN i";
    static private final String ISSUE_PARAM_CODE = "code";

    //  Lobbyist Entity query
    static private final String LOBBYIST_INDEX = "CREATE INDEX ON :Lobbyist (surname, firstName)";
    static private final String LOBBYIST_QUERY = "MATCH (l:Lobbyist {surname:$surname,firstName:$firstName}) RETURN l";
    static private final String LOBBYIST_PARAM_FIRSTNAME = "firstName";
    static private final String LOBBYIST_PARAM_SURNAME = "surname";

    //  Government Entity query
    static private final String ENTITY_QUERY = "MATCH (ge:GovernmentEntity {name:$name}) RETURN ge";
    static private final String ENTITY_PARAM_NAME = "name";

    //  Registrant node query
    static private final String REGISTRANT_INDEX = "CREATE INDEX ON :Registrant (registrantId)";
    static private final String REGISTRANT_QUERY = "MATCH (reg:Registrant {registrantId:$id}) RETURN reg";
    static private final String REGISTRANT_PARAM_NAME = "id";

    /**
     * Constructor
     */
    public PublicFilingLoader() {

        //  Only need one JAXBContext for all the files read.
        try {
            JAXBContext context = JAXBContext.newInstance("generated");
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            System.out.println ("Exception creating JAXB Context: " + e);
        }

        //  Create a session factory
        Configuration configuration = new Configuration.Builder()
            .uri(SERVER_URI)
            .credentials(SERVER_USERNAME, SERVER_PASSWORD)
            .useNativeTypes()
            .build();
        sessionFactory = new SessionFactory(configuration, "com.buddhadata.sandbox.neo4j.filings.node", "com.buddhadata.sandbox.neo4j.filings.relationship");

        //  Get all the caches defined with the appropriate loaders to use during processing
        createCaches();
    }

    /**
     * Define the caches to minimize database accesses on reused Neo4J objects.
     */
    private void createCaches () {

        //  Create the client cache using Guava.
        clientCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .build(key -> sessionFactory.openSession().queryForObject (Client.class, CLIENT_QUERY,
                        Collections.singletonMap(CLIENT_PARAM_NAME, key))
                );

        gentCache = Caffeine.newBuilder()
                .maximumSize(250)
                .build(key -> sessionFactory.openSession().queryForObject (GovernmentEntity.class, ENTITY_QUERY,
                        Collections.singletonMap(ENTITY_PARAM_NAME, key))
                );

        issueCache = Caffeine.newBuilder()
                .maximumSize(250)
                .build(key ->  sessionFactory.openSession().queryForObject (Issue.class, ISSUE_QUERY,
                        Collections.singletonMap(ISSUE_PARAM_CODE, key))
                );

        lobbyistCache = Caffeine.newBuilder()
                .maximumSize(250)
                .build(key -> {
                            //  Create map to hold parameters, there are always two parts of the key and, if there
                            //  aren't two, all sorts of nastyness will occur.
                            Map<String,Object> params = Map.of(
                                LOBBYIST_PARAM_SURNAME, key.getSurname(),
                                LOBBYIST_PARAM_FIRSTNAME, key.getFirstName()
                            );

                            //  Execute query and hope for the best
                            return sessionFactory.openSession().queryForObject (Lobbyist.class, LOBBYIST_QUERY, params);
                        }
                );

        registrantCache = Caffeine.newBuilder()
                .maximumSize(250)
                .build(key -> sessionFactory.openSession().queryForObject (Registrant.class, REGISTRANT_QUERY,
                        Collections.singletonMap(REGISTRANT_PARAM_NAME, key))
                );
    }

    private void purgeDatabase () {
        try {
            Driver driver = sessionFactory.unwrap(Driver.class);
            try(org.neo4j.driver.Session session = driver.session()) {
                session.run("MATCH (n) DETACH DELETE n");
            }
        } catch (Exception e) {
            System.out.println ("Error creating purging database: " + e);
        }
    }

    /**
     * Create indices useful for querying commonly-accessed nodes while loading data
     */
    private void createIndices () {
        try {
            Driver driver = sessionFactory.unwrap(Driver.class);
            try(org.neo4j.driver.Session session = driver.session()) {
                session.run(CLIENT_INDEX);
                session.run(LOBBYIST_INDEX);
                session.run(REGISTRANT_INDEX);
            }
        } catch (Exception e) {
            System.out.println ("Error creating indicies: " + e);
        }
    }

    /**
     * Main method for processing a public filings XML file.  Files available at https://www.senate.gov/legislative/Public_Disclosure/database_download.htm
     */
    private void process () {

        //  Always clean up by purging the database.
        purgeDatabase();
        //  Indices on a few nodes should help querying existing nodes when reusing between filings.
        createIndices();

        //  If you want to process individual files, do this.
        //  processFilings (getPublicFilings (new File("/Users/scsosna/data/src/github/opendata-neo4j/filings/src/main/resources/data/2018_3_7_16.xml")), "2018_2_4_8.xml");
        //  processFilings (getPublicFilings (new File("/Users/scsosna/data/src/github/opendata-neo4j/filings/src/main/resources/data/2018_2_4_8.xml")), "2018_2_4_8.xml");

        //  Process the files of interest
        processZipFile("2015_1.zip");
        processZipFile("2015_2.zip");
        processZipFile("2015_3.zip");
        processZipFile("2015_4.zip");
        processZipFile("2016_1.zip");
        processZipFile("2016_2.zip");
        processZipFile("2016_3.zip");
        processZipFile("2016_4.zip");
        processZipFile("2017_1.zip");
        processZipFile("2017_2.zip");
        processZipFile("2017_3.zip");
        processZipFile("2017_4.zip");
        processZipFile("2018_1.zip");
        processZipFile("2018_2.zip");
        processZipFile("2018_3.zip");
        processZipFile("2018_4.zip");
        processZipFile("2019_1.zip");
        processZipFile("2019_2.zip");
        processZipFile("2019_3.zip");
    }

    /**
     * Process the individual filings loaded from a single XML file (either real file or zip entry).
     * @param filings the filings to be processed
     */
    private void processFilings (PublicFilings filings,
                                 String sourceName) {

        if (filings != null) {
            try {
                //  Uncomment this if you want to see logging information on internal MappingContext collection
                //queryClearMappingContext(false, false);

                System.out.print("Processing " + sourceName + ": ");
                long start = System.currentTimeMillis();

                for (FilingType one : filings.getFiling()) {

                    Session session = sessionFactory.openSession();

                    //  Filings with no amount specified are those filed indicating no lobbying activty
                    //  by the registrant in the current quarter
                    if (one.getAmount() != null && !one.getAmount().isEmpty()) {

                        //  One transaction per filing
                        Transaction txn = session.beginTransaction();

                        //  Get/create the client
                        Client client = findOrCreateClient(one.getClient());

                        //  Create the filing
                        Filing filing = createFiling(one, client);

                        //  Get the registrant and assign to the filing
                        Registrant registrant = findOrCreateRegistrant(one.getRegistrant(), client);
                        filing.setRegistrant(registrant);


                        //  Make sure a lobbyist node exists for all lobbyists associated with filing.
                        if (one.getLobbyists() != null && one.getLobbyists().getLobbyist() != null) {
                            for (LobbyistType l : one.getLobbyists().getLobbyist()) {
                                Lobbyist lobbyist = findOrCreateLobbyist(l, registrant);
                                if (lobbyist != null) {
                                    filing.getLobbyists().add(lobbyist);
                                }
                            }
                        }

                        //  Get the government entities referenced in the file.
                        if (one.getGovernmentEntities() != null) {
                            for (GovernmentEntityType entity : one.getGovernmentEntities().getGovernmentEntity()) {
                                filing.getEntities().add(findOrCreateEntity(entity));
                            }
                        }

                        //  Are there issues associated with the filing?

                        if (one.getIssues() != null && one.getIssues().getIssue() != null) {
                            for (IssueType iss : one.getIssues().getIssue()) {

                                //  Create relationship between filing/issue for the specific description of issue.
                                filing.getIssues().add(findOrCreateIssue(iss.getCode()));
                            }
                        }


                        //  Upon completion of the filing, resave with the updated info.
                        session.save(filing);
                        txn.commit();
                    }
                }

                System.out.println (String.format("%d filings in %d ms", filings.getFiling().size(),
                        (System.currentTimeMillis() - start)));
            } catch (Exception e) {
                System.out.println ("Exception processing " + sourceName + ": " + e);
                e.printStackTrace();
            }
        } else {
            System.out.println ("null filings provided");
        }
    }

    /**
     * Processes a single zip file found as classpath resource.
     * @param fileName the zip file to process
     */
    private void processZipFile(String fileName) {
        try (ZipInputStream zis = openZipResource(fileName)) {

            if (zis != null) {

                //  Process all the files (zip entries) within the zip file (zip input stream)
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    //  Unmarshall the XML document into objects that are easier to work with.
                    PublicFilings filings = getPublicFilings(zis);

                    //  Process
                    processFilings(filings, ze.getName());
                }
            } else {
                System.out.println ("No files unmarshalled.");
            }
        } catch (Throwable e) {
            System.out.println ("Exception while processing public filings: " + e);
        }
    }

    /**
     * Unmarshall the XML file to get the filings data contained within
     * @param xmlFile the XML file to process
     * @return PublicFilings object with 1 or more filings
     */
    private PublicFilings getPublicFilings (File xmlFile) {

        PublicFilings toReturn = null;
        try (Reader rdr = new InputStreamReader (new FileInputStream(xmlFile), Charset.forName("UTF-16"))) {
            toReturn = (PublicFilings) unmarshaller.unmarshal(rdr);
        } catch (IOException ioe) {
            System.out.println ("IOException reading file: " + ioe);
        } catch (JAXBException je) {
            System.out.println ("JAXB Exception processing XML: " + je);
        }


        return toReturn;
    }

    /**
     * Unmarshall the filings data from the original XML
     * @param zis the stream from which each entry is read
     * @return PublicFilings object with 1 or more filings
     */
    private PublicFilings getPublicFilings (ZipInputStream zis) {

        try {

            return (PublicFilings) unmarshaller.unmarshal(new FilterInputStream(zis) {
                @Override public void close() {
                }
            });
        } catch (Exception e) {
            System.out.println ("Exception while unmarshalling: " + e);
        }
        return null;
    }

    /**
     * Find the files in the given folder, which should be nothing but XML used for loading Neo4J
     * @param resourceFolder the name of the folder where the filing data files are found
     * @return array of 0 or more files that need to be processed
     */
    private List<File> findDataResources (String resourceFolder) {

        //  The individual filings XML files, unzipped from the original downloaded zip files, are assumed to be in a
        //  folder with passed in name.
        File folder = new File (Thread.currentThread().getContextClassLoader().getResource(resourceFolder).getPath());

        //  Return all the files - which should be just XML - in the given folder file, sorted.
        List<File> toReturn = Arrays.asList(folder.listFiles());
        toReturn.sort(Comparator.comparing(File::getName));
        return toReturn;
    }


    /**
     * Open the zip file as a resource which we can return as a stream for processing
     * @param zipFileName the zip file to process
     * @return a ZipEntryStream
     */
    private ZipInputStream openZipResource (String zipFileName) {

        try {
            return new ZipInputStream(ClassLoader.getSystemResourceAsStream(zipFileName));
        } catch (Exception e) {
            System.out.println ("Exception while retrieving zip file: " + e);
            return null;
        }
    }

    /**
     * Create a new filing.  Should never have to search for an existing because it's expected that each filing is
     * new and doesn't already exist.  However, would need to process a whole bunch more data to prove.
     * @param ft a Filing from the intial source data
     * @param client for whom the filing was made
     * @return newly-created <code>Filing</code>
     */
    private Filing createFiling (FilingType ft, Client client) {

        String amount = ft.getAmount();
        XMLGregorianCalendar c = ft.getReceived();
        LocalDateTime receivedOn = LocalDateTime.of(c.getYear(), c.getMonth(), c.getDay(), c.getHour(), c.getMinute(), c.getSecond());
        Filing toReturn = new Filing (ft.getID(), ft.getYear(), receivedOn,
            Integer.valueOf(amount), ft.getType(), ft.getPeriod(), client);
        return toReturn;
    }

    /**
     * Either find an exist or create a new client, based on the client information read from the source data.
     * @param client client object read from source data
     * @return <code>Client</code>
     */
    private Client findOrCreateClient (ClientType client) {

        //  Clients are unique by ID, attempt to find in cache or database
        String clientName = client.getClientName().trim();
        return clientCache.get(clientName, k ->
            new Client (client.getClientID(), k, client.getGeneralDescription(), client.getContactFullname(),
                client.getClientCountry(), client.getClientPPBCountry(), client.getClientState(), client.getClientPPBState(),
                Boolean.valueOf(client.getSelfFiler()), Boolean.valueOf(client.getIsStateOrLocalGov())));
    }

    /**
     * Either find an existing or create a new lobbyist, based on the first/surname of the lobbyist
     * @param lobbyist the lobbyist entity from the data file
     * @return <code>Lobbyist</code>
     */
    private Lobbyist findOrCreateLobbyist (LobbyistType lobbyist,
                                           Registrant registrant) {

        Lobbyist toReturn = null;
        String name = lobbyist.getLobbyistName().toUpperCase();

        //  Possible that no lobbyist name provided, in which case there's no work to do.
        if (!name.isEmpty()) {

            //  Lobbyist names are "surname,first" so break them apart by finding ','
            int comma = name.indexOf(',');
            String firstName = name.substring(comma + 1).trim();
            String surname = name.substring(0, comma).trim();
            LobbyistKey key = new LobbyistKey(firstName, surname);

            toReturn = lobbyistCache.get(key, k -> new Lobbyist(firstName, surname, lobbyist.getLobbyistCoveredGovPositionIndicator(),
                        lobbyist.getOfficialPosition(), lobbyist.getActivityInformation()));

            //  Add the registrant to the set of all registrants who employ the lobbyist.  As a set, can just do a put
            //  and don't need to check whether it's already there.  Then save the lobbyist
            toReturn.getEmployers().add(registrant);
        }

        return toReturn;
    }

    /**
     * Either find an existing or create a new registrant, based on the registrant ID in the source data
     * @param governmentEntity the government entity from the data file
     * @return <code>GovernmentEntity</code>
     */
    private GovernmentEntity findOrCreateEntity (GovernmentEntityType governmentEntity) {
        return gentCache.get(governmentEntity.getGovEntityName(), GovernmentEntity::new);
    }

    /**
     * Either find exist or create a new issue based on the government-defined issue code.
     * @param issueCode uniquely identifies the issue
     * @return
     */
    private Issue findOrCreateIssue (String issueCode) {

        return issueCache.get(issueCode, Issue::new);
    }

    /**
     * Either find an existing or create a new registrant, based on the registrant ID in the source data
     * @param registrant the registrant from the data file
     * @param client engaged this registrant for lobbying
     * @return <code>Registrant</code>
     */
    private Registrant findOrCreateRegistrant (RegistrantType registrant,
                                               Client client) {

        Registrant toReturn = registrantCache.get(registrant.getRegistrantID(), k -> new Registrant(k, registrant.getRegistrantName(), registrant.getGeneralDescription(),
                    registrant.getAddress(), registrant.getRegistrantCountry(), registrant.getRegistrantPPBCountry()));

        //  Add the client to the set who employ the registrant.  Because it's a set, no need to check for existance,
        //  just put and then save.
        toReturn.getClients().add(client);
        return toReturn;
    }

    /**
     * Program main program
     * @param args command line arguments
     */
    public static void main (String[] args) {
        new PublicFilingLoader().process();
    }
}
