package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import java.time.LocalTime;
import java.util.Locale;

@Route("vaadin-time-picker/validation-binder")
public class TimePickerValidationBinderPage extends Div {
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";
    public static final String MIN_VALUE_BUTTON = "MIN_VALUE";
    public static final String MAX_VALUE_BUTTON = "MAX_VALUE";
    public static final String ATTACH_BINDER_BUTTON = "ATTACH_BINDER";
    public static final String REQUIRED_BUTTON = "required";

    public static class Bean {
        private LocalTime property;

        public LocalTime getProperty() {
            return property;
        }

        public void setProperty(LocalTime property) {
            this.property = property;
        }
    }

    private LocalTime expectedValue;

    private final TimePicker field;

    public TimePickerValidationBinderPage() {
        field = new TimePicker("Label");
        field.setLocale(Locale.US);

        add(field);

        addServerValidityStateControls();

        add(createButton(ATTACH_BINDER_BUTTON, "Attach binder", e -> {
            var binder = new Binder<>(Bean.class);
            binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                    .withValidator(value -> value.equals(LocalTime.of(12, 0)),
                            UNEXPECTED_VALUE_ERROR_MESSAGE)
                    .bind("property");
        }), createButton(REQUIRED_BUTTON, "Set required",
                e -> field.setRequired(true)),
                createButton(MIN_VALUE_BUTTON, "Set min value",
                        e -> field.setMin(LocalTime.of(10, 0))),
                createButton(MAX_VALUE_BUTTON, "Set max value",
                        e -> field.setMax(LocalTime.of(14, 0))));
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

    /**
     * A helper to create a native input element.
     */
    protected Input createInput(String id, String placeholder,
            HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setId(id);
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        return input;
    }
}
