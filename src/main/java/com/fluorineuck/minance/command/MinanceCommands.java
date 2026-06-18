package com.fluorineuck.minance.command;

import com.fluorineuck.minance.data.MinanceSavedData;
import com.fluorineuck.minance.entity.company.CompanyFinancialReport;
import com.fluorineuck.minance.entity.company.VillageCandidate;
import com.fluorineuck.minance.entity.company.VillageCompany;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.entity.institution.FinancialInstitutionDirectory;
import com.fluorineuck.minance.entity.institution.FinancialServiceAccessPoint;
import com.fluorineuck.minance.entity.institution.FinancialServiceProviderContext;
import com.fluorineuck.minance.market.index.MarketIndexService;
import com.fluorineuck.minance.market.financial.FinancialMarketEngine;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.product.derivative.CommodityDerivativeService;
import com.fluorineuck.minance.product.equity.EquityMarketService;
import com.fluorineuck.minance.product.fund.FundService;
import com.fluorineuck.minance.product.structured.StructuredProductService;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketRow;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;

public final class MinanceCommands {
    private MinanceCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("market")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("companies").executes(context -> listCompanies(context.getSource())))
                .then(Commands.literal("equities").executes(context -> listEquities(context.getSource())))
                .then(Commands.literal("indices").executes(context -> listIndices(context.getSource())))
                .then(Commands.literal("candidates").executes(context -> listCandidates(context.getSource())))
                .then(Commands.literal("company")
                        .then(Commands.argument("company_id", StringArgumentType.string())
                                .executes(context -> showCompany(context.getSource(), StringArgumentType.getString(context, "company_id")))))
                .then(Commands.literal("commodities").executes(context -> listCommodities(context.getSource())))
                .then(Commands.literal("derivatives")
                        .executes(context -> listDerivatives(context.getSource()))
                        .then(Commands.literal("expand")
                                .then(Commands.argument("product_id", StringArgumentType.string())
                                        .executes(context -> expandDerivative(context.getSource(), StringArgumentType.getString(context, "product_id"))))))
                .then(Commands.literal("derivative")
                        .then(Commands.argument("product_id", StringArgumentType.string())
                                .executes(context -> showDerivative(context.getSource(), StringArgumentType.getString(context, "product_id")))))
                .then(Commands.literal("commodity")
                        .then(Commands.argument("item_id", StringArgumentType.string())
                                .executes(context -> showCommodity(context.getSource(), StringArgumentType.getString(context, "item_id")))))
                .then(Commands.literal("funds")
                        .executes(context -> listFunds(context.getSource()))
                        .then(Commands.literal("list").executes(context -> listFunds(context.getSource())))
                        .then(Commands.literal("create_index")
                                .then(Commands.argument("fund_id", StringArgumentType.string())
                                        .then(Commands.argument("index_id", StringArgumentType.string())
                                                .then(Commands.argument("cash", LongArgumentType.longArg(1L))
                                                        .then(Commands.argument("shares", LongArgumentType.longArg(1L))
                                                                .executes(context -> createIndexFund(context.getSource(), StringArgumentType.getString(context, "fund_id"), StringArgumentType.getString(context, "index_id"), LongArgumentType.getLong(context, "cash"), LongArgumentType.getLong(context, "shares"))))))))
                        .then(Commands.literal("company")
                                .then(Commands.literal("give")
                                        .then(Commands.argument("company_id", StringArgumentType.string())
                                                .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                                                        .executes(context -> addCompanyFunds(context.getSource(), StringArgumentType.getString(context, "company_id"), LongArgumentType.getLong(context, "amount"))))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("company_id", StringArgumentType.string())
                                                .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                                                        .executes(context -> addCompanyFunds(context.getSource(), StringArgumentType.getString(context, "company_id"), -LongArgumentType.getLong(context, "amount"))))))))
                .then(Commands.literal("structured").executes(context -> listStructured(context.getSource())))
                .then(Commands.literal("debug").executes(context -> debugMarket(context.getSource())))
                .then(Commands.literal("provider").executes(context -> showProvider(context.getSource())))
                .then(Commands.literal("ui").executes(context -> describeUi(context.getSource()))));
    }

    private static int listCompanies(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        var companies = VillageCompanyService.INSTANCE.sortedCompanies();
        source.sendSuccess(() -> Component.literal("[Minance] company stocks=" + companies.size()), false);
        companies.stream().limit(20).forEach(company -> source.sendSuccess(() -> Component.literal(
                "- " + company.name() + " [" + company.id() + "] price=" + company.sharePrice() + " shares=" + company.totalShares() + " funds=" + company.funds()
        ), false));
        return companies.size();
    }

    private static int listEquities(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        EquityMarketService.INSTANCE.syncCompanies(VillageCompanyService.INSTANCE.companies().values());
        var equities = EquityMarketService.INSTANCE.sortedAssets();
        source.sendSuccess(() -> Component.literal("[Minance] equities=" + equities.size()), false);
        equities.stream().limit(30).forEach(equity -> source.sendSuccess(() -> Component.literal(
                "- " + equity.id() + " company=" + equity.companyId() + " type=" + equity.type().getSerializedName() + " units=" + equity.units() + " price=" + equity.price() + " tradable=" + equity.tradable()
        ), false));
        return equities.size();
    }

    private static int listIndices(CommandSourceStack source) {
        var indices = MarketIndexService.INSTANCE.sortedIndices();
        source.sendSuccess(() -> Component.literal("[Minance] indices=" + indices.size()), false);
        indices.forEach(index -> source.sendSuccess(() -> Component.literal(
                "- " + index.id() + " name=" + index.name() + " price=" + index.price() + " delta=" + index.delta() + " components=" + index.componentCount()
        ), false));
        return indices.size();
    }
    private static int listCandidates(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        var candidates = VillageCompanyService.INSTANCE.sortedCandidates();
        source.sendSuccess(() -> Component.literal("[Minance] village candidates=" + candidates.size()), false);
        candidates.stream().limit(20).forEach(candidate -> source.sendSuccess(() -> Component.literal(
                "- " + candidate.id() + " capital=" + candidate.funds() + "/" + candidate.requiredCapital() + " expected_shares=" + candidate.expectedShares()
        ), false));
        return candidates.size();
    }

    private static int showCompany(CommandSourceStack source, String companyId) {
        MinanceSavedData.get(source.getServer());
        VillageCompany company = VillageCompanyService.INSTANCE.companies().get(companyId);
        if (company == null) {
            source.sendFailure(Component.literal("Unknown company stock: " + companyId));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Company Stock] " + company.name() + " [" + company.id() + "]"), false);
        source.sendSuccess(() -> Component.literal("dimension=" + company.dimension() + " bell=" + company.bellPos().toShortString()), false);
        source.sendSuccess(() -> Component.literal("funds=" + company.funds() + " shares=" + company.totalShares() + " price=" + company.sharePrice()), false);
        CompanyFinancialReport report = company.latestFinancialReport();
        if (report != null) {
            source.sendSuccess(() -> Component.literal("report nav=" + report.navPerShare() + " income=" + report.periodIncome() + " change=" + Math.round(report.reportChangeRatio() * 100.0D) + "% significant=" + report.significantChange()), false);
        }
        source.sendSuccess(() -> Component.literal("professions=" + company.professionCounts()), false);
        source.sendSuccess(() -> Component.literal("holders=" + company.shareholderShares()), false);
        return 1;
    }

    private static int listCommodities(CommandSourceStack source) {
        var rows = SpotMarketService.INSTANCE.rows().stream().sorted(Comparator.comparing(row -> row.item().toString())).toList();
        source.sendSuccess(() -> Component.literal("[Minance] commodities=" + rows.size()), false);
        rows.stream().limit(30).forEach(row -> source.sendSuccess(() -> Component.literal(
                "- " + row.item() + " price=" + row.price() + " delta=" + row.priceDelta() + " vol=" + row.volume()
        ), false));
        return rows.size();
    }

    private static int showCommodity(CommandSourceStack source, String itemId) {
        return SpotMarketService.INSTANCE.rows().stream()
                .filter(row -> row.item().toString().equals(itemId) || row.item().getPath().equals(itemId))
                .findFirst()
                .map(row -> showCommodityRow(source, row))
                .orElseGet(() -> {
                    source.sendFailure(Component.literal("Unknown commodity: " + itemId));
                    return 0;
                });
    }

    private static int showCommodityRow(CommandSourceStack source, SpotMarketRow row) {
        source.sendSuccess(() -> Component.literal("[Commodity] " + row.item()), false);
        source.sendSuccess(() -> Component.literal("price=" + row.price() + " previous=" + row.previousPrice() + " delta=" + row.priceDelta()), false);
        source.sendSuccess(() -> Component.literal("volume=" + row.volume() + " inventory=" + row.inventory() + " volatility=" + row.volatility()), false);
        source.sendSuccess(() -> Component.literal("supply=" + row.supplyBreakdown()), false);
        source.sendSuccess(() -> Component.literal("demand=" + row.demandBreakdown()), false);
        return 1;
    }

    private static int listFunds(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        FundService.INSTANCE.updateAllFunds();
        var funds = FundService.INSTANCE.sortedFunds();
        source.sendSuccess(() -> Component.literal("[Minance] funds=" + funds.size()), false);
        funds.stream().limit(30).forEach(fund -> source.sendSuccess(() -> Component.literal(
                "- " + fund.id() + " name=" + fund.name() + " strategy=" + fund.strategyTag() + " nav=" + Math.round(fund.nav()) + " share_price=" + fund.sharePrice() + " holdings=" + fund.holdings().size()
        ), false));
        return funds.size();
    }

    private static int createIndexFund(CommandSourceStack source, String fundId, String indexId, long cash, long shares) {
        MinanceSavedData data = MinanceSavedData.get(source.getServer());
        var fund = FundService.INSTANCE.createIndexTrackingFund(fundId, indexId, source.getTextName(), cash, shares);
        data.setDirty();
        source.sendSuccess(() -> Component.literal("[Minance] created tracking fund " + fund.id() + " index=" + indexId + " nav=" + Math.round(fund.nav()) + " holdings=" + fund.holdings().size()), true);
        return 1;
    }

    private static int listStructured(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        var products = StructuredProductService.INSTANCE.sortedProducts();
        source.sendSuccess(() -> Component.literal("[Minance] structured_products=" + products.size()), false);
        products.stream().limit(30).forEach(product -> source.sendSuccess(() -> Component.literal(
                "- " + product.id() + " name=" + product.name() + " beneficiaries=" + product.beneficiaries().size()
        ), false));
        return products.size();
    }
    private static int addCompanyFunds(CommandSourceStack source, String companyId, long amount) {
        MinanceSavedData data = MinanceSavedData.get(source.getServer());
        VillageCompany company = VillageCompanyService.INSTANCE.companies().get(companyId);
        if (company == null) {
            source.sendFailure(Component.literal("Unknown company stock: " + companyId));
            return 0;
        }
        company.addFunds(amount);
        EquityMarketService.INSTANCE.syncCompany(company);
        data.setDirty();
        source.sendSuccess(() -> Component.literal("[Minance] company " + company.id() + " funds=" + company.funds()), true);
        return 1;
    }

    private static int listDerivatives(CommandSourceStack source) {
        var service = CommodityDerivativeService.INSTANCE;
        source.sendSuccess(() -> Component.literal("[Minance] futures=" + service.futuresMarkets().size() + " options=" + service.optionMarkets().size()), false);
        service.futuresMarkets().values().stream().limit(20).forEach(market -> source.sendSuccess(() -> Component.literal(
                "- " + market.id() + " underlying=" + market.underlyingProductId() + " depth=" + market.depth() + " day=" + market.durationDays() + " price=" + market.price()
        ), false));
        service.optionMarkets().values().stream().limit(20).forEach(market -> source.sendSuccess(() -> Component.literal(
                "- " + market.id() + " underlying=" + market.underlyingProductId() + " depth=" + market.depth() + " day=" + market.durationDays() + " " + market.right().getSerializedName() + " strike=" + market.strikePrice() + " premium=" + market.premium()
        ), false));
        return service.futuresMarkets().size() + service.optionMarkets().size();
    }

    private static int showDerivative(CommandSourceStack source, String productId) {
        var future = CommodityDerivativeService.INSTANCE.futuresMarkets().get(productId);
        if (future != null) {
            source.sendSuccess(() -> Component.literal("[Future] " + future.id()), false);
            source.sendSuccess(() -> Component.literal("underlying=" + future.underlyingProductId() + " depth=" + future.depth() + " day=" + future.durationDays() + " price=" + future.price()), false);
            source.sendSuccess(() -> Component.literal("volume buy/sell=" + future.buyVolume() + "/" + future.sellVolume()), false);
            return 1;
        }
        var option = CommodityDerivativeService.INSTANCE.optionMarkets().get(productId);
        if (option != null) {
            source.sendSuccess(() -> Component.literal("[Option] " + option.id()), false);
            source.sendSuccess(() -> Component.literal("underlying=" + option.underlyingProductId() + " depth=" + option.depth() + " day=" + option.durationDays() + " right=" + option.right().getSerializedName()), false);
            source.sendSuccess(() -> Component.literal("strike=" + option.strikePrice() + " premium=" + option.premium() + " volume buy/sell=" + option.buyVolume() + "/" + option.sellVolume()), false);
            return 1;
        }
        source.sendFailure(Component.literal("Unknown derivative product: " + productId));
        return 0;
    }
    private static int expandDerivative(CommandSourceStack source, String productId) {
        int before = CommodityDerivativeService.INSTANCE.futuresMarkets().size() + CommodityDerivativeService.INSTANCE.optionMarkets().size();
        CommodityDerivativeService.INSTANCE.ensureDerivativeSetForDerivative(productId);
        int after = CommodityDerivativeService.INSTANCE.futuresMarkets().size() + CommodityDerivativeService.INSTANCE.optionMarkets().size();
        if (after == before) {
            source.sendFailure(Component.literal("Derivative was not expanded. Check product id or derivative_max_depth: " + productId));
            return 0;
        }
        MinanceSavedData.get(source.getServer()).setDirty();
        source.sendSuccess(() -> Component.literal("Expanded derivative product " + productId + "; created " + (after - before) + " child markets."), true);
        return after - before;
    }
    private static int debugMarket(CommandSourceStack source) {
        MinanceSavedData.get(source.getServer());
        FinancialServiceProviderContext providerContext = commandProviderContext();
        source.sendSuccess(() -> Component.literal("[Minance Debug] provider=" + providerContext.displayName() + " [" + providerContext.providerId() + "] access=" + providerContext.accessPoint()), false);
        source.sendSuccess(() -> Component.literal("[Minance Debug] spot=" + SpotMarketService.INSTANCE.assets().size() + " financial=" + FinancialMarketEngine.INSTANCE.markets().size()), false);
        SpotMarketService.INSTANCE.assets().values().stream().limit(10).forEach(asset -> source.sendSuccess(() -> Component.literal(
                "[Spot] " + asset.item() + " current=" + asset.price() + " next=" + Math.round(asset.nextPrice()) + " ref=" + Math.round(asset.referencePrice()) + " inv=" + asset.inventory() + "/" + Math.round(asset.targetInventory()) + " inflow=" + asset.inflow() + " outflow=" + asset.outflow() + " stab=" + asset.stabilizerBuyVolume() + "/" + asset.stabilizerSellVolume() + " orders=" + asset.buyOrderCount() + "/" + asset.sellOrderCount()
        ), false));
        FinancialMarketEngine.INSTANCE.markets().values().stream().limit(10).forEach(market -> source.sendSuccess(() -> Component.literal(
                "[Financial] " + market.productId() + " type=" + market.productType() + " price=" + market.currentPrice() + " bid=" + market.nearestBidLiquidity() + " ask=" + market.nearestAskLiquidity() + " support=" + market.strongestSupportPrice() + " resistance=" + market.strongestResistancePrice() + " orders=" + market.stats().generatedOrderCount() + " trades=" + market.stats().matchedTradeCount() + " imbalance=" + market.lastImbalance() + " volatility=" + market.realizedVolatility()
        ), false));
        return SpotMarketService.INSTANCE.assets().size() + FinancialMarketEngine.INSTANCE.markets().size();
    }

    private static int showProvider(CommandSourceStack source) {
        FinancialServiceProviderContext providerContext = commandProviderContext();
        source.sendSuccess(() -> Component.literal("[Minance Provider] " + providerContext.displayName() + " [" + providerContext.providerId() + "]"), false);
        source.sendSuccess(() -> Component.literal("access=" + providerContext.accessPoint() + " player_owned=" + providerContext.playerOwned()), false);
        source.sendSuccess(() -> Component.literal("roles=" + providerContext.provider().roles()), false);
        return providerContext.provider().roles().size();
    }

    private static int describeUi(CommandSourceStack source) {
        FinancialServiceProviderContext providerContext = commandProviderContext();
        source.sendSuccess(() -> Component.literal("Press M to open the Minance market dashboard. You can rebind it under Controls > Key Binds > Minance."), false);
        source.sendSuccess(() -> Component.literal("Default provider: " + providerContext.displayName() + " [" + providerContext.providerId() + "]"), false);
        return 1;
    }

    private static FinancialServiceProviderContext commandProviderContext() {
        return FinancialInstitutionDirectory.INSTANCE.defaultProviderContext(FinancialServiceAccessPoint.COMMAND);
    }
}




