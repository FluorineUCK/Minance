package com.fluorineuck.minance.client.ui;

import com.fluorineuck.minance.client.ui.elements.MarketPanelElement;
import com.fluorineuck.minance.entity.institution.FinancialInstitutionDirectory;
import com.fluorineuck.minance.entity.institution.FinancialServiceAccessPoint;
import com.fluorineuck.minance.entity.institution.FinancialServiceProviderContext;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.w3c.dom.Document;

public final class MarketLdLibDashboard extends ModularUIScreen {
    private static final ResourceLocation DASHBOARD_XML = ResourceLocation.fromNamespaceAndPath("minance", "market_dashboard.xml");
    private final FinancialServiceProviderContext providerContext;
    private MarketPanelElement panelElement;

    private MarketLdLibDashboard(FinancialServiceProviderContext providerContext) {
        super(createUi(), title(providerContext));
        this.providerContext = providerContext;
    }

    public static void open() {
        open(FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(FinancialServiceAccessPoint.MARKET_DASHBOARD));
    }

    public static void open(FinancialServiceProviderContext providerContext) {
        FinancialServiceProviderContext context = providerContext == null
                ? FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(FinancialServiceAccessPoint.MARKET_DASHBOARD)
                : providerContext;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof MarketLdLibDashboard dashboard && dashboard.providerContext.equals(context)) {
            return;
        }
        minecraft.setScreen(new MarketLdLibDashboard(context));
    }

    public FinancialServiceProviderContext providerContext() {
        return providerContext;
    }

    @Override
    public void init() {
        super.init();
        panelElement = findPanelElement();
    }

    private MarketPanelElement findPanelElement() {
        MarketPanelElement element = getModularUI().getElementsByType(MarketPanelElement.class).stream().findFirst().orElse(null);
        if (element != null) {
            return element;
        }
        UIElement slot = getModularUI().getElementById("minance-dashboard-slot");
        if (slot == null) {
            throw new IllegalStateException("LDLib XML did not create minance-dashboard-slot");
        }
        element = new MarketPanelElement();
        element.setId("minance-main-panel");
        element.layout(style -> style.widthPercent(100).heightPercent(100));
        slot.clearAllChildren();
        slot.addChild(element);
        return element;
    }

    private static ModularUI createUi() {
        try {
            Document xml = XmlUtils.loadXml(DASHBOARD_XML);
            if (xml != null) {
                return ModularUI.of(UI.of(xml)).shouldCloseOnEsc(true).shouldCloseOnKeyInventory(true);
            }
        } catch (RuntimeException exception) {
            // Fall through to custom element fallback.
        }
        return ModularUI.of(UI.of(new MarketPanelElement())).shouldCloseOnEsc(true).shouldCloseOnKeyInventory(true);
    }

    private static Component title(FinancialServiceProviderContext providerContext) {
        return Component.literal("Minance - " + providerContext.displayName());
    }
}
