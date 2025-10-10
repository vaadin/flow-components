/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ChartVariantTest {

    private Chart chart;

    @Before
    public void initTest() {
        chart = new Chart();
    }

    @Test
    public void addAndRemoveLumoGradientVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.GRADIENT);
        assertThemeAttribute("gradient");
        chart.removeThemeVariants(ChartVariant.GRADIENT);
        assertThemeAttribute(null);
    }

    @Test
    public void addGradientVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.GRADIENT);
        assertThemeAttribute("gradient");
    }

    @Test
    public void addMonotoneVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.MONOTONE);
        assertThemeAttribute("monotone");
    }

    @Test
    public void addClassicVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.CLASSIC);
        assertThemeAttribute("classic");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.MONOTONE);
        chart.addThemeVariants(ChartVariant.GRADIENT);
        assertThemeAttributeContains("monotone");
        assertThemeAttributeContains("gradient");
        chart.removeThemeVariants(ChartVariant.GRADIENT);
        assertThemeAttribute("monotone");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.MONOTONE);
        chart.addThemeVariants(ChartVariant.GRADIENT);
        chart.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.CLASSIC);
        chart.addThemeVariants(ChartVariant.CLASSIC);
        assertThemeAttribute("classic");
    }

    private void assertThemeAttribute(String expected) {
        String actual = chart.getThemeName();
        assertEquals("Unexpected theme attribute on chart", expected, actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = chart.getThemeName();
        assertTrue("Theme attribute not present on chart",
                actual.contains(expected));
    }
}
