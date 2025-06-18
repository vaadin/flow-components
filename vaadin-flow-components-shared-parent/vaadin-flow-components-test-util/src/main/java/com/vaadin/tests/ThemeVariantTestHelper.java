/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.tests;

import java.util.Set;

import org.junit.Assert;

import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ThemeVariant;

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
