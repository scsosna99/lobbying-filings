/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import org.neo4j.ogm.annotation.GeneratedValue;
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
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Registrant name
     */
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
    public GovernmentEntity() {
        return;
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
}
