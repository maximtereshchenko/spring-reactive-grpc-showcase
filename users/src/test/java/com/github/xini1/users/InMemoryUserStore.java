package com.github.xini1.users;

import com.github.xini1.users.exception.*;
import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryUserStore implements UserStore {

    private final Map<UUID, Dto> map = new HashMap<>();

    @Override
    public void save(Dto dto, HashingAlgorithm hashingAlgorithm) throws UsernameIsTaken {
        if (usernameExists(dto)) {
            throw new UsernameIsTaken(dto.getUsername());
        }
        map.put(dto.getId(), dto);
    }

    @Override
    public Optional<Dto> findByUsernameAndPassword(
            String username,
            String password,
            HashingAlgorithm hashingAlgorithm
    ) {
        return map.values()
                .stream()
                .filter(dto -> dto.getUsername().equals(username))
                .filter(dto -> dto.getPassword().equals(password))
                .findAny();
    }

    @Override
    public Dto find(UUID userId) {
        return map.get(userId);
    }

    private boolean usernameExists(Dto dto) {
        return map.values()
                .stream()
                .anyMatch(present -> present.getUsername().equals(dto.getUsername()));
    }
}
