# product/component/fund Calling Guide

## Scope

Fund state, holdings, NAV, fund share pricing, subscriptions/redemptions, index tracking, fees, and tracking error.

Funds are members of the generic financial product component collection documented by `product/component/GenericProductComponent`.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Create fund | `FundService.INSTANCE.createFund(...)` | Commands, future UI/product adapters | Creates fund state and initial price |
| Create index-tracking fund | `FundService.INSTANCE.createIndexTrackingFund(...)` | Commands and future asset-manager logic | Creates fund state and index basket holdings |
| Update all funds | `FundService.INSTANCE.updateAllFunds()` | Settlement tick orchestration | Refreshes holdings, NAV, and fund share prices |
| Read tracking metrics | `FundService.INSTANCE.trackingMetrics(...)` | UI, commands, future fund adapters | Returns NAV/share, tracking error, premium/discount, and creation/redemption action |
| Read funds | `FundService.INSTANCE.funds()` / `sortedFunds()` | UI, commands, product adapters | Read view of funds |
| Save/load funds | `FundService.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces fund state |

## Lifecycle

Called by commands/UI for creation, by settlement tick for updates, and by saved-data load/save.

## Inputs

Fund id, manager, strategy tag, cash, shares, tracked index id, index component ids, current product prices, finance config, and saved fund state.

## Outputs

Fund states, holdings, NAV, share prices, tracking error, premium/discount, creation/redemption action, tracking product exposure, and saved NBT state.

## Ownership Rules

Fund valuation and holdings live here. Generic price movement goes through `market/financial/`; index levels are read from `market/index/`.

Index-tracking funds must seed and value their basket from `MarketIndexState.componentIds()`. Index level calculation remains under `market/index/`. ETF-like creation/redemption currently emits an explicit `FundCreationRedemptionAction`; later settlement or inventory effects must be added through documented fund service methods, not UI mutation.

`product/component/` may classify funds as collection members, but it must not own fund state, NAV, holdings, or fund share pricing.

## Forbidden Bypass Calls

Do not mutate `FundState` holdings directly from UI or commands without a documented `FundService` method.
