/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link NumberField}.
 */
@Route("vaadin-text-field/number-field-test")
public class NumberFieldPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public NumberFieldPage() {
        Div message = new Div();
        message.setId("message");
        NumberField numberField = new NumberField();
        numberField.addValueChangeListener(logValueChangeListener(message));
        add(numberField, message);

        NativeButton button = new NativeButton(
                "Set/unset text field read-only");
        button.setId("read-only");
        button.addClickListener(
                event -> numberField.setReadOnly(!numberField.isReadOnly()));
        add(button);

        NativeButton enabled = new NativeButton(
                "Set/unset field enabled property");
        enabled.setId("disabled");
        enabled.addClickListener(
                event -> numberField.setEnabled(!numberField.isEnabled()));
        add(enabled);

        NumberField numberFieldClear = new NumberField();
        numberFieldClear.setId("clear-number-field");
        numberFieldClear.getStyle().set("display", "block");
        numberFieldClear.setClearButtonVisible(true);
        Div clearValueMessage = new Div();
        clearValueMessage.setId("clear-message");
        numberFieldClear.addValueChangeListener(
                logValueChangeListener(clearValueMessage));
        add(numberFieldClear, clearValueMessage);

        NumberField numberFieldStep = new NumberField();
        numberFieldStep.setId("step-number-field");
        numberFieldStep.setStep(0.5);
        numberFieldStep.setMin(0);
        numberFieldStep.setMax(10);
        numberFieldStep.setStepButtonsVisible(true);
        Div stepValueMessage = new Div();
        stepValueMessage.setId("step-message");
        numberFieldStep.addValueChangeListener(
                logValueChangeListener(stepValueMessage));

        add(numberFieldStep, stepValueMessage);
    }

    private ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> logValueChangeListener(
            Div stepValueMessage) {
        return event -> stepValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue()));
    }
}
