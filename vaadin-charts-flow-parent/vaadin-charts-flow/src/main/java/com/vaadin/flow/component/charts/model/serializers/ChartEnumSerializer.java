/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.ChartEnum;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Serializer for all classes implementing {@link ChartEnum}
 *
 */
public class ChartEnumSerializer extends ValueSerializer<ChartEnum> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ChartEnum.class, new ChartEnumSerializer());

        return module;
    }

    @Override
    public void serialize(ChartEnum value, JsonGenerator gen,
            SerializationContext context) {
        gen.writeString(value.toString());
    }
}
