package com.github.xini1.users.domain;

import com.github.xini1.common.event.BasicEventStore;
import com.github.xini1.common.event.user.UserRegistered;
import com.github.xini1.users.port.HashingAlgorithm;
import com.github.xini1.users.port.UserStore;

import java.util.Optional;
import java.util.UUID;

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
        basicEventStore.publish(new UserRegistered(user.id(), user.name(), 1));
    }

    Optional<User> findByUsernameAndPasswordHash(String username, String password) {
        return userStore.findByUsernameAndPassword(username, password, hashingAlgorithm)
                .map(User::new);
    }

    User find(UUID userId) {
        return new User(userStore.find(userId));
    }
}
