# entity/wandering_merchant Calling Guide

## Scope

Reserved for wandering-merchant economic identity and future merchant-origin supply/demand events.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Wandering merchant economic events | Status: reserved | Future entity event adapters | To be defined with the first implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future implementations must document merchant entity state, offers, config, and saved-state inputs.

## Outputs

Future implementations must document emitted spot supply/demand, snapshots, and persistence.

## Ownership Rules

The first implementation must expose a merchant-specific service or event adapter before adding helper classes.

## Forbidden Bypass Calls

Do not record merchant-origin spot flow directly from unrelated packages without a documented entry point here.

