/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;

import static org.junit.Assert.assertEquals;

@NotThreadSafe
public class DateTimePickerTest {

    private UI ui;

    @Before
    public void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void emptyField() {
        DateTimePicker picker = new DateTimePicker();
        assertEquals(null, picker.getValue());
    }

    @Test
    public void setInitialValue() {
        DateTimePicker picker = new DateTimePicker(
                LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    public void setValue() {
        DateTimePicker picker = new DateTimePicker();
        picker.setValue(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    public void setMinGetMin() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMin(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMin());
    }

    @Test
    public void setMaxGetMax() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMax(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMax());
    }

}
