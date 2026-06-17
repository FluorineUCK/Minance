# product/equity Calling Guide

## Scope

Equity assets, equity asset types, tradable company-share synchronization, and adapters that convert company reports into equity pricing inputs.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Sync company equity | `EquityMarketService.INSTANCE.syncCompany(...)` | `entity/company` company registration/update paths | Creates or updates equity asset state |
| Sync multiple companies | `EquityMarketService.INSTANCE.syncCompanies(...)` | Saved-data restore or company batch update paths | Rebuilds equity asset state for provided companies |
| Read equity assets | `EquityMarketService.INSTANCE.assets()` / `sortedAssets()` | UI, commands, funds | Read view of equity assets |
| Build equity price signals | `EquityMarketService.INSTANCE.priceSignals(...)` | Product services and future market-signal orchestration | Returns immutable `PriceSignalBundle`; no market mutation |

## Lifecycle

Called when companies are registered or updated, when funds need holdings, and when company batches need equity synchronization.

## Inputs

Company identity and report fields, equity config, current market price, saved equity state, and finance `equity_signal` config.

## Outputs

Equity assets, equity prices, tradability flags, NAV anchors, and generic equity earnings signals.

## Ownership Rules

This package is the only path that translates company facts into equity product state or equity market signals.

Company accounting data may be read here as an equity input, but callers must receive generic `PriceSignalBundle` output before handing data to `market/financial`.

## Forbidden Bypass Calls

Do not put raw company accounting fields inside `market/financial/`. Do not let funds or UI mutate equity assets directly.
