package com.github.xini1.usecase;

import com.github.xini1.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface OrderItemsInCartUseCase {

    void order(UUID userId, User user);
}
