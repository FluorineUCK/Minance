# product/component/core Calling Guide

## Scope

Reusable component attribute overlays for generic financial product components. This package describes metadata layered onto products; it does not own product state or valuation.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Resolve component overlay | `ComponentOverlayResolver.INSTANCE.resolve(...)` | Product adapters, market adapters, UI, commands | None |
| Read component attributes | `ComponentOverlay`, `ComponentAttributeSet` | Product adapters, market adapters, UI, commands | None |

## Lifecycle

Called whenever code needs product-neutral component metadata for classification, signal attribution, or UI/debug display.

## Inputs

Product id and `FinancialProductType`.

## Outputs

Immutable `ComponentOverlay` and `ComponentAttributeSet` values.

## Ownership Rules

This package owns only reusable overlay metadata such as underlying exposure, maturity, basket exposure, NAV anchors, leverage, and tracking targets. Concrete product packages own state, valuation, persistence, and settlement.

## Forbidden Bypass Calls

Do not put fund holdings, structured payoff state, futures settlement, option exercise, credit assessments, pricing formulas, persistence, or UI rendering here.
