/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.util.ChartSerialization;

import tools.jackson.databind.ObjectMapper;

class LabelsSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = ChartSerialization.createObjectMapper();
    }

    @Test
    void testRotation_isParsableAsNumber_serializedAsNumber() {
        Labels labels = new Labels();
        labels.setRotation("90");
        String json = objectMapper.writeValueAsString(labels);
        Assertions.assertTrue(json.contains("\"rotation\":90.0"),
                "Rotation should be serialized as a number");
    }

    @Test
    void testRotation_isNotParsableAsNumber_serializedAsString() {
        Labels labels = new Labels();
        labels.setRotation("auto");
        String json = objectMapper.writeValueAsString(labels);
        Assertions.assertTrue(json.contains("\"rotation\":\"auto\""),
                "Rotation should be serialized as a string");
    }

    @Test
    void testRotation_isNull_notSerialized() {
        Labels labels = new Labels();
        labels.setRotation((String) null);
        String json = objectMapper.writeValueAsString(labels);
        Assertions.assertFalse(json.contains("\"rotation\""),
                "Rotation should not be serialized");
    }
}
