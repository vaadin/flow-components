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
package com.vaadin.flow.component.textfield.demo;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextArea;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link GeneratedVaadinTextArea} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-text-area")
public class TextAreaView extends DemoView {

    @Override
    public void initView() {
        addBasicFeatures();
        addClearButtonFeature();
        addMaxHeightFeature();
        addMinHeightFeature();
        addDisabledField();
        addVariantsFeature();
        addFocusShortcut();
    }

    private void addFocusShortcut() {
        // begin-source-example
        // source-example-heading: Focus shortcut usage
        TextArea textArea = new TextArea();
        textArea.setLabel("Press ALT + 1 to focus");
        textArea.addFocusShortcut(Key.DIGIT_1, KeyModifier.ALT);
        // end-source-example

        textArea.setId("shortcut-field");
        this.addCard("Focus shortcut usage", textArea);
    }

    private void addVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        // end-source-example

        addVariantsDemo(TextArea::new,
                GeneratedVaadinTextArea::addThemeVariants,
                GeneratedVaadinTextArea::removeThemeVariants,
                TextAreaVariant::getVariantName, TextAreaVariant.LUMO_SMALL);
    }

    private void addMaxHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with max-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area growing stops at 125px");
        textArea.getStyle().set("maxHeight", "125px");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-max-height");

        addCard("Text area with max-height", textArea, message);
    }

    private void addMinHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with min-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area won't shrink under 125px");
        textArea.getStyle().set("minHeight", "125px");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-min-height");

        addCard("Text area with min-height", textArea, message);
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic text area
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.addValueChangeListener(event -> message.setText(
                String.format("Text area value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        textArea.setId("text-area-with-value-change-listener");
        message.setId("text-area-value");

        addCard("Basic text area", textArea,
                new ValueChangeModeButtonProvider(textArea)
                        .getValueChangeModeRadios(),
                message);
    }

    private void addClearButtonFeature() {
        // begin-source-example
        // source-example-heading: Text area with clear button
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        NativeButton clearButton = new NativeButton("Toggle clear button", event -> {
            textArea.setClearButtonVisible(
                    !textArea.isClearButtonVisible());
        });
        // end-source-example

        addCard("Text area with clear button", textArea, clearButton);
    }

    private void addDisabledField() {
        // begin-source-example
        // source-example-heading: Disabled text area
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.setEnabled(false);
        // end-source-example

        textArea.setId("disabled-text-area");
        Div message = new Div();
        message.setId("disabled-text-area-message");
        textArea.addValueChangeListener(
                change -> message.setText("Value changed"));

        addCard("Disabled text area", textArea, message);
    }
}
