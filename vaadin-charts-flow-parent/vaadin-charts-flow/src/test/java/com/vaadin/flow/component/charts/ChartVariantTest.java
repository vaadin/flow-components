/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
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
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("gradient");
        chart.removeThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute(null);
    }

    @Test
    public void addLumoGradientVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("gradient");
    }

    @Test
    public void addLumoMonotoneVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        assertThemeAttribute("monotone");
    }

    @Test
    public void addLumoClassicVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
        assertThemeAttribute("classic");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttributeContains("monotone");
        assertThemeAttributeContains("gradient");
        chart.removeThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("monotone");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        chart.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
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
