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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for NumberField and IntegerField components.
 */
@Route(value = "number-field", layout = MainLayout.class)
@PageTitle("Number Field | Vaadin Kitchen Sink")
public class NumberFieldDemoView extends VerticalLayout {

    public NumberFieldDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Number Field Components"));
        add(new Paragraph("Number fields are used for numeric input."));

        // Basic number field
        NumberField basic = new NumberField("Basic Number Field");
        basic.setPlaceholder("Enter a number");
        addSection("Basic Number Field", basic);

        // With step controls
        NumberField withControls = new NumberField("With Step Controls");
        withControls.setStepButtonsVisible(true);
        withControls.setStep(0.5);
        withControls.setValue(5.0);
        addSection("With Step Controls", withControls);

        // Min/Max values
        NumberField minMax = new NumberField("Min/Max Range (0-100)");
        minMax.setMin(0);
        minMax.setMax(100);
        minMax.setStepButtonsVisible(true);
        minMax.setHelperText("Value must be between 0 and 100");
        addSection("Min/Max Values", minMax);

        // With prefix/suffix
        NumberField price = new NumberField("Price");
        price.setPrefixComponent(new Span("$"));
        price.setValue(99.99);

        NumberField percentage = new NumberField("Percentage");
        percentage.setSuffixComponent(new Span("%"));
        percentage.setMin(0);
        percentage.setMax(100);
        percentage.setValue(50.0);
        addSection("With Prefix/Suffix", price, percentage);

        // Integer field
        IntegerField intField = new IntegerField("Integer Field");
        intField.setStepButtonsVisible(true);
        intField.setStep(1);
        intField.setValue(10);
        addSection("Integer Field", intField);

        // Integer field with min/max
        IntegerField quantity = new IntegerField("Quantity (1-99)");
        quantity.setMin(1);
        quantity.setMax(99);
        quantity.setStepButtonsVisible(true);
        quantity.setValue(1);
        quantity.setHelperText("Select quantity");
        addSection("Integer Field with Range", quantity);

        // Clear button
        NumberField clearButton = new NumberField("With Clear Button");
        clearButton.setClearButtonVisible(true);
        clearButton.setValue(42.0);
        addSection("Clear Button", clearButton);

        // Read-only
        NumberField readonly = new NumberField("Read-only Field");
        readonly.setValue(123.45);
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        NumberField disabled = new NumberField("Disabled Field");
        disabled.setValue(100.0);
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
