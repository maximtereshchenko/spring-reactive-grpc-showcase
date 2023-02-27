package com.github.xini1.users.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.xini1.users.port.TokenProvider;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class JwtProvider implements TokenProvider {

    private final Algorithm algorithm = Algorithm.HMAC256("secret");
    private final JWTVerifier jwtVerifier = JWT.require(algorithm).build();

    @Override
    public String sign(UUID userId) {
        return JWT.create()
                .withClaim("id", userId.toString())
                .sign(algorithm);
    }

    @Override
    public UUID decode(String jwt) {
        return UUID.fromString(
                jwtVerifier.verify(jwt)
                        .getClaim("id")
                        .asString()
        );
    }
}
