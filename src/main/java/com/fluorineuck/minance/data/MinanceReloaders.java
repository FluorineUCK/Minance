package com.fluorineuck.minance.data;

import com.fluorineuck.minance.rule.CompanyNameRule;
import com.fluorineuck.minance.rule.MinanceRuleRegistries;
import com.fluorineuck.minance.rule.ProfessionTradeCategoryRule;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class MinanceReloaders {
    private MinanceReloaders() {
    }

    public static void registerReloaders(AddReloadListenerEvent event) {
        MinanceRuleRegistries rules = MinanceRuleRegistries.INSTANCE;
        event.addListener(new ConfigReloadListener());
        event.addListener(new CodecRuleReloadListener<>("minance_rules/professions", "profession trade category", ProfessionTradeCategoryRule.CODEC, ProfessionTradeCategoryRule::id, rules::replaceProfessionRules));
        event.addListener(new CodecRuleReloadListener<>("minance_rules/company_names", "company names", CompanyNameRule.CODEC, CompanyNameRule::id, rules::replaceCompanyNameRules));
    }
}
