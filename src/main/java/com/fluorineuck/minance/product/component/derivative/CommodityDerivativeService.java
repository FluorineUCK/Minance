package com.fluorineuck.minance.product.derivative;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.market.financial.FinancialMarketResult;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class CommodityDerivativeService {
    public static final CommodityDerivativeService INSTANCE = new CommodityDerivativeService();

    private final Map<String, FuturesMarketState> futuresMarkets = new LinkedHashMap<>();
    private final Map<String, OptionMarketState> optionMarkets = new LinkedHashMap<>();
    private final Map<Long, FuturesContract> futuresContracts = new LinkedHashMap<>();
    private final Map<Long, OptionContract> optionContracts = new LinkedHashMap<>();
    private long nextContractId = 1L;

    private CommodityDerivativeService() {
    }

    public Map<String, FuturesMarketState> futuresMarkets() {
        return futuresMarkets;
    }

    public Map<String, OptionMarketState> optionMarkets() {
        return optionMarkets;
    }

    public Map<Long, FuturesContract> futuresContracts() {
        return futuresContracts;
    }

    public Map<Long, OptionContract> optionContracts() {
        return optionContracts;
    }

    public void ensureDerivativeSet(ResourceLocation commodity, long spotPrice) {
        if (commodity == null || commodity.equals(ResourceLocation.withDefaultNamespace("air"))) {
            return;
        }
        ensureDerivativeSetForProduct(commodity.toString(), 0, spotPrice);
    }

    public void ensureDerivativeSetForProduct(String underlyingProductId, int underlyingDepth, long underlyingPrice) {
        int derivativeDepth = Math.max(0, underlyingDepth) + 1;
        int maxDepth = Math.max(0, ConfigRegistry.INSTANCE.finance().derivative().maxDepth());
        if (derivativeDepth > maxDepth || underlyingProductId == null || underlyingProductId.isBlank()) {
            return;
        }
        long price = Math.max(1L, underlyingPrice);
        for (int day = 0; day <= maxDerivativeDays(); day++) {
            String futureId = futuresId(underlyingProductId, day);
            if (!futuresMarkets.containsKey(futureId)) {
                futuresMarkets.put(futureId, new FuturesMarketState(futureId, underlyingProductId, derivativeDepth, day, futuresInitialPrice(price, day)));
            }
            for (OptionRight right : OptionRight.values()) {
                String optionId = optionId(underlyingProductId, day, right);
                if (!optionMarkets.containsKey(optionId)) {
                    optionMarkets.put(optionId, new OptionMarketState(optionId, underlyingProductId, derivativeDepth, day, right, price, optionInitialPremium(price, day)));
                }
            }
        }
    }

    public void ensureDerivativeSetForDerivative(String derivativeProductId) {
        FuturesMarketState future = futuresMarkets.get(derivativeProductId);
        if (future != null) {
            ensureDerivativeSetForProduct(future.id(), future.depth(), future.price());
            return;
        }
        OptionMarketState option = optionMarkets.get(derivativeProductId);
        if (option != null) {
            ensureDerivativeSetForProduct(option.id(), option.depth(), option.premium());
        }
    }

    public void updateFromSpot(ResourceLocation commodity, long gameTime) {
        SpotMarketAsset asset = SpotMarketService.INSTANCE.assets().get(commodity);
        if (asset == null) {
            return;
        }
        updateFromProduct(commodity.toString(), 0, asset.price(), gameTime);
    }

    public void updateFromProduct(String underlyingProductId, int underlyingDepth, long underlyingPrice, long gameTime) {
        ensureDerivativeSetForProduct(underlyingProductId, underlyingDepth, underlyingPrice);
        int historyLimit = ConfigRegistry.INSTANCE.market().priceHistoryLimit();
        long ticksPerMarketDay = Math.max(1L, ConfigRegistry.INSTANCE.finance().derivative().ticksPerMarketDay());
        long price = Math.max(1L, underlyingPrice);
        int derivativeDepth = Math.max(0, underlyingDepth) + 1;
        for (int day = 0; day <= maxDerivativeDays(); day++) {
            FuturesMarketState future = futuresMarkets.get(futuresId(underlyingProductId, day));
            if (future != null && future.depth() == derivativeDepth) {
                long open = future.price();
                long anchor = futuresInitialPrice(price, day);
                FinancialMarketResult result = FinancialMarketEngine.INSTANCE.update(future.id(), FinancialProductType.FUTURE, open, ConfigRegistry.INSTANCE.finance().defaultVolatility(FinancialProductType.FUTURE), day, anchor);
                future.setPrice(result.nextPrice(), historyLimit);
                future.addVolume(result.buyVolume(), result.sellVolume());
                future.appendBar(gameTime - ticksPerMarketDay, gameTime, open, result.highPrice(), result.lowPrice(), result.nextPrice(), historyLimit);
                future.resetVolume();
            }
            for (OptionRight right : OptionRight.values()) {
                OptionMarketState option = optionMarkets.get(optionId(underlyingProductId, day, right));
                if (option != null && option.depth() == derivativeDepth) {
                    long open = option.premium();
                    long anchor = optionPremium(price, option.strikePrice(), day, right);
                    FinancialMarketResult result = FinancialMarketEngine.INSTANCE.update(option.id(), FinancialProductType.OPTION, open, ConfigRegistry.INSTANCE.finance().defaultVolatility(FinancialProductType.OPTION), day, anchor);
                    option.setPremium(result.nextPrice(), historyLimit);
                    option.addVolume(result.buyVolume(), result.sellVolume());
                    option.appendBar(gameTime - ticksPerMarketDay, gameTime, open, result.highPrice(), result.lowPrice(), result.nextPrice(), historyLimit);
                    option.resetVolume();
                }
            }
        }
    }

    public Optional<FuturesContract> openFuture(String marketId, int quantity, long margin, DerivativeSide side, String holder, long gameTime) {
        FuturesMarketState market = futuresMarkets.get(marketId);
        if (market == null) {
            return Optional.empty();
        }
        long id = nextContractId++;
        FuturesContract contract = new FuturesContract(
                id,
                market.id(),
                market.underlyingProductId(),
                quantity,
                market.price(),
                market.price(),
                gameTime,
                gameTime + market.durationDays() * Math.max(1L, ConfigRegistry.INSTANCE.finance().derivative().ticksPerMarketDay()),
                margin,
                side,
                holder,
                DerivativeDeliveryMethod.CASH_SETTLEMENT,
                DerivativeContractStatus.OPEN,
                0L
        );
        futuresContracts.put(id, contract);
        market.addVolume(side == DerivativeSide.BUY ? quantity : 0, side == DerivativeSide.SELL ? quantity : 0);
        return Optional.of(contract);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("next_contract_id", nextContractId);
        ListTag futures = new ListTag();
        futuresMarkets.values().forEach(market -> futures.add(market.save()));
        tag.put("futures_markets", futures);
        ListTag options = new ListTag();
        optionMarkets.values().forEach(market -> options.add(market.save()));
        tag.put("option_markets", options);
        ListTag futuresContractTags = new ListTag();
        futuresContracts.values().forEach(contract -> futuresContractTags.add(contract.save()));
        tag.put("futures_contracts", futuresContractTags);
        ListTag optionContractTags = new ListTag();
        optionContracts.values().forEach(contract -> optionContractTags.add(contract.save()));
        tag.put("option_contracts", optionContractTags);
        return tag;
    }

    public void load(CompoundTag tag) {
        futuresMarkets.clear();
        optionMarkets.clear();
        futuresContracts.clear();
        optionContracts.clear();
        nextContractId = Math.max(1L, tag.getLong("next_contract_id"));
        ListTag futures = tag.getList("futures_markets", Tag.TAG_COMPOUND);
        for (int i = 0; i < futures.size(); i++) {
            FuturesMarketState market = FuturesMarketState.load(futures.getCompound(i));
            futuresMarkets.put(market.id(), market);
        }
        ListTag options = tag.getList("option_markets", Tag.TAG_COMPOUND);
        for (int i = 0; i < options.size(); i++) {
            OptionMarketState market = OptionMarketState.load(options.getCompound(i));
            optionMarkets.put(market.id(), market);
        }
        ListTag futureContracts = tag.getList("futures_contracts", Tag.TAG_COMPOUND);
        for (int i = 0; i < futureContracts.size(); i++) {
            FuturesContract contract = FuturesContract.load(futureContracts.getCompound(i));
            futuresContracts.put(contract.id(), contract);
        }
        ListTag optionContractsTag = tag.getList("option_contracts", Tag.TAG_COMPOUND);
        for (int i = 0; i < optionContractsTag.size(); i++) {
            OptionContract contract = OptionContract.load(optionContractsTag.getCompound(i));
            optionContracts.put(contract.id(), contract);
        }
    }

    public static String futuresId(ResourceLocation commodity, int day) {
        return futuresId(commodity.toString(), day);
    }

    public static String futuresId(String underlyingProductId, int day) {
        return "F" + encodedProduct(underlyingProductId) + "_" + String.format(java.util.Locale.ROOT, "%04d", Math.max(0, day));
    }

    public static String optionId(ResourceLocation commodity, int day, OptionRight right) {
        return optionId(commodity.toString(), day, right);
    }

    public static String optionId(String underlyingProductId, int day, OptionRight right) {
        return "O" + encodedProduct(underlyingProductId) + "_" + String.format(java.util.Locale.ROOT, "%04d", Math.max(0, day)) + "_" + right.getSerializedName();
    }

    private static String encodedProduct(String productId) {
        return productId.replace(':', '_').replace('/', '_').replace(' ', '_');
    }

    private static long futuresInitialPrice(long spotPrice, int day) {
        return Math.max(1L, Math.round(spotPrice * (1.0D + Math.max(0, day) * ConfigRegistry.INSTANCE.finance().derivative().futuresCarryPerDay())));
    }

    private static long optionInitialPremium(long spotPrice, int day) {
        FinanceConfig.DerivativePricing config = ConfigRegistry.INSTANCE.finance().derivative();
        return Math.max(1L, Math.round(spotPrice * (config.optionPremiumBaseRate() + Math.min(config.optionPremiumMaxDays(), Math.max(0, day)) * config.optionPremiumPerDay())));
    }

    private static long optionPremium(long spotPrice, long strikePrice, int day, OptionRight right) {
        long intrinsic = right == OptionRight.CALL ? Math.max(0L, spotPrice - strikePrice) : Math.max(0L, strikePrice - spotPrice);
        return Math.max(1L, intrinsic + optionInitialPremium(spotPrice, day));
    }

    private static int maxDerivativeDays() {
        return Math.max(0, ConfigRegistry.INSTANCE.finance().derivative().maxDays());
    }
}


