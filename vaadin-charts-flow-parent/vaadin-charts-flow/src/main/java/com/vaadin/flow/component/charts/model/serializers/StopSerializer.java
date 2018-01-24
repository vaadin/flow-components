package com.vaadin.flow.component.charts.model.serializers;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.component.charts.model.Stop;

import java.io.IOException;

/**
 * Serializer for {@link com.vaadin.flow.component.charts.model.Stop}.
 *
 */
public class StopSerializer extends JsonSerializer<Stop> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Stop.class, new StopSerializer());
        return module;
    }

    @Override
    public void serialize(Stop value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartArray();
        gen.writeNumber(value.getPosition());
        gen.writeString(value.getColor().toString());
        gen.writeEndArray();
    }
}
