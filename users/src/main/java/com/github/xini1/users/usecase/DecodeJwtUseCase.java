package com.github.xini1.users.usecase;

import com.github.xini1.common.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface DecodeJwtUseCase {
    Response decode(String jwt);

    final class Response {

        private final UUID userId;
        private final UserType userType;

        public Response(UUID userId, UserType userType) {
            this.userId = userId;
            this.userType = userType;
        }

        public UUID getUserId() {
            return userId;
        }

        public UserType getUserType() {
            return userType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, userType);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var response = (Response) object;
            return Objects.equals(userId, response.userId) &&
                    userType == response.userType;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "userId=" + userId +
                    ", userType=" + userType +
                    '}';
        }
    }
}
