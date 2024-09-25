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
package com.vaadin.flow.component.textfield.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import com.vaadin.flow.function.SerializablePredicate;

public abstract class AbstractBinderValidationTest<T, K extends Component & HasValue<?, T>> {

    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAIL";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean<T> {
        private T property;

        public T getProperty() {
            return property;
        }

        public void setProperty(T property) {
            this.property = property;
        }
    }

    protected abstract void initField();

    protected abstract void setValidValue();

    protected abstract void setComponentInvalidValue();

    protected abstract void setBinderInvalidValue();

    protected K field;

    protected abstract SerializablePredicate<? super T> getValidator();

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusMock;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        initField();
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        attachBinderToField();
        setComponentInvalidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Validation should fail", status.isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        attachBinderToField();
        setBinderInvalidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Binder validation should fail", status.isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnBinder_validate_binderValidationFails() {
        var binder = attachBinderToField(true);
        binder.validate();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Binder validation should fail", status.isError());
        Assert.assertEquals(BINDER_REQUIRED_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnComponent_validate_binderValidationPasses() {
        var binder = attachBinderToField();
        field.setRequiredIndicatorVisible(true);
        binder.validate();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationPasses() {
        attachBinderToField();
        setValidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    private Binder<?> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<?> attachBinderToField(boolean isRequired) {
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(getValidator(), BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        }

        binding.bind("property");

        return binder;
    }
}
