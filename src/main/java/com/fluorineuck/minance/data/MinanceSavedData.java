package com.fluorineuck.minance.data;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.entity.company.VillageCandidate;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.market.index.MarketIndexService;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import net.minecraft.resources.ResourceLocation;
import com.fluorineuck.minance.product.component.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.component.fund.FundService;
import com.fluorineuck.minance.product.component.structured.StructuredProductService;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MinanceSavedData extends SavedData {
    private static final String DATA_NAME = Minance.MOD_ID + "_world_market";

    private MinanceSavedData() {
    }

    public static MinanceSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MinanceSavedData::new, MinanceSavedData::load, null),
                DATA_NAME
        );
    }

    public static MinanceSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MinanceSavedData data = new MinanceSavedData();
        Map<String, VillageCandidate> candidates = new LinkedHashMap<>();
        Map<String, VillageCompany> companies = new LinkedHashMap<>();
        Map<ResourceLocation, SpotMarketAsset> spotAssets = new LinkedHashMap<>();
        ListTag candidateTags = tag.getList("village_candidates", Tag.TAG_COMPOUND);
        for (int i = 0; i < candidateTags.size(); i++) {
            VillageCandidate candidate = VillageCandidate.load(candidateTags.getCompound(i));
            candidates.put(candidate.id(), candidate);
        }
        ListTag companyTags = tag.getList("village_companies", Tag.TAG_COMPOUND);
        for (int i = 0; i < companyTags.size(); i++) {
            VillageCompany company = VillageCompany.load(companyTags.getCompound(i));
            companies.put(company.id(), company);
        }
        ListTag spotTags = tag.getList("spot_assets", Tag.TAG_COMPOUND);
        for (int i = 0; i < spotTags.size(); i++) {
            SpotMarketAsset asset = SpotMarketAsset.load(spotTags.getCompound(i));
            spotAssets.put(asset.item(), asset);
        }
        SpotMarketService.INSTANCE.replaceAssets(spotAssets);
        VillageCompanyService.INSTANCE.replaceState(candidates, companies);
        if (tag.contains("commodity_derivatives", Tag.TAG_COMPOUND)) {
            CommodityDerivativeService.INSTANCE.load(tag.getCompound("commodity_derivatives"));
        }
        if (tag.contains("market_indices", Tag.TAG_COMPOUND)) {
            MarketIndexService.INSTANCE.load(tag.getCompound("market_indices"));
        }
        if (tag.contains("financial_markets", Tag.TAG_COMPOUND)) {
            FinancialMarketEngine.INSTANCE.load(tag.getCompound("financial_markets"));
        }
        if (tag.contains("fund_products", Tag.TAG_COMPOUND)) {
            FundService.INSTANCE.load(tag.getCompound("fund_products"));
        }
        if (tag.contains("structured_products", Tag.TAG_COMPOUND)) {
            StructuredProductService.INSTANCE.load(tag.getCompound("structured_products"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag candidateTags = new ListTag();
        for (VillageCandidate candidate : VillageCompanyService.INSTANCE.candidates().values()) {
            candidateTags.add(candidate.save());
        }
        tag.put("village_candidates", candidateTags);

        ListTag companyTags = new ListTag();
        for (VillageCompany company : VillageCompanyService.INSTANCE.companies().values()) {
            companyTags.add(company.save());
        }
        tag.put("village_companies", companyTags);
        ListTag spotTags = new ListTag();
        for (SpotMarketAsset asset : SpotMarketService.INSTANCE.assets().values()) {
            spotTags.add(asset.save());
        }
        tag.put("spot_assets", spotTags);
        tag.put("commodity_derivatives", CommodityDerivativeService.INSTANCE.save());
        tag.put("market_indices", MarketIndexService.INSTANCE.save());
        tag.put("financial_markets", FinancialMarketEngine.INSTANCE.save());
        tag.put("fund_products", FundService.INSTANCE.save());
        tag.put("structured_products", StructuredProductService.INSTANCE.save());
        return tag;
    }
}



