package com.github.xini1.users.application;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CancellationReason;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import com.github.xini1.users.exception.UsernameIsTaken;
import com.github.xini1.users.port.HashingAlgorithm;
import com.github.xini1.users.port.UserStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class DynamoDbUserStore implements UserStore {

    private final AmazonDynamoDB amazonDynamoDB;
    private final UsersSchema usersSchema = new UsersSchema();
    private final UniqueUsernamesSchema uniqueUsernamesSchema = new UniqueUsernamesSchema();

    DynamoDbUserStore(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void save(Dto dto, HashingAlgorithm hashingAlgorithm) throws UsernameIsTaken {
        try {
            amazonDynamoDB.transactWriteItems(
                    new TransactWriteItemsRequest()
                            .withTransactItems(
                                    new TransactWriteItem()
                                            .withPut(usersSchema.put(dto, hashingAlgorithm)),
                                    new TransactWriteItem()
                                            .withPut(uniqueUsernamesSchema.put(dto.getUsername()))
                            )
            );
        } catch (TransactionCanceledException e) {
            if (hasConstraintViolation(e)) {
                throw new UsernameIsTaken(dto.getUsername(), e);
            }
            throw e;
        }
    }

    @Override
    public Optional<Dto> findByUsernameAndPassword(
            String username,
            String password,
            HashingAlgorithm hashingAlgorithm
    ) {
        var result = amazonDynamoDB.scan(usersSchema.findByUsernameRequest(username));
        if (result.getCount() == 0) {
            return Optional.empty();
        }
        var attributes = usersSchema.bind(result.getItems().get(0));
        if (attributes.hasNotPassword(password, hashingAlgorithm)) {
            return Optional.empty();
        }
        return Optional.of(attributes.toDto());
    }

    @Override
    public Dto find(UUID userId) {
        return usersSchema.bind(amazonDynamoDB.query(usersSchema.findByIdRequest(userId)).getItems().get(0)).toDto();
    }

    private boolean hasConstraintViolation(TransactionCanceledException e) {
        return e.getCancellationReasons()
                .equals(
                        List.of(
                                new CancellationReason().withCode("None"),
                                new CancellationReason()
                                        .withCode("ConditionalCheckFailed")
                                        .withMessage("The conditional request failed")
                        )
                );
    }
}
