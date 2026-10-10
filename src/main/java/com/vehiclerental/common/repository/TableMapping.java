package com.vehiclerental.common.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Knows the TABLE layout of one entity: its columns, how to read a row into
 * an object and how to write an object into a row.
 * One implementation per entity (users, vehicles, bookings, payments, reviews).
 */
public interface TableMapping<T> {

    /** Table name, e.g. "users". */
    String table();

    /** Comma separated column list, in the same order as {@link #args(Object)}. */
    String columns();

    /** Row -> object (including the correct subclass, via the type column). */
    T map(ResultSet rs) throws SQLException;

    /** Object -> column values, in the same order as {@link #columns()}. */
    Object[] args(T entity);
}
