package com.vaadin.flow.component.datetimepicker;

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

import java.time.LocalDateTime;

public class DateTimePickerBinderValidationTest {

    private static final String BINDER_FAIL_MESSAGE = "YEAR_LESS_THAN_MIN";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

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
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation();

        field.setValue(LocalDateTime.now().plusDays(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation();

        field.setValue(LocalDateTime.now().minusYears(1));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
            statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromBinder_emptyField_binderValidationFail() {
        var field = getFieldWithValidation(true);
        field.setValue(LocalDateTime.now());
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2)).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(REQUIRED_MESSAGE,
            statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        var field = getFieldWithValidation();
        field.setRequiredIndicatorVisible(true);
        field.setValue(LocalDateTime.now());
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2)).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation();

        field.setValue(LocalDateTime.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());

    }

    private DateTimePicker getFieldWithValidation() {
        return getFieldWithValidation(false);
    }

    private DateTimePicker getFieldWithValidation(boolean isRequired) {
        var field = new DateTimePicker();
        field.setMax(LocalDateTime.now().plusDays(1));
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalDateTime> binding = binder
                .forField(field)
                .withValidator(date -> date == null
                        || date.getYear() >= LocalDateTime.now().getYear(),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("date");

        return field;
    }
}
