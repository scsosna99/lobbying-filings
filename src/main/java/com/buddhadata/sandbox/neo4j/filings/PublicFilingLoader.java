/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings;

import com.buddhadata.sandbox.neo4j.filings.node.*;
import com.buddhadata.sandbox.neo4j.filings.relationship.FilingIssue;
import generated.*;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Scott C Sosna
 */
public class PublicFilingLoader {

    //  Configuration info for connecting to the Neo4J database
    static private final String SERVER_URI = "bolt://localhost";
    static private final String SERVER_USERNAME = "neo4j";
    static private final String SERVER_PASSWORD = "password";

    //  Client query
    static private final String CLIENT_QUERY = "MATCH (c:Client {clientId:$clientId}) RETURN c";
    static private final String CLIENT_PARAM_ID = "clientId";

    //  Issue query
    static private final String ISSUE_QUERY = "MATCH (i:Issue {code:$code}) RETURN i";
    static private final String ISSUE_PARAM_CODE = "code";

    //  Lobbyist Entity query
    static private final String LOBBYIST_QUERY = "MATCH (l:Lobbyist {surname:$surname,firstName:$firstName}) RETURN l";
    static private final String LOBBYIST_PARAM_FIRSTNAME = "firstName";
    static private final String LOBBYIST_PARAM_SURNAME = "surname";

    //  Government Entity query
    static private final String ENTITY_QUERY = "MATCH (ge:GovernmentEntity {name:$name}) RETURN ge";
    static private final String ENTITY_PARAM_NAME = "name";

    //  Registrant node query
    static private final String REGISTRANT_QUERY = "MATCH (reg:Registrant {registrantId:$id}) RETURN reg";
    static private final String REGISTRANT_PARAM_NAME = "id";


    private JAXBContext context;
    /**
     * Session factory for connecting to Neo4j database
     */
    private final SessionFactory sessionFactory;


    /**
     * Constructor
     */
    public PublicFilingLoader() {

        //  Only need one JAXBContext for all the files read.
        try {
            context = JAXBContext.newInstance("generated");
        } catch (JAXBException e) {
            System.out.println ("Exception creating JAXB Context: " + e);
        }
        //  Define session factory for connecting to Neo4j database
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "com.buddhadata.sandbox.neo4j.filings.node", "com.buddhadata.sandbox.neo4j.filings.relationship");
    }

    /**
     * Main method for processing a public filings XML file.  Files available at https://www.senate.gov/legislative/Public_Disclosure/database_download.htm
     */
    private void process () {

        Transaction txn = null;
        try (ZipInputStream zis = openZipResource("2019_2.zip")) {
            //  When creating a session, always clean up the database by purging the database.
            Session session = sessionFactory.openSession();
            session.purgeDatabase();

            if (zis != null) {

                //  Process all the files (zip entries) within the zip file (zip input stream)
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    System.out.println ("Processing " + ze.getName());

                    //  Unmarshall the XML document into objects that are easier to work with.
                    PublicFilings filings = getPublicFilings(zis, ze);
                    if (filings != null) {
                        for (FilingType one : filings.getFiling()) {
                            //  Make each filing its own transaction
                            txn = session.beginTransaction();

                            //  Get/create the client
                            Client client = findOrCreateClient(session, one.getClient());
                            session.save(client);

                            //  Create the filing
                            Filing filing = createFiling(session, one, client);

                            //  Get the registrant and assign to the filing
                            Registrant registrant = findOrCreateRegistrant(session, one.getRegistrant(), client);
                            filing.setRegistrant(registrant);

                            //  Make sure a lobbyist node exists for all lobbyists associated with filing.
                            if (one.getLobbyists() != null && one.getLobbyists().getLobbyist() != null) {
                                for (LobbyistType l : one.getLobbyists().getLobbyist()) {
                                    Lobbyist lobbyist = findOrCreateLobbyist(session, l, registrant);
                                    filing.getLobbyists().add(lobbyist);
                                }
                            }

                            //  Get the government entities referenced in the file.
                            if (one.getGovernmentEntities() != null) {
                                for (GovernmentEntityType entity : one.getGovernmentEntities().getGovernmentEntity()) {
                                    GovernmentEntity ge = findOrCreateEntity(session, entity);
                                    filing.getEntities().add(ge);
                                }
                            }

                            //  Are there issues associated with the filing?
                            if (one.getIssues() != null && one.getIssues().getIssue() != null) {
                                for (IssueType iss : one.getIssues().getIssue()) {

                                    //  Need to get the issue.
                                    Issue issue = findOrCreateIssue(session, iss.getCode());

                                    //  Create relationship between filing/issue for the specific description of issue.
                                    session.save(new FilingIssue(filing, issue, iss.getSpecificIssue()));
                                }
                            }


                            //  Upon completion of the filing, resave with the updated info and commit.
                            session.save(filing);
                            txn.commit();
                            txn = null;
                        }
                    }
                }
            } else {
                System.out.println ("No files unmarshalled.");
            }
        } catch (Throwable e) {
            System.out.println ("Exception while processing public filings: " + e);
        } finally {
            //  We shouldn't have a transaction here unless an exception occurred
            if (txn != null) {
                txn.rollback();
            }
        }
    }

    /**
     * Unmarshall the filings data from the original XML
     * @param zis the stream from which each entry is read
     * @param ze the zip file entry containing important information about the zip'ed file
     * @return PublicFilings object with 1 or more filings
     */
    private PublicFilings getPublicFilings (ZipInputStream zis,
                                            ZipEntry ze) {

        PublicFilings toReturn = null;

        try {
            //  First, read the bytes for this zip entry.
            byte[] bytes = new byte[(int) ze.getSize() + 2048];
            int offset = 0;
            int read = 0;
            while ((read = zis.read(bytes, offset, 2048)) >= 0) {
                offset += read;
            }

            //  Create a reader to stream the bytes and deserialize the XML.
            try (Reader rdr = new InputStreamReader (new ByteArrayInputStream(bytes, 0, offset), Charset.forName("UTF-16"))) {
                toReturn = (PublicFilings) context.createUnmarshaller().unmarshal(rdr);
            }
        } catch (Exception e) {
            System.out.println ("Exception while unmarshalling: " + e);
        }


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
     * @param session Neo4J database session
     * @param ft a Filing from the intial source data
     * @param client for whom the filing was made
     * @return newly-created <code>Filing</code>
     */
    private Filing createFiling (Session session,
                                 FilingType ft,
                                 Client client) {

        String amount = ft.getAmount();
        Filing toReturn = new Filing (ft.getID(), ft.getYear(), ft.getReceived().toGregorianCalendar().getTime(),
                amount != null && !amount.isEmpty() ? Integer.valueOf(amount) : 0,
                ft.getType(), ft.getPeriod(), client);
        session.save(toReturn);
        return toReturn;
    }

    /**
     * Either find an exist or create a new client, based on the client information read from the source data.
     * @param session Neo4J session
     * @param client client object read from source data
     * @return <code>Client</code>
     */
    private Client findOrCreateClient (Session session,
                                       ClientType client) {

        //  Clients are unique by ID, so query by that to find uniqueness
        Client toReturn = session.queryForObject (Client.class, CLIENT_QUERY,
            Collections.singletonMap(CLIENT_PARAM_ID, client.getClientID()));

        //  If client doesn't already exist, create a new one.
        if (toReturn == null) {
            toReturn = new Client (client.getClientID(), client.getClientName(), client.getGeneralDescription(), client.getContactFullname(),
                client.getClientCountry(), client.getClientPPBCountry(), client.getClientState(), client.getClientPPBState(),
                Boolean.valueOf(client.getSelfFiler()), Boolean.valueOf(client.getIsStateOrLocalGov()));
            session.save (toReturn);
        }


        return toReturn;
    }

    /**
     * Either find an existing or create a new lobbyist, based on the first/surname of the lobbyist
     * @param session Neo4J session
     * @param lobbyist the lobbyist entity from the data file
     * @return <code>Lobbyist</code>
     */
    private Lobbyist findOrCreateLobbyist (Session session,
                                           LobbyistType lobbyist,
                                           Registrant registrant) {
        Lobbyist toReturn;

        //  Lobbyist names are "surname,first" so break them apart by finding ','
        String name = lobbyist.getLobbyistName();
        int comma = name.indexOf(',');
        String surname = name.substring(0, comma).trim().toUpperCase();
        String firstName = name.substring (comma + 1).trim().toUpperCase();

        //  Create map to hold parameters
        Map<String,Object> params = new HashMap<>(2);
        params.put (LOBBYIST_PARAM_SURNAME, surname);
        params.put (LOBBYIST_PARAM_FIRSTNAME, firstName);

        //  Execute query and hope for the best
        toReturn = session.queryForObject (Lobbyist.class, LOBBYIST_QUERY, params);

        //  If lobbyist doesn't already exist, create a new one.
        if (toReturn == null) {
            toReturn = new Lobbyist(firstName, surname, lobbyist.getLobbyistCoveredGovPositionIndicator(),
                lobbyist.getOfficialPosition(), lobbyist.getActivityInformation());
            toReturn.getEmployers().add(registrant);
            session.save (toReturn);
        } else {
            //  Is the lobbyist already employed by the registrant?
            if (!toReturn.getEmployers().contains(registrant)) {
                toReturn.getEmployers().add(registrant);
                session.save (toReturn);
            }
        }


        return toReturn;
    }

    /**
     * Either find an existing or create a new registrant, based on the registrant ID in the source data
     * @param session Neo4J session
     * @param governmentEntity the government entity from the data file
     * @return <code>GovernmentEntity</code>
     */
    private GovernmentEntity findOrCreateEntity (Session session,
                                                 GovernmentEntityType governmentEntity) {

        //  Execute query and hope for the best
        GovernmentEntity toReturn = session.queryForObject (GovernmentEntity.class, ENTITY_QUERY,
            Collections.singletonMap(ENTITY_PARAM_NAME, governmentEntity.getGovEntityName()));

        //  If government entity node doesn't exist, create a new one.
        if (toReturn == null) {
            toReturn = new GovernmentEntity(governmentEntity.getGovEntityName());
            session.save(toReturn);
        }


        return toReturn;
    }

    /**
     * Either find exist or create a new issue based on the government-defined issue code.
     * @param session Neo4J session
     * @param issueCode uniquely identifies the issue
     * @return
     */
    private Issue findOrCreateIssue (Session session,
                                     String issueCode) {

        //  Execute query and hope for the best
        Issue toReturn = session.queryForObject (Issue.class, ISSUE_QUERY,
            Collections.singletonMap(ISSUE_PARAM_CODE, issueCode));

        //  If issue does not already exist, create
        if (toReturn == null) {
            toReturn = new Issue (issueCode);
            session.save (toReturn);
        }


        return toReturn;
    }

    /**
     * Either find an existing or create a new registrant, based on the registrant ID in the source data
     * @param session Neo4J session
     * @param registrant the registrant from the data file
     * @param client engaged this registrant for lobbying
     * @return <code>Registrant</code>
     */
    private Registrant findOrCreateRegistrant (Session session,
                                               RegistrantType registrant,
                                               Client client) {

        //  Execute query and hope for the best
        Registrant toReturn = session.queryForObject (Registrant.class, REGISTRANT_QUERY,
            Collections.singletonMap(REGISTRANT_PARAM_NAME, registrant.getRegistrantID()));

        //  If registrant node doesn't exist, create a new one.
        if (toReturn == null) {
            toReturn = new Registrant(registrant.getRegistrantID(), registrant.getRegistrantName(), registrant.getGeneralDescription(),
                    registrant.getAddress(), registrant.getRegistrantCountry(), registrant.getRegistrantPPBCountry());
            toReturn.getClients().add(client);
            session.save(toReturn);
        } else {
            if (!toReturn.getClients().contains(client)) {
                toReturn.getClients().add(client);
                session.save(toReturn);
            }
        }


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
