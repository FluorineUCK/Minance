# product/commodity/spot Calling Guide

## Scope

Commodity spot-market behavior: asset creation, supply/demand events, order flow, inflow/outflow, observed reference price, stabilizer activity, settlement, price history, rows, and source attribution.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Ensure spot asset | `SpotMarketService.INSTANCE.ensureAsset(...)` | Company derivative setup, saved-data restore, documented product/entity adapters | Creates missing spot asset |
| Record spot supply | `SpotMarketService.INSTANCE.recordSupply(...)` | Villager discovery and future documented adapters | Mutates supply flow, commodity inventory, reference price, and stabilizer counter-flow |
| Record spot demand | `SpotMarketService.INSTANCE.recordDemand(...)` | Villager discovery and future documented adapters | Mutates demand flow, commodity inventory, reference price, and stabilizer counter-flow |
| Settle spot prices | `SpotMarketService.INSTANCE.updatePrices()` | Settlement tick orchestration | Updates price, history, and cycle snapshots |
| Read spot rows | `SpotMarketService.INSTANCE.rows()` | UI, commands, index service, product adapters | Read view of spot assets |
| Restore spot assets | `SpotMarketService.INSTANCE.replaceAssets(...)` | `data/MinanceSavedData` | Replaces spot asset map |

## Lifecycle

Supply/demand is recorded from discovery or event adapters. Settlement runs on `market.settlement_interval_ticks`. Indexes and funds consume settled/current spot state after settlement.

## Inputs

Item id, source type, source id, quantity, observed price, market config, commodity config, and saved spot state.

## Outputs

Spot assets, spot rows, price history, inventory changes, flow snapshots, supply/demand breakdowns, and saved spot state.

## Ownership Rules

External callers use `SpotMarketService` only. `SpotMarketAsset`, `SpotMarketEngine`, `SpotMarketSource`, and `SpotMarketRow` are internal models/read models unless a method is explicitly documented here.

## Forbidden Bypass Calls

Do not mutate `SpotMarketAsset` internals from outside `product/commodity/spot/`. Do not bypass `recordSupply` or `recordDemand` when creating market flow. Do not move commodity attributes into spot cycle state.

