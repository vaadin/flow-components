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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for RadioButtonGroup component.
 */
@Route(value = "radio-button", layout = MainLayout.class)
@PageTitle("Radio Button | Vaadin Kitchen Sink")
public class RadioButtonDemoView extends VerticalLayout {

    public RadioButtonDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Radio Button Group Component"));
        add(new Paragraph("Radio buttons allow users to select exactly one option from a group."));

        // Basic radio group
        RadioButtonGroup<String> basic = new RadioButtonGroup<>();
        basic.setLabel("Select a size");
        basic.setItems("Small", "Medium", "Large");
        basic.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Basic Radio Group", basic);

        // Pre-selected value
        RadioButtonGroup<String> preSelected = new RadioButtonGroup<>();
        preSelected.setLabel("Select a color");
        preSelected.setItems("Red", "Green", "Blue");
        preSelected.setValue("Green");
        addSection("Pre-selected Value", preSelected);

        // Vertical orientation
        RadioButtonGroup<String> vertical = new RadioButtonGroup<>();
        vertical.setLabel("Select payment method");
        vertical.setItems("Credit Card", "PayPal", "Bank Transfer", "Cash on Delivery");
        vertical.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        addSection("Vertical Orientation", vertical);

        // With helper text
        RadioButtonGroup<String> withHelper = new RadioButtonGroup<>();
        withHelper.setLabel("Shipping method");
        withHelper.setItems("Standard (5-7 days)", "Express (2-3 days)", "Overnight");
        withHelper.setHelperText("Additional charges may apply for express shipping");
        addSection("With Helper Text", withHelper);

        // Required
        RadioButtonGroup<String> required = new RadioButtonGroup<>();
        required.setLabel("Select priority");
        required.setItems("Low", "Normal", "High", "Critical");
        required.setRequired(true);
        required.setRequiredIndicatorVisible(true);
        addSection("Required Selection", required);

        // Disabled items
        RadioButtonGroup<String> partiallyDisabled = new RadioButtonGroup<>();
        partiallyDisabled.setLabel("Select plan");
        partiallyDisabled.setItems("Free", "Basic", "Premium (sold out)", "Enterprise");
        partiallyDisabled.setItemEnabledProvider(item -> !item.contains("sold out"));
        addSection("Partially Disabled Items", partiallyDisabled);

        // Read-only
        RadioButtonGroup<String> readonly = new RadioButtonGroup<>();
        readonly.setLabel("Selected option (read-only)");
        readonly.setItems("Option A", "Option B", "Option C");
        readonly.setValue("Option B");
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled group
        RadioButtonGroup<String> disabled = new RadioButtonGroup<>();
        disabled.setLabel("Disabled group");
        disabled.setItems("Option 1", "Option 2", "Option 3");
        disabled.setValue("Option 1");
        disabled.setEnabled(false);
        addSection("Disabled Group", disabled);

        // Invalid state
        RadioButtonGroup<String> invalid = new RadioButtonGroup<>();
        invalid.setLabel("Invalid selection");
        invalid.setItems("Yes", "No", "Maybe");
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please make a selection");
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
