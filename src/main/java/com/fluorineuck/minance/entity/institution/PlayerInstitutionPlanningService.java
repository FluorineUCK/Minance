package com.fluorineuck.minance.entity.institution;

import java.util.Set;
import java.util.UUID;

public final class PlayerInstitutionPlanningService {
    public static final PlayerInstitutionPlanningService INSTANCE = new PlayerInstitutionPlanningService();

    private PlayerInstitutionPlanningService() {
    }

    public PlayerInstitutionPlan plan(
            String institutionId,
            String displayName,
            UUID ownerPlayerId,
            Set<FinancialInstitutionRole> serviceGrants,
            Set<FinancialInstitutionLicense> licenses,
            Set<InstitutionOperatorGrant> operatorGrants
    ) {
        return new PlayerInstitutionPlan(
                institutionId,
                displayName,
                new FinancialInstitutionOwnership(ownerPlayerId, operatorGrants),
                serviceGrants,
                licenses
        );
    }

    public FinancialInstitutionProfile profile(PlayerInstitutionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("plan must not be null");
        }
        return new FinancialInstitutionProfile(plan.institutionId(), plan.displayName(), plan.serviceGrants(), true);
    }

    public FinancialServiceProviderContext providerContext(PlayerInstitutionPlan plan, FinancialServiceAccessPoint accessPoint) {
        return new FinancialServiceProviderContext(profile(plan), accessPoint);
    }

    public boolean canOpenTerminal(PlayerInstitutionPlan plan, UUID playerId) {
        if (plan == null) {
            return false;
        }
        return plan.ownership().hasPermission(playerId, InstitutionOperatorPermission.OPEN_TERMINAL);
    }
}
