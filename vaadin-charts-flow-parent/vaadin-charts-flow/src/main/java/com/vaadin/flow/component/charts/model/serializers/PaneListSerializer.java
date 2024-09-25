/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.component.charts.model.PaneList;

/**
 * Serializer for {@link PaneList}
 *
 */
public class PaneListSerializer extends JsonSerializer<PaneList> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(PaneList.class, new PaneListSerializer());
        return module;
    }

    @Override
    public void serialize(PaneList value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        if (value != null && value.getNumberOfPanes() == 1) {
            gen.writeObject(value.getPane(0));
        } else if (value != null) {
            gen.writeObject(value.getPanes());
        }
    }
}
