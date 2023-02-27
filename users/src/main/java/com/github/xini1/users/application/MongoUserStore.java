package com.github.xini1.users.application;

import com.github.xini1.users.exception.UsernameIsTaken;
import com.github.xini1.users.port.HashingAlgorithm;
import com.github.xini1.users.port.UserStore;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class MongoUserStore implements UserStore {

    private final UserRepository userRepository;

    MongoUserStore(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void save(Dto dto, HashingAlgorithm hashingAlgorithm) throws UsernameIsTaken {
        trySave(dto, hashingAlgorithm).ifPresent(e -> throwUsernameIsTaken(dto.getUsername(), e));
    }

    @Override
    public Optional<Dto> findByUsernameAndPassword(
            String username,
            String password,
            HashingAlgorithm hashingAlgorithm
    ) {
        return userRepository.findByUsername(username)
                .filter(userDocument -> isPasswordMatches(password, userDocument, hashingAlgorithm))
                .map(UserDocument::dto)
                .blockOptional();
    }

    @Override
    public Dto find(UUID userId) {
        return userRepository.findById(userId)
                .map(UserDocument::dto)
                .block();
    }

    private boolean isPasswordMatches(String password, UserDocument userDocument, HashingAlgorithm hashingAlgorithm) {
        return hashingAlgorithm.hash(password, userDocument.getSalt()).equals(userDocument.getPasswordHash());
    }

    private void throwUsernameIsTaken(String username, DuplicateKeyException e) {
        throw new UsernameIsTaken(username, e);
    }

    private Optional<DuplicateKeyException> trySave(Dto dto, HashingAlgorithm hashingAlgorithm) {
        return userRepository.save(new UserDocument(dto, hashingAlgorithm))
                .flatMap(userDocument -> Mono.<DuplicateKeyException>empty())
                .onErrorResume(DuplicateKeyException.class, Mono::just)
                .blockOptional();
    }
}
