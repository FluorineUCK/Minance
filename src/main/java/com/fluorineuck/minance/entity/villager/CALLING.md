# entity/villager Calling Guide

## Scope

Villager event hooks, villager trade discovery, profession-rule discovery, and villager-origin supply/demand intents.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Register villager discovery events | `VillagerTradeDiscoveryService.onEntityJoinLevel(...)`, `onPlayerEntityInteract(...)`, `onTradeWithVillager(...)`, `onBlockBreak(...)`, `onServerTick(...)` | `Minance.java` event registration | Triggers discovery, settlement cadence, index updates, fund updates |
| Scan loaded villagers | `VillagerTradeDiscoveryService.INSTANCE.scanLoadedVillagers(...)` | Internal tick orchestration | Records villager-origin spot supply/demand |

## Lifecycle

Event hooks run from NeoForge events. Server tick performs loaded villager scan, company scan, settlement, index update, and fund update according to config cadences.

## Inputs

Villager entities, merchant offers, profession rules, trading config, market config, and current server tick.

## Outputs

Spot-market supply/demand records, discovery cache updates, company scan calls, index updates, and fund updates.

## Ownership Rules

This package may discover villager-origin facts and intents. Product pricing remains in `product/commodity/spot/`; company registration remains behind `entity/company/VillageCompanyService`.

## Forbidden Bypass Calls

Do not add non-villager market actors here. Do not put generic spot pricing or company accounting logic in this package.

