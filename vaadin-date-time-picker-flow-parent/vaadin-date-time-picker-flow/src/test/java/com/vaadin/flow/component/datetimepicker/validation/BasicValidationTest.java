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
package com.vaadin.flow.component.datetimepicker.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

class BasicValidationTest
        extends AbstractBasicValidationTest<DateTimePicker, LocalDateTime> {

    @Test
    void badInputOnDatePicker_validate_emptyErrorMessageDisplayed() {
        getDatePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getDatePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void badInputOnTimePicker_validate_emptyErrorMessageDisplayed() {
        getTimePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getTimePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void badInputOnDatePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value has invalid format";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage(errorMessage));
        getDatePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getDatePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    void badInputOnTimePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value has invalid format";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage(errorMessage));
        getTimePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getTimePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    void incompleteInputOnDatePicker_validate_emptyErrorMessageDisplayed() {
        var picker = getDatePicker();
        picker.setValue(LocalDate.now());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void incompleteInputOnTimePicker_validate_emptyErrorMessageDisplayed() {
        var picker = getTimePicker();
        picker.setValue(LocalTime.now());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void incompleteInputOnDatePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        var picker = getDatePicker();
        picker.setValue(LocalDate.now());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    void incompleteInputOnTimePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        var picker = getTimePicker();
        picker.setValue(LocalTime.now());
        fireUnparsableChangeDomEvent();
        Assertions.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    void setIncompleteInputErrorMessage_errorMessageIsSet() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        Assertions.assertEquals(errorMessage,
                testField.getI18n().getIncompleteInputErrorMessage());
    }

    @Test
    void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Test
    void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().minusDays(1));
        fireChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMinErrorMessage("Value is too small"));
        testField.setValue(LocalDateTime.now().minusDays(1));
        fireChangeDomEvent();
        Assertions.assertEquals("Value is too small",
                testField.getErrorMessage());
    }

    @Test
    void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().plusDays(1));
        fireChangeDomEvent();
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMaxErrorMessage("Value is too big"));
        testField.setValue(LocalDateTime.now().plusDays(1));
        fireChangeDomEvent();
        Assertions.assertEquals("Value is too big",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assertions.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        testField.setErrorMessage("");
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Test
    void setInvalid_nestedPickersAreInvalid() {
        testField.setInvalid(true);
        Assertions.assertTrue(getDatePicker().isInvalid());
        Assertions.assertTrue(getTimePicker().isInvalid());
    }

    @Test
    void setValueProgrammatically_fieldValidatedOnce() {
        var dateTimePicker = new TestDateTimePicker();
        dateTimePicker.setValue(LocalDateTime.now());
        Assertions.assertEquals(1, dateTimePicker.getValidationCount());
    }

    @Test
    void clearValueProgrammatically_fieldValidatedOnce() {
        var dateTimePicker = new TestDateTimePicker();
        dateTimePicker.setValue(LocalDateTime.now());
        var validationCount = dateTimePicker.getValidationCount();
        dateTimePicker.setValue(null);
        Assertions.assertEquals(validationCount + 1,
                dateTimePicker.getValidationCount());
    }

    @Test
    void setValueProgrammatically_invalidStateIsUpdatedInValueChangeListener() {
        var isInvalid = new AtomicBoolean();
        testField.addValueChangeListener(
                e -> isInvalid.set(e.getSource().isInvalid()));
        testField.setMax(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().plusDays(1));
        Assertions.assertTrue(isInvalid.get());
    }

    @Test
    void incompleteInput_setValidValueProgrammatically_invalidStateCleared() {
        // Simulate incomplete input: date picker has value, time picker is
        // empty
        getDatePicker().setValue(LocalDate.of(2000, 1, 1));
        fireUnparsableChangeDomEvent();
        Assertions.assertTrue(testField.isInvalid(),
                "Field should be invalid with incomplete input");

        // Set a valid complete value programmatically
        testField.setValue(LocalDateTime.of(2000, 1, 1, 12, 0));

        Assertions.assertFalse(testField.isInvalid(),
                "Field should be valid after setting complete value");
    }

    @Override
    protected DateTimePicker createTestField() {
        return new DateTimePicker();
    }

    private DatePicker getDatePicker() {
        return (DatePicker) SlotUtils.getChildInSlot(testField, "date-picker");
    }

    private TimePicker getTimePicker() {
        return (TimePicker) SlotUtils.getChildInSlot(testField, "time-picker");
    }

    private void fireChangeDomEvent() {
        fireDomEvent("change", testField.getElement());
    }

    private void fireUnparsableChangeDomEvent() {
        fireDomEvent("unparsable-change", testField.getElement());
    }

    private void fireDomEvent(String eventType, Element element) {
        var domEvent = new DomEvent(element, eventType,
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(domEvent);
    }

    private class TestDateTimePicker extends DateTimePicker {
        private final AtomicInteger validationCount = new AtomicInteger(0);

        @Override
        protected void validate() {
            super.validate();
            validationCount.incrementAndGet();
        }

        public int getValidationCount() {
            return validationCount.get();
        }
    }
}
