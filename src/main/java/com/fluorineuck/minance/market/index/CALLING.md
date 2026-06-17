# market/index Calling Guide

## Scope

Index read models, index levels, configured index definitions, and spot-market driven index updates.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Update indexes from spot market | `MarketIndexService.INSTANCE.updateFromSpotMarket()` | Settlement tick, UI refresh paths when explicitly allowed | Updates index levels from current spot assets |
| Read indexes | `MarketIndexService.INSTANCE.indices()` / `sortedIndices()` | UI, commands, fund service | Read view of index state |
| Save/load indexes | `MarketIndexService.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces index state |

## Lifecycle

Runs during settlement or index update cadence, and during saved-data load/save.

## Inputs

Configured index definitions, current spot market assets, market config, and saved index state.

## Outputs

Updated `MarketIndexState` values and saved NBT state.

## Ownership Rules

This package calculates index levels. Investable index-tracking products belong in `product/fund/` or a future `product/index/`.

## Forbidden Bypass Calls

Do not make spot pricing depend on index products except through explicit product demand adapters.

