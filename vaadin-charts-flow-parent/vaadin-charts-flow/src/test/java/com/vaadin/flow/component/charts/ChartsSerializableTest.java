/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.testutil.ClassesSerializableTest;
import elemental.json.impl.JreJsonFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class ChartsSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                ".*\\Serializer(Modifier)?$",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializationDelegate"));
    }

    @Test
    public void verifyJsonFactoryDeserialization() throws Throwable {
        final Chart chart = new Chart();
        final JreJsonFactory jsonFactory = chart.getJsonFactory();
        final Chart deserializedChart = super.serializeAndDeserialize(chart);

        Assert.assertNotNull(deserializedChart);
        Assert.assertNotNull(deserializedChart.getJsonFactory());
        Assert.assertNotEquals(chart, deserializedChart);
        Assert.assertNotEquals(jsonFactory, deserializedChart.getJsonFactory());
    }
}
