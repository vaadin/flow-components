package com.vaadin.tests;

import com.vaadin.flow.component.HasThemeVariant;
import com.vaadin.flow.component.ThemeVariant;
import org.junit.Assert;

import java.util.Set;

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
