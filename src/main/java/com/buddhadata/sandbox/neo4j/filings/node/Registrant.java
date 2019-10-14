/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Node representing a lobbying registrant
 *
 * @author Scott C Sosna
 */
@NodeEntity
public class Registrant
    implements Comparable<Registrant> {

    /**
     * Registrant's address
     */
    @Property
    private String address;

    /**
     * Country in which registrant is located
     */
    @Property
    private String country;

    /**
     * description of registrant business
     */
    @Property
    private String description;

    /**
     * Registrant name
     */
    @Property
    private String name;

    /**
     * country of public-or-private body
     */
    @Property
    private String countryPBB;

    /**
     * government-generated ID of registrant
     */
    @Id
    @Property
    private long registrantId;

    /**
     * The clients which have engaged (hired) this registrant/lobbying firm
     */
    @Relationship(type="ENGAGES", direction="INCOMING")
    private Set<Client> clients = new HashSet<>();

    /**
     * Constructor
     * @param registrantId
     * @param name
     * @param description
     * @param address
     * @param country
     * @param countryPBB
     */
    public Registrant(final long registrantId,
                      final String name,
                      final String description,
                      final String address,
                      final String country,
                      final String countryPBB) {

        this.registrantId = registrantId;
        this.name = normalizeString (name);
        this.description = normalizeString (description);
        this.address = normalizeString (address);
        this.country = normalizeString(country);
        this.countryPBB = normalizeString (countryPBB);
    }

    /**
     * Default constructor
     */
    Registrant() {
    }

    /**
     * getter
     * @return registrant's address
     */
    public String getAddress() {
        return address;
    }

    /**
     * setter
     * @param address registrant's address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * getter
     * @return country in which registrant is located
     */
    public String getCountry() {
        return country;
    }

    /**
     * setter
     * @param country country in which registrant is located
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * getter
     * @return description of registrant business
     */
    public String getDescription() {
        return description;
    }

    /**
     * setter
     * @param description description of registrant business
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * getter
     * @return registrant name
     */
    public String getName() {
        return name;
    }

    /**
     * setter
     * @param name registrant name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     * @return public-or-private body country
     */
    public String getCountryPBB() {
        return countryPBB;
    }

    /**
     * setter
     * @param countryPBB public-or-private body country
     */
    public void setCountryPBB(String countryPBB) {
        this.countryPBB = countryPBB;
    }

    /**
     * Getter
     * @return government-generated ID of registrant
     */
    public long getRegistrantId() {
        return registrantId;
    }

    /**
     * setter
     * @param registrantId government-generated ID of registrant
     */
    public void setRegistrantId(long registrantId) {
        this.registrantId = registrantId;
    }

    /**
     * Getter
     * @return set of clients who have engaged (hired) this registrant
     */
    public Set<Client> getClients() {
        return clients;
    }

    /**
     * setter
     * @param clients collection of clients who have engaged (hired) this registrant
     */
    public void setClients(Set<Client> clients) {
        this.clients = clients;
    }

    /**
     * Implement comparable for sorting the registrants
     * @param other other node used in comparison
     * @return -1, 0, 1 as with all comparables
     */
    public int compareTo (Registrant other) {
        return getName().compareTo(other.getName());
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Registrant))
            return false;
        Registrant that = (Registrant) o;
        return registrantId == that.registrantId;
    }

    @Override public int hashCode() {
        return Objects.hash(registrantId);
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
