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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Popover component.
 */
@Route(value = "popover", layout = MainLayout.class)
@PageTitle("Popover | Vaadin Kitchen Sink")
public class PopoverDemoView extends VerticalLayout {

    public PopoverDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Popover Component"));
        add(new Paragraph("Popover displays floating content anchored to a trigger element."));

        // Basic popover
        Button basicTarget = new Button("Click me");
        Popover basicPopover = new Popover();
        basicPopover.setTarget(basicTarget);
        basicPopover.add(new Paragraph("This is a basic popover with text content."));
        add(basicPopover);
        addSection("Basic Popover", basicTarget);

        // Popover positions
        Button topBtn = new Button("Top");
        Popover topPopover = new Popover();
        topPopover.setTarget(topBtn);
        topPopover.setPosition(PopoverPosition.TOP);
        topPopover.add(new Paragraph("Positioned at top"));
        add(topPopover);

        Button bottomBtn = new Button("Bottom");
        Popover bottomPopover = new Popover();
        bottomPopover.setTarget(bottomBtn);
        bottomPopover.setPosition(PopoverPosition.BOTTOM);
        bottomPopover.add(new Paragraph("Positioned at bottom"));
        add(bottomPopover);

        Button startBtn = new Button("Start");
        Popover startPopover = new Popover();
        startPopover.setTarget(startBtn);
        startPopover.setPosition(PopoverPosition.START);
        startPopover.add(new Paragraph("Positioned at start"));
        add(startPopover);

        Button endBtn = new Button("End");
        Popover endPopover = new Popover();
        endPopover.setTarget(endBtn);
        endPopover.setPosition(PopoverPosition.END);
        endPopover.add(new Paragraph("Positioned at end"));
        add(endPopover);

        addSection("Position Variants", topBtn, bottomBtn, startBtn, endBtn);

        // Rich content popover
        Button richTarget = new Button("User Profile");
        Popover richPopover = new Popover();
        richPopover.setTarget(richTarget);

        VerticalLayout richContent = new VerticalLayout();
        richContent.setPadding(false);
        richContent.add(new H3("John Doe"));
        richContent.add(new Paragraph("john.doe@example.com"));
        richContent.add(new Paragraph("Software Engineer"));
        richContent.add(new Button("View Profile"));
        richPopover.add(richContent);
        add(richPopover);
        addSection("Rich Content", richTarget);

        // Form in popover
        Button formTarget = new Button("Quick Add");
        Popover formPopover = new Popover();
        formPopover.setTarget(formTarget);

        VerticalLayout formContent = new VerticalLayout();
        formContent.setPadding(false);
        formContent.add(new H3("Add Item"));
        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        TextField descField = new TextField("Description");
        descField.setWidthFull();
        Button saveBtn = new Button("Save", e -> formPopover.close());
        formContent.add(nameField, descField, saveBtn);
        formPopover.add(formContent);
        add(formPopover);
        addSection("Form in Popover", formTarget);

        // Modal popover
        Button modalTarget = new Button("Modal Popover");
        Popover modalPopover = new Popover();
        modalPopover.setTarget(modalTarget);
        modalPopover.setModal(true);
        modalPopover.add(new Paragraph("This popover is modal - click outside doesn't close it."));
        modalPopover.add(new Button("Close", e -> modalPopover.close()));
        add(modalPopover);
        addSection("Modal Popover", modalTarget);

        // Non-closable
        Button stayOpenTarget = new Button("Stays Open");
        Popover stayOpenPopover = new Popover();
        stayOpenPopover.setTarget(stayOpenTarget);
        stayOpenPopover.setCloseOnOutsideClick(false);
        stayOpenPopover.add(new Paragraph("Won't close when clicking outside."));
        stayOpenPopover.add(new Button("Close manually", e -> stayOpenPopover.close()));
        add(stayOpenPopover);
        addSection("Disable Close on Outside Click", stayOpenTarget);

        // With width
        Button widthTarget = new Button("Wide Popover");
        Popover widthPopover = new Popover();
        widthPopover.setTarget(widthTarget);
        widthPopover.setWidth("400px");
        widthPopover.add(new Paragraph("This popover has a fixed width of 400px, allowing for more content to be displayed comfortably."));
        add(widthPopover);
        addSection("Custom Width", widthTarget);
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
