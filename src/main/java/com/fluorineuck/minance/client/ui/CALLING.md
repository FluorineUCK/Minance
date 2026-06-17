# client/ui Calling Guide

## Scope

Client-only market dashboard screens, LDLib2 widgets, chart rendering, selection state, and visual debug presentation.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Open market dashboard | `MarketLdLibDashboard.open()` | `client/MinanceClientEvents` and client-only UI adapters | Opens or reuses the Minance screen |

## Lifecycle

Called from client key handling or other client-only UI actions. UI elements render snapshots and must not mutate authoritative server state.

## Inputs

Client context, local UI state, and read-only market snapshots exposed by services.

## Outputs

Rendered dashboard, charts, lists, product detail panels, and local selection state.

## Ownership Rules

External callers should open the dashboard through `MarketLdLibDashboard.open()`. Individual element classes under `client/ui/elements/` are implementation details for the dashboard.

## Forbidden Bypass Calls

Do not call UI elements from server code. Do not place pricing, settlement, persistence, or authoritative state mutation in this package.

