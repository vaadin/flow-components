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
import com.vaadin.flow.component.charts.examples.dynamic.DynamicChanges;
import com.vaadin.tests.elements.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;

public class DynamicChangesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return DynamicChanges.class;
    }

    @Test
    public void seriesFunction_addPoint_pointCreated() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("addPointButton")).click();
        assertEquals(initialPointsCount + 1, chart.getPoints().size());
    }

    @Test
    public void pointFunction_removePoint_pointDeleted() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("removePointButton")).click();
        assertEquals(initialPointsCount - 1, chart.getPoints().size());
    }

}
