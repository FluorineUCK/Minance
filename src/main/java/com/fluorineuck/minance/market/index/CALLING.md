# market/index Calling Guide

## Scope

Index read models, index levels, configured index definitions, and spot-market driven index updates.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Update indexes from spot market | `MarketIndexService.INSTANCE.updateFromSpotMarket()` | UI refresh paths when explicitly allowed | Updates index levels from current spot assets |
| Update indexes with cadence | `MarketIndexService.INSTANCE.updateFromSpotMarket(gameTime)` | Settlement/index server tick paths | Applies configured reconstitution and rebalance cadence |
| Calculate index level | `MarketIndexLevelCalculator.INSTANCE.calculate(...)` | `MarketIndexService`, focused tests | Pure calculation from components and config |
| Read indexes | `MarketIndexService.INSTANCE.indices()` / `sortedIndices()` | UI, commands, fund service | Read view of index state |
| Save/load indexes | `MarketIndexService.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces index state |

## Lifecycle

Runs during settlement or index update cadence, during manual UI refresh, and during saved-data load/save.

## Inputs

Configured index definitions, current spot market assets, market config, optional game time, and saved index state. Per-index config controls `weighting_method`, `reconstitution_interval_ticks`, and `rebalance_interval_ticks`.

## Outputs

Updated `MarketIndexState` values, remembered component IDs, reconstitution/rebalance ticks, and saved NBT state.

## Ownership Rules

This package calculates index levels and owns product-agnostic index composition/rebalance mechanics. Investable index-tracking products belong in `product/component/fund/` or a future dedicated product package.

## Forbidden Bypass Calls

Do not make spot pricing depend on index products except through explicit product demand adapters.

