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
import com.vaadin.flow.component.charts.model.TimeUnitMultiples;

import java.io.IOException;

/**
 * Serializer for
 * {@link com.vaadin.flow.component.charts.model.TimeUnitMultiples}.
 *
 */
public class TimeUnitMultiplesSerializer
        extends JsonSerializer<TimeUnitMultiples> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(TimeUnitMultiples.class,
                new TimeUnitMultiplesSerializer());
        return module;
    }

    @Override
    public void serialize(TimeUnitMultiples value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
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
