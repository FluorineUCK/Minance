package com.fluorineuck.minance.product.component;

import com.fluorineuck.minance.market.financial.FinancialProductType;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum GenericProductComponent {
    FUTURE(FinancialProductType.FUTURE, "future", "product/derivative"),
    OPTION(FinancialProductType.OPTION, "option", "product/derivative"),
    FUND(FinancialProductType.FUND, "fund", "product/fund"),
    STRUCTURED(FinancialProductType.STRUCTURED_PRODUCT, "structured", "product/structured");

    private static final Set<FinancialProductType> FINANCIAL_TYPES = Set.copyOf(EnumSet.of(
            FinancialProductType.FUTURE,
            FinancialProductType.OPTION,
            FinancialProductType.FUND,
            FinancialProductType.STRUCTURED_PRODUCT
    ));

    private final FinancialProductType financialProductType;
    private final String serializedName;
    private final String ownerPackage;

    GenericProductComponent(FinancialProductType financialProductType, String serializedName, String ownerPackage) {
        this.financialProductType = financialProductType;
        this.serializedName = serializedName;
        this.ownerPackage = ownerPackage;
    }

    public FinancialProductType financialProductType() {
        return financialProductType;
    }

    public String serializedName() {
        return serializedName;
    }

    public String ownerPackage() {
        return ownerPackage;
    }

    public static boolean contains(FinancialProductType type) {
        return type != null && FINANCIAL_TYPES.contains(type);
    }

    public static Set<FinancialProductType> financialProductTypes() {
        return FINANCIAL_TYPES;
    }

    public static Optional<GenericProductComponent> fromFinancialProductType(FinancialProductType type) {
        if (type == null) {
            return Optional.empty();
        }
        for (GenericProductComponent component : values()) {
            if (component.financialProductType == type) {
                return Optional.of(component);
            }
        }
        return Optional.empty();
    }
}
