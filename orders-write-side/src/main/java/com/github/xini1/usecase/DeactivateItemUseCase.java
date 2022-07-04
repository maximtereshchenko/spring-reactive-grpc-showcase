package com.github.xini1.usecase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface DeactivateItemUseCase {

    void deactivate(UUID userId, User user, UUID itemId);
}
