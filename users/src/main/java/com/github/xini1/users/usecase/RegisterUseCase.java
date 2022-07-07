package com.github.xini1.users.usecase;

import com.github.xini1.common.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface RegisterUseCase {

    UUID register(String username, String password, UserType userType);
}
