package com.github.xini1.orders.read.application;

import com.github.xini1.orders.read.view.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "items")
final class ItemDocument {

    @Id
    private UUID id;
    private String name;
    private boolean active;
    private long version;

    ItemDocument() {
    }

    public ItemDocument(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.active = item.isActive();
        this.version = item.getVersion();
    }

    Item toItem() {
        return new Item(id, name, active, version);
    }

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    boolean isActive() {
        return active;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }
}
