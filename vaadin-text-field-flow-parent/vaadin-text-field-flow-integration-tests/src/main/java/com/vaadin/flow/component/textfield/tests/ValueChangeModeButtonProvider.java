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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

import static com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_ALIGN_RIGHT;
import static com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL;

public class ValueChangeModeButtonProvider {
    private final HasValueChangeMode elementWithChangeMode;

    public ValueChangeModeButtonProvider(
            HasValueChangeMode elementWithChangeMode) {
        this.elementWithChangeMode = elementWithChangeMode;
    }

    private Component getTimeoutInput() {
        TextField field = new TextField("Value change timeout");
        field.setSuffixComponent(new Span("msec"));
        field.setTitle("ValueChangeTimeout");
        field.setPattern("[0-9]*");
        field.setMaxLength(4);
        field.setPreventInvalidInput(true);
        field.addThemeVariants(LUMO_ALIGN_RIGHT, LUMO_SMALL);
        field.setValue(elementWithChangeMode.getValueChangeTimeout() + "");
        field.addValueChangeListener(this::onTimeoutChange);
        return field;
    }

    private void onTimeoutChange(HasValue.ValueChangeEvent<String> event) {
        try {
            elementWithChangeMode
                    .setValueChangeTimeout(new Integer(event.getValue()));
        } catch (NumberFormatException e) {
            event.getHasValue().setValue(
                    elementWithChangeMode.getValueChangeTimeout() + "");
        }
    }

    public Component getValueChangeModeRadios() {
        Div container = new Div();
        RadioButtonGroup<ValueChangeMode> group = new RadioButtonGroup<>();
        group.setItems(ValueChangeMode.values());
        group.setValue(elementWithChangeMode.getValueChangeMode());
        group.addValueChangeListener(event -> elementWithChangeMode
                .setValueChangeMode(event.getValue()));
        container.add(group, getTimeoutInput());
        return container;
    }
}
