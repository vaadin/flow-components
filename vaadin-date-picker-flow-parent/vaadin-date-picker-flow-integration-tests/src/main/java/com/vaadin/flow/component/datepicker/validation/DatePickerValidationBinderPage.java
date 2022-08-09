package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/validation/binder")
public class DatePickerValidationBinderPage extends AbstractValidationPage {
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

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

    protected Binder<?> binder;

    private LocalDate expectedValue;

    public DatePickerValidationBinderPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected date", event -> {
            var value = LocalDate.parse(event.getValue());
            expectedValue = value;
        }));

        add(createInput(MIN_INPUT, "Set min date", event -> {
            var value = LocalDate.parse(event.getValue());
            field.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date", event -> {
            var value = LocalDate.parse(event.getValue());
            field.setMax(value);
        }));

        addServerValidityStateControls();
    }
}
