package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.MarketPriceBar;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.market.financial.FinancialMarketState;
import com.fluorineuck.minance.market.financial.PriceLevel;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@LDLRegister(name = "market-order-flow", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketOrderFlowElement extends UIElement {
    @Override
    public void drawContents(GUIContext context) {
    }

    static void drawCompanyVolume(GuiGraphics graphics, VillageCompany company, int left, int top, int right, int bottom) {
        drawVolume(graphics, new ArrayList<>(company.priceBars()), left, top, right, bottom);
    }

    static void drawCompanyOrderFlow(GuiGraphics graphics, VillageCompany company, int left, int top, int right, int bottom) {
        drawOrderFlow(graphics, new ArrayList<>(company.priceBars()), left, top, right, bottom);
    }

    static void drawVolume(GuiGraphics graphics, List<MarketPriceBar> bars, int left, int top, int right, int bottom) {
        if (bars.isEmpty()) {
            return;
        }
        int maxVolume = bars.stream().mapToInt(MarketPriceBar::volume).max().orElse(1);
        graphics.fill(left, top, right, bottom, 0xFF0E1620);
        int width = Math.max(1, right - left);
        int count = bars.size();
        for (int i = 0; i < count; i++) {
            MarketPriceBar bar = bars.get(i);
            int x0 = left + i * width / count;
            int x1 = left + (i + 1) * width / count - 1;
            int buyHeight = (bottom - top) * Math.max(0, bar.buyVolume()) / Math.max(1, maxVolume);
            int sellHeight = (bottom - top) * Math.max(0, bar.sellVolume()) / Math.max(1, maxVolume);
            graphics.fill(x0, bottom - buyHeight, Math.max(x0 + 1, x1), bottom, 0xAA3FBF7F);
            graphics.fill(x0, bottom - buyHeight - sellHeight, Math.max(x0 + 1, x1), bottom - buyHeight, 0xAAEF6461);
        }
        graphics.drawString(MarketUiSupport.font(), "Volume", left + 4, top + 4, 0xFF7B8493, false);
    }

    static void drawOrderFlow(GuiGraphics graphics, List<MarketPriceBar> bars, int left, int top, int right, int bottom) {
        if (bars.isEmpty()) {
            return;
        }
        graphics.fill(left, top, right, bottom, 0xFF0E1620);
        int maxDelta = bars.stream().mapToInt(bar -> Math.abs(bar.buyVolume() - bar.sellVolume())).max().orElse(1);
        int width = Math.max(1, right - left);
        int count = bars.size();
        int mid = (top + bottom) / 2;
        graphics.hLine(left, right, mid, 0x66404B5A);
        for (int i = 0; i < count; i++) {
            MarketPriceBar bar = bars.get(i);
            int delta = bar.buyVolume() - bar.sellVolume();
            int x0 = left + i * width / count;
            int x1 = left + (i + 1) * width / count - 1;
            int height = (bottom - top) / 2 * Math.abs(delta) / Math.max(1, maxDelta);
            if (delta >= 0) {
                graphics.fill(x0, mid - height, Math.max(x0 + 1, x1), mid, 0xCC3FBF7F);
            } else {
                graphics.fill(x0, mid, Math.max(x0 + 1, x1), mid + height, 0xCCEF6461);
            }
        }
        graphics.drawString(MarketUiSupport.font(), "Order flow delta", left + 4, top + 4, 0xFF7B8493, false);
    }

    static void drawDepthBook(GuiGraphics graphics, String productId, String displayId, long price, int left, int top, int right, int bottom, DepthBookView view) {
        graphics.fill(left, top, right, bottom, 0xFF101822);
        DepthBookView activeView = view == null ? DepthBookView.CUMULATIVE : view;
        graphics.drawString(MarketUiSupport.font(), activeView == DepthBookView.CUMULATIVE ? "Depth" : "Post-match", left + 6, top + 6, 0xFF8BD3FF, false);
        drawDepthBookSelector(graphics, left, top, right, activeView);

        FinancialMarketState market = FinancialMarketEngine.INSTANCE.markets().get(productId == null ? "" : productId);
        double tickSize = Math.max(ConfigRegistry.INSTANCE.market().financialMicrostructure().minimumTickSize(), ConfigRegistry.INSTANCE.market().financialMicrostructure().tickSize());
        BookSnapshot snapshot = market == null || market.surface().levels().isEmpty()
                ? syntheticBook(displayId, price, activeView)
                : snapshotBook(market, tickSize, activeView);
        if (activeView == DepthBookView.MATCHED) {
            drawSeparatedBookRows(graphics, snapshot, left, top + 22, right, bottom);
        } else {
            drawAlternatingBookRows(graphics, snapshot, left, top + 22, right, bottom);
        }
        if (activeView == DepthBookView.MATCHED) {
            String matched = "matched " + MarketUiSupport.fmt(snapshot.matchedVolume());
            graphics.drawString(MarketUiSupport.font(), matched, right - Math.min(76, MarketUiSupport.font().width(matched) + 6), bottom - 10, 0xFF7B8493, false);
        }
    }

    static DepthBookView hitDepthBookSelector(float x, float y, int left, int top, int right) {
        if (y < top + 4 || y > top + 18) {
            return null;
        }
        int matchedLeft = matchedButtonLeft(right);
        int cumulativeLeft = cumulativeButtonLeft(right);
        if (x >= cumulativeLeft && x <= cumulativeLeft + 42) {
            return DepthBookView.CUMULATIVE;
        }
        if (x >= matchedLeft && x <= matchedLeft + 58) {
            return DepthBookView.MATCHED;
        }
        return null;
    }

    private static void drawDepthBookSelector(GuiGraphics graphics, int left, int top, int right, DepthBookView view) {
        drawSelectorButton(graphics, cumulativeButtonLeft(right), top + 4, 42, "Cum", view == DepthBookView.CUMULATIVE);
        drawSelectorButton(graphics, matchedButtonLeft(right), top + 4, 58, "Matched", view == DepthBookView.MATCHED);
    }

    private static void drawSelectorButton(GuiGraphics graphics, int left, int top, int width, String label, boolean active) {
        graphics.fill(left, top, left + width, top + 14, active ? 0xFF234C63 : 0xFF182331);
        graphics.drawString(MarketUiSupport.font(), label, left + 5, top + 3, active ? 0xFF8BD3FF : 0xFF7B8493, false);
    }

    private static int cumulativeButtonLeft(int right) {
        return right - 109;
    }

    private static int matchedButtonLeft(int right) {
        return right - 64;
    }

    private static BookSnapshot snapshotBook(FinancialMarketState market, double tickSize, DepthBookView view) {
        List<MutableLevel> asks = new ArrayList<>();
        List<MutableLevel> bids = new ArrayList<>();
        for (PriceLevel level : market.surface().levels().values()) {
            long price = Math.max(1L, Math.round(level.priceTick() * tickSize));
            if (level.askLiquidity() > 0.0D) {
                asks.add(new MutableLevel(price, level.askLiquidity(), level.askOrderCount()));
            }
            if (level.bidLiquidity() > 0.0D) {
                bids.add(new MutableLevel(price, level.bidLiquidity(), level.bidOrderCount()));
            }
        }
        asks.sort(Comparator.comparingLong(MutableLevel::price));
        bids.sort(Comparator.comparingLong(MutableLevel::price).reversed());
        if (view == DepthBookView.MATCHED) {
            return matchedSnapshot(bids, asks);
        }
        return cumulativeSnapshot(bids, asks);
    }

    private static BookSnapshot cumulativeSnapshot(List<MutableLevel> bids, List<MutableLevel> asks) {
        List<BookSideLevel> cumulativeBids = new ArrayList<>();
        List<BookSideLevel> cumulativeAsks = new ArrayList<>();
        double bidTotal = 0.0D;
        int bidOrders = 0;
        for (MutableLevel bid : bids) {
            bidTotal += bid.quantity();
            bidOrders += bid.orders();
            cumulativeBids.add(new BookSideLevel(bid.price(), bidTotal, bidOrders));
        }
        double askTotal = 0.0D;
        int askOrders = 0;
        for (MutableLevel ask : asks) {
            askTotal += ask.quantity();
            askOrders += ask.orders();
            cumulativeAsks.add(new BookSideLevel(ask.price(), askTotal, askOrders));
        }
        return new BookSnapshot(cumulativeBids, cumulativeAsks, 0.0D);
    }

    private static BookSnapshot matchedSnapshot(List<MutableLevel> bids, List<MutableLevel> asks) {
        List<MutableLevel> remainingBids = bids.stream().map(MutableLevel::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<MutableLevel> remainingAsks = asks.stream().map(MutableLevel::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        double matched = 0.0D;
        double pruneLiquidity = ConfigRegistry.INSTANCE.market().financialMicrostructure().levelPruneLiquidity();
        while (!remainingBids.isEmpty() && !remainingAsks.isEmpty()) {
            MutableLevel bid = remainingBids.getFirst();
            MutableLevel ask = remainingAsks.getFirst();
            if (bid.price() < ask.price()) {
                break;
            }
            double trade = Math.min(bid.quantity(), ask.quantity());
            matched += trade;
            bid.consume(trade);
            ask.consume(trade);
            if (bid.quantity() <= pruneLiquidity) {
                remainingBids.removeFirst();
            }
            if (ask.quantity() <= pruneLiquidity) {
                remainingAsks.removeFirst();
            }
        }
        List<BookSideLevel> bidRows = remainingBids.stream()
                .filter(level -> level.quantity() > pruneLiquidity)
                .map(level -> new BookSideLevel(level.price(), level.quantity(), level.orders()))
                .toList();
        List<BookSideLevel> askRows = remainingAsks.stream()
                .filter(level -> level.quantity() > pruneLiquidity)
                .map(level -> new BookSideLevel(level.price(), level.quantity(), level.orders()))
                .toList();
        return new BookSnapshot(bidRows, askRows, matched);
    }

    private static BookSnapshot syntheticBook(String id, long price, DepthBookView view) {
        long base = Math.max(1L, price);
        int seed = Math.abs((id == null ? "" : id).hashCode());
        List<MutableLevel> asks = new ArrayList<>();
        List<MutableLevel> bids = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            asks.add(new MutableLevel(base + i + 1L, 5 + ((seed + i * 7) % 34), 1 + (seed + i) % 4));
            bids.add(new MutableLevel(Math.max(1L, base - i - 1L), 5 + ((seed / 3 + i * 11) % 34), 1 + (seed / 5 + i) % 4));
        }
        return view == DepthBookView.MATCHED ? matchedSnapshot(bids, asks) : cumulativeSnapshot(bids, asks);
    }

    private static void drawAlternatingBookRows(GuiGraphics graphics, BookSnapshot snapshot, int left, int top, int right, int bottom) {
        if (snapshot.asks().isEmpty() && snapshot.bids().isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "empty book", left + 6, top, 0xFF7B8493, false);
            return;
        }
        int rowHeight = 10;
        int availableRows = Math.max(1, (bottom - top) / rowHeight);
        List<BookRenderRow> rows = new ArrayList<>();
        int levels = Math.max(snapshot.asks().size(), snapshot.bids().size());
        for (int i = 0; i < levels && rows.size() < availableRows; i++) {
            if (i < snapshot.asks().size() && rows.size() < availableRows) {
                rows.add(new BookRenderRow(snapshot.asks().get(i), false));
            }
            if (i < snapshot.bids().size() && rows.size() < availableRows) {
                rows.add(new BookRenderRow(snapshot.bids().get(i), true));
            }
        }
        drawBookRowList(graphics, rows, left, top, right);
    }

    private static void drawSeparatedBookRows(GuiGraphics graphics, BookSnapshot snapshot, int left, int top, int right, int bottom) {
        if (snapshot.asks().isEmpty() && snapshot.bids().isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "empty book", left + 6, top, 0xFF7B8493, false);
            return;
        }
        int rowHeight = 10;
        int availableRows = Math.max(2, (bottom - top - 14) / rowHeight);
        int sideRows = Math.max(1, Math.min(5, availableRows / 2));
        double maxQuantity = maxQuantity(snapshot);
        int askCount = Math.min(sideRows, snapshot.asks().size());
        for (int i = 0; i < askCount; i++) {
            drawBookRow(graphics, new BookRenderRow(snapshot.asks().get(askCount - i - 1), false), left, top + i * rowHeight, right, maxQuantity);
        }
        int separatorY = top + sideRows * rowHeight + 2;
        graphics.hLine(left + 6, right - 8, separatorY, 0x66566171);
        int bidTop = separatorY + 4;
        int bidCount = Math.min(sideRows, snapshot.bids().size());
        for (int i = 0; i < bidCount; i++) {
            int y = bidTop + i * rowHeight;
            if (y > bottom - rowHeight) {
                break;
            }
            drawBookRow(graphics, new BookRenderRow(snapshot.bids().get(i), true), left, y, right, maxQuantity);
        }
    }

    private static void drawBookRowList(GuiGraphics graphics, List<BookRenderRow> rows, int left, int top, int right) {
        double maxQuantity = maxQuantity(rows);
        int rowHeight = 10;
        for (int i = 0; i < rows.size(); i++) {
            drawBookRow(graphics, rows.get(i), left, top + i * rowHeight, right, maxQuantity);
        }
    }

    private static double maxQuantity(BookSnapshot snapshot) {
        double maxQuantity = 1.0D;
        for (BookSideLevel ask : snapshot.asks()) {
            maxQuantity = Math.max(maxQuantity, ask.quantity());
        }
        for (BookSideLevel bid : snapshot.bids()) {
            maxQuantity = Math.max(maxQuantity, bid.quantity());
        }
        return maxQuantity;
    }

    private static double maxQuantity(List<BookRenderRow> rows) {
        double maxQuantity = 1.0D;
        for (BookRenderRow row : rows) {
            maxQuantity = Math.max(maxQuantity, row.level().quantity());
        }
        return maxQuantity;
    }

    private static void drawBookRow(GuiGraphics graphics, BookRenderRow row, int left, int y, int right, double maxQuantity) {
        BookSideLevel level = row.level();
        int barLeft = Math.min(right - 12, left + 84);
        int width = Math.max(1, right - barLeft - 8);
        int rowWidth = Math.max(2, (int) Math.round(width * level.quantity() / Math.max(1.0D, maxQuantity)));
        if (row.bid()) {
            graphics.fill(barLeft, y + 2, barLeft + rowWidth, y + 8, 0x6657D68D);
            graphics.drawString(MarketUiSupport.font(), "B " + level.price() + " x" + MarketUiSupport.fmt(level.quantity()), left + 6, y, 0xFFA7F3D0, false);
        } else {
            graphics.fill(right - 8 - rowWidth, y + 2, right - 8, y + 8, 0x66FF6B6B);
            graphics.drawString(MarketUiSupport.font(), "A " + level.price() + " x" + MarketUiSupport.fmt(level.quantity()), left + 6, y, 0xFFFF9E9E, false);
        }
    }

    static void drawSpotDepthBook(GuiGraphics graphics, SpotMarketRow row, String anonymousMarker, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF101822);
        graphics.drawString(MarketUiSupport.font(), "Spot book", left + 6, top + 6, 0xFF8BD3FF, false);
        if (anonymousMarker != null && !anonymousMarker.isBlank()) {
            String marker = "anon " + MarketUiSupport.trim(anonymousMarker, 12);
            graphics.drawString(MarketUiSupport.font(), marker, right - Math.min(78, MarketUiSupport.font().width(marker) + 6), top + 6, 0xFFFFD166, false);
        }
        if (row == null) {
            graphics.drawString(MarketUiSupport.font(), "empty book", left + 6, top + 22, 0xFF7B8493, false);
            return;
        }
        drawAlternatingBookRows(graphics, spotSnapshot(row), left, top + 22, right, bottom);
    }

    private static BookSnapshot spotSnapshot(SpotMarketRow row) {
        List<MutableLevel> bids = new ArrayList<>();
        List<MutableLevel> asks = new ArrayList<>();
        long base = Math.max(1L, row.price());
        long step = Math.max(1L, Math.round(base * 0.012D));
        int index = 0;
        for (var entry : row.demandBreakdown().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(7)
                .toList()) {
            bids.add(new MutableLevel(Math.max(1L, base - step * (index + 1L)), entry.getValue(), Math.max(1, (int) Math.ceil(entry.getValue() / 8.0D))));
            index++;
        }
        index = 0;
        for (var entry : row.supplyBreakdown().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(7)
                .toList()) {
            asks.add(new MutableLevel(base + step * (index + 1L), entry.getValue(), Math.max(1, (int) Math.ceil(entry.getValue() / 8.0D))));
            index++;
        }
        bids.sort(Comparator.comparingLong(MutableLevel::price).reversed());
        asks.sort(Comparator.comparingLong(MutableLevel::price));
        List<BookSideLevel> bidRows = bids.stream().map(level -> new BookSideLevel(level.price(), level.quantity(), level.orders())).toList();
        List<BookSideLevel> askRows = asks.stream().map(level -> new BookSideLevel(level.price(), level.quantity(), level.orders())).toList();
        return new BookSnapshot(bidRows, askRows, 0.0D);
    }
    static int drawBreakdown(GuiGraphics graphics, Map<String, Double> values, int left, int right, int y, int bottom) {
        if (values.isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "none", left, y, 0xFF566171, false);
            return y + MarketUiSupport.ROW_HEIGHT;
        }
        for (var entry : values.entrySet()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                break;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(entry.getKey(), 16), left, y, 0xFFD7DEE8, false);
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.fmt(entry.getValue()), right - 42, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        return y;
    }

    enum DepthBookView {
        CUMULATIVE,
        MATCHED
    }

    private record BookRenderRow(BookSideLevel level, boolean bid) {
    }

    private record BookSnapshot(List<BookSideLevel> bids, List<BookSideLevel> asks, double matchedVolume) {
    }

    private record BookSideLevel(long price, double quantity, int orders) {
    }

    private static final class MutableLevel {
        private final long price;
        private double quantity;
        private final int orders;

        private MutableLevel(long price, double quantity, int orders) {
            this.price = Math.max(1L, price);
            this.quantity = Math.max(0.0D, quantity);
            this.orders = Math.max(0, orders);
        }

        private long price() {
            return price;
        }

        private double quantity() {
            return quantity;
        }

        private int orders() {
            return orders;
        }

        private void consume(double amount) {
            quantity = Math.max(0.0D, quantity - Math.max(0.0D, amount));
        }

        private MutableLevel copy() {
            return new MutableLevel(price, quantity, orders);
        }
    }
}
