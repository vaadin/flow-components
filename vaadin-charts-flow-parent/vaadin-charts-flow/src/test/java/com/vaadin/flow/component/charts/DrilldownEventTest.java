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

import com.vaadin.flow.component.charts.events.DrilldownEvent;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;

class DrilldownEventTest {

    private Chart chart;
    private DataSeries series;

    @BeforeEach
    void setup() {
        chart = new Chart();
        series = new DataSeries();
        chart.getConfiguration().addSeries(series);
    }

    @Test
    void eagerDrilldown_getDrilldownSeries_returnsDrilldownSeries() {
        DataSeries drilldownSeries = createSeries("drilldown");
        series.addItemWithDrilldown(new DataSeriesItem("item", 1),
                drilldownSeries);

        Assertions.assertSame(drilldownSeries,
                createEvent("drilldown").getDrilldownSeries());
    }

    @Test
    void nestedEagerDrilldown_getDrilldownSeries_returnsDrilldownSeriesOfEachLevel() {
        DataSeries topSeries = new DataSeries();
        DataSeries drilldownSeries = createSeries("drilldown");
        DataSeries nestedDrilldownSeries = createSeries("nested");
        drilldownSeries.addItemWithDrilldown(new DataSeriesItem("nested", 1),
                nestedDrilldownSeries);
        topSeries.addItemWithDrilldown(new DataSeriesItem("item", 1),
                drilldownSeries);
        chart.getConfiguration().addSeries(topSeries);

        Assertions.assertSame(drilldownSeries,
                createEvent("drilldown").getDrilldownSeries());
        Assertions.assertSame(nestedDrilldownSeries,
                createEvent("nested").getDrilldownSeries());
    }

    @Test
    void asyncDrilldown_getDrilldownSeries_returnsNull() {
        series.addItemWithDrilldown(new DataSeriesItem("item", 1));

        Assertions.assertNull(createEvent("true").getDrilldownSeries());
    }

    @Test
    void noDrilldownValue_getDrilldownSeries_returnsNull() {
        Assertions.assertNull(createEvent(null).getDrilldownSeries());
    }

    @Test
    @SuppressWarnings("deprecation")
    void eagerDrilldown_getDrilldown_returnsSeriesId() {
        series.addItemWithDrilldown(new DataSeriesItem("item", 1),
                createSeries("drilldown"));

        Assertions.assertEquals("drilldown",
                createEvent("drilldown").getDrilldown());
    }

    @Test
    @SuppressWarnings("deprecation")
    void asyncDrilldown_getDrilldown_returnsTrue() {
        series.addItemWithDrilldown(new DataSeriesItem("item", 1));

        Assertions.assertEquals("true", createEvent("true").getDrilldown());
    }

    private DataSeries createSeries(String id) {
        DataSeries result = new DataSeries();
        result.setId(id);
        return result;
    }

    private DrilldownEvent createEvent(String drilldown) {
        return new DrilldownEvent(chart, true, drilldown, null, null, null, 0,
                null, 0);
    }
}
