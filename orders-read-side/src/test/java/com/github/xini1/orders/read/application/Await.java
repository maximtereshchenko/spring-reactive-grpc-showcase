package com.github.xini1.orders.read.application;

import java.time.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
final class Await {

    private final Instant start = Instant.now();

    void await(Runnable assertion) {
        Stream.generate(() -> tryAssert(assertion))
                .filter(result -> result.canStop(Instant.now()))
                .findAny()
                .ifPresent(Result::onComplete);
    }

    private Result tryAssert(Runnable assertion) {
        try {
            assertion.run();
            return new Success();
        } catch (AssertionError e) {
            return new Failure(e, start.plusSeconds(5));
        }
    }

    private interface Result {

        boolean canStop(Instant now);

        void onComplete();
    }

    private static final class Success implements Result {

        @Override
        public boolean canStop(Instant now) {
            return true;
        }

        @Override
        public void onComplete() {
            //empty
        }
    }

    private static final class Failure implements Result {

        private final AssertionError error;
        private final Instant deadline;

        private Failure(AssertionError error, Instant deadline) {
            this.error = error;
            this.deadline = deadline;
        }

        @Override
        public boolean canStop(Instant now) {
            return deadline.isBefore(now);
        }

        @Override
        public void onComplete() {
            throw error;
        }
    }
}
