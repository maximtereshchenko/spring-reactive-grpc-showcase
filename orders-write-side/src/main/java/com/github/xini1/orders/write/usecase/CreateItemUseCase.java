package com.github.xini1.orders.write.usecase;

import com.github.xini1.common.*;
import com.github.xini1.orders.write.exception.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface CreateItemUseCase {

    UUID create(UUID userId, UserType userType, String name) throws UserIsNotAdmin;
}
