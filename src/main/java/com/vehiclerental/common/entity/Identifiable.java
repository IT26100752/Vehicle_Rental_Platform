package com.vehiclerental.common.entity;

/**
 * ABSTRACTION
 * -----------
 * Every record that is written to a data file must have an id.
 * Instead of repeating "private int id; getId(); setId();" in six classes we
 * hide that common behaviour behind one tiny abstraction.
 */
public interface Identifiable {
    int getId();
    void setId(int id);
}
