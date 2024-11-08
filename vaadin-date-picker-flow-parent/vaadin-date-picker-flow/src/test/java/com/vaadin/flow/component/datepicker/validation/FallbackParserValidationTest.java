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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
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
    public void enterShortcutValue_noErrorMessageDisplayed() {
        fakeClientPropertyChange(datePicker, "_inputElementValue", "tomorrow");
        fakeClientDomEvent(datePicker, "unparsable-change");

        Assert.assertFalse(datePicker.isInvalid());
        Assert.assertEquals("", datePicker.getErrorMessage());
    }

    @Test
    public void enterUnparsableValue_fallbackParserErrorMessageDisplayed() {
        fakeClientPropertyChange(datePicker, "_inputElementValue", "foobar");
        fakeClientDomEvent(datePicker, "unparsable-change");

        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("Invalid date format",
                datePicker.getErrorMessage());
    }

    @Test
    public void setValue_enterShortcutValue_noErrorMessageDisplayed() {
        datePicker.setValue(LocalDate.now());

        fakeClientPropertyChange(datePicker, "_inputElementValue", "tomorrow");
        fakeClientPropertyChange(datePicker, "value", "");

        Assert.assertFalse(datePicker.isInvalid());
        Assert.assertEquals("", datePicker.getErrorMessage());
    }

    @Test
    public void setValue_enterUnparsableValue_fallbackParserErrorMessageDisplayed() {
        datePicker.setValue(LocalDate.now());

        fakeClientPropertyChange(datePicker, "_inputElementValue", "foobar");
        fakeClientPropertyChange(datePicker, "value", "");

        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("Invalid date format",
                datePicker.getErrorMessage());
    }

    @Test
    public void setI18nErrorMessage_enterUnparsableValue_fallbackParserErrorMessageDisplayed() {
        datePicker.setI18n(new DatePickerI18n()
                .setBadInputErrorMessage("I18n error message"));

        fakeClientPropertyChange(datePicker, "_inputElementValue", "foobar");
        fakeClientDomEvent(datePicker, "unparsable-change");

        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("Invalid date format",
                datePicker.getErrorMessage());
    }

    @Test
    public void setI18nErrorMessage_removeFallbackParser_enterUnparsableValue_i18nErrorMessageDisplayed() {
        datePicker.setI18n(new DatePickerI18n()
                .setBadInputErrorMessage("I18n error message"));

        datePicker.setFallbackParser(null);

        fakeClientPropertyChange(datePicker, "_inputElementValue", "foobar");
        fakeClientDomEvent(datePicker, "unparsable-change");

        Assert.assertTrue(datePicker.isInvalid());
        Assert.assertEquals("I18n error message", datePicker.getErrorMessage());
    }

    private void fakeClientDomEvent(Component component, String eventName) {
        Element element = component.getElement();
        DomEvent event = new DomEvent(element, eventName, Json.createObject());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }

    private void fakeClientPropertyChange(Component component, String property,
            String value) {
        Element element = component.getElement();
        element.getStateProvider().setProperty(element.getNode(), property,
                value, false);
    }
}
