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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for TextArea component.
 */
@Route(value = "text-area", layout = MainLayout.class)
@PageTitle("Text Area | Vaadin Kitchen Sink")
public class TextAreaDemoView extends VerticalLayout {

    public TextAreaDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Text Area Component"));
        add(new Paragraph("The TextArea component is used for multi-line text input."));

        // Basic text area
        TextArea basic = new TextArea("Basic Text Area");
        basic.setPlaceholder("Enter multiple lines of text here");
        basic.setWidthFull();
        addSection("Basic Text Area", basic);

        // With helper text
        TextArea withHelper = new TextArea("Description");
        withHelper.setHelperText("Provide a detailed description");
        withHelper.setWidthFull();
        addSection("With Helper Text", withHelper);

        // Min/Max height
        TextArea autoHeight = new TextArea("Auto-resize Text Area");
        autoHeight.setMinHeight("100px");
        autoHeight.setMaxHeight("300px");
        autoHeight.setWidthFull();
        autoHeight.setPlaceholder("This area grows as you type, up to a maximum height");
        addSection("Auto-resize with Min/Max Height", autoHeight);

        // Character counter
        TextArea charCounter = new TextArea("With Character Limit");
        charCounter.setMaxLength(200);
        charCounter.setHelperText("0/200 characters");
        charCounter.setWidthFull();
        charCounter.addValueChangeListener(e ->
            charCounter.setHelperText(e.getValue().length() + "/200 characters"));
        addSection("Character Counter", charCounter);

        // Small variant
        TextArea small = new TextArea("Small Variant");
        small.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        small.setWidthFull();
        addSection("Small Variant", small);

        // Read-only
        TextArea readonly = new TextArea("Read-only Text Area");
        readonly.setValue("This content is read-only and cannot be edited by the user.\n\n" +
            "Multi-line content is displayed here.");
        readonly.setReadOnly(true);
        readonly.setWidthFull();
        addSection("Read-only", readonly);

        // Disabled
        TextArea disabled = new TextArea("Disabled Text Area");
        disabled.setValue("This text area is disabled");
        disabled.setEnabled(false);
        disabled.setWidthFull();
        addSection("Disabled", disabled);

        // Invalid state
        TextArea invalid = new TextArea("Invalid Text Area");
        invalid.setInvalid(true);
        invalid.setErrorMessage("Please enter a valid description");
        invalid.setWidthFull();
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
