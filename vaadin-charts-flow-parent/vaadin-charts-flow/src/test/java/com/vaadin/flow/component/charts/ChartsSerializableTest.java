package com.vaadin.flow.component.charts;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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
