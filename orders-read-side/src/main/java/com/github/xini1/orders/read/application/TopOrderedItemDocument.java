package com.github.xini1.orders.read.application;

import com.github.xini1.orders.read.view.TopOrderedItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "topOrderedItems")
final class TopOrderedItemDocument {

    @Id
    private UUID id;
    private String name;
    private long timesOrdered;

    TopOrderedItemDocument() {
    }

    public TopOrderedItemDocument(TopOrderedItem topOrderedItem) {
        this.id = topOrderedItem.getId();
        this.name = topOrderedItem.getName();
        this.timesOrdered = topOrderedItem.getTimesOrdered();
    }

    TopOrderedItem toTopOrderedItem() {
        return new TopOrderedItem(id, name, timesOrdered);
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

    long getTimesOrdered() {
        return timesOrdered;
    }

    void setTimesOrdered(long timesOrdered) {
        this.timesOrdered = timesOrdered;
    }
}
