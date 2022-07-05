package com.vaadin.flow.component.datepicker;

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

import java.time.LocalDate;

public class DatePickerBinderValidationTest {

    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAILED";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private LocalDate date;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation(status -> {
            // Assert.assertTrue(status.isError());
            // Assert.assertEquals(ValidationError.GREATER_THAN_MAX,
            // status.getMessage().orElse(""));
        });

        field.setValue(LocalDate.now().plusDays(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());

    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue(LocalDate.now().minusYears(1));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromBinder_emptyField_binderValidationFail() {
        var field = getFieldWithValidation(status -> {
            if (status.getField().isEmpty()) {
                Assert.assertTrue(status.isError());
                Assert.assertEquals(REQUIRED_MESSAGE,
                        status.getMessage().orElse(""));
            }
        }, true);
        field.setValue(LocalDate.now());
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2))
                .statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(REQUIRED_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        var field = getFieldWithValidation(status -> {
            if (status.getField().isEmpty()) {
                Assert.assertFalse(status.isError());
            }
        });
        field.setRequiredIndicatorVisible(true);
        field.setValue(LocalDate.now());
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2))
                .statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());

    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue(LocalDate.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());

    }

    private DatePicker getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private DatePicker getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new DatePicker();
        field.setMax(LocalDate.now().plusDays(1));
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalDate> binding = binder.forField(field)
                .withValidator(
                        date -> date == null
                                || date.getYear() >= LocalDate.now().getYear(),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("date");

        return field;
    }
}
