package com.fluorineuck.minance.product.fund;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.market.financial.FinancialMarketResult;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.market.index.MarketIndexService;
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
        FundState fund = createFund(fundId, fundId, manager, "tracking:" + indexId, cash, shares);
        MarketIndexService.INSTANCE.indices().getOrDefault(indexId, null);
        int maxEquities = Math.max(0, ConfigRegistry.INSTANCE.finance().fund().indexTrackingMaxEquities());
        List<EquityAsset> equities = EquityMarketService.INSTANCE.sortedAssets().stream().filter(EquityAsset::tradable).limit(maxEquities).toList();
        double allocation = equities.isEmpty() ? 0.0D : cash / equities.size();
        for (EquityAsset equity : equities) {
            double quantity = allocation / Math.max(1L, equity.price());
            fund.buyProduct(equity.id(), FinancialProductType.EQUITY, quantity, equity.price());
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
