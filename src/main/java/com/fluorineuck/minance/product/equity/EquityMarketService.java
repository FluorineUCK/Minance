package com.fluorineuck.minance.product.equity;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.CompanyFinancialReport;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.market.financial.PriceSignalBundle;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EquityMarketService {
    public static final EquityMarketService INSTANCE = new EquityMarketService();

    private final Map<String, EquityAsset> assets = new LinkedHashMap<>();
    private final EquitySignalAdapter signalAdapter = new EquitySignalAdapter();

    private EquityMarketService() {
    }

    public Map<String, EquityAsset> assets() {
        return assets;
    }

    public void syncCompany(VillageCompany company) {
        int founderUnits = company.shareholderShares().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("villager:"))
                .mapToInt(Map.Entry::getValue)
                .sum();
        int commonUnits = Math.max(0, company.totalShares() - founderUnits);
        put(company, EquityAssetType.COMMON_STOCK, commonUnits <= 0 ? company.totalShares() : commonUnits, true);
        if (founderUnits > 0) {
            put(company, EquityAssetType.VILLAGER_FOUNDER_STOCK, founderUnits, false);
        }
    }

    public void syncCompanies(Iterable<VillageCompany> companies) {
        for (VillageCompany company : companies) {
            syncCompany(company);
        }
    }

    public List<EquityAsset> sortedAssets() {
        return assets.values().stream().sorted(Comparator.comparing(EquityAsset::companyId).thenComparing(asset -> asset.type().getSerializedName())).toList();
    }

    public PriceSignalBundle priceSignals(VillageCompany company) {
        String id = equityId(company.id(), EquityAssetType.COMMON_STOCK);
        EquityAsset asset = assets.getOrDefault(id, new EquityAsset(id, company.id(), company.name() + " " + EquityAssetType.COMMON_STOCK.getSerializedName(), EquityAssetType.COMMON_STOCK, company.totalShares(), company.sharePrice(), true));
        return priceSignals(asset, company.latestFinancialReport(), company.reportedNavPerShare());
    }

    public PriceSignalBundle priceSignals(EquityAsset asset, CompanyFinancialReport report) {
        long fallbackNav = report == null ? asset.price() : report.navPerShare();
        return priceSignals(asset, report, fallbackNav);
    }

    public PriceSignalBundle priceSignals(EquityAsset asset, CompanyFinancialReport report, long fallbackNavPerShare) {
        return signalAdapter.fromReport(asset, report, fallbackNavPerShare, ConfigRegistry.INSTANCE.finance().equitySignal());
    }

    private void put(VillageCompany company, EquityAssetType type, int units, boolean tradable) {
        String id = equityId(company.id(), type);
        assets.put(id, new EquityAsset(id, company.id(), company.name() + " " + type.getSerializedName(), type, units, company.sharePrice(), tradable));
    }

    public static String equityId(String companyId, EquityAssetType type) {
        return "EQ_" + companyId + "_" + type.getSerializedName();
    }
}
