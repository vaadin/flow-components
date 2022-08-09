package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;

public abstract class AbstractValidationPage extends Div {
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";

    protected DatePicker field;

    public AbstractValidationPage() {
        field = new DatePicker();
        add(field);
    }

    protected void addServerValidityStateControls() {
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
            ValueChangeListener<? super ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setId(id);
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        return input;
    }
}
