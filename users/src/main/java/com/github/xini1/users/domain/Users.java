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
    private final HashingAlgorithm hashingAlgorithm;

    Users(UserStore userStore, BasicEventStore basicEventStore, HashingAlgorithm hashingAlgorithm) {
        this.userStore = userStore;
        this.basicEventStore = basicEventStore;
        this.hashingAlgorithm = hashingAlgorithm;
    }

    void save(User user) {
        userStore.save(user.dto(), hashingAlgorithm);
        basicEventStore.publish(new UserRegistered(1, user.id(), user.name()));
    }

    Optional<User> findByUsernameAndPasswordHash(String username, String password) {
        return userStore.findByUsernameAndPassword(username, password, hashingAlgorithm)
                .map(User::new);
    }

    User find(UUID userId) {
        return new User(userStore.find(userId));
    }
}
