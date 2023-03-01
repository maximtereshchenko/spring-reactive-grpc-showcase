package com.github.xini1.users.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Put;

import java.util.Map;

final class UniqueUsernamesSchema {

    private static final String TABLE_NAME = "uniqueUsernames";
    private static final String USERNAME = "username";

    Put put(String username) {
        return new Put()
                .withTableName(TABLE_NAME)
                .withItem(toMap(username))
                .withConditionExpression(USERNAME + " <> :username")
                .withExpressionAttributeValues(Map.of(":username", new AttributeValue().withS(username)));
    }

    private Map<String, AttributeValue> toMap(String username) {
        return Map.of(USERNAME, new AttributeValue().withS(username));
    }
}
