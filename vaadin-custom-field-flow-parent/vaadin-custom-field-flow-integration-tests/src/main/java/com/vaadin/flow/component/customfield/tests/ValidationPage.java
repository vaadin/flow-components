/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field/validation")
public class ValidationPage extends Div {
    public ValidationPage() {
        MyField customField = new MyField();

        NativeButton setInvalid = new NativeButton("Set invalid", e -> {
            customField.setInvalid(true);
            customField.setErrorMessage("Error message from server");
        });
        setInvalid.setId("set-invalid");

        NativeButton attach = new NativeButton("Attach", e -> {
            add(customField);
        });
        attach.setId("attach");

        NativeButton detach = new NativeButton("Detach", e -> {
            remove(customField);
        });
        detach.setId("detach");

        Span logOutput = new Span();
        logOutput.setId("log-output");
        NativeButton logInvalidState = new NativeButton("Log Invalid State",
                e -> {
                    logOutput.setText(String.valueOf(customField.isInvalid()));
                });
        logInvalidState.setId("log-invalid-state");

        add(customField);
        add(new Div(setInvalid, attach, detach, logInvalidState, logOutput));
    }

    private class MyField extends CustomField<Integer> {
        final TextField field1 = new TextField();
        final TextField field2 = new TextField();

        MyField() {
            field1.setId("field1");
            field2.setId("field2");
            add(field1, field2);
        }

        @Override
        protected Integer generateModelValue() {
            try {
                int i1 = Integer.valueOf(field1.getValue());
                int i2 = Integer.valueOf(field2.getValue());
                return i1 + i2;
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        protected void setPresentationValue(Integer integer) {

        }
    }
}
