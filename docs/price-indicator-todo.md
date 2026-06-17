# Price Indicator TODO

This TODO defines how real-world style indicators should affect product prices, while keeping company-specific fields separate from generic financial product pricing logic.

## Directory Boundary Rule

- Full package-scope reference: `docs/package-boundaries.md`.
- Second-level directory rule: package names must represent a domain slice. `core/` is reserved for foundation shared by sibling directories under the same parent package, not for global common code.
- Program-file rule: non-single-purpose feature implementations must be split into focused source files and grouped into the nearest domain folder instead of extending long coupled classes.
- Calling-interface rule: every feature must expose one documented calling interface, and every second-level directory must keep a local `CALLING.md` index.
- `api/`: stable extension-facing contracts only. It must not own simulation state or formulas.
- `client/`: client-only UI, charts, formatting, and debug presentation. It must not own authoritative market behavior.
- `command/`: thin debug/admin command adapters. It must not own business logic.
- `config/`: typed Java config and config registry for JSON tuning data. It must not own content rules or mutable state.
- `data/`: reload, decode, bind, and persist config/rules/world data. It must not own domain decisions or pricing formulas.
- `rule/`: declarative datapack rule schemas and registries. It must not own runtime state or IO.
- `entity/`: in-world economic actors and raw facts. Company accounting, village/villager discovery, player/merchant actor facts, and ownership records stay here.
- `market/`: product-agnostic market mechanisms, index calculation, event routing, liquidity, and generic price-pressure aggregation.
- `product/`: product-specific assets, lifecycle, payoff/NAV/settlement logic, and adapters that emit generic market signals or anchors.
- `network/`: packet and sync transport only. It must not own authoritative state or domain behavior.
- `entity/company/`: company identity, village members, operating metrics, balance sheet snapshots, financial reports, shareholder records, and company-only accounting fields.
- `product/equity/`: equity assets and adapters that convert company reports into generic equity pricing signals.
- `market/financial/`: product-agnostic price signals, liquidity surface logic, risk premia, anchor price handling, and market microstructure.
- `product/commodity/core/`: commodity identity, static/tunable attributes, and physical inventory state. This layer answers what the commodity is and how it behaves physically, not how the market trades it.
- `product/commodity/spot/`: commodity market behavior: supply/demand events, order flow, inflow/outflow, observed reference price, stabilizer activity, spot settlement, price history, and source attribution.
- `product/commodity/`: shared commodity package boundary. Core commodity attributes may be read by spot pricing, but spot behavior must not be stored in commodity attributes.
- `product/component/`: generic financial product component collection root. It groups component overlay and collection/index concepts, but does not own concrete product state, valuation formulas, persistence, or UI.
- `product/component/core/`: planned shared component attribute overlay model. It describes reusable product add-ons such as underlying exposure, maturity, settlement, payoff shape, basket exposure, NAV anchor, leverage, claim priority, liquidity wrapper, and tracking target.
- `product/component/collection/`: planned membership/index read model for `fund`, `structured`, `future`, and `option`. It may replace or absorb the current flat `GenericProductComponent` enum role.
- `product/component/derivative/`: current component derivative implementation boundary. It should be split into futures and options directories.
- `product/component/derivative/core/`: planned shared derivative primitives, only when both futures and options use them.
- `product/component/derivative/future/`: planned owner for futures-specific contracts, market state, maturity, carry, delivery, and settlement inputs.
- `product/component/derivative/option/`: planned owner for options-specific contracts, market state, right/strike/exercise state, volatility, moneyness, and payoff inputs.
- `product/component/fund/`: NAV, holdings, flows, tracking error, fees, and fund share pricing.
- `product/component/structured/`: payoff rules, basket exposure, barrier/claim state, and issuer/beneficiary risk.
- `product/liabilities/`: liability balances, interest burden, maturity wall, default pressure, and credit spread inputs.
- `product/insurance/`: insurance policies, premiums, claims, reserves, guarantees, credit insurance, and reinsurance signals.
- `config/` and `src/main/resources/data/minance/config/`: tunable weights, thresholds, caps, decay constants, and model selection. Do not hard-code balance weights or signal multipliers in service code.

## Target Architecture

- [ ] Treat funds, structured products, futures, and options as one generic financial product component collection.
  - Current collection index: `product/component/GenericProductComponent`.
  - Target collection layout:
    - `product/component/core/`: shared component attribute overlays.
    - `product/component/collection/`: collection membership/index read model.
  - Implementation owners remain `product/component/fund/`, `product/component/structured/`, `product/component/derivative/future/`, and `product/component/derivative/option/`.
  - The collection must not own product state, valuation formulas, persistence, or UI rendering.
  - `FinancialProductType.FUND`, `STRUCTURED_PRODUCT`, `FUTURE`, and `OPTION` are collection members.

- [ ] Resolve current component path/package alignment gaps.
  - Source files for fund, structured, and derivative implementations are physically under `product/component/*`.
  - Their Java package declarations still use `com.fluorineuck.minance.product.fund`, `product.structured`, and `product.derivative`.
  - Final target should align filesystem, Java package declarations, docs, commands, persistence, and UI imports.
  - Preferred target from the component architecture: `product/component/fund/`, `product/component/structured/`, `product/component/derivative/future/`, and `product/component/derivative/option/`.
  - Do not add new product behavior until this mismatch is either fixed or explicitly documented as a temporary migration state.

- [ ] Add `product/component/core/` as the shared component attribute overlay layer.
  - Core answers "what extra financial attributes are layered onto this product?", not "which concrete product owns behavior?"
  - Add planned types: `ComponentAttribute`, `ComponentAttributeSet`, `ComponentOverlay`, and `ComponentOverlayResolver`.
  - Overlay attributes should be composable and typed: `UNDERLYING_EXPOSURE`, `MATURITY`, `SETTLEMENT`, `PAYOFF_SHAPE`, `BASKET_EXPOSURE`, `NAV_ANCHOR`, `LEVERAGE`, `CLAIM_PRIORITY`, `LIQUIDITY_WRAPPER`, and `TRACKING_TARGET`.
  - Concrete products attach or expose overlays; concrete product state remains in the owner package.
  - `market/financial` may consume resolved overlays as generic signal or anchor metadata, but must not import product internals.
  - Overlay defaults, thresholds, and product-specific rule selections must live in JSON config or rule data when they are tunable.

- [ ] Separate the two derivative implementations.
  - Current `product/component/derivative/` mixes futures and options; target split:
    - `product/component/derivative/future/`: futures contracts, future market state, futures pricing inputs, and settlement hooks.
    - `product/component/derivative/option/`: option contracts, option market state, option pricing inputs, rights, and exercise hooks.
  - Keep shared derivative primitives in `product/component/derivative/core/` only if both futures and options use them.
  - Move `FuturesContract`, `FuturesMarketState`, and futures service/engine behavior to `future/`.
  - Move `OptionContract`, `OptionMarketState`, `OptionRight`, and options service/engine behavior to `option/`.
  - Keep `DerivativeSide`, `DerivativeDeliveryMethod`, and `DerivativeContractStatus` in local `core/` only if both sides need them; otherwise move each type to its owner.
  - `CommodityDerivativeService` should become a thin orchestrator or split into `FutureMarketService` and `OptionMarketService`.
  - Each split directory must get a local `CALLING.md` that names the unified calling interface.

- [ ] Add a generic `PriceSignal` model under `market/financial/`.
  - Signal fields: `productId`, `productType`, `source`, `direction`, `strength`, `confidence`, `horizonTicks`, `anchorPrice`, `liquidityBid`, `liquidityAsk`.
  - Direction should be generic: positive raises price pressure, negative lowers price pressure.
  - Keep signal source typed enough for UI/debugging without coupling market logic to company classes.

- [ ] Add a generic `FundamentalAnchor` concept under `market/financial/`.
  - Examples: NAV, replacement cost, spot price, index value, discounted payoff, liquidation value.
  - The engine should consume anchors generically instead of knowing company fields.

- [ ] Change `FinancialMarketEngine.update(...)` to consume a bundle of price signals.
  - Existing order-flow imbalance remains a signal.
  - Anchoring, mean reversion, momentum, panic, and liquidity impact should use generic signal inputs.
  - Company NAV must enter as an equity anchor, not as a company-specific dependency inside `market/financial`.

- [ ] Add config weights for signal groups.
  - `fundamental_weight`
  - `earnings_weight`
  - `flow_weight`
  - `risk_weight`
  - `liquidity_weight`
  - `sentiment_weight`
  - Product-specific overrides should live in `finance.json`.

## Real Financial Institution Model

The simulation should model only the few institution roles needed for price formation. Do not mirror every real-world institution as a separate subsystem unless it creates distinct game behavior.

- [ ] Enforce a no LLM-Agent architecture rule.
  - Do not implement financial institutions as LLM agents, prompt-driven actors, natural-language planners, or external model calls.
  - Institution behavior must be deterministic or pseudo-random from explicit state, config, and formulas.
  - Allowed patterns: rule engines, state machines, scoring functions, scheduled services, order-flow generators, and data-driven adapters.
  - Every institution action must be replayable from saved state plus config.

- [ ] Add a simplified role model for institutions.
  - `central_bank_and_securities`: the current default public service provider represented by the market UI. It combines central-bank-style base rates/liquidity support with securities-market access, brokerage, listing, clearing access, index publication, and derivative/fund/structured-product service entry points.
  - `issuer`: creates securities or claims from an underlying entity, such as company equity, debt, fund shares, or structured products.
  - `asset_manager`: manages funds, index products, holdings, subscriptions/redemptions, rebalancing, fees, and tracking error.
  - `liquidity_provider`: combines market maker, broker/dealer, venue, and ETF authorized participant behavior into one deterministic market-liquidity layer.
  - `credit_underwriter`: combines simplified bank, securities firm, and rating-service behavior. It produces ratings, credit limits, spread quotes, and underwriting decisions for entities, issues, facilities, or tranches.
  - `clearing_and_custody`: combines settlement, margin, custody, collateral, ownership records, and default handling.

- [ ] Place institution state in the right domain.
  - Default public institution identity and role grants: future `entity/institution/`.
  - Player-created institution identity, licenses, service grants, ownership, and operator permissions: future `entity/institution/` plus `entity/player/`.
  - Issuer fields for companies: `entity/company/`.
  - Issuer adapters for products: the corresponding `product/*/` package.
  - Asset manager state: `product/component/fund/` and index-tracking product code.
  - Credit assessment and underwriting state: `product/liabilities/credit/`.
  - Liquidity provider state: `market/financial/`.
  - ETF creation/redemption rules: `product/component/fund/` or `product/index/` if created, with liquidity effects emitted into `market/financial/`.
  - Clearing, custody, margin, and default state: `market/financial/` plus `product/liabilities/`.

- [ ] Define the current market UI as a default institution service terminal.
  - The current `MarketPanelElement` / LDLib dashboard should be treated as a terminal for the default `central_bank_and_securities` provider.
  - The UI may display market-wide data, listed products, derivatives, funds, structured products, credit assessments, deposits, and debug attribution through that provider context.
  - The UI must not be the authority that creates prices, grants credit, lists products, clears trades, or settles derivatives.
  - Server/domain services must accept an explicit service-provider context where behavior depends on who provides the market service.
  - Current keyboard/command access may default to `central_bank_and_securities` for development and public-market access.
  - Future block/menu terminals can bind to default or player-owned providers without duplicating pricing logic.
  - UI labels should eventually show the active provider, such as default public market, player broker, player exchange, or player fund manager.

- [ ] Preserve player-buildable financial infrastructure.
  - Players should be able to create or operate their own financial institutions when the gameplay layer is added.
  - Player-owned institutions may expose a subset of services: brokerage, exchange/venue, market making, asset management, underwriting/rating, deposit/lending desk, derivative desk, clearing/custody, or insurance desk.
  - Player-built services must use the same documented domain service interfaces as the default provider.
  - Player institutions should have capital, inventory, licenses/permissions, risk limits, fees, collateral, reputation, and service availability.
  - Player institutions may compete with or route through the default public provider depending on configuration and licenses.
  - Player access should come from in-world blocks, menus, contracts, permissions, or company ownership, not only from `/market` commands or the global debug UI.
  - If a player provider lacks a capability, calls should fail with an explicit missing-service state instead of falling back silently to the default provider.

- [ ] Add minimal institution balance-sheet mechanics.
  - Assets: cash, inventory, securities, loans, collateral, receivables.
  - Liabilities: deposits, debt, payables, margin obligations, redemption obligations.
  - Equity capital: loss buffer and capacity constraint.
  - Income: spread, management fees, creation/redemption fees, settlement fees.
  - Risk limits: leverage cap, liquidity reserve, inventory cap, concentration cap, margin requirement.

- [ ] Add institution-driven price channels.
  - Liquidity providers narrow spreads when inventory/risk capacity is healthy.
  - Liquidity providers widen spreads or withdraw depth when inventory/risk limits bind.
  - Liquidity providers convert aggregate client demand into order flow and fees.
  - Asset managers create predictable rebalancing and subscription/redemption flows.
  - ETF creation/redemption arbitrages premium/discount against NAV through deterministic thresholds.
  - Clearing margin increases forced liquidation pressure during stress.
  - Institution-issued credit assessments and underwriting decisions affect debt yield, equity risk discount, structured product valuation, and credit-derivative spreads.

- [ ] Add institution config under `config/`.
  - Fee rates.
  - Spread targets.
  - Inventory limits.
  - Capital/leverage limits.
  - Margin schedule.
  - Redemption/creation thresholds.
  - Rebalance frequency.

## Index Products And Tracking

Index logic should separate index calculation from investable products that track the index.

- [ ] Split index calculation from index-tracking products.
  - Index definitions and levels: `market/index/`.
  - Index funds and ETF-like products: `product/component/fund/` or a future `product/index/`.
  - Futures/options on indexes: `product/component/derivative/future/` and `product/component/derivative/option/`.
  - UI/debug display: `client/ui/`.

- [ ] Generalize index definitions in `market/index/`.
  - `IndexDefinition`: id, name, product universe, eligibility filters, weighting method, rebalance rules.
  - `IndexConstituent`: product id, free-float quantity or basket quantity, weight, cap, sector/category.
  - `IndexLevel`: current level, previous level, divisor, history, component count.
  - `IndexRebalance`: added/removed constituents, target weights, turnover, effective tick.

- [ ] Support stock/equity index methodologies.
  - Market-cap weighted: `sharePrice * sharesOutstanding`.
  - Free-float market-cap weighted: `sharePrice * freeFloatShares`.
  - Price weighted: component share price divided by index divisor.
  - Equal weighted: each eligible component receives equal target weight.
  - Factor/smart-beta weighted: value, quality, momentum, volatility, liquidity, or profitability signals.
  - Cap constituent weights to avoid one company dominating the index.

- [ ] Support spot/commodity index methodologies.
  - Production-weighted basket.
  - Consumption-weighted basket.
  - Liquidity/volume-weighted basket.
  - Inventory-sensitive basket.
  - Fixed basket with periodic rebalance.
  - Commodity spot indexes should use `product/commodity/` data and remain separate from equity index logic.

- [ ] Support other index families.
  - Bond/credit indexes: institution-issued rating or spread quote, issue seniority, duration, yield, observed spread, and realized credit events.
  - Fund indexes: fund NAV and fund share liquidity.
  - Derivative volatility indexes: implied/realized volatility inputs.
  - Custom economy indexes: food, crops, minerals, tools, village productivity, credit stress.

- [ ] Add index maintenance mechanics.
  - Eligibility screening.
  - Reconstitution: membership changes.
  - Rebalance: target weight changes.
  - Divisor adjustment: keep continuity after splits, issuance, deletions, or basket changes.
  - Corporate actions: issuance, buyback, split, dividend, delisting/default.
  - Tracking impact: rebalances create predictable buy/sell pressure.

- [ ] Add index-tracking methods.
  - Full physical replication: hold every constituent in target weights.
  - Sampling/optimization: hold a subset with similar exposure and lower trading cost.
  - Synthetic replication: use swaps/futures/options to match return, with counterparty risk.
  - Futures overlay: use index futures or commodity futures for fast exposure.
  - Cash equitization: use derivatives or temporary baskets while cash is waiting for deployment.
  - Enhanced/factor tracking: intentionally tilt away from pure index weights.

- [ ] Add ETF-like creation/redemption.
  - Track fund NAV from underlying basket.
  - Track secondary-market share price separately from NAV.
  - Authorized participants create shares when market price trades rich to NAV.
  - Authorized participants redeem shares when market price trades cheap to NAV.
  - Creation/redemption should inject basket trades into underlying products.
  - Premium/discount to NAV should be visible in UI/debug.

- [ ] Add tracking quality metrics.
  - Tracking error.
  - Tracking difference.
  - Expense drag.
  - Turnover cost.
  - Cash drag.
  - Sampling error.
  - Premium/discount to NAV.
  - Creation/redemption volume.

- [ ] Add index product price channels.
  - Index level moves NAV.
  - NAV anchors fund/ETF fair value.
  - Secondary market demand creates premium/discount.
  - Authorized participant arbitrage compresses premium/discount.
  - Rebalancing creates predictable order flow in underlying constituents.
  - Fees and expenses create long-run underperformance versus the index.

## Company And Equity Indicators

Company-specific raw fields stay in `entity/company/`; only converted equity signals move into `product/equity/`.

- [ ] Extend company reports with operating statement fields in `entity/company/`.
  - Revenue or production income.
  - Operating cost.
  - Gross margin.
  - Operating margin.
  - Net income.
  - Cash flow.
  - Retained earnings.

- [ ] Extend company reports with balance sheet fields in `entity/company/`.
  - Cash.
  - Inventory value.
  - Productive asset value.
  - Financial asset value.
  - Liabilities.
  - Net asset value.
  - Book value per share.

- [ ] Add leverage and solvency metrics in `entity/company/`.
  - Debt-to-assets.
  - Debt-to-equity.
  - Interest coverage.
  - Current ratio.
  - Default pressure.
  - These fields should not live in `market/financial`.
  - These fields are raw assessment inputs; they are not the final credit rating or credit line.

- [ ] Add profitability metrics in `entity/company/`.
  - ROA.
  - ROE.
  - Profit margin.
  - Earnings growth.
  - Cash-flow growth.

- [ ] Add an equity signal adapter in `product/equity/`.
  - Convert company NAV into a fundamental anchor.
  - Convert earnings growth into positive/negative price pressure.
  - Convert margin deterioration into negative price pressure.
  - Convert issuer credit assessment or `UNRATED` state into risk discount after the credit model exists.
  - Convert dividend or retained earnings into shareholder return expectation.

- [ ] Keep equity asset pricing in `product/equity/`.
  - Common stock, founder stock, preferred stock, treasury share, and pre-IPO stock should share generic equity pricing inputs where possible.
  - Company-only fields should be read through an adapter, not copied into `EquityAsset`.

- [ ] Update company UI/debug panels to show both values.
  - `NAV per share`: accounting/fundamental estimate.
  - `Share price`: market price from order flow and pricing engine.
  - `Premium/discount to NAV`: `(sharePrice - navPerShare) / navPerShare`.

## Commodity Indicators

Commodity-specific logic stays under `product/commodity/`, with `core` commodity attributes and physical state separated from `spot` market behavior. Generic financial-market pricing should consume commodity outputs as anchors or signals, not own commodity attributes, inventory semantics, or spot-market behavior.

- [ ] Keep the commodity model deterministic.
  - No LLM-Agent, prompt-driven actor, or external model call may decide commodity supply, demand, inventory, or prices.
  - Allowed inputs are events, saved state, data-driven rules, seeded randomness where stored, and explicit formulas.

- [ ] Separate commodity attributes from market behavior.
  - Commodity attributes answer what the item is: item id, category, unit, perishability, storage/carry traits, base/reference seed, production tags, consumption tags, substitution group, and seasonality profile.
  - Physical commodity state answers what exists: current inventory, reserved inventory, quality/decay state if added, and last physical settlement tick.
  - Market behavior answers how the item trades: supply events, demand events, order-flow imbalance, inflow/outflow, stabilizer flow, observed reference price, price history, and settlement/clamp attribution.
  - Attribute/profile data belongs in `product/commodity/core/` and config/rule data.
  - Market behavior belongs in `product/commodity/spot/`.

- [ ] Split the current `CommodityState` responsibilities into clearer concepts.
  - `CommodityAttributes` or `CommodityProfile`: mostly data-driven static/tunable fields such as category, unit, perish rate, storage cost, base price seed, target inventory policy, production/consumption tags, substitution group, and seasonality rules.
  - `CommodityInventoryState`: mutable physical stock fields such as inventory, reserved inventory, spoilage/quality state, and inventory settlement metadata.
  - `SpotMarketState`: mutable market-price fields such as current price, reference price observed from trades, price history, volatility estimate, and last settlement tick.
  - `SpotMarketCycleState`: resettable per-cycle behavior such as inflow, outflow, production flow, consumption flow, buy/sell order counts, source breakdowns, and stabilizer activity.
  - The spot engine may read commodity attributes and inventory state, but it mutates only market state, cycle state, and explicitly owned inventory-settlement fields.

- [ ] Preserve the current commodity architecture, but make the state split explicit.
  - Input/event layer: `entity/villager/VillagerTradeDiscoveryService` discovers profession production, villager demand, and merchant offers.
  - Spot aggregation layer: `SpotMarketService` records `SpotMarketSource` supply/demand events into per-item `SpotMarketAsset`.
  - Commodity core layer: commodity attributes/profile and inventory state own item identity, physical traits, target inventory policy, and physical stock.
  - Spot market layer: spot state and cycle state own reference-price observation, inflow/outflow, order counts, stabilizer flow, price history, and settlement attribution.
  - Spot pricing layer: `SpotMarketEngine` turns state and flow into the next spot price.
  - Downstream layer: `MarketIndexService`, derivatives, funds, and future index products consume spot prices as anchors.

- [ ] Document the current spot price formula as the baseline behavior.
  - `inventory_ratio = inventory / max(target_inventory, 1)`
  - `flow_pressure = outflow - inflow`
  - `stabilizer_pressure = signed stabilizer flow`
  - `target_price = reference_price * (1 + inventory_weight * (1 - inventory_ratio)) * (1 + flow_weight * flow_pressure) * (1 + stabilizer_weight * stabilizer_pressure)`
  - `next_price = current_price + (target_price - current_price) * adjustment_speed`
  - Clamp `next_price` by `max_cycle_change`, `min_price`, and `max_price`.

- [ ] Clarify current pressure semantics.
  - Inventory below target raises price.
  - Inventory above target lowers price.
  - Demand/consumption outflow raises price.
  - Supply/production inflow lowers price.
  - Stabilizer buy flow raises price pressure.
  - Stabilizer sell flow lowers price pressure.
  - Reference price is slowly updated from observed source prices through `reference_observed_weight`.

- [ ] Clarify current supply sources.
  - `VILLAGER_PRODUCTION`: profession-driven production; increases production rate, inflow, and inventory.
  - `VILLAGER_SELL_ORDER`: merchant offer result; increases supply flow and inventory.
  - `IMPORT`: external inflow source.
  - `EXPORT` is currently classified as company actor in flow snapshots but should be reviewed for commodity semantics.
  - `COMPANY_ORDER`, `PLAYER_ORDER`, and `EVENT` should remain generic extension points.

- [ ] Clarify current demand sources.
  - `VILLAGER_BUY_ORDER`: profession demand or merchant offer costs; consumes inventory and increases outflow.
  - `PLAYER_ORDER`: player-origin demand or supply once player trade hooks are added.
  - `COMPANY_ORDER`: company/fund/index/derivative demand once product adapters emit real commodity orders.
  - `EVENT`: shocks such as disaster, harvest, war, seasonal demand, or scripted economy events.

- [ ] Clarify current stabilizer behavior.
  - Stabilizer applies only to configured source types.
  - Probability increases when current price deviates from the observed baseline.
  - If current price is above baseline, stabilizer adds sell flow.
  - If current price is below baseline, stabilizer adds buy flow.
  - If price equals baseline, stabilizer adds a smaller neutral counter-flow against the original direction.
  - Stabilizer is a deterministic model target with optional seeded pseudo-randomness; avoid hidden non-replayable randomness in final architecture.

- [ ] Clarify update cadence.
  - Discovery/scanning cadence comes from `company.loaded_village_scan_interval_ticks`.
  - Spot settlement cadence comes from `market.settlement_interval_ticks`.
  - Index update cadence comes from `market.index_update_interval_ticks`.
  - On settlement: update spot prices, update spot indexes, update all funds.
  - Between settlements: update indexes on their own cadence from current spot market state.

- [ ] Keep commodity indexes as a separate read model.
  - Commodity indexes should consume settled/current spot prices and order-pressure adjustments.
  - Index calculation belongs under `market/index/`.
  - Index-tracking products belong under `product/component/fund/` or future `product/index/`.
  - Commodity spot pricing must not depend on index products except through explicit `COMPANY_ORDER` or index/fund demand adapters.

- [ ] Add commodity signal adapter after generic `PriceSignal` exists.
  - Emit `FundamentalAnchor` from spot price and reference price.
  - Emit inventory pressure signal.
  - Emit flow pressure signal.
  - Emit stabilizer pressure signal.
  - Emit volatility/risk signal from price history and configured commodity volatility.
  - Keep this adapter under `product/commodity/`, not `market/financial/`.

- [ ] Add missing real commodity cost components without mixing attributes and market behavior.
  - `perish_rate` should live in commodity attributes/profile data and be applied through inventory settlement.
  - `storage_cost` should live in commodity attributes/profile data and be priced through the spot engine.
  - Add carrying cost pressure: higher storage cost raises fair forward/carry value but may lower spot demand if inventory is excessive.
  - Add spoilage pressure: perishable surplus should increase sell pressure before spoilage and reduce inventory after spoilage.
  - Add scarcity floor: critical low inventory should create nonlinear upward pressure.

- [ ] Add seasonality and production capacity.
  - Production capacity by item/category.
  - Seasonal production multiplier.
  - Seasonal demand multiplier.
  - Shock events for drought, supply disruption, festival demand, or trade route changes.
  - All multipliers and event weights must live in config/data rules.

- [ ] Add cross-product demand channels.
  - Company production inputs should create commodity demand.
  - Company output should create commodity supply.
  - Futures expiry/delivery should create spot demand or supply when physically settled.
  - Index-tracking products should create basket demand on rebalance.
  - Funds should create commodity demand only through explicit holdings or derivative overlay.

- [ ] Keep commodity rules data-driven in `commodity.json`, `market.json`, and future commodity rule files.
  - Commodity attribute parameters: category, unit, base price seed, inventory target policy, perish/storage/carry parameters, production/consumption tags, substitution group, and seasonality profile.
  - Market behavior parameters: supply/demand weights, reference-price observation weight, stabilizer probability and quantity rules, max cycle move, and source-specific flow weights.
  - Shock parameters and event weights.

- [ ] Commodity acceptance criteria.
  - Spot prices are reproducible from saved state plus config.
  - No commodity pricing code imports company report classes.
  - No generic financial engine code owns commodity inventory semantics.
  - Commodity attributes/profile objects do not store inflow, outflow, order counts, price history, or stabilizer activity.
  - Market cycle state can be reset after settlement without losing commodity attributes or physical inventory state.
  - The current `CommodityState` mix of attributes, physical inventory, and market-cycle behavior is either split or wrapped behind interfaces that expose those concepts separately.
  - Price movement debug output can show reference, inventory, flow, stabilizer, spoilage/storage, and clamp contributions.
  - Existing default config preserves current spot price behavior until new components are enabled.

## Futures And Options Indicators

Derivative-specific logic should be split between `product/component/derivative/future/` and `product/component/derivative/option/`; shared primitives stay in `product/component/derivative/core/` only when both implementations use them. It should not depend directly on company accounting fields.

- [ ] Futures price inputs.
  - Underlying spot price.
  - Time to maturity.
  - Carry cost.
  - Storage cost for commodities.
  - Expected future supply/demand.
  - Liquidity and margin pressure.

- [ ] Options price inputs.
  - Underlying price.
  - Strike.
  - Time to maturity.
  - Realized volatility.
  - Implied volatility proxy.
  - Moneyness.
  - Skew from order flow.

- [ ] Map derivative indicators to price.
  - Higher underlying raises call value and lowers put value.
  - Higher volatility raises option premium.
  - Near expiry accelerates time decay.
  - Futures converge toward expected spot near maturity.

- [ ] Keep derivative model configuration in `finance.json`.
  - Carry rate.
  - Volatility premium.
  - Time decay curve.
  - Max derivative depth.
  - Margin/liquidation thresholds if added.

## Fund Indicators

Fund-specific logic stays under `product/component/fund/`.

- [ ] Price fund shares from NAV.
  - Holdings market value.
  - Cash.
  - Liabilities or fees.
  - Total fund shares.

- [ ] Add fund flow indicators.
  - Net subscriptions.
  - Redemptions.
  - Tracking demand.
  - Liquidity stress.

- [ ] Map fund indicators to price.
  - NAV is the primary anchor.
  - Large inflows can create premium pressure.
  - Large redemptions can create discount pressure.
  - Tracking error reduces confidence.

## Structured Product Indicators

Structured product logic stays under `product/component/structured/`.

- [ ] Define payoff inputs.
  - Underlying basket.
  - Participation rate.
  - Cap/floor.
  - Barrier.
  - Claim priority.
  - Beneficiary exposure.

- [ ] Define risk inputs.
  - Issuer credit assessment, spread quote, or `UNRATED` state from `product/liabilities/credit/`.
  - Barrier distance.
  - Underlying volatility.
  - Time to maturity.
  - Liquidity discount.

- [ ] Map structured indicators to price.
  - Higher expected payoff raises fair value.
  - Weaker issuer assessment or wider issuer spread quote lowers value.
  - Higher barrier breach probability changes payoff-weighted value.

## Liability And Credit Indicators

Credit and liability logic should live under `product/liabilities/` and feed risk signals into relevant products. Credit is not a generic entity tag or universal component attribute. It is an institution-issued assessment, quote, or credit-extension relationship.

- [ ] Model credit as an institution-issued relationship.
  - Target owner: `product/liabilities/credit/`.
  - A bank, securities firm, rating service, or simplified `credit_underwriter` issues the credit assessment.
  - The assessment subject may be an entity, issuer, facility, debt issue, deposit institution, structured tranche, reference obligation, or counterparty.
  - Multiple institutions may issue different assessments for the same subject.
  - Company/accounting facts in `entity/company/` are inputs, not the source of truth for the final rating or credit line.
  - Do not compute final credit from entity tags such as profession, sector, village type, or simple issuer category.
  - Ratings, credit limits, spread quotes, and underwriting decisions must be persisted as dated assessment records.

- [ ] Define planned credit assessment records.
  - `CreditAssessment`: assessing institution id, subject id, subject type, optional product/facility/tranche id, assessment scope, rating scale, rating grade, outlook/watch state, confidence, methodology id, valid-from tick, valid-until tick.
  - `CreditLimit`: lender/underwriter id, borrower id, facility id, committed amount, drawn amount, undrawn amount, collateral requirement, covenants, maturity, renewal terms, and status.
  - `CreditSpreadQuote`: quoting institution id, reference obligation or entity id, seniority, secured/unsecured flag, maturity bucket, bid/ask spread, implied default probability, recovery assumption, liquidity adjustment, and quote timestamp.
  - `CreditEventRecord`: bankruptcy/default, failure to pay, restructuring, moratorium/freeze, acceleration, repudiation, or settlement failure when those events are modeled.

- [ ] Define credit assessment inputs.
  - Company solvency facts: leverage, liquidity, interest coverage, cash flow, profitability, asset quality, and operating volatility.
  - Product terms: seniority, collateral, covenants, maturity, amortization, guarantees, insurance, and subordination.
  - Institution view: risk appetite, exposure limit, concentration limit, funding cost, capital constraint, and assessment methodology.
  - Market view: observed debt prices, credit spreads, CDS spreads when implemented, liquidity, and recent credit events.
  - External rule/config view: rating scale definitions, transition matrices, base default curves, sector modifiers, collateral haircuts, and recovery assumptions.

- [ ] Map institutional credit outputs to product logic.
  - Debt products consume the latest applicable rating, spread quote, or credit limit from `product/liabilities/credit/`.
  - Equity risk discount may consume issuer assessment as an input, but equity code must not calculate or own the rating.
  - Structured products consume issuer, collateral, tranche, and counterparty assessments through explicit references.
  - Funds consume credit assessments only through holdings, tracking rules, or risk screens.
  - Market/financial consumes normalized signals emitted from assessments, not raw entity tags or company report classes.
  - If no institution has assessed a subject, use an explicit `UNRATED`/`NO_QUOTE` state instead of inventing a hidden generic score.

- [ ] Define the liability product taxonomy.
  - Operating liabilities: accounts payable, accrued expenses, wages payable, taxes payable, deferred revenue, and settlement payables. These are company/accounting facts unless they are explicitly financed, traded, securitized, or defaultable as a product.
  - Deposit products: demand deposits, checking/current accounts, savings deposits, money-market deposit accounts, term deposits, certificates of deposit, negotiable CDs, notice deposits, call deposits, brokered deposits, wholesale deposits, interbank deposits, escrow/restricted deposits, and insured/uninsured deposit balances.
  - Loan products: term loans, revolvers, credit lines, mortgages, commercial real estate loans, auto loans, student loans, personal loans, payday loans, reverse mortgages, margin loans, bridge loans, asset-based loans, and working-capital facilities.
  - Trade and receivable finance: trade credit, invoice finance, factoring, receivables-backed borrowing, inventory finance, equipment finance, and supply-chain finance.
  - Debt securities: corporate bonds, municipal bonds, Treasury securities, agency securities, savings bonds, commercial paper, medium-term notes, secured/unsecured notes, guaranteed/insured bonds, convertible bonds, callable/putable bonds, zero-coupon debt, floating-rate notes, inflation-linked debt, senior debt, subordinated debt, and perpetual or hybrid debt.
  - Securitized credit: asset-backed securities, mortgage-backed securities, collateralized mortgage obligations, residential/commercial mortgage-backed securities, collateralized loan obligations, collateralized debt obligations, and receivables-backed pools.
  - Funding liabilities: deposits, repo funding, securities lending cash collateral, interbank borrowing, discount-window borrowing, margin obligations, redemption obligations, and clearing/settlement obligations.

- [ ] Add deposit products as a dedicated liability subdomain.
  - Target owner: `product/liabilities/deposit/`.
  - Deposits are issuer-side funding liabilities and holder-side cash-like assets.
  - They are not loans made by the depositor to a company in the operating-accounting sense, and they are not funds, insurance, or market indexes.
  - First implementation should add a local `CALLING.md` if `deposit/` becomes a real subdirectory.
  - Deposit services should expose one unified entry point for account lifecycle, interest accrual, withdrawal/deposit flows, and rate signal extraction.

- [ ] Define deposit product categories.
  - Demand/checking/current deposit: payable on demand, high liquidity, often low or zero interest.
  - Savings deposit: withdrawable account with posted savings rate and possible withdrawal limits.
  - Money-market deposit account: deposit account with money-market-style rate behavior and balance tiers.
  - Time deposit / term deposit / certificate of deposit: fixed term, fixed or formula rate, maturity date, early-withdrawal penalty.
  - Negotiable certificate of deposit: transferable time deposit with secondary-market price and rate sensitivity.
  - Notice deposit: withdrawal requires notice period; rate should reflect reduced liquidity.
  - Call deposit: callable/withdrawable on short notice, often institution/wholesale oriented.
  - Brokered deposit: sourced through a broker channel; may carry different stability and funding-cost assumptions.
  - Interbank deposit: bank-to-bank funding product; belongs to institution funding and liquidity simulation.
  - Escrow/restricted deposit: balance is restricted by contract or settlement condition and should not be freely withdrawable.
  - Insured vs uninsured deposits: insurance coverage changes depositor loss risk and run sensitivity.

- [ ] Define core deposit state.
  - Depository institution.
  - Depositor/account owner.
  - Account id.
  - Deposit category.
  - Balance/principal.
  - Currency or settlement unit.
  - Posted interest rate.
  - Effective annual yield.
  - Rate type: fixed, floating, teaser, tiered, or admin-set.
  - Rate index or benchmark spread.
  - Compounding cadence.
  - Interest payment cadence.
  - Opening tick and maturity tick if time-based.
  - Withdrawal notice period.
  - Early-withdrawal penalty.
  - Minimum balance.
  - Balance tier.
  - Fees.
  - Insurance coverage limit and insured amount.
  - Restricted/escrow flag.
  - Rollover/renewal setting.
  - Account status: open, restricted, matured, closed, defaulted, or frozen.

- [ ] Define deposit-rate inputs.
  - Base policy/funding rate.
  - Deposit beta: how strongly the product rate follows the base rate.
  - Term premium for longer maturities.
  - Balance-tier premium or discount.
  - Liquidity premium/discount from withdrawal flexibility.
  - Institution funding need.
  - Institution solvency/default risk.
  - Deposit insurance protection.
  - Market competition/spread target.
  - Run-risk/stability score.
  - Admin cap/floor and promotional rate rules.

- [ ] Map deposit indicators to value, yield, and flows.
  - Demand deposits should value near par unless institution default/freeze risk is present.
  - Accrued interest increases depositor claim and institution liability.
  - Higher base/funding rate raises new deposit rates and reprices floating deposits.
  - Fixed-rate time deposits gain or lose secondary-market value when market rates change, if transferable.
  - Longer lock-up or notice periods should pay higher rates when liquidity is valuable.
  - Higher early-withdrawal penalty lowers effective liquidity and changes depositor behavior.
  - Stronger insurance coverage lowers depositor loss risk and run sensitivity.
  - Higher institution funding need raises offered deposit rates but may also signal solvency/liquidity stress.
  - High withdrawal pressure reduces institution liquidity and can widen funding spreads.
  - Deposit outflows should feed institution liquidity signals, not generic price movement directly.

- [ ] Define core liability state.
  - Issuer/borrower.
  - Holder/lender.
  - Applicable credit assessment id or explicit `UNRATED` state.
  - Underwriting institution id when a facility or issuance is underwritten.
  - Principal/notional.
  - Coupon or interest rate.
  - Fixed/floating rate index.
  - Maturity and amortization schedule.
  - Payment frequency.
  - Seniority and claim priority.
  - Secured/unsecured status.
  - Collateral type and collateral value.
  - Covenants, margin requirements, and call/put/convertibility terms.
  - Default state, delinquency state, recovery estimate, and restructuring state.

- [ ] Define liability pricing inputs.
  - Risk-free rate or base funding curve.
  - Credit spread from institution quote or market-implied quote.
  - Probability of default from the applicable credit assessment, not from entity tags.
  - Loss given default from the applicable credit assessment and collateral/recovery model.
  - Recovery rate from the applicable assessment, market quote, or recovery model.
  - Collateral haircut.
  - Duration and convexity.
  - Liquidity discount.
  - Subordination discount.
  - Covenant breach pressure.
  - Refinancing pressure and maturity wall.
  - Deposit rate, deposit beta, withdrawal liquidity, and run-risk inputs when the liability is a deposit product.

- [ ] Map liability indicators to price.
  - Higher assessed or implied default probability raises yield/spread and lowers debt market price.
  - Higher assessed recovery rate or stronger collateral lowers spread and raises debt price.
  - Higher seniority lowers loss severity relative to subordinated claims.
  - Longer duration increases sensitivity to interest-rate changes.
  - Near maturity raises refinancing pressure if cash flow is weak.
  - Covenant breach or missed payment accelerates default pressure.
  - Wider market liquidity spread lowers secondary-market price.
  - Deposit products usually do not have a market price; they expose par value, accrued interest, effective yield, withdrawal value after penalties, and institution funding/liquidity signals.

- [ ] Route credit-adjacent products to the correct owner.
  - Cash debt instruments and borrower obligations: `product/liabilities/`.
  - Bond funds, loan funds, and credit ETFs: `product/component/fund/`, with holdings referencing `product/liabilities/`.
  - Structured credit notes, tranched pools, and payoff waterfalls: `product/component/structured/`, with credit exposure sourced from `product/liabilities/`.
  - Credit derivatives such as CDS, CDS indexes, LCDS, total-return swaps on debt, credit spread options, and CDS swaptions: future `product/component/derivative/credit/`.
  - Credit-linked notes and synthetic CDOs: `product/component/structured/` unless implemented as bilateral derivatives.
  - Guarantees, surety bonds, financial guarantee insurance, mortgage insurance, and trade credit insurance: `product/insurance/`.

- [ ] Define credit-derivative logic as reference-based, not tag-based.
  - Target owner: future `product/component/derivative/credit/`.
  - A credit derivative must reference a `referenceEntity`, `referenceObligation`, `referenceIndex`, facility, tranche, or counterparty.
  - Pricing inputs include quoted credit spread, assessment-implied default probability, recovery assumption, maturity, protection premium, accrued premium, counterparty assessment, collateral/margin, liquidity, and settlement convention.
  - Payoff triggers should come from explicit `CreditEventRecord` and contract terms, not entity tags.
  - CDS premium/spread rises when the applicable assessment weakens, market-implied spread widens, recovery assumption falls, or credit-event probability rises.
  - Total-return swaps on debt pass through price/yield movement of the referenced debt plus financing and counterparty risk.
  - Credit spread options and CDS swaptions should reference spread curves or CDS contracts, not raw borrower tags.
  - Credit-linked notes carry issuer risk plus embedded credit-derivative exposure; if implemented as issued notes, route them to `product/component/structured/`.

- [ ] Keep liability model configuration data-driven.
  - Credit rating scale definitions.
  - Credit assessment methodology ids and weights.
  - Spread curves.
  - Default probability curves.
  - Recovery assumptions.
  - Collateral haircuts.
  - Maturity buckets.
  - Seniority ranking.
  - Covenant thresholds.
  - Delinquency/default transition probabilities.
  - Restructuring/recovery timing.
  - Deposit rate curves.
  - Deposit beta by account category.
  - Balance-tier thresholds.
  - Early-withdrawal penalty rules.
  - Insurance coverage limits.
  - Withdrawal/run-risk parameters.

- [ ] Convert institutional credit assessments and credit events into risk discount signals.
- [ ] Feed issuer assessment into equity valuation through `product/equity/`, not directly through `market/financial`.
- [ ] Feed issuer, tranche, collateral, and counterparty assessments into structured products through `product/component/structured/`.

## Insurance And Credit Protection Indicators

Insurance logic should live under `product/insurance/`.

- [x] Standardize the package name.
  - Standardized the placeholder package as `product/insurance/` before adding real insurance products.
  - Updated docs and `CALLING.md` in the same change. No package declarations, imports, saved-data keys, command paths, or UI labels existed for the placeholder.
  - Keep a compatibility/migration note if old saved data ever references the misspelled name.

- [ ] Define the general insurance product taxonomy.
  - Life insurance.
  - Annuities.
  - Health insurance.
  - Disability insurance.
  - Long-term care insurance.
  - Property/homeowners insurance.
  - Auto insurance.
  - Flood insurance.
  - Travel insurance.
  - Cyber insurance.
  - Commercial property/casualty insurance.
  - General liability insurance.
  - Professional liability/errors-and-omissions insurance.
  - Directors-and-officers liability insurance.
  - Workers compensation insurance.
  - Reinsurance and retrocession.
  - Catastrophe risk products if disaster/event risk becomes a tradable product.

- [ ] Define credit-specific insurance and guarantee products.
  - Mortgage insurance.
  - Trade credit insurance.
  - Credit life insurance.
  - Credit disability insurance.
  - Credit unemployment/payment-protection insurance.
  - Financial guarantee insurance or bond insurance.
  - Surety bonds, performance bonds, and payment bonds.
  - Letter-of-credit-like guarantees when modeled as insurance/guarantee rather than bank funding.
  - Political risk or sovereign risk insurance.
  - Deposit insurance or resolution guarantee only if institution/funding risk is modeled explicitly.

- [ ] Define core insurance state.
  - Insurer.
  - Policyholder.
  - Insured exposure.
  - Beneficiary.
  - Premium schedule.
  - Coverage limit.
  - Deductible or attachment point.
  - Retention.
  - Term and renewal state.
  - Exclusions.
  - Claim trigger.
  - Claim state and claim amount.
  - Loss reserve.
  - Unearned premium reserve.
  - Reinsurance coverage.
  - Insurer capital/solvency state.

- [ ] Define insurance pricing inputs.
  - Expected loss frequency.
  - Expected loss severity.
  - Exposure amount.
  - Coverage limit.
  - Deductible/retention.
  - Correlation/concentration risk.
  - Expense load.
  - Capital cost.
  - Reserve requirement.
  - Reinsurance cost.
  - Catastrophe/event stress multiplier.
  - Insurer default risk.

- [ ] Map insurance indicators to price.
  - Higher loss probability raises premium.
  - Higher loss severity raises premium and reserves.
  - Higher deductible lowers premium.
  - Higher coverage limit raises premium and capital requirement.
  - Better collateral, stronger borrower quality, or higher recovery lowers credit-insurance premium.
  - Reinsurance lowers retained loss volatility but adds reinsurance cost and counterparty risk.
  - Claim events reduce insurer capital and can raise future premiums or restrict capacity.
  - Insurer solvency stress lowers insurance contract value to policyholders and can widen guarantee spreads.

- [ ] Keep insurance model configuration data-driven.
  - Loss frequency/severity curves.
  - Premium load factors.
  - Deductible and coverage presets.
  - Reserve factors.
  - Reinsurance attachment/exhaustion points.
  - Capital/solvency thresholds.
  - Claim event weights.
  - Credit-insurance default and recovery assumptions.

## Generic Market Microstructure

These items stay product-agnostic under `market/financial/`.

- [ ] Keep order book/liquidity behavior generic.
  - Bid/ask liquidity.
  - Depth.
  - Spread.
  - Realized volatility.
  - Momentum.
  - Mean reversion.
  - Panic/forced selling.

- [ ] Let product modules provide inputs.
  - Product modules emit signals.
  - `market/financial` combines signals and produces price movement.
  - `market/financial` must not import `entity/company` classes.

- [ ] Add debug snapshots for signal attribution.
  - Fundamental contribution.
  - Flow contribution.
  - Risk contribution.
  - Liquidity contribution.
  - Final price delta.

## Implementation Order

- [ ] Phase 1: Introduce generic `PriceSignal` and `FundamentalAnchor` under `market/financial/`.
- [ ] Phase 2: Add equity/company adapter under `product/equity/` while keeping raw company fields in `entity/company/`.
- [ ] Phase 3: Extend `CompanyFinancialReport` to carry realistic accounting metrics.
- [ ] Phase 4: Update `FinancialMarketEngine` to consume generic signals and preserve current behavior through defaults.
- [ ] Phase 5: Add simplified institution role model and deterministic institution-driven flow/risk signals.
- [ ] Phase 6: Add default `central_bank_and_securities` provider identity and service-provider context for UI/command access.
- [ ] Phase 7: Add player-created institution planning: ownership, permissions, licenses, service grants, and future block/menu terminals.
- [ ] Phase 8: Generalize index definitions, index levels, reconstitution, and rebalance rules under `market/index/`.
- [ ] Phase 9: Add index-tracking fund/ETF mechanics, including NAV, tracking error, and creation/redemption.
- [ ] Phase 10: Add component `core` attribute overlay model and component `collection` read model under `product/component/`.
- [ ] Phase 11: Formalize commodity spot pricing architecture under `product/commodity/`, including the split between commodity attributes, physical inventory state, market cycle behavior, stabilizer, storage, spoilage, and source attribution.
- [ ] Phase 12: Add commodity spot signal adapter under `product/commodity/`.
- [ ] Phase 13: Add derivative pricing inputs under `product/component/derivative/future/` and `product/component/derivative/option/`.
- [ ] Phase 14: Split the current flat derivative implementation into separate futures and options directories/services.
- [ ] Phase 15: Add fund NAV/flow signal adapter under `product/component/fund/`.
- [ ] Phase 16: Add structured credit and payoff risk signal adapters under `product/component/structured/`.
- [ ] Phase 17: Add liability product taxonomy and base liability state model under `product/liabilities/`.
- [ ] Phase 18: Add institution-issued credit assessment, credit limit, spread quote, and credit event model under `product/liabilities/credit/`.
- [ ] Phase 19: Add deposit product taxonomy, deposit state, deposit-rate model, and institution funding-flow adapter under `product/liabilities/deposit/`.
- [ ] Phase 20: Add reference-based credit derivative planning under `product/component/derivative/credit/`.
- [x] Phase 21: Rename `product/insurance/` and add insurance/credit-protection taxonomy plus reserved calling interface.
- [ ] Phase 22: Add UI/debug attribution for active provider, price movement, index rebalance flow, commodity price components, ETF premium/discount, credit assessment changes, credit spread movement, deposit rate/accrual movement, and insurance premium movement.
- [ ] Phase 23: Add focused tests for each product adapter and one integration test for the generic engine.

## Acceptance Criteria

- [ ] `market/financial` has no dependency on `entity/company`.
- [ ] Company-specific accounting fields exist only in `entity/company` or company-facing DTO/report classes.
- [ ] Equity code under `product/equity` is the only path that translates company reports into equity signals.
- [ ] Commodity, derivative, fund, structured, and liability products each own their own indicator extraction.
- [ ] `product/component/core` owns reusable component overlay attributes, while concrete product state stays in the owner package.
- [ ] Generic component collection classifies `fund`, `structured`, `future`, and `option` without owning their behavior.
- [ ] Futures and options have separate owner directories and calling documents.
- [ ] Component implementation directories, Java package declarations, and documentation agree on one final `product/component/*` layout.
- [ ] `product/liabilities` covers loans, debt securities, securitized credit, funding liabilities, and core credit-risk state without absorbing insurance or derivative behavior.
- [ ] `product/liabilities/credit` owns institution-issued ratings, credit limits, spread quotes, credit events, and underwriting decisions; entity tags must not be the source of truth for credit.
- [ ] `product/liabilities/deposit` covers deposit accounts, balances, deposit rates, accrued interest, withdrawal rules, insurance coverage, and institution funding-flow signals.
- [x] `product/insurance` covers policies, premiums, claims, reserves, guarantees, credit insurance, and reinsurance.
- [ ] Credit derivatives are reference-based and routed to `product/component/derivative/credit/`; structured credit notes and synthetic CDOs are routed to `product/component/structured/` unless implemented as bilateral derivatives.
- [ ] Tunable weights and thresholds are represented in JSON config.
- [ ] Existing share price behavior remains reproducible with default config.
- [ ] Debug output can explain why a price moved.
- [ ] New non-single-purpose behavior is implemented as focused source files in the nearest category folder, not added to already broad classes.
- [ ] New callable behavior has one documented entry point and the nearest second-level `CALLING.md` is updated in the same change.
- [ ] Index levels are calculated independently from index-tracking fund prices.
- [ ] Index-tracking products report NAV, market price, tracking error, and premium/discount.
- [ ] ETF-like creation/redemption flows affect both the fund product and the underlying basket.
- [ ] Institution role behavior is represented through explicit deterministic services or adapters, not hidden inside price formulas.
- [ ] Current market UI is documented and implemented as a terminal for the default `central_bank_and_securities` provider, not as the owner of market behavior.
- [ ] Domain calls that depend on service provider behavior can accept explicit provider context.
- [ ] Player-owned financial institutions can be represented without duplicating default-provider logic or requiring `/market` debug access.
- [ ] Missing player-provider capabilities fail explicitly instead of silently routing to the default provider.
- [ ] No LLM-Agent, prompt-driven actor, or external model call is required or allowed for market participants.
- [ ] Re-running the same saved state and config produces the same institution decisions, except where an explicit seeded pseudo-random source is part of the state.
- [ ] Commodity spot price movement can be attributed to reference price, inventory pressure, flow pressure, stabilizer pressure, storage/spoilage pressure, and clamps.
- [ ] Commodity pricing remains under `product/commodity/`; generic financial pricing consumes commodity outputs only as signals or anchors.

## Data-Driven Parameter Check

- Extracted target parameters: signal weights, thresholds, volatility/carry/time-decay settings, liquidity impact settings, and product-specific indicator weights should live in config JSON.
- Extracted target parameters: index eligibility rules, weighting rules, rebalance schedules, tracking error tolerances, creation/redemption thresholds, institution fee schedules, margin rules, and risk limits should live in config JSON.
- Extracted target parameters: commodity attributes such as inventory target policy, storage/perish/carry parameters, seasonality multipliers, substitution groups, and base price seeds should live in commodity rule data.
- Extracted target parameters: commodity market behavior such as inventory/flow/stabilizer weights, reference observation weight, source weights, shock weights, and max cycle moves should live in config JSON or market rule data.
- Extracted target parameters: component overlay defaults, derivative model selections, and product-specific attribute enablement should live in config JSON or product rule data.
- Extracted target parameters: credit rating scales, assessment methodology weights, credit spread curves, default probability curves, transition matrices, recovery assumptions, collateral haircuts, covenant thresholds, credit-event definitions, deposit rate curves, deposit beta rules, balance tiers, early-withdrawal penalties, withdrawal/run-risk parameters, insurance loss curves, premium load factors, reserve factors, and reinsurance structures should live in config JSON or product rule data.
- Kept in code: structural types, package boundaries, record field names, index calculation interfaces, deterministic state-machine transitions, and adapter interfaces.
- Follow-up: after implementation, run a pass over each product adapter to catch any new hard-coded weights or thresholds.

## Reference Anchors

- SEC Investor.gov: ETFs pool investor money into portfolios, have NAV and exchange-traded market prices, and market price may trade at a premium or discount to NAV.
- SEC Investor.gov: index funds track market indexes; funds may hold all constituents or a sample, and tracking error/fees/trading costs can cause underperformance versus the index.
- SEC Investor.gov: market participants include broker-dealers, clearing agencies, credit rating agencies, alternative trading systems, investment advisers, exchanges, SROs, and transfer agents.
- FINRA: bonds are debt securities with defined borrower/lender terms; common categories include corporate, agency, municipal, asset-backed, mortgage-backed, Treasury, savings, international/emerging-market, and bond funds.
- CFPB: consumer credit topics include auto loans, credit cards, debt collection, mortgages, payday loans, reverse mortgages, student loans, and related consumer financial products.
- NAIC: insurance types include homeowners, life and annuities, auto, health, flood, small business, long-term care, sharing-economy, cyber, and related consumer insurance products.
- FDIC: deposit insurance covers traditional deposit accounts such as checking accounts, savings accounts, money-market deposit accounts, and certificates of deposit at insured banks.
