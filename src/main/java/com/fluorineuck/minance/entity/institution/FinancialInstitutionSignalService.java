package com.fluorineuck.minance.entity.institution;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.market.financial.FundamentalAnchor;
import com.fluorineuck.minance.market.financial.FundamentalAnchorType;
import com.fluorineuck.minance.market.financial.PriceSignal;
import com.fluorineuck.minance.market.financial.PriceSignalBundle;
import com.fluorineuck.minance.market.financial.PriceSignalDirection;
import com.fluorineuck.minance.market.financial.PriceSignalSource;

import java.util.ArrayList;
import java.util.List;

public final class FinancialInstitutionSignalService {
    public static final FinancialInstitutionSignalService INSTANCE = new FinancialInstitutionSignalService();

    private FinancialInstitutionSignalService() {
    }

    public PriceSignalBundle priceSignals(FinancialInstitutionProfile institution, InstitutionSignalRequest request) {
        if (institution == null) {
            throw new IllegalArgumentException("institution must not be null");
        }
        FinanceConfig.InstitutionSignalRules config = ConfigRegistry.INSTANCE.finance().institutionSignal();
        List<PriceSignal> signals = new ArrayList<>();
        if (institution.hasRole(FinancialInstitutionRole.LIQUIDITY_PROVIDER) && (request.liquidityBid() > 0.0D || request.liquidityAsk() > 0.0D)) {
            signals.add(new PriceSignal(
                    request.productId(),
                    request.productType(),
                    PriceSignalSource.LIQUIDITY,
                    PriceSignalDirection.NEUTRAL,
                    0.0D,
                    config.liquiditySignalConfidence(),
                    request.horizonTicks(),
                    request.anchorPrice(),
                    request.liquidityBid(),
                    request.liquidityAsk()
            ));
        }
        if (institution.hasRole(FinancialInstitutionRole.ASSET_MANAGER) && request.clientFlowStrength() != 0.0D) {
            signals.add(new PriceSignal(
                    request.productId(),
                    request.productType(),
                    PriceSignalSource.INSTITUTION,
                    request.clientFlowStrength() > 0.0D ? PriceSignalDirection.POSITIVE : PriceSignalDirection.NEGATIVE,
                    clamp(Math.abs(request.clientFlowStrength()), config.maxClientFlowStrength()),
                    config.clientFlowSignalConfidence(),
                    request.horizonTicks(),
                    request.anchorPrice(),
                    0.0D,
                    0.0D
            ));
        }
        if (institution.hasRole(FinancialInstitutionRole.CREDIT_UNDERWRITER) && request.riskDiscountStrength() > 0.0D) {
            signals.add(new PriceSignal(
                    request.productId(),
                    request.productType(),
                    PriceSignalSource.RISK,
                    PriceSignalDirection.NEGATIVE,
                    clamp(request.riskDiscountStrength(), config.maxRiskDiscountStrength()),
                    config.riskSignalConfidence(),
                    request.horizonTicks(),
                    request.anchorPrice(),
                    0.0D,
                    0.0D
            ));
        }
        return new PriceSignalBundle(request.productId(), request.productType(), signals, anchors(request, config));
    }

    private static List<FundamentalAnchor> anchors(InstitutionSignalRequest request, FinanceConfig.InstitutionSignalRules config) {
        if (request.anchorPrice() <= 0L) {
            return List.of();
        }
        return List.of(new FundamentalAnchor(
                request.productId(),
                request.productType(),
                FundamentalAnchorType.UNKNOWN,
                PriceSignalSource.INSTITUTION,
                request.anchorPrice(),
                config.anchorConfidence(),
                request.horizonTicks()
        ));
    }

    private static double clamp(double value, double max) {
        return Math.max(0.0D, Math.min(value, Math.max(0.0D, max)));
    }
}
