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
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.dynamic.DynamicExtremes;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

public class DynamicExtremesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return DynamicExtremes.class;
    }

    @Test
    public void axisFunction_toggleExtremesPoint_pointHidden() {
        ChartElement chart = getChartElement();
        WebElement toggleExtremesButton = findElement(
                By.id("toggleExtremesButton"));
        int initialVisiblePointsCount = chart.getVisiblePoints().size();
        toggleExtremesButton.click();
        assertNotEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
        toggleExtremesButton.click();
        assertEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
    }
}
