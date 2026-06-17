package com.fluorineuck.minance.entity.village;

import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.VillageCandidate;
import com.fluorineuck.minance.rule.MinanceRuleRegistries;
import com.fluorineuck.minance.rule.ProfessionTradeCategoryRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class VillageService {
    public static final VillageService INSTANCE = new VillageService();

    private final Map<String, VillageCandidate> candidates = new LinkedHashMap<>();

    private VillageService() {
    }

    public Map<String, VillageCandidate> candidates() {
        return candidates;
    }

    public void replaceCandidates(Map<String, VillageCandidate> loadedCandidates) {
        candidates.clear();
        candidates.putAll(loadedCandidates);
    }

    public void removeCandidate(String id) {
        candidates.remove(id);
    }

    public Map<String, LoadedVillageScan> scanLoadedVillages(MinecraftServer server, CompanyConfig config) {
        Map<String, LoadedVillageScan> scans = new LinkedHashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            ResourceLocation dimension = level.dimension().location();
            List<? extends Villager> villagers = level.getEntities(EntityTypeTest.forClass(Villager.class), Villager::isAlive);
            for (Villager villager : villagers) {
                Optional<BlockPos> bell = nearestBell(level, villager.blockPosition(), config.bellSearchRadius());
                if (bell.isEmpty()) {
                    continue;
                }
                String id = villageId(dimension, bell.get());
                ResourceLocation profession = professionId(villager);
                scans.computeIfAbsent(id, ignored -> new LoadedVillageScan(id, dimension, bell.get())).add(villager, profession);
            }
        }
        return scans;
    }

    public List<VillageCandidate> updateCandidates(Map<String, LoadedVillageScan> scans, Set<String> existingCompanyIds, CompanyConfig config) {
        List<VillageCandidate> ready = new ArrayList<>();
        for (LoadedVillageScan scan : scans.values()) {
            if (existingCompanyIds.contains(scan.id())) {
                continue;
            }
            VillageCandidate candidate = candidates.computeIfAbsent(scan.id(), id -> new VillageCandidate(id, scan.dimension(), scan.bellPos(), config.initialVillageFunds()));
            candidate.professionCounts().clear();
            candidate.professionCounts().putAll(scan.professionCounts());
            candidate.members().clear();
            candidate.members().putAll(scan.members());
            candidate.setExpectedShares(expectedShares(scan.professionCounts()));
            candidate.setRequiredCapital(requiredCapital(candidate.expectedShares(), config));
            candidate.setProxyLiquidityDemand(scan.proxyLiquidityDemand());
            candidate.setDerivativeDemand(scan.derivativeDemand());
            candidate.addFunds(scan.productionIncome());
            if (candidate.funds() >= candidate.requiredCapital() && candidate.expectedShares() > 0) {
                ready.add(candidate);
            }
        }
        return ready;
    }

    public List<VillageCandidate> sortedCandidates() {
        return candidates.values().stream().sorted(Comparator.comparing(VillageCandidate::id)).toList();
    }

    private int expectedShares(Map<ResourceLocation, Integer> professionCounts) {
        int shares = 0;
        for (Map.Entry<ResourceLocation, Integer> entry : professionCounts.entrySet()) {
            int perVillager = MinanceRuleRegistries.INSTANCE.profession(entry.getKey()).map(ProfessionTradeCategoryRule::expectedShares).orElse(ConfigRegistry.INSTANCE.economy().fallbackExpectedShares());
            shares += Math.max(1, perVillager) * Math.max(0, entry.getValue());
        }
        return shares;
    }

    private long requiredCapital(int expectedShares, CompanyConfig config) {
        return Math.max(1L, config.minimumCapitalPerShare()) * Math.max(1, expectedShares);
    }

    private static Optional<BlockPos> nearestBell(ServerLevel level, BlockPos origin, int radius) {
        int searchRadius = Math.max(1, radius);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        int verticalRadius = Math.max(0, ConfigRegistry.INSTANCE.company().bellSearchVerticalRadius());
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-searchRadius, -verticalRadius, -searchRadius), origin.offset(searchRadius, verticalRadius, searchRadius))) {
            BlockPos immutable = pos.immutable();
            if (!level.isLoaded(immutable) || !level.getBlockState(immutable).is(Blocks.BELL)) {
                continue;
            }
            double distance = immutable.distSqr(origin);
            if (distance < nearestDistance) {
                nearest = immutable;
                nearestDistance = distance;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private static ResourceLocation professionId(Villager villager) {
        ResourceLocation id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
        return id == null ? ResourceLocation.withDefaultNamespace("none") : id;
    }

    public static String villageId(ResourceLocation dimension, BlockPos bellPos) {
        return dimension.toString().replace(':', '_') + "_" + bellPos.getX() + "_" + bellPos.getY() + "_" + bellPos.getZ();
    }
}
