# product/component/derivative Calling Guide

## Scope

Commodity futures/options, derivative market state, contract lifecycle, delivery method, sides, rights, maturity, and derivative-specific pricing inputs.

Futures and options are members of the generic financial product component collection documented by `product/component/GenericProductComponent`.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Ensure derivative set for commodity | `CommodityDerivativeService.INSTANCE.ensureDerivativeSet(...)` | UI, company setup, product adapters | Creates futures/options markets for a commodity |
| Update derivatives from spot | `CommodityDerivativeService.INSTANCE.updateFromSpot(...)` | Commodity/company/product update paths | Updates derivative prices from underlying spot state |
| Expand derivative graph | `CommodityDerivativeService.INSTANCE.ensureDerivativeSetForDerivative(...)` | Commands and UI adapters | Creates derivative markets from an existing derivative |
| Save/load derivatives | `CommodityDerivativeService.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces derivative state |

## Lifecycle

Called when spot markets are discovered, companies are registered, UI/commands request derivative expansion, and during saved-data load/save.

## Inputs

Underlying commodity or product id, spot price, game time, derivative config, finance config, and saved derivative state.

## Outputs

Futures markets, option markets, contract states, market prices, and saved NBT state.

## Ownership Rules

Derivative contracts and derivative market states are owned here. Generic price movement goes through `market/financial/`.

`product/component/` may classify futures and options as collection members, but it must not own derivative state or pricing.

## Forbidden Bypass Calls

Do not mutate derivative maps directly from UI, commands, or funds. Do not make derivative pricing import company accounting fields directly.
