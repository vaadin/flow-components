package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("helper-text")
public class HelperTextPage extends Div {

    public HelperTextPage() {
        ArrayList<String> items = new ArrayList<>();
        items.add("foo");

        ComboBox<String> helperTextComboBox = new ComboBox<>();
        helperTextComboBox.setItems(items);
        helperTextComboBox.setHelperText("Helper text");
        helperTextComboBox.setId("combobox-helper-text");

        NativeButton emptyHelperText = new NativeButton("Clear helper text",
              e -> helperTextComboBox.setHelperText(""));
        emptyHelperText.setId("empty-helper-text");

        ComboBox<String> helperComponentCombobox = new ComboBox<>();
        helperComponentCombobox.setItems(items);
        helperComponentCombobox.setId("combobox-helper-component");

        Span span = new Span("Helper Component");
        span.setId("helper-component");
        helperComponentCombobox.setHelperComponent(span);

        NativeButton emptyHelperComponent = new NativeButton(
              "Clear helper component",
              e -> helperComponentCombobox.setHelperComponent(null));
        emptyHelperComponent.setId("empty-helper-component");

        add(helperTextComboBox, helperComponentCombobox, emptyHelperText,
              emptyHelperComponent);
    }
}
