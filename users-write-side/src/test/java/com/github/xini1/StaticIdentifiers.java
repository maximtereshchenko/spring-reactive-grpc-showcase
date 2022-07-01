package com.github.xini1;

import com.github.xini1.usecase.Identifiers;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class StaticIdentifiers implements Identifiers {

    private final UUID uuid;

    StaticIdentifiers(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID newIdentifier() {
        return uuid;
    }
}
