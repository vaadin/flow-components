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

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
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

        NativeButton required = new NativeButton(
                "Set/unset field required property");
        required.setId("required");
        required.addClickListener(
                event -> numberField.setRequiredIndicatorVisible(
                        !numberField.isRequiredIndicatorVisible()));
        add(required);

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
        numberFieldStep.setHasControls(true);
        Div stepValueMessage = new Div();
        stepValueMessage.setId("step-message");
        numberFieldStep.addValueChangeListener(
                logValueChangeListener(stepValueMessage));

        add(numberFieldStep, stepValueMessage);
        Div isValid = new Div();
        isValid.setId("is-invalid");
        NativeButton checkIsValid = new NativeButton(
                "Check if current value of step-number-field is invalid");
        checkIsValid.setId("check-is-invalid");
        checkIsValid.addClickListener(event -> isValid
                .setText(numberFieldStep.isInvalid() ? "invalid" : "valid"));
        add(checkIsValid, isValid);
        addNumberFields();
    }

    private ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> logValueChangeListener(
            Div stepValueMessage) {
        return event -> stepValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue()));
    }

    private void addNumberFields() {
        NumberField dollarField = new NumberField("Dollars");
        dollarField.setPrefixComponent(new Span("$"));

        NumberField euroField = new NumberField("Euros");
        euroField.setSuffixComponent(new Span("€"));

        NumberField stepperField = new NumberField("Stepper");
        stepperField.setValue(1d);
        stepperField.setMin(0);
        stepperField.setMax(10);
        stepperField.setHasControls(true);

        euroField.setSuffixComponent(new Span("€"));

        dollarField.setId("dollar-field");
        euroField.setId("euro-field");
        stepperField.setId("step-number-field");

        add(dollarField, euroField, stepperField);
    }
}
