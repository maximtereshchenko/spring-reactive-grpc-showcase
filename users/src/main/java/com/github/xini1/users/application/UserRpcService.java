package com.github.xini1.users.application;

import com.github.xini1.common.UserType;
import com.github.xini1.users.exception.IncorrectUsernameOrPassword;
import com.github.xini1.users.exception.UsernameIsTaken;
import com.github.xini1.users.rpc.*;
import com.github.xini1.users.usecase.DecodeJwtUseCase;
import com.github.xini1.users.usecase.LoginUseCase;
import com.github.xini1.users.usecase.RegisterUseCase;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maxim Tereshchenko
 */
final class UserRpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRpcService.class);

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final DecodeJwtUseCase decodeJwtUseCase;

    UserRpcService(RegisterUseCase registerUseCase, LoginUseCase loginUseCase, DecodeJwtUseCase decodeJwtUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.decodeJwtUseCase = decodeJwtUseCase;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<IdResponse> responseObserver) {
        try {
            responseObserver.onNext(
                    IdResponse.newBuilder()
                            .setId(
                                    registerUseCase.register(
                                                    request.getUsername(),
                                                    request.getPassword(),
                                                    UserType.valueOf(request.getUserType())
                                            )
                                            .toString()
                            )
                            .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not register user", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        } catch (UsernameIsTaken e) {
            LOGGER.warn("Username is taken", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<JwtResponse> responseObserver) {
        try {
            responseObserver.onNext(
                    JwtResponse.newBuilder()
                            .setJwt(loginUseCase.login(request.getUsername(), request.getPassword()))
                            .build()
            );
            responseObserver.onCompleted();
        } catch (IncorrectUsernameOrPassword e) {
            LOGGER.warn("Could not login user", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void decode(DecodeJwtRequest request, StreamObserver<DecodedJwtResponse> responseObserver) {
        var response = decodeJwtUseCase.decode(request.getJwt());
        responseObserver.onNext(
                DecodedJwtResponse.newBuilder()
                        .setUserId(response.getUserId().toString())
                        .setUserType(response.getUserType().toString())
                        .build()
        );
        responseObserver.onCompleted();
    }
}
