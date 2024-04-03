package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CheckboxBinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_FAIL_MESSAGE";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    private Checkbox field;

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private Boolean value;

        public Boolean getValue() {
            return value;
        }

        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        field = new Checkbox();
    }

    @Test
    public void elementWithBinderValidation_invalidValue_binderValidationFails() {
        var binder = attachBinderToField();

        field.setValue(true);
        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());

        Assert.assertTrue(statusCaptor.getValue().isError());
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
    public void setRequiredOnBinder_setValidValue_binderValidationPasses() {
        attachBinderToField(true);

        field.setValue(true);

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    private Binder<Bean> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<Bean> attachBinderToField(boolean isRequired) {
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        } else {
            // Consider un-checked as valid
            binding.withValidator(value -> Boolean.FALSE.equals(value),
                    BINDER_FAIL_MESSAGE);
        }

        binding.bind("value");

        return binder;
    }
}
