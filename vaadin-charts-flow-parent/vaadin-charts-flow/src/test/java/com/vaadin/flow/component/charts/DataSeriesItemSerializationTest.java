/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeriesItemSankey;

/**
 * Tests for the serialization of custom data series items extending
 * {@link DataSeriesItem}
 */
class DataSeriesItemSerializationTest {
    @Test
    void dataSeriesItemSankey_empty_toJSON() {
        var json = toJSON(new DataSeriesItemSankey());
        Assertions.assertEquals("{}", json);
    }

    @Test
    void dataSeriesItemSankey_withValues_toJSON() {
        var item = new DataSeriesItemSankey("A", "B", 1);
        var json = toJSON(item);
        Assertions.assertEquals("{\"from\":\"A\",\"to\":\"B\",\"weight\":1}",
                json);
    }
}
