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
package com.vaadin.flow.component.checkbox.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class CheckboxGroupSignalsTest extends AbstractSignalsUnitTest {

    @Test
    public void bindRequired_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(group);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        group.bindRequired(signal);
        assertTrue(group.isRequired());
        assertTrue(group.getElement().getProperty("required", false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(group.isRequired());
        assertFalse(group.getElement().getProperty("required", false));

        // Update to null -> should map to false
        signal.value(null);
        assertFalse(group.isRequired());
        assertFalse(group.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_elementNotAttached_bindingInactive_untilAttach() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        group.bindRequired(signal);

        // While detached, binding should be inactive
        assertFalse(group.isRequired());
        signal.value(false);
        assertFalse(group.isRequired());

        // Attach -> latest value is applied
        UI.getCurrent().add(group);
        assertFalse(group.isRequired());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(group.isRequired());
    }

    @Test
    public void setRequired_whileBindingActive_throwsBindingActiveException() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        UI.getCurrent().add(group);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        group.bindRequired(signal);
        assertTrue(group.isRequired());

        assertThrows(BindingActiveException.class,
                () -> group.setRequired(false));
    }

    @Test
    public void bindRequired_againWhileActive_throwsBindingActiveException() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        UI.getCurrent().add(group);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        group.bindRequired(signal);
        assertTrue(group.isRequired());

        assertThrows(BindingActiveException.class,
                () -> group.bindRequired(new ValueSignal<>(false)));
    }

    @Test
    public void bindRequired_null_unbinds() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        UI.getCurrent().add(group);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        group.bindRequired(signal);
        assertTrue(group.isRequired());

        // Unbind
        group.bindRequired(null);

        // Should be able to set value manually without exception
        group.setRequired(false);
        assertFalse(group.isRequired());

        // Signal updates should no longer be propagated
        signal.value(true);
        assertFalse(group.isRequired());
    }
}
