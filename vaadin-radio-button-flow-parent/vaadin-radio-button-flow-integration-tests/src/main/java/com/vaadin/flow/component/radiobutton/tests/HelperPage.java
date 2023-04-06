
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/helper")
public class HelperPage extends Div {

    public HelperPage() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        Span span = new Span("Helper text");
        radioButtonGroup.setHelperComponent(span);

        radioButtonGroup.setItems("foo", "bar", "baz");
        Binder<Bean> binder = new Binder<>();
        binder.bind(radioButtonGroup, bean -> bean.choice,
                (bean, value) -> bean.choice = value);
        binder.setBean(new Bean());
        add(radioButtonGroup);
    }

    public static class Bean {
        private String choice;
    }
}
