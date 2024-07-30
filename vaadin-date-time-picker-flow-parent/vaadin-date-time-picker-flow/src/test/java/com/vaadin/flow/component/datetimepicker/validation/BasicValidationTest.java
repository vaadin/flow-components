/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import elemental.json.Json;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<DateTimePicker, LocalDateTime> {
    @Test
    public void badInput_validate_emptyErrorMessageDisplayed() {
        getDatePicker().getElement().setProperty("_hasInputValue", true);
        fireValidatedDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage("Value has invalid format"));
        getDatePicker().getElement().setProperty("_hasInputValue", true);
        fireValidatedDomEvent();
        Assert.assertEquals("Value has invalid format",
                testField.getErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(LocalDateTime.now());
        testField.setValue(null);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(LocalDateTime.now());
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().minusDays(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMinErrorMessage("Value is too small"));
        testField.setValue(LocalDateTime.now().minusDays(1));
        Assert.assertEquals("Value is too small", testField.getErrorMessage());
    }

    @Test
    public void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().plusDays(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMaxErrorMessage("Value is too big"));
        testField.setValue(LocalDateTime.now().plusDays(1));
        Assert.assertEquals("Value is too big", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalDateTime.now());
        testField.setValue(null);
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalDateTime.now());
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue(LocalDateTime.now());
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected DateTimePicker createTestField() {
        return new DateTimePicker();
    }

    private DatePicker getDatePicker() {
        return (DatePicker) SlotUtils.getChildInSlot(testField, "date-picker");
    }

    private void fireValidatedDomEvent() {
        DomEvent validatedDomEvent = new DomEvent(testField.getElement(),
                "validated", Json.createObject());
        testField.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(validatedDomEvent);
    }
}
