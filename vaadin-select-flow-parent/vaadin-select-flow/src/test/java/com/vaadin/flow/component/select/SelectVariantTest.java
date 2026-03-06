/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.select;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SelectVariantTest {

    private Select select;

    @Before
    public void initTest() {
        select = new Select<>();
    }

    @Test
    public void addAndRemoveAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_CENTER);
        assertThemeAttribute("align-center");
        select.removeThemeVariants(SelectVariant.ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addHelperAbove_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        select.removeThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        select.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeOnce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = select.getThemeName();
        assertEquals("Unexpected theme attribute on select", expected, actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = select.getThemeName();
        assertTrue("Theme attribute not present on select",
                actual.contains(expected));
    }
}
