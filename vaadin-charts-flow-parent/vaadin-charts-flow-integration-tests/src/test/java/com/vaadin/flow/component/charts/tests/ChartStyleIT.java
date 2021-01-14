/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithClassName;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class ChartStyleIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return PieWithClassName.class;
    }

    @Test
    public void addClassName_showChart_classIsPresent() {
        ChartElement chart = getChartElement();
        assertTrue(chart.getClassNames().contains(PieWithClassName.CLASS_NAME));
    }
}
