package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.entity.company.MarketPriceBar;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.market.index.MarketIndexState;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.fluorineuck.minance.product.fund.FundState;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

@LDLRegister(name = "market-chart", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketChartElement extends UIElement {
    static final int CHART_LINE = 0;
    static final int CHART_BAR = 1;
    static final int CHART_CANDLE = 2;

    @Override
    public void drawContents(GUIContext context) {
    }

    static void drawToolbar(GuiGraphics graphics, int left, int top, int chartMode, boolean showMa5, boolean showMa10, boolean showMa30, boolean showVolume, boolean showOrderFlow, boolean showOhlc) {
        int x = left;
        x = drawToolbarButton(graphics, x, top, "Line", chartMode == CHART_LINE);
        x = drawToolbarButton(graphics, x, top, "Bar", chartMode == CHART_BAR);
        x = drawToolbarButton(graphics, x, top, "Candle", chartMode == CHART_CANDLE);
        x += 4;
        x = drawToolbarButton(graphics, x, top, "MA5", showMa5);
        x = drawToolbarButton(graphics, x, top, "MA10", showMa10);
        x = drawToolbarButton(graphics, x, top, "MA30", showMa30);
        x = drawToolbarButton(graphics, x, top, "Vol", showVolume);
        x = drawToolbarButton(graphics, x, top, "Flow", showOrderFlow);
        drawToolbarButton(graphics, x, top, "OHLC", showOhlc);
    }

    static void drawCompanyChart(GuiGraphics graphics, VillageCompany company, int left, int top, int right, int bottom, int chartMode, boolean showMa5, boolean showMa10, boolean showMa30, boolean showVolume, boolean showOrderFlow, boolean showOhlc, float mouseX, float mouseY) {
        graphics.fill(left, top, right, bottom, 0xFF0B1118);
        if (company == null) {
            graphics.drawString(MarketUiSupport.font(), "No registered equity yet", left + 10, top + 10, 0xFF7B8493, false);
            return;
        }
        List<MarketPriceBar> bars = new ArrayList<>(company.priceBars());
        drawFinancialChart(graphics, MarketUiSupport.trim(company.name(), 32), company.sharePrice(), bars, left, top, right, bottom, chartMode, showMa5, showMa10, showMa30, showVolume, showOrderFlow, showOhlc, mouseX, mouseY);
    }

    static void drawIndexChart(GuiGraphics graphics, MarketIndexState index, int left, int top, int right, int bottom, int chartMode, boolean showMa5, boolean showMa10, boolean showMa30, boolean showVolume, boolean showOrderFlow, boolean showOhlc, float mouseX, float mouseY) {
        graphics.fill(left, top, right, bottom, 0xFF0B1118);
        if (index == null) {
            graphics.drawString(MarketUiSupport.font(), "No index data yet", left + 10, top + 10, 0xFF7B8493, false);
            return;
        }
        List<MarketPriceBar> bars = barsFromIndex(index);
        drawFinancialChart(graphics, index.name() + "  components " + index.componentCount(), index.price(), bars, left, top, right, bottom, chartMode, showMa5, showMa10, showMa30, showVolume, showOrderFlow, showOhlc, mouseX, mouseY);
    }

    static void drawCommodityChart(GuiGraphics graphics, SpotMarketRow row, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF0B1118);
        if (row == null) {
            graphics.drawString(MarketUiSupport.font(), "No commodity data yet", left + 10, top + 10, 0xFF7B8493, false);
            return;
        }
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.shortId(row.item().toString()) + "  price " + row.price() + "  vol " + row.volume(), left + 10, top + 8, 0xFFE7F0FF, false);
        SpotMarketAsset asset = SpotMarketService.INSTANCE.assets().get(row.item());
        List<Long> history = asset == null ? List.of(row.price()) : new ArrayList<>(asset.priceHistory());
        drawPriceLine(graphics, history.isEmpty() ? List.of(row.price()) : history, row.price(), left + 10, top + 34, right - 10, bottom - 10);
    }

    static void drawFundChart(GuiGraphics graphics, FundState fund, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF0B1118);
        if (fund == null) {
            graphics.drawString(MarketUiSupport.font(), "No fund data yet", left + 10, top + 10, 0xFF7B8493, false);
            return;
        }
        graphics.drawString(MarketUiSupport.font(), fund.name() + "  NAV " + Math.round(fund.nav()) + "  share " + fund.sharePrice(), left + 10, top + 8, 0xFFE7F0FF, false);
        int y = top + 30;
        graphics.drawString(MarketUiSupport.font(), "Holdings are purchased product objects", left + 10, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT + 4;
        for (var holding : fund.holdings()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                break;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(holding.productId(), 34) + " qty " + MarketUiSupport.fmt(holding.quantity()) + " value " + Math.round(holding.marketValue()), left + 10, y, holding.unrealizedPnl() >= 0.0D ? 0xFFA7F3D0 : 0xFFFF9E9E, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
    }

    static int handleToolbarClick(float mouseX, float mouseY, int left, int top) {
        if (mouseY < top || mouseY > top + 14 || mouseX < left) {
            return -1;
        }
        int cursor = left;
        String[] labels = {"Line", "Bar", "Candle", "MA5", "MA10", "MA30", "Vol", "Flow", "OHLC"};
        for (int i = 0; i < labels.length; i++) {
            int width = MarketUiSupport.toolbarWidth(labels[i]);
            if (mouseX <= cursor + width) {
                return i;
            }
            cursor += width + (i == 2 ? 7 : 3);
        }
        return -1;
    }

    private static void drawFinancialChart(GuiGraphics graphics, String title, long price, List<MarketPriceBar> bars, int left, int top, int right, int bottom, int chartMode, boolean showMa5, boolean showMa10, boolean showMa30, boolean showVolume, boolean showOrderFlow, boolean showOhlc, float mouseX, float mouseY) {
        graphics.drawString(MarketUiSupport.font(), title + "  price " + price, left + 10, top + 8, 0xFFE7F0FF, false);
        if (right - left >= 330) {
            graphics.drawString(MarketUiSupport.font(), "Line / K-line / MA / Flow", right - 142, top + 8, 0xFF7B8493, false);
        }
        drawToolbar(graphics, left + 10, top + 24, chartMode, showMa5, showMa10, showMa30, showVolume, showOrderFlow, showOhlc);
        if (bars.isEmpty()) {
            drawNoBars(graphics, price, left + 10, top + 48, right - 10, bottom - 10);
            return;
        }
        int lowerHeight = (showVolume ? 38 : 0) + (showOrderFlow ? 20 : 0);
        int chartTop = top + 46;
        int chartBottom = Math.max(chartTop + 42, bottom - lowerHeight - 8);
        drawBars(graphics, bars, left + 10, chartTop, right - 10, chartBottom, chartMode, showMa5, showMa10, showMa30);
        if (showOhlc) {
            drawOhlcHover(graphics, bars, left + 10, chartTop, right - 10, chartBottom, mouseX, mouseY);
        }
        int y = chartBottom + 6;
        if (showVolume) {
            MarketOrderFlowElement.drawVolume(graphics, bars, left + 10, y, right - 10, y + 34);
            y += 38;
        }
        if (showOrderFlow) {
            MarketOrderFlowElement.drawOrderFlow(graphics, bars, left + 10, y, right - 10, Math.min(bottom - 8, y + 16));
        }
    }

    private static void drawNoBars(GuiGraphics graphics, long price, int left, int top, int right, int bottom) {
        graphics.drawString(MarketUiSupport.font(), "No settlement bars yet", left, top, 0xFFFFD166, false);
        graphics.fill(left, top + 18, right, bottom, 0xFF0E1620);
        int midY = (top + 18 + bottom) / 2;
        graphics.hLine(left + 8, right - 8, midY, 0xFF566171);
        graphics.drawString(MarketUiSupport.font(), "Last " + price, left + 12, top + 30, 0xFFA7F3D0, false);
    }

    private static void drawBars(GuiGraphics graphics, List<MarketPriceBar> bars, int left, int top, int right, int bottom, int chartMode, boolean showMa5, boolean showMa10, boolean showMa30) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (MarketPriceBar bar : bars) {
            min = Math.min(min, bar.low());
            max = Math.max(max, bar.high());
        }
        if (min == Long.MAX_VALUE || max <= min) {
            max = min + 1L;
        }
        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);
        graphics.fill(left, top, right, bottom, 0xFF0E1620);
        for (int i = 0; i <= 4; i++) {
            int y = top + i * height / 4;
            graphics.hLine(left, right, y, 0x332A3542);
        }
        if (chartMode == CHART_LINE) {
            drawSmoothCloseLine(graphics, bars, min, max, left, top, right, bottom);
        } else {
            int count = bars.size();
            int step = Math.max(3, width / Math.max(1, count));
            int bodyWidth = Math.max(1, Math.min(6, step - 1));
            for (int i = 0; i < count; i++) {
                MarketPriceBar bar = bars.get(i);
                int x = left + i * width / count + step / 2;
                int openY = MarketUiSupport.scale(bar.open(), min, max, top, bottom);
                int closeY = MarketUiSupport.scale(bar.close(), min, max, top, bottom);
                int highY = MarketUiSupport.scale(bar.high(), min, max, top, bottom);
                int lowY = MarketUiSupport.scale(bar.low(), min, max, top, bottom);
                int color = bar.close() >= bar.open() ? 0xFFA7F3D0 : 0xFFFF9E9E;
                int wickColor = bar.close() >= bar.open() ? 0xFFD7FBE8 : 0xFFFFC2C2;
                if (chartMode == CHART_BAR) {
                    drawVLine(graphics, x, highY, lowY, color);
                    graphics.hLine(x - 3, x + 3, highY, 0xFFE7F0FF);
                    graphics.hLine(x - 3, x + 3, lowY, 0xFFE7F0FF);
                    graphics.hLine(x - bodyWidth - 1, x, openY, color);
                    graphics.hLine(x, x + bodyWidth + 1, closeY, color);
                } else {
                    int bodyTop = Math.min(openY, closeY);
                    int bodyBottom = Math.max(openY, closeY);
                    int halfBody = Math.max(1, bodyWidth / 2);
                    int visibleHighY = highY >= bodyTop ? Math.max(top, bodyTop - 3) : highY;
                    int visibleLowY = lowY <= bodyBottom ? Math.min(bottom - 1, bodyBottom + 3) : lowY;
                    graphics.fill(x - halfBody, bodyTop, x + halfBody + 1, bodyBottom + 1, color);
                    drawVLine(graphics, x, visibleHighY, Math.max(visibleHighY, bodyTop - 1), wickColor);
                    drawVLine(graphics, x, Math.min(visibleLowY, bodyBottom + 1), visibleLowY, wickColor);
                    graphics.hLine(x - 2, x + 2, visibleHighY, wickColor);
                    graphics.hLine(x - 2, x + 2, visibleLowY, wickColor);
                }
            }
        }
        if (showMa5) {
            drawMovingAverage(graphics, bars, 5, 0xFFFFD166, "MA5", min, max, left, top, right, bottom);
        }
        if (showMa10) {
            drawMovingAverage(graphics, bars, 10, 0xFF8BD3FF, "MA10", min, max, left, top, right, bottom);
        }
        if (showMa30) {
            drawMovingAverage(graphics, bars, 30, 0xFFDDA0DD, "MA30", min, max, left, top, right, bottom);
        }
        graphics.drawString(MarketUiSupport.font(), "H " + max, left + 4, top + 4, 0xFF7B8493, false);
        graphics.drawString(MarketUiSupport.font(), "L " + min, left + 4, bottom - 10, 0xFF7B8493, false);
    }


    private static void drawVLine(GuiGraphics graphics, int x, int y1, int y2, int color) {
        graphics.vLine(x, Math.min(y1, y2), Math.max(y1, y2), color);
    }
    private static void drawSmoothCloseLine(GuiGraphics graphics, List<MarketPriceBar> bars, long min, long max, int left, int top, int right, int bottom) {
        if (bars.isEmpty()) {
            return;
        }
        int width = Math.max(1, right - left);
        int lastX = left;
        int lastY = MarketUiSupport.scale(smoothedClose(bars, 0), min, max, top, bottom);
        for (int i = 1; i < bars.size(); i++) {
            int x = left + i * width / Math.max(1, bars.size() - 1);
            int y = MarketUiSupport.scale(smoothedClose(bars, i), min, max, top, bottom);
            MarketUiSupport.drawLine(graphics, lastX, lastY, x, y, bars.get(i).close() >= bars.get(i - 1).close() ? 0xFFA7F3D0 : 0xFFFF9E9E);
            lastX = x;
            lastY = y;
        }
    }

    private static long smoothedClose(List<MarketPriceBar> bars, int index) {
        long previous = bars.get(Math.max(0, index - 1)).close();
        long current = bars.get(index).close();
        long next = bars.get(Math.min(bars.size() - 1, index + 1)).close();
        return (previous + current * 2L + next) / 4L;
    }

    private static void drawMovingAverage(GuiGraphics graphics, List<MarketPriceBar> bars, int period, int color, String label, long min, long max, int left, int top, int right, int bottom) {
        if (bars.size() < period) {
            return;
        }
        int width = Math.max(1, right - left);
        int lastX = -1;
        int lastY = -1;
        for (int i = period - 1; i < bars.size(); i++) {
            long sum = 0L;
            for (int j = i - period + 1; j <= i; j++) {
                sum += bars.get(j).close();
            }
            long average = sum / period;
            int x = left + i * width / Math.max(1, bars.size() - 1);
            int y = MarketUiSupport.scale(average, min, max, top, bottom);
            if (lastX >= 0) {
                MarketUiSupport.drawLine(graphics, lastX, lastY, x, y, color);
            }
            lastX = x;
            lastY = y;
        }
        graphics.drawString(MarketUiSupport.font(), label, Math.max(left + 4, right - 34), top + 4 + (period == 5 ? 0 : period == 10 ? 10 : 20), color, false);
    }

    private static void drawOhlcHover(GuiGraphics graphics, List<MarketPriceBar> bars, int left, int top, int right, int bottom, float mouseX, float mouseY) {
        if (Float.isNaN(mouseX) || Float.isNaN(mouseY) || mouseX < left || mouseX > right || mouseY < top || mouseY > bottom || bars.isEmpty()) {
            return;
        }
        int width = Math.max(1, right - left);
        int index = MarketUiSupport.clamp((int) ((mouseX - left) * bars.size() / width), 0, bars.size() - 1);
        MarketPriceBar bar = bars.get(index);
        int x = left + index * width / Math.max(1, bars.size()) + Math.max(2, width / Math.max(1, bars.size()) / 2);
        graphics.vLine(x, top, bottom, 0x88D7DEE8);
        graphics.hLine(left, right, Math.round(mouseY), 0x44566171);
        String line1 = "O " + bar.open() + "  H " + bar.high();
        String line2 = "L " + bar.low() + "  C " + bar.close();
        String line3 = "V " + bar.volume() + "  B/S " + bar.buyVolume() + "/" + bar.sellVolume();
        int boxWidth = Math.max(MarketUiSupport.font().width(line1), Math.max(MarketUiSupport.font().width(line2), MarketUiSupport.font().width(line3))) + 12;
        int boxLeft = Math.round(mouseX) + 12;
        if (boxLeft + boxWidth > right) {
            boxLeft = Math.round(mouseX) - boxWidth - 12;
        }
        int boxTop = Math.round(mouseY) + 10;
        if (boxTop + 42 > bottom) {
            boxTop = Math.round(mouseY) - 48;
        }
        graphics.fill(boxLeft, boxTop, boxLeft + boxWidth, boxTop + 42, 0xF0182331);
        graphics.hLine(boxLeft, boxLeft + boxWidth, boxTop, 0xFF566171);
        graphics.hLine(boxLeft, boxLeft + boxWidth, boxTop + 41, 0xFF566171);
        graphics.vLine(boxLeft, boxTop, boxTop + 42, 0xFF566171);
        graphics.vLine(boxLeft + boxWidth, boxTop, boxTop + 42, 0xFF566171);
        graphics.drawString(MarketUiSupport.font(), line1, boxLeft + 6, boxTop + 6, 0xFFE7F0FF, false);
        graphics.drawString(MarketUiSupport.font(), line2, boxLeft + 6, boxTop + 17, bar.close() >= bar.open() ? 0xFFA7F3D0 : 0xFFFF9E9E, false);
        graphics.drawString(MarketUiSupport.font(), line3, boxLeft + 6, boxTop + 28, 0xFF7B8493, false);
    }

    private static List<MarketPriceBar> barsFromIndex(MarketIndexState index) {
        List<Long> history = new ArrayList<>(index.history());
        if (history.isEmpty()) {
            history = List.of(index.price());
        }
        List<MarketPriceBar> bars = new ArrayList<>();
        long previous = history.get(0);
        for (int i = 0; i < history.size(); i++) {
            long close = history.get(i);
            long open = i == 0 ? previous : history.get(i - 1);
            long high = Math.max(open, close);
            long low = Math.min(open, close);
            bars.add(new MarketPriceBar(i, i + 1L, open, high, low, close, 0, 0));
            previous = close;
        }
        return bars;
    }

    private static void drawPriceLine(GuiGraphics graphics, List<Long> history, long fallbackPrice, int left, int top, int right, int bottom) {
        long min = history.stream().mapToLong(Long::longValue).min().orElse(fallbackPrice);
        long max = history.stream().mapToLong(Long::longValue).max().orElse(fallbackPrice);
        if (max <= min) {
            max = min + 1;
        }
        graphics.fill(left, top, right, bottom, 0xFF0E1620);
        int width = Math.max(1, right - left);
        int lastX = left;
        int lastY = MarketUiSupport.scale(history.get(0), min, max, top, bottom);
        for (int i = 1; i < history.size(); i++) {
            int x = left + i * width / Math.max(1, history.size() - 1);
            int y = MarketUiSupport.scale(history.get(i), min, max, top, bottom);
            MarketUiSupport.drawLine(graphics, lastX, lastY, x, y, history.get(i) >= history.get(i - 1) ? 0xFFA7F3D0 : 0xFFFF9E9E);
            lastX = x;
            lastY = y;
        }
        graphics.drawString(MarketUiSupport.font(), "H " + max + "  L " + min + "  C " + fallbackPrice, left + 6, top + 6, 0xFFB7C2D0, false);
    }

    private static int drawToolbarButton(GuiGraphics graphics, int left, int top, String label, boolean active) {
        int width = MarketUiSupport.toolbarWidth(label);
        graphics.fill(left, top, left + width, top + 14, active ? 0xFF234C63 : 0xFF182331);
        graphics.drawString(MarketUiSupport.font(), label, left + 5, top + 3, active ? 0xFF8BD3FF : 0xFF7B8493, false);
        return left + width + 3;
    }
}
