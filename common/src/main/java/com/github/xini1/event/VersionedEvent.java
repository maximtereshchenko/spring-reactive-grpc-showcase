package com.github.xini1.event;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public abstract class VersionedEvent implements Event {

    private final long version;

    protected VersionedEvent(long version) {
        this.version = version;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (VersionedEvent) object;
        return version == that.version;
    }

    @Override
    public String toString() {
        return "VersionedEvent{" +
                "version=" + version +
                '}';
    }
}
