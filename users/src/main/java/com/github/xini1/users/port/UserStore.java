package com.github.xini1.users.port;

import com.github.xini1.common.*;
import com.github.xini1.users.exception.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface UserStore {

    void save(Dto dto) throws UsernameIsTaken;

    Optional<Dto> findByUsernameAndPasswordHash(String username, String passwordHash);

    Dto find(UUID userId);

    final class Dto {
        private final UUID id;
        private final String username;
        private final String passwordHash;
        private final UserType userType;

        public Dto(UUID id, String username, String passwordHash, UserType userType) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.userType = userType;
        }

        public UUID getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public UserType getUserType() {
            return userType;
        }
    }
}
