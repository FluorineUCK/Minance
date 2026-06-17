# product/commodity Calling Guide

## Scope

Commodity product domain. `core/` owns commodity identity, attributes, and physical inventory state. `spot/` owns commodity market behavior, supply/demand events, settlement, price history, and source attribution.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Ensure spot asset | `spot/SpotMarketService.INSTANCE.ensureAsset(...)` | Product adapters, company derivative setup, saved-data restore through service | Creates missing spot asset |
| Record spot supply | `spot/SpotMarketService.INSTANCE.recordSupply(...)` | Villager discovery and future documented product/entity adapters | Mutates spot asset flow, inventory, reference price, and stabilizer flow |
| Record spot demand | `spot/SpotMarketService.INSTANCE.recordDemand(...)` | Villager discovery and future documented product/entity adapters | Mutates spot asset flow, inventory, reference price, and stabilizer flow |
| Settle spot prices | `spot/SpotMarketService.INSTANCE.updatePrices()` | Settlement tick orchestration | Updates spot prices and resets cycle counters |
| Restore spot assets | `spot/SpotMarketService.INSTANCE.replaceAssets(...)` | `data/MinanceSavedData` | Replaces current spot asset map |

## Lifecycle

Supply/demand is recorded from discovery/event adapters. Settlement runs on market settlement cadence. Save/load is owned by `data/MinanceSavedData`.

## Inputs

Item id, source type, source id, quantity, observed price, market config, commodity config, and saved spot state.

## Outputs

Spot assets, spot rows, price history, inventory movement, flow breakdowns, and settled spot prices.

## Ownership Rules

External callers use `SpotMarketService` for spot behavior. `core/CommodityState` is state owned by spot assets and should not be mutated from unrelated packages. `CommodityStabilizationDesk` is a spot-behavior refactor target and should not become an external entry point.

## Forbidden Bypass Calls

Do not write directly into `SpotMarketAsset` internals or commodity cycle counters from outside `product/commodity/spot/`. Do not store market flow behavior in commodity attributes.

