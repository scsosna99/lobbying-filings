/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import generated.CoveredEnum;
import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Node representing a lobbyist
 *
 * @author Scott C Sosna
 */
@NodeEntity("Lobbyist")
public class Lobbyist {

    /**
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Does the lobbyist hold a government position.
     */
    @Property
    private boolean govtPositionInd;

    /**
     * Activity Information
     */
    @Property
    private String activityInfo;

    /**
     * Lobbyist first name
     */
    @Property
    private String firstName;

    /**
     * The lobbyist's government position, if applicable.
     */
    @Property
    private String govtPositionDesc;

    /**
     * Lobbyist surname
     */
    @Property
    private String surname;

    /**
     * The registrants who have hired this lobbyist
     */
    @Relationship(type = "EMPLOYS", direction = "INCOMING")
    private Set<Registrant> employers;

    /**
     * Constructor
     * @param firstName lobbyist first name
     * @param surname lobbyist surname
     * @param covered enum indicating whether the lobbyist holds a government position
     * @param govtPositionDesc The lobbyist's government position, if applicable
     * @param activityInfo activity info
     */
    public Lobbyist(final String firstName,
                    final String surname,
                    final CoveredEnum covered,
                    final String govtPositionDesc,
                    final String activityInfo) {

        this();
        this.firstName = normalizeString(firstName);
        this.surname = normalizeString(surname);
        this.govtPositionInd = (covered == CoveredEnum.COVERED);
        this.govtPositionDesc = normalizeString(govtPositionDesc);
        this.activityInfo = normalizeString(activityInfo);
    }

    /**
     * Default constructor
     */
    public Lobbyist() {
        this.employers = new HashSet<>();
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
     * @param id internal Neo4J id of the node
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getter
     * @return does the lobbyist hold a government position
     */
    public boolean isGovtPositionInd() {
        return govtPositionInd;
    }

    /**
     * setter
     * @param govtPositionInd does the lobbyist hold a goverment position
     */
    public void setGovtPositionInd(boolean govtPositionInd) {
        this.govtPositionInd = govtPositionInd;
    }

    /**
     * getter
     * @return activity information
     */
    public String getActivityInfo() {
        return activityInfo;
    }

    /**
     * setter
     * @param activityInfo activity information
     */
    public void setActivityInfo(String activityInfo) {
        this.activityInfo = activityInfo;
    }

    /**
     * getter
     * @return Lobbyist first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * setter
     * @param firstName lobbyist first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * getter
     * @return government position of lobbyist, when applicable
     */
    public String getGovtPositionDesc() {
        return govtPositionDesc;
    }

    /**
     * Setter
     * @param govtPositionDesc government position of lobbyist, when applicable
     */
    public void setGovtPositionDesc(String govtPositionDesc) {
        this.govtPositionDesc = govtPositionDesc;
    }

    /**
     * getter
     * @return lobbyist surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * setter
     * @param surname lobbyist surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * getter
     * @return registrants who employee the lobbyist
     */
    public Set<Registrant> getEmployers() {
        return employers;
    }

    /**
     * setter
     * @param employers collection of employers (registrants)
     */
    public void setEmployers(Set<Registrant> employers) {
        this.employers = employers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lobbyist lobbyist = (Lobbyist) o;

        if (firstName != null ? !firstName.equals(lobbyist.firstName) : lobbyist.firstName != null) return false;
        return surname != null ? surname.equals(lobbyist.surname) : lobbyist.surname == null;

    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
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
