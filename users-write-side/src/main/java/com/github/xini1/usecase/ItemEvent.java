package com.github.xini1.usecase;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ItemEvent extends Event {

    UUID itemId();

    void accept(EventVisitor eventVisitor);
}
