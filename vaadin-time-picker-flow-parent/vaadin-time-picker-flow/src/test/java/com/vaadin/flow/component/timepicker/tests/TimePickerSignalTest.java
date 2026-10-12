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
package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.tests.AbstractSignalsTest;

class TimePickerSignalTest extends AbstractSignalsTest {

    @TestFactory
    Stream<DynamicTest> bindMin() {
        return generateBindingTests(TimePicker::new, TimePicker::bindMin,
                TimePicker::getMin, TimePicker::setMin, LocalTime.of(9, 0),
                LocalTime.of(10, 0));
    }

    @TestFactory
    Stream<DynamicTest> bindMax() {
        return generateBindingTests(TimePicker::new, TimePicker::bindMax,
                TimePicker::getMax, TimePicker::setMax, LocalTime.of(17, 0),
                LocalTime.of(18, 0));
    }
}
