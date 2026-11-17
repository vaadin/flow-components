/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DrilldownTest {

    @Test
    void getSeries_defaultIsEmpty() {
        Assertions.assertTrue(new Drilldown().getSeries().isEmpty());
    }

    @Test
    void addItemWithDrilldown_getSeries_returnsUnmodifiableDrilldownSeries() {
        Configuration configuration = new Configuration();
        DataSeries series = new DataSeries();
        configuration.addSeries(series);
        DataSeries drilldownSeries = new DataSeries();
        drilldownSeries.setId("drilldown");

        series.addItemWithDrilldown(new DataSeriesItem("item", 1),
                drilldownSeries);

        List<Series> result = configuration.getDrilldown().getSeries();
        Assertions.assertEquals(List.of(drilldownSeries), result);
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.add(new DataSeries()));
    }
}
