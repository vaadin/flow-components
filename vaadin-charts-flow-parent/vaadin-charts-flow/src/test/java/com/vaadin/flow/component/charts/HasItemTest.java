/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.charts.events.HasItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Node;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.TreeSeries;
import com.vaadin.flow.component.charts.model.TreeSeriesItem;
import org.junit.Assert;
import org.junit.Test;

public class HasItemTest {
    @Test
    public void getSeries() {
        Chart chart = new Chart();
        DataSeries series = new DataSeries();
        chart.getConfiguration().addSeries(series);

        HasItem hasItem = new HasItemTestImpl(chart, 0, 0);
        Series result = hasItem.getSeries();

        Assert.assertEquals(series, result);
    }

    @Test
    public void getItemWithDataSeries() {
        Chart chart = new Chart();
        DataSeriesItem item = new DataSeriesItem(5, 10);
        DataSeries series = new DataSeries(item);
        chart.getConfiguration().addSeries(series);

        HasItem hasItem = new HasItemTestImpl(chart, 0, 0);
        DataSeriesItem result = hasItem.getItem();

        Assert.assertEquals(item, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getItemWithNodeSeriesThrowsUnsupportedOperationException() {
        Chart chart = new Chart();
        Node node1 = new Node("Node1");
        Node node2 = new Node("Node2");
        NodeSeries series = new NodeSeries();
        series.add(node1, node2);
        chart.getConfiguration().addSeries(series);

        HasItem hasItem = new HasItemTestImpl(chart, 0, 0);
        hasItem.getItem();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getItemWithListSeriesThrowsUnsupportedOperationException() {
        Chart chart = new Chart();
        ListSeries series = new ListSeries(1, 2, 3);
        chart.getConfiguration().addSeries(series);

        HasItem hasItem = new HasItemTestImpl(chart, 0, 0);
        hasItem.getItem();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getItemWithTreeSeriesThrowsUnsupportedOperationException() {
        Chart chart = new Chart();
        TreeSeriesItem item = new TreeSeriesItem("1", "1");
        TreeSeries series = new TreeSeries();
        series.add(item);
        chart.getConfiguration().addSeries(series);

        HasItem hasItem = new HasItemTestImpl(chart, 0, 0);
        hasItem.getItem();
    }

    private static class HasItemTestImpl implements HasItem {

        private final Chart chart;
        private final int itemIndex;
        private final int seriesItemIndex;

        public HasItemTestImpl(Chart chart, int itemIndex,
                int seriesItemIndex) {
            this.chart = chart;
            this.itemIndex = itemIndex;
            this.seriesItemIndex = seriesItemIndex;
        }

        @Override
        public Chart getSource() {
            return this.chart;
        }

        @Override
        public String getCategory() {
            return null;
        }

        @Override
        public int getItemIndex() {
            return this.itemIndex;
        }

        @Override
        public String getItemId() {
            return null;
        }

        @Override
        public int getSeriesItemIndex() {
            return this.seriesItemIndex;
        }
    }
}
