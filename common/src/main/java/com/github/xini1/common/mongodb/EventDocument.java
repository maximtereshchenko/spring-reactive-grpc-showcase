package com.github.xini1.common.mongodb;

import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.EventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "events")
public final class EventDocument {

    @Id
    private EventId id = new EventId();
    private EventType eventType;
    private String data;

    public EventDocument() {
    }

    public EventDocument(Event event, String data) {
        id.setAggregateId(event.aggregateId());
        id.setVersion(event.version());
        this.eventType = event.type();
        this.data = data;
    }

    public EventId getId() {
        return id;
    }

    public void setId(EventId id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAggregateId(UUID aggregateId) {
        id.setAggregateId(aggregateId);
    }

    public void setVersion(int version) {
        id.setVersion(version);
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

    private static final class EventId {

        private UUID aggregateId;
        private long version;

        public UUID getAggregateId() {
            return aggregateId;
        }

        public void setAggregateId(UUID aggregateId) {
            this.aggregateId = aggregateId;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

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
    }
}
