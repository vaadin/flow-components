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

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.GeneratedVaadinPasswordField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link GeneratedVaadinPasswordField} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-password-field")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-text-field.html")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-button.html")
public class PasswordFieldView extends DemoView {

    @Override
    public void initView() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic password field
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password field label");
        passwordField.setPlaceholder("placeholder text");
        passwordField.addValueChangeListener(event -> message.setText(
                String.format("Password field value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        NativeButton button = new NativeButton(
                "Toggle eye icon if password is hidden", event -> {
                    if (!passwordField.isPasswordVisible()) {
                        passwordField.setRevealButtonHidden(
                                !passwordField.isRevealButtonHidden());
                    }
                });
        // end-source-example

        passwordField.setId("password-field-with-value-change-listener");
        message.setId("password-field-value");
        button.setId("toggle-button");

        addCard("Basic password field", button, passwordField, message);
    }
}
