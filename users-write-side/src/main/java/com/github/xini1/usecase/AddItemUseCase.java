package com.github.xini1.usecase;

/**
 * @author Maxim Tereshchenko
 */
public interface AddItemUseCase {

    void addItem(long userId, User user, String name);
}
