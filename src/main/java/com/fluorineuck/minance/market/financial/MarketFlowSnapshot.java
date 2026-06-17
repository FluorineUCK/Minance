package com.fluorineuck.minance.market.financial;

public record MarketFlowSnapshot(
        double buyVolume,
        double sellVolume,
        double companyFlow,
        double playerFlow,
        double stabilizerFlow,
        int tradeCount
) {
    public static final MarketFlowSnapshot EMPTY = new MarketFlowSnapshot(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0);

    public MarketFlowSnapshot {
        buyVolume = Math.max(0.0D, buyVolume);
        sellVolume = Math.max(0.0D, sellVolume);
        tradeCount = Math.max(0, tradeCount);
    }

    public MarketFlowSnapshot recordBuy(double quantity, FlowActor actor) {
        if (quantity <= 0.0D) {
            return this;
        }
        return add(quantity, 0.0D, actor, quantity, 1);
    }

    public MarketFlowSnapshot recordSell(double quantity, FlowActor actor) {
        if (quantity <= 0.0D) {
            return this;
        }
        return add(0.0D, quantity, actor, -quantity, 1);
    }

    private MarketFlowSnapshot add(double buy, double sell, FlowActor actor, double signedFlow, int trades) {
        double nextCompanyFlow = companyFlow;
        double nextPlayerFlow = playerFlow;
        double nextStabilizerFlow = stabilizerFlow;
        switch (actor == null ? FlowActor.OTHER : actor) {
            case COMPANY -> nextCompanyFlow += signedFlow;
            case PLAYER -> nextPlayerFlow += signedFlow;
            case STABILIZER -> nextStabilizerFlow += signedFlow;
            case OTHER -> {
            }
        }
        return new MarketFlowSnapshot(
                buyVolume + buy,
                sellVolume + sell,
                nextCompanyFlow,
                nextPlayerFlow,
                nextStabilizerFlow,
                tradeCount + trades
        );
    }

    public enum FlowActor {
        COMPANY,
        PLAYER,
        STABILIZER,
        OTHER
    }
}
