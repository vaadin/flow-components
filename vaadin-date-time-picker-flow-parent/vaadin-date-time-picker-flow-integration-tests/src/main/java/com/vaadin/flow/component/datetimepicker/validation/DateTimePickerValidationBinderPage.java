package com.vaadin.flow.component.datetimepicker.validation;

import java.time.LocalDateTime;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/validation/binder")
public class DateTimePickerValidationBinderPage extends AbstractValidationPage {
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

    public static class Bean {
        private LocalDateTime property;

        public LocalDateTime getProperty() {
            return property;
        }

        public void setProperty(LocalDateTime property) {
            this.property = property;
        }
    }

    protected Binder<?> binder;

    private LocalDateTime expectedValue;

    public DateTimePickerValidationBinderPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected date time",
                event -> {
                    var value = LocalDateTime.parse(event.getValue());
                    expectedValue = value;
                }));

        add(createInput(MIN_INPUT, "Set min date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            field.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            field.setMax(value);
        }));
    }
}
