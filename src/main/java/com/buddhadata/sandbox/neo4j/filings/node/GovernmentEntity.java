/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import java.util.Objects;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Node representing a government entity
 *
 * @author Scott C Sosna
 */
@NodeEntity
public class GovernmentEntity {

    /**
     * Registrant name
     */
    @Id
    @Property
    private String name;

    /**
     * Constructor
     * @param name
     */
    public GovernmentEntity(final String name) {

        this.name = normalizeString (name);
    }

    /**
     * Default constructor
     */
    GovernmentEntity() {
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
     * Normalize the string data provided in the source data file
     * @param original original string to normalize
     * @return normalized string
     */
    private String normalizeString (String original) {
        return (original != null && !original.isEmpty()) ? original.trim().replace("\r\n", ", ").replace("\n", "  ") : null;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GovernmentEntity))
            return false;
        GovernmentEntity that = (GovernmentEntity) o;
        return name.equals(that.name);
    }

    @Override public int hashCode() {
        return Objects.hash(name);
    }
}
