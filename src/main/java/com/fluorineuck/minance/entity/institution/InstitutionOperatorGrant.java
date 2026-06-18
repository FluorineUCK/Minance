package com.fluorineuck.minance.entity.institution;

import java.util.Set;
import java.util.UUID;

public record InstitutionOperatorGrant(
        UUID playerId,
        Set<InstitutionOperatorPermission> permissions
) {
    public InstitutionOperatorGrant {
        if (playerId == null) {
            throw new IllegalArgumentException("playerId must not be null");
        }
        permissions = permissions == null || permissions.isEmpty() ? Set.of() : Set.copyOf(permissions);
    }

    public boolean hasPermission(InstitutionOperatorPermission permission) {
        return permissions.contains(permission);
    }
}
