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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for TextField component.
 */
@Route(value = "text-field", layout = MainLayout.class)
@PageTitle("Text Field | Vaadin Kitchen Sink")
public class TextFieldDemoView extends VerticalLayout {

    public TextFieldDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Text Field Component"));
        add(new Paragraph("The TextField component is used for single-line text input."));

        // Basic text field
        TextField basic = new TextField("Basic Text Field");
        basic.setPlaceholder("Enter text here");
        addSection("Basic Text Field", basic);

        // With helper text
        TextField withHelper = new TextField("With Helper Text");
        withHelper.setHelperText("This is helper text providing additional context");
        addSection("With Helper Text", withHelper);

        // Required field
        TextField required = new TextField("Required Field");
        required.setRequired(true);
        required.setRequiredIndicatorVisible(true);
        addSection("Required Field", required);

        // With prefix and suffix
        TextField withPrefix = new TextField("Price");
        withPrefix.setPrefixComponent(new Span("$"));

        TextField withSuffix = new TextField("Weight");
        withSuffix.setSuffixComponent(new Span("kg"));

        TextField withIcon = new TextField("Search");
        withIcon.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        addSection("With Prefix/Suffix", withPrefix, withSuffix, withIcon);

        // Clear button
        TextField clearButton = new TextField("With Clear Button");
        clearButton.setClearButtonVisible(true);
        clearButton.setValue("Clear me!");
        addSection("Clear Button", clearButton);

        // Character counter
        TextField charCounter = new TextField("With Character Counter");
        charCounter.setMaxLength(20);
        charCounter.setHelperText("0/20");
        charCounter.addValueChangeListener(e ->
            charCounter.setHelperText(e.getValue().length() + "/20"));
        addSection("Character Counter", charCounter);

        // Pattern validation
        TextField pattern = new TextField("Phone Number");
        pattern.setPattern("[0-9]{3}-[0-9]{3}-[0-9]{4}");
        pattern.setHelperText("Format: 123-456-7890");
        pattern.setAllowedCharPattern("[0-9-]");
        addSection("Pattern Validation", pattern);

        // Theme variants
        TextField small = new TextField("Small Variant");
        small.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        TextField alignCenter = new TextField("Center Aligned");
        alignCenter.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        alignCenter.setValue("Centered");

        TextField alignRight = new TextField("Right Aligned");
        alignRight.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        alignRight.setValue("Right");
        addSection("Theme Variants", small, alignCenter, alignRight);

        // Value change modes
        Div output = new Div();
        output.setId("value-change-output");

        TextField eager = new TextField("Eager Mode");
        eager.setValueChangeMode(ValueChangeMode.EAGER);
        eager.addValueChangeListener(e ->
            output.setText("Eager value: " + e.getValue()));
        addSection("Value Change Mode (Eager)", eager, output);

        // Readonly and disabled
        TextField readonly = new TextField("Read-only Field");
        readonly.setValue("This is read-only");
        readonly.setReadOnly(true);

        TextField disabled = new TextField("Disabled Field");
        disabled.setValue("This is disabled");
        disabled.setEnabled(false);
        addSection("Read-only and Disabled", readonly, disabled);

        // Invalid state
        TextField invalid = new TextField("Invalid Field");
        invalid.setInvalid(true);
        invalid.setErrorMessage("This field has an error");
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
