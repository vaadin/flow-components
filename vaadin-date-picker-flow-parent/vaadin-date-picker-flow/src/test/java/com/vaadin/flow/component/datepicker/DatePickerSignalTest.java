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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DatePickerSignalTest extends AbstractSignalsTest {

    private DatePicker datePicker;
    private ValueSignal<LocalDate> signal;

    @BeforeEach
    void setup() {
        datePicker = new DatePicker();
        signal = new ValueSignal<>(LocalDate.of(2023, 1, 1));
    }

    @Test
    void bindMin_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        Assertions.assertEquals(signal.peek(), datePicker.getMin());

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(signal.peek(), datePicker.getMin());
    }

    @Test
    void bindMin_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMin());

        UI.getCurrent().add(datePicker);
        Assertions.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMin());
    }

    @Test
    void bindMin_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> datePicker.setMin(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void bindMin_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        Assertions.assertThrows(BindingActiveException.class, () -> datePicker
                .bindMin(new ValueSignal<>(LocalDate.of(2023, 2, 1))));
    }

    @Test
    void bindMax_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        Assertions.assertEquals(signal.peek(), datePicker.getMax());

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(signal.peek(), datePicker.getMax());
    }

    @Test
    void bindMax_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMax());

        UI.getCurrent().add(datePicker);
        Assertions.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMax());
    }

    @Test
    void bindMax_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> datePicker.setMax(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void bindMax_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        Assertions.assertThrows(BindingActiveException.class, () -> datePicker
                .bindMax(new ValueSignal<>(LocalDate.of(2023, 2, 1))));
    }

    @Test
    void bindInitialPosition_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        Assertions.assertEquals(signal.peek(), datePicker.getInitialPosition());

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(signal.peek(), datePicker.getInitialPosition());
    }

    @Test
    void bindInitialPosition_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assertions.assertEquals(LocalDate.of(2023, 1, 1),
                datePicker.getInitialPosition());

        UI.getCurrent().add(datePicker);
        Assertions.assertEquals(LocalDate.of(2023, 2, 1),
                datePicker.getInitialPosition());
    }

    @Test
    void bindInitialPosition_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> datePicker.setInitialPosition(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void bindInitialPosition_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> datePicker.bindInitialPosition(
                        new ValueSignal<>(LocalDate.of(2023, 2, 1))));
    }
}
