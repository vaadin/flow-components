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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Checkbox} integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-test-demo")
public class CheckboxDemoPage extends Div {

    public CheckboxDemoPage() {
        addDefaultCheckbox();
        addDisabledCheckbox();
        addIndeterminateCheckbox();
        addValueChangeCheckbox();
        addCheckboxImgComponentLabel();
    }

    private void addDefaultCheckbox() {
        Checkbox checkbox = new Checkbox();
        checkbox.setLabel("Default Checkbox");
        checkbox.setId("default-checkbox");

        NativeButton button = new NativeButton("Change label", event -> {
            checkbox.setLabel("New Label");
        });
        button.setId("change-default-label");

        addCard("Default Checkbox", checkbox, button);
    }

    private void addDisabledCheckbox() {
        Div message = new Div();
        message.setId("disabled-checkbox-message");
        Checkbox disabledCheckbox = new Checkbox("Disabled Checkbox");
        disabledCheckbox.setValue(true);
        disabledCheckbox.setEnabled(false);
        disabledCheckbox.addClickListener(evt -> message.setText("Checkbox "
                + evt.getSource().getLabel()
                + " was clicked, but the component is disabled and this shouldn't happen!"));
        addCard("Disabled Checkbox", disabledCheckbox, message);
        disabledCheckbox.setId("disabled-checkbox");
    }

    private void addIndeterminateCheckbox() {
        Checkbox indeterminateCheckbox = new Checkbox("Indeterminate Checkbox");
        indeterminateCheckbox.setIndeterminate(true);

        NativeButton button = new NativeButton("Reset", event -> {
            indeterminateCheckbox.setValue(false);
            indeterminateCheckbox.setIndeterminate(true);
        });
        button.setId("reset-indeterminate");

        addCard("Indeterminate Checkbox", indeterminateCheckbox, button);
        indeterminateCheckbox.setId("indeterminate-checkbox");
    }

    private void addValueChangeCheckbox() {
        Checkbox valueChangeCheckbox = new Checkbox(
                "Checkbox with a ValueChangeListener");
        Div message = new Div();
        valueChangeCheckbox.addValueChangeListener(event -> message.setText(
                String.format("Checkbox value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        addCard("Checkbox with a ValueChangeListener", valueChangeCheckbox,
                message);
        valueChangeCheckbox.setId("value-change-checkbox");
        message.setId("value-change-checkbox-message");
    }

    private void addCheckboxImgComponentLabel() {
        Checkbox checkbox = new Checkbox();
        Image vaadinImg = new Image("https://vaadin.com/images/vaadin-logo.svg",
                "");
        checkbox.setId("img-component-label-checkbox");
        vaadinImg.setWidth("50px");
        checkbox.setLabelComponent(vaadinImg);

        NativeButton button = new NativeButton("Change label", event -> {
            Image newImage = new Image(
                    "https://vaadin.com/images/vaadin-logo.svg", "");
            newImage.setWidth("30px");
            checkbox.setLabelComponent(newImage);
        });
        button.setId("change-img-component-label");

        addCard("Checkbox with the image component label", checkbox, button);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
