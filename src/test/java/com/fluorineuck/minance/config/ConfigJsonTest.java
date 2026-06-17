package com.fluorineuck.minance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigJsonTest {
    @Test
    void bundledConfigsMatchCodecs() throws IOException {
        assertNotNull(parse("market", MarketConfig.CODEC));
        assertNotNull(parse("commodity", CommodityConfig.CODEC));
        assertNotNull(parse("finance", FinanceConfig.CODEC));
        assertNotNull(parse("company", CompanyConfig.CODEC));
        assertNotNull(parse("trading", TradingConfig.CODEC));
        assertNotNull(parse("risk", RiskConfig.CODEC));
        assertNotNull(parse("economy", EconomyConfig.CODEC));
    }

    private static <T> T parse(String name, Codec<T> codec) throws IOException {
        Path path = Path.of("src/main/resources/data/minance/config/" + name + ".json");
        JsonElement json = JsonParser.parseString(Files.readString(path));
        AtomicReference<String> error = new AtomicReference<>("");
        Optional<T> result = codec.parse(JsonOps.INSTANCE, json).resultOrPartial(error::set);
        assertTrue(result.isPresent(), () -> "Invalid " + name + ".json: " + error.get());
        return result.orElseThrow();
    }
}
