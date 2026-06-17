package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.gui.GuiGraphics;

@LDLRegister(name = "market-debug-panel", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketDebugPanelElement extends UIElement {
    @Override
    public void drawContents(GUIContext context) {
        GuiGraphics graphics = context.graphics;
        int left = Math.round(getContentX());
        int top = Math.round(getContentY());
        int right = left + Math.round(getContentWidth());
        int bottom = top + Math.round(getContentHeight());
        graphics.fill(left, top, right, bottom, 0xEF0D1117);
        graphics.drawString(MarketUiSupport.font(), "Market Debug", left + 8, top + 8, 0xFFE7F0FF, false);
        int y = top + 26;
        graphics.drawString(MarketUiSupport.font(), "Spot", left + 8, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        for (var asset : SpotMarketService.INSTANCE.assets().values().stream().limit(8).toList()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                return;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.shortId(asset.item().toString()) + " p=" + asset.price() + " next=" + Math.round(asset.nextPrice()) + " inv=" + asset.inventory() + "/" + Math.round(asset.targetInventory()) + " in/out=" + MarketUiSupport.fmt(asset.inflow()) + "/" + MarketUiSupport.fmt(asset.outflow()), left + 12, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        y += 6;
        graphics.drawString(MarketUiSupport.font(), "Financial", left + 8, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        for (var market : FinancialMarketEngine.INSTANCE.markets().values().stream().limit(8).toList()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                return;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(market.productId(), 20) + " p=" + market.currentPrice() + " bid/ask=" + MarketUiSupport.fmt(market.nearestBidLiquidity()) + "/" + MarketUiSupport.fmt(market.nearestAskLiquidity()) + " imb=" + MarketUiSupport.fmt(market.lastImbalance()) + " ord=" + market.stats().generatedOrderCount(), left + 12, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
    }
}
