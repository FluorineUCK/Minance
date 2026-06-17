# Calling Guide Template

Use this template for every second-level package `CALLING.md`.

## Scope

State what this directory owns and which behavior belongs somewhere else.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Feature name | `ClassOrMethod` | Packages or systems allowed to call it | State changes, IO, sync, UI, or none |

## Lifecycle

Describe when the entry point is called: event hook, server tick, settlement tick, reload, save/load, UI open, command call, or explicit user action.

## Inputs

List accepted inputs, config/rule dependencies, saved-state dependencies, and validation assumptions.

## Outputs

List returned values, snapshots, emitted signals/events, changed state, and persisted data.

## Ownership Rules

Define which classes are public entry points, which are local helpers, and which state must not be mutated directly by callers.

## Forbidden Bypass Calls

List helper classes, mutable maps, DTOs, UI elements, or persistence structures that external callers must not use directly.

## Change Checklist

- Add or update the unified entry point.
- Keep helpers in the nearest domain folder.
- Update this document in the same change.
- Preserve deterministic behavior from saved state plus config/rules.
