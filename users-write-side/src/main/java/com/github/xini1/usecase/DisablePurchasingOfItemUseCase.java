package com.github.xini1.usecase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface DisablePurchasingOfItemUseCase {

    void disablePurchasing(UUID userId, User user, UUID itemId);
}
