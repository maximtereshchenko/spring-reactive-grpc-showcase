package com.github.xini1.users.port;

import com.github.xini1.common.UserType;
import com.github.xini1.users.exception.UsernameIsTaken;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface UserStore {

    void save(Dto dto, HashingAlgorithm hashingAlgorithm) throws UsernameIsTaken;

    Optional<Dto> findByUsernameAndPassword(String username, String password, HashingAlgorithm hashingAlgorithm);

    Dto find(UUID userId);

    final class Dto {
        private final UUID id;
        private final String username;
        private final String password;
        private final UserType userType;

        public Dto(UUID id, String username, String password, UserType userType) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        public UUID getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public UserType getUserType() {
            return userType;
        }
    }
}
