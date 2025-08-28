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
package com.vaadin.flow.component.textfield.validation;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class NumberFieldBasicValidationTest
        extends AbstractBasicValidationTest<NumberField, Double> {
    @Test
    public void addValidationStatusChangeListener_addAnotherListenerOnInvocation_noExceptions() {
        testField.addValidationStatusChangeListener(event1 -> {
            testField.addValidationStatusChangeListener(event2 -> {
            });
        });

        // Trigger ValidationStatusChangeEvent
        fakeClientPropertyChange(testField, "_inputElementValue", "NaN");
        testField.clear();
    }

    @Test
    public void badInput_validate_emptyErrorMessageDisplayed() {
        fakeClientPropertyChange(testField, "_inputElementValue", "NaN");
        fakeClientDomEvent(testField, "unparsable-change");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setBadInputErrorMessage("Value has invalid format"));
        fakeClientPropertyChange(testField, "_inputElementValue", "NaN");
        fakeClientDomEvent(testField, "unparsable-change");
        Assert.assertEquals("Value has invalid format",
                testField.getErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(1.0);
        testField.setValue(null);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(1.0);
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void min_validate_emptyErrorMessageDisplayed() {
        testField.setMin(3.0);
        testField.setValue(1.0);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void min_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMin(3.0);
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setMinErrorMessage("Value is too small"));
        testField.setValue(1.0);
        Assert.assertEquals("Value is too small", testField.getErrorMessage());
    }

    @Test
    public void max_validate_emptyErrorMessageDisplayed() {
        testField.setMax(1.0);
        testField.setValue(3.0);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void max_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMax(1.0);
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setMaxErrorMessage("Value is too big"));
        testField.setValue(3.0);
        Assert.assertEquals("Value is too big", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(1.0);
        testField.setValue(null);
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new NumberField.NumberFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(1.0);
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue(1.0);
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected NumberField createTestField() {
        return new NumberField();
    }

    private void fakeClientDomEvent(Component component, String eventName) {
        Element element = component.getElement();
        DomEvent event = new DomEvent(element, eventName,
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }

    private void fakeClientPropertyChange(Component component, String property,
            String value) {
        Element element = component.getElement();
        element.getStateProvider().setProperty(element.getNode(), property,
                value, false);
    }
}
