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
package com.vaadin.flow.component.radiobutton;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class RadioButtonGroupSignalTest extends AbstractSignalsTest {
    private final RadioButtonGroup<String> group = new RadioButtonGroup<>();
    private final ValueSignal<Boolean> readonlySignal = new ValueSignal<>(
            false);

    @Test
    void bindReadOnly_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);

        Assertions.assertFalse(group.isReadOnly());

        readonlySignal.set(true);
        Assertions.assertTrue(group.isReadOnly());
    }

    @Test
    void bindReadOnly_elementNotAttached_initialValueApplied() {
        readonlySignal.set(true);
        group.bindReadOnly(readonlySignal);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertTrue(group.isReadOnly());

        UI.getCurrent().add(group);
        Assertions.assertTrue(group.isReadOnly());
    }

    @Test
    void setReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> group.setReadOnly(true));
    }

    @Test
    void bindReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> group.bindReadOnly(new ValueSignal<>(true)));
    }

    @Test
    void bindReadOnly_disablesUncheckedButtons() {
        group.setItems("One", "Two", "Three");
        group.setValue("One");
        group.bindReadOnly(readonlySignal);
        UI.getCurrent().add(group);

        readonlySignal.set(true);

        List<RadioButton<String>> buttons = getRadioButtons();
        Assertions.assertTrue(buttons.get(0).isEnabled(),
                "Selected button should remain enabled");
        Assertions.assertFalse(buttons.get(1).isEnabled(),
                "Unchecked button should be disabled");
        Assertions.assertFalse(buttons.get(2).isEnabled(),
                "Unchecked button should be disabled");
    }

    @SuppressWarnings("unchecked")
    private List<RadioButton<String>> getRadioButtons() {
        return group.getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());
    }
}
