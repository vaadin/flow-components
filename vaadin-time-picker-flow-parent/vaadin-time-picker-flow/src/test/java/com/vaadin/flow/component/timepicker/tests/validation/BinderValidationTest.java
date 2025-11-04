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
package com.vaadin.flow.component.timepicker.tests.validation;

import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;

public class BinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAILED";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    private TimePicker field;

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        field = new TimePicker();
        field.setMax(LocalTime.now().plusHours(1));
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        attachBinderToField();

        field.setValue(LocalTime.now().plusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        attachBinderToField();
        field.setValue(LocalTime.now().minusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnBinder_validate_binderValidationFails() {
        var binder = attachBinderToField(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());

        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_REQUIRED_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnComponent_validate_binderValidationPasses() {
        var binder = attachBinderToField();
        field.setRequiredIndicatorVisible(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationPasses() {
        attachBinderToField();

        field.setValue(LocalTime.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    @Test
    public void validValue_enterUnparsableInput_valueIsPreserved() {
        var binder = attachBinderToField();
        var bean = new Bean();
        var validTime = LocalTime.of(14, 30);
        bean.setTime(validTime);
        binder.setBean(bean);

        // Simulate setting unparsable input
        fakeClientPropertyChange(field, "_inputElementValue", "foobar");
        fakeClientPropertyChange(field, "value", "");
        fakeClientDomEvent("change");

        Assert.assertEquals(
                "Field value should be preserved after entering unparsable input",
                validTime, field.getValue());
        Assert.assertEquals(
                "Binder value should be preserved after entering unparsable input",
                validTime, bean.getTime());
    }

    @Test
    public void validValue_enterUnparsableInput_clearInput_binderValueChangesToNull() {
        var binder = attachBinderToField();
        var bean = new Bean();
        var validTime = LocalTime.of(14, 30);
        bean.setTime(validTime);
        binder.setBean(bean);

        // Simulate setting an invalid input
        fakeClientPropertyChange(field, "_inputElementValue", "foobar");
        fakeClientPropertyChange(field, "value", "");
        fakeClientDomEvent("change");

        // Simulate clearing the invalid input
        fakeClientPropertyChange(field, "_inputElementValue", "");
        fakeClientDomEvent("unparsable-change");

        Assert.assertNull(
                "Field value should be null after clearing unparsable input",
                field.getValue());
        Assert.assertNull(
                "Binder value should be null after clearing unparsable input",
                bean.getTime());
    }

    private Binder<Bean> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<Bean> attachBinderToField(boolean isRequired) {
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalTime> binding = binder.forField(field)
                .withValidator(
                        value -> value == null
                                || value.isAfter(LocalTime.now().minusHours(1)),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        }

        binding.bind("time");

        return binder;
    }

    private void fakeClientDomEvent(String eventName) {
        var element = field.getElement();
        var event = new com.vaadin.flow.dom.DomEvent(element, eventName,
                com.vaadin.flow.internal.JacksonUtils.createObjectNode());
        element.getNode().getFeature(
                com.vaadin.flow.internal.nodefeature.ElementListenerMap.class)
                .fireEvent(event);
    }

    private void fakeClientPropertyChange(
            com.vaadin.flow.component.Component component, String property,
            String value) {
        var element = component.getElement();
        element.getStateProvider().setProperty(element.getNode(), property,
                value, false);
    }
}
