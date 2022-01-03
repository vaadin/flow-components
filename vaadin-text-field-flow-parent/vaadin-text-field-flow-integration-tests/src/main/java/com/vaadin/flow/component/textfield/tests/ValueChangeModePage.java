/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.textfield.tests;

import java.util.Arrays;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

/**
 * Test view for changing the ValueChangMode of TextField, TextArea and
 * PasswordField.
 */
@Route("vaadin-text-field/value-change-mode-test")
public class ValueChangeModePage extends Div {

    private int counter = 0;
    private Div message;

    public ValueChangeModePage() {
        initView();
    }

    private void initView() {
        message = new Div();
        message.setId("message");
        add(message);

        Stream.of(new TextField(), new TextArea(), new PasswordField(),
                new NumberField(), new EmailField(), new IntegerField(),
                new BigDecimalField()).forEach(this::setupTestComponent);
    }

    private void setupTestComponent(Component field) {
        addField((AbstractField<?, ?>) field);
        addButtons((HasValueChangeMode) field);
    }

    private void addField(AbstractField<?, ?> field) {
        getElement().appendChild((new Element("hr")));
        field.addValueChangeListener(
                event -> message.setText("" + (counter++)));
        add(field);
    }

    private void addButtons(HasValueChangeMode component) {
        getElement().appendChild((new Element("br")));
        String name = component.getClass().getSimpleName();
        Arrays.stream(ValueChangeMode.values()).forEach(mode -> {
            NativeButton button = new NativeButton(
                    String.format("Set the value change mode of %s to %s", name,
                            mode.name()));
            button.addClickListener(
                    event -> component.setValueChangeMode(mode));
            button.setId(String.format("%s-%s", name, mode.name()).toLowerCase()
                    .replace('_', '-'));
            add(button);
        });
        TextField timeoutField = new TextField();
        timeoutField.addValueChangeListener(event -> component
                .setValueChangeTimeout(new Integer(event.getValue())));
        timeoutField.setId(name.toLowerCase() + "-set-change-timeout");
        add(timeoutField);
    }

}
