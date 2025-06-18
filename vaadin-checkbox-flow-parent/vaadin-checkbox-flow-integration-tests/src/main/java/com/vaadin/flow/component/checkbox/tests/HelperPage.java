/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import java.util.Set;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
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
        add(checkboxGroup);
    }

    public static class Bean {
        private Set<String> choices;
    }
}
