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

@Route("vaadin-custom-field/custom-helper")
public class CustomHelperView extends Div {
    public CustomHelperView() {
        NameField fieldHelper = new NameField();
        fieldHelper.setHelperText("Helper text");
        fieldHelper.setId("custom-field-helper-text");

        NativeButton clearText = new NativeButton("Clear helper", e -> {
            fieldHelper.setHelperText(null);
        });
        clearText.setId("button-clear-helper");

        NameField fieldHelperComponent = new NameField();
        fieldHelperComponent.setId("custom-field-helper-component");

        Span span = new Span("Helper component");
        span.setId("helper-component");
        fieldHelperComponent.setHelperComponent(span);

        NativeButton clearComponent = new NativeButton("Clear helper component",
                e -> {
                    fieldHelperComponent.setHelperComponent(null);
                });
        clearComponent.setId("button-clear-helper-component");

        NameField fieldHelperComponentLazy = new NameField();
        fieldHelperComponentLazy.setId("custom-field-helper-component-lazy");

        NativeButton addComponent = new NativeButton("Add helper component",
                e -> {
                    Span lazyHelper = new Span("Lazy helper component");
                    lazyHelper.setId("helper-component-lazy");
                    fieldHelperComponentLazy.setHelperComponent(lazyHelper);
                });
        addComponent.setId("button-add-helper-component");

        add(fieldHelper, clearText, fieldHelperComponent,
                fieldHelperComponentLazy, clearComponent, addComponent);
    }

    private class NameField extends CustomField<String> {
        private final TextField firstName = new TextField();
        private final TextField lastName = new TextField();

        NameField() {
            setLabel("Phone number");
            setHelperText("Your full first and last names");
            firstName.setMinLength(2);
            firstName.getStyle().set("width", "7em");
            lastName.getStyle().set("width", "7em");

            Div layout = new Div();
            layout.add(firstName, lastName);

            add(layout);
        }

        @Override
        protected String generateModelValue() {
            return firstName.getValue() + " " + lastName.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            if (newPresentationValue == null) {
                firstName.clear();
                lastName.clear();
            }
        }
    }

}
