# Generic Product Component Collection

This document defines the collection of generic financial product components in Minance.

## Scope

The generic product component collection contains product types whose value is derived from another asset, basket, contract, or managed pool rather than from a single in-world entity or physical commodity.

Current members:

| Component | Financial type | Implementation owner | Notes |
| --- | --- | --- | --- |
| `future` | `FUTURE` | target: `product/component/derivative/future/` (current: `product/component/derivative/`) | Contract over an underlying product or commodity. |
| `option` | `OPTION` | target: `product/component/derivative/option/` (current: `product/component/derivative/`) | Option contract over an underlying product or commodity. |
| `fund` | `FUND` | `product/component/fund/` | Managed pool with holdings, NAV, and secondary share price. |
| `structured` | `STRUCTURED_PRODUCT` | `product/component/structured/` | Product with payoff rules, basket exposure, and beneficiary claims. |

## Collection Interface

The code-level collection is `product/component/GenericProductComponent`.

Use it when a caller needs to answer:

- Is this `FinancialProductType` one of the generic product components?
- Which implementation package owns this component?
- Should this product be handled as a wrapped, managed, or derivative component rather than as a primary entity/commodity/equity product?

## Target Layout

`product/component/` is the root for generic product component concepts.

- `product/component/core/`: shared attribute overlay model for reusable component descriptors.
- `product/component/collection/`: component membership and indexing read model. It may replace or absorb the current flat `GenericProductComponent` enum once implemented.

Concrete implementation owners remain separate:

- `product/component/fund/`: fund state, NAV, tracking, subscriptions, redemptions, and share pricing.
- `product/component/structured/`: structured payoff state, claims, basket exposure, barriers, floors, and caps.
- `product/component/derivative/future/`: futures contracts, future market state, maturity, delivery, and futures-specific pricing inputs.
- `product/component/derivative/option/`: option contracts, option market state, rights, exercise, and option-specific pricing inputs.

## Attribute Overlay Model

`product/component/core/` should answer: what extra financial attributes are layered onto this product?

Planned core types:

- `ComponentAttribute`
- `ComponentAttributeSet`
- `ComponentOverlay`
- `ComponentOverlayResolver`

Overlay attributes should be typed and composable. Expected attributes include:

- `UNDERLYING_EXPOSURE`
- `MATURITY`
- `SETTLEMENT`
- `PAYOFF_SHAPE`
- `BASKET_EXPOSURE`
- `NAV_ANCHOR`
- `LEVERAGE`
- `CLAIM_PRIORITY`
- `LIQUIDITY_WRAPPER`
- `TRACKING_TARGET`

The overlay layer is metadata for adapters, market signals, and UI classification. It must not own concrete product state, valuation formulas, persistence, or settlement behavior.

Credit ratings, credit limits, spread quotes, and underwriting decisions are not component overlays. They are institution-issued assessment records owned by `product/liabilities/credit/` and may be referenced by products through explicit assessment ids.

## Ownership

`product/component/` is only a component metadata, collection, and index layer.

- It does not own state.
- It does not price products.
- It does not persist products.
- It does not render products.
- It must not replace the concrete implementation packages.

Concrete behavior stays in the owner package:

- `product/component/derivative/future/`: futures.
- `product/component/derivative/option/`: options.
- `product/component/derivative/core/`: only primitives genuinely shared by both futures and options.
- `product/component/fund/`: funds and NAV/share pricing.
- `product/component/structured/`: structured payoff products.

## Relationship To Market

The collection does not make these products market-owned. Product packages still compute product-specific anchors and state. `market/financial/` may consume resolved overlays as generic signal or anchor metadata, then produce market prices and liquidity effects without importing concrete product internals.

## Boundary Rule

If a future generic component is added, add it to all of these places in the same change:

- `FinancialProductType`
- `GenericProductComponent`
- `product/component/core/` overlay support when the component introduces reusable attributes
- `product/component/collection/` when the collection read model exists
- the implementation owner package
- the owner package `CALLING.md`
- this document

If future or option behavior changes, update the target split documents for `product/component/derivative/future/` or `product/component/derivative/option/` and keep shared derivative primitives in `product/component/derivative/core/` only when both sides use them.
