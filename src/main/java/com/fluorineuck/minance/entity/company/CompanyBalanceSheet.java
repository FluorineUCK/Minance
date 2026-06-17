package com.fluorineuck.minance.entity.company;

import net.minecraft.nbt.CompoundTag;

public record CompanyBalanceSheet(
        long cash,
        long inventoryValue,
        long productiveAssetValue,
        long financialAssetValue,
        long liabilities,
        long netAssetValue,
        long bookValuePerShare
) {
    public CompanyBalanceSheet {
        cash = Math.max(0L, cash);
        inventoryValue = Math.max(0L, inventoryValue);
        productiveAssetValue = Math.max(0L, productiveAssetValue);
        financialAssetValue = Math.max(0L, financialAssetValue);
        liabilities = Math.max(0L, liabilities);
        long assets = cash + inventoryValue + productiveAssetValue + financialAssetValue;
        netAssetValue = Math.max(0L, Math.min(Math.max(0L, netAssetValue), assets));
        bookValuePerShare = Math.max(1L, bookValuePerShare);
    }

    public long totalAssets() {
        return cash + inventoryValue + productiveAssetValue + financialAssetValue;
    }

    public long equityValue() {
        return Math.max(0L, totalAssets() - liabilities);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("cash", cash);
        tag.putLong("inventory_value", inventoryValue);
        tag.putLong("productive_asset_value", productiveAssetValue);
        tag.putLong("financial_asset_value", financialAssetValue);
        tag.putLong("liabilities", liabilities);
        tag.putLong("net_asset_value", netAssetValue);
        tag.putLong("book_value_per_share", bookValuePerShare);
        return tag;
    }

    public static CompanyBalanceSheet load(CompoundTag tag) {
        return new CompanyBalanceSheet(
                tag.getLong("cash"),
                tag.getLong("inventory_value"),
                tag.getLong("productive_asset_value"),
                tag.getLong("financial_asset_value"),
                tag.getLong("liabilities"),
                tag.getLong("net_asset_value"),
                tag.getLong("book_value_per_share")
        );
    }

    static CompanyBalanceSheet fromLegacy(long closingFunds, int totalShares) {
        int shares = Math.max(1, totalShares);
        long cash = Math.max(0L, closingFunds);
        long book = Math.max(1L, Math.round(cash / (double) shares));
        return new CompanyBalanceSheet(cash, 0L, 0L, 0L, 0L, cash, book);
    }
}
