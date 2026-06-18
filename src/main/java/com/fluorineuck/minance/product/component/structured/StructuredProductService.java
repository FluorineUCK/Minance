package com.fluorineuck.minance.product.component.structured;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructuredProductService {
    public static final StructuredProductService INSTANCE = new StructuredProductService();

    private final Map<String, StructuredProductState> products = new LinkedHashMap<>();

    private StructuredProductService() {
    }

    public Map<String, StructuredProductState> products() {
        return products;
    }

    public List<StructuredProductState> sortedProducts() {
        return products.values().stream().sorted(Comparator.comparing(StructuredProductState::id)).toList();
    }

    public StructuredProductState create(String id, String name) {
        StructuredProductState state = new StructuredProductState(id, name);
        products.put(state.id(), state);
        return state;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        products.values().forEach(product -> list.add(product.save()));
        tag.put("products", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        products.clear();
        ListTag list = tag.getList("products", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            StructuredProductState state = StructuredProductState.load(list.getCompound(i));
            products.put(state.id(), state);
        }
    }
}
