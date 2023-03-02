package com.github.xini1.common;

import java.io.InputStream;

/**
 * @author Maxim Tereshchenko
 */
public enum Shared {
    ;

    public static final String EVENTS_SNS_TOPIC = "events";
    public static final String ORDERS_READ_SIDE_SQS_QUEUE = "orders-read-side";

    public static InputStream rootCertificate() {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("ca-self-signed-certificate.pem");
    }

    public static InputStream serverCertificate() {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("server-certificate.pem");
    }

    public static InputStream serverPrivateKey() {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("server-private-key-pkcs8.key");
    }
}
