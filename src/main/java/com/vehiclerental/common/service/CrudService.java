package com.vehiclerental.common.service;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION
 * -----------
 * Controllers program against this interface, not against concrete services.
 * Every module (users, vehicles, bookings, payments, reviews) promises the
 * same four CRUD operations.
 */
public interface CrudService<T> {

    List<T> findAll();

    Optional<T> findById(int id);

    /** Create */
    T save(T entity);

    /** Update */
    boolean update(T entity);

    /** Delete */
    boolean delete(int id);
}
