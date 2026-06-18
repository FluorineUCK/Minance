package com.fluorineuck.minance.product.component.core;

import java.util.EnumSet;
import java.util.Set;

public record ComponentAttributeSet(Set<ComponentAttribute> attributes) {
    public ComponentAttributeSet {
        attributes = attributes == null || attributes.isEmpty() ? Set.of() : Set.copyOf(attributes);
    }

    public static ComponentAttributeSet of(ComponentAttribute first, ComponentAttribute... additional) {
        EnumSet<ComponentAttribute> values = EnumSet.noneOf(ComponentAttribute.class);
        if (first != null) {
            values.add(first);
        }
        if (additional != null) {
            for (ComponentAttribute attribute : additional) {
                if (attribute != null) {
                    values.add(attribute);
                }
            }
        }
        return new ComponentAttributeSet(values);
    }

    public static ComponentAttributeSet empty() {
        return new ComponentAttributeSet(Set.of());
    }

    public boolean contains(ComponentAttribute attribute) {
        return attributes.contains(attribute);
    }
}
