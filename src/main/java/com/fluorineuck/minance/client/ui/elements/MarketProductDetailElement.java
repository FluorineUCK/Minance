package com.fluorineuck.minance.client.ui.elements;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.entity.company.CompanyFinancialReport;
import com.fluorineuck.minance.entity.company.VillageCandidate;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.market.index.MarketIndexState;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import com.fluorineuck.minance.product.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.equity.EquityAsset;
import com.fluorineuck.minance.product.fund.FundState;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

@LDLRegister(name = "market-product-detail", group = "minance", registry = "ldlib2:ui_element", modID = Minance.MOD_ID)
public final class MarketProductDetailElement extends UIElement {
    @Override
    public void drawContents(GUIContext context) {
    }

    static void drawEquityDetail(GuiGraphics graphics, EquityAsset equity, VillageCompany company, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        int maxChars = Math.max(16, (right - left - 16) / 6);
        if (equity == null || company == null) {
            graphics.drawString(MarketUiSupport.font(), "Company Formation", left + 6, top + 6, 0xFF8BD3FF, false);
            int y = top + 24;
            for (VillageCandidate candidate : VillageCompanyService.INSTANCE.sortedCandidates()) {
                if (y > bottom - MarketUiSupport.ROW_HEIGHT * 3) {
                    break;
                }
                graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(candidate.id(), maxChars), left + 6, y, 0xFFD7DEE8, false);
                y += MarketUiSupport.ROW_HEIGHT;
                graphics.drawString(MarketUiSupport.font(), "capital " + candidate.funds() + " / " + candidate.requiredCapital(), left + 12, y, 0xFFB7C2D0, false);
                y += MarketUiSupport.ROW_HEIGHT;
                graphics.drawString(MarketUiSupport.font(), "shares " + candidate.expectedShares() + " derivative " + MarketUiSupport.fmt(candidate.derivativeDemand()), left + 12, y, 0xFF7B8493, false);
                y += MarketUiSupport.ROW_HEIGHT + 3;
            }
            return;
        }

        graphics.drawString(MarketUiSupport.font(), "Company", left + 6, top + 6, 0xFF8BD3FF, false);
        int y = top + 24;
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(company.name(), maxChars), left + 6, y, 0xFFE7F0FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(company.id(), maxChars), left + 6, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "dim " + MarketUiSupport.trim(company.dimension().toString(), Math.max(8, maxChars - 4)), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "bell " + company.bellPos().getX() + "," + company.bellPos().getY() + "," + company.bellPos().getZ(), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 3;
        graphics.drawString(MarketUiSupport.font(), "Equity", left + 6, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "type " + equity.type().getSerializedName() + " tradable " + equity.tradable(), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "price " + equity.price() + " shares " + company.totalShares(), left + 6, y, 0xFFA7F3D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "funds " + company.funds() + " members " + company.memberProfessions().size(), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "buy/sell " + company.pendingBuyVolume() + " / " + company.pendingSellVolume(), left + 6, y, company.pendingBuyVolume() >= company.pendingSellVolume() ? 0xFFA7F3D0 : 0xFFFF9E9E, false);
        y += MarketUiSupport.ROW_HEIGHT;
        CompanyFinancialReport report = company.latestFinancialReport();
        if (report != null) {
            graphics.drawString(MarketUiSupport.font(), "report NAV " + report.navPerShare() + " income " + report.periodIncome(), left + 6, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
            graphics.drawString(MarketUiSupport.font(), "change " + Math.round(report.reportChangeRatio() * 100.0D) + "% significant " + report.significantChange(), left + 6, y, report.significantChange() ? 0xFFFFD166 : 0xFF7B8493, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        y += 8;

        graphics.drawString(MarketUiSupport.font(), "Price Drivers", left + 6, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        double basis = Math.max(1.0D, company.proxyLiquidityDemand() + company.derivativeDemand() + company.pendingBuyVolume() + company.pendingSellVolume());
        y = drawBreakdownBar(graphics, left + 6, y, right - 6, "Proxy", company.proxyLiquidityDemand(), basis);
        y = drawBreakdownBar(graphics, left + 6, y, right - 6, "Deriv", company.derivativeDemand(), basis);
        y = drawBreakdownBar(graphics, left + 6, y, right - 6, "Buy", company.pendingBuyVolume(), basis);
        y = drawBreakdownBar(graphics, left + 6, y, right - 6, "Sell", -company.pendingSellVolume(), basis);
        y += 6;

        graphics.drawString(MarketUiSupport.font(), "Professions", left + 6, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        for (var entry : company.professionCounts().entrySet()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                return;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(MarketUiSupport.shortId(entry.getKey().toString()), Math.max(8, maxChars - 4)), left + 10, y, 0xFFD7DEE8, false);
            graphics.drawString(MarketUiSupport.font(), Integer.toString(entry.getValue()), right - 24, y, 0xFFB7C2D0, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        if (!company.shareholderShares().isEmpty() && y <= bottom - MarketUiSupport.ROW_HEIGHT * 2) {
            y += 4;
            graphics.drawString(MarketUiSupport.font(), "Initial Holders", left + 6, y, 0xFF8BD3FF, false);
            y += MarketUiSupport.ROW_HEIGHT;
            for (var entry : company.shareholderShares().entrySet()) {
                if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                    break;
                }
                graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(entry.getKey(), Math.max(8, maxChars - 6)), left + 10, y, 0xFFD7DEE8, false);
                graphics.drawString(MarketUiSupport.font(), Integer.toString(entry.getValue()), right - 32, y, 0xFFB7C2D0, false);
                y += MarketUiSupport.ROW_HEIGHT;
            }
        }
    }

    private static int drawBreakdownBar(GuiGraphics graphics, int left, int y, int right, String label, double value, double basis) {
        int labelWidth = 42;
        int barLeft = left + labelWidth;
        int barRight = right;
        int mid = (barLeft + barRight) / 2;
        int max = Math.max(1, (barRight - barLeft) / 2);
        int length = (int) Math.min(max, Math.abs(value) / Math.max(1.0D, basis) * max);
        graphics.drawString(MarketUiSupport.font(), label, left, y, 0xFFB7C2D0, false);
        graphics.fill(barLeft, y + 2, barRight, y + 8, 0xFF202A36);
        if (value >= 0.0D) {
            graphics.fill(mid, y + 2, mid + length, y + 8, 0xFF3FBF7F);
        } else {
            graphics.fill(mid - length, y + 2, mid, y + 8, 0xFFEF6461);
        }
        graphics.vLine(mid, y + 1, y + 9, 0xFF566171);
        return y + MarketUiSupport.ROW_HEIGHT;
    }
    static void drawCommodityDetail(GuiGraphics graphics, SpotMarketRow row, int left, int top, int right, int bottom) {
        drawCommodityDetail(graphics, row, "", left, top, right, bottom);
    }

    static void drawCommodityDetail(GuiGraphics graphics, SpotMarketRow row, String anonymousMarker, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        graphics.drawString(MarketUiSupport.font(), "Commodity", left + 6, top + 6, 0xFF8BD3FF, false);
        if (row == null) {
            return;
        }
        int y = top + 24;
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.shortId(row.item().toString()), left + 6, y, 0xFFE7F0FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "price " + row.price() + " delta " + row.priceDelta(), left + 6, y, MarketUiSupport.trendColor(row), false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "volume " + row.volume() + " inventory " + row.inventory(), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        if (anonymousMarker != null && !anonymousMarker.isBlank()) {
            graphics.drawString(MarketUiSupport.font(), "anon tag " + MarketUiSupport.trim(anonymousMarker, 18), left + 6, y, 0xFFFFD166, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
        long futureCount = CommodityDerivativeService.INSTANCE.futuresMarkets().values().stream().filter(market -> market.underlyingProductId().equals(row.item().toString())).count();
        long optionCount = CommodityDerivativeService.INSTANCE.optionMarkets().values().stream().filter(market -> market.underlyingProductId().equals(row.item().toString())).count();
        graphics.drawString(MarketUiSupport.font(), "F/O " + futureCount + " / " + optionCount + "  vol " + MarketUiSupport.fmt(row.volatility()), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 8;

        graphics.drawString(MarketUiSupport.font(), "Counterparties", left + 6, y, 0xFF8BD3FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "buyer " + MarketUiSupport.trim(dominant(row.demandBreakdown()), 18), left + 6, y, 0xFFA7F3D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "seller " + MarketUiSupport.trim(dominant(row.supplyBreakdown()), 18), left + 6, y, 0xFFFF9E9E, false);
        y += MarketUiSupport.ROW_HEIGHT;
        String bid = averageBid(row) <= 0L ? "--" : Long.toString(averageBid(row));
        String ask = averageAsk(row) <= 0L ? "--" : Long.toString(averageAsk(row));
        graphics.drawString(MarketUiSupport.font(), "avg bid " + bid + "  avg ask " + ask, left + 6, y, 0xFFD7DEE8, false);
        y += MarketUiSupport.ROW_HEIGHT + 8;

        int bookBottom = Math.min(bottom, y + 94);
        MarketOrderFlowElement.drawSpotDepthBook(graphics, row, anonymousMarker, left + 6, y, right - 6, bookBottom);
        y = bookBottom + 6;
        if (y > bottom - MarketUiSupport.ROW_HEIGHT * 3) {
            return;
        }
        graphics.drawString(MarketUiSupport.font(), "Supply", left + 6, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        y = MarketOrderFlowElement.drawBreakdown(graphics, row.supplyBreakdown(), left + 10, right, y, bottom);
        y += 4;
        if (y > bottom - MarketUiSupport.ROW_HEIGHT * 2) {
            return;
        }
        graphics.drawString(MarketUiSupport.font(), "Demand", left + 6, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        MarketOrderFlowElement.drawBreakdown(graphics, row.demandBreakdown(), left + 10, right, y, bottom);
    }

    private static String dominant(Map<String, Double> values) {
        return values.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " " + MarketUiSupport.fmt(entry.getValue()))
                .orElse("none");
    }

    private static long averageBid(SpotMarketRow row) {
        if (row.demandBreakdown().isEmpty()) {
            return 0L;
        }
        return averageQuote(row, -0.5D);
    }

    private static long averageAsk(SpotMarketRow row) {
        if (row.supplyBreakdown().isEmpty()) {
            return 0L;
        }
        return averageQuote(row, 0.5D);
    }

    private static long averageQuote(SpotMarketRow row, double spreadSide) {
        double demand = row.demandBreakdown().values().stream().mapToDouble(Double::doubleValue).sum();
        double supply = row.supplyBreakdown().values().stream().mapToDouble(Double::doubleValue).sum();
        double pressure = (demand - supply) / Math.max(1.0D, demand + supply);
        double mid = Math.max(1.0D, row.price() * (1.0D + pressure * 0.03D));
        double spread = Math.max(1.0D, row.price() * 0.01D);
        return Math.max(1L, Math.round(mid + spread * spreadSide));
    }


    static void drawFundDetail(GuiGraphics graphics, FundState fund, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        graphics.drawString(MarketUiSupport.font(), "Fund", left + 6, top + 6, 0xFF8BD3FF, false);
        if (fund == null) {
            return;
        }
        int y = top + 24;
        graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(fund.id(), 28), left + 6, y, 0xFFE7F0FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "strategy " + MarketUiSupport.trim(fund.strategyTag(), 22), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "cash " + Math.round(fund.cash()) + " liabilities " + Math.round(fund.liabilities()), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "NAV " + Math.round(fund.nav()) + " share " + fund.sharePrice(), left + 6, y, 0xFFA7F3D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 8;
        graphics.drawString(MarketUiSupport.font(), "Purchased products", left + 6, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        for (var holding : fund.holdings()) {
            if (y > bottom - MarketUiSupport.ROW_HEIGHT) {
                break;
            }
            graphics.drawString(MarketUiSupport.font(), MarketUiSupport.trim(holding.productId(), 20), left + 10, y, 0xFFD7DEE8, false);
            graphics.drawString(MarketUiSupport.font(), Math.round(holding.marketValue()) + "", right - 48, y, holding.unrealizedPnl() >= 0.0D ? 0xFFA7F3D0 : 0xFFFF9E9E, false);
            y += MarketUiSupport.ROW_HEIGHT;
        }
    }
    static void drawIndexDetail(GuiGraphics graphics, MarketIndexState index, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF111820);
        graphics.drawString(MarketUiSupport.font(), "Index", left + 6, top + 6, 0xFF8BD3FF, false);
        if (index == null) {
            return;
        }
        int y = top + 24;
        graphics.drawString(MarketUiSupport.font(), index.name(), left + 6, y, 0xFFE7F0FF, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "id " + index.id(), left + 6, y, 0xFF7B8493, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "price " + index.price() + " delta " + index.delta(), left + 6, y, index.delta() > 0 ? 0xFFA7F3D0 : index.delta() < 0 ? 0xFFFF9E9E : 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT;
        graphics.drawString(MarketUiSupport.font(), "components " + index.componentCount(), left + 6, y, 0xFFB7C2D0, false);
        y += MarketUiSupport.ROW_HEIGHT + 8;
        graphics.drawString(MarketUiSupport.font(), "Index is not tradable", left + 6, y, 0xFFFFD166, false);
    }
}

