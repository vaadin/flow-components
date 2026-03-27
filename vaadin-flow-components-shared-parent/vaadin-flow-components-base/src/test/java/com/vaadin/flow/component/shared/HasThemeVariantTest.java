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
package com.vaadin.flow.component.shared;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class HasThemeVariantTest extends AbstractSignalsTest {

    @Test
    void addThemeVariant_themeNamesContainsThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.removeThemeVariants(TestComponentVariant.TEST_VARIANT);

        Assertions.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    void setThemeVariant_setTrue_addsThemeVariant() {
        TestComponent component = new TestComponent();
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, true);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void setThemeVariant_setFalse_removesThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, false);

        Assertions.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    void setThemeVariants_overridesExisting() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariants(TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT_2.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void setThemeVariants_withoutArgs_clearsThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariants();

        Assertions.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    void setThemeVariants_setTrue_addsThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(true, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_2.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void setThemeVariants_setFalse_removesThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(false, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void bindThemeVariant_setSignalValueTrue_themeNamesContainsThemeVariant() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void bindThemeVariant_setSignalValueFalse_themeNamesDoesNotContainThemeVariant() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);

        signal.set(false);
        Assertions.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    void bindThemeVariant_editWithActiveBinding_throwBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);
        Assertions.assertFalse(component.getThemeNames().isEmpty());

        Assertions.assertThrows(BindingActiveException.class, () -> component
                .removeThemeVariants(TestComponentVariant.TEST_VARIANT));
        Assertions.assertThrows(BindingActiveException.class, () -> component
                .addThemeVariants(TestComponentVariant.TEST_VARIANT));
        Assertions.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariant(TestComponentVariant.TEST_VARIANT, false));
        Assertions.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariant(TestComponentVariant.TEST_VARIANT, true));
        Assertions.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariants(false, TestComponentVariant.TEST_VARIANT));
    }

    @Test
    void bindThemeVariants_setsThemeNames() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<List<TestComponentVariant>> signal = new ValueSignal<>(
                List.of(TestComponentVariant.TEST_VARIANT,
                        TestComponentVariant.TEST_VARIANT_2));
        component.bindThemeVariants(signal);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_2.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void bindThemeVariants_signalChanges_themeNamesUpdated() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<List<TestComponentVariant>> signal = new ValueSignal<>(
                List.of(TestComponentVariant.TEST_VARIANT));
        component.bindThemeVariants(signal);

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());

        signal.set(List.of(TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3));

        Assertions.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT_2.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    void bindThemeVariants_editWithActiveGroupBinding_throwsBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<List<TestComponentVariant>> signal = new ValueSignal<>(
                List.of(TestComponentVariant.TEST_VARIANT));
        component.bindThemeVariants(signal);

        Assertions.assertThrows(BindingActiveException.class,
                () -> component.bindThemeVariants(new ValueSignal<>(
                        List.of(TestComponentVariant.TEST_VARIANT_2))));
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
