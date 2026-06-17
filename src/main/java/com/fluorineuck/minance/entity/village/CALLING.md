# entity/village Calling Guide

## Scope

Loaded-village scanning, village-level aggregation, bell-centered village identity, and candidate aggregation support.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Scan loaded villages | `VillageService.INSTANCE.scanLoadedVillages(...)` | `entity/company/VillageCompanyService` | Produces loaded-village scan snapshots |
| Update candidates from scans | `VillageService.INSTANCE.updateCandidates(...)` | `entity/company/VillageCompanyService` | Mutates candidate state |
| Restore/remove candidates | `VillageService.INSTANCE.replaceCandidates(...)` / `removeCandidate(...)` | `entity/company` and `data` load path | Replaces or removes candidate state |

## Lifecycle

Called during company scan cadence and saved-data restore.

## Inputs

Minecraft server levels, company config, profession rules, current company ids, and loaded candidate state.

## Outputs

`LoadedVillageScan` snapshots and updated `VillageCandidate` state.

## Ownership Rules

External packages should normally call `VillageCompanyService` instead of calling `VillageService` directly. `VillageService` is the local village aggregation entry point for company orchestration.

## Forbidden Bypass Calls

Do not put company registration, financial reporting, product creation, or market pricing in `entity/village/`.

