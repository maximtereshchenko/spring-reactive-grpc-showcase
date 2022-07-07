package com.github.xini1.users;

import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryUserStore implements UserStore {

    private final Map<UUID, Dto> map = new HashMap<>();

    @Override
    public void save(Dto dto) {
        map.put(dto.getId(), dto);
    }

    @Override
    public Optional<Dto> findByUsernameAndPasswordHash(String username, String passwordHash) {
        return map.values()
                .stream()
                .filter(dto -> dto.getUsername().equals(username))
                .filter(dto -> dto.getPasswordHash().equals(passwordHash))
                .findAny();
    }
}
