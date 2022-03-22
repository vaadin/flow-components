/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithSize;
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
