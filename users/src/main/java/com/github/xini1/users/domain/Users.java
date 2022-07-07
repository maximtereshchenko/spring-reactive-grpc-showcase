package com.github.xini1.users.domain;

import com.github.xini1.common.event.*;
import com.github.xini1.common.event.user.*;
import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class Users {

    private final UserStore userStore;
    private final BasicEventStore basicEventStore;

    Users(UserStore userStore, BasicEventStore basicEventStore) {
        this.userStore = userStore;
        this.basicEventStore = basicEventStore;
    }

    void save(User user) {
        userStore.save(user.dto());
        basicEventStore.publish(new UserRegistered(1, user.id(), user.name()));
    }

    Optional<User> findByUsernameAndPasswordHash(String username, String passwordHash) {
        return userStore.findByUsernameAndPasswordHash(username, passwordHash)
                .map(User::new);
    }

    User find(UUID userId) {
        return new User(userStore.find(userId));
    }
}
