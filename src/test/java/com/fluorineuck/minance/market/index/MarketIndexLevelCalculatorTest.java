package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.MarketConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketIndexLevelCalculatorTest {
    @Test
    void equalWeightedIndexAveragesAdjustedComponentPrices() {
        MarketConfig.IndexDefinition definition = new MarketConfig.IndexDefinition(
                "test_index",
                "Test Index",
                List.of("test"),
                MarketIndexWeightingMethod.EQUAL_WEIGHTED,
                0L,
                0L
        );
        List<MarketIndexComponent> components = List.of(
                new MarketIndexComponent("test:a", 100.0D, 0.0D, 0.0D, 0, 0, 0.0D),
                new MarketIndexComponent("test:b", 200.0D, 0.0D, 0.0D, 0, 0, 0.0D)
        );

        MarketIndexLevel level = MarketIndexLevelCalculator.INSTANCE.calculate(definition, components, MarketConfig.defaults());

        assertEquals(150L, level.price());
        assertEquals(2, level.componentCount());
    }

    @Test
    void volumeWeightedIndexUsesConfiguredWeightingMethod() {
        MarketConfig.IndexDefinition definition = new MarketConfig.IndexDefinition(
                "test_index",
                "Test Index",
                List.of("test"),
                MarketIndexWeightingMethod.VOLUME_WEIGHTED,
                0L,
                0L
        );
        List<MarketIndexComponent> components = List.of(
                new MarketIndexComponent("test:a", 100.0D, 1.0D, 1.0D, 0, 0, 0.0D),
                new MarketIndexComponent("test:b", 200.0D, 2.0D, 2.0D, 0, 0, 0.0D)
        );

        MarketIndexLevel level = MarketIndexLevelCalculator.INSTANCE.calculate(definition, components, MarketConfig.defaults());

        assertEquals(167L, level.price());
        assertEquals(6.0D, level.totalWeight());
    }

    @Test
    void zeroWeightComponentsAreExcludedWhenAnyRawWeightExists() {
        MarketConfig.IndexDefinition definition = new MarketConfig.IndexDefinition(
                "test_index",
                "Test Index",
                List.of("test"),
                MarketIndexWeightingMethod.VOLUME_WEIGHTED,
                0L,
                0L
        );
        List<MarketIndexComponent> components = List.of(
                new MarketIndexComponent("test:inactive", 100.0D, 0.0D, 0.0D, 0, 0, 0.0D),
                new MarketIndexComponent("test:active", 200.0D, 2.0D, 2.0D, 0, 0, 0.0D)
        );

        MarketIndexLevel level = MarketIndexLevelCalculator.INSTANCE.calculate(definition, components, MarketConfig.defaults());

        assertEquals(200L, level.price());
        assertEquals(4.0D, level.totalWeight());
    }
}
