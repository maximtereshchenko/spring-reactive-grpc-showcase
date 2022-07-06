package com.github.xini1.usecase;

import com.github.xini1.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface RemoveItemFromCartUseCase {

    void remove(UUID userId, User user, UUID itemId, int quantity);
}
