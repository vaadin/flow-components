package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.TimePicker;
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

import java.time.LocalTime;

public class TimePickerBinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAILED";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

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
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        getFieldWithValidation();

        field.setValue(LocalTime.now().plusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        getFieldWithValidation();
        field.setValue(LocalTime.now().minusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnBinder_validate_binderValidationFails() {
        var binder = getFieldWithValidation(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());

        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(REQUIRED_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnComponent_validate_binderValidationPasses() {
        var binder = getFieldWithValidation();
        field.setRequiredIndicatorVisible(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationPasses() {
        getFieldWithValidation();

        field.setValue(LocalTime.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    private Binder<Bean> getFieldWithValidation() {
        return getFieldWithValidation(false);
    }

    private Binder<Bean> getFieldWithValidation(boolean isRequired) {
        field.setMax(LocalTime.now().plusHours(1));
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalTime> binding = binder.forField(field)
                .withValidator(
                        value -> value == null
                                || value.isAfter(LocalTime.now().minusHours(1)),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("time");

        return binder;
    }
}
