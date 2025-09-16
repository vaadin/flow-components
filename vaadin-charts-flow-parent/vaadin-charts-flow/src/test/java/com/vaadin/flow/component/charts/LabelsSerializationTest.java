/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.util.ChartSerialization;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class LabelsSerializationTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = ChartSerialization.createObjectMapper();
    }

    @Test
    public void testRotation_isParsableAsNumber_serializedAsNumber()
            throws JacksonException {
        Labels labels = new Labels();
        labels.setRotation("90");
        String json = objectMapper.writeValueAsString(labels);
        Assert.assertTrue("Rotation should be serialized as a number",
                json.contains("\"rotation\":90.0"));
    }

    @Test
    public void testRotation_isNotParsableAsNumber_serializedAsString()
            throws JacksonException {
        Labels labels = new Labels();
        labels.setRotation("auto");
        String json = objectMapper.writeValueAsString(labels);
        Assert.assertTrue("Rotation should be serialized as a string",
                json.contains("\"rotation\":\"auto\""));
    }

    @Test
    public void testRotation_isNull_notSerialized() throws JacksonException {
        Labels labels = new Labels();
        labels.setRotation((String) null);
        String json = objectMapper.writeValueAsString(labels);
        Assert.assertFalse("Rotation should not be serialized",
                json.contains("\"rotation\""));
    }
}
