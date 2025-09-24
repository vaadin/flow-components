/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.TimeUnitMultiples;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Serializer for
 * {@link com.vaadin.flow.component.charts.model.TimeUnitMultiples}.
 *
 */
public class TimeUnitMultiplesSerializer
        extends ValueSerializer<TimeUnitMultiples> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(TimeUnitMultiples.class,
                new TimeUnitMultiplesSerializer());
        return module;
    }

    @Override
    public void serialize(TimeUnitMultiples value, JsonGenerator gen,
            SerializationContext context) {
        gen.writeStartArray();
        gen.writeString(value.getTimeUnit().toString());
        if (value.getAllowedMultiples() != null) {
            gen.writeStartArray();
            for (int multiple : value.getAllowedMultiples()) {
                gen.writeNumber(multiple);
            }
            gen.writeEndArray();
        } else {
            gen.writeNull();
        }
        gen.writeEndArray();
    }
}
