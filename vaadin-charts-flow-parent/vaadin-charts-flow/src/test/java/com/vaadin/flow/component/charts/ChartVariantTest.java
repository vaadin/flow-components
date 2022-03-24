/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

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
