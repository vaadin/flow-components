package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.demo.examples.bar.SimpleBarChartWithScroll;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleBarChartWithScrollIT extends AbstractTBTest {
    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return SimpleBarChartWithScroll.class;
    }

    @Test
    public void Chart_ScrollStateIsVisible() {
        ChartElement chart = getChartElement();

        ButtonElement toggle = $(ButtonElement.class).first();

        toggle.click();
        // todo: check scrollbar is shown
        assertTrue(false);
    }

    @Test
    public void Chart_ScrollStateIsNotVisible() {
        ChartElement chart = getChartElement();

        ButtonElement toggle = $(ButtonElement.class).first();

        toggle.click();
        toggle.click();

        // todo: check scrollbar is not shown
        assertTrue(false);
    }
}
