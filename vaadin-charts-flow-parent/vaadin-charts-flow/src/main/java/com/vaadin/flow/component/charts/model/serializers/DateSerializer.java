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
import java.util.Date;

import com.vaadin.flow.component.charts.util.Util;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Serializes all {@link Date} objects as UTC long.
 */
public class DateSerializer extends ValueSerializer<Date> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());

        return module;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen,
            SerializationContext context) {
        final Instant instantUTC = value.toInstant(); // converting to UTC
        gen.writeNumber(Util.toHighchartsTS(instantUTC));
    }

}
