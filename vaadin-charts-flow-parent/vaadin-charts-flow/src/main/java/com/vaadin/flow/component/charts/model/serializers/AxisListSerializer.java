/**
 * Copyright (C) 2000-2023 Vaadin Ltd
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
import com.vaadin.flow.component.charts.model.AxisList;

import java.io.IOException;

/**
 * Serializer for {@link AxisList}
 *
 */
public class AxisListSerializer extends JsonSerializer<AxisList> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(AxisList.class, new AxisListSerializer());
        return module;
    }

    @Override
    public void serialize(AxisList value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        if (value != null && value.getNumberOfAxes() == 1) {
            gen.writeObject(value.getAxis(0));
        } else if (value != null) {
            gen.writeObject(value.getAxes());
        }
    }
}
