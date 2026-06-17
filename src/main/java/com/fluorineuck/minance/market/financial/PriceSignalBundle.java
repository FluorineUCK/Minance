package com.fluorineuck.minance.market.financial;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record PriceSignalBundle(
        String productId,
        FinancialProductType productType,
        List<PriceSignal> signals,
        List<FundamentalAnchor> anchors
) {
    public PriceSignalBundle {
        productId = requireProductId(productId);
        productType = Objects.requireNonNull(productType, "productType");
        signals = List.copyOf(signals == null ? List.of() : signals);
        anchors = List.copyOf(anchors == null ? List.of() : anchors);
        for (PriceSignal signal : signals) {
            requireSameProduct(productId, productType, signal.productId(), signal.productType());
        }
        for (FundamentalAnchor anchor : anchors) {
            requireSameProduct(productId, productType, anchor.productId(), anchor.productType());
        }
    }

    public static PriceSignalBundle empty(String productId, FinancialProductType productType) {
        return new PriceSignalBundle(productId, productType, List.of(), List.of());
    }

    public PriceSignalBundle withSignal(PriceSignal signal) {
        Objects.requireNonNull(signal, "signal");
        requireSameProduct(productId, productType, signal.productId(), signal.productType());
        List<PriceSignal> next = new java.util.ArrayList<>(signals);
        next.add(signal);
        return new PriceSignalBundle(productId, productType, next, anchors);
    }

    public PriceSignalBundle withAnchor(FundamentalAnchor anchor) {
        Objects.requireNonNull(anchor, "anchor");
        requireSameProduct(productId, productType, anchor.productId(), anchor.productType());
        List<FundamentalAnchor> next = new java.util.ArrayList<>(anchors);
        next.add(anchor);
        return new PriceSignalBundle(productId, productType, signals, next);
    }

    public double signedStrength() {
        return signals.stream().mapToDouble(PriceSignal::signedStrength).sum();
    }

    public double liquidityBid() {
        return signals.stream().mapToDouble(PriceSignal::liquidityBid).sum();
    }

    public double liquidityAsk() {
        return signals.stream().mapToDouble(PriceSignal::liquidityAsk).sum();
    }

    public Optional<FundamentalAnchor> strongestAnchor() {
        return anchors.stream()
                .filter(FundamentalAnchor::active)
                .max(Comparator.comparingDouble(FundamentalAnchor::confidence));
    }

    private static String requireProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        return productId;
    }

    private static void requireSameProduct(String expectedId, FinancialProductType expectedType, String actualId, FinancialProductType actualType) {
        if (!expectedId.equals(actualId) || expectedType != actualType) {
            throw new IllegalArgumentException("signal product does not match bundle product");
        }
    }
}
