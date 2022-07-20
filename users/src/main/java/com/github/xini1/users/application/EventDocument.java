package com.github.xini1.users.application;

import com.github.xini1.common.event.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "events")
final class EventDocument {

    @Id
    private UUID aggregateId;
    private EventType type;
    private long version;

    EventDocument() {
    }

    public EventDocument(Event event) {
        this.aggregateId = event.aggregateId();
        this.type = event.type();
        this.version = event.version();
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, type, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (EventDocument) object;
        return version == that.version &&
                Objects.equals(aggregateId, that.aggregateId) &&
                type == that.type;
    }

    @Override
    public String toString() {
        return "EventDocument{" +
                "aggregateId=" + aggregateId +
                ", type=" + type +
                ", version=" + version +
                '}';
    }

    UUID getAggregateId() {
        return aggregateId;
    }

    void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    EventType getType() {
        return type;
    }

    void setType(EventType type) {
        this.type = type;
    }

    long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }
}
