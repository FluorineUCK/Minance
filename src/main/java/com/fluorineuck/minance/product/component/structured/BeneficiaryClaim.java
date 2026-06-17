package com.fluorineuck.minance.product.structured;

import net.minecraft.nbt.CompoundTag;

public record BeneficiaryClaim(
        String beneficiaryId,
        double units,
        double priorityWeight,
        double promisedPayout
) {
    public BeneficiaryClaim {
        beneficiaryId = beneficiaryId == null ? "" : beneficiaryId;
        units = Math.max(0.0D, units);
        priorityWeight = Math.max(0.0D, priorityWeight);
        promisedPayout = Math.max(0.0D, promisedPayout);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("beneficiary_id", beneficiaryId);
        tag.putDouble("units", units);
        tag.putDouble("priority_weight", priorityWeight);
        tag.putDouble("promised_payout", promisedPayout);
        return tag;
    }

    public static BeneficiaryClaim load(CompoundTag tag) {
        return new BeneficiaryClaim(tag.getString("beneficiary_id"), tag.getDouble("units"), tag.getDouble("priority_weight"), tag.getDouble("promised_payout"));
    }
}
