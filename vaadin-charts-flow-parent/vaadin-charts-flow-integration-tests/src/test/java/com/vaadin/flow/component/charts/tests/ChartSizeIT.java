package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.demo.examples.pie.PieWithSize;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class ChartSizeIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return PieWithSize.class;
    }

    @Test
    public void setSize_showChart_DimentionsAreSet() {
        ChartElement chart = getChartElement();
        assertEquals(500, chart.getSize().getHeight());
        assertEquals(400, chart.getSize().getWidth());
    }
}
