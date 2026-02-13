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
package com.vaadin.flow.component.login;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

/**
 * Unit tests for
 * {@link AbstractLogin#bindError(com.vaadin.flow.signals.Signal)}.
 */
public class AbstractLoginBindingTest extends AbstractSignalsUnitTest {

    @Test
    public void bindError_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        LoginForm form = new LoginForm();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(form);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        form.bindError(signal);
        assertTrue(form.isError());
        assertTrue(form.isEnabled()); // error=true enables component
        assertTrue(form.getElement().getProperty("error", false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(form.isError());
        assertFalse(form.getElement().getProperty("error", false));

        // Update to null -> should map to false
        signal.value(null);
        assertFalse(form.isError());
        assertFalse(form.getElement().getProperty("error", false));
    }

    @Test
    public void bindError_elementNotAttached_bindingInactive_untilAttach() {
        LoginForm form = new LoginForm();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        form.bindError(signal);

        // While detached, binding should be inactive
        assertFalse(form.isError());
        signal.value(false);
        assertFalse(form.isError());

        // Attach -> latest value is applied
        UI.getCurrent().add(form);
        assertFalse(form.isError());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(form.isError());
        assertTrue(form.isEnabled()); // error=true enables component
    }

    @Test
    public void setError_whileBindingActive_throwsBindingActiveException() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        form.bindError(signal);
        assertTrue(form.isError());

        assertThrows(BindingActiveException.class, () -> form.setError(false));
    }

    @Test
    public void bindError_againWhileActive_throwsBindingActiveException() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        form.bindError(signal);
        assertTrue(form.isError());

        assertThrows(BindingActiveException.class,
                () -> form.bindError(new ValueSignal<>(false)));
    }

    @Test
    public void bindError_errorTrueEnablesComponent() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);
        form.setEnabled(false);

        ValueSignal<Boolean> signal = new ValueSignal<>(false);
        form.bindError(signal);
        assertFalse(form.isEnabled());

        // When error becomes true, component should be enabled
        signal.value(true);
        assertTrue(form.isError());
        assertTrue(form.isEnabled());
    }

    @Test
    public void bindError_errorFalseDoesNotDisableComponent() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);

        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        form.bindError(signal);
        assertTrue(form.isEnabled()); // error=true enables it

        // When error becomes false, component should remain enabled
        signal.value(false);
        assertFalse(form.isError());
        assertTrue(form.isEnabled()); // Should still be enabled
    }

    @Test
    public void bindError_multipleTransitions_enabledStateCorrect() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);
        form.setEnabled(false);

        ValueSignal<Boolean> signal = new ValueSignal<>(false);
        form.bindError(signal);
        assertFalse(form.isEnabled());
        assertFalse(form.isError());

        // error: false -> true (should enable)
        signal.value(true);
        assertTrue(form.isError());
        assertTrue(form.isEnabled());

        // error: true -> false (should stay enabled)
        signal.value(false);
        assertFalse(form.isError());
        assertTrue(form.isEnabled());

        // Manually disable
        form.setEnabled(false);
        assertFalse(form.isEnabled());

        // error: false -> true (should enable again)
        signal.value(true);
        assertTrue(form.isError());
        assertTrue(form.isEnabled());
    }

    @Test
    public void bindError_synchronizesWithElement() {
        LoginForm form = new LoginForm();
        UI.getCurrent().add(form);

        ValueSignal<Boolean> signal = new ValueSignal<>(false);
        form.bindError(signal);

        // Verify element property is synchronized
        assertFalse(form.getElement().getProperty("error", false));

        signal.value(true);
        assertTrue(form.getElement().getProperty("error", false));

        signal.value(false);
        assertFalse(form.getElement().getProperty("error", false));
    }
}
