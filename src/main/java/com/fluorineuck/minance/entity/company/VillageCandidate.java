package com.fluorineuck.minance.entity.company;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class VillageCandidate {
    private final String id;
    private final ResourceLocation dimension;
    private final BlockPos bellPos;
    private final Map<ResourceLocation, Integer> professionCounts = new LinkedHashMap<>();
    private final Map<UUID, ResourceLocation> members = new LinkedHashMap<>();
    private long funds;
    private long requiredCapital;
    private int expectedShares;
    private double proxyLiquidityDemand;
    private double derivativeDemand;

    public VillageCandidate(String id, ResourceLocation dimension, BlockPos bellPos, long funds) {
        this.id = id;
        this.dimension = dimension;
        this.bellPos = bellPos;
        this.funds = Math.max(0L, funds);
    }

    public String id() {
        return id;
    }

    public ResourceLocation dimension() {
        return dimension;
    }

    public BlockPos bellPos() {
        return bellPos;
    }

    public Map<ResourceLocation, Integer> professionCounts() {
        return professionCounts;
    }

    public Map<UUID, ResourceLocation> members() {
        return members;
    }

    public long funds() {
        return funds;
    }

    public void addFunds(long amount) {
        funds = Math.max(0L, funds + amount);
    }

    public long requiredCapital() {
        return requiredCapital;
    }

    public void setRequiredCapital(long requiredCapital) {
        this.requiredCapital = Math.max(0L, requiredCapital);
    }

    public int expectedShares() {
        return expectedShares;
    }

    public void setExpectedShares(int expectedShares) {
        this.expectedShares = Math.max(0, expectedShares);
    }

    public double proxyLiquidityDemand() {
        return proxyLiquidityDemand;
    }

    public void setProxyLiquidityDemand(double proxyLiquidityDemand) {
        this.proxyLiquidityDemand = Math.max(0.0D, proxyLiquidityDemand);
    }

    public double derivativeDemand() {
        return derivativeDemand;
    }

    public void setDerivativeDemand(double derivativeDemand) {
        this.derivativeDemand = Math.max(0.0D, derivativeDemand);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("dimension", dimension.toString());
        tag.putInt("bell_x", bellPos.getX());
        tag.putInt("bell_y", bellPos.getY());
        tag.putInt("bell_z", bellPos.getZ());
        tag.putLong("funds", funds);
        tag.putLong("required_capital", requiredCapital);
        tag.putInt("expected_shares", expectedShares);
        tag.putDouble("proxy_liquidity_demand", proxyLiquidityDemand);
        tag.putDouble("derivative_demand", derivativeDemand);
        tag.put("professions", saveProfessionCounts());
        tag.put("members", saveMembers());
        return tag;
    }

    public static VillageCandidate load(CompoundTag tag) {
        String id = tag.getString("id");
        ResourceLocation dimension = ResourceLocation.parse(tag.getString("dimension"));
        BlockPos bellPos = new BlockPos(tag.getInt("bell_x"), tag.getInt("bell_y"), tag.getInt("bell_z"));
        VillageCandidate candidate = new VillageCandidate(id, dimension, bellPos, tag.getLong("funds"));
        candidate.setRequiredCapital(tag.getLong("required_capital"));
        candidate.setExpectedShares(tag.getInt("expected_shares"));
        candidate.setProxyLiquidityDemand(tag.getDouble("proxy_liquidity_demand"));
        candidate.setDerivativeDemand(tag.getDouble("derivative_demand"));
        loadProfessionCounts(tag.getList("professions", Tag.TAG_COMPOUND), candidate.professionCounts);
        loadMembers(tag.getList("members", Tag.TAG_COMPOUND), candidate.members);
        return candidate;
    }

    private ListTag saveProfessionCounts() {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : professionCounts.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("profession", entry.getKey().toString());
            tag.putInt("count", Math.max(0, entry.getValue()));
            list.add(tag);
        }
        return list;
    }

    private ListTag saveMembers() {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, ResourceLocation> entry : members.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("villager", entry.getKey());
            tag.putString("profession", entry.getValue().toString());
            list.add(tag);
        }
        return list;
    }

    private static void loadProfessionCounts(ListTag list, Map<ResourceLocation, Integer> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            target.put(ResourceLocation.parse(tag.getString("profession")), Math.max(0, tag.getInt("count")));
        }
    }

    private static void loadMembers(ListTag list, Map<UUID, ResourceLocation> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            target.put(tag.getUUID("villager"), ResourceLocation.parse(tag.getString("profession")));
        }
    }
}
