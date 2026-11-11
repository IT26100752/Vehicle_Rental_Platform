package com.vehiclerental.common;

import java.util.List;
import java.util.Optional;

/**
 * Storage contract used by every feature package (ABSTRACTION).
 * The services depend on this behaviour, not on how data is stored, so the file
 * based implementation could be replaced by a JDBC one without touching them.
 */
public interface CrudRepository<T extends BaseEntity> {

    /** C - Create: assigns a new id and persists the entity. */
    T create(T entity);

    /** R - Read one. */
    Optional<T> findById(Long id);

    /** R - Read all. */
    List<T> findAll();

    /** U - Update an existing entity (must already have an id). */
    T update(T entity);

    /** D - Delete by id; returns true when something was removed. */
    boolean deleteById(Long id);

    long count();
}
