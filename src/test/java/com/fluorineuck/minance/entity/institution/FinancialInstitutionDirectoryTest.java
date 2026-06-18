package com.fluorineuck.minance.entity.institution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialInstitutionDirectoryTest {
    @Test
    void defaultProviderCarriesPublicMarketServiceRoles() {
        FinancialInstitutionProfile provider = FinancialInstitutionDirectory.INSTANCE.defaultPublicProvider();

        assertEquals(FinancialInstitutionDirectory.DEFAULT_PUBLIC_PROVIDER_ID, provider.institutionId());
        assertEquals(FinancialInstitutionDirectory.DEFAULT_PUBLIC_PROVIDER_NAME, provider.displayName());
        assertFalse(provider.playerOwned());
        assertTrue(provider.hasRole(FinancialInstitutionRole.CENTRAL_BANK_AND_SECURITIES));
        assertTrue(provider.hasRole(FinancialInstitutionRole.LIQUIDITY_PROVIDER));
        assertTrue(provider.hasRole(FinancialInstitutionRole.CREDIT_UNDERWRITER));
        assertTrue(provider.hasRole(FinancialInstitutionRole.CLEARING_AND_CUSTODY));
    }

    @Test
    void defaultContextPreservesExplicitAccessPoint() {
        FinancialServiceProviderContext context = FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(FinancialServiceAccessPoint.MARKET_DASHBOARD);

        assertEquals(FinancialServiceAccessPoint.MARKET_DASHBOARD, context.accessPoint());
        assertEquals(FinancialInstitutionDirectory.DEFAULT_PUBLIC_PROVIDER_ID, context.providerId());
        assertTrue(context.hasRole(FinancialInstitutionRole.CENTRAL_BANK_AND_SECURITIES));
    }

    @Test
    void contextUsesUnknownAccessPointWhenMissing() {
        FinancialServiceProviderContext context = new FinancialServiceProviderContext(FinancialInstitutionDirectory.INSTANCE.defaultPublicProvider(), null);

        assertEquals(FinancialServiceAccessPoint.UNKNOWN, context.accessPoint());
    }
}
