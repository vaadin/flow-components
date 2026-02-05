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
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

/**
 * Unit tests for {@link LoginOverlay#bindOpened(com.vaadin.signals.Signal)}.
 */
public class LoginOverlayBindingTest extends AbstractSignalsUnitTest {

    @Test
    public void bindOpened_elementAttached_updatesWithSignal_andNullMapsToFalse() {
        LoginOverlay overlay = new LoginOverlay();
        // Attach component so that Element.bindProperty becomes active
        UI.getCurrent().add(overlay);

        // Bind a signal and verify initial propagation
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        overlay.bindOpened(signal);
        assertTrue(overlay.isOpened());
        assertTrue(overlay.getElement().getProperty("opened", false));

        // Update to a different non-null value
        signal.value(false);
        assertFalse(overlay.isOpened());
        assertFalse(overlay.getElement().getProperty("opened", false));

        // Update to null -> should map to false
        signal.value(null);
        assertFalse(overlay.isOpened());
        assertFalse(overlay.getElement().getProperty("opened", false));
    }

    @Test
    public void bindOpened_elementNotAttached_bindingInactive_untilAttach() {
        LoginOverlay overlay = new LoginOverlay();
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        overlay.bindOpened(signal);

        // While detached, binding should be inactive
        assertFalse(overlay.isOpened());
        signal.value(false);
        assertFalse(overlay.isOpened());

        // Attach -> latest value is applied
        UI.getCurrent().add(overlay);
        assertFalse(overlay.isOpened());

        // Update after attach -> applied
        signal.value(true);
        assertTrue(overlay.isOpened());
    }

    @Test
    public void setOpened_whileBindingActive_throwsBindingActiveException() {
        LoginOverlay overlay = new LoginOverlay();
        UI.getCurrent().add(overlay);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        overlay.bindOpened(signal);
        assertTrue(overlay.isOpened());

        assertThrows(BindingActiveException.class,
                () -> overlay.setOpened(false));
    }

    @Test
    public void bindOpened_againWhileActive_throwsBindingActiveException() {
        LoginOverlay overlay = new LoginOverlay();
        UI.getCurrent().add(overlay);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        overlay.bindOpened(signal);
        assertTrue(overlay.isOpened());

        assertThrows(BindingActiveException.class,
                () -> overlay.bindOpened(new ValueSignal<>(false)));
    }

    @Test
    public void bindOpened_passingNull_unbindsExistingBinding() {
        LoginOverlay overlay = new LoginOverlay();
        UI.getCurrent().add(overlay);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        overlay.bindOpened(signal);
        assertTrue(overlay.isOpened());

        // Unbind
        overlay.bindOpened(null);

        // Manual set should work now
        overlay.setOpened(false);
        assertFalse(overlay.isOpened());

        // Signal update should not affect component anymore
        signal.value(true);
        assertFalse(overlay.isOpened());
    }
}
