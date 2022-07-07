package com.github.xini1.usecase;

import com.github.xini1.*;

/**
 * @author Maxim Tereshchenko
 */
public interface RegisterUseCase {

    void register(String username, String password, User user);
}
