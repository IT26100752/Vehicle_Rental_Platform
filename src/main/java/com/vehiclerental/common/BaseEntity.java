package com.vehiclerental.common;

import java.time.LocalDateTime;

/**
 * Root of every persisted domain object.
 * Demonstrates ABSTRACTION + INHERITANCE: all entities share an id and a creation
 * timestamp, and the storage layer only ever needs to know about this type.
 */
public abstract class BaseEntity {

    private Long id;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isNew() {
        return id == null;
    }
}
