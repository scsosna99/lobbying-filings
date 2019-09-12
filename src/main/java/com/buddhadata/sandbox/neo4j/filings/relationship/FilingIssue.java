/*
 * Copyright (c) 2018  Scott C. Sosna  ALL RIGHTS RESERVED
 */

package com.buddhadata.sandbox.neo4j.filings.relationship;

import com.buddhadata.sandbox.neo4j.filings.node.Filing;
import com.buddhadata.sandbox.neo4j.filings.node.Issue;
import org.neo4j.ogm.annotation.*;

/**
 * Base class containing shared properties of both a route and a segment, which are Neo4J relationships.
 *
 * @author Scott C Sosna
 */
@RelationshipEntity(type = "ABOUT")
public class FilingIssue extends BaseRelationship {

    /**
     * Internal Neo4J id of the node
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Filing for which an issue is declared
     */
    @StartNode
    private Filing filing;

    /**
     * general area of issue
     */
    @EndNode
    private Issue issue;

    /**
     * Detailed description of issue for this filing
     */
    @Property
    private String desc;


    /**
     * Constructor
     */
    public FilingIssue() {
    }

    /**
     * Constructor
     * @param filing filing for which an isue
     * @param issue general area of issue
     * @param desc detailed description of issue for this filing
     */
    public FilingIssue(Filing filing,
                       Issue issue,
                       String desc) {
        this.filing = filing;
        this.issue = issue;
        this.desc = normalizeString (desc);
    }

    /**
     * setter
     * @return Neo4J-generated relationship id
     */
    public Long getId() {
        return id;
    }

    /**
     * setter
     * @param id Neo4J-generated relationship id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getter
     * @return filing for which an issue is declared
     */
    public Filing getFiling() {
        return filing;
    }

    /**
     * setter
     * @param filing filing for which an issue is declared
     */
    public void setFiling(Filing filing) {
        this.filing = filing;
    }

    /**
     * getter
     * @return general area of issue
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * setter
     * @param issue general area of issue
     */
    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    /**
     * getter
     * @return detailed description of issue for this filing
     */
    public String getDesc() {
        return desc;
    }

    /**
     * setter
     * @param desc detailed description of issue for this filing
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
