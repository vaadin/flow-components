/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import java.util.Set;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/helper")
public class HelperPage extends Div {

    public HelperPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        Span span = new Span("Helper text");
        checkboxGroup.setHelperComponent(span);

        checkboxGroup.setItems("foo", "bar", "baz");
        Binder<Bean> binder = new Binder<>();
        binder.bind(checkboxGroup, bean -> bean.choices,
                (bean, value) -> bean.choices = value);
        binder.setBean(new Bean());

        CheckboxGroup<String> groupWithHelperGenerator = new CheckboxGroup<>();
        groupWithHelperGenerator.setItems("A", "B", "C");
        groupWithHelperGenerator
                .setItemHelperGenerator(item -> item + " helper");
        groupWithHelperGenerator.setId("cbg-helper-generator");

        NativeButton clearItemHelperGenerator = new NativeButton(
                "Clear helper generator", e -> groupWithHelperGenerator
                        .setItemHelperGenerator(item -> null));
        clearItemHelperGenerator.setId("clear-helper-generator");

        Checkbox helperTextCheckbox = new Checkbox("Using helper text");
        helperTextCheckbox.setHelperText("Helper text");
        helperTextCheckbox.setId("checkbox-helper-text");

        NativeButton emptyHelperText = new NativeButton("Clear helper text",
                e -> helperTextCheckbox.setHelperText(""));
        emptyHelperText.setId("empty-helper-text");

        Checkbox helperComponentCheckbox = new Checkbox(
                "Using helper component");
        helperComponentCheckbox.setId("checkbox-helper-component");

        Span helper = new Span("Helper component");
        helper.setId("helper-component");
        helperComponentCheckbox.setHelperComponent(helper);

        NativeButton emptyHelperComponent = new NativeButton(
                "Clear helper component",
                e -> helperComponentCheckbox.setHelperComponent(null));
        emptyHelperComponent.setId("empty-helper-component");

        add(checkboxGroup, groupWithHelperGenerator, clearItemHelperGenerator,
                helperTextCheckbox, helperComponentCheckbox, emptyHelperText,
                emptyHelperComponent);
    }

    public static class Bean {
        private Set<String> choices;
    }
}
