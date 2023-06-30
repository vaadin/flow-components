package com.vaadin.flow.component.datetimepicker;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;

public class DateTimePickerBinderValidationTest {

    @Tag("test-date-time-picker")
    private class TestDateTimePicker extends DateTimePicker {
        protected boolean isEnforcedFieldValidationEnabled() {
            return true;
        }
    }

    private static final String BINDER_FAIL_MESSAGE = "BINDER_FAIL_MESSAGE";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    private TestDateTimePicker field;

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private LocalDateTime date;

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        field = new TestDateTimePicker();
        field.setMax(LocalDateTime.now().plusDays(1));
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        attachBinderToField();

        field.setValue(LocalDateTime.now().plusDays(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        attachBinderToField();

        field.setValue(LocalDateTime.now().minusYears(1));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnBinder_validate_binderValidationFails() {
        Binder<Bean> binder = attachBinderToField(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_REQUIRED_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnComponent_validate_binderValidationPasses() {
        Binder<Bean> binder = attachBinderToField();
        field.setRequiredIndicatorVisible(true);
        binder.validate();

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationPasses() {
        attachBinderToField();

        field.setValue(LocalDateTime.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());

    }

    private Binder<Bean> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<Bean> attachBinderToField(boolean isRequired) {
        Binder<Bean> binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalDateTime> binding = binder
                .forField(field)
                .withValidator(date -> date == null
                        || date.getYear() >= LocalDateTime.now().getYear(),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        }

        binding.bind("date");

        return binder;
    }
}