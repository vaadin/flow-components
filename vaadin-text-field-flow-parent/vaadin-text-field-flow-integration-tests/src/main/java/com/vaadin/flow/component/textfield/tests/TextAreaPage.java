/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link TextArea}.
 */
@Route("text-area-test")
public class TextAreaPage extends Div {

    /**
     * Constructs a basic layout with a text area.
     */
    public TextAreaPage() {
        TextArea textAreaClear = new TextArea();
        textAreaClear.setId("clear-text-area");
        textAreaClear.getStyle().set("display", "block");
        textAreaClear.setClearButtonVisible(true);
        Div clearValueMessage = new Div();
        clearValueMessage.setId("clear-message");
        textAreaClear.addValueChangeListener(event -> clearValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(textAreaClear, clearValueMessage);
        addFocusShortcut();
        addDisabledField();
        addBasicFeatures();
        addMaxHeightFeature();
        addMinHeightFeature();
        addInvalidCheck();
        addHelperCheck();
    }

    private void addFocusShortcut() {
        TextArea textArea = new TextArea();
        textArea.setLabel("Press ALT + 1 to focus");
        textArea.addFocusShortcut(Key.DIGIT_1, KeyModifier.ALT);
        textArea.setId("shortcut-field");
        add(textArea);
    }

    private void addDisabledField() {
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.setEnabled(false);
        textArea.setId("disabled-text-area");
        Div message = new Div();
        message.setId("disabled-text-area-message");
        textArea.addValueChangeListener(
                change -> message.setText("Value changed"));
        add(textArea, message);
    }

    private void addBasicFeatures() {
        Div message = new Div();
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.addValueChangeListener(event -> message.setText(
                String.format("Text area value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        textArea.setId("text-area-with-value-change-listener");
        message.setId("text-area-value");
        add(textArea, new ValueChangeModeButtonProvider(textArea)
                .getValueChangeModeRadios(), message);
    }

    private void addMaxHeightFeature() {
        Div message = new Div();
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area growing stops at 125px");
        textArea.getStyle().set("maxHeight", "125px");
        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-max-height");
        add(textArea, message);
    }

    private void addMinHeightFeature() {
        Div message = new Div();
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area won't shrink under 125px");
        textArea.getStyle().set("minHeight", "125px");
        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-min-height");
        add(textArea, message);
    }

    private void addInvalidCheck() {
        final TextArea field = new TextArea();
        field.setMaxLength(10);
        field.setMinLength(5);
        TextFieldTestPageUtil.addInvalidCheck(this, field);
    }

    private void addHelperCheck() {
        TextArea textAreaHelperText = new TextArea();
        textAreaHelperText.setHelperText("Helper text test");

        TextArea textAreaHelperComponent = new TextArea();
        textAreaHelperComponent
                .setHelperComponent(VaadinIcon.INFO_CIRCLE_O.create());

        textAreaHelperText.setId("text-area-helper-text");
        textAreaHelperComponent.setId("text-area-helper-component");

        add(textAreaHelperText, textAreaHelperComponent);
    }

}
