package com.vaadin.flow.component.charts.model.serializers;

/*-
 * #%L
 * Vaadin Charts for Flow
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
import com.vaadin.flow.component.charts.model.style.SolidColor;

import java.io.IOException;

/**
 * Serializer for {@link SolidColor}
 *
 */
public class SolidColorSerializer extends JsonSerializer<SolidColor> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(SolidColor.class, new SolidColorSerializer());
        return module;
    }

    @Override
    public void serialize(SolidColor value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }
}
