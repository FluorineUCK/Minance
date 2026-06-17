package com.fluorineuck.minance.market.financial;

public enum PriceSignalDirection {
    POSITIVE(1.0D),
    NEGATIVE(-1.0D),
    NEUTRAL(0.0D);

    private final double sign;

    PriceSignalDirection(double sign) {
        this.sign = sign;
    }

    public double signedStrength(double strength) {
        return sign * Math.max(0.0D, strength);
    }
}
