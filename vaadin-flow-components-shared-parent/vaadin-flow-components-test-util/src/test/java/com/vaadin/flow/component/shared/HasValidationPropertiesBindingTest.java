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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
 * {@link HasValidationProperties#bindErrorMessage(com.vaadin.flow.signals.Signal)}.
 */
public class HasValidationPropertiesBindingTest
        extends AbstractSignalsUnitTest {

    @Tag("test")
    private static class TestComponent extends Component
            implements HasValidationProperties {
    }

    @Test
    public void bindErrorMessage_elementAttached_updatesWithSignal_andNullMapsToEmpty() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(component);

        // Bind a signal and verify initial propagation
        ValueSignal<String> signal = new ValueSignal<>("first error");
        component.bindErrorMessage(signal);
        assertEquals("first error", component.getErrorMessage());
        assertEquals("first error",
                component.getElement().getProperty("errorMessage"));

        // Update to a different non-null value
        signal.value("second error");
        assertEquals("second error", component.getErrorMessage());
        assertEquals("second error",
                component.getElement().getProperty("errorMessage"));

        // Update to null -> should map to empty string
        signal.value(null);
        assertEquals("", component.getElement().getProperty("errorMessage"));
        // API getter returns the property value as-is
        assertEquals("", component.getErrorMessage());
    }

    @Test
    public void bindErrorMessage_elementNotAttached_bindingInactive_untilAttach() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        ValueSignal<String> signal = new ValueSignal<>("msg");
        component.bindErrorMessage(signal);

        // While detached, binding should be inactive
        assertNull(component.getErrorMessage());
        signal.value("updated");
        assertNull(component.getErrorMessage());

        // Attach -> latest value is applied
        UI.getCurrent().add(component);
        assertEquals("updated", component.getErrorMessage());
    }

    @Test
    public void setErrorMessage_whileBindingActive_throwsBindingActiveException() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<String> signal = new ValueSignal<>("foo");
        component.bindErrorMessage(signal);
        assertEquals("foo", component.getErrorMessage());

        assertThrows(BindingActiveException.class,
                () -> component.setErrorMessage("bar"));
    }

    @Test
    public void bindErrorMessage_againWhileActive_throwsBindingActiveException() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<String> signal = new ValueSignal<>("foo");
        component.bindErrorMessage(signal);
        assertEquals("foo", component.getErrorMessage());

        assertThrows(BindingActiveException.class,
                () -> component.bindErrorMessage(new ValueSignal<>("bar")));
    }

    @Test
    public void bindInvalid_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(component);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindInvalid(signal);
        assertTrue(component.isInvalid());
        assertTrue(component.getElement().getProperty("invalid", false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(component.isInvalid());
        assertFalse(component.getElement().getProperty("invalid", false));

        // Update to a different non-null value again
        signal.value(true);
        assertTrue(component.isInvalid());
        assertTrue(component.getElement().getProperty("invalid", false));
    }

    @Test
    public void bindInvalid_elementNotAttached_bindingInactive_untilAttach() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindInvalid(signal);

        // While detached, binding should be inactive
        assertFalse(component.isInvalid());
        signal.value(false);
        assertFalse(component.isInvalid());

        // Attach -> latest value is applied
        UI.getCurrent().add(component);
        assertFalse(component.isInvalid());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(component.isInvalid());
    }

    @Test
    public void setInvalid_whileBindingActive_throwsBindingActiveException() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindInvalid(signal);
        assertTrue(component.isInvalid());

        assertThrows(BindingActiveException.class,
                () -> component.setInvalid(false));
    }

    @Test
    public void bindInvalid_againWhileActive_throwsBindingActiveException() {
        checkUISetup("HasValidationPropertiesBindingTest");
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindInvalid(signal);
        assertTrue(component.isInvalid());

        assertThrows(BindingActiveException.class,
                () -> component.bindInvalid(new ValueSignal<>(false)));
    }
}
