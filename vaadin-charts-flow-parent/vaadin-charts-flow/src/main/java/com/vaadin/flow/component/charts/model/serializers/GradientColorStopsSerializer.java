package com.vaadin.flow.component.charts.model.serializers;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.component.charts.model.style.GradientColor;

import java.io.IOException;

public class GradientColorStopsSerializer
        extends JsonSerializer<GradientColor.Stop> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(GradientColor.Stop.class,
                new GradientColorStopsSerializer());

        return module;
    }

    @Override
    public void serialize(GradientColor.Stop value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartArray();
        gen.writeNumber(value.getPosition());
        gen.writeString(value.getColor().toString());
        gen.writeEndArray();
    }
}
