package com.github.xini1.users.domain;

import com.github.xini1.common.event.user.*;
import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class Users {

    private final UserStore userStore;
    private final EventStore eventStore;

    Users(UserStore userStore, EventStore eventStore) {
        this.userStore = userStore;
        this.eventStore = eventStore;
    }

    void save(User user) {
        userStore.save(user.dto());
        eventStore.publish(new UserRegistered(1, user.id(), user.name()));
    }

    Optional<User> findByUsernameAndPasswordHash(String username, String passwordHash) {
        return userStore.findByUsernameAndPasswordHash(username, passwordHash)
                .map(User::new);
    }
}
