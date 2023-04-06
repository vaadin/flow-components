package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.demo.examples.pie.PieWithClassName;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class ChartStyleIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return PieWithClassName.class;
    }

    @Test
    public void addClassName_showChart_classIsPresent() {
        ChartElement chart = getChartElement();
        assertTrue(chart.getClassNames().contains(PieWithClassName.CLASS_NAME));
    }
}
