package com.github.xini1.orders.write.application;

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
    private EventId id = new EventId();
    private EventType eventType;
    private String data;

    EventDocument() {
    }

    public EventDocument(Event event, String data) {
        id.setAggregateId(event.aggregateId());
        id.setVersion(event.version());
        this.eventType = event.type();
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventType, data);
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
        return Objects.equals(id, that.id) &&
                eventType == that.eventType &&
                Objects.equals(data, that.data);
    }

    @Override
    public String toString() {
        return "EventDocument{" +
                "id=" + id +
                ", eventType=" + eventType +
                ", data='" + data + '\'' +
                '}';
    }

    EventId getId() {
        return id;
    }

    void setId(EventId id) {
        this.id = id;
    }

    EventType getEventType() {
        return eventType;
    }

    void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    String getData() {
        return data;
    }

    void setData(String data) {
        this.data = data;
    }

    void setAggregateId(UUID aggregateId) {
        id.setAggregateId(aggregateId);
    }

    void setVersion(int version) {
        id.setVersion(version);
    }

    private static final class EventId {

        private UUID aggregateId;
        private long version;

        @Override
        public int hashCode() {
            return Objects.hash(aggregateId, version);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var id = (EventId) object;
            return version == id.version &&
                    Objects.equals(aggregateId, id.aggregateId);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "aggregateId=" + aggregateId +
                    ", version=" + version +
                    '}';
        }

        UUID getAggregateId() {
            return aggregateId;
        }

        void setAggregateId(UUID aggregateId) {
            this.aggregateId = aggregateId;
        }

        long getVersion() {
            return version;
        }

        void setVersion(long version) {
            this.version = version;
        }
    }
}
