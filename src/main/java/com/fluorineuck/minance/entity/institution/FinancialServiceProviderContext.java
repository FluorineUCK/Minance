package com.fluorineuck.minance.entity.institution;

import java.util.Objects;

public record FinancialServiceProviderContext(
        FinancialInstitutionProfile provider,
        FinancialServiceAccessPoint accessPoint
) {
    public FinancialServiceProviderContext {
        provider = Objects.requireNonNull(provider, "provider");
        accessPoint = accessPoint == null ? FinancialServiceAccessPoint.UNKNOWN : accessPoint;
    }

    public String providerId() {
        return provider.institutionId();
    }

    public String displayName() {
        return provider.displayName();
    }

    public boolean playerOwned() {
        return provider.playerOwned();
    }

    public boolean hasRole(FinancialInstitutionRole role) {
        return provider.hasRole(role);
    }
}
