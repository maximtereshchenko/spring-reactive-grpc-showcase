package com.github.xini1.usecase;

import com.github.xini1.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface CreateItemUseCase {

    UUID create(UUID userId, User user, String name);
}
