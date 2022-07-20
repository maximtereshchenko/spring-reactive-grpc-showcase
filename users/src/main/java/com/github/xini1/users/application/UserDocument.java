package com.github.xini1.users.application;

import com.github.xini1.common.*;
import com.github.xini1.users.port.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
@Document(collection = "users")
final class UserDocument {

    @Id
    private UUID id;
    @Indexed(unique = true)
    private String username;
    private byte[] salt;
    private String passwordHash;
    private UserType userType;

    UserDocument() {
    }

    UserDocument(UserStore.Dto dto, HashingAlgorithm hashingAlgorithm) {
        this.id = dto.getId();
        this.username = dto.getUsername();
        this.salt = hashingAlgorithm.salt();
        this.passwordHash = hashingAlgorithm.hash(dto.getPassword(), salt);
        this.userType = dto.getUserType();
    }

    byte[] getSalt() {
        return salt.clone();
    }

    void setSalt(byte[] salt) {
        this.salt = salt.clone();
    }

    UserStore.Dto dto() {
        return new UserStore.Dto(id, username, passwordHash, userType);
    }

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    UserType getUserType() {
        return userType;
    }

    void setUserType(UserType userType) {
        this.userType = userType;
    }
}
