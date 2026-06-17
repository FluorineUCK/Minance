package com.fluorineuck.minance.config;

public final class ConfigRegistry {
    public static final ConfigRegistry INSTANCE = new ConfigRegistry();

    private volatile MarketConfig market = MarketConfig.defaults();
    private volatile CommodityConfig commodity = CommodityConfig.defaults();
    private volatile FinanceConfig finance = FinanceConfig.defaults();
    private volatile CompanyConfig company = CompanyConfig.defaults();
    private volatile TradingConfig trading = TradingConfig.defaults();
    private volatile RiskConfig risk = RiskConfig.defaults();
    private volatile EconomyConfig economy = EconomyConfig.defaults();

    private ConfigRegistry() {
    }

    public MarketConfig market() {
        return market;
    }

    public CommodityConfig commodity() {
        return commodity;
    }

    public FinanceConfig finance() {
        return finance;
    }

    public CompanyConfig company() {
        return company;
    }

    public TradingConfig trading() {
        return trading;
    }

    public RiskConfig risk() {
        return risk;
    }

    public EconomyConfig economy() {
        return economy;
    }

    public synchronized void replaceAll(
            MarketConfig market,
            CommodityConfig commodity,
            FinanceConfig finance,
            CompanyConfig company,
            TradingConfig trading,
            RiskConfig risk,
            EconomyConfig economy
    ) {
        this.market = market;
        this.commodity = commodity;
        this.finance = finance;
        this.company = company;
        this.trading = trading;
        this.risk = risk;
        this.economy = economy;
    }
}
