# market/event Calling Guide

## Scope

Reserved for normalized market events and future routing between entity/product inputs and market services.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Market event routing | Status: reserved | Future entity/product adapters | To be defined with the first implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future implementations must document event payloads, source domains, config, and deterministic ordering rules.

## Outputs

Future implementations must document emitted signals, product calls, market calls, and persistence.

## Ownership Rules

This package may normalize and route events, but must not own product-specific state or pricing formulas.

## Forbidden Bypass Calls

Do not use `market/event/` as a generic utility package or as a hidden actor system.

