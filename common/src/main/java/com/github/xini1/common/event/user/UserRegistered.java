package com.github.xini1.common.event.user;

import com.github.xini1.common.event.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class UserRegistered implements UserEvent {

    private final UUID userId;
    private final String username;
    private final long version;

    public UserRegistered(UUID userId, String username, long version) {
        this.userId = userId;
        this.username = username;
        this.version = version;
    }

    public UserRegistered(Map<String, String> properties) {
        this(
                UUID.fromString(properties.get("userId")),
                properties.get("username"),
                Long.parseLong(properties.get("version"))
        );
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public EventType type() {
        return EventType.USER_REGISTERED;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public Map<String, String> asMap() {
        return Map.of(
                "userId", userId.toString(),
                "username", username,
                "version", String.valueOf(version)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (UserRegistered) object;
        return version == that.version &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "UserRegistered{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", version=" + version +
                '}';
    }
}
