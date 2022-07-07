package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
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

    protected abstract HasValue<?, T> getField();

    protected abstract void setValidValue();

    protected abstract void setComponentInvalidValue();

    protected abstract void setBinderInvalidValue();

    private HasValue<?, T> field;

    protected abstract SerializablePredicate<? super T> getValidator();

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusMock;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        field = getField();
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        setupFieldWithValidation();
        setComponentInvalidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Validation should fail", status.isError());
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
    public void setRequiredOnBinder_validate_binderValidationFails() {
        var binder = setupFieldWithValidation(true);
        binder.validate();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        var status = statusCaptor.getValue();

        Assert.assertTrue("Binder validation should fail", status.isError());
        Assert.assertEquals(BINDER_REQUIRED_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    public void setRequiredOnComponent_validate_binderValidationPasses() {
        var binder = setupFieldWithValidation();
        field.setRequiredIndicatorVisible(true);
        binder.validate();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_validValue_validationPasses() {
        setupFieldWithValidation();
        setValidValue();

        Mockito.verify(statusMock).statusChange(statusCaptor.capture());
        Assert.assertFalse("Validation should be ok",
                statusCaptor.getValue().isError());
    }

    private Binder<Bean> setupFieldWithValidation() {
        return setupFieldWithValidation(false);
    }

    private Binder<Bean> setupFieldWithValidation(boolean isRequired) {
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
