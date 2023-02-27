package com.github.xini1.common;

/**
 * @author Maxim Tereshchenko
 */
public final class Shared {

    public static final String EVENTS_SNS_TOPIC = "events";

    public static final String USERS_SQS_QUEUE = "users";
    public static final String ORDERS_WRITE_SIDE_SQS_QUEUE = "orders-write-side";
    public static final String ORDERS_READ_SIDE_SQS_QUEUE = "orders-read-side";

    private Shared() {
    }
}
