package com.github.xini1.usecase;

import com.github.xini1.*;
import com.github.xini1.view.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewCartUseCase {

    Cart view(UUID userId, User user);
}
