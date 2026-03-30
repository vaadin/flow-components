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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SelectVariantTest {

    private Select select;

    @BeforeEach
    void initTest() {
        select = new Select<>();
    }

    @Test
    void addAndRemoveAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_CENTER);
        assertThemeAttribute("align-center");
        select.removeThemeVariants(SelectVariant.ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    void addAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    void addSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        assertThemeAttribute("small");
    }

    @Test
    void addAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    void addHelperAbove_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        select.removeThemeVariants(SelectVariant.HELPER_ABOVE);
        assertThemeAttribute("small");
    }

    @Test
    void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.HELPER_ABOVE);
        select.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    void addTwiceAndSeeOnce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.SMALL);
        select.addThemeVariants(SelectVariant.SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = select.getThemeName();
        assertEquals(expected, actual, "Unexpected theme attribute on select");
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = select.getThemeName();
        assertTrue(actual.contains(expected),
                "Theme attribute not present on select");
    }
}
