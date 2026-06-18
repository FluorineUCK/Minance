# command Calling Guide

## Scope

Thin `/market` debug and admin command adapters. Commands may load saved data, display snapshots, and call documented domain services, but they do not own market behavior.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Register commands | `MinanceCommands.register(...)` | `Minance` mod bootstrap | Registers Brigadier command tree |
| Resolve command provider context | `FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(FinancialServiceAccessPoint.COMMAND)` | `/market` command handlers | Immutable provider context |
| Show active provider | `/market provider` | Operators with `/market` permission | Displays default provider identity, access point, and roles |

## Lifecycle

Commands are registered during mod event bootstrap. Command handlers run only from Minecraft command execution and should delegate behavior to domain services.

## Inputs

Command source, command arguments, saved-data access, and the explicit default `central_bank_and_securities` provider context.

## Outputs

Command feedback messages and, for existing admin commands, documented service calls such as fund creation or derivative expansion.

## Ownership Rules

Commands are adapters. If behavior depends on who provides a service, commands should create or receive a `FinancialServiceProviderContext` and pass it to the domain service once that service supports provider-aware calls.

## Forbidden Bypass Calls

Do not add pricing formulas, credit decisions, settlement logic, or product lifecycle rules directly to command handlers. Do not silently switch from a missing player-owned provider to the default provider.
