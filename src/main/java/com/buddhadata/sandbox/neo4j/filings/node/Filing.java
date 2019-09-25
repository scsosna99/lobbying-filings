/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import org.neo4j.ogm.annotation.*;

import java.util.*;

/**
 * Node representing the overall filing
 *
 * @author Scott C Sosna
 */
@NodeEntity
public class Filing {

    /**
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Date on which the filing was received.
     */
    @Property
    private Date receivedOn;

    /**
     * The dollar amount of the filing.
     */
    @Property
    private int amount;

    /**
     * The year of the filing
     */
    @Property
    private int year;

    /**
     * Government-issued identifier for the filing
     */
    @Property
    private String filingId;

    /**
     * The period represented by the filing.
     */
    @Property
    private String period;

    /**
     * Type of filing
     */
    @Property
    private String type;

    @Relationship (type="ON_BEHALF_OF")
    private Client client;

    /**
     * Government entities for whom the lobbying to directed
     */
    @Relationship(type = "TARGETED_AT")
    private Set<GovernmentEntity> entities;

    @Relationship("ABOUT")
    private Set<Issue> issues;

    /**
     * The specific lobbyists identified by the filing
     */
    @Relationship(type = "LOBBYING_FOR", direction="INCOMING")
    private Set<Lobbyist> lobbyists;

    /**
     * The registered entity for whom the filing was made
     */
    @Relationship(type = "FILED", direction="INCOMING")
    private Registrant registrant;

    /**
     * Constructopr
     * @param filingId government-issued identifier for the filing
     * @param year the year of the filing
     * @param receivedOn date on which the filing was received
     * @param amount the dollar amount of the filing
     * @param type type of filing
     * @param period the period represented by the filing
     * @param client for whom the file was made
     */
    public Filing (final String filingId,
                   final int year,
                   final Date receivedOn,
                   final int amount,
                   final String type,
                   final String period,
                   final Client client) {

        this.filingId = normalizeString (filingId);
        this.year = year;
        this.receivedOn = receivedOn;
        this.amount = amount;
        this.type = normalizeString (type);
        this.period = normalizeString (period);
        this.client = client;
        this.entities = new HashSet<>();
        this.issues = new HashSet<>();
        this.lobbyists = new HashSet<>();
    }

    /**
     * getter
     * @return Internal Neo4J id of the node
     */
    public Long getId() {
        return id;
    }

    /**
     * setter
     * @param id Internal Neo4J id of the node
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getter
     * @return date on which the filing was received
     */
    public Date getReceivedOn() {
        return receivedOn;
    }

    /**
     * setter
     * @param receivedOn date on which the filing was received
     */
    public void setReceivedOn(Date receivedOn) {
        this.receivedOn = receivedOn;
    }

    /**
     * getter
     * @return the dollar amount of the filing
     */
    public int getAmount() {
        return amount;
    }

    /**
     * setter
     * @param amount the dollar amount of the filing
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * getter
     * @return the year of the filing
     */
    public int getYear() {
        return year;
    }

    /**
     * setter
     * @param year the year of the filing
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * getter
     * @return government-issued identifier for the filing
     */
    public String getFilingId() {
        return filingId;
    }

    /**
     * setter
     * @param filingId government-issued identifier for the filing
     */
    public void setFilingId(String filingId) {
        this.filingId = filingId;
    }

    /**
     * getter
     * @return period represented by the filing
     */
    public String getPeriod() {
        return period;
    }

    /**
     * setter
     * @param period period represented by the filing
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * getter
     * @return type of filing
     */
    public String getType() {
        return type;
    }

    /**
     * setter
     * @param type type of filing
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * getter
     * @return the client for whom the filing was made
     */
    public Client getClient() {
        return client;
    }

    /**
     * setter
     * @param client for whom the filing was made
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * getter
     * @return The registered entity for whom the filing was made
     */
    public Registrant getRegistrant() {
        return registrant;
    }

    /**
     * setter
     * @param registrant The registered entity for whom the filing was made
     */
    public void setRegistrant(Registrant registrant) {
        this.registrant = registrant;
    }

    public Set<GovernmentEntity> getEntities() {
        return entities;
    }

    public Set<Issue> getIssues() { return issues; }

    public Set<Lobbyist> getLobbyists() {
        return lobbyists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filing filing = (Filing) o;

        return filingId != null ? filingId.equals(filing.filingId) : filing.filingId == null;

    }

    @Override
    public int hashCode() {
        return filingId != null ? filingId.hashCode() : 0;
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
