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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class CheckboxSignalTest extends AbstractSignalsTest {

    private Checkbox checkbox;
    private ValueSignal<Boolean> signal;

    @BeforeEach
    void setup() {
        checkbox = new Checkbox();
        signal = new ValueSignal<>(false);
    }

    @AfterEach
    void tearDown() {
        if (checkbox != null && checkbox.isAttached()) {
            checkbox.removeFromParent();
        }
    }

    @Test
    void bindIndeterminate_signalBound_propertySync() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        Assertions.assertFalse(checkbox.isIndeterminate());

        signal.set(true);
        Assertions.assertTrue(checkbox.isIndeterminate());

        signal.set(false);
        Assertions.assertFalse(checkbox.isIndeterminate());
    }

    @Test
    void bindIndeterminate_notAttached_noEffect() {
        checkbox.bindIndeterminate(signal, signal::set);

        boolean initial = checkbox.isIndeterminate();
        signal.set(true);
        Assertions.assertEquals(initial, checkbox.isIndeterminate());
    }

    @Test
    void bindIndeterminate_detachAndReattach() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        signal.set(true);
        Assertions.assertTrue(checkbox.isIndeterminate());

        checkbox.removeFromParent();
        signal.set(false);
        Assertions.assertTrue(checkbox.isIndeterminate());

        UI.getCurrent().add(checkbox);
        Assertions.assertFalse(checkbox.isIndeterminate());
    }

    void bindIndeterminate_setWhileBound_syncsToSignal() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        checkbox.setIndeterminate(true);
        Assertions.assertTrue(signal.peek());

        checkbox.setIndeterminate(false);
        Assertions.assertFalse(signal.peek());
    }

    @Test
    void bindIndeterminate_doubleBind_throws() {
        checkbox.bindIndeterminate(signal, signal::set);
        var other = new ValueSignal<>(true);

        Assertions.assertThrows(BindingActiveException.class,
                () -> checkbox.bindIndeterminate(other, other::set));
    }

    @Test
    void bindIndeterminate_nullSignal_throwsNPE() {
        Assertions.assertThrows(NullPointerException.class,
                () -> checkbox.bindIndeterminate(null, null));
    }
}
