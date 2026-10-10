package com.vehiclerental.common.service;

import com.vehiclerental.common.entity.Identifiable;
import com.vehiclerental.common.repository.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION + INHERITANCE
 * -------------------------
 * Implements the four CRUD operations once by delegating to the repository.
 * Concrete services inherit this and add BUSINESS rules on top
 * (e.g. BookingService marks the vehicle RENTED when a booking is created).
 */
public abstract class AbstractCrudService<T extends Identifiable> implements CrudService<T> {

    protected final AbstractFileRepository<T> repository;

    protected AbstractCrudService(AbstractFileRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(int id) {
        return repository.findById(id);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public boolean update(T entity) {
        return repository.update(entity);
    }

    @Override
    public boolean delete(int id) {
        return repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
}
