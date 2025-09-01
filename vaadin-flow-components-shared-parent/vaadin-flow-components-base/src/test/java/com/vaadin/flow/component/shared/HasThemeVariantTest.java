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
package com.vaadin.flow.component.shared;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

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

    @Test
    public void setThemeVariant_setTrueContainsThemeVariant() {
        TestComponent component = new TestComponent();
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, true);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
    }

    @Test
    public void setThemeVariant_setFalseDoesNotContainsThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, false);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
    }

    @Test
    public void setThemeVariants_overridesExisting() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariants(TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
        Assert.assertTrue(themeNames.contains(
                TestComponentVariant.TEST_VARIANT_2.getVariantName()));
        Assert.assertTrue(themeNames.contains(
                TestComponentVariant.TEST_VARIANT_3.getVariantName()));
    }

    @Test
    public void setThemeVariants_setTrueContainsThemeVariantDoesntOverrideExisting() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(true, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
        Assert.assertTrue(themeNames.contains(
                TestComponentVariant.TEST_VARIANT_2.getVariantName()));
    }

    @Test
    public void setThemeVariants_setFalseContainsThemeVariantDoesntOverrideExisting() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(false, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Set<String> themeNames = component.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TestComponentVariant.TEST_VARIANT.getVariantName()));
        Assert.assertFalse(themeNames.contains(
                TestComponentVariant.TEST_VARIANT_2.getVariantName()));
        Assert.assertTrue(themeNames.contains(
                TestComponentVariant.TEST_VARIANT_3.getVariantName()));
    }

    private enum TestComponentVariant implements ThemeVariant {
        TEST_VARIANT("test1"), TEST_VARIANT_2("test2"), TEST_VARIANT_3("test3");

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
