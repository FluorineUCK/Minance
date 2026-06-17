package com.fluorineuck.minance.rule;

import com.fluorineuck.minance.Minance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record CompanyNameRule(
        ResourceLocation id,
        List<String> prefixes,
        List<String> middleWords,
        List<String> suffixes
) {
    public static final Codec<CompanyNameRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CompanyNameRule::id),
            Codec.STRING.listOf().optionalFieldOf("prefixes", defaultRule().prefixes()).forGetter(CompanyNameRule::prefixes),
            Codec.STRING.listOf().optionalFieldOf("middle_words", defaultRule().middleWords()).forGetter(CompanyNameRule::middleWords),
            Codec.STRING.listOf().optionalFieldOf("suffixes", defaultRule().suffixes()).forGetter(CompanyNameRule::suffixes)
    ).apply(instance, CompanyNameRule::new));

    public static CompanyNameRule defaultRule() {
        return new CompanyNameRule(
                ResourceLocation.fromNamespaceAndPath(Minance.MOD_ID, "default"),
                List.of("Green", "Iron", "Oak", "River", "Stone", "Bright", "Copper", "Silver"),
                List.of("Harvest", "Forge", "Market", "Bell", "Craft", "Trade", "Village", "Ledger"),
                List.of("Company", "Holdings", "Exchange", "Works", "Guild", "Trust", "Partners")
        );
    }
}
