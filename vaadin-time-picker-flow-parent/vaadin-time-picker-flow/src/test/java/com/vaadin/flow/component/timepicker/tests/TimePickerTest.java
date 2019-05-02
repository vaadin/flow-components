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
package com.vaadin.flow.component.timepicker.tests;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.Assert;
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
    public void timePicker_nullValue() {
    	TimePicker timePicker = new TimePicker();
    	timePicker.setValue(null);
    	assertEquals(null, timePicker.getValue());
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

    @Test
    public void testSetStep_dividesEvenly_matchesGetter() {
        TimePicker timePicker = new TimePicker();

        assertEquals("Invalid default step", Duration.ofHours(1),
                timePicker.getStep());

        timePicker.setStep(Duration.ofSeconds(1));
        assertEquals("Invalid step returned", Duration.ofSeconds(1),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMillis(1));
        assertEquals("Invalid step returned", Duration.ofMillis(1),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMillis(10));
        assertEquals("Invalid step returned", Duration.ofMillis(10),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMillis(100));
        assertEquals("Invalid step returned", Duration.ofMillis(100),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMillis(1000));
        assertEquals("Invalid step returned", Duration.ofSeconds(1),
                timePicker.getStep());

        // the next 3 would be broken in the web component
        // https://github.com/vaadin/vaadin-time-picker/issues/79
        timePicker.setStep(Duration.ofMinutes(40));
        assertEquals("Invalid step returned", Duration.ofMinutes(40),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMinutes(45));
        assertEquals("Invalid step returned", Duration.ofMinutes(45),
                timePicker.getStep());

        timePicker.setStep(Duration.ofMinutes(90));
        assertEquals("Invalid step returned", Duration.ofMinutes(90),
                timePicker.getStep());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_secondsNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofSeconds(11));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_minutesNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofMinutes(35));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_millisecondsNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofMillis(333));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_hoursNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofHours(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_negativeStep_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofMinutes(-15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStep_lessThan0Ms_throwsException() {
        TimePicker timePicker = new TimePicker();
        timePicker.setStep(Duration.ofNanos(500_000));
    }

    @Test
    public void setMin_getMin() {
        TimePicker timePicker = new TimePicker();
        timePicker.setMin("12:00");
        assertEquals("12:00", timePicker.getMin());
    }

    @Test
    public void setMax_getMax() {
        TimePicker timePicker = new TimePicker();
        timePicker.setMax("12:00");
        assertEquals("12:00", timePicker.getMax());
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        TimePicker timePicker = new TimePicker();

        assertFalse("Clear button should not be visible by default",
                timePicker.isClearButtonVisible());
        assertClearButtonPropertyValueEquals(timePicker, true);
        assertClearButtonPropertyValueEquals(timePicker, false);
    }

    public void assertClearButtonPropertyValueEquals(TimePicker timePicker, Boolean value) {
        timePicker.setClearButtonVisible(value);
        assertEquals(value, timePicker.isClearButtonVisible());
        assertEquals(timePicker.isClearButtonVisible(),
                timePicker.getElement().getProperty("clearButtonVisible", value));
    }

}
