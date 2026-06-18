# entity/institution Calling Guide

## Scope

Financial institution identity, deterministic role grants, and institution-origin signal requests. This package identifies who provides a financial service; it does not price products, settle markets, grant credit, or render UI.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Resolve default public provider | `FinancialInstitutionDirectory.INSTANCE.defaultPublicProvider()` | UI, commands, product service orchestration | Read-only default provider identity |
| Build service-provider context | `FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(...)` / `FinancialServiceProviderContext` | UI, commands, future block/menu terminals and provider-aware services | Immutable context object |
| Plan player-owned institution | `PlayerInstitutionPlanningService.INSTANCE.plan(...)` | Future player interaction, block/menu, command, or ownership adapters | Immutable planning model; no persistence |
| Convert player plan to provider context | `PlayerInstitutionPlanningService.INSTANCE.providerContext(...)` | Future block/menu terminals and provider-aware services | Immutable context object |
| Check terminal permission | `PlayerInstitutionPlanningService.INSTANCE.canOpenTerminal(...)` | Future block/menu terminals | Read-only permission check |
| Build institution-origin price signals | `FinancialInstitutionSignalService.INSTANCE.priceSignals(...)` | Future provider-aware product services and institution orchestration | Returns immutable `PriceSignalBundle`; no market mutation |
| Read institution profile roles | `FinancialInstitutionProfile.hasRole(...)` / `FinancialServiceProviderContext.hasRole(...)` | Product services, commands, UI, future permission checks | Read-only role check |

## Lifecycle

Institution profiles are deterministic inputs to UI terminals, command adapters, product services, and service-provider orchestration. Player institution planning runs when future gameplay code wants to prepare a provider identity before persistence or block/menu binding. Signal construction runs when a product or provider service has explicit institution flow, liquidity, or risk inputs.

## Inputs

Institution profile, role set, service access point, player ownership, operator permissions, service grants, licenses, product id, product type, explicit flow/risk/liquidity inputs, optional anchor price, horizon ticks, and `finance.institution_signal` config.

## Outputs

Default provider identity, player institution plans, immutable service-provider contexts, terminal permission decisions, and generic `PriceSignalBundle` instances that can be passed to `market/financial` through its signal-aware update API.

## Ownership Rules

Institution behavior must be deterministic from explicit state, config, request inputs, and formulas. Player-owned and default institutions must eventually use this same role/profile surface instead of duplicating UI logic.

The default `central_bank_and_securities` profile is the current public service provider for UI and command access. `MarketLdLibDashboard.open()` and `/market` commands use explicit `FinancialServiceProviderContext` values so future block/menu terminals and player-owned providers can route through the same domain surface without copying market logic.

`PlayerInstitutionPlanningService` is not a registry and does not persist institutions. It creates a replayable planning object that names ownership, operator grants, service grants, and licenses. Future persistence, capital, inventory, and block/menu binding must add explicit owner services instead of mutating these records directly from UI code.

`InstitutionSignalRequest` carries observed or computed institution state. `finance.institution_signal` controls signal confidence and strength caps:

| Config key | Purpose |
| --- | --- |
| `liquidity_signal_confidence` | Confidence attached to liquidity-provider bid/ask pressure |
| `client_flow_signal_confidence` | Confidence attached to asset-manager client-flow pressure |
| `risk_signal_confidence` | Confidence attached to credit-underwriter risk discount pressure |
| `anchor_confidence` | Confidence attached to institution-origin anchors |
| `max_client_flow_strength` | Cap for request client-flow strength before market consumption |
| `max_risk_discount_strength` | Cap for request risk-discount strength before market consumption |

## Forbidden Bypass Calls

Do not implement institutions as LLM agents, prompt-driven actors, natural-language planners, or external model calls. Do not mutate market state directly from `entity/institution`; emit generic signals and let `market/financial` consume them.
