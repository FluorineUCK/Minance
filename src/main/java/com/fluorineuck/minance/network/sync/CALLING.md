# network/sync Calling Guide

## Scope

Reserved for client/server synchronization services and snapshot transfer.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Market snapshot sync | Status: reserved | Future server lifecycle, packet handlers, and client adapters | To be defined with the first sync implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future sync services must document source state, snapshot shape, recipients, cadence, and packet dependencies.

## Outputs

Future sync services must document client snapshots, packet sends, and local cache updates.

## Ownership Rules

Sync code transports snapshots. It must not own the authoritative state being synchronized.

## Forbidden Bypass Calls

Do not put pricing, settlement, accounting, or UI layout logic in `network/sync/`.

