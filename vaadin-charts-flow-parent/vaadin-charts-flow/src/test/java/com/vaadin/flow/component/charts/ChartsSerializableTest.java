package com.vaadin.flow.component.charts;

import com.vaadin.flow.testutil.ClassesSerializableTest;
import elemental.json.impl.JreJsonFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class ChartsSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(
                super.getExcludedPatterns(),
                Stream.of(
                        "^((?!\\.charts\\.).)*$" /*TODO: Remove when Flow Beta13 is released*/,
                        ".*\\Serializer(Modifier)?$",
                        "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializationDelegate"
                ));
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
