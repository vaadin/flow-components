/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

import elemental.json.Json;

public class BasicValidationTest
        extends AbstractBasicValidationTest<DateTimePicker, LocalDateTime> {

    @Test
    public void badInputOnDatePicker_validate_emptyErrorMessageDisplayed() {
        getDatePicker().getElement().setProperty("_inputElementValue", "foo");
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInputOnTimePicker_validate_emptyErrorMessageDisplayed() {
        getTimePicker().getElement().setProperty("_inputElementValue", "foo");
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInputOnDatePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value has invalid format";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage(errorMessage));
        getDatePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getDatePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    public void badInputOnTimePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value has invalid format";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage(errorMessage));
        getTimePicker().getElement().setProperty("_inputElementValue", "foo");
        fireDomEvent("unparsable-change", getTimePicker().getElement());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    public void incompleteInputOnDatePicker_validate_emptyErrorMessageDisplayed() {
        var picker = getDatePicker();
        picker.setValue(LocalDate.now());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void incompleteInputOnTimePicker_validate_emptyErrorMessageDisplayed() {
        var picker = getTimePicker();
        picker.setValue(LocalTime.now());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void incompleteInputOnDatePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        var picker = getDatePicker();
        picker.setValue(LocalDate.now());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    public void incompleteInputOnTimePicker_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        var picker = getTimePicker();
        picker.setValue(LocalTime.now());
        fireUnparsableChangeDomEvent();
        Assert.assertEquals(errorMessage, testField.getErrorMessage());
    }

    @Test
    public void setIncompleteInputErrorMessage_errorMessageIsSet() {
        var errorMessage = "Value is incomplete";
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setIncompleteInputErrorMessage(errorMessage));
        Assert.assertEquals(errorMessage,
                testField.getI18n().getIncompleteInputErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().minusDays(1));
        fireChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMinErrorMessage("Value is too small"));
        testField.setValue(LocalDateTime.now().minusDays(1));
        fireChangeDomEvent();
        Assert.assertEquals("Value is too small", testField.getErrorMessage());
    }

    @Test
    public void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setValue(LocalDateTime.now().plusDays(1));
        fireChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(LocalDateTime.now());
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setMaxErrorMessage("Value is too big"));
        testField.setValue(LocalDateTime.now().plusDays(1));
        fireChangeDomEvent();
        Assert.assertEquals("Value is too big", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
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
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        testField.setErrorMessage("");
        testField.setValue(LocalDateTime.now());
        fireChangeDomEvent();
        testField.setValue(null);
        fireChangeDomEvent();
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void setInvalid_nestedPickersAreInvalid() {
        testField.setInvalid(true);
        Assert.assertTrue(getDatePicker().isInvalid());
        Assert.assertTrue(getTimePicker().isInvalid());
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
        var domEvent = new DomEvent(element, eventType, Json.createObject());
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(domEvent);
    }
}
