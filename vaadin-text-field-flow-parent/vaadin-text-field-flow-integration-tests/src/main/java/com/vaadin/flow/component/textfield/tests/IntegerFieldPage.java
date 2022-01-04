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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link IntegerField}.
 */
@Route("vaadin-text-field/integer-field-test")
public class IntegerFieldPage extends Div {

    private Div messageContainer;

    public IntegerFieldPage() {
        messageContainer = new Div();
        messageContainer.setId("messages");

        IntegerField integerField = new IntegerField();
        integerField.addValueChangeListener(this::logValueChangeEvent);
        add(integerField);

        NativeButton toggleReadOnly = new NativeButton("Toggle read-only");
        toggleReadOnly.setId("toggle-read-only");
        toggleReadOnly.addClickListener(
                event -> integerField.setReadOnly(!integerField.isReadOnly()));
        add(toggleReadOnly);

        NativeButton required = new NativeButton("Toggle required");
        required.setId("toggle-required");
        required.addClickListener(
                event -> integerField.setRequiredIndicatorVisible(
                        !integerField.isRequiredIndicatorVisible()));
        add(required);

        NativeButton toggleEnabled = new NativeButton("Toggle enabled");
        toggleEnabled.setId("toggle-enabled");
        toggleEnabled.addClickListener(
                event -> integerField.setEnabled(!integerField.isEnabled()));
        add(toggleEnabled);

        IntegerField integerFieldClear = new IntegerField();
        integerFieldClear.setId("clear-integer-field");
        integerFieldClear.getStyle().set("display", "block");
        integerFieldClear.setClearButtonVisible(true);
        integerFieldClear.addValueChangeListener(this::logValueChangeEvent);
        add(integerFieldClear);

        IntegerField integerFieldStep = new IntegerField();
        integerFieldStep.setId("step-integer-field");
        integerFieldStep.setStep(3);
        integerFieldStep.setMin(4);
        integerFieldStep.setMax(10);
        integerFieldStep.setHasControls(true);
        integerFieldStep.addValueChangeListener(this::logValueChangeEvent);

        add(integerFieldStep);

        Div isValid = new Div();
        isValid.setId("is-invalid");
        NativeButton checkIsValid = new NativeButton(
                "Check if current value of step-integer-field is invalid");
        checkIsValid.setId("check-is-invalid");
        checkIsValid.addClickListener(event -> isValid
                .setText(integerFieldStep.isInvalid() ? "invalid" : "valid"));
        add(checkIsValid, isValid);

        add(messageContainer);
    }

    private void logValueChangeEvent(
            ComponentValueChangeEvent<IntegerField, Integer> event) {
        String text = String.format("Old value: '%s'. New value: '%s'.",
                event.getOldValue(), event.getValue());
        Paragraph paragraph = new Paragraph(text);
        messageContainer.add(paragraph);
    }

}
