# product/component Calling Guide

## Scope

Generic financial product component collection. This package defines the shared collection that includes funds, structured products, futures, and options.

It is an index and classification layer only. It does not own fund state, structured payoff state, futures/options contracts, pricing formulas, persistence, or UI rendering.

Target layout:

- `product/component/core/`: reusable component attribute overlays.
- `product/component/collection/`: component membership and indexing read model.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Check generic component membership | `GenericProductComponent.contains(...)` | Product adapters, market adapters, UI, commands | None |
| Map from financial product type | `GenericProductComponent.fromFinancialProductType(...)` | Product adapters, market adapters, UI, commands | None |
| List financial product types in the collection | `GenericProductComponent.financialProductTypes()` | Product adapters, market adapters, UI, commands | None |

Planned entry points:

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Resolve composable component attributes | `ComponentOverlayResolver.resolve(...)` | Product adapters, market adapters, UI, commands | None |
| Describe attributes layered onto a product | `ComponentOverlay` / `ComponentAttributeSet` | Product owners and read adapters | None |

## Lifecycle

Pure classification. It can be called whenever code needs to decide whether a product is one of the generic financial product components.

## Inputs

`FinancialProductType`.

## Outputs

`GenericProductComponent`, collection membership, owner package metadata, or an empty result.

## Ownership Rules

`product/component/` owns component metadata and collection membership only.

- Reusable overlay attributes belong in `product/component/core/`.
- Component membership/indexing belongs in `product/component/collection/` once the collection package is created.
- Future implementation should move to `product/component/derivative/future/`.
- Option implementation should move to `product/component/derivative/option/`.
- Shared derivative primitives belong in `product/component/derivative/core/` only if both future and option implementations use them.
- Fund implementation remains in `product/component/fund/`.
- Structured product implementation remains in `product/component/structured/`.
- Generic market pricing remains in `market/financial/`.

## Forbidden Bypass Calls

Do not move component-specific state or valuation logic into this package. Do not put fund NAV calculation, structured payoff calculation, futures settlement, option exercise logic, persistence, or UI rendering here. Do not put credit ratings, credit limits, spread quotes, or underwriting decisions here; those belong to `product/liabilities/credit/`. Do not use this package as a generic dumping ground for product utilities.
