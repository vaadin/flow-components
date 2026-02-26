/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for PasswordField component.
 */
@Route(value = "password-field", layout = MainLayout.class)
@PageTitle("Password Field | Vaadin Kitchen Sink")
public class PasswordFieldDemoView extends VerticalLayout {

    public PasswordFieldDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Password Field Component"));
        add(new Paragraph("The PasswordField is used for password input with masking."));

        // Basic password field
        PasswordField basic = new PasswordField("Password");
        basic.setPlaceholder("Enter your password");
        addSection("Basic Password Field", basic);

        // With reveal button
        PasswordField withReveal = new PasswordField("With Reveal Button");
        withReveal.setRevealButtonVisible(true);
        withReveal.setHelperText("Click the eye icon to reveal password");
        addSection("With Reveal Button", withReveal);

        // Required field
        PasswordField required = new PasswordField("Required Password");
        required.setRequired(true);
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // With validation
        PasswordField validation = new PasswordField("Password with Validation");
        validation.setMinLength(8);
        validation.setHelperText("Minimum 8 characters");
        validation.setPattern("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        validation.addValueChangeListener(e -> {
            if (e.getValue().length() < 8) {
                validation.setInvalid(true);
                validation.setErrorMessage("Password must be at least 8 characters");
            } else {
                validation.setInvalid(false);
            }
        });
        addSection("With Validation", validation);

        // Clear button
        PasswordField clearButton = new PasswordField("With Clear Button");
        clearButton.setClearButtonVisible(true);
        clearButton.setValue("secret123");
        addSection("Clear Button", clearButton);

        // Read-only (though unusual for passwords)
        PasswordField readonly = new PasswordField("Read-only Password");
        readonly.setValue("readonly-password");
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        PasswordField disabled = new PasswordField("Disabled Password");
        disabled.setValue("disabled");
        disabled.setEnabled(false);
        addSection("Disabled", disabled);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
