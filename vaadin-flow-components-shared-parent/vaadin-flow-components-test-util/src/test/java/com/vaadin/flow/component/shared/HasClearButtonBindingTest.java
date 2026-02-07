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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

/**
 * Unit tests for
 * {@link HasClearButton#bindClearButtonVisible(com.vaadin.flow.signals.Signal)}.
 */
public class HasClearButtonBindingTest extends AbstractSignalsUnitTest {

    @Tag("test")
    private static class TestComponent extends Component
            implements HasClearButton {
    }

    @Test
    public void bindClearButtonVisible_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        TestComponent component = new TestComponent();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(component);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);
        assertTrue(component.isClearButtonVisible());
        assertTrue(component.getElement().getProperty("clearButtonVisible",
                false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(component.isClearButtonVisible());
        assertFalse(component.getElement().getProperty("clearButtonVisible",
                false));

        // Update to null -> should map to false
        signal.value(null);
        assertFalse(component.isClearButtonVisible());
        assertFalse(component.getElement().getProperty("clearButtonVisible",
                false));
    }

    @Test
    public void bindClearButtonVisible_elementNotAttached_bindingInactive_untilAttach() {
        TestComponent component = new TestComponent();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);

        // While detached, binding should be inactive
        assertFalse(component.isClearButtonVisible());
        signal.value(false);
        assertFalse(component.isClearButtonVisible());

        // Attach -> latest value is applied
        UI.getCurrent().add(component);
        assertFalse(component.isClearButtonVisible());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(component.isClearButtonVisible());
    }

    @Test
    public void setClearButtonVisible_whileBindingActive_throwsBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);
        assertTrue(component.isClearButtonVisible());

        assertThrows(BindingActiveException.class,
                () -> component.setClearButtonVisible(false));
    }

    @Test
    public void bindClearButtonVisible_againWhileActive_throwsBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);
        assertTrue(component.isClearButtonVisible());

        assertThrows(BindingActiveException.class, () -> component
                .bindClearButtonVisible(new ValueSignal<>(false)));
    }

    @Test
    public void bindClearButtonVisible_passingNull_unbindsExistingBinding() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindClearButtonVisible(signal);
        assertTrue(component.isClearButtonVisible());

        // Unbind
        component.bindClearButtonVisible(null);

        // Manual set should work now
        component.setClearButtonVisible(false);
        assertFalse(component.isClearButtonVisible());

        // Signal update should not affect component anymore
        signal.value(true);
        assertFalse(component.isClearButtonVisible());
    }
}
