package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.village.LoadedVillageScan;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.market.financial.FinancialMarketResult;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.product.component.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.equity.EquityMarketService;
import com.fluorineuck.minance.product.equity.EquityAssetType;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.fluorineuck.minance.rule.CompanyNameRule;
import com.fluorineuck.minance.rule.MinanceRuleRegistries;
import com.fluorineuck.minance.rule.ProfessionTradeCategoryRule;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CompanyService {
    public static final CompanyService INSTANCE = new CompanyService();

    private final Map<String, VillageCompany> companies = new LinkedHashMap<>();

    private CompanyService() {
    }

    public Map<String, VillageCompany> companies() {
        return companies;
    }

    public void replaceCompanies(Map<String, VillageCompany> loadedCompanies) {
        companies.clear();
        companies.putAll(loadedCompanies);
        EquityMarketService.INSTANCE.syncCompanies(companies.values());
    }

    public Set<String> registerReadyCompanies(List<VillageCandidate> readyCandidates, Map<String, LoadedVillageScan> scans, CompanyConfig config, long gameTime) {
        Set<String> registered = new LinkedHashSet<>();
        for (VillageCandidate candidate : readyCandidates) {
            LoadedVillageScan scan = scans.get(candidate.id());
            if (scan == null || companies.containsKey(candidate.id())) {
                continue;
            }
            registerCompany(candidate, scan, config, gameTime);
            registered.add(candidate.id());
        }
        return registered;
    }

    public void updateCompaniesFromScans(Map<String, LoadedVillageScan> scans, Set<String> skipCompanyIds, CompanyConfig config, long gameTime) {
        for (VillageCompany company : companies.values()) {
            if (skipCompanyIds.contains(company.id())) {
                continue;
            }
            LoadedVillageScan scan = scans.get(company.id());
            if (scan == null) {
                continue;
            }
            company.professionCounts().clear();
            company.professionCounts().putAll(scan.professionCounts());
            company.memberProfessions().clear();
            company.memberProfessions().putAll(scan.members());
            company.setProxyLiquidityDemand(scan.proxyLiquidityDemand());
            company.setDerivativeDemand(scan.derivativeDemand());
            if (company.financialReports().isEmpty()) {
                company.initializeFinancialReporting(gameTime, config.financialReportHistoryLimit());
            }
            company.recordFinancialObservation(scan.productionIncome(), scan.proxyLiquidityDemand(), scan.derivativeDemand(), gameTime);
            CompanyFinancialReport report = null;
            if (company.shouldPublishFinancialReport(gameTime, config.financialReportIntervalTicks())) {
                report = company.publishFinancialReport(gameTime, config);
                applyFinancialReportAttraction(company, report, config);
            }
            ensureCompanyDerivatives(company, gameTime);
            updateSharePrice(company, config, gameTime, report);
        }
    }

    public List<VillageCompany> sortedCompanies() {
        return companies.values().stream().sorted(Comparator.comparing(VillageCompany::name).thenComparing(VillageCompany::id)).toList();
    }

    public CompanyFinancialReport latestFinancialReport(String companyId) {
        VillageCompany company = companies.get(companyId);
        return company == null ? null : company.latestFinancialReport();
    }

    public List<CompanyFinancialReport> financialReports(String companyId) {
        VillageCompany company = companies.get(companyId);
        return company == null ? List.of() : List.copyOf(company.financialReports());
    }

    private void registerCompany(VillageCandidate candidate, LoadedVillageScan scan, CompanyConfig config, long gameTime) {
        long initialPrice = Math.max(1L, config.minimumCapitalPerShare());
        String name = generateCompanyName(candidate.id());
        VillageCompany company = new VillageCompany(candidate.id(), name, candidate.dimension(), candidate.bellPos(), candidate.funds(), candidate.expectedShares(), initialPrice);
        company.professionCounts().putAll(candidate.professionCounts());
        company.memberProfessions().putAll(candidate.members());
        company.setProxyLiquidityDemand(candidate.proxyLiquidityDemand());
        company.setDerivativeDemand(candidate.derivativeDemand());
        for (Map.Entry<UUID, ResourceLocation> member : candidate.members().entrySet()) {
            int shares = MinanceRuleRegistries.INSTANCE.profession(member.getValue()).map(ProfessionTradeCategoryRule::expectedShares).orElse(ConfigRegistry.INSTANCE.economy().fallbackExpectedShares());
            company.shareholderShares().merge("villager:" + member.getKey(), Math.max(1, shares), Integer::sum);
        }
        companies.put(company.id(), company);
        company.appendPriceBar(new MarketPriceBar(gameTime, gameTime, initialPrice, initialPrice, initialPrice, initialPrice, 0, 0), config.priceHistoryLimit());
        company.initializeFinancialReporting(gameTime, config.financialReportHistoryLimit());
        ensureCompanyDerivatives(company, gameTime);
        EquityMarketService.INSTANCE.syncCompany(company);
    }

    private void ensureCompanyDerivatives(VillageCompany company, long gameTime) {
        Set<ResourceLocation> products = new LinkedHashSet<>();
        for (ResourceLocation profession : company.professionCounts().keySet()) {
            MinanceRuleRegistries.INSTANCE.profession(profession).ifPresent(rule -> products.addAll(rule.productItems()));
        }
        long defaultPrice = ConfigRegistry.INSTANCE.economy().defaultItemPrice();
        for (ResourceLocation item : products) {
            long spotPrice = SpotMarketService.INSTANCE.ensureAsset(item, defaultPrice, ConfigRegistry.INSTANCE.commodity().defaultVolatility()).price();
            CommodityDerivativeService.INSTANCE.updateFromSpot(item, gameTime);
        }
    }

    private void applyFinancialReportAttraction(VillageCompany company, CompanyFinancialReport report, CompanyConfig config) {
        int volume = report.attractionVolume(config);
        if (volume <= 0) {
            return;
        }
        if (report.positiveSurprise()) {
            company.addInvestmentIntent(volume, 0);
        } else {
            company.addInvestmentIntent(0, volume);
        }
    }

    private void updateSharePrice(VillageCompany company, CompanyConfig config, long gameTime, CompanyFinancialReport publishedReport) {
        long previous = company.sharePrice();
        if (!hasSharePriceEvent(company, publishedReport)) {
            company.resetVolumes();
            EquityMarketService.INSTANCE.syncCompany(company);
            return;
        }
        long anchorPrice = publishedReport == null ? previous : publishedReport.navPerShare();
        String equityId = EquityMarketService.equityId(company.id(), EquityAssetType.COMMON_STOCK);
        FinancialMarketEngine.INSTANCE.injectLiquidity(
                equityId,
                FinancialProductType.EQUITY,
                previous,
                company.pendingBuyVolume(),
                company.pendingSellVolume(),
                Math.max(1, company.pendingBuyVolume() + company.pendingSellVolume())
        );
        FinancialMarketResult result = FinancialMarketEngine.INSTANCE.update(
                equityId,
                FinancialProductType.EQUITY,
                previous,
                ConfigRegistry.INSTANCE.finance().defaultVolatility(FinancialProductType.EQUITY),
                0,
                Math.max(1L, anchorPrice)
        );
        company.setSharePrice(result.nextPrice());
        if (result.buyVolume() + result.sellVolume() > 0 || previous != result.nextPrice()) {
            company.appendPriceBar(new MarketPriceBar(
                    gameTime - config.loadedVillageScanIntervalTicks(),
                    gameTime,
                    previous,
                    result.highPrice(),
                    result.lowPrice(),
                    result.nextPrice(),
                    result.buyVolume(),
                    result.sellVolume()
            ), config.priceHistoryLimit());
        }
        company.resetVolumes();
        EquityMarketService.INSTANCE.syncCompany(company);
    }

    static boolean hasSharePriceEvent(VillageCompany company, CompanyFinancialReport publishedReport) {
        return publishedReport != null || company.pendingBuyVolume() > 0 || company.pendingSellVolume() > 0;
    }

    private static String generateCompanyName(String seedText) {
        CompanyNameRule rule = MinanceRuleRegistries.INSTANCE.companyNames();
        int seed = Math.abs(seedText.hashCode());
        String prefix = pick(rule.prefixes(), seed);
        String middle = pick(rule.middleWords(), seed / 31 + 7);
        String suffix = pick(rule.suffixes(), seed / 131 + 13);
        return prefix + " " + middle + " " + suffix;
    }

    private static String pick(List<String> values, int seed) {
        if (values.isEmpty()) {
            return "Village";
        }
        return values.get(Math.floorMod(seed, values.size()));
    }
}



