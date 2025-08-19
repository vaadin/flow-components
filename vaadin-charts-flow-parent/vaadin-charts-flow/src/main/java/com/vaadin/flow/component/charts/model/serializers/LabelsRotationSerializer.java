/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vaadin.flow.component.charts.model.Labels;

/**
 * Serializer for {@link Labels#getRotation()}. Attempts to serialize the
 * rotation value as a number, otherwise falls back to writing it as a string to
 * support values like "auto".
 */
public class LabelsRotationSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen,
            SerializerProvider serializers) throws IOException {
        if (value == null) {
            return;
        }

        try {
            double d = Double.parseDouble(value);
            gen.writeNumber(d);
        } catch (NumberFormatException e) {
            gen.writeString(value);
        }
    }
}
