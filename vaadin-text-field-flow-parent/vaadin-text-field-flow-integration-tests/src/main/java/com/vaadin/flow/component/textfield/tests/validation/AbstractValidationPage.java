package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;

public abstract class AbstractValidationPage<F extends Component & HasValidation & HasValueAndElement<?, ?>>
        extends Div {
    public static final String ATTACH_FIELD_BUTTON = "attach-field-button";
    public static final String DETACH_FIELD_BUTTON = "detach-field-button";
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";

    protected F field;

    public AbstractValidationPage() {
        field = createField();
        field.setRequiredIndicatorVisible(true);
        add(field);

        addAttachDetachControls();
        addServerValidityStateControls();
    }

    private void addAttachDetachControls() {
        NativeButton attachButton = createButton(ATTACH_FIELD_BUTTON,
                "Attach field", event -> add(field));
        NativeButton detachButton = createButton(DETACH_FIELD_BUTTON,
                "Detach field", event -> add(field));

        add(new Div(attachButton, detachButton));
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
            ValueChangeListener<? super ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setId(id);
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        return input;
    }

    /**
     * A field to test.
     */
    protected abstract F createField();
}
