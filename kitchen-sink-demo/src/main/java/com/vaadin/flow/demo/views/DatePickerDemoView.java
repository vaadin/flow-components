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

import java.time.LocalDate;
import java.util.Locale;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for DatePicker component.
 */
@Route(value = "date-picker", layout = MainLayout.class)
@PageTitle("Date Picker | Vaadin Kitchen Sink")
public class DatePickerDemoView extends VerticalLayout {

    public DatePickerDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Date Picker Component"));
        add(new Paragraph("The DatePicker allows users to select a date from a calendar."));

        // Basic date picker
        DatePicker basic = new DatePicker("Select a date");
        basic.setPlaceholder("Pick a date");
        basic.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Basic Date Picker", basic);

        // Pre-selected value
        DatePicker preSelected = new DatePicker("Today's date");
        preSelected.setValue(LocalDate.now());
        addSection("Pre-selected Value", preSelected);

        // With clear button
        DatePicker clearable = new DatePicker("Clearable");
        clearable.setValue(LocalDate.now());
        clearable.setClearButtonVisible(true);
        addSection("With Clear Button", clearable);

        // Min and max dates
        DatePicker minMax = new DatePicker("Date range");
        minMax.setMin(LocalDate.now());
        minMax.setMax(LocalDate.now().plusMonths(3));
        minMax.setHelperText("Select a date within the next 3 months");
        addSection("Min/Max Date Range", minMax);

        // With helper text
        DatePicker withHelper = new DatePicker("Appointment date");
        withHelper.setHelperText("Select your preferred appointment date");
        addSection("With Helper Text", withHelper);

        // Initial position
        DatePicker initialPosition = new DatePicker("Start from next month");
        initialPosition.setInitialPosition(LocalDate.now().plusMonths(1));
        addSection("Initial Position", initialPosition);

        // Required
        DatePicker required = new DatePicker("Required date");
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // Small variant
        DatePicker small = new DatePicker("Small variant");
        small.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        addSection("Small Variant", small);

        // With locale
        DatePicker german = new DatePicker("German locale");
        german.setLocale(Locale.GERMANY);
        german.setValue(LocalDate.now());
        addSection("German Locale", german);

        // Auto-open disabled
        DatePicker noAutoOpen = new DatePicker("Auto-open disabled");
        noAutoOpen.setAutoOpen(false);
        noAutoOpen.setHelperText("Calendar won't open on focus");
        addSection("Auto-open Disabled", noAutoOpen);

        // Read-only
        DatePicker readonly = new DatePicker("Read-only");
        readonly.setValue(LocalDate.now());
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        DatePicker disabled = new DatePicker("Disabled");
        disabled.setValue(LocalDate.now());
        disabled.setEnabled(false);
        addSection("Disabled", disabled);

        // Invalid state
        DatePicker invalid = new DatePicker("Invalid");
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please select a valid date");
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
