package com.fluorineuck.minance.client.ui.elements;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

final class MarketDerivativeSearchElement {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 230;
    private static final int[] DAYS = {0, 1, 2, 4, 7, 14, 30, 60, 100};

    private MarketDerivativeSearchElement() {
    }

    static State open(String itemId) {
        State state = new State();
        state.open = true;
        state.itemId = itemId == null ? "" : itemId;
        state.query = prefix(state.itemId);
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
        graphics.drawString(MarketUiSupport.font(), "Derivative Search", left + 8, top + 7, 0xFFE7F0FF, false);
        graphics.drawString(MarketUiSupport.font(), "X", right - 18, top + 7, 0xFFFF9E9E, false);
        graphics.fill(left + 10, top + 34, right - 10, top + 52, 0xFF0B1118);
        graphics.drawString(MarketUiSupport.font(), state.query + (state.focused ? "_" : ""), left + 16, top + 39, 0xFFD7DEE8, false);
        graphics.drawString(MarketUiSupport.font(), "Right click commodity, then search F/O codes", left + 10, top + 58, 0xFF7B8493, false);
        int y = top + 76;
        for (String code : filteredCodes(state)) {
            if (y > bottom - 18) {
                break;
            }
            graphics.fill(left + 10, y - 2, right - 10, y + 12, 0xFF101822);
            graphics.drawString(MarketUiSupport.font(), code, left + 16, y + 1, 0xFFB7C2D0, false);
            y += 16;
        }
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
            state.focused = false;
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
        state.focused = mouseY >= top + 34 && mouseY <= top + 52;
        if (!state.focused && mouseY >= top + 74) {
            int row = (int) ((mouseY - (top + 76)) / 16);
            List<String> codes = filteredCodes(state);
            if (row >= 0 && row < codes.size()) {
                state.selectedCode = codes.get(row);
                state.open = false;
                return true;
            }
        }
        return true;
    }

    static String consumeSelectedCode(State state) {
        if (state == null || state.selectedCode == null || state.selectedCode.isBlank()) {
            return null;
        }
        String selected = state.selectedCode;
        state.selectedCode = "";
        return selected;
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

    static boolean charTyped(State state, int codePoint) {
        if (state == null || !state.open || !state.focused || Character.isISOControl(codePoint)) {
            return false;
        }
        state.query += Character.toString(codePoint);
        return true;
    }

    static boolean keyDown(State state, int keyCode) {
        if (state == null || !state.open) {
            return false;
        }
        if (keyCode == 259 && state.focused && !state.query.isEmpty()) {
            state.query = state.query.substring(0, state.query.offsetByCodePoints(state.query.length(), -1));
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && state.focused) {
            List<String> codes = filteredCodes(state);
            if (!codes.isEmpty()) {
                state.selectedCode = codes.getFirst();
                state.open = false;
                return true;
            }
        }
        if (keyCode == 256) {
            state.open = false;
            return true;
        }
        return false;
    }

    private static List<String> filteredCodes(State state) {
        List<String> codes = new ArrayList<>();
        String prefix = prefix(state.itemId);
        for (int day : DAYS) {
            codes.add("F" + prefix + "_" + String.format(java.util.Locale.ROOT, "%04d", day));
            codes.add("O" + prefix + "_" + String.format(java.util.Locale.ROOT, "%04d", day) + "_call");
            codes.add("O" + prefix + "_" + String.format(java.util.Locale.ROOT, "%04d", day) + "_put");
        }
        String q = state.query.toLowerCase(java.util.Locale.ROOT);
        return codes.stream().filter(code -> code.toLowerCase(java.util.Locale.ROOT).contains(q)).limit(8).toList();
    }

    private static String prefix(String itemId) {
        return itemId.replace(':', '_').replace('/', '_').replace(' ', '_');
    }

    private static void centerIfNeeded(State state, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        if (state.centered) {
            return;
        }
        state.left = parentLeft + Math.max(0, (parentRight - parentLeft - WIDTH) / 2);
        state.top = parentTop + Math.max(0, (parentBottom - parentTop - HEIGHT) / 2);
        state.centered = true;
    }

    static final class State {
        private boolean open;
        private boolean centered;
        private boolean dragging;
        private boolean focused = true;
        private int left;
        private int top;
        private int dragOffsetX;
        private int dragOffsetY;
        private String itemId = "";
        private String query = "";
        private String selectedCode = "";
    }
}
