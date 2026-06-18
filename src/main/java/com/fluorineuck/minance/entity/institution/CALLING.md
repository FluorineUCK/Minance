# entity/institution Calling Guide

## Scope

Financial institution identity, deterministic role grants, and institution-origin signal requests. This package identifies who provides a financial service; it does not price products, settle markets, grant credit, or render UI.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Build institution-origin price signals | `FinancialInstitutionSignalService.INSTANCE.priceSignals(...)` | Future provider-aware product services and institution orchestration | Returns immutable `PriceSignalBundle`; no market mutation |
| Read institution profile roles | `FinancialInstitutionProfile.hasRole(...)` | Product services, commands, UI, future permission checks | Read-only role check |

## Lifecycle

Institution profiles are deterministic inputs to product and service-provider orchestration. Signal construction runs when a product or provider service has explicit institution flow, liquidity, or risk inputs.

## Inputs

Institution profile, role set, product id, product type, explicit flow/risk/liquidity inputs, optional anchor price, horizon ticks, and `finance.institution_signal` config.

## Outputs

Generic `PriceSignalBundle` instances that can be passed to `market/financial` through its signal-aware update API.

## Ownership Rules

Institution behavior must be deterministic from explicit state, config, request inputs, and formulas. Player-owned and default institutions must eventually use this same role/profile surface instead of duplicating UI logic.

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
