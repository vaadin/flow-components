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
package com.vaadin.flow.component.datetimepicker;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DateTimePickerSignalTest extends AbstractSignalsTest {

    private final DateTimePicker dateTimePicker = new DateTimePicker();
    private final ValueSignal<LocalDateTime> signal = new ValueSignal<>(
            LocalDateTime.of(2023, 10, 1, 10, 0));
    private final ValueSignal<Boolean> readonlySignal = new ValueSignal<>(
            false);

    @Test
    void bindMin_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);

        Assertions.assertEquals(signal.peek(), dateTimePicker.getMin());
        Assertions.assertEquals(signal.peek().toString(),
                dateTimePicker.getElement().getProperty("min"));

        LocalDateTime newValue = LocalDateTime.of(2023, 10, 2, 11, 0);
        signal.set(newValue);
        Assertions.assertEquals(newValue, dateTimePicker.getMin());
        Assertions.assertEquals(newValue.toString(),
                dateTimePicker.getElement().getProperty("min"));
    }

    @Test
    void bindMin_elementNotAttached_initialValueApplied() {
        dateTimePicker.bindMin(signal);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertEquals(signal.peek(), dateTimePicker.getMin());

        UI.getCurrent().add(dateTimePicker);
        Assertions.assertEquals(signal.peek(), dateTimePicker.getMin());
    }

    @Test
    void setMin_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker.setMin(LocalDateTime.now()));
    }

    @Test
    void bindMin_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker
                        .bindMin(new ValueSignal<>(LocalDateTime.now())));
    }

    @Test
    void bindMax_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);

        Assertions.assertEquals(signal.peek(), dateTimePicker.getMax());
        Assertions.assertEquals(signal.peek().toString(),
                dateTimePicker.getElement().getProperty("max"));

        LocalDateTime newValue = LocalDateTime.of(2023, 10, 2, 11, 0);
        signal.set(newValue);
        Assertions.assertEquals(newValue, dateTimePicker.getMax());
        Assertions.assertEquals(newValue.toString(),
                dateTimePicker.getElement().getProperty("max"));
    }

    @Test
    void bindMax_elementNotAttached_initialValueApplied() {
        dateTimePicker.bindMax(signal);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertEquals(signal.peek(), dateTimePicker.getMax());

        UI.getCurrent().add(dateTimePicker);
        Assertions.assertEquals(signal.peek(), dateTimePicker.getMax());
    }

    @Test
    void setMax_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker.setMax(LocalDateTime.now()));
    }

    @Test
    void bindMax_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker
                        .bindMax(new ValueSignal<>(LocalDateTime.now())));
    }

    @Test
    void bindReadOnly_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindReadOnly(readonlySignal);

        Assertions.assertFalse(dateTimePicker.isReadOnly());

        readonlySignal.set(true);
        Assertions.assertTrue(dateTimePicker.isReadOnly());
    }

    @Test
    void bindReadOnly_elementNotAttached_initialValueApplied() {
        readonlySignal.set(true);
        dateTimePicker.bindReadOnly(readonlySignal);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertTrue(dateTimePicker.isReadOnly());

        UI.getCurrent().add(dateTimePicker);
        Assertions.assertTrue(dateTimePicker.isReadOnly());
    }

    @Test
    void setReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindReadOnly(readonlySignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker.setReadOnly(true));
    }

    @Test
    void bindReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindReadOnly(readonlySignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dateTimePicker.bindReadOnly(new ValueSignal<>(true)));
    }

    @Test
    void bindReadOnly_synchronizesChildComponents() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindReadOnly(readonlySignal);

        Assertions.assertFalse(dateTimePicker.isReadOnly());
        Assertions.assertFalse(getDatePicker().isReadOnly());
        Assertions.assertFalse(getTimePicker().isReadOnly());

        readonlySignal.set(true);
        Assertions.assertTrue(dateTimePicker.isReadOnly());
        Assertions.assertTrue(getDatePicker().isReadOnly());
        Assertions.assertTrue(getTimePicker().isReadOnly());
    }

    private DateTimePickerDatePicker getDatePicker() {
        return dateTimePicker.getChildren()
                .filter(child -> child instanceof DateTimePickerDatePicker)
                .map(child -> (DateTimePickerDatePicker) child).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "DateTimePickerDatePicker not found"));
    }

    private DateTimePickerTimePicker getTimePicker() {
        return dateTimePicker.getChildren()
                .filter(child -> child instanceof DateTimePickerTimePicker)
                .map(child -> (DateTimePickerTimePicker) child).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "DateTimePickerTimePicker not found"));
    }

}
