/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import java.time.Instant;

import com.vaadin.flow.component.charts.util.Util;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Serializes all {@link java.time.Instant} objects as UTC long.
 *
 */
public class InstantSerializer extends ValueSerializer<Instant> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantSerializer());

        return module;
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen,
            SerializationContext context) {
        gen.writeNumber(Util.toHighchartsTS(value));
    }
}
