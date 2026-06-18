package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.entity.company.MarketPriceBar;
import com.fluorineuck.minance.entity.company.VillageCandidate;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.market.index.MarketIndexService;
import com.fluorineuck.minance.market.index.MarketIndexState;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.fluorineuck.minance.product.component.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.equity.EquityAsset;
import com.fluorineuck.minance.product.equity.EquityMarketService;
import com.fluorineuck.minance.product.component.fund.FundService;
import com.fluorineuck.minance.product.component.fund.FundState;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@LDLRegister(name = "spot-market-panel", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketPanelElement extends UIElement {
    private static final int HEADER_HEIGHT = 48;
    private static final int PADDING = 8;
    private static final int USER_PANEL_HEIGHT = 40;
    private static final long REFRESH_COOLDOWN_MILLIS = 2_000L;

    private ProductGroup productGroup = ProductGroup.COMMODITY;
    private int selectedEquity;
    private int selectedCommodity;
    private int selectedIndex;
    private int selectedFund;
    private int equityScroll;
    private int commodityScroll;
    private int indexScroll;
    private int fundScroll;
    private int chartMode = MarketChartElement.CHART_CANDLE;
    private boolean showMa5 = true;
    private boolean showMa10 = true;
    private boolean showMa30 = true;
    private boolean showVolume = true;
    private boolean showOrderFlow = true;
    private boolean showOhlc = true;
    private boolean fullscreen;
    private long lastClickMillis;
    private ProductGroup lastClickGroup = ProductGroup.COMMODITY;
    private int lastClickRow = -1;
    private float mouseX = Float.NaN;
    private float mouseY = Float.NaN;
    private MarketProductWindowElement.State productWindow;
    private MarketDerivativeSearchElement.State derivativeSearch;
    private MarketDropdownMenuElement.State dropdownMenu;
    private long lastManualRefreshMillis;
    private String commodityAnonymousMarker = "";

    public MarketPanelElement() {
        setFocusable(true);
        addEventListener(UIEvents.MOUSE_MOVE, this::onMouseMove);
        addEventListener(UIEvents.MOUSE_DOWN, this::onMouseDown);
        addEventListener(UIEvents.MOUSE_UP, this::onMouseUp);
        addEventListener(UIEvents.MOUSE_WHEEL, this::onMouseWheel);
        addEventListener(UIEvents.CHAR_TYPED, this::onCharTyped);
        addEventListener(UIEvents.KEY_DOWN, this::onKeyDown);
    }

    @Override
    public void drawContents(GUIContext context) {
        GuiGraphics graphics = context.graphics;
        int left = Math.round(getContentX());
        int top = Math.round(getContentY());
        int right = left + Math.round(getContentWidth());
        int bottom = top + Math.round(getContentHeight());
        graphics.fill(left, top, right, bottom, 0xEF0D1117);
        drawHeader(graphics, left, top, right);

        int contentTop = top + HEADER_HEIGHT + 6;
        int mainBottom = mainContentBottom(top, bottom);
        int leftPanelRight = left + Math.max(170, (right - left) * 30 / 100);
        boolean hasRightPanel = productGroup == ProductGroup.EQUITY || productGroup == ProductGroup.COMMODITY;
        int rightPanelLeft = hasRightPanel ? right - Math.max(190, (right - left) * 28 / 100) : right;
        drawList(graphics, left + PADDING, contentTop, leftPanelRight - 6, mainBottom);
        if (productGroup == ProductGroup.COMMODITY) {
            drawCommodityWorkspace(graphics, leftPanelRight + 4, contentTop, rightPanelLeft - 4, mainBottom);
        } else {
            drawChart(graphics, leftPanelRight + 4, contentTop, rightPanelLeft - 4, mainBottom);
        }
        if (hasRightPanel) {
            drawDetail(graphics, rightPanelLeft + 6, contentTop, right - PADDING, mainBottom);
        }
        if (hasUserPanel(top, bottom)) {
            drawUserInfoPanel(graphics, left + PADDING, userPanelTop(top, bottom), right - PADDING, bottom - PADDING);
        }
        MarketProductWindowElement.draw(graphics, productWindow, left, top, right, bottom);
        MarketDerivativeSearchElement.draw(graphics, derivativeSearch, left, top, right, bottom);
        MarketDropdownMenuElement.draw(graphics, dropdownMenu, left, top, right, bottom);
    }

    private void drawHeader(GuiGraphics graphics, int left, int top, int right) {
        graphics.fill(left, top, right, top + 24, 0xFF15181D);
        graphics.drawString(MarketUiSupport.font(), "Minance", left + 8, top + 8, 0xFFE7F0FF, false);
        drawProductTabs(graphics, left, top, right);
        graphics.drawString(MarketUiSupport.font(), fullscreen ? "R" : "F", right - 50, top + 8, 0xFFB7C2D0, false);
        graphics.drawString(MarketUiSupport.font(), "X", right - 24, top + 8, 0xFFFF9E9E, false);

        graphics.fill(left, top + 24, right, top + HEADER_HEIGHT, 0xFF16202D);
        int refreshLeft = refreshButtonLeft(right);
        int refreshRight = refreshButtonRight(right);
        int statusLeft = left + 10;
        int statusRight = refreshLeft - 8;
        if (statusRight > statusLeft + 36) {
            String status = MarketUiSupport.trim(marketStatus(), Math.max(8, (statusRight - statusLeft) / 6));
            graphics.drawString(MarketUiSupport.font(), status, statusLeft, top + 33, marketStatusColor(), false);
        }
        graphics.fill(refreshLeft, top + 28, refreshRight, top + 46, canRefresh() ? 0xFF1F4B3A : 0xFF29313B);
        graphics.drawString(MarketUiSupport.font(), refreshLabel(), refreshLeft + 6, top + 33, canRefresh() ? 0xFFA7F3D0 : 0xFF7B8493, false);
    }

    private void drawProductTabs(GuiGraphics graphics, int left, int top, int right) {
        int x = productTabsLeft(left);
        for (ProductGroup group : ProductGroup.values()) {
            int width = productTabWidth(group);
            if (x + width > right - 64) {
                break;
            }
            if (group == productGroup) {
                graphics.fill(x - 5, top + 4, x + width - 4, top + 20, 0xFF24364A);
            }
            graphics.drawString(MarketUiSupport.font(), group.label, x, top + 8, group == productGroup ? 0xFF8BD3FF : 0xFF9AA4B2, false);
            x += width + 8;
        }
    }

    private static int userPanelTop(int panelTop, int panelBottom) {
        int contentTop = panelTop + HEADER_HEIGHT + 6;
        int candidate = panelBottom - PADDING - USER_PANEL_HEIGHT;
        return candidate > contentTop + 96 ? candidate : panelBottom - PADDING;
    }

    private static boolean hasUserPanel(int panelTop, int panelBottom) {
        return userPanelTop(panelTop, panelBottom) < panelBottom - PADDING;
    }

    private static int mainContentBottom(int panelTop, int panelBottom) {
        int userTop = userPanelTop(panelTop, panelBottom);
        return userTop < panelBottom - PADDING ? userTop - 6 : panelBottom - PADDING;
    }

    private void drawUserInfoPanel(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        Minecraft minecraft = Minecraft.getInstance();
        String playerName = minecraft.player == null ? "--" : minecraft.player.getName().getString();
        String position = "pos --";
        if (minecraft.player != null) {
            var pos = minecraft.player.blockPosition();
            position = "pos " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        }
        String dimension = minecraft.level == null ? "dim --" : "dim " + minecraft.level.dimension().location();
        graphics.drawString(MarketUiSupport.font(), "User " + playerName + "  " + dimension + "  " + position, left + 8, top + 7, 0xFFE7F0FF, false);
        String counts = "Companies " + VillageCompanyService.INSTANCE.companies().size()
                + "  Commodities " + SpotMarketService.INSTANCE.rows().size()
                + "  Indices " + MarketIndexService.INSTANCE.sortedIndices().size()
                + "  Funds " + FundService.INSTANCE.funds().size();
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(counts, Math.max(20, (right - left - 16) / 6)), left + 8, top + 22, 0xFF9AA4B2, false);
    }

    private void drawList(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        if (productGroup == ProductGroup.COMMODITY) {
            drawCommodityList(graphics, left, top, right, bottom);
        } else if (productGroup == ProductGroup.EQUITY) {
            drawEquityList(graphics, left, top, right, bottom);
        } else if (productGroup == ProductGroup.INDEX) {
            drawIndexList(graphics, left, top, right, bottom);
        } else {
            drawFundList(graphics, left, top, right, bottom);
        }
    }

    private void drawCommodityList(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.drawString(MarketUiSupport.font(), "Commodities", left + 6, top + 6, 0xFF8BD3FF, false);
        List<SpotMarketRow> rows = commodityRows();
        selectedCommodity = MarketUiSupport.clamp(selectedCommodity, 0, Math.max(0, rows.size() - 1));
        int visibleRows = visibleRows(top, bottom);
        commodityScroll = MarketUiSupport.clamp(commodityScroll, 0, Math.max(0, rows.size() - visibleRows));
        int y = top + 22;
        for (int view = 0; view < visibleRows && commodityScroll + view < rows.size(); view++) {
            int i = commodityScroll + view;
            SpotMarketRow row = rows.get(i);
            if (i == selectedCommodity) {
                graphics.fill(left + 2, y - 1, right - 2, y + MarketUiSupport.ROW_HEIGHT - 1, 0xFF24364A);
            }
            String name = MarketUiSupport.shortId(row.item().toString());
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trendSymbol(row) + " " + MarketUiSupport.trim(name, Math.max(10, (right - left - 68) / 6)), left + 6, y, MarketUiSupport.trendColor(row), false);
            graphics.drawString(MarketUiSupport.font(), Long.toString(row.price()), right - 54, y, MarketUiSupport.trendColor(row), false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        if (rows.isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "No commodity sources discovered", left + 6, y, 0xFF7B8493, false);
        }
    }

    private void drawEquityList(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.drawString(MarketUiSupport.font(), "Companies", left + 6, top + 6, 0xFF8BD3FF, false);
        List<EquityAsset> equities = equityRows();
        selectedEquity = MarketUiSupport.clamp(selectedEquity, 0, Math.max(0, equities.size() - 1));
        int visibleRows = visibleRows(top, bottom);
        equityScroll = MarketUiSupport.clamp(equityScroll, 0, Math.max(0, equities.size() - visibleRows));
        int y = top + 22;
        for (int view = 0; view < visibleRows && equityScroll + view < equities.size(); view++) {
            int i = equityScroll + view;
            EquityAsset equity = equities.get(i);
            VillageCompany company = VillageCompanyService.INSTANCE.companies().get(equity.companyId());
            MarketPriceBar last = company == null ? null : company.priceBars().peekLast();
            if (i == selectedEquity) {
                graphics.fill(left + 2, y - 1, right - 2, y + MarketUiSupport.ROW_HEIGHT - 1, 0xFF24364A);
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(equity.displayName(), Math.max(12, (right - left - 72) / 6)), left + 6, y, 0xFFD7DEE8, false);
            graphics.drawString(MarketUiSupport.font(), Long.toString(equity.price()), right - 52, y, MarketUiSupport.priceColor(last), false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        if (equities.isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "Village candidates", left + 6, y + 8, 0xFF7B8493, false);
            y += MarketUiSupport.ROW_HEIGHT + 12;
            for (VillageCandidate candidate : VillageCompanyService.INSTANCE.sortedCandidates()) {
                if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                    break;
                }
                String progress = candidate.funds() + "/" + candidate.requiredCapital();
                graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(candidate.id(), Math.max(10, (right - left - 76) / 6)), left + 6, y, 0xFFB7C2D0, false);
                graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(progress, 12), right - 70, y, candidate.funds() >= candidate.requiredCapital() ? 0xFFA7F3D0 : 0xFFFFD166, false);
                y += MarketUiSupport.ROW_HEIGHT;
            }
        }
    }

    private void drawIndexList(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.drawString(MarketUiSupport.font(), "Indices", left + 6, top + 6, 0xFF8BD3FF, false);
        List<MarketIndexState> indices = MarketIndexService.INSTANCE.sortedIndices();
        selectedIndex = MarketUiSupport.clamp(selectedIndex, 0, Math.max(0, indices.size() - 1));
        int visibleRows = visibleRows(top, bottom);
        indexScroll = MarketUiSupport.clamp(indexScroll, 0, Math.max(0, indices.size() - visibleRows));
        int y = top + 22;
        for (int view = 0; view < visibleRows && indexScroll + view < indices.size(); view++) {
            int i = indexScroll + view;
            MarketIndexState index = indices.get(i);
            if (i == selectedIndex) {
                graphics.fill(left + 2, y - 1, right - 2, y + MarketUiSupport.ROW_HEIGHT - 1, 0xFF24364A);
            }
            int color = index.delta() > 0 ? 0xFFA7F3D0 : index.delta() < 0 ? 0xFFFF9E9E : 0xFFB7C2D0;
            String symbol = index.delta() > 0 ? "▲" : index.delta() < 0 ? "▼" : "-";
            graphics.drawString(MarketUiSupport.font(), symbol + " " + MarketUiSupport.trim(index.name(), Math.max(10, (right - left - 68) / 6)), left + 6, y, color, false);
            graphics.drawString(MarketUiSupport.font(), Long.toString(index.price()), right - 54, y, color, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
    }

    private void drawFundList(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.drawString(MarketUiSupport.font(), "Funds", left + 6, top + 6, 0xFF8BD3FF, false);
        List<FundState> funds = FundService.INSTANCE.sortedFunds();
        selectedFund = MarketUiSupport.clamp(selectedFund, 0, Math.max(0, funds.size() - 1));
        int visibleRows = visibleRows(top, bottom);
        fundScroll = MarketUiSupport.clamp(fundScroll, 0, Math.max(0, funds.size() - visibleRows));
        int y = top + 22;
        for (int view = 0; view < visibleRows && fundScroll + view < funds.size(); view++) {
            int i = fundScroll + view;
            FundState fund = funds.get(i);
            if (i == selectedFund) {
                graphics.fill(left + 2, y - 1, right - 2, y + MarketUiSupport.ROW_HEIGHT - 1, 0xFF24364A);
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(fund.name(), Math.max(10, (right - left - 68) / 6)), left + 6, y, 0xFFD7DEE8, false);
            graphics.drawString(MarketUiSupport.font(), Long.toString(fund.sharePrice()), right - 54, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        if (funds.isEmpty()) {
            graphics.drawString(MarketUiSupport.font(), "No fund products yet", left + 6, y, 0xFF7B8493, false);
        }
    }

    private void drawCommodityWorkspace(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF0B1118);
        SpotMarketRow row = selectedCommodityRow();
        if (row == null) {
            graphics.drawString(MarketUiSupport.font(), "No commodity selected", left + 10, top + 10, 0xFF7B8493, false);
            return;
        }
        int y = top + 10;
        String name = MarketUiSupport.shortId(row.item().toString());
        graphics.drawString(MarketUiSupport.font(), "Spot Market", left + 10, y, 0xFF8BD3FF, false);
        graphics.drawString(MarketUiSupport.font(), name, right - Math.min(180, MarketUiSupport.font().width(name) + 10), y, 0xFFE7F0FF, false);
        y += 18;
        graphics.drawString(MarketUiSupport.font(), "price " + row.price() + "  prev " + row.previousPrice() + "  delta " + row.priceDelta(), left + 10, y, MarketUiSupport.trendColor(row), false);
        y += MarketUiSupport.ROW_HEIGHT + 4;
        graphics.drawString(MarketUiSupport.font(), "inventory " + row.inventory() + "  volume " + row.volume() + "  volatility " + MarketUiSupport.fmt(row.volatility()), left + 10, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 10;
        double demand = row.demandBreakdown().values().stream().mapToDouble(Double::doubleValue).sum();
        double supply = row.supplyBreakdown().values().stream().mapToDouble(Double::doubleValue).sum();
        double total = Math.max(1.0D, demand + supply);
        int barLeft = left + 10;
        int barRight = right - 10;
        int mid = barLeft + (barRight - barLeft) / 2;
        int demandWidth = (int) Math.round((barRight - barLeft) * demand / total);
        int supplyWidth = (int) Math.round((barRight - barLeft) * supply / total);
        graphics.drawString(MarketUiSupport.font(), "Demand / Supply pressure", left + 10, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.fill(barLeft, y, barRight, y + 9, 0xFF182331);
        graphics.fill(mid - Math.min(mid - barLeft, demandWidth / 2), y, mid, y + 9, 0x6657D68D);
        graphics.fill(mid, y, mid + Math.min(barRight - mid, supplyWidth / 2), y + 9, 0x66FF6B6B);
        graphics.vLine(mid, y - 1, y + 10, 0xFF566171);
        y += 18;
        graphics.drawString(MarketUiSupport.font(), "Buy side " + MarketUiSupport.fmt(demand) + "  Sell side " + MarketUiSupport.fmt(supply), left + 10, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 6;
        graphics.drawString(MarketUiSupport.font(), "No line chart for spot products; use the right panel for live counterparties and depth.", left + 10, y, 0xFF7B8493, false);
    }
    private void drawChart(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (productGroup == ProductGroup.COMMODITY) {
            MarketChartElement.drawCommodityChart(graphics, selectedCommodityRow(), left, top, right, bottom);
        } else if (productGroup == ProductGroup.EQUITY) {
            MarketChartElement.drawCompanyChart(graphics, selectedCompany(), left, top, right, bottom, chartMode, showMa5, showMa10, showMa30, showVolume, showOrderFlow, showOhlc, mouseX, mouseY);
        } else if (productGroup == ProductGroup.INDEX) {
            MarketChartElement.drawIndexChart(graphics, selectedIndex(), left, top, right, bottom, chartMode, showMa5, showMa10, showMa30, showVolume, showOrderFlow, showOhlc, mouseX, mouseY);
        } else {
            MarketChartElement.drawFundChart(graphics, selectedFund(), left, top, right, bottom);
        }
    }

    private void drawDetail(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (productGroup == ProductGroup.COMMODITY) {
            MarketProductDetailElement.drawCommodityDetail(graphics, selectedCommodityRow(), commodityAnonymousMarker, left, top, right, bottom);
            return;
        }
        MarketProductDetailElement.drawEquityDetail(graphics, selectedEquity(), selectedCompany(), left, top, right, bottom);
    }

    public void setCommodityAnonymousMarker(String marker) {
        commodityAnonymousMarker = marker == null ? "" : marker;
    }

    private void onMouseMove(UIEvent event) {
        mouseX = event.x;
        mouseY = event.y;
        if (MarketDropdownMenuElement.mouseMove(dropdownMenu, event.x, event.y)
                || MarketProductWindowElement.mouseMove(productWindow, event.x, event.y)
                || MarketDerivativeSearchElement.mouseMove(derivativeSearch, event.x, event.y)) {
            event.stopPropagation();
        }
    }

    private void onMouseUp(UIEvent event) {
        MarketProductWindowElement.mouseUp(productWindow);
        MarketDerivativeSearchElement.mouseUp(derivativeSearch);
    }

    private void onMouseWheel(UIEvent event) {
        if (dropdownMenuIsOpen()) {
            event.stopPropagation();
            return;
        }
        int left = Math.round(getContentX());
        int top = Math.round(getContentY());
        int right = left + Math.round(getContentWidth());
        int bottom = top + Math.round(getContentHeight());
        int contentTop = top + HEADER_HEIGHT + 6;
        int mainBottom = mainContentBottom(top, bottom);
        int listLeft = left + PADDING;
        int listRight = left + Math.max(170, (right - left) * 30 / 100) - 6;
        if (event.x < listLeft || event.x > listRight || event.y < contentTop + 22 || event.y > mainBottom) {
            return;
        }
        int step = (int) Math.signum(event.deltaY) * 3;
        int rows = visibleRows(contentTop, mainBottom);
        if (productGroup == ProductGroup.COMMODITY) {
            commodityScroll = MarketUiSupport.clamp(commodityScroll - step, 0, Math.max(0, commodityRows().size() - rows));
        } else if (productGroup == ProductGroup.EQUITY) {
            equityScroll = MarketUiSupport.clamp(equityScroll - step, 0, Math.max(0, equityRows().size() - rows));
        } else if (productGroup == ProductGroup.INDEX) {
            indexScroll = MarketUiSupport.clamp(indexScroll - step, 0, Math.max(0, MarketIndexService.INSTANCE.sortedIndices().size() - rows));
        } else {
            fundScroll = MarketUiSupport.clamp(fundScroll - step, 0, Math.max(0, FundService.INSTANCE.sortedFunds().size() - rows));
        }
        event.stopPropagation();
    }

    private void onMouseDown(UIEvent event) {
        MarketDropdownMenuElement.Result dropdownResult = MarketDropdownMenuElement.mouseDown(dropdownMenu, event.x, event.y);
        if (dropdownResult.handled()) {
            applyDropdownAction(dropdownResult.actionId());
            event.stopPropagation();
            return;
        }
        if (MarketDerivativeSearchElement.mouseDown(derivativeSearch, event.x, event.y)) {
            String selectedDerivative = MarketDerivativeSearchElement.consumeSelectedCode(derivativeSearch);
            if (selectedDerivative != null) {
                openDerivativeProductWindow(selectedDerivative);
            }
            event.stopPropagation();
            return;
        }
        if (MarketProductWindowElement.mouseDown(productWindow, event.x, event.y)) {
            event.stopPropagation();
            return;
        }
        if (event.button != 0 && event.button != 1) {
            return;
        }
        int left = Math.round(getContentX());
        int top = Math.round(getContentY());
        int right = left + Math.round(getContentWidth());
        int bottom = top + Math.round(getContentHeight());
        if (event.button == 0 && handleWindowButtons(event, left, top, right)) {
            event.stopPropagation();
            return;
        }
        if (event.button == 0 && handleRefreshClick(event, top, right)) {
            event.stopPropagation();
            return;
        }
        if (event.button == 0 && handleMenuClick(event, left, top)) {
            event.stopPropagation();
            return;
        }
        int contentTop = top + HEADER_HEIGHT + 6;
        int mainBottom = mainContentBottom(top, bottom);
        int listLeft = left + PADDING;
        int listRight = left + Math.max(170, (right - left) * 30 / 100) - 6;
        if (event.x >= listLeft && event.x <= listRight && event.y >= contentTop + 22 && event.y <= mainBottom) {
            int row = (int) ((event.y - (contentTop + 22)) / MarketUiSupport.ROW_HEIGHT);
            int selectedRow = selectVisibleRow(row);
            if (event.button == 1 && productGroup == ProductGroup.COMMODITY) {
                SpotMarketRow commodity = selectedCommodityRow();
                if (commodity != null) {
                    CommodityDerivativeService.INSTANCE.ensureDerivativeSet(commodity.item(), commodity.price());
                    derivativeSearch = MarketDerivativeSearchElement.open(commodity.item().toString());
                }
                event.stopPropagation();
                return;
            }
            if (event.button == 0 && isDoubleClick(selectedRow)) {
                openProductWindow();
            }
            lastClickMillis = System.currentTimeMillis();
            lastClickGroup = productGroup;
            lastClickRow = selectedRow;
            event.stopPropagation();
            return;
        }
        if (event.button == 0 && (productGroup == ProductGroup.EQUITY || productGroup == ProductGroup.INDEX)) {
            int chartLeft = left + Math.max(170, (right - left) * 30 / 100) + 14;
            int toolbarTop = top + HEADER_HEIGHT + 30;
            int hit = MarketChartElement.handleToolbarClick(event.x, event.y, chartLeft, toolbarTop);
            if (hit == 0) {
                chartMode = MarketChartElement.CHART_LINE;
                event.stopPropagation();
            } else if (hit == 1) {
                chartMode = MarketChartElement.CHART_BAR;
                event.stopPropagation();
            } else if (hit == 2) {
                chartMode = MarketChartElement.CHART_CANDLE;
                event.stopPropagation();
            } else if (hit == 3) {
                showMa5 = !showMa5;
                event.stopPropagation();
            } else if (hit == 4) {
                showMa10 = !showMa10;
                event.stopPropagation();
            } else if (hit == 5) {
                showMa30 = !showMa30;
                event.stopPropagation();
            } else if (hit == 6) {
                showVolume = !showVolume;
                event.stopPropagation();
            } else if (hit == 7) {
                showOrderFlow = !showOrderFlow;
                event.stopPropagation();
            } else if (hit == 8) {
                showOhlc = !showOhlc;
                event.stopPropagation();
            }
        }
    }

    private void onCharTyped(UIEvent event) {
        if (MarketDerivativeSearchElement.charTyped(derivativeSearch, event.codePoint)) {
            event.stopPropagation();
        }
    }

    private void onKeyDown(UIEvent event) {
        if (MarketDropdownMenuElement.keyDown(dropdownMenu, event.keyCode)
                || MarketDerivativeSearchElement.keyDown(derivativeSearch, event.keyCode)) {
            event.stopPropagation();
            return;
        }
        if (event.keyCode == 256 && productWindow != null) {
            productWindow = null;
            event.stopPropagation();
        }
    }

    private boolean handleWindowButtons(UIEvent event, int left, int top, int right) {
        if (event.y < top + 4 || event.y > top + 22) {
            return false;
        }
        if (event.x >= right - 30 && event.x <= right - 8) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (event.x >= right - 58 && event.x <= right - 36) {
            toggleFullscreen();
            return true;
        }
        return false;
    }

    private boolean handleRefreshClick(UIEvent event, int top, int right) {
        if (event.y < top + 28 || event.y > top + 46 || event.x < refreshButtonLeft(right) || event.x > refreshButtonRight(right)) {
            return false;
        }
        if (canRefresh()) {
            lastManualRefreshMillis = System.currentTimeMillis();
            EquityMarketService.INSTANCE.syncCompanies(VillageCompanyService.INSTANCE.companies().values());
            MarketIndexService.INSTANCE.updateFromSpotMarket();
            FundService.INSTANCE.updateAllFunds();
        }
        return true;
    }

    private boolean canRefresh() {
        return System.currentTimeMillis() - lastManualRefreshMillis >= REFRESH_COOLDOWN_MILLIS;
    }

    private String refreshLabel() {
        long remaining = REFRESH_COOLDOWN_MILLIS - (System.currentTimeMillis() - lastManualRefreshMillis);
        if (remaining <= 0L) {
            return "Refresh";
        }
        return Math.max(1L, (remaining + 999L) / 1000L) + "s";
    }

    private String marketStatus() {
        Minecraft minecraft = Minecraft.getInstance();
        String session = "local";
        if (minecraft.level != null) {
            long dayTime = minecraft.level.getDayTime() % 24000L;
            session = dayTime < 12000L ? "open" : "closed";
        }
        int companies = VillageCompanyService.INSTANCE.companies().size();
        int commodities = SpotMarketService.INSTANCE.rows().size();
        int indices = MarketIndexService.INSTANCE.sortedIndices().size();
        int funds = FundService.INSTANCE.funds().size();
        if (companies + commodities + indices + funds == 0) {
            return "Market: waiting for sources";
        }
        return "Market: " + session + "  C " + companies + "  S " + commodities + "  I " + indices + "  F " + funds;
    }

    private int marketStatusColor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0xFFB7C2D0;
        }
        return minecraft.level.getDayTime() % 24000L < 12000L ? 0xFFA7F3D0 : 0xFFFFD166;
    }

    private static int refreshButtonLeft(int right) {
        return right - 126;
    }

    private static int refreshButtonRight(int right) {
        return right - 64;
    }

    private void toggleFullscreen() {
        UIElement window = getParent() == null ? this : getParent();
        fullscreen = !fullscreen;
        float width = fullscreen && getModularUI() != null ? getModularUI().getScreenWidth() - 8.0F : 820.0F;
        float height = fullscreen && getModularUI() != null ? getModularUI().getScreenHeight() - 8.0F : 460.0F;
        window.layout(style -> {
            setLayoutFloat(style, "width", width);
            setLayoutFloat(style, "height", height);
            setLayoutFloat(style, "marginLeft", 0.0F);
            setLayoutFloat(style, "marginRight", 0.0F);
            setLayoutFloat(style, "marginTop", 0.0F);
            setLayoutFloat(style, "marginBottom", 0.0F);
        });
    }

    private boolean handleMenuClick(UIEvent event, int left, int top) {
        if (event.y < top + 4 || event.y > top + 22) {
            return false;
        }
        int x = productTabsLeft(left);
        for (ProductGroup group : ProductGroup.values()) {
            int width = productTabWidth(group);
            if (event.x >= x - 5 && event.x <= x + width - 4) {
                productGroup = group;
                dropdownMenu = null;
                return true;
            }
            x += width + 8;
        }
        return false;
    }

    private void openProductDropdown(int left, int top) {
        List<MarketDropdownMenuElement.Item> items = new ArrayList<>();
        for (ProductGroup group : ProductGroup.values()) {
            items.add(new MarketDropdownMenuElement.Item(group.label, group.hint, "group:" + group.name(), productGroup == group));
        }
        dropdownMenu = MarketDropdownMenuElement.open("Market layers", left, top, productMenuButtonWidth(), items);
    }

    private void applyDropdownAction(String actionId) {
        if (actionId == null || actionId.isBlank()) {
            return;
        }
        if (actionId.startsWith("group:")) {
            try {
                productGroup = ProductGroup.valueOf(actionId.substring("group:".length()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private boolean dropdownMenuIsOpen() {
        return dropdownMenu != null && MarketDropdownMenuElement.isOpen(dropdownMenu);
    }

    private static int productTabsLeft(int panelLeft) {
        return panelLeft + 76;
    }

    private static int productTabWidth(ProductGroup group) {
        return MarketUiSupport.font().width(group.label) + 14;
    }

    private static int productMenuButtonWidth() {
        return 150;
    }

    private int selectVisibleRow(int row) {
        if (productGroup == ProductGroup.COMMODITY) {
            selectedCommodity = MarketUiSupport.clamp(commodityScroll + row, 0, Math.max(0, commodityRows().size() - 1));
            return selectedCommodity;
        }
        if (productGroup == ProductGroup.EQUITY) {
            selectedEquity = MarketUiSupport.clamp(equityScroll + row, 0, Math.max(0, equityRows().size() - 1));
            return selectedEquity;
        }
        if (productGroup == ProductGroup.INDEX) {
            selectedIndex = MarketUiSupport.clamp(indexScroll + row, 0, Math.max(0, MarketIndexService.INSTANCE.sortedIndices().size() - 1));
            return selectedIndex;
        }
        selectedFund = MarketUiSupport.clamp(fundScroll + row, 0, Math.max(0, FundService.INSTANCE.sortedFunds().size() - 1));
        return selectedFund;
    }

    private boolean isDoubleClick(int selectedRow) {
        return productGroup == lastClickGroup && selectedRow == lastClickRow && System.currentTimeMillis() - lastClickMillis < 350L;
    }

    private void openProductWindow() {
        if (productGroup == ProductGroup.COMMODITY) {
            SpotMarketRow row = selectedCommodityRow();
            if (row == null) {
                return;
            }
            List<String> lines = new ArrayList<>();
            lines.add("item " + row.item());
            lines.add("inventory " + row.inventory());
            lines.add("volume " + row.volume());
            lines.add("volatility " + MarketUiSupport.fmt(row.volatility()));
            lines.add("supply sources " + row.supplyBreakdown().size());
            lines.add("demand sources " + row.demandBreakdown().size());
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.COMMODITY, MarketUiSupport.shortId(row.item().toString()), row.item().toString(), row.price(), true, lines);
        } else if (productGroup == ProductGroup.EQUITY) {
            EquityAsset equity = selectedEquity();
            VillageCompany company = selectedCompany();
            if (equity == null) {
                return;
            }
            List<String> lines = new ArrayList<>();
            lines.add("id " + equity.id());
            lines.add("type " + equity.type());
            lines.add("units " + equity.units());
            lines.add("tradable " + equity.tradable());
            if (company != null) {
                lines.add("company " + company.id());
                lines.add("funds " + company.funds());
                lines.add("shares " + company.totalShares());
                lines.add("professions " + company.professionCounts().size());
            }
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.EQUITY, equity.displayName(), equity.id(), equity.price(), equity.tradable(), lines);
        } else if (productGroup == ProductGroup.INDEX) {
            MarketIndexState index = selectedIndex();
            if (index == null) {
                return;
            }
            List<String> lines = List.of("id " + index.id(), "components " + index.componentCount(), "previous " + index.previousPrice(), "direct trading disabled", "use tracking funds");
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.INDEX, index.name(), index.id(), index.price(), false, lines);
        } else {
            FundState fund = selectedFund();
            if (fund == null) {
                return;
            }
            List<String> lines = new ArrayList<>();
            lines.add("id " + fund.id());
            lines.add("manager " + fund.manager());
            lines.add("strategy " + fund.strategyTag());
            lines.add("cash " + Math.round(fund.cash()));
            lines.add("liabilities " + Math.round(fund.liabilities()));
            lines.add("shares " + MarketUiSupport.fmt(fund.totalFundShares()));
            lines.add("holdings " + fund.holdings().size());
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.FUND, fund.name(), fund.id(), fund.sharePrice(), true, lines);
        }
    }

    private void openDerivativeProductWindow(String marketId) {
        var future = CommodityDerivativeService.INSTANCE.futuresMarkets().get(marketId);
        if (future != null) {
            List<String> lines = new ArrayList<>();
            lines.add("id " + future.id());
            lines.add("underlying " + future.underlyingProductId());
            lines.add("day " + future.durationDays() + " depth " + future.depth());
            lines.add("buy/sell " + future.buyVolume() + " / " + future.sellVolume());
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.FUTURE, future.id(), future.id(), future.price(), true, lines);
            return;
        }
        var option = CommodityDerivativeService.INSTANCE.optionMarkets().get(marketId);
        if (option != null) {
            List<String> lines = new ArrayList<>();
            lines.add("id " + option.id());
            lines.add("underlying " + option.underlyingProductId());
            lines.add("day " + option.durationDays() + " depth " + option.depth());
            lines.add("right " + option.right().getSerializedName() + " strike " + option.strikePrice());
            lines.add("buy/sell " + option.buyVolume() + " / " + option.sellVolume());
            productWindow = MarketProductWindowElement.open(MarketProductWindowElement.Kind.OPTION, option.id(), option.id(), option.premium(), true, lines);
        }
    }
    private int visibleRows(int top, int bottom) {
        return Math.max(1, (bottom - top - 24) / MarketUiSupport.ROW_HEIGHT);
    }

    private EquityAsset selectedEquity() {
        List<EquityAsset> equities = equityRows();
        if (equities.isEmpty()) {
            return null;
        }
        return equities.get(MarketUiSupport.clamp(selectedEquity, 0, equities.size() - 1));
    }

    private VillageCompany selectedCompany() {
        EquityAsset equity = selectedEquity();
        return equity == null ? null : VillageCompanyService.INSTANCE.companies().get(equity.companyId());
    }

    private SpotMarketRow selectedCommodityRow() {
        List<SpotMarketRow> rows = commodityRows();
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(MarketUiSupport.clamp(selectedCommodity, 0, rows.size() - 1));
    }

    private MarketIndexState selectedIndex() {
        List<MarketIndexState> indices = MarketIndexService.INSTANCE.sortedIndices();
        if (indices.isEmpty()) {
            return null;
        }
        return indices.get(MarketUiSupport.clamp(selectedIndex, 0, indices.size() - 1));
    }

    private FundState selectedFund() {
        List<FundState> funds = FundService.INSTANCE.sortedFunds();
        if (funds.isEmpty()) {
            return null;
        }
        return funds.get(MarketUiSupport.clamp(selectedFund, 0, funds.size() - 1));
    }

    private static List<EquityAsset> equityRows() {
        EquityMarketService.INSTANCE.syncCompanies(VillageCompanyService.INSTANCE.companies().values());
        return EquityMarketService.INSTANCE.sortedAssets();
    }

    private static List<SpotMarketRow> commodityRows() {
        return SpotMarketService.INSTANCE.rows().stream().sorted(Comparator.comparing(row -> row.item().toString())).toList();
    }

    private static void setLayoutFloat(Object style, String method, float value) {
        try {
            style.getClass().getMethod(method, float.class).invoke(style, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("LDLib layout method unavailable: " + method, exception);
        }
    }

    private enum ProductGroup {
        COMMODITY("Commodities", "spot"),
        EQUITY("Companies", "equity"),
        INDEX("Indices", "index"),
        FUND("Funds", "fund");

        private final String label;
        private final String hint;

        ProductGroup(String label, String hint) {
            this.label = label;
            this.hint = hint;
        }
    }
}
