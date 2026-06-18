package com.fluorineuck.minance.entity.institution;

import java.util.Set;
import java.util.UUID;

public record FinancialInstitutionOwnership(
        UUID ownerPlayerId,
        Set<InstitutionOperatorGrant> operatorGrants
) {
    public FinancialInstitutionOwnership {
        if (ownerPlayerId == null) {
            throw new IllegalArgumentException("ownerPlayerId must not be null");
        }
        operatorGrants = operatorGrants == null || operatorGrants.isEmpty() ? Set.of() : Set.copyOf(operatorGrants);
    }

    public boolean isOwner(UUID playerId) {
        return ownerPlayerId.equals(playerId);
    }

    public boolean hasPermission(UUID playerId, InstitutionOperatorPermission permission) {
        if (playerId == null || permission == null) {
            return false;
        }
        if (isOwner(playerId)) {
            return true;
        }
        return operatorGrants.stream().anyMatch(grant -> grant.playerId().equals(playerId) && grant.hasPermission(permission));
    }
}
