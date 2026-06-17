# network/packet Calling Guide

## Scope

Reserved for packet definitions and packet payload DTOs.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Packet definitions | Status: reserved | Future network registration and sync services | To be defined with the first packet implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future packets must document payload fields, validation, sender side, receiver side, and versioning assumptions.

## Outputs

Future packets must document decoded payloads and dispatch target.

## Ownership Rules

Packets should carry snapshots or commands only. Domain decisions belong outside `network/packet/`.

## Forbidden Bypass Calls

Do not mutate authoritative market, entity, or product state directly from packet DTOs.

