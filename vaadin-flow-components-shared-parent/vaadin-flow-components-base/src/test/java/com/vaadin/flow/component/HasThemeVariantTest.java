package com.vaadin.flow.component;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class HasThemeVariantTest {
    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.removeThemeVariants(TestComponentVariant.TEST_VARIANT);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
    }

    private enum TestComponentVariant implements ThemeVariant {
        TEST_VARIANT("test");

        private final String variant;

        TestComponentVariant(String variant) {
            this.variant = variant;
        }

        @Override
        public String getVariantName() {
            return variant;
        }
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasThemeVariant<TestComponentVariant> {
    }
}
