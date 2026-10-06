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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class MultiSelectComboBoxSignalTest extends AbstractSignalsTest {
    private MultiSelectComboBox<String> comboBox;
    private ValueSignal<Boolean> readOnlySignal;

    @BeforeEach
    void setup() {
        comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("foo", "bar");
        readOnlySignal = new ValueSignal<>(false);
        ui.add(comboBox);
    }

    @Test
    void bindReadOnly_updatesWithSignal() {
        comboBox.bindReadOnly(readOnlySignal);
        Assertions.assertFalse(comboBox.isReadOnly());

        readOnlySignal.set(true);
        Assertions.assertTrue(comboBox.isReadOnly());
    }

    @Test
    void setReadOnly_whileBound_throws() {
        comboBox.bindReadOnly(readOnlySignal);

        Assertions.assertThrows(BindingActiveException.class,
                () -> comboBox.setReadOnly(true));
    }

    @Test
    void setReadOnly_switchedOff_dataReset() {
        comboBox.setReadOnly(true);
        ui.dumpPendingJavaScriptInvocations();

        comboBox.setReadOnly(false);

        Assertions.assertTrue(isDataResetScheduled());
    }

    @Test
    void setReadOnly_switchedOn_dataNotReset() {
        ui.dumpPendingJavaScriptInvocations();

        comboBox.setReadOnly(true);

        Assertions.assertFalse(isDataResetScheduled());
    }

    @Test
    void bindReadOnly_switchedOff_dataReset() {
        readOnlySignal.set(true);
        comboBox.bindReadOnly(readOnlySignal);
        ui.dumpPendingJavaScriptInvocations();

        readOnlySignal.set(false);

        Assertions.assertTrue(isDataResetScheduled());
    }

    @Test
    void bindReadOnly_switchedOn_dataNotReset() {
        comboBox.bindReadOnly(readOnlySignal);
        ui.dumpPendingJavaScriptInvocations();

        readOnlySignal.set(true);

        Assertions.assertFalse(isDataResetScheduled());
    }

    private boolean isDataResetScheduled() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(invocation -> invocation.getInvocation().getExpression())
                .anyMatch(expression -> expression
                        .contains("$connector?.reset()"));
    }
}
