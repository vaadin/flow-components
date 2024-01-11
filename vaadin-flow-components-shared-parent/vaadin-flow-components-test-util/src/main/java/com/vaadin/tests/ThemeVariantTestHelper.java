/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.tests;

import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ThemeVariant;
import org.junit.Assert;

import java.util.Set;

/**
 * Helper class that provides methods for testing that a component correctly
 * implements the {@link HasThemeVariant} interface. Individual component tests
 * are supposed to have test methods calling the methods in here to run the test
 * logic.
 */
public class ThemeVariantTestHelper {
    @SuppressWarnings("unchecked")
    public static <TVariantEnum extends ThemeVariant> void addThemeVariant_themeNamesContainsThemeVariant(
            HasThemeVariant<TVariantEnum> component, TVariantEnum testValue) {
        component.addThemeVariants(testValue);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertTrue(themeNames.contains(testValue.getVariantName()));
    }

    @SuppressWarnings("unchecked")
    public static <TVariantEnum extends ThemeVariant> void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
            HasThemeVariant<TVariantEnum> component, TVariantEnum testValue) {
        component.addThemeVariants(testValue);
        component.removeThemeVariants(testValue);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertFalse(themeNames.contains(testValue.getVariantName()));
    }
}
