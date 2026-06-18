package com.fluorineuck.minance.entity.institution;

import java.util.Set;

public record PlayerInstitutionPlan(
        String institutionId,
        String displayName,
        FinancialInstitutionOwnership ownership,
        Set<FinancialInstitutionRole> serviceGrants,
        Set<FinancialInstitutionLicense> licenses
) {
    public PlayerInstitutionPlan {
        if (institutionId == null || institutionId.isBlank()) {
            throw new IllegalArgumentException("institutionId must not be blank");
        }
        displayName = displayName == null || displayName.isBlank() ? institutionId : displayName;
        if (ownership == null) {
            throw new IllegalArgumentException("ownership must not be null");
        }
        serviceGrants = serviceGrants == null || serviceGrants.isEmpty() ? Set.of() : Set.copyOf(serviceGrants);
        licenses = licenses == null || licenses.isEmpty() ? Set.of() : Set.copyOf(licenses);
    }

    public boolean hasLicense(FinancialInstitutionLicense license) {
        return licenses.contains(license);
    }

    public boolean hasServiceGrant(FinancialInstitutionRole role) {
        return serviceGrants.contains(role);
    }
}
