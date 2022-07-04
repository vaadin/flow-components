package com.vaadin.flow.component.textfield.tests.validation.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.Binder;

public abstract class AbstractValidationBinderBasicPage<F extends Component & HasValidation & HasValueAndElement<?, V>, V>
        extends Div {
    public static final String REQUIRED_ERROR_MESSAGE = "REQUIRED";

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

    public AbstractValidationBinderBasicPage() {
        field = getField();
        add(field);

        var binder = new Binder<>(Bean.class);
        binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                .bind("property");

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

    protected abstract F getField();
}
