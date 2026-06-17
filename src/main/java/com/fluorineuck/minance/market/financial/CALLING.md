# market/financial Calling Guide

## Scope

Product-agnostic financial market mechanics: liquidity surface, price levels, generic order-flow effects, market state, and price movement results.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Ensure market state | `FinancialMarketEngine.INSTANCE.ensureMarket(...)` | Product services and data load paths | Creates missing generic market state |
| Inject liquidity | `FinancialMarketEngine.INSTANCE.injectLiquidity(...)` | Product services and adapters | Mutates generic liquidity surface |
| Update market price | `FinancialMarketEngine.INSTANCE.update(...)` | Product services | Mutates generic market state and returns price result |
| Update market price from signals | `FinancialMarketEngine.INSTANCE.update(..., PriceSignalBundle)` | Product services and signal orchestration | Mutates generic market state using product-emitted generic signals |
| Build generic signal inputs | `PriceSignalBundle`, `PriceSignal`, `FundamentalAnchor` | Product services and adapters | Immutable read model, no direct side effects |
| Save/load market state | `FinancialMarketEngine.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces market state |

## Lifecycle

Called during product valuation, product update cycles, and saved-data load/save.

## Inputs

Product id, product type, current price, volatility, maturity, anchor price, config, saved state, and immutable price-signal bundles. Signal group weights come from `market.signal_weights`.

## Outputs

`FinancialMarketResult`, updated `FinancialMarketState`, immutable `PriceSignalBundle` inputs, liquidity snapshots, and saved NBT state.

## Ownership Rules

This package is product-agnostic. Product packages must convert their own facts into generic inputs before calling `FinancialMarketEngine`.

`PriceSignal` expresses product-neutral directional pressure. Positive direction raises price pressure, negative direction lowers price pressure. `FundamentalAnchor` expresses fair-value anchors such as NAV, spot price, index level, discounted payoff, or liquidation value. Product packages own extraction of those facts; `market/financial` owns only the generic carrier types and price engine.

The legacy update overload still works and behaves as an empty signal bundle. Product-specific facts must be converted before calling the signal-aware overload.

## Forbidden Bypass Calls

Do not import company reports, commodity attributes, fund holdings, derivative payoff details, or client UI state into `market/financial/`.

