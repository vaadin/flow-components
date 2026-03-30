/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChartVariantTest {

    private Chart chart;

    @BeforeEach
    void initTest() {
        chart = new Chart();
    }

    @Test
    void addAndRemoveLumoGradientVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("gradient");
        chart.removeThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute(null);
    }

    @Test
    void addLumoGradientVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("gradient");
    }

    @Test
    void addLumoMonotoneVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        assertThemeAttribute("monotone");
    }

    @Test
    void addLumoClassicVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
        assertThemeAttribute("classic");
    }

    @Test
    void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttributeContains("monotone");
        assertThemeAttributeContains("gradient");
        chart.removeThemeVariants(ChartVariant.LUMO_GRADIENT);
        assertThemeAttribute("monotone");
    }

    @Test
    void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_MONOTONE);
        chart.addThemeVariants(ChartVariant.LUMO_GRADIENT);
        chart.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    void addTwiceAndSeeOnce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
        chart.addThemeVariants(ChartVariant.LUMO_CLASSIC);
        assertThemeAttribute("classic");
    }

    private void assertThemeAttribute(String expected) {
        String actual = chart.getThemeName();
        assertEquals(expected, actual, "Unexpected theme attribute on chart");
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = chart.getThemeName();
        assertTrue(actual.contains(expected),
                "Theme attribute not present on chart");
    }
}
