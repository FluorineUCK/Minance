# entity/player Calling Guide

## Scope

Reserved for player economic identity, player-origin intents, holdings, and future player-facing actor state.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Player economic actor state | Status: reserved | Future player command, UI, or interaction adapters | To be defined with the first implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future implementations must document player id, world context, config, and saved-state inputs.

## Outputs

Future implementations must document player state changes, emitted orders, snapshots, and persistence.

## Ownership Rules

The first feature added here must define a `*Service` or explicit adapter entry point before adding helper files.

## Forbidden Bypass Calls

Do not route player-origin orders directly into product or market internals without a documented player-facing entry point.

