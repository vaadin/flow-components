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
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DateTimePickerSignalTest extends AbstractSignalsTest {

    private static final LocalDateTime INITIAL_VALUE = LocalDateTime.of(2023,
            10, 1, 10, 0);
    private static final LocalDateTime UPDATED_VALUE = LocalDateTime.of(2023,
            10, 2, 11, 0);

    private final DateTimePicker dateTimePicker = new DateTimePicker();
    private final ValueSignal<Boolean> readonlySignal = new ValueSignal<>(
            false);

    @TestFactory
    Stream<DynamicTest> bindMin() {
        return generateBindingTests(DateTimePicker::new,
                DateTimePicker::bindMin, DateTimePicker::getMin,
                DateTimePicker::setMin, INITIAL_VALUE, UPDATED_VALUE);
    }

    @TestFactory
    Stream<DynamicTest> bindMax() {
        return generateBindingTests(DateTimePicker::new,
                DateTimePicker::bindMax, DateTimePicker::getMax,
                DateTimePicker::setMax, INITIAL_VALUE, UPDATED_VALUE);
    }

    @TestFactory
    Stream<DynamicTest> bindReadOnly() {
        return generateBindingTests(DateTimePicker::new,
                DateTimePicker::bindReadOnly, DateTimePicker::isReadOnly,
                DateTimePicker::setReadOnly, true, false);
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
