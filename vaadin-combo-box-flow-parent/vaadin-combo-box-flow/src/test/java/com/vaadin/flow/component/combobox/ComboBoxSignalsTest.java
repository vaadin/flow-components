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
package com.vaadin.flow.component.combobox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ComboBoxSignalsTest extends AbstractSignalsUnitTest {

    @Test
    public void bindRequired_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        ComboBox<String> comboBox = new ComboBox<>();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(comboBox);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        comboBox.bindRequired(signal);
        assertTrue(comboBox.isRequired());
        assertTrue(comboBox.getElement().getProperty("required", false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(comboBox.isRequired());
        assertFalse(comboBox.getElement().getProperty("required", false));

        // Update to null -> should map to false
        signal.value(null);
        assertFalse(comboBox.isRequired());
        assertFalse(comboBox.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_elementNotAttached_bindingInactive_untilAttach() {
        ComboBox<String> comboBox = new ComboBox<>();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        comboBox.bindRequired(signal);

        // While detached, binding should be inactive
        assertFalse(comboBox.isRequired());
        signal.value(false);
        assertFalse(comboBox.isRequired());

        // Attach -> latest value is applied
        UI.getCurrent().add(comboBox);
        assertFalse(comboBox.isRequired());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(comboBox.isRequired());
    }

    @Test
    public void setRequired_whileBindingActive_throwsBindingActiveException() {
        ComboBox<String> comboBox = new ComboBox<>();
        UI.getCurrent().add(comboBox);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        comboBox.bindRequired(signal);
        assertTrue(comboBox.isRequired());

        assertThrows(BindingActiveException.class,
                () -> comboBox.setRequired(false));
    }

    @Test
    public void bindRequired_againWhileActive_throwsBindingActiveException() {
        ComboBox<String> comboBox = new ComboBox<>();
        UI.getCurrent().add(comboBox);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        comboBox.bindRequired(signal);
        assertTrue(comboBox.isRequired());

        assertThrows(BindingActiveException.class,
                () -> comboBox.bindRequired(new ValueSignal<>(false)));
    }

    @Test
    public void bindRequired_null_unbinds() {
        ComboBox<String> comboBox = new ComboBox<>();
        UI.getCurrent().add(comboBox);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        comboBox.bindRequired(signal);
        assertTrue(comboBox.isRequired());

        // Unbind
        comboBox.bindRequired(null);

        // Should be able to set value manually without exception
        comboBox.setRequired(false);
        assertFalse(comboBox.isRequired());

        // Signal updates should no longer be propagated
        signal.value(true);
        assertFalse(comboBox.isRequired());
    }
}
