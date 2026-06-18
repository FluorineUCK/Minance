package com.fluorineuck.minance.entity.institution;

public final class FinancialInstitutionDirectory {
    public static final FinancialInstitutionDirectory INSTANCE = new FinancialInstitutionDirectory();

    public static final String DEFAULT_PUBLIC_PROVIDER_ID = "minance:central_bank_and_securities";
    public static final String DEFAULT_PUBLIC_PROVIDER_NAME = "Central Bank and Securities";

    private static final FinancialInstitutionProfile DEFAULT_PUBLIC_PROVIDER = FinancialInstitutionProfile.of(
            DEFAULT_PUBLIC_PROVIDER_ID,
            DEFAULT_PUBLIC_PROVIDER_NAME,
            false,
            FinancialInstitutionRole.CENTRAL_BANK_AND_SECURITIES,
            FinancialInstitutionRole.ISSUER,
            FinancialInstitutionRole.ASSET_MANAGER,
            FinancialInstitutionRole.LIQUIDITY_PROVIDER,
            FinancialInstitutionRole.CREDIT_UNDERWRITER,
            FinancialInstitutionRole.CLEARING_AND_CUSTODY
    );

    private FinancialInstitutionDirectory() {
    }

    public FinancialInstitutionProfile defaultPublicProvider() {
        return DEFAULT_PUBLIC_PROVIDER;
    }

    public FinancialServiceProviderContext defaultProviderContext(FinancialServiceAccessPoint accessPoint) {
        return new FinancialServiceProviderContext(DEFAULT_PUBLIC_PROVIDER, accessPoint);
    }

    public boolean isDefaultPublicProvider(FinancialInstitutionProfile profile) {
        return profile != null && DEFAULT_PUBLIC_PROVIDER_ID.equals(profile.institutionId());
    }
}
