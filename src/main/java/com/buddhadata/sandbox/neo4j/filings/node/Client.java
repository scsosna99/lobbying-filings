/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Node representing a client who's lobbying (lobbying done by a registrant
 *
 * @author Scott C Sosna
 */
@NodeEntity
public class Client {

    /**
     * Does the client file the disclosures themselves or through a separate registrant
     */
    @Property
    private boolean selfFilerInd;

    /**
     * Is the client a state or local government entity?
     */
    @Property
    private boolean stateLocalGovtInd;

    /**
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * government-issued unique identifier
     */
    @Property
    private long clientId;

    /**
     * Who's the contact for this client
     */
    @Property
    private String contactName;

    /**
     * client's country
     */
    @Property
    private String country;

    /**
     * public-or-private body country
     */
    @Property
    private String countryPPB;

    /**
     * General description of the client, e.g., type of business they're in
     */
    @Property
    private String desc;

    /**
     * Client name
     */
    @Property
    private String name;

    /**
     * Client's state, when applicable
     */
    @Property
    private String state;

    /**
     * public-or-private body state of client, when applicable.
     */
    @Property
    private String statePBB;

    /**
     * Constructor
     * @param clientId
     * @param name
     * @param desc
     * @param contactName
     * @param country
     * @param countryPBB
     * @param state
     * @param statePBB
     * @param selfFilerInd
     * @param stateLocalGovtInd
     */
    public Client(final long clientId,
                  final String name,
                  final String desc,
                  final String contactName,
                  final String country,
                  final String countryPBB,
                  final String state,
                  final String statePBB,
                  final boolean selfFilerInd,
                  final boolean stateLocalGovtInd) {

        this.clientId = clientId;
        this.name = normalizeString (name);
        this.desc = normalizeString (desc);
        this.contactName = normalizeString (contactName);
        this.country = normalizeString (country);
        this.countryPPB = normalizeString (countryPBB);
        this.state = normalizeString (state);
        this.statePBB = normalizeString (statePBB);
        this.selfFilerInd = selfFilerInd;
        this.stateLocalGovtInd = stateLocalGovtInd;
    }

    /**
     * getter
     * @return flag indicating if client is making filings themselves
     */
    public boolean isSelfFilerInd() {
        return selfFilerInd;
    }

    /**
     * setter
     * @param selfFilerInd flag indicating if client is making filings themselves
     */
    public void setSelfFilerInd(boolean selfFilerInd) {
        this.selfFilerInd = selfFilerInd;
    }

    /**
     * gettter
     * @return flag indicating client is state or local government entity
     */
    public boolean isStateLocalGovtInd() {
        return stateLocalGovtInd;
    }

    /**
     * setter
     * @param stateLocalGovtInd flag indicating client is state or local government entity
     */
    public void setStateLocalGovtInd(boolean stateLocalGovtInd) {
        this.stateLocalGovtInd = stateLocalGovtInd;
    }

    /**
     * getter
     * @return internal Neo4J id of the node
     */
    public Long getId() {
        return id;
    }

    /**
     * setter
     * @param id Neo4J-assigned node id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getter
     * @return government-issued unique identifier
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * setter
     * @param clientId government-issued unique identifier
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * getter
     * @return who's the contact for this client
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * setter
     * @param contactName who's the contact for this client
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * getter
     * @return client's country
     */
    public String getCountry() {
        return country;
    }

    /**
     * setter
     * @param country client's country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * getter
     * @return public-or-private body country
     */
    public String getCountryPPB() {
        return countryPPB;
    }

    /**
     * setter
     * @param countryPPB public-or-private body country
     */
    public void setCountryPPB(String countryPPB) {
        this.countryPPB = countryPPB;
    }

    /**
     * getter
     * @return general description of the client
     */
    public String getDesc() {
        return desc;
    }

    /**
     * setter
     * @param desc general description of the client
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * getter
     * @return client's name
     */
    public String getName() {
        return name;
    }

    /**
     * setter
     * @param name client's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     * @return client's state, when applicable
     */
    public String getState() {
        return state;
    }

    /**
     * setter
     * @param state client's state, when applicable
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * getter
     * @return public-or-private state of client, when applicable
     */
    public String getStatePBB() {
        return statePBB;
    }

    /**
     * setter
     * @param statePBB public-or-private state of client, when applicable
     */
    public void setStatePBB(String statePBB) {
        this.statePBB = statePBB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (clientId != client.clientId) return false;
        return name != null ? name.equals(client.name) : client.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (clientId ^ (clientId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


    /**
     * Normalize the string data provided in the source data file
     * @param original original string to normalize
     * @return normalized string
     */
    private String normalizeString (String original) {
        return (original != null && !original.isEmpty()) ? original.trim().replace("\r\n", ", ").replace("\n", "  ") : null;
    }
}
