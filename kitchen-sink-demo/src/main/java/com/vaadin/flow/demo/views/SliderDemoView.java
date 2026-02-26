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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Slider component.
 */
@Route(value = "slider", layout = MainLayout.class)
@PageTitle("Slider | Vaadin Kitchen Sink")
public class SliderDemoView extends VerticalLayout {

    public SliderDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Slider Component"));
        add(new Paragraph("The Slider allows users to select a value within a range."));

        // Basic slider
        Slider basic = new Slider("Volume");
        basic.setMin(0);
        basic.setMax(100);
        basic.setValue(50.0);
        basic.setWidthFull();
        basic.addValueChangeListener(e ->
            Notification.show("Value: " + e.getValue()));
        addSection("Basic Slider", basic);

        // With value display
        Span valueDisplay = new Span("50");
        Slider withDisplay = new Slider("Brightness");
        withDisplay.setMin(0);
        withDisplay.setMax(100);
        withDisplay.setValue(50.0);
        withDisplay.setWidthFull();
        withDisplay.addValueChangeListener(e ->
            valueDisplay.setText(String.valueOf(e.getValue().intValue())));
        addSection("With Value Display", withDisplay, valueDisplay);

        // Custom range
        Slider customRange = new Slider("Temperature (10-30)");
        customRange.setMin(10);
        customRange.setMax(30);
        customRange.setValue(20.0);
        customRange.setWidthFull();
        addSection("Custom Range", customRange);

        // With step
        Slider withStep = new Slider("Rating (1-5)");
        withStep.setMin(1);
        withStep.setMax(5);
        withStep.setStep(1);
        withStep.setValue(3.0);
        withStep.setWidthFull();
        addSection("With Step", withStep);

        // Decimal step
        Slider decimalStep = new Slider("Percentage");
        decimalStep.setMin(0);
        decimalStep.setMax(1);
        decimalStep.setStep(0.1);
        decimalStep.setValue(0.5);
        decimalStep.setWidthFull();
        addSection("Decimal Step (0.1)", decimalStep);

        // With helper text
        Slider withHelper = new Slider("Font Size");
        withHelper.setMin(8);
        withHelper.setMax(72);
        withHelper.setValue(16.0);
        withHelper.setWidthFull();
        withHelper.setHelperText("Select font size between 8 and 72 points");
        addSection("With Helper Text", withHelper);

        // Read-only
        Slider readonly = new Slider("Read-only");
        readonly.setMin(0);
        readonly.setMax(100);
        readonly.setValue(75.0);
        readonly.setReadOnly(true);
        readonly.setWidthFull();
        addSection("Read-only", readonly);

        // Disabled
        Slider disabled = new Slider("Disabled");
        disabled.setMin(0);
        disabled.setMax(100);
        disabled.setValue(25.0);
        disabled.setEnabled(false);
        disabled.setWidthFull();
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
