# product/structured Calling Guide

## Scope

Structured product state, beneficiary claims, payoff rules, basket exposure, barrier/floor/cap state, and issuer/beneficiary risk adapters.

Structured products are members of the generic financial product component collection documented by `product/component/GenericProductComponent`.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Create structured product | `StructuredProductService.INSTANCE.create(...)` | Commands and future product adapters | Creates structured product state |
| Read structured products | `StructuredProductService.INSTANCE.products()` / `sortedProducts()` | UI, commands, product adapters | Read view of structured products |
| Save/load structured products | `StructuredProductService.INSTANCE.save()` / `load(...)` | `data/MinanceSavedData` | Serializes or replaces structured product state |

## Lifecycle

Called by future commands/UI/product adapters and during saved-data load/save.

## Inputs

Underlying basket, payoff parameters, beneficiary claim, issuer risk, market prices, config, and saved state.

## Outputs

Structured product states, beneficiary claims, payoff estimates, risk signals, and saved NBT state.

## Ownership Rules

Structured payoff state belongs here. Generic pricing uses `market/financial/`; underlying prices are read from their product packages.

`product/component/` may classify structured products as collection members, but it must not own structured payoff state or valuation logic.

## Forbidden Bypass Calls

Do not put structured payoff formulas in generic market code or UI elements.
