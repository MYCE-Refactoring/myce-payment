package com.myce.payment.filter;

public final class InternalHeaderKey {
    public static final String INTERNAL_HEADER_PREFIX = "X-Internal-";
    public static final String INTERNAL_AUTH = INTERNAL_HEADER_PREFIX + "Auth";

    private InternalHeaderKey() {
    }
}
