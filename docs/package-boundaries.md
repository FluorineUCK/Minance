# Minance Package Boundaries

This document defines the functional scope of the first-level packages under `src/main/java/com/fluorineuck/minance/` and the matching data-resource directories under `src/main/resources/data/minance/`.

## Top-Level Package Scope

| Package | Owns | Must Not Own |
| --- | --- | --- |
| `api/` | Stable extension-facing interfaces, DTOs, service contracts, and integration hooks. It is currently a reserved boundary for future public APIs. | Internal implementation state, pricing formulas, persistence details, UI code, or direct world mutation. |
| `client/` | Client-only entrypoints, LDLib2 screens, widgets, charts, formatting, selection state, active service-provider display, and visual debug panels. The current market panel is a terminal for the default `central_bank_and_securities` provider. | Authoritative simulation state, market settlement, product valuation, saved-data mutation, service-provider permissions, or server-only services. |
| `command/` | `/market` debug/admin commands and thin command-to-service orchestration. | Business rules, pricing formulas, data loading, persistence schemas, or reusable domain services. |
| `config/` | Typed Java config records/classes, config registry, default values, validation shape, and accessors for `data/minance/config/*.json`. | Datapack content rules, mutable world state, entity state, market state, or hard-coded product behavior. |
| `data/` | Data lifecycle plumbing: reload listeners, codec loading, config/rule binding, and world `SavedData` persistence boundaries. | Domain decisions, market formulas, product payoff logic, or UI presentation. |
| `entity/` | In-world economic actors and facts: villages, villagers, companies, players, merchants, financial institution identities/roles, ownership/accounting facts, and raw operating records. | Generic market microstructure, product payoff rules, index calculation, or cross-product pricing engines. |
| `market/` | Product-agnostic market mechanisms: price signals, liquidity surface, generic order-flow effects, indexes, market events, and market-wide debug attribution. | Company-only accounting fields, commodity physical attributes, product-specific payoff/NAV logic, or client rendering. |
| `network/` | Packet definitions, sync messages, client/server transport, and snapshot transfer between authoritative server state and client views. | Authoritative market state, pricing rules, entity accounting, or UI layout logic. |
| `product/` | Tradable or valuated product domains: commodity, equity, generic component families under `component/` (fund, structured, futures, options), liability, and future insurance products. Each product owns its lifecycle, product state, valuation adapter, and product-specific signals. | Raw entity ownership/accounting as source of truth, generic market microstructure, data loading infrastructure, or client-only rendering. |
| `rule/` | Declarative rule schemas and registries for datapack-driven business content, such as profession trade categories and company names. | Runtime mutable state, reload IO, saved-data persistence, market settlement, or hidden hard-coded formulas. |
| `Minance.java` | Mod bootstrap, event registration, and wiring. | Domain behavior beyond startup registration and lifecycle hooks. |

## Second-Level Directory Convention

Second-level directories are domain slices under their first-level owner. A second-level package name should describe the actor, product family, market mechanism, adapter surface, or lifecycle role it owns.

- `core/` has a special meaning: it is shared foundation for the directories that sit next to it under the same parent package.
  - Example: `product/commodity/core/` is shared only by sibling commodity directories such as `product/commodity/spot/` and future commodity-specific siblings.
  - It is not a mod-wide common layer, not a dumping ground for utilities, and not a place for behavior that belongs to one sibling.
  - Sibling packages may depend on their local `core/`.
  - `core/` must not depend on sibling packages.
  - Create `core/` only when at least two sibling directories need the same domain types or pure shared logic. If only one sibling uses a type, keep it inside that sibling.
- Actor directories under `entity/` should be named by in-world actor or aggregate, such as `company/`, `village/`, `villager/`, `player/`, `wandering_merchant/`, and future `institution/`.
- Mechanism directories under `market/` should be named by market-wide function, such as `financial/`, `index/`, and `event/`.
- Product directories under `product/` should be named by product family or product collection role, such as `commodity/`, `component/`, `equity/`, `liabilities/`, and future `insurance/`. Generic financial component families such as fund, structured, futures, and options should live under `product/component/`.
- Adapter directories under `client/`, `network/`, and `command/` should be named by transport or presentation role, such as `ui/`, `packet/`, and `sync/`.
- Resource directories under `data/minance/` keep the same separation: `config/` for numeric tuning and `minance_rules/` for declarative gameplay/economy content.
- Avoid generic second-level names such as `common/`, `util/`, `manager/`, `service/`, or `misc/`. If a package cannot be named by domain role, the boundary is probably not clear enough.

Sibling packages should communicate through explicit models, adapters, events, signals, snapshots, or service methods owned by the correct parent domain. Direct sibling-to-sibling imports are allowed only when the dependency direction is intentional and does not move ownership across boundaries.

## Program File Granularity

Every Java source file should have one primary reason to change. A file may contain a small single-purpose implementation, a data record, an enum, or a thin orchestrator, but it should not accumulate multiple feature implementations.

- Any non-single-purpose feature must be split into independent top-level source files.
- Split files should be grouped into the nearest domain folder, not pushed into a broad global package.
  - Commodity spot behavior goes under `product/commodity/spot/`.
  - Commodity shared attributes and physical state go under `product/commodity/core/`.
  - Company accounting facts go under `entity/company/`.
  - Generic market mechanics go under `market/financial/`.
  - UI widgets and panels go under `client/ui/` or a more specific UI subfolder.
- Prefer role-based file names: `*State`, `*Engine`, `*Service`, `*Adapter`, `*Snapshot`, `*Codec`, `*Row`, `*Panel`, `*Chart`, or a more precise domain name.
- Keep orchestrators thin. If an orchestrator handles scanning, transforming, pricing, persistence, rendering, and input handling, those parts should move into separate files.
- Avoid hiding separate features as large private method regions, nested helper classes, or long switch blocks inside one source file.
- A file should be treated as a split candidate when it crosses roughly 250 lines, has more than one domain responsibility, mixes UI with state mutation, mixes persistence with pricing, or is hard to review without reading unrelated behavior.
- Small cohesive files are preferred over one long file with high coupling. Do not create a new folder for every class; create or reuse the nearest category folder that groups related files.

Current refactor candidates from the existing tree:

- `client/ui/elements/MarketPanelElement.java`: split panel state, list rendering, workspace rendering, selection/input handling, and product-window orchestration.
- `client/ui/elements/MarketOrderFlowElement.java`: split market-flow model extraction from rendering.
- `client/ui/elements/MarketChartElement.java`: split chart data preparation from drawing, and separate chart types if they keep growing.
- `entity/company/VillageCompany.java`: split identity/cap table, accounting report state, price history, and persistence helpers when touched.
- `product/commodity/spot/SpotMarketAsset.java`: split commodity inventory facade, market cycle state, flow breakdown, settlement bridge, and persistence helpers.
- `command/MinanceCommands.java`: split command registration from command handlers if command surface keeps expanding.
- `market/financial/FinancialMarketEngine.java`: split generic market signal aggregation, liquidity impact, trader archetypes, and result assembly if new signals are added.
- `config/MarketConfig.java`: split nested config groups into focused config records if the market config grows further.

## Unified Calling Interface And Calling Docs

Every feature implementation must have one documented calling interface. Callers should enter a feature through that interface instead of reaching into helper classes, state objects, UI elements, or persistence internals.

- A feature's unified calling interface should be a focused `*Service`, `*Engine`, `*Adapter`, `*Facade`, event handler, or explicit public method set in the nearest domain folder.
- If a feature is implemented across several files, only the calling interface should be treated as the cross-package entry point. Helper files should remain local to the package whenever Java visibility allows it.
- Every existing second-level directory must contain a `CALLING.md` file that documents its current calling interface.
- New second-level directories must add `CALLING.md` in the same change that creates the directory.
- New features under an existing second-level directory must update that directory's `CALLING.md` in the same change that adds the feature.
- Third-level directories may add their own `CALLING.md` when they expose separate feature surfaces, but the parent second-level `CALLING.md` remains the required index.
- Call documents should follow `docs/calling-interface-template.md`.
- A call document must list scope, public entry points, allowed callers, lifecycle/cadence, inputs, outputs, side effects, ownership rules, and forbidden bypass calls.
- If no implementation exists yet, the local `CALLING.md` must say `Status: reserved` and define what the future first entry point must document before implementation.

Review rule: a feature change is incomplete if it adds behavior without a unified entry point or changes a callable surface without updating the nearest `CALLING.md`.

## Entity Scope

- `entity/company/`: village company identity, candidate formation, capitalization, shareholders, raw financial reports, company price bars, and company-only accounting facts.
- `entity/institution/`: financial institution identities and role grants, including the future default `central_bank_and_securities` provider and future player-created providers. It owns institution identity, ownership, operator permissions, service grants, licenses, provider metadata, and deterministic institution-origin signal requests, but not market pricing or product payoff logic.
- `entity/village/`: loaded-village scanning and village-level aggregation.
- `entity/villager/`: villager profession and trade discovery that emits supply/demand inputs into product or market services.
- `entity/player/`: player economic identity, player-origin intents, and future player-facing actor state.
- `entity/wandering_merchant/`: wandering-merchant economic identity and future merchant-origin supply/demand events.

Entity packages describe who exists in the Minecraft world and what they own or produce. They should emit facts or intents; they should not decide generic market price movement. Institution entities identify who provides a service; product and market packages still own the service behavior.

## Market Scope

- `market/financial/`: generic market engine, liquidity surface, price levels, trader archetype effects, market flow snapshots, and product-agnostic price movement results.
- `market/index/`: index definitions, levels, calculation, rebalance/reconstitution, and read models for index values.
- `market/event/`: normalized market events and future event routing between entity/product inputs and market services.

Market packages describe how markets clear, aggregate, and explain price pressure. They may consume product signals and anchors, but they must not own product-specific state such as commodity attributes, equity cap tables, fund holdings, or derivative payoff state.

## Product Scope

- `product/commodity/core/`: commodity identity, commodity attributes/profile data, physical inventory state, perish/storage/carry traits, target inventory policy, production/consumption tags, substitution groups, and seasonality profiles.
- `product/commodity/spot/`: commodity spot-market behavior: supply/demand events, order flow, inflow/outflow, observed reference price, stabilizer activity, settlement, price history, spot rows, and source attribution.
- `product/component/`: generic financial product component root. It groups component overlay and collection/index concepts, but does not own concrete product state or pricing.
- `product/component/core/`: reusable component attribute overlay model for attributes such as underlying exposure, maturity, settlement, payoff shape, basket exposure, NAV anchor, leverage, claim priority, liquidity wrapper, and tracking target.
- `product/component/collection/`: generic component membership/index read model for funds, structured products, futures, and options.
- `product/component/fund/`: fund state, holdings, NAV, fund share pricing, subscriptions/redemptions, index tracking, fees, and tracking error.
- `product/component/structured/`: structured product state, beneficiary claims, payoff rules, basket exposure, barrier/floor/cap state, and issuer/beneficiary risk adapters.
- `product/component/derivative/`: current shared derivative component boundary. It should split into futures and options before growing.
- `product/component/derivative/future/`: futures contracts, future market state, maturity, delivery method, contract status, and futures-specific pricing inputs.
- `product/component/derivative/option/`: option contracts, option market state, option rights, exercise state, maturity, and option-specific pricing inputs.
- `product/equity/`: equity asset state, equity types, tradable company-share synchronization, and adapters that convert company reports into equity pricing signals.
- `product/liabilities/`: liability products, borrower obligations, loan state, deposit/funding liabilities, liability securities, securitized credit references, principal, interest, maturity, collateral, seniority, institutional credit assessment references, recovery, and credit spread inputs.
- `product/liabilities/credit/`: planned credit-assessment subdomain for institution-issued ratings, credit limits, spread quotes, underwriting decisions, credit events, and assessment methodology references. Credit is not an entity tag and not a generic component attribute.
- `product/liabilities/deposit/`: planned deposit-product subdomain for account balances, deposit categories, posted rates, accrued interest, term/maturity, withdrawal constraints, insurance coverage, run-risk signals, and institution funding-flow effects.
- `product/insurance/`: insurance-like products, including premium, claim, reserve, guarantee, credit-insurance, and reinsurance logic.

Product packages describe what can be priced, held, issued, redeemed, exercised, settled, or tracked. Product packages may adapt entity facts into product signals and may pass generic signals to `market/financial/`.

Generic financial product components are documented in `docs/generic-product-components.md`. The current collection is `fund`, `structured`, `future`, and `option`. Their concrete behavior stays in `product/component/fund/`, `product/component/structured/`, `product/component/derivative/future/`, and `product/component/derivative/option/`. Until futures and options are physically split, existing flat `product/component/derivative/` classes should be treated as migration targets, not as the final owner layout. Existing Java package declarations may still lag the physical component paths during migration; treat that as a refactor target.

Current refactor note: if `CommodityStabilizationDesk` remains spot-market behavior, it should move from `product/commodity/core/` to `product/commodity/spot/` or be split so only reusable commodity-neutral pieces stay in `core/`.

## Data And Rule Scope

- Java `data/`: reads, decodes, reloads, persists, and exposes loaded config/rule/world data to domain services.
- Java `rule/`: declares rule schemas and registries. Rule classes should stay declarative and deterministic.
- Resource `data/minance/config/`: numeric tuning, thresholds, caps, decay constants, cadence settings, and model-selection parameters.
- Resource `data/minance/minance_rules/`: gameplay/economy content rules, such as profession behavior and company naming.

The split is intentional: `rule/` defines what a rule means, `data/` loads and stores it, and resource JSON provides the editable content.

## Dependency Direction

- `client/`, `command/`, and `network/` are outer adapters. They should call into domain services or display snapshots, not host domain rules.
- `config/` and `rule/` define data shape; `data/` handles lifecycle and persistence.
- `entity/` provides actor facts and accounting facts.
- `product/` converts entity/market facts into product state, anchors, and product-specific signals.
- `market/` combines generic signals, liquidity, indexes, and market-wide mechanics.
- `market/financial/` must remain product-agnostic. Product-specific code should feed it through explicit signals or anchors.
- All behavior must be reproducible from saved state plus config/rules. Do not introduce LLM-agent, prompt-driven, or external-model market actors.
- If a current class violates these boundaries, treat the mismatch as a refactor target instead of expanding the package's scope.
- When adding or modifying behavior, prefer creating a focused file in the nearest category folder over extending an already broad class.
- When adding or modifying a callable feature, update the nearest second-level `CALLING.md` and route external calls through the documented interface.
