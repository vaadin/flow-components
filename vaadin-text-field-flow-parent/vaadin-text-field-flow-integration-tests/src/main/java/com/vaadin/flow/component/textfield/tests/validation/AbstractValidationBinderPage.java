package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.Binder;

public abstract class AbstractValidationBinderPage<F extends Component & HasValidation & HasValueAndElement<?, V>, V>
        extends Div {
    public static final String SERVER_VALIDITY_STATE = "server-validity-state";
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

    public static class Bean<V> {
        private V property;

        public V getProperty() {
            return property;
        }

        public void setProperty(V property) {
            this.property = property;
        }
    }

    protected F field;

    protected Binder<?> binder;

    private String expectedValue;

    public AbstractValidationBinderPage() {
        field = createField();

        binder = new Binder<>(Bean.class);
        binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        add(field);

        addServerValidityStateControls();

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = event.getValue();
        }));
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
