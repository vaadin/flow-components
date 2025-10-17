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
package com.vaadin.flow.component.applayout;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DrawerToggleVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        var drawerToggle = new DrawerToggle();
        drawerToggle.addThemeVariants(DrawerToggleVariant.AURA_PERMANENT);

        Set<String> themeNames = drawerToggle.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(DrawerToggleVariant.AURA_PERMANENT.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var drawerToggle = new DrawerToggle();
        drawerToggle.addThemeVariants(DrawerToggleVariant.AURA_PERMANENT);
        drawerToggle.removeThemeVariants(DrawerToggleVariant.AURA_PERMANENT);

        Set<String> themeNames = drawerToggle.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(DrawerToggleVariant.AURA_PERMANENT.getVariantName()));
    }
}
