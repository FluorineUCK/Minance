package com.fluorineuck.minance.client.ui.elements;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

final class MarketProductWindowElement {
    private static final int WIDTH = 430;
    private static final int HEIGHT = 270;

    private MarketProductWindowElement() {
    }

    static State open(Kind kind, String title, String subtitle, long price, boolean tradable, List<String> lines) {
        State state = new State();
        state.open = true;
        state.kind = kind;
        state.title = title == null || title.isBlank() ? kind.label : title;
        state.subtitle = subtitle == null ? "" : subtitle;
        state.bookProductId = state.subtitle;
        state.price = Math.max(1L, price);
        state.limitPrice = state.price;
        state.tradable = tradable;
        state.lines.clear();
        if (lines != null) {
            state.lines.addAll(lines);
        }
        state.centered = false;
        return state;
    }

    static void draw(GuiGraphics graphics, State state, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        if (state == null || !state.open) {
            return;
        }
        centerIfNeeded(state, parentLeft, parentTop, parentRight, parentBottom);
        int left = state.left;
        int top = state.top;
        int right = left + WIDTH;
        int bottom = top + HEIGHT;
        graphics.fill(left, top, right, bottom, 0xF016202D);
        graphics.fill(left, top, right, top + 22, 0xFF1E2A38);
        graphics.drawString(MarketUiSupport.font(), state.kind.label + " - " + MarketUiSupport.trim(state.title, 40), left + 8, top + 7, 0xFFE7F0FF, false);
        graphics.drawString(MarketUiSupport.font(), "X", right - 18, top + 7, 0xFFFF9E9E, false);

        int y = top + 30;
        graphics.drawString(MarketUiSupport.font(), state.subtitle, left + 10, y, 0xFF8BD3FF, false);
        graphics.drawString(MarketUiSupport.font(), "Last " + state.price, right - 78, y, 0xFFE7F0FF, false);
        y += 16;

        int detailsRight = left + 208;
        graphics.fill(left + 8, y, detailsRight, bottom - 12, 0xFF101822);
        int detailY = y + 8;
        for (String line : state.lines) {
            if (detailY > bottom - 22) {
                break;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(line, 30), left + 14, detailY, 0xFFB7C2D0, false);
            detailY += MarketUiSupport.ROW_HEIGHT;
        }

        int bookLeft = detailsRight + 8;
        int bookTop = y;
        MarketOrderFlowElement.drawDepthBook(graphics, state.bookProductId, state.title, state.price, bookLeft, bookTop, right - 8, bookTop + 96, state.depthBookView);
        drawOrderTicket(graphics, state, bookLeft, bookTop + 106, right - 8, bottom - 12);
    }

    static boolean mouseDown(State state, float mouseX, float mouseY) {
        if (state == null || !state.open) {
            return false;
        }
        int left = state.left;
        int top = state.top;
        int right = left + WIDTH;
        int bottom = top + HEIGHT;
        if (mouseX < left || mouseX > right || mouseY < top || mouseY > bottom) {
            return false;
        }
        if (mouseY <= top + 22) {
            if (mouseX >= right - 26 && mouseX <= right - 6) {
                state.open = false;
                return true;
            }
            state.dragging = true;
            state.dragOffsetX = Math.round(mouseX) - left;
            state.dragOffsetY = Math.round(mouseY) - top;
            return true;
        }
        int bookLeft = left + 216;
        int bookTop = top + 46;
        MarketOrderFlowElement.DepthBookView view = MarketOrderFlowElement.hitDepthBookSelector(mouseX, mouseY, bookLeft, bookTop, right - 8);
        if (view != null) {
            state.depthBookView = view;
            return true;
        }
        int ticketTop = top + 152;
        if (button(mouseX, mouseY, left + 224, ticketTop + 16, 42, 16)) {
            state.buySide = true;
            return true;
        }
        if (button(mouseX, mouseY, left + 270, ticketTop + 16, 42, 16)) {
            state.buySide = false;
            return true;
        }
        if (button(mouseX, mouseY, left + 316, ticketTop + 16, 48, 16)) {
            state.marketOrder = true;
            return true;
        }
        if (button(mouseX, mouseY, left + 368, ticketTop + 16, 48, 16)) {
            state.marketOrder = false;
            return true;
        }
        if (button(mouseX, mouseY, left + 224, ticketTop + 42, 26, 16)) {
            state.quantity = Math.max(1, state.quantity - 1);
            return true;
        }
        if (button(mouseX, mouseY, left + 286, ticketTop + 42, 26, 16)) {
            state.quantity = Math.min(999, state.quantity + 1);
            return true;
        }
        if (button(mouseX, mouseY, left + 324, ticketTop + 42, 26, 16)) {
            state.limitPrice = Math.max(1L, state.limitPrice - 1L);
            return true;
        }
        if (button(mouseX, mouseY, left + 390, ticketTop + 42, 26, 16)) {
            state.limitPrice = Math.max(1L, state.limitPrice + 1L);
            return true;
        }
        if (button(mouseX, mouseY, left + 224, ticketTop + 68, 192, 18)) {
            state.lastMessage = state.tradable ? "ticket staged locally" : "index is not directly tradable";
            return true;
        }
        return true;
    }

    static boolean mouseMove(State state, float mouseX, float mouseY) {
        if (state == null || !state.open || !state.dragging) {
            return false;
        }
        state.left = Math.round(mouseX) - state.dragOffsetX;
        state.top = Math.round(mouseY) - state.dragOffsetY;
        return true;
    }

    static void mouseUp(State state) {
        if (state != null) {
            state.dragging = false;
        }
    }

    private static void drawOrderTicket(GuiGraphics graphics, State state, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF101822);
        graphics.drawString(MarketUiSupport.font(), "Order ticket", left + 6, top + 6, 0xFF8BD3FF, false);
        drawButton(graphics, left + 6, top + 18, 42, 16, "Buy", state.buySide);
        drawButton(graphics, left + 52, top + 18, 42, 16, "Sell", !state.buySide);
        drawButton(graphics, left + 98, top + 18, 48, 16, "Mkt", state.marketOrder);
        drawButton(graphics, left + 150, top + 18, 48, 16, "Limit", !state.marketOrder);
        drawButton(graphics, left + 6, top + 44, 26, 16, "-", false);
        graphics.drawString(MarketUiSupport.font(), "Qty " + state.quantity, left + 38, top + 48, 0xFFD7DEE8, false);
        drawButton(graphics, left + 68, top + 44, 26, 16, "+", false);
        drawButton(graphics, left + 106, top + 44, 26, 16, "-", false);
        graphics.drawString(MarketUiSupport.font(), "Px " + state.limitPrice, left + 138, top + 48, 0xFFD7DEE8, false);
        drawButton(graphics, right - 34, top + 44, 26, 16, "+", false);
        drawButton(graphics, left + 6, top + 70, right - left - 12, 18, "Submit", false);
        if (!state.lastMessage.isBlank()) {
            graphics.drawString(MarketUiSupport.font(), state.lastMessage, left + 6, bottom - 12, 0xFF7B8493, false);
        }
    }

    private static void drawButton(GuiGraphics graphics, int left, int top, int width, int height, String label, boolean active) {
        graphics.fill(left, top, left + width, top + height, active ? 0xFF234C63 : 0xFF182331);
        graphics.drawString(MarketUiSupport.font(), label, left + 5, top + 5, active ? 0xFF8BD3FF : 0xFFB7C2D0, false);
    }

    private static boolean button(float x, float y, int left, int top, int width, int height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }

    private static void centerIfNeeded(State state, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        if (state.centered) {
            return;
        }
        state.left = parentLeft + Math.max(0, (parentRight - parentLeft - WIDTH) / 2);
        state.top = parentTop + Math.max(0, (parentBottom - parentTop - HEIGHT) / 2);
        state.centered = true;
    }

    enum Kind {
        EQUITY("Equity"),
        COMMODITY("Commodity"),
        INDEX("Index"),
        FUND("Fund"),
        FUTURE("Future"),
        OPTION("Option");

        private final String label;

        Kind(String label) {
            this.label = label;
        }
    }

    static final class State {
        private boolean open;
        private boolean centered;
        private boolean dragging;
        private int left;
        private int top;
        private int dragOffsetX;
        private int dragOffsetY;
        private Kind kind = Kind.EQUITY;
        private String title = "";
        private String subtitle = "";
        private long price = 1L;
        private boolean tradable = true;
        private boolean buySide = true;
        private boolean marketOrder = true;
        private int quantity = 1;
        private long limitPrice = 1L;
        private String bookProductId = "";
        private MarketOrderFlowElement.DepthBookView depthBookView = MarketOrderFlowElement.DepthBookView.CUMULATIVE;
        private String lastMessage = "";
        private final List<String> lines = new ArrayList<>();
    }
}
