package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.Locale;

@Route("vaadin-date-time-picker/binder-validation")
public class DateTimePickerBinderValidationPage extends Div {
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";
    public static final String MIN_VALUE_BUTTON = "min-value";
    public static final String MAX_VALUE_BUTTON = "max-value";
    public static final String ATTACH_BINDER_BUTTON = "attach-binder";
    public static final String REQUIRED_BUTTON = "required";

    public static class Bean {
        private LocalDateTime property;

        public LocalDateTime getProperty() {
            return property;
        }

        public void setProperty(LocalDateTime property) {
            this.property = property;
        }
    }

    private LocalDateTime expectedValue;

    private final DateTimePicker field;

    public DateTimePickerBinderValidationPage() {
        field = new DateTimePicker("Label");
        field.setLocale(Locale.US);

        add(field);

        addServerValidityStateControls();

        add(createButton(ATTACH_BINDER_BUTTON, "Attach binder", e -> {
            var binder = new Binder<>(Bean.class);
            binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                    .withValidator(
                            value -> value.equals(
                                    LocalDateTime.of(2022, 1, 1, 12, 0)),
                            UNEXPECTED_VALUE_ERROR_MESSAGE)
                    .bind("property");
        }), createButton(REQUIRED_BUTTON, "Set required",
                e -> field.setRequiredIndicatorVisible(true)),
                createButton(MIN_VALUE_BUTTON, "Set min value",
                        e -> field.setMin(LocalDateTime.of(2022, 1, 1, 10, 0))),
                createButton(MAX_VALUE_BUTTON, "Set max value", e -> field
                        .setMax(LocalDateTime.of(2022, 1, 1, 14, 0))));
    }

    private void addServerValidityStateControls() {
        Div validityState = new Div();
        validityState.setId(SERVER_VALIDITY_STATE);

        NativeButton validityStateButton = createButton(
                SERVER_VALIDITY_STATE_BUTTON, "Retrieve server validity state",
                event -> {
                    boolean isValid = !field.isInvalid();
                    validityState.setText(String.valueOf(isValid));
                });

        add(new Div(validityState, validityStateButton));
    }

    /**
     * A helper to create a native button element.
     */
    protected NativeButton createButton(String id, String title,
            ComponentEventListener<ClickEvent<NativeButton>> listener) {
        NativeButton button = new NativeButton(title, listener);
        button.setId(id);
        return button;
    }
}
