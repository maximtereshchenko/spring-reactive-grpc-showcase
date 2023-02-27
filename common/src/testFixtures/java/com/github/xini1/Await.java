package com.github.xini1;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Maxim Tereshchenko
 */
public final class Await {

    private Await() {
    }

    public static void await(Runnable assertion) {
        var start = Instant.now();
        Stream.generate(() -> tryAssert(start, assertion))
                .filter(result -> result.canStop(Instant.now()))
                .findAny()
                .ifPresent(Result::onComplete);
    }

    private static Result tryAssert(Instant start, Runnable assertion) {
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
