package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-picker/validation/binder")
public class BinderValidationPage extends AbstractValidationPage<DatePicker> {
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";
    public static final String RESET_BEAN_BUTTON = "reset-bean-button";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

    public static class Bean {
        private LocalDate property;

        public LocalDate getProperty() {
            return property;
        }

        public void setProperty(LocalDate property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private LocalDate expectedValue;

    public BinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected date", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            expectedValue = value;
        }));

        add(createInput(MIN_INPUT, "Set min date", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));

        add(createButton(RESET_BEAN_BUTTON, "Reset bean", event -> {
            binder.setBean(new Bean());
        }));
    }

    @Override
    protected DatePicker createTestField() {
        return new DatePicker();
    }
}
