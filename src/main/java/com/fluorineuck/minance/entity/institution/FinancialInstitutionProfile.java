package com.fluorineuck.minance.entity.institution;

import java.util.EnumSet;
import java.util.Set;

public record FinancialInstitutionProfile(
        String institutionId,
        String displayName,
        Set<FinancialInstitutionRole> roles,
        boolean playerOwned
) {
    public FinancialInstitutionProfile {
        if (institutionId == null || institutionId.isBlank()) {
            throw new IllegalArgumentException("institutionId must not be blank");
        }
        displayName = displayName == null || displayName.isBlank() ? institutionId : displayName;
        roles = roles == null || roles.isEmpty() ? Set.of() : Set.copyOf(roles);
    }

    public static FinancialInstitutionProfile of(String institutionId, String displayName, boolean playerOwned, FinancialInstitutionRole firstRole, FinancialInstitutionRole... additionalRoles) {
        EnumSet<FinancialInstitutionRole> roleSet = EnumSet.noneOf(FinancialInstitutionRole.class);
        if (firstRole != null) {
            roleSet.add(firstRole);
        }
        if (additionalRoles != null) {
            for (FinancialInstitutionRole role : additionalRoles) {
                if (role != null) {
                    roleSet.add(role);
                }
            }
        }
        return new FinancialInstitutionProfile(institutionId, displayName, roleSet, playerOwned);
    }

    public boolean hasRole(FinancialInstitutionRole role) {
        return roles.contains(role);
    }
}
