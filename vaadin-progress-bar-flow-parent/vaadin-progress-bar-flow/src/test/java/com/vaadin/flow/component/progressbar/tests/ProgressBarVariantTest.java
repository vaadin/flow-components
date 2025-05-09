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
package com.vaadin.flow.component.progressbar.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.dom.ThemeList;

public class ProgressBarVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);

        ThemeList themeNames = progressBar.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(ProgressBarVariant.LUMO_ERROR.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        progressBar.removeThemeVariants(ProgressBarVariant.LUMO_ERROR);

        ThemeList themeNames = progressBar.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(ProgressBarVariant.LUMO_ERROR.getVariantName()));
    }
}
