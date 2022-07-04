package com.github.xini1;

import com.github.xini1.port.Identifiers;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class IncrementedIdentifiers implements Identifiers {

    private int nextId = 1;

    @Override
    public UUID newIdentifier() {
        return uuid(nextId++);
    }

    UUID uuid(int id) {
        return UUID.fromString("00000000-000-0000-0000-" + padWithZeros(id));
    }

    private String padWithZeros(int id) {
        var idString = String.valueOf(id);
        return "0".repeat(12 - idString.length()) + idString;
    }
}
