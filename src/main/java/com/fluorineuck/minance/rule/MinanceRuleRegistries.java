package com.fluorineuck.minance.rule;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MinanceRuleRegistries {
    public static final MinanceRuleRegistries INSTANCE = new MinanceRuleRegistries();

    private volatile Map<ResourceLocation, ProfessionTradeCategoryRule> professionRules = Map.of();
    private volatile Map<ResourceLocation, CompanyNameRule> companyNameRules = Map.of();

    private MinanceRuleRegistries() {
    }

    public Map<ResourceLocation, ProfessionTradeCategoryRule> professionRules() {
        return professionRules;
    }

    public Optional<ProfessionTradeCategoryRule> profession(ResourceLocation profession) {
        return professionRules.values().stream().filter(rule -> rule.profession().equals(profession)).findFirst();
    }

    public CompanyNameRule companyNames() {
        return companyNameRules.values().stream().findFirst().orElse(CompanyNameRule.defaultRule());
    }

    public synchronized void replaceProfessionRules(Map<ResourceLocation, ProfessionTradeCategoryRule> rules) {
        professionRules = Map.copyOf(new ConcurrentHashMap<>(rules));
    }

    public synchronized void replaceCompanyNameRules(Map<ResourceLocation, CompanyNameRule> rules) {
        companyNameRules = Map.copyOf(new ConcurrentHashMap<>(rules));
    }
}
