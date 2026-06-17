package com.fluorineuck.minance.market.financial;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.config.TradingConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class FinancialMarketEngine {
    public static final FinancialMarketEngine INSTANCE = new FinancialMarketEngine();

    private final Map<String, FinancialMarketState> markets = new LinkedHashMap<>();

    private FinancialMarketEngine() {
    }

    public Map<String, FinancialMarketState> markets() {
        return markets;
    }

    public FinancialMarketState ensureMarket(String productId, FinancialProductType type, long currentPrice, double volatility) {
        return markets.computeIfAbsent(productId, ignored -> new FinancialMarketState(productId, type, currentPrice, volatility));
    }

    public void injectLiquidity(String productId, FinancialProductType type, long referencePrice, double bidLiquidity, double askLiquidity, int orderCount) {
        MarketConfig.FinancialMicrostructure financial = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        TradingConfig.OrderFlow orderFlow = ConfigRegistry.INSTANCE.trading().orderFlow();
        FinancialMarketState state = ensureMarket(productId, type, referencePrice, ConfigRegistry.INSTANCE.finance().defaultVolatility(type));
        long tick = priceTick(referencePrice, financial);
        int split = Math.max(1, orderFlow.injectedOrderSplitDivisor());
        int minimumOrders = Math.max(0, orderFlow.minimumInjectedOrders());
        int buyOrders = bidLiquidity > 0.0D ? Math.max(minimumOrders, orderCount / split) : 0;
        int sellOrders = askLiquidity > 0.0D ? Math.max(minimumOrders, orderCount / split) : 0;
        if (bidLiquidity > 0.0D) {
            state.surface().addBid(tick, bidLiquidity, buyOrders, state.stats());
        }
        if (askLiquidity > 0.0D) {
            state.surface().addAsk(tick, askLiquidity, sellOrders, state.stats());
        }
    }

    public FinancialMarketResult update(String productId, FinancialProductType type, long currentPrice, double volatility, int maturityDays, long anchorPrice) {
        MarketConfig.FinancialMicrostructure financial = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        FinanceConfig finance = ConfigRegistry.INSTANCE.finance();
        FinanceConfig.ProductMarketParameters product = finance.product(type);
        double effectiveVolatility = volatility <= 0.0D ? product.defaultVolatility() : volatility;
        FinancialMarketState state = ensureMarket(productId, type, currentPrice, effectiveVolatility);
        state.stats().resetCycle();

        double tickSize = Math.max(financial.minimumTickSize(), financial.tickSize());
        long currentTick = priceTick(currentPrice, financial);
        state.surface().decay(
                currentTick,
                tickSize,
                state.realizedVolatility(),
                maturityDays,
                financial.ageLambda() * product.decayAgeMultiplier(),
                financial.distanceLambda() * product.decayDistanceMultiplier(),
                financial.volatilityLambda() * product.decayVolatilityMultiplier(),
                product.maturityLambda()
        );
        generateBehavioralLiquidity(state, currentTick, anchorPrice, tickSize, type, maturityDays, finance, ConfigRegistry.INSTANCE.trading(), financial);

        int configuredRadius = type == FinancialProductType.OPTION ? financial.optionNearRadius() : financial.defaultNearRadius();
        int radius = Math.max(financial.minimumNearRadius(), configuredRadius);
        double bid = state.surface().nearBidLiquidity(currentTick, radius);
        double ask = state.surface().nearAskLiquidity(currentTick, radius);
        double imbalance = (bid - ask) / Math.max(bid + ask, 1.0D);
        double liquidityImpact = financial.liquidityImpact() / Math.max(financial.minLiquidityDenominator(), Math.log10(financial.liquidityLogBase() + bid + ask));
        double change = imbalance * Math.max(financial.minVolatility(), effectiveVolatility) * liquidityImpact * Math.max(1.0D, currentPrice);
        long next = Math.max(financial.minimumPrice(), Math.round((currentPrice + change) / tickSize) * Math.round(tickSize));
        if (next == currentPrice && Math.abs(imbalance) > financial.forceMoveImbalanceThreshold()) {
            next += imbalance > 0.0D ? Math.round(tickSize) : -Math.round(tickSize);
        }
        next = Math.max(financial.minimumPrice(), next);

        long nextTick = priceTick(next, financial);
        double consumedVolume = Math.max(1.0D, Math.abs(nextTick - currentTick)) * Math.max(bid + ask, 1.0D) * Math.max(0.0D, financial.consumedVolumeFactor());
        state.surface().consumeBetween(currentTick, nextTick, consumedVolume, state.stats());
        PriceLevel support = state.surface().strongestSupport(financial.supportAgeWeight(), financial.supportTouchWeight());
        PriceLevel resistance = state.surface().strongestResistance(financial.supportAgeWeight(), financial.supportTouchWeight());
        long supportPrice = support == null ? 0L : Math.max(financial.minimumPrice(), Math.round(support.priceTick() * tickSize));
        long resistancePrice = resistance == null ? 0L : Math.max(financial.minimumPrice(), Math.round(resistance.priceTick() * tickSize));
        double realized = Math.max(
                financial.minVolatility(),
                state.realizedVolatility() * financial.realizedVolatilityPreviousWeight()
                        + Math.abs(next - currentPrice) / (double) Math.max(1L, currentPrice) * financial.realizedVolatilityChangeWeight()
        );
        state.updateDebug(next, realized, imbalance, bid, ask, supportPrice, resistancePrice);
        int buyVolume = (int) Math.round(state.stats().generatedBuyLiquidity() + state.stats().consumedSellLiquidity());
        int sellVolume = (int) Math.round(state.stats().generatedSellLiquidity() + state.stats().consumedBuyLiquidity());
        return new FinancialMarketResult(productId, type, currentPrice, next, Math.max(currentPrice, next), Math.min(currentPrice, next), buyVolume, sellVolume, imbalance, bid, ask, supportPrice, resistancePrice, state.stats());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        markets.values().forEach(market -> list.add(market.save()));
        tag.put("markets", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        markets.clear();
        ListTag list = tag.getList("markets", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            FinancialMarketState state = FinancialMarketState.load(list.getCompound(i));
            markets.put(state.productId(), state);
        }
    }

    private static void generateBehavioralLiquidity(
            FinancialMarketState state,
            long currentTick,
            long anchorPrice,
            double tickSize,
            FinancialProductType type,
            int maturityDays,
            FinanceConfig finance,
            TradingConfig trading,
            MarketConfig.FinancialMicrostructure financial
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double scale = Math.max(0.0D, finance.product(type).baseLiquidity());
        TradingConfig.OrderFlow orderFlow = trading.orderFlow();
        for (TraderArchetype archetype : TraderArchetype.values()) {
            switch (archetype) {
                case RETAIL_NOISE -> applyRetailNoise(state, currentTick, scale, random, orderFlow.retailNoise());
                case MARKET_MAKER -> applyBand(state, currentTick, scale, random, orderFlow.marketMaker());
                case ANCHORING -> {
                    long anchorTick = priceTick(anchorPrice > 0 ? anchorPrice : Math.round(currentTick * tickSize), financial);
                    applyBand(state, anchorTick, scale, random, orderFlow.anchoring());
                }
                case MOMENTUM -> applyMomentum(state, currentTick, scale, random, orderFlow.momentum());
                case MEAN_REVERSION, VALUE_INVESTOR -> {
                    long anchorTick = priceTick(anchorPrice > 0 ? anchorPrice : Math.round(currentTick * tickSize), financial);
                    applyMeanReversion(state, currentTick, anchorTick, scale, random, orderFlow.meanReversion());
                }
                case HERDING -> applyHerding(state, currentTick, scale, random, orderFlow.herding());
                case PANIC -> applyPanic(state, currentTick, scale, random, orderFlow.panic());
                case ARBITRAGEUR, FUND_MANAGER -> {
                    long anchorTick = priceTick(anchorPrice > 0 ? anchorPrice : Math.round(currentTick * tickSize), financial);
                    applyAnchorFollower(state, currentTick, anchorTick, scale, random, orderFlow.anchorFollower());
                }
            }
        }
        TradingConfig.ExpiringOptionFlow expiring = orderFlow.expiringOption();
        if (type == FinancialProductType.OPTION && maturityDays < expiring.daysThreshold() && participates(random, expiring.participationRate())) {
            state.surface().addAsk(currentTick, scale * (expiring.daysThreshold() - maturityDays) * Math.max(0.0D, expiring.liquidityMultiplier()), Math.max(0, expiring.orderCount()), state.stats());
        }
    }

    private static void applyRetailNoise(FinancialMarketState state, long currentTick, double scale, ThreadLocalRandom random, TradingConfig.RetailNoise config) {
        if (!participates(random, config.participationRate())) {
            return;
        }
        long offset = randomLong(random, config.minTickOffset(), config.maxTickOffsetExclusive());
        double liquidity = scale * randomDouble(random, config.minLiquidityMultiplier(), config.maxLiquidityMultiplier());
        int orders = randomInt(random, config.minOrders(), config.maxOrdersExclusive());
        if (random.nextBoolean()) {
            state.surface().addBid(currentTick + offset, liquidity, orders, state.stats());
        } else {
            state.surface().addAsk(currentTick + offset, liquidity, orders, state.stats());
        }
    }

    private static void applyBand(FinancialMarketState state, long centerTick, double scale, ThreadLocalRandom random, TradingConfig.LiquidityBand config) {
        if (!participates(random, config.participationRate())) {
            return;
        }
        int offset = Math.max(0, config.tickOffset());
        double liquidity = scale * Math.max(0.0D, config.liquidityMultiplier());
        int orders = Math.max(0, config.orderCount());
        state.surface().addBid(centerTick - offset, liquidity, orders, state.stats());
        state.surface().addAsk(centerTick + offset, liquidity, orders, state.stats());
    }

    private static void applyMomentum(FinancialMarketState state, long currentTick, double scale, ThreadLocalRandom random, TradingConfig.ThresholdFlow config) {
        if (!participates(random, config.participationRate())) {
            return;
        }
        int offset = Math.max(0, config.tickOffset());
        double liquidity = scale * Math.max(0.0D, config.liquidityMultiplier());
        int orders = Math.max(0, config.orderCount());
        if (state.lastImbalance() > config.threshold()) {
            state.surface().addBid(currentTick + offset, liquidity, orders, state.stats());
        } else if (state.lastImbalance() < -config.threshold()) {
            state.surface().addAsk(currentTick - offset, liquidity, orders, state.stats());
        }
    }

    private static void applyMeanReversion(FinancialMarketState state, long currentTick, long anchorTick, double scale, ThreadLocalRandom random, TradingConfig.MeanReversionFlow config) {
        if (!participates(random, config.participationRate())) {
            return;
        }
        int distance = Math.max(0, config.anchorDistance());
        int offset = Math.max(0, config.tickOffset());
        double liquidity = scale * Math.max(0.0D, config.liquidityMultiplier());
        int orders = Math.max(0, config.orderCount());
        if (currentTick > anchorTick + distance) {
            state.surface().addAsk(anchorTick + offset, liquidity, orders, state.stats());
        } else if (currentTick < anchorTick - distance) {
            state.surface().addBid(anchorTick - offset, liquidity, orders, state.stats());
        }
    }

    private static void applyHerding(FinancialMarketState state, long currentTick, double scale, ThreadLocalRandom random, TradingConfig.ThresholdFlow config) {
        if (!participates(random, config.participationRate()) || Math.abs(state.lastImbalance()) <= config.threshold()) {
            return;
        }
        int offset = Math.max(0, config.tickOffset());
        double liquidity = scale * Math.max(0.0D, config.liquidityMultiplier());
        int orders = Math.max(0, config.orderCount());
        if (state.lastImbalance() > 0.0D) {
            state.surface().addBid(currentTick + offset, liquidity, orders, state.stats());
        } else {
            state.surface().addAsk(currentTick - offset, liquidity, orders, state.stats());
        }
    }

    private static void applyPanic(FinancialMarketState state, long currentTick, double scale, ThreadLocalRandom random, TradingConfig.PanicFlow config) {
        if (!participates(random, config.participationRate()) || state.realizedVolatility() <= config.volatilityThreshold()) {
            return;
        }
        state.surface().addAsk(currentTick - Math.max(0, config.tickOffset()), scale * Math.max(0.0D, config.liquidityMultiplier()), Math.max(0, config.orderCount()), state.stats());
    }

    private static void applyAnchorFollower(FinancialMarketState state, long currentTick, long anchorTick, double scale, ThreadLocalRandom random, TradingConfig.AnchorFollowerFlow config) {
        if (!participates(random, config.participationRate())) {
            return;
        }
        double liquidity = scale * Math.max(0.0D, config.liquidityMultiplier());
        int orders = Math.max(0, config.orderCount());
        if (currentTick > anchorTick) {
            state.surface().addAsk(currentTick, liquidity, orders, state.stats());
        } else if (currentTick < anchorTick) {
            state.surface().addBid(currentTick, liquidity, orders, state.stats());
        }
    }

    private static long priceTick(long price, MarketConfig.FinancialMicrostructure financial) {
        return Math.max(financial.minimumPrice(), Math.round(price / Math.max(financial.minimumTickSize(), financial.tickSize())));
    }

    private static boolean participates(ThreadLocalRandom random, double rate) {
        return random.nextDouble() < clamp(rate, 0.0D, 1.0D);
    }

    private static long randomLong(ThreadLocalRandom random, long min, long maxExclusive) {
        return maxExclusive <= min ? min : random.nextLong(min, maxExclusive);
    }

    private static double randomDouble(ThreadLocalRandom random, double min, double maxExclusive) {
        return maxExclusive <= min ? min : random.nextDouble(min, maxExclusive);
    }

    private static int randomInt(ThreadLocalRandom random, int min, int maxExclusive) {
        return maxExclusive <= min ? min : random.nextInt(min, maxExclusive);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
