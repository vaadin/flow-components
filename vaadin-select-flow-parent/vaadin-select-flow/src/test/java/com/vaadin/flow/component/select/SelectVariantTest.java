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
package com.vaadin.flow.component.select;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

public class SelectVariantTest {

    private Select select;

    @Before
    public void initTest() {
        select = new Select<>();
    }

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        select.removeThemeVariants(SelectVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addMaterialAlwaysFloatLabel_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        assertThemeAttribute("always-float-label");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_SMALL);
        select.addThemeVariants(SelectVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        select.removeThemeVariants(SelectVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_SMALL);
        select.addThemeVariants(SelectVariant.LUMO_HELPER_ABOVE_FIELD);
        select.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        select.addThemeVariants(SelectVariant.LUMO_SMALL);
        select.addThemeVariants(SelectVariant.LUMO_SMALL);
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
