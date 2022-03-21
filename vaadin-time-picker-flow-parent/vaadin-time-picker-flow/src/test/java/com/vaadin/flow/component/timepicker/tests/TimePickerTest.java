/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class TimePickerTest {
    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        TimePicker picker = new TimePicker();
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
        TimePicker picker = new TimePicker((LocalTime) null);
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsTime_valuePropertyHasInitialValue() {
        TimePicker picker = new TimePicker(LocalTime.of(9, 32));
        assertEquals(LocalTime.of(9, 32), picker.getValue());
        assertEquals("09:32", picker.getElement().getProperty("value"));
    }

    @Test
    public void timePicker_basicCases() {
        TimePicker picker = new TimePicker();

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
        TimePicker timePicker=new TimePicker();timePicker.setStep(Duration.ofNanos(500_000));
    }

    @Test
    public void setMinTime_getMin() {
        TimePicker timePicker = new TimePicker();
        final String min = "12:00";
        LocalTime minTime = LocalTime.parse(min);
        timePicker.setMinTime(minTime);
        assertEquals(minTime, timePicker.getMin());
        assertEquals(minTime, timePicker.getMinTime());
    }

    @Test
    public void setMin_getMin_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMin());
        timePicker.setMin(null);
        assertEquals(null, timePicker.getMin());
        assertEquals(null, timePicker.getMinTime());
    }

    @Test
    public void setMinTime_getMin_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMinTime());
        timePicker.setMinTime(null);
        assertEquals(null, timePicker.getMin());
        assertEquals(null, timePicker.getMinTime());
    }

    @Test
    public void setMaxTime_getMax() {
        TimePicker timePicker = new TimePicker();
        final String max = "12:00";
        LocalTime maxTime = LocalTime.parse(max);
        timePicker.setMaxTime(maxTime);
        assertEquals(maxTime, timePicker.getMax());
        assertEquals(maxTime, timePicker.getMaxTime());
    }

    @Test
    public void setMax_getMax_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMax());
        timePicker.setMax(null);
        assertEquals(null, timePicker.getMax());
        assertEquals(null, timePicker.getMaxTime());
    }

    @Test
    public void setMaxTime_getMax_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMaxTime());
        timePicker.setMaxTime(null);
        assertEquals(null, timePicker.getMax());
        assertEquals(null, timePicker.getMaxTime());
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        TimePicker timePicker = new TimePicker();

        assertFalse("Clear button should not be visible by default",
                timePicker.isClearButtonVisible());
        assertClearButtonPropertyValueEquals(timePicker, true);
        assertClearButtonPropertyValueEquals(timePicker, false);
    }

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-time-picker");

        String value = LocalTime.now().plus(31l, ChronoUnit.MINUTES).toString();
        element.setProperty("value", value);
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TimePicker.class))
                .thenAnswer(invocation -> new TimePicker());

        TimePicker field = Component.from(element, TimePicker.class);
        Assert.assertEquals(value, field.getElement().getPropertyRaw("value"));
    }

    public void assertClearButtonPropertyValueEquals(TimePicker timePicker,
            Boolean value) {
        timePicker.setClearButtonVisible(value);
        assertEquals(value, timePicker.isClearButtonVisible());
        assertEquals(timePicker.isClearButtonVisible(), timePicker.getElement()
                .getProperty("clearButtonVisible", value));
    }

    @Test
    public void setAutoOpenDisabled() {
        TimePicker timePicker = new TimePicker();
        assertTrue(timePicker.isAutoOpen());
        timePicker.setAutoOpen(false);
        assertTrue(timePicker.getElement().getProperty(PROP_AUTO_OPEN_DISABLED,
                false));
        assertFalse(timePicker.isAutoOpen());
    }
}
