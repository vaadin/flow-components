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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

/**
 * Demo view for ComboBox component.
 */
@Route(value = "combo-box", layout = MainLayout.class)
@PageTitle("Combo Box | Vaadin Kitchen Sink")
public class ComboBoxDemoView extends VerticalLayout {

    private static final List<String> COUNTRIES = Arrays.asList(
        "United States", "Canada", "Mexico", "United Kingdom", "Germany",
        "France", "Spain", "Italy", "Japan", "China", "Australia", "Brazil"
    );

    public ComboBoxDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Combo Box Component"));
        add(new Paragraph("The ComboBox allows filtering and selecting from a list of options."));

        // Basic combo box
        ComboBox<String> basic = new ComboBox<>("Country");
        basic.setItems(COUNTRIES);
        basic.setPlaceholder("Select or search");
        basic.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Basic Combo Box", basic);

        // Pre-selected value
        ComboBox<String> preSelected = new ComboBox<>("Default country");
        preSelected.setItems(COUNTRIES);
        preSelected.setValue("United States");
        addSection("Pre-selected Value", preSelected);

        // With clear button
        ComboBox<String> clearable = new ComboBox<>("Clearable");
        clearable.setItems(COUNTRIES);
        clearable.setClearButtonVisible(true);
        clearable.setValue("Germany");
        addSection("With Clear Button", clearable);

        // With helper text
        ComboBox<String> withHelper = new ComboBox<>("Shipping destination");
        withHelper.setItems(COUNTRIES);
        withHelper.setHelperText("Type to filter countries");
        addSection("With Helper Text", withHelper);

        // Custom filtering
        ComboBox<String> customFilter = new ComboBox<>("Custom filtering");
        customFilter.setItems(COUNTRIES);
        customFilter.setItemLabelGenerator(String::toUpperCase);
        addSection("Custom Label Generator", customFilter);

        // Allow custom value
        ComboBox<String> allowCustom = new ComboBox<>("Allow custom value");
        allowCustom.setItems(COUNTRIES);
        allowCustom.setAllowCustomValue(true);
        allowCustom.addCustomValueSetListener(e -> {
            String customValue = e.getDetail();
            Notification.show("Custom value: " + customValue);
        });
        allowCustom.setHelperText("You can enter a custom country name");
        addSection("Allow Custom Value", allowCustom);

        // Required
        ComboBox<String> required = new ComboBox<>("Required selection");
        required.setItems(COUNTRIES);
        required.setRequired(true);
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // Small variant
        ComboBox<String> small = new ComboBox<>("Small variant");
        small.setItems(COUNTRIES);
        small.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        addSection("Small Variant", small);

        // Read-only
        ComboBox<String> readonly = new ComboBox<>("Read-only");
        readonly.setItems(COUNTRIES);
        readonly.setValue("France");
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        ComboBox<String> disabled = new ComboBox<>("Disabled");
        disabled.setItems(COUNTRIES);
        disabled.setValue("Italy");
        disabled.setEnabled(false);
        addSection("Disabled", disabled);

        // Invalid state
        ComboBox<String> invalid = new ComboBox<>("Invalid");
        invalid.setItems(COUNTRIES);
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please select a valid country");
        addSection("Invalid State", invalid);

        // Auto-open disabled
        ComboBox<String> noAutoOpen = new ComboBox<>("Auto-open disabled");
        noAutoOpen.setItems(COUNTRIES);
        noAutoOpen.setAutoOpen(false);
        noAutoOpen.setHelperText("Dropdown won't open on focus");
        addSection("Auto-open Disabled", noAutoOpen);
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
