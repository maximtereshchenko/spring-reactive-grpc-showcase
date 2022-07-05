package com.github.xini1.usecase;

import com.github.xini1.User;
import com.github.xini1.view.Cart;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewCartUseCase {

    Cart view(UUID userId, User user);
}
