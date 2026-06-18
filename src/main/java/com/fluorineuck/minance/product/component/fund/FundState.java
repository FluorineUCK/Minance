package com.fluorineuck.minance.product.fund;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class FundState {
    public static final String TRACKING_STRATEGY_PREFIX = "tracking:";

    private final String id;
    private String name;
    private String manager;
    private String strategyTag;
    private String trackingIndexId;
    private double cash;
    private double liabilities;
    private double totalFundShares;
    private long sharePrice;
    private double nav;
    private double trackingError;
    private double premiumDiscount;
    private FundCreationRedemptionAction creationRedemptionAction = FundCreationRedemptionAction.NONE;
    private final List<FundHolding> holdings = new ArrayList<>();

    public FundState(String id, String name, String manager, String strategyTag, double cash, double totalFundShares) {
        this.id = id == null ? "" : id;
        this.name = name == null || name.isBlank() ? this.id : name;
        this.manager = manager == null ? "system" : manager;
        this.strategyTag = strategyTag == null || strategyTag.isBlank() ? "balanced" : strategyTag;
        this.trackingIndexId = trackingIndexFromStrategy(this.strategyTag);
        this.cash = Math.max(0.0D, cash);
        this.totalFundShares = Math.max(1.0D, totalFundShares);
        recalculateNav();
    }

    public String id() { return id; }
    public String name() { return name; }
    public String manager() { return manager; }
    public String strategyTag() { return strategyTag; }
    public String trackingIndexId() { return trackingIndexId; }
    public double cash() { return cash; }
    public double liabilities() { return liabilities; }
    public double totalFundShares() { return totalFundShares; }
    public long sharePrice() { return sharePrice; }
    public double nav() { return nav; }
    public double trackingError() { return trackingError; }
    public double premiumDiscount() { return premiumDiscount; }
    public FundCreationRedemptionAction creationRedemptionAction() { return creationRedemptionAction; }
    public List<FundHolding> holdings() { return holdings; }

    public boolean tracksIndex() {
        return trackingIndexId != null && !trackingIndexId.isBlank();
    }

    public void setTrackingIndexId(String trackingIndexId) {
        this.trackingIndexId = trackingIndexId == null ? "" : trackingIndexId;
        if (!this.trackingIndexId.isBlank()) {
            this.strategyTag = TRACKING_STRATEGY_PREFIX + this.trackingIndexId;
        }
    }

    public void setLiabilities(double liabilities) {
        this.liabilities = Math.max(0.0D, liabilities);
    }

    public void addCash(double amount) {
        cash = Math.max(0.0D, cash + amount);
    }

    public void setSharePrice(long sharePrice) {
        this.sharePrice = Math.max(1L, sharePrice);
    }

    public void buyProduct(String productId, FinancialProductType productType, double quantity, long price) {
        if (quantity <= 0.0D || price <= 0L || productId == null || productId.isBlank()) {
            return;
        }
        double cost = quantity * price;
        cash = Math.max(0.0D, cash - cost);
        holdings.add(new FundHolding(productId, productType, quantity, price, price));
        recalculateNav();
    }

    public void recordTrackingMetrics(FundTrackingMetrics metrics) {
        if (metrics == null) {
            return;
        }
        this.trackingIndexId = metrics.indexId();
        this.trackingError = metrics.trackingError();
        this.premiumDiscount = metrics.premiumDiscount();
        this.creationRedemptionAction = metrics.creationRedemptionAction();
    }

    public void refreshHoldingPrices(ProductPriceResolver resolver) {
        for (int i = 0; i < holdings.size(); i++) {
            FundHolding holding = holdings.get(i);
            holdings.set(i, holding.withCurrentPrice(resolver.resolvePrice(holding.productId(), holding.productType(), holding.currentPrice())));
        }
        recalculateNav();
    }

    public void recalculateNav() {
        nav = cash + holdings.stream().mapToDouble(FundHolding::marketValue).sum() - liabilities;
        sharePrice = Math.max(1L, Math.round(nav / Math.max(1.0D, totalFundShares)));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("manager", manager);
        tag.putString("strategy_tag", strategyTag);
        tag.putString("tracking_index_id", trackingIndexId);
        tag.putDouble("cash", cash);
        tag.putDouble("liabilities", liabilities);
        tag.putDouble("total_fund_shares", totalFundShares);
        tag.putLong("share_price", sharePrice);
        tag.putDouble("nav", nav);
        tag.putDouble("tracking_error", trackingError);
        tag.putDouble("premium_discount", premiumDiscount);
        tag.putString("creation_redemption_action", creationRedemptionAction.name());
        ListTag list = new ListTag();
        holdings.forEach(holding -> list.add(holding.save()));
        tag.put("holdings", list);
        return tag;
    }

    public static FundState load(CompoundTag tag) {
        FundState fund = new FundState(tag.getString("id"), tag.getString("name"), tag.getString("manager"), tag.getString("strategy_tag"), tag.getDouble("cash"), tag.getDouble("total_fund_shares"));
        fund.trackingIndexId = tag.contains("tracking_index_id", Tag.TAG_STRING) ? tag.getString("tracking_index_id") : trackingIndexFromStrategy(fund.strategyTag);
        fund.liabilities = tag.getDouble("liabilities");
        fund.sharePrice = Math.max(1L, tag.getLong("share_price"));
        fund.nav = tag.getDouble("nav");
        fund.trackingError = tag.getDouble("tracking_error");
        fund.premiumDiscount = tag.getDouble("premium_discount");
        try {
            fund.creationRedemptionAction = FundCreationRedemptionAction.valueOf(tag.getString("creation_redemption_action"));
        } catch (IllegalArgumentException exception) {
            fund.creationRedemptionAction = FundCreationRedemptionAction.NONE;
        }
        fund.holdings.clear();
        ListTag list = tag.getList("holdings", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            fund.holdings.add(FundHolding.load(list.getCompound(i)));
        }
        fund.recalculateNav();
        return fund;
    }

    private static String trackingIndexFromStrategy(String strategyTag) {
        if (strategyTag == null || !strategyTag.startsWith(TRACKING_STRATEGY_PREFIX)) {
            return "";
        }
        return strategyTag.substring(TRACKING_STRATEGY_PREFIX.length());
    }

    @FunctionalInterface
    public interface ProductPriceResolver {
        long resolvePrice(String productId, FinancialProductType productType, long fallbackPrice);
    }
}
