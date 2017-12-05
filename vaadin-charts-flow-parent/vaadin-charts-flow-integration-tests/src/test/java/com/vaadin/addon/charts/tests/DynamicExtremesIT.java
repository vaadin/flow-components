/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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
package com.vaadin.addon.charts.tests;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.examples.dynamic.DynamicExtremes;
import com.vaadin.tests.elements.ChartElement;

public class DynamicExtremesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return DynamicExtremes.class;
    }

    @Test
    public void axisFunction_toggleExtremesPoint_pointHidden() {
        ChartElement chart = getChartElement();
        int initialVisiblePointsCount = chart.getVisiblePoints().size();
        findElement(By.id("toggleExtremesButton")).click();
        assertNotEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
    }
}
