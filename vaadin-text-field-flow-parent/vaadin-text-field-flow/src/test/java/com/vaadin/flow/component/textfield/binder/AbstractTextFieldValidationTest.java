package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.shared.ValidationError;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

public abstract class AbstractTextFieldValidationTest<T> {

    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAIL";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean<T> {
        private T property;

        public T getProperty() {
            return property;
        }

        public void setProperty(T property) {
            this.property = property;
        }
    }

    protected abstract HasValue<?, T> getField();

    protected abstract void setValidValue();

    protected abstract void setComponentInvalidValue();

    protected abstract void setBinderInvalidValue();

    protected abstract void setEmptyValue();

    protected abstract SerializablePredicate<? super T> getValidator();

    protected String fieldConstraintErrorMessage = ValidationError.GREATER_THAN_MAX;

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        setupFieldWithValidation();
        setComponentInvalidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Validation should fail", status.isError());
        Assert.assertEquals(fieldConstraintErrorMessage,
                status.getMessage().orElse(""));
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        setupFieldWithValidation();
        setBinderInvalidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Binder validation should fail", status.isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromBinder_emptyField_binderValidationFail() {
        setupFieldWithValidation(true);
        setValidValue();
        setEmptyValue();

        Mockito.verify(statusMock, Mockito.times(2))
                .statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Binder validation should fail", status.isError());
        Assert.assertEquals(REQUIRED_MESSAGE, status.getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        setValidValue();
        setupFieldWithValidation();
        getField().setRequiredIndicatorVisible(true);
        setEmptyValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        setupFieldWithValidation();
        setValidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    private void setupFieldWithValidation() {
        setupFieldWithValidation(false);
    }

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusMock;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    private void setupFieldWithValidation(boolean isRequired) {
        var field = getField();
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(getValidator(), BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusMock);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("property");
    }
}
