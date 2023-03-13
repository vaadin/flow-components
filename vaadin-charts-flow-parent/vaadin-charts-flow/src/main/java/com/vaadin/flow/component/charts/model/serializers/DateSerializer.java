/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.component.charts.util.Util;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

/**
 * Serializes all {@link Date} objects as UTC long.
 */
public class DateSerializer extends JsonSerializer<Date> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());

        return module;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        final Instant instantUTC = value.toInstant(); // converting to UTC
        gen.writeNumber(Util.toHighchartsTS(instantUTC));
    }

}
