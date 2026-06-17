package com.fluorineuck.minance.product.equity;

public record EquityAsset(
        String id,
        String companyId,
        String displayName,
        EquityAssetType type,
        int units,
        long price,
        boolean tradable
) {
    public EquityAsset {
        id = id == null ? "" : id;
        companyId = companyId == null ? "" : companyId;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        type = type == null ? EquityAssetType.COMMON_STOCK : type;
        units = Math.max(0, units);
        price = Math.max(1L, price);
    }
}
