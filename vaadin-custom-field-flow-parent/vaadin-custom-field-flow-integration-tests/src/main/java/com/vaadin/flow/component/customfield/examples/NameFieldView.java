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
}
