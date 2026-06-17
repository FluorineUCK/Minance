package com.fluorineuck.minance.entity.company;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyServiceTest {
    @Test
    void sharePriceOnlyUpdatesForReportsOrExplicitFlow() {
        VillageCompany company = new VillageCompany("company", ResourceLocation.withDefaultNamespace("overworld"), BlockPos.ZERO, 1_000L, 10, 100L);

        assertFalse(CompanyService.hasSharePriceEvent(company, null));

        company.addInvestmentIntent(1, 0);
        assertTrue(CompanyService.hasSharePriceEvent(company, null));

        company.resetVolumes();
        CompanyFinancialReport report = CompanyFinancialReport.initial("company", 0L, 1_000L, 10);
        assertTrue(CompanyService.hasSharePriceEvent(company, report));
    }
}
