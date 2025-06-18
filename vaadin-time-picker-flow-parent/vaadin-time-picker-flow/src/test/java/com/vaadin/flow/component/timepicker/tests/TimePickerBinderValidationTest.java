/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;

public class TimePickerBinderValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAILED";
    private static final String BINDER_REQUIRED_MESSAGE = "REQUIRED";

    private TimePicker field;

    @Captor
    private ArgumentCaptor<BindingValidationStatus<?>> statusCaptor;

    @Mock
    private BindingValidationStatusHandler statusHandlerMock;

    @Tag("test-time-picker")
    private class TestTimePicker extends TimePicker {
        protected boolean isFeatureFlagEnabled(Feature feature) {
            if (feature.getId() == FeatureFlags.ENFORCE_FIELD_VALIDATION
                    .getId()) {
                return true;
            }

            return super.isFeatureFlagEnabled(feature);
        }
    }

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
        field = new TestTimePicker();
        field.setMax(LocalTime.now().plusHours(1));
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        attachBinderToField();

        field.setValue(LocalTime.now().plusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        attachBinderToField();
        field.setValue(LocalTime.now().minusHours(2));

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertTrue("Validation should fail",
                statusCaptor.getValue().isError());
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
    public void elementWithConstraints_validValue_validationPasses() {
        attachBinderToField();

        field.setValue(LocalTime.now());

        Mockito.verify(statusHandlerMock).statusChange(statusCaptor.capture());
        Assert.assertFalse(statusCaptor.getValue().isError());
    }

    private Binder<Bean> attachBinderToField() {
        return attachBinderToField(false);
    }

    private Binder<Bean> attachBinderToField(boolean isRequired) {
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalTime> binding = binder.forField(field)
                .withValidator(
                        value -> value == null
                                || value.isAfter(LocalTime.now().minusHours(1)),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(statusHandlerMock);

        if (isRequired) {
            binding.asRequired(BINDER_REQUIRED_MESSAGE);
        }

        binding.bind("time");

        return binder;
    }
}
