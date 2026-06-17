# product/commodity/core Calling Guide

## Scope

Commodity identity, commodity attributes/profile state, physical inventory state, and shared commodity-domain primitives used by sibling commodity packages.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Commodity physical state | `CommodityState` through owning `spot/SpotMarketAsset` | `product/commodity/spot` | Mutates commodity inventory, reference price, and cycle counters when called by spot ownership layer |
| Commodity stabilizer behavior | Status: refactor target | Currently called by `spot/SpotMarketService`; should move to `spot/` if it remains market behavior | May add counter-flow through spot asset methods |

## Lifecycle

Commodity state is created with a spot asset, restored from saved data, updated by spot supply/demand recording, and reset after spot settlement.

## Inputs

Item id, initial price, commodity config, supply/demand quantities, and saved commodity state.

## Outputs

Commodity inventory, target inventory, reference price, production/consumption counters, inflow/outflow counters, and saved state.

## Ownership Rules

`core/` should expose shared commodity primitives only to sibling commodity packages. External callers should enter commodity spot behavior through `product/commodity/spot/SpotMarketService`.

## Forbidden Bypass Calls

Do not store spot-market order flow, price history, stabilizer flow, or source attribution as commodity attributes. Do not add feature-specific spot behavior here unless it is split into reusable commodity-neutral primitives.

