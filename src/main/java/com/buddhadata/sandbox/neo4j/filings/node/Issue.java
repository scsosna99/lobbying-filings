/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.node;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Node representing an issue being lobbied for
 *
 * @author Scott C Sosna
 */
@NodeEntity
public class Issue {

    /**
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * general area of the issue, standardized for all issues across filings
     */
    @Property
    private String code;

    /**
     * Constructor
     * @param code the general area of the issue,
     */
    public Issue(final String code) {

        this.code = normalizeString(code);
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
     * @return general area of the issue, standardized for all issues across filings
     */
    public String getCode() {
        return code;
    }

    /**
     * setter
     * @param code general area of the issue, standardized for all issues across filings
     */
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        return code != null ? code.equals(issue.code) : issue.code == null;

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
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
