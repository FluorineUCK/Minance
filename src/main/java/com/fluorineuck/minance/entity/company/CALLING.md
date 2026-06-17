# entity/company Calling Guide

## Scope

Village company identity, candidates, capitalization, shareholder records, company accounting, raw financial reports, and company-owned facts.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Company scan and registration orchestration | `VillageCompanyService.INSTANCE.scanLoadedVillages(...)` | `entity/villager` tick orchestration and server lifecycle code | Updates candidates, companies, reports, equity sync, derivatives, and saved-data dirty state |
| Read sorted companies/candidates | `VillageCompanyService.INSTANCE.sortedCompanies()` / `sortedCandidates()` | UI, commands, product adapters | Read-only list views |
| Read company reports | `VillageCompany.latestFinancialReport()` / `financialReports()` | Product adapters, UI, commands | Read-only report and accounting snapshot access |
| Restore company state | `VillageCompanyService.INSTANCE.replaceState(...)` | `data/MinanceSavedData` | Replaces loaded company and candidate state |

## Lifecycle

Company scanning runs on the configured loaded-village scan cadence. Restore runs during saved-data load.

## Inputs

Loaded village scans, company config, profession rules, saved NBT state, and current game time.

## Outputs

Village candidates, registered companies, shareholder records, company reports, operating statements, balance sheets, financial metrics, equity assets, and derivative update triggers.

## Ownership Rules

External callers should use `VillageCompanyService` as the package entry point. `CompanyService`, `VillageCompany`, `VillageCandidate`, `CompanyFinancialReport`, and `MarketPriceBar` are domain internals unless a specific read method is documented here.

`CompanyFinancialReport` owns raw company accounting facts only. Product packages may read reports and convert them into product-specific signals, but market packages must consume only generic product output.

## Forbidden Bypass Calls

Do not let market engines mutate company accounting fields directly. Do not translate company reports into generic market signals outside `product/equity/`.

