package com.fluorineuck.minance.product.component.structured;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class StructuredProductState {
    private final String id;
    private final String name;
    private final List<BeneficiaryClaim> beneficiaries = new ArrayList<>();

    public StructuredProductState(String id, String name) {
        this.id = id == null ? "" : id;
        this.name = name == null || name.isBlank() ? this.id : name;
    }

    public String id() { return id; }
    public String name() { return name; }
    public List<BeneficiaryClaim> beneficiaries() { return beneficiaries; }

    public void addBeneficiary(BeneficiaryClaim claim) {
        beneficiaries.add(claim);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        ListTag list = new ListTag();
        beneficiaries.forEach(claim -> list.add(claim.save()));
        tag.put("beneficiaries", list);
        return tag;
    }

    public static StructuredProductState load(CompoundTag tag) {
        StructuredProductState state = new StructuredProductState(tag.getString("id"), tag.getString("name"));
        ListTag list = tag.getList("beneficiaries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            state.beneficiaries.add(BeneficiaryClaim.load(list.getCompound(i)));
        }
        return state;
    }
}
