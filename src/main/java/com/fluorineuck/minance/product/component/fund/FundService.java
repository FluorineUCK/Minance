package com.fluorineuck.minance.product.fund;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.market.financial.FinancialMarketResult;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.market.index.MarketIndexService;
import com.fluorineuck.minance.market.index.MarketIndexState;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.fluorineuck.minance.product.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.equity.EquityAsset;
import com.fluorineuck.minance.product.equity.EquityMarketService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FundService {
    public static final FundService INSTANCE = new FundService();

    private final Map<String, FundState> funds = new LinkedHashMap<>();

    private FundService() {
    }

    public Map<String, FundState> funds() {
        return funds;
    }

    public List<FundState> sortedFunds() {
        return funds.values().stream().sorted(Comparator.comparing(FundState::id)).toList();
    }

    public FundState createFund(String id, String name, String manager, String strategyTag, double cash, double shares) {
        FundState fund = new FundState(id, name, manager, strategyTag, cash, shares);
        funds.put(fund.id(), fund);
        updateFund(fund);
        return fund;
    }

    public FundState createIndexTrackingFund(String fundId, String indexId, String manager, double cash, double shares) {
        FundState fund = createFund(fundId, fundId, manager, FundState.TRACKING_STRATEGY_PREFIX + indexId, cash, shares);
        fund.setTrackingIndexId(indexId);
        MarketIndexState index = trackedIndex(fund);
        if (index == null || index.componentIds().isEmpty()) {
            MarketIndexService.INSTANCE.updateFromSpotMarket();
            index = trackedIndex(fund);
        }
        if (index != null) {
            IndexTrackingFundAdapter.INSTANCE.seedInitialBasket(fund, index, ConfigRegistry.INSTANCE.finance().fund(), this::resolvePrice);
        }
        updateFund(fund);
        return fund;
    }

    public void updateAllFunds() {
        for (FundState fund : funds.values()) {
            updateFund(fund);
        }
    }

    public void updateFund(FundState fund) {
        fund.refreshHoldingPrices(this::resolvePrice);
        long previous = fund.sharePrice();
        FinancialMarketResult result = FinancialMarketEngine.INSTANCE.update(fund.id(), FinancialProductType.FUND, previous, ConfigRegistry.INSTANCE.finance().defaultVolatility(FinancialProductType.FUND), 0, Math.max(1L, Math.round(fund.nav() / Math.max(1.0D, fund.totalFundShares()))));
        fund.setSharePrice(result.nextPrice());
        refreshTrackingMetrics(fund);
    }

    public long resolvePrice(String productId, FinancialProductType productType, long fallbackPrice) {
        if (productType == FinancialProductType.EQUITY) {
            EquityAsset asset = EquityMarketService.INSTANCE.assets().get(productId);
            if (asset != null) {
                return asset.price();
            }
            VillageCompany company = VillageCompanyService.INSTANCE.companies().get(productId);
            if (company != null) {
                return company.sharePrice();
            }
        }
        if (productType == FinancialProductType.COMMODITY_SPOT) {
            return resolveSpotPrice(productId, fallbackPrice);
        }
        if (productType == FinancialProductType.FUTURE) {
            var future = CommodityDerivativeService.INSTANCE.futuresMarkets().get(productId);
            return future == null ? fallbackPrice : future.price();
        }
        if (productType == FinancialProductType.OPTION) {
            var option = CommodityDerivativeService.INSTANCE.optionMarkets().get(productId);
            return option == null ? fallbackPrice : option.premium();
        }
        if (productType == FinancialProductType.FUND) {
            FundState fund = funds.get(productId);
            return fund == null ? fallbackPrice : fund.sharePrice();
        }
        return resolveSpotPrice(productId, fallbackPrice);
    }

    public FundTrackingMetrics trackingMetrics(FundState fund) {
        MarketIndexState index = trackedIndex(fund);
        return IndexTrackingFundAdapter.INSTANCE.metrics(fund, index, ConfigRegistry.INSTANCE.finance().fund().creationRedemptionPremiumThreshold());
    }

    private void refreshTrackingMetrics(FundState fund) {
        if (fund != null && fund.tracksIndex()) {
            MarketIndexState index = trackedIndex(fund);
            if (index != null) {
                fund.recordTrackingMetrics(IndexTrackingFundAdapter.INSTANCE.metrics(fund, index, ConfigRegistry.INSTANCE.finance().fund().creationRedemptionPremiumThreshold()));
            }
        }
    }

    private MarketIndexState trackedIndex(FundState fund) {
        if (fund == null || !fund.tracksIndex()) {
            return null;
        }
        return MarketIndexService.INSTANCE.indices().get(fund.trackingIndexId());
    }

    private long resolveSpotPrice(String productId, long fallbackPrice) {
        return SpotMarketService.INSTANCE.rows().stream()
                .filter(row -> row.item().toString().equals(productId) || row.item().getPath().equals(productId))
                .findFirst()
                .map(row -> row.price())
                .orElse(fallbackPrice);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        funds.values().forEach(fund -> list.add(fund.save()));
        tag.put("funds", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        funds.clear();
        ListTag list = tag.getList("funds", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            FundState fund = FundState.load(list.getCompound(i));
            funds.put(fund.id(), fund);
        }
    }
}
