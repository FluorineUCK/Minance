package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

@LDLRegister(name = "market-dropdown-menu", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketDropdownMenuElement extends UIElement {
    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 20;

    @Override
    public void drawContents(GUIContext context) {
    }

    static State open(String title, int left, int top, int width, List<Item> items) {
        State state = new State();
        state.open = true;
        state.title = title == null ? "" : title;
        state.left = left;
        state.top = top;
        state.width = Math.max(130, width);
        state.items.clear();
        if (items != null) {
            state.items.addAll(items);
        }
        return state;
    }

    static void draw(GuiGraphics graphics, State state, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        if (state == null || !state.open) {
            return;
        }
        graphics.fill(parentLeft, parentTop + 24, parentRight, parentBottom, 0x66000000);
        int left = Math.max(parentLeft + 6, Math.min(state.left, parentRight - state.width - 6));
        int top = Math.max(parentTop + 24, Math.min(state.top, parentBottom - menuHeight(state) - 6));
        int right = left + state.width;
        int bottom = top + menuHeight(state);
        graphics.fill(left, top, right, bottom, 0xF016202D);
        graphics.hLine(left, right, top, 0xFF566171);
        graphics.hLine(left, right, bottom, 0xFF566171);
        graphics.vLine(left, top, bottom, 0xFF566171);
        graphics.vLine(right, top, bottom, 0xFF566171);
        graphics.fill(left, top, right, top + HEADER_HEIGHT, 0xFF1E2A38);
        graphics.drawString(MarketUiSupport.font(), state.title, left + 8, top + 7, 0xFFE7F0FF, false);
        int y = top + HEADER_HEIGHT;
        for (int i = 0; i < state.items.size(); i++) {
            Item item = state.items.get(i);
            int rowColor = i == state.hoverIndex ? 0xFF24364A : item.active ? 0xFF173042 : 0xFF101822;
            graphics.fill(left + 1, y, right - 1, y + ROW_HEIGHT, rowColor);
            int color = item.active ? 0xFF8BD3FF : 0xFFD7DEE8;
            graphics.drawString(MarketUiSupport.font(), item.label, left + 8, y + 6, color, false);
            if (!item.hint.isBlank()) {
                graphics.drawString(MarketUiSupport.font(), item.hint, right - MarketUiSupport.font().width(item.hint) - 8, y + 6, 0xFF7B8493, false);
            }
            y += ROW_HEIGHT;
        }
    }

    static boolean mouseMove(State state, float mouseX, float mouseY) {
        if (state == null || !state.open) {
            return false;
        }
        state.hoverIndex = rowAt(state, mouseX, mouseY);
        return state.hoverIndex >= 0;
    }

    static Result mouseDown(State state, float mouseX, float mouseY) {
        if (state == null || !state.open) {
            return Result.ignored();
        }
        int row = rowAt(state, mouseX, mouseY);
        if (row >= 0 && row < state.items.size()) {
            Item item = state.items.get(row);
            state.open = false;
            return Result.selected(item.actionId);
        }
        state.open = false;
        return Result.closed();
    }

    static boolean isOpen(State state) {
        return state != null && state.open;
    }

    static boolean keyDown(State state, int keyCode) {
        if (state != null && state.open && keyCode == 256) {
            state.open = false;
            return true;
        }
        return false;
    }

    private static int rowAt(State state, float mouseX, float mouseY) {
        int left = state.left;
        int top = state.top;
        int right = left + state.width;
        int bottom = top + menuHeight(state);
        if (mouseX < left || mouseX > right || mouseY < top + HEADER_HEIGHT || mouseY > bottom) {
            return -1;
        }
        return (int) ((mouseY - (top + HEADER_HEIGHT)) / ROW_HEIGHT);
    }

    private static int menuHeight(State state) {
        return HEADER_HEIGHT + state.items.size() * ROW_HEIGHT + 1;
    }

    static final class State {
        private boolean open;
        private String title = "";
        private int left;
        private int top;
        private int width;
        private int hoverIndex = -1;
        private final List<Item> items = new ArrayList<>();
    }

    static final class Item {
        private final String label;
        private final String hint;
        private final String actionId;
        private final boolean active;

        Item(String label, String hint, String actionId, boolean active) {
            this.label = label == null ? "" : label;
            this.hint = hint == null ? "" : hint;
            this.actionId = actionId == null ? "" : actionId;
            this.active = active;
        }
    }

    static final class Result {
        private final boolean handled;
        private final String actionId;

        private Result(boolean handled, String actionId) {
            this.handled = handled;
            this.actionId = actionId;
        }

        static Result ignored() {
            return new Result(false, "");
        }

        static Result closed() {
            return new Result(true, "");
        }

        static Result selected(String actionId) {
            return new Result(true, actionId);
        }

        boolean handled() {
            return handled;
        }

        String actionId() {
            return actionId;
        }
    }
}
