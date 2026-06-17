package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.entity.company.MarketPriceBar;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

final class MarketUiSupport {
    static final int ROW_HEIGHT = 12;

    private MarketUiSupport() {
    }

    static Font font() {
        return Minecraft.getInstance().font;
    }

    static int scale(long value, long min, long max, int top, int bottom) {
        double ratio = (value - min) / (double) Math.max(1L, max - min);
        return bottom - 1 - (int) Math.round(ratio * Math.max(1, bottom - top - 2));
    }

    static void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps <= 0) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    static String trendSymbol(SpotMarketRow row) {
        if (row.priceDelta() > 0L) {
            return "▲";
        }
        if (row.priceDelta() < 0L) {
            return "▼";
        }
        return "-";
    }

    static int trendColor(SpotMarketRow row) {
        if (row.priceDelta() > 0L) {
            return 0xFFA7F3D0;
        }
        if (row.priceDelta() < 0L) {
            return 0xFFFF9E9E;
        }
        return 0xFFB7C2D0;
    }

    static int priceColor(MarketPriceBar last) {
        if (last == null || last.close() == last.open()) {
            return 0xFFB7C2D0;
        }
        return last.close() > last.open() ? 0xFFA7F3D0 : 0xFFFF9E9E;
    }

    static String shortId(String id) {
        int index = id.indexOf(':');
        return index >= 0 ? id.substring(index + 1) : id;
    }

    static String trim(String value, int maxChars) {
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, Math.max(1, maxChars - 1)) + "...";
    }

    static String fmt(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static int toolbarWidth(String label) {
        return Math.max(30, font().width(label) + 10);
    }
}
