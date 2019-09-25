package com.buddhadata.sandbox.neo4j.filings.node;

import java.util.Objects;

/**
 * Used as key object for caching the lobbyist nodes.
 */
public class LobbyistKey {

    /**
     * Lobbyist's first name
     */
    private final String firstName;

    /**
     * Lobbyist's surname
     */
    private final String surname;

    /**
     * Constructor
     * @param firstName lobbyist's first name
     * @param surname lobbyist's surname
     */
    public LobbyistKey (String firstName,
                        String surname) {
        this.firstName = firstName;
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbyistKey that = (LobbyistKey) o;
        return firstName.equals(that.firstName) &&
                surname.equals(that.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, surname);
    }
}
