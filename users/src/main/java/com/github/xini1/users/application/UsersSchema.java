package com.github.xini1.users.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.github.xini1.common.UserType;
import com.github.xini1.users.port.HashingAlgorithm;
import com.github.xini1.users.port.UserStore;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

final class UsersSchema {

    private static final String TABLE_NAME = "users";
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String SALT = "salt";
    private static final String PASSWORD_HASH = "passwordHash";
    private static final String USER_TYPE = "userType";

    UsersSchema.Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes);
    }

    ScanRequest findByUsernameRequest(String username) {
        return new ScanRequest()
                .withTableName(TABLE_NAME)
                .withFilterExpression(USERNAME + " = :username")
                .withExpressionAttributeValues(Map.of(":username", new AttributeValue().withS(username)));
    }

    QueryRequest findByIdRequest(UUID id) {
        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(ID + " = :id")
                .withExpressionAttributeValues(Map.of(":id", new AttributeValue().withS(id.toString())));
    }

    Put put(UserStore.Dto dto, HashingAlgorithm hashingAlgorithm) {
        return new Put()
                .withTableName(TABLE_NAME)
                .withItem(attributes(dto, hashingAlgorithm));
    }

    private Map<String, AttributeValue> attributes(UserStore.Dto dto, HashingAlgorithm hashingAlgorithm) {
        var salt = hashingAlgorithm.salt();
        return Map.of(
                ID, new AttributeValue().withS(dto.getId().toString()),
                USERNAME, new AttributeValue().withS(dto.getUsername()),
                SALT, new AttributeValue().withB(ByteBuffer.wrap(salt)),
                PASSWORD_HASH, new AttributeValue().withS(hashingAlgorithm.hash(dto.getPassword(), salt)),
                USER_TYPE, new AttributeValue().withS(dto.getUserType().toString())
        );
    }

    static final class Binding {

        private final Map<String, AttributeValue> attributes;

        private Binding(Map<String, AttributeValue> attributes) {
            this.attributes = attributes;
        }

        boolean hasNotPassword(String password, HashingAlgorithm hashingAlgorithm) {
            return !hashingAlgorithm.hash(password, attributes.get(SALT).getB().array())
                    .equals(attributes.get(PASSWORD_HASH).getS());
        }

        UserStore.Dto toDto() {
            return new UserStore.Dto(
                    UUID.fromString(attributes.get(ID).getS()),
                    attributes.get(USERNAME).getS(),
                    attributes.get(PASSWORD_HASH).getS(),
                    UserType.valueOf(attributes.get(USER_TYPE).getS())
            );
        }
    }
}
