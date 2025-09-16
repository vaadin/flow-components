/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.AxisList;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Serializer for {@link AxisList}
 *
 */
public class AxisListSerializer extends ValueSerializer<AxisList> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(AxisList.class, new AxisListSerializer());
        return module;
    }

    @Override
    public void serialize(AxisList value, JsonGenerator gen,
            SerializationContext context) {
        if (value != null && value.getNumberOfAxes() == 1) {
            gen.writePOJO(value.getAxis(0));
        } else if (value != null) {
            gen.writePOJO(value.getAxes());
        }
    }
}
