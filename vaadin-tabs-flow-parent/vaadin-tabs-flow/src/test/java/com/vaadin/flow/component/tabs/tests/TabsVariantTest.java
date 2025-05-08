/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.dom.ThemeList;

public class TabsVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        Tabs tabs = new Tabs();
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);

        ThemeList themeNames = tabs.getThemeNames();
        Assert.assertTrue(
                themeNames.contains(TabsVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        Tabs tabs = new Tabs();
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.removeThemeVariants(TabsVariant.LUMO_SMALL);

        ThemeList themeNames = tabs.getThemeNames();
        Assert.assertFalse(
                themeNames.contains(TabsVariant.LUMO_SMALL.getVariantName()));
    }
}
