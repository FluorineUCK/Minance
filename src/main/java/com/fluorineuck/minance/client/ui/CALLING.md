# client/ui Calling Guide

## Scope

Client-only market dashboard screens, LDLib2 widgets, chart rendering, selection state, active service-provider context display, and visual debug presentation.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Open default market dashboard | `MarketLdLibDashboard.open()` | `client/MinanceClientEvents` and client-only UI adapters | Opens or reuses the Minance screen for the default provider |
| Open provider-bound market dashboard | `MarketLdLibDashboard.open(FinancialServiceProviderContext)` | Future block/menu/client UI adapters | Opens or reuses the Minance screen for an explicit provider context |

## Lifecycle

Called from client key handling or other client-only UI actions. UI elements render snapshots and must not mutate authoritative server state. Current keyboard access defaults to `central_bank_and_securities`.

## Inputs

Client context, local UI state, optional `FinancialServiceProviderContext`, and read-only market snapshots exposed by services.

## Outputs

Rendered dashboard, active provider title, charts, lists, product detail panels, and local selection state.

## Ownership Rules

External callers should open the dashboard through `MarketLdLibDashboard.open()`. Provider-aware callers should use the context overload instead of introducing a parallel UI path. Individual element classes under `client/ui/elements/` are implementation details for the dashboard.

## Forbidden Bypass Calls

Do not call UI elements from server code. Do not place pricing, settlement, persistence, or authoritative state mutation in this package.

