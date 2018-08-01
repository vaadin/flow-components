/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import org.junit.Test;

import com.vaadin.flow.component.timepicker.TimePicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TimePickerTest {

    @Test
    public void timePicker_basicCases() {
        TimePicker picker = new TimePicker();

        assertEquals(null, picker.getValue());
        assertFalse(picker.getElement().hasProperty("value"));

        picker.setValue(LocalTime.of(5, 30));
        assertEquals("05:30", picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "07:40");
        assertEquals(LocalTime.of(7, 40), picker.getValue());
    }

    @Test
    public void setInitialValue() {
        TimePicker picker = new TimePicker(LocalTime.of(9, 32));
        assertEquals(LocalTime.of(9, 32), picker.getValue());
        assertEquals("09:32", picker.getElement().getProperty("value"));
    }
    
    @Test
    public void timePickerWithLabel() {
        String label = new String("Time Picker Label");
        TimePicker picker = new TimePicker(label);
        assertEquals(label, picker.getElement().getProperty("label"));
    }

    @Test
    public void timePickerWithPlaceholder() {
        String placeholder = new String("This is a Time Picker");
        TimePicker picker = new TimePicker();
        picker.setPlaceholder(placeholder);

        assertEquals(placeholder,
                picker.getElement().getProperty("placeholder"));
    }

}
