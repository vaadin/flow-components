package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CheckboxGroupBinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_FAIL_MESSAGE";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    private CheckboxGroup<String> field;

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private Set<String> value;

        public Set<String> getValue() {
            return value;
        }

        public void setValue(Set<String> value) {
            this.value = value;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        field = new CheckboxGroup<>();
        field.setItems(Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    public void elementWithBinderValidation_invalidValue_binderValidationFails() {
        attachBinderToField();

        field.setValue(Collections.singleton("bar"));
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
    public void setValidValue_binderValidationPasses() {
        attachBinderToField();

        field.setValue(Collections.singleton("foo"));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    private Binder<Bean> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<Bean> attachBinderToField(boolean isRequired) {
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(
                        value -> value == null || value.isEmpty()
                                || (value.size() == 1 && value.contains("foo")),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        }

        binding.bind("value");

        return binder;
    }
}
