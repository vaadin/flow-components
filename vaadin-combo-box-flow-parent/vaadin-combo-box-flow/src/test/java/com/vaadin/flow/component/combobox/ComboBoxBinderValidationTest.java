package com.vaadin.flow.component.combobox;

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
import java.util.Objects;

public class ComboBoxBinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "YEAR_LESS_THAN_MIN";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    public static class Bean {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation();

        field.setValue("bar");
        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());

        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(BINDER_FAIL_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromBinder_emptyField_binderValidationFail() {
        var field = getFieldWithValidation(true);
        field.setValue("foo");
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2))
                .statusChange(statusCaptor.capture());
        Assert.assertTrue(statusCaptor.getValue().isError());
        Assert.assertEquals(REQUIRED_MESSAGE,
                statusCaptor.getValue().getMessage().orElse(""));
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        var field = getFieldWithValidation();
        field.setRequiredIndicatorVisible(true);
        field.setValue("foo");
        field.setValue(null);

        Mockito.verify(statusHandlerMock, Mockito.times(2))
                .statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());

    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation();

        field.setValue("foo");

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    private ComboBox<String> getFieldWithValidation() {
        return getFieldWithValidation(false);
    }

    private ComboBox<String> getFieldWithValidation(boolean isRequired) {
        var field = new ComboBox<String>();
        field.setItems(Arrays.asList("foo", "bar", "baz"));
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(
                        value -> value == null || Objects.equals(value, "foo"),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("value");

        return field;
    }
}
