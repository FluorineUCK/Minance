package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class VillageCompany {
    private final String id;
    private String name;
    private final ResourceLocation dimension;
    private final BlockPos bellPos;
    private final Map<ResourceLocation, Integer> professionCounts = new LinkedHashMap<>();
    private final Map<UUID, ResourceLocation> memberProfessions = new LinkedHashMap<>();
    private final Map<String, Integer> shareholderShares = new LinkedHashMap<>();
    private final Deque<MarketPriceBar> priceBars = new ArrayDeque<>();
    private final Deque<CompanyFinancialReport> financialReports = new ArrayDeque<>();
    private long funds;
    private int totalShares;
    private long sharePrice;
    private double proxyLiquidityDemand;
    private double derivativeDemand;
    private int pendingBuyVolume;
    private int pendingSellVolume;
    private long reportPeriodStartTick = -1L;
    private long reportOpeningFunds;
    private long pendingReportIncome;
    private double pendingReportProxyLiquidityDemand;
    private double pendingReportDerivativeDemand;
    private int pendingReportObservations;

    public VillageCompany(String id, ResourceLocation dimension, BlockPos bellPos, long funds, int totalShares, long sharePrice) {
        this(id, id, dimension, bellPos, funds, totalShares, sharePrice);
    }

    public VillageCompany(String id, String name, ResourceLocation dimension, BlockPos bellPos, long funds, int totalShares, long sharePrice) {
        this.id = id;
        this.name = name == null || name.isBlank() ? id : name;
        this.dimension = dimension;
        this.bellPos = bellPos;
        this.funds = Math.max(0L, funds);
        this.totalShares = Math.max(1, totalShares);
        this.sharePrice = Math.max(1L, sharePrice);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public ResourceLocation dimension() {
        return dimension;
    }

    public BlockPos bellPos() {
        return bellPos;
    }

    public Map<ResourceLocation, Integer> professionCounts() {
        return professionCounts;
    }

    public Map<UUID, ResourceLocation> memberProfessions() {
        return memberProfessions;
    }

    public Map<String, Integer> shareholderShares() {
        return shareholderShares;
    }

    public long funds() {
        return funds;
    }

    public void addFunds(long amount) {
        funds = Math.max(0L, funds + amount);
    }

    public int totalShares() {
        return totalShares;
    }

    public long sharePrice() {
        return sharePrice;
    }

    public void setSharePrice(long sharePrice) {
        this.sharePrice = Math.max(1L, sharePrice);
    }

    public double proxyLiquidityDemand() {
        return proxyLiquidityDemand;
    }

    public void setProxyLiquidityDemand(double proxyLiquidityDemand) {
        this.proxyLiquidityDemand = Math.max(0.0D, proxyLiquidityDemand);
    }

    public double derivativeDemand() {
        return derivativeDemand;
    }

    public void setDerivativeDemand(double derivativeDemand) {
        this.derivativeDemand = Math.max(0.0D, derivativeDemand);
    }

    public int pendingBuyVolume() {
        return pendingBuyVolume;
    }

    public int pendingSellVolume() {
        return pendingSellVolume;
    }

    public void addInvestmentIntent(int buyVolume, int sellVolume) {
        pendingBuyVolume += Math.max(0, buyVolume);
        pendingSellVolume += Math.max(0, sellVolume);
    }

    public Deque<MarketPriceBar> priceBars() {
        return priceBars;
    }

    public void appendPriceBar(MarketPriceBar bar, int limit) {
        priceBars.addLast(bar);
        while (priceBars.size() > Math.max(1, limit)) {
            priceBars.removeFirst();
        }
    }

    public Deque<CompanyFinancialReport> financialReports() {
        return financialReports;
    }

    public CompanyFinancialReport latestFinancialReport() {
        return financialReports.peekLast();
    }

    public long reportedNavPerShare() {
        CompanyFinancialReport report = latestFinancialReport();
        if (report != null) {
            return report.navPerShare();
        }
        return Math.max(1L, Math.round(funds / (double) Math.max(1, totalShares)));
    }

    public void initializeFinancialReporting(long gameTime, int limit) {
        if (financialReports.isEmpty()) {
            appendFinancialReport(CompanyFinancialReport.initial(id, gameTime, funds, totalShares), limit);
        }
        beginFinancialReportPeriod(gameTime);
    }

    public void recordFinancialObservation(long productionIncome, double proxyLiquidityDemand, double derivativeDemand, long gameTime) {
        beginFinancialReportPeriod(gameTime);
        pendingReportIncome += Math.max(0L, productionIncome);
        pendingReportProxyLiquidityDemand += Math.max(0.0D, proxyLiquidityDemand);
        pendingReportDerivativeDemand += Math.max(0.0D, derivativeDemand);
        pendingReportObservations++;
    }

    public boolean shouldPublishFinancialReport(long gameTime, int intervalTicks) {
        beginFinancialReportPeriod(gameTime);
        return gameTime - reportPeriodStartTick >= Math.max(1, intervalTicks);
    }

    public CompanyFinancialReport publishFinancialReport(long gameTime, CompanyConfig config) {
        beginFinancialReportPeriod(gameTime);
        CompanyFinancialReport previous = latestFinancialReport();
        int observations = Math.max(1, pendingReportObservations);
        long income = pendingReportIncome;
        long openingFunds = reportOpeningFunds;
        addFunds(income);
        CompanyFinancialReport report = CompanyFinancialReport.publish(
                id,
                reportPeriodStartTick,
                gameTime,
                openingFunds,
                income,
                funds,
                totalShares,
                pendingReportProxyLiquidityDemand / observations,
                pendingReportDerivativeDemand / observations,
                previous,
                config
        );
        appendFinancialReport(report, config.financialReportHistoryLimit());
        reportPeriodStartTick = gameTime;
        reportOpeningFunds = funds;
        pendingReportIncome = 0L;
        pendingReportProxyLiquidityDemand = 0.0D;
        pendingReportDerivativeDemand = 0.0D;
        pendingReportObservations = 0;
        return report;
    }

    public void appendFinancialReport(CompanyFinancialReport report, int limit) {
        financialReports.addLast(report);
        while (financialReports.size() > Math.max(1, limit)) {
            financialReports.removeFirst();
        }
    }

    public void resetVolumes() {
        pendingBuyVolume = 0;
        pendingSellVolume = 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("dimension", dimension.toString());
        tag.putInt("bell_x", bellPos.getX());
        tag.putInt("bell_y", bellPos.getY());
        tag.putInt("bell_z", bellPos.getZ());
        tag.putLong("funds", funds);
        tag.putInt("total_shares", totalShares);
        tag.putLong("share_price", sharePrice);
        tag.putDouble("proxy_liquidity_demand", proxyLiquidityDemand);
        tag.putDouble("derivative_demand", derivativeDemand);
        tag.putInt("pending_buy_volume", pendingBuyVolume);
        tag.putInt("pending_sell_volume", pendingSellVolume);
        tag.putLong("report_period_start_tick", reportPeriodStartTick);
        tag.putLong("report_opening_funds", reportOpeningFunds);
        tag.putLong("pending_report_income", pendingReportIncome);
        tag.putDouble("pending_report_proxy_liquidity_demand", pendingReportProxyLiquidityDemand);
        tag.putDouble("pending_report_derivative_demand", pendingReportDerivativeDemand);
        tag.putInt("pending_report_observations", pendingReportObservations);
        tag.put("professions", saveProfessionCounts());
        tag.put("members", saveMembers());
        tag.put("shareholders", saveShareholders());
        tag.put("price_bars", savePriceBars());
        tag.put("financial_reports", saveFinancialReports());
        return tag;
    }

    public static VillageCompany load(CompoundTag tag) {
        String id = tag.getString("id");
        ResourceLocation dimension = ResourceLocation.parse(tag.getString("dimension"));
        BlockPos bellPos = new BlockPos(tag.getInt("bell_x"), tag.getInt("bell_y"), tag.getInt("bell_z"));
        VillageCompany company = new VillageCompany(
                id,
                tag.contains("name", Tag.TAG_STRING) ? tag.getString("name") : id,
                dimension,
                bellPos,
                tag.getLong("funds"),
                Math.max(1, tag.getInt("total_shares")),
                Math.max(1L, tag.getLong("share_price"))
        );
        company.setProxyLiquidityDemand(tag.getDouble("proxy_liquidity_demand"));
        company.setDerivativeDemand(tag.getDouble("derivative_demand"));
        company.pendingBuyVolume = Math.max(0, tag.getInt("pending_buy_volume"));
        company.pendingSellVolume = Math.max(0, tag.getInt("pending_sell_volume"));
        company.reportPeriodStartTick = tag.contains("report_period_start_tick", Tag.TAG_LONG) ? tag.getLong("report_period_start_tick") : -1L;
        company.reportOpeningFunds = tag.contains("report_opening_funds", Tag.TAG_LONG) ? Math.max(0L, tag.getLong("report_opening_funds")) : company.funds;
        company.pendingReportIncome = Math.max(0L, tag.getLong("pending_report_income"));
        company.pendingReportProxyLiquidityDemand = Math.max(0.0D, tag.getDouble("pending_report_proxy_liquidity_demand"));
        company.pendingReportDerivativeDemand = Math.max(0.0D, tag.getDouble("pending_report_derivative_demand"));
        company.pendingReportObservations = Math.max(0, tag.getInt("pending_report_observations"));
        loadProfessionCounts(tag.getList("professions", Tag.TAG_COMPOUND), company.professionCounts);
        loadMembers(tag.getList("members", Tag.TAG_COMPOUND), company.memberProfessions);
        loadShareholders(tag.getList("shareholders", Tag.TAG_COMPOUND), company.shareholderShares);
        loadPriceBars(tag.getList("price_bars", Tag.TAG_COMPOUND), company.priceBars);
        loadFinancialReports(tag.getList("financial_reports", Tag.TAG_COMPOUND), company.financialReports);
        return company;
    }

    private ListTag saveProfessionCounts() {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : professionCounts.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("profession", entry.getKey().toString());
            tag.putInt("count", Math.max(0, entry.getValue()));
            list.add(tag);
        }
        return list;
    }

    private ListTag saveMembers() {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, ResourceLocation> entry : memberProfessions.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("villager", entry.getKey());
            tag.putString("profession", entry.getValue().toString());
            list.add(tag);
        }
        return list;
    }

    private ListTag saveShareholders() {
        ListTag list = new ListTag();
        for (Map.Entry<String, Integer> entry : shareholderShares.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("holder", entry.getKey());
            tag.putInt("shares", Math.max(0, entry.getValue()));
            list.add(tag);
        }
        return list;
    }

    private ListTag savePriceBars() {
        ListTag list = new ListTag();
        for (MarketPriceBar bar : priceBars) {
            list.add(bar.save());
        }
        return list;
    }

    private ListTag saveFinancialReports() {
        ListTag list = new ListTag();
        for (CompanyFinancialReport report : financialReports) {
            list.add(report.save());
        }
        return list;
    }

    private static void loadProfessionCounts(ListTag list, Map<ResourceLocation, Integer> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            target.put(ResourceLocation.parse(tag.getString("profession")), Math.max(0, tag.getInt("count")));
        }
    }

    private static void loadMembers(ListTag list, Map<UUID, ResourceLocation> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            target.put(tag.getUUID("villager"), ResourceLocation.parse(tag.getString("profession")));
        }
    }

    private static void loadShareholders(ListTag list, Map<String, Integer> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            target.put(tag.getString("holder"), Math.max(0, tag.getInt("shares")));
        }
    }

    private static void loadPriceBars(ListTag list, Deque<MarketPriceBar> target) {
        for (int i = 0; i < list.size(); i++) {
            target.addLast(MarketPriceBar.load(list.getCompound(i)));
        }
    }

    private static void loadFinancialReports(ListTag list, Deque<CompanyFinancialReport> target) {
        for (int i = 0; i < list.size(); i++) {
            target.addLast(CompanyFinancialReport.load(list.getCompound(i)));
        }
    }

    private void beginFinancialReportPeriod(long gameTime) {
        if (reportPeriodStartTick < 0L) {
            reportPeriodStartTick = gameTime;
            reportOpeningFunds = funds;
        }
    }
}
