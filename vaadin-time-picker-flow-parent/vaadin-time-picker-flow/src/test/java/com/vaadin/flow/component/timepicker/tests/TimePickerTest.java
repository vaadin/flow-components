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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class TimePickerTest {
    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        TimePicker picker = new TimePicker();
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        TimePicker picker = new TimePicker((LocalTime) null);
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsTime_valuePropertyHasInitialValue() {
        TimePicker picker = new TimePicker(LocalTime.of(9, 32));
        assertEquals(LocalTime.of(9, 32), picker.getValue());
        assertEquals("09:32", picker.getElement().getProperty("value"));
    }

    @Test
    void timePicker_basicCases() {
        TimePicker picker = new TimePicker();

        picker.setValue(LocalTime.of(5, 30));
        assertEquals("05:30", picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "07:40");
        assertEquals(LocalTime.of(7, 40), picker.getValue());
    }

    @Test
    void emptyValueIsNull() {
        TimePicker picker = new TimePicker();
        Assertions.assertNull(picker.getEmptyValue());
    }

    @Test
    void setInitialValue_emptyValueIsNull() {
        TimePicker picker = new TimePicker(LocalTime.of(5, 30));
        Assertions.assertNull(picker.getEmptyValue());
    }

    @Test
    void timePicker_nullValue() {
        TimePicker timePicker = new TimePicker();
        timePicker.setValue(null);
        assertEquals(null, timePicker.getValue());
    }

    @Test
    void timePickerWithLabel() {
        String label = new String("Time Picker Label");
        TimePicker picker = new TimePicker(label);
        assertEquals(label, picker.getElement().getProperty("label"));
    }

    @Test
    void timePickerWithPlaceholder() {
        String placeholder = new String("This is a Time Picker");
        TimePicker picker = new TimePicker();
        picker.setPlaceholder(placeholder);

        assertEquals(placeholder,
                picker.getElement().getProperty("placeholder"));
    }

    @Test
    void testSetStep_dividesEvenly_matchesGetter() {
        TimePicker timePicker = new TimePicker();

        assertEquals(Duration.ofHours(1), timePicker.getStep(),
                "Invalid default step");

        timePicker.setStep(Duration.ofSeconds(1));
        assertEquals(Duration.ofSeconds(1), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMillis(1));
        assertEquals(Duration.ofMillis(1), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMillis(10));
        assertEquals(Duration.ofMillis(10), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMillis(100));
        assertEquals(Duration.ofMillis(100), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMillis(1000));
        assertEquals(Duration.ofSeconds(1), timePicker.getStep(),
                "Invalid step returned");

        // the next 3 would be broken in the web component
        // https://github.com/vaadin/vaadin-time-picker/issues/79
        timePicker.setStep(Duration.ofMinutes(40));
        assertEquals(Duration.ofMinutes(40), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMinutes(45));
        assertEquals(Duration.ofMinutes(45), timePicker.getStep(),
                "Invalid step returned");

        timePicker.setStep(Duration.ofMinutes(90));
        assertEquals(Duration.ofMinutes(90), timePicker.getStep(),
                "Invalid step returned");
    }

    @Test
    void testSetStep_secondsNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofSeconds(11)));
    }

    @Test
    void testSetStep_minutesNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofMinutes(35)));
    }

    @Test
    void testSetStep_millisecondsNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofMillis(333)));
    }

    @Test
    void testSetStep_hoursNotDivideEvenly_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofHours(5)));
    }

    @Test
    void testSetStep_negativeStep_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofMinutes(-15)));
    }

    @Test
    void testSetStep_lessThan0Ms_throwsException() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> timePicker.setStep(Duration.ofNanos(500_000)));
    }

    @Test
    void setMin_getMin_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMin());
        timePicker.setMin(null);
        assertEquals(null, timePicker.getMin());
    }

    @Test
    void setMax_getMax_null() {
        TimePicker timePicker = new TimePicker();
        assertEquals(null, timePicker.getMax());
        timePicker.setMax(null);
        assertEquals(null, timePicker.getMax());
    }

    @Test
    void clearButtonVisiblePropertyValue() {
        TimePicker timePicker = new TimePicker();

        assertFalse(timePicker.isClearButtonVisible(),
                "Clear button should not be visible by default");
        assertClearButtonPropertyValueEquals(timePicker, true);
        assertClearButtonPropertyValueEquals(timePicker, false);
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-time-picker");

        String value = LocalTime.now().plus(31l, ChronoUnit.MINUTES).toString();
        element.setProperty("value", value);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TimePicker.class))
                .thenAnswer(invocation -> new TimePicker());

        TimePicker field = Component.from(element, TimePicker.class);
        Assertions.assertEquals(value,
                field.getElement().getPropertyRaw("value"));
    }

    public void assertClearButtonPropertyValueEquals(TimePicker timePicker,
            Boolean value) {
        timePicker.setClearButtonVisible(value);
        assertEquals(value, timePicker.isClearButtonVisible());
        assertEquals(timePicker.isClearButtonVisible(), timePicker.getElement()
                .getProperty("clearButtonVisible", value));
    }

    @Test
    void setAutoOpenDisabled() {
        TimePicker timePicker = new TimePicker();
        assertTrue(timePicker.isAutoOpen());
        timePicker.setAutoOpen(false);
        assertTrue(timePicker.getElement().getProperty(PROP_AUTO_OPEN_DISABLED,
                false));
        assertFalse(timePicker.isAutoOpen());
    }

    @Test
    void implementsHasAllowedCharPattern() {
        Assertions.assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new TimePicker().getClass()),
                "TimePicker should support char pattern");
    }

    @Test
    void implementsHasTooltip() {
        TimePicker timePicker = new TimePicker();
        Assertions.assertTrue(timePicker instanceof HasTooltip);
    }

    @Test
    void setPrefix_hasPrefix() {
        TimePicker picker = new TimePicker();
        TestPrefix prefix = new TestPrefix();

        picker.setPrefixComponent(prefix);

        Assertions.assertEquals(prefix, picker.getPrefixComponent());
    }

    @Test
    void setTextAsPrefix_throws() {
        TimePicker picker = new TimePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> picker.setPrefixComponent(new Text("Prefix")));
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(TimePicker.class),
                "Time picker should support aria-label and aria-labelledby");
    }

    @Test
    void setAriaLabel() {
        TimePicker timePicker = new TimePicker();

        timePicker.setAriaLabel("aria-label");
        Assertions.assertTrue(timePicker.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", timePicker.getAriaLabel().get());

        timePicker.setAriaLabel(null);
        Assertions.assertTrue(timePicker.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        TimePicker timePicker = new TimePicker();

        timePicker.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(timePicker.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                timePicker.getAriaLabelledBy().get());

        timePicker.setAriaLabelledBy(null);
        Assertions.assertTrue(timePicker.getAriaLabelledBy().isEmpty());
    }

    @Test
    void unregisterInvalidChangeListenerOnEvent() {
        var timePicker = new TimePicker();

        var listenerInvokedCount = new AtomicInteger(0);
        timePicker.addInvalidChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        timePicker.setInvalid(true);
        timePicker.setInvalid(false);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void implementsInputField() {
        TimePicker field = new TimePicker();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<TimePicker, LocalTime>, LocalTime>);
    }

    @Tag("div")
    private static class TestPrefix extends Component {
    }
}
