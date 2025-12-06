package com.toy.store.model;

import java.math.BigDecimal;

public enum MemberLevel {
    COMMON("平民", new BigDecimal("0")),
    BRONZE("青銅", new BigDecimal("10000")),
    BRASS("黃銅", new BigDecimal("100000")),
    GOLD("黃金", new BigDecimal("300000")),
    PLATINUM("白金", new BigDecimal("500000")),
    SUPER_PLATINUM("超白金", new BigDecimal("1000000"));

    private final String displayName;
    private final BigDecimal threshold;

    MemberLevel(String displayName, BigDecimal threshold) {
        this.displayName = displayName;
        this.threshold = threshold;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public MemberLevel next() {
        int ordinal = this.ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return this;
    }

    public MemberLevel previous() {
        int ordinal = this.ordinal();
        if (ordinal > 0) {
            return values()[ordinal - 1];
        }
        return this;
    }
}
