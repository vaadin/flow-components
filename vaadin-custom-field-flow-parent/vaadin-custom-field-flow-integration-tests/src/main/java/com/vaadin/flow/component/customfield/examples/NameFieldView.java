package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("custom-field-helper")
public class NameFieldView extends Div {

    public NameFieldView() {

        NameField fieldHelper = new NameField();
        fieldHelper.setHelperText("Helper text");
        fieldHelper.setId("custom-field-helper-text");

        NativeButton clearText = new NativeButton("Clear helper", e -> {
            fieldHelper.setHelperText(null);
        });
        clearText.setId("button-clear-helper");

        NameField fieldHelperComponent = new NameField();
        fieldHelperComponent.setId("custom-field-helper-component");

        NativeButton clearComponent = new NativeButton("Clear helper component",
                e -> {
                    fieldHelperComponent.setHelperComponent(null);
                });
        clearComponent.setId("button-clear-helper-component");

        NativeButton addComponent = new NativeButton("Add helper component",
                e -> {
                    Span span = new Span("Helper component");
                    span.getElement().setAttribute("name", "helper-component");
                    span.setId("helper-component");
                    fieldHelperComponent.setHelperComponent(span);
                });
        addComponent.setId("button-add-helper-component");

        add(fieldHelper, clearText, fieldHelperComponent, clearComponent,
                addComponent);
    }
}
