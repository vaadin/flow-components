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
package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

import elemental.json.Json;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<DatePicker, LocalDate> {
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
        testField.setI18n(new DatePicker.DatePickerI18n()
                .setBadInputErrorMessage("Date has invalid format"));
        testField.getElement().setProperty("_hasInputValue", true);
        fireUnparsableChangeDomEvent();
        Assert.assertEquals("Date has invalid format",
                testField.getErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(LocalDate.now());
        testField.setValue(null);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new DatePicker.DatePickerI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(LocalDate.now());
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(LocalDate.now());
        testField.setValue(LocalDate.now().minusDays(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(LocalDate.now());
        testField.setI18n(new DatePicker.DatePickerI18n()
                .setMinErrorMessage("Date is too small"));
        testField.setValue(LocalDate.now().minusDays(1));
        Assert.assertEquals("Date is too small", testField.getErrorMessage());
    }

    @Test
    public void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(LocalDate.now());
        testField.setValue(LocalDate.now().plusDays(1));
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(LocalDate.now());
        testField.setI18n(new DatePicker.DatePickerI18n()
                .setMaxErrorMessage("Date is too big"));
        testField.setValue(LocalDate.now().plusDays(1));
        Assert.assertEquals("Date is too big", testField.getErrorMessage());
    }

    @Override
    protected DatePicker createTestField() {
        return new DatePicker();
    }

    private void fireUnparsableChangeDomEvent() {
        DomEvent unparsableChangeDomEvent = new DomEvent(testField.getElement(),
                "unparsable-change", Json.createObject());
        testField.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(unparsableChangeDomEvent);
    }
}
