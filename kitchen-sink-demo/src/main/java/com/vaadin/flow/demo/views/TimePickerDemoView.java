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

import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.TimePickerVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for TimePicker component.
 */
@Route(value = "time-picker", layout = MainLayout.class)
@PageTitle("Time Picker | Vaadin Kitchen Sink")
public class TimePickerDemoView extends VerticalLayout {

    public TimePickerDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Time Picker Component"));
        add(new Paragraph("The TimePicker allows users to select a time value."));

        // Basic time picker
        TimePicker basic = new TimePicker("Select a time");
        basic.setPlaceholder("Pick a time");
        basic.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Basic Time Picker", basic);

        // Pre-selected value
        TimePicker preSelected = new TimePicker("Current time");
        preSelected.setValue(LocalTime.now());
        addSection("Pre-selected Value", preSelected);

        // With clear button
        TimePicker clearable = new TimePicker("Clearable");
        clearable.setValue(LocalTime.of(14, 30));
        clearable.setClearButtonVisible(true);
        addSection("With Clear Button", clearable);

        // Custom step
        TimePicker step15 = new TimePicker("15-minute intervals");
        step15.setStep(Duration.ofMinutes(15));
        addSection("15-Minute Step", step15);

        TimePicker step30 = new TimePicker("30-minute intervals");
        step30.setStep(Duration.ofMinutes(30));
        addSection("30-Minute Step", step30);

        // Min and max time
        TimePicker minMax = new TimePicker("Business hours");
        minMax.setMin(LocalTime.of(9, 0));
        minMax.setMax(LocalTime.of(17, 0));
        minMax.setHelperText("Select a time between 9 AM and 5 PM");
        addSection("Min/Max Time Range", minMax);

        // With helper text
        TimePicker withHelper = new TimePicker("Appointment time");
        withHelper.setHelperText("Select your preferred time");
        addSection("With Helper Text", withHelper);

        // Required
        TimePicker required = new TimePicker("Required time");
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // Small variant
        TimePicker small = new TimePicker("Small variant");
        small.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        addSection("Small Variant", small);

        // Auto-open disabled
        TimePicker noAutoOpen = new TimePicker("Auto-open disabled");
        noAutoOpen.setAutoOpen(false);
        noAutoOpen.setHelperText("Dropdown won't open on focus");
        addSection("Auto-open Disabled", noAutoOpen);

        // Read-only
        TimePicker readonly = new TimePicker("Read-only");
        readonly.setValue(LocalTime.of(10, 30));
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        TimePicker disabled = new TimePicker("Disabled");
        disabled.setValue(LocalTime.of(12, 0));
        disabled.setEnabled(false);
        addSection("Disabled", disabled);

        // Invalid state
        TimePicker invalid = new TimePicker("Invalid");
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please select a valid time");
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
