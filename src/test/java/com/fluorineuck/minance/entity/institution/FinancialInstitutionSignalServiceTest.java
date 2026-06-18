package com.fluorineuck.minance.entity.institution;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.market.financial.PriceSignal;
import com.fluorineuck.minance.market.financial.PriceSignalBundle;
import com.fluorineuck.minance.market.financial.PriceSignalDirection;
import com.fluorineuck.minance.market.financial.PriceSignalSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialInstitutionSignalServiceTest {
    @Test
    void roleProfileBuildsDeterministicFlowRiskAndLiquiditySignals() {
        FinancialInstitutionProfile profile = FinancialInstitutionProfile.of(
                "institution:test",
                "Institution Test",
                false,
                FinancialInstitutionRole.LIQUIDITY_PROVIDER,
                FinancialInstitutionRole.ASSET_MANAGER,
                FinancialInstitutionRole.CREDIT_UNDERWRITER
        );
        FinanceConfig.InstitutionSignalRules config = ConfigRegistry.INSTANCE.finance().institutionSignal();
        InstitutionSignalRequest request = new InstitutionSignalRequest("equity:test", FinancialProductType.EQUITY, 2_400L, 100L, 2.0D, 2.0D, 8.0D, 3.0D);

        PriceSignalBundle bundle = FinancialInstitutionSignalService.INSTANCE.priceSignals(profile, request);

        assertEquals(3, bundle.signals().size());
        assertEquals(1, bundle.anchors().size());
        PriceSignal liquidity = bundle.signals().stream().filter(signal -> signal.source() == PriceSignalSource.LIQUIDITY).findFirst().orElseThrow();
        PriceSignal flow = bundle.signals().stream().filter(signal -> signal.source() == PriceSignalSource.INSTITUTION).findFirst().orElseThrow();
        PriceSignal risk = bundle.signals().stream().filter(signal -> signal.source() == PriceSignalSource.RISK).findFirst().orElseThrow();
        assertTrue(liquidity.liquidityBid() == 8.0D && liquidity.liquidityAsk() == 3.0D);
        assertEquals(config.liquiditySignalConfidence(), liquidity.confidence());
        assertEquals(PriceSignalDirection.POSITIVE, flow.direction());
        assertEquals(config.maxClientFlowStrength(), flow.strength());
        assertEquals(config.clientFlowSignalConfidence(), flow.confidence());
        assertEquals(PriceSignalDirection.NEGATIVE, risk.direction());
        assertEquals(config.maxRiskDiscountStrength(), risk.strength());
        assertEquals(config.riskSignalConfidence(), risk.confidence());
        assertEquals(config.anchorConfidence(), bundle.anchors().getFirst().confidence());
    }

    @Test
    void missingRolesDoNotEmitSignals() {
        FinancialInstitutionProfile profile = FinancialInstitutionProfile.of("issuer:test", "Issuer Test", false, FinancialInstitutionRole.ISSUER);
        InstitutionSignalRequest request = new InstitutionSignalRequest("equity:test", FinancialProductType.EQUITY, 2_400L, 100L, 0.25D, 0.15D, 8.0D, 3.0D);

        PriceSignalBundle bundle = FinancialInstitutionSignalService.INSTANCE.priceSignals(profile, request);

        assertEquals(0, bundle.signals().size());
        assertEquals(1, bundle.anchors().size());
    }
}
