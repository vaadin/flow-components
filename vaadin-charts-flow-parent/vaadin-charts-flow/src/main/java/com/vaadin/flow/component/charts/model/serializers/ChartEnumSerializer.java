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
import com.vaadin.flow.component.charts.model.ChartEnum;

import java.io.IOException;

/**
 * Serializer for all classes implementing {@link ChartEnum}
 *
 */
public class ChartEnumSerializer extends JsonSerializer<ChartEnum> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ChartEnum.class, new ChartEnumSerializer());

        return module;
    }

    @Override
    public void serialize(ChartEnum value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }
}
