# product/component/collection Calling Guide

## Scope

Read-model index for generic financial product component membership. This package classifies existing component families; it does not own concrete product behavior.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| List component entries | `ComponentCollectionIndex.INSTANCE.entries()` | Product adapters, market adapters, UI, commands | None |
| Resolve component entry | `ComponentCollectionIndex.INSTANCE.entry(...)` | Product adapters, market adapters, UI, commands | None |
| Check component membership | `ComponentCollectionIndex.INSTANCE.contains(...)` | Product adapters, market adapters, UI, commands | None |

## Lifecycle

Pure read model. It can be called whenever code needs to classify a `FinancialProductType` as a generic component family.

## Inputs

`FinancialProductType`.

## Outputs

Immutable `ComponentCollectionEntry` values with owner package and default overlay attributes.

## Ownership Rules

This package owns only component membership metadata. Concrete behavior remains in `product/component/fund/`, `product/component/structured/`, and derivative owner packages.

## Forbidden Bypass Calls

Do not add product state, valuation formulas, payoff rules, settlement, persistence, or UI rendering here.
