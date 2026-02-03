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
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        var grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        var themeNames = grid.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(GridVariant.LUMO_NO_BORDER.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.removeThemeVariants(GridVariant.LUMO_NO_BORDER);

        var themeNames = grid.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(GridVariant.LUMO_NO_BORDER.getVariantName()));
    }
}
