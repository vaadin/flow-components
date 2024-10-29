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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

import elemental.json.Json;

public class FallbackParserValidationTest {
    private DatePicker datePicker;

    @Before
    public void init() {
        datePicker = new DatePicker();
        datePicker.setFallbackParser((s) -> {
            if (s.equals("tomorrow")) {
                return Result.ok(LocalDate.now().plusDays(1));
            } else {
                return Result.error("Invalid date format");
            }
        });
    }

    @Test
    public void enterShortcutValue_validate_noErrorMessageDisplayed() {
        datePicker.getElement().setProperty("_inputElementValue", "tomorrow");
        fireUnparsableChangeDomEvent();
        Assert.assertFalse(datePicker.isInvalid());
        Assert.assertEquals("", datePicker.getErrorMessage());
    }

    @Test
    public void enterUnparsableValue_validate_fallbackParserErrorMessageDisplayed() {
        datePicker.getElement().setProperty("_inputElementValue", "foobar");
        fireUnparsableChangeDomEvent();
        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("Invalid date format", datePicker.getErrorMessage());
    }

    @Test
    public void setI18nErrorMessage_enterUnparsableValue_validate_fallbackParserErrorMessageDisplayed() {
        datePicker.setI18n(new DatePickerI18n().setBadInputErrorMessage("I18n error message"));
        datePicker.getElement().setProperty("_inputElementValue", "foobar");
        fireUnparsableChangeDomEvent();
        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("Invalid date format", datePicker.getErrorMessage());
    }

    @Test
    public void setI18nErrorMessage_removeFallbackParser_validate_i18nErrorMessageDisplayed() {
        datePicker.setI18n(new DatePickerI18n().setBadInputErrorMessage("I18n error message"));
        datePicker.setFallbackParser(null);
        datePicker.getElement().setProperty("_inputElementValue", "foobar");
        fireUnparsableChangeDomEvent();
        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("I18n error message", datePicker.getErrorMessage());
    }

    private void fireUnparsableChangeDomEvent() {
        DomEvent unparsableChangeDomEvent = new DomEvent(datePicker.getElement(),
                "unparsable-change", Json.createObject());
        datePicker.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(unparsableChangeDomEvent);
    }
}
