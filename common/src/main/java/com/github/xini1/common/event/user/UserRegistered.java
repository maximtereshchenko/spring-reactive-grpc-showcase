package com.github.xini1.common.event.user;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class UserRegistered extends VersionedEvent {

    private final UUID userId;
    private final String username;

    public UserRegistered(long version, UUID userId, String username) {
        super(version);
        this.userId = userId;
        this.username = username;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, username);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        var that = (UserRegistered) object;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "UserRegistered{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                "} " + super.toString();
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public EventType type() {
        return EventType.USER;
    }

}
