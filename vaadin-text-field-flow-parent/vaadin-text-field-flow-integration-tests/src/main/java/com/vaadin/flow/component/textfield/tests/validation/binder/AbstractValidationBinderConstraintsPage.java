package com.vaadin.flow.component.textfield.tests.validation.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.Binder;

public abstract class AbstractValidationBinderConstraintsPage<F extends Component & HasValidation & HasValueAndElement<?, V>, V>
        extends Div {
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

    public AbstractValidationBinderConstraintsPage() {
        field = getField();
        add(field);

        binder = new Binder<>(Bean.class);

        addValidityStateControls();
    }

    private void addValidityStateControls() {
        Div validityState = new Div();
        validityState.setId("validity-state");

        NativeButton retrieveValidityState = new NativeButton(
                "Retrieve server validity state", event -> {
                    boolean isValid = !field.isInvalid();
                    validityState.setText(String.valueOf(isValid));
                });
        retrieveValidityState.setId("retrieve-validity-state");

        add(new Div(validityState, retrieveValidityState));
    }

    protected void addInputControl(String id, String placeholder,
            ValueChangeListener<? super ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setId(id);
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        add(input);
    }

    protected abstract F getField();
}
