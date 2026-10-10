package com.vehiclerental.common.repository;

import java.util.List;

/**
 * ABSTRACTION of the whole storage mechanism (the viva-friendly part).
 * ---------------------------------------------------------------
 * The rest of the application (repositories, services, controllers) only
 * knows this interface. Two implementations exist:
 *
 *   {@link JsonFileStore}  - stores each entity as a JSON text file
 *   {@link com.vehiclerental.common.repository.MySqlStore} - stores each entity
 *                            as a real MySQL table
 *
 * Which one is used is decided at start-up by the property
 *     app.storage = mysql | file        (see application.properties)
 *
 * That is the Strategy pattern: same contract, two interchangeable
 * implementations, chosen at runtime.
 */
public interface DataStore<T> {

    /** READ - return every record of this entity, in id order. */
    List<T> readAll();

    /** WRITE - make the stored records exactly equal to this list. */
    void writeAll(List<T> items);

    /** True when no record exists yet. */
    boolean isEmpty();

    /** Human readable description, logged at start-up ("file:users.json" / "mysql:users"). */
    String describe();
}
