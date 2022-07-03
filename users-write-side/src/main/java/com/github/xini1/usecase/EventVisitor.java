package com.github.xini1.usecase;

/**
 * @author Maxim Tereshchenko
 */
public interface EventVisitor {

    void visit(ItemCreated itemCreated);

    void visit(ItemDeactivated itemDeactivated);

    void visit(ItemActivated itemActivated);
}
