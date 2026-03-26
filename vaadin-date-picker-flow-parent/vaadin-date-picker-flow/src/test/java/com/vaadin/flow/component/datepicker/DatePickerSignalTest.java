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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

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

    @TestFactory
    Stream<DynamicTest> bindMin() {
        return generateBindingTests(DatePicker::new, DatePicker::bindMin,
                DatePicker::getMin, DatePicker::setMin,
                () -> new ValueSignal<>(LocalDate.of(2023, 1, 1)),
                LocalDate.of(2023, 1, 2));
    }

    @TestFactory
    Stream<DynamicTest> bindMax() {
        return generateBindingTests(DatePicker::new, DatePicker::bindMax,
                DatePicker::getMax, DatePicker::setMax,
                () -> new ValueSignal<>(LocalDate.of(2023, 1, 1)),
                LocalDate.of(2023, 1, 2));
    }

    @TestFactory
    Stream<DynamicTest> bindInitialPosition() {
        return generateBindingTests(DatePicker::new,
                DatePicker::bindInitialPosition, DatePicker::getInitialPosition,
                DatePicker::setInitialPosition,
                () -> new ValueSignal<>(LocalDate.of(2023, 1, 1)),
                LocalDate.of(2023, 1, 2));
    }
}
