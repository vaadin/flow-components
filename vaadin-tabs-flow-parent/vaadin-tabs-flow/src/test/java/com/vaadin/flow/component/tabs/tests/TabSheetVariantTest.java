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
package com.vaadin.flow.component.tabs.tests;

import org.junit.Test;

import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.tests.ThemeVariantTestHelper;

public class TabSheetVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ThemeVariantTestHelper.addThemeVariant_themeNamesContainsThemeVariant(
                new TabSheet(), TabSheetVariant.LUMO_BORDERED);
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ThemeVariantTestHelper
                .addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
                        new TabSheet(), TabSheetVariant.LUMO_BORDERED);
    }
}
