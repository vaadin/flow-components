/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.Labels;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for {@link Labels#getRotation()}. Attempts to serialize the
 * rotation value as a number, otherwise falls back to writing it as a string to
 * support values like "auto".
 */
public class LabelsRotationSerializer extends ValueSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen,
            SerializationContext context) {
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
