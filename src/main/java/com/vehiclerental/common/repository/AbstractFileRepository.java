package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION + CODE REUSE
 * ------------------------
 * Every entity repository (users, vehicles, bookings, payments, reviews)
 * needs the same six operations. We write them ONCE here, on top of the
 * {@link DataStore} abstraction, and the concrete repositories only add
 * entity specific queries (e.g. findByUsername).
 *
 * The store injected by StorageConfig is either a JsonFileStore (file mode)
 * or a MySqlStore (mysql mode) - this class cannot tell the difference.
 */
public abstract class AbstractFileRepository<T extends Identifiable> {

    protected final DataStore<T> store;

    protected AbstractFileRepository(DataStore<T> store) {
        this.store = store;
    }

    public synchronized List<T> findAll() {
        return store.readAll();
    }

    public synchronized Optional<T> findById(int id) {
        return store.readAll().stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    /** CREATE - assigns the next free id and appends the record. */
    public synchronized T save(T entity) {
        List<T> all = store.readAll();
        entity.setId(nextId(all));
        all.add(entity);
        store.writeAll(all);
        return entity;
    }

    /** UPDATE - replaces the stored record that has the same id. */
    public synchronized boolean update(T entity) {
        List<T> all = store.readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == entity.getId()) {
                all.set(i, entity);
                store.writeAll(all);
                return true;
            }
        }
        return false;
    }

    /** DELETE - removes the record with the given id. */
    public synchronized boolean deleteById(int id) {
        List<T> all = store.readAll();
        boolean removed = all.removeIf(item -> item.getId() == id);
        if (removed) {
            store.writeAll(all);
        }
        return removed;
    }

    public synchronized long count() {
        return store.readAll().size();
    }

    /** True when the backing storage has no records yet. */
    public boolean isEmpty() {
        return store.isEmpty();
    }

    /** id generator: highest existing id + 1 (safe after deletions too). */
    protected int nextId(List<T> all) {
        return all.stream().mapToInt(Identifiable::getId).max().orElse(0) + 1;
    }

    protected List<T> copy(List<T> in) {
        return new ArrayList<>(in);
    }
}
