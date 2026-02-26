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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for EmailField component.
 */
@Route(value = "email-field", layout = MainLayout.class)
@PageTitle("Email Field | Vaadin Kitchen Sink")
public class EmailFieldDemoView extends VerticalLayout {

    public EmailFieldDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Email Field Component"));
        add(new Paragraph("The EmailField provides built-in email validation."));

        // Basic email field
        EmailField basic = new EmailField("Email Address");
        basic.setPlaceholder("user@example.com");
        addSection("Basic Email Field", basic);

        // With icon
        EmailField withIcon = new EmailField("Email with Icon");
        withIcon.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        withIcon.setPlaceholder("Enter your email");
        addSection("With Icon", withIcon);

        // Required field
        EmailField required = new EmailField("Required Email");
        required.setRequired(true);
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // With helper text
        EmailField withHelper = new EmailField("Work Email");
        withHelper.setHelperText("Please use your company email address");
        withHelper.setPlaceholder("name@company.com");
        addSection("With Helper Text", withHelper);

        // Clear button
        EmailField clearButton = new EmailField("With Clear Button");
        clearButton.setClearButtonVisible(true);
        clearButton.setValue("user@example.com");
        addSection("Clear Button", clearButton);

        // Pattern validation
        EmailField pattern = new EmailField("Company Email Only");
        pattern.setPattern(".+@company\\.com");
        pattern.setHelperText("Must be a @company.com email");
        pattern.setErrorMessage("Please use your company email");
        addSection("Pattern Validation", pattern);

        // Read-only
        EmailField readonly = new EmailField("Read-only Email");
        readonly.setValue("readonly@example.com");
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        EmailField disabled = new EmailField("Disabled Email");
        disabled.setValue("disabled@example.com");
        disabled.setEnabled(false);
        addSection("Disabled", disabled);

        // Invalid state
        EmailField invalid = new EmailField("Invalid Email");
        invalid.setValue("invalid-email");
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please enter a valid email address");
        addSection("Invalid State", invalid);
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
