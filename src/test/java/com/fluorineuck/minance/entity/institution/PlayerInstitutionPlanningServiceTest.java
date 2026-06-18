package com.fluorineuck.minance.entity.institution;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerInstitutionPlanningServiceTest {
    @Test
    void playerInstitutionPlanBuildsPlayerOwnedProviderContext() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
        PlayerInstitutionPlan plan = PlayerInstitutionPlanningService.INSTANCE.plan(
                "player:exchange",
                "Player Exchange",
                owner,
                Set.of(FinancialInstitutionRole.LIQUIDITY_PROVIDER, FinancialInstitutionRole.CLEARING_AND_CUSTODY),
                Set.of(FinancialInstitutionLicense.EXCHANGE_VENUE, FinancialInstitutionLicense.MARKET_MAKING),
                Set.of()
        );

        FinancialServiceProviderContext context = PlayerInstitutionPlanningService.INSTANCE.providerContext(plan, FinancialServiceAccessPoint.MENU_TERMINAL);

        assertEquals("player:exchange", context.providerId());
        assertEquals("Player Exchange", context.displayName());
        assertEquals(FinancialServiceAccessPoint.MENU_TERMINAL, context.accessPoint());
        assertTrue(context.playerOwned());
        assertTrue(context.hasRole(FinancialInstitutionRole.LIQUIDITY_PROVIDER));
        assertTrue(plan.hasLicense(FinancialInstitutionLicense.MARKET_MAKING));
    }

    @Test
    void ownerAndExplicitOperatorCanOpenTerminal() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID operator = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID stranger = UUID.fromString("00000000-0000-0000-0000-000000000012");
        PlayerInstitutionPlan plan = PlayerInstitutionPlanningService.INSTANCE.plan(
                "player:desk",
                "Player Desk",
                owner,
                Set.of(FinancialInstitutionRole.ASSET_MANAGER),
                Set.of(FinancialInstitutionLicense.ASSET_MANAGEMENT),
                Set.of(new InstitutionOperatorGrant(operator, Set.of(InstitutionOperatorPermission.OPEN_TERMINAL)))
        );

        assertTrue(PlayerInstitutionPlanningService.INSTANCE.canOpenTerminal(plan, owner));
        assertTrue(PlayerInstitutionPlanningService.INSTANCE.canOpenTerminal(plan, operator));
        assertFalse(PlayerInstitutionPlanningService.INSTANCE.canOpenTerminal(plan, stranger));
    }
}
