/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
        assertTrue("Theme attribute not present ib chart",
                actual.contains(expected));
    }
}
