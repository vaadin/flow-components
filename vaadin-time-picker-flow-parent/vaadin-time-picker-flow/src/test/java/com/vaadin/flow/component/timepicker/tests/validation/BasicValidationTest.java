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
package com.vaadin.flow.component.timepicker.tests.validation;

import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

import elemental.json.Json;

public class BasicValidationTest
        extends AbstractBasicValidationTest<TimePicker, LocalTime> {
    @Test
    public void addValidationStatusChangeListener_addAnotherListenerOnInvocation_noExceptions() {
        testField.addValidationStatusChangeListener(event1 -> {
            testField.addValidationStatusChangeListener(event2 -> {
            });
        });

        // Trigger ValidationStatusChangeEvent
        testField.getElement().setProperty("_hasInputValue", true);
        testField.clear();
    }

    @Test
    public void badInput_validate_emptyErrorMessageDisplayed() {
        testField.getElement().setProperty("_hasInputValue", true);
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setBadInputErrorMessage("Time has invalid format"));
        testField.getElement().setProperty("_hasInputValue", true);
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("Time has invalid format",
                testField.getErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(LocalTime.now());
        testField.setValue(null);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(LocalTime.now());
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(LocalTime.now());
        testField.setValue(LocalTime.now().minusHours(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(LocalTime.now());
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setMinErrorMessage("Time is too small"));
        testField.setValue(LocalTime.now().minusHours(1));
        Assert.assertEquals("Time is too small", testField.getErrorMessage());
    }

    @Test
    public void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(LocalTime.now());
        testField.setValue(LocalTime.now().plusHours(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(LocalTime.now());
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setMaxErrorMessage("Time is too big"));
        testField.setValue(LocalTime.now().plusHours(1));
        Assert.assertEquals("Time is too big", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalTime.now());
        testField.setValue(null);
        Assert.assertEquals("Custom error message", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new TimePicker.TimePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(LocalTime.now());
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue(LocalTime.now());
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    protected TimePicker createTestField() {
        return new TimePicker();
    }

    private void fireUnparsableChangeDomEvent() {
        DomEvent unparsableChangeDomEvent = new DomEvent(testField.getElement(),
                "unparsable-change", Json.createObject());
        testField.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(unparsableChangeDomEvent);
    }
}
