package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;

public abstract class AbstractValidationBasicPage<F extends Component & HasValidation & HasValueAndElement<?, ?>>
        extends Div {
    protected F field;

    public AbstractValidationBasicPage() {
        field = getField();
        field.setRequiredIndicatorVisible(true);
        add(field);

        addAttachDetachControls();
        addValidityStateControls();
    }

    private void addAttachDetachControls() {
        NativeButton attach = new NativeButton("Attach field",
                event -> add(field));
        attach.setId("attach-field");
        NativeButton detach = new NativeButton("Detach field",
                event -> remove(field));
        detach.setId("detach-field");

        add(new Div(attach, detach));
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
