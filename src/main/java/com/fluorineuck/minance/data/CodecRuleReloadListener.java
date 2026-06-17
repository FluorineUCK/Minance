package com.fluorineuck.minance.data;

import com.fluorineuck.minance.Minance;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CodecRuleReloadListener<T> extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    private final Codec<T> codec;
    private final Function<T, ResourceLocation> idGetter;
    private final Consumer<Map<ResourceLocation, T>> replace;
    private final String debugName;

    public CodecRuleReloadListener(String path, String debugName, Codec<T> codec, Function<T, ResourceLocation> idGetter, Consumer<Map<ResourceLocation, T>> replace) {
        super(GSON, path);
        this.codec = codec;
        this.debugName = debugName;
        this.idGetter = idGetter;
        this.replace = replace;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, T> decoded = new LinkedHashMap<>();
        objects.forEach((fileId, json) -> codec.parse(JsonOps.INSTANCE, json).resultOrPartial(message ->
                Minance.LOGGER.warn("Failed to load {} rule {}: {}", debugName, fileId, message)
        ).ifPresent(rule -> decoded.put(idGetter.apply(rule), rule)));
        replace.accept(decoded);
        Minance.LOGGER.info("Loaded {} {} rule(s)", decoded.size(), debugName);
    }
}