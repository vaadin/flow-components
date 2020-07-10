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

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.other.GlobalOptions;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class GlobalOptionsIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return GlobalOptions.class;
    }

    @Test
    public void addChart_defaultLangUsed() {
        addChart();
        final ChartElement chart = getChartElement();
        assertAxisLabels(chart, Locale.ENGLISH);
    }

    @Test
    public void addChart_setLang_newLangUsed() {
        addChart();
        final ChartElement chart = getChartElement();
        assertAxisLabels(chart, Locale.ENGLISH);
        setLang();
        assertAxisLabels(chart, new Locale("fi"));
    }

    @Test
    public void setLang_addChart_newLangUsed() {
        setLang();
        addChart();
        final ChartElement chart = getChartElement();
        assertAxisLabels(chart, new Locale("fi"));
    }

    private void addChart() {
        findElement(By.id("add_chart")).click();
    }

    private void setLang() {
        findElement(By.id("change_lang")).click();
    }

    private void assertAxisLabels(ChartElement chart, Locale locale) {
        WebElement container = chart.$("div").id("chart");
        List<WebElement> axisLabels = container.findElements(
                By.cssSelector(".highcharts-xaxis-labels > text"));
        List<String> actual = axisLabels.stream().map(WebElement::getText)
                .collect(Collectors.toList());
        List<String> expected = Arrays
                .asList(new DateFormatSymbols(locale).getShortWeekdays());
        Assert.assertTrue(expected.containsAll(actual));
    }

}
