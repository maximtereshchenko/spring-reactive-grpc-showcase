package com.github.xini1.domain;

import com.github.xini1.User;
import com.github.xini1.usecase.ViewCartUseCase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewService implements ViewCartUseCase {

    @Override
    public CartView view(UUID userId, User regular) {
        return new CartView();
    }
}
