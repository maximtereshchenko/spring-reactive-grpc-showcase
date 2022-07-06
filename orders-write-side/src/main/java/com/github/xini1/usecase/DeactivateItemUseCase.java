package com.github.xini1.usecase;

import com.github.xini1.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface DeactivateItemUseCase {

    void deactivate(UUID userId, User user, UUID itemId);
}
