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
package com.vaadin.flow.component.dialog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DialogThemeVariantTest {

    private final Dialog dialog = new Dialog();

    @Test
    public void addAndRemoveLumoNoPaddingVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        assertThemeAttribute("no-padding");
        dialog.removeThemeVariants(DialogVariant.LUMO_NO_PADDING);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveMaterialNoPaddingVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dialog.addThemeVariants(DialogVariant.MATERIAL_NO_PADDING);
        assertThemeAttribute("no-padding");
        dialog.removeThemeVariants(DialogVariant.MATERIAL_NO_PADDING);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = dialog.getThemeName();
        assertEquals("Unexpected theme attribute on dialog", expected, actual);
    }

}
