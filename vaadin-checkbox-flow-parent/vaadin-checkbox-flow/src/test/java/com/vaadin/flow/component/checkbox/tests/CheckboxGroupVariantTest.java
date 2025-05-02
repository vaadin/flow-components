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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.dom.ThemeList;

public class CheckboxGroupVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        ThemeList themeNames = group.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(CheckboxGroupVariant.LUMO_VERTICAL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        group.removeThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        ThemeList themeNames = group.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(CheckboxGroupVariant.LUMO_VERTICAL.getVariantName()));
    }
}
