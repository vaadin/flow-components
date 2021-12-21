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
import com.vaadin.flow.component.charts.examples.area.AreaChart;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

public class BasicChartIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return AreaChart.class;
    }

    @Test
    public void Chart_TitleDisplayed() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(title.getText().contains("First Chart for Flow"));
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void Chart_TitleCanBeChanged() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(title.getText().contains("First Chart for Flow"));

        final WebElement changeTitleButton = findElement(By.id("change_title"));
        changeTitleButton.click();

        final WebElement titleChanged = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(titleChanged.getText()
                .contains("First Chart for Flow - title changed"));
    }

    @Test
    public void Chart_SeriesNameIsSet() {
        final TestBenchElement chart = getChartElement();
        final WebElement series = chart.$("*")
                .attributeContains("class", "highcharts-legend-item").first();
        assertTrue(series.getText().contains("Tokyo"));
    }

}
