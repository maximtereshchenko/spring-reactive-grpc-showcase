package com.github.xini1.usecase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface CreateItemUseCase {

    void create(UUID userId, User user, String name);
}
